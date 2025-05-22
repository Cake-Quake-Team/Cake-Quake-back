package com.cakequake.cakequakeback.cake.item.entities;

import com.cakequake.cakequakeback.cake.option.entities.Cream;
import com.cakequake.cakequakeback.cake.option.entities.Extra;
import com.cakequake.cakequakeback.cake.option.entities.Sheet;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

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

    private String cname;                   // 하트 초코 케이크, 딸기 생크림 케이크

    private String description;

    private String thumbnailImageUrl;

    private Boolean isOnsale;               // 판매여부

    private Boolean isDeleted;              // 삭제여부

    private int price;

    private int viewCount;                  // 조회수

    private int orderCount;                 // 주문수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sheetId", nullable = false)
    private Sheet sheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creamId", nullable = false)
    private Cream cream;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cake_item_extra",                           // 중간 테이블 명
            joinColumns = @JoinColumn(name = "cakeId"),
            inverseJoinColumns = @JoinColumn(name = "extraId")
    )
    private Set<Extra> extras;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "shop_id", nullable = false)
    // private Shop shop;
}
