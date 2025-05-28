package com.cakequake.cakequakeback.order.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatus {
    RESERVATION_PENDING("예약확인중"),
    RESERVATION_CONFIRMED("예약확정"),
    RESERVATION_CANCELLED("예약취소"),
    NO_SHOW("노쇼"),
    PICKUP_COMPLETED("픽업완료");

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

}