package com.cakequake.cakequakeback.badge.service;

import com.cakequake.cakequakeback.badge.dto.AcquiredBadgeDTO;
import com.cakequake.cakequakeback.badge.dto.MemberBadgeDTO;
import com.cakequake.cakequakeback.badge.entities.Badge;
import com.cakequake.cakequakeback.badge.entities.MemberBadge;
import com.cakequake.cakequakeback.badge.repo.BadgeRepository;
import com.cakequake.cakequakeback.badge.repo.MemberBadgeRepository;
import com.cakequake.cakequakeback.badge.validator.BadgeValidator;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberDetail;
import com.cakequake.cakequakeback.member.repo.MemberDetailRepository;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final BadgeValidator badgeValidator;

    @Override
    // 대표 뱃지 설정
    public void setProfileBadge(Long uid, Long badgeId) {

        Member member = badgeValidator.validateMember(uid);
        Badge newProfileBadge = badgeValidator.validateBadge(badgeId);
        MemberBadge memberNewBadge = badgeValidator.validateMemberBadge(member, newProfileBadge);
        MemberDetail memberDetail = badgeValidator.validateMemberDetail(uid);

        // 기존 대표 뱃지 해제 (Optional: 현재 대표 뱃지가 있다면)
        Optional<MemberBadge> currentRepresentativeBadgeOpt = memberBadgeRepository.findByMemberAndIsRepresentative(member, true);
        currentRepresentativeBadgeOpt.ifPresent(mb -> {
            mb.deactivateRepresentative(); // isRepresentative를 false로 변경하는 메서드 호출
            memberBadgeRepository.save(mb); // 변경사항 저장
        });

        // 새로운 뱃지를 대표 뱃지로 설정
        memberNewBadge.activateRepresentative(); // isRepresentative를 true로 변경하는 메서드 호출
        memberBadgeRepository.save(memberNewBadge); // 변경사항 저장

        // MemberDetail 업데이트
        memberDetail.changeProfileBadge(newProfileBadge.getName()); // 뱃지 이름으로 업데이트
        memberDetailRepository.save(memberDetail);
    }

    @Override
    // 뱃지 획득
    public void acquireBadge(Long uid, Long badgeId) {

        Member member = badgeValidator.validateMember(uid);
        Badge badgeToAcquire  = badgeValidator.validateBadge(badgeId);

        // 기존 뱃지 획득 여부 확인
        Optional<MemberBadge> existingMemberBadge = memberBadgeRepository.findByMemberAndBadge(member, badgeToAcquire);

        if (existingMemberBadge.isPresent()) {
            // 이미 획득한 뱃지인 경우
            throw new BusinessException(ErrorCode.BADGE_ALREADY_ACQUIRED);
        }

        // MemberBadge 생성 및 저장
        MemberBadge newMemberBadge = MemberBadge.builder()
                .member(member)
                .badge(badgeToAcquire)
                .build();

        memberBadgeRepository.save(newMemberBadge);
    }

    @Override
    // 뱃지 전체 목록 조회
    public List<AcquiredBadgeDTO> getAllBadgesWithAcquisitionStatus(Long uid) {
        // 모든 뱃지 정보 조회
        List<Badge> allBadges = badgeRepository.findAll();

        Member member = badgeValidator.validateMember(uid);

        // 해당 회원이 획득한 뱃지 정보 조회
        List<MemberBadge> acquiredMemberBadges = memberBadgeRepository.findByMember(member);

        Map<Long, MemberBadge> acquiredBadgeMap = acquiredMemberBadges.stream()
                .collect(Collectors.toMap(mb -> mb.getBadge().getBadgeId(), mb -> mb));

        //모든 뱃지를 순회하며 획득 여부 및 획득일 정보 추가
        return allBadges.stream()
                .map(badge -> {
                    // Map에서 현재 뱃지 ID에 해당하는 MemberBadge 찾기
                    MemberBadge memberBadge = acquiredBadgeMap.get(badge.getBadgeId());

                    boolean acquired = (memberBadge != null); // MemberBadge가 존재하면 획득한 것
                    LocalDateTime acquiredDate = null;

                    if (acquired) {
                        acquiredDate = memberBadge.getAcquiredDate(); // MemberBadge에서 획득일 가져옴
                    }
                    return AcquiredBadgeDTO.fromEntity(badge, acquired, acquiredDate);
                })
                .collect(Collectors.toList());
    }

    @Override
    // 획득한 뱃지 목록 조회
    public List<MemberBadgeDTO> getMemberBadges(Long uid) {

        Member member = badgeValidator.validateMember(uid);

        List<MemberBadge> memberBadges = memberBadgeRepository.findByMember(member);

        return memberBadges.stream()
                .map(MemberBadgeDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
