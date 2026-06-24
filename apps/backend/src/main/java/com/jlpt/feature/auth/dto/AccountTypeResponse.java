/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountTypeResponse {
    private String accountType;
}
