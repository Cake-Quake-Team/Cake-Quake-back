package com.cakequake.cakequakeback.security.exception;

import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.exception.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

// 인증(로그인)이 되지 않은 사용자가 인증이 필요한 URL에 접근할 때 발생. 401
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 인증이 되지 않은 사용자가 보호된 리소스에 접근했을 때 호출됨
        // 예: JWT 토큰 없음, 토큰 형식 오류, 인증 헤더 없음 등
        ErrorCode errorCode = ErrorCode.INVALID_JWT;

        sendErrorResponse(response, errorCode);
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                errorCode.getHttpStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        );

        response.setStatus(errorCode.getHttpStatus());
        response.setContentType("application/json;charset=UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }
}
