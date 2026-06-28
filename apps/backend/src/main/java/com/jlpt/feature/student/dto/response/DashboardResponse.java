/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Dữ liệu trang Dashboard học viên (UC-09). Gồm thống kê streak/từ vựng và lộ trình học
 * (skill-category cards) theo từng cấp độ để FE lọc theo {@code selectedLevel}.
 */
@Data
@Builder
public class DashboardResponse {

    private int streak;
    private List<Boolean> weekDays;
    private String selectedLevel;
    private long wordCount;
    private long daysThisMonth;
    private List<LessonCard> lessons;

    /** Một thẻ kỹ năng trên lộ trình (Hiragana/Vocab/Kanji/...). progress trong [0,1]. */
    @Data
    @Builder
    public static class LessonCard {
        private long id;
        private String title;
        private String description;
        private String jlptLevel;
        private String lessonType; // KANA | VOCAB | GRAMMAR | KANJI | READING | LISTENING
        private String status; // active | available
        private double progress;
        private String thumbnail;
    }
}
