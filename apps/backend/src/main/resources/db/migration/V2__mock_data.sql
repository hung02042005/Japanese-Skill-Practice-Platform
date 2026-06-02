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
   Lưu ý    : two_factor_enabled = 0 cho môi trường DEV (bật lại khi lên PROD)
   ============================================================================ */

USE JLPT_LearningDB;
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
        two_factor_enabled,
        login_attempts, created_at, updated_at
    ) VALUES (
        'admin@sakuji.com',
        '$2b$12$kJcYAwbtPEqtu8tZUWv4fetUeoydXgfwS4ckmsiHW3oLU3Hhtig/G',   -- Admin@123456
        N'Quản Trị Viên',
        'active',
        0,   -- tắt 2FA cho môi trường DEV
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
BEGIN
    INSERT INTO vocabulary (
        word, furigana, meaning, word_type, jlpt_level, topic,
        example_sentence_jp, example_sentence_vi,
        status, created_by, approved_by, published_at,
        created_at, updated_at
    ) VALUES
    ( N'食べる',   N'たべる',     N'Ăn',                     N'động từ nhóm 2', 'N5', N'Ẩm thực',
      N'私は毎朝ご飯を食べる。', N'Tôi ăn cơm mỗi buổi sáng.',
      'published', @staff_id, @manager_id, @now, @now, @now ),

    ( N'飲む',     N'のむ',       N'Uống',                   N'động từ nhóm 1', 'N5', N'Ẩm thực',
      N'水を飲む。',              N'Uống nước.',
      'published', @staff_id, @manager_id, @now, @now, @now ),

    ( N'学校',     N'がっこう',   N'Trường học',             N'danh từ',        'N5', N'Giáo dục',
      N'学校に行く。',            N'Đi đến trường.',
      'published', @staff_id, @manager_id, @now, @now, @now ),

    ( N'先生',     N'せんせい',   N'Giáo viên',              N'danh từ',        'N5', N'Giáo dục',
      N'先生はやさしい。',        N'Giáo viên thân thiện.',
      'published', @staff_id, @manager_id, @now, @now, @now ),

    ( N'友達',     N'ともだち',   N'Bạn bè',                 N'danh từ',        'N5', N'Xã hội',
      N'友達と遊ぶ。',            N'Chơi với bạn bè.',
      'published', @staff_id, @manager_id, @now, @now, @now ),

    ( N'きれい',   N'きれい',     N'Đẹp / Sạch sẽ',         N'tính từ đuôi な','N5', N'Mô tả',
      N'この花はきれいです。',    N'Bông hoa này rất đẹp.',
      'published', @staff_id, @manager_id, @now, @now, @now ),

    ( N'大きい',   N'おおきい',   N'To lớn',                 N'tính từ đuôi い','N5', N'Mô tả',
      N'大きい犬がいる。',        N'Có một con chó to.',
      'published', @staff_id, @manager_id, @now, @now, @now ),

    ( N'電話',     N'でんわ',     N'Điện thoại',             N'danh từ',        'N5', N'Công nghệ',
      N'電話をかける。',          N'Gọi điện thoại.',
      'published', @staff_id, @manager_id, @now, @now, @now );
END
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
