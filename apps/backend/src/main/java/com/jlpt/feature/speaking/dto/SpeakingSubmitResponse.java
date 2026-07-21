/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import lombok.Builder;
import lombok.Data;

/** Phản hồi ngay khi nộp bài — async, chưa có kết quả AI (UC-13 §3.1 bước 11). */
@Data
@Builder
public class SpeakingSubmitResponse {

    private Long jobId; // = submissionId, client dùng để poll
    private String status; // luôn "PENDING" tại thời điểm nộp
}
