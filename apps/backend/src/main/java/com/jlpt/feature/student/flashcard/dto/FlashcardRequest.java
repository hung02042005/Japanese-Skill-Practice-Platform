package com.jlpt.feature.student.flashcard.dto;

import lombok.Data;

@Data
public class FlashcardRequest {
    private String contentType;
    private Long contentId;
}
