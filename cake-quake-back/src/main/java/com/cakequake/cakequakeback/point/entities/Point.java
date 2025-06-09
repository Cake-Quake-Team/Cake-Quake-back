package com.cakequake.cakequakeback.point.entities;


import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int pointId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="uid",nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private Long totalPoints = 0L;

    public void setMember(Member member) {
        this.member = member;
    }

    public void setTotalPoints(Long totalPoints) {
        this.totalPoints = totalPoints;
    }


}
