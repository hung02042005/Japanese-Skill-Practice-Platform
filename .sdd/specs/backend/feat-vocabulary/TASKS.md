# TASKS — Học Từ Vựng (`feat-vocabulary`)

## Phase 1: Database & Entities

- [ ] 1.1 Migration (Flyway/Liquibase) bảng `vocabulary` (+ `is_vip_only`, `is_deleted`) và index `IX_vocabulary_level_topic_status`.
- [ ] 1.2 Đảm bảo bảng/Entity `student_content_progress` & `flashcards` sẵn sàng (dùng chung `feat-core-learning`).
- [ ] 1.3 Tạo JPA Entity `Vocabulary` (map đúng cột, CHECK constraint level/status).
- [ ] 1.4 Thiết lập Soft Delete (`is_deleted` / `status='deleted'`) trên Entity.

## Phase 2: Repositories & DTOs

- [ ] 2.1 `VocabularyRepository`: query lọc `jlpt_level` + `topic` + keyword (chỉ published, not deleted), phân trang.
- [ ] 2.2 Query distinct `topic` + count theo level (FR-VOC-05).
- [ ] 2.3 Query `completedCount` join `student_content_progress` (tránh N+1 — `@EntityGraph`/`JOIN FETCH`).
- [ ] 2.4 DTO Response: `VocabularyListItemResponse`, `VocabularyDetailResponse`, `VocabTopicResponse`, `PageResponse<T>`.
- [ ] 2.5 DTO Request dùng chung: `LearningProgressRequest`, `AddFlashcardRequest` + validation annotations.

## Phase 3: Business Logic (Services)

- [ ] 3.1 `VocabularyService.list()`: validate level (422 LEVEL_MISMATCH), clamp page/size, annotate isCompleted/isInFlashcard.
- [ ] 3.2 `VocabularyService.getTopics(level)`.
- [ ] 3.3 `VocabularyService.getDetail()`: 404 nếu chưa published/đã xóa; VIP check (403); cập nhật `last_activity_date`.
- [ ] 3.4 `LearningProgressService.upsert()` cho `content_type='vocabulary'`: chặn regress (422 PROGRESS_REGRESSION), tôn trọng `UQ_progress`.
- [ ] 3.5 `FlashcardService.add()` cho vocabulary: chặn trùng (409 FLASHCARD_EXISTS).

## Phase 4: Controllers & Routing

- [ ] 4.1 `VocabularyController`: `GET /api/vocabulary`, `/topics`, `/{id}` + `@PreAuthorize("hasRole('STUDENT')")`.
- [ ] 4.2 Kiểm tra route dùng chung `POST /api/learning-progress`, `POST /api/flashcards` hoạt động với vocabulary.
- [ ] 4.3 `GlobalExceptionHandler`: map mã lỗi §7 (400/401/403/404/409/422/500).

## Phase 5: Testing & QA (Backend)

- [ ] 5.1 Unit test `VocabularyService` ≥ 80% (level invalid, VIP block, empty filter, completedCount).
- [ ] 5.2 Unit test rule progress (chặn regress) & flashcard (chặn trùng).
- [ ] 5.3 Integration test `/api/vocabulary/*` (happy + error paths) — đối chiếu AC-VOC-01..14.

## Phase 6: Frontend

- [ ] 6.1 API service: `getVocabularyList`, `getVocabTopics`, `markVocabComplete`, `addVocabToFlashcard`.
- [ ] 6.2 Trang `VocabularyList` (level tabs, topic select, search debounce, phân trang, stats).
- [ ] 6.3 Component `VocabCard` (audio play, nút +FC / Đã học, trạng thái loading/added).
- [ ] 6.4 Empty/Loading/Error state + accessibility (label ẩn, aria, no autoplay).

## Phase 7: Final Review & Refine

- [ ] 7.1 Cross-check `SPEC.md` + `CONSTITUTION.md`: không trộn level, soft delete, error mapping, DTO-only.
- [ ] 7.2 Lint sạch (Frontend `npm run lint`, Backend `mvn spotless:apply`) — 0 warning.
- [ ] 7.3 Code Review & Merge theo quy định PR.
