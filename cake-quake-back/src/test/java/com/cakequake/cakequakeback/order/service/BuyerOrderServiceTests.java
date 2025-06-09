package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cart.repo.CartItemRepository;
import com.cakequake.cakequakeback.cart.repo.CartRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.dto.buyer.CreateOrder;
import com.cakequake.cakequakeback.order.dto.buyer.OrderDetail;
import com.cakequake.cakequakeback.order.dto.buyer.OrderList;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemOptionRepository;
import com.cakequake.cakequakeback.order.repo.CakeOptionMappingRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BuyerOrderServiceTests {
    @Mock BuyerOrderRepository buyerOrderRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock CartRepository cartRepository;
    @Mock CakeOrderItemRepository cakeOrderItemRepository;
    @Mock CakeOptionMappingRepository cakeOptionMappingRepository;
    @Mock CakeOrderItemOptionRepository cakeOrderItemOptionRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks BuyerOrderServiceImpl buyerOrderService;

    private Member testMember;
    private String testUserId;
    private Shop testShop;
    private CakeOrder testOrder;

    @BeforeEach
    void setUp() {
        testUserId = "testUser123";
        testMember = Member.builder()
                .uid(1L)
                .userId(testUserId)
                .uname("테스트유저")
                .build();
        testShop = Shop.builder()
                .shopId(10L)
                .shopName("테스트가게")
                .build();

        // dummy order with ID=123
        testOrder = CakeOrder.builder()
                .orderId(123L)
                .member(testMember)
                .shop(testShop)
                .orderNumber("ORD-123")
                .orderNote("테스트주문")
                .totalNumber(1)
                .orderTotalPrice(10000)
                .pickupDate(LocalDate.now())
                .pickupTime(LocalTime.NOON)
                .status(OrderStatus.RESERVATION_PENDING)
                .build();


    }

    @Nested
    class CreateOrderTests {
        @Test
        void createOrder_withDirectItems_success() {
            CreateOrder.DirectItem directItem = CreateOrder.DirectItem.builder()
                    .productId(1L)
                    .quantity(2)
                    .options(Map.of("10", "1"))
                    .build();
            CreateOrder.Request request = CreateOrder.Request.builder()
                    .directItems(List.of(directItem))
                    .pickupDate(LocalDate.now())
                    .pickupTime(LocalTime.NOON)
                    .build();

            given(memberRepository.findByUserId(testUserId))
                    .willReturn(Optional.of(testMember));

            given(buyerOrderRepository.save(any(CakeOrder.class)))
                    .willReturn(testOrder);

            CreateOrder.Response res = buyerOrderService.createOrder(testUserId, request);

            assertNotNull(res);
            assertEquals(123L, res.getOrderId());
        }

        @Test
        void createOrder_memberNotFound_throwsException() {
            given(memberRepository.findByUserId(testUserId))
                    .willReturn(Optional.empty());

            CreateOrder.Request request = CreateOrder.Request.builder()
                    .pickupDate(LocalDate.now())
                    .pickupTime(LocalTime.NOON)
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    buyerOrderService.createOrder(testUserId, request));
            assertEquals(ErrorCode.NOT_FOUND_UID, ex.getErrorCode());
        }
    }

    @Nested
    class GetOrderListTests {
        @Test
        void getOrderList_success() {
            CakeOrder o1 = CakeOrder.builder()
                    .orderId(1L)
                    .member(testMember)
                    .shop(testShop)
                    .orderNumber("ORD-1")
                    .pickupDate(LocalDate.now())
                    .pickupTime(LocalTime.NOON)
                    .orderTotalPrice(10000)
                    .status(OrderStatus.RESERVATION_CONFIRMED)
                    .build();
            CakeOrder o2 = CakeOrder.builder()
                    .orderId(2L)
                    .member(testMember)
                    .shop(testShop)
                    .orderNumber("ORD-2")
                    .pickupDate(LocalDate.now())
                    .pickupTime(LocalTime.NOON)
                    .orderTotalPrice(20000)
                    .status(OrderStatus.RESERVATION_PENDING)
                    .build();

            Page<CakeOrder> page = new PageImpl<>(List.of(o1, o2));
            given(buyerOrderRepository.findByMemberUserId(testUserId, PageRequest.of(0,10)))
                    .willReturn(page);

            OrderList.Response res = buyerOrderService.getOrderList(testUserId, PageRequest.of(0,10));

            assertThat(res.getOrders()).hasSize(2);
            assertEquals("ORD-1", res.getOrders().get(0).getOrderNumber());
        }
    }

    @Nested
    class GetOrderDetailTests {
        @Test
        void getOrderDetail_success() {
            Long orderId = 1L;
            CakeOrder order = CakeOrder.builder()
                    .orderId(orderId)
                    .member(testMember)
                    .shop(testShop)
                    .orderNumber("ORD-123")
                    .orderTotalPrice(15000)
                    .status(OrderStatus.RESERVATION_CONFIRMED)
                    .pickupDate(LocalDate.now())
                    .pickupTime(LocalTime.NOON)
                    .build();

            given(buyerOrderRepository.findById(orderId))
                    .willReturn(Optional.of(order));

            OrderDetail.Response response = buyerOrderService.getOrderDetail(testUserId, orderId);

            assertEquals(orderId, response.getOrderId());
            assertEquals("ORD-123", response.getOrderNumber());
        }

        @Test
        void getOrderDetail_notMyOrder_throwsException() {
            Long orderId = 2L;
            Member other = Member.builder().uid(99L).userId("other").uname("other").build();
            CakeOrder order = CakeOrder.builder().orderId(orderId).member(other).shop(testShop).build();
            given(buyerOrderRepository.findById(orderId))
                    .willReturn(Optional.of(order));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> buyerOrderService.getOrderDetail(testUserId, orderId));
            assertEquals(ErrorCode.NOT_OWN_ORDER, ex.getErrorCode());
        }

        @Test
        void getOrderDetail_notFound_throwsException() {
            given(buyerOrderRepository.findById(404L))
                    .willReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> buyerOrderService.getOrderDetail(testUserId, 404L));
            assertEquals(ErrorCode.NOT_FOUND_ORDER_ID, ex.getErrorCode());
        }
    }
}
