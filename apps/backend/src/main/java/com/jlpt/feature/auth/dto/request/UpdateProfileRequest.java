/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String targetJlptLevel;
}
