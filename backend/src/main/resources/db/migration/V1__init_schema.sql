-- Initial database schema for JLPT E-Learning Platform
-- Migration: V1__init_schema.sql

-- Users table
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name NVARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'STAFF', 'ADMIN')),
    jlpt_level VARCHAR(2) CHECK (jlpt_level IN ('N5', 'N4', 'N3', 'N2', 'N1')),
    subscription_status VARCHAR(20) NOT NULL DEFAULT 'FREE' CHECK (subscription_status IN ('FREE', 'VIP')),
    subscription_expires_at DATETIME2,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    created_by BIGINT
);

-- Courses table
CREATE TABLE courses (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    jlpt_level VARCHAR(2) NOT NULL CHECK (jlpt_level IN ('N5', 'N4', 'N3', 'N2', 'N1')),
    is_vip_only BIT NOT NULL DEFAULT 0,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    created_by BIGINT
);

-- Lessons table
CREATE TABLE lessons (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title NVARCHAR(255) NOT NULL,
    lesson_type VARCHAR(20) NOT NULL CHECK (lesson_type IN ('KANJI', 'KANA', 'VOCAB', 'GRAMMAR', 'READING', 'LISTENING')),
    content NVARCHAR(MAX),
    audio_path VARCHAR(500),
    image_path VARCHAR(500),
    lesson_order INT NOT NULL,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    created_by BIGINT,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Quizzes table
CREATE TABLE quizzes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    jlpt_level VARCHAR(2) NOT NULL CHECK (jlpt_level IN ('N5', 'N4', 'N3', 'N2', 'N1')),
    quiz_type VARCHAR(20) NOT NULL CHECK (quiz_type IN ('MOCK_EXAM', 'TOPIC_QUIZ', 'FLASHCARD')),
    max_score INT NOT NULL CHECK (max_score > 0),
    time_limit_minutes INT,
    is_vip_only BIT NOT NULL DEFAULT 0,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    created_by BIGINT
);

-- Questions table
CREATE TABLE questions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_text NVARCHAR(MAX) NOT NULL,
    question_type VARCHAR(20) NOT NULL CHECK (question_type IN ('MULTIPLE_CHOICE', 'TRUE_FALSE', 'FILL_BLANK', 'MATCHING')),
    points INT NOT NULL DEFAULT 1 CHECK (points > 0),
    is_locked BIT NOT NULL DEFAULT 0,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    created_by BIGINT,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- Answers table
CREATE TABLE answers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    question_id BIGINT NOT NULL,
    answer_text NVARCHAR(MAX) NOT NULL,
    is_correct BIT NOT NULL DEFAULT 0,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Quiz attempts table (immutable after submission)
CREATE TABLE quiz_attempts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score INT NOT NULL CHECK (score >= 0),
    max_score INT NOT NULL CHECK (max_score > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'GRADED', 'CANCELLED')),
    started_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    submitted_at DATETIME2,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- User progress table
CREATE TABLE user_progress (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    score_percentage DECIMAL(5,2),
    completed_at DATETIME2,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

-- Learning activity log (audit trail)
CREATE TABLE learning_activity_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    description NVARCHAR(MAX),
    metadata NVARCHAR(MAX),
    ip_address VARCHAR(45),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Flashcard table
CREATE TABLE flashcards (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    jlpt_level VARCHAR(2) NOT NULL CHECK (jlpt_level IN ('N5', 'N4', 'N3', 'N2', 'N1')),
    front_text NVARCHAR(500) NOT NULL,
    back_text NVARCHAR(500) NOT NULL,
    example_sentence NVARCHAR(MAX),
    audio_path VARCHAR(500),
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    created_by BIGINT
);

-- User flashcard progress (Spaced Repetition)
CREATE TABLE user_flashcard_progress (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flashcard_id BIGINT NOT NULL,
    ease_factor DECIMAL(5,2) NOT NULL DEFAULT 2.50,
    interval_days INT NOT NULL DEFAULT 0,
    repetitions INT NOT NULL DEFAULT 0,
    next_review_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    last_reviewed_at DATETIME2,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (flashcard_id) REFERENCES flashcards(id)
);

-- Payments table
CREATE TABLE payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    gateway_reference VARCHAR(255),
    payment_method VARCHAR(50),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_courses_level ON courses(jlpt_level);
CREATE INDEX idx_lessons_course ON lessons(course_id);
CREATE INDEX idx_lessons_order ON lessons(lesson_order);
CREATE INDEX idx_questions_quiz ON questions(quiz_id);
CREATE INDEX idx_quiz_attempts_user ON quiz_attempts(user_id);
CREATE INDEX idx_quiz_attempts_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_user_progress_user ON user_progress(user_id);
CREATE INDEX idx_learning_log_user ON learning_activity_log(user_id);
CREATE INDEX idx_payments_idempotency ON payments(idempotency_key);
CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_flashcards_level ON flashcards(jlpt_level);
CREATE INDEX idx_user_flashcard_progress ON user_flashcard_progress(user_id, next_review_at);
