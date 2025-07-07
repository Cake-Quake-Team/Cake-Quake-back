package com.cakequake.cakequakeback.chatting;

import com.cakequake.cakequakeback.chatting.service.JwtHandshakeInterceptor;
import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor

public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtAuthenticationInterceptor stompJwtAuthenticationInterceptor;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final JWTUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 메시지를 구독하는 엔드포인트 접두사를 설정합니다. (예: /topic/chat/{roomKey})
        // SimpleBroker는 인메모리 메시지 브로커입니다.
        config.enableSimpleBroker("/topic", "/queue");
        // 클라이언트가 서버로 메시지를 보낼 때 사용하는 엔드포인트 접두사를 설정합니다. (예: /app/chat/{roomKey})
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:5174",
                        "http://localhost:8080","http://localhost:80",
                        "http://localhost:3000"
                )
                .addInterceptors(jwtHandshakeInterceptor);  // 👉 여기 추가


        // ⭐ HandshakeInterceptor를 직접 추가하는 부분은 제거합니다.
        // STOMP 인증은 ChannelInterceptor를 통해 처리하는 것이 권장됩니다.
        // .addInterceptors(new StompJwtAuthenticationInterceptor(jwtUtil, memberRepository))
    }

    // ⭐ 클라이언트로부터 들어오는 모든 STOMP 메시지를 가로챌 ChannelInterceptor를 등록합니다.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtAuthenticationInterceptor);
    }


}


