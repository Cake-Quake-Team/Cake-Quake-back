package com.cakequake.cakequakeback.review.repo.buyer;

import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.repo.common.CommonReviewRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BuyerReviewRepo extends CommonReviewRepo {

    //orderId읽어 오기
    @Query("SELECT r FROM Review r WHERE r.order.orderId = :orderId")
    Optional<Review> findByOrderId(@Param("orderId") Long orderId);

    Optional<Review> findByOrderOrderIdAndCakeItemCakeId(Long orderId,Long cakeId);

    //구매자가 작성한 리뷰 전체 조화(페이징)
    @Query("SELECT new com.cakequake.cakequakeback.review.dto.ReviewResponseDTO(" +
            "r.reviewId ," +
            "r.order.orderId," +
            "r.cakeItem.cakeId, r.cakeItem.cname,"+
            "r.rating, r.content, r.reviewPictureUrl, r.regDate,r.modDate, cr.reply) " +
            "FROM Review r " +
            "LEFT JOIN r.ceoReview cr " +
            "WHERE r.member.uid = :uid AND r.status = com.cakequake.cakequakeback.review.entities.ReviewStatus.ACTIVE")
    Page<ReviewResponseDTO> listOfUserReviews(@Param("uid") Long userId, Pageable pageable);

}
