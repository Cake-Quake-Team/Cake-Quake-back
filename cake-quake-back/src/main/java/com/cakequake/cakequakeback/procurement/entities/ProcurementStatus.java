package com.cakequake.cakequakeback.procurement.entities;

public enum ProcurementStatus {
    REQUESTED, //매장이 요청을 제출한 상태
    COMPLETED,   // 실시간 처리 완료 상태 ← 새로 추가
    SHIPPED,  //물류로 발송된 상태
    DELIVERED, //매장에 도착 완료된 상태
    CANCELLED, //발주 취소 상태
}
