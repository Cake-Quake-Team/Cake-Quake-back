package com.cakequake.cakequakeback.order.service;

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
            pointService.changePoint(member.getUid(), -usedPoints.longValue(), "주문 결제 할인");
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

        List<OrderList.OrderListItem> items = page.getContent().stream()
                .map(this::mapToOrderListItem)
                .collect(Collectors.toList());

        OrderList.PageInfo pageInfo = OrderList.PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return OrderList.Response.builder()
                .orders(items)
                .pageInfo(pageInfo).build();
    }

    @Override
    public OrderDetail.Response getOrderDetail(String userId, Long orderId) {
        CakeOrder order = buyerOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID, "해당 주문 정보를 찾을 수 없습니다."));

        if (!Objects.equals(order.getMember().getUserId(), userId)) {
            throw new BusinessException(ErrorCode.NOT_OWN_ORDER, "주문 번호가 본인의 것이 아닙니다.");
        }

        List<CakeOrderItem> items = cakeOrderItemRepository.findByCakeOrder_OrderId(orderId);

        List<OrderDetail.OrderDetailItem> itemDtos = items.stream()
                .map(item -> OrderDetail.OrderDetailItem.builder()
                        .orderItemId(item.getOrderItemId())
                        .cakeId(item.getCakeItem().getCakeId())
                        .cname(item.getCakeItem().getCname())
                        .productCnt(item.getQuantity())
                        .price(item.getUnitPrice().longValue())
                        .thumbnailImageUrl(item.getCakeItem().getThumbnailImageUrl())
                        .build()
                )
                .collect(Collectors.toList());

        return OrderDetail.Response.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus().name())
                .orderNumber(order.getOrderNumber())
                .reservedAt(LocalDateTime
                        .of(order.getPickupDate(), order.getPickupTime())
                        .toString())
                .uname(order.getMember().getUname())
                .phone(order.getMember().getPhoneNumber())
                .shopId(order.getShop().getShopId())
                .items(itemDtos)
                .totalPrice(order.getOrderTotalPrice().longValue())
                .orderNote(order.getOrderNote()) //orderNote 추가
                .discountAmount(order.getDiscountAmount()) // 추가
                .finalPaymentAmount(order.getFinalPaymentAmount()) // 추가
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
    }


    //특정 구매자(userId)의 최신 주문 N개 조회
    @Override
    public List<OrderList.OrderListItem> getLatestBuyerOrders(String userId) {
        Pageable latest3Pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "regDate"));

        Page<CakeOrder> page = buyerOrderRepository.findByMemberUserIdOrderByRegDateDesc(userId, latest3Pageable); // ⭐ Repository 메서드 호출

        // CakeOrderItem 정보를 가져와 DTO로 변환합니다. (mapToOrderListItem 재활용)
        List<OrderList.OrderListItem> dtoItems = page.getContent().stream()
                .map(this::mapToOrderListItem) // ⭐ mapToOrderListItem 메서드 호출
                .collect(Collectors.toList());

        return dtoItems; // 최신 3개 리스트만 반환
    }

    // mapToOrderListItem 헬퍼 메서드
    private OrderList.OrderListItem mapToOrderListItem(CakeOrder order) {
        List<CakeOrderItem> items = this.cakeOrderItemRepository.findByCakeOrder_OrderId(order.getOrderId());

        List<OrderList.OrderItemOption> itemOptions = items.stream() // ⭐ OrderList.Response.OrderItemOption으로 변경
                .map(this::mapToOrderItemOption) // ⭐ mapToOrderItemOption 메서드 호출
                .collect(Collectors.toList());

        // shopName이 null이 될 수 있으므로, null 체크를 통해 안전하게 처리
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName()
                : "";

        return OrderList.OrderListItem.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .shopName(shopName)
                .orderTotalPrice(order.getOrderTotalPrice())
                .status(order.getStatus().name())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .items(itemOptions)
                .discountAmount(order.getDiscountAmount()) // 추가
                .finalPaymentAmount(order.getFinalPaymentAmount()) // 추가
                .build();
    }

    // mapToOrderItemOption 헬퍼 메서드
    private OrderList.OrderItemOption mapToOrderItemOption(CakeOrderItem cakeOrderItem) {
        String cname = cakeOrderItem.getCakeItem().getCname();
        String thumbnail = cakeOrderItem.getCakeItem().getThumbnailImageUrl();
        // 가격 필드가 Long인지 Integer인지 확인. CakeOrderItem.getUnitPrice()는 Integer.longValue()로 캐스팅해야 합니다.
        Long price = cakeOrderItem.getUnitPrice() != null ? cakeOrderItem.getUnitPrice().longValue() : 0L; // null 체크 및 캐스팅
        Integer count = cakeOrderItem.getQuantity();

        Map<String, String> options = new HashMap<>();
        List<CakeOrderItemOption> opts = this.cakeOrderItemOptionRepository
                .findByCakeOrderItem_OrderItemId(cakeOrderItem.getOrderItemId());

        for (CakeOrderItemOption oio : opts) {
            // CakeOptionMapping 및 OptionItem이 null이 아닐 때만 접근
            if (oio.getCakeOptionMapping() != null && oio.getCakeOptionMapping().getOptionItem() != null) {
                options.put(
                        // 옵션의 이름 또는 그룹 이름을 키로 사용하는 것이 더 의미 있을 수 있습니다.
                        // 예: oio.getCakeOptionMapping().getOptionItem().getName()
                        // 현재는 mappingId를 String으로 변환하여 사용
                        String.valueOf(oio.getCakeOptionMapping().getMappingId()),
                        String.valueOf(oio.getOptionCnt())
                );
            }
        }

        return OrderList.OrderItemOption.builder()
                .cname(cname)
                .thumbnailImageUrl(thumbnail)
                .price(price)
                .productCnt(count)
                .options(options)
                .build();
    }
}