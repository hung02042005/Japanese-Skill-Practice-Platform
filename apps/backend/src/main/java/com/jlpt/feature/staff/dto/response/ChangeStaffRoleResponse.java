/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangeStaffRoleResponse {

    private Long staffId;
    private String oldRole;
    private String newRole;
    private LocalDateTime updatedAt;
}
