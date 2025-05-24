package com.cakequake.cakequakeback.cake.item.entities;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopId", nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private String cname;                   // 하트 초코 케이크, 딸기 생크림 케이크

    @Column
    private String description;

    @Column(nullable = false)
    private String thumbnailImageUrl;

    @Column(nullable = false)
    private Boolean isOnsale = false;       // 품절여부

    @Column(nullable = false)
    private Boolean isDeleted = false;      // 삭제여부

    @Column(nullable = false)
    private int price = 0;

    @Column(nullable = false)
    private int viewCount = 0;               // 조회수

    @Column(nullable = false)
    private int orderCount = 0;              // 주문수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CakeCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", nullable = false)
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedBy", nullable = false)
    private Member modifiedBy;


}
