-- V12: Add title column to grammar_points table for UC-25.
-- Renumbered from a disabled V7 (duplicate version với V7__vocab_grammar_seed.sql).
-- Idempotent guard: ddl-auto=update có thể đã tạo cột này trong DB dev hiện tại.
IF COL_LENGTH('grammar_points', 'title') IS NULL
    ALTER TABLE grammar_points ADD title NVARCHAR(255) NULL;
