/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateStaffResponse {

    private Long staffId;
    private String fullName;
    private String email;
    private String staffRole;
    private String status;
}
