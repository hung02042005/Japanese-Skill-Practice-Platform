/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** UC-33 §6.1 — Một dòng trong hàng đợi duyệt. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewQueueItemResponse {
    private final Long contentId;
    private final String contentType;
    private final String titleOrText;
    private final String jlptLevel;
    private final String submittedBy;
    private final Long submittedById;
    private final LocalDateTime submittedAt;
}
