package com.cakequake.cakequakeback.chatting.service;

import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberStatus;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        log.info(">>> [Handshake] WebSocket Handshake 시작");

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        Map<String, Object> claims = jwtUtil.validateToken(token);
                        String userId = (String) claims.get("userId");

                        Member member = memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE)
                                .orElseThrow();

                        CustomUserDetails userDetails = new CustomUserDetails(member);

                        // Principal 주입
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        attributes.put("SPRING.PRINCIPAL", authentication);  // 핵심
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.info("✅ WebSocket Handshake Principal 설정 완료: {}", userId);
                        return true;
                    }
                }
            }
        }

        log.warn("❌ WebSocket Handshake 인증 실패 → 연결 차단");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        log.info(">>> [Handshake] WebSocket Handshake 완료");
    }
}
