/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Kết quả poll (UC-13 §3.1 bước 14-16 / SPEC-speaking §5).
 * {@code status}: PENDING | COMPLETED | FAILED. Các trường điểm/transcript chỉ có khi COMPLETED.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeakingResultResponse {

    private Long jobId;
    private String status;
    private Integer score; // điểm tổng (%), chỉ có khi COMPLETED
    private Boolean provisional;
    private String transcript;
    private List<WordResultDto> wordResults;
    private String feedback;
    private String error; // thông báo thân thiện khi FAILED (không expose raw AI error)
}
