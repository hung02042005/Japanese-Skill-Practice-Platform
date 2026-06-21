/* ============================================================================
   V15 — FLASHCARD: BỎ CỘT deck_name THỪA (đã first-class hoá deck từ V9)
   ----------------------------------------------------------------------------
   Bối cảnh: từ V9, sổ tay là bảng flashcard_decks (deck_id FK). Cột chuỗi
   flashcards.deck_name chỉ còn được đồng bộ thủ công, KHÔNG query nào đọc
   (code dùng deck.name). Index cũ IX_flashcards_owner_deck(student_id, deck_name)
   cũng không còn dùng nhưng vẫn bị maintain mỗi INSERT/UPDATE.
   Việc bỏ giúp: ghi nhẹ hơn, schema sạch. V10 (dự kiến drop) đã bị dùng cho
   vocabulary_topics_path nên dời sang V15.
   Thứ tự: DROP index -> DROP default constraint -> DROP column.
   Idempotent: chỉ thao tác khi đối tượng còn tồn tại (an toàn chạy lại / ddl-auto).
   DBMS    : Microsoft SQL Server 2019+
   ============================================================================ */

USE JLPT_LearningDB;
GO

/* ── BƯỚC 1: Drop index cũ trên (student_id, deck_name) ──────────────────────── */
IF EXISTS (SELECT 1 FROM sys.indexes
           WHERE name = 'IX_flashcards_owner_deck'
             AND object_id = OBJECT_ID('dbo.flashcards'))
    DROP INDEX IX_flashcards_owner_deck ON flashcards;
GO

/* ── BƯỚC 2: Drop cột deck_name (gỡ default constraint tên auto-gen trước) ────── */
IF COL_LENGTH('dbo.flashcards', 'deck_name') IS NOT NULL
BEGIN
    DECLARE @df NVARCHAR(200);
    SELECT @df = dc.name
    FROM sys.default_constraints dc
    JOIN sys.columns c ON c.object_id = dc.parent_object_id
                      AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID('dbo.flashcards') AND c.name = 'deck_name';
    IF @df IS NOT NULL EXEC('ALTER TABLE flashcards DROP CONSTRAINT ' + @df);

    ALTER TABLE flashcards DROP COLUMN deck_name;
END
GO
