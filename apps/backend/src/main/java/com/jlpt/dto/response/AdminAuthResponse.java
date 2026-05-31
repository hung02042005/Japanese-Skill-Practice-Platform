/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tempToken; // Used for 2FA flow if required
    private boolean requires2Fa; // True if login was successful but needs 2FA code
    private AdminProfileResponse admin;
}
