/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/** Nhan vien ho tro co the duoc giao ticket (assignee picker — UC-29). */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffMemberResponse {

    private Long staffId;
    private String fullName;
    private String email;
    private String staffRole;
    private Long assignedOpenCount;
}
