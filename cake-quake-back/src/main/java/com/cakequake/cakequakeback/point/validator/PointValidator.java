package com.cakequake.cakequakeback.point.validator;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.point.entities.Point;
import com.cakequake.cakequakeback.point.repo.PointRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointValidator {
    private final MemberRepository memberRepo;
    private final PointRepo pointRepo;

    /** 회원이 존재하는지 검증하고 Member 반환 */
    public Member validateMemberExists(Long uid) {
        return memberRepo.findById(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /** Point 엔티티를 조회하거나, 없으면 생성 후 반환 */
    public Point getOrCreatePoint(Member member) {
        return pointRepo.findByMemberUid(member.getUid())
                .orElseGet(() -> {
                    Point p = new Point();
                    p.createMember(member);
                    p.updateTotalPoints(0L);
                    return pointRepo.save(p);
                });
    }
}
