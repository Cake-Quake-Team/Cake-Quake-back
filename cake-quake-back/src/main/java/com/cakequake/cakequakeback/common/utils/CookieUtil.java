package com.cakequake.cakequakeback.common.utils;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Arrays;

@Slf4j
@UtilityClass
public class CookieUtil {

    private final String ACCESS_TOKEN_NAME = "accessToken";
    private final String REFRESH_TOKEN_NAME = "refreshToken";

    // AccessToken 쿠키 추가
    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_NAME, token)
                .httpOnly(true)
//                .secure(true)      // HTTPS 사용 시 true
                .secure(false)
                .path("/")
//                .sameSite("None") // HTTPS
                .sameSite("Lax")    // 크로스 사이트 POST 요청 불가
                .maxAge(Duration.ofMinutes(5))  // 유효기간 5분
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // RefreshToken 쿠키 추가
    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_NAME, token)
                .httpOnly(true)
//                .secure(true)     // HTTPS
                .secure(false)
                .path("/")
//                .sameSite("None")     // HTTPS
                .sameSite("Lax")        // 크로스 사이트 POST 요청 불가
                .maxAge(Duration.ofDays(7))  // 유효기간 7일
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 두 토큰 쿠키 한 번에 추가
    public void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        log.debug("+++++addAuthCookies+++++");
        addAccessTokenCookie(response, accessToken);
        addRefreshTokenCookie(response, refreshToken);
    }

    public String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> name.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(() -> new JwtException(name + " 쿠키가 없습니다."));
        }
        throw new JwtException("쿠키가 없습니다.");
    }

    // 특정 쿠키 삭제 (토큰 만료 또는 로그아웃 시 사용)
    public void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
//                .sameSite("None")
                .sameSite("Lax")
                .maxAge(0)  // 삭제
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 모든 토큰 쿠키 삭제
    public void clearAuthCookies(HttpServletResponse response) {
        clearCookie(response, ACCESS_TOKEN_NAME);
        clearCookie(response, REFRESH_TOKEN_NAME);
    }
}
