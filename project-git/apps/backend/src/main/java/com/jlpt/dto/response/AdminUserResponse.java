/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUserResponse {
    private Long id;
    private String userType; // STUDENT | STAFF | ADMIN
    private String fullName;
    private String email;
    private String role; // same as userType, for UI display
    private String jlptLevel;
    private String status; // ACTIVE | BANNED (BANNED maps from SUSPENDED in DB)
    private Integer streak;
    private LocalDateTime createdAt;
}
