/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffResetRequestResponse {
    private Long requestId;
    private Long staffId;
    private String staffName;
    private String staffEmail;
    private LocalDateTime requestedAt;
    private LocalDateTime expiresAt;
    private String status;
}
