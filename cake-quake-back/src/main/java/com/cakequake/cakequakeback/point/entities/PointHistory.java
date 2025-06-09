package com.cakequake.cakequakeback.point.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long pointHistoryId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="uid", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private ChangeType changeType;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balanceAmount;

}
