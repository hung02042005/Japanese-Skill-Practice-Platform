/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {

    private LocalDateTime generatedAt;

    /* ── Admin-only fields ──────────────────────────────────── */
    private Long totalStudents;
    private Long activeStudents;
    private Long suspendedStudents;
    private Long newStudentsThisMonth;
    private Long openTickets;
    private Long inProgressTickets;
    private Long resolvedTicketsThisMonth;
    private Long pendingSubmissions;
    private Long gradedSubmissionsThisMonth;

    /* ── Staff-only fields ──────────────────────────────────── */
    private Long myOpenTickets;
    private Long myInProgressTickets;
    private Long myPendingGrades;

    /* ── Common ─────────────────────────────────────────────── */
    private List<RecentActivityItem> recentActivity;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecentActivityItem {
        private String actorName;
        private String actorType;
        private String action;
        private LocalDateTime timestamp;
    }
}
