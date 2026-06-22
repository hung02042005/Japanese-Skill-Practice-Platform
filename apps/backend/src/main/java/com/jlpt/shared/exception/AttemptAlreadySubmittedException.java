/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.exception;

public class AttemptAlreadySubmittedException extends BusinessException {
    public AttemptAlreadySubmittedException(String message) {
        super(422, "ATTEMPT_ALREADY_SUBMITTED", message);
    }
}
