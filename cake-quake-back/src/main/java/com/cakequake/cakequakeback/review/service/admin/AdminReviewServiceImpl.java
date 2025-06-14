package com.cakequake.cakequakeback.review.service.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.entities.ReviewDeletionRequest;
import com.cakequake.cakequakeback.review.repo.request.ReviewDeletionRequestRepo;
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
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewDeletionRequestRepo reviewDeletionRequestRepo;

    //삭제 요청 리뷰 전체 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ReviewDeletionRequest> listRequest(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("regDate");
        Page<ReviewDeletionRequest> page = reviewDeletionRequestRepo.findAllRequest(pageable);

        log.info(" ● Repo.findAllRequest: 페이지 size={}, totalElements={}",
                page.getNumberOfElements(), page.getTotalElements());


        return InfiniteScrollResponseDTO.<ReviewDeletionRequest>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
                .build();
    }

    //요청 승인
    @Override
    public void approveDeletion(Long requestId) {
        ReviewDeletionRequest req = reviewDeletionRequestRepo.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELETION_REQUEST_NOT_FOUND));
        //상태 변경  PENDING -> APPROVE
        req.approve();

        Review review =req.getReview();
        review.softDelete();
    }

    @Override
    public void rejectDeletion(Long requestId) {
        ReviewDeletionRequest req = reviewDeletionRequestRepo.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELETION_REQUEST_NOT_FOUND));
        //상태 변경  PENDING -> REJECT
        req.reject();

        Review review =req.getReview();
        review.cancelDeleteRequest();
    }
}
