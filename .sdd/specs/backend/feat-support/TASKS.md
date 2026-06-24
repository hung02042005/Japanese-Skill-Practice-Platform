# TASKS — Support, Notifications & Manual Grading (`feat-support`)
> **UC Coverage:** UC-29, UC-30, UC-31 | **Actor:** Student, Staff, Staff Manager
> **Branch:** `feature/JLPT-XX-support-ticket` | **Scope commit:** `support`
> **STATUS: ✅ HOÀN THÀNH** (verified 2026-06-24) — toàn bộ backend + frontend đã implement và pass test. Xem ghi chú "Vị trí thực tế" ở mỗi phase vì codebase đã refactor sang cấu trúc feature-based (package `com.jlpt.feature.support`, `com.jlpt.feature.notification`) khác với đường dẫn package phẳng (`com.jlpt.controller/dto/service`) mô tả ban đầu trong SPEC/PLAN.

---

## Phase 0: Git Setup
- [x] 0.1 Sync code mới nhất từ `main` — N/A, code đã merge thẳng vào nhánh `refactor/feature-based-structure` hiện tại.
- [x] 0.2 Tạo branch — N/A, không tạo branch riêng `feature/JLPT-XX-support-ticket`.
- [x] 0.3 Xác nhận branch đang đúng — đã chạy `mvn test` để verify working tree không có regression trước khi báo cáo hoàn thành.

---

## Phase 1: DTOs ✅

> **Vị trí thực tế:** `feature/support/dto/*.java` (Ticket*, AssignTicketRequest, ManualGradeRequest, GradeResponse, SubmissionResponse) và `feature/notification/dto/*.java` (SendNotificationRequest, NotificationResponse) — không phải `dto/request|response` phẳng.

- [x] 1.1 `TicketRequest.java` — subject/content/category/priority, default NORMAL nếu null
- [x] 1.2 `TicketReplyRequest.java` — message + attachmentUrl optional
- [x] 1.3 `SendNotificationRequest.java` (trong `feature/notification/dto`)
- [x] 1.4 `ManualGradeRequest.java` — DecimalMin/Max 0–100
- [x] 1.5 `TicketResponse.java` — đủ field theo spec
- [x] 1.6 `TicketDetailResponse.java` — có `List<TicketReplyResponse> replies`
- [x] 1.7 `TicketReplyResponse.java`
- [x] 1.8 `NotificationResponse.java` (trong `feature/notification/dto`) — `unreadCount` trả ở wrapper Map trong controller, không phải field trong DTO
- [x] 1.9 `GradeResponse.java`
- [x] 1.10 *(bổ sung ngoài spec gốc)* `AssignTicketRequest.java`, `SubmissionResponse.java` — phục vụ hàng đợi chấm bài (`GET /staff/submissions`)

---

## Phase 2: Repositories ✅

> **Vị trí thực tế:** `feature/support/repository/{TicketRepository,TicketReplyRepository}.java`, `feature/notification/repository/NotificationRepository.java`.

- [x] 2.1 `TicketRepository.java` — `findByStudentId`, `findByStudentIdAndStatus`, `findAllByFilters` (JPQL, dùng enum thay String cho status/priority)
- [x] 2.2 `TicketReplyRepository.java` — `findByTicketIdOrderByCreatedAtAsc`, `countByTicketId`
- [x] 2.3 `NotificationRepository.java` — `findByStudentId`, `countUnreadByStudentId`, `markAllReadByStudentId` (`@Modifying`), `existsByStudentIdAndRuleKeyAndCreatedAtAfter` (FR-SUPPORT-13 — hiện được dùng bởi `NotificationRuleService` cho milestone tự động; nhánh `manualGrade()` không gọi guard này nhưng không cần vì `status` submission chuyển `ai_graded → graded` ngay sau lần chấm đầu, chặn việc grade lại nên `rule_key = speaking_graded_{id}` tự nhiên chỉ phát sinh 1 lần)

---

## Phase 3: Async Configuration ✅

> **Vị trí thực tế:** `shared/config/AsyncConfig.java` (không phải `config/AsyncConfig.java` ở root package).

- [x] 3.1 `notificationExecutor` `ThreadPoolTaskExecutor` bean
- [x] 3.2 `@EnableAsync` bật trong `AsyncConfig` — `SupportTicketService.broadcastAsync()` dùng `@Async("notificationExecutor")`, verified non-blocking (<500ms) bởi cả unit test (`sendNotification_returnsJobIdImmediately`) và integration test (`broadcastNotification_returns201WithJobId`).

---

## Phase 4: Service Layer ✅

> **Vị trí thực tế:** `feature/support/service/SupportTicketService.java`. Khác biệt nhỏ so với spec: actor được nhận diện bằng `String actorEmail` (từ `Authentication.getName()`) thay vì `Long actorId, String role` — vì JWT của Staff/Admin chỉ mang email + role claim, không mang staffId trong `Authentication` principal.

- [x] 4.1 `SupportTicketService` — `@Service @RequiredArgsConstructor @Slf4j`; `StudentSubmissionRepository` field-injected `@Autowired(required = false)`
- [x] 4.2 `createTicket` — validate qua `@Valid` ở DTO, status=OPEN, priority default NORMAL, log info (không có audit log riêng `TICKET_CREATED` — chỉ log SLF4J)
- [x] 4.3 `getMyTickets` — chỉ trả tickets của `studentId`, verified bởi test `getMyTickets_onlyReturnsOwnTickets`
- [x] 4.4 Tách thành 2 method riêng: `getStudentTicketDetail` (có check ownership → 403) và `getStaffTicketDetail` (không check, vì Staff xem mọi ticket)
- [x] 4.5 Tách thành `addStudentReply` (check ownership + closed) và `addStaffReply` (check closed, tự đổi `OPEN → IN_PROGRESS`) — đúng constraint `CK_replies_sender` (chỉ set 1 sender field)
- [x] 4.6 `closeTicket` — set RESOLVED + resolvedAt, ghi `AdminAuditLog` action=`TICKET_CLOSED`
- [x] 4.7 `assignTicket(ticketId, assignToStaffId, actorEmail, isAdmin)` — `isAdmin` được Controller tính từ `ROLE_ADMIN` authority; nếu không phải admin thì check `staffRole == STAFF_MANAGER` mới cho phép, ghi `AdminAuditLog` action=`TICKET_ASSIGNED`
- [x] 4.8 `getAllTickets` — filter status/category/priority/q qua `findAllByFilters`
- [x] 4.9 `sendNotification` — trả `jobId` ngay, `broadcastAsync()` chạy trên `notificationExecutor`, ghi `AdminAuditLog` action=`BROADCAST_SENT`, verified <500ms bởi test
- [x] 4.10 `getMyNotifications` + `getUnreadCount` riêng (Controller gọi cả 2, gộp vào response Map)
- [x] 4.11 `markNotificationRead` — set is_read/read_at, check ownership → `ForbiddenException`
- [x] 4.12 `markAllNotificationsRead` — bulk `@Modifying` query, trả count
- [x] 4.13 `manualGrade` — check SPEAKING + AI_GRADED (422 nếu sai), set manualScore/finalScore/manualFeedback/gradedBy/gradedAt/status=GRADED, gửi Notification (ACHIEVEMENT, rule_key=`speaking_graded_{id}`), ghi `AdminAuditLog` action=`SUBMISSION_GRADED`, log SLF4J đúng format spec

---

## Phase 5: Controller Layer ✅

> **Vị trí thực tế khác với spec:** không có file `StaffTicketController.java` riêng — toàn bộ endpoint Staff (tickets + notifications + submissions/grade) được gộp vào `feature/admin/controller/AdminDashboardController.java` đã có sẵn (file này cũng phục vụ dashboard UC-38 và student analytics UC-19). Lý do: tránh trùng `@RequestMapping("/api/staff")` với 1 controller khác — class-level mapping đã tồn tại nên các route ticket/notification/grading được thêm vào làm method trong cùng class.

- [x] 5.1 `feature/support/controller/SupportController.java` — `POST/GET /tickets`, `GET /tickets/{id}`, `POST /tickets/{id}/reply` — `@PreAuthorize("hasRole('STUDENT')")`
- [x] 5.2 `feature/notification/controller/NotificationController.java` — `GET /api/notifications` (+ unreadCount), `POST /{id}/read`, `POST /read-all`
- [x] 5.3 Endpoint Staff nằm trong `AdminDashboardController` (`@PreAuthorize("hasAnyRole('STAFF','ADMIN')")` ở class-level):
  - `GET /api/staff/tickets`, `GET /api/staff/tickets/{id}`, `POST /api/staff/tickets/{id}/reply`, `POST /api/staff/tickets/{id}/close`, `POST /api/staff/tickets/{id}/assign`
  - `POST /api/staff/notifications`
  - `GET /api/staff/submissions`, `GET /api/staff/submissions/{id}`, `POST /api/staff/submissions/{id}/grade`
  - ⚠️ `assign` không có `@PreAuthorize` riêng cho `STAFF_MANAGER` (role đó không tồn tại ở tầng Spring Security — JWT chỉ mang `ROLE_STAFF`/`ROLE_ADMIN`); việc phân biệt STAFF_MANAGER vs STAFF thường được check ở `SupportTicketService.assignTicket()` qua field `StaffUser.staffRole`, trả `403` qua `ForbiddenException` — verified bởi `assignTicket_asPlainStaff_returns403`.
- [x] 5.4 Verified qua test: 201 (create ticket, broadcast notification), 200 (reply/close/assign/grade/list), 409 (reply ticket closed), 422 (grade sai loại/trạng thái), 403 (xem ticket người khác, non-STUDENT gọi `/support/*`, plain STAFF gọi `/assign`)

---

## Phase 6: Testing ✅

> Chạy `mvn -o -Dtest="com.jlpt.feature.support.**,com.jlpt.feature.admin.controller.StaffTicketIntegrationTest" test` (2026-06-24): **30/30 tests pass** (19 unit + 5 + 6 integration).

- [x] 6.1 `feature/support/service/SupportTicketServiceTest.java` — 19 test case, bao phủ đủ danh sách trong spec + thêm `assignTicket_asStaffManager_succeeds`, `assignTicket_asAdmin_succeedsWithoutManagerCheck`, `manualGrade_submissionNotFound_throws404`
- [x] 6.2 `feature/support/controller/SupportControllerIntegrationTest.java` — 5 test, bao phủ đủ danh sách spec + `getMyNotifications_returnsUnreadCount`
- [x] 6.3 `feature/admin/controller/StaffTicketIntegrationTest.java` — 6 test, bao phủ đủ danh sách spec (đặt trong package `admin.controller` vì controller thực tế là `AdminDashboardController`) + `assignTicket_asPlainStaff_returns403`

---

## Phase 7: PR & Code Review

> **N/A** — code đã nằm sẵn trên nhánh `refactor/feature-based-structure` hiện tại (không qua flow branch riêng + PR riêng cho `feat-support`). Checklist nội dung đã verify trực tiếp trên code (2026-06-24):
- [x] Không có Entity trả trực tiếp ra API — toàn bộ Controller chỉ trả DTO (`TicketResponse`, `GradeResponse`, `NotificationResponse`...)
- [x] Không có `DELETE FROM` trong migration liên quan tickets/notifications — chỉ soft state qua `status`
- [x] Mỗi `TicketReply` chỉ set 1 trong 2 sender field — `addStudentReply`/`addStaffReply` tách biệt, đúng `CK_replies_sender`
- [x] `sendNotification` async qua `@Async("notificationExecutor")`, verified <500ms
- [x] `manualGrade` ghi `AdminAuditLog` action=`SUBMISSION_GRADED` đầy đủ staffActor + targetId
- [ ] 7.1–7.5 (git flow riêng cho feature) — không áp dụng, giữ nguyên trên nhánh refactor hiện tại theo quyết định của team.
