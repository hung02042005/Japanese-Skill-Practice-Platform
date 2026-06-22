package com.jlpt.feature.student.kanji.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class KanjiWritingAttemptRequest {

    @NotNull
    private Long kanjiId;

    @NotBlank
    private String characterValue;

    @NotNull
    private Integer totalStrokes;

    /** Kết quả từng nét — thu thập từ các lần gọi evaluate-stroke */
    private List<StrokeResult> strokes;

    @Data
    public static class StrokeResult {
        private int strokeIndex;
        private double dtwScore;
        private String quality;
        private String direction;
    }
}
