package com.cakequake.cakequakeback.review.controller;

import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.repo.ReviewRepo;
import com.cakequake.cakequakeback.review.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService reviewService;
    private ReviewRepo reviewRepo;

    public ReviewController(ReviewRepo reviewRepo, ReviewService reviewService) {
        this.reviewRepo = reviewRepo;
        this.reviewService = reviewService;
    }

    /**
     * 리뷰 작성
     * 이전: POST /api/buyers/profile/orders/{orderId}/reviews
     * 변경: POST /api/orders/{orderId}/reviews
     */
    @PostMapping("/orders/{orderId}/reviews")
    @ResponseStatus(HttpStatus.CREATED) //성공하면 201 Created 상태 코드가 자동 적용
    public ReviewResponseDTO createReview(
            @PathVariable Long orderId,
            ReviewRequestDTO dto,
            Long userId){
        return reviewService.createReview(orderId, dto, userId);

    }


}
