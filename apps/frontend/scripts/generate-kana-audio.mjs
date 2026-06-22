/**
 * Sinh file phát âm (.mp3) cho toàn bộ bảng chữ Kana.
 *
 * Cách dùng:   node scripts/generate-kana-audio.mjs
 *
 * - Phát âm Hiragana và Katakana là GIỐNG NHAU, nên file được đặt tên theo
 *   romaji (vd: a.mp3, shi.mp3, kya.mp3) và dùng chung cho cả hai bảng.
 * - File được lưu vào  apps/backend/uploads/audio/kana/  (ADR-006: file ở /uploads),
 *   backend phục vụ tĩnh tại  /api/files/audio/kana/<romaji>.mp3  (khớp migration V9).
 * - Script bỏ qua file đã tồn tại nên có thể chạy lại an toàn.
 *
 * Nguồn audio: Google Translate TTS (free, không cần API key). Nếu cần chất
 * lượng cao hơn có thể thay hàm fetchTts() bằng dịch vụ khác (Azure/VOICEVOX).
 */
import { mkdir, writeFile, access } from 'node:fs/promises';
import { constants } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const __dirname = dirname(fileURLToPath(import.meta.url));
// apps/frontend/scripts -> apps/backend/uploads/audio/kana
const OUT_DIR = join(__dirname, '..', '..', 'backend', 'uploads', 'audio', 'kana');

// romaji  ->  ký tự hiragana dùng để đọc (phát âm = katakana tương ứng)
const KANA = {
  // gojūon
  a: 'あ', i: 'い', u: 'う', e: 'え', o: 'お',
  ka: 'か', ki: 'き', ku: 'く', ke: 'け', ko: 'こ',
  sa: 'さ', shi: 'し', su: 'す', se: 'せ', so: 'そ',
  ta: 'た', chi: 'ち', tsu: 'つ', te: 'て', to: 'と',
  na: 'な', ni: 'に', nu: 'ぬ', ne: 'ね', no: 'の',
  ha: 'は', hi: 'ひ', fu: 'ふ', he: 'へ', ho: 'ほ',
  ma: 'ま', mi: 'み', mu: 'む', me: 'め', mo: 'も',
  ya: 'や', yu: 'ゆ', yo: 'よ',
  ra: 'ら', ri: 'り', ru: 'る', re: 'れ', ro: 'ろ',
  wa: 'わ', wo: 'を', n: 'ん',
  // dakuten / handakuten
  ga: 'が', gi: 'ぎ', gu: 'ぐ', ge: 'げ', go: 'ご',
  za: 'ざ', ji: 'じ', zu: 'ず', ze: 'ぜ', zo: 'ぞ',
  da: 'だ', de: 'で', do: 'ど',
  ba: 'ば', bi: 'び', bu: 'ぶ', be: 'べ', bo: 'ぼ',
  pa: 'ぱ', pi: 'ぴ', pu: 'ぷ', pe: 'ぺ', po: 'ぽ',
  // yōon
  kya: 'きゃ', kyu: 'きゅ', kyo: 'きょ',
  sha: 'しゃ', shu: 'しゅ', sho: 'しょ',
  cha: 'ちゃ', chu: 'ちゅ', cho: 'ちょ',
  nya: 'にゃ', nyu: 'にゅ', nyo: 'にょ',
  hya: 'ひゃ', hyu: 'ひゅ', hyo: 'ひょ',
  mya: 'みゃ', myu: 'みゅ', myo: 'みょ',
  rya: 'りゃ', ryu: 'りゅ', ryo: 'りょ',
  gya: 'ぎゃ', gyu: 'ぎゅ', gyo: 'ぎょ',
  ja: 'じゃ', ju: 'じゅ', jo: 'じょ',
  bya: 'びゃ', byu: 'びゅ', byo: 'びょ',
  pya: 'ぴゃ', pyu: 'ぴゅ', pyo: 'ぴょ',
};

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function exists(path) {
  try {
    await access(path, constants.F_OK);
    return true;
  } catch {
    return false;
  }
}

async function fetchTts(text) {
  const url =
    'https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=ja&q=' +
    encodeURIComponent(text);
  const res = await fetch(url, {
    headers: { 'User-Agent': 'Mozilla/5.0', Referer: 'https://translate.google.com/' },
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return Buffer.from(await res.arrayBuffer());
}

async function main() {
  await mkdir(OUT_DIR, { recursive: true });
  const entries = Object.entries(KANA);
  let made = 0;
  let skipped = 0;

  for (const [romaji, char] of entries) {
    const out = join(OUT_DIR, `${romaji}.mp3`);
    if (await exists(out)) {
      skipped++;
      continue;
    }
    try {
      const buf = await fetchTts(char);
      await writeFile(out, buf);
      made++;
      console.log(`✓ ${romaji} (${char})  ${buf.length} bytes`);
      await sleep(400); // tránh bị rate-limit
    } catch (err) {
      console.error(`✗ ${romaji} (${char}): ${err.message}`);
    }
  }

  console.log(`\nXong. Tạo mới: ${made}, bỏ qua (đã có): ${skipped}, tổng: ${entries.length}`);
  console.log(`Thư mục: ${OUT_DIR}`);
}

main();
