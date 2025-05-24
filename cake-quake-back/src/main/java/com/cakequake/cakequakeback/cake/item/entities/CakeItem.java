package com.cakequake.cakequakeback.cake.item.entities;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cake_item")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class CakeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cake_item_seq")
    @SequenceGenerator(
            name = "cake_item_seq",
            sequenceName = "cake_item_seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long cakeId;

    @Column(nullable = false)
    private String cname;                   // 하트 초코 케이크, 딸기 생크림 케이크

    private String description;

    @Column(nullable = false)
    private String thumbnailImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isOnsale = false;       // 삭제여부

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDeleted = false;      // 삭제여부

    @Column(nullable = false)
    private int price;

    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;               // 조회수

    @Builder.Default
    @Column(nullable = false)
    private int orderCount = 0;              // 주문수

    @Enumerated(EnumType.STRING)
    private CakeCategory category;


    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "shop_id", nullable = false)
    // private Shop shop;
}
