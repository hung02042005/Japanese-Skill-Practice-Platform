-- V4: Thêm trạng thái 'assigned' cho ticket (Staff Manager duyệt + gán cho Staff)
-- Luồng: open -> (Staff Manager gán) assigned -> (Staff hỗ trợ) in_progress -> resolved/closed

-- Bản SQL Server phải dò tên CHECK constraint tự sinh qua sys.check_constraints
-- rồi DROP bằng dynamic SQL (EXEC). MySQL không có sys.check_constraints, và
-- dynamic SQL thì cần stored procedure. Không cần cả hai: V1 đã đặt tên tường
-- minh CK_tickets_status cho constraint này, nên drop thẳng theo tên.
-- Yêu cầu MySQL >= 8.0.16 (có DROP CHECK, và CHECK được thực thi thật).

ALTER TABLE tickets
    DROP CHECK CK_tickets_status;

ALTER TABLE tickets
    ADD CONSTRAINT CK_tickets_status
        CHECK (status IN ('open','assigned','in_progress','resolved','closed'));
