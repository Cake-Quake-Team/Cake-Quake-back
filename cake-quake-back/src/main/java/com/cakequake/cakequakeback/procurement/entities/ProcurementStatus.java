package com.cakequake.cakequakeback.procurement.entities;

public enum ProcurementStatus {
    REQUESTED, //매장이 요청을 제출한 상태
    SCHEDULED, //어드민이 일정 지정 완료
    SHIPPED,  //물류로 발송된 상태
    DELIVERED, //매장에 도착 완료된 상태
    CANCELLED, //발주 취소 상태
}
