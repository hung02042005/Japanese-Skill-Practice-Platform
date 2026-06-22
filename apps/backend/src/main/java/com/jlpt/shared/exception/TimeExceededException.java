/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.exception;

public class TimeExceededException extends BusinessException {
    public TimeExceededException(String message) {
        super(400, "TIME_EXCEEDED", message);
    }
}
