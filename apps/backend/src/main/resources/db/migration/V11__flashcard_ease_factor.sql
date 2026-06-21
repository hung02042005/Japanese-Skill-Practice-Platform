-- V11: Thêm cột ease_factor cho thuật toán SM-2 (FR-FC-23/24).
-- DB cũ thiếu cột này vì entity Flashcard trước đây không map ease_factor.
-- Thêm kèm DEFAULT 2.50 để ALTER hợp lệ trên bảng đã có dữ liệu (SQL Server yêu cầu NULL/DEFAULT).
-- Idempotent: chỉ thêm khi cột chưa tồn tại (tránh đụng ddl-auto=update / chạy lại).
IF COL_LENGTH('dbo.flashcards', 'ease_factor') IS NULL
BEGIN
    ALTER TABLE flashcards
        ADD ease_factor DECIMAL(5,2) NOT NULL
            CONSTRAINT DF_flashcards_ease_factor DEFAULT (2.50);
END;
