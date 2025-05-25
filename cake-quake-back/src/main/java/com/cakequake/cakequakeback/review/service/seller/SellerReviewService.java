package com.cakequake.cakequakeback.review.service.seller;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReplyRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;

public interface SellerReviewService {

    //매장 전체 리뷰 조회
    InfiniteScrollResponseDTO<ReviewResponseDTO> getShopReviews(PageRequestDTO pageRequestDTO, Long shopId);

    //매장 리뷰 단건 조회
    ReviewResponseDTO getReview(Long reviewId, Long shopId);

    //판매자 답글 작성
    void replyToReview(Long reviewId, ReplyRequestDTO dto, Long shopId);

}
