/* ============================================================================
   V7 — SEED: TỪ VỰNG + NGỮ PHÁP N5→N1
   ----------------------------------------------------------------------------
   Mục đích : Thêm dữ liệu từ vựng và ngữ pháp để sử dụng local (dev/test)
   Phạm vi  : ~100 từ vựng + 40 ngữ pháp, trải đều N5→N1, status = published
   DBMS     : Microsoft SQL Server 2019+
   ============================================================================ */

USE JLPT_LearningDB;
GO

/* ============================================================
   KHAI BÁO BIẾN DÙNG CHUNG
   ============================================================ */
DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

/* ============================================================
   I. TỪ VỰNG N5
   ============================================================ */

IF NOT EXISTS (SELECT 1 FROM vocabulary WHERE word = N'母')
INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
-- Gia đình
( N'母',       N'はは',           N'Mẹ (cách nói khiêm tốn)',    N'danh từ',           'N5', N'Gia đình',
  N'母は料理が上手です。',          N'Mẹ tôi nấu ăn rất giỏi.',        'published', @staff_id, @manager_id, @now, @now, @now ),
( N'父',       N'ちち',           N'Bố (cách nói khiêm tốn)',    N'danh từ',           'N5', N'Gia đình',
  N'父は会社員です。',              N'Bố tôi là nhân viên công ty.',    'published', @staff_id, @manager_id, @now, @now, @now ),
( N'兄',       N'あに',           N'Anh trai (khiêm tốn)',       N'danh từ',           'N5', N'Gia đình',
  N'兄は大学生です。',              N'Anh trai tôi là sinh viên đại học.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'姉',       N'あね',           N'Chị gái (khiêm tốn)',        N'danh từ',           'N5', N'Gia đình',
  N'姉は先生です。',                N'Chị tôi là giáo viên.',           'published', @staff_id, @manager_id, @now, @now, @now ),
( N'家族',     N'かぞく',         N'Gia đình',                   N'danh từ',           'N5', N'Gia đình',
  N'私の家族は四人です。',          N'Gia đình tôi có bốn người.',      'published', @staff_id, @manager_id, @now, @now, @now ),

-- Ẩm thực
( N'ご飯',     N'ごはん',         N'Cơm / Bữa ăn',              N'danh từ',           'N5', N'Ẩm thực',
  N'毎日ご飯を食べます。',          N'Mỗi ngày tôi ăn cơm.',           'published', @staff_id, @manager_id, @now, @now, @now ),
( N'お茶',     N'おちゃ',         N'Trà',                        N'danh từ',           'N5', N'Ẩm thực',
  N'お茶を飲みますか。',            N'Bạn có uống trà không?',          'published', @staff_id, @manager_id, @now, @now, @now ),
( N'魚',       N'さかな',         N'Cá',                         N'danh từ',           'N5', N'Ẩm thực',
  N'魚が好きです。',                N'Tôi thích cá.',                   'published', @staff_id, @manager_id, @now, @now, @now ),
( N'肉',       N'にく',           N'Thịt',                       N'danh từ',           'N5', N'Ẩm thực',
  N'肉を買いました。',              N'Tôi đã mua thịt.',                'published', @staff_id, @manager_id, @now, @now, @now ),
( N'野菜',     N'やさい',         N'Rau củ',                     N'danh từ',           'N5', N'Ẩm thực',
  N'野菜をたくさん食べてください。',N'Hãy ăn nhiều rau nhé.',           'published', @staff_id, @manager_id, @now, @now, @now ),

-- Giao thông
( N'駅',       N'えき',           N'Ga tàu',                     N'danh từ',           'N5', N'Giao thông',
  N'駅まで歩きます。',              N'Tôi đi bộ đến ga.',               'published', @staff_id, @manager_id, @now, @now, @now ),
( N'バス',     N'バス',           N'Xe buýt',                    N'danh từ',           'N5', N'Giao thông',
  N'バスで学校に行きます。',        N'Tôi đi học bằng xe buýt.',        'published', @staff_id, @manager_id, @now, @now, @now ),
( N'飛行機',   N'ひこうき',       N'Máy bay',                    N'danh từ',           'N5', N'Giao thông',
  N'飛行機に乗りました。',          N'Tôi đã đi máy bay.',              'published', @staff_id, @manager_id, @now, @now, @now ),
( N'自転車',   N'じてんしゃ',     N'Xe đạp',                     N'danh từ',           'N5', N'Giao thông',
  N'自転車で通学します。',          N'Tôi đi học bằng xe đạp.',         'published', @staff_id, @manager_id, @now, @now, @now ),

-- Thời gian
( N'今日',     N'きょう',         N'Hôm nay',                    N'danh từ',           'N5', N'Thời gian',
  N'今日は月曜日です。',            N'Hôm nay là thứ Hai.',             'published', @staff_id, @manager_id, @now, @now, @now ),
( N'明日',     N'あした',         N'Ngày mai',                   N'danh từ',           'N5', N'Thời gian',
  N'明日は休みです。',              N'Ngày mai tôi được nghỉ.',         'published', @staff_id, @manager_id, @now, @now, @now ),
( N'昨日',     N'きのう',         N'Hôm qua',                    N'danh từ',           'N5', N'Thời gian',
  N'昨日は雨でした。',              N'Hôm qua trời mưa.',               'published', @staff_id, @manager_id, @now, @now, @now ),

-- Địa điểm
( N'病院',     N'びょういん',     N'Bệnh viện',                  N'danh từ',           'N5', N'Địa điểm',
  N'病院に行きます。',              N'Tôi đến bệnh viện.',              'published', @staff_id, @manager_id, @now, @now, @now ),
( N'図書館',   N'としょかん',     N'Thư viện',                   N'danh từ',           'N5', N'Địa điểm',
  N'図書館で本を読みます。',        N'Tôi đọc sách ở thư viện.',        'published', @staff_id, @manager_id, @now, @now, @now ),
( N'銀行',     N'ぎんこう',       N'Ngân hàng',                  N'danh từ',           'N5', N'Địa điểm',
  N'銀行でお金をおろします。',      N'Tôi rút tiền ở ngân hàng.',       'published', @staff_id, @manager_id, @now, @now, @now ),

-- Tính từ / trạng thái
( N'新しい',   N'あたらしい',     N'Mới',                        N'tính từ đuôi い',  'N5', N'Mô tả',
  N'新しい本を買いました。',        N'Tôi đã mua cuốn sách mới.',       'published', @staff_id, @manager_id, @now, @now, @now ),
( N'古い',     N'ふるい',         N'Cũ / Lâu đời',              N'tính từ đuôi い',  'N5', N'Mô tả',
  N'古い映画が好きです。',          N'Tôi thích phim cũ.',              'published', @staff_id, @manager_id, @now, @now, @now ),
( N'暑い',     N'あつい',         N'Nóng',                       N'tính từ đuôi い',  'N5', N'Thời tiết',
  N'今日はとても暑いです。',        N'Hôm nay rất nóng.',               'published', @staff_id, @manager_id, @now, @now, @now ),
( N'寒い',     N'さむい',         N'Lạnh',                       N'tính từ đuôi い',  'N5', N'Thời tiết',
  N'冬は寒いです。',                N'Mùa đông rất lạnh.',              'published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   II. TỪ VỰNG N4
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM vocabulary WHERE word = N'旅行' AND jlpt_level = 'N4')
INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
-- Du lịch & Hoạt động
( N'旅行',     N'りょこう',       N'Du lịch',                    N'danh từ / động từ', 'N4', N'Du lịch',
  N'来月、京都へ旅行します。',      N'Tháng tới tôi đi du lịch Kyoto.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'予約',     N'よやく',         N'Đặt trước / Đặt chỗ',       N'danh từ / động từ', 'N4', N'Du lịch',
  N'ホテルを予約しました。',        N'Tôi đã đặt khách sạn.',           'published', @staff_id, @manager_id, @now, @now, @now ),
( N'地図',     N'ちず',           N'Bản đồ',                     N'danh từ',           'N4', N'Du lịch',
  N'地図を見ながら歩きました。',    N'Tôi đi bộ trong khi nhìn bản đồ.','published', @staff_id, @manager_id, @now, @now, @now ),

-- Cảm xúc
( N'嬉しい',   N'うれしい',       N'Vui mừng / Sung sướng',     N'tính từ đuôi い',  'N4', N'Cảm xúc',
  N'合格して嬉しいです。',          N'Tôi vui vì đã đỗ.',               'published', @staff_id, @manager_id, @now, @now, @now ),
( N'悲しい',   N'かなしい',       N'Buồn',                       N'tính từ đuôi い',  'N4', N'Cảm xúc',
  N'別れが悲しいです。',            N'Tôi buồn khi chia tay.',          'published', @staff_id, @manager_id, @now, @now, @now ),
( N'心配',     N'しんぱい',       N'Lo lắng',                    N'danh từ / động từ', 'N4', N'Cảm xúc',
  N'試験が心配です。',              N'Tôi lo lắng về kỳ thi.',          'published', @staff_id, @manager_id, @now, @now, @now ),

-- Mua sắm
( N'値段',     N'ねだん',         N'Giá cả',                     N'danh từ',           'N4', N'Mua sắm',
  N'この服の値段はいくらですか。',  N'Bộ quần áo này giá bao nhiêu?',   'published', @staff_id, @manager_id, @now, @now, @now ),
( N'お釣り',   N'おつり',         N'Tiền thừa / Tiền trả lại',  N'danh từ',           'N4', N'Mua sắm',
  N'お釣りをもらいました。',        N'Tôi đã nhận lại tiền thừa.',      'published', @staff_id, @manager_id, @now, @now, @now ),
( N'割引',     N'わりびき',       N'Giảm giá / Chiết khấu',     N'danh từ / động từ', 'N4', N'Mua sắm',
  N'10%割引のセールがあります。',   N'Có đợt sale giảm 10%.',           'published', @staff_id, @manager_id, @now, @now, @now ),

-- Sức khỏe
( N'熱',       N'ねつ',           N'Sốt / Nhiệt độ',             N'danh từ',           'N4', N'Sức khỏe',
  N'熱が出ました。',                N'Tôi bị sốt.',                     'published', @staff_id, @manager_id, @now, @now, @now ),
( N'薬',       N'くすり',         N'Thuốc',                      N'danh từ',           'N4', N'Sức khỏe',
  N'薬を飲んでください。',          N'Hãy uống thuốc đi.',              'published', @staff_id, @manager_id, @now, @now, @now ),
( N'休む',     N'やすむ',         N'Nghỉ ngơi / Nghỉ học/làm',  N'động từ nhóm 1',    'N4', N'Sức khỏe',
  N'今日は体調が悪くて休みました。',N'Hôm nay tôi nghỉ vì không khỏe.','published', @staff_id, @manager_id, @now, @now, @now ),

-- Công việc / Trường học
( N'試験',     N'しけん',         N'Kỳ thi / Kiểm tra',         N'danh từ',           'N4', N'Giáo dục',
  N'来週試験があります。',          N'Tuần tới có kỳ thi.',             'published', @staff_id, @manager_id, @now, @now, @now ),
( N'合格',     N'ごうかく',       N'Đỗ / Đạt yêu cầu',          N'danh từ / động từ', 'N4', N'Giáo dục',
  N'JLPT N4に合格しました！',       N'Tôi đã đỗ JLPT N4!',             'published', @staff_id, @manager_id, @now, @now, @now ),
( N'練習',     N'れんしゅう',     N'Luyện tập',                  N'danh từ / động từ', 'N4', N'Giáo dục',
  N'毎日日本語を練習します。',      N'Tôi luyện tiếng Nhật mỗi ngày.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'単語',     N'たんご',         N'Từ vựng / Từ đơn',           N'danh từ',           'N4', N'Giáo dục',
  N'毎日新しい単語を覚えます。',    N'Mỗi ngày tôi học từ vựng mới.',  'published', @staff_id, @manager_id, @now, @now, @now ),

-- Gia đình (mở rộng)
( N'結婚',     N'けっこん',       N'Kết hôn',                    N'danh từ / động từ', 'N4', N'Gia đình',
  N'来年結婚する予定です。',        N'Tôi dự định kết hôn năm tới.',   'published', @staff_id, @manager_id, @now, @now, @now ),
( N'子供',     N'こども',         N'Trẻ em / Con cái',           N'danh từ',           'N4', N'Gia đình',
  N'子供が二人います。',            N'Tôi có hai đứa con.',             'published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   III. TỪ VỰNG N3
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM vocabulary WHERE word = N'経験' AND jlpt_level = 'N3')
INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
-- Công việc
( N'経験',     N'けいけん',       N'Kinh nghiệm',                N'danh từ / động từ', 'N3', N'Công việc',
  N'海外での経験が役に立ちます。',  N'Kinh nghiệm làm việc ở nước ngoài rất hữu ích.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'仕事',     N'しごと',         N'Công việc / Việc làm',       N'danh từ',           'N3', N'Công việc',
  N'新しい仕事を探しています。',    N'Tôi đang tìm việc mới.',          'published', @staff_id, @manager_id, @now, @now, @now ),
( N'会議',     N'かいぎ',         N'Cuộc họp',                   N'danh từ',           'N3', N'Công việc',
  N'午後から会議があります。',      N'Chiều nay có cuộc họp.',          'published', @staff_id, @manager_id, @now, @now, @now ),
( N'締め切り', N'しめきり',       N'Hạn chót / Deadline',        N'danh từ',           'N3', N'Công việc',
  N'締め切りに間に合いました。',    N'Tôi đã kịp hạn chót.',           'published', @staff_id, @manager_id, @now, @now, @now ),
( N'給料',     N'きゅうりょう',   N'Lương / Tiền lương',         N'danh từ',           'N3', N'Công việc',
  N'今月の給料が入りました。',      N'Lương tháng này đã vào.',         'published', @staff_id, @manager_id, @now, @now, @now ),

-- Xã hội
( N'社会',     N'しゃかい',       N'Xã hội',                     N'danh từ',           'N3', N'Xã hội',
  N'現代社会では情報が大切です。',  N'Trong xã hội hiện đại, thông tin rất quan trọng.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'文化',     N'ぶんか',         N'Văn hóa',                    N'danh từ',           'N3', N'Xã hội',
  N'日本の文化を学んでいます。',    N'Tôi đang học văn hóa Nhật Bản.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'伝統',     N'でんとう',       N'Truyền thống',               N'danh từ',           'N3', N'Xã hội',
  N'日本の伝統を大切にしています。',N'Chúng tôi coi trọng truyền thống Nhật Bản.', 'published', @staff_id, @manager_id, @now, @now, @now ),

-- Thiên nhiên
( N'海',       N'うみ',           N'Biển / Đại dương',           N'danh từ',           'N3', N'Thiên nhiên',
  N'海で泳ぎました。',              N'Tôi đã bơi ở biển.',              'published', @staff_id, @manager_id, @now, @now, @now ),
( N'森',       N'もり',           N'Rừng',                       N'danh từ',           'N3', N'Thiên nhiên',
  N'森の中を散歩しました。',        N'Tôi đã tản bộ trong rừng.',       'published', @staff_id, @manager_id, @now, @now, @now ),
( N'環境',     N'かんきょう',     N'Môi trường',                 N'danh từ',           'N3', N'Thiên nhiên',
  N'環境を守ることが大切です。',    N'Bảo vệ môi trường rất quan trọng.', 'published', @staff_id, @manager_id, @now, @now, @now ),

-- Tư duy / Cảm giác
( N'努力',     N'どりょく',       N'Nỗ lực / Cố gắng',          N'danh từ / động từ', 'N3', N'Tư duy',
  N'努力すれば夢は叶います。',      N'Nếu cố gắng, ước mơ sẽ trở thành hiện thực.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'成功',     N'せいこう',       N'Thành công',                 N'danh từ / động từ', 'N3', N'Tư duy',
  N'プロジェクトが成功しました。',  N'Dự án đã thành công.',            'published', @staff_id, @manager_id, @now, @now, @now ),
( N'失敗',     N'しっぱい',       N'Thất bại',                   N'danh từ / động từ', 'N3', N'Tư duy',
  N'失敗から学ぶことが大切です。',  N'Học từ thất bại là điều quan trọng.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'意見',     N'いけん',         N'Ý kiến / Quan điểm',         N'danh từ',           'N3', N'Tư duy',
  N'あなたの意見を聞かせてください。',N'Hãy cho tôi biết ý kiến của bạn.', 'published', @staff_id, @manager_id, @now, @now, @now ),

-- Giao tiếp
( N'連絡',     N'れんらく',       N'Liên lạc / Thông báo',      N'danh từ / động từ', 'N3', N'Giao tiếp',
  N'後で連絡します。',              N'Tôi sẽ liên lạc sau.',            'published', @staff_id, @manager_id, @now, @now, @now ),
( N'相談',     N'そうだん',       N'Tư vấn / Bàn bạc',          N'danh từ / động từ', 'N3', N'Giao tiếp',
  N'先生に相談しました。',          N'Tôi đã bàn bạc với giáo viên.',   'published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   IV. TỪ VỰNG N2
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM vocabulary WHERE word = N'研究' AND jlpt_level = 'N2')
INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
-- Học thuật
( N'研究',     N'けんきゅう',     N'Nghiên cứu',                 N'danh từ / động từ', 'N2', N'Học thuật',
  N'大学院で研究をしています。',    N'Tôi đang nghiên cứu ở cao học.',  'published', @staff_id, @manager_id, @now, @now, @now ),
( N'分析',     N'ぶんせき',       N'Phân tích',                  N'danh từ / động từ', 'N2', N'Học thuật',
  N'データを分析しました。',        N'Tôi đã phân tích dữ liệu.',       'published', @staff_id, @manager_id, @now, @now, @now ),
( N'論文',     N'ろんぶん',       N'Luận văn / Bài báo khoa học', N'danh từ',          'N2', N'Học thuật',
  N'論文を書き終えました。',        N'Tôi đã viết xong luận văn.',      'published', @staff_id, @manager_id, @now, @now, @now ),
( N'仮説',     N'かせつ',         N'Giả thuyết',                 N'danh từ',           'N2', N'Học thuật',
  N'仮説を立てて実験しました。',    N'Tôi đặt ra giả thuyết rồi thực nghiệm.', 'published', @staff_id, @manager_id, @now, @now, @now ),

-- Kinh tế / Kinh doanh
( N'経済',     N'けいざい',       N'Kinh tế',                    N'danh từ',           'N2', N'Kinh tế',
  N'世界経済が変化しています。',    N'Kinh tế thế giới đang thay đổi.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'企業',     N'きぎょう',       N'Doanh nghiệp / Công ty',    N'danh từ',           'N2', N'Kinh tế',
  N'大手企業に就職しました。',      N'Tôi đã vào làm ở công ty lớn.',  'published', @staff_id, @manager_id, @now, @now, @now ),
( N'投資',     N'とうし',         N'Đầu tư',                     N'danh từ / động từ', 'N2', N'Kinh tế',
  N'株に投資しています。',          N'Tôi đang đầu tư vào cổ phiếu.',  'published', @staff_id, @manager_id, @now, @now, @now ),
( N'利益',     N'りえき',         N'Lợi nhuận / Lợi ích',       N'danh từ',           'N2', N'Kinh tế',
  N'今年は利益が増えました。',      N'Năm nay lợi nhuận tăng.',         'published', @staff_id, @manager_id, @now, @now, @now ),

-- Pháp luật / Hành chính
( N'法律',     N'ほうりつ',       N'Luật pháp',                  N'danh từ',           'N2', N'Pháp luật',
  N'法律を守ることが大切です。',    N'Tuân thủ luật pháp là điều quan trọng.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'権利',     N'けんり',         N'Quyền lợi / Quyền hạn',     N'danh từ',           'N2', N'Pháp luật',
  N'すべての人に権利があります。',  N'Mọi người đều có quyền.',         'published', @staff_id, @manager_id, @now, @now, @now ),
( N'義務',     N'ぎむ',           N'Nghĩa vụ / Bổn phận',       N'danh từ',           'N2', N'Pháp luật',
  N'税金を払う義務があります。',    N'Chúng ta có nghĩa vụ nộp thuế.', 'published', @staff_id, @manager_id, @now, @now, @now ),

-- Tâm lý / Con người
( N'影響',     N'えいきょう',     N'Ảnh hưởng / Tác động',      N'danh từ / động từ', 'N2', N'Tâm lý',
  N'ストレスは体に影響を与えます。',N'Stress ảnh hưởng đến cơ thể.',   'published', @staff_id, @manager_id, @now, @now, @now ),
( N'判断',     N'はんだん',       N'Phán đoán / Quyết định',    N'danh từ / động từ', 'N2', N'Tâm lý',
  N'正しい判断をしてください。',    N'Hãy đưa ra quyết định đúng đắn.','published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   V. TỪ VỰNG N1
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM vocabulary WHERE word = N'概念' AND jlpt_level = 'N1')
INSERT INTO vocabulary (word, furigana, meaning, word_type, jlpt_level, topic,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
-- Triết học / Tư tưởng
( N'概念',     N'がいねん',       N'Khái niệm',                  N'danh từ',           'N1', N'Triết học',
  N'抽象的な概念を理解するのは難しい。', N'Khó hiểu các khái niệm trừu tượng.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'哲学',     N'てつがく',       N'Triết học',                  N'danh từ',           'N1', N'Triết học',
  N'西洋哲学を専攻しています。',    N'Tôi chuyên ngành triết học phương Tây.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'倫理',     N'りんり',         N'Đạo đức / Luân lý',         N'danh từ',           'N1', N'Triết học',
  N'医療倫理は重要な問題です。',    N'Y đức là vấn đề quan trọng.',    'published', @staff_id, @manager_id, @now, @now, @now ),

-- Chuyên ngành học thuật
( N'普遍',     N'ふへん',         N'Phổ quát / Phổ biến toàn cầu', N'danh từ / tính từ đuôi な', 'N1', N'Học thuật',
  N'普遍的な価値観について考える。',N'Suy nghĩ về các giá trị phổ quát.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'矛盾',     N'むじゅん',       N'Mâu thuẫn',                  N'danh từ / động từ', 'N1', N'Học thuật',
  N'その意見には矛盾があります。',  N'Ý kiến đó có mâu thuẫn.',         'published', @staff_id, @manager_id, @now, @now, @now ),
( N'抽象',     N'ちゅうしょう',   N'Trừu tượng',                 N'danh từ / tính từ đuôi な', 'N1', N'Học thuật',
  N'抽象的な思考が求められます。',  N'Cần có tư duy trừu tượng.',       'published', @staff_id, @manager_id, @now, @now, @now ),

-- Văn học / Nghệ thuật
( N'随筆',     N'ずいひつ',       N'Tùy bút / Bài văn thử nghiệm', N'danh từ',        'N1', N'Văn học',
  N'近代文学の随筆を研究しています。', N'Tôi nghiên cứu tùy bút văn học hiện đại.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'比喩',     N'ひゆ',           N'Ẩn dụ / So sánh',           N'danh từ',           'N1', N'Văn học',
  N'詩には多くの比喩が使われます。',N'Trong thơ có nhiều ẩn dụ.',       'published', @staff_id, @manager_id, @now, @now, @now ),

-- Khoa học / Công nghệ
( N'革新',     N'かくしん',       N'Cải cách / Đổi mới',         N'danh từ / động từ', 'N1', N'Công nghệ',
  N'技術革新が社会を変えます。',    N'Đổi mới công nghệ thay đổi xã hội.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'持続可能', N'じぞくかのう',   N'Bền vững',                   N'tính từ đuôi な',  'N1', N'Môi trường',
  N'持続可能な発展が求められます。',N'Sự phát triển bền vững là điều cần thiết.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'脆弱',     N'ぜいじゃく',     N'Dễ bị tổn thương / Yếu ớt', N'tính từ đuôi な',  'N1', N'Học thuật',
  N'システムの脆弱性を修正する。',  N'Sửa lỗ hổng bảo mật của hệ thống.', 'published', @staff_id, @manager_id, @now, @now, @now ),
( N'縮小',     N'しゅくしょう',   N'Thu nhỏ / Rút gọn',         N'danh từ / động từ', 'N1', N'Học thuật',
  N'規模を縮小することにしました。',N'Chúng tôi quyết định thu nhỏ quy mô.', 'published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   VI. NGỮ PHÁP N5
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM grammar_points WHERE structure = N'〜は〜です')
INSERT INTO grammar_points (structure, formula, meaning, usage_explanation, jlpt_level,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
( N'〜は〜です',      N'N は N/Adj です',
  N'[Chủ thể] là [danh từ/tính từ]',
  N'Cấu trúc câu cơ bản dùng để định nghĩa hoặc miêu tả chủ thể.',
  'N5', N'私は学生です。', N'Tôi là học sinh.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜が好きです',    N'N が好き（きら）いです',
  N'Thích / Ghét cái gì',
  N'Dùng để diễn đạt sở thích hoặc không thích của bản thân. が là trợ từ chỉ đối tượng.',
  'N5', N'日本語が好きです。', N'Tôi thích tiếng Nhật.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜たい',          N'V(ます形) + たい',
  N'Muốn làm gì (nguyện vọng của bản thân)',
  N'Thêm たい vào sau dạng ます (bỏ ます) để diễn đạt mong muốn. Chỉ dùng cho ngôi thứ nhất.',
  'N5', N'日本に行きたいです。', N'Tôi muốn đi Nhật Bản.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜ている',        N'V(て形) + いる',
  N'Đang làm / Trạng thái hiện tại',
  N'Diễn đạt hành động đang xảy ra (tiến diễn) hoặc trạng thái kết quả của hành động trước đó.',
  'N5', N'今、勉強しています。', N'Hiện tại tôi đang học.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜てください',    N'V(て形) + ください',
  N'Hãy làm gì (yêu cầu lịch sự)',
  N'Dùng để yêu cầu hoặc hướng dẫn ai đó thực hiện hành động một cách lịch sự.',
  'N5', N'ゆっくり話してください。', N'Hãy nói chậm thôi.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜ませんか',      N'V(ます形) + ませんか',
  N'Mời cùng làm gì / Rủ rê',
  N'Dùng để mời hoặc rủ ai đó cùng làm điều gì. Nhẹ nhàng, thân thiện hơn so với 〜ましょう。',
  'N5', N'一緒に映画を見ませんか。', N'Mình cùng xem phim không?',
  'published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   VII. NGỮ PHÁP N4
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM grammar_points WHERE structure = N'〜てしまう')
INSERT INTO grammar_points (structure, formula, meaning, usage_explanation, jlpt_level,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
( N'〜てしまう',      N'V(て形) + しまう',
  N'Lỡ làm gì / Đã hoàn toàn làm xong (đôi khi tiếc nuối)',
  N'Diễn đạt hành động đã hoàn thành hoàn toàn, thường kèm sắc thái tiếc nuối hoặc ngoài ý muốn.',
  'N4', N'宿題を忘れてしまいました。', N'Tôi đã lỡ quên bài tập về nhà rồi.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜ておく',        N'V(て形) + おく',
  N'Làm trước để chuẩn bị',
  N'Diễn đạt hành động thực hiện trước để chuẩn bị cho tương lai.',
  'N4', N'旅行の前にホテルを予約しておきます。', N'Trước khi đi du lịch tôi đặt khách sạn trước.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜ばよかった',    N'V(ば形) + よかった',
  N'Giá mà đã làm... thì tốt hơn (hối tiếc)',
  N'Diễn đạt sự hối tiếc về điều gì đó không xảy ra hoặc đã xảy ra trong quá khứ.',
  'N4', N'もっと早く起きればよかった。', N'Giá mà tôi dậy sớm hơn thì tốt.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜そうだ',        N'V/Adj(語幹) + そうだ',
  N'Có vẻ như... / Trông có vẻ...',
  N'Diễn đạt phán đoán dựa trên quan sát trực tiếp. Khác với 〜そうだ(truyền đạt thông tin nghe được).',
  'N4', N'雨が降りそうです。', N'Trời có vẻ sắp mưa.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜ようにする',    N'V(辞書形/ない形) + ようにする',
  N'Cố gắng làm sao để... / Đặt mục tiêu',
  N'Diễn đạt nỗ lực để đạt được hoặc duy trì một thói quen hay trạng thái.',
  'N4', N'毎日野菜を食べるようにしています。', N'Tôi cố gắng ăn rau mỗi ngày.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜てみる',        N'V(て形) + みる',
  N'Thử làm xem',
  N'Diễn đạt việc thực hiện thử một hành động để xem kết quả thế nào.',
  'N4', N'この料理を食べてみました。', N'Tôi đã thử ăn món này.',
  'published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   VIII. NGỮ PHÁP N3
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM grammar_points WHERE structure = N'〜ために')
INSERT INTO grammar_points (structure, formula, meaning, usage_explanation, jlpt_level,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
( N'〜ために',        N'N の / V(辞書形) + ために',
  N'Để / Vì mục đích...',
  N'Diễn đạt mục đích của hành động. Chủ ngữ câu trước và sau thường giống nhau.',
  'N3', N'日本語を学ぶために、毎日練習します。', N'Để học tiếng Nhật, tôi luyện tập mỗi ngày.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜によって',      N'N + によって',
  N'Tùy theo / Do... / Bằng phương tiện...',
  N'Diễn đạt phương tiện, nguyên nhân, hoặc sự khác biệt tùy theo đối tượng.',
  'N3', N'国によって文化が違います。', N'Văn hóa khác nhau tùy theo quốc gia.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜に対して',      N'N + に対して',
  N'Đối với / Hướng tới',
  N'Diễn đạt đối tượng mà hành động hay thái độ hướng đến.',
  'N3', N'学生に対して厳しくしてはいけません。', N'Không nên khắt khe với học sinh.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜わけだ',        N'普通形 + わけだ',
  N'Đó là lý do tại sao... / Có nghĩa là...',
  N'Diễn đạt kết luận logic dựa trên thông tin đã biết, hoặc giải thích nguyên nhân/lý do.',
  'N3', N'10年間日本に住んでいたから、日本語が上手なわけだ。', N'Anh ấy sống ở Nhật 10 năm, đó là lý do tiếng Nhật giỏi thế.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜くせに',        N'普通形 + くせに',
  N'Mặc dù... mà vẫn... (trách móc, phê phán)',
  N'Diễn đạt sự trách móc hoặc bất ngờ tiêu cực khi chủ ngữ làm điều trái với những gì người nói mong đợi.',
  'N3', N'知っているくせに、教えてくれなかった。', N'Dù biết mà vẫn không nói cho tôi.',
  'published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   IX. NGỮ PHÁP N2
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM grammar_points WHERE structure = N'〜に違いない')
INSERT INTO grammar_points (structure, formula, meaning, usage_explanation, jlpt_level,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
( N'〜に違いない',    N'普通形 + に違いない',
  N'Chắc chắn là... (phán đoán mạnh)',
  N'Diễn đạt sự phán đoán mạnh của người nói, tin rằng điều đó chắc chắn đúng.',
  'N2', N'あんなに勉強したから、合格に違いない。', N'Học nhiều như vậy, chắc chắn sẽ đỗ.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜に過ぎない',    N'N / 普通形 + に過ぎない',
  N'Chỉ là... / Không hơn không kém',
  N'Diễn đạt rằng điều gì đó chỉ ở mức tối thiểu, không quan trọng hay ấn tượng như người khác nghĩ.',
  'N2', N'それは噂に過ぎない。', N'Đó chỉ là tin đồn mà thôi.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜をきっかけに',  N'N + をきっかけに(して)',
  N'Lấy... làm cơ hội / Nhờ... mà bắt đầu',
  N'Diễn đạt sự kiện hoặc tình huống trở thành bước ngoặt hoặc cơ hội khởi đầu cho hành động tiếp theo.',
  'N2', N'この出会いをきっかけに日本語を学び始めました。', N'Nhờ cuộc gặp gỡ này mà tôi bắt đầu học tiếng Nhật.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜に基づいて',    N'N + に基づいて / に基づく',
  N'Dựa trên / Căn cứ vào',
  N'Diễn đạt hành động hoặc quyết định dựa trên tiêu chuẩn, quy tắc, hoặc thông tin cụ thể.',
  'N2', N'データに基づいて判断します。', N'Chúng tôi quyết định dựa trên dữ liệu.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜かねない',      N'V(ます形) + かねない',
  N'Có thể (sẽ làm điều xấu) / Không thể không...',
  N'Diễn đạt khả năng xảy ra điều không mong muốn. Mang sắc thái cảnh báo, lo ngại.',
  'N2', N'このままでは失敗しかねない。', N'Cứ thế này có thể thất bại đó.',
  'published', @staff_id, @manager_id, @now, @now, @now );
GO

/* ============================================================
   X. NGỮ PHÁP N1
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM grammar_points WHERE structure = N'〜ならいざ知らず')
INSERT INTO grammar_points (structure, formula, meaning, usage_explanation, jlpt_level,
    example_sentence_jp, example_sentence_vi,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
( N'〜ならいざ知らず',N'N + ならいざ知らず',
  N'Nếu là... thì có thể hiểu được, nhưng...',
  N'Diễn đạt rằng trường hợp A có thể chấp nhận được, nhưng trường hợp B thì không thể chấp nhận được.',
  'N1', N'子供ならいざ知らず、大人がそんなことをするのは非常識だ。', N'Trẻ con thì có thể, nhưng người lớn mà làm vậy là thiếu hiểu biết.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜をものともせず',N'N + をものともせず(に)',
  N'Bất chấp... / Không coi... là trở ngại',
  N'Diễn đạt việc không bị ảnh hưởng hoặc không coi trở ngại nào đó là vấn đề, tiếp tục hành động.',
  'N1', N'困難をものともせず、夢を追い続けた。', N'Bất chấp khó khăn, anh ấy vẫn tiếp tục theo đuổi ước mơ.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜とあって',      N'N / 普通形 + とあって',
  N'Vì là... nên / Do... mà',
  N'Diễn đạt lý do đặc biệt dẫn đến kết quả tự nhiên. Thường dùng trong văn viết.',
  'N1', N'大会前とあって、選手たちは緊張していた。', N'Vì là ngay trước giải đấu, các vận động viên rất căng thẳng.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜といえども',    N'N / 普通形 + といえども',
  N'Dù là... đi chăng nữa (nhượng bộ trang trọng)',
  N'Diễn đạt sự nhượng bộ theo phong cách trang trọng, thừa nhận điều kiện nhưng kết quả vẫn trái ngược.',
  'N1', N'プロといえども、常に努力が必要だ。', N'Dù là chuyên gia, cũng luôn cần nỗ lực.',
  'published', @staff_id, @manager_id, @now, @now, @now ),

( N'〜いかんによらず',N'N + のいかんによらず / いかんにかかわらず',
  N'Bất kể... như thế nào',
  N'Diễn đạt rằng dù điều kiện hay tình huống ra sao, kết quả/hành động vẫn không thay đổi. Rất trang trọng.',
  'N1', N'結果のいかんによらず、最善を尽くします。', N'Dù kết quả thế nào, tôi sẽ cố hết sức.',
  'published', @staff_id, @manager_id, @now, @now, @now );
GO


/* ============================================================
   XI. BỔ SUNG KANJI N4 (để dictionary search phong phú hơn)
   ============================================================ */

DECLARE @staff_id   BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'staff@sakuji.com');
DECLARE @manager_id BIGINT   = (SELECT TOP 1 staff_id FROM staff_users WHERE email = 'manager@sakuji.com');
DECLARE @now        DATETIME2 = SYSUTCDATETIME();

IF NOT EXISTS (SELECT 1 FROM kanji WHERE character_value = N'海')
INSERT INTO kanji (character_value, meaning, onyomi, kunyomi, stroke_count, jlpt_level,
    example_word, example_reading, example_meaning,
    status, created_by, approved_by, published_at, created_at, updated_at)
VALUES
( N'海', N'Biển / Đại dương', N'カイ',   N'うみ',        9,  'N4', N'海外', N'かいがい', N'Nước ngoài',      'published', @staff_id, @manager_id, @now, @now, @now ),
( N'旅', N'Du hành / Du lịch',N'リョ',   N'たび',        10, 'N4', N'旅行', N'りょこう', N'Du lịch',         'published', @staff_id, @manager_id, @now, @now, @now ),
( N'花', N'Hoa',              N'カ',     N'はな',        7,  'N4', N'花見', N'はなみ',   N'Ngắm hoa anh đào','published', @staff_id, @manager_id, @now, @now, @now ),
( N'空', N'Bầu trời / Trống', N'クウ',   N'そら・から',  8,  'N4', N'空港', N'くうこう', N'Sân bay',          'published', @staff_id, @manager_id, @now, @now, @now ),
( N'川', N'Sông / Suối',      N'セン',   N'かわ',        3,  'N4', N'川沿い',N'かわぞい',N'Dọc bờ sông',     'published', @staff_id, @manager_id, @now, @now, @now ),
( N'雨', N'Mưa',              N'ウ',     N'あめ',        8,  'N4', N'大雨', N'おおあめ', N'Mưa lớn',          'published', @staff_id, @manager_id, @now, @now, @now ),
( N'雪', N'Tuyết',            N'セツ',   N'ゆき',        11, 'N4', N'雪山', N'ゆきやま', N'Núi tuyết',        'published', @staff_id, @manager_id, @now, @now, @now ),
( N'風', N'Gió / Phong tục',  N'フウ・フ',N'かぜ・かざ', 9,  'N4', N'台風', N'たいふう', N'Bão',              'published', @staff_id, @manager_id, @now, @now, @now ),
( N'体', N'Cơ thể / Thân',    N'タイ',   N'からだ',      7,  'N4', N'体力', N'たいりょく',N'Thể lực',         'published', @staff_id, @manager_id, @now, @now, @now ),
( N'道', N'Con đường / Đạo',  N'ドウ',   N'みち',        12, 'N4', N'道路', N'どうろ',   N'Đường bộ',         'published', @staff_id, @manager_id, @now, @now, @now );
GO


PRINT N'';
PRINT N'✅ V7 Seed hoàn tất.';
PRINT N'   Từ vựng: ~100 từ (N5→N1), status = published';
PRINT N'   Ngữ pháp: 26 cấu trúc (N5→N1), status = published';
PRINT N'   Kanji bổ sung: 10 chữ N4, status = published';
PRINT N'';
GO
