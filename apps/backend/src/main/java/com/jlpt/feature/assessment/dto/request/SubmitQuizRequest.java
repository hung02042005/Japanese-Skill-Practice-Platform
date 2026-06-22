/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Data;

@Data
public class SubmitQuizRequest {

    @NotNull(message = "attemptId không hợp lệ") @Positive(message = "attemptId không hợp lệ") private Long attemptId;

    @NotEmpty(message = "Danh sách đáp án không được rỗng")
    @Valid
    private List<AnswerRequest> answers;
}
