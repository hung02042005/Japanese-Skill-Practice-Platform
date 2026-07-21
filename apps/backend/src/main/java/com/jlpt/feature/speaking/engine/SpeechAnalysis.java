/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.engine;

import java.util.List;

/**
 * Kết quả phân tích của một engine nhận dạng giọng nói. Điểm số nằm trong khoảng [0, 100]
 * (engine tự đảm bảo; service vẫn clamp lại theo BR-13-05).
 */
public record SpeechAnalysis(
        int overallScore,
        int pronunciationScore,
        int fluencyScore,
        String transcript,
        List<WordScore> wordResults,
        String suggestions) {

    /** Đánh giá phát âm một từ/cụm. */
    public record WordScore(String word, boolean correct, String feedback) {}
}
