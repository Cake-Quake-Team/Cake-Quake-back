package com.cakequake.cakequakeback.review.controller.cake;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.service.cake.CakeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cakes/{cakeItemId}/reviews")
@RequiredArgsConstructor
public class CakeReviewController {
    private final CakeReviewService cakeReviewService;

    @GetMapping
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getReviews(
            PageRequestDTO pageRequestDTO,
            @PathVariable Long cakeItemId) {
        return cakeReviewService.getCakeItemReviews(cakeItemId, pageRequestDTO);
    }

    @GetMapping("/{reviewId}")
    public ReviewResponseDTO getReview(
            @PathVariable Long reviewId,
            @PathVariable Long cakeItemId
    ) {
        return cakeReviewService.getReview(cakeItemId,reviewId);
    };
}
