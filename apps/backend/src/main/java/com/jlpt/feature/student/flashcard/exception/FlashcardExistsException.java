package com.jlpt.feature.student.flashcard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FlashcardExistsException extends RuntimeException {
    public FlashcardExistsException(String message) {
        super(message);
    }
}
