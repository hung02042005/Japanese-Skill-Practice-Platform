/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.FlashcardConstants;
import com.jlpt.feature.flashcard.FlashcardDeck;
import com.jlpt.feature.flashcard.repository.FlashcardDeckRepository;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Kiểm tra sở hữu (student ↔ deck/card) và get-or-create deck. Tách khỏi {@link FlashcardSrsService}
 * để phiên ôn (SRS) và Sổ tay ({@link NotebookService}) cùng dùng. Các method chạy trong transaction
 * của caller (cả hai service gọi đều @Transactional) nên không cần @Transactional riêng ở đây.
 */
@Component
@RequiredArgsConstructor
public class FlashcardDeckSupport {

    private final FlashcardDeckRepository flashcardDeckRepository;
    private final FlashcardRepository flashcardRepository;

    public FlashcardDeck ownDeckOrThrow(Long studentId, Long deckId) {
        return flashcardDeckRepository
                .findByIdAndStudentId(deckId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Sổ tay", deckId));
    }

    public Flashcard ownCardOrThrow(Long flashcardId, Long studentId) {
        // 1 query: nạp thẻ rồi so chủ sở hữu trên FK id (student LAZY, getId() không hit DB).
        Flashcard card = flashcardRepository
                .findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", flashcardId));
        if (card.getStudent() == null || !studentId.equals(card.getStudent().getId())) {
            throw new ForbiddenException("Flashcard không thuộc về bạn");
        }
        return card;
    }

    public FlashcardDeck getOrCreateDeck(StudentUser student, String name) {
        return flashcardDeckRepository
                .findByStudentIdAndName(student.getId(), name)
                .orElseGet(() -> {
                    FlashcardDeck.FlashcardDeckBuilder b =
                            FlashcardDeck.builder().student(student).name(name);
                    // Tách jlpt_level/topic từ pattern "{level}_{topic}" để deck có metadata.
                    if (name.matches("N[1-5]_.+")) {
                        b.jlptLevel(name.substring(0, 2)).topic(name.substring(3));
                    }
                    return flashcardDeckRepository.save(b.build());
                });
    }

    public FlashcardDeck getOrCreateReviewDeck(StudentUser student) {
        return flashcardDeckRepository
                .findByStudentIdAndIsReviewDeckTrue(student.getId())
                .orElseGet(() -> flashcardDeckRepository.save(FlashcardDeck.builder()
                        .student(student)
                        .name(FlashcardConstants.REVIEW_DECK_NAME)
                        .isReviewDeck(true)
                        .build()));
    }
}
