/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-25 — Request DTO for updating an existing grammar point (PUT /api/staff/grammar/{id}).
 * All fields are optional (null = keep existing). The "status" field is intentionally absent
 * to prevent client from changing status directly (FR-16).
 */
@Data
public class UpdateGrammarRequest {

    private Long grammarId;
    private String title;

    private String structure;

    private String formula;

    private String meaning;

    private String usageExplanation;

    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    private String exampleSentenceJp;

    private String exampleSentenceVi;

    /** Set to null explicitly to unlink, omit to keep existing. */
    private Long lessonId;

    /** Flag: when true, lessonId=null means "unlink lesson". Default false. */
    private boolean clearLesson = false;
}
