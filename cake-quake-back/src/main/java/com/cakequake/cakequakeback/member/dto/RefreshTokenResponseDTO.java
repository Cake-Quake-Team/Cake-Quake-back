package com.cakequake.cakequakeback.member.dto;

public class RefreshTokenResponseDTO {

    private final String accessToken;
    private final String refreshToken;

    public RefreshTokenResponseDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

}
