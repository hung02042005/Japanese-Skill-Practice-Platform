/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketResponse {

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
    private Long replyCount;
    private LocalDateTime lastReplyAt;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
