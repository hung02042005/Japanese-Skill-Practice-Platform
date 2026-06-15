/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;

import com.jlpt.entity.Flashcard;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Unit test thuật toán SM-2 (NFR-FC-02 yêu cầu ≥ 10 case). Gọi trực tiếp {@code applySm2}
 * (package-private) — thuần logic, không cần Spring context.
 */
class FlashcardSrsServiceSm2Test {

    // Service không dùng repository nào trong applySm2 -> khởi tạo với null.
    private final FlashcardSrsService service = new FlashcardSrsService(null, null, null, null, null, null);

    private static Flashcard card(int intervalDays, int repetitionCount) {
        return Flashcard.builder()
                .intervalDays(intervalDays)
                .repetitionCount(repetitionCount)
                .build();
    }

    // ── WRONG (FR-FC-21) ──────────────────────────────────────────────────────

    @Test
    void wrong_resetsIntervalAndRepetition() {
        Flashcard c = card(10, 4);
        service.applySm2(c, Flashcard.LastRating.WRONG);
        assertEquals(1, c.getIntervalDays());
        assertEquals(0, c.getRepetitionCount());
    }

    @Test
    void wrong_setsNextReviewTomorrow() {
        Flashcard c = card(10, 4);
        service.applySm2(c, Flashcard.LastRating.WRONG);
        assertEquals(LocalDate.now().plusDays(1), c.getNextReviewDate());
    }

    @Test
    void wrong_setsLastRatingWrong() {
        Flashcard c = card(10, 4);
        service.applySm2(c, Flashcard.LastRating.WRONG);
        assertEquals(Flashcard.LastRating.WRONG, c.getLastRating());
    }

    // ── EASY progression (FR-FC-23) ───────────────────────────────────────────

    @Test
    void easy_firstRep_intervalIsOne() {
        Flashcard c = card(1, 0);
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertEquals(1, c.getIntervalDays());
        assertEquals(1, c.getRepetitionCount());
    }

    @Test
    void easy_secondRep_intervalIsSix() {
        Flashcard c = card(1, 1);
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertEquals(6, c.getIntervalDays());
        assertEquals(2, c.getRepetitionCount());
    }

    @Test
    void easy_thirdRep_intervalScalesByFactor() {
        Flashcard c = card(6, 2);
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertEquals(15, c.getIntervalDays()); // round(6 * 2.5)
        assertEquals(3, c.getRepetitionCount());
    }

    @Test
    void easy_fourthRep_roundsHalfUp() {
        Flashcard c = card(15, 3);
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertEquals(38, c.getIntervalDays()); // round(15 * 2.5 = 37.5) = 38
        assertEquals(4, c.getRepetitionCount());
    }

    @Test
    void easy_setsNextReviewByInterval() {
        Flashcard c = card(1, 1);
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertEquals(LocalDate.now().plusDays(6), c.getNextReviewDate());
    }

    // ── HARD (FR-FC-22) ───────────────────────────────────────────────────────

    @Test
    void hard_growsIntervalByOnePointTwo() {
        Flashcard c = card(10, 2);
        service.applySm2(c, Flashcard.LastRating.HARD);
        assertEquals(12, c.getIntervalDays()); // (int)(10 * 1.2)
    }

    @Test
    void hard_keepsAtLeastOne() {
        Flashcard c = card(1, 0);
        service.applySm2(c, Flashcard.LastRating.HARD);
        assertEquals(1, c.getIntervalDays()); // max(1, (int)(1 * 1.2))
    }

    @Test
    void hard_doesNotChangeRepetitionCount() {
        Flashcard c = card(10, 3);
        service.applySm2(c, Flashcard.LastRating.HARD);
        assertEquals(3, c.getRepetitionCount());
    }

    // ── Common side effects ───────────────────────────────────────────────────

    @Test
    void anyRating_setsLastReviewedAt() {
        Flashcard c = card(1, 0);
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertNotNull(c.getLastReviewedAt());
    }

    @Test
    void easy_setsLastRatingEasy() {
        Flashcard c = card(1, 0);
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertEquals(Flashcard.LastRating.EASY, c.getLastRating());
    }
}
