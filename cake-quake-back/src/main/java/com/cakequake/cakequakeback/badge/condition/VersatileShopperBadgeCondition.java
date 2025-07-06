package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.VERSATILE_SHOPPER_BADGE_ID;

@Component
@RequiredArgsConstructor
// 다재다능 쇼퍼
public class VersatileShopperBadgeCondition implements BadgeCondition {

    private final CakeOrderItemRepository cakeOrderItemRepository;

    @Override
    public Long getBadgeId() {
        return VERSATILE_SHOPPER_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member) {
        final int REQUIRED_DISTINCT_CATEGORIES = 5; // 필요한 고유 카테고리 개수

        // 회원이 픽업 완료한 주문에서 구매한 고유한 케이크 카테고리 수를 조회
        Long distinctCategoriesCount = cakeOrderItemRepository.countDistinctCategoriesByMemberUidAndOrderStatus(
                member.getUid(),
                OrderStatus.PICKUP_COMPLETED // 픽업 완료된 주문만 대상으로 함
        );

        // distinctCategoriesCount가 null일 경우 0으로 처리 (아직 구매 내역이 없을 경우)
        if (distinctCategoriesCount == null) {
            distinctCategoriesCount = 0L;
        }

        return distinctCategoriesCount >= REQUIRED_DISTINCT_CATEGORIES;
    }
}