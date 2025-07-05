package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.SHOPPING_RUSH_BADGE_ID;

@Component
@RequiredArgsConstructor
// 쇼핑 러쉬
public class ShoppingRushBadgeCondition implements BadgeCondition {

    private final BuyerOrderRepository buyerOrderRepository;

    @Override
    public Long getBadgeId() {
        return SHOPPING_RUSH_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member){
        if (member == null) {
            return false;
        }

        // 회원의 모든 'PICKUP_COMPLETED' 상태의 주문 조회
        List<CakeOrder> completedOrders = buyerOrderRepository.findByMemberUidAndStatus(member.getUid(), OrderStatus.PICKUP_COMPLETED);

        if (completedOrders.size() < 3) {
            // 연속 구매 일수보다 완료된 주문이 적으면 바로 false 반환
            return false;
        }

        // 주문 완료 날짜만 추출하여 중복을 제거하고 정렬된 Set에 저장
        // TreeSet을 사용하여 날짜를 자동으로 정렬하고 중복을 제거합니다.
        Set<LocalDate> purchaseDates = new TreeSet<>();
        for (CakeOrder order : completedOrders) {
            // LocalDateTime 타입의 regDate를 LocalDate로 변환 (시간 정보 제거)
            // 시스템 기본 시간대(ZoneId.systemDefault())를 고려하여 정확한 날짜 추출
            purchaseDates.add(order.getRegDate().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        // 3. 연속된 날짜가 있는지 확인
        int consecutiveCount = 0;
        LocalDate previousDate = null;

        for (LocalDate currentDate : purchaseDates) {
            if (previousDate != null) {
                // 이전 날짜의 다음 날이 현재 날짜와 같으면 연속
                if (currentDate.equals(previousDate.plusDays(1))) {
                    consecutiveCount++;
                } else {
                    // 연속이 끊기면 카운트 초기화 (현재 날짜부터 다시 시작)
                    consecutiveCount = 0;
                }
            }
            if (consecutiveCount >= 2) {
                return true;
            }
            previousDate = currentDate;
        }
        return false;
    }
}
