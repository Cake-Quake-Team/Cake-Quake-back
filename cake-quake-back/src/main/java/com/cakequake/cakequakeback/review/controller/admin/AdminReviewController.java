package com.cakequake.cakequakeback.review.controller.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.entities.ReviewDeletionRequest;
import com.cakequake.cakequakeback.review.service.admin.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/review-deletion-request")
@RequiredArgsConstructor
public class AdminReviewController {
    private final AdminReviewService adminReviewService;

    //전체 요청 조회
    @GetMapping
    public InfiniteScrollResponseDTO<ReviewDeletionRequest> getReviews(
            PageRequestDTO pageRequestDTO
    ){
        return adminReviewService.listRequest(pageRequestDTO);
    }

    //요청 승인
    @PatchMapping("/{requestId}/approve")
    public void approve(@PathVariable Long requestId) {
        adminReviewService.approveDeletion(requestId);
    }


    //요청 거절
    @PatchMapping("/{requestId}/reject")
    public void reject(@PathVariable Long requestId) {
        adminReviewService.rejectDeletion(requestId);
    }


}
