/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProfileResponse {
    private Long adminId;
    private String email;
    private String fullName;
    private LocalDateTime createdAt;
}
