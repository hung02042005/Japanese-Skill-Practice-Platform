/* ============================================================================
   V13 — FLASHCARD: NGUỒN THẺ (added_reason)
   ----------------------------------------------------------------------------
   Tham chiếu: .sdd/specs/frontend/feat-student/SPEC-notebook.md §7
   Mục đích: lưu vì sao một thẻ vào sổ, để Sổ tay hiển thị "Nguồn: …".
     'wrong'  — trả lời sai khi ôn (FR-FC-81)
     'manual' — lưu thủ công từ Từ điển
     'learn'  — sinh ra khi học từ trong phiên (level + topic)
   Nullable: thẻ cũ không xác định nguồn → NULL, FE chỉ hiển thị "Ôn tiếp".
   Idempotent: chỉ thêm khi cột chưa tồn tại (tránh đụng ddl-auto / chạy lại).
   DBMS    : Microsoft SQL Server 2019+
   ============================================================================ */

USE JLPT_LearningDB;
GO

IF COL_LENGTH('dbo.flashcards', 'added_reason') IS NULL
    ALTER TABLE flashcards ADD added_reason NVARCHAR(20) NULL;
GO

-- Backfill thẻ đang nằm trong sổ "Từ cần ôn lại": suy ra nguồn từ last_rating.
-- Thẻ từng trả lời sai → 'wrong'; còn lại coi như lưu thủ công → 'manual'.
UPDATE f
SET f.added_reason = CASE WHEN f.last_rating = 'wrong' THEN 'wrong' ELSE 'manual' END
FROM flashcards f
JOIN flashcard_decks d ON d.deck_id = f.deck_id
WHERE d.is_review_deck = 1
  AND f.added_reason IS NULL;
GO
