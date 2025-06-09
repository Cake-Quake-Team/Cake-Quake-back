package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemOptionRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SellerOrderServiceTests {

    @Mock
    private SellerOrderRepository sellerOrderRepository;

    @Mock
    private CakeOrderItemRepository cakeOrderItemRepository;

    @Mock
    private CakeOrderItemOptionRepository cakeOrderItemOptionRepository;

    @InjectMocks
    private SellerOrderServiceImpl sellerOrderService;

    private final Long testShopId = 10L;
    private final Long otherShopId = 99L;

    @Nested
    @DisplayName("판매자 주문 목록 조회 기능 테스트")
    class GetShopOrderListTests {

        @Test
        @DisplayName("페이징하여 가게 주문 목록 조회 성공")
        void getShopOrderList_paging_success() {
            // given
            Pageable pageable = PageRequest.of(0, 2);

            // 1) CakeOrder 엔티티 2개 생성
            Member testMember = Member.builder()
                    .uid(1L)
                    .userId("buyerUser")
                    .uname("구매자_테스트")
                    .phoneNumber("010-1111-2222")
                    .build();

            Shop testShop = Shop.builder()
                    .shopId(testShopId)
                    .shopName("테스트_가게")
                    .build();

            CakeOrder order1 = CakeOrder.builder()
                    .orderId(101L)
                    .member(testMember)
                    .shop(testShop)
                    .orderNumber("ORD-20250701-0001")
                    .status(OrderStatus.RESERVATION_PENDING)
                    .pickupDate(LocalDate.of(2025, 7, 1))
                    .pickupTime(LocalTime.of(10, 0))
                    .orderTotalPrice(15000)
                    .build();

            CakeOrder order2 = CakeOrder.builder()
                    .orderId(102L)
                    .member(testMember)
                    .shop(testShop)
                    .orderNumber("ORD-20250702-0001")
                    .status(OrderStatus.RESERVATION_CONFIRMED)
                    .pickupDate(LocalDate.of(2025, 7, 2))
                    .pickupTime(LocalTime.of(11, 30))
                    .orderTotalPrice(25000)
                    .build();

            List<CakeOrder> orders = List.of(order1, order2);
            Page<CakeOrder> pageOfOrders = new PageImpl<>(orders, pageable, 5);

            // 2) 레포지토리 mock 설정
            given(sellerOrderRepository.findByShopId(testShopId, pageable))
                    .willReturn(pageOfOrders);

            // 3) 각 주문마다 첫 번째 CakeOrderItem을 리턴하도록 설정
            //    - 첫 번째 주문: 상품명 "초코케이크", 썸네일 "/img/choco.jpg", 수량 2
            //    - 두 번째 주문: 상품명 "딸기케이크", 썸네일 "/img/berry.jpg", 수량 1
            CakeOrderItem item1 = CakeOrderItem.builder()
                    .orderItemId(1001L)
                    .cakeOrder(order1)
                    .cakeItem(
                            com.cakequake.cakequakeback.cake.item.entities.CakeItem.builder()
                                    .cname("초코케이크")
                                    .thumbnailImageUrl("/img/choco.jpg")
                                    .build()
                    )
                    .quantity(2)
                    .build();

            CakeOrderItem item2 = CakeOrderItem.builder()
                    .orderItemId(1002L)
                    .cakeOrder(order2)
                    .cakeItem(
                            com.cakequake.cakequakeback.cake.item.entities.CakeItem.builder()
                                    .cname("딸기케이크")
                                    .thumbnailImageUrl("/img/berry.jpg")
                                    .build()
                    )
                    .quantity(1)
                    .build();

            given(cakeOrderItemRepository.findByCakeOrder_OrderId(order1.getOrderId()))
                    .willReturn(List.of(item1));
            given(cakeOrderItemRepository.findByCakeOrder_OrderId(order2.getOrderId()))
                    .willReturn(List.of(item2));

            // when
            SellerOrderList.Response response = sellerOrderService.getShopOrderList(testShopId, pageable);

            // then
            assertNotNull(response);
            List<SellerOrderList.Response.SellerOrderListItem> items = response.getOrders();
            assertThat(items).hasSize(2);

            // 첫 번째 아이템 검증
            SellerOrderList.Response.SellerOrderListItem dto1 = items.get(0);
            assertEquals(101L, dto1.getOrderId());
            assertEquals("ORD-20250701-0001", dto1.getOrderNumber());
            assertEquals("초코케이크", dto1.getCname());
            assertEquals("/img/choco.jpg", dto1.getThumbnailImageUrl());
            assertEquals(LocalDate.of(2025, 7, 1), dto1.getPickupDate());
            assertEquals(LocalTime.of(10, 0), dto1.getPickupTime());
            assertEquals("RESERVATION_PENDING", dto1.getStatus());
            assertEquals(2, dto1.getProductCnt());
            assertEquals(15000, dto1.getOrderTotalPrice());

            // 두 번째 아이템 검증
            SellerOrderList.Response.SellerOrderListItem dto2 = items.get(1);
            assertEquals(102L, dto2.getOrderId());
            assertEquals("ORD-20250702-0001", dto2.getOrderNumber());
            assertEquals("딸기케이크", dto2.getCname());
            assertEquals("/img/berry.jpg", dto2.getThumbnailImageUrl());
            assertEquals(LocalDate.of(2025, 7, 2), dto2.getPickupDate());
            assertEquals(LocalTime.of(11, 30), dto2.getPickupTime());
            assertEquals("RESERVATION_CONFIRMED", dto2.getStatus());
            assertEquals(1, dto2.getProductCnt());
            assertEquals(25000, dto2.getOrderTotalPrice());

            // 페이지 정보 검증
            SellerOrderList.Response.PageInfo pageInfo = response.getPageInfo();
            assertEquals(0, pageInfo.getCurrentPage());
            assertEquals(3, pageInfo.getTotalPages());      // totalElements(5) / pageSize(2) → ceil(5/2) = 3
            assertEquals(5L, pageInfo.getTotalElements());

            // verify 호출 횟수
            verify(sellerOrderRepository, times(1)).findByShopId(testShopId, pageable);
            verify(cakeOrderItemRepository, times(1)).findByCakeOrder_OrderId(order1.getOrderId());
            verify(cakeOrderItemRepository, times(1)).findByCakeOrder_OrderId(order2.getOrderId());
        }

        @Test
        @DisplayName("주문이 없는 경우 빈 리스트 반환")
        void getShopOrderList_empty() {
            // given
            Pageable pageable = PageRequest.of(0, 5);
            Page<CakeOrder> emptyPage = Page.empty(pageable);

            given(sellerOrderRepository.findByShopId(testShopId, pageable))
                    .willReturn(emptyPage);

            // when
            SellerOrderList.Response response = sellerOrderService.getShopOrderList(testShopId, pageable);

            // then
            assertNotNull(response);
            assertThat(response.getOrders()).isEmpty();

            SellerOrderList.Response.PageInfo pageInfo = response.getPageInfo();
            assertEquals(0, pageInfo.getCurrentPage());
            assertEquals(0, pageInfo.getTotalPages());
            assertEquals(0L, pageInfo.getTotalElements());

            verify(sellerOrderRepository, times(1)).findByShopId(testShopId, pageable);
            verifyNoInteractions(cakeOrderItemRepository);
        }

        @Test
        @DisplayName("잘못된 가게 ID로 조회하면 빈 결과 반환")
        void getShopOrderList_invalidShopId() {
            // given: 다른 가게 ID로 조회 시, 빈 페이지 반환 설정
            Pageable pageable = PageRequest.of(0, 3);
            Page<CakeOrder> emptyPage = Page.empty(pageable);

            given(sellerOrderRepository.findByShopId(otherShopId, pageable))
                    .willReturn(emptyPage);

            // when
            SellerOrderList.Response response = sellerOrderService.getShopOrderList(otherShopId, pageable);

            // then
            assertNotNull(response);
            assertThat(response.getOrders()).isEmpty();

            verify(sellerOrderRepository, times(1)).findByShopId(otherShopId, pageable);
            verifyNoInteractions(cakeOrderItemRepository);
        }
    }

    @Nested
    @DisplayName("판매자 주문 상세 조회 기능 테스트")
    class GetShopOrderDetailTests {

        @Test
        @DisplayName("주문 상세 조회 성공")
        void getShopOrderDetail_success() {
            // given
            Long orderId = 202L;

            // 1) 가게, 주문자(User), 주문 엔티티 준비
            Member buyer = Member.builder()
                    .uid(2L)
                    .userId("buyerA")
                    .uname("구매자A")
                    .phoneNumber("010-3333-4444")
                    .build();

            Shop testShop = Shop.builder()
                    .shopId(testShopId)
                    .shopName("판매자_가게")
                    .build();

            CakeOrder mockOrder = CakeOrder.builder()
                    .orderId(orderId)
                    .member(buyer)
                    .shop(testShop)
                    .orderNumber("ORD-20250710-0002")
                    .status(OrderStatus.RESERVATION_CONFIRMED)
                    .pickupDate(LocalDate.of(2025, 7, 10))
                    .pickupTime(LocalTime.of(14, 0))
                    .orderTotalPrice(30000)
                    .build();

            given(sellerOrderRepository.findByOrderIdAndShopId(orderId, testShopId))
                    .willReturn(Optional.of(mockOrder));

            // 2) CakeOrderItem + CakeItem 준비
            com.cakequake.cakequakeback.cake.item.entities.CakeItem cakeItem =
                    com.cakequake.cakequakeback.cake.item.entities.CakeItem.builder()
                            .cname("바닐라케이크")
                            .thumbnailImageUrl("/img/vanilla.jpg")
                            .build();

            CakeOrderItem orderItem = CakeOrderItem.builder()
                    .orderItemId(3001L)
                    .cakeOrder(mockOrder)
                    .cakeItem(cakeItem)
                    .quantity(3)
                    .unitPrice(9000)
                    .subTotalPrice(BigDecimal.valueOf(27000))
                    .build();

            given(cakeOrderItemRepository.findByCakeOrder_OrderId(orderId))
                    .willReturn(List.of(orderItem));

            // 3) CakeOrderItemOption + CakeOptionMapping 준비
            com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping mapping =
                    com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping.builder()
                            .mappingId(5L)
                            .build();

            CakeOrderItemOption option = CakeOrderItemOption.builder()
                    .orderItemOptionId(4001L)
                    .cakeOrderItem(orderItem)
                    .cakeOptionMapping(mapping)
                    .optionCnt(2)
                    .build();

            given(cakeOrderItemOptionRepository.findByCakeOrderItem_OrderItemId(orderItem.getOrderItemId()))
                    .willReturn(List.of(option));

            // when
            SellerOrderDetail.Response response = sellerOrderService.getShopOrderDetail(testShopId, orderId);

            // then
            assertNotNull(response);
            assertEquals(orderId, response.getOrderId());
            assertEquals("ORD-20250710-0002", response.getOrderNumber());
            assertEquals("RESERVATION_CONFIRMED", response.getStatus());
            assertEquals(LocalDate.of(2025, 7, 10), response.getPickupDate());
            assertEquals(LocalTime.of(14, 0), response.getPickupTime());
            assertEquals(30000, response.getOrderTotalPrice());

            // 구매자 정보 검증
            SellerOrderDetail.BuyerInfo buyerInfo = response.getBuyer();
            assertNotNull(buyerInfo);
            assertEquals("구매자A", buyerInfo.getUname());
            assertEquals("010-3333-4444", buyerInfo.getPhoneNumber());

            // 상품 리스트 검증
            List<SellerOrderDetail.ProductDetail> products = response.getProducts();
            assertThat(products).hasSize(1);

            SellerOrderDetail.ProductDetail pd = products.get(0);
            assertEquals("바닐라케이크", pd.getName());
            assertEquals(3, pd.getQuantity());
            assertEquals(9000, pd.getUnitPrice());
            assertThat(pd.getSubTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(27000));
            assertEquals("/img/vanilla.jpg", pd.getThumbnailImageUrl());

            // 옵션 맵 검증 (키는 "5", 값은 2)
            Map<String, Integer> opts = pd.getOptions();
            assertThat(opts).containsKey("5");
            assertEquals(2, opts.get("5"));

            verify(sellerOrderRepository, times(1)).findByOrderIdAndShopId(orderId, testShopId);
            verify(cakeOrderItemRepository, times(1)).findByCakeOrder_OrderId(orderId);
            verify(cakeOrderItemOptionRepository, times(1))
                    .findByCakeOrderItem_OrderItemId(orderItem.getOrderItemId());
        }

        @Test
        @DisplayName("상이한 가게 ID로 조회 시 예외 발생")
        void getShopOrderDetail_notFound_throwsBusinessException() {
            // given
            Long orderId = 303L;
            given(sellerOrderRepository.findByOrderIdAndShopId(orderId, testShopId))
                    .willReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,() -> sellerOrderService.getShopOrderDetail(testShopId, orderId));
            assertEquals(ErrorCode.NOT_FOUND_ORDER_ID, ex.getErrorCode());

            verify(sellerOrderRepository, times(1)).findByOrderIdAndShopId(orderId, testShopId);
            verifyNoInteractions(cakeOrderItemRepository, cakeOrderItemOptionRepository);
        }
    }

    @Nested
    @DisplayName("판매자 주문 상태 변경 기능 테스트")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("유효한 상태 전환 (RESERVATION_PENDING → RESERVATION_CONFIRMED) 성공")
        void updateOrderStatus_validTransition_success() {
            // given
            Long orderId = 404L;
            Member buyer = Member.builder()
                    .uid(3L)
                    .userId("buyerB")
                    .uname("구매자B")
                    .phoneNumber("010-5555-6666")
                    .build();

            Shop testShop = Shop.builder()
                    .shopId(testShopId)
                    .shopName("판매자_가게B")
                    .build();

            CakeOrder existingOrder = CakeOrder.builder()
                    .orderId(orderId)
                    .member(buyer)
                    .shop(testShop)
                    .orderNumber("ORD-20250720-0003")
                    .status(OrderStatus.RESERVATION_PENDING)
                    .pickupDate(LocalDate.of(2025, 7, 20))
                    .pickupTime(LocalTime.of(13, 0))
                    .orderTotalPrice(18000)
                    .build();

            given(sellerOrderRepository.findByOrderIdAndShopId(orderId, testShopId))
                    .willReturn(Optional.of(existingOrder));

            // when
            sellerOrderService.updateOrderStatus(testShopId, orderId, "RESERVATION_CONFIRMED");

            // then
            assertEquals(OrderStatus.RESERVATION_CONFIRMED, existingOrder.getStatus());
            // 영속성 컨텍스트에서 변경 감지(dirty checking)로 자동 반영된다고 가정하므로 save 호출 검증은 없습니다.
            verify(sellerOrderRepository, times(1)).findByOrderIdAndShopId(orderId, testShopId);
        }

        @Test
        @DisplayName("유효하지 않은 상태 문자열 입력 시 IllegalArgumentException 발생")
        void updateOrderStatus_invalidStatusString_throwsBusinessException() {
            // given
            Long orderId = 505L;
            Member buyer = Member.builder()
                    .uid(4L)
                    .userId("buyerC")
                    .uname("구매자C")
                    .phoneNumber("010-7777-8888")
                    .build();

            Shop testShop = Shop.builder()
                    .shopId(testShopId)
                    .shopName("판매자_가게C")
                    .build();

            CakeOrder existingOrder = CakeOrder.builder()
                    .orderId(orderId)
                    .member(buyer)
                    .shop(testShop)
                    .orderNumber("ORD-20250725-0004")
                    .status(OrderStatus.RESERVATION_PENDING)
                    .pickupDate(LocalDate.of(2025, 7, 25))
                    .pickupTime(LocalTime.of(10, 30))
                    .orderTotalPrice(22000)
                    .build();

            given(sellerOrderRepository.findByOrderIdAndShopId(orderId, testShopId))
                    .willReturn(Optional.of(existingOrder));

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> sellerOrderService.updateOrderStatus(testShopId, orderId,
                            "INVALID_STATUS"));   // ← 유효하지 않은 값
            assertEquals(ErrorCode.INVALID_TYPE, ex.getErrorCode());

            verify(sellerOrderRepository, times(1)).findByOrderIdAndShopId(orderId, testShopId);
        }

        @Test
        @DisplayName("허용되지 않는 상태 전환 시 IllegalStateException 발생")
        void updateOrderStatus_invalidTransition_throwsBusinessException() {
            // given
            Long orderId = 606L;
            Member buyer = Member.builder()
                    .uid(5L)
                    .userId("buyerD")
                    .uname("구매자D")
                    .phoneNumber("010-9999-0000")
                    .build();

            Shop testShop = Shop.builder()
                    .shopId(testShopId)
                    .shopName("판매자_가게D")
                    .build();

            // 현재 상태가 PICKUP_COMPLETED인 주문을 생성
            CakeOrder existingOrder = CakeOrder.builder()
                    .orderId(orderId)
                    .member(buyer)
                    .shop(testShop)
                    .orderNumber("ORD-20250730-0005")
                    .status(OrderStatus.PICKUP_COMPLETED)
                    .pickupDate(LocalDate.of(2025, 7, 30))
                    .pickupTime(LocalTime.of(16, 0))
                    .orderTotalPrice(20000)
                    .build();

            given(sellerOrderRepository.findByOrderIdAndShopId(orderId, testShopId))
                    .willReturn(Optional.of(existingOrder));

            // when & then: PICKUP_COMPLETED 상태에서 RESERVATION_CONFIRMED로 바꾸려 할 때 예외
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> sellerOrderService.updateOrderStatus(testShopId, orderId, "RESERVATION_CONFIRMED"));
            assertEquals(ErrorCode.ORDER_MISMATCH, ex.getErrorCode());
            verify(sellerOrderRepository, times(1)).findByOrderIdAndShopId(orderId, testShopId);
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 NoSuchElementException 발생")
        void updateOrderStatus_notFound_throwsBusinessException() {
            // given
            Long orderId = 707L;
            given(sellerOrderRepository.findByOrderIdAndShopId(orderId, testShopId))
                    .willReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> sellerOrderService.updateOrderStatus(testShopId, orderId, "RESERVATION_CANCELLED"));
            assertEquals(ErrorCode.NOT_FOUND_ORDER_ID, ex.getErrorCode());

            verify(sellerOrderRepository, times(1)).findByOrderIdAndShopId(orderId, testShopId);
        }
    }
}
