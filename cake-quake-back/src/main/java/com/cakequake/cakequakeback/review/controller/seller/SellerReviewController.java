package com.cakequake.cakequakeback.review.controller.seller;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReplyRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.repo.seller.SellerReviewRepo;
import com.cakequake.cakequakeback.review.service.seller.SellerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shops/{shopId}/reviews")
@RequiredArgsConstructor
public class SellerReviewController {
    private final SellerReviewService sellerReviewService;


    //매장 리뷰 전체 조회
    @GetMapping
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getShopReviews(
            PageRequestDTO pageRequestDTO,
            @PathVariable Long shopId
            //@AuthenticationPrincipal Long uesrId
            ) {
        return sellerReviewService.getShopReviews(pageRequestDTO, shopId);
    };

    //매장 리뷰 단건 상세 조회
    @GetMapping("/{reviewId}")
    public ReviewResponseDTO getReview(
            @PathVariable Long reviewId,
            @PathVariable Long shopId) {
        return sellerReviewService.getReview(shopId,reviewId);
    }

    //리뷰에 답글 달기
    @PostMapping("/{reviewId}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    public void reply(
            @PathVariable Long reviewId,
            @PathVariable Long shopId,
            @RequestBody @Valid ReplyRequestDTO replyRequestDTO
    ){
        sellerReviewService.replyToReview(reviewId,replyRequestDTO,shopId);
    }

    //리뷰 삭제 요청
    @PostMapping("/{reviewId}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    public void requestDelete(
            @PathVariable Long shopId,
            @PathVariable Long reviewId,
            @RequestParam String reason
    ){
        sellerReviewService.requestDeletion(shopId,reviewId,reason);
    }

}
