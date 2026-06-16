-- V7: Add title column to grammar_points table for UC-25
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('grammar_points') AND name = 'title'
)
    ALTER TABLE grammar_points
        ADD title NVARCHAR(255) NULL;
