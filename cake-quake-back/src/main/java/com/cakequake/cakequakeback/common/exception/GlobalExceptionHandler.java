package com.cakequake.cakequakeback.common.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

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

}
