package com.cakequake.cakequakeback.review.seller;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.dto.ReplyRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.*;
import com.cakequake.cakequakeback.review.repo.request.ReviewDeletionRequestRepo;
import com.cakequake.cakequakeback.review.repo.seller.SellerReviewRepo;
import com.cakequake.cakequakeback.review.service.seller.SellerReviewServiceImpl;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerReviewTest {

    @Mock
    private SellerReviewRepo sellerReviewRepo;

    @Mock
    private ReviewDeletionRequestRepo reviewDeletionRequestRepo;

    @InjectMocks
    private SellerReviewServiceImpl sellerReviewServiceImpl;

    //  ---------getshopReviews - 매장 전체 리뷰 조회 테스트------
    @Test
    @DisplayName("getShopReviews: 페이지당 2개 요청 시  hasNext = tree , totalCount =3")
    void getShopReviews() {
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(2).build();
        Long shopId = 100L;

        List<ReviewResponseDTO> content = List.of(
                new ReviewResponseDTO(1L, 10L, 20L, "케이크A", 5, "좋아요", null, LocalDateTime.now(), LocalDateTime.now(), null),
                new ReviewResponseDTO(2L, 11L, 21L, "케이크B", 4, "괜찮아요", null, LocalDateTime.now(), LocalDateTime.now(), null)
        );
        Page<ReviewResponseDTO> page =new PageImpl<>(
                content,
                pageRequestDTO.getPageable("regDate"),
                3
        );

        given(sellerReviewRepo.listOfShopReviews(shopId,pageRequestDTO.getPageable("regDate")))
                .willReturn(page);


        //실행
        InfiniteScrollResponseDTO<ReviewResponseDTO> result = sellerReviewServiceImpl.getShopReviews(pageRequestDTO,shopId);

        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getTotalCount()).isEqualTo(3);

    }

    // getReview 매장 리뷰 단건 조회
    @Test
    @DisplayName("getReview:정상 조회 시 DTO 반환")
    void getReview() {
        Long reviewId = 50L;
        Long shopId = 100L;

        Shop shop = Shop.builder()
                .shopId(shopId)
                .build();
        //cakeItem의 shop과 위의 shop이 같아야 함
        CakeItem cake = CakeItem.builder()
                .shop(shop)
                .build();
        Review review = Review.builder()
                .reviewId(reviewId)
                .cakeItem(cake)
                .status(ReviewStatus.ACTIVE)
                .build();

        ReviewResponseDTO dto = new ReviewResponseDTO(
                reviewId, 1L, 2L, "케이크",5,"맛있어요",null,
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        given(sellerReviewRepo.findById(reviewId)).willReturn(Optional.of(review));
        given(sellerReviewRepo.selectDTO(reviewId)).willReturn(dto);

        ReviewResponseDTO result = sellerReviewServiceImpl.getReview(reviewId,shopId);

        assertThat(result).isEqualTo(dto);

    }

    // replyToReview : 답글 쓰기
    @Test
    @DisplayName("replyToReview: 신규 CeoReview 생성 후 reply 세팅")
    void replyToReview() {
        Long reviewId = 50L;
        Long shopId = 100L;
        String text = "감사합니다";

        Shop shop = Shop.builder()
                .shopId(shopId)
                .build();
        Review review = Review.builder()
                .reviewId(reviewId)
                .cakeItem(CakeItem.builder().shop(shop).build())
                .status(ReviewStatus.ACTIVE)
                .build();

        given(sellerReviewRepo.findById(reviewId)).willReturn(Optional.of(review));

        sellerReviewServiceImpl.replyToReview(reviewId, new ReplyRequestDTO(text),shopId);

        assertThat(review.getCeoReview()).isNotNull();
        assertThat(review.getCeoReview().getReply()).isEqualTo(text);
    }

    //requestDeletion 판매자가 삭제 요청
    @Test
    @DisplayName("requestDeletion: PENDING 요청 생성")
    void requestDeletion(){
        Long reviewId = 50L;
        Long shopId = 100L;
        String reason = "부적절한 내용";

        Shop shop = Shop.builder()
                .shopId(shopId)
                .build();
        Review review = Review.builder()
                .reviewId(reviewId)
                .cakeItem(CakeItem.builder().shop(shop).build())
                .status(ReviewStatus.ACTIVE)
                .build();

        given(sellerReviewRepo.findById(reviewId))
                .willReturn(Optional.of(review));
        given(reviewDeletionRequestRepo.findByReview_ReviewId(reviewId))
                .willReturn(Optional.empty());

        sellerReviewServiceImpl.requestDeletion(shopId,reviewId,reason);

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.DELTE_REQUEST);

        then(reviewDeletionRequestRepo).should().save(any(ReviewDeletionRequest.class));
    }

    // --- 7) 삭제 요청: 중복 요청 방지 ---
    @Test
    @DisplayName("requestDeletion: 이미 요청된 경우 ALREADY_DELETION_REQUEST")
    void requestDeletion_alreadyRequested() {
        Long reviewId = 8L;
        Long shopId = 30L;

        ReviewDeletionRequest existing = ReviewDeletionRequest.builder()
                .review(Review.builder().reviewId(reviewId).build())
                .build();

        given(sellerReviewRepo.findById(reviewId))
                .willReturn(Optional.of(Review.builder()
                        .reviewId(reviewId)
                        .cakeItem(CakeItem.builder().shop(Shop.builder().shopId(shopId).build()).build())
                        .build()));
        given(reviewDeletionRequestRepo.findByReview_ReviewId(reviewId))
                .willReturn(Optional.of(existing));

        assertThatThrownBy(() -> sellerReviewServiceImpl.requestDeletion(shopId, reviewId, ""))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.ALREADY_DELETION_REQUEST);
    }

    // --- 8) 삭제 요청: 권한 없음 ---
    @Test
    @DisplayName("requestDeletion: 다른 매장 리뷰 요청 시 NO_SHOP_ACCESS")
    void requestDeletion_noAccess() {
        Long reviewId = 9L;
        Long shopId = 40L;

        Review review = Review.builder()
                .reviewId(reviewId)
                .cakeItem(CakeItem.builder().shop(Shop.builder().shopId(999L).build()).build())
                .build();

        given(sellerReviewRepo.findById(reviewId))
                .willReturn(Optional.of(review));

        assertThatThrownBy(() -> sellerReviewServiceImpl.requestDeletion(shopId, reviewId, ""))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NO_SHOP_ACCESS);
    }
}


