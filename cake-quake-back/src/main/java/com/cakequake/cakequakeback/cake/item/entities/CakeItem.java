package com.cakequake.cakequakeback.cake.item.entities;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.UpdateCakeDTO;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.entities.Shop;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cake_item")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class CakeItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cake_id")
    private Long cakeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopId")
    private Shop shop;

    @Column
    private String cname;                   // 하트 초코 케이크, 딸기 생크림 케이크

    @Column
    private String description;

    @Column
    private String thumbnailImageUrl;

    @Column
    private Boolean isOnsale = false;       // 품절여부

    @Column
    private Boolean isDeleted = false;      // 삭제여부

    @Column
    private int price = 0;

    @Column
    private int viewCount = 0;               // 조회수

    @Column
    private int orderCount = 0;              // 주문수

    @Enumerated(EnumType.STRING)
    @Column
    private CakeCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedBy")
    private Member modifiedBy;

    public void updateFromDTO(UpdateCakeDTO dto) {
        if (dto.getCname() != null) this.cname = dto.getCname();
        if (dto.getPrice() != null) this.price = dto.getPrice();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getCategory() != null) this.category = dto.getCategory();
        if (dto.getThumbnailImageUrl() != null) this.thumbnailImageUrl = dto.getThumbnailImageUrl();
        if (dto.getIsOnsale() != null) this.isOnsale = dto.getIsOnsale();
    }

    public void changeIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }


}
