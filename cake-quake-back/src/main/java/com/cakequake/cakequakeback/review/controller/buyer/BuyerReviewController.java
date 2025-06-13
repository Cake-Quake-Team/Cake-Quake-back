package com.cakequake.cakequakeback.review.controller.buyer;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.repo.buyer.BuyerReviewRepo;
import com.cakequake.cakequakeback.review.service.buyer.BuyerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Log4j2
public class BuyerReviewController {
    private final BuyerReviewService buyerReviewService;
    private final BuyerReviewRepo buyerReviewRepo;


    /**
     * 리뷰 작성
     * 이전: POST /api/buyers/profile/orders/{orderId}/reviews
     * 변경: POST /api/orders/{orderId}/reviews
     */
    @PostMapping("/orders/{orderId}/reviews")
    @ResponseStatus(HttpStatus.CREATED) //성공하면 201 Created 상태 코드가 자동 적용
    public ReviewResponseDTO createReview(
            @RequestHeader(value="Authorization", required=false) String authHeader,
            @PathVariable Long orderId,
            @ModelAttribute @Valid ReviewRequestDTO dto,
            @AuthenticationPrincipal(expression = "member.uid") Long uid
             ){
        log.info("▶ Authorization 헤더 = {}", authHeader);
        log.info("▶ principal uid = {}", uid);
        return buyerReviewService.createReview(orderId, dto, uid);

    }

    /**
     * 내 리뷰 전체 조회 (무한 스크롤)
     * GET /api/buyers/reviews?page=1&size=10 --postman ok
     */
    @GetMapping("buyers/reviews")
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getBuyerReviews(
            @AuthenticationPrincipal(expression = "member.uid") Long uid,
            PageRequestDTO pageRequestDTO
    ){

        return buyerReviewService.getMyReviews(pageRequestDTO, uid);
    }

    /**
     * 내 리뷰 단건 조회
     * GET /api/buyers/reviews/{reviewId} -- postman ok
     */
    @GetMapping("/buyers/reviews/{reviewId}")
    public ReviewResponseDTO getMyReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal(expression = "member.uid") Long uid
             ){
        return buyerReviewService.getReview(reviewId, uid);
    }

    /**
     * 내 리뷰 수정
     * PATCH /api/buyers/reviews/{reviewId}
     */
    @PatchMapping("/buyers/reviews/{reviewId}")
    public ReviewResponseDTO updateReview(
            @PathVariable Long reviewId,
            @Valid @ModelAttribute ReviewRequestDTO reviewRequestDTO,
            @AuthenticationPrincipal(expression = "member.uid") Long uid
    ){
        return buyerReviewService.updateReview(reviewId, reviewRequestDTO, uid);
    }

    /**
     * 내 리뷰 삭제
     * DELETE /api/buyers/reviews/{reviewId}
     */
    @DeleteMapping("/buyers/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal(expression = "member.uid") Long uid){

        log.info("▶ principal uid = {}", uid);
        buyerReviewService.deleteReview(reviewId, uid);
    }


}
