# TASKS — Content & Question Bank Management (`feat-content-management`)

> **UC:** UC-24, UC-25, UC-26, UC-27, UC-28 | **Nguồn:** `SPEC.md`, `UC-24..UC-28`, `PLAN.md`
> **Actor:** Staff | **Cập nhật:** 2026-06-12
>
> **📌 Tài liệu này là TASKS tổng hợp (8 Phase chung).** TASKS chi tiết theo từng UC ở: `TASKS-UC-24.md`, `TASKS-UC-25.md`, `TASKS-UC-26.md`, `TASKS-UC-27.md`, `TASKS-UC-28.md`.

## Phase 1: Database & Domain
- [ ] 1.1 Migration (Flyway/Liquibase) **chỉ thêm index**: `IX_questions_filter (status, jlpt_level, skill, question_type)`, `IX_assessments_filter (assessment_type, status, jlpt_level, lesson_id)`, `IX_assign_parent (parent_type, parent_id)`. KHÔNG sửa cột có sẵn (NFR-24-01/26-01/28-01).
- [ ] 1.2 Tạo enum dùng chung: `ContentStatus`, `ContentType`, `AssessmentType`, `QuestionType`, `Skill`, `JlptLevel`, `SectionName` — ánh xạ đúng ràng buộc `CHECK` của DB.
- [ ] 1.3 Rà soát Entity `Question`, `GrammarPoint`, `Lesson`, `Vocabulary`, `Kanji`, `Assessment`, `QuestionAssignment`, `AttemptAnswer`, `StaffUser` có map đủ cột workflow (`status`, `created_by`, `approved_by`, `published_at`, `created_at`, `updated_at`); bổ sung nếu thiếu (chỉ tầng Entity, không đổi DB).

## Phase 2: Repository Layer
- [ ] 2.1 `QuestionRepository`: `findWithFilters(q, skill, level, type, status, Pageable)` (loại `deleted` trừ khi yêu cầu); `existsAttemptAnswerByQuestionId(id)` (nguồn `isLocked` — FR-24-15/17).
- [ ] 2.2 `AssessmentRepository`: `findByTypeWithFilters(type, level, status, lessonId, Pageable)`; `sumAssignedScore(assessmentId)` (FR-26-14/28-14).
- [ ] 2.3 `QuestionAssignmentRepository`: `deleteByParent(parentType, parentId)`, `saveAll(...)`, `findByParentOrderByDisplayOrder(...)` (replace semantics — FR-26-23/28-27).
- [ ] 2.4 `GrammarPointRepository`, `LessonRepository`, `VocabularyRepository`, `KanjiRepository`: `findByCreatedByWithFilters(...)`; `KanjiRepository.existsByCharacterValue(...)` (FR-27-21).

## Phase 3: DTO & Validation
- [ ] 3.1 Request DTOs UC-24: `CreateQuestionRequest`, `UpdateQuestionRequest` (validator theo `questionType`: multiple_choice cần A/B/C/D + `correctOption`; fill_blank/true_false cần `correctAnswerText` — FR-24-06/07/08).
- [ ] 3.2 Request DTOs UC-25: `CreateGrammarRequest`, `UpdateGrammarRequest` (bắt buộc `structure`, `meaning`, `usageExplanation`, `exampleSentenceJp`, `jlptLevel` — FR-25-03).
- [ ] 3.3 Request DTOs UC-27: `CreateLessonRequest`/`UpdateLessonRequest` (≥1 nội dung; listening cần `audioUrl` — FR-27-11/12), `CreateVocabularyRequest` (FR-27-16), `CreateKanjiRequest` (≥1 onyomi/kunyomi — FR-27-20).
- [ ] 3.4 Request DTOs UC-26/28: `CreateAssessmentRequest`/`UpdateAssessmentRequest` (range: `durationMin>0`, `totalScore>0`, `0<=passScore<=totalScore`; quiz cần `lessonId` hoặc `topic`), `AssignQuestionsRequest` (item: `questionId`, `sectionName?`, `displayOrder>=0`, `score>0`; `sectionName` bắt buộc cho exam — FR-28-21/22), `SubmitReviewRequest`.
- [ ] 3.5 Response DTOs: `QuestionSummaryResponse`/`QuestionDetailResponse` (+`isLocked`), `Grammar/Lesson/Vocabulary/KanjiDetailResponse`, `AssessmentSummaryResponse`/`AssessmentDetailResponse` (+`assignedScoreSum`, `scoreMatched`, `sections[]`/`questions[]`), `AssignResultResponse`, `SubmitReviewResponse`, `PageResponse<T>` — KHÔNG lộ Entity (ADR-005).

## Phase 4: Service Layer (Business Logic)
- [ ] 4.1 `OwnershipGuard.assertOwner(createdBy, currentStaffId, role)` — bỏ qua khi `STAFF_MANAGER`; vi phạm → `OwnershipDeniedException` (FR-24-24/26-31/27-06/28-34).
- [ ] 4.2 `QuestionService`: create (ép `draft`, `created_by`; bỏ qua status client), list/detail (+`isLocked`), update (re-validate + `assertNotLocked` + guard status `{draft,rejected}`) — UC-24 FR-24-01..19.
- [ ] 4.3 `GrammarService`: create/list("của tôi")/detail/update; liên kết `lessonId` (tồn tại + khớp `jlpt_level`, else 404/422) — UC-25 FR-01..17.
- [ ] 4.4 `LearningContentService`: lesson/vocabulary/kanji create+update; ràng buộc nội dung theo type; trùng kanji → `KanjiDuplicateException`; media chỉ lưu URL (ADR-006) — UC-27 FR-27-09..24.
- [ ] 4.5 `AssessmentService.create/update`: ép `assessment_type` theo endpoint, không cho đổi type; guard status — UC-26 FR-26-01..18 / UC-28 FR-28-01..19.
- [ ] 4.6 `AssessmentService.assignQuestions(id, req, staffId)`: replace semantics trong **một** `@Transactional`; verify mỗi `questionId` tồn tại + `published` (+ exam: cùng `jlpt_level`); chống trùng; chặn khi `published`/status sai → rollback toàn batch (FR-26-19..25 / FR-28-20..29).
- [ ] 4.7 `ContentSubmissionResolver` + `ContentSubmissionService.submitForReview(req, staffId)`: resolve `contentType` → re-validate bắt buộc; với assessment kiểm tra Σscore=total_score, ≥1 câu, (exam: level khớp); transition `draft/rejected → pending_review` (FR-24-20..22 / FR-26-26..28 / FR-27-25..26 / FR-28-30..32).
- [ ] 4.8 Custom exceptions + SLF4J log mọi thao tác ghi `[INFO] Staff {staffId} {action} {contentType} {contentId}` (FR-24-25/26-32/27-08/28-35); soft delete (`status='deleted'`, ADR-004).

## Phase 5: Controller & Security
- [ ] 5.1 `StaffQuestionController` (`/api/staff/questions`): POST, GET (list/search/filter), GET /{id}, PUT /{id}.
- [ ] 5.2 `StaffGrammarController` (`/api/staff/grammar`): POST, GET, GET /{id}, PUT /{id}.
- [ ] 5.3 `StaffLearningContentController` (`/api/staff/lessons`, `/vocabulary`, `/kanji`): POST + PUT (lesson).
- [ ] 5.4 `StaffAssessmentController` (`/api/staff/assessments`): POST, GET (`?type=quiz|exam`), GET /{id}, PUT /{id}, POST /{id}/assign-questions.
- [ ] 5.5 `StaffContentSubmissionController` (`/api/staff/contents/submit-review`): POST.
- [ ] 5.6 `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")` + SecurityFilterChain cho `/api/staff/**` (NFR-*-02); thiếu JWT → 401, sai role → 403.
- [ ] 5.7 Cập nhật `GlobalExceptionHandler`: map 400/403/404/409/422/500 theo `{ status, message, data }` đúng §7 của UC-24/25/26/27/28 (ADR-008).

## Phase 6: Testing & QA (Backend)
- [ ] 6.1 Unit test `QuestionService` (create, validate theo type, lock-guard, status-guard, ownership) — xem `feat-testing/TC-UC-24`.
- [ ] 6.2 Unit test `GrammarService` + `LearningContentService` (bắt buộc trường, JLPT, kanji trùng, lesson-level mismatch) — `TC-UC-25`, `TC-UC-27`.
- [ ] 6.3 Unit test `AssessmentService` (score invariant, replace atomic, duplicate, published lock, level mismatch, empty) — `TC-UC-26`, `TC-UC-28`.
- [ ] 6.4 Unit test `ContentSubmissionService` (transition hợp lệ, chặn publish, gate điểm).
- [ ] 6.5 Integration test (Testcontainers SQL Server + Flyway): lock qua `attempt_answers`, UNIQUE assignment, sum score, soft-delete exclude.
- [ ] 6.6 API/Controller test (MockMvc + Spring Security): happy + error (400/401/403/404/409/422) cho mọi endpoint; bất biến không lộ Entity.
- [ ] 6.7 Coverage Service ≥ 80%; cross-check mọi AC (UC-24 §8 .. UC-28 §8) có test phủ.

## Phase 7: Frontend
- [ ] 7.1 `services/staffContentService` gom API 5 nhóm endpoint.
- [ ] 7.2 `QuestionBankPage` (list + search/filter, badge `isLocked`) + `QuestionEditorPage` (form theo `questionType`).
- [ ] 7.3 `GrammarEditorPage` + `LearningContentPage` (tab lesson/vocabulary/kanji, upload trả URL).
- [ ] 7.4 `AssessmentBuilderPage`: gán câu hỏi (order + `section_name` cho exam), hiển thị `assignedScoreSum`/`scoreMatched`; nút "Gửi duyệt" disabled khi chưa khớp/rỗng (UX).
- [ ] 7.5 Tách UI Staff khỏi StaffManager/Admin (LESSON-001); `ProtectedRoute` quyền `staff`; loading/error state.

## Phase 8: Final Review
- [ ] 8.1 Cross-check `UC-24..28` §3 (FR) ↔ code; xác nhận mọi Rule/AC có test phủ (coverage checklist trong từng `TC-UC-*`).
- [ ] 8.2 Lint sạch (`mvn spotless:apply`, `npm run lint`); không TODO; không `System.out.println`.
- [ ] 8.3 Code Review & Merge theo PR (≤ 400 dòng/PR; chia nhỏ theo UC nếu vượt).
