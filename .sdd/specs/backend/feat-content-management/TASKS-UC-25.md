# TASKS — Manage Grammar Content (`feat-content-management`) — UC-25

> **UC:** UC-25 (Manage Grammar Content) | **Nguồn:** `UC-25-manage-grammar-content.md`, `PLAN-UC-25.md`
> **Cập nhật:** 2026-06-12 | **Lưu ý:** Không có bảng `courses`; chỉ liên kết `lesson_id`.

## Phase 1: Database & Domain
- [ ] 1.1 Tận dụng index `IX_grammar_public_level`; bổ sung `(created_by, status)` nếu thiếu (NFR-04). KHÔNG sửa cột.
- [ ] 1.2 Entity `GrammarPoint` map đủ cột (`structure`, `formula`, `meaning`, `usage_explanation`, `jlpt_level`, `example_sentence_jp/vi`, `lesson_id`, workflow).

## Phase 2: Repository Layer
- [ ] 2.1 `GrammarPointRepository.findByCreatedByWithFilters(staffId, jlptLevel, status, Pageable)` — chỉ của caller, loại `deleted` (FR-09/11).
- [ ] 2.2 `LessonRepository.findActiveById(id)` — kiểm tra tồn tại + chưa `deleted` (FR-07).

## Phase 3: DTO & Validation
- [ ] 3.1 `CreateGrammarRequest` (bắt buộc `structure`, `meaning`, `usageExplanation`, `exampleSentenceJp`, `jlptLevel` — FR-03/05), `UpdateGrammarRequest` (không `status` — FR-16).
- [ ] 3.2 `GrammarSummaryResponse`, `GrammarDetailResponse` (+ lesson rút gọn) — không lộ Entity (FR-21 NV: không lộ password_hash creator).

## Phase 4: Service Layer
- [ ] 4.1 `GrammarService.create` — ép `draft`, `created_by`, bỏ qua status; validate JLPT (FR-04) + trường bắt buộc (FR-01/02/03/05).
- [ ] 4.2 Liên kết lesson: tồn tại (404 `ERR-LESSON-404`) + khớp `jlpt_level` (422 `ERR-LEVEL-MISMATCH-422`) (FR-06/07/08).
- [ ] 4.3 `listMine` + `getDetail` (404 nếu không tồn tại/deleted/không thuộc quyền) (FR-09..12).
- [ ] 4.4 `update` — `OwnershipGuard` (403, FR-17) → guard status `{draft,rejected}` (422, FR-13/14/15) → bỏ qua `status` client → refresh `updated_at` (FR-16).
- [ ] 4.5 Submit-review qua `ContentSubmissionService` (`contentType=grammar`): re-validate đầy đủ (422 `ERR-SUBMIT-INCOMPLETE-422` nếu thiếu — FR-20) → `draft/rejected → pending_review` (FR-18/19).
- [ ] 4.6 SLF4J application log `GRAMMAR_CREATED/UPDATED/SUBMITTED` (FR-22); soft delete `status='deleted'` (NFR-09).

## Phase 5: Controller & Security
- [ ] 5.1 `StaffGrammarController` (`/api/staff/grammar`): POST, GET, GET /{id}, PUT /{id}.
- [ ] 5.2 `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")`; map 400/401/403/404/422 theo UC-25 §7.

## Phase 6: Testing & QA
- [ ] 6.1 Unit `GrammarService` (bắt buộc trường, JLPT, lesson-level mismatch, state-machine, ownership, submit-gate) — `feat-testing/TC-UC-25`.
- [ ] 6.2 Integration: list của caller loại deleted; soft delete giữ bản ghi.
- [ ] 6.3 API/MockMvc: 201/401/403/404/422; không lộ Entity.

## Phase 7: Frontend
- [ ] 7.1 `GrammarEditorPage` (form + chọn lesson cùng cấp độ) + list "của tôi" + lọc `jlptLevel`/`status`.

## Phase 8: Final Review
- [ ] 8.1 Cross-check `UC-25` §3 (FR-01..22) ↔ code; AC-01..17 có test phủ (`TRACEABILITY.md`).
- [ ] 8.2 Lint sạch; không TODO; PR ≤ 400 dòng.
