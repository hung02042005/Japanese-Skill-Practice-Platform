/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Staff tạo chủ đề từ vựng mới (POST /api/staff/vocabulary-topics).
 * {@code slug} tùy chọn — nếu bỏ trống sẽ tự sinh từ {@code titleVi}.
 */
@Data
public class CreateVocabTopicRequest {

    @NotBlank(message = "Cấp độ JLPT phải là N5 đến N1")
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    @NotBlank(message = "Thiếu trường bắt buộc: titleJa")
    @Size(max = 100)
    private String titleJa;

    @NotBlank(message = "Thiếu trường bắt buộc: titleVi")
    @Size(max = 100)
    private String titleVi;

    @Size(max = 80)
    private String slug;
}
