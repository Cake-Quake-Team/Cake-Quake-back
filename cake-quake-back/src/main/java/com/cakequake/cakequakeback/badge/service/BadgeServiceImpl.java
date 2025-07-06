package com.cakequake.cakequakeback.badge.service;

import com.cakequake.cakequakeback.badge.condition.BadgeCondition;
import com.cakequake.cakequakeback.badge.constants.BadgeConstants;
import com.cakequake.cakequakeback.badge.dto.AcquiredBadgeDTO;
import com.cakequake.cakequakeback.badge.dto.MemberBadgeDTO;
import com.cakequake.cakequakeback.badge.dto.RepresentativeBadgeResponseDTO;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Log4j2
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final BadgeValidator badgeValidator;
    private final Map<Long, BadgeCondition> badgeConditionsMap;

    public BadgeServiceImpl(
            BadgeRepository badgeRepository,
            MemberBadgeRepository memberBadgeRepository,
            MemberDetailRepository memberDetailRepository,
            BadgeValidator badgeValidator,
            List<BadgeCondition> badgeConditions
    ) {
        this.badgeRepository = badgeRepository;
        this.memberBadgeRepository = memberBadgeRepository;
        this.memberDetailRepository = memberDetailRepository;
        this.badgeValidator = badgeValidator;

        // 주입받은 BadgeCondition 리스트를 Map<Long, BadgeCondition>으로 변환하여 저장
        // Map의 키는 뱃지 ID, 값은 해당 BadgeCondition 객체가 됩니다.
        this.badgeConditionsMap = badgeConditions.stream()
                .collect(Collectors.toMap(BadgeCondition::getBadgeId, Function.identity()));
        log.info("초기화된 뱃지 조건 개수: {}", this.badgeConditionsMap.size());
    }

    @Override
    // 대표 뱃지 설정
    public RepresentativeBadgeResponseDTO setProfileBadge(Long uid, Long badgeId) {

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
        memberDetail.changeProfileBadge(newProfileBadge.getIcon());
        memberDetailRepository.save(memberDetail);

        return RepresentativeBadgeResponseDTO.builder()
                .icon(newProfileBadge.getIcon())
                .name(newProfileBadge.getName())
                .build();
    }

    @Override
    public RepresentativeBadgeResponseDTO getProfileBadge(Long uid) {
        MemberDetail detail = memberDetailRepository.findById(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // profileBadge 필드가 null일 수도 있으니 방어 코드도 추가하면 좋음
        if (detail.getProfileBadge() == null) {
            return null;
        }

        // badge icon 이름을 기준으로 Badge 엔티티 다시 찾아서 name 추출
        Badge badge = badgeRepository.findByIcon(detail.getProfileBadge())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_BADGE_ID));

        return RepresentativeBadgeResponseDTO.builder()
                .icon(badge.getIcon())
                .name(badge.getName())
                .build();
    }

    @Override
    // 즉시 뱃지 획득
    public void acquireBadge(Long uid, Long badgeId) {
        Member member = badgeValidator.validateMember(uid);

        // 뱃지 ID에 해당하는 Badge 엔티티 조회
        Optional<Badge> badgeOpt = badgeRepository.findById(badgeId);
        if (badgeOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_BADGE_ID);
        }
        Badge badgeToAward = badgeOpt.get();

        // 이미 뱃지를 획득했는지 먼저 확인
        boolean alreadyAcquired = memberBadgeRepository.existsByMemberUidAndBadgeBadgeId(uid, badgeToAward.getBadgeId());
        if (alreadyAcquired) {
            log.debug("회원 UID {} 는 이미 뱃지 '{}'(ID: {})를 획득했습니다. 건너뜁니다.", uid, badgeToAward.getName(), badgeId);
            return;
        }

        // 뱃지 획득 조건 컴포넌트 가져오기
        BadgeCondition condition = badgeConditionsMap.get(badgeId);
        if (condition == null) {
            log.warn("뱃지 ID '{}'에 대한 획득 조건 정의를 찾을 수 없습니다. 부여 과정을 건너뜁니다.", badgeId);
            return;
        }

        // 조건이 충족되면 뱃지 부여
        if (condition.isEligible(member)) { // <-- Member 객체를 조건 검사 메서드에 전달
            MemberBadge newMemberBadge = MemberBadge.builder()
                    .member(member) // 조회한 Member 객체 사용
                    .badge(badgeToAward)
                    .acquiredDate(LocalDateTime.now())
                    .isRepresentative(false) // 기본값
                    .build();
            memberBadgeRepository.save(newMemberBadge);
        } else {
            log.debug("회원 UID {} 가 뱃지 '{}'(ID: {}) 획득 조건 미충족.", uid, badgeToAward.getName(), badgeId);
        }
    }

    @Override
    // 취소/노쇼 검사 후 뱃지 부여
    public void checkAndAcquireBadges(Long uid) {
        Member member = badgeValidator.validateMember(uid);

        Set<Long> acquiredBadgeIds = memberBadgeRepository.findByMember(member).stream()
                .map(mb -> mb.getBadge().getBadgeId())
                .collect(Collectors.toSet());

        Map<Long, Badge> allBadgesMap = badgeRepository.findAll().stream()
                .collect(Collectors.toMap(Badge::getBadgeId, Function.identity(), (existing, replacement) -> existing));

        for (Map.Entry<Long, BadgeCondition> entry : badgeConditionsMap.entrySet()) {
            Long badgeId = entry.getKey();
            BadgeCondition condition = entry.getValue();

            // 이미 획득한 뱃지인지 확인 (중복 부여 방지)
            if (acquiredBadgeIds.contains(badgeId)) {
                log.debug("회원 UID {} 는 뱃지 ID {}를 이미 획득했습니다. 건너뜀.", uid, badgeId);
                continue;
            }

            // 뱃지 획득 조건을 만족하는지 검사
            if (condition.isEligible(member)) {
                // 조건 만족 시 뱃지 부여 (DB에 저장)
                Badge badgeToAward = allBadgesMap.get(badgeId);
                if (badgeToAward == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_BADGE_ID);
                }

                MemberBadge newBadge = MemberBadge.builder()
                        .member(member)
                        .badge(badgeToAward)
                        .acquiredDate(LocalDateTime.now())
                        .isRepresentative(false)
                        .build();
                memberBadgeRepository.save(newBadge);
            } else {
                log.debug("회원 UID {} 가 뱃지 ID {} 획득 조건 미충족.", uid, badgeId);
            }
        }
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
