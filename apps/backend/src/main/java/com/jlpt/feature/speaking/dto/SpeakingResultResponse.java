/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Kết quả một lần nộp bài nói (UC-13, chấm bởi giáo viên).
 * {@code status}: PENDING (chờ giáo viên chấm) | COMPLETED (đã chấm) | FAILED (bị từ chối).
 * Điểm/nhận xét chỉ có khi COMPLETED.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeakingResultResponse {

    private Long jobId;
    private String status;
    private Integer score; // điểm giáo viên chấm (%), chỉ có khi COMPLETED
    private String feedback; // nhận xét của giáo viên
    private String error; // thông báo khi FAILED
}
