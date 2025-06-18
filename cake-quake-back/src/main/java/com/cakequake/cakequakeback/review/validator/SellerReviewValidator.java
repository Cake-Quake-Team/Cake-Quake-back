package com.cakequake.cakequakeback.review.validator;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.entities.ReviewStatus;
import com.cakequake.cakequakeback.review.repo.request.ReviewDeletionRequestRepo;
import com.cakequake.cakequakeback.review.repo.seller.SellerReviewRepo;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerReviewValidator {

    private final SellerReviewRepo reviewRepo;
    private final ReviewDeletionRequestRepo deletionReqRepo;
    private final ShopRepository shopRepo;

    /** 리뷰가 존재하고, shopId와 일치하는지 검증 */
    public Review validateReviewBelongsToShop(Long reviewId, Long shopId) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID));
        if (!review.getCakeItem().getShop().getShopId().equals(shopId)) {
            throw new BusinessException(ErrorCode.NO_SHOP_ACCESS);
        }
        return review;
    }

    /** shopId가 실제로 존재하는지 검증 */
    public void validateShopExists(Long shopId) {
        if (!shopRepo.existsById(shopId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID);
        }
    }

    /** 이미 삭제 요청된 리뷰인지 검증 */
    public void validateNoExistingDeletionRequest(Long reviewId) {
        if (deletionReqRepo.findByReview_ReviewId(reviewId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETION_REQUEST);
        }
    }


    /** 소프트 삭제된 리뷰는 모든 접근에서 차단 */
    public void validateNotDeleted(Review review) {
        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_DELETED);
        }
    }

}
