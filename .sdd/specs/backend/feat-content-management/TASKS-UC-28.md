# TASKS — Manage JLPT Mock Exams (`feat-content-management`) — UC-28

> **UC:** UC-28 (Manage JLPT Mock Exams) | **Nguồn:** `SPEC.md` §3.2, `UC-28-manage-jlpt-mock-exams.md`, `PLAN-UC-28.md`
> **Cập nhật:** 2026-06-12 | **Lưu ý:** Cùng `AssessmentService`/Controller với UC-26; exam **bắt buộc** `section_name` + câu cùng cấp độ.

## Phase 1: Database & Domain
- [ ] 1.1 Migration thêm index `IX_assessments_filter (assessment_type, status, jlpt_level)`, `IX_assign_parent (parent_type, parent_id)` (NFR-28-01/03). KHÔNG sửa cột.
- [ ] 1.2 Entity `Assessment` + `QuestionAssignment` (+`section_name`); enum `SectionName`/`AssessmentType`.

## Phase 2: Repository Layer
- [ ] 2.1 `AssessmentRepository.findByTypeWithFilters("exam", level, status, Pageable)` — loại `deleted`, sort `updated_at` desc; 404 nếu không phải exam (FR-28-10..15).
- [ ] 2.2 `sumAssignedScore(id)` (FR-28-14/30).
- [ ] 2.3 `QuestionAssignmentRepository.deleteByParent`, `saveAll`, `findByParentGroupedBySection` (gom section — FR-28-13).

## Phase 3: DTO & Validation
- [ ] 3.1 `CreateAssessmentRequest`/`UpdateAssessmentRequest` (range điểm — FR-28-02..04).
- [ ] 3.2 `AssignQuestionsRequest` (item: `questionId`, `sectionName` **bắt buộc**, `displayOrder>=0`, `score>0`) (FR-28-20..22).
- [ ] 3.3 `AssessmentDetailResponse` (+`sections[]`/`sectionScore`/`scoreMatched`), `AssignResultResponse` (+`sectionSummary[]`) — không lộ Entity (NFR-28-05).

## Phase 4: Service Layer
- [ ] 4.1 `AssessmentService.createExam` — ép `assessment_type='exam'`, `draft`, `created_by`; bỏ qua status client (FR-28-01/05/06).
- [ ] 4.2 `list` + `getDetail` (gom theo `section_name`, sort `displayOrder`, `sectionScore` + `assignedScoreSum`/`scoreMatched`) (FR-28-10..15).
- [ ] 4.3 `updateMetadata` — `OwnershipGuard` → guard status `{draft,rejected}` (409) → không đổi type (FR-28-16..19/34).
- [ ] 4.4 `assignQuestions(id, req, staffId)` — guard published (409) + status; `sectionName` bắt buộc + hợp lệ (400 `INVALID_SECTION`); câu tồn tại (404) + `published` (422) + **cùng `jlpt_level`** (422 `LEVEL_MISMATCH`); chống trùng (409); replace trong **một** `@Transactional`, rollback toàn batch (FR-28-20..29).
- [ ] 4.5 Submit-review qua `ContentSubmissionService`: Σscore=total (422 `SCORE_MISMATCH`) + ≥1 câu (422 `EMPTY_EXAM`) + status `{draft,rejected}` → `pending_review` (FR-28-30..32).
- [ ] 4.6 Chặn publish (403, FR-28-33); SLF4J log; soft delete (FR-28-35/36).

## Phase 5: Controller & Security
- [ ] 5.1 `StaffAssessmentController` (dùng chung UC-26): POST, GET (`?type=exam`), GET /{id}, PUT /{id}, POST /{id}/assign-questions.
- [ ] 5.2 `@PreAuthorize(...)`; map 400/401/403/404/409/422 theo UC-28 §7.

## Phase 6: Testing & QA
- [ ] 6.1 Unit `AssessmentService` exam (section bắt buộc/hợp lệ, level mismatch, score invariant, assign atomic, duplicate, published-lock, ownership) — `feat-testing/TC-UC-28`.
- [ ] 6.2 Integration: gom section + `sectionScore`, atomic rollback khi level mismatch, UNIQUE assignment.
- [ ] 6.3 API/MockMvc: 201/400/401/403/409/422; DTO (không lộ password_hash creator).

## Phase 7: Frontend
- [ ] 7.1 `AssessmentBuilderPage` (exam): gán câu hỏi kèm `section_name`, gom section, hiển thị `sectionScore`/`scoreMatched`; disable "Gửi duyệt" khi chưa khớp/rỗng.

## Phase 8: Final Review
- [ ] 8.1 Cross-check `UC-28` §3 (FR-28-01..36) ↔ code; AC-28-01..22 có test phủ (`TRACEABILITY.md`).
- [ ] 8.2 Lint sạch; không TODO; PR ≤ 400 dòng.
