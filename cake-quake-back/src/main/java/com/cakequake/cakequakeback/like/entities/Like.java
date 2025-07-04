package com.cakequake.cakequakeback.like.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Like extends BaseEntity {
    // 찜 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "likeId")
    private Long likeId;

    // 찜한 회원 (ManyToOne 관계)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_uid", nullable = false,
            foreignKey = @ForeignKey(name = "fk_like_member"))
    private Member member;

    // 찜된 케이크 상품 (ManyToOne 관계)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cake_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_like_cake_item"))
    private CakeItem cakeItem;
}
