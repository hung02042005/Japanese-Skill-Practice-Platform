/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SoftDeleteUserResponse {

    private Long userId;
    private String userType;
    private String status;
    private LocalDateTime deletedAt;
}
