package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import com.cakequake.cakequakeback.cart.repo.CartItemRepository;
import com.cakequake.cakequakeback.cart.repo.CartRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.dto.buyer.CreateOrder;
import com.cakequake.cakequakeback.order.dto.buyer.OrderDetail;
import com.cakequake.cakequakeback.order.dto.buyer.OrderList;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.*;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


    @Override
    public CreateOrder.Response createOrder(String userId, CreateOrder.Request request) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("유효하지 않은 사용자입니다: " + userId));
        // (1) cartItemIds와 directItems 중 하나만 제공되어야 함
        boolean hasCart = request.getCartItemIds() != null && !request.getCartItemIds().isEmpty();
        boolean hasDirect = request.getDirectItems() != null && !request.getDirectItems().isEmpty();
        if (hasCart == hasDirect) { // 둘 다 존재하거나 둘 다 비어있으면 잘못된 요청
            throw new ValidationException("cartItemIds와 directItems 중 하나만 지정하세요.");
        }
        // (2) CakeOrder 생성
        CakeOrder order = CakeOrder.builder()
                .member(member)
                .pickupDate(request.getPickupDate())
                .pickupTime(request.getPickupTime())
                .status(OrderStatus.RESERVATION_PENDING)
                //.orderNumber(generateOrderNumber(memberUid))
                .build();
        buyerOrderRepository.save(order);

        long OrderToTalPrice = 0L;

        if (hasDirect) {

            for (CreateOrder.DirectItem directItem : request.getDirectItems()) {
                CakeOrderItem item = CakeOrderItem.builder()
                        .cakeOrder(order)
                        //.cakeItem(CakeOrderItem(direct.getProductId())) // 실제 CakeItem 조회 로직 필요
                        .quantity(directItem.getQuantity())
                        //.unitPrice(fetchUnitPrice(direct.getProductId()))    // 실제 단가 조회 로직 필요
                        //.subTotalPrice(fetchUnitPrice(direct.getProductId()) * direct.getQuantity()) //이것도
                        .build();
                cakeOrderItemRepository.save(item);

                OrderToTalPrice += item.getQuantity(); // totalPrice += item.getSubTotalPrice();

                Map<String, String> optionsMap = directItem.getOptions();
                if (optionsMap != null && optionsMap.containsKey("mappingId")) {
                    Long mappingId;
                    try {
                        mappingId = Long.valueOf(optionsMap.get("mappingId"));
                    } catch (NumberFormatException exception) {
                        throw new ValidationException("유효하지 않은 mappindId: " + optionsMap.get("mappingId"));
                    }
                    CakeOptionMapping mapping = cakeOptionMappingRepository
                            .findById(mappingId)                                      // Optional<CakeOptionMapping> 반환
                            .orElseThrow(() -> new NoSuchElementException(
                                    "유효하지 않은 옵션 매핑입니다: " + mappingId
                            ));

                    CakeOrderItemOption itemOption = CakeOrderItemOption.builder()
                            .cakeOrderItem(item)
                            .cakeOptionMapping(mapping)
                            .optionCnt(1) // 옵션 개수(예시)
                            .build();
                    cakeOrderItemOptionRepository.save(itemOption);

                    OrderToTalPrice += mapping.getOptionItem().getPrice();
                }
            }
        }
        /* 장바구니 주문할 때 //생각을 해보니 장바구니에도 mapping옵션을 넣어야하네..
        else {
            // --- CartItemIds 기반 주문 처리 ---
            // (1) 회원의 장바구니 조회
            Cart cart = cartRepository.findByMember(member)
                    .orElseThrow(() -> new NoSuchElementException("장바구니가 존재하지 않습니다."));

            // (2) 요청된 cartItemIds 순회하며, CartItem → CakeOrderItem 생성
            for (Long cartItemId : request.getCartItemIds()) {
                CartItem cartItem = cartItemRepository.findById(cartItemId)
                        .orElseThrow(() -> new NoSuchElementException("유효하지 않은 CartItem ID: " + cartItemId));

                // 해당 CartItem이 실제로 현재 회원의 Cart에 속했는지 검증
                if (!Objects.equals(cartItem.getCart().getCartId(), cart.getCartId())) {
                    throw new IllegalArgumentException("본인의 장바구니 아이템이 아닙니다: ID=" + cartItemId);
                }

                // (3) CakeOrderItem 생성 및 저장
                CakeOrderItem orderItem = CakeOrderItem.builder()
                        .cakeOrder(order)
                        .cakeItem(cartItem.getCakeItem())
                        .unitPrice(cartItem.getCakeItem().getPrice())
                        .subTotalPrice(cartItem.getCakeItem().getPrice() * cartItem.getQuantity())
                        .build();
                cakeOrderItemRepository.save(orderItem);

                // (4) 주문 총액에 소계 반영
                totalPrice += orderItem.getSubTotalPrice();

                // (5) CartItemOption이 존재하면, CakeOrderItemOption으로 변환해 저장
                List<CartItem> cartOptions = cartItem.getCartItemId();
                for (CartItemOption cartOpt : cartOptions) {
                    // CartItemOption에는 CakeOptionMapping ID와 optionCnt 정보가 있어야 함
                    CakeOptionMapping mapping = cakeOptionMappingRepository
                            .findById(cartOpt.getMappingOId())
                            .orElseThrow(() -> new NoSuchElementException(
                                    "유효하지 않은 옵션 매핑입니다: " + cartOpt.getMappingId()
                            ));

                    CakeOrderItemOption orderItemOption = CakeOrderItemOption.builder()
                            .cakeOrderItem(orderItem)
                            .cakeOptionMapping(mapping)
                            .optionCnt(cartOpt.getOptionCnt())
                            .build();
                    cakeOrderItemOptionRepository.save(orderItemOption);

                    // 옵션 가격 반영
                    totalPrice += mapping.getOptionItem().getPrice() * cartOpt.getOptionCnt();
                }

                // (6) CartItem을 주문으로 이동했으므로, 장바구니에서는 삭제
                cartItemRepository.delete(cartItem);
            }

            // (7) 장바구니 총액 재계산 (삭제된 항목 반영)
            recalculateCartTotalPrice(cart);
        }
*/
        // (3) 주문 총액 업데이트
        //orderTotalPrice(order.getOrderTotalPrice());
        buyerOrderRepository.save(order);

        // (4) 응답 DTO 반환
        return CreateOrder.Response.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderTotalPrice(order.getOrderTotalPrice())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .build();
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
        CakeOrder order = buyerOrderRepository.findByOrderIdAndMemberUserId(orderId, userId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다: " + orderId));

        List<CakeOrderItem> items = cakeOrderItemRepository.findByCakeOrder_OrderId(orderId);

        List<OrderDetail.OrderDetailItem> itemDtos = items.stream()
                .map(item -> OrderDetail.OrderDetailItem.builder()
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
                .items(itemDtos)
                .totalPrice(order.getOrderTotalPrice().longValue())
                .build();
    }


    @Override
    public void cancelOrder(String userId, Long orderId) {
        CakeOrder order = buyerOrderRepository
                .findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다: " + orderId));

        if (!Objects.equals(order.getMember().getUid(), userId)) {
            throw new IllegalArgumentException("본인의 주문이 아닙니다.");
        }
        if (order.getStatus() != OrderStatus.RESERVATION_PENDING) {
            throw new IllegalStateException("진행 중인 주문은 취소할 수 없습니다.");
        }

        //order.getStatus(OrderStatus.RESERVATION_CANCELLED);
    }

    //---헬퍼---//

    private OrderList.OrderListItem mapToOrderListItem(CakeOrder order) {
        List<CakeOrderItem> items = this.cakeOrderItemRepository.findByCakeOrder_OrderId(order.getOrderId());

        List<OrderList.OrderItemOption> itemDtos = items.stream()
                .map(this::mapToOrderItemOption)
                .collect(Collectors.toList());

        String shopName = items.isEmpty()
                ? ""
                : items.get(0).getCakeItem().getShop().getShopName();

        return OrderList.OrderListItem.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .shopName(shopName)
                .orderTotalPrice(order.getOrderTotalPrice())
                .status(order.getStatus().name())
                //.orderType(order.isCustom() ? "CUSTOM" : "GENERAL")
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .items(itemDtos)
                .build();
    }

    private OrderList.OrderItemOption mapToOrderItemOption(CakeOrderItem cakeOrderItem) {
        String cname = cakeOrderItem.getCakeItem().getCname();
        String thumbnail = cakeOrderItem.getCakeItem().getThumbnailImageUrl();
        Long price = cakeOrderItem.getUnitPrice().longValue();
        Integer count = cakeOrderItem.getQuantity();

        Map<String, String> options = new HashMap<>();
        List<CakeOrderItemOption> opts = this.cakeOrderItemOptionRepository
                .findByCakeOrderItem_OrderItemId(cakeOrderItem.getOrderItemId());

        for (CakeOrderItemOption oio : opts) {
            if (oio.getCakeOptionMapping() != null && oio.getCakeOptionMapping().getMappingId() != null) {
                options.put(
                        oio.getCakeOptionMapping().getMappingId().toString(),
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