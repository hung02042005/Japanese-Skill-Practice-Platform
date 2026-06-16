# TASKS — Manage Quiz (`feat-content-management`) — UC-26

> **UC:** UC-26 (Manage Quiz) | **Nguồn:** `SPEC.md` §3.2, `UC-26-manage-quiz.md`, `PLAN-UC-26.md`
> **Cập nhật:** 2026-06-12 | **Lưu ý:** Cùng `AssessmentService` với UC-28; quiz **không** bắt buộc `section_name`.

## Phase 1: Database & Domain
- [ ] 1.1 Migration thêm index `IX_assessments_filter (assessment_type, status, jlpt_level, lesson_id)`, `IX_assign_parent (parent_type, parent_id)` (NFR-26-01/03). KHÔNG sửa cột.
- [ ] 1.2 Entity `Assessment` + `QuestionAssignment`; enum `AssessmentType`.

## Phase 2: Repository Layer
- [ ] 2.1 `AssessmentRepository.findByTypeWithFilters("quiz", level, status, lessonId, Pageable)` — loại `deleted`, sort `updated_at` desc (FR-26-10..12).
- [ ] 2.2 `sumAssignedScore(assessmentId)` (FR-26-14/26).
- [ ] 2.3 `QuestionAssignmentRepository.deleteByParent`, `saveAll`, `findByParentOrderByDisplayOrder` (replace — FR-26-23).

## Phase 3: DTO & Validation
- [ ] 3.1 `CreateAssessmentRequest`/`UpdateAssessmentRequest` (range điểm; ≥1 `lessonId`/`topic` — FR-26-02..05).
- [ ] 3.2 `AssignQuestionsRequest` (item: `questionId`, `displayOrder>=0`, `score>0`, `sectionName?`) (FR-26-19/20).
- [ ] 3.3 `AssessmentSummaryResponse`, `AssessmentDetailResponse` (+`assignedScoreSum`/`scoreMatched`/`questions[]`), `AssignResultResponse` — không lộ Entity (NFR-26-05).

## Phase 4: Service Layer
- [ ] 4.1 `AssessmentService.createQuiz` — ép `assessment_type='quiz'`, `draft`, `created_by`; verify `lessonId` tồn tại (404) (FR-26-01/06/07).
- [ ] 4.2 `list` + `getDetail` (questions sort `displayOrder` + `assignedScoreSum`/`scoreMatched`) (FR-26-10..14).
- [ ] 4.3 `updateMetadata` — `OwnershipGuard` → guard status `{draft,rejected}` (409) → không đổi type → refresh `updated_at` (FR-26-15..18/31).
- [ ] 4.4 `assignQuestions(id, req, staffId)` — guard published (409 `ASSESSMENT_PUBLISHED`) + status; verify mỗi `questionId` tồn tại (404) + `published` (422); chống trùng (409); replace trong **một** `@Transactional`, rollback toàn batch (FR-26-19..25).
- [ ] 4.5 Submit-review qua `ContentSubmissionService`: Σscore=total (422 `SCORE_MISMATCH`) + ≥1 câu (422 `EMPTY_QUIZ`) + status `{draft,rejected}` → `pending_review` (FR-26-26..28).
- [ ] 4.6 Chặn publish (403); SLF4J log; soft delete (FR-26-30/32/33).

## Phase 5: Controller & Security
- [ ] 5.1 `StaffAssessmentController`: POST, GET (`?type=quiz`), GET /{id}, PUT /{id}, POST /{id}/assign-questions.
- [ ] 5.2 `@PreAuthorize(...)`; map 400/401/403/404/409/422 theo UC-26 §7.

## Phase 6: Testing & QA
- [ ] 6.1 Unit `AssessmentService` quiz (score invariant, assign atomic, duplicate, published-lock, ownership) — `feat-testing/TC-UC-26`.
- [ ] 6.2 Integration: UNIQUE assignment, `sumAssignedScore`, atomic rollback, list type=quiz loại deleted.
- [ ] 6.3 API/MockMvc: 201/401/403/409/422; detail là DTO.

## Phase 7: Frontend
- [ ] 7.1 `AssessmentBuilderPage` (quiz): gán câu hỏi + `displayOrder`; hiển thị `scoreMatched`; disable "Gửi duyệt" khi chưa khớp/rỗng.

## Phase 8: Final Review
- [ ] 8.1 Cross-check `UC-26` §3 (FR-26-01..33) ↔ code; AC-26-01..16 có test phủ (`TRACEABILITY.md`).
- [ ] 8.2 Lint sạch; không TODO; PR ≤ 400 dòng.
