package com.cakequake.cakequakeback.like.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity; // BaseEntity를 상속하여 생성/수정 시간 자동 관리
import com.cakequake.cakequakeback.member.entities.Member; // 회원 엔티티 임포트 (경로 확인 필요)
import com.cakequake.cakequakeback.shop.entities.Shop; // ⭐ 매장 엔티티 임포트 (경로 확인 필요) ⭐
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_likes", // 테이블 이름은 'shop_likes'로 지정하여 기존 'likes'와 구분
        uniqueConstraints = {@UniqueConstraint(columnNames = {"member_uid", "shop_id"})}) //복합 유니크 키 설정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ShopLike extends BaseEntity {

    // 매장 찜 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopLikeId")
    private Long shopLikeId;

    // 찜한 회원 (ManyToOne 관계)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_uid", nullable = false, // DB 컬럼명은 member_uid
            foreignKey = @ForeignKey(name = "fk_shop_like_member"))
    private Member member; // Member 엔티티와 연결

    // 찜된 매장 (ManyToOne 관계)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false, // DB 컬럼명은 shop_id
            foreignKey = @ForeignKey(name = "fk_shop_like_shop"))
    private Shop shop; // Shop 엔티티와 연결


}