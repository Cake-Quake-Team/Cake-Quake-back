package com.cakequake.cakequakeback.review.service.seller;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReplyRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.*;
import com.cakequake.cakequakeback.review.repo.request.ReviewDeletionRequestRepo;
import com.cakequake.cakequakeback.review.repo.seller.SellerReviewRepo;

import com.cakequake.cakequakeback.review.validator.SellerReviewValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class SellerReviewServiceImpl implements SellerReviewService {

    private final SellerReviewRepo sellerReviewRepo;
    private final ReviewDeletionRequestRepo reviewDeletionRequestRepo;
    private final SellerReviewValidator validator;


    //매장 전체 리뷰 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getShopReviews(PageRequestDTO pageRequestDTO, Long shopId) {
        //shopId 검증 추가
        validator.validateShopExists(shopId);

        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<ReviewResponseDTO> page = sellerReviewRepo.listOfShopReviews(shopId, pageable);

        return InfiniteScrollResponseDTO.<ReviewResponseDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
                .build();
    }

    //매장 리뷰 상세 보기
    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReview(Long shopId, Long reviewId) {
        // 존재 + 권한 검증
        Review review = validator.validateReviewBelongsToShop(reviewId, shopId);
        // 소프트 삭제 여부 검증
        validator.validateNotDeleted(review);

        return sellerReviewRepo.selectDTO(reviewId);
    }

    //리뷰에 답글 쓰기
    @Override
    public void replyToReview(Long reviewId, ReplyRequestDTO dto, Long shopId) {

        Review review = validator.validateReviewBelongsToShop(reviewId, shopId);
        validator.validateNotDeleted(review);

        CeoReview cr = review.getCeoReview();
        if(cr == null) {
            cr = CeoReview.builder()
                    .review(review)
                    .build();
            review.updateCeoReview(cr);
        }
        cr.updateReply(dto.getReply());
    }

    @Override
    public void requestDeletion(Long shopId, Long reviewId, String reason) {

        Review review = validator.validateReviewBelongsToShop(reviewId, shopId);
        validator.validateNotDeleted(review);
        validator.validateNoExistingDeletionRequest(reviewId);

        //소프트 삭제 요청 상태 전환
        review.requestDelete();

        ReviewDeletionRequest req = ReviewDeletionRequest.builder()
                .review(review)
                .status(DeletionRequestStatus.PENDING)
                .reason(reason)
                .build();
        reviewDeletionRequestRepo.save(req);
    }

}
