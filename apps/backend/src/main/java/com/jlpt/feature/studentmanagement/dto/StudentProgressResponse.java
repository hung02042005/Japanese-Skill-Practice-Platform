/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.studentmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentProgressResponse {

    private Long studentId;
    private String fullName;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActivityDate;
    private Integer lessonsCompleted;
    private Integer kanjiCompleted;
    private Integer vocabularyCompleted;
    private Integer grammarCompleted;
    private Integer kanaCompleted;
    private Long totalExamsTaken;
    private BigDecimal averageExamScore;
    private BigDecimal highestExamScore;
}
