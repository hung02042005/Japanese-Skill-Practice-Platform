/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-27 — Request DTO for creating a kanji entry (POST /api/staff/kanji).
 * FR-27-20: characterValue, meaning, jlptLevel + at least one of onyomi/kunyomi are mandatory
 *           (the on/kun rule is validated in the service).
 * FR-27-21: characterValue must be unique.
 * FR-27-22: strokeCount (when supplied) must be >= 1.
 */
@Data
public class CreateKanjiRequest {

    @NotBlank(message = "Thiếu trường bắt buộc: characterValue")
    private String characterValue;

    @NotBlank(message = "Thiếu trường bắt buộc: meaning")
    private String meaning;

    @NotBlank(message = "Cấp độ JLPT phải là N5 đến N1")
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    private String onyomi;
    private String kunyomi;

    @Min(value = 1, message = "strokeCount phải >= 1")
    private Integer strokeCount;

    private String strokeOrderUrl;
    private String exampleWord;
    private String exampleReading;
    private String exampleMeaning;
}
