package com.cakequake.cakequakeback.cart;

import com.cakequake.cakequakeback.cart.dto.AddCart;
import com.cakequake.cakequakeback.cart.dto.GetCart;
import com.cakequake.cakequakeback.cart.dto.UpdateCartItem;
import com.cakequake.cakequakeback.cart.dto.DeletedCartItem;
import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import com.cakequake.cakequakeback.cart.repo.CartItemRepository;
import com.cakequake.cakequakeback.cart.repo.CartRepository;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.cart.service.CartServiceImpl;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CartServiceTests {
    @Mock MemberRepository memberRepository;
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock CakeItemRepository cakeItemRepository;
    @InjectMocks
    CartServiceImpl cartService;

    private Member testMember;
    private Cart testCart;
    private CakeItem testCakeItem;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .uid(1L)
                .userId("user123")
                .uname("테스트유저")
                .build();

        testCart = Cart.builder()
                .cartId(100L)
                .member(testMember)
                .build();

        testCakeItem = CakeItem.builder()
                .cakeId(10L)
                .cname("초콜릿 케이크")
                .price(20000)
                .build();

        testCartItem = CartItem.builder()
                .cart(testCart)
                .cakeItem(testCakeItem)
                .productCnt(2)
                .itemTotalPrice((long)testCakeItem.getPrice() * 2)
                .build();
    }

    @Nested
    class AddCartTests {
        @Test
        void addCart_success() {
            AddCart.Request req = AddCart.Request.builder()
                    .cakeItemId(10L)
                    .productCnt(2)
                    .build();

            given(memberRepository.findByUserId("user123"))
                    .willReturn(Optional.of(testMember));
            given(cakeItemRepository.findById(10L))
                    .willReturn(Optional.of(testCakeItem));
            given(cartRepository.findByMember(testMember))
                    .willReturn(Optional.of(testCart));
            given(cartItemRepository.save(any(CartItem.class)))
                    .willAnswer(inv -> {
                        CartItem arg = inv.getArgument(0);
                        return CartItem.builder()
                                .cartItemId(999L)
                                .cart(arg.getCart())
                                .cakeItem(arg.getCakeItem())
                                .productCnt(arg.getProductCnt())
                                .itemTotalPrice(arg.getItemTotalPrice())
                                .build();
                    });

            AddCart.Response res = cartService.addCart("user123", req);

            assertNotNull(res);
            assertEquals(999L, res.getCartItemId());
            assertEquals(10L, res.getCakeItemId());
            assertEquals(2, res.getProductCnt());
            assertEquals(2L * testCakeItem.getPrice(), res.getItemTotalPrice());
        }

        @Test
        void addCart_memberNotFound_throwsException() {
            given(memberRepository.findByUserId("user123"))
                    .willReturn(Optional.empty());

            AddCart.Request req = AddCart.Request.builder()
                    .cakeItemId(10L)
                    .productCnt(1)
                    .build();

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> cartService.addCart("user123", req));
            assertEquals(ErrorCode.NOT_FOUND_UID, ex.getErrorCode());
        }
    }

    @Nested
    class GetCartTests {
        @Test
        void getCart_withItems() {
            given(memberRepository.findByUserId("user123"))
                    .willReturn(Optional.of(testMember));
            given(cartRepository.findByMember(testMember))
                    .willReturn(Optional.of(testCart));
            given(cartItemRepository.findByCart(testCart))
                    .willReturn(List.of(testCartItem));

            GetCart.Response res = cartService.getCart("user123");

            assertThat(res.getItems()).hasSize(1);
            assertEquals(testCartItem.getCakeItem().getCname(), res.getItems().get(0).getCname());
        }

        @Test
        void getCart_noCart_returnsEmpty() {
            given(memberRepository.findByUserId("user123"))
                    .willReturn(Optional.of(testMember));
            given(cartRepository.findByMember(testMember))
                    .willReturn(Optional.empty());

            GetCart.Response res = cartService.getCart("user123");

            assertThat(res.getItems()).isEmpty();
        }
    }

    @Nested
    class UpdateCartItemTests {
        @Test
        void updateCartItem_success() {
            // 요청 준비
            UpdateCartItem.Request req = UpdateCartItem.Request.builder()
                    .cartItemId(123L)
                    .productCnt(5)
                    .build();

            // 기존 CartItem 더미
            CartItem existing = CartItem.builder()
                    .cart(testCart)
                    .cakeItem(testCakeItem)
                    .cartItemId(123L)
                    .productCnt(2)
                    .itemTotalPrice((long)testCakeItem.getPrice() * 2)
                    .build();

            // 모킹: 유저 조회
            given(memberRepository.findByUserId("user123"))
                    .willReturn(Optional.of(testMember));
            // 모킹: 해당 유저의 Cart 조회
            given(cartRepository.findByMember(testMember))
                    .willReturn(Optional.of(testCart));
            // 모킹: CartItem 조회 – 기존 findById 대신 서비스에서 실제 쓰는 메서드로
            given(cartItemRepository.findByCartAndCartItemId(testCart, 123L))
                    .willReturn(Optional.of(existing));
            // 모킹: 저장 후 반환
            given(cartItemRepository.save(any(CartItem.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // 실제 호출
            UpdateCartItem.Response res = cartService.updateCartItem("user123", req);

            // 검증
            assertEquals(5, res.getUpdatedProductCnt());
            assertEquals(5L * testCakeItem.getPrice(), res.getUpdatedItemTotalPrice());
        }


        @Test
        void updateCartItem_notFound_throwsException() {
            given(memberRepository.findByUserId("user123"))
                    .willReturn(Optional.of(testMember));

            UpdateCartItem.Request req = UpdateCartItem.Request.builder()
                    .cartItemId(123L)
                    .productCnt(2)
                    .build();

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> cartService.updateCartItem("user123", req));
            assertEquals(ErrorCode.NOT_FOUND_CART_ID, ex.getErrorCode());
        }
    }

    @Nested
    class DeleteCartItemTests {
        @Test
        void deleteCartItem_success() {
            List<Long> toDelete = List.of(123L, 456L);
            DeletedCartItem.Request req = /* change DTO visibility */ null;

            given(memberRepository.findByUserId("user123"))
                    .willReturn(Optional.of(testMember));
            given(cartRepository.findByMember(testMember))
                    .willReturn(Optional.of(testCart));
            given(cartItemRepository.findByCart(testCart))
                    .willReturn(List.of(
                            CartItem.builder().cartItemId(123L).cart(testCart).cakeItem(testCakeItem).productCnt(1).itemTotalPrice((long)testCakeItem.getPrice()).build(),
                            CartItem.builder().cartItemId(456L).cart(testCart).cakeItem(testCakeItem).productCnt(1).itemTotalPrice((long)testCakeItem.getPrice()).build()
                    ));

            DeletedCartItem.Response res = cartService.deleteCartItem("user123");

            assertEquals(toDelete, res.getDeletedCartItemIds());
            assertThat(res.getMessage()).contains("삭제");
        }

        @Test
        void deleteCartItem_noCart_returnsEmpty() {
            DeletedCartItem.Request req = /* see above */ null;

            given(memberRepository.findByUserId("user123"))
                    .willReturn(Optional.of(testMember));
            given(cartRepository.findByMember(testMember))
                    .willReturn(Optional.empty());

            DeletedCartItem.Response res = cartService.deleteCartItem("user123");

            assertThat(res.getDeletedCartItemIds()).isEmpty();
            assertThat(res.getMessage()).contains("삭제할 장바구니가 없습니다.");
        }
    }
}
