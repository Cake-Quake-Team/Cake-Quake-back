package com.cakequake.cakequakeback.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // 모든 필드에 대한 Getter 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
@Builder // 빌더 패턴으로 객체 생성 가능
public class ShopOperatingHoursDTO {

    private Long shopId; // 매장 ID
    private String openTime; // 매장 오픈 시간 (HH:mm:ss 형식의 문자열)
    private String closeTime; // 매장 마감 시간 (HH:mm:ss 형식의 문자열)
    private boolean isClosed; // 해당 날짜가 휴무일인지 여부
    private String message; // 휴무일이거나 특정 메시지가 필요한 경우 (예: "매장 휴무일입니다.")

}