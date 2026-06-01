/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Unified response for POST /api/auth/login across all roles.
 * Null fields are omitted so admin (MFA step-1) and student/staff (direct) shapes differ cleanly.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginApiResponse {

    /** true = admin 2FA challenge; false / absent = direct login (student/staff) */
    private Boolean requiresTwoFactor;

    /** Short-lived MFA temp token — only present when requiresTwoFactor = true */
    private String mfaToken;

    /** Role of the authenticated principal: ADMIN | STAFF | STUDENT */
    private String role;

    /** JWT access token — present only on direct login (student/staff) */
    private String accessToken;

    /** Refresh token — present only on direct login (student/staff) */
    private String refreshToken;

    /** Logged-in user info — present only on direct login (student/staff) */
    private StudentResponse user;
}
