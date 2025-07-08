package com.cakequake.cakequakeback.security.config;

import com.cakequake.cakequakeback.security.exception.CustomAccessDeniedHandler;
import com.cakequake.cakequakeback.security.exception.CustomAuthenticationEntryPoint;
import com.cakequake.cakequakeback.security.filter.JWTAuthenticationFilter;
import com.cakequake.cakequakeback.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.List;

@Slf4j
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    static {
        // 이 전략을 설정하면 SecurityContext가 스레드 간에 상속됩니다.
        // WebSocket 메시지 처리와 같이 스레드 풀을 사용하는 환경에서 중요합니다.
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        log.info("SecurityContextHolder strategy set to MODE_INHERITABLETHREADLOCAL.");
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("------------------Security Config-----------------------");

        http.authorizeHttpRequests(auth -> {
//            log.debug(String.valueOf(SecurityContextHolder.getContext().getAuthentication()));

//            auth.requestMatchers("/**").permitAll(); // 모든 요청 허용
            auth.requestMatchers(
                    "/api/v1/auth/signup/**",
                    "/api/v1/auth/signin/**",
                    "/api/v1/auth/otp/send", // 전화번호 인증
                    "/api/v1/auth/otp/verify",
                    "/api/v1/auth/business/verify", // 사업자 진위 여부
                    "/api/v1/auth/refresh",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico","/s3/upload", "/error",
                            "/ws/**", "/ws", "/websocket/**", "/sockjs-node/**"
                    ).permitAll()
                    .anyRequest().authenticated();

        });

        http.userDetailsService(customUserDetailsService);

        http.formLogin(config -> config.disable() );

        // 소셜 로그인 만들기 전까지 무시
        http.oauth2Login( oauth2 -> oauth2.disable());

        http.csrf(csrf -> csrf.disable() );

        http.cors(cors ->
            cors.configurationSource(corsConfigurationSource())
        );

        // 원래 인증의 아이디 패스워드 확인 전에 추가.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
        );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://localhost:5174","https://*.ngrok-free.app","http://localhost:80")); // 배포 후 변경
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}
