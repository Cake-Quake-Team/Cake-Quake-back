package com.cakequake.cakequakeback.payment.entities;

public enum PaymentStatus {
    READY,  //결제 준비 (카카오/토스 ready 요청 완료)
    APPROVED, //결제 승인 완료
    FAILED,   //결제 실패
    CANCELLED , //결제 취소
    REFUNDED
}

