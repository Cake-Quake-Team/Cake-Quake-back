package com.cakequake.cakequakeback.review.service.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.entities.ReviewDeletionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReviewService {

    //전체 삭제 요청 조회
    InfiniteScrollResponseDTO<ReviewDeletionRequest> listRequest(PageRequestDTO pageRequestDTO);

    //요청 승인
    void approveDeletion(Long requestId);


    //요청 거절
    void rejectDeletion(Long requestId);

}
