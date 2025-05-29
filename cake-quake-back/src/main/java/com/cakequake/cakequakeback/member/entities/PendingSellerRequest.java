package com.cakequake.cakequakeback.member.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pending_seller_requests",
        indexes = {
            @Index(name = "idx_user_id", columnList = "userId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PendingSellerRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 50)
    private String businessNumber; // 사업자 등록 번호

    @Column(nullable = false, length = 100)
    private String shopname; // 매장 이름

    @Column(nullable = false, length = 50)
    private String bossName; // 대표자 명

    @Column(nullable = false, length = 20)
    private String phoneNumber; // 전화번호

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String operationHours; // 운영시간

    @Column(nullable = false, length = 50)
    private String category; // 케이크 카테고리

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mainProductDescription; // 주요 상품 설명

    @Builder.Default
    @Column(nullable = false)
    private Boolean publicInfo = true; // 정보 공개 여부

    @Column(nullable = false)
    private String businessCertificateUrl; // 사업자 등록증 파일

    private String sanitationCertificateUrl; // 위생 관련 인증서. null 허용

    @Column(nullable = false)
    private String shopImageUrl; // 매장 대표 이미지

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerRequestStatus status = SellerRequestStatus.PENDING; // 가입 신청 상태


    // 수정용
    public void changeStatus(SellerRequestStatus status) {
        this.status = status;
    }

    public void changeBossName(String bossName) {
        this.bossName = bossName;
    }

    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void changeAddress(String address) {
        this.address = address;
    }

    public void changeOperationHours(String operationHours) {
        this.operationHours = operationHours;
    }

    public void changeCategory(String category) {
        this.category = category;
    }

    public void changeMainProductDescription(String description) {
        this.mainProductDescription = description;
    }

    public void changePublicInfo(Boolean publicInfo) {
        this.publicInfo = publicInfo;
    }

    public void changeBusinessCertificateUrl(String url) {
        this.businessCertificateUrl = url;
    }

    public void changeSanitationCertificateUrl(String url) {
        this.sanitationCertificateUrl = url;
    }

    public void changeShopImageUrl(String url) {
        this.shopImageUrl = url;
    }

    public void changeShopname(String shopname) {
        this.shopname = shopname;
    }

}
