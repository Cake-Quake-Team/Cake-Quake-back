package com.cakequake.cakequakeback.review.service.seller;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReplyRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.CeoReview;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.repo.seller.SellerReviewRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class SellerReviewServiceImpl implements SellerReviewService {

    private SellerReviewRepo sellerReviewRepo;


    public SellerReviewServiceImpl( SellerReviewRepo sellerReviewRepo) {
        this.sellerReviewRepo = sellerReviewRepo;
    }

    //매장 전체 리뷰 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getShopReviews(PageRequestDTO pageRequestDTO, Long shopId) {
        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<ReviewResponseDTO> page = sellerReviewRepo.listOfShopReviews(shopId, pageable);

        return InfiniteScrollResponseDTO.<ReviewResponseDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
                .build();
    }

    //매장 리뷰 상세 보기
    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReview(Long reviewId, Long shopId) {
        Review review = sellerReviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(1004, "해당 리뷰를 찾을 수 없습니다."));
        if(!review.getCakeItem().getShop().getId().equals(shopId)){
            throw new BusinessException(907,"해당 매장에 대한 권한한이 없습니다");
        }
        return sellerReviewRepo.selectDTO(reviewId);
    }

    //리뷰에 답글 쓰기
    @Override
    public void replyToReview(Long reviewId, ReplyRequestDTO dto, Long shopId) {
        Review review = sellerReviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(1004, "해당 리뷰를 찾을 수 없습니다."));
//        if(!review.getCakeItem().getShop().getId().equals(shopId)){
//            throw new BusinessException(907,"해당 매장에 대한 권한한이 없습니다");
//        }

        CeoReview cr = review.getCeoReview();
        if(cr == null) {
            cr = CeoReview.builder()
                    .review(review)
                    .build();
            review.setCeoReview(cr);
        }
        cr.setReply(dto.getReply());
    }
}
