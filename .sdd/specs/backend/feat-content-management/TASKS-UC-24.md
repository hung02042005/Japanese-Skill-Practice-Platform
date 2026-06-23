# TASKS — Manage Question Bank (`feat-content-management`) — UC-24

> **UC:** UC-24 (Manage Question Bank) | **Nguồn:** `UC-24-manage-question-bank.md`, `PLAN-UC-24.md`
> **Cập nhật:** 2026-06-12 | **Lưu ý:** Hạ tầng dùng chung ở `TASKS.md` (Phase 1.2, 4.1, 4.7, 5.6/5.7).

## Phase 1: Database & Domain

- [ ] 1.1 Migration **chỉ thêm** index `IX_questions_filter (status, jlpt_level, skill, question_type)` (NFR-24-01). KHÔNG sửa cột.
- [ ] 1.2 Entity `Question` map đủ cột workflow; enum `QuestionType`/`Skill`/`JlptLevel` khớp `CHECK` DB.

## Phase 2: Repository Layer

- [ ] 2.1 `QuestionRepository.findWithFilters(q, skill, level, type, status, Pageable)` — LIKE `question_text`, AND các filter, loại `deleted` trừ khi yêu cầu, sort `updated_at` desc (FR-24-10..13).
- [ ] 2.2 `existsAttemptAnswerByQuestionId(id)` — nguồn `isLocked` (FR-24-15/17).

## Phase 3: DTO & Validation

- [ ] 3.1 `CreateQuestionRequest` + `UpdateQuestionRequest` (`@Valid`); custom validator theo `questionType`: multiple_choice cần A/B/C/D + `correctOption`; true_false/fill_blank cần `correctAnswerText` (FR-24-02..08).
- [ ] 3.2 `QuestionSummaryResponse`, `QuestionDetailResponse` (+`isLocked`), `PageResponse<T>` — không lộ Entity (ADR-005).

## Phase 4: Service Layer

- [ ] 4.1 `QuestionService.create(req, staffId)` — ép `draft`, `created_by`, bỏ qua status client; trả 201 (FR-24-01/09).
- [ ] 4.2 `QuestionService.list(filters, pageable)` + `getDetail(id)` (+`isLocked`); 404 nếu không tồn tại/deleted (FR-24-10..15).
- [ ] 4.3 `QuestionService.update(id, req, staffId)` — `OwnershipGuard` → guard status `{draft,rejected}` → `assertNotLocked` (409 `QUESTION_LOCKED`) → re-validate → refresh `updated_at` (FR-24-16..19/24).
- [ ] 4.4 Submit-review qua `ContentSubmissionService` (`contentType=question`): re-validate + transition `draft/rejected → pending_review`; chặn nếu status sai (FR-24-20..22).
- [ ] 4.5 Chặn `status=published` ở mọi endpoint → `PublishNotAllowedException` (403) (FR-24-23); SLF4J log `[INFO] Staff {id} {action} question {qid}` (FR-24-25).

## Phase 5: Controller & Security

- [ ] 5.1 `StaffQuestionController` (`/api/staff/questions`): POST, GET (search/filter), GET /{id}, PUT /{id}.
- [ ] 5.2 `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")`; map lỗi 400/401/403/404/409 theo UC-24 §7 trong `GlobalExceptionHandler`.

## Phase 6: Testing & QA

- [ ] 6.1 Unit `QuestionService` (create/validate-type/lock/status-guard/ownership/submit) ≥ 80% — `feat-testing/TC-UC-24`.
- [ ] 6.2 Integration: `existsAttemptAnswer` lock, filter AND + loại deleted, search LIKE (Testcontainers).
- [ ] 6.3 API/MockMvc: 201/401/403/404/409, không lộ Entity.

## Phase 7: Frontend

- [ ] 7.1 `QuestionBankPage` (search/filter, badge `isLocked`).
- [ ] 7.2 `QuestionEditorPage` (form theo `questionType`); disable "Gửi duyệt" khi khóa/không draft.

## Phase 8: Final Review

- [ ] 8.1 Cross-check `UC-24` §3 (FR-24-01..25) ↔ code; mọi AC-24-01..13 có test phủ (`TRACEABILITY.md`).
- [ ] 8.2 Lint sạch; không TODO; PR ≤ 400 dòng.
