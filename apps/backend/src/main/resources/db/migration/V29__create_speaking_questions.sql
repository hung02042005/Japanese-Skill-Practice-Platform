CREATE TABLE speaking_questions (
    speaking_question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_id            BIGINT       NOT NULL,
    prompt_text          TEXT         NOT NULL,
    instruction          TEXT         NULL,
    sample_audio_url     VARCHAR(500) NULL,
    display_order        INT          NOT NULL DEFAULT 0,
    created_at           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT FK_speaking_questions_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_speaking_questions_lesson_order
    ON speaking_questions(lesson_id, display_order);

CREATE INDEX IX_lessons_speaking_workflow
    ON lessons(lesson_type, jlpt_level, status, display_order);
