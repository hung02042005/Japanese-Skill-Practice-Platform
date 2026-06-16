# TASKS — Học Kanji (`feat-kanji`)

## Phase 1: Database & Entities
- [ ] 1.1 Migration (Flyway/Liquibase) bảng `kanji` (+ `radical`, `is_vip_only`, `is_deleted`) và index `IX_kanji_level_status_stroke`.
- [ ] 1.2 Đảm bảo `student_content_progress` & `flashcards` sẵn sàng (dùng chung `feat-core-learning`).
- [ ] 1.3 Tạo JPA Entity `Kanji` (map cột, CHECK constraint level/status, UNIQUE `character_value`).
- [ ] 1.4 Thiết lập Soft Delete (`is_deleted` / `status='deleted'`).

## Phase 2: Repositories & DTOs
- [ ] 2.1 `KanjiRepository`: query lọc `jlpt_level` + khoảng `stroke_count` + `radical` + keyword (published, not deleted), phân trang.
- [ ] 2.2 Query distinct `radical` + count theo level (endpoint `/radicals`).
- [ ] 2.3 Query `completedCount` join `student_content_progress` (tránh N+1).
- [ ] 2.4 DTO Response: `KanjiListItemResponse`, `KanjiDetailResponse`, `RadicalResponse`, `PageResponse<T>`.
- [ ] 2.5 DTO Request dùng chung: `LearningProgressRequest`, `AddFlashcardRequest` + validation.

## Phase 3: Business Logic (Services)
- [ ] 3.1 `KanjiService.list()`: validate level (422), `strokeMin ≤ strokeMax` (400), clamp page/size, annotate isCompleted/isInFlashcard.
- [ ] 3.2 `KanjiService.getRadicals(level)`.
- [ ] 3.3 `KanjiService.getDetail()`: 404 nếu chưa published/đã xóa; VIP check (403); trả stroke_order_url ảnh tĩnh; cập nhật `last_activity_date`.
- [ ] 3.4 `LearningProgressService.upsert()` cho `content_type='kanji'`: chặn regress (422), tôn trọng `UQ_progress`.
- [ ] 3.5 `FlashcardService.add()` cho kanji: chặn trùng (409).

## Phase 4: Controllers & Routing
- [ ] 4.1 `KanjiController`: `GET /api/kanji`, `/radicals`, `/{id}` + `@PreAuthorize("hasRole('STUDENT')")`.
- [ ] 4.2 Kiểm tra route dùng chung `POST /api/learning-progress`, `POST /api/flashcards` với kanji.
- [ ] 4.3 `GlobalExceptionHandler`: map mã lỗi §7 (400/401/403/404/409/422/500).

## Phase 5: Testing & QA (Backend)
- [ ] 5.1 Unit test `KanjiService` ≥ 80% (level invalid, stroke range invalid, VIP block, completedCount).
- [ ] 5.2 Unit test rule progress (chặn regress) & flashcard (chặn trùng).
- [ ] 5.3 Integration test `/api/kanji/*` (happy + error paths) — đối chiếu AC-KAN-01..15.

## Phase 6: Frontend
- [ ] 6.1 API service: `getKanjiList`, `getKanjiRadicals`, `getKanjiDetail`, `markKanjiComplete`, `addKanjiToFlashcard`.
- [ ] 6.2 Trang `KanjiList` (level tabs, lọc số nét/bộ thủ, search, phân trang, stats).
- [ ] 6.3 Trang/Component chi tiết Kanji (ảnh thứ tự nét tĩnh, onyomi/kunyomi, từ ví dụ).
- [ ] 6.4 Nút +FC / Đã học + Empty/Loading/Error state + accessibility.

## Phase 7: Final Review & Refine
- [ ] 7.1 Cross-check `SPEC.md` + `CONSTITUTION.md` + ADR-007: stroke order chỉ ảnh tĩnh, không trộn level, DTO-only, error mapping.
- [ ] 7.2 Lint sạch (Frontend `npm run lint`, Backend `mvn spotless:apply`) — 0 warning.
- [ ] 7.3 Code Review & Merge theo quy định PR.
