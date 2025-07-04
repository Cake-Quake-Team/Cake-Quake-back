package com.cakequake.cakequakeback.badge.entities;

import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_badges", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"uid", "badge_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class MemberBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid") // users 테이블의 uid 참조
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id") // badges 테이블의 id 참조
    private Badge badge;

    @Column(nullable = false)
    private LocalDateTime acquiredDate = LocalDateTime.now(); // 뱃지 획득 시점

    @Column
    private boolean isRepresentative = false; // 대표 뱃지 여부

    public void activateRepresentative() {
        this.isRepresentative = true;
    }

    public void deactivateRepresentative() {
        this.isRepresentative = false;
    }

}
