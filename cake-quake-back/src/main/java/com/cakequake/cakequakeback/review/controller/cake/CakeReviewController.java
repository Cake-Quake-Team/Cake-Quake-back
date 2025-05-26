package com.cakequake.cakequakeback.review.controller.cake;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.service.cake.CakeReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cakes/{cakeItemId}/reviews")
public class CakeReviewController {
    private CakeReviewService cakeReviewService;

    @GetMapping
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getReviews(
            PageRequestDTO pageRequestDTO,
            @PathVariable Long cakeItemId) {
        return cakeReviewService.getCakeItemReviews(cakeItemId, pageRequestDTO);
    }
}
