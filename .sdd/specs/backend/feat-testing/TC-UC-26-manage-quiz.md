# TC-UC-26 — Test Cases: Quản Lý Bài Trắc Nghiệm (Manage Quiz)

> **Feature:** `feat-content-management` | **UC:** UC-26 | **Version:** 1.0
> **Nguồn AC:** UC-26 § 8 (AC-26-01..16) | **Nguồn FR:** UC-26 § 3 (FR-26-01..33)
> **Actor:** Staff | **Bảng:** `assessments` (type=quiz) + `question_assignments` | **Cập nhật:** 2026-06-12

---

## 1. UNIT TESTS — AssessmentService (JUnit 5 + Mockito)

> **File:** `AssessmentServiceQuizTest.java` | **Tag:** `@Tag("unit")`

### TC-U-26-01 — Tạo quiz hợp lệ → draft, type=quiz

| **ID** | TC-U-26-01 · **Tham chiếu** AC-26-01, FR-26-01/07 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `currentStaffId=5`; body đủ `title`, `lessonId=3`, `jlptLevel=N5`, `durationMin=15`, `passScore=5`, `totalScore=10`, kèm `status="published"`.
**Expected:** Lưu `assessment_type='quiz'`, `status='draft'`, `created_by=5`; bỏ qua `status`/`approved_by`/`published_at` client.

### TC-U-26-02 — Thiếu cả lessonId và topic → VALIDATION_FAILED

| **ID** | TC-U-26-02 · **Tham chiếu** AC-26-02, FR-26-03 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 400 `VALIDATION_FAILED`; không `save`.

### TC-U-26-03 — passScore > totalScore → VALIDATION_FAILED

| **ID** | TC-U-26-03 · **Tham chiếu** AC-26-03, FR-26-05 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `passScore=12`, `totalScore=10`. **Expected:** 400 `VALIDATION_FAILED`; rollback.

### TC-U-26-04 — jlptLevel sai → INVALID_JLPT_LEVEL

| **ID** | TC-U-26-04 · **Tham chiếu** AC-26-04, FR-26-04 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 400 `INVALID_JLPT_LEVEL`.

### TC-U-26-05 — lessonId không tồn tại → LESSON_NOT_FOUND

| **ID** | TC-U-26-05 · **Tham chiếu** FR-26-06 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 404 `LESSON_NOT_FOUND`.

### TC-U-26-06 — Gán câu hỏi lưu order + score (replace semantics)

| **ID** | TC-U-26-06 · **Tham chiếu** AC-26-07, FR-26-19/23 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Quiz 24 `draft`, owner=5; 2 câu hỏi `published`. Mock `deleteByParent` + `saveAll`.
**Steps:** `assignQuestions(24, req[2 item], 5L)`.
**Expected:** `deleteByParent('assessment',24)` gọi trước; tạo 2 `question_assignments` đúng `display_order`/`score`; trong **một** transaction.

### TC-U-26-07 — Gán câu hỏi trùng → DUPLICATE_ASSIGNMENT, rollback batch

| **ID** | TC-U-26-07 · **Tham chiếu** AC-26-08, FR-26-22 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** 2 item cùng `questionId=105`. **Expected:** 409 `DUPLICATE_ASSIGNMENT`; không bản ghi nào được commit (rollback toàn batch).

### TC-U-26-08 — Gán câu hỏi chưa publish → QUESTION_NOT_PUBLISHED

| **ID** | TC-U-26-08 · **Tham chiếu** AC-26-09, FR-26-21 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Câu hỏi `status='draft'`. **Expected:** 422 `QUESTION_NOT_PUBLISHED`; rollback.

### TC-U-26-09 — Gán câu hỏi không tồn tại → QUESTION_NOT_FOUND

| **ID** | TC-U-26-09 · **Tham chiếu** FR-26-21 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 404 `QUESTION_NOT_FOUND`; rollback.

### TC-U-26-10 — Gán khi quiz published → ASSESSMENT_PUBLISHED

| **ID** | TC-U-26-10 · **Tham chiếu** AC-26-13, FR-26-24, LESSON-005 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Quiz 24 `status='published'`. **Expected:** 409 `ASSESSMENT_PUBLISHED`; không thay đổi assignment.

### TC-U-26-11 — Submit-review Σscore=total → pending_review

| **ID** | TC-U-26-11 · **Tham chiếu** AC-26-10, FR-26-26/27 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Quiz 24 `totalScore=10`, `sumAssignedScore=10`, ≥1 câu.
**Expected:** `status='pending_review'`.

### TC-U-26-12 — Submit-review Σscore≠total → SCORE_MISMATCH

| **ID** | TC-U-26-12 · **Tham chiếu** AC-26-11, FR-26-26 · **Ưu tiên** P0 (Correctness) |
|:---|:---|

**Setup:** `totalScore=10`, `sumAssignedScore=9`.
**Expected:** 422 `SCORE_MISMATCH`; status không đổi.

### TC-U-26-13 — Submit-review quiz rỗng → EMPTY_QUIZ

| **ID** | TC-U-26-13 · **Tham chiếu** AC-26-12, FR-26-28 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Quiz 24 không có assignment. **Expected:** 422 `EMPTY_QUIZ`.

### TC-U-26-14 — Update khi pending_review → INVALID_STATUS_TRANSITION

| **ID** | TC-U-26-14 · **Tham chiếu** AC-26-14, FR-26-16 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 409 `INVALID_STATUS_TRANSITION`.

### TC-U-26-15 — Không phải chủ sở hữu → FORBIDDEN

| **ID** | TC-U-26-15 · **Tham chiếu** AC-26-16, FR-26-31 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Quiz 24 `created_by=5`; caller=9. **Expected:** 403 `FORBIDDEN` cho update/assign/submit.

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `AssessmentQuizRepositoryIT.java` | **Tag:** `@Tag("integration")` | Testcontainers + Flyway

### TC-I-26-01 — List chỉ trả type=quiz, loại deleted, sort updated_at desc

| **ID** | TC-I-26-01 · **Tham chiếu** AC-26-05, FR-26-10/12 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** Seed 12 quiz (4 N5, 1 deleted) + 3 exam; gọi `findByTypeWithFilters("quiz","N5",null,null,page)`.
**Expected:** Chỉ quiz N5 không `deleted`; không lẫn exam.

### TC-I-26-02 — UNIQUE (parent_type,parent_id,question_id) chặn trùng

| **ID** | TC-I-26-02 · **Tham chiếu** FR-26-22 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** Insert 2 assignment cùng `(assessment,24,105)`. **Expected:** Lần 2 ném DataIntegrityViolation.

### TC-I-26-03 — sumAssignedScore tính đúng tổng score

| **ID** | TC-I-26-03 · **Tham chiếu** FR-26-14 · **Ưu tiên** P1 |
|:---|:---|

**Steps:** Gán 3 câu score 1.0/2.0/2.0. **Expected:** `sumAssignedScore(24)=5.0`.

### TC-I-26-04 — assignQuestions atomic: 1 item lỗi → rollback toàn bộ

| **ID** | TC-I-26-04 · **Tham chiếu** AC-26-08/09, NFR-26-04 · **Ưu tiên** P0 (Atomicity) |
|:---|:---|

**Steps:** Batch 3 item, item thứ 3 trùng/không published; thực thi trong transaction.
**Expected:** 0 bản ghi mới được commit; assignment cũ giữ nguyên.

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `StaffAssessmentControllerQuizTest.java` | **Tag:** `@Tag("api")`

### TC-A-26-01 — POST /api/staff/assessments — quiz → 201

| **ID** | TC-A-26-01 · **Tham chiếu** AC-26-01 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 201`, `data:{ assessmentId, assessmentType:"quiz", status:"draft" }`.

### TC-A-26-02 — POST /{id}/assign-questions trùng → 409

| **ID** | TC-A-26-02 · **Tham chiếu** AC-26-08 · **Ưu tiên** P0 |
|:---|:---|

**Mock:** service ném `DuplicateAssignmentException`. **Expected:** `HTTP 409 DUPLICATE_ASSIGNMENT`.

### TC-A-26-03 — submit-review Σscore lệch → 422 SCORE_MISMATCH

| **ID** | TC-A-26-03 · **Tham chiếu** AC-26-11 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 422`, message "Tổng điểm câu hỏi không khớp với total_score...".

### TC-A-26-04 — Cố đặt status=published → 403 PUBLISH_NOT_ALLOWED

| **ID** | TC-A-26-04 · **Tham chiếu** AC-26-15, FR-26-30 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 403 PUBLISH_NOT_ALLOWED`.

### TC-A-26-05 — Không JWT → 401; role student → 403

| **ID** | TC-A-26-05 · **Tham chiếu** NFR-26-02 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 401 / 403 tương ứng.

### TC-A-26-06 — GET /{id} chi tiết: câu hỏi sort displayOrder + scoreMatched

| **ID** | TC-A-26-06 · **Tham chiếu** AC-26-06, FR-26-13/14 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** `data.questions[]` theo `displayOrder`; có `assignedScoreSum`, `scoreMatched`; là DTO (không lộ Entity).

---

## 4. TEST DATA SUMMARY

| Fixture | assessmentId | type | created_by | status | totalScore | Σscore |
|:---|:---|:---|:---|:---|:---|:---|
| `quizDraftMatched` | 24 | quiz | 5 | draft | 10 | 10 |
| `quizDraftMismatch` | 25 | quiz | 5 | draft | 10 | 9 |
| `quizEmpty` | 26 | quiz | 5 | draft | 10 | 0 |
| `quizPublished` | 27 | quiz | 5 | published | 10 | 10 |
| `quizByOther` | 28 | quiz | 9 | draft | 10 | 0 |
| `pubQuestion` | (q)105 | — | 5 | published | — | — |

---

## 5. COVERAGE CHECKLIST (FR UC-26)

| FR | Mô tả | Test Case | Covered? |
|:---|:---|:---|:---|
| FR-26-01/07 | Create quiz → draft, ép type | TC-U-26-01, TC-A-26-01 | ✅ |
| FR-26-03/05 | lesson/topic + range điểm | TC-U-26-02/03 | ✅ |
| FR-26-19/23 | Assign + replace semantics | TC-U-26-06, TC-I-26-04 | ✅ |
| FR-26-21 | Câu hỏi published/tồn tại | TC-U-26-08/09 | ✅ |
| FR-26-22 | Chống trùng | TC-U-26-07, TC-I-26-02, TC-A-26-02 | ✅ |
| FR-26-24 | Khóa khi published | TC-U-26-10 | ✅ |
| FR-26-26 | Σscore=total gate | TC-U-26-11/12, TC-A-26-03 | ✅ |
| FR-26-28 | Empty quiz | TC-U-26-13 | ✅ |
| FR-26-16 | Update theo status | TC-U-26-14 | ✅ |
| FR-26-31 | Ownership | TC-U-26-15 | ✅ |
| FR-26-30 | Cấm publish | TC-A-26-04 | ✅ |
| NFR-26-02/05 | Security + DTO | TC-A-26-05/06 | ✅ |
