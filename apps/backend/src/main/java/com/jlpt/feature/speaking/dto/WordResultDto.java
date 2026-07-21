/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Chi tiết phát âm từng từ/cụm (UC-13 kết quả AI). Dùng cho cả JSON lưu DB lẫn API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WordResultDto {

    private String word;
    private Boolean correct;
    private String feedback; // gợi ý khi phát âm chưa đạt (null nếu đạt)
}
