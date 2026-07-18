/* ============================================================================
   JLPT LEARNING PLATFORM - DATABASE SCHEMA (MySQL)
   ----------------------------------------------------------------------------
   Squash của V1-V25 cũ (DDL only). Toàn bộ dữ liệu seed/mock nằm ở V2.
   DBMS:   MySQL 8.0.16+ (bắt buộc — CHECK constraint chỉ được thực thi từ 8.0.16)
   ----------------------------------------------------------------------------
   Charset: utf8mb4 / utf8mb4_unicode_ci trên mọi bảng — bắt buộc cho nội dung
            tiếng Nhật (kanji, kana) và emoji. Không dựa vào default của server.
   Thời gian: cột DATETIME(6) + DEFAULT CURRENT_TIMESTAMP(6). Container MySQL
            chạy --default-time-zone=+00:00 nên CURRENT_TIMESTAMP = UTC, giữ
            đúng ngữ nghĩa SYSUTCDATETIME() của bản SQL Server cũ.
   ============================================================================ */

/* ============================================================================
   I. NHÓM BẢNG: NGƯỜI DÙNG & XÁC THỰC
   ============================================================================ */

-- 1. Quản trị viên (Admin)
CREATE TABLE admin_users (
    admin_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                VARCHAR(255)    NOT NULL UNIQUE,
    password_hash        VARCHAR(255)    NULL,
    full_name            VARCHAR(150)    NOT NULL,
    status               VARCHAR(20)     NOT NULL DEFAULT 'active'
        CHECK (status IN ('active','suspended','pending','deleted')),
    suspend_reason       VARCHAR(500)    NULL,
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME(6)     NULL,
    last_login_at        DATETIME(6)     NULL,
    created_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_admins_status ON admin_users(status);

-- 2. Nhân viên (Staff)
CREATE TABLE staff_users (
    staff_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                VARCHAR(255)    NOT NULL UNIQUE,
    password_hash        VARCHAR(255)    NULL,
    full_name            VARCHAR(150)    NOT NULL,
    staff_role           VARCHAR(30)     NOT NULL DEFAULT 'staff'
        CHECK (staff_role IN ('staff','staff_manager')),
    status               VARCHAR(20)     NOT NULL DEFAULT 'active'
        CHECK (status IN ('active','suspended','pending','deleted')),
    suspend_reason       VARCHAR(500)    NULL,
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME(6)     NULL,
    last_login_at        DATETIME(6)     NULL,
    must_change_password BOOLEAN         NOT NULL DEFAULT 0,
    created_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_staffs_status ON staff_users(status);
CREATE INDEX IX_staffs_role_status ON staff_users(staff_role, status);

-- 2b. Yêu cầu reset mật khẩu của Staff (do Admin xử lý)
CREATE TABLE staff_password_reset_requests (
    request_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id        BIGINT          NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'completed', 'expired', 'cancelled')),
    requested_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at      DATETIME(6)     NOT NULL,
    completed_at    DATETIME(6)     NULL,
    completed_by    BIGINT          NULL,
    request_ip      VARCHAR(45)     NULL,

    CONSTRAINT FK_reset_req_staff FOREIGN KEY (staff_id) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_reset_req_admin FOREIGN KEY (completed_by) REFERENCES admin_users(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_reset_req_status_expires ON staff_password_reset_requests (status, expires_at);
CREATE INDEX IX_reset_req_staff_requested ON staff_password_reset_requests (staff_id, requested_at);

-- 3. Học viên (Students)
CREATE TABLE student_users (
    student_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                VARCHAR(255)    NOT NULL UNIQUE,
    password_hash        VARCHAR(255)    NULL,
    full_name            VARCHAR(150)    NOT NULL,
    status               VARCHAR(20)     NOT NULL DEFAULT 'active'
        CHECK (status IN ('active','suspended','pending','deleted')),
    suspend_reason       VARCHAR(500)    NULL,
    email_verified_at    DATETIME(6)     NULL,
    avatar_url           VARCHAR(500)    NULL,
    phone                VARCHAR(20)     NULL,
    oauth_provider       VARCHAR(30)     NULL
        CHECK (oauth_provider IN ('google','facebook','apple','github')),
    oauth_provider_id    VARCHAR(255)    NULL,
    oauth_provider_email VARCHAR(255)    NULL,
    oauth_linked_at      DATETIME(6)     NULL,
    current_jlpt_level   VARCHAR(5)      NULL
        CHECK (current_jlpt_level IN ('N5','N4','N3','N2','N1')),
    target_jlpt_level    VARCHAR(5)      NULL
        CHECK (target_jlpt_level IN ('N5','N4','N3','N2','N1')),
    current_streak       INT             NOT NULL DEFAULT 0,
    longest_streak       INT             NOT NULL DEFAULT 0,
    last_activity_date   DATE            NULL,
    login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until         DATETIME(6)     NULL,
    last_login_at        DATETIME(6)     NULL,
    created_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_students_status ON student_users(status);
CREATE INDEX IX_students_jlpt_current ON student_users(current_jlpt_level);

-- Bản SQL Server dùng filtered index:
--   WHERE oauth_provider IS NOT NULL AND oauth_provider_id IS NOT NULL
-- MySQL không hỗ trợ filtered index, nhưng KHÔNG cần workaround ở đây: unique
-- index của MySQL bỏ qua mọi dòng có cột NULL, nên các tài khoản không dùng
-- OAuth (oauth_provider NULL) vẫn không đụng nhau — đúng y hệt ngữ nghĩa cũ.
CREATE UNIQUE INDEX UX_students_oauth ON student_users(oauth_provider, oauth_provider_id);

-- 4. Token xác thực dùng chung cho Admin / Staff / Student
CREATE TABLE auth_tokens (
    token_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_type      VARCHAR(20)     NOT NULL
        CHECK (actor_type IN ('admin','staff','student')),
    admin_id        BIGINT          NULL,
    staff_id        BIGINT          NULL,
    student_id      BIGINT          NULL,
    token_type      VARCHAR(30)     NOT NULL
        CHECK (token_type IN ('session','email_verification','password_reset','refresh','limited_session')),
    token_value     VARCHAR(500)    NOT NULL,
    ip_address      VARCHAR(45)     NULL,
    expires_at      DATETIME(6)     NOT NULL,
    revoked_at      DATETIME(6)     NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_auth_tokens_admin FOREIGN KEY (admin_id) REFERENCES admin_users(admin_id) ON DELETE CASCADE,
    CONSTRAINT FK_auth_tokens_staff FOREIGN KEY (staff_id) REFERENCES staff_users(staff_id) ON DELETE CASCADE,
    CONSTRAINT FK_auth_tokens_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT CK_auth_token_actor CHECK (
        (actor_type = 'admin' AND admin_id IS NOT NULL AND staff_id IS NULL AND student_id IS NULL) OR
        (actor_type = 'staff' AND staff_id IS NOT NULL AND admin_id IS NULL AND student_id IS NULL) OR
        (actor_type = 'student' AND student_id IS NOT NULL AND admin_id IS NULL AND staff_id IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_auth_tokens_value ON auth_tokens(token_value);

-- 3 index dưới đây ở bản SQL Server là filtered index
--   (WHERE actor_type = '...' AND revoked_at IS NULL).
-- MySQL không hỗ trợ → bỏ mệnh đề WHERE. Đây là index phi-unique, chỉ phục vụ
-- tra cứu, nên bỏ filter KHÔNG đổi ngữ nghĩa — index rộng hơn một chút (bao cả
-- token đã thu hồi), đánh đổi được chấp nhận. Thêm revoked_at vào cuối để
-- truy vấn "token còn hiệu lực" vẫn lọc được ngay trên index.
CREATE INDEX IX_auth_tokens_admin_active ON auth_tokens(admin_id, token_type, expires_at, revoked_at);
CREATE INDEX IX_auth_tokens_staff_active ON auth_tokens(staff_id, token_type, expires_at, revoked_at);
CREATE INDEX IX_auth_tokens_student_active ON auth_tokens(student_id, token_type, expires_at, revoked_at);
CREATE INDEX IX_auth_tokens_expiry ON auth_tokens(expires_at, revoked_at);


/* ============================================================================
   II. NHÓM BẢNG: NỘI DUNG HỌC TẬP
   ============================================================================ */

-- 5. Bài học JLPT
CREATE TABLE lessons (
    lesson_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_type      VARCHAR(20)     NOT NULL DEFAULT 'lesson'
        CHECK (lesson_type IN ('lesson','reading','listening','speaking')),
    title            VARCHAR(255)    NOT NULL,
    jlpt_level       VARCHAR(5)      NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    content_text     LONGTEXT        NULL,
    video_url        VARCHAR(500)    NULL,
    audio_url        VARCHAR(500)    NULL,
    attachment_url   VARCHAR(500)    NULL,
    explanation      LONGTEXT        NULL,
    display_order    INT             NOT NULL DEFAULT 0,
    status           VARCHAR(20)     NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by       BIGINT          NULL,
    approved_by      BIGINT          NULL,
    published_at     DATETIME(6)     NULL,
    created_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_lessons_creator FOREIGN KEY (created_by) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_lessons_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_lessons_public_list ON lessons(lesson_type, status, jlpt_level, display_order);
CREATE INDEX IX_lessons_creator_status ON lessons(created_by, status);

-- 6. Bảng chữ cái Kana - UC-08
CREATE TABLE kana_characters (
    kana_id           INT AUTO_INCREMENT PRIMARY KEY,
    character_value   VARCHAR(5)      NOT NULL,
    romaji            VARCHAR(10)     NOT NULL,
    kana_type         VARCHAR(15)     NOT NULL
        CHECK (kana_type IN ('hiragana','katakana')),
    audio_url         VARCHAR(500)    NULL,
    stroke_order_url  VARCHAR(500)    NULL,
    display_order     INT             NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Kanji - UC-07
CREATE TABLE kanji (
    kanji_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_value   VARCHAR(5)     NOT NULL UNIQUE,
    meaning           VARCHAR(500)   NOT NULL,
    onyomi            VARCHAR(200)   NULL,
    kunyomi           VARCHAR(200)   NULL,
    stroke_count      INT            NULL,
    jlpt_level        VARCHAR(5)     NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    stroke_order_url  VARCHAR(500)   NULL,
    example_word      VARCHAR(100)   NULL,
    example_reading   VARCHAR(200)   NULL,
    example_meaning   VARCHAR(500)   NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by        BIGINT         NULL,
    approved_by       BIGINT         NULL,
    published_at      DATETIME(6)    NULL,
    created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_kanji_creator FOREIGN KEY (created_by) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_kanji_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_kanji_public_level ON kanji(status, jlpt_level);

-- 7b. Lịch sử luyện viết Kanji (DTW scoring)
CREATE TABLE kanji_writing_attempts (
    attempt_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT          NOT NULL,
    kanji_id        BIGINT          NOT NULL,
    character_value VARCHAR(5)      NOT NULL,
    total_strokes   INT             NOT NULL,
    avg_dtw_score   FLOAT           NULL,
    final_quality   VARCHAR(20)     NULL
        CHECK (final_quality IN ('perfect', 'good', 'ok', 'bad')),
    stroke_details  LONGTEXT        NULL,
    is_deleted      BOOLEAN         NOT NULL DEFAULT 0,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by      BIGINT          NULL,

    CONSTRAINT FK_kwa_student FOREIGN KEY (student_id) REFERENCES student_users(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_kwa_student_id ON kanji_writing_attempts(student_id);
CREATE INDEX IX_kwa_kanji_id   ON kanji_writing_attempts(kanji_id);

-- 8. Danh mục chủ đề từ vựng (UC-09 path)
CREATE TABLE vocabulary_topics (
    topic_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    jlpt_level    VARCHAR(5)    NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    slug          VARCHAR(80)   NOT NULL,
    title_ja      VARCHAR(100)  NOT NULL,
    title_vi      VARCHAR(100)  NOT NULL,
    display_order INT           NOT NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'published'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by    BIGINT        NULL,
    created_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_vocab_topics_creator FOREIGN KEY (created_by) REFERENCES staff_users(staff_id),
    CONSTRAINT UQ_vocab_topics_level_slug UNIQUE (jlpt_level, slug),
    CONSTRAINT UQ_vocab_topics_level_title_vi UNIQUE (jlpt_level, title_vi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_vocab_topics_public_path ON vocabulary_topics(status, jlpt_level, display_order);

-- 9. Từ vựng - UC-09, UC-16
CREATE TABLE vocabulary (
    vocabulary_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    word                VARCHAR(100)   NOT NULL,
    furigana            VARCHAR(200)   NULL,
    meaning             VARCHAR(500)   NOT NULL,
    word_type           VARCHAR(50)    NULL,
    jlpt_level          VARCHAR(5)     NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    topic_id            BIGINT         NOT NULL,
    audio_url           VARCHAR(500)   NULL,
    example_sentence_jp LONGTEXT       NULL,
    example_sentence_vi LONGTEXT       NULL,
    lesson_id           BIGINT         NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by          BIGINT         NULL,
    approved_by         BIGINT         NULL,
    published_at        DATETIME(6)    NULL,
    created_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_vocab_topic FOREIGN KEY (topic_id) REFERENCES vocabulary_topics(topic_id),
    CONSTRAINT FK_vocab_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id),
    CONSTRAINT FK_vocab_creator FOREIGN KEY (created_by) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_vocab_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_vocab_public_lookup ON vocabulary(status, jlpt_level, topic_id);
CREATE INDEX IX_vocab_word ON vocabulary(word);
CREATE INDEX IX_vocab_topic_id ON vocabulary(topic_id);

-- 10. Ngữ pháp - UC-06, UC-24
CREATE TABLE grammar_points (
    grammar_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    structure           VARCHAR(255)   NOT NULL,
    title               VARCHAR(255)   NULL,
    formula             VARCHAR(500)   NULL,
    meaning             VARCHAR(500)   NOT NULL,
    usage_explanation   LONGTEXT       NULL,
    jlpt_level          VARCHAR(5)     NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    example_sentence_jp LONGTEXT       NULL,
    example_sentence_vi LONGTEXT       NULL,
    lesson_id           BIGINT         NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    created_by          BIGINT         NULL,
    approved_by         BIGINT         NULL,
    published_at        DATETIME(6)    NULL,
    created_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_grammar_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id),
    CONSTRAINT FK_grammar_creator FOREIGN KEY (created_by) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_grammar_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_grammar_public_level ON grammar_points(status, jlpt_level);

/* ============================================================================
   III. NHÓM BẢNG: NGÂN HÀNG CÂU HỎI - QUIZ - ĐỀ THI JLPT
   ============================================================================ */

-- 11. Ngân hàng câu hỏi - UC-23
CREATE TABLE questions (
    question_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text    LONGTEXT       NOT NULL,
    question_type    VARCHAR(30)    NOT NULL
        CHECK (question_type IN ('multiple_choice','fill_blank','true_false')),
    skill            VARCHAR(30)    NOT NULL
        CHECK (skill IN ('vocabulary','grammar','kanji','reading','listening','mixed')),
    jlpt_level       VARCHAR(5)     NOT NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    explanation      LONGTEXT       NULL,
    audio_url        VARCHAR(500)   NULL,
    image_url        VARCHAR(500)   NULL,
    option_a         LONGTEXT       NULL,
    option_b         LONGTEXT       NULL,
    option_c         LONGTEXT       NULL,
    option_d         LONGTEXT       NULL,
    correct_option   CHAR(1)        NULL
        CHECK (correct_option IN ('A','B','C','D')),
    correct_answer_text LONGTEXT    NULL,
    created_by       BIGINT         NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    approved_by      BIGINT         NULL,
    published_at     DATETIME(6)    NULL,
    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_questions_creator FOREIGN KEY (created_by) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_questions_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_questions_skill_level ON questions(skill, jlpt_level);
CREATE INDEX IX_questions_public_bank ON questions(status, skill, jlpt_level);
CREATE INDEX IX_questions_creator_status ON questions(created_by, status);

-- 12. Bài kiểm tra Quiz / Exam - UC-26, UC-31
CREATE TABLE assessments (
    assessment_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_type VARCHAR(20)     NOT NULL
        CHECK (assessment_type IN ('quiz','exam')),
    title           VARCHAR(255)    NOT NULL,
    lesson_id       BIGINT          NULL,
    topic           VARCHAR(100)    NULL,
    description     VARCHAR(500)    NULL,
    jlpt_level      VARCHAR(5)      NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    duration_min    INT             NULL,
    pass_score      INT             NULL,
    total_score     INT             NULL,
    audio_url       VARCHAR(500)    NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft','pending_review','rejected','published','archived','deleted')),
    is_deleted      BOOLEAN         NOT NULL DEFAULT 0,
    created_by      BIGINT          NULL,
    approved_by     BIGINT          NULL,
    published_at    DATETIME(6)     NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_assessments_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id),
    CONSTRAINT FK_assessments_creator FOREIGN KEY (created_by) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_assessments_approver FOREIGN KEY (approved_by) REFERENCES staff_users(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_assessments_public_list ON assessments(assessment_type, status, jlpt_level, topic);
CREATE INDEX IX_assessments_lesson_status ON assessments(lesson_id, status);
CREATE INDEX IX_assessments_creator_status ON assessments(created_by, status);
CREATE INDEX IX_assessments_not_deleted ON assessments(is_deleted, assessment_type, status);

-- 13. Gán câu hỏi cho Assessment / Reading-Listening-Speaking Material
CREATE TABLE question_assignments (
    assignment_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_type     VARCHAR(30)     NOT NULL
        CHECK (parent_type IN ('assessment','lesson')),
    parent_id       BIGINT          NOT NULL,
    question_id     BIGINT          NOT NULL,
    section_name    VARCHAR(100)    NULL,
    score           DECIMAL(6,2)    NOT NULL DEFAULT 1,
    display_order   INT             NOT NULL DEFAULT 0,

    CONSTRAINT FK_assign_question FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    CONSTRAINT UQ_assign UNIQUE (parent_type, parent_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_assign_parent ON question_assignments(parent_type, parent_id);

/* ============================================================================
   IV. NHÓM BẢNG: LÀM BÀI - KẾT QUẢ - BÀI NỘP
   ============================================================================ */

-- 14. Lần làm bài (quiz / exam / practice / reading / listening) của Học viên
CREATE TABLE test_attempts (
    attempt_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id        BIGINT          NOT NULL,
    attempt_type      VARCHAR(20)     NOT NULL
        CHECK (attempt_type IN ('exam','quiz','practice','reading','listening')),
    parent_type       VARCHAR(30)     NOT NULL
        CHECK (parent_type IN ('assessment','lesson','random_practice')),
    parent_id         BIGINT          NULL,
    started_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    submitted_at      DATETIME(6)     NULL,
    duration_seconds  INT             NULL,
    total_score       DECIMAL(8,2)    NULL,
    max_score         DECIMAL(8,2)    NULL,
    is_passed         BOOLEAN         NULL,
    language_knowledge_score DECIMAL(8,2) NULL,
    reading_score            DECIMAL(8,2) NULL,
    listening_score          DECIMAL(8,2) NULL,
    status            VARCHAR(20)     NOT NULL DEFAULT 'in_progress'
        CHECK (status IN ('in_progress','submitted','auto_submitted','abandoned')),

    CONSTRAINT FK_attempt_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_attempt_student_type ON test_attempts(student_id, attempt_type);
CREATE INDEX IX_attempt_parent    ON test_attempts(parent_type, parent_id);
CREATE INDEX IX_attempt_student_status_date ON test_attempts(student_id, status, submitted_at);

-- 15. Đáp án từng câu mà học viên đã chọn
CREATE TABLE attempt_answers (
    answer_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id         BIGINT          NOT NULL,
    question_id        BIGINT          NOT NULL,
    selected_option    CHAR(1)         NULL
        CHECK (selected_option IN ('A','B','C','D')),
    answer_text        LONGTEXT        NULL,
    is_correct         BOOLEAN         NULL,
    score              DECIMAL(6,2)    NULL,
    answered_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_ans_attempt  FOREIGN KEY (attempt_id) REFERENCES test_attempts(attempt_id) ON DELETE CASCADE,
    CONSTRAINT FK_ans_question FOREIGN KEY (question_id) REFERENCES questions(question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_ans_attempt ON attempt_answers(attempt_id);

-- 16. Bài nộp của học viên - compact cho Speaking/Handwriting/Manual grading
CREATE TABLE student_submissions (
    submission_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id        BIGINT          NOT NULL,
    submission_type   VARCHAR(20)     NOT NULL
        CHECK (submission_type IN ('speaking','handwriting')),
    status            VARCHAR(20)     NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending','ai_graded','graded','rejected')),
    exercise_id       BIGINT          NULL,
    recording_url     VARCHAR(500)    NULL,
    duration_seconds  INT             NULL,
    ai_overall_score        DECIMAL(5,2)  NULL,
    ai_pronunciation_score  DECIMAL(5,2)  NULL,
    ai_fluency_score        DECIMAL(5,2)  NULL,
    ai_error_summary        LONGTEXT      NULL,
    ai_suggestions          LONGTEXT      NULL,
    ai_graded_at            DATETIME(6)   NULL,
    kanji_id                BIGINT        NULL,
    kana_id                 INT           NULL,
    handwriting_image_url   VARCHAR(500)  NULL,
    expected_character      VARCHAR(5)    NULL,
    recognized_character    VARCHAR(5)    NULL,
    similarity_percent      DECIMAL(5,2)  NULL,
    is_correct              BOOLEAN       NULL,
    ocr_processed_at        DATETIME(6)   NULL,
    manual_score            DECIMAL(5,2)  NULL,
    manual_feedback         LONGTEXT      NULL,
    graded_by               BIGINT        NULL,
    graded_at               DATETIME(6)   NULL,
    submitted_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_sub_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_sub_exercise FOREIGN KEY (exercise_id) REFERENCES lessons(lesson_id),
    CONSTRAINT FK_sub_kanji FOREIGN KEY (kanji_id) REFERENCES kanji(kanji_id),
    CONSTRAINT FK_sub_kana FOREIGN KEY (kana_id) REFERENCES kana_characters(kana_id),
    CONSTRAINT FK_sub_grader FOREIGN KEY (graded_by) REFERENCES staff_users(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_sub_student_status ON student_submissions(student_id, status);
CREATE INDEX IX_sub_type_status ON student_submissions(submission_type, status);
CREATE INDEX IX_sub_grader ON student_submissions(graded_by, graded_at);


/* ============================================================================
   V. NHÓM BẢNG: TIẾN ĐỘ HỌC TẬP - FLASHCARD - BOOKMARK
   ============================================================================ */

-- 17. Tiến độ học và bookmark (học viên)
CREATE TABLE student_content_progress (
    progress_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id       BIGINT          NOT NULL,
    content_type     VARCHAR(30)     NOT NULL
        CHECK (content_type IN ('lesson','vocabulary','kanji','kana','grammar')),
    content_id       BIGINT          NOT NULL,
    status           VARCHAR(20)     NOT NULL DEFAULT 'learning'
        CHECK (status IN ('learning','completed','reviewing')),
    progress_percent DECIMAL(5,2)    NOT NULL DEFAULT 0,
    completed_at     DATETIME(6)     NULL,
    is_bookmarked    BOOLEAN         NOT NULL DEFAULT 0,
    bookmark_note    VARCHAR(500)    NULL,
    bookmarked_at    DATETIME(6)     NULL,
    last_studied_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_progress_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT UQ_progress UNIQUE (student_id, content_type, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_progress_student_type ON student_content_progress(student_id, content_type);
CREATE INDEX IX_progress_bookmarks ON student_content_progress(student_id, is_bookmarked, content_type);

-- 18. Sổ tay Flashcard (deck) - first-class
--
-- Hai cột *_key bên dưới là generated column, KHÔNG có trong bản SQL Server.
-- Lý do: bản cũ dùng filtered unique index (CREATE UNIQUE INDEX ... WHERE ...)
-- mà MySQL không hỗ trợ. Bỏ mệnh đề WHERE sẽ phá vỡ nghiệp vụ, nên thay bằng
-- generated column trả NULL khi dòng nằm ngoài phạm vi filter — unique index
-- của MySQL bỏ qua dòng có cột NULL, tái lập đúng ngữ nghĩa filtered index.
--
-- BẮT BUỘC là VIRTUAL, không được STORED: student_id vừa là base column của
-- review_deck_key, vừa là FK có ON DELETE CASCADE. MySQL cấm tổ hợp
-- "STORED generated column + FK ON DELETE CASCADE trên base column của nó"
-- (ERROR 1215 khi CREATE TABLE). VIRTUAL không vướng hạn chế này và vẫn đánh
-- được unique index.
CREATE TABLE flashcard_decks (
    deck_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id     BIGINT          NULL,
    name           VARCHAR(255)    NOT NULL,
    description    VARCHAR(500)    NULL,
    jlpt_level     VARCHAR(5)      NULL
        CHECK (jlpt_level IN ('N5','N4','N3','N2','N1')),
    topic          VARCHAR(100)    NULL,
    color          VARCHAR(20)     NULL,
    display_order  INT             NOT NULL DEFAULT 0,
    is_system      BOOLEAN         NOT NULL DEFAULT 0,
    is_review_deck BOOLEAN         NOT NULL DEFAULT 0,
    is_deleted     BOOLEAN         NOT NULL DEFAULT 0,
    created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    -- Thay cho: UQ_deck_student_name WHERE is_deleted = 0 AND student_id IS NOT NULL
    -- NULL khi deck đã xoá mềm -> deck cũ không chặn việc tạo lại deck trùng tên
    -- (giữ đúng ADR-004 Soft Delete).
    active_name_key VARCHAR(255)
        AS (CASE WHEN is_deleted = 0 THEN name END) VIRTUAL,

    -- Thay cho: UQ_review_deck_per_student WHERE is_review_deck = 1 AND is_deleted = 0
    -- NULL với mọi deck không phải review deck -> chỉ ràng buộc "1 review deck
    -- còn sống / 1 học viên", không đụng các deck thường.
    review_deck_key BIGINT
        AS (CASE WHEN is_review_deck = 1 AND is_deleted = 0 THEN student_id END) VIRTUAL,

    CONSTRAINT FK_deck_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX UQ_deck_student_name ON flashcard_decks(student_id, active_name_key);
CREATE UNIQUE INDEX UQ_review_deck_per_student ON flashcard_decks(review_deck_key);
CREATE INDEX IX_decks_owner ON flashcard_decks(student_id, is_deleted);

-- 19. Thẻ Flashcard - UC-12
CREATE TABLE flashcards (
    flashcard_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id       BIGINT          NULL,
    deck_id          BIGINT          NOT NULL,
    is_system        BOOLEAN         NOT NULL DEFAULT 0,
    content_type     VARCHAR(20)     NOT NULL
        CHECK (content_type IN ('kanji','vocabulary','grammar','custom')),
    content_id       BIGINT          NULL,
    front_text       LONGTEXT        NULL,
    back_text        LONGTEXT        NULL,
    last_rating      VARCHAR(10)     NULL
        CHECK (last_rating IN ('easy','hard','wrong')),
    interval_days    INT             NOT NULL DEFAULT 1,
    repetition_count INT             NOT NULL DEFAULT 0,
    ease_factor      DECIMAL(5,2)    NOT NULL DEFAULT 2.50,
    next_review_date DATE            NULL,
    last_reviewed_at DATETIME(6)     NULL,
    added_reason     VARCHAR(20)     NULL,
    last_session_id  VARCHAR(36)     NULL,
    is_deleted       BOOLEAN         NOT NULL DEFAULT 0,
    created_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_flashcard_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_flashcard_deck FOREIGN KEY (deck_id) REFERENCES flashcard_decks(deck_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_flashcards_next_review ON flashcards(student_id, next_review_date);
CREATE INDEX IX_flashcards_owner_active ON flashcards(student_id, is_deleted);
CREATE INDEX IX_flashcards_deck_due ON flashcards(deck_id, next_review_date);
CREATE INDEX IX_flashcards_owner_content ON flashcards(student_id, content_type, content_id);
CREATE INDEX IX_flashcards_owner_session ON flashcards(student_id, last_session_id);

/* ============================================================================
   VI. NHÓM BẢNG: HỖ TRỢ - THÔNG BÁO
   ============================================================================ */

-- 20. Ticket hỗ trợ - UC-21
CREATE TABLE tickets (
    ticket_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT          NOT NULL,
    subject         VARCHAR(255)    NOT NULL,
    content         LONGTEXT        NOT NULL,
    category        VARCHAR(50)     NULL,
    priority        VARCHAR(20)     NOT NULL DEFAULT 'normal'
        CHECK (priority IN ('low','normal','high','urgent')),
    -- CHECK này được đặt tên tường minh (khác các CHECK inline còn lại) vì V25
    -- phải DROP nó để thêm trạng thái 'assigned'. MySQL tự đặt tên CHECK inline
    -- theo dạng tickets_chk_N phụ thuộc thứ tự khai báo — drop theo tên đó rất
    -- dễ vỡ khi bảng đổi. Đặt tên sẵn ở đây để V25 drop xác định.
    status          VARCHAR(20)     NOT NULL DEFAULT 'open',
    assigned_to     BIGINT          NULL,
    last_reply_at   DATETIME(6)     NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    resolved_at     DATETIME(6)     NULL,

    CONSTRAINT CK_tickets_status CHECK (status IN ('open','in_progress','resolved','closed')),
    CONSTRAINT FK_tk_student  FOREIGN KEY (student_id)  REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_tk_assignee FOREIGN KEY (assigned_to) REFERENCES staff_users(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_tk_status   ON tickets(status);
CREATE INDEX IX_tk_assignee ON tickets(assigned_to, status);
CREATE INDEX IX_tk_student_status ON tickets(student_id, status);

-- 21. Phản hồi ticket
CREATE TABLE ticket_replies (
    reply_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id          BIGINT          NOT NULL,
    student_sender_id  BIGINT          NULL,
    staff_sender_id    BIGINT          NULL,
    message            LONGTEXT        NOT NULL,
    attachment_url     VARCHAR(500)    NULL,
    created_at         DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_rep_ticket         FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE CASCADE,
    CONSTRAINT FK_rep_student_sender FOREIGN KEY (student_sender_id) REFERENCES student_users(student_id),
    CONSTRAINT FK_rep_staff_sender   FOREIGN KEY (staff_sender_id) REFERENCES staff_users(staff_id),
    CONSTRAINT CK_replies_sender     CHECK (
        (student_sender_id IS NOT NULL AND staff_sender_id IS NULL) OR
        (student_sender_id IS NULL AND staff_sender_id IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_rep_ticket ON ticket_replies(ticket_id);

-- 22. Thông báo - UC-28, UC-42
CREATE TABLE notifications (
    notification_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id        BIGINT          NOT NULL,
    title             VARCHAR(255)    NOT NULL,
    content           LONGTEXT        NOT NULL,
    notification_type VARCHAR(30)     NOT NULL DEFAULT 'news'
        CHECK (notification_type IN ('news','warning','promotion','system','achievement','reminder')),
    channel           VARCHAR(30)     NOT NULL DEFAULT 'in_app'
        CHECK (channel IN ('in_app','email','both')),
    is_auto           BOOLEAN         NOT NULL DEFAULT 0,
    rule_key          VARCHAR(100)    NULL,
    scheduled_at      DATETIME(6)     NULL,
    sent_at           DATETIME(6)     NULL,
    is_read           BOOLEAN         NOT NULL DEFAULT 0,
    read_at           DATETIME(6)     NULL,
    delivered_at      DATETIME(6)     NULL,
    admin_creator_id  BIGINT          NULL,
    staff_creator_id  BIGINT          NULL,
    created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_noti_student FOREIGN KEY (student_id) REFERENCES student_users(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_noti_admin_creator FOREIGN KEY (admin_creator_id) REFERENCES admin_users(admin_id),
    CONSTRAINT FK_noti_staff_creator FOREIGN KEY (staff_creator_id) REFERENCES staff_users(staff_id),
    CONSTRAINT CK_noti_creator CHECK (
        (admin_creator_id IS NOT NULL AND staff_creator_id IS NULL) OR
        (admin_creator_id IS NULL AND staff_creator_id IS NOT NULL) OR
        (admin_creator_id IS NULL AND staff_creator_id IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bản SQL Server có filtered index WHERE sent_at IS NULL (chỉ index thông báo
-- chưa gửi). MySQL không hỗ trợ -> bỏ WHERE. Index phi-unique, chỉ ảnh hưởng
-- hiệu năng: sent_at đã nằm trong index nên truy vấn "chưa gửi" vẫn lọc được.
CREATE INDEX IX_notifications_schedule ON notifications(scheduled_at, sent_at);
CREATE INDEX IX_notifications_student_read ON notifications(student_id, is_read, created_at);


/* ============================================================================
   VII. NHÓM BẢNG: CẤU HÌNH HỆ THỐNG - LOG
   ============================================================================ */

-- 23. Cấu hình hệ thống - UC-41 (key-value)
CREATE TABLE system_settings (
    setting_id      INT AUTO_INCREMENT PRIMARY KEY,
    setting_group   VARCHAR(50)     NOT NULL,
    setting_key     VARCHAR(100)    NOT NULL,
    setting_value   LONGTEXT        NULL,
    value_type      VARCHAR(20)     NOT NULL DEFAULT 'string'
        CHECK (value_type IN ('string','integer','boolean','time')),
    is_editable     BOOLEAN         NOT NULL DEFAULT 1,
    updated_by      BIGINT          NULL,
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT UQ_setting UNIQUE (setting_group, setting_key),
    CONSTRAINT FK_setting_admin FOREIGN KEY (updated_by) REFERENCES admin_users(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. Audit log thao tác Admin/Staff/Student
CREATE TABLE admin_audit_logs (
    audit_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_actor_id    BIGINT          NULL,
    staff_actor_id    BIGINT          NULL,
    student_actor_id  BIGINT          NULL,
    action            VARCHAR(100)    NOT NULL,
    target_table      VARCHAR(100)    NULL,
    target_id         BIGINT          NULL,
    description       LONGTEXT        NULL,
    ip_address        VARCHAR(45)     NULL,
    created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_audit_admin FOREIGN KEY (admin_actor_id) REFERENCES admin_users(admin_id),
    CONSTRAINT FK_audit_staff FOREIGN KEY (staff_actor_id) REFERENCES staff_users(staff_id),
    CONSTRAINT FK_audit_student FOREIGN KEY (student_actor_id) REFERENCES student_users(student_id),
    CONSTRAINT CK_audit_actor CHECK (
        (CASE WHEN admin_actor_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN staff_actor_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN student_actor_id IS NOT NULL THEN 1 ELSE 0 END) = 1
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_audit_admin_date ON admin_audit_logs(admin_actor_id, created_at);
CREATE INDEX IX_audit_staff_date ON admin_audit_logs(staff_actor_id, created_at);
CREATE INDEX IX_audit_student_date ON admin_audit_logs(student_actor_id, created_at);


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

    (SELECT COUNT(*) FROM test_attempts t
     WHERE t.student_id = s.student_id AND t.attempt_type='exam' AND t.status IN ('submitted','auto_submitted')) AS total_exams_taken,
    (SELECT COUNT(*) FROM test_attempts t
     WHERE t.student_id = s.student_id AND t.attempt_type='quiz' AND t.status IN ('submitted','auto_submitted')) AS total_quizzes_taken,
    (SELECT MAX(t.total_score) FROM test_attempts t
     WHERE t.student_id = s.student_id AND t.attempt_type='exam') AS highest_exam_score,
    (SELECT AVG(t.total_score) FROM test_attempts t
     WHERE t.student_id = s.student_id AND t.attempt_type='exam') AS average_exam_score
FROM student_users s;
