package com.cakequake.cakequakeback.member.dto.admin;

import com.cakequake.cakequakeback.member.entities.SellerRequestStatus;
import com.cakequake.cakequakeback.member.entities.SocialType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingSellerRequestListDTO {

    private Long tempSellerId;
    private String userId;
    private String uname;
    private String phoneNumber; // 전화번호
    private String businessNumber; // 사업자 등록 번호
    private String bossName; // 대표자 성명
    private String openingDate; // 개업일자
    private String shopName; // 매장 이름
    private SocialType socialType;  // GOOGLE, KAKAO, BASIC 등
    private Boolean publicInfo; // 정보 공개 여부
    private String businessCertificateUrl; // 사업자 등록증 파일
    private SellerRequestStatus status; // 가입 신청 상태

    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String mainProductDescription; // 주요 상품 설명
    private String shopPhoneNumber; // 매장 전화번호
    private String shopImageUrl; // 매장 대표 이미지 url
    private String sanitationCertificateUrl; // 위생 관련 인증서 url

    private LocalDateTime regDate;
    private LocalDateTime modDate;

    // setter
    public void setTempSellerId(Long tempSellerId) {
        this.tempSellerId = tempSellerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }

    public void setBossName(String bossName) {
        this.bossName = bossName;
    }

    public void setOpeningDate(String openingDate) {
        this.openingDate = openingDate;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public void setSocialType(SocialType socialType) {
        this.socialType = socialType;
    }

    public void setPublicInfo(Boolean publicInfo) {
        this.publicInfo = publicInfo;
    }

    public void setBusinessCertificateUrl(String businessCertificateUrl) {
        this.businessCertificateUrl = businessCertificateUrl;
    }

    public void setStatus(SellerRequestStatus status) {
        this.status = status;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public void setMainProductDescription(String mainProductDescription) {
        this.mainProductDescription = mainProductDescription;
    }

    public void setShopPhoneNumber(String shopPhoneNumber) {
        this.shopPhoneNumber = shopPhoneNumber;
    }

    public void setShopImageUrl(String shopImageUrl) {
        this.shopImageUrl = shopImageUrl;
    }

    public void setSanitationCertificateUrl(String sanitationCertificateUrl) {
        this.sanitationCertificateUrl = sanitationCertificateUrl;
    }

    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }

    public void setModDate(LocalDateTime modDate) {
        this.modDate = modDate;
    }

    @Override
    public String toString() {
        return String.format("SellerResponseDTO{tempSellerId=%d, userId=%s, uname=%s, shopName=%s, shopImageUrl=%s, businessCertificateUrl=%s, sanitationCertificateUrl=%s}",
                tempSellerId, userId, uname, shopName, shopImageUrl, businessCertificateUrl, sanitationCertificateUrl);
    }
}
