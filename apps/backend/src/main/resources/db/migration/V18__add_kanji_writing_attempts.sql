/* ============================================================================
   V8 — Thêm bảng lưu lịch sử luyện viết Kanji (DTW scoring)
   ============================================================================ */

CREATE TABLE kanji_writing_attempts (
    attempt_id      BIGINT          IDENTITY(1,1) PRIMARY KEY,
    student_id      BIGINT          NOT NULL,
    kanji_id        BIGINT          NOT NULL,
    character_value NVARCHAR(5)     NOT NULL,
    total_strokes   INT             NOT NULL,
    avg_dtw_score   FLOAT           NULL,
    final_quality   NVARCHAR(20)    NULL
        CHECK (final_quality IN ('perfect', 'good', 'ok', 'bad')),
    stroke_details  NVARCHAR(MAX)   NULL,       -- JSON array per-stroke results
    is_deleted      BIT             NOT NULL DEFAULT 0,
    created_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    created_by      BIGINT          NULL,

    CONSTRAINT FK_kwa_student
        FOREIGN KEY (student_id) REFERENCES student_users(student_id)
);
GO

CREATE INDEX IX_kwa_student_id ON kanji_writing_attempts(student_id);
GO
CREATE INDEX IX_kwa_kanji_id   ON kanji_writing_attempts(kanji_id);
GO
