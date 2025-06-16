package com.cakequake.cakequakeback.review.validator;

import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.repo.cake.CakeReviewRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CakeReviewValidator {
    private final CakeItemRepository cakeItemRepo;
    private final CakeReviewRepo cakeReviewRepo;

    /** 케이크 단품이 실제로 존재하는지 검증 */
    public void validateCakeExists(Long cakeItemId) {
        cakeItemRepo.findById(cakeItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT_ID));
    }

    /** 리뷰가 존재하고, 해당 케이크에 속해 있는지 검증 */
    public ReviewResponseDTO validateReviewBelongsToCake(Long reviewId, Long cakeItemId) {
        ReviewResponseDTO dto = cakeReviewRepo.selectDTO(reviewId);
        if (dto == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID);
        }
        if (!dto.getCakeId().equals(cakeItemId)) {
            throw new BusinessException(ErrorCode.MISSING_CAKE_ITEM_ID);
        }
        return dto;
    }
}
