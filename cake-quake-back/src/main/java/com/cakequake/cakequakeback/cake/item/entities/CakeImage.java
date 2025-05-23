package com.cakequake.cakequakeback.cake.item.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cake_image")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class CakeImage {
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

    private Boolean isThumbnail;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "cakeId", nullable = false)
     private CakeItem cakeItem;
}