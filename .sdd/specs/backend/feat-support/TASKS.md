# TASKS — Support, Notifications & Manual Grading (`feat-support`)
> **UC Coverage:** UC-29, UC-30, UC-31 | **Actor:** Student, Staff, Staff Manager
> **Branch:** `feature/JLPT-XX-support-ticket` | **Scope commit:** `support`

---

## Phase 0: Git Setup
- [ ] 0.1 Sync code mới nhất từ `main`:
  ```bash
  git fetch origin
  git rebase origin/main
  ```
- [ ] 0.2 Tạo branch đúng format GIT_RULES Section 6:
  ```bash
  git checkout -b feature/JLPT-XX-support-ticket
  ```
  > Thay `XX` bằng ticket ID thực tế trên board.
- [ ] 0.3 Xác nhận branch đang đúng: `git status` — working tree phải clean trước khi code.

---

## Phase 1: DTOs

- [ ] 1.1 Tạo `dto/request/TicketRequest.java`
  - `@NotBlank @Size(max=255) String subject`
  - `@NotBlank String content`
  - `@Size(max=50) String category`
  - `String priority` — default `NORMAL` nếu null
- [ ] 1.2 Tạo `dto/request/TicketReplyRequest.java`
  - `@NotBlank String message`
  - `@Size(max=500) String attachmentUrl` — optional
- [ ] 1.3 Tạo `dto/request/SendNotificationRequest.java`
  - `@NotBlank @Size(max=255) String title`
  - `@NotBlank String content`
  - `@NotBlank String notificationType` — `news|warning|promotion|system|achievement|reminder`
  - `@NotBlank String channel` — `in_app|email|both`
  - `String targetJlptLevel` — null = broadcast tất cả
  - `LocalDateTime scheduledAt` — null = gửi ngay
- [ ] 1.4 Tạo `dto/request/ManualGradeRequest.java`
  - `@NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal manualScore`
  - `@Size(max=2000) String manualFeedback`
- [ ] 1.5 Tạo `dto/response/TicketResponse.java`
  - `ticketId`, `studentId`, `studentName`, `studentEmail`, `subject`, `content`
  - `category`, `priority`, `status`, `assignedToStaffId`, `assignedToStaffName`
  - `replyCount`, `lastReplyAt`, `createdAt`, `resolvedAt`
- [ ] 1.6 Tạo `dto/response/TicketDetailResponse.java` *(extends TicketResponse)*
  - Thêm: `List<TicketReplyResponse> replies`
- [ ] 1.7 Tạo `dto/response/TicketReplyResponse.java`
  - `replyId`, `senderName`, `senderRole` (`STUDENT`|`STAFF`), `message`, `attachmentUrl`, `createdAt`
- [ ] 1.8 Tạo `dto/response/NotificationResponse.java`
  - `notificationId`, `title`, `content`, `notificationType`, `channel`, `isRead`, `readAt`, `createdAt`
  - List response có thêm `Integer unreadCount` ở wrapper
- [ ] 1.9 Tạo `dto/response/GradeResponse.java`
  - `submissionId`, `studentId`, `studentName`, `submissionType`, `status`
  - `aiOverallScore`, `manualScore`, `finalScore`, `manualFeedback`, `gradedByStaffName`, `gradedAt`, `submittedAt`

**Commit sau phase này:**
```
feat(support): add DTOs for ticket, notification, and manual grading
```

---

## Phase 2: Repositories

- [ ] 2.1 Tạo `repository/TicketRepository.java`
  - `findByStudentId(Long studentId, Pageable pageable)`
  - `findByStudentIdAndStatus(Long studentId, String status, Pageable pageable)`
  - `findAllByFilters(String status, String category, String priority, String q, Pageable pageable)` — `@Query` JPQL
- [ ] 2.2 Tạo `repository/TicketReplyRepository.java`
  - `findByTicketIdOrderByCreatedAtAsc(Long ticketId)`
  - `countByTicketId(Long ticketId)`
- [ ] 2.3 Tạo `repository/NotificationRepository.java`
  - `findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable)`
  - `countByStudentIdAndIsReadFalse(Long studentId)`
  - `updateAllUnreadByStudentId(Long studentId, LocalDateTime now)` — `@Modifying @Query`
  - `existsByStudentIdAndRuleKeyAndCreatedAtAfter(Long studentId, String ruleKey, LocalDateTime after)` — duplicate check 24h

**Commit sau phase này:**
```
feat(support): add TicketRepository, TicketReplyRepository, NotificationRepository
```

---

## Phase 3: Async Configuration

- [ ] 3.1 Tạo hoặc cập nhật `config/AsyncConfig.java`
  - Định nghĩa `ThreadPoolTaskExecutor` bean tên `notificationExecutor`
  - `corePoolSize=5`, `maxPoolSize=20`, `queueCapacity=500`
  - Prefix thread name: `notification-`
- [ ] 3.2 Đảm bảo `@EnableAsync` được bật trên Application hoặc Config class

**Commit sau phase này:**
```
chore(config): add async thread pool executor for notification broadcast
```

---

## Phase 4: Service Layer

- [ ] 4.1 Tạo `service/SupportTicketService.java`
  - Annotations: `@Service`, `@RequiredArgsConstructor`, `@Slf4j`, `@Transactional`
  - Inject `StudentSubmissionRepository` với `@Autowired(required = false)` (phụ thuộc Người 3)
- [ ] 4.2 Implement `createTicket(Long studentId, TicketRequest req)`
  - Validate subject/content không rỗng
  - Tạo Ticket với `status = OPEN`, `priority = NORMAL` (default nếu null)
  - Ghi audit log `TICKET_CREATED`
- [ ] 4.3 Implement `getMyTickets(Long studentId, String status, int page, int size)`
  - Chỉ trả tickets thuộc `studentId` — **không trả ticket của người khác**
- [ ] 4.4 Implement `getTicketDetail(Long ticketId, Long actorId, String role)`
  - Load ticket → `404` nếu không có
  - Nếu `role == STUDENT`: check `ticket.studentId == actorId` → `403` nếu không phải chủ
  - Load kèm replies
- [ ] 4.5 Implement `replyToTicket(Long ticketId, Long actorId, String role, String message, String attachmentUrl)`
  - Check `ticket.status != CLOSED` → `409 TICKET_CLOSED` nếu đã đóng
  - Nếu `role == STUDENT`: check ownership → `403`
  - Tạo `TicketReply` — set **duy nhất 1** trong 2 sender field (tuân thủ DB constraint `CK_replies_sender`)
  - Update `ticket.lastReplyAt = now()`
  - Nếu Staff reply và ticket đang `OPEN` → set `IN_PROGRESS`
- [ ] 4.6 Implement `closeTicket(Long ticketId, Long staffId)` — chỉ STAFF/ADMIN
  - Set `status = RESOLVED`, `resolvedAt = now()`
  - Ghi audit log `TICKET_CLOSED`
- [ ] 4.7 Implement `assignTicket(Long ticketId, Long staffId, Long assignToStaffId)` — chỉ STAFF_MANAGER/ADMIN
  - Set `assignedTo = assignToStaffId`
  - Ghi audit log `TICKET_ASSIGNED`
- [ ] 4.8 Implement `getAllTickets(String status, String category, String priority, String q, int page, int size)`
  - Staff xem tất cả tickets với filter
- [ ] 4.9 Implement `sendNotification(Long staffId, SendNotificationRequest req)` — **async bắt buộc**
  - Resolve danh sách student target theo `targetJlptLevel` (null = broadcast tất cả active students)
  - Gọi `broadcastAsync()` với `@Async("notificationExecutor")` — non-blocking
  - Trả `jobId` ngay lập tức (trong vòng 200ms) — **không được block**
  - Log: `[INFO] Staff {} triggered notification broadcast jobId {} targets {}`
- [ ] 4.10 Implement `getMyNotifications(Long studentId, int page, int size)`
  - Trả kèm `unreadCount` trong response
- [ ] 4.11 Implement `markNotificationRead(Long notificationId, Long studentId)`
  - Set `is_read = true`, `read_at = now()`
  - Validate ownership → `403` nếu không phải của student này
- [ ] 4.12 Implement `markAllNotificationsRead(Long studentId)`
  - Bulk update tất cả unread của student, trả số lượng đã mark
- [ ] 4.13 Implement `manualGrade(Long submissionId, Long staffId, ManualGradeRequest req)`
  - Load `StudentSubmission` → `404` nếu không có (inject required=false)
  - Check `submissionType == 'speaking'` → `422` nếu không phải
  - Check `status == 'ai_graded'` → `422` nếu chưa qua AI
  - Trong `@Transactional`: set `manualScore`, `finalScore = manualScore`, `manualFeedback`, `gradedBy`, `gradedAt = now()`, `status = GRADED`
  - Gửi in-app notification cho student: `type = achievement`, `rule_key = speaking_graded_{submissionId}`
  - Ghi audit log `SUBMISSION_GRADED`
  - Log SLF4J: `[INFO] Staff {} graded submission {} score {}`

**Commit sau phase này:**
```
feat(support): implement SupportTicketService with ticket, notification, and manual grading logic
```

---

## Phase 5: Controller Layer

- [ ] 5.1 Tạo `controller/student/SupportController.java`
  - `@RequestMapping("/api/support")`, security: `@PreAuthorize("hasRole('STUDENT')")`
  - `POST /tickets` — tạo ticket mới (201)
  - `GET /tickets` — xem tickets của mình (filter `status`)
  - `GET /tickets/{ticketId}` — chi tiết ticket + replies
  - `POST /tickets/{ticketId}/reply` — student reply
- [ ] 5.2 Tạo `controller/student/NotificationController.java` (hoặc thêm vào SupportController)
  - `GET /api/notifications` — danh sách + unreadCount
  - `POST /api/notifications/{notificationId}/read` — mark 1 đã đọc
  - `POST /api/notifications/read-all` — mark tất cả
- [ ] 5.3 Tạo `controller/admin/StaffTicketController.java`
  - `@RequestMapping("/api/staff")`, security: `@PreAuthorize("hasAnyRole('STAFF','ADMIN')")`
  - `GET /tickets` — tất cả tickets với filter
  - `POST /tickets/{ticketId}/reply` — staff reply
  - `POST /tickets/{ticketId}/close` — đóng ticket
  - `POST /tickets/{ticketId}/assign` — security riêng: `@PreAuthorize("hasAnyRole('STAFF_MANAGER','ADMIN')")`
  - `POST /notifications` — broadcast notification (201, trả jobId)
  - `POST /submissions/{submissionId}/grade` — chấm điểm bài nói
- [ ] 5.4 Kiểm tra tất cả endpoints trả đúng HTTP status code (201 cho create, 200 cho update/query, 409/422/403/404 cho lỗi)

**Commit sau phase này:**
```
feat(support): add SupportController (student) and StaffTicketController with all endpoints
```

---

## Phase 6: Testing

- [ ] 6.1 Unit test `SupportTicketServiceTest`:
  - `createTicket_withValidData_returnsTicketWithOpenStatus`
  - `replyToTicket_asStudent_notOwnTicket_throws403`
  - `replyToTicket_toClosedTicket_throws409`
  - `replyToTicket_asStaff_whenOpen_changesStatusToInProgress`
  - `manualGrade_withSpeakingAiGraded_setsManualAndFinalScore`
  - `manualGrade_withHandwriting_throws422`
  - `manualGrade_withPendingStatus_throws422`
  - `manualGrade_withScoreOver100_throwsValidationException`
  - `manualGrade_sendsNotificationToStudent`
  - `sendNotification_returnsJobIdImmediately` — assert thời gian < 500ms
- [ ] 6.2 Integration test `SupportControllerTest`:
  - `POST /support/tickets` → 201 Created
  - `GET /support/tickets/{otherId}` → 403 FORBIDDEN (xem ticket người khác)
  - `POST /support/tickets/{closedId}/reply` → 409 TICKET_CLOSED
  - `Staff JWT gọi POST /support/tickets` → 403
- [ ] 6.3 Integration test `StaffTicketControllerTest`:
  - `POST /staff/submissions/{id}/grade` (speaking, ai_graded) → 200
  - `POST /staff/submissions/{id}/grade` (handwriting) → 422
  - `POST /staff/submissions/{id}/grade` (score = -1) → 400
  - `POST /staff/notifications` → 201 với jobId (phản hồi < 500ms)
  - `Student JWT gọi /staff/tickets` → 403

**Commit sau phase này:**
```
test(support): add unit and integration tests for SupportTicketService and controllers
```

---

## Phase 7: PR & Code Review

- [ ] 7.1 Tự review diff trước khi tạo PR:
  ```bash
  git diff origin/main...HEAD
  ```
  Kiểm tra checklist:
  - [ ] Không có file `.env`, `.vscode/`, `.idea/` bị commit nhầm
  - [ ] Không return Entity trực tiếp — chỉ DTO ra API
  - [ ] Soft delete đúng cách — không có `DELETE FROM`
  - [ ] Mỗi `TicketReply` chỉ set **1** trong 2 sender field (không vi phạm `CK_replies_sender`)
  - [ ] `sendNotification` phải **async** — không block request thread
  - [ ] `manualGrade` ghi audit log đầy đủ
- [ ] 7.2 Sync code mới nhất trước khi tạo PR:
  ```bash
  git fetch origin
  git rebase origin/main
  ```
- [ ] 7.3 Tạo PR trên GitHub:
  - **Title:** `feat(support): support ticket system, notification broadcast, manual grading UC-29/30/31`
  - **Description:** Mô tả WHAT + WHY, link ticket
  - Yêu cầu ít nhất **1 reviewer** approve
- [ ] 7.4 Merge bằng **Squash and Merge**
- [ ] 7.5 Xóa branch sau khi merge thành công
