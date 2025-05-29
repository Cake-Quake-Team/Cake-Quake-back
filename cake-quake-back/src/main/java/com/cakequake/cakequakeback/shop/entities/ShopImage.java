package com.cakequake.cakequakeback.shop.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table( name = "shop_image")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShopImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long imageId;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="shopId",nullable = false)
    private Shop shop; //매장ID

    @Column(nullable = false)
    private String shopImage; //이미지 URL

    @Column(nullable = false)
    private String thumbnail; //대표이미지

    @Column(nullable = false)
    private String createdBy; //생성자

    @Column( nullable = false)
    private String modifiedBy; //수정자
}

