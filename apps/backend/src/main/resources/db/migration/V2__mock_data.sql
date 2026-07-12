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

/* Không dùng "USE JLPT_LearningDB;" ở đây: JDBC connection string của Flyway
   (spring.datasource.url, tham số databaseName=...) đã trỏ đúng database rồi —
   hard-code tên database cụ thể trong migration khiến nó CHỈ chạy được trên
   1 database tên đúng "JLPT_LearningDB", vỡ ngay trên bất kỳ môi trường nào
   dùng tên khác (staging: JLPT_LearningDB_staging, CI: H2 in-memory, hoặc sau
   này restore sang tên khác). Phát hiện khi test P1.4 (staging) lần đầu — xem
   docs/05-Deployment/Deploy_Improvement_Plan.md, P1.4. Không ảnh hưởng
   production: spring.flyway.validate-on-migrate=false nên sửa nội dung 1
   migration đã áp dụng rồi không làm checksum mismatch fail. */

/* ============================================================
   0. CẤU HÌNH HỆ THỐNG (gộp từ V1 cũ + V25)
   ============================================================ */

INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type)
SELECT g, k, v, t FROM (VALUES
    ('general',           'platform_name',              N'JLPT Learning Platform',           'string'),
    ('general',           'logo_url',                   N'/assets/logo.png',                 'string'),
    ('general',           'default_language',           N'vi',                               'string'),
    ('general',           'maintenance_mode',           N'false',                            'boolean'),
    ('general',           'allow_registration',         N'true',                             'boolean'),
    ('security',          'max_login_attempts',         N'5',                                'integer'),
    ('security',          'session_timeout_min',        N'60',                               'integer'),
    ('security',          'password_reset_min',         N'15',                               'integer'),
    ('smtp',              'host',                       N'smtp.gmail.com',                   'string'),
    ('smtp',              'port',                       N'587',                              'integer'),
    ('smtp',              'username',                   N'',                                 'string'),
    ('smtp',              'from_email',                 N'noreply@jlpt.com',                 'string'),
    ('smtp',              'secure',                     N'STARTTLS',                         'string'),
    ('smtp',              'from_name',                  N'JLPT Platform',                    'string'),
    ('auto_notification', 'streak_10_days_enabled',     N'true',                             'boolean'),
    ('auto_notification', 'streak_10_days_title',       N'Tuyệt vời!',                       'string'),
    ('auto_notification', 'streak_10_days_template',    N'Bạn đã học liên tiếp 10 ngày!',    'string'),
    ('auto_notification', 'daily_flashcard_enabled',    N'true',                             'boolean'),
    ('auto_notification', 'daily_flashcard_time',       N'08:00',                            'time'),
    ('auto_notification', 'daily_flashcard_title',      N'Ôn flashcard',                     'string'),
    ('auto_notification', 'daily_flashcard_template',   N'Đến giờ ôn flashcard rồi!',        'string'),
    ('auto_notification', 'exam_result_ready_enabled',  N'true',                             'boolean'),
    ('auto_notification', 'exam_result_ready_title',    N'Kết quả thi',                      'string'),
    ('auto_notification', 'exam_result_ready_template', N'Điểm bài thi của bạn đã có',       'string'),
    ('email_register',    'from_email',                 N'noreply@jlpt.com',                 'string'),
    ('email_register',    'from_name',                  N'JLPT Platform',                    'string'),
    ('email_register',    'subject',                    N'Xác nhận đăng ký tài khoản',       'string'),
    ('email_otp',         'from_email',                 N'noreply@jlpt.com',                 'string'),
    ('email_otp',         'from_name',                  N'JLPT Platform',                    'string'),
    ('email_otp',         'subject',                    N'Mã xác thực của bạn',              'string'),
    ('email_reset',       'from_email',                 N'noreply@jlpt.com',                 'string'),
    ('email_reset',       'from_name',                  N'JLPT Platform',                    'string'),
    ('email_reset',       'subject',                    N'Cấp lại mật khẩu tài khoản Staff', 'string')
) AS src(g, k, v, t)
WHERE NOT EXISTS (
    SELECT 1 FROM system_settings s
    WHERE s.setting_group = src.g AND s.setting_key = src.k
);
GO

/* ============================================================
   I. TÀI KHOẢN NGƯỜI DÙNG
   ============================================================ */

DECLARE @admin_id      BIGINT,
        @manager_id    BIGINT,
        @staff_id      BIGINT,
        @student1_id   BIGINT,
        @student2_id   BIGINT;

-- ── 1. Admin ──────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM admin_users WHERE email = 'admin@sakuji.com')
    INSERT INTO admin_users (
        email, password_hash, full_name,
        status,
        login_attempts, created_at, updated_at
    ) VALUES (
        'admin@sakuji.com',
        '$2b$12$kJcYAwbtPEqtu8tZUWv4fetUeoydXgfwS4ckmsiHW3oLU3Hhtig/G',   -- Admin@123456
        N'Quản Trị Viên',
        'active',
        0, SYSUTCDATETIME(), SYSUTCDATETIME()
    );
SELECT @admin_id = admin_id FROM admin_users WHERE email = 'admin@sakuji.com';

-- ── 2. Staff Manager ─────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM staff_users WHERE email = 'manager@sakuji.com')
    INSERT INTO staff_users (
        email, password_hash, full_name, staff_role,
        status,
        login_attempts, created_at, updated_at
    ) VALUES (
        'manager@sakuji.com',
        '$2b$12$9HCWZhWE7OgwP2/SMU3Ob.zscUYQ2oV28ArNO3x6F/NboRnFhofmu',   -- Staff@123456
        N'Trưởng Nhóm Nội Dung',
        'staff_manager',
        'active',
        0, SYSUTCDATETIME(), SYSUTCDATETIME()
    );
SELECT @manager_id = staff_id FROM staff_users WHERE email = 'manager@sakuji.com';

-- ── 3. Staff ────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM staff_users WHERE email = 'staff@sakuji.com')
    INSERT INTO staff_users (
        email, password_hash, full_name, staff_role,
        status,
        login_attempts, created_at, updated_at
    ) VALUES (
        'staff@sakuji.com',
        '$2b$12$9HCWZhWE7OgwP2/SMU3Ob.zscUYQ2oV28ArNO3x6F/NboRnFhofmu',   -- Staff@123456
        N'Biên Soạn Viên',
        'staff',
        'active',
        0, SYSUTCDATETIME(), SYSUTCDATETIME()
    );
SELECT @staff_id = staff_id FROM staff_users WHERE email = 'staff@sakuji.com';

-- ── 4. Học viên 1 ────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM student_users WHERE email = 'student1@sakuji.com')
    INSERT INTO student_users (
        email, password_hash, full_name,
        status, email_verified_at,
        current_jlpt_level, target_jlpt_level,
        current_streak, longest_streak, last_activity_date,
        login_attempts, created_at, updated_at
    ) VALUES (
        'student1@sakuji.com',
        '$2b$12$JrZ/8BbDiUQxHPXrHvUBQOTA043UiqP9HNinbrnRale4.9tU2B2im',   -- Student@123456
        N'Nguyễn Minh Anh',
        'active', SYSUTCDATETIME(),
        'N5', 'N3',
        7, 14, CAST(GETUTCDATE() AS DATE),
        0, SYSUTCDATETIME(), SYSUTCDATETIME()
    );
SELECT @student1_id = student_id FROM student_users WHERE email = 'student1@sakuji.com';

-- ── 5. Học viên 2 ────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM student_users WHERE email = 'student2@sakuji.com')
    INSERT INTO student_users (
        email, password_hash, full_name,
        status, email_verified_at,
        current_jlpt_level, target_jlpt_level,
        current_streak, longest_streak, last_activity_date,
        login_attempts, created_at, updated_at
    ) VALUES (
        'student2@sakuji.com',
        '$2b$12$JrZ/8BbDiUQxHPXrHvUBQOTA043UiqP9HNinbrnRale4.9tU2B2im',   -- Student@123456
        N'Trần Thị Lan',
        'active', SYSUTCDATETIME(),
        'N4', 'N2',
        3, 21, CAST(GETUTCDATE() AS DATE),
        0, SYSUTCDATETIME(), SYSUTCDATETIME()
    );
SELECT @student2_id = student_id FROM student_users WHERE email = 'student2@sakuji.com';
GO


/* ============================================================
   I-b. DANH MỤC CHỦ ĐỀ TỪ VỰNG (gộp từ V10 — bắt buộc trước III)
   ============================================================ */

DECLARE @topic_staff_id BIGINT = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');

INSERT INTO vocabulary_topics (jlpt_level, slug, title_ja, title_vi, display_order, status, created_by) VALUES
('N5', N'family',        N'家族',   N'Gia đình',   1,  'published', @topic_staff_id),
('N5', N'food',          N'食べ物', N'Ẩm thực',    2,  'published', @topic_staff_id),
('N5', N'transport',     N'交通',   N'Giao thông', 3,  'published', @topic_staff_id),
('N5', N'time',          N'時間',   N'Thời gian',  4,  'published', @topic_staff_id),
('N5', N'place',         N'場所',   N'Địa điểm',   5,  'published', @topic_staff_id),
('N5', N'description',   N'描写',   N'Mô tả',      6,  'published', @topic_staff_id),
('N5', N'weather',       N'天気',   N'Thời tiết',  7,  'published', @topic_staff_id),
('N5', N'education',     N'学校',   N'Giáo dục',   8,  'published', @topic_staff_id),
('N5', N'society',       N'社会',   N'Xã hội',     9,  'published', @topic_staff_id),
('N5', N'technology',    N'技術',   N'Công nghệ',  10, 'published', @topic_staff_id),

('N4', N'travel',        N'旅行',   N'Du lịch',    1, 'published', @topic_staff_id),
('N4', N'emotion',       N'感情',   N'Cảm xúc',    2, 'published', @topic_staff_id),
('N4', N'shopping',      N'買い物', N'Mua sắm',    3, 'published', @topic_staff_id),
('N4', N'health',        N'健康',   N'Sức khỏe',   4, 'published', @topic_staff_id),
('N4', N'education',     N'学校',   N'Giáo dục',   5, 'published', @topic_staff_id),
('N4', N'family',        N'家族',   N'Gia đình',   6, 'published', @topic_staff_id),

('N3', N'work',          N'仕事',   N'Công việc',  1, 'published', @topic_staff_id),
('N3', N'society',       N'社会',   N'Xã hội',     2, 'published', @topic_staff_id),
('N3', N'nature',        N'自然',   N'Thiên nhiên',3, 'published', @topic_staff_id),
('N3', N'thinking',      N'思考',   N'Tư duy',     4, 'published', @topic_staff_id),
('N3', N'communication', N'連絡',   N'Giao tiếp',  5, 'published', @topic_staff_id),

('N2', N'academia',      N'学術',   N'Học thuật',  1, 'published', @topic_staff_id),
('N2', N'economy',       N'経済',   N'Kinh tế',    2, 'published', @topic_staff_id),
('N2', N'law',           N'法律',   N'Pháp luật',  3, 'published', @topic_staff_id),
('N2', N'psychology',    N'心理',   N'Tâm lý',     4, 'published', @topic_staff_id),

('N1', N'philosophy',    N'哲学',   N'Triết học',  1, 'published', @topic_staff_id),
('N1', N'academia',      N'学術',   N'Học thuật',  2, 'published', @topic_staff_id),
('N1', N'literature',    N'文学',   N'Văn học',    3, 'published', @topic_staff_id),
('N1', N'technology',    N'技術',   N'Công nghệ',  4, 'published', @topic_staff_id),
('N1', N'environment',   N'環境',   N'Môi trường', 5, 'published', @topic_staff_id);
GO


/* ============================================================
   II. KANJI MẪU — N5 (published)
   ============================================================ */

DECLARE @staff_id BIGINT = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM kanji WHERE character_value = N'山')
BEGIN
    INSERT INTO kanji (
        character_value, meaning, onyomi, kunyomi,
        stroke_count, jlpt_level,
        example_word, example_reading, example_meaning,
        status, created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES
    ( N'山', N'Núi',          N'サン',   N'やま',    3,  'N5', N'富士山',  N'ふじさん',  N'Núi Phú Sĩ',   'published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'水', N'Nước',         N'スイ',   N'みず',    4,  'N5', N'水道',    N'すいどう',  N'Đường nước',   'published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'火', N'Lửa',          N'カ',     N'ひ',      4,  'N5', N'火事',    N'かじ',      N'Hỏa hoạn',     'published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'木', N'Cây gỗ',       N'モク',   N'き',      4,  'N5', N'木曜日',  N'もくようび',N'Thứ năm',      'published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'日', N'Mặt trời/Ngày',N'ニチ',   N'ひ',      4,  'N5', N'日本語',  N'にほんご',  N'Tiếng Nhật',   'published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'月', N'Mặt trăng/Tháng',N'ゲツ', N'つき',    4,  'N5', N'一月',    N'いちがつ',  N'Tháng Một',    'published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'人', N'Con người',    N'ジン',   N'ひと',    2,  'N5', N'外国人',  N'がいこくじん',N'Người nước ngoài','published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'口', N'Miệng',        N'コウ',   N'くち',    3,  'N5', N'人口',    N'じんこう',  N'Dân số',       'published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'車', N'Xe',           N'シャ',   N'くるま',  7,  'N5', N'電車',    N'でんしゃ',  N'Tàu điện',     'published', @staff_id, @manager_id, @now, @now, @now ),
    ( N'語', N'Ngôn ngữ',     N'ゴ',     N'かたる',  14, 'N5', N'日本語',  N'にほんご',  N'Tiếng Nhật',   'published', @staff_id, @manager_id, @now, @now, @now );
END
GO


/* ============================================================
   III. TỪ VỰNG MẪU — N5 (published)
   ============================================================ */

DECLARE @staff_id  BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT  = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now       DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM vocabulary WHERE word = N'食べる')
INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic_id,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
SELECT v.word, v.furigana, v.meaning, v.word_type, v.jlpt_level,
       (SELECT topic_id FROM vocabulary_topics t WHERE t.jlpt_level = v.jlpt_level AND t.title_vi = v.topic_vi),
       v.example_jp, v.example_vi,
       'published', @staff_id, @manager_id, @now, @now, @now
FROM (VALUES
    ( N'食べる',   N'たべる',     N'Ăn',                     N'động từ nhóm 2', 'N5', N'Ẩm thực',  N'私は毎朝ご飯を食べる。', N'Tôi ăn cơm mỗi buổi sáng.' ),
    ( N'飲む',     N'のむ',       N'Uống',                   N'động từ nhóm 1', 'N5', N'Ẩm thực',  N'水を飲む。', N'Uống nước.' ),
    ( N'学校',     N'がっこう',   N'Trường học',             N'danh từ',        'N5', N'Giáo dục', N'学校に行く。', N'Đi đến trường.' ),
    ( N'先生',     N'せんせい',   N'Giáo viên',              N'danh từ',        'N5', N'Giáo dục', N'先生はやさしい。', N'Giáo viên thân thiện.' ),
    ( N'友達',     N'ともだち',   N'Bạn bè',                 N'danh từ',        'N5', N'Xã hội',   N'友達と遊ぶ。', N'Chơi với bạn bè.' ),
    ( N'きれい',   N'きれい',     N'Đẹp / Sạch sẽ',         N'tính từ đuôi な','N5', N'Mô tả',    N'この花はきれいです。', N'Bông hoa này rất đẹp.' ),
    ( N'大きい',   N'おおきい',   N'To lớn',                 N'tính từ đuôi い','N5', N'Mô tả',    N'大きい犬がいる。', N'Có một con chó to.' ),
    ( N'電話',     N'でんわ',     N'Điện thoại',             N'danh từ',        'N5', N'Công nghệ',N'電話をかける。', N'Gọi điện thoại.' )
) AS v(word, furigana, meaning, word_type, jlpt_level, topic_vi, example_jp, example_vi);
GO


/* ============================================================
   IV. BÀI HỌC MẪU — N5
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();
DECLARE @lesson_id  BIGINT;

IF NOT EXISTS (SELECT 1 FROM lessons WHERE title = N'Giới thiệu Kanji cơ bản N5')
    INSERT INTO lessons (
        lesson_type, title, jlpt_level,
        content_text, explanation,
        display_order, status,
        created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES (
        'lesson',
        N'Giới thiệu Kanji cơ bản N5',
        'N5',
        N'Trong bài học này bạn sẽ học 10 Kanji cơ bản nhất ở cấp độ N5: 山 水 火 木 日 月 人 口 車 語. Mỗi chữ đều có âm On''yomi, Kun''yomi và ví dụ câu đi kèm.',
        N'Hãy luyện tập viết tay mỗi chữ ít nhất 5 lần để ghi nhớ nét bút.',
        1, 'published',
        @staff_id, @manager_id, @now, @now, @now
    );
SELECT @lesson_id = lesson_id FROM lessons WHERE title = N'Giới thiệu Kanji cơ bản N5';
GO


/* ============================================================
   V. CÂU HỎI MẪU + BÀI QUIZ
   ============================================================ */

DECLARE @staff_id    BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id  BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @lesson_id   BIGINT   = (SELECT TOP 1 lesson_id FROM lessons WHERE title LIKE N'%N5%');
DECLARE @now         DATETIME2 = SYSUTCDATETIME();
DECLARE @q1 BIGINT, @q2 BIGINT, @q3 BIGINT, @q4 BIGINT, @q5 BIGINT;
DECLARE @assessment_id BIGINT;

-- Câu hỏi 1
IF NOT EXISTS (SELECT 1 FROM questions WHERE question_text = N'「山」の読み方はどれですか？')
    INSERT INTO questions (
        question_text, question_type, skill, jlpt_level,
        option_a, option_b, option_c, option_d, correct_option,
        explanation, status, created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES (
        N'「山」の読み方はどれですか？',
        'multiple_choice', 'kanji', 'N5',
        N'やま', N'かわ', N'もり', N'うみ', 'A',
        N'「山」はやま（kun）またはサン（on）と読みます。',
        'published', @staff_id, @manager_id, @now, @now, @now
    );
SELECT @q1 = question_id FROM questions WHERE question_text = N'「山」の読み方はどれですか？';

-- Câu hỏi 2
IF NOT EXISTS (SELECT 1 FROM questions WHERE question_text = N'「水」の意味は何ですか？')
    INSERT INTO questions (
        question_text, question_type, skill, jlpt_level,
        option_a, option_b, option_c, option_d, correct_option,
        explanation, status, created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES (
        N'「水」の意味は何ですか？',
        'multiple_choice', 'kanji', 'N5',
        N'Lửa', N'Nước', N'Gió', N'Đất', 'B',
        N'「水」（みず）はWaterの意味です。',
        'published', @staff_id, @manager_id, @now, @now, @now
    );
SELECT @q2 = question_id FROM questions WHERE question_text = N'「水」の意味は何ですか？';

-- Câu hỏi 3
IF NOT EXISTS (SELECT 1 FROM questions WHERE question_text = N'（　）に入る言葉を選んでください。「私は毎朝ご飯を（　）。」')
    INSERT INTO questions (
        question_text, question_type, skill, jlpt_level,
        option_a, option_b, option_c, option_d, correct_option,
        explanation, status, created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES (
        N'（　）に入る言葉を選んでください。「私は毎朝ご飯を（　）。」',
        'multiple_choice', 'vocabulary', 'N5',
        N'飲む', N'食べる', N'見る', N'聞く', 'B',
        N'「食べる」は to eat の意味です。',
        'published', @staff_id, @manager_id, @now, @now, @now
    );
SELECT @q3 = question_id FROM questions WHERE question_text = N'（　）に入る言葉を選んでください。「私は毎朝ご飯を（　）。」';

-- Câu hỏi 4
IF NOT EXISTS (SELECT 1 FROM questions WHERE question_text = N'「先生」の意味はどれですか？')
    INSERT INTO questions (
        question_text, question_type, skill, jlpt_level,
        option_a, option_b, option_c, option_d, correct_option,
        explanation, status, created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES (
        N'「先生」の意味はどれですか？',
        'multiple_choice', 'vocabulary', 'N5',
        N'Học sinh', N'Giáo viên', N'Bố', N'Mẹ', 'B',
        N'「先生」（せんせい）は Teacher の意味です。',
        'published', @staff_id, @manager_id, @now, @now, @now
    );
SELECT @q4 = question_id FROM questions WHERE question_text = N'「先生」の意味はどれですか？';

-- Câu hỏi 5
IF NOT EXISTS (SELECT 1 FROM questions WHERE question_text = N'「日本語」の正しい読み方はどれですか？')
    INSERT INTO questions (
        question_text, question_type, skill, jlpt_level,
        option_a, option_b, option_c, option_d, correct_option,
        explanation, status, created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES (
        N'「日本語」の正しい読み方はどれですか？',
        'multiple_choice', 'kanji', 'N5',
        N'にほんご', N'にっぽんご', N'にほんぐ', N'にほんく', 'A',
        N'「日本語」はにほんごと読みます（Japanese language）。',
        'published', @staff_id, @manager_id, @now, @now, @now
    );
SELECT @q5 = question_id FROM questions WHERE question_text = N'「日本語」の正しい読み方はどれですか？';

-- Quiz N5 tổng hợp
IF NOT EXISTS (SELECT 1 FROM assessments WHERE title = N'Quiz Kanji & Từ vựng N5 — Bài 1')
    INSERT INTO assessments (
        assessment_type, title, lesson_id, topic, jlpt_level,
        duration_min, pass_score, total_score,
        status, created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES (
        'quiz',
        N'Quiz Kanji & Từ vựng N5 — Bài 1',
        @lesson_id,
        N'Kanji N5',
        'N5',
        10, 60, 100,
        'published', @staff_id, @manager_id, @now, @now, @now
    );
SELECT @assessment_id = assessment_id FROM assessments WHERE title = N'Quiz Kanji & Từ vựng N5 — Bài 1';

-- Gán câu hỏi vào quiz
IF NOT EXISTS (SELECT 1 FROM question_assignments WHERE parent_type = 'assessment' AND parent_id = @assessment_id)
    INSERT INTO question_assignments (parent_type, parent_id, question_id, score, display_order)
    VALUES
        ('assessment', @assessment_id, @q1, 20, 1),
        ('assessment', @assessment_id, @q2, 20, 2),
        ('assessment', @assessment_id, @q3, 20, 3),
        ('assessment', @assessment_id, @q4, 20, 4),
        ('assessment', @assessment_id, @q5, 20, 5);
GO


/* ============================================================
   VI. TIẾN ĐỘ HỌC MẪU (Student 1)
   ============================================================ */

DECLARE @student1_id BIGINT = (SELECT TOP 1 student_id FROM student_users WHERE email = 'student1@sakuji.com');
DECLARE @lesson_id   BIGINT = (SELECT TOP 1 lesson_id  FROM lessons     WHERE title LIKE N'%N5%');

-- Tiến độ bài học
IF NOT EXISTS (SELECT 1 FROM student_content_progress WHERE student_id = @student1_id AND content_type = 'lesson' AND content_id = @lesson_id)
    INSERT INTO student_content_progress
        (student_id, content_type, content_id, status, progress_percent, last_studied_at, created_at)
    VALUES
        (@student1_id, 'lesson', @lesson_id, 'completed', 100.0, SYSUTCDATETIME(), SYSUTCDATETIME());

-- Tiến độ 5 Kanji đầu
INSERT INTO student_content_progress
    (student_id, content_type, content_id, status, progress_percent, last_studied_at, created_at)
SELECT TOP 5
    @student1_id, 'kanji', kanji_id, 'completed', 100.0, SYSUTCDATETIME(), SYSUTCDATETIME()
FROM kanji
WHERE jlpt_level = 'N5'
  AND NOT EXISTS (
    SELECT 1 FROM student_content_progress p
    WHERE p.student_id = @student1_id AND p.content_type = 'kanji' AND p.content_id = kanji.kanji_id
  )
ORDER BY kanji_id;

-- Tiến độ 3 từ vựng
INSERT INTO student_content_progress
    (student_id, content_type, content_id, status, progress_percent, last_studied_at, created_at)
SELECT TOP 3
    @student1_id, 'vocabulary', vocabulary_id, 'learning', 60.0, SYSUTCDATETIME(), SYSUTCDATETIME()
FROM vocabulary
WHERE jlpt_level = 'N5'
  AND NOT EXISTS (
    SELECT 1 FROM student_content_progress p
    WHERE p.student_id = @student1_id AND p.content_type = 'vocabulary' AND p.content_id = vocabulary.vocabulary_id
  )
ORDER BY vocabulary_id;
GO


/* ============================================================
   VII. THÔNG BÁO MẪU
   ============================================================ */

DECLARE @admin_id    BIGINT = (SELECT TOP 1 admin_id   FROM admin_users   WHERE email = 'admin@sakuji.com');
DECLARE @student1_id BIGINT = (SELECT TOP 1 student_id FROM student_users WHERE email = 'student1@sakuji.com');
DECLARE @student2_id BIGINT = (SELECT TOP 1 student_id FROM student_users WHERE email = 'student2@sakuji.com');

IF NOT EXISTS (SELECT 1 FROM notifications WHERE student_id = @student1_id AND title = N'Chào mừng đến với SakuJi!')
    INSERT INTO notifications (
        student_id, title, content, notification_type, channel,
        is_auto, sent_at, admin_creator_id, created_at
    ) VALUES
    (
        @student1_id,
        N'Chào mừng đến với SakuJi!',
        N'Chào Minh Anh! Tài khoản của bạn đã sẵn sàng. Hãy bắt đầu hành trình học tiếng Nhật ngay hôm nay 🌸',
        'system', 'in_app', 0, SYSUTCDATETIME(), @admin_id, SYSUTCDATETIME()
    ),
    (
        @student1_id,
        N'Streak 7 ngày — Tuyệt vời!',
        N'Bạn đã học liên tiếp 7 ngày! Saku-chan rất tự hào về bạn 🎉 Hãy tiếp tục duy trì nhé!',
        'achievement', 'in_app', 1, SYSUTCDATETIME(), NULL, SYSUTCDATETIME()
    ),
    (
        @student2_id,
        N'Chào mừng đến với SakuJi!',
        N'Chào Thị Lan! Tài khoản của bạn đã sẵn sàng. Chúc bạn chinh phục JLPT N2 thành công!',
        'system', 'in_app', 0, SYSUTCDATETIME(), @admin_id, SYSUTCDATETIME()
    );
GO


/* ============================================================
   VIII. AUDIT LOG — ADMIN TẠO TÀI KHOẢN
   ============================================================ */

DECLARE @admin_id BIGINT = (SELECT TOP 1 admin_id FROM admin_users WHERE email = 'admin@sakuji.com');

INSERT INTO admin_audit_logs (admin_actor_id, action, target_table, description, ip_address, created_at)
VALUES
(
    @admin_id,
    'seed_mock_data',
    'admin_users, staff_users, student_users',
    N'Seed dữ liệu mẫu cho môi trường DEV — V2 migration',
    '127.0.0.1',
    SYSUTCDATETIME()
);
GO


/* ============================================================
   IX. KANJI BỔ SUNG N4 (gộp từ V7 — phong phú dictionary search)
   ============================================================ */

DECLARE @staff_id9   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id9 BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now9        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM kanji WHERE character_value = N'海')
INSERT INTO kanji (character_value, meaning, onyomi, kunyomi, stroke_count, jlpt_level,
    example_word, example_reading, example_meaning,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
( N'海', N'Biển / Đại dương', N'カイ',   N'うみ',        9,  'N4', N'海外', N'かいがい', N'Nước ngoài',      'published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'旅', N'Du hành / Du lịch',N'リョ',   N'たび',        10, 'N4', N'旅行', N'りょこう', N'Du lịch',         'published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'花', N'Hoa',              N'カ',     N'はな',        7,  'N4', N'花見', N'はなみ',   N'Ngắm hoa anh đào','published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'空', N'Bầu trời / Trống', N'クウ',   N'そら・から',  8,  'N4', N'空港', N'くうこう', N'Sân bay',          'published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'川', N'Sông / Suối',      N'セン',   N'かわ',        3,  'N4', N'川沿い',N'かわぞい',N'Dọc bờ sông',     'published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'雨', N'Mưa',              N'ウ',     N'あめ',        8,  'N4', N'大雨', N'おおあめ', N'Mưa lớn',          'published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'雪', N'Tuyết',            N'セツ',   N'ゆき',        11, 'N4', N'雪山', N'ゆきやま', N'Núi tuyết',        'published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'風', N'Gió / Phong tục',  N'フウ・フ',N'かぜ・かざ', 9,  'N4', N'台風', N'たいふう', N'Bão',              'published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'体', N'Cơ thể / Thân',    N'タイ',   N'からだ',      7,  'N4', N'体力', N'たいりょく',N'Thể lực',         'published', @staff_id9, @manager_id9, @now9, @now9, @now9 ),
( N'道', N'Con đường / Đạo',  N'ドウ',   N'みち',        12, 'N4', N'道路', N'どうろ',   N'Đường bộ',         'published', @staff_id9, @manager_id9, @now9, @now9, @now9 );
GO


/* ============================================================
   X. TỪ VỰNG BỔ SUNG N5→N1 (gộp từ V7)
   ============================================================ */

DECLARE @staff_id10   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id10 BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now10         DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM vocabulary WHERE word = N'母')
INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic_id,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
SELECT v.word, v.furigana, v.meaning, v.word_type, v.jlpt_level,
       (SELECT topic_id FROM vocabulary_topics t WHERE t.jlpt_level = v.jlpt_level AND t.title_vi = v.topic_vi),
       v.example_jp, v.example_vi,
       'published', @staff_id10, @manager_id10, @now10, @now10, @now10
FROM (VALUES
-- N5
( N'母',       N'はは',           N'Mẹ (cách nói khiêm tốn)',    N'danh từ',           'N5', N'Gia đình', N'母は料理が上手です。', N'Mẹ tôi nấu ăn rất giỏi.' ),
( N'父',       N'ちち',           N'Bố (cách nói khiêm tốn)',    N'danh từ',           'N5', N'Gia đình', N'父は会社員です。', N'Bố tôi là nhân viên công ty.' ),
( N'兄',       N'あに',           N'Anh trai (khiêm tốn)',       N'danh từ',           'N5', N'Gia đình', N'兄は大学生です。', N'Anh trai tôi là sinh viên đại học.' ),
( N'姉',       N'あね',           N'Chị gái (khiêm tốn)',        N'danh từ',           'N5', N'Gia đình', N'姉は先生です。', N'Chị tôi là giáo viên.' ),
( N'家族',     N'かぞく',         N'Gia đình',                   N'danh từ',           'N5', N'Gia đình', N'私の家族は四人です。', N'Gia đình tôi có bốn người.' ),
( N'ご飯',     N'ごはん',         N'Cơm / Bữa ăn',              N'danh từ',           'N5', N'Ẩm thực',  N'毎日ご飯を食べます。', N'Mỗi ngày tôi ăn cơm.' ),
( N'お茶',     N'おちゃ',         N'Trà',                        N'danh từ',           'N5', N'Ẩm thực',  N'お茶を飲みますか。', N'Bạn có uống trà không?' ),
( N'魚',       N'さかな',         N'Cá',                         N'danh từ',           'N5', N'Ẩm thực',  N'魚が好きです。', N'Tôi thích cá.' ),
( N'肉',       N'にく',           N'Thịt',                       N'danh từ',           'N5', N'Ẩm thực',  N'肉を買いました。', N'Tôi đã mua thịt.' ),
( N'野菜',     N'やさい',         N'Rau củ',                     N'danh từ',           'N5', N'Ẩm thực',  N'野菜をたくさん食べてください。', N'Hãy ăn nhiều rau nhé.' ),
( N'駅',       N'えき',           N'Ga tàu',                     N'danh từ',           'N5', N'Giao thông', N'駅まで歩きます。', N'Tôi đi bộ đến ga.' ),
( N'バス',     N'バス',           N'Xe buýt',                    N'danh từ',           'N5', N'Giao thông', N'バスで学校に行きます。', N'Tôi đi học bằng xe buýt.' ),
( N'飛行機',   N'ひこうき',       N'Máy bay',                    N'danh từ',           'N5', N'Giao thông', N'飛行機に乗りました。', N'Tôi đã đi máy bay.' ),
( N'自転車',   N'じてんしゃ',     N'Xe đạp',                     N'danh từ',           'N5', N'Giao thông', N'自転車で通学します。', N'Tôi đi học bằng xe đạp.' ),
( N'今日',     N'きょう',         N'Hôm nay',                    N'danh từ',           'N5', N'Thời gian', N'今日は月曜日です。', N'Hôm nay là thứ Hai.' ),
( N'明日',     N'あした',         N'Ngày mai',                   N'danh từ',           'N5', N'Thời gian', N'明日は休みです。', N'Ngày mai tôi được nghỉ.' ),
( N'昨日',     N'きのう',         N'Hôm qua',                    N'danh từ',           'N5', N'Thời gian', N'昨日は雨でした。', N'Hôm qua trời mưa.' ),
( N'病院',     N'びょういん',     N'Bệnh viện',                  N'danh từ',           'N5', N'Địa điểm', N'病院に行きます。', N'Tôi đến bệnh viện.' ),
( N'図書館',   N'としょかん',     N'Thư viện',                   N'danh từ',           'N5', N'Địa điểm', N'図書館で本を読みます。', N'Tôi đọc sách ở thư viện.' ),
( N'銀行',     N'ぎんこう',       N'Ngân hàng',                  N'danh từ',           'N5', N'Địa điểm', N'銀行でお金をおろします。', N'Tôi rút tiền ở ngân hàng.' ),
( N'新しい',   N'あたらしい',     N'Mới',                        N'tính từ đuôi い',  'N5', N'Mô tả', N'新しい本を買いました。', N'Tôi đã mua cuốn sách mới.' ),
( N'古い',     N'ふるい',         N'Cũ / Lâu đời',              N'tính từ đuôi い',  'N5', N'Mô tả', N'古い映画が好きです。', N'Tôi thích phim cũ.' ),
( N'暑い',     N'あつい',         N'Nóng',                       N'tính từ đuôi い',  'N5', N'Thời tiết', N'今日はとても暑いです。', N'Hôm nay rất nóng.' ),
( N'寒い',     N'さむい',         N'Lạnh',                       N'tính từ đuôi い',  'N5', N'Thời tiết', N'冬は寒いです。', N'Mùa đông rất lạnh.' ),
-- N4
( N'旅行',     N'りょこう',       N'Du lịch',                    N'danh từ / động từ', 'N4', N'Du lịch', N'来月、京都へ旅行します。', N'Tháng tới tôi đi du lịch Kyoto.' ),
( N'予約',     N'よやく',         N'Đặt trước / Đặt chỗ',       N'danh từ / động từ', 'N4', N'Du lịch', N'ホテルを予約しました。', N'Tôi đã đặt khách sạn.' ),
( N'地図',     N'ちず',           N'Bản đồ',                     N'danh từ',           'N4', N'Du lịch', N'地図を見ながら歩きました。', N'Tôi đi bộ trong khi nhìn bản đồ.' ),
( N'嬉しい',   N'うれしい',       N'Vui mừng / Sung sướng',     N'tính từ đuôi い',  'N4', N'Cảm xúc', N'合格して嬉しいです。', N'Tôi vui vì đã đỗ.' ),
( N'悲しい',   N'かなしい',       N'Buồn',                       N'tính từ đuôi い',  'N4', N'Cảm xúc', N'別れが悲しいです。', N'Tôi buồn khi chia tay.' ),
( N'心配',     N'しんぱい',       N'Lo lắng',                    N'danh từ / động từ', 'N4', N'Cảm xúc', N'試験が心配です。', N'Tôi lo lắng về kỳ thi.' ),
( N'値段',     N'ねだん',         N'Giá cả',                     N'danh từ',           'N4', N'Mua sắm', N'この服の値段はいくらですか。', N'Bộ quần áo này giá bao nhiêu?' ),
( N'お釣り',   N'おつり',         N'Tiền thừa / Tiền trả lại',  N'danh từ',           'N4', N'Mua sắm', N'お釣りをもらいました。', N'Tôi đã nhận lại tiền thừa.' ),
( N'割引',     N'わりびき',       N'Giảm giá / Chiết khấu',     N'danh từ / động từ', 'N4', N'Mua sắm', N'10%割引のセールがあります。', N'Có đợt sale giảm 10%.' ),
( N'熱',       N'ねつ',           N'Sốt / Nhiệt độ',             N'danh từ',           'N4', N'Sức khỏe', N'熱が出ました。', N'Tôi bị sốt.' ),
( N'薬',       N'くすり',         N'Thuốc',                      N'danh từ',           'N4', N'Sức khỏe', N'薬を飲んでください。', N'Hãy uống thuốc đi.' ),
( N'休む',     N'やすむ',         N'Nghỉ ngơi / Nghỉ học/làm',  N'động từ nhóm 1',    'N4', N'Sức khỏe', N'今日は体調が悪くて休みました。', N'Hôm nay tôi nghỉ vì không khỏe.' ),
( N'試験',     N'しけん',         N'Kỳ thi / Kiểm tra',         N'danh từ',           'N4', N'Giáo dục', N'来週試験があります。', N'Tuần tới có kỳ thi.' ),
( N'合格',     N'ごうかく',       N'Đỗ / Đạt yêu cầu',          N'danh từ / động từ', 'N4', N'Giáo dục', N'JLPT N4に合格しました！', N'Tôi đã đỗ JLPT N4!' ),
( N'練習',     N'れんしゅう',     N'Luyện tập',                  N'danh từ / động từ', 'N4', N'Giáo dục', N'毎日日本語を練習します。', N'Tôi luyện tiếng Nhật mỗi ngày.' ),
( N'単語',     N'たんご',         N'Từ vựng / Từ đơn',           N'danh từ',           'N4', N'Giáo dục', N'毎日新しい単語を覚えます。', N'Mỗi ngày tôi học từ vựng mới.' ),
( N'結婚',     N'けっこん',       N'Kết hôn',                    N'danh từ / động từ', 'N4', N'Gia đình', N'来年結婚する予定です。', N'Tôi dự định kết hôn năm tới.' ),
( N'子供',     N'こども',         N'Trẻ em / Con cái',           N'danh từ',           'N4', N'Gia đình', N'子供が二人います。', N'Tôi có hai đứa con.' ),
-- N3
( N'経験',     N'けいけん',       N'Kinh nghiệm',                N'danh từ / động từ', 'N3', N'Công việc', N'海外での経験が役に立ちます。', N'Kinh nghiệm làm việc ở nước ngoài rất hữu ích.' ),
( N'仕事',     N'しごと',         N'Công việc / Việc làm',       N'danh từ',           'N3', N'Công việc', N'新しい仕事を探しています。', N'Tôi đang tìm việc mới.' ),
( N'会議',     N'かいぎ',         N'Cuộc họp',                   N'danh từ',           'N3', N'Công việc', N'午後から会議があります。', N'Chiều nay có cuộc họp.' ),
( N'締め切り', N'しめきり',       N'Hạn chót / Deadline',        N'danh từ',           'N3', N'Công việc', N'締め切りに間に合いました。', N'Tôi đã kịp hạn chót.' ),
( N'給料',     N'きゅうりょう',   N'Lương / Tiền lương',         N'danh từ',           'N3', N'Công việc', N'今月の給料が入りました。', N'Lương tháng này đã vào.' ),
( N'社会',     N'しゃかい',       N'Xã hội',                     N'danh từ',           'N3', N'Xã hội', N'現代社会では情報が大切です。', N'Trong xã hội hiện đại, thông tin rất quan trọng.' ),
( N'文化',     N'ぶんか',         N'Văn hóa',                    N'danh từ',           'N3', N'Xã hội', N'日本の文化を学んでいます。', N'Tôi đang học văn hóa Nhật Bản.' ),
( N'伝統',     N'でんとう',       N'Truyền thống',               N'danh từ',           'N3', N'Xã hội', N'日本の伝統を大切にしています。', N'Chúng tôi coi trọng truyền thống Nhật Bản.' ),
( N'海',       N'うみ',           N'Biển / Đại dương',           N'danh từ',           'N3', N'Thiên nhiên', N'海で泳ぎました。', N'Tôi đã bơi ở biển.' ),
( N'森',       N'もり',           N'Rừng',                       N'danh từ',           'N3', N'Thiên nhiên', N'森の中を散歩しました。', N'Tôi đã tản bộ trong rừng.' ),
( N'環境',     N'かんきょう',     N'Môi trường',                 N'danh từ',           'N3', N'Thiên nhiên', N'環境を守ることが大切です。', N'Bảo vệ môi trường rất quan trọng.' ),
( N'努力',     N'どりょく',       N'Nỗ lực / Cố gắng',          N'danh từ / động từ', 'N3', N'Tư duy', N'努力すれば夢は叶います。', N'Nếu cố gắng, ước mơ sẽ trở thành hiện thực.' ),
( N'成功',     N'せいこう',       N'Thành công',                 N'danh từ / động từ', 'N3', N'Tư duy', N'プロジェクトが成功しました。', N'Dự án đã thành công.' ),
( N'失敗',     N'しっぱい',       N'Thất bại',                   N'danh từ / động từ', 'N3', N'Tư duy', N'失敗から学ぶことが大切です。', N'Học từ thất bại là điều quan trọng.' ),
( N'意見',     N'いけん',         N'Ý kiến / Quan điểm',         N'danh từ',           'N3', N'Tư duy', N'あなたの意見を聞かせてください。', N'Hãy cho tôi biết ý kiến của bạn.' ),
( N'連絡',     N'れんらく',       N'Liên lạc / Thông báo',      N'danh từ / động từ', 'N3', N'Giao tiếp', N'後で連絡します。', N'Tôi sẽ liên lạc sau.' ),
( N'相談',     N'そうだん',       N'Tư vấn / Bàn bạc',          N'danh từ / động từ', 'N3', N'Giao tiếp', N'先生に相談しました。', N'Tôi đã bàn bạc với giáo viên.' ),
-- N2
( N'研究',     N'けんきゅう',     N'Nghiên cứu',                 N'danh từ / động từ', 'N2', N'Học thuật', N'大学院で研究をしています。', N'Tôi đang nghiên cứu ở cao học.' ),
( N'分析',     N'ぶんせき',       N'Phân tích',                  N'danh từ / động từ', 'N2', N'Học thuật', N'データを分析しました。', N'Tôi đã phân tích dữ liệu.' ),
( N'論文',     N'ろんぶん',       N'Luận văn / Bài báo khoa học', N'danh từ',          'N2', N'Học thuật', N'論文を書き終えました。', N'Tôi đã viết xong luận văn.' ),
( N'仮説',     N'かせつ',         N'Giả thuyết',                 N'danh từ',           'N2', N'Học thuật', N'仮説を立てて実験しました。', N'Tôi đặt ra giả thuyết rồi thực nghiệm.' ),
( N'経済',     N'けいざい',       N'Kinh tế',                    N'danh từ',           'N2', N'Kinh tế', N'世界経済が変化しています。', N'Kinh tế thế giới đang thay đổi.' ),
( N'企業',     N'きぎょう',       N'Doanh nghiệp / Công ty',    N'danh từ',           'N2', N'Kinh tế', N'大手企業に就職しました。', N'Tôi đã vào làm ở công ty lớn.' ),
( N'投資',     N'とうし',         N'Đầu tư',                     N'danh từ / động từ', 'N2', N'Kinh tế', N'株に投資しています。', N'Tôi đang đầu tư vào cổ phiếu.' ),
( N'利益',     N'りえき',         N'Lợi nhuận / Lợi ích',       N'danh từ',           'N2', N'Kinh tế', N'今年は利益が増えました。', N'Năm nay lợi nhuận tăng.' ),
( N'法律',     N'ほうりつ',       N'Luật pháp',                  N'danh từ',           'N2', N'Pháp luật', N'法律を守ることが大切です。', N'Tuân thủ luật pháp là điều quan trọng.' ),
( N'権利',     N'けんり',         N'Quyền lợi / Quyền hạn',     N'danh từ',           'N2', N'Pháp luật', N'すべての人に権利があります。', N'Mọi người đều có quyền.' ),
( N'義務',     N'ぎむ',           N'Nghĩa vụ / Bổn phận',       N'danh từ',           'N2', N'Pháp luật', N'税金を払う義務があります。', N'Chúng ta có nghĩa vụ nộp thuế.' ),
( N'影響',     N'えいきょう',     N'Ảnh hưởng / Tác động',      N'danh từ / động từ', 'N2', N'Tâm lý', N'ストレスは体に影響を与えます。', N'Stress ảnh hưởng đến cơ thể.' ),
( N'判断',     N'はんだん',       N'Phán đoán / Quyết định',    N'danh từ / động từ', 'N2', N'Tâm lý', N'正しい判断をしてください。', N'Hãy đưa ra quyết định đúng đắn.' ),
-- N1
( N'概念',     N'がいねん',       N'Khái niệm',                  N'danh từ',           'N1', N'Triết học', N'抽象的な概念を理解するのは難しい。', N'Khó hiểu các khái niệm trừu tượng.' ),
( N'哲学',     N'てつがく',       N'Triết học',                  N'danh từ',           'N1', N'Triết học', N'西洋哲学を専攻しています。', N'Tôi chuyên ngành triết học phương Tây.' ),
( N'倫理',     N'りんり',         N'Đạo đức / Luân lý',         N'danh từ',           'N1', N'Triết học', N'医療倫理は重要な問題です。', N'Y đức là vấn đề quan trọng.' ),
( N'普遍',     N'ふへん',         N'Phổ quát / Phổ biến toàn cầu', N'danh từ / tính từ đuôi な', 'N1', N'Học thuật', N'普遍的な価値観について考える。', N'Suy nghĩ về các giá trị phổ quát.' ),
( N'矛盾',     N'むじゅん',       N'Mâu thuẫn',                  N'danh từ / động từ', 'N1', N'Học thuật', N'その意見には矛盾があります。', N'Ý kiến đó có mâu thuẫn.' ),
( N'抽象',     N'ちゅうしょう',   N'Trừu tượng',                 N'danh từ / tính từ đuôi な', 'N1', N'Học thuật', N'抽象的な思考が求められます。', N'Cần có tư duy trừu tượng.' ),
( N'随筆',     N'ずいひつ',       N'Tùy bút / Bài văn thử nghiệm', N'danh từ',        'N1', N'Văn học', N'近代文学の随筆を研究しています。', N'Tôi nghiên cứu tùy bút văn học hiện đại.' ),
( N'比喩',     N'ひゆ',           N'Ẩn dụ / So sánh',           N'danh từ',           'N1', N'Văn học', N'詩には多くの比喩が使われます。', N'Trong thơ có nhiều ẩn dụ.' ),
( N'革新',     N'かくしん',       N'Cải cách / Đổi mới',         N'danh từ / động từ', 'N1', N'Công nghệ', N'技術革新が社会を変えます。', N'Đổi mới công nghệ thay đổi xã hội.' ),
( N'持続可能', N'じぞくかのう',   N'Bền vững',                   N'tính từ đuôi な',  'N1', N'Môi trường', N'持続可能な発展が求められます。', N'Sự phát triển bền vững là điều cần thiết.' ),
( N'脆弱',     N'ぜいじゃく',     N'Dễ bị tổn thương / Yếu ớt', N'tính từ đuôi な',  'N1', N'Học thuật', N'システムの脆弱性を修正する。', N'Sửa lỗ hổng bảo mật của hệ thống.' ),
( N'縮小',     N'しゅくしょう',   N'Thu nhỏ / Rút gọn',         N'danh từ / động từ', 'N1', N'Học thuật', N'規模を縮小することにしました。', N'Chúng tôi quyết định thu nhỏ quy mô.' )
) AS v(word, furigana, meaning, word_type, jlpt_level, topic_vi, example_jp, example_vi);
GO


/* ============================================================
   XI. NGỮ PHÁP N5→N1 (gộp từ V7)
   ============================================================ */

DECLARE @staff_id11   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id11 BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now11         DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM grammar_points WHERE structure = N'〜は〜です')
INSERT INTO grammar_points (structure, formula, meaning, usage_explanation, jlpt_level,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
-- N5
( N'〜は〜です',      N'N は N/Adj です', N'[Chủ thể] là [danh từ/tính từ]',
  N'Cấu trúc câu cơ bản dùng để định nghĩa hoặc miêu tả chủ thể.',
  'N5', N'私は学生です。', N'Tôi là học sinh.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜が好きです',    N'N が好き（きら）いです', N'Thích / Ghét cái gì',
  N'Dùng để diễn đạt sở thích hoặc không thích của bản thân. が là trợ từ chỉ đối tượng.',
  'N5', N'日本語が好きです。', N'Tôi thích tiếng Nhật.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜たい',          N'V(ます形) + たい', N'Muốn làm gì (nguyện vọng của bản thân)',
  N'Thêm たい vào sau dạng ます (bỏ ます) để diễn đạt mong muốn. Chỉ dùng cho ngôi thứ nhất.',
  'N5', N'日本に行きたいです。', N'Tôi muốn đi Nhật Bản.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜ている',        N'V(て形) + いる', N'Đang làm / Trạng thái hiện tại',
  N'Diễn đạt hành động đang xảy ra (tiến diễn) hoặc trạng thái kết quả của hành động trước đó.',
  'N5', N'今、勉強しています。', N'Hiện tại tôi đang học.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜てください',    N'V(て形) + ください', N'Hãy làm gì (yêu cầu lịch sự)',
  N'Dùng để yêu cầu hoặc hướng dẫn ai đó thực hiện hành động một cách lịch sự.',
  'N5', N'ゆっくり話してください。', N'Hãy nói chậm thôi.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜ませんか',      N'V(ます形) + ませんか', N'Mời cùng làm gì / Rủ rê',
  N'Dùng để mời hoặc rủ ai đó cùng làm điều gì. Nhẹ nhàng, thân thiện hơn so với 〜ましょう。',
  'N5', N'一緒に映画を見ませんか。', N'Mình cùng xem phim không?', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
-- N4
( N'〜てしまう',      N'V(て形) + しまう', N'Lỡ làm gì / Đã hoàn toàn làm xong (đôi khi tiếc nuối)',
  N'Diễn đạt hành động đã hoàn thành hoàn toàn, thường kèm sắc thái tiếc nuối hoặc ngoài ý muốn.',
  'N4', N'宿題を忘れてしまいました。', N'Tôi đã lỡ quên bài tập về nhà rồi.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜ておく',        N'V(て形) + おく', N'Làm trước để chuẩn bị',
  N'Diễn đạt hành động thực hiện trước để chuẩn bị cho tương lai.',
  'N4', N'旅行の前にホテルを予約しておきます。', N'Trước khi đi du lịch tôi đặt khách sạn trước.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜ばよかった',    N'V(ば形) + よかった', N'Giá mà đã làm... thì tốt hơn (hối tiếc)',
  N'Diễn đạt sự hối tiếc về điều gì đó không xảy ra hoặc đã xảy ra trong quá khứ.',
  'N4', N'もっと早く起きればよかった。', N'Giá mà tôi dậy sớm hơn thì tốt.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜そうだ',        N'V/Adj(語幹) + そうだ', N'Có vẻ như... / Trông có vẻ...',
  N'Diễn đạt phán đoán dựa trên quan sát trực tiếp. Khác với 〜そうだ(truyền đạt thông tin nghe được).',
  'N4', N'雨が降りそうです。', N'Trời có vẻ sắp mưa.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜ようにする',    N'V(辞書形/ない形) + ようにする', N'Cố gắng làm sao để... / Đặt mục tiêu',
  N'Diễn đạt nỗ lực để đạt được hoặc duy trì một thói quen hay trạng thái.',
  'N4', N'毎日野菜を食べるようにしています。', N'Tôi cố gắng ăn rau mỗi ngày.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜てみる',        N'V(て形) + みる', N'Thử làm xem',
  N'Diễn đạt việc thực hiện thử một hành động để xem kết quả thế nào.',
  'N4', N'この料理を食べてみました。', N'Tôi đã thử ăn món này.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
-- N3
( N'〜ために',        N'N の / V(辞書形) + ために', N'Để / Vì mục đích...',
  N'Diễn đạt mục đích của hành động. Chủ ngữ câu trước và sau thường giống nhau.',
  'N3', N'日本語を学ぶために、毎日練習します。', N'Để học tiếng Nhật, tôi luyện tập mỗi ngày.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜によって',      N'N + によって', N'Tùy theo / Do... / Bằng phương tiện...',
  N'Diễn đạt phương tiện, nguyên nhân, hoặc sự khác biệt tùy theo đối tượng.',
  'N3', N'国によって文化が違います。', N'Văn hóa khác nhau tùy theo quốc gia.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜に対して',      N'N + に対して', N'Đối với / Hướng tới',
  N'Diễn đạt đối tượng mà hành động hay thái độ hướng đến.',
  'N3', N'学生に対して厳しくしてはいけません。', N'Không nên khắt khe với học sinh.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜わけだ',        N'普通形 + わけだ', N'Đó là lý do tại sao... / Có nghĩa là...',
  N'Diễn đạt kết luận logic dựa trên thông tin đã biết, hoặc giải thích nguyên nhân/lý do.',
  'N3', N'10年間日本に住んでいたから、日本語が上手なわけだ。', N'Anh ấy sống ở Nhật 10 năm, đó là lý do tiếng Nhật giỏi thế.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜くせに',        N'普通形 + くせに', N'Mặc dù... mà vẫn... (trách móc, phê phán)',
  N'Diễn đạt sự trách móc hoặc bất ngờ tiêu cực khi chủ ngữ làm điều trái với những gì người nói mong đợi.',
  'N3', N'知っているくせに、教えてくれなかった。', N'Dù biết mà vẫn không nói cho tôi.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
-- N2
( N'〜に違いない',    N'普通形 + に違いない', N'Chắc chắn là... (phán đoán mạnh)',
  N'Diễn đạt sự phán đoán mạnh của người nói, tin rằng điều đó chắc chắn đúng.',
  'N2', N'あんなに勉強したから、合格に違いない。', N'Học nhiều như vậy, chắc chắn sẽ đỗ.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜に過ぎない',    N'N / 普通形 + に過ぎない', N'Chỉ là... / Không hơn không kém',
  N'Diễn đạt rằng điều gì đó chỉ ở mức tối thiểu, không quan trọng hay ấn tượng như người khác nghĩ.',
  'N2', N'それは噂に過ぎない。', N'Đó chỉ là tin đồn mà thôi.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜をきっかけに',  N'N + をきっかけに(して)', N'Lấy... làm cơ hội / Nhờ... mà bắt đầu',
  N'Diễn đạt sự kiện hoặc tình huống trở thành bước ngoặt hoặc cơ hội khởi đầu cho hành động tiếp theo.',
  'N2', N'この出会いをきっかけに日本語を学び始めました。', N'Nhờ cuộc gặp gỡ này mà tôi bắt đầu học tiếng Nhật.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜に基づいて',    N'N + に基づいて / に基づく', N'Dựa trên / Căn cứ vào',
  N'Diễn đạt hành động hoặc quyết định dựa trên tiêu chuẩn, quy tắc, hoặc thông tin cụ thể.',
  'N2', N'データに基づいて判断します。', N'Chúng tôi quyết định dựa trên dữ liệu.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜かねない',      N'V(ます形) + かねない', N'Có thể (sẽ làm điều xấu) / Không thể không...',
  N'Diễn đạt khả năng xảy ra điều không mong muốn. Mang sắc thái cảnh báo, lo ngại.',
  'N2', N'このままでは失敗しかねない。', N'Cứ thế này có thể thất bại đó.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
-- N1
( N'〜ならいざ知らず',N'N + ならいざ知らず', N'Nếu là... thì có thể hiểu được, nhưng...',
  N'Diễn đạt rằng trường hợp A có thể chấp nhận được, nhưng trường hợp B thì không thể chấp nhận được.',
  'N1', N'子供ならいざ知らず、大人がそんなことをするのは非常識だ。', N'Trẻ con thì có thể, nhưng người lớn mà làm vậy là thiếu hiểu biết.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜をものともせず',N'N + をものともせず(に)', N'Bất chấp... / Không coi... là trở ngại',
  N'Diễn đạt việc không bị ảnh hưởng hoặc không coi trở ngại nào đó là vấn đề, tiếp tục hành động.',
  'N1', N'困難をものともせず、夢を追い続けた。', N'Bất chấp khó khăn, anh ấy vẫn tiếp tục theo đuổi ước mơ.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜とあって',      N'N / 普通形 + とあって', N'Vì là... nên / Do... mà',
  N'Diễn đạt lý do đặc biệt dẫn đến kết quả tự nhiên. Thường dùng trong văn viết.',
  'N1', N'大会前とあって、選手たちは緊張していた。', N'Vì là ngay trước giải đấu, các vận động viên rất căng thẳng.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜といえども',    N'N / 普通形 + といえども', N'Dù là... đi chăng nữa (nhượng bộ trang trọng)',
  N'Diễn đạt sự nhượng bộ theo phong cách trang trọng, thừa nhận điều kiện nhưng kết quả vẫn trái ngược.',
  'N1', N'プロといえども、常に努力が必要だ。', N'Dù là chuyên gia, cũng luôn cần nỗ lực.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 ),
( N'〜いかんによらず',N'N + のいかんによらず / いかんにかかわらず', N'Bất kể... như thế nào',
  N'Diễn đạt rằng dù điều kiện hay tình huống ra sao, kết quả/hành động vẫn không thay đổi. Rất trang trọng.',
  'N1', N'結果のいかんによらず、最善を尽くします。', N'Dù kết quả thế nào, tôi sẽ cố hết sức.', 'published', @staff_id11, @manager_id11, @now11, @now11, @now11 );
GO


/* ============================================================
   XII. BẢNG CHỮ KANA (gộp từ V19 + V24)
   ============================================================ */

IF NOT EXISTS (SELECT 1 FROM kana_characters)
BEGIN
    INSERT INTO kana_characters (character_value, romaji, kana_type, display_order) VALUES
    -- ===== HIRAGANA — gojūon =====
    (N'あ', N'a',   N'hiragana', 1),  (N'い', N'i',   N'hiragana', 2),  (N'う', N'u',   N'hiragana', 3),  (N'え', N'e',   N'hiragana', 4),  (N'お', N'o',   N'hiragana', 5),
    (N'か', N'ka',  N'hiragana', 6),  (N'き', N'ki',  N'hiragana', 7),  (N'く', N'ku',  N'hiragana', 8),  (N'け', N'ke',  N'hiragana', 9),  (N'こ', N'ko',  N'hiragana', 10),
    (N'さ', N'sa',  N'hiragana', 11), (N'し', N'shi', N'hiragana', 12), (N'す', N'su',  N'hiragana', 13), (N'せ', N'se',  N'hiragana', 14), (N'そ', N'so',  N'hiragana', 15),
    (N'た', N'ta',  N'hiragana', 16), (N'ち', N'chi', N'hiragana', 17), (N'つ', N'tsu', N'hiragana', 18), (N'て', N'te',  N'hiragana', 19), (N'と', N'to',  N'hiragana', 20),
    (N'な', N'na',  N'hiragana', 21), (N'に', N'ni',  N'hiragana', 22), (N'ぬ', N'nu',  N'hiragana', 23), (N'ね', N'ne',  N'hiragana', 24), (N'の', N'no',  N'hiragana', 25),
    (N'は', N'ha',  N'hiragana', 26), (N'ひ', N'hi',  N'hiragana', 27), (N'ふ', N'fu',  N'hiragana', 28), (N'へ', N'he',  N'hiragana', 29), (N'ほ', N'ho',  N'hiragana', 30),
    (N'ま', N'ma',  N'hiragana', 31), (N'み', N'mi',  N'hiragana', 32), (N'む', N'mu',  N'hiragana', 33), (N'め', N'me',  N'hiragana', 34), (N'も', N'mo',  N'hiragana', 35),
    (N'や', N'ya',  N'hiragana', 36), (N'ゆ', N'yu',  N'hiragana', 37), (N'よ', N'yo',  N'hiragana', 38),
    (N'ら', N'ra',  N'hiragana', 39), (N'り', N'ri',  N'hiragana', 40), (N'る', N'ru',  N'hiragana', 41), (N'れ', N're',  N'hiragana', 42), (N'ろ', N'ro',  N'hiragana', 43),
    (N'わ', N'wa',  N'hiragana', 44), (N'を', N'wo',  N'hiragana', 45),
    (N'ん', N'n',   N'hiragana', 46),
    -- ===== HIRAGANA — dakuten / handakuten =====
    (N'が', N'ga',  N'hiragana', 47), (N'ぎ', N'gi',  N'hiragana', 48), (N'ぐ', N'gu',  N'hiragana', 49), (N'げ', N'ge',  N'hiragana', 50), (N'ご', N'go',  N'hiragana', 51),
    (N'ざ', N'za',  N'hiragana', 52), (N'じ', N'ji',  N'hiragana', 53), (N'ず', N'zu',  N'hiragana', 54), (N'ぜ', N'ze',  N'hiragana', 55), (N'ぞ', N'zo',  N'hiragana', 56),
    (N'だ', N'da',  N'hiragana', 57), (N'ぢ', N'di',  N'hiragana', 58), (N'づ', N'du',  N'hiragana', 59), (N'で', N'de',  N'hiragana', 60), (N'ど', N'do',  N'hiragana', 61),
    (N'ば', N'ba',  N'hiragana', 62), (N'び', N'bi',  N'hiragana', 63), (N'ぶ', N'bu',  N'hiragana', 64), (N'べ', N'be',  N'hiragana', 65), (N'ぼ', N'bo',  N'hiragana', 66),
    (N'ぱ', N'pa',  N'hiragana', 67), (N'ぴ', N'pi',  N'hiragana', 68), (N'ぷ', N'pu',  N'hiragana', 69), (N'ぺ', N'pe',  N'hiragana', 70), (N'ぽ', N'po',  N'hiragana', 71),
    -- ===== KATAKANA — gojūon =====
    (N'ア', N'a',   N'katakana', 1),  (N'イ', N'i',   N'katakana', 2),  (N'ウ', N'u',   N'katakana', 3),  (N'エ', N'e',   N'katakana', 4),  (N'オ', N'o',   N'katakana', 5),
    (N'カ', N'ka',  N'katakana', 6),  (N'キ', N'ki',  N'katakana', 7),  (N'ク', N'ku',  N'katakana', 8),  (N'ケ', N'ke',  N'katakana', 9),  (N'コ', N'ko',  N'katakana', 10),
    (N'サ', N'sa',  N'katakana', 11), (N'シ', N'shi', N'katakana', 12), (N'ス', N'su',  N'katakana', 13), (N'セ', N'se',  N'katakana', 14), (N'ソ', N'so',  N'katakana', 15),
    (N'タ', N'ta',  N'katakana', 16), (N'チ', N'chi', N'katakana', 17), (N'ツ', N'tsu', N'katakana', 18), (N'テ', N'te',  N'katakana', 19), (N'ト', N'to',  N'katakana', 20),
    (N'ナ', N'na',  N'katakana', 21), (N'ニ', N'ni',  N'katakana', 22), (N'ヌ', N'nu',  N'katakana', 23), (N'ネ', N'ne',  N'katakana', 24), (N'ノ', N'no',  N'katakana', 25),
    (N'ハ', N'ha',  N'katakana', 26), (N'ヒ', N'hi',  N'katakana', 27), (N'フ', N'fu',  N'katakana', 28), (N'ヘ', N'he',  N'katakana', 29), (N'ホ', N'ho',  N'katakana', 30),
    (N'マ', N'ma',  N'katakana', 31), (N'ミ', N'mi',  N'katakana', 32), (N'ム', N'mu',  N'katakana', 33), (N'メ', N'me',  N'katakana', 34), (N'モ', N'mo',  N'katakana', 35),
    (N'ヤ', N'ya',  N'katakana', 36), (N'ユ', N'yu',  N'katakana', 37), (N'ヨ', N'yo',  N'katakana', 38),
    (N'ラ', N'ra',  N'katakana', 39), (N'リ', N'ri',  N'katakana', 40), (N'ル', N'ru',  N'katakana', 41), (N'レ', N're',  N'katakana', 42), (N'ロ', N'ro',  N'katakana', 43),
    (N'ワ', N'wa',  N'katakana', 44), (N'ヲ', N'wo',  N'katakana', 45),
    (N'ン', N'n',   N'katakana', 46),
    -- ===== KATAKANA — dakuten / handakuten =====
    (N'ガ', N'ga',  N'katakana', 47), (N'ギ', N'gi',  N'katakana', 48), (N'グ', N'gu',  N'katakana', 49), (N'ゲ', N'ge',  N'katakana', 50), (N'ゴ', N'go',  N'katakana', 51),
    (N'ザ', N'za',  N'katakana', 52), (N'ジ', N'ji',  N'katakana', 53), (N'ズ', N'zu',  N'katakana', 54), (N'ゼ', N'ze',  N'katakana', 55), (N'ゾ', N'zo',  N'katakana', 56),
    (N'ダ', N'da',  N'katakana', 57), (N'ヂ', N'di',  N'katakana', 58), (N'ヅ', N'du',  N'katakana', 59), (N'デ', N'de',  N'katakana', 60), (N'ド', N'do',  N'katakana', 61),
    (N'バ', N'ba',  N'katakana', 62), (N'ビ', N'bi',  N'katakana', 63), (N'ブ', N'bu',  N'katakana', 64), (N'ベ', N'be',  N'katakana', 65), (N'ボ', N'bo',  N'katakana', 66),
    (N'パ', N'pa',  N'katakana', 67), (N'ピ', N'pi',  N'katakana', 68), (N'プ', N'pu',  N'katakana', 69), (N'ペ', N'pe',  N'katakana', 70), (N'ポ', N'po',  N'katakana', 71);
END
GO

UPDATE kana_characters
SET audio_url = '/api/files/audio/kana/' + LOWER(romaji) + '.mp3'
WHERE romaji IS NOT NULL
  AND LTRIM(RTRIM(romaji)) <> ''
  AND (audio_url IS NULL OR LTRIM(RTRIM(audio_url)) = '');
GO

-- ぢ/づ (di/du) phát âm trùng じ/ず (ji/zu) → dùng chung file audio đã có.
UPDATE kana_characters SET audio_url = '/api/files/audio/kana/ji.mp3' WHERE romaji = 'di';
UPDATE kana_characters SET audio_url = '/api/files/audio/kana/zu.mp3' WHERE romaji = 'du';
GO


PRINT N'✅ V2 Mock Data đã được seed thành công.';
PRINT N'';
PRINT N'─────────────────────────────────────────────────';
PRINT N' ADMIN    : admin@sakuji.com    / Admin@123456';
PRINT N' MANAGER  : manager@sakuji.com  / Staff@123456';
PRINT N' STAFF    : staff@sakuji.com    / Staff@123456';
PRINT N' STUDENT1 : student1@sakuji.com / Student@123456';
PRINT N' STUDENT2 : student2@sakuji.com / Student@123456';
PRINT N'─────────────────────────────────────────────────';
GO
