package com.cakequake.cakequakeback.chatting.service;

import com.cakequake.cakequakeback.common.utils.CookieUtil;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j

public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository; // ⭐ MemberRepository 주입 추가

    private static final String SIMP_USER_HEADER_KEY = "simpUser";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        log.info("🤝 WebSocket Handshake 시작");

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            try {
                String token = CookieUtil.getCookieValue(httpRequest, "accessToken");

                if (token == null || token.isBlank()) {
                    log.warn("[Handshake] ❌ 토큰이 존재하지 않습니다.");
                    return false;
                }

                Map<String, Object> claims = jwtUtil.validateToken(token);

                // JWT 클레임에서 사용자 정보 추출
                Long uid = Long.valueOf(claims.get("uid").toString());
                String userId = claims.get("userId").toString(); // userId 클레임도 사용 (필요하다면)
                String role = claims.get("role").toString();

                // ⭐ 1. MemberRepository를 사용하여 Member 객체 조회
                //    AuthenticatedUserService.getCurrentMember()가 Member 객체를 반환하므로 필수.
                Optional<Member> optionalMember = memberRepository.findByUidAndStatus(uid, MemberStatus.ACTIVE);
                if (optionalMember.isEmpty()) {
                    log.warn("[Handshake] ❌ 활성 상태의 사용자를 찾을 수 없습니다: UID={}", uid);
                    return false;
                }
                Member member = optionalMember.get();

                // ⭐ 2. CustomUserDetails 생성 시 Member 객체 주입
                CustomUserDetails userDetails = new CustomUserDetails(member);

                // ⭐ 3. Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // ⭐ 4. 현재 HTTP 요청의 SecurityContextHolder에 Authentication 설정
                //    이것은 Handshake 과정의 HTTP 요청 스코프에만 유효합니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // ⭐ 5. WebSocketSession의 attributes에 Authentication 객체를 저장 (가장 중요!)
                //    SimpMessageHeaderAccessor.USER_HEADER를 키로 사용하여 Principal을 저장하면
                //    StompHeaderAccessor.getUser()를 통해 STOMP 메시지 처리 스레드에서 접근 가능합니다.
                attributes.put(SIMP_USER_HEADER_KEY, authentication);

                log.info("[Handshake] ✅ WebSocket 인증 성공 및 Principal 설정 완료: UID={}, Username={}, Role={}", member.getUid(), member.getUserId(), member.getRole());

            } catch (Exception e) {
                log.warn("[Handshake] ❌ JWT 인증 또는 사용자 정보 로드 실패: {}", e.getMessage());
                // 상세 에러 로깅 (예: 토큰 만료, 잘못된 토큰 등)
                return false;
            }
        } else {
            log.warn("[Handshake] ❌ ServletServerHttpRequest 타입이 아닙니다. WebSocket 핸드셰이크를 처리할 수 없습니다.");
            return false;
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Handshake 완료 후 SecurityContextHolder를 정리하는 것이 좋습니다.
        // 왜냐하면 이 SecurityContext는 HTTP 요청 스코프에 한정되기 때문입니다.
        // 다음 STOMP 메시지 처리 시에는 StompJwtAuthenticationInterceptor가 다시 설정할 것입니다.
        SecurityContextHolder.clearContext();
        log.info(">>> [Handshake] WebSocket Handshake 완료. SecurityContextHolder 정리됨.");
    }
}
