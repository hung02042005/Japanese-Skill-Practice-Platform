/* ============================================================================
   V9 — FLASHCARD: SỔ TAY (DECK) FIRST-CLASS
   ----------------------------------------------------------------------------
   Tham chiếu: .sdd/specs/backend/feat-flashcard-srs/SPEC.md §5 (v2.0)
   Mục đích:
     1. Tách "sổ tay riêng" thành bảng flashcard_decks độc lập (deck có id,
        metadata, đổi tên rẻ) thay cho cột chuỗi flashcards.deck_name.
     2. flashcards.deck_name (string) -> flashcards.deck_id (FK).
     3. Bỏ hack "thẻ giữ chỗ" (is_placeholder) — deck rỗng nay là bản ghi thật.
   Thứ tự BẮT BUỘC (§5.3):
     1) CREATE flashcard_decks + filtered unique index
     2) Backfill deck từ DISTINCT (student_id, deck_name) của flashcards
     3) ADD deck_id NULL -> UPDATE join theo (student_id, deck_name)
     4) DELETE thẻ placeholder -> DROP COLUMN is_placeholder
     5) Set deck_id NOT NULL + FK_flashcard_deck
     6) GIỮ deck_name lại — chỉ DROP ở V10 sau khi code chạy ổn (an toàn rollback)
   Lưu ý:
     - Backfill PHẢI chạy trước khi set deck_id NOT NULL.
     - System deck (student_id NULL) không dính unique -> dùng filtered index.
   DBMS    : Microsoft SQL Server 2019+
   ============================================================================ */

USE JLPT_LearningDB;
GO

/* ── BƯỚC 1: Bảng sổ tay first-class ──────────────────────────────────────── */
CREATE TABLE flashcard_decks (
    deck_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id     BIGINT          NULL,                 -- NULL = deck hệ thống
    name           NVARCHAR(255)   NOT NULL,
    description    NVARCHAR(500)   NULL,
    jlpt_level     NVARCHAR(5)     NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    topic          NVARCHAR(100)   NULL,
    color          NVARCHAR(20)    NULL,
    display_order  INT             NOT NULL DEFAULT 0,
    is_system      BIT             NOT NULL DEFAULT 0,    -- deck hệ thống toàn cục
    is_review_deck BIT             NOT NULL DEFAULT 0,    -- sổ auto "Từ cần ôn lại" (per-student)
    is_deleted     BIT             NOT NULL DEFAULT 0,
    created_at     DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at     DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_deck_student FOREIGN KEY (student_id)
        REFERENCES student_users(student_id) ON DELETE CASCADE
);
GO

-- Unique tên deck theo từng học viên (bỏ qua deck đã xóa & deck hệ thống student_id NULL)
CREATE UNIQUE INDEX UQ_deck_student_name
    ON flashcard_decks(student_id, name)
    WHERE is_deleted = 0 AND student_id IS NOT NULL;
GO

-- Mỗi học viên TỐI ĐA 1 sổ "Từ cần ôn lại"
CREATE UNIQUE INDEX UQ_review_deck_per_student
    ON flashcard_decks(student_id)
    WHERE is_review_deck = 1 AND is_deleted = 0;
GO

-- Liệt kê deck của học viên (FR-FC-01)
CREATE INDEX IX_decks_owner ON flashcard_decks(student_id, is_deleted);
GO

/* ── BƯỚC 2: Backfill deck từ flashcards hiện có ──────────────────────────────
   - DISTINCT (student_id, deck_name) -> 1 deck/row (kể cả deck chỉ có thẻ placeholder
     => deck rỗng vẫn được giữ làm row thật, đúng FR-FC-07).
   - student_id NULL => deck hệ thống (is_system = 1).
   - Tách jlpt_level/topic từ magic string cũ "{level}_{topic}" (vd "N5_食べ物")
     CHỈ khi tên khớp pattern N[1-5]_…; còn lại để NULL.                          */
INSERT INTO flashcard_decks (student_id, name, is_system, jlpt_level, topic)
SELECT
    f.student_id,
    f.deck_name,
    CASE WHEN f.student_id IS NULL THEN 1 ELSE MAX(CAST(f.is_system AS INT)) END,
    CASE WHEN f.deck_name LIKE 'N[1-5][_]%' THEN LEFT(f.deck_name, 2) END,
    CASE WHEN f.deck_name LIKE 'N[1-5][_]%'
         THEN NULLIF(SUBSTRING(f.deck_name, 4, LEN(f.deck_name)), N'') END
FROM flashcards f
GROUP BY f.student_id, f.deck_name;
GO

/* ── BƯỚC 3: flashcards.deck_id (NULL trước) + UPDATE join ─────────────────── */
ALTER TABLE flashcards ADD deck_id BIGINT NULL;
GO

UPDATE f
SET f.deck_id = d.deck_id
FROM flashcards f
INNER JOIN flashcard_decks d
        ON d.is_deleted = 0
       AND d.name = f.deck_name
       AND (d.student_id = f.student_id
            OR (d.student_id IS NULL AND f.student_id IS NULL));
GO

/* ── BƯỚC 4: Bỏ thẻ giữ chỗ + cột is_placeholder ─────────────────────────────
   Deck rỗng đã thành row thật ở flashcard_decks nên thẻ placeholder thừa.        */
IF EXISTS (SELECT 1 FROM sys.columns
           WHERE object_id = OBJECT_ID('flashcards') AND name = 'is_placeholder')
BEGIN
    DELETE FROM flashcards WHERE is_placeholder = 1;

    -- DROP COLUMN cần gỡ default constraint (tên auto-gen) trước.
    DECLARE @df NVARCHAR(200);
    SELECT @df = dc.name
    FROM sys.default_constraints dc
    JOIN sys.columns c ON c.object_id = dc.parent_object_id
                       AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID('flashcards') AND c.name = 'is_placeholder';
    IF @df IS NOT NULL EXEC('ALTER TABLE flashcards DROP CONSTRAINT ' + @df);

    ALTER TABLE flashcards DROP COLUMN is_placeholder;
END
GO

/* ── BƯỚC 5: deck_id NOT NULL + FK ───────────────────────────────────────────
   Mọi thẻ còn lại đều đã có deck_id (backfill từ chính bảng này).                */
ALTER TABLE flashcards ALTER COLUMN deck_id BIGINT NOT NULL;
GO

ALTER TABLE flashcards ADD CONSTRAINT FK_flashcard_deck
    FOREIGN KEY (deck_id) REFERENCES flashcard_decks(deck_id);
GO

-- Mở deck + ưu tiên thẻ đến hạn (FR-FC-02)
CREATE INDEX IX_flashcards_deck_due ON flashcards(deck_id, next_review_date);
GO

/* ── BƯỚC 6: GIỮ cột deck_name ────────────────────────────────────────────────
   KHÔNG drop deck_name ở V9. Để V10 drop sau khi code chạy ổn (rollback an toàn).
   Index cũ IX_flashcards_owner_deck(student_id, deck_name) cũng giữ tới V10.     */
GO
