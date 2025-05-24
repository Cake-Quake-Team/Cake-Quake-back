package com.cakequake.cakequakeback.review.service;

import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.repo.ReviewRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReviewServiceImpl implements ReviewService{

    private ReviewRepo  reviewRepo;
//    private OrderRepo orderRepo;


    //구매자 리뷰 추가
    @Override
    public ReviewResponseDTO createReview(Long orderId, ReviewRequestDTO dto, Long userId) {

        log.info("--------------review created-------------");

        //해당 주문서가 존재하지 않거나 권한이 없는 경우
        //Order order = orderRepo.findByIdAndUserId(orderId,userId)
        //.orElseThrow(()-> new BusinessException(1008, "해당 주문 정보를 찾을 수 없거나 권한이 없습니다."));

//        //이미 리뷰 작성 여부 체크
//        if(reviewRepo.findByOrederId(orderId).isPersent()){
//            throw new BussinessException(708, "해당 주문에는 이미 리뷰가 작성 되었습니다.");
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

        Review savedReview = reviewRepo.save(review);
        log.info("Saved review: " + savedReview.getReviewId());

        // Repo 프로젝션으로 바로 DTㅒ 반환
        ReviewResponseDTO response = reviewRepo.selectDTO(savedReview.getReviewId());

        return response;
    }
}
