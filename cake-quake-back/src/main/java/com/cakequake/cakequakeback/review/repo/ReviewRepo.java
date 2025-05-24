package com.cakequake.cakequakeback.review.repo;

import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepo extends JpaRepository<Review, Integer> {

    //Optional<Review> findByOrderId(Long orderId);

    //엔티티 없이 DTO 직접 반환 -> 단건 조회
    @Query("SELECT  new com.cakequake.cakequakeback.review.dto.ReviewResponseDTO(" +
            "r.reviewId ," +
            "r.order.orderId," +
            "r.cakeItem.cakeId, r.cakeItem.cname," +
            "r.rating, r.content, r.reviewPictureUrl, r.createdAt, cr.content) " +
            "FROM Review r " +
            "LEFT JOIN r.ceoReview cr " +
            "WHERE r.reviewId = :reviewId")
    ReviewResponseDTO selectDTO(@Param("reviewId") Long reviewId);
}
