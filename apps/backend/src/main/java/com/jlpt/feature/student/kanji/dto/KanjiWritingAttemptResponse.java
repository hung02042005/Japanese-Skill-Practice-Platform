package com.jlpt.feature.student.kanji.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KanjiWritingAttemptResponse {

    private Long attemptId;

    /** "perfect" | "good" | "ok" | "bad" */
    private String finalQuality;

    private double avgDtwScore;

    private int totalStrokes;
}
