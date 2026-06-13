/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffDetailResponse {

    private Long staffId;
    private String fullName;
    private String email;
    private String staffRole;
    private String status;
    private String suspendReason;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
