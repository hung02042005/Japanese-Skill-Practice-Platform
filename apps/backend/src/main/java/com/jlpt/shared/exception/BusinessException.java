/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int status;
    private final String errorCode;

    public BusinessException(int status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
