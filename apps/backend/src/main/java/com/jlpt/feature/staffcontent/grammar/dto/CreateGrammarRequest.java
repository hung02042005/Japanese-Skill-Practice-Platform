/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-25 — Request DTO for creating a new grammar point (POST /api/staff/grammar).
 * FR-03: structure, meaning, usageExplanation, exampleSentenceJp, jlptLevel are mandatory.
 * FR-04: jlptLevel must be in {N5, N4, N3, N2, N1}.
 */
@Data
public class CreateGrammarRequest {

    private String title;

    @NotBlank(message = "Thiếu trường bắt buộc: structure")
    private String structure;

    private String formula;

    @NotBlank(message = "Thiếu trường bắt buộc: meaning")
    private String meaning;

    @NotBlank(message = "Thiếu trường bắt buộc: usageExplanation")
    private String usageExplanation;

    @NotBlank(message = "Cấp độ JLPT phải là N5 đến N1")
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    @NotBlank(message = "Thiếu trường bắt buộc: exampleSentenceJp")
    private String exampleSentenceJp;

    private String exampleSentenceVi;

    /** Optional: link to a lesson with the same jlpt_level (FR-06/07/08). */
    private Long lessonId;
}
