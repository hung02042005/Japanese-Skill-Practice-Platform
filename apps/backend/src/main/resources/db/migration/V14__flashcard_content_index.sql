/* ============================================================================
   V14 — FLASHCARD: INDEX TRA THẺ THEO NỘI DUNG
   ----------------------------------------------------------------------------
   Mục đích: tăng tốc các truy vấn lọc theo (student_id, content_type, content_id):
     - FlashcardRepository.findByStudentAndContent      (addCard, getSession, review-deck)
     - FlashcardRepository.findByStudentAndContentIds   (phiên level+topic)
     - FlashcardRepository.findVocabContentIdsByStudent
   Trước V14 các query này phải scan thẻ của student (chỉ có index theo deck_id /
   next_review_date), gọi lặp trong vòng lặp nên tốn kém.
   Idempotent: chỉ tạo khi index chưa tồn tại (tránh đụng ddl-auto / chạy lại).
   DBMS    : Microsoft SQL Server 2019+
   ============================================================================ */

USE JLPT_LearningDB;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = 'IX_flashcards_owner_content'
                 AND object_id = OBJECT_ID('dbo.flashcards'))
    CREATE INDEX IX_flashcards_owner_content
        ON flashcards(student_id, content_type, content_id);
GO
