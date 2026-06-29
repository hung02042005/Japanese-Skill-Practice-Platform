/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.feedback;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** Phản hồi của manager (từ chối / yêu cầu chỉnh sửa) gửi về cho staff xem. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewFeedbackResponse {
    private final String feedback;
    private final String actionType;
    private final LocalDateTime reviewedAt;
}
