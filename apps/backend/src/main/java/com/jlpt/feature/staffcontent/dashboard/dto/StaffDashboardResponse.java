/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.dashboard.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Tổng quan bảng điều hành của Staff. */
@Data
@Builder
public class StaffDashboardResponse {
    private long draftCount;
    private long pendingReviewCount;
    private long openTicketCount; // tính năng ticket chưa làm → 0
    private long pendingGradingCount; // chấm bài nói chưa làm → 0
    private List<ActivityItem> recentActivity;

    @Data
    @Builder
    public static class ActivityItem {
        private Long id;
        private String date; // dd/MM/yyyy
        private String type; // Quiz | Đề thi
        private String title;
        private String status;
    }
}
