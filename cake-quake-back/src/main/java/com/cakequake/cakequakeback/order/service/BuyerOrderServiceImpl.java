package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.badge.constants.BadgeConstants;
import com.cakequake.cakequakeback.badge.entities.Badge;
import com.cakequake.cakequakeback.badge.entities.MemberBadge;
import com.cakequake.cakequakeback.badge.repo.BadgeRepository;
import com.cakequake.cakequakeback.badge.repo.MemberBadgeRepository;
import com.cakequake.cakequakeback.badge.service.BadgeService;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import com.cakequake.cakequakeback.cart.repo.CartItemRepository;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.notification.entities.NotificationType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.notification.service.NotificationService;
import com.cakequake.cakequakeback.notification.service.PickupReminderSchedulingService;
import com.cakequake.cakequakeback.order.dto.buyer.CreateOrder;
import com.cakequake.cakequakeback.order.dto.buyer.OrderDetail;
import com.cakequake.cakequakeback.order.dto.buyer.OrderList;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.*;
import com.cakequake.cakequakeback.point.entities.Point;
import com.cakequake.cakequakeback.point.repo.PointRepo;
import com.cakequake.cakequakeback.point.service.PointService;
import com.cakequake.cakequakeback.schedule.service.ShopScheduleService;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BuyerOrderServiceImpl implements BuyerOrderService {
    private final BuyerOrderRepository buyerOrderRepository;
    private final CakeOrderItemRepository cakeOrderItemRepository;
    private final CakeOptionMappingRepository cakeOptionMappingRepository;
    private final CakeOrderItemOptionRepository cakeOrderItemOptionRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;
    private final CakeItemRepository cakeItemRepository;
    private final PointRepo pointRepository;
    private final PointService pointService;
    private final CartItemRepository cartItemRepository;

    private final ShopScheduleService shopScheduleService;

    private final BadgeService badgeService;
    private final NotificationService notificationService;


    @Override
    public CreateOrder.Response createOrder(String userId, CreateOrder.Request request) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        // ⭐ Point 엔티티 조회 (유효성 검사를 위해 여전히 필요) ⭐
        Point memberPoint = pointRepository.findByMemberUid(member.getUid())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POINT_HISTORY, "해당 회원의 포인트 정보를 찾을 수 없습니다."));


        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));


        boolean hasCart = request.getCartItemIds() != null && !request.getCartItemIds().isEmpty();
        boolean hasDirect = request.getDirectItems() != null && !request.getDirectItems().isEmpty();

        if (hasCart == hasDirect) {
            throw new BusinessException(ErrorCode.INVALID_CART_ITEMS, "주문은 장바구니 또는 바로구매 중 한 가지 방식만 가능합니다.");
        }

        long calculatedTotalPrice = 0L; // 할인 전 총 주문 금액
        int totalItemCount = 0; // 총 상품 수량
        List<CakeOrderItem> tempOrderItems = new ArrayList<>(); // CakeOrderItem 저장을 위한 임시 리스트

        List<CartItem> cartItemsFromRepo = null;
        if (hasCart) {
            cartItemsFromRepo = cartItemRepository.findAllById(request.getCartItemIds());

            if (cartItemsFromRepo.isEmpty() || cartItemsFromRepo.size() != request.getCartItemIds().size()) {
                throw new BusinessException(ErrorCode.NOT_FOUND_CAKE_ITEM, "유효하지 않거나 찾을 수 없는 장바구니 아이템이 포함되어 있습니다.");
            }
        }


        if (hasDirect) { // 바로구매 상품 처리
            for (CreateOrder.DirectItem directItem : request.getDirectItems()) {
                int quantity = directItem.getQuantity();
                if (quantity <= 0) {
                    throw new BusinessException(ErrorCode.INVALID_QUANTITY, "바로구매 아이템 수량은 1개 이상이어야 합니다.");
                }
                totalItemCount += quantity;

                CakeItem cakeItem = cakeItemRepository.findById(directItem.getCakeItemId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CAKE_ITEM, "케이크 상품을 찾을 수 없습니다: " + directItem.getCakeItemId()));

                int itemUnitPrice = cakeItem.getPrice();
                int itemSubTotal = itemUnitPrice * quantity; // 케이크 아이템 기본 가격 * 수량

                // 옵션 처리 및 가격 합산
                Map<Long, Integer> optionsMap = directItem.getOptions();
                if (optionsMap != null && !optionsMap.isEmpty()) {
                    for (Map.Entry<Long, Integer> entry : optionsMap.entrySet()) {
                        Long mappingId = entry.getKey();
                        Integer optionQuantity = entry.getValue();

                        CakeOptionMapping mapping = cakeOptionMappingRepository.findById(mappingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID, "케이크 옵션 매핑을 찾을 수 없습니다: " + mappingId));

                        itemSubTotal += mapping.getOptionItem().getPrice() * optionQuantity; // 옵션 가격 * 수량
                    }
                }
                calculatedTotalPrice += itemSubTotal; // 모든 아이템의 기본 가격 + 옵션 가격 합산

                // CakeOrderItem 생성 (아직 CakeOrder 참조는 null, 나중에 저장된 order와 연결)
                CakeOrderItem item = CakeOrderItem.builder()
                        .quantity(quantity)
                        .unitPrice(itemUnitPrice) // 단가는 CakeItem의 기본 가격
                        .subTotalPrice(itemSubTotal) // 케이크 기본 가격 + 옵션 가격
                        .cakeItem(cakeItem)
                        .build();
                tempOrderItems.add(item);
            }
        } else if (hasCart) { // 장바구니 상품 처리
            for (CartItem cartItem : cartItemsFromRepo) { //cartItemsFromRepo 변수 사용
                int quantity = cartItem.getQuantity();
                if (quantity <= 0) {
                    throw new BusinessException(ErrorCode.INVALID_QUANTITY, "장바구니 아이템 수량은 1개 이상이어야 합니다.");
                }
                totalItemCount += quantity;

                CakeItem cakeItem = cartItem.getCakeItem();
                if (cakeItem == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_CAKE_ITEM, "장바구니 아이템에 연결된 케이크 상품을 찾을 수 없습니다.");
                }

                int itemUnitPrice = cartItem.getUnitPrice();
                int itemSubTotal = itemUnitPrice * quantity; // 케이크 아이템 기본 가격 * 수량

                // 옵션 처리 및 가격 합산
                Map<Long, Integer> optionsMap = cartItem.getOptions();
                if (optionsMap != null && !optionsMap.isEmpty()) {
                    for (Map.Entry<Long, Integer> entry : optionsMap.entrySet()) {
                        Long mappingId = entry.getKey();
                        Integer optionQuantity = entry.getValue();

                        CakeOptionMapping mapping = cakeOptionMappingRepository.findById(mappingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID, "장바구니 아이템 옵션 매핑을 찾을 수 없습니다: " + mappingId));

                        itemSubTotal += mapping.getOptionItem().getPrice() * optionQuantity; // 옵션 가격 * 수량
                    }
                }
                calculatedTotalPrice += itemSubTotal; // 모든 아이템의 기본 가격 + 옵션 가격 합산

                // CakeOrderItem 생성 (아직 CakeOrder 참조는 null, 나중에 저장된 order와 연결)
                CakeOrderItem item = CakeOrderItem.builder()
                        .quantity(quantity)
                        .unitPrice(itemUnitPrice) // 단가는 CartItem의 UnitPrice 또는 CakeItem의 가격
                        .subTotalPrice(itemSubTotal) // 케이크 기본 가격 + 옵션 가격
                        .cakeItem(cakeItem)
                        .build();
                tempOrderItems.add(item);
            }
        } else {
            throw new BusinessException(ErrorCode.INVALID_CART_ITEMS, "주문할 상품 정보가 없습니다. (cartItemIds 또는 directItems 중 하나가 제공되어야 합니다.)");
        }

        // 포인트 사용 로직 시작
        Integer usedPoints = request.getUsedPoints() != null ? request.getUsedPoints() : 0;

        if (usedPoints < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_VALUE, "사용할 포인트는 음수일 수 없습니다.");
        }
        if (usedPoints > memberPoint.getTotalPoints().intValue()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINTS, String.format("보유 포인트(%d)를 초과하여 포인트(%d)를 사용할 수 없습니다.", memberPoint.getTotalPoints(), usedPoints));
        }
        if (usedPoints > calculatedTotalPrice) {
            throw new BusinessException(ErrorCode.INVALID_POINT_VALUE, String.format("주문 금액(%d)보다 많은 포인트(%d)를 사용할 수 없습니다.", (int)calculatedTotalPrice, usedPoints));
        }

        Integer finalPaymentAmount = (int) calculatedTotalPrice - usedPoints;
        if (finalPaymentAmount < 0) {
            finalPaymentAmount = 0; // 최종 결제 금액은 최소 0원
        }

        // ⭐⭐⭐ 픽업 스케줄 검증 및 슬롯 감소 로직 시작 ⭐⭐⭐
        try {
            shopScheduleService.decreaseSlotsForOrderCreation(
                    request.getShopId(),
                    request.getPickupDate(),
                    request.getPickupTime()
            );
        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.PICKUP_SLOT_UNAVAILABLE, "선택하신 픽업 시간은 예약이 마감되었거나 유효하지 않습니다.");
        }
        // ⭐⭐⭐ 픽업 스케줄 검증 및 슬롯 감소 로직 끝 ⭐⭐⭐

        // CakeOrder 엔티티 생성
        CakeOrder order = CakeOrder.builder()
                .member(member) // 주문자 설정
                .shop(shop)     // 매장 설정
                .orderNumber(generateOrderNumber(member.getUserId())) // 주문 번호 생성
                .orderNote(request.getOrderNote())
                .totalNumber(totalItemCount) // 총 상품 수량
                .orderTotalPrice((int) calculatedTotalPrice) // 할인 전 총 금액
                .discountAmount(usedPoints) // 사용된 포인트 (할인액)
                .finalPaymentAmount(finalPaymentAmount) // 최종 결제 금액
                .pickupDate(request.getPickupDate())
                .pickupTime(request.getPickupTime())
                .status(OrderStatus.RESERVATION_PENDING) // 초기 주문 상태
                .build();
        // 포인트 사용 로직 끝


        // 주문 저장
        CakeOrder savedOrder = buyerOrderRepository.save(order);

        // 판매자에게 "새로운 주문" 알림 전송
        try {
            Long sellerUid = savedOrder.getShop().getMember().getUid();
            notificationService.sendNotification(
                    sellerUid,
                    "새로운 주문이 접수되었습니다! 주문 번호: " + savedOrder.getOrderNumber(),
                    savedOrder.getOrderId(),
                    NotificationType.NEW_ORDER
            );
        } catch (Exception e) {
            System.err.println("새 주문 알림 전송 실패: " + e.getMessage());
        }

        // CakeOrderItem 및 CakeOrderItemOption 저장
        for (CakeOrderItem item : tempOrderItems) {
            CakeOrderItem finalItem = CakeOrderItem.builder()
                    .cakeItem(item.getCakeItem())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subTotalPrice(item.getSubTotalPrice())
                    .cakeOrder(savedOrder)
                    .build();
            CakeOrderItem savedOrderItem = cakeOrderItemRepository.save(finalItem);

            // 주문된 케이크 아이템의 주문 수 증가
            CakeItem orderedCakeItem = savedOrderItem.getCakeItem(); // 주문 항목에 연결된 CakeItem 가져오기
            orderedCakeItem.incrementOrderCount(); // CakeItem 엔티티의 ordersCount 증가 메서드 호출
            cakeItemRepository.save(orderedCakeItem); // 변경된 CakeItem 엔티티 저장

            Map<Long, Integer> optionsMap = null;
            if (hasDirect) {
                CreateOrder.DirectItem directItemRequest = request.getDirectItems().stream()
                        .filter(di -> di.getCakeItemId().equals(item.getCakeItem().getCakeId()))
                        .findFirst().orElse(null);
                if(directItemRequest != null) optionsMap = directItemRequest.getOptions();
            } else if (hasCart) {
                CartItem cartItem = cartItemsFromRepo.stream()
                        .filter(ci -> ci.getCakeItem().getCakeId().equals(item.getCakeItem().getCakeId()))
                        .findFirst().orElse(null);
                if(cartItem != null) optionsMap = cartItem.getOptions();
            }

            if (optionsMap != null && !optionsMap.isEmpty()) {
                for (Map.Entry<Long, Integer> entry : optionsMap.entrySet()) {
                    Long mappingId = entry.getKey();
                    Integer optionQuantity = entry.getValue();

                    CakeOptionMapping mapping = cakeOptionMappingRepository.findById(mappingId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID, "케이크 옵션 매핑을 찾을 수 없습니다: " + mappingId));

                    cakeOrderItemOptionRepository.save(CakeOrderItemOption.builder()
                            .cakeOrderItem(savedOrderItem)
                            .cakeOptionMapping(mapping)
                            .optionCnt(optionQuantity)
                            .build());
                }
            }
        }

        // 장바구니 아이템 삭제
        if (hasCart) {
            cartItemRepository.deleteAllById(request.getCartItemIds());
        }

        // 사용자 포인트 차감 (changePoint 메서드 사용)
        if (usedPoints > 0) {
            pointService.changePoint(member.getUid(), -usedPoints.longValue(), "주문 결제 할인"); // 여기 -를 뺌, 포인트 반환 로직에서만 +로
        }
        // 사용자 포인트 차감 끝

        return CreateOrder.Response.builder()
                .orderId(savedOrder.getOrderId())
                .orderNumber(savedOrder.getOrderNumber())
                .orderTotalPrice(savedOrder.getOrderTotalPrice())
                .pickupDate(savedOrder.getPickupDate())
                .pickupTime(savedOrder.getPickupTime())
                .orderNote(savedOrder.getOrderNote())
                .shopId(savedOrder.getShop().getShopId())
                .build();
    }

    private String generateOrderNumber(String userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = (int) (Math.random() * 100000);
        return "ORD-" + date + "-" + userId + "-" + String.format("%05d", random);
    }

    @Override
    public OrderList.Response getOrderList(String userId, Pageable pageable) {
        Page<CakeOrder> page = buyerOrderRepository.findByMemberUserId(userId, pageable);

        // 1. 페이지의 모든 CakeOrder ID를 추출
        List<Long> orderIdsInPage = page.getContent().stream()
                .map(CakeOrder::getOrderId)
                .collect(Collectors.toList());

        // 2. 페이지 내 모든 CakeOrder에 해당하는 모든 CakeOrderItem 목록을 한 번에 조회 (CakeItem 정보 포함)
        List<CakeOrderItem> allOrderItemsInPage = new ArrayList<>();
        if (!orderIdsInPage.isEmpty()) {
            // CakeOrderItemRepository에 findByCakeOrder_OrderIdInWithCakeItem 메서드 추가 필요
            // @Query("SELECT coi FROM CakeOrderItem coi JOIN FETCH coi.cakeItem WHERE coi.cakeOrder.orderId IN :orderIds")
            allOrderItemsInPage = cakeOrderItemRepository.findByCakeOrder_OrderIdInWithCakeItem(orderIdsInPage);
        }

        // 3. 모든 CakeOrderItem의 ID를 추출하여, 모든 CakeOrderItemOption을 한 번에 조회 (핵심 N+1 해결)
        List<Long> allOrderItemIdsInPage = allOrderItemsInPage.stream()
                .map(CakeOrderItem::getOrderItemId)
                .collect(Collectors.toList());
        List<CakeOrderItemOption> allItemOptionsInPage = new ArrayList<>();
        if (!allOrderItemIdsInPage.isEmpty()) {
            allItemOptionsInPage = cakeOrderItemOptionRepository.findByCakeOrderItem_OrderItemIdIn(allOrderItemIdsInPage);
        }

        // 4. 조회된 데이터들을 Map으로 그룹화하여 헬퍼 메서드에 전달할 준비
        Map<Long, List<CakeOrderItem>> orderItemsByOrderId = allOrderItemsInPage.stream()
                .collect(Collectors.groupingBy(item -> item.getCakeOrder().getOrderId()));
        Map<Long, List<CakeOrderItemOption>> optionsByOrderItemId = allItemOptionsInPage.stream()
                .collect(Collectors.groupingBy(option -> option.getCakeOrderItem().getOrderItemId()));

        // 5. OrderList.OrderListItem DTO로 변환
        List<OrderList.OrderListItem> items = page.getContent().stream()
                .map(order -> {
                    List<CakeOrderItem> currentOrderItems = orderItemsByOrderId.getOrDefault(order.getOrderId(), Collections.emptyList());
                    return mapToOrderListItem(order, currentOrderItems, optionsByOrderItemId); // 헬퍼 메서드 호출 시 데이터 전달
                })
                .collect(Collectors.toList());

        OrderList.PageInfo pageInfo = OrderList.PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return OrderList.Response.builder()
                .orders(items)
                .pageInfo(pageInfo)
                .build();
    }

    @Override
    public OrderDetail.Response getOrderDetail(String userId, Long orderId) {
        /// 1. CakeOrder 정보 조회: Member와 Shop을 FETCH JOIN으로 함께 가져와 N+1 방지
        // buyerOrderRepository에 findByOrderIdAndMemberUserIdWithMemberAndShop 메서드를 추가했다고 가정합니다.
        CakeOrder order = buyerOrderRepository
                .findByOrderIdAndMemberUserIdWithMemberAndShop(orderId, userId) // 수정: 새로운 Repository 메서드 사용
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID, "해당 주문 정보를 찾을 수 없습니다."));

        // 소유권 검증 (Member 정보는 이미 FETCH JOIN으로 가져왔으므로 추가 쿼리 없음)
        if (!Objects.equals(order.getMember().getUserId(), userId)) {
            throw new BusinessException(ErrorCode.NOT_OWN_ORDER, "주문 번호가 본인의 것이 아닙니다.");
        }

        // 2. 모든 CakeOrderItem 목록 조회: CakeItem 정보도 함께 FETCH JOIN으로 가져와 N+1 방지
        // 수정: cakeOrderItemRepository의 findByCakeOrder_OrderIdWithCakeItem 메서드 사용
        List<CakeOrderItem> orderItems = cakeOrderItemRepository.findByCakeOrder_OrderId(orderId);

        // 3. 모든 CakeOrderItem의 ID를 추출
        List<Long> orderItemIds = orderItems.stream()
                .map(CakeOrderItem::getOrderItemId)
                .collect(Collectors.toList());

        // 4. 추출된 ID를 이용해 모든 CakeOrderItemOption을 한 번에 조회 (핵심 N+1 해결)
        List<CakeOrderItemOption> allItemOptions = new ArrayList<>();
        if (!orderItemIds.isEmpty()) {
            // 수정: CakeOrderItemOptionRepository의 findByCakeOrderItem_OrderItemIdIn 메서드 사용 (OptionItem까지 JOIN FETCH)
            allItemOptions = cakeOrderItemOptionRepository.findByCakeOrderItem_OrderItemIdIn(orderItemIds);
        }

        // 5. 조회된 옵션들을 orderItemId를 기준으로 그룹화하여 Map으로 준비
        Map<Long, List<CakeOrderItemOption>> optionsByOrderItemId = allItemOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getCakeOrderItem().getOrderItemId()));

        // 6. OrderDetailItem DTO 리스트 생성 및 각 OrderDetailItem에 옵션 정보 추가
        List<OrderDetail.OrderDetailItem> itemDtos = orderItems.stream()
                .map(item -> {
                    // 해당 orderItem에 속하는 옵션 리스트 가져오기
                    List<CakeOrderItemOption> itemOptions = optionsByOrderItemId.getOrDefault(item.getOrderItemId(), Collections.emptyList());

                    // OrderDetail.OrderDetailItem DTO의 options 필드는 List<String> 형태
                    // 옵션 이름을 포함한 문자열로 포맷팅합니다.
                    // ⭐ 이 부분을 수정합니다. ⭐
                    List<CreateOrder.SelectedOptionDetail> selectedOptionDetails = itemOptions.stream()
                            .map(oio -> CreateOrder.SelectedOptionDetail.builder()
                                    .mappingId(oio.getCakeOptionMapping().getMappingId())
                                    .optionName(oio.getCakeOptionMapping().getOptionItem().getOptionName())
                                    .price(oio.getCakeOptionMapping().getOptionItem().getPrice())
                                    .count(oio.getOptionCnt())
                                    .optionType(oio.getCakeOptionMapping().getOptionItem().getOptionType() != null ?
                                            oio.getCakeOptionMapping().getOptionItem().getOptionType().getOptionType() : null) // OptionType에 getType 이름을 가져오는 메서드가 있다고 가정
                                    .build())
                            .collect(Collectors.toList());

                    return OrderDetail.OrderDetailItem.builder()
                            .orderItemId(item.getOrderItemId())
                            .cakeId(item.getCakeItem().getCakeId())
                            .cname(item.getCakeItem().getCname())
                            .productCnt(item.getQuantity())
                            .price(item.getUnitPrice().longValue())
                            .thumbnailImageUrl(item.getCakeItem().getThumbnailImageUrl())
                            .selectedOptions(selectedOptionDetails)
                            .itemSubTotalPrice(item.getSubTotalPrice().longValue()) // 이 줄 추가
                            .build();
                })
                .collect(Collectors.toList());

        // 7. 최종 OrderDetail.Response DTO 생성 및 반환
        return OrderDetail.Response.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus().name())
                .orderNumber(order.getOrderNumber())
                .reservedAt(LocalDateTime
                        .of(order.getPickupDate(), order.getPickupTime())
                        .toString())
                .uname(order.getMember().getUname()) // Member 정보는 이미 FETCH JOIN으로 가져옴
                .phone(order.getMember().getPhoneNumber()) // Member 정보는 이미 FETCH JOIN으로 가져옴
                .shopId(order.getShop().getShopId()) // Shop 정보는 이미 FETCH JOIN으로 가져옴
                .items(itemDtos)
                .totalPrice(order.getOrderTotalPrice().longValue())
                .orderNote(order.getOrderNote()) //
                .discountAmount(order.getDiscountAmount()) //
                .finalPaymentAmount(order.getFinalPaymentAmount()) //
                .build();
    }

    @Override
    public void cancelOrder(String userId, Long orderId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID, "해당 회원을 찾을 수 없습니다."));

        CakeOrder order = buyerOrderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID, "해 해당 주문 정보를 찾을 수 없습니다."));

        if (!Objects.equals(order.getMember().getUserId().trim(), userId.trim())) { // .trim() 추가
            throw new BusinessException(ErrorCode.NOT_OWN_ORDER, "주문 번호가 본인의 것이 아닙니다.");
        }

        // 이 조건문을 수정하여 여러 상태에서 취소 가능하도록 변경
        List<OrderStatus> cancellableStatuses = Arrays.asList(
                OrderStatus.RESERVATION_PENDING,
                OrderStatus.RESERVATION_CONFIRMED, // 추가: 예약 확정 상태에서도 취소 가능
                OrderStatus.PREPARING // 추가: 준비 중 상태에서도 취소 가능
        );

        if (!cancellableStatuses.contains(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_MISMATCH,
                    String.format("현재 주문 상태 (%s) 에서는 취소할 수 없습니다.", order.getStatus().getKr())); // 한글명 사용
        }

        order.updateStatus(OrderStatus.RESERVATION_CANCELLED);
        buyerOrderRepository.save(order);

        // 구매자가 주문 취소 시 판매자 알림
        try {
            Shop shop = order.getShop();
            if (shop != null && shop.getMember() != null) {
                Long sellerUid = shop.getMember().getUid();

                String orderNumber = order.getOrderNumber() != null ? order.getOrderNumber() : "N/A";

                String messageContent = String.format("주문이 취소되었습니다. 주문 번호 %s", orderNumber);

                // 판매자에게 알림 전송
                notificationService.sendNotification(
                        sellerUid,
                        messageContent,
                        order.getOrderId(),
                        NotificationType.ORDER_CANCELLED_BY_BUYER
                );
                System.out.println("DEBUG: 구매자 주문 취소로 판매자에게 알림 전송 완료: 주문 ID " + order.getOrderId() + ", 판매자 UID: " + sellerUid);
            } else {
                System.err.println("DEBUG: 주문 ID " + order.getOrderId() + "에 연결된 가게 또는 판매자 정보가 NULL입니다. 구매자 취소 알림을 보낼 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("DEBUG: 구매자 주문 취소 알림 전송 실패: 주문 ID " + order.getOrderId() + ", 에러: " + e.getMessage());
            e.printStackTrace();
        }

        // ⭐⭐⭐ 포인트 반환 로직 추가 ⭐⭐⭐
        Integer usedPoints = order.getDiscountAmount(); // 주문 시 사용된 포인트 (할인액)
        if (usedPoints != null && usedPoints > 0) {
            // PointService를 사용하여 포인트 반환 (적립)
            pointService.changePoint(member.getUid(), usedPoints.longValue(), "주문 취소로 인한 포인트 반환");
        }
        // ⭐⭐⭐ 포인트 반환 로직 끝 ⭐⭐⭐

        // 주문 취소 시 뱃지 조건 재검사 (선택 사항, 필요하다면 활성화)
         if (member != null) {
             badgeService.checkAndAcquireBadges(member.getUid());
             System.out.println("DEBUG: 주문 취소로 인해 회원 UID " + member.getUid() + "의 뱃지 조건 재검사 완료.");
        }
    }


    //특정 구매자(userId)의 최신 주문 N개 조회
    @Override
    public List<OrderList.OrderListItem> getLatestBuyerOrders(String userId) {
        Pageable latest3Pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "regDate"));

        Page<CakeOrder> page = buyerOrderRepository.findByMemberUserIdOrderByRegDateDesc(userId, latest3Pageable); // ⭐ Repository 메서드 호출

        // 1. 페이지의 모든 CakeOrder ID를 추출
        List<Long> orderIdsInPage = page.getContent().stream()
                .map(CakeOrder::getOrderId)
                .collect(Collectors.toList());

        // 2. 페이지 내 모든 CakeOrder에 해당하는 모든 CakeOrderItem 목록을 한 번에 조회 (CakeItem 정보 포함)
        List<CakeOrderItem> allOrderItemsInPage = new ArrayList<>();
        if (!orderIdsInPage.isEmpty()) {
            allOrderItemsInPage = cakeOrderItemRepository.findByCakeOrder_OrderIdInWithCakeItem(orderIdsInPage);
        }

        // 3. 모든 CakeOrderItem의 ID를 추출하여, 모든 CakeOrderItemOption을 한 번에 조회 (핵심 N+1 해결)
        List<Long> allOrderItemIdsInPage = allOrderItemsInPage.stream()
                .map(CakeOrderItem::getOrderItemId)
                .collect(Collectors.toList());
        List<CakeOrderItemOption> allItemOptionsInPage = new ArrayList<>();
        if (!allOrderItemIdsInPage.isEmpty()) {
            allItemOptionsInPage = cakeOrderItemOptionRepository.findByCakeOrderItem_OrderItemIdIn(allOrderItemIdsInPage);
        }

        // 4. 조회된 데이터들을 Map으로 그룹화하여 헬퍼 메서드에 전달할 준비
        Map<Long, List<CakeOrderItem>> orderItemsByOrderId = allOrderItemsInPage.stream()
                .collect(Collectors.groupingBy(item -> item.getCakeOrder().getOrderId()));
        Map<Long, List<CakeOrderItemOption>> optionsByOrderItemId = allItemOptionsInPage.stream()
                .collect(Collectors.groupingBy(option -> option.getCakeOrderItem().getOrderItemId()));


        // CakeOrderItem 정보를 가져와 DTO로 변환합니다. (mapToOrderListItem 재활용)
        List<OrderList.OrderListItem> dtoItems = page.getContent().stream()
                // ⭐ 이 부분을 수정합니다: 람다식으로 필요한 인자들을 명시적으로 전달 ⭐
                .map(order -> {
                    List<CakeOrderItem> currentOrderItems = orderItemsByOrderId.getOrDefault(order.getOrderId(), Collections.emptyList());
                    return mapToOrderListItem(order, currentOrderItems, optionsByOrderItemId);
                })
                .collect(Collectors.toList());

        return dtoItems; // 최신 3개 리스트만 반환
    }

    // mapToOrderListItem 헬퍼 메서드
    private OrderList.OrderListItem mapToOrderListItem(
            CakeOrder order,
            List<CakeOrderItem> orderItemsForThisOrder, // 해당 주문의 OrderItem 목록
            Map<Long, List<CakeOrderItemOption>> optionsByOrderItemId // 모든 OrderItemOption 맵
    ) {
        // shopName이 null이 될 수 있으므로, null 체크를 통해 안전하게 처리
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName()
                : "";

        // OrderList.OrderItemOption DTO 리스트 생성
        List<OrderList.OrderItemOption> itemOptions = orderItemsForThisOrder.stream()
                .map(item -> mapToOrderItemOption(item, optionsByOrderItemId.getOrDefault(item.getOrderItemId(), Collections.emptyList()))) // 옵션 데이터 전달
                .collect(Collectors.toList());

        return OrderList.OrderListItem.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .shopName(shopName)
                .orderTotalPrice(order.getOrderTotalPrice())
                .status(order.getStatus().name())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .items(itemOptions) // 수정: 옵션 목록 설정
                .discountAmount(order.getDiscountAmount()) //
                .finalPaymentAmount(order.getFinalPaymentAmount()) //
                .build();
    }

    // mapToOrderItemOption 헬퍼 메서드
    private OrderList.OrderItemOption mapToOrderItemOption(
            CakeOrderItem cakeOrderItem,
            List<CakeOrderItemOption> itemOptionsForThisOrderItem // 해당 OrderItem의 옵션 목록
    ) {
        String cname = cakeOrderItem.getCakeItem().getCname();
        String thumbnail = cakeOrderItem.getCakeItem().getThumbnailImageUrl();
        Long price = cakeOrderItem.getUnitPrice() != null ? cakeOrderItem.getUnitPrice().longValue() : 0L;
        Integer count = cakeOrderItem.getQuantity();

        // ⭐ 수정: Map<String, String> 대신 List<CreateOrder.SelectedOptionDetail> 생성 ⭐
        List<CreateOrder.SelectedOptionDetail> selectedOptionDetails = itemOptionsForThisOrderItem.stream()
                .map(oio -> CreateOrder.SelectedOptionDetail.builder()
                        .mappingId(oio.getCakeOptionMapping().getMappingId())
                        .optionName(oio.getCakeOptionMapping().getOptionItem().getOptionName())
                        .price(oio.getCakeOptionMapping().getOptionItem().getPrice())
                        .count(oio.getOptionCnt())
                        .optionType(oio.getCakeOptionMapping().getOptionItem().getOptionType() != null ?
                                oio.getCakeOptionMapping().getOptionItem().getOptionType().getOptionType() : null)
                        .build())
                .collect(Collectors.toList());

        return OrderList.OrderItemOption.builder()
                .cname(cname)
                .thumbnailImageUrl(thumbnail)
                .price(price)
                .productCnt(count)
                .selectedOptions(selectedOptionDetails) // 수정된 필드명으로 변경
                .itemSubTotalPrice(cakeOrderItem.getSubTotalPrice().longValue())
                .build();
    }
}