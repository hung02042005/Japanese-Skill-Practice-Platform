/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.FlashcardDeck;
import com.jlpt.feature.flashcard.repository.FlashcardDeckRepository;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlashcardDeckSupportTest {

    @Mock
    private FlashcardDeckRepository flashcardDeckRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @InjectMocks
    private FlashcardDeckSupport support;

    @Test
    void ownCardOrThrow_notFound() {
        when(flashcardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> support.ownCardOrThrow(1L, 100L));
    }

    @Test
    void ownCardOrThrow_forbidden() {
        Flashcard card = Flashcard.builder()
                .id(1L)
                .student(StudentUser.builder().id(99L).build())
                .build();
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(ForbiddenException.class, () -> support.ownCardOrThrow(1L, 100L));
    }

    @Test
    void ownCardOrThrow_success() {
        Flashcard card = Flashcard.builder()
                .id(1L)
                .student(StudentUser.builder().id(100L).build())
                .build();
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(card));

        Flashcard res = support.ownCardOrThrow(1L, 100L);
        assertEquals(1L, res.getId());
    }

    @Test
    void getOrCreateDeck_existing() {
        StudentUser student = StudentUser.builder().id(100L).build();
        FlashcardDeck deck = FlashcardDeck.builder().id(10L).name("Deck").build();
        when(flashcardDeckRepository.findByStudentIdAndName(100L, "Deck")).thenReturn(Optional.of(deck));

        FlashcardDeck res = support.getOrCreateDeck(student, "Deck");
        assertEquals(10L, res.getId());
    }
}
