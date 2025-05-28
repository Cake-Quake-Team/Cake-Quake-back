package com.cakequake.cakequakeback.member.entities;

public enum MemberRole {
    BUYER,
    SELLER,
    ADMIN;

    public static MemberRole from(String value) {
        return MemberRole.valueOf(value.toUpperCase());
    }
}
