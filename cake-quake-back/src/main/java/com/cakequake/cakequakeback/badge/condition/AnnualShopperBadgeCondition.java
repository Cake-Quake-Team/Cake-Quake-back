package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.ANNUAL_SHOPPER_BADGE_ID;

@Component
@RequiredArgsConstructor
// 연간 쇼퍼
public class AnnualShopperBadgeCondition implements BadgeCondition {

    private final BuyerOrderRepository buyerOrderRepository;

    @Override
    public Long getBadgeId() {
        return ANNUAL_SHOPPER_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member) {
        if (member == null) {
            return false;
        }

        // 회원의 모든 'PICKUP_COMPLETED' 상태의 주문 조회
        List<CakeOrder> completedOrders = buyerOrderRepository.findByMemberUidAndStatus(member.getUid(), OrderStatus.PICKUP_COMPLETED);

        // 최소 연속 월 수보다 완료된 주문이 적으면 바로 false 반환 (최소 12개 이상의 주문이 있어야 함)
        if (completedOrders.size() < 12) {
            return false;
        }

        // 주문 완료 날짜에서 '년-월'만 추출하여 중복을 제거하고 정렬된 Set에 저장
        // TreeSet을 사용하여 YearMonth 객체를 자동으로 정렬하고 중복을 제거합니다.
        Set<YearMonth> purchaseMonths = new TreeSet<>();
        for (CakeOrder order : completedOrders) {
            // LocalDateTime 타입의 regDate를 YearMonth로 변환
            purchaseMonths.add(YearMonth.from(order.getRegDate().atZone(ZoneId.systemDefault())));
        }

        // 연속된 월이 있는지 확인
        if (purchaseMonths.size() < 12) {
            return false; // 고유한 구매 월의 수가 부족하면 바로 false
        }

        int consecutiveCount = 0;
        YearMonth previousMonth = null;

        // TreeSet 덕분에 purchaseMonths는 이미 오름차순으로 정렬되어 있습니다.
        for (YearMonth currentMonth : purchaseMonths) {
            if (previousMonth != null) {
                // 이전 월의 다음 월이 현재 월과 같으면 연속
                if (currentMonth.equals(previousMonth.plusMonths(1))) {
                    consecutiveCount++;
                } else {
                    // 연속이 끊기면 카운트 초기화 (현재 월부터 다시 시작)
                    consecutiveCount = 0;
                }
            }
            // 연속 카운트가 12 - 1 (즉, 11)에 도달하면 12개월 연속 구매 달성
            if (consecutiveCount >= 11) {
                return true;
            }
            previousMonth = currentMonth;
        }

        return false;
    }
}