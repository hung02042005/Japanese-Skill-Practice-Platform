# TC-UC-27 — Test Cases: Quản Lý Học Liệu (Manage Learning Content)

> **Feature:** `feat-content-management` | **UC:** UC-27 | **Version:** 1.0
> **Nguồn AC:** UC-27 § 8 (AC-27-01..16) | **Nguồn FR:** UC-27 § 3 (FR-27-01..26)
> **Actor:** Staff | **Phạm vi:** lesson (`lessons`), vocabulary, kanji | **Cập nhật:** 2026-06-12

---

## 1. UNIT TESTS — LearningContentService (JUnit 5 + Mockito)

> **File:** `LearningContentServiceTest.java` | **Tag:** `@Tag("unit")`

### TC-U-27-01 — Tạo lesson hợp lệ (có contentText) → draft

| **ID** | TC-U-27-01 · **Tham chiếu** AC-27-01, FR-27-01/09/11 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `currentStaffId=5`; lesson đủ `title`, `lessonType='lesson'`, `jlptLevel='N5'`, `contentText`.
**Expected:** `status='draft'`, `created_by=5`; bỏ qua status client; timestamp server.

### TC-U-27-02 — Lesson thiếu lessonType → VALIDATION_FAILED

| **ID** | TC-U-27-02 · **Tham chiếu** AC-27-02, FR-27-09 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 400 `VALIDATION_FAILED`; không `save`.

### TC-U-27-03 — Lesson không có nội dung nào → LESSON_CONTENT_REQUIRED

| **ID** | TC-U-27-03 · **Tham chiếu** AC-27-03, FR-27-11 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `lessonType` hợp lệ nhưng thiếu cả `contentText`/`videoUrl`/`audioUrl`/`attachmentUrl`.
**Expected:** 400 `LESSON_CONTENT_REQUIRED`; rollback.

### TC-U-27-04 — Lesson listening thiếu audioUrl → LESSON_CONTENT_REQUIRED

| **ID** | TC-U-27-04 · **Tham chiếu** AC-27-04, FR-27-12 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `lessonType='listening'`, không `audioUrl`. **Expected:** 400 `LESSON_CONTENT_REQUIRED`.

### TC-U-27-05 — lessonType ngoài tập → INVALID_LESSON_TYPE

| **ID** | TC-U-27-05 · **Tham chiếu** FR-27-10 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 400 `INVALID_LESSON_TYPE`.

### TC-U-27-06 — Tạo vocabulary hợp lệ → draft

| **ID** | TC-U-27-06 · **Tham chiếu** AC-27-05, FR-27-16 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `word`, `furigana`, `meaning`, `jlptLevel` đủ. **Expected:** 201; `status='draft'`.

### TC-U-27-07 — Vocabulary thiếu furigana → VALIDATION_FAILED

| **ID** | TC-U-27-07 · **Tham chiếu** AC-27-06, FR-27-16 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** 400 `VALIDATION_FAILED`.

### TC-U-27-08 — Vocabulary gắn lessonId không tồn tại → LESSON_NOT_FOUND

| **ID** | TC-U-27-08 · **Tham chiếu** AC-27-07, FR-27-18 · **Ưu tiên** P1 |
|:---|:---|

**Setup:** `lessonId=9999` không có. **Expected:** 404 `LESSON_NOT_FOUND`.

### TC-U-27-09 — Tạo kanji hợp lệ (có onyomi) → draft

| **ID** | TC-U-27-09 · **Tham chiếu** AC-27-08, FR-27-20 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 201; `status='draft'`.

### TC-U-27-10 — Kanji trùng characterValue → KANJI_DUPLICATE

| **ID** | TC-U-27-10 · **Tham chiếu** AC-27-09, FR-27-21 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** `existsByCharacterValue('水')=true`. **Expected:** 409 `KANJI_DUPLICATE`; không `save`.

### TC-U-27-11 — Kanji thiếu cả onyomi & kunyomi → VALIDATION_FAILED

| **ID** | TC-U-27-11 · **Tham chiếu** AC-27-10, FR-27-20 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 400 `VALIDATION_FAILED`.

### TC-U-27-12 — Update khi pending_review → INVALID_STATUS_TRANSITION

| **ID** | TC-U-27-12 · **Tham chiếu** AC-27-11, FR-27-04 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Lesson 88 `status='pending_review'`. **Expected:** 409 `INVALID_STATUS_TRANSITION`.

### TC-U-27-13 — Update khi rejected → 200, refresh updated_at

| **ID** | TC-U-27-13 · **Tham chiếu** AC-27-12, FR-27-04/07 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** `save` được gọi; `updated_at` mới; status vẫn `rejected`.

### TC-U-27-14 — Submit-review lesson draft đủ trường → pending_review

| **ID** | TC-U-27-14 · **Tham chiếu** AC-27-13, FR-27-25/26 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** `submitForReview(new SubmitReviewRequest("lesson",88), 5L)`.
**Expected:** `status='pending_review'`.

### TC-U-27-15 — Không phải chủ sở hữu → FORBIDDEN

| **ID** | TC-U-27-15 · **Tham chiếu** AC-27-15, FR-27-06 · **Ưu tiên** P0 |
|:---|:---|

**Setup:** Lesson 88 `created_by=5`; caller `staffId=9`. **Expected:** 403 `FORBIDDEN`.

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `LearningContentRepositoryIT.java` | **Tag:** `@Tag("integration")` | Testcontainers + Flyway

### TC-I-27-01 — Media chỉ lưu URL, không BLOB

| **ID** | TC-I-27-01 · **Tham chiếu** AC-27-16, FR-27-03, ADR-006 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** Lưu lesson có `videoUrl`. **Expected:** Cột lưu chuỗi URL; schema không có cột BLOB.

### TC-I-27-02 — character_value UNIQUE chặn trùng ở DB

| **ID** | TC-I-27-02 · **Tham chiếu** FR-27-21 · **Ưu tiên** P1 |
|:---|:---|

**Steps:** Insert 2 kanji cùng `character_value`. **Expected:** Lần 2 ném DataIntegrityViolation (UNIQUE).

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `StaffLearningContentControllerTest.java` | **Tag:** `@Tag("api")`

### TC-A-27-01 — POST /api/staff/lessons — STAFF → 201

| **ID** | TC-A-27-01 · **Tham chiếu** AC-27-01 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 201`, `data:{ lessonId, status:"draft" }`.

### TC-A-27-02 — POST /api/staff/kanji — trùng → 409

| **ID** | TC-A-27-02 · **Tham chiếu** AC-27-09 · **Ưu tiên** P0 |
|:---|:---|

**Mock:** service ném `KanjiDuplicateException`. **Expected:** `HTTP 409 KANJI_DUPLICATE`.

### TC-A-27-03 — Cố đặt status=published → 403 PUBLISH_NOT_ALLOWED

| **ID** | TC-A-27-03 · **Tham chiếu** AC-27-14, FR-27-05 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 403 PUBLISH_NOT_ALLOWED`; status không đổi.

### TC-A-27-04 — Không JWT → 401; role student → 403

| **ID** | TC-A-27-04 · **Tham chiếu** NFR-27-02 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** 401 / 403 tương ứng.

### TC-A-27-05 — Response chỉ DTO

| **ID** | TC-A-27-05 · **Tham chiếu** NFR-27-04, ADR-005 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** DTO; không lộ Entity/field nội bộ.

---

## 4. TEST DATA SUMMARY

| Fixture | type | id | created_by | status |
|:---|:---|:---|:---|:---|
| `lessonDraftOwn` | lesson | 88 | 5 | draft |
| `lessonRejectedOwn` | lesson | 89 | 5 | rejected |
| `lessonPendingOwn` | lesson | 90 | 5 | pending_review |
| `lessonByOther` | lesson | 91 | 9 | draft |
| `kanjiExisting` | kanji | 301 (水) | 5 | published |

---

## 5. COVERAGE CHECKLIST (FR UC-27)

| FR | Mô tả | Test Case | Covered? |
|:---|:---|:---|:---|
| FR-27-01 | Create → draft + created_by | TC-U-27-01/06/09 | ✅ |
| FR-27-09/10/11/12 | Ràng buộc lesson + nội dung | TC-U-27-02/03/04/05 | ✅ |
| FR-27-16/18 | Vocabulary + liên kết lesson | TC-U-27-07/08 | ✅ |
| FR-27-20/21 | Kanji + trùng | TC-U-27-10/11, TC-I-27-02 | ✅ |
| FR-27-03 | Media URL, không BLOB | TC-I-27-01 | ✅ |
| FR-27-04 | Update theo state machine | TC-U-27-12/13 | ✅ |
| FR-27-05 | Cấm publish | TC-A-27-03 | ✅ |
| FR-27-06 | Ownership | TC-U-27-15 | ✅ |
| FR-27-25/26 | Submit-review | TC-U-27-14 | ✅ |
| NFR-27-02/04 | Security + DTO | TC-A-27-04/05 | ✅ |
