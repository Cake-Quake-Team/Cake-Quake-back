package com.cakequake.cakequakeback.review.validator;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.entities.ReviewStatus;
import com.cakequake.cakequakeback.review.repo.buyer.BuyerReviewRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BuyerReviewValidator {

    private final BuyerOrderRepository orderRepo;
    private final CakeOrderItemRepository orderItemRepo;
    private final BuyerReviewRepo reviewRepo;

    /** 주문이 존재하고, uid가 일치하는지 검증 */
    public CakeOrder validateOrderBelongsToUser(Long orderId, Long uid) {
        return orderRepo.findByOrderIdAndMemberUid(orderId, uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID));
    }

    /** 리뷰 중복 작성 방지 검증 */
    public void validateNotReviewedYet(Long orderId, Long cakeId) {
        if (reviewRepo.findByOrderOrderIdAndCakeItemCakeId(orderId, cakeId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED_ORDER);
        }
    }

    /** 해당 주문에 지정된 cakeId 아이템이 있는지 검증 */
    public CakeOrderItem validateOrderItemExists(Long orderId, Long cakeId) {
        List<CakeOrderItem> items = orderItemRepo.findByCakeOrder_OrderId(orderId);
        return items.stream()
                .filter(it -> it.getCakeItem().getCakeId().equals(cakeId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ORDER_ID));
    }

    /** 리뷰 존재 여부 및 소유권 검증 */
    public Review validateReviewOwnership(Long reviewId, Long uid) {
        Review r = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID));
        if (!r.getMember().getUid().equals(uid)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }
        return r;
    }

    public Review validateNotDeleted(Review review) {
        if(review.getStatus() == ReviewStatus.DELETED) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_DELETED);
        }
        return review;
    }

}
