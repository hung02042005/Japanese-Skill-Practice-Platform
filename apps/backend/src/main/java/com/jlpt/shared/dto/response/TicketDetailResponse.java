/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDetailResponse {

    private Long ticketId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String subject;
    private String content;
    private String category;
    private String priority;
    private String status;
    private Long assignedToStaffId;
    private String assignedToStaffName;
    private LocalDateTime lastReplyAt;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private List<TicketReplyResponse> replies;
}
