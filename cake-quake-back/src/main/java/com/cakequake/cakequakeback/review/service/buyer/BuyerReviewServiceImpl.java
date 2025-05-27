package com.cakequake.cakequakeback.review.service.buyer;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.repo.buyer.BuyerReviewRepo;
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
public class BuyerReviewServiceImpl implements BuyerReviewService {

    private BuyerReviewRepo buyerReviewRepo;
    //private OrderRepo orderRepo;

    public BuyerReviewServiceImpl(BuyerReviewRepo buyerReviewRepo) {
        this.buyerReviewRepo = buyerReviewRepo;
    }


    //구매자 리뷰 추가
    @Override
    public ReviewResponseDTO createReview(Long orderId, ReviewRequestDTO dto, Long userId) {

        log.info("--------------review created-------------");

        //해당 주문서가 존재하지 않거나 권한이 없는 경우
//        CakeOrder order = orderRepo.findByIdAndUserId(orderId,userId)
//        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID));

        //이미 리뷰 작성 여부 체크
//        if(reviewRepo.findByOrderId(orderId).isPresent()){
//            throw new BusinessException(ErrorCode.ALREADY_REVIEWED_ORDER);
//        }

        //DTO -> Entity 매핑
        Review review = Review.builder()
                //.order(order)
                //.user(order.getUser())
                //.cakeItem(order.getCakeItem())
                .rating(dto.getRating())
                .content(dto.getContent())
                .reviewPictureUrl(dto.getReviewPictureUrl())
                //.regDate(LocalDateTime.now())
                .build();

        Review savedReview = buyerReviewRepo.save(review);
        log.info("Saved review: " + savedReview.getReviewId());

        // Repo 프로젝션으로 바로 DTO 반환
        ReviewResponseDTO response = buyerReviewRepo.selectDTO(savedReview.getReviewId());

        return response;
    }

    //구매자 전체 리뷰 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getMyReviews(PageRequestDTO pageRequestDTO, Long userId) {

        //pageRequestDTO로 부터 Pageable생성 (regDate 내림차순)
        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<ReviewResponseDTO> page = buyerReviewRepo.listOfUserReviews(userId, pageable);

        //DTO로 변환
        return InfiniteScrollResponseDTO.<ReviewResponseDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements() )
                .build();
    }

    //구매자 리뷰 단건 조회
    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReview(Long reviewId, Long uid) {
        //리뷰 존재 여부 체크
        Review review = buyerReviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID));
        //본인 리뷰인지 검증
        if (!review.getUser().getUid().equals(uid)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        return buyerReviewRepo.selectDTO(reviewId);

    }
    //구매자가 자신 리뷰 수정
    @Override
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO dto, Long uid) {
        //리뷰 존재 여부 체크
        Review review = buyerReviewRepo.findById(reviewId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID)
                );
        //본인 리뷰인지 검증
        if (!review.getUser().getUid().equals(uid)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        //수정 가능한 필드만 수정하기
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setReviewPictureUrl(dto.getReviewPictureUrl());

        buyerReviewRepo.save(review);

        return buyerReviewRepo.selectDTO(reviewId);
    }

    //리뷰 삭제
    @Override
    public void deleteReview(Long reviewId, Long uid) {
        //리뷰 존재 여부 체크
        Review review = buyerReviewRepo.findById(reviewId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID)
                );
        //본인 리뷰인지 검증
        if (!review.getUser().getUid().equals(uid)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        //삭제 -> 상태만 DELETE로 변경
        review.deleteByBuyer();
    }
}
