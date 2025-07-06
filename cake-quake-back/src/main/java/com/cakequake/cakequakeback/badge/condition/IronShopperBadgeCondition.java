package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.IRON_SHOPPER_BADGE_ID;

@Component
@RequiredArgsConstructor
// 철벽 쇼퍼
public class IronShopperBadgeCondition implements BadgeCondition {

    private final BuyerOrderRepository buyerOrderRepository;

    @Override
    public Long getBadgeId() {
        return IRON_SHOPPER_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member) {
        if (member == null) {
            return false;
        }

        // 현재 시점으로부터 1년 전의 날짜 계산 (기준 시점)
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        LocalDateTime now = LocalDateTime.now(); // 현재 시점

        // 취소(RESERVATION_CANCELLED) 및 노쇼(NO_SHOW) 상태 목록 정의
        List<OrderStatus> negativeStatuses = Arrays.asList(
                OrderStatus.RESERVATION_CANCELLED,
                OrderStatus.NO_SHOW
        );

        // 회원의 주문 이력에 지난 1년 동안 취소 또는 노쇼 상태의 주문이 하나라도 있는지 확인
        boolean hasNegativeHistoryInLastYear = buyerOrderRepository.existsByMemberUidAndStatusInAndRegDateAfter(
                member.getUid(),
                negativeStatuses,
                oneYearAgo
        );

        // 만약 지난 1년 동안 취소나 노쇼 이력이 하나라도 있다면, 뱃지 부여 자격 없음
        if (hasNegativeHistoryInLastYear) {
            return false;
        }

        // 취소/노쇼 이력이 없다면, 지난 1년 동안 픽업 완료된 주문의 총 개수를 확인
        long completedOrdersCountInLastYear = buyerOrderRepository.countByMemberUidAndStatusAndRegDateBetween(
                member.getUid(),
                OrderStatus.PICKUP_COMPLETED,
                oneYearAgo,
                now
        );

        // 지난 1년 동안 픽업 완료된 주문 개수가 기준치를 넘는지 확인
        return completedOrdersCountInLastYear >= 5;
    }
}