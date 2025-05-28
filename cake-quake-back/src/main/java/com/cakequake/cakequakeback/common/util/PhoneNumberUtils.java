package com.cakequake.cakequakeback.common.util;

public class PhoneNumberUtils {

    // 전화번호가 올바른 형식인지 검사할 때 사용
    public static boolean isValid(String phoneNumber) {
        if (phoneNumber == null) return false;
        String normalized = phoneNumber.replaceAll("[^0-9]", "");
        return normalized.matches("^010\\d{8}$"); // 010으로 시작하고 총 11자리인지 검사
    }

    // DB 저장, 비교, 전송 등을 위해 하이픈 제거된 숫자만 필요할 때 사용
    public static String normalize(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }

}
