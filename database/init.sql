/* ============================================================================
   JLPT LEARNING PLATFORM — SCHEMA KHỞI TẠO (SQL SERVER)
   ----------------------------------------------------------------------------
   Mô tả : Schema đầy đủ, phản ánh trạng thái hiện tại sau tất cả migration
           (V1 → V6).  Chạy lần đầu trên database trống đã được tạo sẵn.
   DBMS  : Microsoft SQL Server 2019+
   Phiên : v2.6
   ============================================================================ */

/* ============================================================================
   I. NGƯỜI DÙNG & XÁC THỰC
   ============================================================================ */

   create database JLPT_LearningDB
   go

   use JLPT_LearningDB
-- 1. Admin
CREATE TABLE admin_users (
    admin_id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    email                NVARCHAR(255)   NOT NULL UNIQUE,
    password_hash        NVARCHAR(255)   NULL,
    full_name            NVARCHAR(150)   NOT NULL,
    status               NVARCHAR(20)    NOT NULL DEFAULT 'active'
        CHECK (status IN ('active','suspended','pending','deleted')),
    suspend_reason       NVARCHAR(500)   NULL,
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME2       NULL,
    last_login_at        DATETIME2       NULL,
    two_factor_enabled   BIT             NOT NULL DEFAULT 0,
    two_factor_secret    NVARCHAR(255)   NULL,
    created_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE INDEX IX_admins_status ON admin_users(status);
GO

-- 2. Staff
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
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME2       NULL,
    last_login_at        DATETIME2       NULL,
    must_change_password BIT             NOT NULL DEFAULT 0,
    created_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE INDEX IX_staffs_status ON staff_users(status);
CREATE INDEX IX_staffs_role_status ON staff_users(staff_role, status);
GO

-- 3. Student
CREATE TABLE student_users (
    student_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    email                NVARCHAR(255)   NOT NULL UNIQUE,
    password_hash        NVARCHAR(255)   NULL,
    full_name            NVARCHAR(150)   NOT NULL,
    status               NVARCHAR(20)    NOT NULL DEFAULT 'active'
        CHECK (status IN ('active','suspended','pending','deleted')),
    suspend_reason       NVARCHAR(500)   NULL,
    email_verified_at    DATETIME2       NULL,
    avatar_url           NVARCHAR(500)   NULL,
    phone                NVARCHAR(20)    NULL,
    oauth_provider       NVARCHAR(30)    NULL
        CHECK (oauth_provider IN ('google','facebook','apple','github')),
    oauth_provider_id    NVARCHAR(255)   NULL,
    oauth_provider_email NVARCHAR(255)   NULL,
    oauth_linked_at      DATETIME2       NULL,
    current_jlpt_level   NVARCHAR(5)     NULL
        CHECK (current_jlpt_level IN ('N5','N4','N3','N2','N1')),
    target_jlpt_level    NVARCHAR(5)     NULL
        CHECK (target_jlpt_level IN ('N5','N4','N3','N2','N1')),
    current_streak       INT             NOT NULL DEFAULT 0,
    longest_streak       INT             NOT NULL DEFAULT 0,
    last_activity_date   DATE            NULL,
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME2       NULL,
    last_login_at        DATETIME2       NULL,
    created_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at           DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE INDEX IX_students_status ON student_users(status);
CREATE INDEX IX_students_jlpt_current ON student_users(current_jlpt_level);
CREATE UNIQUE INDEX UX_students_oauth ON student_users(oauth_provider, oauth_provider_id)
    WHERE oauth_provider IS NOT NULL AND oauth_provider_id IS NOT NULL;
GO

-- 4. Auth tokens (dùng chung Admin / Staff / Student)
CREATE TABLE auth_tokens (
    token_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    actor_type      NVARCHAR(20)    NOT NULL
        CHECK (actor_type IN ('admin','staff','student')),
    admin_id        BIGINT          NULL,
    staff_id        BIGINT          NULL,
    student_id      BIGINT          NULL,
    token_type      NVARCHAR(30)    NOT NULL
        CHECK (token_type IN ('session','email_verification','password_reset','2fa_temp','refresh','limited_session')),
    token_value     NVARCHAR(500)   NOT NULL,
    ip_address      NVARCHAR(45)    NULL,
    expires_at      DATETIME2       NOT NULL,
    revoked_at      DATETIME2       NULL,
    mfa_attempts    INT             NOT NULL DEFAULT 0,
    created_at      DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_auth_tokens_admin   FOREIGN KEY (admin_id)   REFERENCES admin_users(admin_id)   ON DELETE CASCADE,
    CONSTRAINT FK_auth_tokens_staff   FOREIGN KEY (staff_id)   REFERENCES staff_users(staff_id)   ON DELETE CASCADE,
    CONSTRAINT FK_auth_tokens_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT CK_auth_token_actor CHECK (
        (actor_type = 'admin'   AND admin_id   IS NOT NULL AND staff_id IS NULL     AND student_id IS NULL) OR
        (actor_type = 'staff'   AND staff_id   IS NOT NULL AND admin_id IS NULL     AND student_id IS NULL) OR
        (actor_type = 'student' AND student_id IS NOT NULL AND admin_id IS NULL     AND staff_id   IS NULL)
    )
);
GO

CREATE INDEX IX_auth_tokens_value          ON auth_tokens(token_value);
CREATE INDEX IX_auth_tokens_admin_active   ON auth_tokens(admin_id,   token_type, expires_at) WHERE actor_type = 'admin'   AND revoked_at IS NULL;
CREATE INDEX IX_auth_tokens_staff_active   ON auth_tokens(staff_id,   token_type, expires_at) WHERE actor_type = 'staff'   AND revoked_at IS NULL;
CREATE INDEX IX_auth_tokens_student_active ON auth_tokens(student_id, token_type, expires_at) WHERE actor_type = 'student' AND revoked_at IS NULL;
CREATE INDEX IX_auth_tokens_expiry         ON auth_tokens(expires_at, revoked_at);
GO

-- 5. Yêu cầu reset mật khẩu (Staff)
CREATE TABLE staff_password_reset_requests (
    request_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    staff_id        BIGINT          NOT NULL,
    status          NVARCHAR(20)    NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending','completed','expired','cancelled')),
    requested_at    DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    expires_at      DATETIME2       NOT NULL,
    completed_at    DATETIME2       NULL,
    completed_by    BIGINT          NULL,
    request_ip      NVARCHAR(45)    NULL,

    CONSTRAINT FK_reset_req_staff FOREIGN KEY (staff_id)     REFERENCES staff_users(staff_id),
    CONSTRAINT FK_reset_req_admin FOREIGN KEY (completed_by) REFERENCES admin_users(admin_id)
);
GO

CREATE INDEX IX_reset_req_status_expires  ON staff_password_reset_requests(status, expires_at);
CREATE INDEX IX_reset_req_staff_requested ON staff_password_reset_requests(staff_id, requested_at);
GO


/* ============================================================================
   II. NỘI DUNG HỌC TẬP
   ============================================================================ */

-- 6. Bài học
CREATE TABLE lessons (
    lesson_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    lesson_type      NVARCHAR(20)    NOT NULL DEFAULT 'lesson'
        CHECK (lesson_type IN ('lesson','reading','listening','speaking')),
    title            NVARCHAR(255)   NOT NULL,
    jlpt_level       NVARCHAR(5)     NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    content_text     NVARCHAR(MAX)   NULL,
    video_url        NVARCHAR(500)   NULL,
    audio_url        NVARCHAR(500)   NULL,
    attachment_url   NVARCHAR(500)   NULL,
    explanation      NVARCHAR(MAX)   NULL,
    display_order    INT             NOT NULL DEFAULT 0,
    status           NVARCHAR(20)    NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by       BIGINT          NULL,
    approved_by      BIGINT          NULL,
    published_at     DATETIME2       NULL,
    created_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at       DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_lessons_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_lessons_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_lessons_public_list    ON lessons(lesson_type, status, jlpt_level, display_order);
CREATE INDEX IX_lessons_creator_status ON lessons(created_by, status);
GO

-- 7. Kana
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

-- 8. Kanji
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

-- 9. Từ vựng
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
CREATE INDEX IX_vocab_word          ON vocabulary(word);
GO

-- 10. Ngữ pháp
CREATE TABLE grammar_points (
    grammar_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    title               NVARCHAR(255)  NULL,
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
   III. NGÂN HÀNG CÂU HỎI — QUIZ — ĐỀ THI
   ============================================================================ */

-- 11. Ngân hàng câu hỏi
CREATE TABLE questions (
    question_id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    question_text       NVARCHAR(MAX)  NOT NULL,
    question_type       NVARCHAR(30)   NOT NULL
        CHECK (question_type IN ('multiple_choice','fill_blank','true_false')),
    skill               NVARCHAR(30)   NOT NULL
        CHECK (skill IN ('vocabulary','grammar','kanji','reading','listening','mixed')),
    jlpt_level          NVARCHAR(5)    NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    explanation         NVARCHAR(MAX)  NULL,
    audio_url           NVARCHAR(500)  NULL,
    image_url           NVARCHAR(500)  NULL,
    option_a            NVARCHAR(MAX)  NULL,
    option_b            NVARCHAR(MAX)  NULL,
    option_c            NVARCHAR(MAX)  NULL,
    option_d            NVARCHAR(MAX)  NULL,
    correct_option      CHAR(1)        NULL
        CHECK (correct_option IN ('A','B','C','D')),
    correct_answer_text NVARCHAR(MAX)  NULL,
    created_by          BIGINT         NULL,
    status              NVARCHAR(20)   NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    approved_by         BIGINT         NULL,
    published_at        DATETIME2      NULL,
    created_at          DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at          DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_questions_creator  FOREIGN KEY (created_by)  REFERENCES staff_users(staff_id),
    CONSTRAINT FK_questions_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_questions_skill_level    ON questions(skill, jlpt_level);
CREATE INDEX IX_questions_public_bank    ON questions(status, skill, jlpt_level);
CREATE INDEX IX_questions_creator_status ON questions(created_by, status);
GO

-- 12. Assessments (quiz + exam)
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

CREATE INDEX IX_assessments_public_list    ON assessments(assessment_type, status, jlpt_level, topic);
CREATE INDEX IX_assessments_lesson_status  ON assessments(lesson_id, status);
CREATE INDEX IX_assessments_creator_status ON assessments(created_by, status);
GO

-- 13. Gán câu hỏi vào assessment / lesson
CREATE TABLE question_assignments (
    assignment_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    parent_type     NVARCHAR(30)    NOT NULL
        CHECK (parent_type IN ('assessment','lesson')),
    parent_id       BIGINT          NOT NULL,
    question_id     BIGINT          NOT NULL,
    section_name    NVARCHAR(100)   NULL,
    score           DECIMAL(6,2)    NOT NULL DEFAULT 1,
    display_order   INT             NOT NULL DEFAULT 0,

    CONSTRAINT FK_assign_question FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    CONSTRAINT UQ_assign UNIQUE (parent_type, parent_id, question_id)
);
GO

CREATE INDEX IX_assign_parent ON question_assignments(parent_type, parent_id);
GO


/* ============================================================================
   IV. LÀM BÀI — KẾT QUẢ — BÀI NỘP
   ============================================================================ */

-- 14. Lần làm bài
CREATE TABLE test_attempts (
    attempt_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id        BIGINT          NOT NULL,
    attempt_type      NVARCHAR(20)    NOT NULL
        CHECK (attempt_type IN ('exam','quiz','practice','reading','listening')),
    parent_type       NVARCHAR(30)    NOT NULL
        CHECK (parent_type IN ('assessment','lesson','random_practice')),
    parent_id         BIGINT          NULL,
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

CREATE INDEX IX_attempt_student_type        ON test_attempts(student_id, attempt_type);
CREATE INDEX IX_attempt_parent              ON test_attempts(parent_type, parent_id);
CREATE INDEX IX_attempt_student_status_date ON test_attempts(student_id, status, submitted_at);
GO

-- 15. Đáp án từng câu
CREATE TABLE attempt_answers (
    answer_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    attempt_id      BIGINT          NOT NULL,
    question_id     BIGINT          NOT NULL,
    selected_option CHAR(1)         NULL
        CHECK (selected_option IN ('A','B','C','D')),
    answer_text     NVARCHAR(MAX)   NULL,
    is_correct      BIT             NULL,
    score           DECIMAL(6,2)    NULL,
    answered_at     DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_ans_attempt  FOREIGN KEY (attempt_id)  REFERENCES test_attempts(attempt_id) ON DELETE CASCADE,
    CONSTRAINT FK_ans_question FOREIGN KEY (question_id) REFERENCES questions(question_id)
);
GO

CREATE INDEX IX_ans_attempt ON attempt_answers(attempt_id);
GO

-- 16. Bài nộp (speaking / handwriting)
CREATE TABLE student_submissions (
    submission_id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id               BIGINT          NOT NULL,
    submission_type          NVARCHAR(20)    NOT NULL
        CHECK (submission_type IN ('speaking','handwriting')),
    status                   NVARCHAR(20)    NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending','ai_graded','graded','rejected')),
    exercise_id              BIGINT          NULL,
    recording_url            NVARCHAR(500)   NULL,
    duration_seconds         INT             NULL,
    ai_overall_score         DECIMAL(5,2)    NULL,
    ai_pronunciation_score   DECIMAL(5,2)    NULL,
    ai_fluency_score         DECIMAL(5,2)    NULL,
    ai_error_summary         NVARCHAR(MAX)   NULL,
    ai_suggestions           NVARCHAR(MAX)   NULL,
    ai_graded_at             DATETIME2       NULL,
    kanji_id                 BIGINT          NULL,
    kana_id                  INT             NULL,
    handwriting_image_url    NVARCHAR(500)   NULL,
    expected_character       NVARCHAR(5)     NULL,
    recognized_character     NVARCHAR(5)     NULL,
    similarity_percent       DECIMAL(5,2)    NULL,
    is_correct               BIT             NULL,
    ocr_processed_at         DATETIME2       NULL,
    manual_score             DECIMAL(5,2)    NULL,
    manual_feedback          NVARCHAR(MAX)   NULL,
    graded_by                BIGINT          NULL,
    graded_at                DATETIME2       NULL,
    submitted_at             DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at               DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

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
   V. TIẾN ĐỘ — FLASHCARD — BOOKMARK
   ============================================================================ */

-- 17. Tiến độ học + bookmark
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

-- 18. Flashcard + SRS
CREATE TABLE flashcards (
    flashcard_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id       BIGINT          NULL,
    deck_name        NVARCHAR(255)   NOT NULL DEFAULT N'Mặc định',
    is_system        BIT             NOT NULL DEFAULT 0,
    content_type     NVARCHAR(20)    NOT NULL
        CHECK (content_type IN ('kanji','vocabulary','grammar','custom')),
    content_id       BIGINT          NULL,
    front_text       NVARCHAR(MAX)   NULL,
    back_text        NVARCHAR(MAX)   NULL,
    last_rating      NVARCHAR(10)    NULL
        CHECK (last_rating IN ('easy','hard','wrong')),
    interval_days    INT             NOT NULL DEFAULT 1,
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
   VI. HỖ TRỢ — THÔNG BÁO
   ============================================================================ */

-- 19. Ticket hỗ trợ
CREATE TABLE tickets (
    ticket_id    BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id   BIGINT          NOT NULL,
    subject      NVARCHAR(255)   NOT NULL,
    content      NVARCHAR(MAX)   NOT NULL,
    category     NVARCHAR(50)    NULL,
    priority     NVARCHAR(20)    NOT NULL DEFAULT 'normal'
        CHECK (priority IN ('low','normal','high','urgent')),
    status       NVARCHAR(20)    NOT NULL DEFAULT 'open'
        CHECK (status IN ('open','in_progress','resolved','closed')),
    assigned_to  BIGINT          NULL,
    last_reply_at DATETIME2      NULL,
    created_at   DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),
    resolved_at  DATETIME2       NULL,

    CONSTRAINT FK_tk_student  FOREIGN KEY (student_id)  REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_tk_assignee FOREIGN KEY (assigned_to) REFERENCES staff_users(staff_id)
);
GO

CREATE INDEX IX_tk_status         ON tickets(status);
CREATE INDEX IX_tk_assignee       ON tickets(assigned_to, status);
CREATE INDEX IX_tk_student_status ON tickets(student_id, status);
GO

-- 20. Phản hồi ticket
CREATE TABLE ticket_replies (
    reply_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    ticket_id         BIGINT          NOT NULL,
    student_sender_id BIGINT          NULL,
    staff_sender_id   BIGINT          NULL,
    message           NVARCHAR(MAX)   NOT NULL,
    attachment_url    NVARCHAR(500)   NULL,
    created_at        DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_rep_ticket         FOREIGN KEY (ticket_id)         REFERENCES tickets(ticket_id) ON DELETE CASCADE,
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

-- 21. Thông báo
CREATE TABLE notifications (
    notification_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id        BIGINT          NOT NULL,
    title             NVARCHAR(255)   NOT NULL,
    content           NVARCHAR(MAX)   NOT NULL,
    notification_type NVARCHAR(30)    NOT NULL DEFAULT 'news'
        CHECK (notification_type IN ('news','warning','promotion','system','achievement','reminder')),
    channel           NVARCHAR(30)    NOT NULL DEFAULT 'in_app'
        CHECK (channel IN ('in_app','email','both')),
    is_auto           BIT             NOT NULL DEFAULT 0,
    rule_key          NVARCHAR(100)   NULL,
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
        (admin_creator_id IS NULL     AND staff_creator_id IS NULL)
    )
);
GO

CREATE INDEX IX_notifications_schedule    ON notifications(scheduled_at, sent_at) WHERE sent_at IS NULL;
CREATE INDEX IX_notifications_student_read ON notifications(student_id, is_read, created_at);
GO


/* ============================================================================
   VII. CẤU HÌNH HỆ THỐNG — AUDIT LOG
   ============================================================================ */

-- 22. System settings
CREATE TABLE system_settings (
    setting_id    INT IDENTITY(1,1) PRIMARY KEY,
    setting_group NVARCHAR(50)    NOT NULL,
    setting_key   NVARCHAR(100)   NOT NULL,
    setting_value NVARCHAR(MAX)   NULL,
    value_type    NVARCHAR(20)    NOT NULL DEFAULT 'string'
        CHECK (value_type IN ('string','integer','boolean','time')),
    is_editable   BIT             NOT NULL DEFAULT 1,
    updated_by    BIGINT          NULL,
    updated_at    DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT UQ_setting    UNIQUE (setting_group, setting_key),
    CONSTRAINT FK_setting_admin FOREIGN KEY (updated_by) REFERENCES admin_users(admin_id)
);
GO

-- 23. Audit log
CREATE TABLE admin_audit_logs (
    audit_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    admin_actor_id BIGINT          NULL,
    staff_actor_id BIGINT          NULL,
    action         NVARCHAR(100)   NOT NULL,
    target_table   NVARCHAR(100)   NULL,
    target_id      BIGINT          NULL,
    description    NVARCHAR(MAX)   NULL,
    ip_address     NVARCHAR(45)    NULL,
    created_at     DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME(),

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
   VIII. VIEW THỐNG KÊ
   ============================================================================ */

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
    (SELECT COUNT(*) FROM student_content_progress p WHERE p.student_id = s.student_id AND p.content_type = 'lesson'     AND p.status = 'completed') AS lessons_completed,
    (SELECT COUNT(*) FROM student_content_progress p WHERE p.student_id = s.student_id AND p.content_type = 'kanji'      AND p.status = 'completed') AS kanji_completed,
    (SELECT COUNT(*) FROM student_content_progress p WHERE p.student_id = s.student_id AND p.content_type = 'vocabulary' AND p.status = 'completed') AS vocabulary_completed,
    (SELECT COUNT(*) FROM student_content_progress p WHERE p.student_id = s.student_id AND p.content_type = 'grammar'    AND p.status = 'completed') AS grammar_completed,
    (SELECT COUNT(*) FROM student_content_progress p WHERE p.student_id = s.student_id AND p.content_type = 'kana'       AND p.status = 'completed') AS kana_completed,
    (SELECT COUNT(*)    FROM test_attempts t WHERE t.student_id = s.student_id AND t.attempt_type = 'exam' AND t.status IN ('submitted','auto_submitted')) AS total_exams_taken,
    (SELECT COUNT(*)    FROM test_attempts t WHERE t.student_id = s.student_id AND t.attempt_type = 'quiz' AND t.status IN ('submitted','auto_submitted')) AS total_quizzes_taken,
    (SELECT MAX(t.total_score) FROM test_attempts t WHERE t.student_id = s.student_id AND t.attempt_type = 'exam') AS highest_exam_score,
    (SELECT AVG(t.total_score) FROM test_attempts t WHERE t.student_id = s.student_id AND t.attempt_type = 'exam') AS average_exam_score
FROM student_users s;
GO


/* ============================================================================
   IX. DỮ LIỆU MẪU — SYSTEM SETTINGS
   ============================================================================ */

INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
('general',           'platform_name',              N'JLPT Learning Platform',         'string'),
('general',           'logo_url',                   N'/assets/logo.png',               'string'),
('general',           'default_language',           N'vi',                             'string'),
('general',           'maintenance_mode',           N'false',                          'boolean'),
('general',           'allow_registration',         N'true',                           'boolean'),
('security',          'max_login_attempts',         N'5',                              'integer'),
('security',          'session_timeout_min',        N'60',                             'integer'),
('security',          'password_reset_min',         N'15',                             'integer'),
('smtp',              'host',                       N'smtp.gmail.com',                 'string'),
('smtp',              'port',                       N'587',                            'integer'),
('smtp',              'username',                   N'',                               'string'),
('smtp',              'from_email',                 N'noreply@jlpt.com',               'string'),
('auto_notification', 'streak_10_days_enabled',     N'true',                           'boolean'),
('auto_notification', 'streak_10_days_title',       N'Tuyệt vời!',                     'string'),
('auto_notification', 'streak_10_days_template',    N'Bạn đã học liên tiếp 10 ngày!',  'string'),
('auto_notification', 'daily_flashcard_enabled',    N'true',                           'boolean'),
('auto_notification', 'daily_flashcard_time',       N'08:00',                          'time'),
('auto_notification', 'daily_flashcard_title',      N'Ôn flashcard',                   'string'),
('auto_notification', 'daily_flashcard_template',   N'Đến giờ ôn flashcard rồi!',      'string'),
('auto_notification', 'exam_result_ready_enabled',  N'true',                           'boolean'),
('auto_notification', 'exam_result_ready_title',    N'Kết quả thi',                    'string'),
('auto_notification', 'exam_result_ready_template', N'Điểm bài thi của bạn đã có',     'string');
GO

PRINT N'✅ Schema JLPT_LearningDB v2.6 đã khởi tạo thành công.';
GO


