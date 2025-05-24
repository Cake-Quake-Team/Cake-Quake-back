package com.cakequake.cakequakeback.review.service;

import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;

public interface ReviewService {

    //리뷰 생성후 응답용 DTO 전체 반환
    ReviewResponseDTO createReview(Long orderId, ReviewRequestDTO dto, Long userId);


}
