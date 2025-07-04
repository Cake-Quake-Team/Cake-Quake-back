package com.cakequake.cakequakeback.badge.dto;

import com.cakequake.cakequakeback.badge.entities.Badge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
// 획득한 뱃지 목록
public class AcquiredBadgeDTO {

    private Long badgeId;
    private String name;
    private String icon;
    private String description;
    private boolean acquired;       // 획득 여부
    private LocalDateTime acquiredDate;

    public static AcquiredBadgeDTO fromEntity(Badge badge, boolean acquired, LocalDateTime acquiredDate) {
        return AcquiredBadgeDTO.builder()
                .badgeId(badge.getBadgeId())
                .name(badge.getName())
                .icon(badge.getIcon())
                .description(badge.getDescription())
                .acquired(acquired)
                .acquiredDate(acquiredDate)
                .build();
    }

    public static AcquiredBadgeDTO fromEntity(Badge badge) {
        return fromEntity(badge, false, null);
    }
}
