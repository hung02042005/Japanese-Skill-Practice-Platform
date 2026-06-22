package com.jlpt.feature.student.flashcard;

import com.jlpt.feature.student.flashcard.dto.FlashcardRequest;
import com.jlpt.feature.student.flashcard.dto.FlashcardResponse;

public interface StudentFlashcardService {
    FlashcardResponse addToFlashcard(FlashcardRequest request, Long studentId);
}
