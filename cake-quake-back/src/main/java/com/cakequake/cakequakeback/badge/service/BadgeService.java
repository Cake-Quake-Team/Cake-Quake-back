package com.cakequake.cakequakeback.badge.service;

import com.cakequake.cakequakeback.badge.dto.AcquiredBadgeDTO;
import com.cakequake.cakequakeback.badge.dto.MemberBadgeDTO;

import java.util.List;

public interface BadgeService {

    // 대표 뱃지 설정
    void setProfileBadge(Long uid, Long badgeId);

    // 뱃지 획득
    void acquireBadge(Long uid, Long badgeId);

    // 뱃지 전체 목록
    List<AcquiredBadgeDTO> getAllBadgesWithAcquisitionStatus(Long uid);

    // 획득한 뱃지 목록
    List<MemberBadgeDTO> getMemberBadges(Long uid);
}
