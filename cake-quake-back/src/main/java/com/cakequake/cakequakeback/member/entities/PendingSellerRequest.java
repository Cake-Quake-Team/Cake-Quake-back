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
    private Long temp_seller_id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String uname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String phoneNumber; // 전화번호

    /*
        사업자 진위여부에 필수 정보: 사업자 등록 번호, 대표자명, 개업일자
     */
    @Column(nullable = false, length = 50)
    private String businessNumber; // 사업자 등록 번호

    @Column(nullable = false, length = 50)
    private String bossName; // 대표자 성명

    @Column(nullable = false, length = 8)
    private String openingDate; // 개업일자

    @Column(nullable = false, length = 100)
    private String shopName; // 매장 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;  // GOOGLE, KAKAO, BASIC 등

    @Builder.Default
    @Column(nullable = false)
    private Boolean publicInfo = true; // 정보 공개 여부

    @Column(nullable = false)
    private String businessCertificateUrl; // 사업자 등록증 파일


    // 아래는 2단계 인증에서 사용. 그 전에 저장을 위해서 null 허용
    private String address;

    private String operationHours; // 운영시간

    @Column(length = 50)
    private String category; // 케이크 카테고리

    @Column(columnDefinition = "TEXT")
    private String mainProductDescription; // 주요 상품 설명

    private String sanitationCertificateUrl; // 위생 관련 인증서.

    private String shopImageUrl; // 매장 대표 이미지

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerRequestStatus status = SellerRequestStatus.PENDING; // 가입 신청 상태


    // 수정용
    public void changePassword(String password) {
        this.password = password;
    }

    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void changeBossName(String bossName) {
        this.bossName = bossName;
    }

    public void changeShopName(String shopname) {
        this.shopName = shopname;
    }

    public void changePublicInfo(Boolean publicInfo) {
        this.publicInfo = publicInfo;
    }

    public void changeBusinessCertificateUrl(String url) {
        this.businessCertificateUrl = url;
    }

    public void changeShopPhoneNumber(String phoneNumber) {
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

    public void changeSanitationCertificateUrl(String url) {
        this.sanitationCertificateUrl = url;
    }

    public void changeShopImageUrl(String url) {
        this.shopImageUrl = url;
    }

    public void changeStatus(SellerRequestStatus status) {
        this.status = status;
    }

}
