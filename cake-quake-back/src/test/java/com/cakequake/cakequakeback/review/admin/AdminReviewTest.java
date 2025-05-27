package com.cakequake.cakequakeback.review.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.entities.ReviewDeletionRequest;
import com.cakequake.cakequakeback.review.entities.DeletionRequestStatus;
import com.cakequake.cakequakeback.review.entities.ReviewStatus;
import com.cakequake.cakequakeback.review.repo.request.ReviewDeletionRequestRepo;
import com.cakequake.cakequakeback.review.service.admin.AdminReviewServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminReviewTest {

    @Mock
    private ReviewDeletionRequestRepo repo;

    @InjectMocks
    private AdminReviewServiceImpl service;

    @Test
    @DisplayName("listRequest: 페이지당 2개 요청 시 hasNext=true, totalCount=3")
    void listRequest_returnsInfiniteScrollResponse() {
        // given
        PageRequestDTO dto = PageRequestDTO.builder().page(1).size(2).build();
        Pageable pageable = dto.getPageable("regDate");

        ReviewDeletionRequest r1 = ReviewDeletionRequest.builder()
                .requestId(1L)
                .status(DeletionRequestStatus.PENDING)
                .review(Review.builder().reviewId(10L).status(ReviewStatus.DELETED).build())
                .build();
        ReviewDeletionRequest r2 = ReviewDeletionRequest.builder()
                .requestId(2L)
                .status(DeletionRequestStatus.PENDING)
                .review(Review.builder().reviewId(11L).status(ReviewStatus.DELETED).build())
                .build();

        Page<ReviewDeletionRequest> page = new PageImpl<>(
                List.of(r1, r2),
                pageable,
                3
        );

        given(repo.findAllRequest(any(Pageable.class))).willReturn(page);

        // when
        InfiniteScrollResponseDTO<ReviewDeletionRequest> result =
                service.listRequest(dto);

        // then
        assertThat(result.getContent()).containsExactly(r1, r2);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getTotalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("approveDeletion: 존재하는 요청이면 요청 상태=APPROVED, 리뷰 상태=DELETED")
    void approveDeletion_success() {
        // given
        Long reqId = 100L;
        Review review = Review.builder()
                .reviewId(50L)
                .status(ReviewStatus.DELTE_REQUEST)
                .build();
        ReviewDeletionRequest req = ReviewDeletionRequest.builder()
                .requestId(reqId)
                .status(DeletionRequestStatus.PENDING)
                .review(review)
                .build();

        given(repo.findById(reqId)).willReturn(Optional.of(req));

        // when
        service.approveDeletion(reqId);

        // then
        assertThat(req.getStatus()).isEqualTo(DeletionRequestStatus.APPROVED);
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.DELETED);
    }

    @Test
    @DisplayName("approveDeletion: 없는 요청이면 BusinessException(DELETION_REQUEST_NOT_FOUND)")
    void approveDeletion_notFound() {
        // given
        given(repo.findById(anyLong())).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.approveDeletion(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DELETION_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("rejectDeletion: 존재하는 요청이면 요청 상태=REJECTED, 리뷰 상태=ACTIVE")
    void rejectDeletion_success() {
        // given
        Long reqId = 200L;
        Review review = Review.builder()
                .reviewId(51L)
                .status(ReviewStatus.DELTE_REQUEST)  // 삭제 요청 상태였으므로 DELETED 로 가정
                .build();
        ReviewDeletionRequest req = ReviewDeletionRequest.builder()
                .requestId(reqId)
                .status(DeletionRequestStatus.PENDING)
                .review(review)
                .build();

        given(repo.findById(reqId)).willReturn(Optional.of(req));

        // when
        service.rejectDeletion(reqId);

        // then
        assertThat(req.getStatus()).isEqualTo(DeletionRequestStatus.REJECTED);
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.ACTIVE);
    }

    @Test
    @DisplayName("rejectDeletion: 없는 요청이면 BusinessException(DELETION_REQUEST_NOT_FOUND)")
    void rejectDeletion_notFound() {
        given(repo.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.rejectDeletion(888L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DELETION_REQUEST_NOT_FOUND);
    }
}
