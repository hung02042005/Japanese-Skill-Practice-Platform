/* ============================================================================
   V20 - Vocabulary topic = single source of truth (topic_id)
   ----------------------------------------------------------------------------
   Làm lại mô hình chủ đề từ vựng: topic_id (FK -> vocabulary_topics) trở thành
   khoá DUY NHẤT. Backfill mọi vocab về topic_id, rồi siết NOT NULL và BỎ cột
   free-text 'topic'. Sau migration entity Vocabulary (đã bỏ field topic) khớp
   schema để ddl-auto=validate pass.
   ============================================================================ */

USE JLPT_LearningDB;
GO

/* 1. Backfill topic_id từ free-text 'topic' khớp title_vi của catalog (nếu cột còn). */
IF COL_LENGTH('vocabulary', 'topic') IS NOT NULL
EXEC('
    UPDATE v
    SET v.topic_id = t.topic_id
    FROM vocabulary v
    JOIN vocabulary_topics t
      ON t.jlpt_level = v.jlpt_level
     AND t.title_vi = v.topic
    WHERE v.topic_id IS NULL AND v.topic IS NOT NULL;
');
GO

/* 2. Free-text 'topic' chưa có trong catalog -> tạo catalog row (slug suy ra) rồi link.
   created_by = NULL (FK cho phép) để khỏi phụ thuộc staff seed. slug suy ra deterministic
   từ title_vi; (jlpt_level, title_vi) vốn distinct nên rủi ro trùng slug rất thấp. */
IF COL_LENGTH('vocabulary', 'topic') IS NOT NULL
EXEC('
    INSERT INTO vocabulary_topics (jlpt_level, slug, title_ja, title_vi, display_order, status, created_by)
    SELECT o.jlpt_level,
           LOWER(REPLACE(REPLACE(o.title_vi, '' '', ''-''), ''/'', ''-'')),
           o.title_vi,
           o.title_vi,
           1000 + ROW_NUMBER() OVER (PARTITION BY o.jlpt_level ORDER BY o.title_vi),
           ''published'',
           NULL
    FROM (
        SELECT DISTINCT v.jlpt_level, v.topic AS title_vi
        FROM vocabulary v
        WHERE v.topic_id IS NULL
          AND v.topic IS NOT NULL AND LTRIM(RTRIM(v.topic)) <> ''''
          AND NOT EXISTS (
              SELECT 1 FROM vocabulary_topics t
              WHERE t.jlpt_level = v.jlpt_level AND t.title_vi = v.topic)
    ) o;
');
GO

IF COL_LENGTH('vocabulary', 'topic') IS NOT NULL
EXEC('
    UPDATE v
    SET v.topic_id = t.topic_id
    FROM vocabulary v
    JOIN vocabulary_topics t
      ON t.jlpt_level = v.jlpt_level
     AND t.title_vi = v.topic
    WHERE v.topic_id IS NULL AND v.topic IS NOT NULL;
');
GO

/* 3. Vocab không có chủ đề -> chủ đề "Khác" (slug 'other') theo từng cấp độ. */
INSERT INTO vocabulary_topics (jlpt_level, slug, title_ja, title_vi, display_order, status, created_by)
SELECT DISTINCT v.jlpt_level, 'other', N'その他', N'Khác', 9999, 'published', NULL
FROM vocabulary v
WHERE v.topic_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM vocabulary_topics t
      WHERE t.jlpt_level = v.jlpt_level AND t.slug = 'other');
GO

UPDATE v
SET v.topic_id = t.topic_id
FROM vocabulary v
JOIN vocabulary_topics t
  ON t.jlpt_level = v.jlpt_level
 AND t.slug = 'other'
WHERE v.topic_id IS NULL;
GO

/* 4. Mọi vocab giờ đã có topic_id -> siết NOT NULL.
   Phải DROP index IX_vocab_topic_id (tạo ở V10) trước vì SQL Server không cho
   ALTER COLUMN đang nằm trong index; tạo lại index sau khi siết NOT NULL. */
IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_vocab_topic_id' AND object_id = OBJECT_ID('vocabulary'))
    DROP INDEX IX_vocab_topic_id ON vocabulary;
GO

ALTER TABLE vocabulary ALTER COLUMN topic_id BIGINT NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_vocab_topic_id' AND object_id = OBJECT_ID('vocabulary'))
    CREATE INDEX IX_vocab_topic_id ON vocabulary(topic_id);
GO

/* 5. Bỏ hẳn cột free-text 'topic' (đã thay bằng topic_id).
   Phải DROP index IX_vocab_public_lookup (V1, có cột topic) trước khi drop cột;
   tạo lại index theo mô hình mới (status, jlpt_level, topic_id). */
IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_vocab_public_lookup' AND object_id = OBJECT_ID('vocabulary'))
    DROP INDEX IX_vocab_public_lookup ON vocabulary;
GO

IF COL_LENGTH('vocabulary', 'topic') IS NOT NULL
    ALTER TABLE vocabulary DROP COLUMN topic;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_vocab_public_lookup' AND object_id = OBJECT_ID('vocabulary'))
    CREATE INDEX IX_vocab_public_lookup ON vocabulary(status, jlpt_level, topic_id);
GO

PRINT N'V20 Vocabulary topic_id required + free-text topic column dropped.';
GO
