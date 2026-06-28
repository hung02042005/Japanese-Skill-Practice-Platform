# PLAN — Manage Quiz (`feat-content-management`) — UC-26

> **UC Coverage:** UC-26 (Manage Quiz)
> **Trọng tâm:** Tạo / sửa metadata / gán câu hỏi / gửi duyệt bài trắc nghiệm (`assessments` type=quiz + `question_assignments`) — vai trò Staff
> **Nguồn:** `SPEC.md` §3.2, `UC-26-manage-quiz.md` | **Cập nhật:** 2026-06-12
> **Lưu ý:** Cùng bảng `assessments` với UC-28 (exam), phân biệt `assessment_type`. Khác UC-28: `section_name` **không** bắt buộc cho quiz.

---

## 1. Mục tiêu (Goals)

Cho phép Staff tạo quiz (`assessment_type='quiz'`, `draft`), sửa metadata khi chưa xuất bản, **gán câu hỏi đã published** kèm `display_order`/`score`, và gửi duyệt — bảo đảm **Σscore = total_score** và **khóa danh sách câu hỏi khi published**.

## 2. Kiến trúc & Công nghệ

Như `PLAN.md` §2. Bảng: `assessments` (type=quiz), `question_assignments`; đọc thêm `questions` (chỉ `published`), `lessons`, `staff_users`.

## 3. Quyết định thiết kế chính (Design Decisions)

- **Bất biến tổng điểm là cổng gửi duyệt (DD-06):** Σ`question_assignments.score` phải `== total_score` và ≥1 câu trước `submit-review`; lệch ⇒ 422 `SCORE_MISMATCH`, rỗng ⇒ 422 `EMPTY_QUIZ` (FR-26-26/28). Detail trả `assignedScoreSum`/`scoreMatched` để UI hiển thị (FR-26-14).
- **Assign replace-semantics + atomic (DD-05):** payload là tập đầy đủ; `deleteByParent` rồi `saveAll` trong **một** `@Transactional`; 1 item lỗi ⇒ rollback toàn batch (FR-26-23, NFR-26-04). Mỗi câu phải tồn tại + `published` (404/422), không trùng (409 `DUPLICATE_ASSIGNMENT` qua UNIQUE) (FR-26-19..22).
- **Khóa khi published (DD-02):** assign/update chỉ khi `status∈{draft,rejected}`; published ⇒ 409 `ASSESSMENT_PUBLISHED` (assign) / `INVALID_STATUS_TRANSITION` (update) (FR-26-16/24/25, LESSON-005).
- **Ép type + không đổi type:** `POST` ép `assessment_type='quiz'`; `PUT` không cho đổi type (FR-26-07/17). Cấm publish ⇒ 403 `PUBLISH_NOT_ALLOWED` (FR-26-30).
- **Validate metadata:** cần `title`/`jlpt_level`/`duration_min`/`pass_score`/`total_score`; ≥1 trong `lesson_id`/`topic`; range `duration_min>0`, `total_score>0`, `0≤pass_score≤total_score`; lesson tồn tại (FR-26-02..06).
- **Ownership (FR-26-31):** chỉ chủ sở hữu (hoặc STAFF_MANAGER) mới create/update/assign/submit.

## 4. Các thành phần Backend

### 4.1. Database Migration

- Index `IX_assessments_filter (assessment_type, status, jlpt_level, lesson_id)` + `IX_assign_parent (parent_type, parent_id)` (NFR-26-01/03). KHÔNG sửa cột.

### 4.2. Entities

- `Assessment`, `QuestionAssignment`; `Question`/`Lesson` read-only. Enum `AssessmentType`.

### 4.3. Repositories

- `AssessmentRepository.findByTypeWithFilters("quiz", level, status, lessonId, Pageable)`, `sumAssignedScore(id)`.
- `QuestionAssignmentRepository.deleteByParent`, `saveAll`, `findByParentOrderByDisplayOrder`.

### 4.4. DTOs

- Request: `CreateAssessmentRequest`/`UpdateAssessmentRequest`, `AssignQuestionsRequest` (item: `questionId`, `displayOrder`, `score`, `sectionName?`), `SubmitReviewRequest`.
- Response: `AssessmentSummaryResponse`, `AssessmentDetailResponse` (+`assignedScoreSum`/`scoreMatched`/`questions[]`), `AssignResultResponse`.

### 4.5. Services

- `AssessmentService` (`@Transactional`): `createQuiz`, `list`, `getDetail`, `updateMetadata`, `assignQuestions` (replace+atomic). Submit-review qua `ContentSubmissionService` (`contentType=assessment`) với gate điểm.
- Exceptions: `ScoreMismatchException`(422), `EmptyAssessmentException`(422), `DuplicateAssignmentException`(409), `AssessmentPublishedException`(409), `QuestionNotPublishedException`(422), `QuestionNotFoundException`(404), `InvalidStatusTransitionException`(409), `LessonNotFoundException`(404), `PublishNotAllowedException`(403), `OwnershipDeniedException`(403).

### 4.6. Controllers & Security

- `StaffAssessmentController` (`/api/staff/assessments`): `POST`, `GET` (`?type=quiz`), `GET /{id}`, `PUT /{id}`, `POST /{id}/assign-questions`.
- `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")`; map lỗi theo UC-26 §7.

## 5. Các thành phần Frontend

- `AssessmentBuilderPage` (quiz): gán câu hỏi kéo-thả `displayOrder`, hiển thị `assignedScoreSum`/`scoreMatched` realtime; "Gửi duyệt" disabled khi chưa khớp/rỗng (UX).

## 6. Definition of Done

- Σscore=total gate (`SCORE_MISMATCH`/`EMPTY_QUIZ`); assign atomic + chống trùng; khóa khi published.
- Ép type; cấm publish; ownership; controller chỉ DTO; coverage ≥ 80% (`feat-testing/TC-UC-26`).
