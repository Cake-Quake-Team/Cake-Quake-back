package com.cakequake.cakequakeback.review.service.cake;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;

public interface CakeReviewService {

    //특정 케이크의 리뷰 전체 조회
    InfiniteScrollResponseDTO<ReviewResponseDTO> getCakeItemReviews(Long cakeItemId, PageRequestDTO pageRequestDTO);

    //특정 케이크의 리뷰 단건 조회
    ReviewResponseDTO getReview(Long reviewId, Long cakeItemId);
}
