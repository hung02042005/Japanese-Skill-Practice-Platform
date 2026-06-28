/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponse {
    private Long studentId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String currentJlptLevel;
    private String targetJlptLevel;
    private LocalDateTime createdAt;

    /** Đã hoàn thành onboarding chưa (suy từ targetJlptLevel != null) — quyết định có ép vào /onboarding. */
    private boolean onboardingCompleted;
}
