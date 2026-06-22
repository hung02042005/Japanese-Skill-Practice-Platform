/* ============================================================
   V7 — Cho phép Student là actor của admin_audit_logs
   ------------------------------------------------------------
   Lý do: feat-assessment (UC-11) cần ghi audit log QUIZ_SUBMITTED
   do học viên thực hiện, nhưng admin_audit_logs trước đây chỉ
   cho phép actor là Admin hoặc Staff (CK_audit_actor).
   Lưu ý: IF NOT EXISTS/IF EXISTS bảo vệ migration idempotent.
   ============================================================ */

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('admin_audit_logs') AND name = 'student_actor_id'
)
    ALTER TABLE admin_audit_logs ADD student_actor_id BIGINT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_audit_student'
)
    ALTER TABLE admin_audit_logs
    ADD CONSTRAINT FK_audit_student FOREIGN KEY (student_actor_id) REFERENCES student_users(student_id);
GO

EXEC sp_executesql N'
    DECLARE @cn NVARCHAR(128);
    SELECT TOP 1 @cn = name FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID(''admin_audit_logs'') AND name = N''CK_audit_actor'';
    IF @cn IS NOT NULL
        EXEC(''ALTER TABLE admin_audit_logs DROP CONSTRAINT ['' + @cn + '']'');
';
GO

ALTER TABLE admin_audit_logs ADD CONSTRAINT CK_audit_actor CHECK (
    (CASE WHEN admin_actor_id IS NOT NULL THEN 1 ELSE 0 END +
     CASE WHEN staff_actor_id IS NOT NULL THEN 1 ELSE 0 END +
     CASE WHEN student_actor_id IS NOT NULL THEN 1 ELSE 0 END) = 1
);
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = 'IX_audit_student_date' AND object_id = OBJECT_ID('admin_audit_logs')
)
    CREATE INDEX IX_audit_student_date ON admin_audit_logs(student_actor_id, created_at);
GO
