/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jlpt.feature.student.dto.response.StudentResponse;
import lombok.Builder;
import lombok.Data;

/**
 * Unified response for POST /api/auth/login across all roles.
 * Null fields are omitted via @JsonInclude(NON_NULL).
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginApiResponse {

    /** Role of the authenticated principal: ADMIN | STAFF | STUDENT */
    private String role;

    private Boolean requirePasswordChange;

    /** JWT access token — present only on direct login (student/staff) */
    private String accessToken;

    /** Refresh token — present only on direct login (student/staff) */
    private String refreshToken;

    /** Logged-in user info — present only on direct login (student/staff) */
    private StudentResponse user;

    /** Staff sub-role: "staff" | "staff_manager" — present only for STAFF role */
    private String staffRole;
}
