package com.cakequake.cakequakeback.review.repo.cake;

import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//케이트 단일 조회
public interface CakeReviewRepo extends JpaRepository<Review, Long> {
    @Query("SELECT new com.cakequake.cakequakeback.review.dto.ReviewResponseDTO(" +
            "r.reviewId ," +
            "r.order.orderId," +
            "r.cakeItem.cakeId, r.cakeItem.cname,"+
            "r.rating, r.content, r.reviewPictureUrl, r.regDate,r.modDate, cr.reply) " +
            "FROM Review r " +
            "LEFT JOIN r.ceoReview cr " +
            "WHERE r.cakeItem.cakeId = :cakeId AND r.status = com.cakequake.cakequakeback.review.entities.ReviewStatus.ACTIVE")
    Page<ReviewResponseDTO> listOfCakeReviews(
            @Param("cakeId") Long cakeItemId,
            Pageable pageable
    );


}
