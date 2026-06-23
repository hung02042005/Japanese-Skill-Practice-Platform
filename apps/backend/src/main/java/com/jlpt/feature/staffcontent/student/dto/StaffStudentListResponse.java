/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.student.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Trang danh sách học viên cho Staff. */
@Data
@Builder
public class StaffStudentListResponse {
    private List<StaffStudentSummaryResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
