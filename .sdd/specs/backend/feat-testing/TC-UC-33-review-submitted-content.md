# TC-UC-33 — Test Cases: Kiểm Duyệt Nội Dung Chờ Duyệt (Review Submitted Content)

> **Feature:** `feat-content-review` | **UC:** UC-33 | **Version:** 1.0
> **Nguồn AC:** UC-33 § 8 | **Nguồn FR:** UC-33 § 3 (FR-33-01..22), SPEC.md § 3.1
> **Actor:** StaffManager | **Cập nhật:** 2026-06-12

---

## 1. UNIT TESTS — ContentReviewService (JUnit 5 + Mockito)

> **File:** `ContentReviewServiceTest.java` | **Tag:** `@Tag("unit")`

---

### TC-U-33-01 — Approve nội dung pending thành công

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-01 |
| **Tham chiếu** | AC-33-06, FR-33-09..11 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Long managerId = 10L;
Question content = aQuestion().withId(105L).withCreatedBy(7L).withStatus("pending_review").build();
when(resolver.repositoryFor(ContentType.QUESTION)).thenReturn(questionRepository);
when(questionRepository.findById(105L)).thenReturn(Optional.of(content));
when(questionRepository.updateStatusIfPending(105L, "published", managerId, any())).thenReturn(1); // 1 dòng
```

**Steps:**
1. Gọi `service.approve(new ReviewActionRequest(QUESTION, 105L, "APPROVE", "OK"), managerId)`

**Expected:**
- Trả `ReviewResultResponse` với `status = "published"`, `approvedAt` != null
- `updateStatusIfPending(...)` được gọi với `status="published"`, `approved_by=10`, `published_at≈NOW()`
- `reviewAuditService.log("approve_content", 10, "questions", 105, ...)` được gọi đúng 1 lần
- Không ném exception

---

### TC-U-33-02 — Cấm tự phê duyệt (Four-Eyes) — Approve nội dung do chính mình tạo

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-02 |
| **Tham chiếu** | AC-33-11, FR-33-17/18, Rule 8 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 (Correctness) |

**Setup:**
```java
Long managerId = 7L;
Question content = aQuestion().withId(105L).withCreatedBy(7L).withStatus("pending_review").build();
when(questionRepository.findById(105L)).thenReturn(Optional.of(content));
```

**Steps:**
1. Gọi `service.approve(new ReviewActionRequest(QUESTION, 105L, "APPROVE", null), 7L)`

**Expected:**
- Ném `SelfReviewNotAllowedException` (HTTP 403 / `SELF_REVIEW_DENIED`)
- `updateStatusIfPending(...)` **KHÔNG được gọi**
- `reviewAuditService.log(...)` **KHÔNG được gọi**
- Status không thay đổi

---

### TC-U-33-03 — Xử lý đồng thời: item đã bị reviewer khác xử lý → 409

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-03 |
| **Tham chiếu** | AC-33-12, FR-33-10/19, Rule 9 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 (Concurrency) |

**Setup:**
```java
Question content = aQuestion().withId(105L).withCreatedBy(7L).withStatus("pending_review").build();
when(questionRepository.findById(105L)).thenReturn(Optional.of(content));
when(questionRepository.updateStatusIfPending(eq(105L), any(), any(), any())).thenReturn(0); // 0 dòng — đã bị đổi
```

**Steps:**
1. Gọi `service.approve(new ReviewActionRequest(QUESTION, 105L, "APPROVE", "OK"), 10L)`

**Expected:**
- Ném `ConcurrentReviewException` (HTTP 409 / `CONCURRENT_REVIEW`)
- `reviewAuditService.log(...)` **KHÔNG được gọi** (không ghi đè)

---

### TC-U-33-04 — Reject thiếu feedback → FEEDBACK_REQUIRED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-04 |
| **Tham chiếu** | AC-33-07, FR-33-14, Rule 5 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Question content = aQuestion().withId(105L).withCreatedBy(7L).withStatus("pending_review").build();
when(questionRepository.findById(105L)).thenReturn(Optional.of(content));
```

**Steps:**
1. Gọi `service.reject(new ReviewActionRequest(QUESTION, 105L, "REJECT", "   "), 10L)`

**Expected:**
- Ném `FeedbackRequiredException` (HTTP 400 / `FEEDBACK_REQUIRED`)
- `updateStatusIfPending(...)` KHÔNG được gọi; status không đổi

---

### TC-U-33-05 — Reject thành công, lưu feedback vào audit log

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-05 |
| **Tham chiếu** | AC-33-08, FR-33-12/16/20 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Question content = aQuestion().withId(105L).withCreatedBy(7L).withStatus("pending_review").build();
when(questionRepository.findById(105L)).thenReturn(Optional.of(content));
when(questionRepository.updateStatusIfPending(105L, "rejected", null, null)).thenReturn(1);
```

**Steps:**
1. Gọi `service.reject(new ReviewActionRequest(QUESTION, 105L, "REJECT", "Sai đáp án D"), 10L)`

**Expected:**
- `status = "rejected"`
- `reviewAuditService.log("reject_content", 10, "questions", 105, "Sai đáp án D")` được gọi
- `description` trong audit = "Sai đáp án D"

---

### TC-U-33-06 — Request Changes về draft thành công

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-06 |
| **Tham chiếu** | AC-33-09, FR-33-13, Rule 7 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
Question content = aQuestion().withId(105L).withCreatedBy(7L).withStatus("pending_review").build();
when(questionRepository.findById(105L)).thenReturn(Optional.of(content));
when(questionRepository.updateStatusIfPending(105L, "draft", null, null)).thenReturn(1);
```

**Steps:**
1. Gọi `service.requestChanges(new RequestChangesRequest(QUESTION, 105L, "draft", "Bổ sung giải thích"), 10L)`

**Expected:**
- `status = "draft"`; audit `action = "request_changes_content"`, `description = "Bổ sung giải thích"`

---

### TC-U-33-07 — Request Changes với targetStatus không hợp lệ → VALIDATION_FAILED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-07 |
| **Tham chiếu** | FR-33-15 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Steps:**
1. Gọi `service.requestChanges(new RequestChangesRequest(QUESTION, 105L, "published", "x"), 10L)`

**Expected:**
- Ném `ValidationException` (HTTP 400 / `VALIDATION_FAILED`) — `targetStatus` chỉ chấp nhận {draft, rejected}
- Không đổi status

---

### TC-U-33-08 — Content không tồn tại → CONTENT_NOT_FOUND

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-08 |
| **Tham chiếu** | AC-33-05, FR-33-07 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
when(questionRepository.findById(999L)).thenReturn(Optional.empty());
```

**Steps:**
1. Gọi `service.getContentDetail(QUESTION, 999L)`

**Expected:**
- Ném `ContentNotFoundException` (HTTP 404 / `CONTENT_NOT_FOUND`)

---

### TC-U-33-09 — Audit log lỗi → rollback đổi status (atomic transaction)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-33-09 |
| **Tham chiếu** | AC-33-14, FR-33-22 |
| **Loại** | Unit — Service (Transaction) |
| **Ưu tiên** | P0 |

**Setup:**
```java
when(questionRepository.findById(105L)).thenReturn(Optional.of(aQuestion().withCreatedBy(7L).withStatus("pending_review").build()));
when(questionRepository.updateStatusIfPending(any(), any(), any(), any())).thenReturn(1);
doThrow(new RuntimeException("DB down")).when(reviewAuditService).log(any(), any(), any(), any(), any());
```

**Steps:**
1. Gọi `service.approve(req, 10L)` trong ngữ cảnh transaction

**Expected:**
- Exception lan ra → transaction rollback (`@Transactional`); đổi status không được commit
- Verify: phương thức ném exception, không nuốt lỗi

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `ContentReviewRepositoryIT.java` | **Tag:** `@Tag("integration")`
> **Stack:** Testcontainers (SQL Server) + Flyway

---

### TC-I-33-01 — Review Queue chỉ trả pending_review

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-33-01 |
| **Tham chiếu** | AC-33-01, FR-33-02 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P0 |

**Steps:**
1. Seed `questions`: 3 bản ghi `pending_review`, 5 bản ghi `published`, 2 `draft`
2. Gọi `questionRepository.findByStatus("pending_review", PageRequest.of(0, 20))`

**Expected:**
- Trả đúng 3 bản ghi; tất cả có `status = "pending_review"`; không lẫn `published`/`draft`

---

### TC-I-33-02 — Guarded update chỉ thành công khi đang pending (chống đồng thời)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-33-02 |
| **Tham chiếu** | FR-33-19, Rule 9 |
| **Loại** | Integration — DB Concurrency |
| **Ưu tiên** | P0 (Concurrency) |

**Steps:**
1. Seed question ID 105 `status = "pending_review"`
2. Luồng A: `updateStatusIfPending(105, "published", 10, NOW)` → trả 1
3. Luồng B (sau đó): `updateStatusIfPending(105, "published", 11, NOW)` → trả 0

**Expected:**
- Lần gọi đầu: 1 dòng ảnh hưởng, `status` chuyển `published`, `approved_by = 10`
- Lần gọi thứ hai: **0 dòng** ảnh hưởng (WHERE status='pending_review' không khớp) → Service sẽ ném 409

---

### TC-I-33-03 — Audit log persist đúng cột

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-33-03 |
| **Tham chiếu** | AC-33-13, FR-33-20 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P1 |

**Steps:**
1. Gọi `reviewAuditService.log("approve_content", 10L, "questions", 105L, "OK")`
2. Query `admin_audit_logs` mới nhất

**Expected:**
- `staff_actor_id = 10`, `action = "approve_content"`, `target_table = "questions"`, `target_id = 105`, `description = "OK"`, `created_at ≈ NOW()`

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `ManagerReviewControllerTest.java` | **Tag:** `@Tag("api")`
> **Stack:** `@WebMvcTest(ManagerReviewController.class)` + Spring Security test

---

### TC-A-33-01 — GET /api/manager/review-queue — StaffManager → 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-01 |
| **Tham chiếu** | AC-33-01 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:** `GET /api/manager/review-queue?type=question&page=0&size=20` + `@WithMockUser(authorities="STAFF_MANAGER")`

**Mock:** `service.getReviewQueue(...)` trả 1 item

**Expected:**
```
HTTP 200
{ "status": 200, "message": "...", "data": { "content": [ { "contentId": 105, "contentType": "question", ... } ], "totalElements": 1 } }
```

---

### TC-A-33-02 — GET /api/manager/review-queue — role staff thường → 403

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-02 |
| **Tham chiếu** | AC-33-03, FR-33-01, Rule 1 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 (Security) |

**Request:** `GET /api/manager/review-queue` + `@WithMockUser(authorities="STAFF")`

**Expected:**
```
HTTP 403
{ "status": 403, "message": "Tài khoản không có thẩm quyền kiểm duyệt", ... }  // FORBIDDEN
```
- `service.getReviewQueue(...)` KHÔNG được gọi

---

### TC-A-33-03 — GET /api/manager/review-queue — không JWT → 401

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-03 |
| **Tham chiếu** | FR-33-01 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 |

**Request:** `GET /api/manager/review-queue` (không header Authorization)

**Expected:** `HTTP 401` / `UNAUTHORIZED`

---

### TC-A-33-04 — POST /api/manager/reviews — Approve → 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-04 |
| **Tham chiếu** | AC-33-06 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**
```json
POST /api/manager/reviews
{ "contentType": "question", "contentId": 105, "action": "APPROVE", "feedback": "OK" }
```

**Mock:** `service.approve(...)` → `ReviewResultResponse(105, "published", "2026-06-12T09:44:00Z")`

**Expected:**
```
HTTP 200
{ "status": 200, "message": "Phê duyệt nội dung thành công", "data": { "contentId": 105, "status": "published", "approvedAt": "..." } }
```

---

### TC-A-33-05 — POST /api/manager/reviews — tự duyệt → 403 SELF_REVIEW_DENIED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-05 |
| **Tham chiếu** | AC-33-11, FR-33-17 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `service.approve(...)` ném `SelfReviewNotAllowedException`

**Expected:**
```
HTTP 403
{ "status": 403, "message": "Nguyên tắc chéo: Không thể tự phê duyệt nội dung của chính mình", ... }  // SELF_REVIEW_DENIED
```

---

### TC-A-33-06 — POST /api/manager/reviews — đồng thời → 409 CONCURRENT_REVIEW

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-06 |
| **Tham chiếu** | AC-33-12, FR-33-19 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `service.approve(...)` ném `ConcurrentReviewException`

**Expected:**
```
HTTP 409
{ "status": 409, "message": "Nội dung này đã được xử lý bởi một StaffManager khác", ... }  // CONCURRENT_REVIEW
```

---

### TC-A-33-07 — POST /api/manager/reviews — Reject thiếu feedback → 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-07 |
| **Tham chiếu** | AC-33-07, FR-33-14 |
| **Loại** | API — Validation |
| **Ưu tiên** | P0 |

**Request:**
```json
{ "contentType": "question", "contentId": 105, "action": "REJECT", "feedback": "" }
```

**Expected:**
```
HTTP 400
{ "status": 400, "message": "Phải nhập lý do khi từ chối hoặc yêu cầu chỉnh sửa", ... }  // FEEDBACK_REQUIRED
```

---

### TC-A-33-08 — POST /api/manager/reviews/request-changes → 200 (status=draft)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-08 |
| **Tham chiếu** | AC-33-09 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "contentType": "question", "contentId": 105, "targetStatus": "draft", "feedback": "Bổ sung ví dụ" }
```

**Mock:** `service.requestChanges(...)` → `status="draft"`

**Expected:**
```
HTTP 200
{ "status": 200, "message": "Yêu cầu chỉnh sửa nội dung thành công", "data": { "contentId": 105, "status": "draft" } }
```

---

### TC-A-33-09 — GET /api/manager/contents/{id} — không tồn tại → 404

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-09 |
| **Tham chiếu** | AC-33-05, FR-33-07 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:** `GET /api/manager/contents/999?contentType=question`

**Mock:** `service.getContentDetail(...)` ném `ContentNotFoundException`

**Expected:** `HTTP 404` / `CONTENT_NOT_FOUND`

---

### TC-A-33-10 — Response KHÔNG lộ Entity / cột nội bộ

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-33-10 |
| **Tham chiếu** | NFR-33-05, ADR-005 |
| **Loại** | API — Invariant |
| **Ưu tiên** | P0 |

**Steps:**
1. Gọi `GET /api/manager/contents/105?contentType=question`
2. Parse đệ quy toàn bộ JSON

**Expected:**
- Response là DTO; có `contentId`, `contentType`, `titleOrText`, `status`, `submittedBy`
- KHÔNG có key Hibernate nội bộ (`hibernateLazyInitializer`, `handler`) hay trường nhạy cảm không khai báo trong DTO

---

## 4. TEST DATA SUMMARY

| Fixture | contentType | contentId | created_by | status |
|:---|:---|:---|:---|:---|
| `pendingByOther` | question | 105 | 7 | pending_review |
| `pendingByManager` | question | 106 | 10 | pending_review |
| `alreadyPublished` | question | 107 | 7 | published |
| `pendingGrammar` | grammar | 201 | 8 | pending_review |
| `missing` | question | 999 | — | (không tồn tại) |

> Reviewer chuẩn: `managerId = 10` (`staff_role = staff_manager`).

---

## 5. COVERAGE CHECKLIST (Rule nghiệp vụ UC-33)

| Rule | Mô tả | Test Case | Covered? |
|:---|:---|:---|:---|
| Rule 1 | Chỉ StaffManager truy cập Review Queue | TC-A-33-02, TC-A-33-03 | ✅ |
| Rule 2 | Queue chỉ hiển thị `pending_review` | TC-I-33-01, TC-A-33-01 | ✅ |
| Rule 3/4 | Approve → published + approved_by + published_at | TC-U-33-01, TC-A-33-04 | ✅ |
| Rule 5 | Reject/Request Changes bắt buộc feedback | TC-U-33-04, TC-A-33-07 | ✅ |
| Rule 6 | Reject → rejected | TC-U-33-05 | ✅ |
| Rule 7 | Request Changes → draft/rejected | TC-U-33-06, TC-U-33-07, TC-A-33-08 | ✅ |
| Rule 8 | Cấm tự duyệt (Four-Eyes) | TC-U-33-02, TC-A-33-05 | ✅ |
| Rule 9 | Đồng thời → 409 CONCURRENT_REVIEW | TC-U-33-03, TC-I-33-02, TC-A-33-06 | ✅ |
| Rule 10 | Ghi admin_audit_logs | TC-U-33-05, TC-I-33-03, TC-U-33-09 | ✅ |
| ADR-005 | Không lộ Entity ra API | TC-A-33-10 | ✅ |
