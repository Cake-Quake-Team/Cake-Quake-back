package com.cakequake.cakequakeback.badge.controller;

import com.cakequake.cakequakeback.badge.dto.AcquiredBadgeDTO;
import com.cakequake.cakequakeback.badge.dto.MemberBadgeDTO;
import com.cakequake.cakequakeback.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buyers/{uid}/badges")
@RequiredArgsConstructor
@Log4j2
public class BadgeController {

    private final BadgeService badgeService;

    @PutMapping("/profile")
    // 대표 뱃지 설정
    public ResponseEntity<Void> setProfileBadge(
            @PathVariable Long uid,
            @RequestBody Map<String, Long> requestBody) {

        Long badgeId = requestBody.get("badgeId");
        badgeService.setProfileBadge(uid, badgeId);

        return ResponseEntity.ok().build(); // 200 OK 반환
    }

    @PostMapping("/acquire")
    // 뱃지 획득
    public ResponseEntity<Void> acquireBadge(
            @PathVariable Long uid,
            @RequestBody Map<String, Long> requestBody) {

        Long badgeId = requestBody.get("badgeId");
        badgeService.acquireBadge(uid, badgeId);

        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created 반환
    }

    @GetMapping("/all")
    // 뱃지 전체 목록 조회
    public ResponseEntity<List<AcquiredBadgeDTO>> getAllBadgesWithAcquisitionStatus(@PathVariable Long uid) {
        List<AcquiredBadgeDTO> allBadgesWithStatus = badgeService.getAllBadgesWithAcquisitionStatus(uid);
        return ResponseEntity.ok(allBadgesWithStatus); // 200 OK와 데이터 반환
    }

    @GetMapping("")
    // 획득한 뱃지 목록 조회
    public ResponseEntity<List<MemberBadgeDTO>> getMemberBadges(@PathVariable Long uid) {
        List<MemberBadgeDTO> memberBadges = badgeService.getMemberBadges(uid);
        return ResponseEntity.ok(memberBadges); // 200 OK와 데이터 반환
    }
}
