-- V27: Bảng email_outbox — lưu lại email gửi thất bại sau khi hết 3 lần retry
-- trong EmailService, để có thể retry lại định kỳ (kể cả sau khi container
-- restart) thay vì chỉ log ERROR rồi mất hẳn.

CREATE TABLE email_outbox (
    outbox_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    to_email         NVARCHAR(255)   NOT NULL,
    subject          NVARCHAR(500)   NOT NULL,
    body_html        NVARCHAR(MAX)   NOT NULL,
    status           NVARCHAR(20)    NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending','sent','failed')),
    attempt_count    INT             NOT NULL DEFAULT 0,
    last_error       NVARCHAR(1000)  NULL,
    created_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    sent_at          DATETIME2       NULL,
    last_attempt_at  DATETIME2       NULL
);
GO

CREATE INDEX IX_email_outbox_status ON email_outbox(status, last_attempt_at);
GO
