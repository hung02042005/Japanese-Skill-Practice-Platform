// Tra cứu Kanji theo cách đọc — dùng cho phần Staff tạo Kanji.
// Người dùng gõ romaji ("gaku") -> chuyển sang kana ("がく") -> tra index reading→[Kanji].
import readingIndex from '../data/kanjiReadingIndex.json';
import charInfo from '../data/kanjiCharInfo.json';

// Bảng romaji -> hiragana (Hepburn + một số biến thể kunrei). Khớp tham lam theo độ dài giảm dần.
const ROMAJI_MAP = {
  kya: 'きゃ', kyu: 'きゅ', kyo: 'きょ', sha: 'しゃ', shu: 'しゅ', sho: 'しょ',
  sya: 'しゃ', syu: 'しゅ', syo: 'しょ', cha: 'ちゃ', chu: 'ちゅ', cho: 'ちょ',
  tya: 'ちゃ', tyu: 'ちゅ', tyo: 'ちょ', nya: 'にゃ', nyu: 'にゅ', nyo: 'にょ',
  hya: 'ひゃ', hyu: 'ひゅ', hyo: 'ひょ', mya: 'みゃ', myu: 'みゅ', myo: 'みょ',
  rya: 'りゃ', ryu: 'りゅ', ryo: 'りょ', gya: 'ぎゃ', gyu: 'ぎゅ', gyo: 'ぎょ',
  ja: 'じゃ', ju: 'じゅ', jo: 'じょ', jya: 'じゃ', jyu: 'じゅ', jyo: 'じょ',
  zya: 'じゃ', zyu: 'じゅ', zyo: 'じょ', bya: 'びゃ', byu: 'びゅ', byo: 'びょ',
  pya: 'ぴゃ', pyu: 'ぴゅ', pyo: 'ぴょ', dya: 'ぢゃ', dyu: 'ぢゅ', dyo: 'ぢょ',
  shi: 'し', chi: 'ち', tsu: 'つ',
  ka: 'か', ki: 'き', ku: 'く', ke: 'け', ko: 'こ',
  sa: 'さ', si: 'し', su: 'す', se: 'せ', so: 'そ',
  ta: 'た', ti: 'ち', tu: 'つ', te: 'て', to: 'と',
  na: 'な', ni: 'に', nu: 'ぬ', ne: 'ね', no: 'の',
  ha: 'は', hi: 'ひ', fu: 'ふ', hu: 'ふ', he: 'へ', ho: 'ほ',
  ma: 'ま', mi: 'み', mu: 'む', me: 'め', mo: 'も',
  ya: 'や', yu: 'ゆ', yo: 'よ',
  ra: 'ら', ri: 'り', ru: 'る', re: 'れ', ro: 'ろ',
  wa: 'わ', wo: 'を', wi: 'うぃ', we: 'うぇ',
  ga: 'が', gi: 'ぎ', gu: 'ぐ', ge: 'げ', go: 'ご',
  za: 'ざ', ji: 'じ', zi: 'じ', zu: 'ず', ze: 'ぜ', zo: 'ぞ',
  da: 'だ', di: 'ぢ', du: 'づ', de: 'で', do: 'ど',
  ba: 'ば', bi: 'び', bu: 'ぶ', be: 'べ', bo: 'ぼ',
  pa: 'ぱ', pi: 'ぴ', pu: 'ぷ', pe: 'ぺ', po: 'ぽ',
  fa: 'ふぁ', fi: 'ふぃ', fe: 'ふぇ', fo: 'ふぉ',
  a: 'あ', i: 'い', u: 'う', e: 'え', o: 'お',
};

const isVowel = (c) => 'aiueo'.includes(c);

// Chuyển chuỗi romaji (ascii) sang hiragana. Xử lý: phụ âm đôi -> っ, "n"/"n'" -> ん.
export function romajiToHiragana(input) {
  let s = String(input || '').toLowerCase().replace(/[^a-z']/g, '');
  let out = '';
  let i = 0;
  while (i < s.length) {
    const c = s[i];
    // Phụ âm đôi (sokuon): "kk", "tt"... -> っ (trừ "nn" được xử lý riêng).
    if (c !== 'n' && !isVowel(c) && s[i + 1] === c) {
      out += 'っ';
      i += 1;
      continue;
    }
    // "n" đứng trước phụ âm (không phải y) hoặc cuối chuỗi, hoặc "n'" -> ん.
    if (c === 'n') {
      const nx = s[i + 1];
      if (nx === "'") { out += 'ん'; i += 2; continue; }
      if (!nx || (!isVowel(nx) && nx !== 'y')) { out += 'ん'; i += 1; continue; }
    }
    // Khớp tham lam 3 -> 2 -> 1 ký tự.
    let matched = false;
    for (let len = 3; len >= 1; len -= 1) {
      const chunk = s.slice(i, i + len);
      if (ROMAJI_MAP[chunk]) { out += ROMAJI_MAP[chunk]; i += len; matched = true; break; }
    }
    if (!matched) { i += 1; } // bỏ qua ký tự không nhận dạng được
  }
  return out;
}

const kataToHira = (s) => s.replace(/[ァ-ヶ]/g, (c) => String.fromCharCode(c.charCodeAt(0) - 0x60));

// Tra danh sách Kanji theo cách đọc. Nhận cả romaji ("gaku") lẫn kana ("がく"/"ガク").
// Trả về { kana, candidates } — candidates đã sắp theo cấp độ JLPT (N5 trước) rồi tần suất.
export function lookupKanjiByReading(input) {
  const raw = String(input || '').trim();
  if (!raw) return { kana: '', candidates: [] };
  // Nếu đã là kana thì chuẩn hoá về hiragana, ngược lại chuyển từ romaji.
  const hasKana = /[ぁ-ゖァ-ヶ]/.test(raw);
  const kana = hasKana ? kataToHira(raw).replace(/[^ぁ-ゖ]/g, '') : romajiToHiragana(raw);
  if (!kana) return { kana: '', candidates: [] };
  return { kana, candidates: readingIndex[kana] || [] };
}

// Lấy cách đọc on/kun có sẵn của một ký tự Kanji (để tự điền form). Trả null nếu không có dữ liệu.
export function getKanjiInfo(ch) {
  return charInfo[ch] || null;
}

/**
 * Custom charDataLoader for HanziWriter.
 * Fetches Japanese Kanji stroke data from unpkg CDN (non-blocked in Vietnam).
 * Falls back to Chinese Hanzi data if the Kanji is not present in the Japanese dataset.
 */
export function jpCharDataLoader(char, onLoad, onError) {
  fetch(`https://unpkg.com/hanzi-writer-data-jp@0/${encodeURIComponent(char)}.json`)
    .then((res) => {
      if (!res.ok) {
        // Fallback to standard Chinese dataset
        return fetch(`https://unpkg.com/hanzi-writer-data@2/${encodeURIComponent(char)}.json`);
      }
      return res;
    })
    .then((res) => {
      if (!res.ok) throw new Error(`Failed to load stroke data for character: ${char}`);
      return res.json();
    })
    .then(onLoad)
    .catch(onError);
}
