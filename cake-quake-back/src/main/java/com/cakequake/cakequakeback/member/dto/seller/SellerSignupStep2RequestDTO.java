package com.cakequake.cakequakeback.member.dto.seller;

import com.cakequake.cakequakeback.member.entities.SellerRequestStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;

// 1단계 저장을 위해서 모두 null 허용. 단계랑 상관없이 원래 null 가능한 컬럼은 @Nullable 사용.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerSignupStep2RequestDTO {
    @NotNull(message = "tempSellerId는 필수입니다. 1단계부터 진행해주세요.")
    private Long tempSellerId;

    @NotBlank(message = "매장 주소를 입력해주세요")
    private String shopAddress;

    @Nullable
    @Pattern(regexp = "^\\d{2,4}-\\d{3,4}-\\d{4}$", message = "전화번호 양식은 XXX-XXX(X)-XXXX로 입력해야합니다.")
    private String shopPhoneNumber; // 매장 전화번호는 안 쓰는 경우도 있어서 null 허용

    @NotNull(message = "영업 시작 시간은 필수입니다.")
    private LocalTime openTime;

    @NotNull(message = "영업 종료 시간은 필수입니다.")
    private LocalTime closeTime;

    @NotBlank(message = "주요 제품 설명은 필수 입력 항목입니다.")
    @Size(min = 10, max = 200, message = "주요 제품 설명은 10자 이상 200자 사이로 입력해주세요")
    private String mainProductDescription;

    @Nullable
    private MultipartFile shopImage; // 매장 대표 이미지

    @Nullable
    private MultipartFile sanitationCertificate; // 위생 관련 인증서.


    @Override
    public String toString() {
        return "SellerSignupStep2RequestDTO{" +
                "tempSellerId=" + tempSellerId +
                ", shopAddress='" + mask(shopAddress) + '\'' +
                ", shopPhoneNumber='" + mask(shopPhoneNumber) + '\'' +
                ", openTime=" + openTime +
                ", closeTime=" + closeTime +
                ", mainProductDescription='" + mainProductDescription + '\'' +
                '}';
    }

    private String mask(String input) {
        if (input == null || input.length() < 4) return "***";
        return input.substring(0, 2) + "***" + input.substring(input.length() - 2);
    }
}
