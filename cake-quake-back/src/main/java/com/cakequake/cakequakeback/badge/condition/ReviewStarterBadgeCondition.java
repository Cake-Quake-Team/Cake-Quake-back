package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.review.repo.buyer.BuyerReviewRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.REVIEW_STARTER_BADGE_ID;

@Component
@RequiredArgsConstructor
// 리뷰 스타터
public class ReviewStarterBadgeCondition implements BadgeCondition {

    private final BuyerReviewRepo buyerReviewRepo;

    @Override
    public Long getBadgeId() {
        return REVIEW_STARTER_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member) {
        // 회원의 리뷰 개수를 확인하여 조건 충족 여부 반환
        long totalReviews = buyerReviewRepo.countByMemberUid(member.getUid());
        return totalReviews == 1; // 첫 번째 리뷰일 경우 뱃지 획득 자격 부여
    }
}
