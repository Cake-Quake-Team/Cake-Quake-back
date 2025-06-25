package com.cakequake.cakequakeback.member.entities;

import com.cakequake.cakequakeback.badge.entities.Badge;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberDetail extends BaseEntity {

    @Id
    private Long uid; // PK이자 FK

    @MapsId // FK를 PK로 사용함
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid") // users.uid를 참조
    private Member member;

    @Column(nullable = false)
    private String profileBadge; // 대표 뱃지

    @Column
    private LocalDateTime delDate;

    public void changeProfileBadge(String newProfileBadge) {
        this.profileBadge = newProfileBadge;
    }
}
