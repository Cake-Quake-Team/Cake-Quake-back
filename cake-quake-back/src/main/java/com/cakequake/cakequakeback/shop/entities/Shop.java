package com.cakequake.cakequakeback.shop.entities;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table( name = "shops")
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "shop_seq_gen")
    @SequenceGenerator(
            name = "shop_seq_gen",
            sequenceName="shop_seq",
            initialValue=1,
            allocationSize = 50
    )
    private Long shopId;

    //userId 컬럼이 member 테이블의 userId 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String businessNumber; //사업자 등록 번호

    @Column(nullable = false)
    private String shopName; // 매장 이름

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column
    private String Content;

    @Column(precision = 2,scale=1)
    private BigDecimal rating;

    @Column
    private Integer reviewCount;

    @Column
    private String operationHours;

    @Column
    private String closeDays;

    @Column
    private String websiteUrl;

    @Column
    private String instagramUrl;

    @Column
    @Enumerated(EnumType.STRING)
    private ShopStatus status;

    @Column(precision = 9,scale=6)
    private BigDecimal lat;

    @Column(precision = 9,scale = 6)
    private BigDecimal lng;









}
