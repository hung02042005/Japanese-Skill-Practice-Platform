/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import java.time.LocalDate;
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
    private LocalDate dateOfBirth;
    private String bio;
    private String avatarUrl;
    private String currentJlptLevel;
    private String targetJlptLevel;
    private LocalDateTime createdAt;
}
