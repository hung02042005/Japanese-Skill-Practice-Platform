/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto.response;

import com.jlpt.feature.student.dto.response.StudentResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private StudentResponse student;
}
