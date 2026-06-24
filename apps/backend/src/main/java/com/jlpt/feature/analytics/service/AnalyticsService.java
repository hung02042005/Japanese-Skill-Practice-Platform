/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.service;

import com.jlpt.shared.audit.AdminAuditLog;
import com.jlpt.feature.assessment.entity.Assessment;
import com.jlpt.feature.assessment.entity.AttemptAnswer;
import com.jlpt.feature.corelearning.entity.StudentContentProgress;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.feature.assessment.entity.TestAttempt;
import com.jlpt.feature.support.entity.Ticket;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.feature.admin.dto.DashboardResponse;
import com.jlpt.feature.analytics.dto.AdminReportResponse;
import com.jlpt.feature.analytics.dto.AnalyticsResponse;
import com.jlpt.feature.analytics.dto.CompletionRateResponse;
import com.jlpt.feature.analytics.dto.ExamAttemptResponse;
import com.jlpt.feature.analytics.dto.QuizStatsResponse;
import com.jlpt.feature.analytics.dto.StreakDetailResponse;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.assessment.repository.AssessmentRepository;
import com.jlpt.feature.assessment.repository.AttemptAnswerRepository;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.feature.corelearning.repository.StudentContentProgressRepository;
import com.jlpt.feature.corelearning.repository.StudentSubmissionRepository;
import com.jlpt.feature.student.repository.StudentUserRepository;
import com.jlpt.feature.assessment.repository.TestAttemptRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final StudentUserRepository studentUserRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final StaffUserRepository staffUserRepository;
    private final AssessmentRepository assessmentRepository;

    @Autowired(required = false)
    private StudentContentProgressRepository progressRepository;

    @Autowired(required = false)
    private StudentSubmissionRepository submissionRepository;

    @Autowired(required = false)
    private TestAttemptRepository testAttemptRepository;

    @Autowired(required = false)
    private AttemptAnswerRepository attemptAnswerRepository;

    @Autowired(required = false)
    private TicketRepository ticketRepository;

    // ── UC-19: Student progress analytics ───────────────────────────────────

    @Transactional(readOnly = true)
    public AnalyticsResponse getStudentProgressAnalytics(Long studentId) {
        StudentUser student = findStudentOrThrow(studentId);

        String streakStatus = computeStreakStatus(student.getLastActivityDate());
        String streakMsg = buildStreakMessage(streakStatus);

        Map<String, Integer> completions = buildCompletions(studentId);
        Map<String, Double> completionRates = buildCompletionRates(studentId, completions);
        Map<String, Double> skillsRadar = buildSkillsRadar(student);

        return AnalyticsResponse.builder()
                .studentId(student.getId())
                .currentStreak(student.getCurrentStreak())
                .longestStreak(student.getLongestStreak())
                .lastActivityDate(student.getLastActivityDate())
                .streakStatus(streakStatus)
                .streakStatusMessage(streakMsg)
                .completions(completions)
                .completionRates(completionRates)
                .skillsRadar(skillsRadar)
                .build();
    }

    // ── UC-19: Streak detail ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public StreakDetailResponse getStudyStreakDetail(Long studentId) {
        StudentUser student = findStudentOrThrow(studentId);
        String streakStatus = computeStreakStatus(student.getLastActivityDate());
        String streakMsg = buildStreakMessage(streakStatus);

        return StreakDetailResponse.builder()
                .studentId(student.getId())
                .currentStreak(student.getCurrentStreak())
                .longestStreak(student.getLongestStreak())
                .lastActivityDate(student.getLastActivityDate())
                .streakStatus(streakStatus)
                .streakStatusMessage(streakMsg)
                .build();
    }

    // ── UC-19: Completion rate ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CompletionRateResponse getCompletionRate(Long studentId, String contentType) {
        findStudentOrThrow(studentId);

        if (contentType != null) {
            // Single content type
            StudentContentProgress.ContentType type = parseContentType(contentType);
            long total = progressRepository != null
                    ? progressRepository.countByStudentIdAndContentType(studentId, type) : 0L;
            long completed = progressRepository != null
                    ? progressRepository.countCompletedByStudentIdAndContentType(studentId, type) : 0L;
            double rate = computeRate(completed, total);

            return CompletionRateResponse.builder()
                    .contentType(contentType)
                    .totalCount(total)
                    .completedCount(completed)
                    .completionRate(rate)
                    .build();
        }

        // Aggregate all content types
        long totalAll = 0L;
        long completedAll = 0L;
        for (StudentContentProgress.ContentType type : StudentContentProgress.ContentType.values()) {
            totalAll += progressRepository != null
                    ? progressRepository.countByStudentIdAndContentType(studentId, type) : 0L;
            completedAll += progressRepository != null
                    ? progressRepository.countCompletedByStudentIdAndContentType(studentId, type) : 0L;
        }

        return CompletionRateResponse.builder()
                .contentType(null)
                .totalCount(totalAll)
                .completedCount(completedAll)
                .completionRate(computeRate(completedAll, totalAll))
                .build();
    }

    // ── UC-32: Quiz stats ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public QuizStatsResponse getQuizStats(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra này"));

        if (testAttemptRepository == null || attemptAnswerRepository == null) {
            // Repositories not yet available — return empty stats
            return QuizStatsResponse.builder()
                    .assessmentId(assessmentId)
                    .title(assessment.getTitle())
                    .totalAttempts(0)
                    .averageScore(0.0)
                    .maxScore(assessment.getTotalScore() != null ? assessment.getTotalScore().doubleValue() : 0.0)
                    .passRate(0.0)
                    .questionAccuracy(List.of())
                    .build();
        }

        // Server-side aggregation — FR-ANALYTICS-10/11
        long totalAttempts = testAttemptRepository.countSubmittedByParentId(assessmentId);
        BigDecimal avgScore = testAttemptRepository.avgScoreByParentId(assessmentId);
        long passedCount = testAttemptRepository.countPassedByParentId(assessmentId);
        double passRate = totalAttempts == 0 ? 0.0
                : Math.round((passedCount * 100.0 / totalAttempts) * 10.0) / 10.0;

        // Per-question accuracy
        List<TestAttempt> attempts = testAttemptRepository.findSubmittedByParentId(assessmentId);
        List<Long> attemptIds = attempts.stream().map(TestAttempt::getId).toList();
        List<QuizStatsResponse.QuestionAccuracyItem> questionAccuracy = buildQuestionAccuracy(attemptIds, (int) totalAttempts);

        return QuizStatsResponse.builder()
                .assessmentId(assessmentId)
                .title(assessment.getTitle())
                .totalAttempts((int) totalAttempts)
                .averageScore(avgScore != null ? avgScore.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0)
                .maxScore(assessment.getTotalScore() != null ? assessment.getTotalScore().doubleValue() : 0.0)
                .passRate(passRate)
                .questionAccuracy(questionAccuracy)
                .build();
    }

    // ── UC-32: Student exam history ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ExamAttemptResponse> getStudentExamHistory(Long studentId, int page, int size) {
        findStudentOrThrow(studentId);

        if (testAttemptRepository == null) {
            return Page.empty();
        }

        Page<TestAttempt> attempts = testAttemptRepository.findByStudentIdAndAttemptTypeIn(
                studentId,
                List.of(TestAttempt.AttemptType.EXAM, TestAttempt.AttemptType.QUIZ),
                PageRequest.of(page, size));

        List<ExamAttemptResponse> responses = attempts.getContent().stream()
                .map(this::toExamAttemptResponse)
                .toList();

        return new PageImpl<>(responses, attempts.getPageable(), attempts.getTotalElements());
    }

    // ── UC-38: Admin dashboard ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardResponse getAdminDashboard() {
        long totalStudents = studentUserRepository.count();
        long activeStudents = studentUserRepository.countByStatusValue("active");
        long suspendedStudents = studentUserRepository.countByStatusValue("suspended");
        long newStudentsThisMonth = studentUserRepository.countCreatedThisMonth();

        long openTickets = ticketRepository != null
                ? ticketRepository.countByStatus(Ticket.TicketStatus.OPEN) : 0L;
        long inProgressTickets = ticketRepository != null
                ? ticketRepository.countByStatus(Ticket.TicketStatus.IN_PROGRESS) : 0L;
        long pendingSubmissions = submissionRepository != null
                ? submissionRepository.countPendingManualGrade() : 0L;

        List<DashboardResponse.RecentActivityItem> recentActivity =
                adminAuditLogRepository.findTop5ByOrderByCreatedAtDesc().stream()
                        .map(this::toActivityItem)
                        .toList();

        return DashboardResponse.builder()
                .generatedAt(LocalDateTime.now())
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .suspendedStudents(suspendedStudents)
                .newStudentsThisMonth(newStudentsThisMonth)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .pendingSubmissions(pendingSubmissions)
                .recentActivity(recentActivity)
                .build();
    }

    // ── UC-36: Admin dashboard summary (compact for admin home page) ─────────

    @Transactional(readOnly = true)
    public com.jlpt.feature.admin.dto.AdminDashboardSummaryResponse getAdminDashboardSummary(
            boolean isMaintenanceMode) {
        long totalStudents = studentUserRepository.count();
        long totalStaff = staffUserRepository.count();
        long activeToday = studentUserRepository.countActiveToday();
        long quizAttemptsToday = testAttemptRepository != null
                ? testAttemptRepository.countTodayAttempts() : 0L;
        String systemStatus = isMaintenanceMode ? "MAINTENANCE" : "OK";
        return com.jlpt.feature.admin.dto.AdminDashboardSummaryResponse.builder()
                .totalUsers(totalStudents + totalStaff)
                .activeToday(activeToday)
                .quizAttemptsToday(quizAttemptsToday)
                .systemStatus(systemStatus)
                .build();
    }

    // ── UC-36: Audit log paginated ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<com.jlpt.feature.admin.dto.AuditLogItemResponse> getAuditLogPaginated(
            String action, int page, int size) {
        Page<AdminAuditLog> logs = adminAuditLogRepository.findAllByActionFilter(
                action,
                PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        return logs.map(this::toAuditLogItem);
    }

    private com.jlpt.feature.admin.dto.AuditLogItemResponse toAuditLogItem(AdminAuditLog log) {
        String actorEmail = log.getAdminActor() != null
                ? log.getAdminActor().getEmail()
                : (log.getStaffActor() != null ? log.getStaffActor().getEmail() : null);
        return com.jlpt.feature.admin.dto.AuditLogItemResponse.builder()
                .logId(log.getId())
                .actionType(log.getAction() != null ? log.getAction().toLowerCase() : null)
                .adminEmail(actorEmail)
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }

    // ── Staff dashboard ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardResponse getStaffDashboard(String staffEmail) {
        Long staffId = staffUserRepository.findByEmail(staffEmail)
                .map(s -> s.getId())
                .orElse(null);

        long myOpen = (staffId != null && ticketRepository != null)
                ? ticketRepository.countByAssignedToIdAndStatus(staffId, Ticket.TicketStatus.OPEN) : 0L;
        long myInProgress = (staffId != null && ticketRepository != null)
                ? ticketRepository.countByAssignedToIdAndStatus(staffId, Ticket.TicketStatus.IN_PROGRESS) : 0L;
        long myPendingGrades = submissionRepository != null
                ? submissionRepository.countPendingManualGrade() : 0L;

        Page<Ticket> recentTickets = ticketRepository != null
                ? ticketRepository.findAllByFilters(null, null, null, null, PageRequest.of(0, 5))
                : Page.empty();

        List<DashboardResponse.RecentActivityItem> recentActivity = recentTickets.stream()
                .map(t -> DashboardResponse.RecentActivityItem.builder()
                        .actorName(t.getStudent().getFullName())
                        .actorType("STUDENT")
                        .action("Tạo ticket: " + t.getSubject())
                        .timestamp(t.getCreatedAt())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .generatedAt(LocalDateTime.now())
                .myOpenTickets(myOpen)
                .myInProgressTickets(myInProgress)
                .myPendingGrades(myPendingGrades)
                .recentActivity(recentActivity)
                .build();
    }

    // ── UC-38: Admin report ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminReportResponse getAdminReport(LocalDate startDate, LocalDate endDate) {
        // Validate date range (FR-ANALYTICS-21, SPEC §10)
        if (!startDate.isBefore(endDate)) {
            throw new BadRequestException("startDate phải trước endDate");
        }

        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(23, 59, 59);

        long newRegistrations = studentUserRepository.countByCreatedAtBetween(from, to);

        long totalExamAttempts = testAttemptRepository != null
                ? testAttemptRepository.countExamAttemptsBetween(from, to) : 0L;

        BigDecimal avgScoreRaw = testAttemptRepository != null
                ? testAttemptRepository.avgExamScoreBetween(from, to) : null;
        double avgExamScore = avgScoreRaw != null
                ? avgScoreRaw.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0;

        // Build per-level completion rates
        List<AdminReportResponse.LevelCompletionRate> courseCompletionRates =
                buildLevelCompletionRates();

        return AdminReportResponse.builder()
                .period(AdminReportResponse.ReportPeriod.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build())
                .newRegistrations(newRegistrations)
                .totalExamAttempts(totalExamAttempts)
                .avgExamScore(avgExamScore)
                .courseCompletionRates(courseCompletionRates)
                .build();
    }

    // ── UC-38: Export report CSV/XLSX ────────────────────────────────────────

    public Map<String, Object> exportReport(LocalDate startDate, LocalDate endDate, String format) {
        // Validate date range
        if (!startDate.isBefore(endDate)) {
            throw new BadRequestException("startDate phải trước endDate");
        }

        // Validate format
        if (!"csv".equalsIgnoreCase(format) && !"xlsx".equalsIgnoreCase(format)) {
            throw new BadRequestException("Format không hợp lệ. Chỉ hỗ trợ: csv, xlsx");
        }

        AdminReportResponse reportData = getAdminReport(startDate, endDate);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "report-" + timestamp + "." + format.toLowerCase();

        // Ensure exports directory exists
        File exportsDir = new File("uploads/exports");
        if (!exportsDir.exists()) {
            exportsDir.mkdirs();
        }

        String filePath = "uploads/exports/" + fileName;

        if ("xlsx".equalsIgnoreCase(format)) {
            generateXlsx(filePath, reportData);
        } else {
            generateCsv(filePath, reportData);
        }

        // Log per NFR-ANALYTICS-05
        log.warn("Admin exported report format={} file={} period={} to {}",
                format, fileName, startDate + " to " + endDate, filePath);

        // Return download URL (relative — frontend will prefix with base URL)
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        return Map.of(
                "downloadUrl", "/exports/" + fileName,
                "expiresAt", expiresAt.toString());
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private StudentUser findStudentOrThrow(Long id) {
        return studentUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên"));
    }

    private String computeStreakStatus(LocalDate lastActivity) {
        if (lastActivity == null) return "BROKEN";
        LocalDate today = LocalDate.now();
        if (lastActivity.equals(today)) return "ACTIVE";
        if (lastActivity.equals(today.minusDays(1))) return "AT_RISK";
        return "BROKEN";
    }

    private String buildStreakMessage(String streakStatus) {
        return switch (streakStatus) {
            case "ACTIVE" -> "Tuyệt vời! Bạn đã học hôm nay. Hãy tiếp tục phong độ!";
            case "AT_RISK" -> "Bạn chưa học hôm nay. Hãy học ngay để duy trì streak!";
            default -> "Chuỗi học tập đã bị ngắt. Hãy bắt đầu lại ngay hôm nay!";
        };
    }

    private Map<String, Integer> buildCompletions(Long studentId) {
        Map<String, Integer> result = new HashMap<>();
        for (StudentContentProgress.ContentType type : StudentContentProgress.ContentType.values()) {
            long count = progressRepository != null
                    ? progressRepository.countCompletedByStudentIdAndContentType(studentId, type) : 0L;
            result.put(type.getValue(), (int) count);
        }
        return result;
    }

    private Map<String, Double> buildCompletionRates(Long studentId, Map<String, Integer> completions) {
        Map<String, Double> rates = new HashMap<>();
        for (StudentContentProgress.ContentType type : StudentContentProgress.ContentType.values()) {
            long total = progressRepository != null
                    ? progressRepository.countByStudentIdAndContentType(studentId, type) : 0L;
            int completed = completions.getOrDefault(type.getValue(), 0);
            double rate = total == 0 ? 0.0 : Math.round((completed * 100.0 / total) * 10.0) / 10.0;
            rates.put(type.getValue(), rate);
        }
        return rates;
    }

    private Map<String, Double> buildSkillsRadar(StudentUser student) {
        Map<String, Double> radar = new HashMap<>();
        radar.put("grammar", 0.0);
        radar.put("vocabulary", 0.0);
        radar.put("reading", 0.0);
        radar.put("listening", 0.0);

        if (submissionRepository != null) {
            Double speakingAvg = submissionRepository.avgSpeakingScoreByStudentId(student.getId());
            radar.put("speaking", speakingAvg != null ? speakingAvg : 0.0);
        } else {
            radar.put("speaking", 0.0);
        }

        if (testAttemptRepository != null) {
            List<TestAttempt> exams = testAttemptRepository.findByStudentIdAndAttemptTypeIn(
                    student.getId(),
                    List.of(TestAttempt.AttemptType.EXAM, TestAttempt.AttemptType.QUIZ),
                    PageRequest.of(0, 50)).getContent();

            double grammarSum = 0, grammarCount = 0;
            double readingSum = 0, readingCount = 0;
            double listeningSum = 0, listeningCount = 0;
            for (TestAttempt t : exams) {
                if (t.getLanguageKnowledgeScore() != null) {
                    grammarSum += t.getLanguageKnowledgeScore().doubleValue();
                    grammarCount++;
                }
                if (t.getReadingScore() != null) {
                    readingSum += t.getReadingScore().doubleValue();
                    readingCount++;
                }
                if (t.getListeningScore() != null) {
                    listeningSum += t.getListeningScore().doubleValue();
                    listeningCount++;
                }
            }
            if (grammarCount > 0) radar.put("grammar", grammarSum / grammarCount);
            if (readingCount > 0) radar.put("reading", readingSum / readingCount);
            if (listeningCount > 0) radar.put("listening", listeningSum / listeningCount);
        }
        return radar;
    }

    private List<QuizStatsResponse.QuestionAccuracyItem> buildQuestionAccuracy(
            List<Long> attemptIds, int totalAttempts) {
        if (attemptAnswerRepository == null || attemptIds.isEmpty()) return List.of();

        List<AttemptAnswer> allAnswers = attemptAnswerRepository.findByAttemptIdIn(attemptIds);

        // Group by question
        Map<Long, List<AttemptAnswer>> byQuestion = allAnswers.stream()
                .filter(a -> a.getQuestion() != null)
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        List<QuizStatsResponse.QuestionAccuracyItem> items = new ArrayList<>();
        for (Map.Entry<Long, List<AttemptAnswer>> entry : byQuestion.entrySet()) {
            Long questionId = entry.getKey();
            List<AttemptAnswer> answers = entry.getValue();
            String questionText = answers.get(0).getQuestion().getQuestionText();

            long correctCount = answers.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .count();
            long incorrectCount = answers.size() - correctCount;
            double accuracyPercent = totalAttempts == 0 ? 0.0
                    : Math.round((correctCount * 100.0 / totalAttempts) * 10.0) / 10.0;

            items.add(QuizStatsResponse.QuestionAccuracyItem.builder()
                    .questionId(questionId)
                    .questionText(questionText)
                    .correctCount((int) correctCount)
                    .incorrectCount((int) incorrectCount)
                    .accuracyPercent(accuracyPercent)
                    .build());
        }
        return items;
    }

    private ExamAttemptResponse toExamAttemptResponse(TestAttempt t) {
        String title = null;
        if (t.getParentType() == TestAttempt.ParentType.ASSESSMENT && t.getParentId() != null) {
            title = assessmentRepository.findById(t.getParentId())
                    .map(Assessment::getTitle)
                    .orElse(null);
        }
        return ExamAttemptResponse.builder()
                .attemptId(t.getId())
                .attemptType(t.getAttemptType().getValue())
                .assessmentTitle(title)
                .totalScore(t.getTotalScore())
                .maxScore(t.getMaxScore())
                .isPassed(t.getIsPassed())
                .startedAt(t.getStartedAt())
                .submittedAt(t.getSubmittedAt())
                .build();
    }

    private List<AdminReportResponse.LevelCompletionRate> buildLevelCompletionRates() {
        List<AdminReportResponse.LevelCompletionRate> rates = new ArrayList<>();
        for (StudentUser.JlptLevel level : StudentUser.JlptLevel.values()) {
            long totalStudents = studentUserRepository.countByCurrentJlptLevel(level);
            long completedStudents = testAttemptRepository != null
                    ? testAttemptRepository.countDistinctPassedStudentsByJlptLevel(level) : 0L;

            rates.add(AdminReportResponse.LevelCompletionRate.builder()
                    .jlptLevel(level.name())
                    .completedStudentsCount(completedStudents)
                    .totalStudentsCount(totalStudents)
                    .completionRate(computeRate(completedStudents, totalStudents))
                    .build());
        }
        return rates;
    }

    private DashboardResponse.RecentActivityItem toActivityItem(AdminAuditLog log) {
        String actorName = log.getAdminActor() != null
                ? log.getAdminActor().getFullName()
                : (log.getStaffActor() != null ? log.getStaffActor().getFullName() : "System");
        String actorType = log.getAdminActor() != null ? "ADMIN" : "STAFF";
        return DashboardResponse.RecentActivityItem.builder()
                .actorName(actorName)
                .actorType(actorType)
                .action(log.getAction())
                .timestamp(log.getCreatedAt())
                .build();
    }

    private double computeRate(long completed, long total) {
        if (total == 0) return 0.0;
        return Math.round((completed * 100.0 / total) * 10.0) / 10.0;
    }

    private StudentContentProgress.ContentType parseContentType(String contentType) {
        try {
            return StudentContentProgress.ContentType.valueOf(contentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "contentType không hợp lệ. Các giá trị hợp lệ: lesson, vocabulary, kanji, kana, grammar");
        }
    }

    // ── Export helpers ───────────────────────────────────────────────────────

    private void generateXlsx(String filePath, AdminReportResponse data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Metric");
            header.createCell(1).setCellValue("Value");

            // Summary data
            int rowNum = 1;
            Row r1 = sheet.createRow(rowNum++);
            r1.createCell(0).setCellValue("Period Start");
            r1.createCell(1).setCellValue(data.getPeriod().getStartDate().toString());

            Row r2 = sheet.createRow(rowNum++);
            r2.createCell(0).setCellValue("Period End");
            r2.createCell(1).setCellValue(data.getPeriod().getEndDate().toString());

            Row r3 = sheet.createRow(rowNum++);
            r3.createCell(0).setCellValue("New Registrations");
            r3.createCell(1).setCellValue(data.getNewRegistrations());

            Row r4 = sheet.createRow(rowNum++);
            r4.createCell(0).setCellValue("Total Exam Attempts");
            r4.createCell(1).setCellValue(data.getTotalExamAttempts());

            Row r5 = sheet.createRow(rowNum++);
            r5.createCell(0).setCellValue("Avg Exam Score");
            r5.createCell(1).setCellValue(data.getAvgExamScore());

            // Level completion rates
            rowNum++; // blank row
            Row levelHeader = sheet.createRow(rowNum++);
            levelHeader.createCell(0).setCellValue("JLPT Level");
            levelHeader.createCell(1).setCellValue("Total Students");
            levelHeader.createCell(2).setCellValue("Completion Rate (%)");

            if (data.getCourseCompletionRates() != null) {
                for (AdminReportResponse.LevelCompletionRate lvl : data.getCourseCompletionRates()) {
                    Row levelRow = sheet.createRow(rowNum++);
                    levelRow.createCell(0).setCellValue(lvl.getJlptLevel());
                    levelRow.createCell(1).setCellValue(lvl.getTotalStudentsCount());
                    levelRow.createCell(2).setCellValue(lvl.getCompletionRate());
                }
            }

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            log.error("Failed to generate XLSX report: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi xuất báo cáo XLSX, vui lòng thử lại");
        }
    }

    private void generateCsv(String filePath, AdminReportResponse data) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(filePath))) {
            writer.println("Metric,Value");
            writer.println("Period Start," + data.getPeriod().getStartDate());
            writer.println("Period End," + data.getPeriod().getEndDate());
            writer.println("New Registrations," + data.getNewRegistrations());
            writer.println("Total Exam Attempts," + data.getTotalExamAttempts());
            writer.println("Avg Exam Score," + data.getAvgExamScore());
            writer.println();
            writer.println("JLPT Level,Total Students,Completion Rate (%)");
            if (data.getCourseCompletionRates() != null) {
                for (AdminReportResponse.LevelCompletionRate lvl : data.getCourseCompletionRates()) {
                    writer.println(lvl.getJlptLevel() + "," + lvl.getTotalStudentsCount()
                            + "," + lvl.getCompletionRate());
                }
            }
        } catch (IOException e) {
            log.error("Failed to generate CSV report: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi xuất báo cáo CSV, vui lòng thử lại");
        }
    }
}
