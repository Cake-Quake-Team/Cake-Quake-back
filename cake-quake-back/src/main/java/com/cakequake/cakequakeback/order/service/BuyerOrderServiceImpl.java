package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.cart.entities.CartItem;
// import com.cakequake.cakequakeback.cart.entities.CartItemOption; // ⭐ CartItemOption 제거 ⭐
import com.cakequake.cakequakeback.cart.repo.CartItemRepository;
import com.cakequake.cakequakeback.cart.repo.CartRepository;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
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
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


    @Override
    public CreateOrder.Response createOrder(String userId, CreateOrder.Request request) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));


        boolean hasCart = request.getCartItemIds() != null && !request.getCartItemIds().isEmpty();
        boolean hasDirect = request.getDirectItems() != null && !request.getDirectItems().isEmpty();

        if (hasCart == hasDirect) {
            throw new BusinessException(ErrorCode.INVALID_CART_ITEMS, "주문은 장바구니 또는 바로구매 중 한 가지 방식만 가능합니다.");
        }

        CakeOrder order = CakeOrder.builder()
                .member(member)
                .pickupDate(request.getPickupDate())
                .pickupTime(request.getPickupTime())
                .orderNote(request.getOrderNote())
                .orderNumber(generateOrderNumber(userId))
                .status(OrderStatus.RESERVATION_PENDING)
                .shop(shop)
                .build();

        long calculatedTotalPrice = 0L;
        int totalItemCount = 0;
        List<CakeOrderItem> tempOrderItems = new ArrayList<>();

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
                int itemSubTotal = itemUnitPrice * quantity;
                calculatedTotalPrice += itemSubTotal;

                CakeOrderItem item = CakeOrderItem.builder()
                        .cakeOrder(order)
                        .quantity(quantity)
                        .unitPrice(itemUnitPrice)
                        .subTotalPrice(itemSubTotal)
                        .cakeItem(cakeItem)
                        .build();
                tempOrderItems.add(item);

                // 옵션 처리
                Map<Long, Integer> optionsMap = directItem.getOptions();
                if (optionsMap != null && !optionsMap.isEmpty()) {
                    for (Map.Entry<Long, Integer> entry : optionsMap.entrySet()) {
                        Long mappingId = entry.getKey();
                        Integer optionQuantity = entry.getValue();

                        CakeOptionMapping mapping = cakeOptionMappingRepository.findById(mappingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID, "케이크 옵션 매핑을 찾을 수 없습니다: " + mappingId));

                        // CakeOrderItemOption 생성 및 저장
                        CakeOrderItemOption orderItemOption = CakeOrderItemOption.builder()
                                .cakeOrderItem(item)
                                .cakeOptionMapping(mapping)
                                .optionCnt(optionQuantity)
                                .build();
                        cakeOrderItemOptionRepository.save(orderItemOption);

                        calculatedTotalPrice += (long) mapping.getOptionItem().getPrice() * optionQuantity;
                    }
                }
            }
        } else if (hasCart) { // 장바구니 상품 처리
            List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());

            if (cartItems.isEmpty() || cartItems.size() != request.getCartItemIds().size()) {
                throw new BusinessException(ErrorCode.NOT_FOUND_CAKE_ITEM, "유효하지 않거나 찾을 수 없는 장바구니 아이템이 포함되어 있습니다.");
            }

            for (CartItem cartItem : cartItems) {
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
                int itemSubTotal = itemUnitPrice * quantity;
                calculatedTotalPrice += itemSubTotal;

                CakeOrderItem item = CakeOrderItem.builder()
                        .cakeOrder(order)
                        .quantity(quantity)
                        .unitPrice(itemUnitPrice)
                        .subTotalPrice(itemSubTotal)
                        .cakeItem(cakeItem)
                        .build();
                tempOrderItems.add(item); // 임시 리스트에 추가

                // ⭐⭐ CartItemOption이 없으므로, CartItem의 options 맵을 직접 참조하여 CakeOrderItemOption 생성 ⭐⭐
                // CartItem 엔티티에 Map<Long, Integer> options 필드가 있다고 가정
                Map<Long, Integer> optionsMap = cartItem.getOptions(); // CartItem.getOptions()가 Map<Long, Integer>를 반환한다고 가정
                if (optionsMap != null && !optionsMap.isEmpty()) {
                    for (Map.Entry<Long, Integer> entry : optionsMap.entrySet()) {
                        Long mappingId = entry.getKey();
                        Integer optionQuantity = entry.getValue();

                        CakeOptionMapping mapping = cakeOptionMappingRepository.findById(mappingId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID, "장바구니 아이템 옵션 매핑을 찾을 수 없습니다: " + mappingId));

                        CakeOrderItemOption orderItemOption = CakeOrderItemOption.builder()
                                .cakeOrderItem(item)
                                .cakeOptionMapping(mapping)
                                .optionCnt(optionQuantity)
                                .build();
                        cakeOrderItemOptionRepository.save(orderItemOption);

                        calculatedTotalPrice += (long) mapping.getOptionItem().getPrice() * optionQuantity;
                    }
                }
            }
            cartItemRepository.deleteAllById(request.getCartItemIds());
        }

        order.applyOrderTotalPrice(calculatedTotalPrice);
        order.applyTotalNumber(totalItemCount);

        CakeOrder savedOrder = buyerOrderRepository.save(order);

        for (CakeOrderItem item : tempOrderItems) {
            CakeOrderItem finalItem = CakeOrderItem.builder()
                    .cakeItem(item.getCakeItem())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subTotalPrice(item.getSubTotalPrice())
                    .cakeOrder(savedOrder)
                    .build();
            cakeOrderItemRepository.save(finalItem);
        }

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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID, "해당 주문 정보를 찾을 수 없습니다."));

        if (!Objects.equals(order.getMember().getUserId().trim(), userId.trim())) { // .trim() 추가
            throw new BusinessException(ErrorCode.NOT_OWN_ORDER, "주문 번호가 본인의 것이 아닙니다.");
        }
        if (order.getStatus() != OrderStatus.RESERVATION_PENDING) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE, "현재 주문 상태에서는 취소할 수 없습니다.");
        }

        order.updateStatus(OrderStatus.RESERVATION_CANCELLED);
        buyerOrderRepository.save(order);
    }

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