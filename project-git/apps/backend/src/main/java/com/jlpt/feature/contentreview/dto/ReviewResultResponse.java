/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** UC-33 §6.3/§6.4 — Kết quả sau Approve / Reject / Request Changes. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResultResponse {
    private final Long contentId;
    private final String contentType;
    private final String status;
    private final LocalDateTime approvedAt;
}
