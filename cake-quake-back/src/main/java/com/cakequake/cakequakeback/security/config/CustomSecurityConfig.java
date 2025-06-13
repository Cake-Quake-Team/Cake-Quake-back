package com.cakequake.cakequakeback.security.config;

import com.cakequake.cakequakeback.security.exception.CustomAccessDeniedHandler;
import com.cakequake.cakequakeback.security.exception.CustomAuthenticationEntryPoint;
import com.cakequake.cakequakeback.security.filter.JWTAuthenticationFilter;
import com.cakequake.cakequakeback.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("------------------Security Config-----------------------");

        http.authorizeHttpRequests(auth -> {
//            auth.requestMatchers("/**").permitAll(); // 모든 요청 허용
            auth.requestMatchers(
                    "/api/v1/auth/signup/**",
                    "/api/v1/auth/signin",
                    "/api/v1/auth/otp/send", // 전화번호 인증
                    "/api/v1/auth/otp/verify",
                    "/api/v1/auth/business/verify", // 사업자 진위 여부
                    "/api/v1/auth/refresh",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
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

        corsConfiguration.setAllowedOriginPatterns(List.of("*")); // 어디서든 허락
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}
