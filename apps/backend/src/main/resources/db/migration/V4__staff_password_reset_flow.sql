IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('staff_users') AND name = 'must_change_password'
)
    ALTER TABLE staff_users
    ADD must_change_password BIT NOT NULL CONSTRAINT DF_staff_users_must_change_password DEFAULT 0;
GO

CREATE TABLE staff_password_reset_requests (
    request_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    staff_id        BIGINT          NOT NULL,
    status          NVARCHAR(20)    NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'completed', 'expired', 'cancelled')),
    requested_at    DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    expires_at      DATETIME2       NOT NULL,
    completed_at    DATETIME2       NULL,
    completed_by    BIGINT          NULL,
    request_ip      NVARCHAR(45)    NULL,

    CONSTRAINT FK_reset_req_staff FOREIGN KEY (staff_id) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_reset_req_admin FOREIGN KEY (completed_by) REFERENCES admin_users(admin_id)
);
GO

CREATE INDEX IX_reset_req_status_expires ON staff_password_reset_requests (status, expires_at);
CREATE INDEX IX_reset_req_staff_requested ON staff_password_reset_requests (staff_id, requested_at);
GO

EXEC sp_executesql N'
    DECLARE @cn NVARCHAR(128);
    SELECT TOP 1 @cn = cc.name
    FROM sys.check_constraints cc
    JOIN sys.tables t ON cc.parent_object_id = t.object_id
    WHERE t.name = N''auth_tokens'' AND cc.definition LIKE N''%token_type%'';
    IF @cn IS NOT NULL
        EXEC(''ALTER TABLE auth_tokens DROP CONSTRAINT ['' + @cn + '']'');
';
GO

ALTER TABLE auth_tokens ADD CONSTRAINT CK_auth_tokens_token_type
CHECK (token_type IN ('session','email_verification','password_reset','2fa_temp','refresh','limited_session'));
GO
