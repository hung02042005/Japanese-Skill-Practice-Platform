# Requirements Specification — UC-25: Manage Grammar Content

> **Mã UC:** UC-25 · **Module:** Learning & Content · **Actor chính:** Staff
> **Chuẩn:** SDD / Spec-Kit (8 thành phần) · **Văn phong yêu cầu:** EARS
> **Tham chiếu:** `CONSTITUTION.md`, `AGENTS.md §6/§7`, `CLAUDE.md` (Learning & Content Module), `database/init.sql` (`grammar_points`, `lessons`, `staff_users`)
> **Ngày tạo:** 2026-06-12

---

## ⚠️ Lưu ý sai lệch giữa đề bài và schema thực tế

Tuân `AGENTS.md §9.1` (spec không khớp → nêu rõ, không tự đoán):

1. **Không tồn tại bảng `courses`** trong `init.sql`. Nội dung ngữ pháp chỉ liên kết tới `lessons` qua `grammar_points.lesson_id`. "Course" được đưa vào *Out of Scope* (§9).
2. **Tên cột thực tế khác đề bài:** `structure` (≈ title/pattern), `usage_explanation` (≈ explanation), `example_sentence_jp/vi` (≈ example). Spec dùng đúng tên cột DB và ghi chú ánh xạ ở §5.

---

## 1. Context and Goal

### 1.1. Bối cảnh

Hệ thống JLPT E-Learning cung cấp nội dung học theo 4 loại: KANJI, KANA, VOCAB, **GRAMMAR**. Staff là người sản xuất nội dung; nội dung phải đi qua vòng đời biên tập trước khi đến học viên. UC-25 đặc tả phần nghiệp vụ **soạn và quản lý điểm ngữ pháp (grammar points)** do Staff thực hiện.

### 1.2. Mục tiêu

Cho phép Staff **tạo, xem, chỉnh sửa, liên kết và gửi duyệt** các điểm ngữ pháp JLPT, đảm bảo:

- Nội dung mới luôn ở trạng thái nháp (`draft`), không bao giờ tự lên `published`.
- Toàn vẹn dữ liệu cấp độ JLPT (`AGENTS.md §5 Forbidden #5`).
- Tách bạch quyền Staff vs Admin (`CLAUDE.md LESSON-001`).
- Vòng đời nội dung có kiểm soát: chỉ Staff Manager/Admin (UC khác) mới được `publish`.

### 1.3. Giả định & Ràng buộc nền tảng

- Backend Spring Boot 3.x, REST `/api/...`, response chuẩn `{ status, message, data }` (`AGENTS.md §6`).
- Mọi endpoint yêu cầu JWT hợp lệ + role `staff`/`staff_manager` (`CONSTITUTION.md §3`).
- DTO Pattern bắt buộc, **không** trả Entity ra API (`ADR-005`, `AGENTS.md §5 #9`).
- Soft delete toàn hệ thống; `grammar_points` dùng `status='deleted'` thay cho hard delete (`ADR-004`).
- Mọi validation nghiệp vụ ở backend Service layer (`CONSTITUTION.md §2.5`).

---

## 2. Actor

| Actor | Loại | Mô tả & quyền trong UC-25 |
|-------|------|----------------------------|
| **Staff** (`staff_role='staff'`) | Chính | Tạo, xem (nội dung của mình), sửa khi `draft`/`rejected`, liên kết lesson, gửi duyệt (`pending_review`). |
| **Staff Manager** (`staff_role='staff_manager'`) | Phụ | Kế thừa quyền Staff; thao tác duyệt/publish thuộc UC khác (ngoài phạm vi). |
| **System** (Backend Service) | Hệ thống | Gán `status=draft` mặc định, gán `created_by`, sinh timestamp, enforce state machine, ghi log. |
| Admin / Student | Không tham gia | Ngoài phạm vi UC-25. |

**Tiền điều kiện chung:** Actor đã đăng nhập, JWT còn hiệu lực, `staff_users.status='active'`.

---

## 3. Functional Requirements (EARS)

> Quy ước EARS: **Ubiquitous** (THE SYSTEM SHALL) · **Event** (WHEN … THE SYSTEM SHALL) · **State** (WHILE …) · **Conditional** (IF … THEN THE SYSTEM SHALL) · **Optional** (WHERE …).

### 3.1. Tạo grammar (POST)

- **FR-01 (Event):** WHEN một Staff đã xác thực gửi yêu cầu tạo grammar hợp lệ, THE SYSTEM SHALL tạo bản ghi mới trong `grammar_points` với `status = 'draft'`.
- **FR-02 (Ubiquitous):** THE SYSTEM SHALL tự gán `created_by` = `staff_id` của người gọi và `created_at`/`updated_at` = thời gian server; THE SYSTEM SHALL bỏ qua mọi giá trị `status`, `created_by`, `approved_by`, `published_at` do client gửi lên.
- **FR-03 (Ubiquitous):** THE SYSTEM SHALL bắt buộc các trường: `structure` (title/pattern), `meaning`, `usage_explanation` (explanation), ít nhất một câu ví dụ (`example_sentence_jp`), và `jlpt_level`.
- **FR-04 (Conditional):** IF `jlpt_level` không thuộc tập {N5, N4, N3, N2, N1}, THEN THE SYSTEM SHALL từ chối với 400 và không tạo bản ghi.
- **FR-05 (Conditional):** IF bất kỳ trường bắt buộc nào thiếu/rỗng, THEN THE SYSTEM SHALL từ chối với 400 và liệt kê field lỗi.

### 3.2. Liên kết lesson (Optional)

- **FR-06 (Optional/Where):** WHERE Staff cung cấp `lessonId`, THE SYSTEM SHALL gán `grammar_points.lesson_id` sau khi xác minh lesson tồn tại và chưa bị xóa.
- **FR-07 (Conditional):** IF `lessonId` được cung cấp nhưng không tồn tại (hoặc `status='deleted'`), THEN THE SYSTEM SHALL từ chối với 404.
- **FR-08 (Conditional):** IF `lessonId` tồn tại nhưng `lessons.jlpt_level` ≠ `grammar.jlpt_level`, THEN THE SYSTEM SHALL từ chối với 422 (chống lẫn lộn cấp độ — `AGENTS.md §5 #5`).

> *Ghi chú:* Liên kết "course" theo đề bài **không khả thi** ở schema hiện tại (không có bảng `courses`); xem §9 Out of Scope.

### 3.3. Xem grammar (GET)

- **FR-09 (Event):** WHEN Staff yêu cầu danh sách grammar, THE SYSTEM SHALL trả về danh sách phân trang các grammar **do chính Staff đó tạo** (`created_by = caller`), hỗ trợ lọc theo `jlpt_level` và `status`.
- **FR-10 (Event):** WHEN Staff yêu cầu chi tiết theo `grammarId`, THE SYSTEM SHALL trả về bản ghi tương ứng dưới dạng DTO.
- **FR-11 (Ubiquitous):** THE SYSTEM SHALL loại trừ bản ghi có `status='deleted'` khỏi mọi kết quả đọc.
- **FR-12 (Conditional):** IF `grammarId` không tồn tại hoặc đã `deleted`, THEN THE SYSTEM SHALL trả 404.

### 3.4. Chỉnh sửa grammar (PUT)

- **FR-13 (State):** WHILE `status ∈ {draft, rejected}`, THE SYSTEM SHALL cho phép Staff cập nhật các trường nội dung và `lesson_id`.
- **FR-14 (Conditional):** IF `status = 'published'`, THEN THE SYSTEM SHALL từ chối chỉnh sửa trực tiếp với 422 và yêu cầu tạo phiên bản mới (Rule #6).
- **FR-15 (Conditional):** IF `status ∈ {pending_review, archived}`, THEN THE SYSTEM SHALL từ chối chỉnh sửa với 422.
- **FR-16 (Ubiquitous):** THE SYSTEM SHALL cập nhật `updated_at` = thời gian server cho mọi sửa đổi thành công; THE SYSTEM SHALL **không** cho phép client thay đổi `status` qua endpoint PUT.
- **FR-17 (Conditional):** IF người gọi không phải `created_by` của bản ghi (và không có quyền cao hơn), THEN THE SYSTEM SHALL trả 403.

### 3.5. Gửi duyệt (Submit review)

- **FR-18 (Event):** WHEN Staff gửi yêu cầu submit-review cho grammar đang ở `status ∈ {draft, rejected}`, THE SYSTEM SHALL chuyển `status` → `pending_review` và cập nhật `updated_at`.
- **FR-19 (Conditional):** IF grammar không ở `draft`/`rejected`, THEN THE SYSTEM SHALL từ chối chuyển trạng thái với 422.
- **FR-20 (Conditional):** IF grammar thiếu trường bắt buộc (FR-03), THEN THE SYSTEM SHALL chặn submit với 422 (không cho gửi duyệt nội dung chưa hoàn chỉnh).

### 3.6. Ràng buộc phân quyền publish

- **FR-21 (Ubiquitous):** THE SYSTEM SHALL **không** cung cấp cho Staff bất kỳ phương tiện nào để đặt `status='published'` (Rule #8); chuyển sang `published` chỉ thực hiện bởi UC duyệt nội dung của Staff Manager/Admin.

### 3.7. Audit

- **FR-22 (Ubiquitous):** THE SYSTEM SHALL ghi log có cấu trúc (SLF4J) cho các sự kiện `GRAMMAR_CREATED`, `GRAMMAR_UPDATED`, `GRAMMAR_SUBMITTED` kèm `staff_id` và `grammar_id`.

### 3.8. State Machine (tham chiếu)

```
[draft] ──submit──► [pending_review] ──(approve: UC khác)──► [published]
   ▲                      │
   │                      └──(reject: UC khác)──► [rejected] ──submit──► [pending_review]
   └──────── edit ────────┘ (draft & rejected editable)
[published] ──(new version: UC khác)──► [draft]
* delete (soft): bất kỳ → 'deleted' (UC quản lý riêng)
```

---

## 4. Non-Functional Requirements

| # | Nhóm | Yêu cầu (EARS Ubiquitous) |
|---|------|----------------------------|
| NFR-01 | Security | THE SYSTEM SHALL yêu cầu JWT hợp lệ + role `staff`/`staff_manager` cho mọi endpoint UC-25; thiếu/sai token → 401, sai role → 403. |
| NFR-02 | Security | THE SYSTEM SHALL chỉ dùng JPA/Hibernate parameterized queries (zero SQL injection — `CONSTITUTION.md §3.2`). |
| NFR-03 | Validation | THE SYSTEM SHALL validate mọi `@RequestBody` bằng Jakarta Bean Validation (`@Valid`); không bypass annotation (`AGENTS.md §5 #10`). |
| NFR-04 | Performance | THE SYSTEM SHALL trả response GET list ≤ 500ms ở p95 với page size mặc định 20; dùng index `IX_grammar_public_level`. |
| NFR-05 | Pagination | THE SYSTEM SHALL phân trang danh sách (default `page=0`, `size=20`, `size` max 100). |
| NFR-06 | Maintainability | THE SYSTEM SHALL tuân thủ Controller→Service→Repository→Entity; method ≤ 40 dòng, file ≤ 300 dòng (`CONSTITUTION.md §2.2`). |
| NFR-07 | API Contract | THE SYSTEM SHALL trả đúng format `{ status, message, data }` và chỉ phơi bày DTO (không Entity). |
| NFR-08 | Observability | THE SYSTEM SHALL log bằng SLF4J, không `System.out.println`. |
| NFR-09 | Data Integrity | THE SYSTEM SHALL đảm bảo soft delete; không thực thi `DELETE FROM grammar_points`. |
| NFR-10 | i18n | THE SYSTEM SHALL hỗ trợ lưu trữ Unicode (NVARCHAR) cho nội dung tiếng Nhật/Việt. |

---

## 5. Data Model

### 5.1. Bảng `grammar_points` (nguồn: `init.sql`, không thay đổi schema)

| Cột | Kiểu | Null | Ràng buộc / Ghi chú | Ánh xạ đề bài |
|-----|------|------|---------------------|----------------|
| `grammar_id` | BIGINT IDENTITY | PK | Khóa chính | — |
| `structure` | NVARCHAR(255) | NOT NULL | Cấu trúc/mẫu ngữ pháp | **title/pattern** |
| `formula` | NVARCHAR(500) | NULL | Công thức kết hợp | (tùy chọn) |
| `meaning` | NVARCHAR(500) | NOT NULL | Nghĩa | **meaning** |
| `usage_explanation` | NVARCHAR(MAX) | NULL → *bắt buộc theo NV* | Giải thích cách dùng | **explanation** |
| `jlpt_level` | NVARCHAR(5) | NOT NULL | CHECK ∈ {N5,N4,N3,N2,N1} | **jlpt_level** |
| `example_sentence_jp` | NVARCHAR(MAX) | NULL → *bắt buộc ≥1 theo NV* | Câu ví dụ tiếng Nhật | **example** |
| `example_sentence_vi` | NVARCHAR(MAX) | NULL | Dịch ví dụ | example (vi) |
| `lesson_id` | BIGINT | NULL | FK → `lessons.lesson_id` | liên kết lesson |
| `status` | NVARCHAR(20) | NOT NULL DEFAULT 'draft' | CHECK ∈ {draft,pending_review,rejected,published,archived,deleted} | status |
| `created_by` | BIGINT | NULL | FK → `staff_users.staff_id` | tác giả |
| `approved_by` | BIGINT | NULL | FK → `staff_users.staff_id` (UC duyệt) | — |
| `published_at` | DATETIME2 | NULL | Set khi publish (UC khác) | — |
| `created_at` / `updated_at` | DATETIME2 | NOT NULL | DEFAULT SYSUTCDATETIME() | audit |

> **Lưu ý NV:** `usage_explanation` và `example_sentence_jp` ở DB là NULL-able, nhưng Rule #2 yêu cầu bắt buộc → **enforce ở Service layer** (không sửa schema, tuân `ADR-004`/migration policy).

### 5.2. Quan hệ

```
staff_users (1) ──< created_by ──  grammar_points  ── lesson_id >── (0..1) lessons
staff_users (1) ──< approved_by ── grammar_points
lessons (1) ── jlpt_level phải khớp ── grammar_points.jlpt_level  (enforce service)
```

### 5.3. Bảng liên quan (read-only trong UC-25)

- `lessons` — kiểm tra tồn tại + khớp `jlpt_level` khi liên kết.
- `staff_users` — nguồn `created_by`, kiểm tra `status='active'`.
- ~~`courses`~~ — **không tồn tại** trong schema (xem §9).

---

## 6. API Spec

> Prefix theo `AGENTS.md §3.3`. Mọi response bọc `{ status, message, data }`. Auth: `Bearer <JWT>` (role staff).

### 6.1. POST `/api/staff/grammar` — Tạo grammar (201)

**Request (CreateGrammarRequest):**

```json
{
  "structure": "～たことがある",
  "formula": "Vた + ことがある",
  "meaning": "Đã từng làm việc gì",
  "usageExplanation": "Diễn tả kinh nghiệm trong quá khứ...",
  "jlptLevel": "N5",
  "exampleSentenceJp": "日本へ行ったことがある。",
  "exampleSentenceVi": "Tôi đã từng đến Nhật.",
  "lessonId": 12
}
```

**Response 201:**

```json
{ "status": 201, "message": "Grammar created",
  "data": { "grammarId": 101, "status": "draft", "jlptLevel": "N5", "createdBy": 7, "createdAt": "2026-06-12T03:00:00Z" } }
```

### 6.2. GET `/api/staff/grammar` — Danh sách của Staff (200)

**Query:** `?page=0&size=20&jlptLevel=N5&status=draft`
**Response 200:** `data: { content: [GrammarSummaryResponse...], page, size, totalElements, totalPages }`
Chỉ trả grammar `created_by = caller`, loại `deleted`.

### 6.3. GET `/api/staff/grammar/{grammarId}` — Chi tiết (200)

**Response 200:** `data: GrammarDetailResponse` (toàn bộ field + `lesson` rút gọn). 404 nếu không tồn tại/deleted/không thuộc quyền.

### 6.4. PUT `/api/staff/grammar/{grammarId}` — Cập nhật (200)

**Request (UpdateGrammarRequest):** giống Create nhưng **không** có `status`. Chỉ cho phép khi `status ∈ {draft, rejected}` (FR-13/14/15).
**Response 200:** `data: GrammarDetailResponse` (đã cập nhật, `updatedAt` mới).

### 6.5. POST `/api/staff/contents/submit-review` — Gửi duyệt (200)

**Request (SubmitReviewRequest):**

```json
{ "contentType": "GRAMMAR", "contentId": 101 }
```

**Response 200:**

```json
{ "status": 200, "message": "Submitted for review",
  "data": { "grammarId": 101, "status": "pending_review" } }
```

### 6.6. Bảng tóm tắt

| Method | Path | Mục đích | Success | Auth |
|--------|------|----------|---------|------|
| POST | `/api/staff/grammar` | Tạo (draft) | 201 | staff |
| GET | `/api/staff/grammar` | List của mình | 200 | staff |
| GET | `/api/staff/grammar/{grammarId}` | Chi tiết | 200 | staff |
| PUT | `/api/staff/grammar/{grammarId}` | Sửa (draft/rejected) | 200 | staff |
| POST | `/api/staff/contents/submit-review` | Gửi duyệt | 200 | staff |

---

## 7. Error Handling

Xử lý tập trung qua `@RestControllerAdvice` (`ADR-008`). Format lỗi: `{ status, message, data:{ field?, error? } }`.

| Mã | HTTP | Tình huống | FR liên quan |
|----|------|-----------|--------------|
| ERR-AUTH-401 | 401 | Thiếu/sai/hết hạn JWT | NFR-01 |
| ERR-AUTH-403 | 403 | Sai role, hoặc sửa grammar không thuộc `created_by` | FR-17, NFR-01 |
| ERR-VAL-400 | 400 | Thiếu trường bắt buộc (`structure`/`meaning`/`usageExplanation`/`exampleSentenceJp`/`jlptLevel`) | FR-03, FR-05 |
| ERR-LEVEL-400 | 400 | `jlptLevel` ∉ {N5..N1} | FR-04 |
| ERR-NF-404 | 404 | `grammarId` không tồn tại / đã `deleted` | FR-12 |
| ERR-LESSON-404 | 404 | `lessonId` không tồn tại / `deleted` | FR-07 |
| ERR-LEVEL-MISMATCH-422 | 422 | `lesson.jlpt_level` ≠ `grammar.jlpt_level` | FR-08 |
| ERR-STATE-EDIT-422 | 422 | Sửa khi `status='published'` (cần version mới) | FR-14 |
| ERR-STATE-EDIT2-422 | 422 | Sửa khi `pending_review`/`archived` | FR-15 |
| ERR-STATE-SUBMIT-422 | 422 | Submit khi không ở `draft`/`rejected` | FR-19 |
| ERR-SUBMIT-INCOMPLETE-422 | 422 | Submit nội dung thiếu trường bắt buộc | FR-20 |
| ERR-FORBIDDEN-PUBLISH-403 | 403 | Staff cố đặt `status='published'` | FR-21 |
| ERR-SYS-500 | 500 | Lỗi không lường trước (log full) | NFR-08 |

---

## 8. Acceptance Criteria

> Định dạng Gherkin (Given/When/Then), ánh xạ FR. Là cơ sở cho Integration tests (`CONSTITUTION.md §5` — 100% coverage endpoint).

- **AC-01 (FR-01/02/03):** *Given* Staff đăng nhập với payload đầy đủ hợp lệ, *When* POST `/api/staff/grammar`, *Then* trả 201, `status='draft'`, `created_by`=staff hiện tại, bỏ qua `status` client gửi.
- **AC-02 (FR-04):** *Given* `jlptLevel="N6"`, *When* POST, *Then* 400 `ERR-LEVEL-400`, không tạo bản ghi.
- **AC-03 (FR-05):** *Given* thiếu `meaning`, *When* POST, *Then* 400 liệt kê field lỗi.
- **AC-04 (FR-06/08):** *Given* `lessonId` hợp lệ cùng cấp độ, *When* POST, *Then* 201 và `lesson_id` được gán; *Given* lesson khác cấp độ, *Then* 422.
- **AC-05 (FR-09/11):** *Given* Staff A có 3 grammar (1 deleted), *When* GET list, *Then* trả 2 bản ghi (không gồm deleted), không gồm grammar của Staff B.
- **AC-06 (FR-12):** *Given* `grammarId` không tồn tại, *When* GET chi tiết, *Then* 404.
- **AC-07 (FR-13):** *Given* grammar `status='draft'`, *When* PUT cập nhật `meaning`, *Then* 200, `updated_at` thay đổi.
- **AC-08 (FR-14):** *Given* grammar `status='published'`, *When* PUT, *Then* 422 `ERR-STATE-EDIT-422`.
- **AC-09 (FR-15):** *Given* grammar `status='pending_review'`, *When* PUT, *Then* 422.
- **AC-10 (FR-16):** *Given* PUT body chứa `"status":"published"`, *When* PUT, *Then* trường status bị bỏ qua, trạng thái không đổi.
- **AC-11 (FR-17):** *Given* Staff B sửa grammar của Staff A, *When* PUT, *Then* 403.
- **AC-12 (FR-18):** *Given* grammar `draft` đầy đủ trường, *When* submit-review, *Then* 200, `status='pending_review'`.
- **AC-13 (FR-19):** *Given* grammar `published`, *When* submit-review, *Then* 422.
- **AC-14 (FR-20):** *Given* grammar `draft` thiếu `exampleSentenceJp`, *When* submit-review, *Then* 422.
- **AC-15 (FR-21):** Không tồn tại bất kỳ endpoint/field nào cho phép Staff đặt `published` (kiểm tra contract + security test).
- **AC-16 (NFR-01):** *Given* request không JWT, *When* gọi bất kỳ endpoint, *Then* 401; *Given* role student, *Then* 403.
- **AC-17 (NFR-07):** Mọi response là DTO, không lộ Entity/field nội bộ (vd: không trả password_hash của creator).

---

## 9. Out of Scope

1. **Liên kết với `courses`** — schema hiện tại **không có** bảng `courses`; chỉ hỗ trợ liên kết `lesson_id`. Nếu cần "course" → yêu cầu thay đổi schema qua migration (ngoài UC-25, cần `CONSTITUTION.md §7.3 vote`).
2. **Duyệt / từ chối / publish** grammar (chuyển `pending_review → published/rejected`, set `approved_by`, `published_at`) — thuộc UC của **Staff Manager/Admin**.
3. **Versioning nội dung** khi sửa bản `published` (tạo phiên bản mới) — chỉ nêu ràng buộc chặn (FR-14), cơ chế tạo version là UC riêng.
4. **Soft delete grammar** (`status='deleted'`) — UC quản lý xóa nội dung riêng.
5. **Hiển thị grammar cho Student** (đọc nội dung `published` theo cấp độ/subscription) — module Student.
6. **Bulk import / export**, đính kèm media (audio/video) cho grammar.
7. **Audit log của Admin** (`admin_audit_logs`) — UC-25 chỉ ghi application log (FR-22), không ghi bảng audit Admin.
