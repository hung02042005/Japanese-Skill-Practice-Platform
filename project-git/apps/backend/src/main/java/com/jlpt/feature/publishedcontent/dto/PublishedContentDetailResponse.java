/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** UC-34 — Chi tiết nội dung kèm danh sách tham chiếu đang dùng (FR-34-07). */
@Data
@Builder
public class PublishedContentDetailResponse {
    private Long contentId;
    private String contentType;
    private String titleOrText;
    private String jlptLevel;
    private String status;
    private LocalDateTime publishedAt;
    private List<ReferenceItemResponse> references;
}
