package com.cakequake.cakequakeback.badge.repo;

import com.cakequake.cakequakeback.badge.entities.Badge;
import com.cakequake.cakequakeback.badge.entities.MemberBadge;
import com.cakequake.cakequakeback.member.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {

    // 회원과 특정 뱃지 조합의 MemberBadge 조회
    Optional<MemberBadge> findByMemberAndBadge(Member member, Badge badge);

    // 회원의 대표 뱃지(isRepresentative = true) MemberBadge 조회
    Optional<MemberBadge> findByMemberAndIsRepresentative(Member member, boolean isRepresentative);

    // 회원이 보유한 모든 뱃지 조회
    List<MemberBadge> findByMember(Member member);

    // 회원이 특정 뱃지를 획득했는지 여부 확인
    boolean existsByMemberUidAndBadgeBadgeId(Long memberUid, Long badgeBadgeId);
}
