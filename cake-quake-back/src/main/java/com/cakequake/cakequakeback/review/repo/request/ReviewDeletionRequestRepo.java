package com.cakequake.cakequakeback.review.repo.request;

import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.ReviewDeletionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewDeletionRequestRepo extends JpaRepository<ReviewResponseDTO, Long> {


    /** 특정 리뷰에 대한 삭제 요청 조회 */
    @Query("SELECT rdr FROM ReviewDeletionRequest rdr WHERE rdr.review.reviewId = :reviewId")
    Optional<ReviewDeletionRequest> findByReview_ReviewId(Long reviewId);

    /** 관리자용: 전체 요청 페이징 조회 */
    @Query("SELECT rdr FROM ReviewDeletionRequest rdr ORDER BY rdr.requestedAt DESC")
    Page<ReviewDeletionRequest> findAllRequest(Pageable pageable);

}
