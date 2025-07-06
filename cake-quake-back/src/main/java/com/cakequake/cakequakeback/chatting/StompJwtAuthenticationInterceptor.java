package com.cakequake.cakequakeback.chatting;

import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberStatus; // MemberStatus 임포트 필요
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component

public class StompJwtAuthenticationInterceptor implements ChannelInterceptor {

        // 이 인터셉터에서는 직접 사용되지 않을 수 있지만, JWT 발행/검증 로직은 다른 곳에서 필요할 수 있으므로 유지
        private final JWTUtil jwtUtil;
        private final MemberRepository memberRepository;

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // STOMP CONNECT 명령이 들어왔을 때만 인증 로직을 수행합니다.
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        log.info("STOMP CONNECT Command received. Attempting authentication using session principal (HttpOnly Cookie based).");

                        // 1. WebSocket 세션에 이미 Principal(인증된 사용자)이 설정되어 있는지 확인합니다.
                        //    이 Principal은 HttpOnly 쿠키를 통한 HTTP 핸드셰이크 과정에서
                        //    Spring Security에 의해 설정되었을 것으로 기대합니다.
                        Authentication authentication = (Authentication) accessor.getUser();

                        if (authentication != null && authentication.isAuthenticated()) {
                                // 이미 인증된 Principal이 존재하고 유효합니다.
                                // SecurityContextHolder에 인증 정보를 다시 설정합니다.
                                // (이것은 HTTP 핸드셰이크 단계에서 이미 이루어졌을 수 있지만, STOMP 컨텍스트에 명시적으로 설정)
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.info("STOMP CONNECT: User '{}' already authenticated via session. Principal set.", authentication.getName());
                        } else {
                                // Principal이 없거나 인증되지 않았습니다.
                                // 이는 HttpOnly 쿠키 기반 인증이 실패했거나, 쿠키가 없다는 의미입니다.
                                log.warn("STOMP CONNECT: No authenticated principal found in WebSocket session. Authentication failed.");
                                // 인증 실패 시 STOMP 연결을 거부합니다.
                                throw new RuntimeException("Authentication required for WebSocket connection.");
                        }
                }
                // 다른 STOMP 명령 (SEND, SUBSCRIBE 등)은 인증된 사용자로 계속 처리
                return message;
        }

}
