package com.cakequake.cakequakeback.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoPayReadyResponseDTO {
    private String tid; //결제 고유 번호
    @JsonProperty("next_redirect_pc_rul")
    private String nextRedirectPcRul; // PC 웹 결제 URL

    @JsonProperty("next_redirect_mobile_url")
    private String nextRedirectMobileUrl;       // 모바일 결제 URL

    @JsonProperty("android_app_scheme")
    private String androidAppScheme;            // 안드로이드 Intent 스킴

    @JsonProperty("ios_app_scheme")
    private String iosAppScheme;                // iOS 스킴

    @JsonProperty("created_at")
    private String createdAt;                   // 생성 시각 ISO8601

}
