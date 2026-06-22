/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Data;

/** Adapter DTO khớp contract cũ {quizId, answers} của studentService.js#submitPracticeQuiz. */
@Data
public class LegacySubmitQuizRequest {

    @NotNull(message = "quizId không hợp lệ") @Positive(message = "quizId không hợp lệ") private Long quizId;

    @NotEmpty(message = "Danh sách đáp án không được rỗng")
    @Valid
    private List<LegacyAnswerRequest> answers;
}
