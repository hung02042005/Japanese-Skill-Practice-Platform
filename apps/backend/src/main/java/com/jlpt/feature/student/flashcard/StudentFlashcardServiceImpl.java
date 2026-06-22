package com.jlpt.feature.student.flashcard;

import com.jlpt.feature.learning.Flashcard;
import com.jlpt.feature.learning.Flashcard.ContentType;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.flashcard.dto.FlashcardRequest;
import com.jlpt.feature.student.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.student.flashcard.exception.FlashcardExistsException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentFlashcardServiceImpl implements StudentFlashcardService {

    private final StudentFlashcardRepository flashcardRepository;
    private final StudentUserRepository studentUserRepository;

    @Override
    @Transactional
    public FlashcardResponse addToFlashcard(FlashcardRequest request, Long studentId) {
        StudentUser student = studentUserRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentUser", studentId));

        ContentType contentType;
        try {
            contentType = ContentType.valueOf(request.getContentType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid content type: " + request.getContentType());
        }

        flashcardRepository.findByStudentIdAndContentTypeAndContentId(studentId, contentType, request.getContentId())
                .ifPresent(f -> {
                    throw new FlashcardExistsException("Flashcard already exists for this content.");
                });

        Flashcard flashcard = Flashcard.builder()
                .student(student)
                .deckName("Mặc định")
                .isSystem(false)
                .contentType(contentType)
                .contentId(request.getContentId())
                .intervalDays(1)
                .repetitionCount(0)
                .build();

        Flashcard saved = flashcardRepository.save(flashcard);

        return FlashcardResponse.builder()
                .id(saved.getId())
                .deckName(saved.getDeckName())
                .contentType(saved.getContentType().getValue())
                .contentId(saved.getContentId())
                .build();
    }
}
