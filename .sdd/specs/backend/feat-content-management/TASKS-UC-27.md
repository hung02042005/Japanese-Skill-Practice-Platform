# TASKS — Manage Learning Content (`feat-content-management`) — UC-27

> **UC:** UC-27 (Manage Learning Content) | **Nguồn:** `UC-27-manage-learning-content.md`, `PLAN-UC-27.md`
> **Cập nhật:** 2026-06-12 | **Lưu ý:** "course" = `lessons` (không có `/api/staff/courses`); media chỉ URL.

## Phase 1: Database & Domain
- [ ] 1.1 Tận dụng index sẵn (`IX_lessons_*`, `IX_vocab_*`, `IX_kanji_public_level`) (NFR-27-01). KHÔNG sửa cột.
- [ ] 1.2 Entity `Lesson`/`Vocabulary`/`Kanji` map đủ cột + workflow; enum `LessonType`/`JlptLevel`.

## Phase 2: Repository Layer
- [ ] 2.1 `Lesson/Vocabulary/KanjiRepository.findByCreatedByWithFilters(...)` — loại `deleted`.
- [ ] 2.2 `KanjiRepository.existsByCharacterValue(value)` (FR-27-21); `LessonRepository.findActiveById(id)` (FR-27-18).

## Phase 3: DTO & Validation
- [ ] 3.1 `CreateLessonRequest`/`UpdateLessonRequest` — `title`/`lessonType`/`jlptLevel`; ≥1 nội dung; listening cần `audioUrl`; `displayOrder>=0` (FR-27-09..14).
- [ ] 3.2 `CreateVocabularyRequest` — `word`/`meaning`/`furigana`/`jlptLevel` (FR-27-16); `CreateKanjiRequest` — `characterValue`/`meaning`/`jlptLevel` + ≥1 onyomi/kunyomi (FR-27-20).
- [ ] 3.3 `Lesson/Vocabulary/KanjiDetailResponse` — không lộ Entity (NFR-27-04).

## Phase 4: Service Layer
- [ ] 4.1 `LearningContentService.createLesson/updateLesson` — ép `draft`+`created_by`; validate type/nội dung/listening (400 `INVALID_LESSON_TYPE`/`LESSON_CONTENT_REQUIRED`); media chỉ URL (FR-27-01/03/09..15).
- [ ] 4.2 `createVocabulary` — bắt buộc trường; `lessonId` tồn tại (404 `LESSON_NOT_FOUND`) (FR-27-16..19).
- [ ] 4.3 `createKanji` — bắt buộc + ≥1 onyomi/kunyomi; trùng `characterValue` (409 `KANJI_DUPLICATE`); `strokeCount>=1` (FR-27-20..24).
- [ ] 4.4 Guard chung: sửa chỉ `{draft,rejected}` (409, FR-27-04); cấm publish/archive (403, FR-27-05); `OwnershipGuard` (403, FR-27-06); refresh `updated_at` (FR-27-07).
- [ ] 4.5 Submit-review qua `ContentSubmissionService` (`contentType∈{lesson,vocabulary,kanji}`): re-validate theo loại + guard status → `pending_review` (FR-27-25/26).
- [ ] 4.6 SLF4J log `[INFO] Staff {id} {action} {contentType} {cid}` (FR-27-08); soft delete (NFR-27-07).

## Phase 5: Controller & Security
- [ ] 5.1 `StaffLearningContentController` (`/api/staff/lessons`, `/vocabulary`, `/kanji`): POST + PUT (lesson).
- [ ] 5.2 `@PreAuthorize(...)`; map 400/401/403/404/409 theo UC-27 §7.

## Phase 6: Testing & QA
- [ ] 6.1 Unit `LearningContentService` (content-required, listening-audio, kanji trùng, vocab furigana, state-machine, ownership) — `feat-testing/TC-UC-27`.
- [ ] 6.2 Integration: media URL (không BLOB), `character_value` UNIQUE.
- [ ] 6.3 API/MockMvc: 201/401/403/404/409; DTO.

## Phase 7: Frontend
- [ ] 7.1 `LearningContentPage` (tab lesson/vocabulary/kanji; upload trả URL; form theo loại).

## Phase 8: Final Review
- [ ] 8.1 Cross-check `UC-27` §3 (FR-27-01..26) ↔ code; AC-27-01..16 có test phủ (`TRACEABILITY.md`).
- [ ] 8.2 Lint sạch; không TODO; PR ≤ 400 dòng.
