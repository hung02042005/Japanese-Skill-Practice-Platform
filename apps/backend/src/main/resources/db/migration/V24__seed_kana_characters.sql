-- V24: Seed dữ liệu bảng chữ Kana (UC-08)
-- Bảng kana_characters được tạo ở V1 nhưng chưa từng có dòng dữ liệu nào,
-- khiến màn "かな Kana" của Student rỗng. Migration này nạp đầy đủ:
--   - Hiragana & Katakana: gojūon (46) + dakuten/handakuten (25) = 71 ký tự / bảng.
-- kana_type lưu chữ THƯỜNG để thoả CHECK constraint ('hiragana','katakana').
-- Chỉ seed khi bảng đang rỗng để không đụng dữ liệu đã chỉnh tay.
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

-- Gán đường dẫn audio theo romaji (đồng bộ với V19). Hiragana/Katakana cùng romaji dùng chung file.
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
