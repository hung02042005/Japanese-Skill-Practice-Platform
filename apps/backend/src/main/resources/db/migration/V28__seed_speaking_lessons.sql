/* ============================================================
   V28 — SEED bài luyện nói (Speaking / Shadowing) — UC-13
   Idempotent: chỉ chèn khi chưa tồn tại (khớp theo title).
   ============================================================ */

SET @manager_id = (SELECT staff_id FROM staff_users WHERE email = 'manager@sakuji.com' LIMIT 1);
SET @staff_id   = (SELECT staff_id FROM staff_users WHERE email = 'staff@sakuji.com'   LIMIT 1);
SET @now        = CURRENT_TIMESTAMP(6);

INSERT INTO lessons (
    lesson_type, title, jlpt_level, content_text, explanation,
    display_order, status, created_by, approved_by, published_at, created_at, updated_at)
SELECT 'speaking', 'Giới thiệu bản thân', 'N5',
       'はじめまして。わたしは田中です。よろしくお願いします。',
       'Đọc to, rõ từng âm tiết. Chú ý ngữ điệu xuống ở cuối câu.',
       1, 'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM lessons x WHERE x.title = 'Giới thiệu bản thân' AND x.lesson_type = 'speaking');

INSERT INTO lessons (
    lesson_type, title, jlpt_level, content_text, explanation,
    display_order, status, created_by, approved_by, published_at, created_at, updated_at)
SELECT 'speaking', 'Gọi món tại nhà hàng', 'N5',
       'すみません、これをください。おいくらですか。',
       'Luyện cách nói lịch sự khi yêu cầu. Nhấn nhẹ ở 「ください」.',
       2, 'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM lessons x WHERE x.title = 'Gọi món tại nhà hàng' AND x.lesson_type = 'speaking');

INSERT INTO lessons (
    lesson_type, title, jlpt_level, content_text, explanation,
    display_order, status, created_by, approved_by, published_at, created_at, updated_at)
SELECT 'speaking', 'Hỏi đường', 'N4',
       'すみません、駅はどこですか。まっすぐ行って、右に曲がってください。',
       'Câu dài hơn — giữ nhịp đều, ngắt hơi sau dấu phẩy.',
       1, 'published', @staff_id, @manager_id, @now, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM lessons x WHERE x.title = 'Hỏi đường' AND x.lesson_type = 'speaking');
