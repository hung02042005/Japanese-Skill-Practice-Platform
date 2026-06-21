/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.dto;

import com.jlpt.feature.learning.VocabularyTopic;

/**
 * Chủ đề từ vựng (catalog {@code vocabulary_topics}) — dùng chung cho Student (lưới chủ đề) và Staff
 * (chọn/quản lý chủ đề khi soạn từ vựng). {@code topicId} là khoá định danh duy nhất xuyên hệ thống.
 */
public record VocabTopicResponse(
        Long topicId, String jlptLevel, String slug, String titleJa, String titleVi, int displayOrder, String status) {

    public static VocabTopicResponse from(VocabularyTopic t) {
        return new VocabTopicResponse(
                t.getId(),
                t.getJlptLevel() != null ? t.getJlptLevel().name() : null,
                t.getSlug(),
                t.getTitleJa(),
                t.getTitleVi(),
                t.getDisplayOrder() != null ? t.getDisplayOrder() : 0,
                t.getStatus() != null ? t.getStatus().getValue() : null);
    }
}
