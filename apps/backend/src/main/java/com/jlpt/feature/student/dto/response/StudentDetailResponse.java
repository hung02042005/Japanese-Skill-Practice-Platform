/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentDetailResponse {

    private Long studentId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private String suspendReason;
    private String currentJlptLevel;
    private String targetJlptLevel;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
