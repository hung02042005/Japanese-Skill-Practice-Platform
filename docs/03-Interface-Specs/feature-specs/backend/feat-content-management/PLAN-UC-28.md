# PLAN — Manage JLPT Mock Exams (`feat-content-management`) — UC-28

> **UC Coverage:** UC-28 (Manage JLPT Mock Exams)
> **Trọng tâm:** Tạo / sửa metadata / chia section + gán câu hỏi / gửi duyệt đề thi thử (`assessments` type=exam + `question_assignments`) — vai trò Staff
> **Nguồn:** `SPEC.md` §3.2, `UC-28-manage-jlpt-mock-exams.md` | **Cập nhật:** 2026-06-12
> **Lưu ý:** "Section" không phải bảng riêng — mô hình hóa qua `question_assignments.section_name`. Khác UC-26 (quiz): `section_name` **bắt buộc** cho exam; câu hỏi phải **cùng `jlpt_level`** với đề.

---

## 1. Mục tiêu (Goals)

Cho phép Staff tạo đề thi (`assessment_type='exam'`, `draft`), chia đề thành section qua `section_name`, **gán câu hỏi đã published cùng cấp độ**, và gửi duyệt — bảo đảm **Σscore = total_score** và **khóa danh sách câu hỏi khi published**.

## 2. Kiến trúc & Công nghệ

Như `PLAN.md` §2 + UC-26 (cùng `AssessmentService`). Bảng: `assessments` (type=exam), `question_assignments`; đọc thêm `questions` (chỉ `published`, cùng level), `staff_users`.

## 3. Quyết định thiết kế chính (Design Decisions)

- **Section qua `section_name` (DD-04):** không tạo bảng `sections`; "section" = nhóm `question_assignments` cùng `section_name∈{vocabulary,grammar,kanji,reading,listening}` (400 `INVALID_SECTION`). `section_name` **bắt buộc** mỗi item (400 `VALIDATION_FAILED`) (FR-28-21/22). Detail gom theo section + `sectionScore` (FR-28-13/14).
- **Bất biến tổng điểm là cổng gửi duyệt (DD-06):** Σscore `== total_score` + ≥1 câu trước submit; lệch ⇒ 422 `SCORE_MISMATCH`, rỗng ⇒ 422 `EMPTY_EXAM` (FR-28-30/31).
- **Khớp cấp độ JLPT (FR-28-25):** mỗi câu gán phải `jlpt_level == exam.jlpt_level`; lệch ⇒ 422 `LEVEL_MISMATCH` (chống lẫn cấp độ — AGENTS §5#5).
- **Assign replace-semantics + atomic (DD-05):** `deleteByParent` + `saveAll` trong **một** `@Transactional`; câu tồn tại + `published` (404/422), không trùng (409 `DUPLICATE_ASSIGNMENT`); 1 item lỗi ⇒ rollback toàn batch (FR-28-20..29).
- **Khóa khi published + ép type:** assign/update chỉ `{draft,rejected}`; published ⇒ 409 `ASSESSMENT_PUBLISHED`/`INVALID_STATUS_TRANSITION` (FR-28-17/28/29); `POST` ép `exam`, không đổi type; cấm publish ⇒ 403 (FR-28-05/18/33).
- **Validate metadata (FR-28-02..04):** `title`/`jlpt_level`/`duration_min`/`pass_score`/`total_score`; range `duration_min>0`, `total_score>0`, `0≤pass_score≤total_score`.

## 4. Các thành phần Backend

### 4.1. Database Migration

- Index `IX_assessments_filter (assessment_type, status, jlpt_level)` + `IX_assign_parent (parent_type, parent_id)` (NFR-28-01/03). KHÔNG sửa cột; `section_name` NULL-able ở DB nhưng bắt buộc với exam ⇒ enforce ở Service.

### 4.2. Entities

- `Assessment`, `QuestionAssignment` (+`section_name`); `Question` read-only. Enum `SectionName`, `AssessmentType`.

### 4.3. Repositories

- `AssessmentRepository.findByTypeWithFilters("exam", level, status, Pageable)`, `sumAssignedScore(id)`.
- `QuestionAssignmentRepository.deleteByParent`, `saveAll`, `findByParentGroupedBySection(...)`.

### 4.4. DTOs

- Request: `CreateAssessmentRequest`/`UpdateAssessmentRequest`, `AssignQuestionsRequest` (item: `questionId`, `sectionName` bắt buộc, `displayOrder`, `score`), `SubmitReviewRequest`.
- Response: `AssessmentSummaryResponse`, `AssessmentDetailResponse` (+`sections[]` gom theo `sectionName` + `sectionScore` + `assignedScoreSum`/`scoreMatched`), `AssignResultResponse` (+`sectionSummary[]`).

### 4.5. Services

- `AssessmentService` (`@Transactional`): `createExam`, `list`, `getDetail` (gom section), `updateMetadata`, `assignQuestions` (replace+atomic+level check). Submit-review qua `ContentSubmissionService`.
- Exceptions: `InvalidSectionException`(400), `LevelMismatchException`(422), `ScoreMismatchException`(422), `EmptyAssessmentException`(422), `DuplicateAssignmentException`(409), `AssessmentPublishedException`(409), `QuestionNotPublishedException`(422), `QuestionNotFoundException`(404), `InvalidStatusTransitionException`(409), `PublishNotAllowedException`(403), `OwnershipDeniedException`(403).

### 4.6. Controllers & Security

- `StaffAssessmentController` (dùng chung với UC-26): `POST`, `GET` (`?type=exam`), `GET /{id}`, `PUT /{id}`, `POST /{id}/assign-questions`.
- `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")`; map lỗi theo UC-28 §7.

## 5. Các thành phần Frontend

- `AssessmentBuilderPage` (exam): gán câu hỏi kèm `section_name`, gom nhóm section, hiển thị `sectionScore`/`assignedScoreSum`/`scoreMatched`; "Gửi duyệt" disabled khi chưa khớp/rỗng (UX).

## 6. Definition of Done

- `section_name` bắt buộc + hợp lệ; level mismatch chặn (422); Σscore=total gate (`SCORE_MISMATCH`/`EMPTY_EXAM`).
- Assign atomic + chống trùng; khóa khi published; ép type; cấm publish; ownership.
- DTO; SLF4J; soft delete; coverage ≥ 80% (`feat-testing/TC-UC-28`).
