/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.FlashcardDeck;
import com.jlpt.feature.flashcard.repository.FlashcardDeckRepository;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Kiểm tra sở hữu thẻ (student ↔ card) và get-or-create deck phiên ôn theo topic. Dùng chung cho
 * {@link FlashcardSrsService} và {@link NotebookService} (sổ tay chỉ cần {@link #ownCardOrThrow};
 * deck "Từ cần ôn lại" là chuyện riêng của Sổ tay nên nằm trong {@code NotebookService}). Các method
 * chạy trong transaction của caller (cả hai service gọi đều @Transactional) nên không cần
 * @Transactional riêng ở đây.
 */
@Component
@RequiredArgsConstructor
public class FlashcardDeckSupport {

    private final FlashcardDeckRepository flashcardDeckRepository;
    private final FlashcardRepository flashcardRepository;

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
                .orElseGet(() -> flashcardDeckRepository.save(
                        FlashcardDeck.builder().student(student).name(name).build()));
    }
}
