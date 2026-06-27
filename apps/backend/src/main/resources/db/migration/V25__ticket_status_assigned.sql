-- V4: Thêm trạng thái 'assigned' cho ticket (Staff Manager duyệt + gán cho Staff)
-- Luồng: open -> (Staff Manager gán) assigned -> (Staff hỗ trợ) in_progress -> resolved/closed

-- CHECK constraint cũ trên cột status là inline (tên do SQL Server tự sinh) -> tìm & drop trước
DECLARE @cn SYSNAME;
SELECT @cn = cc.name
FROM sys.check_constraints cc
JOIN sys.columns c
    ON c.object_id = cc.parent_object_id AND c.column_id = cc.parent_column_id
WHERE cc.parent_object_id = OBJECT_ID('dbo.tickets') AND c.name = 'status';

IF @cn IS NOT NULL
    EXEC('ALTER TABLE dbo.tickets DROP CONSTRAINT ' + @cn);
GO

ALTER TABLE dbo.tickets
    ADD CONSTRAINT CK_tickets_status
        CHECK (status IN ('open','assigned','in_progress','resolved','closed'));
GO
