/* ============================================================================
   V2 — DỮ LIỆU MẪU (Mock Data)
   ----------------------------------------------------------------------------
   Mục đích : Seed tài khoản dev + nội dung mẫu để test toàn bộ luồng hệ thống
   Mật khẩu :
     admin@sakuji.com   → Admin@123456
     manager@sakuji.com → Staff@123456
     staff@sakuji.com   → Staff@123456
     student1@sakuji.com → Student@123456
     student2@sakuji.com → Student@123456
   Lưu ý    : Seed data cho môi trường DEV
   ============================================================================ */

/* Không dùng "USE <database>;" ở đây: JDBC connection string của Flyway
   (spring.datasource.url) đã trỏ đúng database rồi — hard-code tên database cụ
   thể trong migration khiến nó CHỈ chạy được trên 1 database tên đúng, vỡ ngay
   trên bất kỳ môi trường nào dùng tên khác (staging: JLPT_LearningDB_staging).
   Phát hiện khi test P1.4 (staging) lần đầu — xem
   docs/05-Deployment/Deploy_Improvement_Plan.md, P1.4. */

/* Ghi chú migration SQL Server → MySQL:
   - INSERT IGNORE dùng cho bảng đã có UNIQUE constraint trên khoá tự nhiên
     (email, character_value, (setting_group,setting_key)...). Thay cho
     "IF NOT EXISTS (...) INSERT" của T-SQL, và idempotent theo từng dòng.
   - Bảng không có UNIQUE trên khoá tự nhiên (lessons.title, questions.
     question_text, assessments.title) vẫn giữ nguyên cách chặn theo lô bằng
     "SELECT ... WHERE NOT EXISTS", đúng ngữ nghĩa bản cũ.
   - Biến @x của MySQL tồn tại theo connection. Flyway chạy trọn 1 file trên 1
     connection nên các SET @... bên dưới dùng được xuyên suốt file. */

/* ============================================================
   0. CẤU HÌNH HỆ THỐNG (gộp từ V1 cũ + V25)
   ============================================================ */

INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
    ('general',           'platform_name',              'JLPT Learning Platform',           'string'),
    ('general',           'logo_url',                   '/assets/logo.png',                 'string'),
    ('general',           'default_language',           'vi',                               'string'),
    ('general',           'maintenance_mode',           'false',                            'boolean'),
    ('general',           'allow_registration',         'true',                             'boolean'),
    ('security',          'max_login_attempts',         '5',                                'integer'),
    ('security',          'session_timeout_min',        '60',                               'integer'),
    ('security',          'password_reset_min',         '15',                               'integer'),
    ('smtp',              'host',                       'smtp.gmail.com',                   'string'),
    ('smtp',              'port',                       '587',                              'integer'),
    ('smtp',              'username',                   '',                                 'string'),
    ('smtp',              'from_email',                 'noreply@jlpt.com',                 'string'),
    ('smtp',              'secure',                     'STARTTLS',                         'string'),
    ('smtp',              'from_name',                  'JLPT Platform',                    'string'),
    ('auto_notification', 'streak_10_days_enabled',     'true',                             'boolean'),
    ('auto_notification', 'streak_10_days_title',       'Tuyệt vời!',                       'string'),
    ('auto_notification', 'streak_10_days_template',    'Bạn đã học liên tiếp 10 ngày!',    'string'),
    ('auto_notification', 'daily_flashcard_enabled',    'true',                             'boolean'),
    ('auto_notification', 'daily_flashcard_time',       '08:00',                            'time'),
    ('auto_notification', 'daily_flashcard_title',      'Ôn flashcard',                     'string'),
    ('auto_notification', 'daily_flashcard_template',   'Đến giờ ôn flashcard rồi!',        'string'),
    ('auto_notification', 'exam_result_ready_enabled',  'true',                             'boolean'),
    ('auto_notification', 'exam_result_ready_title',    'Kết quả thi',                      'string'),
    ('auto_notification', 'exam_result_ready_template', 'Điểm bài thi của bạn đã có',       'string'),
    ('email_register',    'from_email',                 'noreply@jlpt.com',                 'string'),
    ('email_register',    'from_name',                  'JLPT Platform',                    'string'),
    ('email_register',    'subject',                    'Xác nhận đăng ký tài khoản',       'string'),
    ('email_otp',         'from_email',                 'noreply@jlpt.com',                 'string'),
    ('email_otp',         'from_name',                  'JLPT Platform',                    'string'),
    ('email_otp',         'subject',                    'Mã xác thực của bạn',              'string'),
    ('email_reset',       'from_email',                 'noreply@jlpt.com',                 'string'),
    ('email_reset',       'from_name',                  'JLPT Platform',                    'string'),
    ('email_reset',       'subject',                    'Cấp lại mật khẩu tài khoản Staff', 'string');


/* ============================================================
   I. TÀI KHOẢN NGƯỜI DÙNG
   ============================================================ */

-- ── 1. Admin ──────────────────────────────────────────────────
INSERT IGNORE INTO admin_users (
    email, password_hash, full_name,
    status,
    login_attempts, created_at, updated_at
) VALUES (
    'admin@sakuji.com',
    '$2b$12$kJcYAwbtPEqtu8tZUWv4fetUeoydXgfwS4ckmsiHW3oLU3Hhtig/G',   -- Admin@123456
    'Quản Trị Viên',
    'active',
    0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
);

-- ── 2. Staff Manager ─────────────────────────────────────────
INSERT IGNORE INTO staff_users (
    email, password_hash, full_name, staff_role,
    status,
    login_attempts, created_at, updated_at
) VALUES (
    'manager@sakuji.com',
    '$2b$12$9HCWZhWE7OgwP2/SMU3Ob.zscUYQ2oV28ArNO3x6F/NboRnFhofmu',   -- Staff@123456
    'Trưởng Nhóm Nội Dung',
    'staff_manager',
    'active',
    0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
);

-- ── 3. Staff ────────────────────────────────────────────────
INSERT IGNORE INTO staff_users (
    email, password_hash, full_name, staff_role,
    status,
    login_attempts, created_at, updated_at
) VALUES (
    'staff@sakuji.com',
    '$2b$12$9HCWZhWE7OgwP2/SMU3Ob.zscUYQ2oV28ArNO3x6F/NboRnFhofmu',   -- Staff@123456
    'Biên Soạn Viên',
    'staff',
    'active',
    0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
);

-- ── 4. Học viên 1 ────────────────────────────────────────────
INSERT IGNORE INTO student_users (
    email, password_hash, full_name,
    status, email_verified_at,
    current_jlpt_level, target_jlpt_level,
    current_streak, longest_streak, last_activity_date,
    login_attempts, created_at, updated_at
) VALUES (
    'student1@sakuji.com',
    '$2b$12$JrZ/8BbDiUQxHPXrHvUBQOTA043UiqP9HNinbrnRale4.9tU2B2im',   -- Student@123456
    'Nguyễn Minh Anh',
    'active', CURRENT_TIMESTAMP(6),
    'N5', 'N3',
    7, 14, CAST(CURRENT_TIMESTAMP AS DATE),
    0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
);

-- ── 5. Học viên 2 ────────────────────────────────────────────
INSERT IGNORE INTO student_users (
    email, password_hash, full_name,
    status, email_verified_at,
    current_jlpt_level, target_jlpt_level,
    current_streak, longest_streak, last_activity_date,
    login_attempts, created_at, updated_at
) VALUES (
    'student2@sakuji.com',
    '$2b$12$JrZ/8BbDiUQxHPXrHvUBQOTA043UiqP9HNinbrnRale4.9tU2B2im',   -- Student@123456
    'Trần Thị Lan',
    'active', CURRENT_TIMESTAMP(6),
    'N4', 'N2',
    3, 21, CAST(CURRENT_TIMESTAMP AS DATE),
    0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
);

-- Các biến dùng lại xuyên suốt file (thay cho DECLARE @... của T-SQL)
SET @admin_id    = (SELECT admin_id   FROM admin_users   WHERE email = 'admin@sakuji.com'    LIMIT 1);
SET @manager_id  = (SELECT staff_id   FROM staff_users   WHERE email = 'manager@sakuji.com'  LIMIT 1);
SET @staff_id    = (SELECT staff_id   FROM staff_users   WHERE email = 'staff@sakuji.com'    LIMIT 1);
SET @student1_id = (SELECT student_id FROM student_users WHERE email = 'student1@sakuji.com' LIMIT 1);
SET @student2_id = (SELECT student_id FROM student_users WHERE email = 'student2@sakuji.com' LIMIT 1);
SET @now         = CURRENT_TIMESTAMP(6);


/* ============================================================
   I-b. DANH MỤC CHỦ ĐỀ TỪ VỰNG (gộp từ V10 — bắt buộc trước III)
   ============================================================ */

INSERT IGNORE INTO vocabulary_topics (jlpt_level, slug, title_ja, title_vi, display_order, status, created_by) VALUES
('N5', 'family',        '家族',   'Gia đình',   1,  'published', @staff_id),
('N5', 'food',          '食べ物', 'Ẩm thực',    2,  'published', @staff_id),
('N5', 'transport',     '交通',   'Giao thông', 3,  'published', @staff_id),
('N5', 'time',          '時間',   'Thời gian',  4,  'published', @staff_id),
('N5', 'place',         '場所',   'Địa điểm',   5,  'published', @staff_id),
('N5', 'description',   '描写',   'Mô tả',      6,  'published', @staff_id),
('N5', 'weather',       '天気',   'Thời tiết',  7,  'published', @staff_id),
('N5', 'education',     '学校',   'Giáo dục',   8,  'published', @staff_id),
('N5', 'society',       '社会',   'Xã hội',     9,  'published', @staff_id),
('N5', 'technology',    '技術',   'Công nghệ',  10, 'published', @staff_id),

('N4', 'travel',        '旅行',   'Du lịch',    1, 'published', @staff_id),
('N4', 'emotion',       '感情',   'Cảm xúc',    2, 'published', @staff_id),
('N4', 'shopping',      '買い物', 'Mua sắm',    3, 'published', @staff_id),
('N4', 'health',        '健康',   'Sức khỏe',   4, 'published', @staff_id),
('N4', 'education',     '学校',   'Giáo dục',   5, 'published', @staff_id),
('N4', 'family',        '家族',   'Gia đình',   6, 'published', @staff_id),

('N3', 'work',          '仕事',   'Công việc',  1, 'published', @staff_id),
('N3', 'society',       '社会',   'Xã hội',     2, 'published', @staff_id),
('N3', 'nature',        '自然',   'Thiên nhiên',3, 'published', @staff_id),
('N3', 'thinking',      '思考',   'Tư duy',     4, 'published', @staff_id),
('N3', 'communication', '連絡',   'Giao tiếp',  5, 'published', @staff_id),

('N2', 'academia',      '学術',   'Học thuật',  1, 'published', @staff_id),
('N2', 'economy',       '経済',   'Kinh tế',    2, 'published', @staff_id),
('N2', 'law',           '法律',   'Pháp luật',  3, 'published', @staff_id),
('N2', 'psychology',    '心理',   'Tâm lý',     4, 'published', @staff_id),

('N1', 'philosophy',    '哲学',   'Triết học',  1, 'published', @staff_id),
('N1', 'academia',      '学術',   'Học thuật',  2, 'published', @staff_id),
('N1', 'literature',    '文学',   'Văn học',    3, 'published', @staff_id),
('N1', 'technology',    '技術',   'Công nghệ',  4, 'published', @staff_id),
('N1', 'environment',   '環境',   'Môi trường', 5, 'published', @staff_id);


/* ============================================================
   II. KANJI MẪU — N5 (published)
   ============================================================ */

INSERT IGNORE INTO kanji (
    character_value, meaning, onyomi, kunyomi,
    stroke_count, jlpt_level,
    example_word, example_reading, example_meaning,
    status, created_by, approved_by, published_at,
    created_at, updated_at
) VALUES
( '山', 'Núi',            'サン',   'やま',    3,  'N5', '富士山',  'ふじさん',  'Núi Phú Sĩ',    'published', @staff_id, @manager_id, @now, @now, @now ),
( '水', 'Nước',           'スイ',   'みず',    4,  'N5', '水道',    'すいどう',  'Đường nước',    'published', @staff_id, @manager_id, @now, @now, @now ),
( '火', 'Lửa',            'カ',     'ひ',      4,  'N5', '火事',    'かじ',      'Hỏa hoạn',      'published', @staff_id, @manager_id, @now, @now, @now ),
( '木', 'Cây gỗ',         'モク',   'き',      4,  'N5', '木曜日',  'もくようび','Thứ năm',       'published', @staff_id, @manager_id, @now, @now, @now ),
( '日', 'Mặt trời/Ngày',  'ニチ',   'ひ',      4,  'N5', '日本語',  'にほんご',  'Tiếng Nhật',    'published', @staff_id, @manager_id, @now, @now, @now ),
( '月', 'Mặt trăng/Tháng','ゲツ',   'つき',    4,  'N5', '一月',    'いちがつ',  'Tháng Một',     'published', @staff_id, @manager_id, @now, @now, @now ),
( '人', 'Con người',      'ジン',   'ひと',    2,  'N5', '外国人',  'がいこくじん','Người nước ngoài','published', @staff_id, @manager_id, @now, @now, @now ),
( '口', 'Miệng',          'コウ',   'くち',    3,  'N5', '人口',    'じんこう',  'Dân số',        'published', @staff_id, @manager_id, @now, @now, @now ),
( '車', 'Xe',             'シャ',   'くるま',  7,  'N5', '電車',    'でんしゃ',  'Tàu điện',      'published', @staff_id, @manager_id, @now, @now, @now ),
( '語', 'Ngôn ngữ',       'ゴ',     'かたる',  14, 'N5', '日本語',  'にほんご',  'Tiếng Nhật',    'published', @staff_id, @manager_id, @now, @now, @now );


/* ============================================================
   III. TỪ VỰNG MẪU — N5 (published)
   ============================================================ */

-- vocabulary.word không có UNIQUE constraint -> giữ nguyên cách chặn theo lô
-- bằng WHERE NOT EXISTS trên 1 dòng đại diện, đúng như bản SQL Server.
INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic_id,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
SELECT v.word, v.furigana, v.meaning, v.word_type, v.jlpt_level,
       (SELECT topic_id FROM vocabulary_topics t WHERE t.jlpt_level = v.jlpt_level AND t.title_vi = v.topic_vi),
       v.example_jp, v.example_vi,
       'published', @staff_id, @manager_id, @now, @now, @now
FROM (
              SELECT '食べる' AS word, 'たべる' AS furigana, 'Ăn' AS meaning, 'động từ nhóm 2' AS word_type, 'N5' AS jlpt_level, 'Ẩm thực' AS topic_vi, '私は毎朝ご飯を食べる。' AS example_jp, 'Tôi ăn cơm mỗi buổi sáng.' AS example_vi
    UNION ALL SELECT '飲む',   'のむ',     'Uống',           'động từ nhóm 1', 'N5', 'Ẩm thực',   '水を飲む。',           'Uống nước.'
    UNION ALL SELECT '学校',   'がっこう', 'Trường học',     'danh từ',        'N5', 'Giáo dục',  '学校に行く。',         'Đi đến trường.'
    UNION ALL SELECT '先生',   'せんせい', 'Giáo viên',      'danh từ',        'N5', 'Giáo dục',  '先生はやさしい。',     'Giáo viên thân thiện.'
    UNION ALL SELECT '友達',   'ともだち', 'Bạn bè',         'danh từ',        'N5', 'Xã hội',    '友達と遊ぶ。',         'Chơi với bạn bè.'
    UNION ALL SELECT 'きれい', 'きれい',   'Đẹp / Sạch sẽ',  'tính từ đuôi な','N5', 'Mô tả',     'この花はきれいです。', 'Bông hoa này rất đẹp.'
    UNION ALL SELECT '大きい', 'おおきい', 'To lớn',         'tính từ đuôi い','N5', 'Mô tả',     '大きい犬がいる。',     'Có một con chó to.'
    UNION ALL SELECT '電話',   'でんわ',   'Điện thoại',     'danh từ',        'N5', 'Công nghệ', '電話をかける。',       'Gọi điện thoại.'
) AS v
WHERE NOT EXISTS (SELECT 1 FROM vocabulary x WHERE x.word = '食べる');


/* ============================================================
   IV. BÀI HỌC MẪU — N5
   ============================================================ */

INSERT INTO lessons (
    lesson_type, title, jlpt_level,
    content_text, explanation,
    display_order, status,
    created_by, approved_by, published_at,
    created_at, updated_at
)
SELECT
    'lesson',
    'Giới thiệu Kanji cơ bản N5',
    'N5',
    'Trong bài học này bạn sẽ học 10 Kanji cơ bản nhất ở cấp độ N5: 山 水 火 木 日 月 人 口 車 語. Mỗi chữ đều có âm On''yomi, Kun''yomi và ví dụ câu đi kèm.',
    'Hãy luyện tập viết tay mỗi chữ ít nhất 5 lần để ghi nhớ nét bút.',
    1, 'published',
    @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM lessons x WHERE x.title = 'Giới thiệu Kanji cơ bản N5');

SET @lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Giới thiệu Kanji cơ bản N5' LIMIT 1);


/* ============================================================
   V. CÂU HỎI MẪU + BÀI QUIZ
   ============================================================ */

-- Câu hỏi 1
INSERT INTO questions (
    question_text, question_type, skill, jlpt_level,
    option_a, option_b, option_c, option_d, correct_option,
    explanation, status, created_by, approved_by, published_at,
    created_at, updated_at
)
SELECT
    '「山」の読み方はどれですか？',
    'multiple_choice', 'kanji', 'N5',
    'やま', 'かわ', 'もり', 'うみ', 'A',
    '「山」はやま（kun）またはサン（on）と読みます。',
    'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM questions x WHERE x.question_text = '「山」の読み方はどれですか？');

-- Câu hỏi 2
INSERT INTO questions (
    question_text, question_type, skill, jlpt_level,
    option_a, option_b, option_c, option_d, correct_option,
    explanation, status, created_by, approved_by, published_at,
    created_at, updated_at
)
SELECT
    '「水」の意味は何ですか？',
    'multiple_choice', 'kanji', 'N5',
    'Lửa', 'Nước', 'Gió', 'Đất', 'B',
    '「水」（みず）はWaterの意味です。',
    'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM questions x WHERE x.question_text = '「水」の意味は何ですか？');

-- Câu hỏi 3
INSERT INTO questions (
    question_text, question_type, skill, jlpt_level,
    option_a, option_b, option_c, option_d, correct_option,
    explanation, status, created_by, approved_by, published_at,
    created_at, updated_at
)
SELECT
    '（　）に入る言葉を選んでください。「私は毎朝ご飯を（　）。」',
    'multiple_choice', 'vocabulary', 'N5',
    '飲む', '食べる', '見る', '聞く', 'B',
    '「食べる」は to eat の意味です。',
    'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM questions x WHERE x.question_text = '（　）に入る言葉を選んでください。「私は毎朝ご飯を（　）。」');

-- Câu hỏi 4
INSERT INTO questions (
    question_text, question_type, skill, jlpt_level,
    option_a, option_b, option_c, option_d, correct_option,
    explanation, status, created_by, approved_by, published_at,
    created_at, updated_at
)
SELECT
    '「先生」の意味はどれですか？',
    'multiple_choice', 'vocabulary', 'N5',
    'Học sinh', 'Giáo viên', 'Bố', 'Mẹ', 'B',
    '「先生」（せんせい）は Teacher の意味です。',
    'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM questions x WHERE x.question_text = '「先生」の意味はどれですか？');

-- Câu hỏi 5
INSERT INTO questions (
    question_text, question_type, skill, jlpt_level,
    option_a, option_b, option_c, option_d, correct_option,
    explanation, status, created_by, approved_by, published_at,
    created_at, updated_at
)
SELECT
    '「日本語」の正しい読み方はどれですか？',
    'multiple_choice', 'kanji', 'N5',
    'にほんご', 'にっぽんご', 'にほんぐ', 'にほんく', 'A',
    '「日本語」はにほんごと読みます（Japanese language）。',
    'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM questions x WHERE x.question_text = '「日本語」の正しい読み方はどれですか？');

SET @q1 = (SELECT question_id FROM questions WHERE question_text = '「山」の読み方はどれですか？' LIMIT 1);
SET @q2 = (SELECT question_id FROM questions WHERE question_text = '「水」の意味は何ですか？' LIMIT 1);
SET @q3 = (SELECT question_id FROM questions WHERE question_text = '（　）に入る言葉を選んでください。「私は毎朝ご飯を（　）。」' LIMIT 1);
SET @q4 = (SELECT question_id FROM questions WHERE question_text = '「先生」の意味はどれですか？' LIMIT 1);
SET @q5 = (SELECT question_id FROM questions WHERE question_text = '「日本語」の正しい読み方はどれですか？' LIMIT 1);

-- Quiz N5 tổng hợp
INSERT INTO assessments (
    assessment_type, title, lesson_id, topic, jlpt_level,
    duration_min, pass_score, total_score,
    status, created_by, approved_by, published_at,
    created_at, updated_at
)
SELECT
    'quiz',
    'Quiz Kanji & Từ vựng N5 — Bài 1',
    @lesson_id,
    'Kanji N5',
    'N5',
    10, 60, 100,
    'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM assessments x WHERE x.title = 'Quiz Kanji & Từ vựng N5 — Bài 1');

SET @assessment_id = (SELECT assessment_id FROM assessments WHERE title = 'Quiz Kanji & Từ vựng N5 — Bài 1' LIMIT 1);

-- Gán câu hỏi vào quiz
INSERT INTO question_assignments (parent_type, parent_id, question_id, score, display_order)
SELECT * FROM (
              SELECT 'assessment' AS pt, @assessment_id AS pid, @q1 AS qid, 20 AS sc, 1 AS ord
    UNION ALL SELECT 'assessment', @assessment_id, @q2, 20, 2
    UNION ALL SELECT 'assessment', @assessment_id, @q3, 20, 3
    UNION ALL SELECT 'assessment', @assessment_id, @q4, 20, 4
    UNION ALL SELECT 'assessment', @assessment_id, @q5, 20, 5
) AS a
WHERE NOT EXISTS (
    SELECT 1 FROM question_assignments x
    WHERE x.parent_type = 'assessment' AND x.parent_id = @assessment_id
);


/* ============================================================
   VI. TIẾN ĐỘ HỌC MẪU (Student 1)
   ============================================================ */

-- Tiến độ bài học
INSERT INTO student_content_progress
    (student_id, content_type, content_id, status, progress_percent, last_studied_at, created_at)
SELECT @student1_id, 'lesson', @lesson_id, 'completed', 100.0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM student_content_progress x
    WHERE x.student_id = @student1_id AND x.content_type = 'lesson' AND x.content_id = @lesson_id
);

-- Tiến độ 5 Kanji đầu
INSERT INTO student_content_progress
    (student_id, content_type, content_id, status, progress_percent, last_studied_at, created_at)
SELECT
    @student1_id, 'kanji', k.kanji_id, 'completed', 100.0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM (
    SELECT kanji_id FROM kanji
    WHERE jlpt_level = 'N5'
    ORDER BY kanji_id
    LIMIT 5
) AS k
WHERE NOT EXISTS (
    SELECT 1 FROM student_content_progress p
    WHERE p.student_id = @student1_id AND p.content_type = 'kanji' AND p.content_id = k.kanji_id
);

-- Tiến độ 3 từ vựng
INSERT INTO student_content_progress
    (student_id, content_type, content_id, status, progress_percent, last_studied_at, created_at)
SELECT
    @student1_id, 'vocabulary', v.vocabulary_id, 'learning', 60.0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM (
    SELECT vocabulary_id FROM vocabulary
    WHERE jlpt_level = 'N5'
    ORDER BY vocabulary_id
    LIMIT 3
) AS v
WHERE NOT EXISTS (
    SELECT 1 FROM student_content_progress p
    WHERE p.student_id = @student1_id AND p.content_type = 'vocabulary' AND p.content_id = v.vocabulary_id
);


/* ============================================================
   VII. THÔNG BÁO MẪU
   ============================================================ */

INSERT INTO notifications (
    student_id, title, content, notification_type, channel,
    is_auto, sent_at, admin_creator_id, created_at
)
SELECT * FROM (
              SELECT @student1_id AS sid, 'Chào mừng đến với SakuJi!' AS ti,
                     'Chào Minh Anh! Tài khoản của bạn đã sẵn sàng. Hãy bắt đầu hành trình học tiếng Nhật ngay hôm nay 🌸' AS co,
                     'system' AS nt, 'in_app' AS ch, 0 AS au, CURRENT_TIMESTAMP(6) AS se, @admin_id AS ac, CURRENT_TIMESTAMP(6) AS cr
    UNION ALL SELECT @student1_id, 'Streak 7 ngày — Tuyệt vời!',
                     'Bạn đã học liên tiếp 7 ngày! Saku-chan rất tự hào về bạn 🎉 Hãy tiếp tục duy trì nhé!',
                     'achievement', 'in_app', 1, CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6)
    UNION ALL SELECT @student2_id, 'Chào mừng đến với SakuJi!',
                     'Chào Thị Lan! Tài khoản của bạn đã sẵn sàng. Chúc bạn chinh phục JLPT N2 thành công!',
                     'system', 'in_app', 0, CURRENT_TIMESTAMP(6), @admin_id, CURRENT_TIMESTAMP(6)
) AS n
WHERE NOT EXISTS (
    SELECT 1 FROM notifications x
    WHERE x.student_id = @student1_id AND x.title = 'Chào mừng đến với SakuJi!'
);


/* ============================================================
   VIII. AUDIT LOG — ADMIN TẠO TÀI KHOẢN
   ============================================================ */

INSERT INTO admin_audit_logs (admin_actor_id, action, target_table, description, ip_address, created_at)
VALUES
(
    @admin_id,
    'seed_mock_data',
    'admin_users, staff_users, student_users',
    'Seed dữ liệu mẫu cho môi trường DEV — V2 migration',
    '127.0.0.1',
    CURRENT_TIMESTAMP(6)
);


/* ============================================================
   IX. KANJI BỔ SUNG N4 (gộp từ V7 — phong phú dictionary search)
   ============================================================ */

INSERT IGNORE INTO kanji (character_value, meaning, onyomi, kunyomi, stroke_count, jlpt_level,
    example_word, example_reading, example_meaning,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
( '海', 'Biển / Đại dương', 'カイ',    'うみ',        9,  'N4', '海外',  'かいがい', 'Nước ngoài',       'published', @staff_id, @manager_id, @now, @now, @now ),
( '旅', 'Du hành / Du lịch','リョ',    'たび',        10, 'N4', '旅行',  'りょこう', 'Du lịch',          'published', @staff_id, @manager_id, @now, @now, @now ),
( '花', 'Hoa',              'カ',      'はな',        7,  'N4', '花見',  'はなみ',   'Ngắm hoa anh đào', 'published', @staff_id, @manager_id, @now, @now, @now ),
( '空', 'Bầu trời / Trống', 'クウ',    'そら・から',  8,  'N4', '空港',  'くうこう', 'Sân bay',          'published', @staff_id, @manager_id, @now, @now, @now ),
( '川', 'Sông / Suối',      'セン',    'かわ',        3,  'N4', '川沿い','かわぞい', 'Dọc bờ sông',      'published', @staff_id, @manager_id, @now, @now, @now ),
( '雨', 'Mưa',              'ウ',      'あめ',        8,  'N4', '大雨',  'おおあめ', 'Mưa lớn',          'published', @staff_id, @manager_id, @now, @now, @now ),
( '雪', 'Tuyết',            'セツ',    'ゆき',        11, 'N4', '雪山',  'ゆきやま', 'Núi tuyết',        'published', @staff_id, @manager_id, @now, @now, @now ),
( '風', 'Gió / Phong tục',  'フウ・フ','かぜ・かざ',  9,  'N4', '台風',  'たいふう', 'Bão',              'published', @staff_id, @manager_id, @now, @now, @now ),
( '体', 'Cơ thể / Thân',    'タイ',    'からだ',      7,  'N4', '体力',  'たいりょく','Thể lực',         'published', @staff_id, @manager_id, @now, @now, @now ),
( '道', 'Con đường / Đạo',  'ドウ',    'みち',        12, 'N4', '道路',  'どうろ',   'Đường bộ',         'published', @staff_id, @manager_id, @now, @now, @now );


/* ============================================================
   X. TỪ VỰNG BỔ SUNG N5→N1 (gộp từ V7)
   ============================================================ */

INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic_id,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
SELECT v.word, v.furigana, v.meaning, v.word_type, v.jlpt_level,
       (SELECT topic_id FROM vocabulary_topics t WHERE t.jlpt_level = v.jlpt_level AND t.title_vi = v.topic_vi),
       v.example_jp, v.example_vi,
       'published', @staff_id, @manager_id, @now, @now, @now
FROM (
    -- N5
              SELECT '母' AS word, 'はは' AS furigana, 'Mẹ (cách nói khiêm tốn)' AS meaning, 'danh từ' AS word_type, 'N5' AS jlpt_level, 'Gia đình' AS topic_vi, '母は料理が上手です。' AS example_jp, 'Mẹ tôi nấu ăn rất giỏi.' AS example_vi
    UNION ALL SELECT '父',       'ちち',         'Bố (cách nói khiêm tốn)',      'danh từ',            'N5', 'Gia đình',   '父は会社員です。',               'Bố tôi là nhân viên công ty.'
    UNION ALL SELECT '兄',       'あに',         'Anh trai (khiêm tốn)',         'danh từ',            'N5', 'Gia đình',   '兄は大学生です。',               'Anh trai tôi là sinh viên đại học.'
    UNION ALL SELECT '姉',       'あね',         'Chị gái (khiêm tốn)',          'danh từ',            'N5', 'Gia đình',   '姉は先生です。',                 'Chị tôi là giáo viên.'
    UNION ALL SELECT '家族',     'かぞく',       'Gia đình',                     'danh từ',            'N5', 'Gia đình',   '私の家族は四人です。',           'Gia đình tôi có bốn người.'
    UNION ALL SELECT 'ご飯',     'ごはん',       'Cơm / Bữa ăn',                 'danh từ',            'N5', 'Ẩm thực',    '毎日ご飯を食べます。',           'Mỗi ngày tôi ăn cơm.'
    UNION ALL SELECT 'お茶',     'おちゃ',       'Trà',                          'danh từ',            'N5', 'Ẩm thực',    'お茶を飲みますか。',             'Bạn có uống trà không?'
    UNION ALL SELECT '魚',       'さかな',       'Cá',                           'danh từ',            'N5', 'Ẩm thực',    '魚が好きです。',                 'Tôi thích cá.'
    UNION ALL SELECT '肉',       'にく',         'Thịt',                         'danh từ',            'N5', 'Ẩm thực',    '肉を買いました。',               'Tôi đã mua thịt.'
    UNION ALL SELECT '野菜',     'やさい',       'Rau củ',                       'danh từ',            'N5', 'Ẩm thực',    '野菜をたくさん食べてください。', 'Hãy ăn nhiều rau nhé.'
    UNION ALL SELECT '駅',       'えき',         'Ga tàu',                       'danh từ',            'N5', 'Giao thông', '駅まで歩きます。',               'Tôi đi bộ đến ga.'
    UNION ALL SELECT 'バス',     'バス',         'Xe buýt',                      'danh từ',            'N5', 'Giao thông', 'バスで学校に行きます。',         'Tôi đi học bằng xe buýt.'
    UNION ALL SELECT '飛行機',   'ひこうき',     'Máy bay',                      'danh từ',            'N5', 'Giao thông', '飛行機に乗りました。',           'Tôi đã đi máy bay.'
    UNION ALL SELECT '自転車',   'じてんしゃ',   'Xe đạp',                       'danh từ',            'N5', 'Giao thông', '自転車で通学します。',           'Tôi đi học bằng xe đạp.'
    UNION ALL SELECT '今日',     'きょう',       'Hôm nay',                      'danh từ',            'N5', 'Thời gian',  '今日は月曜日です。',             'Hôm nay là thứ Hai.'
    UNION ALL SELECT '明日',     'あした',       'Ngày mai',                     'danh từ',            'N5', 'Thời gian',  '明日は休みです。',               'Ngày mai tôi được nghỉ.'
    UNION ALL SELECT '昨日',     'きのう',       'Hôm qua',                      'danh từ',            'N5', 'Thời gian',  '昨日は雨でした。',               'Hôm qua trời mưa.'
    UNION ALL SELECT '病院',     'びょういん',   'Bệnh viện',                    'danh từ',            'N5', 'Địa điểm',   '病院に行きます。',               'Tôi đến bệnh viện.'
    UNION ALL SELECT '図書館',   'としょかん',   'Thư viện',                     'danh từ',            'N5', 'Địa điểm',   '図書館で本を読みます。',         'Tôi đọc sách ở thư viện.'
    UNION ALL SELECT '銀行',     'ぎんこう',     'Ngân hàng',                    'danh từ',            'N5', 'Địa điểm',   '銀行でお金をおろします。',       'Tôi rút tiền ở ngân hàng.'
    UNION ALL SELECT '新しい',   'あたらしい',   'Mới',                          'tính từ đuôi い',    'N5', 'Mô tả',      '新しい本を買いました。',         'Tôi đã mua cuốn sách mới.'
    UNION ALL SELECT '古い',     'ふるい',       'Cũ / Lâu đời',                 'tính từ đuôi い',    'N5', 'Mô tả',      '古い映画が好きです。',           'Tôi thích phim cũ.'
    UNION ALL SELECT '暑い',     'あつい',       'Nóng',                         'tính từ đuôi い',    'N5', 'Thời tiết',  '今日はとても暑いです。',         'Hôm nay rất nóng.'
    UNION ALL SELECT '寒い',     'さむい',       'Lạnh',                         'tính từ đuôi い',    'N5', 'Thời tiết',  '冬は寒いです。',                 'Mùa đông rất lạnh.'
    -- N4
    UNION ALL SELECT '旅行',     'りょこう',     'Du lịch',                      'danh từ / động từ',  'N4', 'Du lịch',    '来月、京都へ旅行します。',       'Tháng tới tôi đi du lịch Kyoto.'
    UNION ALL SELECT '予約',     'よやく',       'Đặt trước / Đặt chỗ',          'danh từ / động từ',  'N4', 'Du lịch',    'ホテルを予約しました。',         'Tôi đã đặt khách sạn.'
    UNION ALL SELECT '地図',     'ちず',         'Bản đồ',                       'danh từ',            'N4', 'Du lịch',    '地図を見ながら歩きました。',     'Tôi đi bộ trong khi nhìn bản đồ.'
    UNION ALL SELECT '嬉しい',   'うれしい',     'Vui mừng / Sung sướng',        'tính từ đuôi い',    'N4', 'Cảm xúc',    '合格して嬉しいです。',           'Tôi vui vì đã đỗ.'
    UNION ALL SELECT '悲しい',   'かなしい',     'Buồn',                         'tính từ đuôi い',    'N4', 'Cảm xúc',    '別れが悲しいです。',             'Tôi buồn khi chia tay.'
    UNION ALL SELECT '心配',     'しんぱい',     'Lo lắng',                      'danh từ / động từ',  'N4', 'Cảm xúc',    '試験が心配です。',               'Tôi lo lắng về kỳ thi.'
    UNION ALL SELECT '値段',     'ねだん',       'Giá cả',                       'danh từ',            'N4', 'Mua sắm',    'この服の値段はいくらですか。',   'Bộ quần áo này giá bao nhiêu?'
    UNION ALL SELECT 'お釣り',   'おつり',       'Tiền thừa / Tiền trả lại',     'danh từ',            'N4', 'Mua sắm',    'お釣りをもらいました。',         'Tôi đã nhận lại tiền thừa.'
    UNION ALL SELECT '割引',     'わりびき',     'Giảm giá / Chiết khấu',        'danh từ / động từ',  'N4', 'Mua sắm',    '10%割引のセールがあります。',    'Có đợt sale giảm 10%.'
    UNION ALL SELECT '熱',       'ねつ',         'Sốt / Nhiệt độ',               'danh từ',            'N4', 'Sức khỏe',   '熱が出ました。',                 'Tôi bị sốt.'
    UNION ALL SELECT '薬',       'くすり',       'Thuốc',                        'danh từ',            'N4', 'Sức khỏe',   '薬を飲んでください。',           'Hãy uống thuốc đi.'
    UNION ALL SELECT '休む',     'やすむ',       'Nghỉ ngơi / Nghỉ học/làm',     'động từ nhóm 1',     'N4', 'Sức khỏe',   '今日は体調が悪くて休みました。', 'Hôm nay tôi nghỉ vì không khỏe.'
    UNION ALL SELECT '試験',     'しけん',       'Kỳ thi / Kiểm tra',            'danh từ',            'N4', 'Giáo dục',   '来週試験があります。',           'Tuần tới có kỳ thi.'
    UNION ALL SELECT '合格',     'ごうかく',     'Đỗ / Đạt yêu cầu',             'danh từ / động từ',  'N4', 'Giáo dục',   'JLPT N4に合格しました！',        'Tôi đã đỗ JLPT N4!'
    UNION ALL SELECT '練習',     'れんしゅう',   'Luyện tập',                    'danh từ / động từ',  'N4', 'Giáo dục',   '毎日日本語を練習します。',       'Tôi luyện tiếng Nhật mỗi ngày.'
    UNION ALL SELECT '単語',     'たんご',       'Từ vựng / Từ đơn',             'danh từ',            'N4', 'Giáo dục',   '毎日新しい単語を覚えます。',     'Mỗi ngày tôi học từ vựng mới.'
    UNION ALL SELECT '結婚',     'けっこん',     'Kết hôn',                      'danh từ / động từ',  'N4', 'Gia đình',   '来年結婚する予定です。',         'Tôi dự định kết hôn năm tới.'
    UNION ALL SELECT '子供',     'こども',       'Trẻ em / Con cái',             'danh từ',            'N4', 'Gia đình',   '子供が二人います。',             'Tôi có hai đứa con.'
    -- N3
    UNION ALL SELECT '経験',     'けいけん',     'Kinh nghiệm',                  'danh từ / động từ',  'N3', 'Công việc',  '海外での経験が役に立ちます。',   'Kinh nghiệm làm việc ở nước ngoài rất hữu ích.'
    UNION ALL SELECT '仕事',     'しごと',       'Công việc / Việc làm',         'danh từ',            'N3', 'Công việc',  '新しい仕事を探しています。',     'Tôi đang tìm việc mới.'
    UNION ALL SELECT '会議',     'かいぎ',       'Cuộc họp',                     'danh từ',            'N3', 'Công việc',  '午後から会議があります。',       'Chiều nay có cuộc họp.'
    UNION ALL SELECT '締め切り', 'しめきり',     'Hạn chót / Deadline',          'danh từ',            'N3', 'Công việc',  '締め切りに間に合いました。',     'Tôi đã kịp hạn chót.'
    UNION ALL SELECT '給料',     'きゅうりょう', 'Lương / Tiền lương',           'danh từ',            'N3', 'Công việc',  '今月の給料が入りました。',       'Lương tháng này đã vào.'
    UNION ALL SELECT '社会',     'しゃかい',     'Xã hội',                       'danh từ',            'N3', 'Xã hội',     '現代社会では情報が大切です。',   'Trong xã hội hiện đại, thông tin rất quan trọng.'
    UNION ALL SELECT '文化',     'ぶんか',       'Văn hóa',                      'danh từ',            'N3', 'Xã hội',     '日本の文化を学んでいます。',     'Tôi đang học văn hóa Nhật Bản.'
    UNION ALL SELECT '伝統',     'でんとう',     'Truyền thống',                 'danh từ',            'N3', 'Xã hội',     '日本の伝統を大切にしています。', 'Chúng tôi coi trọng truyền thống Nhật Bản.'
    UNION ALL SELECT '海',       'うみ',         'Biển / Đại dương',             'danh từ',            'N3', 'Thiên nhiên','海で泳ぎました。',               'Tôi đã bơi ở biển.'
    UNION ALL SELECT '森',       'もり',         'Rừng',                         'danh từ',            'N3', 'Thiên nhiên','森の中を散歩しました。',         'Tôi đã tản bộ trong rừng.'
    UNION ALL SELECT '環境',     'かんきょう',   'Môi trường',                   'danh từ',            'N3', 'Thiên nhiên','環境を守ることが大切です。',     'Bảo vệ môi trường rất quan trọng.'
    UNION ALL SELECT '努力',     'どりょく',     'Nỗ lực / Cố gắng',             'danh từ / động từ',  'N3', 'Tư duy',     '努力すれば夢は叶います。',       'Nếu cố gắng, ước mơ sẽ trở thành hiện thực.'
    UNION ALL SELECT '成功',     'せいこう',     'Thành công',                   'danh từ / động từ',  'N3', 'Tư duy',     'プロジェクトが成功しました。',   'Dự án đã thành công.'
    UNION ALL SELECT '失敗',     'しっぱい',     'Thất bại',                     'danh từ / động từ',  'N3', 'Tư duy',     '失敗から学ぶことが大切です。',   'Học từ thất bại là điều quan trọng.'
    UNION ALL SELECT '意見',     'いけん',       'Ý kiến / Quan điểm',           'danh từ',            'N3', 'Tư duy',     'あなたの意見を聞かせてください。','Hãy cho tôi biết ý kiến của bạn.'
    UNION ALL SELECT '連絡',     'れんらく',     'Liên lạc / Thông báo',         'danh từ / động từ',  'N3', 'Giao tiếp',  '後で連絡します。',               'Tôi sẽ liên lạc sau.'
    UNION ALL SELECT '相談',     'そうだん',     'Tư vấn / Bàn bạc',             'danh từ / động từ',  'N3', 'Giao tiếp',  '先生に相談しました。',           'Tôi đã bàn bạc với giáo viên.'
    -- N2
    UNION ALL SELECT '研究',     'けんきゅう',   'Nghiên cứu',                   'danh từ / động từ',  'N2', 'Học thuật',  '大学院で研究をしています。',     'Tôi đang nghiên cứu ở cao học.'
    UNION ALL SELECT '分析',     'ぶんせき',     'Phân tích',                    'danh từ / động từ',  'N2', 'Học thuật',  'データを分析しました。',         'Tôi đã phân tích dữ liệu.'
    UNION ALL SELECT '論文',     'ろんぶん',     'Luận văn / Bài báo khoa học',  'danh từ',            'N2', 'Học thuật',  '論文を書き終えました。',         'Tôi đã viết xong luận văn.'
    UNION ALL SELECT '仮説',     'かせつ',       'Giả thuyết',                   'danh từ',            'N2', 'Học thuật',  '仮説を立てて実験しました。',     'Tôi đặt ra giả thuyết rồi thực nghiệm.'
    UNION ALL SELECT '経済',     'けいざい',     'Kinh tế',                      'danh từ',            'N2', 'Kinh tế',    '世界経済が変化しています。',     'Kinh tế thế giới đang thay đổi.'
    UNION ALL SELECT '企業',     'きぎょう',     'Doanh nghiệp / Công ty',       'danh từ',            'N2', 'Kinh tế',    '大手企業に就職しました。',       'Tôi đã vào làm ở công ty lớn.'
    UNION ALL SELECT '投資',     'とうし',       'Đầu tư',                       'danh từ / động từ',  'N2', 'Kinh tế',    '株に投資しています。',           'Tôi đang đầu tư vào cổ phiếu.'
    UNION ALL SELECT '利益',     'りえき',       'Lợi nhuận / Lợi ích',          'danh từ',            'N2', 'Kinh tế',    '今年は利益が増えました。',       'Năm nay lợi nhuận tăng.'
    UNION ALL SELECT '法律',     'ほうりつ',     'Luật pháp',                    'danh từ',            'N2', 'Pháp luật',  '法律を守ることが大切です。',     'Tuân thủ luật pháp là điều quan trọng.'
    UNION ALL SELECT '権利',     'けんり',       'Quyền lợi / Quyền hạn',        'danh từ',            'N2', 'Pháp luật',  'すべての人に権利があります。',   'Mọi người đều có quyền.'
    UNION ALL SELECT '義務',     'ぎむ',         'Nghĩa vụ / Bổn phận',          'danh từ',            'N2', 'Pháp luật',  '税金を払う義務があります。',     'Chúng ta có nghĩa vụ nộp thuế.'
    UNION ALL SELECT '影響',     'えいきょう',   'Ảnh hưởng / Tác động',         'danh từ / động từ',  'N2', 'Tâm lý',     'ストレスは体に影響を与えます。', 'Stress ảnh hưởng đến cơ thể.'
    UNION ALL SELECT '判断',     'はんだん',     'Phán đoán / Quyết định',       'danh từ / động từ',  'N2', 'Tâm lý',     '正しい判断をしてください。',     'Hãy đưa ra quyết định đúng đắn.'
    -- N1
    UNION ALL SELECT '概念',     'がいねん',     'Khái niệm',                    'danh từ',            'N1', 'Triết học',  '抽象的な概念を理解するのは難しい。','Khó hiểu các khái niệm trừu tượng.'
    UNION ALL SELECT '哲学',     'てつがく',     'Triết học',                    'danh từ',            'N1', 'Triết học',  '西洋哲学を専攻しています。',     'Tôi chuyên ngành triết học phương Tây.'
    UNION ALL SELECT '倫理',     'りんり',       'Đạo đức / Luân lý',            'danh từ',            'N1', 'Triết học',  '医療倫理は重要な問題です。',     'Y đức là vấn đề quan trọng.'
    UNION ALL SELECT '普遍',     'ふへん',       'Phổ quát / Phổ biến toàn cầu', 'danh từ / tính từ đuôi な', 'N1', 'Học thuật', '普遍的な価値観について考える。', 'Suy nghĩ về các giá trị phổ quát.'
    UNION ALL SELECT '矛盾',     'むじゅん',     'Mâu thuẫn',                    'danh từ / động từ',  'N1', 'Học thuật',  'その意見には矛盾があります。',   'Ý kiến đó có mâu thuẫn.'
    UNION ALL SELECT '抽象',     'ちゅうしょう', 'Trừu tượng',                   'danh từ / tính từ đuôi な', 'N1', 'Học thuật', '抽象的な思考が求められます。',  'Cần có tư duy trừu tượng.'
    UNION ALL SELECT '随筆',     'ずいひつ',     'Tùy bút / Bài văn thử nghiệm', 'danh từ',            'N1', 'Văn học',    '近代文学の随筆を研究しています。','Tôi nghiên cứu tùy bút văn học hiện đại.'
    UNION ALL SELECT '比喩',     'ひゆ',         'Ẩn dụ / So sánh',              'danh từ',            'N1', 'Văn học',    '詩には多くの比喩が使われます。', 'Trong thơ có nhiều ẩn dụ.'
    UNION ALL SELECT '革新',     'かくしん',     'Cải cách / Đổi mới',           'danh từ / động từ',  'N1', 'Công nghệ',  '技術革新が社会を変えます。',     'Đổi mới công nghệ thay đổi xã hội.'
    UNION ALL SELECT '持続可能', 'じぞくかのう', 'Bền vững',                     'tính từ đuôi な',    'N1', 'Môi trường', '持続可能な発展が求められます。', 'Sự phát triển bền vững là điều cần thiết.'
    UNION ALL SELECT '脆弱',     'ぜいじゃく',   'Dễ bị tổn thương / Yếu ớt',    'tính từ đuôi な',    'N1', 'Học thuật',  'システムの脆弱性を修正する。',   'Sửa lỗ hổng bảo mật của hệ thống.'
    UNION ALL SELECT '縮小',     'しゅくしょう', 'Thu nhỏ / Rút gọn',            'danh từ / động từ',  'N1', 'Học thuật',  '規模を縮小することにしました。', 'Chúng tôi quyết định thu nhỏ quy mô.'
) AS v
WHERE NOT EXISTS (SELECT 1 FROM vocabulary x WHERE x.word = '母');


/* ============================================================
   XI. NGỮ PHÁP N5→N1 (gộp từ V7)
   ============================================================ */

INSERT INTO grammar_points (structure, formula, meaning, usage_explanation, jlpt_level,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
SELECT * FROM (
    -- N5
              SELECT '〜は〜です' AS structure, 'N は N/Adj です' AS formula, '[Chủ thể] là [danh từ/tính từ]' AS meaning,
                     'Cấu trúc câu cơ bản dùng để định nghĩa hoặc miêu tả chủ thể.' AS usage_explanation,
                     'N5' AS jlpt_level, '私は学生です。' AS example_sentence_jp, 'Tôi là học sinh.' AS example_sentence_vi,
                     'published' AS status, @staff_id AS created_by, @manager_id AS approved_by, @now AS published_at, @now AS created_at, @now AS updated_at
    UNION ALL SELECT '〜が好きです', 'N が好き（きら）いです', 'Thích / Ghét cái gì',
                     'Dùng để diễn đạt sở thích hoặc không thích của bản thân. が là trợ từ chỉ đối tượng.',
                     'N5', '日本語が好きです。', 'Tôi thích tiếng Nhật.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜たい', 'V(ます形) + たい', 'Muốn làm gì (nguyện vọng của bản thân)',
                     'Thêm たい vào sau dạng ます (bỏ ます) để diễn đạt mong muốn. Chỉ dùng cho ngôi thứ nhất.',
                     'N5', '日本に行きたいです。', 'Tôi muốn đi Nhật Bản.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜ている', 'V(て形) + いる', 'Đang làm / Trạng thái hiện tại',
                     'Diễn đạt hành động đang xảy ra (tiến diễn) hoặc trạng thái kết quả của hành động trước đó.',
                     'N5', '今、勉強しています。', 'Hiện tại tôi đang học.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜てください', 'V(て形) + ください', 'Hãy làm gì (yêu cầu lịch sự)',
                     'Dùng để yêu cầu hoặc hướng dẫn ai đó thực hiện hành động một cách lịch sự.',
                     'N5', 'ゆっくり話してください。', 'Hãy nói chậm thôi.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜ませんか', 'V(ます形) + ませんか', 'Mời cùng làm gì / Rủ rê',
                     'Dùng để mời hoặc rủ ai đó cùng làm điều gì. Nhẹ nhàng, thân thiện hơn so với 〜ましょう。',
                     'N5', '一緒に映画を見ませんか。', 'Mình cùng xem phim không?', 'published', @staff_id, @manager_id, @now, @now, @now
    -- N4
    UNION ALL SELECT '〜てしまう', 'V(て形) + しまう', 'Lỡ làm gì / Đã hoàn toàn làm xong (đôi khi tiếc nuối)',
                     'Diễn đạt hành động đã hoàn thành hoàn toàn, thường kèm sắc thái tiếc nuối hoặc ngoài ý muốn.',
                     'N4', '宿題を忘れてしまいました。', 'Tôi đã lỡ quên bài tập về nhà rồi.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜ておく', 'V(て形) + おく', 'Làm trước để chuẩn bị',
                     'Diễn đạt hành động thực hiện trước để chuẩn bị cho tương lai.',
                     'N4', '旅行の前にホテルを予約しておきます。', 'Trước khi đi du lịch tôi đặt khách sạn trước.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜ばよかった', 'V(ば形) + よかった', 'Giá mà đã làm... thì tốt hơn (hối tiếc)',
                     'Diễn đạt sự hối tiếc về điều gì đó không xảy ra hoặc đã xảy ra trong quá khứ.',
                     'N4', 'もっと早く起きればよかった。', 'Giá mà tôi dậy sớm hơn thì tốt.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜そうだ', 'V/Adj(語幹) + そうだ', 'Có vẻ như... / Trông có vẻ...',
                     'Diễn đạt phán đoán dựa trên quan sát trực tiếp. Khác với 〜そうだ(truyền đạt thông tin nghe được).',
                     'N4', '雨が降りそうです。', 'Trời có vẻ sắp mưa.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜ようにする', 'V(辞書形/ない形) + ようにする', 'Cố gắng làm sao để... / Đặt mục tiêu',
                     'Diễn đạt nỗ lực để đạt được hoặc duy trì một thói quen hay trạng thái.',
                     'N4', '毎日野菜を食べるようにしています。', 'Tôi cố gắng ăn rau mỗi ngày.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜てみる', 'V(て形) + みる', 'Thử làm xem',
                     'Diễn đạt việc thực hiện thử một hành động để xem kết quả thế nào.',
                     'N4', 'この料理を食べてみました。', 'Tôi đã thử ăn món này.', 'published', @staff_id, @manager_id, @now, @now, @now
    -- N3
    UNION ALL SELECT '〜ために', 'N の / V(辞書形) + ために', 'Để / Vì mục đích...',
                     'Diễn đạt mục đích của hành động. Chủ ngữ câu trước và sau thường giống nhau.',
                     'N3', '日本語を学ぶために、毎日練習します。', 'Để học tiếng Nhật, tôi luyện tập mỗi ngày.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜によって', 'N + によって', 'Tùy theo / Do... / Bằng phương tiện...',
                     'Diễn đạt phương tiện, nguyên nhân, hoặc sự khác biệt tùy theo đối tượng.',
                     'N3', '国によって文化が違います。', 'Văn hóa khác nhau tùy theo quốc gia.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜に対して', 'N + に対して', 'Đối với / Hướng tới',
                     'Diễn đạt đối tượng mà hành động hay thái độ hướng đến.',
                     'N3', '学生に対して厳しくしてはいけません。', 'Không nên khắt khe với học sinh.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜わけだ', '普通形 + わけだ', 'Đó là lý do tại sao... / Có nghĩa là...',
                     'Diễn đạt kết luận logic dựa trên thông tin đã biết, hoặc giải thích nguyên nhân/lý do.',
                     'N3', '10年間日本に住んでいたから、日本語が上手なわけだ。', 'Anh ấy sống ở Nhật 10 năm, đó là lý do tiếng Nhật giỏi thế.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜くせに', '普通形 + くせに', 'Mặc dù... mà vẫn... (trách móc, phê phán)',
                     'Diễn đạt sự trách móc hoặc bất ngờ tiêu cực khi chủ ngữ làm điều trái với những gì người nói mong đợi.',
                     'N3', '知っているくせに、教えてくれなかった。', 'Dù biết mà vẫn không nói cho tôi.', 'published', @staff_id, @manager_id, @now, @now, @now
    -- N2
    UNION ALL SELECT '〜に違いない', '普通形 + に違いない', 'Chắc chắn là... (phán đoán mạnh)',
                     'Diễn đạt sự phán đoán mạnh của người nói, tin rằng điều đó chắc chắn đúng.',
                     'N2', 'あんなに勉強したから、合格に違いない。', 'Học nhiều như vậy, chắc chắn sẽ đỗ.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜に過ぎない', 'N / 普通形 + に過ぎない', 'Chỉ là... / Không hơn không kém',
                     'Diễn đạt rằng điều gì đó chỉ ở mức tối thiểu, không quan trọng hay ấn tượng như người khác nghĩ.',
                     'N2', 'それは噂に過ぎない。', 'Đó chỉ là tin đồn mà thôi.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜をきっかけに', 'N + をきっかけに(して)', 'Lấy... làm cơ hội / Nhờ... mà bắt đầu',
                     'Diễn đạt sự kiện hoặc tình huống trở thành bước ngoặt hoặc cơ hội khởi đầu cho hành động tiếp theo.',
                     'N2', 'この出会いをきっかけに日本語を学び始めました。', 'Nhờ cuộc gặp gỡ này mà tôi bắt đầu học tiếng Nhật.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜に基づいて', 'N + に基づいて / に基づく', 'Dựa trên / Căn cứ vào',
                     'Diễn đạt hành động hoặc quyết định dựa trên tiêu chuẩn, quy tắc, hoặc thông tin cụ thể.',
                     'N2', 'データに基づいて判断します。', 'Chúng tôi quyết định dựa trên dữ liệu.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜かねない', 'V(ます形) + かねない', 'Có thể (sẽ làm điều xấu) / Không thể không...',
                     'Diễn đạt khả năng xảy ra điều không mong muốn. Mang sắc thái cảnh báo, lo ngại.',
                     'N2', 'このままでは失敗しかねない。', 'Cứ thế này có thể thất bại đó.', 'published', @staff_id, @manager_id, @now, @now, @now
    -- N1
    UNION ALL SELECT '〜ならいざ知らず', 'N + ならいざ知らず', 'Nếu là... thì có thể hiểu được, nhưng...',
                     'Diễn đạt rằng trường hợp A có thể chấp nhận được, nhưng trường hợp B thì không thể chấp nhận được.',
                     'N1', '子供ならいざ知らず、大人がそんなことをするのは非常識だ。', 'Trẻ con thì có thể, nhưng người lớn mà làm vậy là thiếu hiểu biết.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜をものともせず', 'N + をものともせず(に)', 'Bất chấp... / Không coi... là trở ngại',
                     'Diễn đạt việc không bị ảnh hưởng hoặc không coi trở ngại nào đó là vấn đề, tiếp tục hành động.',
                     'N1', '困難をものともせず、夢を追い続けた。', 'Bất chấp khó khăn, anh ấy vẫn tiếp tục theo đuổi ước mơ.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜とあって', 'N / 普通形 + とあって', 'Vì là... nên / Do... mà',
                     'Diễn đạt lý do đặc biệt dẫn đến kết quả tự nhiên. Thường dùng trong văn viết.',
                     'N1', '大会前とあって、選手たちは緊張していた。', 'Vì là ngay trước giải đấu, các vận động viên rất căng thẳng.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜といえども', 'N / 普通形 + といえども', 'Dù là... đi chăng nữa (nhượng bộ trang trọng)',
                     'Diễn đạt sự nhượng bộ theo phong cách trang trọng, thừa nhận điều kiện nhưng kết quả vẫn trái ngược.',
                     'N1', 'プロといえども、常に努力が必要だ。', 'Dù là chuyên gia, cũng luôn cần nỗ lực.', 'published', @staff_id, @manager_id, @now, @now, @now
    UNION ALL SELECT '〜いかんによらず', 'N + のいかんによらず / いかんにかかわらず', 'Bất kể... như thế nào',
                     'Diễn đạt rằng dù điều kiện hay tình huống ra sao, kết quả/hành động vẫn không thay đổi. Rất trang trọng.',
                     'N1', '結果のいかんによらず、最善を尽くします。', 'Dù kết quả thế nào, tôi sẽ cố hết sức.', 'published', @staff_id, @manager_id, @now, @now, @now
) AS g
WHERE NOT EXISTS (SELECT 1 FROM grammar_points x WHERE x.structure = '〜は〜です');


/* ============================================================
   XII. BẢNG CHỮ KANA (gộp từ V19 + V24)
   ============================================================ */

INSERT INTO kana_characters (character_value, romaji, kana_type, display_order)
SELECT * FROM (
    -- ===== HIRAGANA — gojūon =====
              SELECT 'あ' AS cv, 'a' AS rj, 'hiragana' AS kt, 1 AS ord
    UNION ALL SELECT 'い', 'i',   'hiragana', 2   UNION ALL SELECT 'う', 'u',   'hiragana', 3
    UNION ALL SELECT 'え', 'e',   'hiragana', 4   UNION ALL SELECT 'お', 'o',   'hiragana', 5
    UNION ALL SELECT 'か', 'ka',  'hiragana', 6   UNION ALL SELECT 'き', 'ki',  'hiragana', 7
    UNION ALL SELECT 'く', 'ku',  'hiragana', 8   UNION ALL SELECT 'け', 'ke',  'hiragana', 9
    UNION ALL SELECT 'こ', 'ko',  'hiragana', 10  UNION ALL SELECT 'さ', 'sa',  'hiragana', 11
    UNION ALL SELECT 'し', 'shi', 'hiragana', 12  UNION ALL SELECT 'す', 'su',  'hiragana', 13
    UNION ALL SELECT 'せ', 'se',  'hiragana', 14  UNION ALL SELECT 'そ', 'so',  'hiragana', 15
    UNION ALL SELECT 'た', 'ta',  'hiragana', 16  UNION ALL SELECT 'ち', 'chi', 'hiragana', 17
    UNION ALL SELECT 'つ', 'tsu', 'hiragana', 18  UNION ALL SELECT 'て', 'te',  'hiragana', 19
    UNION ALL SELECT 'と', 'to',  'hiragana', 20  UNION ALL SELECT 'な', 'na',  'hiragana', 21
    UNION ALL SELECT 'に', 'ni',  'hiragana', 22  UNION ALL SELECT 'ぬ', 'nu',  'hiragana', 23
    UNION ALL SELECT 'ね', 'ne',  'hiragana', 24  UNION ALL SELECT 'の', 'no',  'hiragana', 25
    UNION ALL SELECT 'は', 'ha',  'hiragana', 26  UNION ALL SELECT 'ひ', 'hi',  'hiragana', 27
    UNION ALL SELECT 'ふ', 'fu',  'hiragana', 28  UNION ALL SELECT 'へ', 'he',  'hiragana', 29
    UNION ALL SELECT 'ほ', 'ho',  'hiragana', 30  UNION ALL SELECT 'ま', 'ma',  'hiragana', 31
    UNION ALL SELECT 'み', 'mi',  'hiragana', 32  UNION ALL SELECT 'む', 'mu',  'hiragana', 33
    UNION ALL SELECT 'め', 'me',  'hiragana', 34  UNION ALL SELECT 'も', 'mo',  'hiragana', 35
    UNION ALL SELECT 'や', 'ya',  'hiragana', 36  UNION ALL SELECT 'ゆ', 'yu',  'hiragana', 37
    UNION ALL SELECT 'よ', 'yo',  'hiragana', 38  UNION ALL SELECT 'ら', 'ra',  'hiragana', 39
    UNION ALL SELECT 'り', 'ri',  'hiragana', 40  UNION ALL SELECT 'る', 'ru',  'hiragana', 41
    UNION ALL SELECT 'れ', 're',  'hiragana', 42  UNION ALL SELECT 'ろ', 'ro',  'hiragana', 43
    UNION ALL SELECT 'わ', 'wa',  'hiragana', 44  UNION ALL SELECT 'を', 'wo',  'hiragana', 45
    UNION ALL SELECT 'ん', 'n',   'hiragana', 46
    -- ===== HIRAGANA — dakuten / handakuten =====
    UNION ALL SELECT 'が', 'ga',  'hiragana', 47  UNION ALL SELECT 'ぎ', 'gi',  'hiragana', 48
    UNION ALL SELECT 'ぐ', 'gu',  'hiragana', 49  UNION ALL SELECT 'げ', 'ge',  'hiragana', 50
    UNION ALL SELECT 'ご', 'go',  'hiragana', 51  UNION ALL SELECT 'ざ', 'za',  'hiragana', 52
    UNION ALL SELECT 'じ', 'ji',  'hiragana', 53  UNION ALL SELECT 'ず', 'zu',  'hiragana', 54
    UNION ALL SELECT 'ぜ', 'ze',  'hiragana', 55  UNION ALL SELECT 'ぞ', 'zo',  'hiragana', 56
    UNION ALL SELECT 'だ', 'da',  'hiragana', 57  UNION ALL SELECT 'ぢ', 'di',  'hiragana', 58
    UNION ALL SELECT 'づ', 'du',  'hiragana', 59  UNION ALL SELECT 'で', 'de',  'hiragana', 60
    UNION ALL SELECT 'ど', 'do',  'hiragana', 61  UNION ALL SELECT 'ば', 'ba',  'hiragana', 62
    UNION ALL SELECT 'び', 'bi',  'hiragana', 63  UNION ALL SELECT 'ぶ', 'bu',  'hiragana', 64
    UNION ALL SELECT 'べ', 'be',  'hiragana', 65  UNION ALL SELECT 'ぼ', 'bo',  'hiragana', 66
    UNION ALL SELECT 'ぱ', 'pa',  'hiragana', 67  UNION ALL SELECT 'ぴ', 'pi',  'hiragana', 68
    UNION ALL SELECT 'ぷ', 'pu',  'hiragana', 69  UNION ALL SELECT 'ぺ', 'pe',  'hiragana', 70
    UNION ALL SELECT 'ぽ', 'po',  'hiragana', 71
    -- ===== KATAKANA — gojūon =====
    UNION ALL SELECT 'ア', 'a',   'katakana', 1   UNION ALL SELECT 'イ', 'i',   'katakana', 2
    UNION ALL SELECT 'ウ', 'u',   'katakana', 3   UNION ALL SELECT 'エ', 'e',   'katakana', 4
    UNION ALL SELECT 'オ', 'o',   'katakana', 5   UNION ALL SELECT 'カ', 'ka',  'katakana', 6
    UNION ALL SELECT 'キ', 'ki',  'katakana', 7   UNION ALL SELECT 'ク', 'ku',  'katakana', 8
    UNION ALL SELECT 'ケ', 'ke',  'katakana', 9   UNION ALL SELECT 'コ', 'ko',  'katakana', 10
    UNION ALL SELECT 'サ', 'sa',  'katakana', 11  UNION ALL SELECT 'シ', 'shi', 'katakana', 12
    UNION ALL SELECT 'ス', 'su',  'katakana', 13  UNION ALL SELECT 'セ', 'se',  'katakana', 14
    UNION ALL SELECT 'ソ', 'so',  'katakana', 15  UNION ALL SELECT 'タ', 'ta',  'katakana', 16
    UNION ALL SELECT 'チ', 'chi', 'katakana', 17  UNION ALL SELECT 'ツ', 'tsu', 'katakana', 18
    UNION ALL SELECT 'テ', 'te',  'katakana', 19  UNION ALL SELECT 'ト', 'to',  'katakana', 20
    UNION ALL SELECT 'ナ', 'na',  'katakana', 21  UNION ALL SELECT 'ニ', 'ni',  'katakana', 22
    UNION ALL SELECT 'ヌ', 'nu',  'katakana', 23  UNION ALL SELECT 'ネ', 'ne',  'katakana', 24
    UNION ALL SELECT 'ノ', 'no',  'katakana', 25  UNION ALL SELECT 'ハ', 'ha',  'katakana', 26
    UNION ALL SELECT 'ヒ', 'hi',  'katakana', 27  UNION ALL SELECT 'フ', 'fu',  'katakana', 28
    UNION ALL SELECT 'ヘ', 'he',  'katakana', 29  UNION ALL SELECT 'ホ', 'ho',  'katakana', 30
    UNION ALL SELECT 'マ', 'ma',  'katakana', 31  UNION ALL SELECT 'ミ', 'mi',  'katakana', 32
    UNION ALL SELECT 'ム', 'mu',  'katakana', 33  UNION ALL SELECT 'メ', 'me',  'katakana', 34
    UNION ALL SELECT 'モ', 'mo',  'katakana', 35  UNION ALL SELECT 'ヤ', 'ya',  'katakana', 36
    UNION ALL SELECT 'ユ', 'yu',  'katakana', 37  UNION ALL SELECT 'ヨ', 'yo',  'katakana', 38
    UNION ALL SELECT 'ラ', 'ra',  'katakana', 39  UNION ALL SELECT 'リ', 'ri',  'katakana', 40
    UNION ALL SELECT 'ル', 'ru',  'katakana', 41  UNION ALL SELECT 'レ', 're',  'katakana', 42
    UNION ALL SELECT 'ロ', 'ro',  'katakana', 43  UNION ALL SELECT 'ワ', 'wa',  'katakana', 44
    UNION ALL SELECT 'ヲ', 'wo',  'katakana', 45  UNION ALL SELECT 'ン', 'n',   'katakana', 46
    -- ===== KATAKANA — dakuten / handakuten =====
    UNION ALL SELECT 'ガ', 'ga',  'katakana', 47  UNION ALL SELECT 'ギ', 'gi',  'katakana', 48
    UNION ALL SELECT 'グ', 'gu',  'katakana', 49  UNION ALL SELECT 'ゲ', 'ge',  'katakana', 50
    UNION ALL SELECT 'ゴ', 'go',  'katakana', 51  UNION ALL SELECT 'ザ', 'za',  'katakana', 52
    UNION ALL SELECT 'ジ', 'ji',  'katakana', 53  UNION ALL SELECT 'ズ', 'zu',  'katakana', 54
    UNION ALL SELECT 'ゼ', 'ze',  'katakana', 55  UNION ALL SELECT 'ゾ', 'zo',  'katakana', 56
    UNION ALL SELECT 'ダ', 'da',  'katakana', 57  UNION ALL SELECT 'ヂ', 'di',  'katakana', 58
    UNION ALL SELECT 'ヅ', 'du',  'katakana', 59  UNION ALL SELECT 'デ', 'de',  'katakana', 60
    UNION ALL SELECT 'ド', 'do',  'katakana', 61  UNION ALL SELECT 'バ', 'ba',  'katakana', 62
    UNION ALL SELECT 'ビ', 'bi',  'katakana', 63  UNION ALL SELECT 'ブ', 'bu',  'katakana', 64
    UNION ALL SELECT 'ベ', 'be',  'katakana', 65  UNION ALL SELECT 'ボ', 'bo',  'katakana', 66
    UNION ALL SELECT 'パ', 'pa',  'katakana', 67  UNION ALL SELECT 'ピ', 'pi',  'katakana', 68
    UNION ALL SELECT 'プ', 'pu',  'katakana', 69  UNION ALL SELECT 'ペ', 'pe',  'katakana', 70
    UNION ALL SELECT 'ポ', 'po',  'katakana', 71
) AS k
WHERE NOT EXISTS (SELECT 1 FROM kana_characters x);

-- CHÚ Ý migration: bản SQL Server dùng '...' + LOWER(romaji) + '...' để nối
-- chuỗi. Trong MySQL, toán tử + là PHÉP CỘNG SỐ HỌC — nó sẽ ép chuỗi về số và
-- ghi 0 vào audio_url mà KHÔNG báo lỗi. Bắt buộc dùng CONCAT().
UPDATE kana_characters
SET audio_url = CONCAT('/api/files/audio/kana/', LOWER(romaji), '.mp3')
WHERE romaji IS NOT NULL
  AND TRIM(romaji) <> ''
  AND (audio_url IS NULL OR TRIM(audio_url) = '');

-- ぢ/づ (di/du) phát âm trùng じ/ず (ji/zu) → dùng chung file audio đã có.
UPDATE kana_characters SET audio_url = '/api/files/audio/kana/ji.mp3' WHERE romaji = 'di';
UPDATE kana_characters SET audio_url = '/api/files/audio/kana/zu.mp3' WHERE romaji = 'du';

/* ────────────────────────────────────────────────────────────
   ✅ V2 Mock Data seed xong. Tài khoản mẫu:
     ADMIN    : admin@sakuji.com    / Admin@123456
     MANAGER  : manager@sakuji.com  / Staff@123456
     STAFF    : staff@sakuji.com    / Staff@123456
     STUDENT1 : student1@sakuji.com / Student@123456
     STUDENT2 : student2@sakuji.com / Student@123456
   (T-SQL PRINT không có tương đương trong MySQL — chuyển thành comment.)
   ──────────────────────────────────────────────────────────── */
