package com.cakequake.cakequakeback.shop.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table( name = "shops")
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class Shop extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long shopId;

    //userId 컬럼이 member 테이블의 userId 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Member member;

    @Column(nullable = false, length =50)
    private String businessNumber; //사업자 등록 번호

    @Column(nullable = false, length=100)
    private String shopName; // 매장 이름

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length=50)
    private String bossName; // 대표자명

    @Column
    private String phone; //가게 전화번호 -> null시 본인의 번호가 나오게

    @Column(nullable = false)
    private String content;

    @Column
    private String thumbnailImageUrl;

    @Column(precision = 2,scale=1)
    private BigDecimal rating;

    @Column(nullable = false)
    private Integer reviewCount;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;


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
