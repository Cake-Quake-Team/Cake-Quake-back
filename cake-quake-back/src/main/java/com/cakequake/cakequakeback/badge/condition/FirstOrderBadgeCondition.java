package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.FIRST_ORDER_BADGE_ID;

@Component
@RequiredArgsConstructor
// 첫 오더 달성
public class FirstOrderBadgeCondition implements BadgeCondition {

    private final BuyerOrderRepository buyerOrderRepository;

    @Override
    public Long getBadgeId() {
        return FIRST_ORDER_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member) {
        long completedOrdersCount = buyerOrderRepository.countByMemberUidAndStatus(member.getUid(), OrderStatus.RESERVATION_PENDING);
        return completedOrdersCount >= 1; // 완료된 주문이 1개 이상이면 자격 부여
    }
}