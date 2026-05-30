/* ============================================================================
   JLPT LEARNING PLATFORM - DATABASE SCHEMA (SQL SERVER)
   ----------------------------------------------------------------------------
   Description : Full schema for the JLPT Japanese learning system
                 (Role-separated architecture, optimized to 23 tables)
   Supports    : UC-01 to UC-20 (Student) + UC-21 to UC-32 (Staff)
                 + UC-33 to UC-34 (StaffManager) + UC-35 to UC-40 (Admin)
   DBMS        : Microsoft SQL Server 2019+
   Version     : v2.4
   ============================================================================ */

-- =====================================================
-- 0. DATABASE INITIALIZATION
-- =====================================================
IF DB_ID('JLPT_LearningDB') IS NOT NULL
BEGIN
    ALTER DATABASE JLPT_LearningDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE JLPT_LearningDB;
END
GO

CREATE DATABASE JLPT_LearningDB;
GO

USE JLPT_LearningDB;
GO


/* ============================================================================
   I. USER & AUTHENTICATION TABLES
   (UC-01, UC-02, UC-03, UC-04, UC-05, UC-18 Student)
   (UC-22, UC-23 Staff | UC-35, UC-37 Admin)
   ============================================================================ */

-- 1. Admin Users
CREATE TABLE admin_users (
    admin_id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    email                NVARCHAR(255)   NOT NULL UNIQUE,
    password_hash        NVARCHAR(255)   NULL,                       -- NULL for OAuth login
    full_name            NVARCHAR(150)   NOT NULL,
    status               NVARCHAR(20)    NOT NULL DEFAULT 'active'
        CHECK (status IN ('active','suspended','pending','deleted')),
    suspend_reason       NVARCHAR(500)   NULL,
    email_verified_at    DATETIME2       NULL,
    -- Security & Authentication
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME2       NULL,
    last_login_at        DATETIME2       NULL,
    last_login_ip        NVARCHAR(45)    NULL,
    password_changed_at  DATETIME2       NULL,
    two_factor_enabled   BIT             NOT NULL DEFAULT 0,
    two_factor_secret    NVARCHAR(255)   NULL,

    created_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE INDEX IX_admins_status ON admin_users(status);
GO

-- 2. Staff Users (includes StaffManager via staff_role)
CREATE TABLE staff_users (
    staff_id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    email                NVARCHAR(255)   NOT NULL UNIQUE,
    password_hash        NVARCHAR(255)   NULL,
    full_name            NVARCHAR(150)   NOT NULL,
    staff_role           NVARCHAR(30)    NOT NULL DEFAULT 'staff'
        CHECK (staff_role IN ('staff','staff_manager')),
    status               NVARCHAR(20)    NOT NULL DEFAULT 'active'
        CHECK (status IN ('active','suspended','pending','deleted')),
    suspend_reason       NVARCHAR(500)   NULL,
    email_verified_at    DATETIME2       NULL,
    -- Security & Authentication
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME2       NULL,
    last_login_at        DATETIME2       NULL,
    last_login_ip        NVARCHAR(45)    NULL,
    password_changed_at  DATETIME2       NULL,

    created_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE INDEX IX_staffs_status ON staff_users(status);
CREATE INDEX IX_staffs_role_status ON staff_users(staff_role, status);
GO

-- 3. Student Users
CREATE TABLE student_users (
    student_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    email                NVARCHAR(255)   NOT NULL UNIQUE,
    password_hash        NVARCHAR(255)   NULL,                       -- NULL for OAuth login
    full_name            NVARCHAR(150)   NOT NULL,
    status               NVARCHAR(20)    NOT NULL DEFAULT 'active'
        CHECK (status IN ('active','suspended','pending','deleted')),
    suspend_reason       NVARCHAR(500)   NULL,
    email_verified_at    DATETIME2       NULL,
    avatar_url           NVARCHAR(500)   NULL,
    phone                NVARCHAR(20)    NULL,
    date_of_birth        DATE            NULL,                       -- UC-04 User Profile
    bio                  NVARCHAR(500)   NULL,                       -- UC-04 User Profile (short bio)

    -- OAuth fields (single identity per student)
    oauth_provider       NVARCHAR(30)    NULL
        CHECK (oauth_provider IN ('google','facebook','apple','github')),
    oauth_provider_id    NVARCHAR(255)   NULL,
    oauth_provider_email NVARCHAR(255)   NULL,                       -- Email returned from OAuth provider
    oauth_linked_at      DATETIME2       NULL,                       -- Timestamp when OAuth was linked

    -- JLPT Level
    current_jlpt_level   NVARCHAR(5)     NULL
        CHECK (current_jlpt_level IN ('N5','N4','N3','N2','N1')),
    target_jlpt_level    NVARCHAR(5)     NULL
        CHECK (target_jlpt_level IN ('N5','N4','N3','N2','N1')),

    -- Study streak statistics
    current_streak       INT             NOT NULL DEFAULT 0,
    longest_streak       INT             NOT NULL DEFAULT 0,
    last_activity_date   DATE            NULL,

    -- Security & Authentication
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME2       NULL,
    last_login_at        DATETIME2       NULL,
    last_login_ip        NVARCHAR(45)    NULL,
    password_changed_at  DATETIME2       NULL,

    created_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE INDEX IX_students_status ON student_users(status);
CREATE INDEX IX_students_jlpt_current ON student_users(current_jlpt_level);
CREATE UNIQUE INDEX UX_students_oauth ON student_users(oauth_provider, oauth_provider_id)
    WHERE oauth_provider IS NOT NULL AND oauth_provider_id IS NOT NULL;
GO

-- 4. Shared auth token table for Admin / Staff / Student
CREATE TABLE auth_tokens (
    token_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    actor_type      NVARCHAR(20)    NOT NULL
        CHECK (actor_type IN ('admin','staff','student')),
    admin_id        BIGINT          NULL,
    staff_id        BIGINT          NULL,
    student_id      BIGINT          NULL,
    token_type      NVARCHAR(30)    NOT NULL
        CHECK (token_type IN ('session','email_verification','password_reset','2fa_temp','refresh')),
    token_value     NVARCHAR(500)   NOT NULL,
    ip_address      NVARCHAR(45)    NULL,
    expires_at      DATETIME2       NOT NULL,
    revoked_at      DATETIME2       NULL,
    created_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_auth_tokens_admin   FOREIGN KEY (admin_id)   REFERENCES admin_users(admin_id)   ON DELETE CASCADE,
    CONSTRAINT FK_auth_tokens_staff   FOREIGN KEY (staff_id)   REFERENCES staff_users(staff_id)   ON DELETE CASCADE,
    CONSTRAINT FK_auth_tokens_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT CK_auth_token_actor CHECK (
        (actor_type = 'admin'   AND admin_id   IS NOT NULL AND staff_id IS NULL   AND student_id IS NULL) OR
        (actor_type = 'staff'   AND staff_id   IS NOT NULL AND admin_id IS NULL   AND student_id IS NULL) OR
        (actor_type = 'student' AND student_id IS NOT NULL AND admin_id IS NULL   AND staff_id   IS NULL)
    )
);
GO

CREATE INDEX IX_auth_tokens_value ON auth_tokens(token_value);
CREATE INDEX IX_auth_tokens_admin_active   ON auth_tokens(admin_id,   token_type, expires_at) WHERE actor_type = 'admin'   AND revoked_at IS NULL;
CREATE INDEX IX_auth_tokens_staff_active   ON auth_tokens(staff_id,   token_type, expires_at) WHERE actor_type = 'staff'   AND revoked_at IS NULL;
CREATE INDEX IX_auth_tokens_student_active ON auth_tokens(student_id, token_type, expires_at) WHERE actor_type = 'student' AND revoked_at IS NULL;
CREATE INDEX IX_auth_tokens_expiry ON auth_tokens(expires_at, revoked_at);
GO


/* ============================================================================
   II. LEARNING CONTENT TABLES
   (UC-06, UC-07, UC-08, UC-09, UC-14, UC-15, UC-16 Student)
   (UC-25, UC-27, UC-33, UC-34 Staff/StaffManager)
   ============================================================================ */

-- 5. Courses (JLPT learning course container) - UC-27, UC-33
CREATE TABLE courses (
    course_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    title            NVARCHAR(255)   NOT NULL,
    description      NVARCHAR(MAX)   NULL,
    jlpt_level       NVARCHAR(5)     NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    thumbnail_url    NVARCHAR(500)   NULL,
    is_vip_only      BIT             NOT NULL DEFAULT 0,             -- VIP subscription gate
    display_order    INT             NOT NULL DEFAULT 0,
    status           NVARCHAR(20)    NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by       BIGINT          NULL,
    approved_by      BIGINT          NULL,
    published_at     DATETIME2       NULL,
    created_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_courses_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_courses_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_courses_public_list ON courses(status, jlpt_level, display_order);
CREATE INDEX IX_courses_creator_status ON courses(created_by, status);
GO

-- 6. Lessons (covers lesson / reading / listening / speaking)
CREATE TABLE lessons (
    lesson_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    course_id        BIGINT          NULL,                       -- FK to courses (NULL = standalone)
    lesson_type      NVARCHAR(20)    NOT NULL DEFAULT 'lesson'
        CHECK (lesson_type IN ('lesson','reading','listening','speaking')),
    title            NVARCHAR(255)   NOT NULL,
    jlpt_level       NVARCHAR(5)     NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    content_text     NVARCHAR(MAX)   NULL,                      -- lecture body text
    video_url        NVARCHAR(500)   NULL,                      -- YouTube / Vimeo
    audio_url        NVARCHAR(500)   NULL,
    attachment_url   NVARCHAR(500)   NULL,                      -- PDF attachment
    explanation      NVARCHAR(MAX)   NULL,
    display_order    INT             NOT NULL DEFAULT 0,
    status           NVARCHAR(20)    NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by       BIGINT          NULL,
    approved_by      BIGINT          NULL,
    published_at     DATETIME2       NULL,
    created_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_lessons_course   FOREIGN KEY (course_id)   REFERENCES courses(course_id),
    CONSTRAINT FK_lessons_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_lessons_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_lessons_public_list   ON lessons(lesson_type, status, jlpt_level, display_order);
CREATE INDEX IX_lessons_creator_status ON lessons(created_by, status);
CREATE INDEX IX_lessons_course         ON lessons(course_id, display_order);
GO

-- 6. Kana Characters (Hiragana / Katakana) - UC-08
CREATE TABLE kana_characters (
    kana_id           INT IDENTITY(1,1) PRIMARY KEY,
    character_value   NVARCHAR(5)     NOT NULL,
    romaji            NVARCHAR(10)    NOT NULL,
    kana_type         NVARCHAR(15)    NOT NULL
        CHECK (kana_type IN ('hiragana','katakana')),
    audio_url         NVARCHAR(500)   NULL,
    stroke_order_url  NVARCHAR(500)   NULL,
    display_order     INT             NOT NULL DEFAULT 0
);
GO

-- 7. Kanji - UC-07
CREATE TABLE kanji (
    kanji_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    character_value   NVARCHAR(5)    NOT NULL UNIQUE,
    meaning           NVARCHAR(500)  NOT NULL,
    onyomi            NVARCHAR(200)  NULL,
    kunyomi           NVARCHAR(200)  NULL,
    stroke_count      INT            NULL,
    jlpt_level        NVARCHAR(5)    NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    stroke_order_url  NVARCHAR(500)  NULL,
    example_word      NVARCHAR(100)  NULL,
    example_reading   NVARCHAR(200)  NULL,
    example_meaning   NVARCHAR(500)  NULL,
    status            NVARCHAR(20)   NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by        BIGINT         NULL,
    approved_by       BIGINT         NULL,
    published_at      DATETIME2      NULL,
    created_at        DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at        DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_kanji_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_kanji_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_kanji_public_level ON kanji(status, jlpt_level);
GO

-- 8. Vocabulary - UC-09, UC-16
CREATE TABLE vocabulary (
    vocabulary_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    word                NVARCHAR(100)  NOT NULL,
    furigana            NVARCHAR(200)  NULL,
    meaning             NVARCHAR(500)  NOT NULL,
    word_type           NVARCHAR(50)   NULL,
    jlpt_level          NVARCHAR(5)    NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    topic               NVARCHAR(100)  NULL,
    audio_url           NVARCHAR(500)  NULL,
    example_sentence_jp NVARCHAR(MAX)  NULL,
    example_sentence_vi NVARCHAR(MAX)  NULL,
    lesson_id           BIGINT         NULL,
    status              NVARCHAR(20)   NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by          BIGINT         NULL,
    approved_by         BIGINT         NULL,
    published_at        DATETIME2      NULL,
    created_at          DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at          DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_vocab_lesson   FOREIGN KEY (lesson_id)   REFERENCES lessons(lesson_id),
    CONSTRAINT FK_vocab_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_vocab_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_vocab_public_lookup ON vocabulary(status, jlpt_level, topic);
CREATE INDEX IX_vocab_word ON vocabulary(word);
GO

-- 9. Grammar Points - UC-06, UC-24
CREATE TABLE grammar_points (
    grammar_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    structure           NVARCHAR(255)  NOT NULL,
    formula             NVARCHAR(500)  NULL,
    meaning             NVARCHAR(500)  NOT NULL,
    usage_explanation   NVARCHAR(MAX)  NULL,
    jlpt_level          NVARCHAR(5)    NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    example_sentence_jp NVARCHAR(MAX)  NULL,
    example_sentence_vi NVARCHAR(MAX)  NULL,
    lesson_id           BIGINT         NULL,
    status              NVARCHAR(20)   NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by          BIGINT         NULL,
    approved_by         BIGINT         NULL,
    published_at        DATETIME2      NULL,
    created_at          DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at          DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_grammar_lesson   FOREIGN KEY (lesson_id)   REFERENCES lessons(lesson_id),
    CONSTRAINT FK_grammar_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_grammar_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_grammar_public_level ON grammar_points(status, jlpt_level);
GO


/* ============================================================================
   III. QUESTION BANK - QUIZ - EXAM TABLES
   (UC-10, UC-11, UC-14, UC-15, UC-23, UC-26, UC-27, UC-31)
   ============================================================================ */

-- 10. Question Bank - UC-23
CREATE TABLE questions (
    question_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    question_text    NVARCHAR(MAX)  NOT NULL,
    question_type    NVARCHAR(30)   NOT NULL
        CHECK (question_type IN ('multiple_choice','fill_blank','true_false')),
    skill            NVARCHAR(30)   NOT NULL
        CHECK (skill IN ('vocabulary','grammar','kanji','reading','listening','mixed')),
    jlpt_level       NVARCHAR(5)    NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    explanation      NVARCHAR(MAX)  NULL,                       -- answer explanation
    audio_url        NVARCHAR(500)  NULL,                       -- for listening questions
    image_url        NVARCHAR(500)  NULL,
    option_a         NVARCHAR(MAX)  NULL,
    option_b         NVARCHAR(MAX)  NULL,
    option_c         NVARCHAR(MAX)  NULL,
    option_d         NVARCHAR(MAX)  NULL,
    correct_option   CHAR(1)        NULL
        CHECK (correct_option IN ('A','B','C','D')),
    correct_answer_text NVARCHAR(MAX) NULL,
    created_by       BIGINT         NULL,                       -- created by staff
    status           NVARCHAR(20)   NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    approved_by      BIGINT         NULL,
    published_at     DATETIME2      NULL,
    created_at       DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at       DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_questions_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_questions_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_questions_skill_level ON questions(skill, jlpt_level);
CREATE INDEX IX_questions_public_bank ON questions(status, skill, jlpt_level);
CREATE INDEX IX_questions_creator_status ON questions(created_by, status);
GO

-- 11. Assessments (Quiz + Exam combined) - UC-26, UC-31
CREATE TABLE assessments (
    assessment_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    assessment_type NVARCHAR(20)    NOT NULL
        CHECK (assessment_type IN ('quiz','exam')),
    title           NVARCHAR(255)   NOT NULL,
    lesson_id       BIGINT          NULL,
    topic           NVARCHAR(100)   NULL,
    jlpt_level      NVARCHAR(5)     NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    duration_min    INT             NULL,
    pass_score      INT             NULL,
    total_score     INT             NULL,
    audio_url       NVARCHAR(500)   NULL,
    status          NVARCHAR(20)    NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by      BIGINT          NULL,
    approved_by     BIGINT          NULL,
    published_at    DATETIME2       NULL,
    created_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_assessments_lesson   FOREIGN KEY (lesson_id)   REFERENCES lessons(lesson_id),
    CONSTRAINT FK_assessments_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_assessments_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_assessments_public_list ON assessments(assessment_type, status, jlpt_level, topic);
CREATE INDEX IX_assessments_lesson_status ON assessments(lesson_id, status);
CREATE INDEX IX_assessments_creator_status ON assessments(created_by, status);
GO

-- 12. Question Assignments (link questions to assessments or lessons)
CREATE TABLE question_assignments (
    assignment_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    parent_type     NVARCHAR(30)    NOT NULL
        CHECK (parent_type IN ('assessment','lesson')),
    parent_id       BIGINT          NOT NULL,                   -- assessment_id or lesson_id
    question_id     BIGINT          NOT NULL,
    section_name    NVARCHAR(100)   NULL,                       -- exam section: 'Reading', 'Listening'...
    score           DECIMAL(6,2)    NOT NULL DEFAULT 1,         -- points for this question
    display_order   INT             NOT NULL DEFAULT 0,

    CONSTRAINT FK_assign_question FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    CONSTRAINT UQ_assign UNIQUE (parent_type, parent_id, question_id)
);
GO

CREATE INDEX IX_assign_parent ON question_assignments(parent_type, parent_id);
GO


/* ============================================================================
   IV. ATTEMPT & SUBMISSION TABLES
   (UC-10, UC-11, UC-13, UC-14, UC-15, UC-27, UC-56)
   ============================================================================ */

-- 13. Test Attempts (quiz / exam / practice / reading / listening)
CREATE TABLE test_attempts (
    attempt_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id        BIGINT          NOT NULL,
    attempt_type      NVARCHAR(20)    NOT NULL
        CHECK (attempt_type IN ('exam','quiz','practice','reading','listening')),
    parent_type       NVARCHAR(30)    NOT NULL
        CHECK (parent_type IN ('assessment','lesson','random_practice')),
    parent_id         BIGINT          NULL,                     -- NULL for random_practice
    started_at        DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    submitted_at      DATETIME2       NULL,
    duration_seconds  INT             NULL,
    total_score       DECIMAL(8,2)    NULL,
    max_score         DECIMAL(8,2)    NULL,
    is_passed         BIT             NULL,
    language_knowledge_score DECIMAL(8,2) NULL,
    reading_score            DECIMAL(8,2) NULL,
    listening_score          DECIMAL(8,2) NULL,
    status            NVARCHAR(20)    NOT NULL DEFAULT 'in_progress'
        CHECK (status IN ('in_progress','submitted','auto_submitted','abandoned')),

    CONSTRAINT FK_attempt_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_attempt_student_type ON test_attempts(student_id, attempt_type);
CREATE INDEX IX_attempt_parent       ON test_attempts(parent_type, parent_id);
CREATE INDEX IX_attempt_student_status_date ON test_attempts(student_id, status, submitted_at);
GO

-- 14. Attempt Answers (per-question answer records)
CREATE TABLE attempt_answers (
    answer_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    attempt_id         BIGINT          NOT NULL,
    question_id        BIGINT          NOT NULL,
    selected_option    CHAR(1)         NULL
        CHECK (selected_option IN ('A','B','C','D')),
    answer_text        NVARCHAR(MAX)   NULL,                    -- for fill_blank questions
    is_correct         BIT             NULL,
    score              DECIMAL(6,2)    NULL,
    answered_at        DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_ans_attempt  FOREIGN KEY (attempt_id)  REFERENCES test_attempts(attempt_id) ON DELETE CASCADE,
    CONSTRAINT FK_ans_question FOREIGN KEY (question_id) REFERENCES questions(question_id)
);
GO

CREATE INDEX IX_ans_attempt ON attempt_answers(attempt_id);
GO

-- 15. Student Submissions (Speaking / Handwriting with AI & manual grading)
CREATE TABLE student_submissions (
    submission_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id        BIGINT          NOT NULL,
    submission_type   NVARCHAR(20)    NOT NULL
        CHECK (submission_type IN ('speaking','handwriting')),
    status            NVARCHAR(20)    NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending','ai_graded','graded','rejected')),
    exercise_id       BIGINT          NULL,                     -- FK to lessons(lesson_id) where lesson_type='speaking'
    recording_url     NVARCHAR(500)   NULL,
    duration_seconds  INT             NULL,

    -- AI grading (Speaking)
    ai_overall_score        DECIMAL(5,2)  NULL,                -- AI suggested total score
    ai_pronunciation_score  DECIMAL(5,2)  NULL,                -- AI pronunciation score
    ai_fluency_score        DECIMAL(5,2)  NULL,                -- AI fluency score
    ai_highlighted_errors   NVARCHAR(MAX) NULL,                -- Key errors detected by AI
    ai_suggestions          NVARCHAR(MAX) NULL,                -- AI improvement suggestions
    ai_graded_at            DATETIME2     NULL,                -- Timestamp AI completed grading

    -- OCR grading (Handwriting: Kanji / Kana)
    target_type             NVARCHAR(20)  NULL                 -- Type of character: 'kanji' | 'kana'
        CONSTRAINT CK_sub_target_type CHECK (target_type IN ('kanji','kana')),
    kanji_id                BIGINT        NULL,
    kana_id                 INT           NULL,
    handwriting_image_url   NVARCHAR(500) NULL,
    expected_character      NVARCHAR(5)   NULL,
    recognized_character    NVARCHAR(5)   NULL,
    similarity_percent      DECIMAL(5,2)  NULL,                -- ADR-007: similarity % only, no stroke analysis
    is_correct              BIT           NULL,
    ocr_processed_at        DATETIME2     NULL,

    -- Final score & manual grading (Staff)
    final_score             DECIMAL(5,2)  NULL,                -- Final score: manual_score ?? ai_overall_score
    manual_score            DECIMAL(5,2)  NULL,                -- Score set by Staff (overrides AI)
    manual_feedback         NVARCHAR(MAX) NULL,
    graded_by               BIGINT        NULL,
    graded_at               DATETIME2     NULL,

    submitted_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at        DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_sub_student  FOREIGN KEY (student_id)  REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_sub_exercise FOREIGN KEY (exercise_id) REFERENCES lessons(lesson_id),
    CONSTRAINT FK_sub_kanji    FOREIGN KEY (kanji_id)    REFERENCES kanji(kanji_id),
    CONSTRAINT FK_sub_kana     FOREIGN KEY (kana_id)     REFERENCES kana_characters(kana_id),
    CONSTRAINT FK_sub_grader   FOREIGN KEY (graded_by)   REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_sub_student_status ON student_submissions(student_id, status);
CREATE INDEX IX_sub_type_status    ON student_submissions(submission_type, status);
CREATE INDEX IX_sub_grader         ON student_submissions(graded_by, graded_at);
GO


/* ============================================================================
   V. PROGRESS - FLASHCARD - BOOKMARK TABLES
   (UC-06, UC-07, UC-08, UC-09, UC-12, UC-17, UC-19)
   ============================================================================ */

-- 16. Student Content Progress & Bookmarks
CREATE TABLE student_content_progress (
    progress_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id       BIGINT          NOT NULL,
    content_type     NVARCHAR(30)    NOT NULL
        CHECK (content_type IN ('lesson','vocabulary','kanji','kana','grammar')),
    content_id       BIGINT          NOT NULL,
    status           NVARCHAR(20)    NOT NULL DEFAULT 'learning'
        CHECK (status IN ('learning','completed','reviewing')),
    progress_percent DECIMAL(5,2)    NOT NULL DEFAULT 0,
    completed_at     DATETIME2       NULL,
    is_bookmarked    BIT             NOT NULL DEFAULT 0,
    bookmark_note    NVARCHAR(500)   NULL,
    bookmarked_at    DATETIME2       NULL,
    last_studied_at  DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    created_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_progress_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT UQ_progress UNIQUE (student_id, content_type, content_id)
);
GO

CREATE INDEX IX_progress_student_type ON student_content_progress(student_id, content_type);
CREATE INDEX IX_progress_bookmarks    ON student_content_progress(student_id, is_bookmarked, content_type);
GO

-- 17. Flashcards (deck + card + SRS state combined) - UC-12
CREATE TABLE flashcards (
    flashcard_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id       BIGINT          NULL,
    deck_name        NVARCHAR(255)   NOT NULL DEFAULT N'Default',
    is_system        BIT             NOT NULL DEFAULT 0,
    content_type     NVARCHAR(20)    NOT NULL
        CHECK (content_type IN ('kanji','vocabulary','grammar','custom')),
    content_id       BIGINT          NULL,
    front_text       NVARCHAR(MAX)   NULL,
    back_text        NVARCHAR(MAX)   NULL,
    last_rating      NVARCHAR(10)    NULL
        CHECK (last_rating IN ('easy','hard','wrong')),
    interval_days    INT             NOT NULL DEFAULT 1,         -- days until next review
    ease_factor      DECIMAL(5,2)    NOT NULL DEFAULT 2.50,      -- SM-2 factor: increases on easy, decreases on wrong
    repetition_count INT             NOT NULL DEFAULT 0,
    next_review_date DATE            NULL,
    last_reviewed_at DATETIME2       NULL,
    created_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_flashcard_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_flashcards_owner_deck  ON flashcards(student_id, deck_name);
CREATE INDEX IX_flashcards_next_review ON flashcards(student_id, next_review_date);
GO


/* ============================================================================
   VI. SUPPORT & NOTIFICATION TABLES
   (UC-21, UC-28, UC-42)
   ============================================================================ */

-- 18. Support Tickets - UC-21
CREATE TABLE tickets (
    ticket_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id      BIGINT          NOT NULL,                   -- submitted by student
    subject         NVARCHAR(255)   NOT NULL,
    content         NVARCHAR(MAX)   NOT NULL,
    category        NVARCHAR(50)    NULL,                       -- 'technical','content','account'...
    priority        NVARCHAR(20)    NOT NULL DEFAULT 'normal'
        CHECK (priority IN ('low','normal','high','urgent')),
    status          NVARCHAR(20)    NOT NULL DEFAULT 'open'
        CHECK (status IN ('open','in_progress','resolved','closed')),
    assigned_to     BIGINT          NULL,                       -- staff assigned
    last_reply_at   DATETIME2       NULL,
    created_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    resolved_at     DATETIME2       NULL,

    CONSTRAINT FK_tk_student  FOREIGN KEY (student_id)  REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_tk_assignee FOREIGN KEY (assigned_to) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_tk_status         ON tickets(status);
CREATE INDEX IX_tk_assignee       ON tickets(assigned_to, status);
CREATE INDEX IX_tk_student_status ON tickets(student_id, status);
GO

-- 19. Ticket Replies
CREATE TABLE ticket_replies (
    reply_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    ticket_id          BIGINT          NOT NULL,
    student_sender_id  BIGINT          NULL,
    staff_sender_id    BIGINT          NULL,
    message            NVARCHAR(MAX)   NOT NULL,
    attachment_url     NVARCHAR(500)   NULL,
    created_at         DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_rep_ticket         FOREIGN KEY (ticket_id)         REFERENCES tickets(ticket_id)      ON DELETE CASCADE,
    CONSTRAINT FK_rep_student_sender FOREIGN KEY (student_sender_id) REFERENCES student_users(student_id),
    CONSTRAINT FK_rep_staff_sender   FOREIGN KEY (staff_sender_id)   REFERENCES staff_users(staff_id),
    CONSTRAINT CK_replies_sender CHECK (
        (student_sender_id IS NOT NULL AND staff_sender_id IS NULL) OR
        (student_sender_id IS NULL     AND staff_sender_id IS NOT NULL)
    )
);
GO

CREATE INDEX IX_rep_ticket ON ticket_replies(ticket_id);
GO

-- 20. Notifications (one record per student) - UC-28, UC-42
CREATE TABLE notifications (
    notification_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id        BIGINT          NOT NULL,
    title             NVARCHAR(255)   NOT NULL,
    content           NVARCHAR(MAX)   NOT NULL,
    notification_type NVARCHAR(30)    NOT NULL DEFAULT 'news'
        CHECK (notification_type IN ('news','warning','promotion','system','achievement','reminder')),
    channel           NVARCHAR(30)    NOT NULL DEFAULT 'in_app'
        CHECK (channel IN ('in_app','email','both')),
    is_auto           BIT             NOT NULL DEFAULT 0,       -- sent by automatic rule
    rule_key          NVARCHAR(100)   NULL,                     -- automation rule key
    scheduled_at      DATETIME2       NULL,
    sent_at           DATETIME2       NULL,
    is_read           BIT             NOT NULL DEFAULT 0,
    read_at           DATETIME2       NULL,
    delivered_at      DATETIME2       NULL,
    admin_creator_id  BIGINT          NULL,
    staff_creator_id  BIGINT          NULL,
    created_at        DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_noti_student       FOREIGN KEY (student_id)       REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_noti_admin_creator FOREIGN KEY (admin_creator_id) REFERENCES admin_users(admin_id),
    CONSTRAINT FK_noti_staff_creator FOREIGN KEY (staff_creator_id) REFERENCES staff_users(staff_id),
    CONSTRAINT CK_noti_creator CHECK (
        (admin_creator_id IS NOT NULL AND staff_creator_id IS NULL) OR
        (admin_creator_id IS NULL     AND staff_creator_id IS NOT NULL) OR
        (admin_creator_id IS NULL     AND staff_creator_id IS NULL) -- system-generated
    )
);
GO

CREATE INDEX IX_notifications_schedule    ON notifications(scheduled_at, sent_at) WHERE sent_at IS NULL;
CREATE INDEX IX_notifications_student_read ON notifications(student_id, is_read, created_at);
GO


/* ============================================================================
   VII. SYSTEM CONFIGURATION & AUDIT LOG TABLES
   (UC-31 Admin, UC-40, UC-41, UC-42)
   ============================================================================ */

-- 21. System Settings (key-value store) - UC-41
CREATE TABLE system_settings (
    setting_id      INT IDENTITY(1,1) PRIMARY KEY,
    setting_group   NVARCHAR(50)    NOT NULL,                   -- 'general','smtp','security','auto_notification'
    setting_key     NVARCHAR(100)   NOT NULL,
    setting_value   NVARCHAR(MAX)   NULL,
    value_type      NVARCHAR(20)    NOT NULL DEFAULT 'string'
        CHECK (value_type IN ('string','integer','boolean','time')),
    is_editable     BIT             NOT NULL DEFAULT 1,
    updated_by      BIGINT          NULL,                       -- Admin who last updated
    updated_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT UQ_setting    UNIQUE (setting_group, setting_key),
    CONSTRAINT FK_setting_admin FOREIGN KEY (updated_by) REFERENCES admin_users(admin_id)
);
GO

-- 22. Admin Audit Logs (Admin / Staff / StaffManager actions)
CREATE TABLE admin_audit_logs (
    audit_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    admin_actor_id  BIGINT          NULL,                       -- Admin who performed action
    staff_actor_id  BIGINT          NULL,                       -- Staff who performed action
    action          NVARCHAR(100)   NOT NULL,                   -- 'create_user','suspend_user','update_setting'...
    target_table    NVARCHAR(100)   NULL,
    target_id       BIGINT          NULL,
    description     NVARCHAR(MAX)   NULL,
    ip_address      NVARCHAR(45)    NULL,
    created_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_audit_admin FOREIGN KEY (admin_actor_id) REFERENCES admin_users(admin_id),
    CONSTRAINT FK_audit_staff FOREIGN KEY (staff_actor_id) REFERENCES staff_users(staff_id),
    CONSTRAINT CK_audit_actor CHECK (
        (admin_actor_id IS NOT NULL AND staff_actor_id IS NULL) OR
        (admin_actor_id IS NULL     AND staff_actor_id IS NOT NULL)
    )
);
GO

CREATE INDEX IX_audit_admin_date ON admin_audit_logs(admin_actor_id, created_at);
CREATE INDEX IX_audit_staff_date ON admin_audit_logs(staff_actor_id, created_at);
GO


/* ============================================================================
   VIII. STATISTICS VIEWS
   (UC-19, UC-31, UC-40)
   ============================================================================ */

-- View: Student learning statistics summary
CREATE VIEW vw_student_learning_stats AS
SELECT
    s.student_id,
    s.full_name,
    s.email,
    s.current_jlpt_level,
    s.target_jlpt_level,
    s.current_streak,
    s.longest_streak,
    s.last_activity_date,

    -- Completed items
    (SELECT COUNT(*) FROM student_content_progress ucp
     WHERE ucp.student_id = s.student_id AND ucp.content_type='lesson'     AND ucp.status='completed') AS lessons_completed,
    (SELECT COUNT(*) FROM student_content_progress ucp
     WHERE ucp.student_id = s.student_id AND ucp.content_type='kanji'      AND ucp.status='completed') AS kanji_completed,
    (SELECT COUNT(*) FROM student_content_progress ucp
     WHERE ucp.student_id = s.student_id AND ucp.content_type='vocabulary' AND ucp.status='completed') AS vocabulary_completed,
    (SELECT COUNT(*) FROM student_content_progress ucp
     WHERE ucp.student_id = s.student_id AND ucp.content_type='grammar'    AND ucp.status='completed') AS grammar_completed,
    (SELECT COUNT(*) FROM student_content_progress ucp
     WHERE ucp.student_id = s.student_id AND ucp.content_type='kana'       AND ucp.status='completed') AS kana_completed,

    -- Exam / Quiz statistics
    (SELECT COUNT(*) FROM test_attempts t
     WHERE t.student_id = s.student_id AND t.attempt_type='exam' AND t.status IN ('submitted','auto_submitted')) AS total_exams_taken,
    (SELECT COUNT(*) FROM test_attempts t
     WHERE t.student_id = s.student_id AND t.attempt_type='quiz' AND t.status IN ('submitted','auto_submitted')) AS total_quizzes_taken,
    (SELECT MAX(t.total_score) FROM test_attempts t
     WHERE t.student_id = s.student_id AND t.attempt_type='exam') AS highest_exam_score,
    (SELECT AVG(t.total_score) FROM test_attempts t
     WHERE t.student_id = s.student_id AND t.attempt_type='exam') AS average_exam_score
FROM student_users s;
GO


/* ============================================================================
   IX. SEED DATA - SYSTEM SETTINGS
   ============================================================================ */

INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
('general',  'platform_name',       N'JLPT Learning Platform', 'string'),
('general',  'logo_url',            N'/assets/logo.png',       'string'),
('general',  'default_language',    N'vi',                     'string'),
('general',  'maintenance_mode',    N'false',                  'boolean'),
('general',  'allow_registration',  N'true',                   'boolean'),

('security', 'max_login_attempts',  N'5',                      'integer'),
('security', 'session_timeout_min', N'60',                     'integer'),
('security', 'password_reset_min',  N'15',                     'integer'),

('smtp',     'host',                N'smtp.gmail.com',         'string'),
('smtp',     'port',                N'587',                    'integer'),
('smtp',     'username',            N'',                       'string'),
('smtp',     'from_email',          N'noreply@jlpt.com',       'string'),

('auto_notification', 'streak_10_days_enabled',     N'true',                               'boolean'),
('auto_notification', 'streak_10_days_title',       N'Great job!',                         'string'),
('auto_notification', 'streak_10_days_template',    N'You have studied 10 days in a row!', 'string'),
('auto_notification', 'daily_flashcard_enabled',    N'true',                               'boolean'),
('auto_notification', 'daily_flashcard_time',       N'08:00',                              'time'),
('auto_notification', 'daily_flashcard_title',      N'Flashcard Review',                   'string'),
('auto_notification', 'daily_flashcard_template',   N'Time to review your flashcards!',    'string'),
('auto_notification', 'exam_result_ready_enabled',  N'true',                               'boolean'),
('auto_notification', 'exam_result_ready_title',    N'Exam Result Ready',                  'string'),
('auto_notification', 'exam_result_ready_template', N'Your exam score is now available.',  'string');
GO

PRINT N'Database JLPT_LearningDB initialized successfully (v2.3 - 22 tables).';
GO
