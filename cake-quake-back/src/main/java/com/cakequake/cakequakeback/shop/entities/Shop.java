package com.cakequake.cakequakeback.shop.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import com.cakequake.cakequakeback.shop.dto.ShopUpdateDTO;
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


    public void updateShop(ShopUpdateDTO updateDTO){
        // shopId는 여기서 직접 접근할 수 없으므로, 필요하다면 상위 메서드에서 로그를 출력해야 합니다.
        // 여기서는 updateDTO의 각 필드들을 로그로 출력합니다.

        System.out.println("DEBUG: updateShop 메서드 호출됨");
        System.out.println("DEBUG: ShopUpdateDTO 내용 ---");
        System.out.println("DEBUG: Address: " + updateDTO.getAddress());
        System.out.println("DEBUG: Phone: " + updateDTO.getPhone());
        System.out.println("DEBUG: Content: " + updateDTO.getContent());
        System.out.println("DEBUG: OpenTime: " + updateDTO.getOpenTime());
        System.out.println("DEBUG: CloseTime: " + updateDTO.getCloseTime());
        System.out.println("DEBUG: CloseDays: " + updateDTO.getCloseDays());
        System.out.println("DEBUG: WebsiteUrl: " + updateDTO.getWebsiteUrl());
        System.out.println("DEBUG: InstagramUrl: " + updateDTO.getInstagramUrl());
        System.out.println("DEBUG: Status: " + updateDTO.getStatus());
        System.out.println("DEBUG: ThumbnailImageUrl: " + updateDTO.getThumbnailImageUrl());
        System.out.println("DEBUG: --- ShopUpdateDTO 내용 종료");

        if (updateDTO.getAddress() != null) {
            this.address = updateDTO.getAddress();
            System.out.println("DEBUG: Address 업데이트됨: " + this.address);
        }
        if (updateDTO.getPhone() != null) {
            this.phone = updateDTO.getPhone();
            System.out.println("DEBUG: Phone 업데이트됨: " + this.phone);
        }
        if (updateDTO.getContent() != null) {
            this.content = updateDTO.getContent();
            System.out.println("DEBUG: Content 업데이트됨: " + this.content);
        }
        if (updateDTO.getOpenTime() != null) {
            this.openTime = updateDTO.getOpenTime();
            System.out.println("DEBUG: OpenTime 업데이트됨: " + this.openTime);
        }
        if (updateDTO.getCloseTime() != null) {
            this.closeTime = updateDTO.getCloseTime();
            System.out.println("DEBUG: CloseTime 업데이트됨: " + this.closeTime);
        }
        if (updateDTO.getCloseDays() != null) {
            this.closeDays = updateDTO.getCloseDays();
            System.out.println("DEBUG: CloseDays 업데이트됨: " + this.closeDays);
        }
        if (updateDTO.getWebsiteUrl() != null) {
            this.websiteUrl = updateDTO.getWebsiteUrl();
            System.out.println("DEBUG: WebsiteUrl 업데이트됨: " + this.websiteUrl);
        }
        if (updateDTO.getInstagramUrl() != null) {
            this.instagramUrl = updateDTO.getInstagramUrl();
            System.out.println("DEBUG: InstagramUrl 업데이트됨: " + this.instagramUrl);
        }
        if (updateDTO.getStatus() != null) { // Enum 타입도 null 체크 필요
            this.status = updateDTO.getStatus();
            System.out.println("DEBUG: Status 업데이트됨: " + this.status);
        }
        if (updateDTO.getThumbnailImageUrl() != null) {
            this.thumbnailImageUrl = updateDTO.getThumbnailImageUrl();
            System.out.println("DEBUG: ThumbnailImageUrl 업데이트됨: " + this.thumbnailImageUrl);
        }

        System.out.println("DEBUG: updateShop 메서드 종료.");
    }

    public void updateThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    // 판매자 탈퇴 시 변경용
    public void changeStatus(ShopStatus status) {
        this.status = status;
    }


    public void updateReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }


    public void updateRating(BigDecimal rating) {
        this.rating = rating;
    }
}
