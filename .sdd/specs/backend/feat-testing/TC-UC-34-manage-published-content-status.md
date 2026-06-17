# TC-UC-34 — Test Cases: Quản Lý Trạng Thái Nội Dung Đã Xuất Bản (Manage Published Content Status)

> **Feature:** `feat-content-review` | **UC:** UC-34 | **Version:** 1.0
> **Nguồn AC:** UC-34 § 8 (AC-34-01..15) | **Nguồn FR:** UC-34 § 3 (FR-34-01..24), SPEC.md § 3.2
> **Actor:** StaffManager | **Cập nhật:** 2026-06-12

---

## 1. UNIT TESTS — PublishedContentService (JUnit 5 + Mockito)

> **File:** `PublishedContentServiceTest.java` | **Tag:** `@Tag("unit")`

---

### TC-U-34-01 — Archive nội dung published thành công (soft delete)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-01 |
| **Tham chiếu** | AC-34-06, FR-34-10/13, Rule 2 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Long managerId = 10L;
Assessment content = anAssessment().withId(24L).withStatus("published").build();
when(resolver.repositoryFor(ContentType.ASSESSMENT)).thenReturn(assessmentRepository);
when(assessmentRepository.findById(24L)).thenReturn(Optional.of(content));
when(referenceChecker.findBlockingReferences(ASSESSMENT, 24L)).thenReturn(List.of()); // không bị chặn
when(assessmentRepository.updateStatusFrom(24L, "published", "archived")).thenReturn(1);
```

**Steps:**
1. Gọi `service.updateStatus(24L, new UpdateContentStatusRequest(ASSESSMENT, "archived", "Đề cũ 2024"), managerId)`

**Expected:**
- Trả `ContentStatusResultResponse` với `status = "archived"`
- `updateStatusFrom(24L, "published", "archived")` được gọi đúng 1 lần (chỉ đổi `status`, KHÔNG `DELETE FROM`)
- `auditService.log("archive_content", 10, "assessments", 24, "Đề cũ 2024")` được gọi đúng 1 lần
- Không ném exception

---

### TC-U-34-02 — Chặn unpublish/archive câu hỏi đang trong đề thi published → RESOURCE_IN_USE

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-02 |
| **Tham chiếu** | AC-34-08, FR-34-14/16, Rule 5 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 (Data Integrity) |

**Setup:**
```java
Question content = aQuestion().withId(1L).withStatus("published").build();
when(questionRepository.findById(1L)).thenReturn(Optional.of(content));
when(referenceChecker.findBlockingReferences(QUESTION, 1L)).thenReturn(List.of(
    new ResourceReferenceResponse("assessment", 2L, "Đề thi thử N5 - Tháng 6")));
```

**Steps:**
1. Gọi `service.updateStatus(1L, new UpdateContentStatusRequest(QUESTION, "archived", "Lý do hợp lệ"), 10L)`

**Expected:**
- Ném `ResourceInUseException` (HTTP 409 / `RESOURCE_IN_USE`)
- Exception chứa `references = [{assessment, 2, "Đề thi thử N5 - Tháng 6"}]`
- `updateStatusFrom(...)` **KHÔNG được gọi**; `auditService.log(...)` **KHÔNG được gọi**

---

### TC-U-34-03 — Unpublish thiếu reason → REASON_REQUIRED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-03 |
| **Tham chiếu** | AC-34-07, FR-34-11, Rule 4 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Question content = aQuestion().withId(9L).withStatus("published").build();
when(questionRepository.findById(9L)).thenReturn(Optional.of(content));
```

**Steps:**
1. Gọi `service.updateStatus(9L, new UpdateContentStatusRequest(QUESTION, "unpublished", "   "), 10L)`

**Expected:**
- Ném `ReasonRequiredException` (HTTP 400 / `REASON_REQUIRED`)
- `referenceChecker` / `updateStatusFrom(...)` KHÔNG được gọi; status không đổi

---

### TC-U-34-04 — Unpublish hợp lệ → status = draft

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-04 |
| **Tham chiếu** | AC-34-09, FR-34-10, Rule 2/3 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Question content = aQuestion().withId(9L).withStatus("published").build();
when(questionRepository.findById(9L)).thenReturn(Optional.of(content));
when(referenceChecker.findBlockingReferences(QUESTION, 9L)).thenReturn(List.of());
when(questionRepository.updateStatusFrom(9L, "published", "draft")).thenReturn(1);
```

**Steps:**
1. Gọi `service.updateStatus(9L, new UpdateContentStatusRequest(QUESTION, "unpublished", "Sai đáp án"), 10L)`

**Expected:**
- `status = "draft"`; audit `action = "unpublish_content"`, `description = "Sai đáp án"`
- `updateStatusFrom(9L, "published", "draft")` được gọi

---

### TC-U-34-05 — Delete là soft delete (status = deleted, bản ghi được giữ)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-05 |
| **Tham chiếu** | AC-34-10, FR-34-10, Rule 2 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Lesson content = aLesson().withId(5L).withStatus("published").build();
when(lessonRepository.findById(5L)).thenReturn(Optional.of(content));
when(referenceChecker.findBlockingReferences(LESSON, 5L)).thenReturn(List.of());
when(lessonRepository.updateStatusFrom(5L, "published", "deleted")).thenReturn(1);
```

**Steps:**
1. Gọi `service.updateStatus(5L, new UpdateContentStatusRequest(LESSON, "deleted", "Trùng nội dung"), 10L)`

**Expected:**
- `status = "deleted"`; `auditService.log("delete_content", 10, "lessons", 5, "Trùng nội dung")` được gọi
- Verify: KHÔNG gọi `repository.delete(...)` / `deleteById(...)` — chỉ guarded update

---

### TC-U-34-06 — Restore nội dung archived → published

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-06 |
| **Tham chiếu** | AC-34-11, FR-34-18, Rule 8 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Assessment content = anAssessment().withId(24L).withStatus("archived").build();
when(assessmentRepository.findById(24L)).thenReturn(Optional.of(content));
when(assessmentRepository.updateStatusFrom(24L, "archived", "published")).thenReturn(1);
```

**Steps:**
1. Gọi `service.restore(24L, new RestoreContentRequest(ASSESSMENT), 10L)`

**Expected:**
- `status = "published"`; audit `action = "restore_content"`
- `updateStatusFrom(24L, "archived", "published")` được gọi

---

### TC-U-34-07 — Cấm restore nội dung deleted → RESTORE_NOT_ALLOWED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-07 |
| **Tham chiếu** | AC-34-12, FR-34-19, Rule 8 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Question content = aQuestion().withId(9L).withStatus("deleted").build();
when(questionRepository.findById(9L)).thenReturn(Optional.of(content));
```

**Steps:**
1. Gọi `service.restore(9L, new RestoreContentRequest(QUESTION), 10L)`

**Expected:**
- Ném `RestoreNotAllowedException` (HTTP 409 / `RESTORE_NOT_ALLOWED`)
- `updateStatusFrom(...)` / `auditService.log(...)` KHÔNG được gọi; status không đổi

---

### TC-U-34-08 — Restore item không phải archived/deleted → INVALID_STATE_TRANSITION

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-08 |
| **Tham chiếu** | FR-34-20 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
Question content = aQuestion().withId(9L).withStatus("published").build();
when(questionRepository.findById(9L)).thenReturn(Optional.of(content));
```

**Steps:**
1. Gọi `service.restore(9L, new RestoreContentRequest(QUESTION), 10L)`

**Expected:**
- Ném `InvalidStateTransitionException` (HTTP 409 / `INVALID_STATE_TRANSITION`); status không đổi

---

### TC-U-34-09 — status target không hợp lệ → VALIDATION_FAILED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-09 |
| **Tham chiếu** | FR-34-12 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Steps:**
1. Gọi `service.updateStatus(9L, new UpdateContentStatusRequest(QUESTION, "published", "x"), 10L)`

**Expected:**
- Ném `ValidationException` (HTTP 400 / `VALIDATION_FAILED`) — `status` chỉ chấp nhận {unpublished, archived, deleted}
- Không đổi status

---

### TC-U-34-10 — Content không tồn tại → CONTENT_NOT_FOUND

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-10 |
| **Tham chiếu** | AC-34-05, FR-34-08 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
when(questionRepository.findById(999L)).thenReturn(Optional.empty());
```

**Steps:**
1. Gọi `service.getDetail(QUESTION, 999L)`

**Expected:**
- Ném `ContentNotFoundException` (HTTP 404 / `CONTENT_NOT_FOUND`)

---

### TC-U-34-11 — Đồng thời: item đã rời 'published' → INVALID_STATE_TRANSITION (guarded update 0 dòng)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-11 |
| **Tham chiếu** | FR-34-13/17 |
| **Loại** | Unit — Service (Concurrency) |
| **Ưu tiên** | P0 |

**Setup:**
```java
Question content = aQuestion().withId(9L).withStatus("published").build();
when(questionRepository.findById(9L)).thenReturn(Optional.of(content));
when(referenceChecker.findBlockingReferences(QUESTION, 9L)).thenReturn(List.of());
when(questionRepository.updateStatusFrom(9L, "published", "archived")).thenReturn(0); // 0 dòng — đã bị đổi
```

**Steps:**
1. Gọi `service.updateStatus(9L, new UpdateContentStatusRequest(QUESTION, "archived", "Lý do hợp lệ"), 10L)`

**Expected:**
- Ném `InvalidStateTransitionException` (HTTP 409); `auditService.log(...)` KHÔNG được gọi

---

### TC-U-34-12 — Audit log lỗi → rollback đổi status (atomic transaction)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-34-12 |
| **Tham chiếu** | AC-34-15, FR-34-24 |
| **Loại** | Unit — Service (Transaction) |
| **Ưu tiên** | P0 |

**Setup:**
```java
when(questionRepository.findById(9L)).thenReturn(Optional.of(aQuestion().withStatus("published").build()));
when(referenceChecker.findBlockingReferences(QUESTION, 9L)).thenReturn(List.of());
when(questionRepository.updateStatusFrom(any(), any(), any())).thenReturn(1);
doThrow(new RuntimeException("DB down")).when(auditService).log(any(), any(), any(), any(), any());
```

**Steps:**
1. Gọi `service.updateStatus(9L, req, 10L)` trong ngữ cảnh transaction

**Expected:**
- Exception lan ra → transaction rollback (`@Transactional`); đổi status không được commit
- Verify: phương thức ném exception, không nuốt lỗi

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `PublishedContentRepositoryIT.java` | **Tag:** `@Tag("integration")`
> **Stack:** Testcontainers (SQL Server) + Flyway

---

### TC-I-34-01 — Reference check: câu hỏi trong assessment published bị phát hiện

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-34-01 |
| **Tham chiếu** | AC-34-08, FR-34-14 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P0 (Data Integrity) |

**Steps:**
1. Seed question ID 1 `status='published'`; assessment ID 2 `status='published'`; `question_assignments(parent_type='assessment', parent_id=2, question_id=1)`
2. Gọi `questionAssignmentRepository.findPublishedAssessmentRefs(1L)`

**Expected:**
- Trả đúng 1 dòng: `(assessment_id=2, title="Đề thi thử N5 - Tháng 6")`

---

### TC-I-34-02 — Reference check: câu hỏi chỉ trong assessment archived → KHÔNG bị chặn

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-34-02 |
| **Tham chiếu** | FR-34-14 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P1 |

**Steps:**
1. Seed question ID 9 `published`; assessment ID 8 `status='archived'`; assignment (assessment, 8, 9)
2. Gọi `questionAssignmentRepository.findPublishedAssessmentRefs(9L)`

**Expected:**
- Trả **0 dòng** (chỉ đếm assessment `published`) → không chặn unpublish/archive

---

### TC-I-34-03 — Guarded update chỉ thành công khi đúng trạng thái nguồn

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-34-03 |
| **Tham chiếu** | FR-34-13/17 |
| **Loại** | Integration — DB Concurrency |
| **Ưu tiên** | P0 |

**Steps:**
1. Seed question ID 9 `status='published'`
2. Lần 1: `updateStatusFrom(9, "published", "archived")` → trả 1
3. Lần 2: `updateStatusFrom(9, "published", "deleted")` → trả 0

**Expected:**
- Lần 1: 1 dòng, `status='archived'`
- Lần 2: **0 dòng** (WHERE status='published' không khớp) → Service ném 409

---

### TC-I-34-04 — Soft delete giữ nguyên bản ghi vật lý

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-34-04 |
| **Tham chiếu** | AC-34-10, NFR-34-01, ADR-004 |
| **Loại** | Integration — DB |
| **Ưu tiên** | P0 |

**Steps:**
1. Seed lesson ID 5 `published`
2. `updateStatusFrom(5, "published", "deleted")`
3. `SELECT COUNT(*) FROM lessons WHERE lesson_id = 5`

**Expected:**
- `status='deleted'` nhưng `COUNT(*) = 1` — bản ghi VẪN tồn tại (không hard delete)

---

### TC-I-34-05 — Audit log persist đúng cột

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-34-05 |
| **Tham chiếu** | AC-34-14, FR-34-23 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P1 |

**Steps:**
1. Gọi `auditService.log("archive_content", 10L, "assessments", 24L, "Đề cũ 2024")`
2. Query `admin_audit_logs` mới nhất

**Expected:**
- `staff_actor_id=10`, `action="archive_content"`, `target_table="assessments"`, `target_id=24`, `description="Đề cũ 2024"`, `created_at ≈ NOW()`

---

### TC-I-34-06 — Nội dung đã ẩn không lọt Student-facing query

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-34-06 |
| **Tham chiếu** | AC-34-13, FR-34-21/22 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P0 |

**Steps:**
1. Seed question ID 9 `status='archived'`
2. Gọi truy vấn Student-facing (vd `questionRepository.findPublishedForPractice(...)` lọc `status='published'`)

**Expected:**
- Question 9 KHÔNG xuất hiện trong kết quả

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `ManagerContentStatusControllerTest.java` | **Tag:** `@Tag("api")`
> **Stack:** `@WebMvcTest(ManagerContentStatusController.class)` + Spring Security test

---

### TC-A-34-01 — GET /api/manager/published-contents — StaffManager → 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-01 |
| **Tham chiếu** | AC-34-01 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:** `GET /api/manager/published-contents?type=question&page=0&size=20` + `@WithMockUser(authorities="STAFF_MANAGER")`

**Mock:** `service.listPublished(...)` trả 1 item

**Expected:**
```
HTTP 200
{ "status": 200, "message": "...", "data": { "content": [ { "contentId": 105, "contentType": "question", "status": "published", ... } ], "totalElements": 1 } }
```

---

### TC-A-34-02 — GET /api/manager/published-contents — role staff thường → 403

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-02 |
| **Tham chiếu** | AC-34-03, FR-34-01, Rule 1 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 (Security) |

**Request:** `GET /api/manager/published-contents` + `@WithMockUser(authorities="STAFF")`

**Expected:**
```
HTTP 403  { "status": 403, "message": "Tài khoản không có thẩm quyền quản lý nội dung xuất bản", ... }  // FORBIDDEN
```
- `service.listPublished(...)` KHÔNG được gọi

---

### TC-A-34-03 — GET /api/manager/published-contents — không JWT → 401

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-03 |
| **Tham chiếu** | FR-34-01 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 |

**Request:** `GET /api/manager/published-contents` (không header Authorization)

**Expected:** `HTTP 401` / `UNAUTHORIZED`

---

### TC-A-34-04 — PUT /contents/{id}/status — Archive → 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-04 |
| **Tham chiếu** | AC-34-06 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**
```json
PUT /api/manager/contents/24/status
{ "contentType": "assessment", "status": "archived", "reason": "Đề cũ 2024 không còn phù hợp." }
```

**Mock:** `service.updateStatus(...)` → `ContentStatusResultResponse(24, "assessment", "archived")`

**Expected:**
```
HTTP 200
{ "status": 200, "message": "Cập nhật trạng thái xuất bản thành công", "data": { "contentId": 24, "contentType": "assessment", "status": "archived" } }
```

---

### TC-A-34-05 — PUT /contents/{id}/status — câu hỏi đang thi → 409 RESOURCE_IN_USE + references

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-05 |
| **Tham chiếu** | AC-34-08, FR-34-14/16 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 (Data Integrity) |

**Request:**
```json
PUT /api/manager/contents/1/status
{ "contentType": "question", "status": "archived", "reason": "Lý do hợp lệ" }
```

**Mock:** `service.updateStatus(...)` ném `ResourceInUseException` với `references=[{assessment,2,"Đề thi thử N5 - Tháng 6"}]`

**Expected:**
```
HTTP 409
{ "status": 409, "message": "Câu hỏi đang được sử dụng trong đề thi đang hoạt động, không thể thu hồi",
  "data": { "errorCode": "RESOURCE_IN_USE", "references": [ { "referenceType": "assessment", "referenceId": 2, "referenceTitle": "Đề thi thử N5 - Tháng 6" } ] } }
```

---

### TC-A-34-06 — PUT /contents/{id}/status — thiếu reason → 400 REASON_REQUIRED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-06 |
| **Tham chiếu** | AC-34-07, FR-34-11 |
| **Loại** | API — Validation |
| **Ưu tiên** | P0 |

**Request:**
```json
{ "contentType": "question", "status": "deleted", "reason": "" }
```

**Expected:**
```
HTTP 400  { "status": 400, "message": "Phải nhập lý do khi thu hồi, lưu trữ hoặc xóa nội dung", ... }  // REASON_REQUIRED
```

---

### TC-A-34-07 — PUT /contents/{id}/status — status không hợp lệ → 400 VALIDATION_FAILED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-07 |
| **Tham chiếu** | FR-34-12 |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "contentType": "question", "status": "published", "reason": "abc def ghi" }
```

**Expected:** `HTTP 400` / `VALIDATION_FAILED` (status ngoài {unpublished, archived, deleted})

---

### TC-A-34-08 — POST /contents/{id}/restore — archived → 200 (status=published)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-08 |
| **Tham chiếu** | AC-34-11, FR-34-18 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**
```json
POST /api/manager/contents/24/restore
{ "contentType": "assessment" }
```

**Mock:** `service.restore(...)` → `status="published"`

**Expected:**
```
HTTP 200  { "status": 200, "message": "Khôi phục nội dung thành công", "data": { "contentId": 24, "contentType": "assessment", "status": "published" } }
```

---

### TC-A-34-09 — POST /contents/{id}/restore — deleted → 409 RESTORE_NOT_ALLOWED

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-09 |
| **Tham chiếu** | AC-34-12, FR-34-19 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `service.restore(...)` ném `RestoreNotAllowedException`

**Expected:**
```
HTTP 409  { "status": 409, "message": "Nội dung đã bị xóa không thể khôi phục (trạng thái cuối)", "data": { "errorCode": "RESTORE_NOT_ALLOWED" } }
```

---

### TC-A-34-10 — GET /api/manager/contents/{id} — không tồn tại → 404

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-10 |
| **Tham chiếu** | AC-34-05, FR-34-08 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:** `GET /api/manager/contents/999?contentType=question`

**Mock:** `service.getDetail(...)` ném `ContentNotFoundException`

**Expected:** `HTTP 404` / `CONTENT_NOT_FOUND`

---

### TC-A-34-11 — Response KHÔNG lộ Entity / cột nội bộ

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-34-11 |
| **Tham chiếu** | NFR-34-05, ADR-005 |
| **Loại** | API — Invariant |
| **Ưu tiên** | P0 |

**Steps:**
1. Gọi `GET /api/manager/contents/105?contentType=question`
2. Parse đệ quy toàn bộ JSON

**Expected:**
- Response là DTO; có `contentId`, `contentType`, `status`, `references`
- KHÔNG có key Hibernate nội bộ (`hibernateLazyInitializer`, `handler`) hay trường nhạy cảm không khai báo trong DTO

---

## 4. TEST DATA SUMMARY

| Fixture | contentType | contentId | status | Ghi chú |
|:---|:---|:---|:---|:---|
| `publishedQuestionInExam` | question | 1 | published | nằm trong assessment 2 (`published`) → bị chặn |
| `publishedQuestionFree` | question | 9 | published | không bị tham chiếu → ẩn được |
| `publishedAssessment` | assessment | 24 | published | đề thi độc lập |
| `archivedAssessment` | assessment | 24 | archived | dùng cho Restore |
| `deletedQuestion` | question | 9 | deleted | dùng cho Restore bị chặn |
| `publishedLesson` | lesson | 5 | published | dùng cho soft delete |
| `archivedRefAssessment` | assessment | 8 | archived | tham chiếu question 9 nhưng KHÔNG chặn |
| `missing` | question | 999 | (không tồn tại) | 404 |

> Reviewer chuẩn: `managerId = 10` (`staff_role = staff_manager`).

---

## 5. COVERAGE CHECKLIST (Rule nghiệp vụ UC-34)

| Rule | Mô tả | Test Case | Covered? |
|:---|:---|:---|:---|
| Rule 1 | Chỉ StaffManager quản lý trạng thái published | TC-A-34-02, TC-A-34-03 | ✅ |
| Rule 2 | Archive/Delete là soft delete (chỉ đổi status) | TC-U-34-01, TC-U-34-05, TC-I-34-04 | ✅ |
| Rule 3 | Nội dung ẩn không hiển thị Student-facing | TC-I-34-06 | ✅ |
| Rule 4 | Bắt buộc reason khi unpublish/archive/delete | TC-U-34-03, TC-A-34-06 | ✅ |
| Rule 5 | Chặn ẩn câu hỏi đang trong đề thi published | TC-U-34-02, TC-I-34-01, TC-A-34-05 | ✅ |
| Rule 6 | Kiểm tra tham chiếu lesson/assessment trước khi ẩn | TC-U-34-02, TC-I-34-01/02 | ✅ |
| Rule 7 | 409 RESOURCE_IN_USE + danh sách tham chiếu | TC-A-34-05, TC-U-34-02 | ✅ |
| Rule 8 | Restore chỉ cho archived, không cho deleted | TC-U-34-06, TC-U-34-07, TC-A-34-08, TC-A-34-09 | ✅ |
| Rule 9 | Ghi admin_audit_logs mọi thao tác | TC-U-34-01/05, TC-I-34-05, TC-U-34-12 | ✅ |
| ADR-004 | Soft delete — không hard delete | TC-U-34-05, TC-I-34-04 | ✅ |
| ADR-005 | Không lộ Entity ra API | TC-A-34-11 | ✅ |
