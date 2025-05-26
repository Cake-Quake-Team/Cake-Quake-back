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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cake_image_seq")
    @SequenceGenerator(
            name = "cake_image_seq",
            sequenceName = "cake_image_seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long imageId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isThumbnail = false;        // 대표 이미지 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cakeId", nullable = false)
    private CakeItem cakeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", nullable = false)
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedBy", nullable = false)
    private Member modifiedBy;
}