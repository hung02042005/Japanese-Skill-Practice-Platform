# TC-UC-25 — Test Cases: Quản Lý Nội Dung Ngữ Pháp (Manage Grammar Content)

> **Feature:** `feat-content-management` | **UC:** UC-25 | **Version:** 1.0
> **Nguồn AC:** UC-25 § 8 (AC-01..17) | **Nguồn FR:** UC-25 § 3 (FR-01..22)
> **Actor:** Staff | **Cập nhật:** 2026-06-12

---

## 1. UNIT TESTS — GrammarService (JUnit 5 + Mockito)

> **File:** `GrammarServiceTest.java` | **Tag:** `@Tag("unit")`

### TC-U-25-01 — Tạo grammar hợp lệ → draft, bỏ qua status client

| **ID** | TC-U-25-01 · **Tham chiếu** AC-01, FR-01/02/03 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `currentStaffId=7`; request đủ `structure`, `meaning`, `usageExplanation`, `exampleSentenceJp`, `jlptLevel=N5`, kèm `status="published"`.
**Expected:** Lưu `status='draft'`, `created_by=7`; `status` client bị bỏ qua; timestamp server.

### TC-U-25-02 — jlptLevel ngoài tập → ERR-LEVEL-400

| **ID** | TC-U-25-02 · **Tham chiếu** AC-02, FR-04 · **Ưu tiên** P1 |
|:---|:---|

**Steps:** `jlptLevel='N6'`. **Expected:** 400 `ERR-LEVEL-400`; không `save`.

### TC-U-25-03 — Thiếu trường bắt buộc (meaning) → 400 liệt kê field

| **ID** | TC-U-25-03 · **Tham chiếu** AC-03, FR-05 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 400 `ERR-VAL-400`, payload lỗi nêu `meaning`; không `save`.

### TC-U-25-04 — Liên kết lesson cùng cấp độ → gán lesson_id

| **ID** | TC-U-25-04 · **Tham chiếu** AC-04, FR-06 · **Ưu tiên** P1 |
|:---|:---|

**Setup:** `lessonId=12` tồn tại, `lesson.jlpt_level=N5`, grammar N5.
**Expected:** 201; `lesson_id=12` được gán.

### TC-U-25-05 — Lesson khác cấp độ → ERR-LEVEL-MISMATCH-422

| **ID** | TC-U-25-05 · **Tham chiếu** AC-04, FR-08 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Grammar N5, `lesson.jlpt_level=N3`. **Expected:** 422 `ERR-LEVEL-MISMATCH-422`; không `save`.

### TC-U-25-06 — lessonId không tồn tại → ERR-LESSON-404

| **ID** | TC-U-25-06 · **Tham chiếu** FR-07 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 404 `ERR-LESSON-404`.

### TC-U-25-07 — Sửa khi published → ERR-STATE-EDIT-422

| **ID** | TC-U-25-07 · **Tham chiếu** AC-08, FR-14 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Grammar 101 `status='published'`. **Expected:** 422; gợi ý tạo version mới; không `save`.

### TC-U-25-08 — Sửa khi pending_review → 422

| **ID** | TC-U-25-08 · **Tham chiếu** AC-09, FR-15 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 422 `ERR-STATE-EDIT2-422`.

### TC-U-25-09 — PUT bỏ qua status client

| **ID** | TC-U-25-09 · **Tham chiếu** AC-10, FR-16 · **Ưu tiên** P1 |
|:---|:---|

**Setup:** Grammar 101 `draft`; body chứa `"status":"published"`.
**Expected:** Trường `status` bị bỏ qua; trạng thái không đổi; `updated_at` mới.

### TC-U-25-10 — Sửa grammar của Staff khác → 403

| **ID** | TC-U-25-10 · **Tham chiếu** AC-11, FR-17 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Grammar 101 `created_by=7`; caller `staffId=9`. **Expected:** 403 `ERR-AUTH-403`.

### TC-U-25-11 — Submit-review grammar draft đủ trường → pending_review

| **ID** | TC-U-25-11 · **Tham chiếu** AC-12, FR-18 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `status='pending_review'`; log `GRAMMAR_SUBMITTED`.

### TC-U-25-12 — Submit-review thiếu exampleSentenceJp → 422 incomplete

| **ID** | TC-U-25-12 · **Tham chiếu** AC-14, FR-20 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Grammar draft thiếu `example_sentence_jp`.
**Expected:** 422 `ERR-SUBMIT-INCOMPLETE-422`; status không đổi.

### TC-U-25-13 — Submit-review khi published → 422

| **ID** | TC-U-25-13 · **Tham chiếu** AC-13, FR-19 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 422 `ERR-STATE-SUBMIT-422`.

### TC-U-25-14 — GET chi tiết grammar không tồn tại / deleted → ERR-NF-404

| **ID** | TC-U-25-14 · **Tham chiếu** AC-06, FR-12 · **Ưu tiên** P1 |
|:---|:---|

**Setup:** `findById(9999)` trả empty (hoặc bản ghi `status='deleted'`).
**Steps:** `service.getDetail(9999L, callerId)`.
**Expected:** Ném `ContentNotFoundException` (404 `ERR-NF-404`).

### TC-U-25-15 — Cập nhật grammar draft hợp lệ → refresh updated_at

| **ID** | TC-U-25-15 · **Tham chiếu** AC-07, FR-13/16 · **Ưu tiên** P1 |
|:---|:---|

**Setup:** Grammar 101 `status='draft'`, owner=7; body sửa `meaning` hợp lệ.
**Steps:** `service.update(101L, req, 7L)`.
**Expected:** `save` được gọi; `updated_at` mới; status vẫn `draft`; nội dung `meaning` cập nhật.

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `GrammarRepositoryIT.java` | **Tag:** `@Tag("integration")` | Testcontainers + Flyway

### TC-I-25-01 — List chỉ trả grammar của caller, loại deleted

| **ID** | TC-I-25-01 · **Tham chiếu** AC-05, FR-09/11 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** Staff A có 3 grammar (1 `deleted`), Staff B có 2; gọi `findByCreatedByWithFilters(A,...)`.
**Expected:** Trả 2 bản ghi của A (không gồm `deleted`), không gồm của B.

### TC-I-25-02 — Soft delete: status='deleted' không bị hard delete

| **ID** | TC-I-25-02 · **Tham chiếu** NFR-09, ADR-004 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** Bản ghi vẫn tồn tại trong DB với `status='deleted'`; không có `DELETE FROM`.

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `StaffGrammarControllerTest.java` | **Tag:** `@Tag("api")`

### TC-A-25-01 — POST /api/staff/grammar — STAFF → 201

| **ID** | TC-A-25-01 · **Tham chiếu** AC-01 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 201`, `data:{ grammarId, status:"draft", jlptLevel }`.

### TC-A-25-02 — Không JWT → 401; role student → 403

| **ID** | TC-A-25-02 · **Tham chiếu** AC-16, NFR-01 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** Không Authorization → 401; `@WithMockUser(authorities="STUDENT")` → 403.

### TC-A-25-03 — PUT published → 422

| **ID** | TC-A-25-03 · **Tham chiếu** AC-08 · **Ưu tiên** P1 |
|:---|:---|

**Mock:** service ném state-edit exception. **Expected:** `HTTP 422 ERR-STATE-EDIT-422`.

### TC-A-25-04 — Không lộ Entity (không trả password_hash creator)

| **ID** | TC-A-25-04 · **Tham chiếu** AC-17, NFR-07 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** Response là DTO; không có field nội bộ/nhạy cảm.

---

## 4. TEST DATA SUMMARY

| Fixture | grammarId | created_by | status | lesson_id |
|:---|:---|:---|:---|:---|
| `draftOwn` | 101 | 7 | draft | null |
| `linkedSameLevel` | 102 | 7 | draft | 12 (N5) |
| `publishedOwn` | 103 | 7 | published | null |
| `byOther` | 104 | 9 | draft | null |
| `deletedOwn` | 105 | 7 | deleted | null |

---

## 5. COVERAGE CHECKLIST (FR UC-25)

| FR | Mô tả | Test Case | Covered? |
|:---|:---|:---|:---|
| FR-01/02/03 | Create → draft, bỏ qua status | TC-U-25-01, TC-A-25-01 | ✅ |
| FR-04/05 | JLPT + trường bắt buộc | TC-U-25-02/03 | ✅ |
| FR-06/07/08 | Liên kết lesson + khớp level | TC-U-25-04/05/06 | ✅ |
| FR-13/14/15/16 | Edit theo state machine | TC-U-25-07/08/09/15, TC-A-25-03 | ✅ |
| FR-17 | Ownership | TC-U-25-10 | ✅ |
| FR-18/19/20 | Submit-review + gate đầy đủ | TC-U-25-11/12/13 | ✅ |
| FR-09/11 | List của mình, loại deleted | TC-I-25-01 | ✅ |
| FR-12 | Chi tiết không tồn tại → 404 | TC-U-25-14 | ✅ |
| FR-21 | Cấm publish | (contract: không có endpoint/field) — TC-A-25-02 phụ trợ | ✅ |
| NFR-01/07 | Security + DTO | TC-A-25-02/04 | ✅ |
