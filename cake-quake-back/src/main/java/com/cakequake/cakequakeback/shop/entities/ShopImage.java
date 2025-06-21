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
    private Long shopImageId;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="shopId",nullable = false)
    private Shop shop; //매장ID

    @Column(nullable = false)
    private String shopImageUrl; //이미지 URL

    @Column(nullable = false)
    private Boolean isThumbnail=false; //대표이미지

    @Column
    private String createdBy; //생성자

    @Column
    private String modifiedBy; //수정자

    public ShopImage(Long shopImageId, String shopImageUrl, Boolean isThumbnail) {
        this.shopImageId = shopImageId;
        this.shopImageUrl = shopImageUrl;
        this.isThumbnail = isThumbnail;
    }

    public void changeThumbnail() {
        this.isThumbnail = true;
    }
    public void deleteThumbnail() {
        this.isThumbnail = false;
    }
}

