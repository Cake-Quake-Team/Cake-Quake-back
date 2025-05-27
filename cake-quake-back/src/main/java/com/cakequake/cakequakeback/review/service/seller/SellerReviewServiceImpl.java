package com.cakequake.cakequakeback.review.service.seller;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.dto.ReplyRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.*;
import com.cakequake.cakequakeback.review.repo.request.ReviewDeletionRequestRepo;
import com.cakequake.cakequakeback.review.repo.seller.SellerReviewRepo;

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

    private SellerReviewRepo sellerReviewRepo;
    private ReviewDeletionRequestRepo reviewDeletionRequestRepo;


    //매장 전체 리뷰 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getShopReviews(PageRequestDTO pageRequestDTO, Long shopId) {
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
    public ReviewResponseDTO getReview(Long reviewId, Long shopId) {
        Review review = sellerReviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID));
        if(!review.getCakeItem().getShop().getShopId().equals(shopId)){
            throw new BusinessException(ErrorCode.NO_SHOP_ACCESS);
        }
        return sellerReviewRepo.selectDTO(reviewId);
    }

    //리뷰에 답글 쓰기
    @Override
    public void replyToReview(Long reviewId, ReplyRequestDTO dto, Long shopId) {
        Review review = sellerReviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID));
        if(!review.getCakeItem().getShop().getShopId().equals(shopId)){
            throw new BusinessException(ErrorCode.NO_SHOP_ACCESS);
        }

        CeoReview cr = review.getCeoReview();
        if(cr == null) {
            cr = CeoReview.builder()
                    .review(review)
                    .build();
            review.setCeoReview(cr);
        }
        cr.setReply(dto.getReply());
    }

    @Override
    public void requestDeletion(Long shopId, Long reviewId, String reason) {

        Review review = sellerReviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID));
        // 매장 권한 체크
        if (!review.getCakeItem().getShop().getShopId().equals(shopId)) {
            throw new BusinessException(ErrorCode.NO_SHOP_ACCESS);
        }
        // 중복 요청 방지
        if (reviewDeletionRequestRepo.findByReview_ReviewId(reviewId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETION_REQUEST);
        }

        review.requestDelete();

        ReviewDeletionRequest req = ReviewDeletionRequest.builder()
                .review(review)
                .status(DeletionRequestStatus.PENDING)
                .reason(reason)
                .build();
        reviewDeletionRequestRepo.save(req);
    }


}
