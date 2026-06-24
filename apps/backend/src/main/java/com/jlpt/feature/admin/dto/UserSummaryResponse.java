/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummaryResponse {

    private Long userId;
    private String userType;
    private String fullName;
    private String email;
    private String status;
    private String currentJlptLevel;
    private String staffRole;
    private Integer currentStreak;
    private LocalDateTime createdAt;
}
