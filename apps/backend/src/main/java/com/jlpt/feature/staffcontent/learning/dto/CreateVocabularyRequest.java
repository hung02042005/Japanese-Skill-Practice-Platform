/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-27 — Request DTO for creating a vocabulary entry (POST /api/staff/vocabulary).
 * FR-27-16: word, furigana (reading), meaning, jlptLevel are mandatory.
 * FR-27-17: optional fields persisted when supplied.
 * FR-27-18: lessonId (when supplied) must reference an existing, non-deleted lesson.
 */
@Data
public class CreateVocabularyRequest {

    @NotBlank(message = "Thiếu trường bắt buộc: word")
    private String word;

    @NotBlank(message = "Thiếu trường bắt buộc: furigana")
    private String furigana;

    @NotBlank(message = "Thiếu trường bắt buộc: meaning")
    private String meaning;

    @NotBlank(message = "Cấp độ JLPT phải là N5 đến N1")
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    private String wordType;
    private String topic;
    private String audioUrl;
    private String exampleSentenceJp;
    private String exampleSentenceVi;
    private Long lessonId;
}
