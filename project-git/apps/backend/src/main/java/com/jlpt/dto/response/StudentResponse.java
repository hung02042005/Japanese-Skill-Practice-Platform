/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

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
}
