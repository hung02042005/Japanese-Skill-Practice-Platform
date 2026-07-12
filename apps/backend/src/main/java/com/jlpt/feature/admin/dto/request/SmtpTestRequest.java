/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.request;

import lombok.Data;

@Data
public class SmtpTestRequest {
    private String host;
    private String port;
    private String username;
    private String password;
    private String secure;
}
