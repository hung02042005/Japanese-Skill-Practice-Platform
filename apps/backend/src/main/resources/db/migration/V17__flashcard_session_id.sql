/* ============================================================================
   V17 — FLASHCARD: GẮN last_session_id CHO MỖI LƯỢT ÔN
   ----------------------------------------------------------------------------
   Mục đích: thay cách phát hiện "từ sai trong phiên" bằng cửa sổ thời gian 2 giờ
   (mong manh: học >2h hoặc 2 phiên/ngày sẽ gom nhầm) bằng một session_id rõ ràng.
   getSession sinh 1 UUID/phiên; mỗi lượt review đóng dấu last_session_id lên thẻ;
   cuối phiên gom thẻ WRONG theo đúng session_id (FlashcardRepository).
   Index (student_id, last_session_id) phục vụ findWrongVocabCardsInSession.
   Idempotent: chỉ thêm khi cột/index chưa tồn tại (an toàn chạy lại / ddl-auto).
   DBMS    : Microsoft SQL Server 2019+
   ============================================================================ */

USE JLPT_LearningDB;
GO

/* ── BƯỚC 1: Thêm cột last_session_id (UUID dạng chuỗi, NULL với thẻ cũ) ─────── */
IF COL_LENGTH('dbo.flashcards', 'last_session_id') IS NULL
    ALTER TABLE flashcards ADD last_session_id VARCHAR(36) NULL;
GO

/* ── BƯỚC 2: Index gom thẻ theo (student_id, last_session_id) ────────────────── */
IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = 'IX_flashcards_owner_session'
                 AND object_id = OBJECT_ID('dbo.flashcards'))
    CREATE INDEX IX_flashcards_owner_session
        ON flashcards(student_id, last_session_id);
GO
