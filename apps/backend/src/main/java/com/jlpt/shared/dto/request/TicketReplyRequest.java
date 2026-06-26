/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketReplyRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String message;

    @Size(max = 500, message = "URL đính kèm không vượt quá 500 ký tự")
    private String attachmentUrl;
}
