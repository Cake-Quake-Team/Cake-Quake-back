package com.cakequake.cakequakeback.common.exception;

public class CustomSecurityException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomSecurityException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
