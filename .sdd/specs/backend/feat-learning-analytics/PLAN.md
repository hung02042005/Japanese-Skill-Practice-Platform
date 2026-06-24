# PLAN — Learning Analytics & Reports (`feat-learning-analytics`)
> **Feature ID:** `feat-learning-analytics` | **UC:** UC-19, UC-32, UC-38 | **Version:** 1.0 | **Updated:** 2026-06-16

---

## 1. User Intent

### 1.1 Vấn đề cần giải quyết

Ba nhóm người dùng cần khả năng quan sát và phân tích dữ liệu học tập khác nhau:

1. **Học viên (Student)** — cần thấy tiến độ học tập cá nhân theo từng kỹ năng, chuỗi học tập hàng ngày (streak) và lịch sử điểm thi để tự đánh giá và duy trì động lực.
2. **Nhân viên (Staff)** — cần phân tích kết quả quiz của toàn bộ học viên, nhìn thấy câu hỏi nào khó (độ chính xác thấp) để cải thiện nội dung, và xem dashboard công việc cá nhân.
3. **Quản trị viên (Admin)** — cần dashboard tổng quát về sức khỏe hệ thống (số học viên mới, số ticket, số bài nộp chờ chấm) và xuất báo cáo CSV/XLSX để báo cáo lên ban lãnh đạo.

### 1.2 Người dùng và kỳ vọng

| Actor | Kỳ vọng chính |
|:---|:---|
| **Student** | Analytics load nhanh < 400ms, streak đúng múi giờ, không xem được data của người khác |
| **Staff** | Quiz stats chính xác server-side, dashboard hiện số ticket/bài chấm của mình |
| **Admin** | Dashboard tổng quát load < 1.5s, export report đúng định dạng, không bao giờ tính toán ở client |

### 1.3 Ràng buộc nghiệp vụ quan trọng

- `studentId` trong analytics endpoints **bắt buộc lấy từ JWT** — không từ request param (FR-ANALYTICS-06).
- `streakStatus` tính server-side dựa trên `LocalDate.now()`: `ACTIVE` / `AT_RISK` / `BROKEN`.
- `completionRate` phải xử lý chia-cho-0 → trả `0`, không throw exception.
- Toàn bộ thống kê (accuracy, completion rate, score distribution) phải tính **server-side** — không để client tính.
- Export report phải log `[WARN] Admin {} exported {} format {}` (NFR-ANALYTICS-05).
- `DashboardResponse` dùng chung cho Admin view và Staff view — phân nhánh tại service layer.
- Các phụ thuộc từ Người 3 (`StudentContentProgressRepository`, `TestAttemptRepository`, `StudentSubmissionRepository`) phải được inject với `@Autowired(required = false)` để tránh compile error.

---

## 2. Architectural Blueprint

### 2.1 Layer Stack

```
[React Frontend - Student]        [React Frontend - Staff/Admin]
       |                                    |
       | GET /api/analytics/my-progress     | GET /api/analytics/dashboard
       | GET /api/analytics/streak          | GET /api/analytics/quizzes/{id}/stats
       | GET /api/analytics/completion      | GET /api/analytics/admin/reports
       v                                    v
[StudentAnalyticsController]    [StudentAnalyticsController (Staff/Admin routes)]
  com.jlpt.controller.student   [MOI]
       |                                    |
       +--------------------+---------------+
                            v
              [AnalyticsService]              <- com.jlpt.service  [MOI]
                            |
       +--------------------+-------------------+-------------------+
       v                    v                   v                   v
[StudentContentProgressRepo] [TestAttemptRepo] [StudentSubmissionRepo] [TicketRepo]
(READ-ONLY, Nguoi 3)         (READ-ONLY, N3)  (READ-ONLY, Nguoi 3)    (READ-ONLY, feat-support)
       ^                    ^                   ^                   ^
  required=false       required=false      required=false      required=false

Thêm:
[AdminAuditLogRepository]    <- (đã có) — đọc recentActivity cho Admin dashboard
[StudentUserRepository]      <- (đã có) — đếm totalStudents, activeStudents
```

### 2.2 File Structure

```
controller/student/
  StudentAnalyticsController.java        [MOI] — student + staff/admin analytics routes

service/
  AnalyticsService.java                  [MOI]

dto/response/
  AnalyticsResponse.java                 [MOI]
  StreakDetailResponse.java              [MOI]
  CompletionRateResponse.java            [MOI]
  QuizStatsResponse.java                 [MOI]
  ExamAttemptResponse.java               [MOI]
  DashboardResponse.java                 [MOI] — dùng chung Admin/Staff view
  AdminReportResponse.java               [MOI]
```

> **Lưu ý:** `DashboardResponse.java` được share sang `feat-system-admin` cho `AdminDashboardController.java`. Không tạo duplicate class.

### 2.3 API Routes

| Method | Path | Actor | Mô tả |
|:---|:---|:---|:---|
| GET | `/api/analytics/my-progress` | Student | Tiến độ học tập cá nhân (studentId từ JWT) |
| GET | `/api/analytics/streak` | Student | Chi tiết chuỗi học tập |
| GET | `/api/analytics/completion` | Student | Tỉ lệ hoàn thành theo content type |
| GET | `/api/analytics/quizzes/{assessmentId}/stats` | Staff/Admin | Thống kê quiz + per-question accuracy |
| GET | `/api/analytics/students/{studentId}/exam-history` | Staff/Admin | Lịch sử thi của 1 học viên |
| GET | `/api/analytics/dashboard` | Staff/Admin | Dashboard theo role |
| GET | `/api/analytics/admin/reports` | Admin | Báo cáo tổng quát theo khoảng thời gian |
| GET | `/api/analytics/admin/reports/export` | Admin | Xuất báo cáo CSV/XLSX |

### 2.4 Data Flow — Student Progress Analytics

```
GET /api/analytics/my-progress
  |
  +- [Controller] Lấy studentId từ JWT (Authentication.getName() → email → resolve studentId)
  |               KHÔNG nhận studentId từ request param
  |
  +- [Service] AnalyticsService.getStudentProgressAnalytics(studentId)
  |     +-- Load StudentUser → 404 nếu không có
  |     +-- Tính streakStatus:
  |     |     lastActivityDate == today → ACTIVE
  |     |     lastActivityDate == yesterday → AT_RISK
  |     |     else → BROKEN
  |     +-- Query completions (fallback empty nếu repo null):
  |     |     progressRepo.countCompletedByStudentAndType(studentId, "lesson")
  |     |     progressRepo.countCompletedByStudentAndType(studentId, "kanji") ... v.v.
  |     +-- Tính completionRate = completed / total × 100 (chia 0 → 0)
  |     +-- Build skillsRadar từ:
  |     |     grammar/vocab/reading/listening: avg score từ test_attempts
  |     |     speaking: avg final_score từ student_submissions
  |     +-- Return AnalyticsResponse
```

### 2.5 Data Flow — Admin Dashboard

```
GET /api/analytics/dashboard
  |
  +- [Controller] Xác định role từ JWT
  |     ADMIN → analyticsService.getAdminDashboard()
  |     STAFF → analyticsService.getStaffDashboard(staffId)
  |
  +- [Service] getAdminDashboard()
  |     +-- studentUserRepo.countAll() → totalStudents
  |     +-- studentUserRepo.countByStatus("active") → activeStudents
  |     +-- studentUserRepo.countByStatus("suspended") → suspendedStudents
  |     +-- studentUserRepo.countCreatedThisMonth() → newStudentsThisMonth
  |     +-- ticketRepo?.countByStatus("open") → openTickets (null-safe)
  |     +-- ticketRepo?.countByStatus("in_progress") → inProgressTickets
  |     +-- submissionRepo?.countByStatus("ai_graded") → pendingSubmissions
  |     +-- auditLogRepo.findTop5ByOrderByCreatedAtDesc() → recentActivity
  |     +-- Return DashboardResponse (Admin view)
  |
  +- [Service] getStaffDashboard(staffId)
        +-- ticketRepo?.countByAssignedToAndStatus(staffId, "open") → myOpenTickets
        +-- ticketRepo?.countByAssignedToAndStatus(staffId, "in_progress") → myInProgressTickets
        +-- submissionRepo?.countByStatus("ai_graded") → myPendingGrades
        +-- ticketRepo?.findTop5RecentByAssignedTo(staffId) → recentActivity
        +-- Return DashboardResponse (Staff view)
```

### 2.6 Data Flow — Report Export

```
GET /api/analytics/admin/reports/export?startDate=2026-05-01&endDate=2026-05-31&format=xlsx
  |
  +- [Controller] Validate format in {csv, xlsx}
  |
  +- [Service] exportReport(startDate, endDate, format)
  |     +-- Validate startDate < endDate → 400 BAD_DATE_RANGE nếu sai
  |     +-- Aggregate data:
  |     |     student_users (registrations trong period)
  |     |     test_attempts (exam stats trong period)
  |     |     student_content_progress (completion rates)
  |     +-- Compile → ghi file vào /uploads/exports/report-{timestamp}.{format}
  |     +-- Tạo secure URL download có thời hạn
  |     +-- Log: [WARN] Admin {} exported {} format {}
  |     +-- Return { downloadUrl, expiresAt }
```

### 2.7 Null-safe Dependency Pattern

```java
// AnalyticsService.java — inject các repo từ Nguoi 3 với required = false
@Autowired(required = false)
private StudentContentProgressRepository progressRepository;

@Autowired(required = false)
private TestAttemptRepository testAttemptRepository;

@Autowired(required = false)
private StudentSubmissionRepository submissionRepository;

@Autowired(required = false)
private TicketRepository ticketRepository;  // từ feat-support

// Helper method — null-safe:
private long countCompletedByType(Long studentId, String contentType) {
    if (progressRepository == null) return 0L;
    return progressRepository.countByStudentIdAndContentTypeAndStatus(
        studentId, contentType, "completed");
}

// Dashboard null-safe:
private long safeCountTickets(String status) {
    if (ticketRepository == null) return 0L;
    return ticketRepository.countByStatus(status);
}
```

### 2.8 Streak Calculation Logic

```java
// AnalyticsService.java
private String computeStreakStatus(LocalDate lastActivityDate) {
    if (lastActivityDate == null) return "BROKEN";
    LocalDate today = LocalDate.now();  // timezone: server's LocalDate
    if (lastActivityDate.equals(today)) return "ACTIVE";
    if (lastActivityDate.equals(today.minusDays(1))) return "AT_RISK";
    return "BROKEN";
}

private String computeStreakMessage(String status) {
    return switch (status) {
        case "ACTIVE" -> "Tuyệt vời! Bạn đã học hôm nay. Hãy tiếp tục duy trì!";
        case "AT_RISK" -> "Bạn chưa học hôm nay. Hãy học ngay để duy trì streak!";
        default -> "Chuỗi học tập đã bị gián đoạn. Hãy bắt đầu lại ngay hôm nay!";
    };
}
```

---

## 3. Risk Assessment

| # | Rủi ro | Xác suất | Mức độ | Biện pháp |
|:--|:---|:---:|:---:|:---|
| R1 | `StudentContentProgressRepository` chưa tồn tại (Người 3) → compile error | Cao | Cao | Dùng `@Autowired(required = false)` + null-safe helper methods trả 0/empty |
| R2 | N+1 query khi build skillsRadar (5 skill × N students) | Trung bình | Cao | Dùng JPQL aggregation `AVG(score) GROUP BY skillType`; không query từng record |
| R3 | Division by zero khi tính completionRate (totalCount = 0) | Trung bình | Cao | Check `totalCount == 0 → return 0` trước phép chia |
| R4 | studentId bị leak qua request param thay vì JWT | Thấp | Cao | Controller lấy từ `Authentication.getName()` → resolve email → studentId; unit test kiểm tra |
| R5 | Export báo cáo XLSX timeout cho tập dữ liệu lớn | Trung bình | Trung bình | Cân nhắc async export job; timeout 60s; trả download URL thay vì stream trực tiếp |
| R6 | `DashboardResponse` chứa cả Admin và Staff fields → data leakage | Thấp | Trung bình | Dùng `@JsonInclude(NON_NULL)`: Admin fields null trong Staff view và ngược lại |
| R7 | `TicketRepository` chưa tồn tại khi build dashboard (feat-support chưa merge) | Cao | Trung bình | Inject `required = false`; dashboard trả `openTickets = null` nếu repo chưa có |
| R8 | Streak timezone không đồng nhất (server khác timezone học viên) | Trung bình | Thấp | Dùng `LocalDate.now()` tại server; ghi rõ trong doc là server timezone |

---

## 4. Verification Plan

### 4.1 Unit Tests (Service Layer)

```
AnalyticsServiceTest:
  getStudentProgressAnalytics_studentIdFromJwtNotParam
  getStudentProgressAnalytics_withNullProgressRepo_returnsZeroCompletions
  getStudyStreakDetail_lastActivityToday_returnsActive
  getStudyStreakDetail_lastActivityYesterday_returnsAtRisk
  getStudyStreakDetail_lastActivityOlder_returnsBroken
  getStudyStreakDetail_nullLastActivity_returnsBroken
  getCompletionRate_withZeroTotal_returnsZeroNotException
  getCompletionRate_withValidData_calculatesCorrectly
  getQuizStats_calculatesPerQuestionAccuracyServerSide
  getQuizStats_withZeroAttempts_returnsZeroAccuracy
  getAdminDashboard_withNullTicketRepo_returnsNullOpenTickets
  getStaffDashboard_returnsOnlyStaffSpecificData
```

### 4.2 Integration Tests (Controller Layer)

```
StudentAnalyticsControllerTest:
  GET /analytics/my-progress (Student JWT) → 200, studentId = JWT's student
  GET /analytics/my-progress (Student A JWT) → KHÔNG trả data của Student B
  GET /analytics/streak → 200 streakStatus đúng
  GET /analytics/completion?contentType=kanji (chưa học) → completionRate = 0
  GET /analytics/completion (null type) → tổng hợp tất cả
  GET /analytics/quizzes/{id}/stats (Staff JWT) → 200 với questionAccuracy
  GET /analytics/dashboard (Admin JWT) → Admin view (totalStudents, openTickets...)
  GET /analytics/dashboard (Staff JWT) → Staff view (myOpenTickets...)
  GET /analytics/dashboard (Student JWT) → 403
  GET /analytics/admin/reports?startDate=2026-05-01&endDate=2026-04-01 → 400 BAD_DATE_RANGE
  GET /analytics/admin/reports/export?format=xlsx → 200 downloadUrl
  GET /analytics/admin/reports (Student JWT) → 403
```

### 4.3 Manual Verification Checklist

- [ ] Student đăng nhập → vào trang analytics → thấy streak và completionRate đúng với dữ liệu thực tế
- [ ] Thay đổi `lastActivityDate` trong DB → GET /streak → `streakStatus` tự động thay đổi (không cần restart server)
- [ ] Truy cập `/analytics/my-progress` với token của Student A → không trả data của Student B
- [ ] Staff xem quiz stats → per-question accuracy tính đúng (verify thủ công với sample data)
- [ ] Admin xem dashboard → openTickets, pendingSubmissions phản ánh đúng DB
- [ ] Admin export XLSX → file tải về được, nội dung đúng với khoảng thời gian được chọn
- [ ] Xem network tab → không có `studentId` trong request param; chỉ trong Authorization header
