/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload cho trang chủ Từ vựng (SPEC-vocab-home §13, FR-VH-01).
 * Shape: { streak, weekDays[], courseTitle, subscription, lessons[] }.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabHomeResponse {

    private int streak;
    private List<Boolean> weekDays;
    private String courseTitle;
    private String level; // cấp độ JLPT của lộ trình (N5..N1) — dùng để mở phiên flashcard
    private String subscription;
    private List<LessonItem> lessons;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonItem {
        private Long topicId;
        private String slug; // định danh chủ đề để mở phiên flashcard (FlashcardSrsService.getSession)
        private String titleJp;
        private String subtitleEn;
        private String status; // "active" | "available" | "locked"
        private String thumbnail;
        private boolean vipOnly;
        private long totalWords; // tổng số từ published của chủ đề (#5 — thanh tiến độ)
        private long learnedCount; // số từ đã học (loại phantom 'learn' chưa ôn — 1D)
        private long masteredCount; // số từ thành thạo (repetitionCount >= MASTERY_THRESHOLD)
    }
}
