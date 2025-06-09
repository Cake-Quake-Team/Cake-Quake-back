package com.cakequake.cakequakeback.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${jwt.secret}") String secret) {

//        log.debug(">>> 주입된 시크릿 키 (앞 10자리): {}", secret.substring(0, 10));
        try {
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("시크릿 키 초기화 실패: " + e.getMessage());
        }
    }

    public String createToken(Map<String, Object> valueMap, int minutes) {

        return Jwts.builder().header()
                .add("typ", "JWT")
                .add("alg", "HS256")
                .and()
                .issuedAt(Date.from(ZonedDateTime.now().toInstant())) // 발급 시간
                .expiration((Date.from(ZonedDateTime.now()
                        .plusMinutes(minutes).toInstant()))).claims(valueMap) // 만료 시간
                .signWith(secretKey)
                .compact();
    }

    // 만료된 토큰 확인
    public Map<String, Object> validateToken(String token) {
        log.debug("=================validateToken=====================");

        try {
            // 시크릿 키로 서명 검증하는 파서 생성
            Claims claims = Jwts.parser().verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            log.debug("claims: {}", claims);

            return claims;
        } catch (JwtException e) {
            throw e;
        }
    }
}
