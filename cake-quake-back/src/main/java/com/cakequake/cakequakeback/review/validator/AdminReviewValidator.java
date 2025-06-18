package com.cakequake.cakequakeback.review.validator;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.entities.DeletionRequestStatus;
import com.cakequake.cakequakeback.review.entities.ReviewDeletionRequest;
import com.cakequake.cakequakeback.review.repo.request.ReviewDeletionRequestRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminReviewValidator {
    private final ReviewDeletionRequestRepo reqRepo;

    /** 요청이 존재하는지, PENDING 상태인지 검증 */
    public ReviewDeletionRequest validatePendingRequest(Long requestId) {
        ReviewDeletionRequest req = reqRepo.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELETION_REQUEST_NOT_FOUND));
        if (req.getStatus() != DeletionRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_CANCELLED_REQUEST);
        }
        return req;
    }
}
