package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.LUXURY_SHOPPER_BADGE_ID;

@Component
@RequiredArgsConstructor
// 럭셔리 쇼퍼
public class LuxuryShopperBadgeCondition implements BadgeCondition {

    private final BuyerOrderRepository buyerOrderRepository;

    @Override
    public Long getBadgeId() {
        return LUXURY_SHOPPER_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member) {
        if (member == null) {
            return false;
        }

        // 회원의 모든 픽업 완료 상태의 주문들의 총 금액을 합산
        Long totalPurchasedAmount = buyerOrderRepository.sumOrderTotalPriceByMemberUidAndStatus(
                member.getUid(),
                OrderStatus.PICKUP_COMPLETED
        );
        if (totalPurchasedAmount == null) {
            totalPurchasedAmount = 0L;
        }

        return totalPurchasedAmount >= 1_000_000; // 100만 원
    }
}