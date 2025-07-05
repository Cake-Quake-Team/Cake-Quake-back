package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.cakequake.cakequakeback.badge.constants.BadgeConstants.TRUSTED_SHOPPER_BADGE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
// 믿음의 쇼퍼
public class TrustedShopperBadgeCondition implements BadgeCondition {

    private final BuyerOrderRepository buyerOrderRepository;

    @Override
    public Long getBadgeId() {
        return TRUSTED_SHOPPER_BADGE_ID;
    }

    @Override
    public boolean isEligible(Member member) {
        if (member == null) {
            log.debug("TrustedShopperBadgeCondition - member is null. Returning false.");
            return false;
        }

        // 가입 후 1년이 경과했는지 확인
        LocalDateTime registrationDate = member.getRegDate(); // Member 엔티티의 가입일
        if (registrationDate == null) {
            log.warn("회원 UID {} 의 가입일(regDate)이 null입니다. 뱃지 부여 불가.", member.getUid());
            return false;
        }

        LocalDateTime oneYearAfterRegistration = registrationDate.plusYears(1);
        LocalDateTime now = LocalDateTime.now(); // 현재 시간

        if (now.isBefore(oneYearAfterRegistration)) {
            log.debug("회원 UID {} 는 가입 후 1년이 아직 경과하지 않았습니다 ({} < {}). 뱃지 부여 불가.",
                    member.getUid(), now, oneYearAfterRegistration);
            return false; // 1년이 지나지 않았다면 바로 false 반환
        }

        // 1년 경과 후 (또는 전체 기간 동안) 취소/노쇼 주문이 없는지 확인
        List<OrderStatus> negativeStatuses = Arrays.asList(
                OrderStatus.RESERVATION_CANCELLED,
                OrderStatus.NO_SHOW
        );

        boolean hasNegativeHistory = buyerOrderRepository.existsByMemberUidAndStatusInAndRegDateAfter(
                member.getUid(),
                negativeStatuses,
                registrationDate
        );

        return !hasNegativeHistory;
    }
}