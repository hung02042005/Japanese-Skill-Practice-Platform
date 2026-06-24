# TASKS — Learning Analytics & Reports (`feat-learning-analytics`)
> **UC Coverage:** UC-19, UC-32, UC-38 | **Actor:** Student, Staff, Admin
> **Branch:** `feature/JLPT-XX-learning-analytics` | **Scope commit:** `analytics`

---

## Phase 0: Git Setup
- [ ] 0.1 Sync code mới nhất từ `main`:
  ```bash
  git fetch origin
  git rebase origin/main
  ```
- [ ] 0.2 Tạo branch đúng format GIT_RULES Section 6:
  ```bash
  git checkout -b feature/JLPT-XX-learning-analytics
  ```
  > Thay `XX` bằng ticket ID thực tế trên board.
- [ ] 0.3 Xác nhận branch đang đúng: `git status` — working tree phải clean trước khi code.

---

## Phase 1: DTOs

- [x] 1.1 Tạo `dto/response/AnalyticsResponse.java`
  - `studentId`, `currentStreak`, `longestStreak`, `lastActivityDate`, `streakStatus` (`ACTIVE`|`AT_RISK`|`BROKEN`)
  - `Map<String, Integer> completions` — key: `lesson|kanji|vocabulary|grammar|kana`
  - `Map<String, Double> completionRates` — key: content type, value: 0–100
  - `Map<String, Double> skillsRadar` — key: `grammar|vocabulary|reading|listening|speaking`
- [x] 1.2 Tạo `dto/response/StreakDetailResponse.java`
  - `currentStreak`, `longestStreak`, `lastActivityDate`, `streakStatus`, `streakStatusMessage`
- [x] 1.3 Tạo `dto/response/CompletionRateResponse.java`
  - `contentType`, `totalCount`, `completedCount`, `completionRate`
- [x] 1.4 Tạo `dto/response/QuizStatsResponse.java`
  - `assessmentId`, `title`, `totalAttempts`, `averageScore`, `maxScore`, `passRate`
  - `List<QuestionAccuracyItem> questionAccuracy`
  - Inner class `QuestionAccuracyItem`: `questionId`, `questionText`, `correctCount`, `incorrectCount`, `accuracyPercent`
- [x] 1.5 Tạo `dto/response/ExamAttemptResponse.java`
  - `attemptId`, `attemptType`, `assessmentTitle`, `totalScore`, `maxScore`, `isPassed`, `startedAt`, `submittedAt`
- [x] 1.6 Tạo `dto/response/DashboardResponse.java`
  - Admin fields: `totalStudents`, `activeStudents`, `suspendedStudents`, `newStudentsThisMonth`, `openTickets`, `inProgressTickets`, `resolvedTicketsThisMonth`, `pendingSubmissions`, `gradedSubmissionsThisMonth`
  - Staff fields: `myOpenTickets`, `myInProgressTickets`, `myPendingGrades`
  - Common: `generatedAt`, `List<RecentActivityItem> recentActivity`
  - Inner class `RecentActivityItem`: `actorName`, `actorType`, `action`, `timestamp`
- [x] 1.7 Tạo `dto/response/AdminReportResponse.java`
  - `period` (startDate, endDate), `newRegistrations`, `totalExamAttempts`, `avgExamScore`
  - `List<LevelCompletionRate> courseCompletionRates`
  - Inner class `LevelCompletionRate`: `jlptLevel`, `completedStudentsCount`, `totalStudentsCount`, `completionRate`

**Commit sau phase này:**
```
feat(analytics): add DTOs for analytics, dashboard, quiz stats, and report
```

---

## Phase 2: Service Layer

- [x] 2.1 Tạo `service/AnalyticsService.java`
  - Annotations: `@Service`, `@RequiredArgsConstructor`, `@Slf4j`
  - Inject các repo phụ thuộc với `@Autowired(required = false)`:
    - `StudentContentProgressRepository` (Người 3)
    - `TestAttemptRepository` (Người 3)
    - `StudentSubmissionRepository` (Người 3)
    - `TicketRepository` (feat-support)
- [x] 2.2 Implement `getStudentProgressAnalytics(Long studentId)`
  - Load `StudentUser` → `404` nếu không có
  - Tính `streakStatus` server-side:
    - `ACTIVE`: `lastActivityDate == LocalDate.now()`
    - `AT_RISK`: `lastActivityDate == LocalDate.now().minusDays(1)`
    - `BROKEN`: còn lại
  - Query completions per `contentType` (fallback empty nếu repo null)
  - Tính `completionRate = completedCount / totalCount × 100` — **xử lý chia cho 0 trả về 0**
  - Build `skillsRadar` từ `test_attempts` (grammar/vocab/reading/listening) và `student_submissions` (speaking avg `final_score`)
  - **studentId phải lấy từ JWT — không từ request param** (FR-ANALYTICS-06)
- [x] 2.3 Implement `getStudyStreakDetail(Long studentId)`
  - Tính `streakStatus` và `streakStatusMessage` tương ứng
  - Múi giờ: `LocalDate.now()` tại server
- [x] 2.4 Implement `getCompletionRate(Long studentId, String contentType)`
  - `contentType == null` → tổng hợp tất cả types
  - Xử lý chia cho 0 → trả `completionRate = 0`
- [x] 2.5 Implement `getQuizStats(Long assessmentId)`
  - Query `testAttempts` theo `parentId + parentType='assessment'`
  - Tính `totalAttempts`, `avgScore`, `passRate`
  - Query `attemptAnswers` để tính per-question accuracy:
    `correctCount / totalAttempts × 100`
  - **Tính toán hoàn toàn server-side** — không tin số liệu từ client (CLAUDE.md anti-pattern)
- [x] 2.6 Implement `getStudentExamHistory(Long studentId, int page, int size)`
  - Query `testAttempts` theo `studentId + type in ('exam','quiz')`, order `startedAt DESC`
- [x] 2.7 Implement `getAdminDashboard()` — chỉ ADMIN
  - Aggregate từ nhiều repo (dùng fallback null-safe nếu repo chưa tồn tại)
  - `recentActivity` lấy 5 records gần nhất từ `admin_audit_logs`
  - Performance: tận dụng JPQL aggregation, tránh N+1
- [x] 2.8 Implement `getStaffDashboard(Long staffId)` — chỉ STAFF
  - `myOpenTickets`, `myInProgressTickets` (tickets được assign cho staffId)
  - `myPendingGrades` (submissions `status = ai_graded` chưa được chấm)
  - `recentActivity` lấy 5 tickets gần nhất của staff này
- [x] 2.9 Implement `getAdminReport(LocalDate startDate, LocalDate endDate)`
  - Validate `startDate < endDate` → `400 BAD_DATE_RANGE` nếu sai
  - Aggregate `student_users` (registrations), `test_attempts` (exam stats), `student_content_progress` (completion rates)
- [x] 2.10 Implement `exportReport(LocalDate startDate, LocalDate endDate, String format)`
  - Hỗ trợ `format = csv | xlsx`
  - Compile dữ liệu → ghi file → lưu vào `/uploads/exports/`
  - Trả URL download có thời hạn
  - Log: `[WARN] Admin {} exported {} format {}`

**Commit sau phase này:**
```
feat(analytics): implement AnalyticsService with streak, completion, quiz stats, and dashboard
```

---

## Phase 3: Controller Layer

- [x] 3.1 Tạo `controller/student/StudentAnalyticsController.java` (implemented as `AnalyticsController` tại `/api/analytics`)
  - `@RequestMapping("/api/analytics")`, security: `@PreAuthorize("hasRole('STUDENT')")`
  - `GET /my-progress` — `studentId` **lấy từ JWT**, không từ param
  - `GET /streak`
  - `GET /completion` — optional `contentType` query param
- [x] 3.2 Thêm Staff/Admin endpoints vào cùng controller hoặc `StaffAnalyticsController`:
  - `GET /api/analytics/quizzes/{assessmentId}/stats` — `hasAnyRole('STAFF','ADMIN')`
  - `GET /api/analytics/students/{studentId}/exam-history` — `hasAnyRole('STAFF','ADMIN')`
  - `GET /api/analytics/dashboard` — `hasAnyRole('STAFF','ADMIN')`; logic phân nhánh Admin/Staff trong service
  - `GET /api/analytics/admin/reports` — `hasRole('ADMIN')`
  - `GET /api/analytics/admin/reports/export` — `hasRole('ADMIN')`
- [x] 3.3 Verify: Student gọi `GET /analytics/students/{otherId}/exam-history` → `403` (không được xem của người khác)

**Commit sau phase này:**
```
feat(analytics): add StudentAnalyticsController and analytics endpoints for staff/admin
```

---

## Phase 4: Testing

- [x] 4.1 Unit test `AnalyticsServiceTest`: (34 test, all pass)
  - `getStudentProgressAnalytics_studentIdFromJwtNotParam`
  - `getStudyStreakDetail_lastActivityYesterday_returnsAtRisk`
  - `getStudyStreakDetail_lastActivityToday_returnsActive`
  - `getCompletionRate_withZeroTotal_returnsZeroNotException`
  - `getQuizStats_calculatesPerQuestionAccuracyServerSide`
  - `getStudentProgressAnalytics_withNullProgressRepo_returnsZeroCompletions`
- [x] 4.2 Integration test `StudentAnalyticsControllerTest`: (implemented as `AnalyticsControllerTest`, fixed @WebMvcTest context-bleed bug, all pass)
  - `GET /analytics/my-progress` → 200, studentId khớp với JWT (không phải param)
  - `Student JWT gọi /analytics/admin/reports` → 403
  - `GET /analytics/completion?contentType=kanji` với student chưa học → `completionRate = 0`
- [x] 4.3 Integration test Dashboard:
  - `GET /analytics/dashboard` với Admin JWT → trả Admin view
  - `GET /analytics/dashboard` với Staff JWT → trả Staff view
  - `GET /analytics/dashboard` với Student JWT → 403

**Commit sau phase này:**
```
test(analytics): add unit and integration tests for AnalyticsService and controllers
```

---

## Phase 5: PR & Code Review

- [ ] 5.1 Tự review diff trước khi tạo PR:
  ```bash
  git diff origin/main...HEAD
  ```
  Kiểm tra checklist:
  - [ ] Không có file `.env`, `.vscode/`, `.idea/` bị commit nhầm
  - [ ] Không return Entity trực tiếp — chỉ DTO ra API
  - [ ] `studentId` analytics lấy từ JWT, không từ request param
  - [ ] Chia cho 0 được xử lý, không throw exception
  - [ ] Mọi tính toán thống kê thực hiện server-side
- [ ] 5.2 Sync code mới nhất trước khi tạo PR:
  ```bash
  git fetch origin
  git rebase origin/main
  ```
- [ ] 5.3 Tạo PR trên GitHub:
  - **Title:** `feat(analytics): learning analytics, dashboard, and admin report export UC-19/32/38`
  - **Description:** Mô tả WHAT + WHY, link ticket
  - Yêu cầu ít nhất **1 reviewer** approve
- [ ] 5.4 Merge bằng **Squash and Merge**
- [ ] 5.5 Xóa branch sau khi merge thành công
