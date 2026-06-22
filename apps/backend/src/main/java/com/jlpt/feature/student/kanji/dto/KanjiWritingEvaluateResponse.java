package com.jlpt.feature.student.kanji.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KanjiWritingEvaluateResponse {

    /** DTW distance đã normalize (thấp hơn = tốt hơn) */
    private double dtwScore;

    /** "perfect" | "good" | "ok" | "bad" */
    private String quality;

    /** Hướng nét chuẩn từ reference median, VD: "Sang phải", "Xuống" */
    private String direction;

    /** Thông điệp feedback ngắn để hiển thị */
    private String feedbackMsg;
}
