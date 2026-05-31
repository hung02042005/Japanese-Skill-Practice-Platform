/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminProfileResponse {
    private Long adminId;
    private String email;
    private String fullName;
    private LocalDateTime createdAt;
}
