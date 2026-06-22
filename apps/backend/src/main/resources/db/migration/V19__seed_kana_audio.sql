-- V9: Gán đường dẫn file phát âm cho bảng chữ Kana (UC-08)
-- File mp3 được sinh bởi  apps/frontend/scripts/generate-kana-audio.mjs
-- lưu tại  apps/backend/uploads/audio/kana/<romaji>.mp3  (ADR-006: file ở /uploads)
-- và phục vụ tĩnh qua backend tại  /api/files/audio/kana/<romaji>.mp3 .
-- Hiragana và Katakana cùng romaji dùng chung 1 file (phát âm giống nhau).
-- Chỉ ghi đè các dòng chưa có audio để không đụng dữ liệu đã chỉnh tay.
UPDATE kana_characters
SET audio_url = '/api/files/audio/kana/' + LOWER(romaji) + '.mp3'
WHERE romaji IS NOT NULL
  AND LTRIM(RTRIM(romaji)) <> ''
  AND (audio_url IS NULL OR LTRIM(RTRIM(audio_url)) = '');
GO
