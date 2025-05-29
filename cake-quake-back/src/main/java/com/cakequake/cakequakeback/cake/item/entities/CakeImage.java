package com.cakequake.cakequakeback.cake.item.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cake_image")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class CakeImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long imageId;

    @Column
    private String imageUrl;

    @Column
    private Boolean isThumbnail = false;        // 대표 이미지 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cake_id")
    private CakeItem cakeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by")
    private Member modifiedBy;
}