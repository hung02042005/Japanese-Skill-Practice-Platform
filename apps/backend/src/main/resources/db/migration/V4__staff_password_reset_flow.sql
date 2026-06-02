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

DECLARE @constraintName NVARCHAR(128)
DECLARE @sql NVARCHAR(500)

SELECT TOP 1 @constraintName = cc.name
FROM sys.check_constraints cc
JOIN sys.tables t ON cc.parent_object_id = t.object_id
WHERE t.name = 'auth_tokens'
  AND cc.definition LIKE '%token_type%'

IF @constraintName IS NOT NULL
BEGIN
    SET @sql = N'ALTER TABLE auth_tokens DROP CONSTRAINT [' + @constraintName + N']'
    EXEC sp_executesql @sql
END
GO

ALTER TABLE auth_tokens ADD CONSTRAINT CK_auth_tokens_token_type
CHECK (token_type IN ('session','email_verification','password_reset','2fa_temp','refresh','limited_session'));
GO
