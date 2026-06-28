# PLAN — Content & Question Bank Management (`feat-content-management`)

> **UC Coverage:** UC-24 (Manage Question Bank), UC-25 (Manage Grammar Content), UC-26 (Manage Quiz), UC-27 (Manage Learning Content), UC-28 (Manage JLPT Mock Exams)
> **Actor:** Staff (`staff_role = 'staff'` / kế thừa bởi `staff_manager`)
> **Nguồn:** `SPEC.md`, `UC-24..UC-28` | **Cập nhật:** 2026-06-12
> **Tham chiếu:** `CONSTITUTION.md`, `AGENTS.md §5/§6/§7`, `CLAUDE.md` (ADR-004/005/006/008, LESSON-001/002/005)
>
> **📌 Tài liệu này là OVERVIEW chung** (hạ tầng dùng chung cho cả 5 UC). PLAN chi tiết từng UC ở: `PLAN-UC-24.md`, `PLAN-UC-25.md`, `PLAN-UC-26.md`, `PLAN-UC-27.md`, `PLAN-UC-28.md`. Ma trận truy vết FR→AC→TC ở `TRACEABILITY.md`.

---

## 1. Mục tiêu (Goals)

Triển khai bộ công cụ **soạn thảo nội dung học liệu và đánh giá** cho vai trò **Staff** theo 5 UC. Hệ thống phải cho phép Staff **tạo / xem / sửa / gửi duyệt** các thực thể nội dung ở trạng thái nháp (`draft`), gán câu hỏi vào quiz/exam, và đẩy nội dung sang hàng đợi `pending_review` — đồng thời thực thi nghiêm ngặt:

- **Vòng đời trạng thái** `draft → pending_review → (UC-29) published`; Staff **không bao giờ** tự `publish`.
- **Khóa câu hỏi đã làm bài** (`is_locked` qua tồn tại trong `attempt_answers`) — UC-24, LESSON-005.
- **Bất biến tổng điểm** Σ`question_assignments.score` = `assessments.total_score` trước khi gửi duyệt — UC-26/UC-28.
- **Khóa danh sách câu hỏi** của assessment đã `published`.
- **Quyền sở hữu** (`created_by = caller`), DTO Pattern, soft delete, audit log SLF4J.

Phạm vi PLAN này **không** bao gồm luồng duyệt/publish (thuộc `feat-content-review` / UC-29/UC-33/UC-34).

## 2. Kiến trúc & Công nghệ

- **Backend:** Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA.
- **Database:** SQL Server; Migration bằng Flyway/Liquibase (**chỉ thêm index**, KHÔNG đổi schema cột đã tồn tại — tuân ADR-004/migration policy).
- **Frontend:** React 18, Tailwind CSS, Context API (UI Staff tách riêng khỏi StaffManager/Admin — LESSON-001).
- **Tuân thủ kiến trúc:** Controller → Service → Repository → Entity; DTO Pattern bắt buộc (ADR-005); `@Transactional` ở Service Layer; business logic 100% ở backend (Constitution §2.5); method ≤ 40 dòng, file ≤ 300 dòng.

## 3. Quyết định thiết kế chính (Design Decisions)

- **DD-01 — Validate trạng thái mặc định ở Service, bỏ qua giá trị client:** Mọi `create` ép `status='draft'`, gán `created_by = currentStaffId`, set timestamp server; **bỏ qua** mọi `status`/`created_by`/`approved_by`/`published_at` client gửi (FR-24-01, FR-25-02, FR-26-07, FR-27-01, FR-28-05). Đây là chốt chặn chống "Staff tự publish".

- **DD-02 — Guard cập nhật theo state machine:** Update/assign chỉ hợp lệ khi `status ∈ {draft, rejected}`; trạng thái khác → `409 INVALID_STATUS_TRANSITION`. Kiểm tra ở Service Layer, không phụ thuộc UI (FR-24-18, FR-26-16/25, FR-27-04, FR-28-17/29).

- **DD-03 — Khóa câu hỏi đã làm bài (UC-24):** `isLocked` = tồn tại bản ghi `attempt_answers.question_id`. Kiểm tra trong cùng transaction với UPDATE; nếu khóa → `409 QUESTION_LOCKED`, KHÔNG sửa (FR-24-17, NFR-24-03, LESSON-005). Versioning là Phase 2 — UC chỉ chặn.

- **DD-04 — `assessments` đa hình (quiz vs exam):** Quiz (UC-26) và Exam (UC-28) dùng chung bảng `assessments` phân biệt bằng `assessment_type`. Endpoint `POST /api/staff/assessments` ép `assessment_type` theo payload nhưng **không cho đổi type** khi update. Khác biệt chính: `section_name` **bắt buộc** với exam, không bắt buộc với quiz (FR-28-22 vs UC-26).

- **DD-05 — Gán câu hỏi: replace semantics + atomic:** `assign-questions` coi payload là **tập đầy đủ mong muốn**: xóa assignment cũ, ghi assignment mới trong **một** `@Transactional`; vi phạm bất kỳ item nào ⇒ rollback toàn batch (FR-26-23, FR-28-27, NFR-26-04/28-04). Chống trùng qua UNIQUE `(parent_type, parent_id, question_id)` (FR-26-22, FR-28-26).

- **DD-06 — Bất biến tổng điểm là cổng gửi duyệt:** Σ`score` phải bằng `total_score` (và ≥ 1 câu hỏi; với exam mỗi câu cùng `jlpt_level`) trước khi `submit-review`; lệch ⇒ `422 SCORE_MISMATCH` / `EMPTY_QUIZ` / `EMPTY_EXAM` / `LEVEL_MISMATCH` (FR-26-26/28, FR-28-30/31/25).

- **DD-07 — Endpoint gửi duyệt thống nhất + Resolver đa hình:** Một endpoint `POST /api/staff/contents/submit-review` nhận `contentType ∈ {question, grammar, lesson, vocabulary, kanji, assessment}`. Dùng **`ContentSubmissionResolver`** map `contentType` → repository + chiến lược validate/transition (tránh `if/else` rải rác và God Class — như `ReviewableContentResolver` ở feat-content-review).

- **DD-08 — Media chỉ lưu URL:** audio/image/video/attachment lưu dạURL string; KHÔNG BLOB (ADR-006, LESSON-002, FR-27-03). URL phải qua module upload đã validate extension/size (Constitution §3.3) — ngoài phạm vi các endpoint này.

- **DD-09 — Soft delete + Audit:** Xóa = `status='deleted'` (ADR-004); mọi thao tác ghi đều log SLF4J `[INFO] Staff {staffId} {action} {contentType} {contentId}` (FR-24-25, FR-26-32, FR-27-08, FR-28-35). UC-25 chỉ ghi application log (không ghi `admin_audit_logs`).

## 4. Các thành phần Backend

### 4.1. Database Migration

- File migration **chỉ bổ sung index** phục vụ tìm kiếm/lọc (NFR-24-01, NFR-26-01, NFR-28-01):
  - `IX_questions_filter (status, jlpt_level, skill, question_type)`.
  - `IX_assessments_filter (assessment_type, status, jlpt_level, lesson_id)`.
  - `IX_assign_parent (parent_type, parent_id)` — tổng hợp điểm theo assessment.
  - `grammar_points`, `lessons`, `vocabulary`, `kanji`: tận dụng index có sẵn (`IX_grammar_public_level`, `IX_lessons_*`, `IX_vocab_*`, `IX_kanji_public_level`); bổ sung index `created_by, status` nếu thiếu cho list "của tôi".
- KHÔNG sửa cột có sẵn; ràng buộc bắt buộc của Rule (vd `usage_explanation`, `section_name`) enforce ở **Service Layer** vì cột DB là NULL-able.

### 4.2. Entities (tận dụng có sẵn — chỉ map, không đổi DB)

- `Question`, `GrammarPoint`, `Lesson`, `Vocabulary`, `Kanji`, `Assessment`, `QuestionAssignment`, `StaffUser`, `AttemptAnswer` (read-only — nguồn khóa).
- Enum dùng chung: `ContentStatus { DRAFT, PENDING_REVIEW, REJECTED, PUBLISHED, ARCHIVED, DELETED }`, `ContentType { QUESTION, GRAMMAR, LESSON, VOCABULARY, KANJI, ASSESSMENT }`, `AssessmentType { QUIZ, EXAM }`, `QuestionType`, `Skill`, `JlptLevel`, `SectionName`.

### 4.3. Repositories

- `QuestionRepository`: `findByStatusAndFilters(...)` (q/skill/level/type/status, paged, loại `deleted`); `existsAttemptAnswerByQuestionId(id)` (khóa); guarded update theo status.
- `AssessmentRepository`: `findByTypeAndFilters(type, level, status, lessonId, paged)`; `sumAssignedScore(assessmentId)`.
- `QuestionAssignmentRepository`: `deleteByParent(parentType, parentId)`, `saveAll(...)`, `findByParentOrderByDisplayOrder(...)`.
- `GrammarPointRepository`, `LessonRepository`, `VocabularyRepository`, `KanjiRepository`: `findByCreatedByAndFilters(...)`, `existsByCharacterValue(...)` (kanji, FR-27-21).

### 4.4. DTOs (Request/Response — không lộ Entity)

- **Request:** `CreateQuestionRequest`/`UpdateQuestionRequest`, `CreateGrammarRequest`/`UpdateGrammarRequest`, `CreateLessonRequest`/`UpdateLessonRequest`, `CreateVocabularyRequest`, `CreateKanjiRequest`, `CreateAssessmentRequest`/`UpdateAssessmentRequest`, `AssignQuestionsRequest` (list item: `questionId, sectionName?, displayOrder, score`), `SubmitReviewRequest` (`contentType, contentId`).
- **Response:** `QuestionSummaryResponse`/`QuestionDetailResponse` (kèm `isLocked`), `GrammarDetailResponse`, `LessonDetailResponse`, `VocabularyDetailResponse`, `KanjiDetailResponse`, `AssessmentSummaryResponse`/`AssessmentDetailResponse` (kèm `assignedScoreSum`, `scoreMatched`, `sections[]`/`questions[]`), `AssignResultResponse`, `SubmitReviewResponse`, `PageResponse<T>`.
- **Validation:** `@Valid` + Jakarta annotations; validator nghiệp vụ theo `question_type`/`lesson_type`/`section_name`, range điểm, enum JLPT.

### 4.5. Services (Business Logic — `@Transactional`)

- **`QuestionService`** — create/list/detail/update/lock-guard (UC-24); `assertNotLocked(...)`.
- **`GrammarService`** — create/list/detail/update + liên kết lesson (khớp `jlpt_level`) (UC-25).
- **`LearningContentService`** — lesson/vocabulary/kanji create/update + ràng buộc theo type (UC-27); kiểm tra trùng kanji.
- **`AssessmentService`** — create/list/detail/update quiz & exam; `assignQuestions(...)` (replace + atomic); tính `assignedScoreSum`/`scoreMatched` (UC-26/UC-28).
- **`ContentSubmissionService`** — `submitForReview(req, staffId)`: resolve contentType → re-validate bắt buộc + cổng điểm (assessment) → transition `draft/rejected → pending_review`.
- **`ContentSubmissionResolver`** — map `contentType` → repository + chiến lược validate/transition (tránh God Class).
- **`OwnershipGuard`** — `assertOwner(content.createdBy, currentStaffId, role)`; bỏ qua khi `STAFF_MANAGER`.
- **Custom exceptions:** `QuestionLockedException`(409), `InvalidStatusTransitionException`(409), `ScoreMismatchException`(422), `DuplicateAssignmentException`(409), `AssessmentPublishedException`(409), `QuestionNotPublishedException`(422), `LevelMismatchException`(422), `KanjiDuplicateException`(409), `PublishNotAllowedException`(403), `ContentNotFoundException`(404), `OwnershipDeniedException`(403).

### 4.6. Controllers & Security

- **`StaffQuestionController`** (`/api/staff/questions`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`.
- **`StaffGrammarController`** (`/api/staff/grammar`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`.
- **`StaffLearningContentController`** (`/api/staff/lessons`, `/vocabulary`, `/kanji`): create/update.
- **`StaffAssessmentController`** (`/api/staff/assessments`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `POST /{id}/assign-questions`.
- **`StaffContentSubmissionController`** (`/api/staff/contents/submit-review`): `POST`.
- **Security:** `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")` + SecurityFilterChain cho `/api/staff/**`; thiếu JWT → 401, sai role → 403.
- **`GlobalExceptionHandler`:** map mọi custom exception → `{ status, message, data }` đúng bảng §7 của từng UC (ADR-008).

## 5. Các thành phần Frontend

- `services/staffContentService` gom API gọi 5 nhóm endpoint (tránh Direct API in Component).
- `QuestionBankPage` (list + search/filter skill/level/type/status, badge `isLocked`), `QuestionEditorPage`.
- `GrammarEditorPage`, `LearningContentPage` (tab lesson/vocab/kanji).
- `AssessmentBuilderPage`: tạo quiz/exam, gán câu hỏi (kéo-thả `displayOrder`, gán `section_name` cho exam), hiển thị `assignedScoreSum`/`scoreMatched` realtime; nút "Gửi duyệt" disabled khi `scoreMatched=false`/rỗng (UX, không thay backend).
- Tách UI Staff khỏi StaffManager/Admin (LESSON-001); `ProtectedRoute` theo quyền `staff`; mọi trang có loading/error state.

## 6. Tiêu chuẩn đánh giá (Definition of Done)

- Staff **không** thể đặt `status=published` qua bất kỳ endpoint nào; có test chứng minh (`PUBLISH_NOT_ALLOWED`).
- Sửa câu hỏi đã làm bài bị chặn (`QUESTION_LOCKED`); kiểm tra trong cùng transaction.
- `assign-questions` nguyên tử (rollback toàn batch khi 1 item lỗi); chống trùng; replace semantics đúng.
- Bất biến điểm enforce trước `submit-review` (`SCORE_MISMATCH`/`EMPTY_*`/`LEVEL_MISMATCH`).
- Ownership guard chặn Staff khác (`FORBIDDEN`); update chỉ khi `draft`/`rejected`.
- Controller chỉ trả DTO (không lộ Entity); error đúng `{ status, message, data }`.
- Mọi action ghi SLF4J; soft delete; không `System.out.println`; không TODO.
- Unit test Service ≥ 80% coverage; Integration test 100% endpoint (happy + error). Xem `feat-testing/TC-UC-24..28`.
- Pass `mvn spotless:apply` & `npm run lint`; mỗi PR ≤ 400 dòng.
