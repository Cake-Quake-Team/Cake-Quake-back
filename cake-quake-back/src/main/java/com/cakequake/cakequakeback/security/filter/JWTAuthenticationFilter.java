package com.cakequake.cakequakeback.security.filter;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.exception.ErrorResponseDTO;
import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    public JWTAuthenticationFilter(JWTUtil jwtUtil, MemberRepository memberRepository, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
    }

    public enum JWTErrorCode {

        NO_ACCESS_TOKEN(401, "No access token"),
        EXPIRED_TOKEN(401, "Expired token"),
        BAD_SIGNATURE(401, "Bad signature"),
        MALFORMED_TOKEN(401, "Malformed token");

        private int code;
        private String message;

        JWTErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
        public int getCode() {
            return code;
        }
        public String getMessage() {
            return message;
        }

        public ErrorCode toGlobalErrorCode() {
            return switch (this) {
                case NO_ACCESS_TOKEN -> ErrorCode.MISSING_JWT;
                case EXPIRED_TOKEN -> ErrorCode.EXPIRED_JWT;
                case BAD_SIGNATURE, MALFORMED_TOKEN -> ErrorCode.INVALID_JWT;
            };
        }

    }

    private static final List<String> EXCLUDE_PATHS = List.of(
            "/api/v1/auth/signin",
            "/api/v1/auth/signup",
            "/api/v1/auth/otp",
            "/api/v1/auth/business/verify",
            "/api/v1/auth/refresh",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        log.info("=============JWT-shouldNotFilter======================");

        String path = request.getServletPath();
        boolean exclude = EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
        log.debug("필터 상태 - 경로: '{}', 적용 여부: {}", path, exclude ? "제외됨" : "적용됨");
        return exclude;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.debug("=============JWT-doFilterInternal======================");

        log.debug("requestURI: {}", request.getRequestURI());

        // 헤더 정보 중에 Authorization 가져옴.
        String headerStr = request.getHeader("Authorization");
        log.debug("headerStr: {}", headerStr);

        /*
            Bearer 토큰 방식으로 인증
         */
        //Access Token이 없는 경우
        if (headerStr == null || headerStr.contains("undefined") || !headerStr.startsWith("Bearer ")) {
            handleException(response, JWTErrorCode.NO_ACCESS_TOKEN);
            return;
        }

        // accessToken 만료 확인
        String accessToken = headerStr.substring(7);
        log.debug("accessToken: {}", accessToken.substring(0, 6));

        try {
            // JWT 토큰 유효성 검증 및 페이로드(Map<String, Object>) 반환
            Map<String, Object> tokenMap = jwtUtil.validateToken(accessToken);

            String userId = (String) tokenMap.get("userId");
            log.debug("userId: {}", userId);

            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // 인증에 사용할 사용자 정보 객체 생성
            CustomUserDetails userDetails = new CustomUserDetails(member);

            // 시큐리티에서 사용할 인증 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // 토큰이 유효하면 굳이 비밀번호를 다시 확인할 필요 없어서 null
                            userDetails.getAuthorities()
                    );

            log.debug("JWT 필터 userDetails.getAuthorities(): {}", userDetails.getAuthorities());

            // 생성한 인증 정보를 시큐리티 컨텍스트에 설정하여 인증 처리 완료
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("++++++++++doFilterInternal++++++++++++++");
            String message = e.getMessage();
            if(message.startsWith("JWT signature")){
                handleException(response, JWTErrorCode.BAD_SIGNATURE);
            }else if(message.startsWith("JWT malformed")){
                handleException(response, JWTErrorCode.MALFORMED_TOKEN);
            }else if(message.startsWith("JWT expired")){
                handleException(response, JWTErrorCode.EXPIRED_TOKEN);
            }
        }
    }

    private void handleException(HttpServletResponse response, JWTErrorCode jwtErrorCode) throws IOException {

        ErrorCode globalCode = jwtErrorCode.toGlobalErrorCode();

        // 공통 응답 포맷 생성
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(globalCode);

        response.setStatus(globalCode.getHttpStatus()); // 보통 401
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 직렬화해서 응답
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
