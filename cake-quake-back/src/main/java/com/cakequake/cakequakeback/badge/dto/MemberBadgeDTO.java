package com.cakequake.cakequakeback.badge.dto;

import com.cakequake.cakequakeback.badge.entities.MemberBadge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
// 뱃지 전체 목록
public class MemberBadgeDTO {

    private Long memberBadgeId;
    private Long badgeId;
    private String name;
    private String icon;
    private String description;
    private LocalDateTime acquiredDate;
    private Boolean isRepresentative;

    public static MemberBadgeDTO fromEntity(MemberBadge memberBadge) {
        return MemberBadgeDTO.builder()
                .memberBadgeId(memberBadge.getId())
                .badgeId(memberBadge.getBadge().getBadgeId())
                .name(memberBadge.getBadge().getName())
                .icon(memberBadge.getBadge().getIcon())
                .description(memberBadge.getBadge().getDescription())
                .acquiredDate(memberBadge.getAcquiredDate())
                .isRepresentative(memberBadge.isRepresentative())
                .build();
    }
}
