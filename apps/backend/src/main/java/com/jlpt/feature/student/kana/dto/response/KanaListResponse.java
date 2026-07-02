/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kana.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanaListResponse {
    private List<KanaResponse> characters;
    private long completedCount;
    private long totalCount;
}
