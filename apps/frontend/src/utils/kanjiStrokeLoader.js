export const fetchKanjiStrokeData = async (character) => {
  const charEnc = encodeURIComponent((character || '').trim());
  if (!charEnc) throw new Error('Empty character');

  const urls = [
    `https://unpkg.com/hanzi-writer-data@2.0.1/${charEnc}.json`,
    `https://fastly.jsdelivr.net/npm/hanzi-writer-data@2.0/${charEnc}.json`,
    `https://cdn.jsdelivr.net/npm/hanzi-writer-data@2.0/${charEnc}.json`
  ];

  for (const url of urls) {
    try {
      const res = await fetch(url);
      if (res.ok) {
        return await res.json();
      }
    } catch (e) {
      // Ignore and try the next CDN
    }
  }

  throw new Error(`Ký tự ${character} chưa có dữ liệu nét`);
};
