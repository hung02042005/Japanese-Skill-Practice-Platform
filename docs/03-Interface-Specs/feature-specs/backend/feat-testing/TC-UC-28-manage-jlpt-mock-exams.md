# TC-UC-28 — Test Cases: Quản Lý Đề Thi Thử JLPT (Manage JLPT Mock Exams)

> **Feature:** `feat-content-management` | **UC:** UC-28 | **Version:** 1.0
> **Nguồn AC:** UC-28 § 8 (AC-28-01..22) | **Nguồn FR:** UC-28 § 3 (FR-28-01..36)
> **Actor:** Staff | **Bảng:** `assessments` (type=exam) + `question_assignments` (section qua `section_name`) | **Cập nhật:** 2026-06-12

---

## 1. UNIT TESTS — AssessmentService (JUnit 5 + Mockito)

> **File:** `AssessmentServiceExamTest.java` | **Tag:** `@Tag("unit")`

### TC-U-28-01 — Tạo exam hợp lệ → draft, type=exam

| **ID** | TC-U-28-01 · **Tham chiếu** AC-28-01, FR-28-01/05 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `currentStaffId=5`; N3, `durationMin=140`, `passScore=95`, `totalScore=180`.
**Expected:** `assessment_type='exam'`, `status='draft'`, `created_by=5`.

### TC-U-28-02 — Bỏ qua status client gửi

| **ID** | TC-U-28-02 · **Tham chiếu** AC-28-02, FR-28-05 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Body kèm `"status":"published"`. **Expected:** 201; vẫn `status='draft'`.

### TC-U-28-03 — Thiếu totalScore → VALIDATION_FAILED

| **ID** | TC-U-28-03 · **Tham chiếu** AC-28-03, FR-28-02 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 400 `VALIDATION_FAILED`; không `save`.

### TC-U-28-04 — passScore > totalScore → VALIDATION_FAILED

| **ID** | TC-U-28-04 · **Tham chiếu** AC-28-04, FR-28-04 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `passScore=200`, `totalScore=180`. **Expected:** 400 `VALIDATION_FAILED`; rollback.

### TC-U-28-05 — jlptLevel sai → INVALID_JLPT_LEVEL

| **ID** | TC-U-28-05 · **Tham chiếu** AC-28-05, FR-28-03 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 400 `INVALID_JLPT_LEVEL`.

### TC-U-28-06 — Gán câu hỏi kèm section hợp lệ → lưu section_name/order/score

| **ID** | TC-U-28-06 · **Tham chiếu** AC-28-08, FR-28-20/27 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Exam 51 `draft` N3; 5 câu `published` cùng N3, mỗi item có `sectionName`. Replace semantics.
**Expected:** `deleteByParent` trước; tạo `question_assignments` đúng `section_name`/`display_order`/`score` trong một transaction.

### TC-U-28-07 — Thiếu sectionName khi gán → VALIDATION_FAILED, rollback batch

| **ID** | TC-U-28-07 · **Tham chiếu** AC-28-09, FR-28-21 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Một item thiếu `sectionName` (bắt buộc với exam). **Expected:** 400 `VALIDATION_FAILED`; rollback toàn batch.

### TC-U-28-08 — sectionName ngoài tập → INVALID_SECTION

| **ID** | TC-U-28-08 · **Tham chiếu** AC-28-10, FR-28-22 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `sectionName='speaking'`. **Expected:** 400 `INVALID_SECTION`; rollback.

### TC-U-28-09 — Gán câu hỏi chưa publish → QUESTION_NOT_PUBLISHED

| **ID** | TC-U-28-09 · **Tham chiếu** AC-28-11, FR-28-24 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 422 `QUESTION_NOT_PUBLISHED`; rollback.

### TC-U-28-10 — Gán câu hỏi khác cấp độ → LEVEL_MISMATCH

| **ID** | TC-U-28-10 · **Tham chiếu** AC-28-12, FR-28-25, AGENTS §5#5 · **Ưu tiên** P0 (Correctness) |
|:---|:---|

**Setup:** Exam N3, câu hỏi N2. **Expected:** 422 `LEVEL_MISMATCH`; rollback.

### TC-U-28-11 — Gán câu hỏi trùng → DUPLICATE_ASSIGNMENT

| **ID** | TC-U-28-11 · **Tham chiếu** AC-28-13, FR-28-26 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 409 `DUPLICATE_ASSIGNMENT`; rollback.

### TC-U-28-12 — Gán khi exam published → ASSESSMENT_PUBLISHED

| **ID** | TC-U-28-12 · **Tham chiếu** AC-28-17, FR-28-28, LESSON-005 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Exam 51 `status='published'`. **Expected:** 409 `ASSESSMENT_PUBLISHED`; không đổi.

### TC-U-28-13 — Submit-review Σscore=total → pending_review

| **ID** | TC-U-28-13 · **Tham chiếu** AC-28-14, FR-28-30/32 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `totalScore=180`, `sumAssignedScore=180`, ≥1 câu. **Expected:** `status='pending_review'`.

### TC-U-28-14 — Submit-review Σscore≠total → SCORE_MISMATCH

| **ID** | TC-U-28-14 · **Tham chiếu** AC-28-15, FR-28-30 · **Ưu tiên** P0 (Correctness) |
|:---|:---|

**Setup:** `totalScore=180`, `sumAssignedScore=175`. **Expected:** 422 `SCORE_MISMATCH`; status không đổi.

### TC-U-28-15 — Submit-review exam rỗng → EMPTY_EXAM

| **ID** | TC-U-28-15 · **Tham chiếu** AC-28-16, FR-28-31 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 422 `EMPTY_EXAM`.

### TC-U-28-16 — Update khi pending_review → INVALID_STATUS_TRANSITION

| **ID** | TC-U-28-16 · **Tham chiếu** AC-28-18, FR-28-17 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 409 `INVALID_STATUS_TRANSITION`.

### TC-U-28-17 — Không phải chủ sở hữu → FORBIDDEN

| **ID** | TC-U-28-17 · **Tham chiếu** AC-28-20, FR-28-34 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Exam 51 `created_by=5`; caller=9. **Expected:** 403 `FORBIDDEN`.

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `AssessmentExamRepositoryIT.java` | **Tag:** `@Tag("integration")` | Testcontainers + Flyway

### TC-I-28-01 — List chỉ trả type=exam, loại deleted

| **ID** | TC-I-28-01 · **Tham chiếu** AC-28-06, FR-28-10/12 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** Seed 4 exam (2 N3, 1 deleted) + quiz; `findByTypeWithFilters("exam","N3",...)`.
**Expected:** Chỉ exam N3 không `deleted`; không lẫn quiz.

### TC-I-28-02 — Chi tiết gom theo section + sectionScore

| **ID** | TC-I-28-02 · **Tham chiếu** AC-28-07, FR-28-13/14 · **Ưu tiên** P1 |
|:---|:---|

**Steps:** Exam 51 có câu ở 2 section (vocabulary, grammar); query gom nhóm.
**Expected:** `sections[]` gom theo `section_name`; mỗi section sort `display_order`; có `sectionScore` + `assignedScoreSum`.

### TC-I-28-03 — assignQuestions atomic: 1 item LEVEL_MISMATCH → rollback toàn bộ

| **ID** | TC-I-28-03 · **Tham chiếu** AC-28-12, NFR-28-04 · **Ưu tiên** P0 (Atomicity) |
|:---|:---|

**Steps:** Batch 5 item, 1 item câu N2 (đề N3); thực thi trong transaction.
**Expected:** 0 bản ghi commit; assignment cũ giữ nguyên.

### TC-I-28-04 — UNIQUE assignment chặn trùng ở DB

| **ID** | TC-I-28-04 · **Tham chiếu** FR-28-26 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** Insert trùng `(assessment,51,q)` → DataIntegrityViolation.

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `StaffAssessmentControllerExamTest.java` | **Tag:** `@Tag("api")`

### TC-A-28-01 — POST /api/staff/assessments — exam → 201

| **ID** | TC-A-28-01 · **Tham chiếu** AC-28-01 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 201`, `data:{ assessmentId, assessmentType:"exam", status:"draft" }`.

### TC-A-28-02 — assign-questions thiếu sectionName → 400 VALIDATION_FAILED

| **ID** | TC-A-28-02 · **Tham chiếu** AC-28-09 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 400 VALIDATION_FAILED`.

### TC-A-28-03 — assign-questions câu khác level → 422 LEVEL_MISMATCH

| **ID** | TC-A-28-03 · **Tham chiếu** AC-28-12 · **Ưu tiên** P0 |
|:---|:---|

**Mock:** service ném `LevelMismatchException`. **Expected:** `HTTP 422 LEVEL_MISMATCH`.

### TC-A-28-04 — submit-review Σscore lệch → 422 SCORE_MISMATCH

| **ID** | TC-A-28-04 · **Tham chiếu** AC-28-15 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 422 SCORE_MISMATCH`.

### TC-A-28-05 — Cố đặt status=published → 403 PUBLISH_NOT_ALLOWED

| **ID** | TC-A-28-05 · **Tham chiếu** AC-28-19, FR-28-33 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 403 PUBLISH_NOT_ALLOWED`.

### TC-A-28-06 — Không JWT → 401; role student → 403

| **ID** | TC-A-28-06 · **Tham chiếu** AC-28-21, NFR-28-02 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 401 / 403 tương ứng.

### TC-A-28-07 — Chỉ trả DTO (không lộ password_hash creator)

| **ID** | TC-A-28-07 · **Tham chiếu** AC-28-22, NFR-28-05, ADR-005 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** Response DTO; không có field nội bộ/nhạy cảm.

---

## 4. TEST DATA SUMMARY

| Fixture | assessmentId | type | created_by | status | jlptLevel | totalScore | Σscore |
|:---|:---|:---|:---|:---|:---|:---|:---|
| `examDraftMatched` | 51 | exam | 5 | draft | N3 | 180 | 180 |
| `examDraftMismatch` | 52 | exam | 5 | draft | N3 | 180 | 175 |
| `examEmpty` | 53 | exam | 5 | draft | N3 | 180 | 0 |
| `examPublished` | 54 | exam | 5 | published | N3 | 180 | 180 |
| `examByOther` | 55 | exam | 9 | draft | N3 | 180 | 0 |
| `pubQuestionN3` | (q)12 | — | 5 | published | N3 | — | — |
| `pubQuestionN2` | (q)80 | — | 5 | published | N2 | — | — |

---

## 5. COVERAGE CHECKLIST (Rule/FR UC-28)

| Rule/FR | Mô tả | Test Case | Covered? |
|:---|:---|:---|:---|
| FR-28-01/05 | Create exam → draft, bỏ qua status | TC-U-28-01/02, TC-A-28-01 | ✅ |
| FR-28-02/04 | Trường bắt buộc + range điểm | TC-U-28-03/04 | ✅ |
| FR-28-20/27 | Section + assign + replace | TC-U-28-06, TC-I-28-02 | ✅ |
| FR-28-21/22 | sectionName bắt buộc + hợp lệ | TC-U-28-07/08, TC-A-28-02 | ✅ |
| FR-28-24 | Câu hỏi published | TC-U-28-09 | ✅ |
| FR-28-25 | Cùng cấp độ JLPT | TC-U-28-10, TC-I-28-03, TC-A-28-03 | ✅ |
| FR-28-26 | Chống trùng | TC-U-28-11, TC-I-28-04 | ✅ |
| FR-28-28 | Khóa khi published | TC-U-28-12 | ✅ |
| FR-28-30/31 | Σscore=total + ≥1 câu gate | TC-U-28-13/14/15, TC-A-28-04 | ✅ |
| FR-28-17 | Update theo status | TC-U-28-16 | ✅ |
| FR-28-34 | Ownership | TC-U-28-17 | ✅ |
| FR-28-33 | Cấm publish | TC-A-28-05 | ✅ |
| NFR-28-02/05 | Security + DTO | TC-A-28-06/07 | ✅ |
