-- V27: Bảng email_outbox — lưu lại email gửi thất bại sau khi hết 3 lần retry
-- trong EmailService, để có thể retry lại định kỳ (kể cả sau khi container
-- restart) thay vì chỉ log ERROR rồi mất hẳn.

CREATE TABLE email_outbox (
    outbox_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    to_email         VARCHAR(255)    NOT NULL,
    subject          VARCHAR(500)    NOT NULL,
    body_html        LONGTEXT        NOT NULL,
    status           VARCHAR(20)     NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending','sent','failed')),
    attempt_count    INT             NOT NULL DEFAULT 0,
    last_error       VARCHAR(1000)   NULL,
    created_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    sent_at          DATETIME(6)     NULL,
    last_attempt_at  DATETIME(6)     NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_email_outbox_status ON email_outbox(status, last_attempt_at);
