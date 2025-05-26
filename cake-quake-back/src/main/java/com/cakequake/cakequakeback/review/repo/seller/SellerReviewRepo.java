package com.cakequake.cakequakeback.review.repo.seller;

import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.repo.common.CommonReviewRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerReviewRepo extends CommonReviewRepo {

    //구매자가 작성한 리뷰 전체 조화(페이징)
    @Query("SELECT new com.cakequake.cakequakeback.review.dto.ReviewResponseDTO(" +
            "r.reviewId ," +
            "r.order.orderId," +
            "r.cakeItem.cakeId, r.cakeItem.cname,"+
            "r.rating, r.content, r.reviewPictureUrl, r.regDate, cr.reply) " +
            "FROM Review r " +
            "LEFT JOIN r.ceoReview cr " +
            "WHERE r.cakeItem.shop.shopId = :shopId AND r.status = com.cakequake.cakequakeback.review.entities.ReviewStatus.ACTIVE")
    Page<ReviewResponseDTO> listOfShopReviews(@Param("shopId") Long shopId, Pageable pageable);



}
