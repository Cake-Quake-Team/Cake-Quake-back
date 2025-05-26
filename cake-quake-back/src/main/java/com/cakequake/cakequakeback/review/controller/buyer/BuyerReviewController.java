package com.cakequake.cakequakeback.review.controller.buyer;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.repo.buyer.BuyerReviewRepo;
import com.cakequake.cakequakeback.review.service.buyer.BuyerReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BuyerReviewController {
    private final BuyerReviewService buyerReviewService;
    private BuyerReviewRepo buyerReviewRepo;

    public BuyerReviewController(BuyerReviewRepo buyerReviewRepo, BuyerReviewService buyerReviewService) {
        this.buyerReviewRepo = buyerReviewRepo;
        this.buyerReviewService = buyerReviewService;
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
            @Valid ReviewRequestDTO dto,
            @AuthenticationPrincipal Long userId){
        return buyerReviewService.createReview(orderId, dto, userId);

    }

    /**
     * 내 리뷰 전체 조회 (무한 스크롤)
     * GET /api/buyers/reviews?page=1&size=10
     */
    @GetMapping("buyers/reviews")
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getBuyerReviews(
            @AuthenticationPrincipal Long userId,
            PageRequestDTO pageRequestDTO
    ){
        return buyerReviewService.getMyReviews(pageRequestDTO, userId);
    }

    /**
     * 내 리뷰 단건 조회
     * GET /api/buyers/reviews/{reviewId}
     */
    @GetMapping("buyers/reviews/{reviewId}")
    public ReviewResponseDTO getMyReview(@PathVariable Long reviewId, @AuthenticationPrincipal Long userId){
        return buyerReviewService.getReview(reviewId, userId);
    }

    /**
     * 내 리뷰 수정
     * PATCH /api/buyers/reviews/{reviewId}
     */
    @PatchMapping("/buyers/reviews/{reviewId}")
    public ReviewResponseDTO updateReview(
            @PathVariable Long reviewId,
            @Valid ReviewRequestDTO reviewRequestDTO,
            @AuthenticationPrincipal Long userId
    ){
        return buyerReviewService.updateReview(reviewId, reviewRequestDTO, userId);
    }

    /**
     * 내 리뷰 삭제
     * DELETE /api/buyers/reviews/{reviewId}
     */
    @DeleteMapping("/buyers/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long reviewId, @AuthenticationPrincipal Long userId){
        buyerReviewService.deleteReview(reviewId, userId);
    }


}
