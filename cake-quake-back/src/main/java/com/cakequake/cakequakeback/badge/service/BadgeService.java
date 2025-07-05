package com.cakequake.cakequakeback.badge.service;

import com.cakequake.cakequakeback.badge.dto.AcquiredBadgeDTO;
import com.cakequake.cakequakeback.badge.dto.MemberBadgeDTO;
import com.cakequake.cakequakeback.badge.dto.RepresentativeBadgeResponseDTO;

import java.util.List;

public interface BadgeService {

    // 대표 뱃지 설정
    RepresentativeBadgeResponseDTO setProfileBadge(Long uid, Long badgeId);

    // 대표 뱃지 조회
    RepresentativeBadgeResponseDTO getProfileBadge(Long uid);

    // 뱃지 즉시 획득
    void acquireBadge(Long uid, Long badgeId);

    // 취소/노쇼 조건 검사 후 뱃지 획득
    void checkAndAcquireBadges(Long uid);

    // 뱃지 전체 목록
    List<AcquiredBadgeDTO> getAllBadgesWithAcquisitionStatus(Long uid);

    // 획득한 뱃지 목록
    List<MemberBadgeDTO> getMemberBadges(Long uid);
}
