-- V7: Add title column to grammar_points table for UC-25
ALTER TABLE grammar_points
    ADD title NVARCHAR(255) NULL;
