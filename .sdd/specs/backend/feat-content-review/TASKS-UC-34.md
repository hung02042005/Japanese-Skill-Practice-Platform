# TASKS — Manage Published Content Status (`feat-content-review`) — UC-34

> **UC:** UC-34 (Manage Published Content Status) | **Nguồn:** `UC-34-manage-published-content-status.md`, `PLAN-UC-34.md`
> **Cập nhật:** 2026-06-12
> **Lưu ý:** Tái sử dụng hạ tầng UC-33 (resolver, enum, security). TASKS của UC-33 ở `TASKS.md`.

## Phase 1: Database & Domain
- [ ] 1.1 Rà soát index lọc trạng thái cho danh sách `published` (đã có `IX_questions_public_bank`, `IX_assessments_public_list`, `IX_assign_parent`). Nếu cần, migration **chỉ thêm** index `(status, published_at)` cho các bảng còn thiếu (NFR-34-03). KHÔNG sửa cột.
- [ ] 1.2 Dùng lại enum `ContentStatus` / `ContentType` (từ UC-33); xác nhận đủ giá trị `ARCHIVED`, `DELETED`, `DRAFT`, `PUBLISHED`.
- [ ] 1.3 Xác nhận Entity `QuestionAssignment` map đúng `parent_type`, `parent_id`, `question_id`; `Assessment` có `lesson_id`, `status`.

## Phase 2: Repository Layer
- [ ] 2.1 Mỗi repository học liệu: `Page<T> findByStatus("published", Pageable)` (dùng lại) + sort `published_at` desc.
- [ ] 2.2 Mỗi repository: guarded update `@Modifying @Query("UPDATE ... SET status=:to, updatedAt=:now WHERE id=:id AND status=:from")` trả số dòng ảnh hưởng (FR-34-13, FR-34-17).
- [ ] 2.3 `QuestionAssignmentRepository.findPublishedAssessmentRefs(questionId)` — JOIN `assessments` `status='published'`, trả `(assessment_id, title)` (FR-34-14).
- [ ] 2.4 `AssessmentRepository.findPublishedByLessonId(lessonId)` — kiểm tra lesson bị tham chiếu (FR-34-15).
- [ ] 2.5 `AdminAuditLogRepository.save(...)` (dùng lại).

## Phase 3: DTO & Validation
- [ ] 3.1 `UpdateContentStatusRequest` (`contentType`, `status` ∈ {unpublished,archived,deleted}, `reason` `@NotBlank` `@Size(10,500)`).
- [ ] 3.2 `RestoreContentRequest` (`contentType`).
- [ ] 3.3 Response DTOs: `PublishedContentItemResponse`, `PublishedContentDetailResponse` (kèm `references`), `ContentStatusResultResponse`, `ResourceReferenceResponse`.
- [ ] 3.4 Enum validator cho `contentType`/`status`; thông báo lỗi `VALIDATION_FAILED` (FR-34-12).

## Phase 4: Service Layer (Business Logic)
- [ ] 4.1 `ContentReferenceChecker.findBlockingReferences(contentType, contentId)` — gom logic FR-34-14/15, trả `List<ResourceReferenceResponse>` (rỗng = không chặn).
- [ ] 4.2 `PublishedContentService.listPublished(type, jlptLevel, pageable)` — gộp `published`, sort `published_at` desc (FR-34-03..06).
- [ ] 4.3 `PublishedContentService.getDetail(contentType, contentId)` — map DTO + `references`; 404 nếu không tồn tại (FR-34-07..09).
- [ ] 4.4 `PublishedContentService.updateStatus(contentId, req, managerId)`:
  - validate `reason` (FR-34-11) → `ReasonRequiredException` nếu rỗng;
  - gọi `ContentReferenceChecker` → nếu có ref ⇒ `ResourceInUseException` (kèm danh sách) (FR-34-14..16);
  - guarded update `WHERE status='published'` → `draft`/`archived`/`deleted`; 0 dòng ⇒ `InvalidStateTransitionException` (FR-34-13, 17);
  - audit (FR-34-23/24).
- [ ] 4.5 `PublishedContentService.restore(contentId, req, managerId)`:
  - nếu nguồn `deleted` ⇒ `RestoreNotAllowedException` (FR-34-19);
  - nếu nguồn ≠ `archived` ⇒ `InvalidStateTransitionException` (FR-34-20);
  - guarded update `WHERE status='archived'` → `published`; audit (FR-34-18, 23).
- [ ] 4.6 `StatusChangeAuditService.log(...)` — ghi `admin_audit_logs` (`action` ∈ {unpublish_content, archive_content, delete_content, restore_content}, `target_table`, `target_id`, `description=reason`) + SLF4J; cùng `@Transactional` (FR-34-24).
- [ ] 4.7 Custom exceptions: `ResourceInUseException` (chứa `references`), `RestoreNotAllowedException`, `InvalidStateTransitionException`, `ReasonRequiredException`, `ContentNotFoundException`.

## Phase 5: Controller & Security
- [ ] 5.1 `ManagerContentStatusController`: `GET /api/manager/published-contents`, `GET /api/manager/contents/{contentId}`, `PUT /api/manager/contents/{contentId}/status`, `POST /api/manager/contents/{contentId}/restore`.
- [ ] 5.2 `@PreAuthorize("hasAuthority('STAFF_MANAGER')")` + SecurityFilterChain cho `/api/manager/**` (FR-34-01, NFR-34-04).
- [ ] 5.3 Cập nhật `GlobalExceptionHandler`: map `ResourceInUseException`→409 (đưa `references` vào `data`), `RestoreNotAllowedException`→409, `InvalidStateTransitionException`→409, `ContentNotFoundException`→404, `ReasonRequiredException`→400 theo `{ status, message, data }` (UC-34 §7).

## Phase 6: Testing & QA (Backend)
- [ ] 6.1 Unit test `PublishedContentService` ≥ 80% (unpublish/archive/delete/restore, reason bắt buộc, reference-in-use, restore deleted bị chặn, soft delete) — xem `feat-testing/TC-UC-34`.
- [ ] 6.2 Integration test reference check + guarded update (Testcontainers SQL Server): question trong assessment published → bị chặn; soft delete giữ bản ghi.
- [ ] 6.3 Integration test 4 endpoint (happy + error: 400/403/404/409) bằng MockMvc.
- [ ] 6.4 Test bất biến: response không lộ Entity; audit log ghi đúng; nội dung ẩn không lọt Student-facing.

## Phase 7: Frontend
- [ ] 7.1 `services/managerContentService` gom 4 API call.
- [ ] 7.2 `PublishedContentPage` (bảng phân trang + lọc `type`/`jlptLevel`, loading/error).
- [ ] 7.3 `ContentStatusActions` (Unpublish/Archive/Delete với modal `reason` bắt buộc; Restore cho `archived`); modal hiển thị `references` khi 409.
- [ ] 7.4 Tách UI StaffManager khỏi Staff (LESSON-001); ProtectedRoute theo quyền `staff_manager`.

## Phase 8: Final Review
- [ ] 8.1 Cross-check `UC-34` §3 (FR-34-01..24) ↔ code; xác nhận Rule 1–9 đều có test phủ.
- [ ] 8.2 Lint sạch (`mvn spotless:apply`, `npm run lint`); không TODO.
- [ ] 8.3 Code Review & Merge theo PR (≤ 400 dòng/PR; chia nhỏ nếu vượt).
