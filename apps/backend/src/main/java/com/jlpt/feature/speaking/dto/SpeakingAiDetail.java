/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phần chi tiết AI (transcript + phát âm từng từ) được serialize thành JSON và lưu ở cột
 * {@code ai_error_summary} của {@code student_submissions} — tránh phải thêm cột/migration mới
 * (ADR-006: không BLOB, chỉ text). Điểm số vẫn nằm ở các cột ai_*_score riêng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingAiDetail {

    private String transcript;
    private List<WordResultDto> wordResults;
}
