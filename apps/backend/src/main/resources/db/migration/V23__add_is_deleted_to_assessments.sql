/* ============================================================
   V10 — Thêm soft delete cho bảng assessments
   ------------------------------------------------------------
   Lý do: StaffAssessmentController.deleteAssessment() đang dùng
   hard delete (deleteById). Theo AGENTS.md § 7.4, phải soft delete.
   Idempotent: IF NOT EXISTS bảo vệ migration có thể chạy lại an toàn.
   ============================================================ */

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('assessments') AND name = 'is_deleted'
)
    ALTER TABLE assessments ADD is_deleted BIT NOT NULL DEFAULT 0;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_assessments_not_deleted' AND object_id = OBJECT_ID('assessments')
)
    CREATE INDEX IX_assessments_not_deleted
        ON assessments(is_deleted, assessment_type, status);
GO

PRINT N'✅ V10: Added is_deleted column to assessments table.';
GO
