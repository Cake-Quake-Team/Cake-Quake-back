package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.REGULAR_VIP_BADGE_ID;

@Component
@RequiredArgsConstructor
// 단골 VIP
public class RegularVipBadgeCondition implements BadgeCondition {

    private final BuyerOrderRepository buyerOrderRepository;

    @Override
    public Long getBadgeId() {
        return REGULAR_VIP_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member) {
        long completedOrdersCount = buyerOrderRepository.countByMemberUidAndStatus(member.getUid(), OrderStatus.PICKUP_COMPLETED);
        return completedOrdersCount >= 20; // 완료된 주문이 1개 이상이면 자격 부텨
    }
}
