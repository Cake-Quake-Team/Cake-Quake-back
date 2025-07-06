package com.cakequake.cakequakeback.badge.validator;

import com.cakequake.cakequakeback.badge.entities.Badge;
import com.cakequake.cakequakeback.badge.entities.MemberBadge;
import com.cakequake.cakequakeback.badge.repo.BadgeRepository;
import com.cakequake.cakequakeback.badge.repo.MemberBadgeRepository;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberDetail;
import com.cakequake.cakequakeback.member.repo.MemberDetailRepository;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadgeValidator {

    private final MemberRepository memberRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final MemberDetailRepository memberDetailRepository;

    // 회원 정보 조회
    public Member validateMember(Long uid) {
        return memberRepository.findById(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 뱃지 정보 조회
    public Badge validateBadge(Long badgeId) {
        return badgeRepository.findByBadgeId(badgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_BADGE_ID));
    }

    // 회원이 해당 뱃지를 소유하고 있는지 확인
    public MemberBadge validateMemberBadge(Member member, Badge newProfileBadge) {
        return memberBadgeRepository.findByMemberAndBadge(member, newProfileBadge)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_BADGE_ID));
    }

    // 회원 상세 정보 조회
    public MemberDetail validateMemberDetail(Long uid) {
        return memberDetailRepository.findById(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
