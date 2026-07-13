/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.dto.response.StudentResponse;
import org.springframework.stereotype.Component;

/** Map StudentUser -> StudentResponse dùng chung giữa các service auth/profile. */
@Component
public class StudentResponseMapper {

    public StudentResponse toResponse(StudentUser user) {
        return StudentResponse.builder()
                .studentId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .currentJlptLevel(
                        user.getCurrentJlptLevel() != null
                                ? user.getCurrentJlptLevel().name()
                                : null)
                .targetJlptLevel(
                        user.getTargetJlptLevel() != null
                                ? user.getTargetJlptLevel().name()
                                : null)
                .createdAt(user.getCreatedAt())
                .onboardingCompleted(user.getTargetJlptLevel() != null)
                .build();
    }
}
