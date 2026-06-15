/* ============================================================================
   V10 - Vocabulary topic catalog for learning path
   ----------------------------------------------------------------------------
   Adds first-class vocabulary topics so /api/vocabulary/path can return stable
   ids, slugs, Japanese/Vietnamese titles, order, and backend-computed progress.
   ============================================================================ */

USE JLPT_LearningDB;
GO

/* Self-heal: a prior Hibernate `ddl-auto: update` run may have auto-created an
   empty `vocabulary_topics` stub (varchar, no defaults) + an auto-named FK,
   conflicting with this migration. Drop the FK then the stub ONLY when empty,
   so the canonical nvarchar table below is created. Empty-only => no data loss. */
DECLARE @fk SYSNAME =
    (SELECT TOP 1 name FROM sys.foreign_keys WHERE referenced_object_id = OBJECT_ID('vocabulary_topics'));
IF @fk IS NOT NULL EXEC('ALTER TABLE vocabulary DROP CONSTRAINT ' + @fk);
GO

IF OBJECT_ID('vocabulary_topics', 'U') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM vocabulary_topics)
    DROP TABLE vocabulary_topics;
GO

IF OBJECT_ID('vocabulary_topics', 'U') IS NULL
CREATE TABLE vocabulary_topics (
    topic_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    jlpt_level    NVARCHAR(5)   NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    slug          NVARCHAR(80)  NOT NULL,
    title_ja      NVARCHAR(100) NOT NULL,
    title_vi      NVARCHAR(100) NOT NULL,
    display_order INT           NOT NULL,
    status        NVARCHAR(20)  NOT NULL DEFAULT 'published'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by    BIGINT        NULL,
    created_at    DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at    DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_vocab_topics_creator FOREIGN KEY (created_by) REFERENCES staff_users(staff_id),
    CONSTRAINT UQ_vocab_topics_level_slug UNIQUE (jlpt_level, slug),
    CONSTRAINT UQ_vocab_topics_level_title_vi UNIQUE (jlpt_level, title_vi)
);
GO

IF COL_LENGTH('vocabulary', 'topic_id') IS NULL
    ALTER TABLE vocabulary ADD topic_id BIGINT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_vocab_topic')
    ALTER TABLE vocabulary
    ADD CONSTRAINT FK_vocab_topic FOREIGN KEY (topic_id) REFERENCES vocabulary_topics(topic_id);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_vocab_topics_public_path' AND object_id = OBJECT_ID('vocabulary_topics'))
    CREATE INDEX IX_vocab_topics_public_path ON vocabulary_topics(status, jlpt_level, display_order);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_vocab_topic_id' AND object_id = OBJECT_ID('vocabulary'))
    CREATE INDEX IX_vocab_topic_id ON vocabulary(topic_id);
GO

DECLARE @staff_id BIGINT = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');

INSERT INTO vocabulary_topics (jlpt_level, slug, title_ja, title_vi, display_order, status, created_by)
SELECT v.jlpt_level, v.slug, v.title_ja, v.title_vi, v.display_order, 'published', @staff_id
FROM (VALUES
    ('N5', N'family',        N'家族',   N'Gia đình',   1),
    ('N5', N'food',          N'食べ物', N'Ẩm thực',    2),
    ('N5', N'transport',     N'交通',   N'Giao thông', 3),
    ('N5', N'time',          N'時間',   N'Thời gian',  4),
    ('N5', N'place',         N'場所',   N'Địa điểm',   5),
    ('N5', N'description',   N'描写',   N'Mô tả',      6),
    ('N5', N'weather',       N'天気',   N'Thời tiết',  7),
    ('N5', N'education',     N'学校',   N'Giáo dục',   8),
    ('N5', N'society',       N'社会',   N'Xã hội',     9),
    ('N5', N'technology',    N'技術',   N'Công nghệ',  10),

    ('N4', N'travel',        N'旅行',   N'Du lịch',    1),
    ('N4', N'emotion',       N'感情',   N'Cảm xúc',    2),
    ('N4', N'shopping',      N'買い物', N'Mua sắm',    3),
    ('N4', N'health',        N'健康',   N'Sức khỏe',   4),
    ('N4', N'education',     N'学校',   N'Giáo dục',   5),
    ('N4', N'family',        N'家族',   N'Gia đình',   6),

    ('N3', N'work',          N'仕事',   N'Công việc',  1),
    ('N3', N'society',       N'社会',   N'Xã hội',     2),
    ('N3', N'nature',        N'自然',   N'Thiên nhiên',3),
    ('N3', N'thinking',      N'思考',   N'Tư duy',     4),
    ('N3', N'communication', N'連絡',   N'Giao tiếp',  5),

    ('N2', N'academia',      N'学術',   N'Học thuật',  1),
    ('N2', N'economy',       N'経済',   N'Kinh tế',    2),
    ('N2', N'law',           N'法律',   N'Pháp luật',  3),
    ('N2', N'psychology',    N'心理',   N'Tâm lý',     4),

    ('N1', N'philosophy',    N'哲学',   N'Triết học',  1),
    ('N1', N'academia',      N'学術',   N'Học thuật',  2),
    ('N1', N'literature',    N'文学',   N'Văn học',    3),
    ('N1', N'technology',    N'技術',   N'Công nghệ',  4),
    ('N1', N'environment',   N'環境',   N'Môi trường', 5)
) AS v(jlpt_level, slug, title_ja, title_vi, display_order)
WHERE EXISTS (
    SELECT 1 FROM vocabulary
    WHERE vocabulary.jlpt_level = v.jlpt_level
      AND vocabulary.topic = v.title_vi
)
AND NOT EXISTS (
    SELECT 1 FROM vocabulary_topics t
    WHERE t.jlpt_level = v.jlpt_level
      AND t.slug = v.slug
);
GO

UPDATE v
SET topic_id = t.topic_id
FROM vocabulary v
JOIN vocabulary_topics t
  ON t.jlpt_level = v.jlpt_level
 AND t.title_vi = v.topic
WHERE v.topic_id IS NULL;
GO

PRINT N'V10 Vocabulary topic catalog and path backfill completed.';
GO
