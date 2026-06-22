package com.jlpt.feature.student.kana.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanaResponse {
    private Integer kanaId;
    private String character;
    private String romaji;
    private String kanaType;
    private String audioUrl;
    private String strokeOrderUrl;
    private Integer displayOrder;
    private String row;
    private boolean isCompleted;
}
