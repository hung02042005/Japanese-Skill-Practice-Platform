package com.jlpt.feature.student.kanji.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class KanjiWritingEvaluateRequest {

    @NotNull
    private Integer strokeIndex;

    /** Tọa độ người dùng vẽ [[x, y], ...] — Y đã được flip sang Y-up trước khi gửi */
    @NotNull
    private List<List<Double>> userPath;

    /** Median của nét tương ứng từ HanziWriter [[x, y], ...] — Y-up (HanziWriter coords) */
    @NotNull
    private List<List<Double>> referencePath;
}
