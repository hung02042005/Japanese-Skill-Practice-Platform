package com.jlpt.feature.student.flashcard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlashcardResponse {
    private Long id;
    private String deckName;
    private String contentType;
    private Long contentId;
}
