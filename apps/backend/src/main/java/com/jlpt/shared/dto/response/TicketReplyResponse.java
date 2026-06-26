/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketReplyResponse {

    private Long replyId;
    private String senderName;
    private String senderRole;
    private String message;
    private String attachmentUrl;
    private LocalDateTime createdAt;
}
