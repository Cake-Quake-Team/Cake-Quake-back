package com.cakequake.cakequakeback.member.entities;

public enum SocialType {
    KAKAO, GOOGLE, BASIC;

    public static SocialType from(String value) {
        return SocialType.valueOf(value.toUpperCase());
    }
}
