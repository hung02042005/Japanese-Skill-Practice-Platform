/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;

import com.jlpt.feature.flashcard.Flashcard;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Unit test thuật toán SM-2 (NFR-FC-02 yêu cầu ≥ 10 case). Gọi trực tiếp {@code applySm2}
 * (package-private) — thuần logic, không cần Spring context.
 */
class FlashcardSrsServiceSm2Test {

    // Service không dùng repository nào trong applySm2 -> khởi tạo với null.
    private final FlashcardSrsService service = new FlashcardSrsService(null, null, null, null, null, null, null);

    private static Flashcard card(int intervalDays, int repetitionCount) {
        return Flashcard.builder()
                .intervalDays(intervalDays)
                .repetitionCount(repetitionCount)
                .build();
    }

    private static Flashcard card(int intervalDays, int repetitionCount, String ease) {
        return Flashcard.builder()
                .intervalDays(intervalDays)
                .repetitionCount(repetitionCount)
                .easeFactor(new BigDecimal(ease))
                .build();
    }

    private static double ease(Flashcard c) {
        return c.getEaseFactor().doubleValue();
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
    void hard_keepsPreviousInterval() {
        Flashcard c = card(10, 2);
        service.applySm2(c, Flashcard.LastRating.HARD);
        assertEquals(10, c.getIntervalDays()); // FR-FC-22: interval = MAX(1, previous)
    }

    @Test
    void hard_keepsAtLeastOne() {
        Flashcard c = card(1, 0);
        service.applySm2(c, Flashcard.LastRating.HARD);
        assertEquals(1, c.getIntervalDays()); // max(1, previous)
    }

    @Test
    void hard_doesNotChangeRepetitionCount() {
        Flashcard c = card(10, 3);
        service.applySm2(c, Flashcard.LastRating.HARD);
        assertEquals(3, c.getRepetitionCount());
    }

    @Test
    void hard_keepsEaseUnchanged() {
        Flashcard c = card(10, 2, "2.00");
        service.applySm2(c, Flashcard.LastRating.HARD);
        assertEquals(2.00, ease(c), 1e-9); // FR-FC-22: ease unchanged
    }

    // ── Ease factor (FR-FC-23/24) ─────────────────────────────────────────────

    @Test
    void easy_increasesEaseByPointOne() {
        Flashcard c = card(6, 2, "2.00");
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertEquals(2.10, ease(c), 1e-9); // +0.1 (FR-FC-23)
    }

    @Test
    void easy_capsEaseAtMax() {
        Flashcard c = card(6, 2, "2.50");
        service.applySm2(c, Flashcard.LastRating.EASY);
        assertEquals(2.50, ease(c), 1e-9); // không vượt 2.5 (FR-FC-23)
    }

    @Test
    void wrong_decreasesEase() {
        Flashcard c = card(10, 4, "2.50");
        service.applySm2(c, Flashcard.LastRating.WRONG);
        assertEquals(1.70, ease(c), 1e-9); // 2.5 - 0.8 (quality 0)
    }

    @Test
    void wrong_easeNeverBelowFloor() {
        Flashcard c = card(10, 4, "1.40");
        service.applySm2(c, Flashcard.LastRating.WRONG); // 1.4 - 0.8 = 0.6 -> sàn 1.3
        assertEquals(1.30, ease(c), 1e-9);
        service.applySm2(c, Flashcard.LastRating.WRONG); // vẫn 1.3 (FR-FC-24)
        assertEquals(1.30, ease(c), 1e-9);
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
