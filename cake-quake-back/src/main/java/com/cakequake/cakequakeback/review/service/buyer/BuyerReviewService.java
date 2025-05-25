package com.cakequake.cakequakeback.review.service.buyer;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;

public interface BuyerReviewService {

    //리뷰 생성후 응답용 DTO 전체 반환
    ReviewResponseDTO createReview(Long orderId, ReviewRequestDTO dto, Long userId);

    //구매자 내 리뷰 전체 조회 (무한 스크롤)
    InfiniteScrollResponseDTO<ReviewResponseDTO> getMyReviews(PageRequestDTO pageRequestDTO, Long userId);

    //내 리뷰 상세 조회
    ReviewResponseDTO getReview(Long reviewId, Long userId);

    //리뷰 업데이트
    ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO dto, Long userId);

    // 리뷰 삭제
    void deleteReview(Long reviewId, Long userId);
}
