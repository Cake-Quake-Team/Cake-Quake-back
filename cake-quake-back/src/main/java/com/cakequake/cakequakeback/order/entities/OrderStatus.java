package com.cakequake.cakequakeback.order.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatus {
    RESERVATION_PENDING("예약 확인 중"),
    RESERVATION_CONFIRMED("예약 확정"),
    PREPARING("준비 중"),
    READY_FOR_PICKUP("픽업 준비 완료"),
    PICKUP_COMPLETED("픽업 완료"),
    RESERVATION_CANCELLED("예약 취소"),
    NO_SHOW("노쇼");

    /** 한글 라벨 값을 저장하는 필드 */
    private final String kr;

    /**
     * JSON 직렬화 시 호출됨.
     * 이 메서드를 통해 API 응답에 enum 이름 대신 한글 라벨을 내려줄 수 있음.
     * @return 한글 상태 문자열
     */
    OrderStatus(String kr) {
        this.kr = kr;
    }

    /** 한글 라벨을 직렬화에 사용하도록 public getter 추가 */
    @JsonValue
    public String getKr() {
        return kr;
    }

    public static OrderStatus fromKorean(String krLabel) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.kr.equals(krLabel)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus korean label: " + krLabel);
    }

}