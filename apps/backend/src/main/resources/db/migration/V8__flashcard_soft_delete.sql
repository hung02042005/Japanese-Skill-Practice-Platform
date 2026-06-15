/* ============================================================================
   V8 — FLASHCARD: SOFT DELETE + DECK PLACEHOLDER FLAG
   ----------------------------------------------------------------------------
   Mục đích :
     1. is_deleted    — Tuân thủ ADR-004 (Soft Delete toàn hệ thống). Xoá bộ thẻ
                        chỉ đánh dấu is_deleted = 1, KHÔNG dùng DELETE FROM.
     2. is_placeholder — Đánh dấu thẻ "giữ chỗ" (rỗng) sinh ra khi tạo bộ thẻ mới.
                        Vì deck được suy ra từ GROUP BY deck_name (chưa có bảng deck
                        riêng), một bộ thẻ rỗng cần ít nhất 1 thẻ để hiển thị trong
                        danh sách. Thẻ giữ chỗ KHÔNG được tính vào số thẻ và KHÔNG
                        hiển thị khi xem thẻ.
   DBMS    : Microsoft SQL Server 2019+
   ============================================================================ */

USE JLPT_LearningDB;
GO

ALTER TABLE flashcards ADD is_deleted BIT NOT NULL DEFAULT 0;
GO

ALTER TABLE flashcards ADD is_placeholder BIT NOT NULL DEFAULT 0;
GO

/* Backfill: đánh dấu các thẻ giữ chỗ đã tồn tại (thẻ custom rỗng, không gắn nội dung). */
UPDATE flashcards
SET is_placeholder = 1
WHERE content_type = 'custom'
  AND content_id IS NULL
  AND (front_text IS NULL OR front_text = N'');
GO

/* Index lọc nhanh thẻ chưa xoá theo chủ sở hữu. */
CREATE INDEX IX_flashcards_owner_active ON flashcards(student_id, is_deleted);
GO
