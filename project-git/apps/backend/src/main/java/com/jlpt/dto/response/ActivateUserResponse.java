/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivateUserResponse {

    private Long userId;
    private String userType;
    private String status;
    private LocalDateTime activatedAt;
}
