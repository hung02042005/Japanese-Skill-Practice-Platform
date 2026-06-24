/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDetailResponse {

    private Long adminId;
    private String fullName;
    private String email;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
