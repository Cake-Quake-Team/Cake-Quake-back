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

                // STOMP CONNECT 명령 처리 (초기 인증 설정)
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        log.info("STOMP CONNECT Command received. Attempting authentication via WebSocket session principal.");

                        // WebSocket 세션에 저장된 Principal을 가져옵니다.
                        // 이 Principal은 JwtHandshakeInterceptor에 의해 설정되었을 것으로 기대합니다.
                        Authentication authentication = (Authentication) accessor.getUser();

                        if (authentication != null && authentication.isAuthenticated()) {
                                // Principal이 유효하면 SecurityContextHolder에 설정합니다.
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.info("STOMP CONNECT: User '{}' authenticated via WebSocket session. Principal set in SecurityContextHolder.", authentication.getName());
                        } else {
                                // Principal이 없거나 인증되지 않은 경우 (비정상 상황)
                                log.warn("STOMP CONNECT: No authenticated principal found in WebSocket session. Connection will be rejected.");
                                throw new RuntimeException("Authentication required for WebSocket connection.");
                        }
                }
                // ⭐ 모든 STOMP 메시지 (SEND, SUBSCRIBE, DISCONNECT 등)에 대해 SecurityContextHolder 설정
                //    이것은 STOMP 메시지가 처리되는 스레드에 Principal을 전파하는 핵심입니다.
                else {
                        Authentication authentication = (Authentication) accessor.getUser();
                        if (authentication != null && authentication.isAuthenticated()) {
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.debug("STOMP {}: User '{}' principal set in SecurityContextHolder.", accessor.getCommand(), authentication.getName());
                        } else {
                                // 인증된 Principal이 없는 상태에서 SEND/SUBSCRIBE 등 메시지가 오면
                                log.warn("STOMP {}: No authenticated principal found for command. Message will be rejected.", accessor.getCommand());
                                // 이 상황에서 예외를 던져 메시지 처리를 중단할 수도 있습니다.
                                // throw new RuntimeException("Authentication required for this operation.");
                                // 현재는 AuthenticatedUserService에서 예외가 발생하므로 여기서는 경고만 남깁니다.
                        }
                }

                return message; // 메시지 처리 계속
        }

}
