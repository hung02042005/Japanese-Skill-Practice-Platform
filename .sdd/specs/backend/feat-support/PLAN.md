# PLAN — Support, Notifications & Manual Grading (`feat-support`)
> **Feature ID:** `feat-support` | **UC:** UC-29, UC-30, UC-31 | **Version:** 1.0 | **Updated:** 2026-06-14

---

## 1. User Intent

### 1.1 Vấn đề cần giải quyết

Ba nhóm người dùng có nhu cầu giao tiếp và chấm điểm khác nhau:

1. **Học viên (Student)** — cần kênh hỗ trợ chính thức để báo lỗi kỹ thuật hoặc thắc mắc học tập, và cần được thông báo về các cột mốc học tập của mình.
2. **Nhân viên (Staff)** — cần dashboard quản lý ticket, phân loại ưu tiên, giao việc cho đồng nghiệp, và chấm điểm thủ công bài nói mà AI chưa đủ chính xác.
3. **Staff Manager** — cần thêm quyền assign ticket cho từng Staff cụ thể.

### 1.2 Người dùng và kỳ vọng

| Actor | Ky vong chinh |
|:---|:---|
| **Student** | Tao ticket de dang, theo doi trang thai, nhan thong bao milestone tu dong |
| **Staff** | Xem tat ca ticket voi filter, reply, dong ticket, cham bai noi, gui thong bao dien rong |
| **Staff Manager** | Tat ca quyen Staff + phan cong ticket cho dong nghiep |

### 1.3 Ràng buộc nghiệp vụ quan trọng

- Student chi xem va reply ticket **cua chinh minh** — khong duoc xem ticket nguoi khac (403).
- Reply ticket da `closed` thi tra HTTP 409 `TICKET_CLOSED`.
- `final_score = manual_score` neu Staff da cham, nguoc lai = `ai_overall_score`.
- Chi cham bai co `submission_type = 'speaking'` va `status = 'ai_graded'` — sai dieu kien tra HTTP 422.
- Gui thong bao batch phai **async** — tra `jobId` ngay, khong block.
- Khong gui cung `rule_key` notification cho 1 student qua 1 lan trong 24 gio.
- DB constraint `CK_replies_sender`: chi 1 trong 2 (`student_sender_id`, `staff_sender_id`) duoc set.

---

## 2. Architectural Blueprint

### 2.1 Layer Stack

```
[React Frontend - Student]              [React Frontend - Staff]
      |                                       |
      | POST /api/support/tickets             | GET /api/staff/tickets
      | GET  /api/notifications               | POST /api/staff/tickets/{id}/reply
      v                                       v
[SupportController]                  [StaffTicketController]
(Student routes) [MOI]               (Staff routes) [MOI]
      |                                       |
      +-------------------+-------------------+
                          v
             [SupportTicketService]        <- com.jlpt.service  [MOI]
                          |
       +------------------+------------------+------------------+
       v                  v                  v                  v
[TicketRepo]     [TicketReplyRepo]  [NotificationRepo]  [StudentSubmissionRepo]
[MOI]            [MOI]              [MOI]               (READ + update grade)
```

### 2.2 File Structure

```
controller/
  student/
    SupportController.java               [MOI] — student ticket & notification
  admin/
    StaffTicketController.java           [MOI] — staff ticket + grading

service/
  SupportTicketService.java              [MOI]

repository/
  TicketRepository.java                  [MOI]
  TicketReplyRepository.java             [MOI]
  NotificationRepository.java            [MOI]

dto/request/
  TicketRequest.java                     [MOI]
  TicketReplyRequest.java                [MOI]
  SendNotificationRequest.java           [MOI]
  ManualGradeRequest.java                [MOI]

dto/response/
  TicketResponse.java                    [MOI]
  TicketDetailResponse.java              [MOI]
  TicketReplyResponse.java               [MOI]
  NotificationResponse.java              [MOI]
  GradeResponse.java                     [MOI]
```

### 2.3 API Routes

| Method | Path | Actor | Mo ta |
|:---|:---|:---|:---|
| POST | `/api/support/tickets` | Student | Tao ticket moi |
| GET | `/api/support/tickets` | Student | Xem tickets cua minh |
| GET | `/api/support/tickets/{id}` | Student | Chi tiet ticket + replies |
| POST | `/api/support/tickets/{id}/reply` | Student | Tra loi ticket |
| GET | `/api/notifications` | Student | Xem danh sach notifications |
| POST | `/api/notifications/{id}/read` | Student | Mark 1 notification da doc |
| POST | `/api/notifications/read-all` | Student | Mark tat ca da doc |
| GET | `/api/staff/tickets` | Staff/Admin | Xem tat ca tickets co filter |
| POST | `/api/staff/tickets/{id}/reply` | Staff/Admin | Staff reply ticket |
| POST | `/api/staff/tickets/{id}/close` | Staff/Admin | Dong ticket |
| POST | `/api/staff/tickets/{id}/assign` | StaffManager/Admin | Assign ticket |
| POST | `/api/staff/notifications` | Staff/Admin | Gui thong bao broadcast |
| POST | `/api/staff/submissions/{id}/grade` | Staff/Admin | Cham diem bai noi |

### 2.4 Data Flow — Ticket Reply Ownership Check

```
POST /api/support/tickets/{ticketId}/reply
  |
  +- [Controller] Lay studentId tu JWT
  |
  +- [Service] replyToTicket(ticketId, studentId, "STUDENT", message)
  |     +-- Load Ticket -> 404 neu khong co
  |     +-- Neu actor = STUDENT: check ticket.studentId == actorId -> 403 neu khong phai chu
  |     +-- Check ticket.status != CLOSED -> 409 neu da dong
  |     +-- @Transactional:
  |     |     +-- Tao TicketReply (studentSenderId = actorId)
  |     |     +-- ticket.setLastReplyAt(now())
  |     |     +-- Neu ticket.status == OPEN && actor == STAFF: ticket.setStatus(IN_PROGRESS)
  |     +-- Return TicketReplyResponse
```

### 2.5 Data Flow — Manual Grade

```
POST /api/staff/submissions/{submissionId}/grade
  |
  +- [Controller] Lay staffId tu JWT, @Valid ManualGradeRequest
  |
  +- [Service] manualGrade(submissionId, staffId, req)
  |     +-- Load StudentSubmission -> 404 neu khong co
  |     +-- Check submissionType == 'speaking' -> 422 neu khong phai
  |     +-- Check status == 'ai_graded' -> 422 neu chua qua AI
  |     +-- Validate manualScore trong [0.00, 100.00] -> 400 neu ngoai range
  |     +-- @Transactional:
  |     |     +-- submission.setManualScore(req.manualScore)
  |     |     +-- submission.setFinalScore(req.manualScore)   <- ghi de AI score
  |     |     +-- submission.setManualFeedback(req.manualFeedback)
  |     |     +-- submission.setGradedBy(staffUser)
  |     |     +-- submission.setGradedAt(now())
  |     |     +-- submission.setStatus(GRADED)
  |     |     +-- notificationRepo.save(Notification cho student)
  |     +-- Return GradeResponse
```

### 2.6 Async Notification Broadcast

```java
// SupportTicketService.java
@Async("notificationExecutor")
public CompletableFuture<Void> broadcastAsync(
        List<Long> studentIds, SendNotificationRequest req, Long staffId) {
    for (Long studentId : studentIds) {
        Notification n = Notification.builder()
            .studentId(studentId)
            .title(req.getTitle())
            .content(req.getContent())
            .channel(req.getChannel())
            .staffCreatorId(staffId)
            .build();
        notificationRepository.save(n);
    }
    return CompletableFuture.completedFuture(null);
}

// Trong sendNotification():
String jobId = "job_notification_" + System.currentTimeMillis();
List<Long> targets = resolveTargetStudentIds(req.getTargetJlptLevel());
broadcastAsync(targets, req, staffId);  // non-blocking
return jobId;
```

### 2.7 Service Method Signatures

| Method | Signature | Logic tom tat |
|:---|:---|:---|
| `createTicket` | `TicketResponse createTicket(Long studentId, TicketRequest req)` | Validate, tao Ticket status=OPEN |
| `getMyTickets` | `Page<TicketResponse> getMyTickets(Long studentId, String status, int page, int size)` | Chi tra tickets thuoc studentId |
| `getTicketDetail` | `TicketDetailResponse getTicketDetail(Long ticketId, Long actorId, String role)` | Kiem tra ownership, load replies |
| `replyToTicket` | `TicketReplyResponse replyToTicket(Long ticketId, Long actorId, String role, String message, String attachmentUrl)` | Check ownership, status, tao reply, update lastReplyAt |
| `closeTicket` | `TicketResponse closeTicket(Long ticketId, Long staffId)` | Chi STAFF/ADMIN, set RESOLVED |
| `assignTicket` | `TicketResponse assignTicket(Long ticketId, Long staffId, Long assignToStaffId)` | Chi STAFF_MANAGER/ADMIN |
| `getAllTickets` | `Page<TicketResponse> getAllTickets(String status, String category, String priority, String q, int page, int size)` | Staff xem tat ca voi filter |
| `sendNotification` | `String sendNotification(Long staffId, SendNotificationRequest req)` | Batch async, tra jobId ngay |
| `getMyNotifications` | `Page<NotificationResponse> getMyNotifications(Long studentId, int page, int size)` | Tra kem unreadCount |
| `markNotificationRead` | `void markNotificationRead(Long notificationId, Long studentId)` | Set is_read=true, validate ownership |
| `markAllNotificationsRead` | `int markAllNotificationsRead(Long studentId)` | Bulk update, tra so da mark |
| `manualGrade` | `GradeResponse manualGrade(Long submissionId, Long staffId, ManualGradeRequest req)` | Validate, set manual/final score, notify student |

---

## 3. Risk Assessment

| # | Rui ro | Xac suat | Muc do | Bien phap |
|:--|:---|:---:|:---:|:---|
| R1 | `StudentSubmissionRepository` chua ton tai (Nguoi 3) -> compile error | Cao | Cao | Tao interface stub voi cac method can thiet; dung `@Autowired(required = false)` trong service |
| R2 | DB Constraint `CK_replies_sender` bi vi pham khi set ca 2 sender | Trung binh | Cao | Service set duy nhat 1 sender field; unit test kiem tra ca 2 case |
| R3 | Broadcast notification den 10,000 student lam timeout request | Cao | Cao | Xu ly async bat buoc; tra jobId trong <200ms; @Async voi ThreadPoolTaskExecutor rieng |
| R4 | Duplicate milestone notification gui nhieu lan | Trung binh | Trung binh | Check rule_key + studentId trong window 24h truoc khi insert |
| R5 | Staff cham sai bai nop (handwriting thay vi speaking) | Thap | Trung binh | Validate submissionType = 'speaking' o service layer -> HTTP 422 |
| R6 | Student goi GET /staff/tickets nhan 403 nhung Frontend khong xu ly | Thap | Thap | 403 duoc handle boi Axios interceptor chung; Frontend khong can xu ly rieng |
| R7 | Ticket reply trung lap do double-click | Thap | Thap | Frontend disable nut sau click (UI state); Backend khong block duplicate (stateless) |

---

## 4. Verification Plan

### 4.1 Unit Tests (Service Layer)

```
SupportTicketServiceTest:
  createTicket_withValidData_returnsTicketWithOpenStatus
  replyToTicket_asStudent_ownsTicket_returnsReply
  replyToTicket_asStudent_notOwnTicket_throws403
  replyToTicket_toClosedTicket_throws409
  replyToTicket_asStaff_whenOpen_changesStatusToInProgress
  closeTicket_asStaff_setsResolvedStatus
  manualGrade_withSpeakingAiGraded_setsManualAndFinalScore
  manualGrade_withHandwriting_throws422
  manualGrade_withPendingStatus_throws422
  manualGrade_withScoreOver100_throwsValidationException
  manualGrade_sendsNotificationToStudent
  sendNotification_returnsJobIdImmediately
  markNotificationRead_setsIsReadAndReadAt
  markAllNotificationsRead_returnsMarkedCount
  getMyTickets_onlyReturnsOwnTickets
  getMyNotifications_returnsUnreadCount
```

### 4.2 Integration Tests (Controller Layer)

```
SupportControllerTest (Student):
  POST /support/tickets                      -> 201 Created
  GET /support/tickets                       -> 200, chi thay tickets cua minh
  GET /support/tickets/{otherId}             -> 403 FORBIDDEN
  POST /support/tickets/{closedId}/reply     -> 409 TICKET_CLOSED
  GET /notifications                         -> 200 voi unreadCount
  POST /notifications/{id}/read              -> 200, is_read = true
  Staff JWT goi /support/tickets             -> 403

StaffTicketControllerTest (Staff):
  GET /staff/tickets                         -> 200 paginated tat ca tickets
  GET /staff/tickets?status=open             -> filter dung
  POST /staff/tickets/{id}/reply             -> 200, ticket IN_PROGRESS neu OPEN
  POST /staff/tickets/{id}/close             -> 200, status = resolved
  POST /staff/tickets/{id}/assign (Staff thuong) -> 403
  POST /staff/submissions/{id}/grade (speaking, ai_graded) -> 200
  POST /staff/submissions/{id}/grade (handwriting)         -> 422
  POST /staff/submissions/{id}/grade (pending)             -> 422
  POST /staff/submissions/{id}/grade (score = -1)          -> 400
  POST /staff/notifications                  -> 201 voi jobId ngay lap tuc (<500ms)
  Student JWT goi /staff/tickets             -> 403
```

### 4.3 Manual Verification Checklist

- [ ] Hoc vien tao ticket -> thay ticket trong danh sach cua minh -> KHONG thay ticket nguoi khac
- [ ] Staff reply -> ticket status chuyen IN_PROGRESS -> hoc vien thay reply
- [ ] Staff dong ticket -> hoc vien co reply -> nhan loi "Ticket da dong"
- [ ] Staff cham bai noi voi score 85.50 -> final_score = 85.50 (ghi de AI score)
- [ ] Hoc vien nhan notification sau khi bai noi duoc cham
- [ ] Staff gui broadcast den N3 -> tra jobId ngay < 1s -> hoc vien N3 nhan notification
- [ ] Kiem tra bang `notifications` co record dung voi `rule_key`, `is_read = 0`
