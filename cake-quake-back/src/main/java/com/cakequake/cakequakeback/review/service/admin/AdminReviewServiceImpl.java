package com.cakequake.cakequakeback.review.service.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.point.service.PointService;
import com.cakequake.cakequakeback.review.dto.ReviewDeletionRequestDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.entities.ReviewDeletionRequest;
import com.cakequake.cakequakeback.review.event.ReviewChangedEvent;
import com.cakequake.cakequakeback.review.repo.request.ReviewDeletionRequestRepo;
import com.cakequake.cakequakeback.review.validator.AdminReviewValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewDeletionRequestRepo reviewDeletionRequestRepo;
    private final AdminReviewValidator validator;
    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher;

    //삭제 요청 리뷰 전체 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ReviewDeletionRequestDTO> listRequest(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("regDate");
        Page<ReviewDeletionRequest> page = reviewDeletionRequestRepo.findAllRequest(pageable);

        List<ReviewDeletionRequestDTO> dtos = page.stream()
                .map(r -> ReviewDeletionRequestDTO.builder()
                        .requestId(r.getRequestId())
                        .reviewId(r.getReview().getReviewId())
                        .status(r.getStatus().name())
                        .reason(r.getReason())
                        .regDate(r.getRegDate())
                        .reviewContent(r.getReview().getContent())
                        // Review → Shop 관계에서 매장명 가져오기
                        .shopName(r.getReview().getShop().getShopName())
                        .build()
                )
                .collect(Collectors.toList());

        return InfiniteScrollResponseDTO.<ReviewDeletionRequestDTO>builder()
                .content(dtos)
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
                .build();
    }

    //요청 승인
    @Override
    public void approveDeletion(Long requestId) {
        // 검증: 존재 & PENDING
        ReviewDeletionRequest req = validator.validatePendingRequest(requestId);

        //상태 변경  PENDING -> APPROVE
        req.approve();

        Review review =req.getReview();
        review.softDelete();

        //작성자에게 지급된 포인트 만큼 차감
        Long reviewUid = review.getMember().getUid();
        boolean hadImage = review.getReviewPictureUrl() != null;
        long amountToDeduct = hadImage? 1000L:500L;
        String desc = "리뷰 삭제로 인한 포인트 차감";
        pointService.changePoint(reviewUid,-amountToDeduct,desc);


        // 4) Shop 통계를 갱신하기 위한 이벤트 발행 (삭제 승인 시점)
        eventPublisher.publishEvent(
                new ReviewChangedEvent(this, review.getShop().getShopId())
        );
    }

    @Override
    public void rejectDeletion(Long requestId) {
        // 검증: 존재 & PENDING
        ReviewDeletionRequest req = validator.validatePendingRequest(requestId);

        //상태 변경  PENDING -> REJECT
        req.reject();

        //리뷰 삭제 요청 취소
        Review review =req.getReview();
        review.cancelDeleteRequest();
    }
}
