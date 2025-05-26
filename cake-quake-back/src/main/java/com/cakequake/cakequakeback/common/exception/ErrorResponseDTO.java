package com.cakequake.cakequakeback.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {
    private LocalDateTime timestamp; //발생 시각
    private int status; //HTTP 상태 코드
    private int code;   // 비즈니스 에러 코드
    private String message;  //사용자 메시지
}
