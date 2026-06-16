/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/** UC-33 §6.2 — Chi tiết nội dung phục vụ kiểm duyệt (không lộ Entity — ADR-005). */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewableContentDetailResponse {
    private final Long contentId;
    private final String contentType;
    private final String titleOrText;
    private final String jlptLevel;
    private final String status;
    private final Long submittedById;
    private final String submittedBy;
    private final LocalDateTime submittedAt;
    private final Map<String, Object> detail;
}
