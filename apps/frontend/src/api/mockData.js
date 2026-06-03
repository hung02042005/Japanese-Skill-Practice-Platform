// DEMO_MODE mock data â€” dÃ¹ng Ä‘á»ƒ kiá»ƒm tra UI mÃ  khÃ´ng cáº§n backend
// Set DEMO_MODE = false khi káº¿t ná»‘i backend tháº­t

export const DEMO_MODE = true;

// â”€â”€â”€ LearnNew â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_NEXT_LESSON = {
  lessonId: 1,
  title: 'Báº£ng chá»¯ cÃ¡i Hiragana',
  jlptLevel: 'N5',
  lessonType: 'KANA',
  estimatedMinutes: 15,
  description: 'Há»c 46 kÃ½ tá»± Hiragana cÆ¡ báº£n â€” ná»n táº£ng Ä‘á»ƒ Ä‘á»c tiáº¿ng Nháº­t.',
  progressPercent: 30,
};

export const MOCK_SUGGESTED_LESSONS = [
  { lessonId: 2, title: 'Tá»« vá»±ng chÃ o há»i N5', jlptLevel: 'N5', lessonType: 'VOCAB',   estimatedMinutes: 10, progressPercent: 0 },
  { lessonId: 3, title: 'Ngá»¯ phÃ¡p: ã¯ ã¨ ãŒ',  jlptLevel: 'N5', lessonType: 'GRAMMAR', estimatedMinutes: 20, progressPercent: 60 },
  { lessonId: 4, title: 'Kanji sá»‘ Ä‘áº¿m ä¸€äºŒä¸‰', jlptLevel: 'N5', lessonType: 'KANJI',   estimatedMinutes: 12, progressPercent: 0 },
];

// â”€â”€â”€ LessonDetail â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_LESSON_DETAIL_MAP = {
  1: {
    lessonId: 1,
    title: 'Hiragana',
    jlptLevel: 'N5',
    lessonType: 'KANA',
    estimatedMinutes: 15,
    progressPercent: 60,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>Hiragana lÃ  gÃ¬?</h2>
      <p>Hiragana (ã²ã‚‰ãŒãª) lÃ  má»™t trong ba báº£ng chá»¯ cÃ¡i tiáº¿ng Nháº­t, gá»“m <strong>46 kÃ½ tá»±</strong> cÆ¡ báº£n.</p>
      <h3>NhÃ³m nguyÃªn Ã¢m (ã‚è¡Œ)</h3>
      <ul><li>ã‚ (a)ã€€ã„ (i)ã€€ã† (u)ã€€ãˆ (e)ã€€ãŠ (o)</li></ul>
      <h3>NhÃ³m ã‹è¡Œ</h3>
      <ul><li>ã‹ (ka)ã€€ã (ki)ã€€ã (ku)ã€€ã‘ (ke)ã€€ã“ (ko)</li></ul>
      <h3>NhÃ³m ã•è¡Œ</h3>
      <ul><li>ã• (sa)ã€€ã— (shi)ã€€ã™ (su)ã€€ã› (se)ã€€ã (so)</li></ul>
      <p>ðŸ’¡ <strong>Máº¹o há»c:</strong> Há»c má»—i ngÃ y 1 hÃ ng (5 kÃ½ tá»±), luyá»‡n viáº¿t vÃ  Ä‘á»c thÃ nh tiáº¿ng Ä‘á»ƒ nhá»› lÃ¢u hÆ¡n!</p>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 2,
    vocabulary: [
      { vocabId: 1, word: 'ã“ã‚“ã«ã¡ã¯', reading: 'konnichiwa', meaning: 'Xin chÃ o (buá»•i trÆ°a/chiá»u)', exampleSentence: 'ã“ã‚“ã«ã¡ã¯ã€ç”°ä¸­ã•ã‚“ã€‚', exampleTranslation: 'Xin chÃ o, anh Tanaka.' },
      { vocabId: 2, word: 'ã‚ã‚ŠãŒã¨ã†', reading: 'arigatou', meaning: 'Cáº£m Æ¡n', exampleSentence: 'ã‚ã‚ŠãŒã¨ã†ã”ã–ã„ã¾ã™ã€‚', exampleTranslation: 'Xin cáº£m Æ¡n ráº¥t nhiá»u.' },
      { vocabId: 3, word: 'ã™ã¿ã¾ã›ã‚“', reading: 'sumimasen', meaning: 'Xin lá»—i / Cho há»i', exampleSentence: 'ã™ã¿ã¾ã›ã‚“ã€é§…ã¯ã©ã“ã§ã™ã‹ï¼Ÿ', exampleTranslation: 'Xin lá»—i, ga tÃ u á»Ÿ Ä‘Ã¢u áº¡?' },
    ],
    grammarPoints: [
      { grammarId: 1, structure: 'ã€œã¯ã€œã§ã™', formula: 'Danh tá»« + ã¯ + Danh tá»« + ã§ã™', meaning: 'A lÃ  B', exampleJp: 'ã‚ãŸã—ã¯å­¦ç”Ÿã§ã™ã€‚', exampleVi: 'TÃ´i lÃ  há»c sinh.' },
    ],
  },
  2: {
    lessonId: 2,
    title: 'Katakana',
    jlptLevel: 'N5',
    lessonType: 'KANA',
    estimatedMinutes: 15,
    progressPercent: 0,
    progressStatus: 'not_started',
    isLocked: false,
    contentHtml: `
      <h2>Katakana lÃ  gÃ¬?</h2>
      <p>Katakana (ã‚«ã‚¿ã‚«ãƒŠ) lÃ  báº£ng chá»¯ cÃ¡i thá»© hai cá»§a tiáº¿ng Nháº­t, gá»“m <strong>46 kÃ½ tá»±</strong>. DÃ¹ng chá»§ yáº¿u Ä‘á»ƒ phiÃªn Ã¢m tá»« nÆ°á»›c ngoÃ i.</p>
      <h3>NhÃ³m nguyÃªn Ã¢m (ã‚¢è¡Œ)</h3>
      <ul><li>ã‚¢ (a)ã€€ã‚¤ (i)ã€€ã‚¦ (u)ã€€ã‚¨ (e)ã€€ã‚ª (o)</li></ul>
      <h3>NhÃ³m ã‚«è¡Œ</h3>
      <ul><li>ã‚« (ka)ã€€ã‚­ (ki)ã€€ã‚¯ (ku)ã€€ã‚± (ke)ã€€ã‚³ (ko)</li></ul>
      <p>ðŸ’¡ <strong>VÃ­ dá»¥ tá»« ngoáº¡i lai:</strong> ã‚³ãƒ¼ãƒ’ãƒ¼ (kÅhÄ« - cÃ  phÃª)ã€ãƒ†ãƒ¬ãƒ“ (terebi - tivi)</p>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: 1, nextLessonId: 3,
    vocabulary: [
      { vocabId: 10, word: 'ã‚³ãƒ¼ãƒ’ãƒ¼', reading: 'kÅhÄ«', meaning: 'CÃ  phÃª', exampleSentence: 'ã‚³ãƒ¼ãƒ’ãƒ¼ã‚’ä¸€æ¯ãã ã•ã„ã€‚', exampleTranslation: 'Cho tÃ´i má»™t ly cÃ  phÃª.' },
      { vocabId: 11, word: 'ãƒ†ãƒ¬ãƒ“', reading: 'terebi', meaning: 'Tivi', exampleSentence: 'ãƒ†ãƒ¬ãƒ“ã‚’è¦‹ã¾ã™ã€‚', exampleTranslation: 'TÃ´i xem tivi.' },
    ],
    grammarPoints: [],
  },
  3: {
    lessonId: 3,
    title: 'Tá»« vá»±ng N5',
    jlptLevel: 'N5',
    lessonType: 'VOCAB',
    estimatedMinutes: 20,
    progressPercent: 0,
    progressStatus: 'not_started',
    isLocked: false,
    contentHtml: `
      <h2>800 tá»« vá»±ng N5 thÃ´ng dá»¥ng nháº¥t</h2>
      <p>ÄÃ¢y lÃ  danh sÃ¡ch cÃ¡c tá»« vá»±ng cÆ¡ báº£n nháº¥t trong ká»³ thi JLPT N5. Náº¯m vá»¯ng nhá»¯ng tá»« nÃ y giÃºp báº¡n giao tiáº¿p trong cÃ¡c tÃ¬nh huá»‘ng hÃ ng ngÃ y.</p>
      <h3>Chá»§ Ä‘á»: Gia Ä‘Ã¬nh</h3>
      <ul>
        <li>ãŠçˆ¶ã•ã‚“ (otÅsan) â€” bá»‘</li>
        <li>ãŠæ¯ã•ã‚“ (okÄsan) â€” máº¹</li>
        <li>å…„ (ani) â€” anh trai</li>
        <li>å§‰ (ane) â€” chá»‹ gÃ¡i</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: 2, nextLessonId: null,
    vocabulary: [
      { vocabId: 20, word: 'ãŠçˆ¶ã•ã‚“', reading: 'otÅsan', meaning: 'Bá»‘ (kÃ­nh ngá»¯)', exampleSentence: 'ãŠçˆ¶ã•ã‚“ã¯ã©ã“ã§ã™ã‹ï¼Ÿ', exampleTranslation: 'Bá»‘ Ä‘ang á»Ÿ Ä‘Ã¢u áº¡?' },
      { vocabId: 21, word: 'ãŠæ¯ã•ã‚“', reading: 'okÄsan', meaning: 'Máº¹ (kÃ­nh ngá»¯)', exampleSentence: 'ãŠæ¯ã•ã‚“ã¯æ–™ç†ãŒä¸Šæ‰‹ã§ã™ã€‚', exampleTranslation: 'Máº¹ náº¥u Äƒn ráº¥t giá»i.' },
      { vocabId: 22, word: 'å­¦æ ¡', reading: 'gakkÅ', meaning: 'TrÆ°á»ng há»c', exampleSentence: 'å­¦æ ¡ã¸è¡Œãã¾ã™ã€‚', exampleTranslation: 'TÃ´i Ä‘i há»c.' },
    ],
    grammarPoints: [],
  },
  6: {
    lessonId: 6,
    title: 'Tá»« vá»±ng N4',
    jlptLevel: 'N4',
    lessonType: 'VOCAB',
    estimatedMinutes: 25,
    progressPercent: 30,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>1.500 tá»« vá»±ng N4 giao tiáº¿p hÃ ng ngÃ y</h2>
      <p>á»ž cáº¥p Ä‘á»™ N4, báº¡n cáº§n náº¯m vá»¯ng cÃ¡c tá»« vá»±ng dÃ¹ng trong giao tiáº¿p hÃ ng ngÃ y vÃ  cÃ¡c tÃ¬nh huá»‘ng phá»• biáº¿n.</p>
      <h3>Chá»§ Ä‘á»: CÃ´ng viá»‡c & XÃ£ há»™i</h3>
      <ul>
        <li>ä¼šç¤¾ (kaisha) â€” cÃ´ng ty</li>
        <li>ç¤¾å“¡ (shain) â€” nhÃ¢n viÃªn cÃ´ng ty</li>
        <li>ä¼šè­° (kaigi) â€” cuá»™c há»p</li>
        <li>ä»•äº‹ (shigoto) â€” cÃ´ng viá»‡c</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 7,
    vocabulary: [
      { vocabId: 30, word: 'ä¼šç¤¾', reading: 'kaisha', meaning: 'CÃ´ng ty', exampleSentence: 'ä¼šç¤¾ã«å‹¤ã‚ã¦ã„ã¾ã™ã€‚', exampleTranslation: 'TÃ´i Ä‘ang lÃ m viá»‡c á»Ÿ cÃ´ng ty.' },
      { vocabId: 31, word: 'ä¼šè­°', reading: 'kaigi', meaning: 'Cuá»™c há»p', exampleSentence: 'åˆå¾Œã«ä¼šè­°ãŒã‚ã‚Šã¾ã™ã€‚', exampleTranslation: 'Buá»•i chiá»u cÃ³ cuá»™c há»p.' },
      { vocabId: 32, word: 'é›»è©±', reading: 'denwa', meaning: 'Äiá»‡n thoáº¡i', exampleSentence: 'é›»è©±ã‚’ã‹ã‘ã¾ã™ã€‚', exampleTranslation: 'TÃ´i gá»i Ä‘iá»‡n thoáº¡i.' },
    ],
    grammarPoints: [
      { grammarId: 10, structure: 'ã€œã¦ã„ã‚‹', formula: 'Äá»™ng tá»« ã¦å½¢ + ã„ã‚‹', meaning: 'Äang lÃ m / Tráº¡ng thÃ¡i káº¿t quáº£', exampleJp: 'ä»Šã€ä»•äº‹ã‚’ã—ã¦ã„ã¾ã™ã€‚', exampleVi: 'BÃ¢y giá» tÃ´i Ä‘ang lÃ m viá»‡c.' },
    ],
  },
  7: {
    lessonId: 7,
    title: 'Ngá»¯ phÃ¡p N4',
    jlptLevel: 'N4',
    lessonType: 'GRAMMAR',
    estimatedMinutes: 30,
    progressPercent: 0,
    progressStatus: 'not_started',
    isLocked: false,
    contentHtml: `
      <h2>CÃ¡c máº«u cÃ¢u N4 phá»• biáº¿n</h2>
      <p>Ngá»¯ phÃ¡p N4 bao gá»“m cÃ¡c cáº¥u trÃºc cÃ¢u phá»©c táº¡p hÆ¡n N5, cho phÃ©p diá»…n Ä‘áº¡t Ã½ kiáº¿n, Ä‘iá»u kiá»‡n vÃ  cáº£m xÃºc.</p>
      <h3>Máº«u cÃ¢u Ä‘iá»u kiá»‡n</h3>
      <ul>
        <li>ã€œãŸã‚‰ â€” náº¿u...thÃ¬ (Ä‘iá»u kiá»‡n Ä‘Ã£ hoÃ n thÃ nh)</li>
        <li>ã€œã° â€” náº¿u...thÃ¬ (Ä‘iá»u kiá»‡n giáº£ Ä‘á»‹nh)</li>
        <li>ã€œã¨ â€” náº¿u...thÃ¬ (Ä‘iá»u kiá»‡n tá»± nhiÃªn)</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: 6, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 20, structure: 'ã€œãŸã‚‰', formula: 'Äá»™ng tá»« ãŸå½¢ + ã‚‰', meaning: 'Náº¿u/Khi (Ä‘iá»u kiá»‡n)', exampleJp: 'å®¶ã«å¸°ã£ãŸã‚‰ã€é›»è©±ã—ã¾ã™ã€‚', exampleVi: 'Khi vá» nhÃ , tÃ´i sáº½ gá»i Ä‘iá»‡n.' },
      { grammarId: 21, structure: 'ã€œãªãŒã‚‰', formula: 'Äá»™ng tá»« ã¾ã™å½¢ (bá» ã¾ã™) + ãªãŒã‚‰', meaning: 'Vá»«a...vá»«a...', exampleJp: 'éŸ³æ¥½ã‚’è´ããªãŒã‚‰å‹‰å¼·ã—ã¾ã™ã€‚', exampleVi: 'TÃ´i vá»«a nghe nháº¡c vá»«a há»c bÃ i.' },
    ],
  },
  9: {
    lessonId: 9,
    title: 'Tá»« vá»±ng N3',
    jlptLevel: 'N3',
    lessonType: 'VOCAB',
    estimatedMinutes: 30,
    progressPercent: 10,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>3.000 tá»« vá»±ng N3 trung cáº¥p thá»±c táº¿</h2>
      <p>N3 lÃ  cáº¥p Ä‘á»™ trung cáº¥p, yÃªu cáº§u vá»‘n tá»« vá»±ng phong phÃº hÆ¡n Ä‘á»ƒ hiá»ƒu cÃ¡c vÄƒn báº£n thá»±c táº¿ nhÆ° tin tá»©c, truyá»‡n ngáº¯n.</p>
      <h3>Chá»§ Ä‘á»: XÃ£ há»™i & VÄƒn hÃ³a</h3>
      <ul>
        <li>ç¤¾ä¼š (shakai) â€” xÃ£ há»™i</li>
        <li>æ–‡åŒ– (bunka) â€” vÄƒn hÃ³a</li>
        <li>çµŒæ¸ˆ (keizai) â€” kinh táº¿</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 10,
    vocabulary: [
      { vocabId: 40, word: 'ç¤¾ä¼š', reading: 'shakai', meaning: 'XÃ£ há»™i', exampleSentence: 'ç¤¾ä¼šã«è²¢çŒ®ã—ãŸã„ã€‚', exampleTranslation: 'TÃ´i muá»‘n Ä‘Ã³ng gÃ³p cho xÃ£ há»™i.' },
      { vocabId: 41, word: 'çµŒæ¸ˆ', reading: 'keizai', meaning: 'Kinh táº¿', exampleSentence: 'çµŒæ¸ˆãŒç™ºå±•ã—ã¦ã„ã¾ã™ã€‚', exampleTranslation: 'Kinh táº¿ Ä‘ang phÃ¡t triá»ƒn.' },
    ],
    grammarPoints: [],
  },
  10: {
    lessonId: 10,
    title: 'Ngá»¯ phÃ¡p N3',
    jlptLevel: 'N3',
    lessonType: 'GRAMMAR',
    estimatedMinutes: 35,
    progressPercent: 0,
    progressStatus: 'not_started',
    isLocked: false,
    contentHtml: `
      <h2>Cáº¥u trÃºc cÃ¢u phá»©c há»£p N3</h2>
      <p>Ngá»¯ phÃ¡p N3 bao gá»“m cÃ¡c cáº¥u trÃºc phá»©c táº¡p dÃ¹ng Ä‘á»ƒ diá»…n Ä‘áº¡t nguyÃªn nhÃ¢n, má»¥c Ä‘Ã­ch, vÃ  cÃ¡c má»‘i quan há»‡ logic.</p>
      <h3>CÃ¡c máº«u cÃ¢u quan trá»ng</h3>
      <ul>
        <li>ã€œãŸã‚ã« â€” Ä‘á»ƒ, vÃ¬ má»¥c Ä‘Ã­ch</li>
        <li>ã€œã«ã‚ˆã£ã¦ â€” theo, báº±ng cÃ¡ch</li>
        <li>ã€œã«å¯¾ã—ã¦ â€” Ä‘á»‘i vá»›i</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: 9, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 30, structure: 'ã€œãŸã‚ã«', formula: 'Äá»™ng tá»« tá»« Ä‘iá»ƒn / Danh tá»« ã® + ãŸã‚ã«', meaning: 'Äá»ƒ (má»¥c Ä‘Ã­ch)', exampleJp: 'æ—¥æœ¬èªžã‚’è©±ã™ãŸã‚ã«ã€æ¯Žæ—¥ç·´ç¿’ã—ã¾ã™ã€‚', exampleVi: 'Äá»ƒ nÃ³i tiáº¿ng Nháº­t, tÃ´i luyá»‡n táº­p má»—i ngÃ y.' },
      { grammarId: 31, structure: 'ã€œã«ã‚ˆã£ã¦', formula: 'Danh tá»« + ã«ã‚ˆã£ã¦', meaning: 'Theo / TÃ¹y theo / Báº±ng cÃ¡ch', exampleJp: 'äººã«ã‚ˆã£ã¦æ„è¦‹ãŒé•ã„ã¾ã™ã€‚', exampleVi: 'TÃ¹y ngÆ°á»i mÃ  Ã½ kiáº¿n khÃ¡c nhau.' },
    ],
  },
  4: {
    lessonId: 4, title: 'Ngá»¯ phÃ¡p N5', jlptLevel: 'N5', lessonType: 'GRAMMAR',
    estimatedMinutes: 20, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Ngá»¯ phÃ¡p cÆ¡ báº£n N5</h2><p>CÃ¡c máº«u cÃ¢u ná»n táº£ng giÃºp báº¡n giao tiáº¿p trong cÃ¡c tÃ¬nh huá»‘ng Ä‘Æ¡n giáº£n hÃ ng ngÃ y.</p><h3>Máº«u cÃ¢u cÆ¡ báº£n</h3><ul><li>ã€œã¯ã€œã§ã™ â€” A lÃ  B</li><li>ã€œãŒå¥½ãã§ã™ â€” TÃ´i thÃ­ch...</li><li>ã€œã¾ã—ã‚‡ã† â€” HÃ£y cÃ¹ng...</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 3, nextLessonId: 5,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 3, structure: 'ã€œãŒå¥½ãã§ã™', formula: 'Danh tá»« + ãŒå¥½ãã§ã™', meaning: 'ThÃ­ch...', exampleJp: 'éŸ³æ¥½ãŒå¥½ãã§ã™ã€‚', exampleVi: 'TÃ´i thÃ­ch Ã¢m nháº¡c.' },
      { grammarId: 4, structure: 'ã€œã¾ã—ã‚‡ã†', formula: 'Äá»™ng tá»« ã¾ã™å½¢ (bá» ã¾ã™) + ã¾ã—ã‚‡ã†', meaning: 'HÃ£y cÃ¹ng lÃ m...', exampleJp: 'ä¸€ç·’ã«é£Ÿã¹ã¾ã—ã‚‡ã†ã€‚', exampleVi: 'HÃ£y cÃ¹ng nhau Äƒn nÃ o.' },
      { grammarId: 5, structure: 'ã€œãŸã„', formula: 'Äá»™ng tá»« ã¾ã™å½¢ (bá» ã¾ã™) + ãŸã„', meaning: 'Muá»‘n lÃ m...', exampleJp: 'æ—¥æœ¬ã¸è¡ŒããŸã„ã§ã™ã€‚', exampleVi: 'TÃ´i muá»‘n Ä‘i Nháº­t.' },
    ],
  },
  5: {
    lessonId: 5, title: 'Kanji N5', jlptLevel: 'N5', lessonType: 'KANJI',
    estimatedMinutes: 25, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>103 Kanji N5 theo bá»™ thá»§</h2><p>Kanji N5 lÃ  nhá»¯ng chá»¯ HÃ¡n cÆ¡ báº£n nháº¥t, thÆ°á»ng gáº·p trong cuá»™c sá»‘ng hÃ ng ngÃ y.</p><h3>NhÃ³m thiÃªn nhiÃªn</h3><ul><li>æ—¥ (nháº­t) â€” ngÃ y, máº·t trá»i</li><li>æœˆ (nguyá»‡t) â€” thÃ¡ng, máº·t trÄƒng</li><li>å±± (sÆ¡n) â€” nÃºi</li><li>å· (xuyÃªn) â€” sÃ´ng</li></ul><h3>NhÃ³m sá»‘ Ä‘áº¿m</h3><ul><li>ä¸€äºŒä¸‰å››äº”å…­ä¸ƒå…«ä¹å</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 4, nextLessonId: 17,
    vocabulary: [
      { vocabId: 25, word: 'æ—¥æœ¬èªž', reading: 'nihongo', meaning: 'Tiáº¿ng Nháº­t', exampleSentence: 'æ—¥æœ¬èªžã‚’å‹‰å¼·ã—ã¦ã„ã¾ã™ã€‚', exampleTranslation: 'TÃ´i Ä‘ang há»c tiáº¿ng Nháº­t.' },
      { vocabId: 26, word: 'å±±ç”°ã•ã‚“', reading: 'Yamada-san', meaning: 'Anh/chá»‹ Yamada', exampleSentence: 'å±±ç”°ã•ã‚“ã¯ã©ã“ã§ã™ã‹ï¼Ÿ', exampleTranslation: 'Anh Yamada Ä‘ang á»Ÿ Ä‘Ã¢u?' },
    ],
    grammarPoints: [],
  },
  8: {
    lessonId: 8, title: 'Kanji N4', jlptLevel: 'N4', lessonType: 'KANJI',
    estimatedMinutes: 30, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>181 Kanji N4 theo chá»§ Ä‘á»</h2><p>Kanji N4 bao gá»“m cÃ¡c chá»¯ HÃ¡n thÆ°á»ng dÃ¹ng trong vÄƒn báº£n giao tiáº¿p vÃ  mÃ´i trÆ°á»ng lÃ m viá»‡c.</p><h3>NhÃ³m gia Ä‘Ã¬nh</h3><ul><li>çˆ¶ (phá»¥) â€” cha</li><li>æ¯ (máº«u) â€” máº¹</li><li>å…„ (huynh) â€” anh trai</li><li>å§‰ (tá»·) â€” chá»‹ gÃ¡i</li></ul><h3>NhÃ³m xÃ£ há»™i</h3><ul><li>ä¼šç¤¾ (kaisha) â€” cÃ´ng ty</li><li>ç¤¾å“¡ (shain) â€” nhÃ¢n viÃªn</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 7, nextLessonId: 19,
    vocabulary: [
      { vocabId: 35, word: 'ä¼šç¤¾å“¡', reading: 'kaishain', meaning: 'NhÃ¢n viÃªn cÃ´ng ty', exampleSentence: 'ç§ã¯ä¼šç¤¾å“¡ã§ã™ã€‚', exampleTranslation: 'TÃ´i lÃ  nhÃ¢n viÃªn cÃ´ng ty.' },
    ],
    grammarPoints: [],
  },
  11: {
    lessonId: 11, title: 'Äá»c hiá»ƒu N3', jlptLevel: 'N3', lessonType: 'READING',
    estimatedMinutes: 35, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>PhÃ¢n tÃ­ch vÄƒn báº£n tin tá»©c & truyá»‡n ngáº¯n N3</h2><p>Äá»c hiá»ƒu N3 yÃªu cáº§u hiá»ƒu ná»™i dung chÃ­nh, Ã½ kiáº¿n cá»§a tÃ¡c giáº£ vÃ  chi tiáº¿t cá»¥ thá»ƒ trong cÃ¡c Ä‘oáº¡n vÄƒn thá»±c táº¿.</p><h3>Chiáº¿n lÆ°á»£c Ä‘á»c hiá»ƒu</h3><ul><li>Äá»c cÃ¢u há»i trÆ°á»›c Ä‘á»ƒ Ä‘á»‹nh hÆ°á»›ng tÃ¬m thÃ´ng tin</li><li>ChÃº Ã½ cÃ¡c tá»« ná»‘i nhÆ° ã§ã‚‚ã€ã—ã‹ã—ã€ã ã‹ã‚‰</li><li>TÃ¬m cÃ¢u chá»§ Ä‘á» á»Ÿ Ä‘áº§u hoáº·c cuá»‘i Ä‘oáº¡n vÄƒn</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 10, nextLessonId: 21,
    vocabulary: [
      { vocabId: 42, word: 'ã—ã‹ã—', reading: 'shikashi', meaning: 'NhÆ°ng, tuy nhiÃªn', exampleSentence: 'é›¨ãŒé™ã£ãŸã€‚ã—ã‹ã—ã€è©¦åˆã¯ç¶šã„ãŸã€‚', exampleTranslation: 'Trá»i mÆ°a. Tuy nhiÃªn, tráº­n Ä‘áº¥u váº«n tiáº¿p tá»¥c.' },
    ],
    grammarPoints: [],
  },
  13: {
    lessonId: 13, title: 'Ngá»¯ phÃ¡p N2', jlptLevel: 'N2', lessonType: 'GRAMMAR',
    estimatedMinutes: 40, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Cáº¥u trÃºc cÃ¢u nÃ¢ng cao N2</h2><p>Ngá»¯ phÃ¡p N2 bao gá»“m cÃ¡c máº«u cÃ¢u há»c thuáº­t vÃ  lá»‹ch sá»± ngá»¯ thÆ°á»ng tháº¥y trong bÃ¡o chÃ­, vÄƒn báº£n chÃ­nh thá»©c.</p><h3>CÃ¡c máº«u cÃ¢u nÃ¢ng cao</h3><ul><li>ã€œã«ã‚‚ã‹ã‹ã‚ã‚‰ãš â€” máº·c dÃ¹, báº¥t cháº¥p</li><li>ã€œã«éš›ã—ã¦ â€” vÃ o dá»‹p, nhÃ¢n dá»‹p</li><li>ã€œã«ã‚ãŸã£ã¦ â€” khi, vÃ o lÃºc</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 12, nextLessonId: 14,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 32, structure: 'ã€œã«ã‚‚ã‹ã‹ã‚ã‚‰ãš', formula: 'Äá»™ng tá»« / Danh tá»« + ã«ã‚‚ã‹ã‹ã‚ã‚‰ãš', meaning: 'Máº·c dÃ¹, báº¥t cháº¥p', exampleJp: 'é›¨ã«ã‚‚ã‹ã‹ã‚ã‚‰ãšã€å½¼ã¯æ¥ãŸã€‚', exampleVi: 'Máº·c dÃ¹ trá»i mÆ°a, anh áº¥y váº«n Ä‘áº¿n.' },
      { grammarId: 33, structure: 'ã€œã«éš›ã—ã¦', formula: 'Danh tá»« / Äá»™ng tá»« tá»« Ä‘iá»ƒn + ã«éš›ã—ã¦', meaning: 'VÃ o dá»‹p, nhÃ¢n dá»‹p', exampleJp: 'å…¥å­¦ã«éš›ã—ã¦ã€æº–å‚™ã‚’ã™ã‚‹ã€‚', exampleVi: 'NhÃ¢n dá»‹p nháº­p há»c, cáº§n chuáº©n bá»‹.' },
    ],
  },
  14: {
    lessonId: 14, title: 'Äá»c hiá»ƒu N2', jlptLevel: 'N2', lessonType: 'READING',
    estimatedMinutes: 40, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>BÃ¡o chÃ­, luáº­n vÄƒn & vÄƒn báº£n chÃ­nh thá»©c N2</h2><p>Äá»c hiá»ƒu N2 yÃªu cáº§u phÃ¢n tÃ­ch vÄƒn báº£n phá»©c táº¡p, nháº­n biáº¿t láº­p luáº­n vÃ  Ä‘Ã¡nh giÃ¡ quan Ä‘iá»ƒm tÃ¡c giáº£.</p><h3>Loáº¡i vÄƒn báº£n thÆ°á»ng gáº·p</h3><ul><li>BÃ i bÃ¡o tin tá»©c (ãƒ‹ãƒ¥ãƒ¼ã‚¹è¨˜äº‹)</li><li>BÃ i luáº­n Ã½ kiáº¿n (æ„è¦‹æ–‡)</li><li>ThÃ´ng bÃ¡o chÃ­nh thá»©c (å…¬å‘Š)</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 13, nextLessonId: 23,
    vocabulary: [
      { vocabId: 52, word: 'è«–ã˜ã‚‹', reading: 'ronjiru', meaning: 'Láº­p luáº­n, bÃ n luáº­n', exampleSentence: 'å•é¡Œã«ã¤ã„ã¦è«–ã˜ã¾ã—ãŸã€‚', exampleTranslation: 'ChÃºng tÃ´i Ä‘Ã£ bÃ n luáº­n vá» váº¥n Ä‘á» Ä‘Ã³.' },
    ],
    grammarPoints: [],
  },
  16: {
    lessonId: 16, title: 'Ngá»¯ phÃ¡p N1', jlptLevel: 'N1', lessonType: 'GRAMMAR',
    estimatedMinutes: 50, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>CÃº phÃ¡p vÄƒn há»c, há»c thuáº­t & cá»• ngá»¯ N1</h2><p>Ngá»¯ phÃ¡p N1 bao gá»“m cÃ¡c cáº¥u trÃºc cá»• Ä‘iá»ƒn, vÄƒn há»c vÃ  há»c thuáº­t á»Ÿ má»©c Ä‘á»™ báº£n ngá»¯.</p><h3>Cá»• ngá»¯ & vÄƒn há»c</h3><ul><li>ã€œã¹ã‹ã‚‰ãš â€” khÃ´ng Ä‘Æ°á»£c (cáº¥m Ä‘oÃ¡n cá»• Ä‘iá»ƒn)</li><li>ã€œãšã—ã¦ â€” khÃ´ng lÃ m mÃ  váº«n...</li><li>ã€œã«ã»ã‹ãªã‚‰ãªã„ â€” khÃ´ng gÃ¬ khÃ¡c ngoÃ i...</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 15, nextLessonId: 25,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 40, structure: 'ã€œã«ã»ã‹ãªã‚‰ãªã„', formula: 'Danh tá»« / Äá»™ng tá»« + ã«ã»ã‹ãªã‚‰ãªã„', meaning: 'KhÃ´ng gÃ¬ khÃ¡c ngoÃ i, chÃ­nh lÃ ', exampleJp: 'ã“ã‚Œã¯åŠªåŠ›ã®çµæžœã«ã»ã‹ãªã‚‰ãªã„ã€‚', exampleVi: 'ÄÃ¢y khÃ´ng gÃ¬ khÃ¡c chÃ­nh lÃ  káº¿t quáº£ cá»§a sá»± ná»— lá»±c.' },
    ],
  },
  17: {
    lessonId: 17, title: 'Äá»c hiá»ƒu N5', jlptLevel: 'N5', lessonType: 'READING',
    estimatedMinutes: 15, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>BÃ i Ä‘á»c ngáº¯n & cÃ¢u há»i hiá»ƒu ná»™i dung N5</h2><p>Äá»c hiá»ƒu N5 bao gá»“m cÃ¡c Ä‘oáº¡n vÄƒn ngáº¯n vá» chá»§ Ä‘á» quen thuá»™c nhÆ° thá»i tiáº¿t, gia Ä‘Ã¬nh, thá»©c Äƒn.</p><h3>BÃ i Ä‘á»c máº«u</h3><p>ã‚ãŸã—ã¯ ã¾ã„ã«ã¡ ã‚ã• 7ã˜ã« ãŠãã¾ã™ã€‚ãã‚Œã‹ã‚‰ ã‚·ãƒ£ãƒ¯ãƒ¼ã‚’ ã‚ã³ã¦ã€ãƒ‘ãƒ³ã‚’ ãŸã¹ã¾ã™ã€‚8ã˜ã« ãŒã£ã“ã†ã¸ ã„ãã¾ã™ã€‚</p><p><strong>CÃ¢u há»i:</strong> NgÆ°á»i nÃ y Ä‘áº¿n trÆ°á»ng lÃºc máº¥y giá»?</p>`,
    audioUrl: null, imageUrl: null, prevLessonId: 5, nextLessonId: 18,
    vocabulary: [
      { vocabId: 27, word: 'ã¾ã„ã«ã¡', reading: 'mainichi', meaning: 'HÃ ng ngÃ y, má»—i ngÃ y', exampleSentence: 'ã¾ã„ã«ã¡ æ—¥æœ¬èªžã‚’ å‹‰å¼·ã—ã¾ã™ã€‚', exampleTranslation: 'TÃ´i há»c tiáº¿ng Nháº­t hÃ ng ngÃ y.' },
    ],
    grammarPoints: [],
  },
  18: {
    lessonId: 18, title: 'Nghe hiá»ƒu N5', jlptLevel: 'N5', lessonType: 'LISTENING',
    estimatedMinutes: 15, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Há»™i thoáº¡i & cÃ¢u há»i nghe hiá»ƒu cÆ¡ báº£n N5</h2><p>Nghe hiá»ƒu N5 bao gá»“m cÃ¡c Ä‘oáº¡n há»™i thoáº¡i ngáº¯n trong tÃ¬nh huá»‘ng hÃ ng ngÃ y â€” mua sáº¯m, há»i Ä‘Æ°á»ng, tá»± giá»›i thiá»‡u.</p><h3>Chá»§ Ä‘á» nghe hiá»ƒu N5</h3><ul><li>Giá»›i thiá»‡u báº£n thÃ¢n</li><li>Há»i vÃ  chá»‰ Ä‘Æ°á»ng Ä‘Æ¡n giáº£n</li><li>Mua Ä‘á»“ táº¡i cá»­a hÃ ng</li><li>Há»i vá» thá»i gian vÃ  Ä‘á»‹a Ä‘iá»ƒm</li></ul><p>ðŸ’¡ <strong>Máº¹o:</strong> Nghe 2-3 láº§n trÆ°á»›c khi tráº£ lá»i cÃ¢u há»i.</p>`,
    audioUrl: null, imageUrl: null, prevLessonId: 17, nextLessonId: null,
    vocabulary: [
      { vocabId: 28, word: 'ã™ã¿ã¾ã›ã‚“', reading: 'sumimasen', meaning: 'Xin lá»—i / Cho há»i', exampleSentence: 'ã™ã¿ã¾ã›ã‚“ã€é§…ã¯ã©ã“ã§ã™ã‹ï¼Ÿ', exampleTranslation: 'Xin lá»—i, ga tÃ u á»Ÿ Ä‘Ã¢u áº¡?' },
    ],
    grammarPoints: [],
  },
  19: {
    lessonId: 19, title: 'Äá»c hiá»ƒu N4', jlptLevel: 'N4', lessonType: 'READING',
    estimatedMinutes: 25, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Äoáº¡n vÄƒn thá»±c táº¿ & cÃ¢u há»i hiá»ƒu ná»™i dung N4</h2><p>Äá»c hiá»ƒu N4 gá»“m cÃ¡c Ä‘oáº¡n vÄƒn vá» cuá»™c sá»‘ng cÃ´ng sá»Ÿ, thÆ° cÃ¡ nhÃ¢n vÃ  thÃ´ng bÃ¡o ngáº¯n.</p><h3>Loáº¡i bÃ i Ä‘á»c N4</h3><ul><li>Email/thÆ° ngáº¯n (ãƒ¡ãƒ¼ãƒ«ãƒ»æ‰‹ç´™)</li><li>ThÃ´ng bÃ¡o nÆ¡i lÃ m viá»‡c</li><li>BÃ i viáº¿t blog Ä‘Æ¡n giáº£n</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 8, nextLessonId: 20,
    vocabulary: [
      { vocabId: 36, word: 'é€£çµ¡', reading: 'renraku', meaning: 'LiÃªn láº¡c, thÃ´ng bÃ¡o', exampleSentence: 'å¾Œã§é€£çµ¡ã—ã¾ã™ã€‚', exampleTranslation: 'TÃ´i sáº½ liÃªn láº¡c sau.' },
    ],
    grammarPoints: [],
  },
  20: {
    lessonId: 20, title: 'Nghe hiá»ƒu N4', jlptLevel: 'N4', lessonType: 'LISTENING',
    estimatedMinutes: 25, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Há»™i thoáº¡i hÃ ng ngÃ y & cÃ¢u há»i nghe hiá»ƒu N4</h2><p>Nghe hiá»ƒu N4 bao gá»“m há»™i thoáº¡i vá» cÃ´ng viá»‡c, cuá»™c sá»‘ng xÃ£ há»™i vÃ  cÃ¡c tÃ¬nh huá»‘ng phá»• biáº¿n hÆ¡n.</p><h3>Chá»§ Ä‘á» nghe hiá»ƒu N4</h3><ul><li>Há»™i thoáº¡i táº¡i nÆ¡i lÃ m viá»‡c</li><li>Cuá»™c trÃ² chuyá»‡n vá» káº¿ hoáº¡ch</li><li>ThÃ´ng bÃ¡o táº¡i ga, siÃªu thá»‹</li><li>Phá»ng váº¥n ngáº¯n</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 19, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [],
  },
  21: {
    lessonId: 21, title: 'Kanji N3', jlptLevel: 'N3', lessonType: 'KANJI',
    estimatedMinutes: 35, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>367 Kanji N3 theo nhÃ³m ngá»¯ nghÄ©a</h2><p>Kanji N3 bao gá»“m cÃ¡c chá»¯ HÃ¡n trung cáº¥p thÆ°á»ng xuáº¥t hiá»‡n trong bÃ¡o chÃ­, sÃ¡ch giÃ¡o khoa vÃ  vÄƒn báº£n xÃ£ há»™i.</p><h3>NhÃ³m cáº£m xÃºc & tÃ¢m lÃ½</h3><ul><li>æ„Ÿ (cáº£m) â€” cáº£m giÃ¡c</li><li>æƒ… (tÃ¬nh) â€” tÃ¬nh cáº£m</li><li>æ‚² (bi) â€” buá»“n</li><li>å–œ (há»·) â€” vui má»«ng</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 11, nextLessonId: 22,
    vocabulary: [
      { vocabId: 43, word: 'æ„Ÿæƒ…', reading: 'kanjÅ', meaning: 'Cáº£m xÃºc, tÃ¬nh cáº£m', exampleSentence: 'æ„Ÿæƒ…ã‚’ã‚³ãƒ³ãƒˆãƒ­ãƒ¼ãƒ«ã™ã‚‹ã€‚', exampleTranslation: 'Kiá»ƒm soÃ¡t cáº£m xÃºc cá»§a mÃ¬nh.' },
    ],
    grammarPoints: [],
  },
  22: {
    lessonId: 22, title: 'Nghe hiá»ƒu N3', jlptLevel: 'N3', lessonType: 'LISTENING',
    estimatedMinutes: 35, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Há»™i thoáº¡i phá»©c táº¡p & tÃ¬nh huá»‘ng thá»±c táº¿ N3</h2><p>Nghe hiá»ƒu N3 bao gá»“m cÃ¡c cuá»™c há»™i thoáº¡i vÃ  bÃ i phÃ¡t biá»ƒu trong cÃ¡c tÃ¬nh huá»‘ng xÃ£ há»™i Ä‘a dáº¡ng.</p><h3>Chá»§ Ä‘á» nghe hiá»ƒu N3</h3><ul><li>Tranh luáº­n & Ã½ kiáº¿n cÃ¡ nhÃ¢n</li><li>Tin tá»©c ngáº¯n trÃªn radio</li><li>Há»™i thoáº¡i trong bá»‡nh viá»‡n, ngÃ¢n hÃ ng</li><li>MÃ´ táº£ sá»± kiá»‡n & cÃ¢u chuyá»‡n</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 21, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [],
  },
  23: {
    lessonId: 23, title: 'Kanji N2', jlptLevel: 'N2', lessonType: 'KANJI',
    estimatedMinutes: 45, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>367 Kanji N2 theo lÄ©nh vá»±c há»c thuáº­t</h2><p>Kanji N2 táº­p trung vÃ o cÃ¡c chá»¯ HÃ¡n trong vÄƒn báº£n há»c thuáº­t, khoa há»c vÃ  mÃ´i trÆ°á»ng chuyÃªn nghiá»‡p.</p><h3>NhÃ³m khoa há»c & há»c thuáº­t</h3><ul><li>ç ” (nghiÃªn) â€” nghiÃªn cá»©u</li><li>ç©¶ (cá»©u) â€” tÃ¬m hiá»ƒu</li><li>è«– (luáº­n) â€” lÃ½ luáº­n</li><li>è¨¼ (chá»©ng) â€” báº±ng chá»©ng</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 14, nextLessonId: 24,
    vocabulary: [
      { vocabId: 53, word: 'ç ”ç©¶è€…', reading: 'kenkyÅ«sha', meaning: 'NhÃ  nghiÃªn cá»©u', exampleSentence: 'å½¼ã¯è‘—åãªç ”ç©¶è€…ã§ã™ã€‚', exampleTranslation: 'Ã”ng áº¥y lÃ  nhÃ  nghiÃªn cá»©u ná»•i tiáº¿ng.' },
    ],
    grammarPoints: [],
  },
  24: {
    lessonId: 24, title: 'Nghe hiá»ƒu N2', jlptLevel: 'N2', lessonType: 'LISTENING',
    estimatedMinutes: 45, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>BÃ i giáº£ng, phá»ng váº¥n & ná»™i dung há»c thuáº­t N2</h2><p>Nghe hiá»ƒu N2 yÃªu cáº§u hiá»ƒu cÃ¡c bÃ i giáº£ng dÃ i, phá»ng váº¥n chuyÃªn gia vÃ  ná»™i dung thÃ´ng tin phá»©c táº¡p.</p><h3>Chá»§ Ä‘á» nghe hiá»ƒu N2</h3><ul><li>BÃ i giáº£ng Ä‘áº¡i há»c ngáº¯n</li><li>Phá»ng váº¥n chuyÃªn gia</li><li>ChÆ°Æ¡ng trÃ¬nh radio/podcast thá»±c táº¿</li><li>Tháº£o luáº­n nhÃ³m chuyÃªn nghiá»‡p</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 23, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [],
  },
  25: {
    lessonId: 25, title: 'Äá»c hiá»ƒu N1', jlptLevel: 'N1', lessonType: 'READING',
    estimatedMinutes: 55, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>VÄƒn báº£n há»c thuáº­t, vÄƒn há»c & triáº¿t há»c N1</h2><p>Äá»c hiá»ƒu N1 lÃ  thá»­ thÃ¡ch cao nháº¥t â€” bao gá»“m cÃ¡c vÄƒn báº£n triáº¿t há»c, khoa há»c xÃ£ há»™i vÃ  vÄƒn há»c tiÃªu biá»ƒu.</p><h3>Loáº¡i vÄƒn báº£n N1</h3><ul><li>BÃ i bÃ¡o há»c thuáº­t (è«–æ–‡)</li><li>Tiá»ƒu luáº­n vÄƒn há»c (éšç­†)</li><li>PhÃ¢n tÃ­ch triáº¿t há»c (å“²å­¦çš„è€ƒå¯Ÿ)</li><li>BÃ¬nh luáº­n xÃ£ há»™i (ç¤¾ä¼šè©•è«–)</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 16, nextLessonId: 26,
    vocabulary: [
      { vocabId: 62, word: 'è€ƒå¯Ÿ', reading: 'kÅsatsu', meaning: 'Xem xÃ©t, nghiÃªn cá»©u', exampleSentence: 'ã“ã®å•é¡Œã«ã¤ã„ã¦è€ƒå¯Ÿã™ã‚‹ã€‚', exampleTranslation: 'Xem xÃ©t váº¥n Ä‘á» nÃ y.' },
    ],
    grammarPoints: [],
  },
  26: {
    lessonId: 26, title: 'Kanji N1', jlptLevel: 'N1', lessonType: 'KANJI',
    estimatedMinutes: 60, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>1.162 Kanji N1 â€” bao gá»“m kanji hiáº¿m gáº·p</h2><p>Kanji N1 bao gá»“m cÃ¡c chá»¯ HÃ¡n hiáº¿m gáº·p, kanji trong tÃªn Ä‘á»‹a danh, tÃªn ngÆ°á»i vÃ  vÄƒn báº£n cá»• Ä‘iá»ƒn.</p><h3>NhÃ³m kanji hiáº¿m gáº·p</h3><ul><li>é¬± (uáº¥t) â€” u uáº¥t, bá»±c bá»™i</li><li>éº’ (ká»³) â€” ká»³ lÃ¢n</li><li>çº (triá»n) â€” cuá»‘n quanh</li></ul><p>ðŸ’¡ <strong>LÆ°u Ã½:</strong> Táº­p trung vÃ o Ã¢m Ä‘á»c vÃ  ngá»¯ cáº£nh sá»­ dá»¥ng thay vÃ¬ há»c thuá»™c tá»«ng nÃ©t.</p>`,
    audioUrl: null, imageUrl: null, prevLessonId: 25, nextLessonId: 27,
    vocabulary: [],
    grammarPoints: [],
  },
  27: {
    lessonId: 27, title: 'Nghe hiá»ƒu N1', jlptLevel: 'N1', lessonType: 'LISTENING',
    estimatedMinutes: 60, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>BÃ i phÃ¡t biá»ƒu, tranh luáº­n & ná»™i dung phá»©c táº¡p N1</h2><p>Nghe hiá»ƒu N1 yÃªu cáº§u hiá»ƒu cÃ¡c bÃ i diá»…n thuyáº¿t phá»©c táº¡p, tranh luáº­n há»c thuáº­t vÃ  ná»™i dung vÄƒn hÃ³a sÃ¢u sáº¯c.</p><h3>Chá»§ Ä‘á» nghe hiá»ƒu N1</h3><ul><li>Diá»…n thuyáº¿t chÃ­nh thá»©c & chÃ­nh trá»‹</li><li>Tranh luáº­n triáº¿t há»c & khoa há»c</li><li>Phim tÃ i liá»‡u & chÆ°Æ¡ng trÃ¬nh vÄƒn hÃ³a</li><li>Há»™i nghá»‹ chuyÃªn ngÃ nh</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 26, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [],
  },
  12: {
    lessonId: 12,
    title: 'Tá»« vá»±ng N2',
    jlptLevel: 'N2',
    lessonType: 'VOCAB',
    estimatedMinutes: 40,
    progressPercent: 5,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>6.000 tá»« vá»±ng N2 cao cáº¥p & há»c thuáº­t</h2>
      <p>N2 yÃªu cáº§u vá»‘n tá»« vá»±ng há»c thuáº­t vÃ  chuyÃªn ngÃ nh, phÃ¹ há»£p vá»›i mÃ´i trÆ°á»ng lÃ m viá»‡c vÃ  há»c táº­p chuyÃªn sÃ¢u.</p>
      <h3>Chá»§ Ä‘á»: Há»c thuáº­t & NghiÃªn cá»©u</h3>
      <ul>
        <li>ç ”ç©¶ (kenkyÅ«) â€” nghiÃªn cá»©u</li>
        <li>è«–æ–‡ (ronbun) â€” luáº­n vÄƒn, bÃ i bÃ¡o</li>
        <li>å®Ÿé¨“ (jikken) â€” thÃ­ nghiá»‡m</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 13,
    vocabulary: [
      { vocabId: 50, word: 'ç ”ç©¶', reading: 'kenkyÅ«', meaning: 'NghiÃªn cá»©u', exampleSentence: 'æ–°ã—ã„ç ”ç©¶ã‚’å§‹ã‚ã¾ã—ãŸã€‚', exampleTranslation: 'TÃ´i Ä‘Ã£ báº¯t Ä‘áº§u nghiÃªn cá»©u má»›i.' },
      { vocabId: 51, word: 'è«–æ–‡', reading: 'ronbun', meaning: 'Luáº­n vÄƒn / BÃ i bÃ¡o', exampleSentence: 'è«–æ–‡ã‚’æ›¸ã„ã¦ã„ã¾ã™ã€‚', exampleTranslation: 'TÃ´i Ä‘ang viáº¿t luáº­n vÄƒn.' },
    ],
    grammarPoints: [],
  },
  15: {
    lessonId: 15,
    title: 'Tá»« vá»±ng N1',
    jlptLevel: 'N1',
    lessonType: 'VOCAB',
    estimatedMinutes: 50,
    progressPercent: 2,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>10.000 tá»« vá»±ng N1 & thÃ nh ngá»¯ báº£n ngá»¯</h2>
      <p>N1 lÃ  cáº¥p Ä‘á»™ cao nháº¥t, yÃªu cáº§u hiá»ƒu cÃ¡c vÄƒn báº£n phá»©c táº¡p, ngÃ´n ngá»¯ vÄƒn há»c vÃ  thÃ nh ngá»¯ nhÆ° ngÆ°á»i báº£n ngá»¯.</p>
      <h3>ThÃ nh ngá»¯ phá»• biáº¿n</h3>
      <ul>
        <li>çŒ«ã®æ‰‹ã‚‚å€Ÿã‚ŠãŸã„ â€” Báº­n Ä‘áº¿n má»©c muá»‘n mÆ°á»£n cáº£ tay mÃ¨o (cá»±c ká»³ báº­n)</li>
        <li>ä¸ƒè»¢ã³å…«èµ·ã â€” Tháº¥t báº¡i báº£y láº§n Ä‘á»©ng dáº­y tÃ¡m láº§n (kiÃªn trÃ¬)</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 16,
    vocabulary: [
      { vocabId: 60, word: 'æ¨¡ç´¢', reading: 'mosaku', meaning: 'MÃ² máº«m, tÃ¬m kiáº¿m', exampleSentence: 'è§£æ±ºç­–ã‚’æ¨¡ç´¢ã—ã¦ã„ã¾ã™ã€‚', exampleTranslation: 'ChÃºng tÃ´i Ä‘ang tÃ¬m kiáº¿m giáº£i phÃ¡p.' },
      { vocabId: 61, word: 'æ´žå¯Ÿ', reading: 'dÅsatsu', meaning: 'Nháº­n thá»©c sÃ¢u sáº¯c, tháº¥u hiá»ƒu', exampleSentence: 'é‹­ã„æ´žå¯ŸåŠ›ã‚’æŒã£ã¦ã„ã‚‹ã€‚', exampleTranslation: 'Anh áº¥y cÃ³ kháº£ nÄƒng nháº­n thá»©c sáº¯c bÃ©n.' },
    ],
    grammarPoints: [],
  },
};

export const MOCK_LESSON_DETAIL = MOCK_LESSON_DETAIL_MAP[1];

// â”€â”€â”€ Flashcard â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_FLASHCARD_DECKS = [
  { deckName: 'N5 Tá»« vá»±ng', isSystem: true,  totalCards: 120, dueToday: 8,  nextReviewDate: '2026-06-04' },
  { deckName: 'N5 Kanji',   isSystem: true,  totalCards: 80,  dueToday: 3,  nextReviewDate: '2026-06-05' },
  { deckName: 'YÃªu thÃ­ch',  isSystem: false, totalCards: 25,  dueToday: 0,  nextReviewDate: null },
  { deckName: 'Sai nhiá»u',  isSystem: false, totalCards: 12,  dueToday: 12, nextReviewDate: '2026-06-03' },
];

export const MOCK_DECK_CARDS = [
  { flashcardId: 1, frontText: 'ã“ã‚“ã«ã¡ã¯', isDue: true },
  { flashcardId: 2, frontText: 'ã‚ã‚ŠãŒã¨ã†', isDue: true },
  { flashcardId: 3, frontText: 'æ—¥',         isDue: false },
  { flashcardId: 4, frontText: 'æœ¬',         isDue: false },
  { flashcardId: 5, frontText: 'å±±',         isDue: true },
  { flashcardId: 6, frontText: 'å·',         isDue: false },
  { flashcardId: 7, frontText: 'ã™ã¿ã¾ã›ã‚“', isDue: false },
  { flashcardId: 8, frontText: 'å­¦ç”Ÿ',       isDue: true },
  { flashcardId: 9, frontText: 'å…ˆç”Ÿ',       isDue: false },
  { flashcardId: 10, frontText: 'å¤§å­¦',      isDue: true },
];

// â”€â”€â”€ KanjiList â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
const N5_KANJI = [
  { kanjiId: 1,  characterValue: 'æ—¥', meaning: 'ngÃ y, máº·t trá»i',  isCompleted: true  },
  { kanjiId: 2,  characterValue: 'æœˆ', meaning: 'thÃ¡ng, máº·t trÄƒng', isCompleted: true  },
  { kanjiId: 3,  characterValue: 'å±±', meaning: 'nÃºi',              isCompleted: true  },
  { kanjiId: 4,  characterValue: 'å·', meaning: 'sÃ´ng',             isCompleted: false },
  { kanjiId: 5,  characterValue: 'ç”°', meaning: 'ruá»™ng',            isCompleted: false },
  { kanjiId: 6,  characterValue: 'äºº', meaning: 'ngÆ°á»i',            isCompleted: true  },
  { kanjiId: 7,  characterValue: 'å¤§', meaning: 'lá»›n',              isCompleted: false },
  { kanjiId: 8,  characterValue: 'å°', meaning: 'nhá»',              isCompleted: false },
  { kanjiId: 9,  characterValue: 'ä¸­', meaning: 'giá»¯a, trong',      isCompleted: true  },
  { kanjiId: 10, characterValue: 'ä¸Š', meaning: 'trÃªn',             isCompleted: false },
  { kanjiId: 11, characterValue: 'ä¸‹', meaning: 'dÆ°á»›i',             isCompleted: false },
  { kanjiId: 12, characterValue: 'å£', meaning: 'miá»‡ng, cá»­a',       isCompleted: true  },
  { kanjiId: 13, characterValue: 'ç›®', meaning: 'máº¯t',              isCompleted: false },
  { kanjiId: 14, characterValue: 'è€³', meaning: 'tai',              isCompleted: false },
  { kanjiId: 15, characterValue: 'æ‰‹', meaning: 'tay',              isCompleted: true  },
  { kanjiId: 16, characterValue: 'è¶³', meaning: 'chÃ¢n',             isCompleted: false },
  { kanjiId: 17, characterValue: 'æœ¨', meaning: 'cÃ¢y, gá»—',          isCompleted: true  },
  { kanjiId: 18, characterValue: 'æ°´', meaning: 'nÆ°á»›c',             isCompleted: false },
  { kanjiId: 19, characterValue: 'ç«', meaning: 'lá»­a',              isCompleted: false },
  { kanjiId: 20, characterValue: 'åœŸ', meaning: 'Ä‘áº¥t',              isCompleted: false },
  { kanjiId: 21, characterValue: 'é‡‘', meaning: 'vÃ ng, tiá»n',       isCompleted: true  },
  { kanjiId: 22, characterValue: 'ä¸€', meaning: 'má»™t',              isCompleted: true  },
  { kanjiId: 23, characterValue: 'äºŒ', meaning: 'hai',              isCompleted: true  },
  { kanjiId: 24, characterValue: 'ä¸‰', meaning: 'ba',               isCompleted: true  },
  { kanjiId: 25, characterValue: 'å››', meaning: 'bá»‘n',              isCompleted: false },
  { kanjiId: 26, characterValue: 'äº”', meaning: 'nÄƒm',              isCompleted: false },
  { kanjiId: 27, characterValue: 'å…­', meaning: 'sÃ¡u',              isCompleted: false },
  { kanjiId: 28, characterValue: 'ä¸ƒ', meaning: 'báº£y',              isCompleted: false },
  { kanjiId: 29, characterValue: 'å…«', meaning: 'tÃ¡m',              isCompleted: false },
  { kanjiId: 30, characterValue: 'ä¹', meaning: 'chÃ­n',             isCompleted: false },
  { kanjiId: 31, characterValue: 'å', meaning: 'mÆ°á»i',             isCompleted: true  },
  { kanjiId: 32, characterValue: 'ç™¾', meaning: 'trÄƒm',             isCompleted: false },
  { kanjiId: 33, characterValue: 'åƒ', meaning: 'nghÃ¬n',            isCompleted: false },
  { kanjiId: 34, characterValue: 'ä¸‡', meaning: 'váº¡n (mÆ°á»i nghÃ¬n)', isCompleted: false },
  { kanjiId: 35, characterValue: 'å¹´', meaning: 'nÄƒm (thá»i gian)',   isCompleted: true  },
  { kanjiId: 36, characterValue: 'æ™‚', meaning: 'giá», thá»i gian',   isCompleted: false },
  { kanjiId: 37, characterValue: 'åˆ†', meaning: 'phÃºt, chia',       isCompleted: false },
  { kanjiId: 38, characterValue: 'æœ¬', meaning: 'sÃ¡ch, gá»‘c',        isCompleted: true  },
  { kanjiId: 39, characterValue: 'èªž', meaning: 'ngÃ´n ngá»¯',         isCompleted: false },
  { kanjiId: 40, characterValue: 'å­¦', meaning: 'há»c',              isCompleted: true  },
];

const N4_KANJI = [
  { kanjiId: 101, characterValue: 'çˆ¶', meaning: 'cha, bá»‘',          isCompleted: false },
  { kanjiId: 102, characterValue: 'æ¯', meaning: 'máº¹',               isCompleted: false },
  { kanjiId: 103, characterValue: 'å…„', meaning: 'anh trai',          isCompleted: false },
  { kanjiId: 104, characterValue: 'å§‰', meaning: 'chá»‹ gÃ¡i',           isCompleted: false },
  { kanjiId: 105, characterValue: 'å¼Ÿ', meaning: 'em trai',           isCompleted: false },
  { kanjiId: 106, characterValue: 'å¦¹', meaning: 'em gÃ¡i',            isCompleted: false },
  { kanjiId: 107, characterValue: 'å‹', meaning: 'báº¡n bÃ¨',            isCompleted: false },
  { kanjiId: 108, characterValue: 'ä¼š', meaning: 'gáº·p, há»™i, hiá»ƒu',    isCompleted: false },
  { kanjiId: 109, characterValue: 'ç¤¾', meaning: 'cÃ´ng ty, xÃ£ há»™i',   isCompleted: false },
  { kanjiId: 110, characterValue: 'å“¡', meaning: 'thÃ nh viÃªn, nhÃ¢n viÃªn', isCompleted: false },
  { kanjiId: 111, characterValue: 'é•·', meaning: 'dÃ i, trÆ°á»Ÿng',       isCompleted: false },
  { kanjiId: 112, characterValue: 'å¼·', meaning: 'máº¡nh',              isCompleted: false },
  { kanjiId: 113, characterValue: 'å¼±', meaning: 'yáº¿u',               isCompleted: false },
  { kanjiId: 114, characterValue: 'è¿‘', meaning: 'gáº§n',               isCompleted: false },
  { kanjiId: 115, characterValue: 'é ', meaning: 'xa',                isCompleted: false },
  { kanjiId: 116, characterValue: 'é«˜', meaning: 'cao, Ä‘áº¯t',          isCompleted: false },
  { kanjiId: 117, characterValue: 'ä½Ž', meaning: 'tháº¥p',              isCompleted: false },
  { kanjiId: 118, characterValue: 'å¤š', meaning: 'nhiá»u',             isCompleted: false },
  { kanjiId: 119, characterValue: 'å°‘', meaning: 'Ã­t',                isCompleted: false },
  { kanjiId: 120, characterValue: 'æ–°', meaning: 'má»›i',               isCompleted: false },
];

export const MOCK_KANJI_LIST = {
  N5: { kanji: N5_KANJI, completedCount: 18, totalElements: 103 },
  N4: { kanji: N4_KANJI, completedCount: 0,  totalElements: 181 },
  N3: { kanji: [],        completedCount: 0,  totalElements: 367 },
  N2: { kanji: [],        completedCount: 0,  totalElements: 367 },
  N1: { kanji: [],        completedCount: 0,  totalElements: 1162 },
};

// â”€â”€â”€ KanjiPractice â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_KANJI_DETAIL_MAP = {
  1: { kanjiId: 1, characterValue: 'æ—¥', jlptLevel: 'N5', meaning: 'NgÃ y, máº·t trá»i', onyomi: 'ãƒ‹ãƒã€ã‚¸ãƒ„', kunyomi: 'ã²ã€ã‹', strokeCount: 4, strokeOrderUrl: null, exampleWord: 'æ—¥æœ¬èªž', exampleReading: 'ã«ã»ã‚“ã”', exampleMeaning: 'Tiáº¿ng Nháº­t', prevKanjiId: null, nextKanjiId: 2 },
  2: { kanjiId: 2, characterValue: 'æœˆ', jlptLevel: 'N5', meaning: 'ThÃ¡ng, máº·t trÄƒng', onyomi: 'ã‚²ãƒ„ã€ã‚¬ãƒ„', kunyomi: 'ã¤ã', strokeCount: 4, strokeOrderUrl: null, exampleWord: 'æœˆæ›œæ—¥', exampleReading: 'ã’ã¤ã‚ˆã†ã³', exampleMeaning: 'Thá»© Hai', prevKanjiId: 1, nextKanjiId: 3 },
  3: { kanjiId: 3, characterValue: 'å±±', jlptLevel: 'N5', meaning: 'NÃºi', onyomi: 'ã‚µãƒ³', kunyomi: 'ã‚„ã¾', strokeCount: 3, strokeOrderUrl: null, exampleWord: 'å¯Œå£«å±±', exampleReading: 'ãµã˜ã•ã‚“', exampleMeaning: 'NÃºi PhÃº SÄ©', prevKanjiId: 2, nextKanjiId: 4 },
};
export const MOCK_KANJI_DETAIL_DEFAULT = MOCK_KANJI_DETAIL_MAP[1];

// â”€â”€â”€ Review â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_FLASHCARDS_DUE = [
  { flashcardId: 1, frontText: 'ã“ã‚“ã«ã¡ã¯' },
  { flashcardId: 2, frontText: 'æ—¥' },
  { flashcardId: 3, frontText: 'ã‚ã‚ŠãŒã¨ã†' },
  { flashcardId: 4, frontText: 'å±±' },
  { flashcardId: 5, frontText: 'å­¦ç”Ÿ' },
  { flashcardId: 6, frontText: 'å…ˆç”Ÿ' },
];

export const MOCK_BACK_CONTENT_MAP = {
  1: { reading: 'konnichiwa',   meaning: 'Xin chÃ o (buá»•i trÆ°a/chiá»u)', exampleSentence: 'ã“ã‚“ã«ã¡ã¯ã€ç”°ä¸­ã•ã‚“ã€‚' },
  2: { reading: 'ã²ã€ã«ã¡ã€ã‹', meaning: 'NgÃ y, máº·t trá»i',              exampleSentence: 'ä»Šæ—¥ã¯ã„ã„æ—¥ã§ã™ã­ã€‚' },
  3: { reading: 'arigatou',     meaning: 'Cáº£m Æ¡n',                      exampleSentence: 'ã‚ã‚ŠãŒã¨ã†ã”ã–ã„ã¾ã™ã€‚' },
  4: { reading: 'ã‚„ã¾ã€ã•ã‚“',   meaning: 'NÃºi',                          exampleSentence: 'å¯Œå£«å±±ã¯ãã‚Œã„ã§ã™ã€‚' },
  5: { reading: 'ãŒãã›ã„',     meaning: 'Há»c sinh, sinh viÃªn',          exampleSentence: 'ã‚ãŸã—ã¯å­¦ç”Ÿã§ã™ã€‚' },
  6: { reading: 'ã›ã‚“ã›ã„',     meaning: 'GiÃ¡o viÃªn, tháº§y/cÃ´ giÃ¡o',      exampleSentence: 'ç”°ä¸­å…ˆç”Ÿã¯ã‚„ã•ã—ã„ã§ã™ã€‚' },
};

// â”€â”€â”€ Progress â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_STATS = {
  currentStreak:    7,
  longestStreak:    14,
  wordCount:        243,
  lessonsCompleted: 18,
  daysThisMonth:    12,
  radarData: { vocabulary: 65, grammar: 50, reading: 45, listening: 40, kanji: 55 },
  completions: {
    kanji:      { completed: 18,  total: 103 },
    vocabulary: { completed: 243, total: 800 },
    grammar:    { completed: 12,  total: 60  },
    kana:       { completed: 46,  total: 92  },
  },
};

export const MOCK_EXAM_HISTORY = {
  content: [
    { attemptId: 1, assessmentId: 1, assessmentTitle: 'N5 Äá» thi thá»­ #1', jlptLevel: 'N5', score: 72, maxScore: 100, isPassed: true,  attemptedAt: '2026-05-28T09:30:00' },
    { attemptId: 2, assessmentId: 2, assessmentTitle: 'N5 Äá» thi thá»­ #2', jlptLevel: 'N5', score: 58, maxScore: 100, isPassed: false, attemptedAt: '2026-05-20T14:00:00' },
    { attemptId: 3, assessmentId: 1, assessmentTitle: 'N5 Äá» thi thá»­ #1', jlptLevel: 'N5', score: 65, maxScore: 100, isPassed: true,  attemptedAt: '2026-05-15T10:15:00' },
  ],
  totalPages: 1,
};

// â”€â”€â”€ MockTestList â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_EXAM_LIST = {
  N5: {
    content: [
      { assessmentId: 1, title: 'N5 Äá» thi thá»­ #1 â€” Tá»•ng há»£p',           jlptLevel: 'N5', totalQuestions: 100, durationMin: 105, passScore: 65, maxScore: 100, lastAttempt: { attemptedAt: '2026-05-28T09:30:00', score: 72, isPassed: true  } },
      { assessmentId: 2, title: 'N5 Äá» thi thá»­ #2 â€” Trá»ng tÃ¢m tá»« vá»±ng',  jlptLevel: 'N5', totalQuestions: 40,  durationMin: 35,  passScore: 26, maxScore: 40,  lastAttempt: { attemptedAt: '2026-05-20T14:00:00', score: 20, isPassed: false } },
      { assessmentId: 3, title: 'N5 Äá» thi thá»­ #3 â€” Ngá»¯ phÃ¡p & Äá»c hiá»ƒu',jlptLevel: 'N5', totalQuestions: 60,  durationMin: 60,  passScore: 38, maxScore: 60,  lastAttempt: null },
    ],
    totalPages: 1,
  },
  N4: {
    content: [
      { assessmentId: 4, title: 'N4 Äá» thi thá»­ #1 â€” Tá»•ng há»£p', jlptLevel: 'N4', totalQuestions: 100, durationMin: 105, passScore: 65, maxScore: 100, lastAttempt: null },
    ],
    totalPages: 1,
  },
  N3: { content: [], totalPages: 1 },
  N2: { content: [], totalPages: 1 },
  N1: { content: [], totalPages: 1 },
};

// â”€â”€â”€ MockTestAttempt â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_ASSESSMENT_DETAIL = {
  assessmentId: 1,
  title: 'N5 Äá» thi thá»­ #1 â€” Tá»•ng há»£p',
  jlptLevel: 'N5',
  durationMin: 105,
  sections: [
    {
      sectionName: 'Tá»« vá»±ng',
      questions: [
        { questionId: 'q1', questionText: 'æ¯Žæœã€ã‚ãŸã—ã¯ï¼–___ã« ãŠãã¾ã™ã€‚', optionA: 'ã˜', optionB: 'ãµã‚“', optionC: 'ã¾ãˆ', optionD: 'ã”ã‚', audioUrl: null },
        { questionId: 'q2', questionText: 'ã“ã® ã‚Šã‚“ã”ã¯ ___ ã§ã™ã‹ã€‚', optionA: 'ã„ãã‚‰', optionB: 'ã„ã¤', optionC: 'ã©ã“', optionD: 'ã ã‚Œ', audioUrl: null },
        { questionId: 'q3', questionText: 'ãã‚‡ã†ã® ã¦ã‚“ãã¯___ã§ã™ã€‚', optionA: 'ã¯ã‚Œ', optionB: 'ã•ã‹ãª', optionC: 'ã»ã‚“', optionD: 'ãˆã', audioUrl: null },
        { questionId: 'q4', questionText: '___ã§ ã§ã‚“ã—ã‚ƒã« ã®ã‚Šã¾ã™ã€‚', optionA: 'ãˆã', optionB: 'ã¿ã›', optionC: 'ãŒã£ã“ã†', optionD: 'ã†ã¡', audioUrl: null },
      ],
    },
    {
      sectionName: 'Ngá»¯ phÃ¡p',
      questions: [
        { questionId: 'q5', questionText: 'ã‚ãŸã—ã¯ ã¾ã„ã«ã¡ 6ã˜___ãŠãã¾ã™ã€‚', optionA: 'ã«', optionB: 'ã‚’', optionC: 'ãŒ', optionD: 'ã§', audioUrl: null },
        { questionId: 'q6', questionText: 'ã‚ã® ã‹ã°ã‚“___ã‚ãŸã—ã® ã§ã™ã€‚', optionA: 'ã¯', optionB: 'ã‚’', optionC: 'ã«', optionD: 'ã¸', audioUrl: null },
        { questionId: 'q7', questionText: 'ã¨ã—ã‚‡ã‹ã‚“___ã»ã‚“ã‚’ ã‹ã‚Šã¾ã™ã€‚', optionA: 'ã§', optionB: 'ã«', optionC: 'ã‚’', optionD: 'ãŒ', audioUrl: null },
      ],
    },
    {
      sectionName: 'Äá»c hiá»ƒu',
      questions: [
        { questionId: 'q8', questionText: '(Äá»c Ä‘oáº¡n vÄƒn) ãŸãªã‹ã•ã‚“ã¯ ã¾ã„ã«ã¡ ãªã‚“ã˜ã« ãŠãã¾ã™ã‹ã€‚', optionA: '6ã˜', optionB: '7ã˜', optionC: '8ã˜', optionD: '9ã˜', audioUrl: null },
        { questionId: 'q9', questionText: 'ãŸãªã‹ã•ã‚“ã¯ ãªã«ã§ ã‹ã„ã—ã‚ƒã¸ ã„ãã¾ã™ã‹ã€‚', optionA: 'ãƒã‚¹', optionB: 'ã§ã‚“ã—ã‚ƒ', optionC: 'ã˜ã¦ã‚“ã—ã‚ƒ', optionD: 'ã‚ã‚‹ã„ã¦', audioUrl: null },
        { questionId: 'q10', questionText: 'ãŸãªã‹ã•ã‚“ã® ã—ã‚…ã¿ã¯ ãªã‚“ã§ã™ã‹ã€‚', optionA: 'ã‚Šã‚‡ã†ã‚Š', optionB: 'ã©ãã—ã‚‡', optionC: 'ã‚¹ãƒãƒ¼ãƒ„', optionD: 'ãŠã‚“ãŒã', audioUrl: null },
      ],
    },
  ],
};

// â”€â”€â”€ MockTestResults â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const MOCK_QUIZ_RESULT = {
  attemptId: 1,
  assessmentId: 1,
  assessmentTitle: 'N5 Äá» thi thá»­ #1 â€” Tá»•ng há»£p',
  jlptLevel: 'N5',
  score: 72,
  maxScore: 100,
  passScore: 65,
  isPassed: true,
  attemptedAt: '2026-05-28T09:30:00',
  previousAttempt: { score: 65 },
  sectionScores: {
    vocabulary: { percent: 80 },
    grammar:    { percent: 70 },
    reading:    { percent: 65 },
    listening:  { percent: 60 },
  },
  questionResults: [
    { questionNumber: 1,  skill: 'vocabulary', selectedOption: 'A', correctOption: 'A', isCorrect: true  },
    { questionNumber: 2,  skill: 'vocabulary', selectedOption: 'A', correctOption: 'A', isCorrect: true  },
    { questionNumber: 3,  skill: 'vocabulary', selectedOption: 'B', correctOption: 'A', isCorrect: false },
    { questionNumber: 4,  skill: 'vocabulary', selectedOption: 'A', correctOption: 'A', isCorrect: true  },
    { questionNumber: 5,  skill: 'grammar',    selectedOption: 'A', correctOption: 'A', isCorrect: true  },
    { questionNumber: 6,  skill: 'grammar',    selectedOption: 'D', correctOption: 'A', isCorrect: false },
    { questionNumber: 7,  skill: 'grammar',    selectedOption: 'A', correctOption: 'A', isCorrect: true  },
    { questionNumber: 8,  skill: 'reading',    selectedOption: 'B', correctOption: 'B', isCorrect: true  },
    { questionNumber: 9,  skill: 'reading',    selectedOption: 'C', correctOption: 'B', isCorrect: false },
    { questionNumber: 10, skill: 'reading',    selectedOption: null, correctOption: 'B', isCorrect: false },
  ],
};
