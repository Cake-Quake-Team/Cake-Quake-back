package com.cakequake.cakequakeback.common.exception;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBussinessException(BusinessException ex) {
        ErrorCode ec = ex.getErrorCode();
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                ec.getHttpStatus(),
                ec.getCode(),
                ec.getMessage()
        );
        return ResponseEntity.status(ec.getHttpStatus()).body(body);
    }

    @ExceptionHandler(CustomSecurityException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomSecurityException(CustomSecurityException ex) {
        return buildErrorResponse(ex.getErrorCode());
    }

    // 외부 API 요청 오류 (400)
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpClientError(HttpClientErrorException ex) {
        ErrorCode errorCode = resolveClientErrorCode(ex);
        return buildErrorResponse(errorCode);
    }

    // 외부 API 서버 오류 (5xx)
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpServerError(HttpServerErrorException ex) {
        ErrorCode errorCode = resolveServerErrorCode(ex);
        return buildErrorResponse(errorCode);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponseDTO> handleJwtException(JwtException e) {
        ErrorCode errorCode;

        if (e instanceof ExpiredJwtException) {
            errorCode = ErrorCode.EXPIRED_JWT;

        } else if (e instanceof MalformedJwtException) {
            errorCode = ErrorCode.INVALID_JWT;

        } else if (e instanceof SignatureException) {
            errorCode = ErrorCode.INVALID_JWT;

        } else if (e instanceof JwtException) {
            errorCode = ErrorCode.INVALID_JWT;

        } else {
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        }
        log.error(errorCode.getMessage());

        return buildErrorResponse(errorCode);
    }


    // 중복 제거용 공통 메서드
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(ErrorCode ec) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                ec.getHttpStatus(),
                ec.getCode(),
                ec.getMessage()
        );
        return ResponseEntity.status(ec.getHttpStatus()).body(body);
    }

    private ErrorCode resolveClientErrorCode(HttpClientErrorException ex) {
        String msg = ex.getMessage().toLowerCase();
        if (msg.contains("kakao")) return ErrorCode.EXTERNAL_CLIENT_ERROR_KAKAO;
        if (msg.contains("google")) return ErrorCode.EXTERNAL_CLIENT_ERROR_GOOGLE;
        if (msg.contains("odcloud") || msg.contains("nts-businessman")) return ErrorCode.EXTERNAL_CLIENT_ERROR_ODCLOUD;
        return ErrorCode.EXTERNAL_CLIENT_ERROR;
    }

    private ErrorCode resolveServerErrorCode(HttpServerErrorException ex) {
        String msg = ex.getMessage().toLowerCase();
        if (msg.contains("kakao")) return ErrorCode.EXTERNAL_SERVER_ERROR_KAKAO;
        if (msg.contains("google")) return ErrorCode.EXTERNAL_SERVER_ERROR_GOOGLE;
        if (msg.contains("odcloud") || msg.contains("nts-businessman")) return ErrorCode.EXTERNAL_SERVER_ERROR_ODCLOUD;
        return ErrorCode.EXTERNAL_SERVER_ERROR;
    }

}
