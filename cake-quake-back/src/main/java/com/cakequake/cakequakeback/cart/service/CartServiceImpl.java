package com.cakequake.cakequakeback.cart.service;

import com.cakequake.cakequakeback.cart.dto.AddCart;
import com.cakequake.cakequakeback.cart.dto.DeletedCartItem;
import com.cakequake.cakequakeback.cart.dto.GetCart;
import com.cakequake.cakequakeback.cart.dto.UpdateCartItem;
import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import com.cakequake.cakequakeback.cart.repo.CartItemRepository;
import com.cakequake.cakequakeback.cart.repo.CartRepository;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.dto.buyer.CreateOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CakeItemRepository cakeItemRepository;
    private final ObjectMapper objectMapper;

    private Cart getOrCreateCart(Member member) {
        return cartRepository.findByMember(member)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .member(member)
                            .cartTotalPrice(0)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private void recalculateCartTotalPrice(Cart cart) {
        if (cart == null) {
            return;
        }
        List<CartItem> itemsInCart = cartItemRepository.findByCart(cart);

        long totalCalculatedPrice = 0L;
        for (CartItem item : itemsInCart) {
            long basePrice = (long) item.getCakeItem().getPrice() * item.getProductCnt();
            long optionsPrice = 0L;

            if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
                try {
                    List<AddCart.CartItemOption> parsedOptions = objectMapper.readValue(item.getSelectedOptions(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, AddCart.CartItemOption.class));
                    optionsPrice = parsedOptions.stream()
                            .mapToLong(option -> (long) (option.getOptionPrice() != null ? option.getOptionPrice() : 0) * (option.getOptionCnt() != null ? option.getOptionCnt() : 1))
                            .sum();
                } catch (JsonProcessingException e) {
                    log.error("장바구니 총 가격 재계산 중 옵션 JSON 파싱 실패 (cartItemId: {}, error: {})", item.getCartItemId(), e.getMessage());
                }
            }
            totalCalculatedPrice += (basePrice + optionsPrice);
        }

        cart.updateCartTotalPrice((int) totalCalculatedPrice);
        cartRepository.save(cart);
    }

    @Override
    public AddCart.Response addCart(String userId, AddCart.Request request) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        Cart cart = getOrCreateCart(member);

        CakeItem cakeItem = cakeItemRepository.findById(request.getCakeItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSING_CAKE_ITEM_ID));

        // ⭐ [수정] selectedOptionsJson과 optionsTotalPrice 변수를 final 또는 effectively final로 만들기 ⭐
        final String currentSelectedOptionsJson; // final 키워드 추가
        final long currentOptionsTotalPrice;     // final 키워드 추가

        if (request.getCakeOptions() != null && !request.getCakeOptions().isEmpty()) {
            try {
                currentSelectedOptionsJson = objectMapper.writeValueAsString(request.getCakeOptions());
                currentOptionsTotalPrice = request.getCakeOptions().stream()
                        .mapToLong(option -> (long) (option.getOptionPrice() != null ? option.getOptionPrice() : 0) * (option.getOptionCnt() != null ? option.getOptionCnt() : 1))
                        .sum();
            } catch (JsonProcessingException e) {
                log.error("장바구니 추가 시 옵션 정보 JSON 변환 실패: {}", e.getMessage(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "옵션 정보 변환 실패");
            }
        } else {
            // 옵션이 없을 경우 기본값 할당 (final 변수이므로 모든 경로에서 초기화되어야 함)
            currentSelectedOptionsJson = null;
            currentOptionsTotalPrice = 0L;
        }

        log.info("AddCart Request CakeOptions: {}", request.getCakeOptions());
        log.info("Selected Options JSON: {}", currentSelectedOptionsJson);
        log.info("Calculated Options Total Price: {}", currentOptionsTotalPrice);

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByCart(cart).stream()
                .filter(ci -> ci.getCakeItem().getCakeId().equals(request.getCakeItemId()))
                // ⭐ [수정] 람다 내에서 effectively final 변수 사용 ⭐
                .filter(ci -> Objects.equals(ci.getSelectedOptions(), currentSelectedOptionsJson))
                .findFirst();

        CartItem savedCartItem;
        int quantity = request.getProductCnt();

        if (quantity < 1 || quantity > 99) {
            throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED, "장바구니 상품 수량은 1개 이상 99개 이하여야 합니다.");
        }

        // 최종 itemTotalPrice 계산
        long newItemTotalPrice = (long) cakeItem.getPrice() * quantity + currentOptionsTotalPrice; // ⭐ [수정] currentOptionsTotalPrice 사용 ⭐

        if (existingCartItemOpt.isPresent()) {
            CartItem existingCartItem = existingCartItemOpt.get();
            int newCount = existingCartItem.getProductCnt() + quantity;
            if (newCount > 99) {
                throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED, "상품의 총 수량이 99개를 초과할 수 없습니다.");
            }
            savedCartItem = CartItem.builder()
                    .cartItemId(existingCartItem.getCartItemId())
                    .cart(existingCartItem.getCart())
                    .cakeItem(existingCartItem.getCakeItem())
                    .productCnt(newCount)
                    .itemTotalPrice(newItemTotalPrice)
                    .selectedOptions(currentSelectedOptionsJson) // ⭐ [수정] currentSelectedOptionsJson 사용 ⭐
                    .build();
        } else {
            savedCartItem = CartItem.builder()
                    .cart(cart)
                    .cakeItem(cakeItem)
                    .productCnt(quantity)
                    .itemTotalPrice(newItemTotalPrice)
                    .selectedOptions(currentSelectedOptionsJson) // ⭐ [수정] currentSelectedOptionsJson 사용 ⭐
                    .build();
        }
        savedCartItem = cartItemRepository.save(savedCartItem);
        recalculateCartTotalPrice(cart);

        return AddCart.Response.builder()
                .cartItemId(savedCartItem.getCartItemId())
                .cakeItemId(savedCartItem.getCakeItem().getCakeId())
                .cname(savedCartItem.getCakeItem().getCname())
                .productCnt(savedCartItem.getProductCnt())
                .itemTotalPrice(savedCartItem.getItemTotalPrice())
                .selectedOptions(savedCartItem.getSelectedOptions())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCart.Response getCart(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElse(null);

        if (cart == null) {
            return GetCart.Response.builder()
                    .items(List.of())
                    .cartTotalPrice(0L)
                    .build();
        }
        List<CartItem> cartItemEntities = cartItemRepository.findByCart(cart);

        List<GetCart.ItemInfo> cartItemDtos = cartItemEntities.stream()
                .map(entity -> {
                    List<CreateOrder.SelectedOptionDetail> selectedOptionDetails = new ArrayList<>();
                    String selectedOptionsJson = entity.getSelectedOptions(); // CartItem 엔티티에서 JSON 문자열 가져옴

                    if (selectedOptionsJson != null && !selectedOptionsJson.isEmpty()) {
                        try {
                            // JSON 문자열을 List<AddCart.CartItemOption> 형태로 역직렬화 (저장 시 사용한 DTO)
                            List<AddCart.CartItemOption> parsedOptions = objectMapper.readValue(selectedOptionsJson,
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, AddCart.CartItemOption.class));

                            for (AddCart.CartItemOption option : parsedOptions) {
                                // AddCart.CartItemOption DTO에는 이미 optionName과 optionPrice가 포함되어 있습니다.
                                // CakeOptionMapping을 다시 조회할 필요 없이 이 정보를 사용합니다.
                                selectedOptionDetails.add(CreateOrder.SelectedOptionDetail.builder()
                                        .mappingId(option.getOptionItemId()) // optionItemId를 mappingId로 사용
                                        .optionName(option.getOptionName()) // ⭐ 옵션 이름 설정! ⭐
                                        .price(option.getOptionPrice()) // ⭐ 옵션 가격 설정! ⭐
                                        .count(option.getOptionCnt())
                                        .build());
                            }
                        } catch (JsonProcessingException e) {
                            log.error("장바구니 아이템 옵션 JSON 파싱 실패 (cartItemId: {}): {}", entity.getCartItemId(), e.getMessage());
                            // 파싱 실패 시 옵션 정보는 비워둠.
                        }
                    }

                    // GetCart.ItemInfo DTO를 빌드하여 반환
                    return GetCart.ItemInfo.builder()
                            .cartItemId(entity.getCartItemId())
                            .cakeId(entity.getCakeItem().getCakeId())
                            .cname(entity.getCakeItem().getCname())
                            .price(entity.getCakeItem().getPrice())
                            .thumbnailImageUrl(entity.getCakeItem().getThumbnailImageUrl())
                            .productCnt(entity.getProductCnt())
                            .itemTotalPrice(entity.getItemTotalPrice()) // ⭐ 엔티티에서 계산된 itemTotalPrice를 그대로 사용 ⭐
                            .shopId(entity.getCakeItem().getShop().getShopId())
                            .selectedOptions(selectedOptionDetails) // 변환된 List<CreateOrder.SelectedOptionDetail> 설정
                            .build();
                })
                .collect(Collectors.toList());

        return GetCart.Response.builder()
                .items(cartItemDtos)
                .cartTotalPrice(cart.getCartTotalPrice() != null ? cart.getCartTotalPrice().longValue() : 0L)
                .build();
    }

    @Override
    public UpdateCartItem.Response updateCartItem(String userId,UpdateCartItem.Request requestDto) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CART_ID, "해당 사용자의 장바구니를 찾을 수 없습니다."));


        CartItem existingCartItem = cartItemRepository
                .findByCartAndCartItemIdWithCakeItem(cart, requestDto.getCartItemId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND_CART_ID,
                        "ID " + requestDto.getCartItemId() +
                                "에 해당하는 장바구니 아이템을 찾을 수 없거나, 사용자 소유가 아닙니다."
                ));

        int newCnt = requestDto.getProductCnt();
        if (newCnt < 1 || newCnt > 99) {
            throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED, "장바구니 수량은 1~99 사이여야 합니다.");
        }

        Long optionsTotalPrice = 0L;
        if (existingCartItem.getSelectedOptions() != null && !existingCartItem.getSelectedOptions().isEmpty()) {
            try {
                List<AddCart.CartItemOption> parsedOptions = objectMapper.readValue(existingCartItem.getSelectedOptions(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, AddCart.CartItemOption.class));
                optionsTotalPrice = parsedOptions.stream()
                        .mapToLong(option -> (long) (option.getOptionPrice() != null ? option.getOptionPrice() : 0) * (option.getOptionCnt() != null ? option.getOptionCnt() : 1))
                        .sum();
            } catch (JsonProcessingException e) {
                log.error("장바구니 아이템 업데이트 시 옵션 정보 JSON 파싱 실패: {}", e.getMessage(), e);
                optionsTotalPrice = 0L;
            }
        }

        long newItemTotalPrice = (long) existingCartItem.getCakeItem().getPrice() * newCnt + optionsTotalPrice;


        CartItem updatedCartItem = CartItem.builder()
                .cartItemId(existingCartItem.getCartItemId())
                .cart(existingCartItem.getCart())
                .cakeItem(existingCartItem.getCakeItem())
                .productCnt(newCnt)
                .itemTotalPrice(newItemTotalPrice)
                .selectedOptions(existingCartItem.getSelectedOptions())
                .build();

        updatedCartItem = cartItemRepository.save(updatedCartItem);
        recalculateCartTotalPrice(cart);

        return UpdateCartItem.Response.builder()
                .cartItemId(updatedCartItem.getCartItemId())
                .updatedProductCnt(updatedCartItem.getProductCnt())
                .updatedItemTotalPrice(updatedCartItem.getItemTotalPrice())
                .message("장바구니 상품 ID " + updatedCartItem.getCartItemId() + "의 수량이 " + newCnt + "개로 변경되었습니다.")
                .build();
    }

    @Override
    public DeletedCartItem.Response deleteCartItem(String userId, Long cartItemId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElse(null);

        if (cart == null) {
            return DeletedCartItem.Response.builder()
                    .deletedCartItemIds(List.of())
                    .message("삭제할 장바구니가 없습니다.")
                    .build();
        }

        CartItem itemToDelete = cartItemRepository.findByCartAndCartItemIdWithCakeItem(cart, cartItemId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CART_ITEMS,
                        "ID " + cartItemId + "에 해당하는 장바구니 아이템을 찾을 수 없거나, 사용자 소유가 아닙니다."
                ));

        cartItemRepository.delete(itemToDelete);
        recalculateCartTotalPrice(cart);

        return DeletedCartItem.Response.builder()
                .deletedCartItemIds(List.of(cartItemId))
                .message(userId + " 사용자의 장바구니에 있던 상품 ID " + cartItemId + "가 삭제되었습니다.")
                .build();
    }

    @Override
    public void deleteAllCartItems(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        Cart cart = cartRepository.findByMember(member)
                .orElse(null);

        if (cart == null) {
            return;
        }

        cartItemRepository.deleteAllByCart_CartId(cart.getCartId());
        recalculateCartTotalPrice(cart);
    }

}