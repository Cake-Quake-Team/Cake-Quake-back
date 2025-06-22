package com.cakequake.cakequakeback.member.entities;

public enum VerificationType {
    SIGNUP,
    RESET,      // 비밀번호 재설정(찾기) 시 인증
    CHANGE;     // 전화번호 변경 전에 새 번호 인증

    public static VerificationType from(String value) {
        return VerificationType.valueOf(value.toUpperCase());
    }
}
