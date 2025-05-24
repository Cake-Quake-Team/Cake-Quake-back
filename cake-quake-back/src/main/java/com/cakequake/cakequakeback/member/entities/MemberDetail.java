package com.cakequake.cakequakeback.member.entities;

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

    @Column
    private String badges;

    @Column
    private LocalDateTime delDate;
}
