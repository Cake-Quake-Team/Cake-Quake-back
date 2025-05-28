package com.cakequake.cakequakeback.member.entities;

public enum VerificationType {
    SIGNUP, RESET;

    public static VerificationType from(String value) {
        return VerificationType.valueOf(value.toUpperCase());
    }
}
