/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import lombok.Builder;
import lombok.Data;

/** Response returned by POST /api/auth/verify-mfa on successful 2FA (UC-35 step 20). */
@Data
@Builder
public class AdminVerifyMfaResponse {

    private String accessToken;
    private String refreshToken;
    private AdminInfo admin;

    @Data
    @Builder
    public static class AdminInfo {
        private Long adminId;
        private String fullName;
        private String email;
    }
}
