// DEMO_MODE mock data — dùng để kiểm tra UI mà không cần backend
// Set DEMO_MODE = false khi kết nối backend thật

export const DEMO_MODE = true;

// ─── LearnNew ───────────────────────────────────────────────────────────────
export const MOCK_NEXT_LESSON = {
  lessonId: 1,
  title: 'Bảng chữ cái Hiragana',
  jlptLevel: 'N5',
  lessonType: 'KANA',
  estimatedMinutes: 15,
  description: 'Học 46 ký tự Hiragana cơ bản — nền tảng để đọc tiếng Nhật.',
  progressPercent: 30,
};

export const MOCK_SUGGESTED_LESSONS = [
  { lessonId: 2, title: 'Từ vựng chào hỏi N5', jlptLevel: 'N5', lessonType: 'VOCAB',   estimatedMinutes: 10, progressPercent: 0 },
  { lessonId: 3, title: 'Ngữ pháp: は と が',  jlptLevel: 'N5', lessonType: 'GRAMMAR', estimatedMinutes: 20, progressPercent: 60 },
  { lessonId: 4, title: 'Kanji số đếm 一二三', jlptLevel: 'N5', lessonType: 'KANJI',   estimatedMinutes: 12, progressPercent: 0 },
];

// ─── LessonDetail ────────────────────────────────────────────────────────────
export const MOCK_LESSON_DETAIL = {
  lessonId: 1,
  title: 'Bảng chữ cái Hiragana',
  jlptLevel: 'N5',
  lessonType: 'KANA',
  estimatedMinutes: 15,
  progressPercent: 30,
  progressStatus: 'in_progress',
  isVipOnly: false,
  isLocked: false,
  contentHtml: `
    <h2>Hiragana là gì?</h2>
    <p>Hiragana (ひらがな) là một trong ba bảng chữ cái tiếng Nhật, gồm <strong>46 ký tự</strong> cơ bản. Mỗi ký tự đại diện cho một âm tiết.</p>
    <h3>Nhóm nguyên âm (あ行)</h3>
    <ul>
      <li>あ (a)　い (i)　う (u)　え (e)　お (o)</li>
    </ul>
    <h3>Nhóm か行</h3>
    <ul>
      <li>か (ka)　き (ki)　く (ku)　け (ke)　こ (ko)</li>
    </ul>
    <h3>Nhóm さ行</h3>
    <ul>
      <li>さ (sa)　し (shi)　す (su)　せ (se)　そ (so)</li>
    </ul>
    <p>💡 <strong>Mẹo học:</strong> Học mỗi ngày 1 hàng (5 ký tự), luyện viết và đọc thành tiếng để nhớ lâu hơn!</p>
  `,
  audioUrl: null,
  imageUrl: null,
  prevLessonId: null,
  nextLessonId: 2,
  vocabulary: [
    {
      vocabId: 1,
      word: 'こんにちは',
      reading: 'konnichiwa',
      meaning: 'Xin chào (buổi trưa/chiều)',
      exampleSentence: 'こんにちは、田中さん。',
      exampleTranslation: 'Xin chào, anh Tanaka.',
    },
    {
      vocabId: 2,
      word: 'ありがとう',
      reading: 'arigatou',
      meaning: 'Cảm ơn',
      exampleSentence: 'ありがとうございます。',
      exampleTranslation: 'Xin cảm ơn rất nhiều.',
    },
    {
      vocabId: 3,
      word: 'すみません',
      reading: 'sumimasen',
      meaning: 'Xin lỗi / Cho hỏi',
      exampleSentence: 'すみません、駅はどこですか？',
      exampleTranslation: 'Xin lỗi, ga tàu ở đâu ạ?',
    },
    {
      vocabId: 4,
      word: 'はじめまして',
      reading: 'hajimemashite',
      meaning: 'Rất vui được gặp bạn',
      exampleSentence: 'はじめまして、リンです。',
      exampleTranslation: 'Rất vui được gặp bạn, tôi là Linh.',
    },
    {
      vocabId: 5,
      word: 'おはようございます',
      reading: 'ohayou gozaimasu',
      meaning: 'Chào buổi sáng (lịch sự)',
      exampleSentence: 'おはようございます、先生。',
      exampleTranslation: 'Chào buổi sáng, thầy giáo.',
    },
  ],
  grammarPoints: [
    {
      grammarId: 1,
      structure: '〜は〜です',
      formula: 'Danh từ + は + Danh từ + です',
      meaning: 'A là B',
      exampleJp: 'わたしは学生です。',
      exampleVi: 'Tôi là học sinh.',
    },
    {
      grammarId: 2,
      structure: '〜じゃありません',
      formula: 'Danh từ + じゃありません',
      meaning: 'Không phải là...',
      exampleJp: 'これはペンじゃありません。',
      exampleVi: 'Đây không phải là bút.',
    },
  ],
};

// ─── Flashcard ────────────────────────────────────────────────────────────────
export const MOCK_FLASHCARD_DECKS = [
  { deckName: 'N5 Từ vựng', isSystem: true,  totalCards: 120, dueToday: 8,  nextReviewDate: '2026-06-04' },
  { deckName: 'N5 Kanji',   isSystem: true,  totalCards: 80,  dueToday: 3,  nextReviewDate: '2026-06-05' },
  { deckName: 'Yêu thích',  isSystem: false, totalCards: 25,  dueToday: 0,  nextReviewDate: null },
  { deckName: 'Sai nhiều',  isSystem: false, totalCards: 12,  dueToday: 12, nextReviewDate: '2026-06-03' },
];

export const MOCK_DECK_CARDS = [
  { flashcardId: 1, frontText: 'こんにちは', isDue: true },
  { flashcardId: 2, frontText: 'ありがとう', isDue: true },
  { flashcardId: 3, frontText: '日',         isDue: false },
  { flashcardId: 4, frontText: '本',         isDue: false },
  { flashcardId: 5, frontText: '山',         isDue: true },
  { flashcardId: 6, frontText: '川',         isDue: false },
  { flashcardId: 7, frontText: 'すみません', isDue: false },
  { flashcardId: 8, frontText: '学生',       isDue: true },
  { flashcardId: 9, frontText: '先生',       isDue: false },
  { flashcardId: 10, frontText: '大学',      isDue: true },
];

// ─── KanjiList ────────────────────────────────────────────────────────────────
const N5_KANJI = [
  { kanjiId: 1,  characterValue: '日', meaning: 'ngày, mặt trời',  isCompleted: true  },
  { kanjiId: 2,  characterValue: '月', meaning: 'tháng, mặt trăng', isCompleted: true  },
  { kanjiId: 3,  characterValue: '山', meaning: 'núi',              isCompleted: true  },
  { kanjiId: 4,  characterValue: '川', meaning: 'sông',             isCompleted: false },
  { kanjiId: 5,  characterValue: '田', meaning: 'ruộng',            isCompleted: false },
  { kanjiId: 6,  characterValue: '人', meaning: 'người',            isCompleted: true  },
  { kanjiId: 7,  characterValue: '大', meaning: 'lớn',              isCompleted: false },
  { kanjiId: 8,  characterValue: '小', meaning: 'nhỏ',              isCompleted: false },
  { kanjiId: 9,  characterValue: '中', meaning: 'giữa, trong',      isCompleted: true  },
  { kanjiId: 10, characterValue: '上', meaning: 'trên',             isCompleted: false },
  { kanjiId: 11, characterValue: '下', meaning: 'dưới',             isCompleted: false },
  { kanjiId: 12, characterValue: '口', meaning: 'miệng, cửa',       isCompleted: true  },
  { kanjiId: 13, characterValue: '目', meaning: 'mắt',              isCompleted: false },
  { kanjiId: 14, characterValue: '耳', meaning: 'tai',              isCompleted: false },
  { kanjiId: 15, characterValue: '手', meaning: 'tay',              isCompleted: true  },
  { kanjiId: 16, characterValue: '足', meaning: 'chân',             isCompleted: false },
  { kanjiId: 17, characterValue: '木', meaning: 'cây, gỗ',          isCompleted: true  },
  { kanjiId: 18, characterValue: '水', meaning: 'nước',             isCompleted: false },
  { kanjiId: 19, characterValue: '火', meaning: 'lửa',              isCompleted: false },
  { kanjiId: 20, characterValue: '土', meaning: 'đất',              isCompleted: false },
  { kanjiId: 21, characterValue: '金', meaning: 'vàng, tiền',       isCompleted: true  },
  { kanjiId: 22, characterValue: '一', meaning: 'một',              isCompleted: true  },
  { kanjiId: 23, characterValue: '二', meaning: 'hai',              isCompleted: true  },
  { kanjiId: 24, characterValue: '三', meaning: 'ba',               isCompleted: true  },
  { kanjiId: 25, characterValue: '四', meaning: 'bốn',              isCompleted: false },
  { kanjiId: 26, characterValue: '五', meaning: 'năm',              isCompleted: false },
  { kanjiId: 27, characterValue: '六', meaning: 'sáu',              isCompleted: false },
  { kanjiId: 28, characterValue: '七', meaning: 'bảy',              isCompleted: false },
  { kanjiId: 29, characterValue: '八', meaning: 'tám',              isCompleted: false },
  { kanjiId: 30, characterValue: '九', meaning: 'chín',             isCompleted: false },
  { kanjiId: 31, characterValue: '十', meaning: 'mười',             isCompleted: true  },
  { kanjiId: 32, characterValue: '百', meaning: 'trăm',             isCompleted: false },
  { kanjiId: 33, characterValue: '千', meaning: 'nghìn',            isCompleted: false },
  { kanjiId: 34, characterValue: '万', meaning: 'vạn (mười nghìn)', isCompleted: false },
  { kanjiId: 35, characterValue: '年', meaning: 'năm (thời gian)',   isCompleted: true  },
  { kanjiId: 36, characterValue: '時', meaning: 'giờ, thời gian',   isCompleted: false },
  { kanjiId: 37, characterValue: '分', meaning: 'phút, chia',       isCompleted: false },
  { kanjiId: 38, characterValue: '本', meaning: 'sách, gốc',        isCompleted: true  },
  { kanjiId: 39, characterValue: '語', meaning: 'ngôn ngữ',         isCompleted: false },
  { kanjiId: 40, characterValue: '学', meaning: 'học',              isCompleted: true  },
];

const N4_KANJI = [
  { kanjiId: 101, characterValue: '父', meaning: 'cha, bố',          isCompleted: false },
  { kanjiId: 102, characterValue: '母', meaning: 'mẹ',               isCompleted: false },
  { kanjiId: 103, characterValue: '兄', meaning: 'anh trai',          isCompleted: false },
  { kanjiId: 104, characterValue: '姉', meaning: 'chị gái',           isCompleted: false },
  { kanjiId: 105, characterValue: '弟', meaning: 'em trai',           isCompleted: false },
  { kanjiId: 106, characterValue: '妹', meaning: 'em gái',            isCompleted: false },
  { kanjiId: 107, characterValue: '友', meaning: 'bạn bè',            isCompleted: false },
  { kanjiId: 108, characterValue: '会', meaning: 'gặp, hội, hiểu',    isCompleted: false },
  { kanjiId: 109, characterValue: '社', meaning: 'công ty, xã hội',   isCompleted: false },
  { kanjiId: 110, characterValue: '員', meaning: 'thành viên, nhân viên', isCompleted: false },
  { kanjiId: 111, characterValue: '長', meaning: 'dài, trưởng',       isCompleted: false },
  { kanjiId: 112, characterValue: '強', meaning: 'mạnh',              isCompleted: false },
  { kanjiId: 113, characterValue: '弱', meaning: 'yếu',               isCompleted: false },
  { kanjiId: 114, characterValue: '近', meaning: 'gần',               isCompleted: false },
  { kanjiId: 115, characterValue: '遠', meaning: 'xa',                isCompleted: false },
  { kanjiId: 116, characterValue: '高', meaning: 'cao, đắt',          isCompleted: false },
  { kanjiId: 117, characterValue: '低', meaning: 'thấp',              isCompleted: false },
  { kanjiId: 118, characterValue: '多', meaning: 'nhiều',             isCompleted: false },
  { kanjiId: 119, characterValue: '少', meaning: 'ít',                isCompleted: false },
  { kanjiId: 120, characterValue: '新', meaning: 'mới',               isCompleted: false },
];

export const MOCK_KANJI_LIST = {
  N5: { kanji: N5_KANJI, completedCount: 18, totalElements: 103 },
  N4: { kanji: N4_KANJI, completedCount: 0,  totalElements: 181 },
  N3: { kanji: [],        completedCount: 0,  totalElements: 367 },
  N2: { kanji: [],        completedCount: 0,  totalElements: 367 },
  N1: { kanji: [],        completedCount: 0,  totalElements: 1162 },
};

// ─── KanjiPractice ───────────────────────────────────────────────────────────
export const MOCK_KANJI_DETAIL_MAP = {
  1: { kanjiId: 1, characterValue: '日', jlptLevel: 'N5', meaning: 'Ngày, mặt trời', onyomi: 'ニチ、ジツ', kunyomi: 'ひ、か', strokeCount: 4, strokeOrderUrl: null, exampleWord: '日本語', exampleReading: 'にほんご', exampleMeaning: 'Tiếng Nhật', prevKanjiId: null, nextKanjiId: 2 },
  2: { kanjiId: 2, characterValue: '月', jlptLevel: 'N5', meaning: 'Tháng, mặt trăng', onyomi: 'ゲツ、ガツ', kunyomi: 'つき', strokeCount: 4, strokeOrderUrl: null, exampleWord: '月曜日', exampleReading: 'げつようび', exampleMeaning: 'Thứ Hai', prevKanjiId: 1, nextKanjiId: 3 },
  3: { kanjiId: 3, characterValue: '山', jlptLevel: 'N5', meaning: 'Núi', onyomi: 'サン', kunyomi: 'やま', strokeCount: 3, strokeOrderUrl: null, exampleWord: '富士山', exampleReading: 'ふじさん', exampleMeaning: 'Núi Phú Sĩ', prevKanjiId: 2, nextKanjiId: 4 },
};
export const MOCK_KANJI_DETAIL_DEFAULT = MOCK_KANJI_DETAIL_MAP[1];

// ─── Review ──────────────────────────────────────────────────────────────────
export const MOCK_FLASHCARDS_DUE = [
  { flashcardId: 1, frontText: 'こんにちは' },
  { flashcardId: 2, frontText: '日' },
  { flashcardId: 3, frontText: 'ありがとう' },
  { flashcardId: 4, frontText: '山' },
  { flashcardId: 5, frontText: '学生' },
  { flashcardId: 6, frontText: '先生' },
];

export const MOCK_BACK_CONTENT_MAP = {
  1: { reading: 'konnichiwa',   meaning: 'Xin chào (buổi trưa/chiều)', exampleSentence: 'こんにちは、田中さん。' },
  2: { reading: 'ひ、にち、か', meaning: 'Ngày, mặt trời',              exampleSentence: '今日はいい日ですね。' },
  3: { reading: 'arigatou',     meaning: 'Cảm ơn',                      exampleSentence: 'ありがとうございます。' },
  4: { reading: 'やま、さん',   meaning: 'Núi',                          exampleSentence: '富士山はきれいです。' },
  5: { reading: 'がくせい',     meaning: 'Học sinh, sinh viên',          exampleSentence: 'わたしは学生です。' },
  6: { reading: 'せんせい',     meaning: 'Giáo viên, thầy/cô giáo',      exampleSentence: '田中先生はやさしいです。' },
};

// ─── Progress ────────────────────────────────────────────────────────────────
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
    { attemptId: 1, assessmentId: 1, assessmentTitle: 'N5 Đề thi thử #1', jlptLevel: 'N5', score: 72, maxScore: 100, isPassed: true,  attemptedAt: '2026-05-28T09:30:00' },
    { attemptId: 2, assessmentId: 2, assessmentTitle: 'N5 Đề thi thử #2', jlptLevel: 'N5', score: 58, maxScore: 100, isPassed: false, attemptedAt: '2026-05-20T14:00:00' },
    { attemptId: 3, assessmentId: 1, assessmentTitle: 'N5 Đề thi thử #1', jlptLevel: 'N5', score: 65, maxScore: 100, isPassed: true,  attemptedAt: '2026-05-15T10:15:00' },
  ],
  totalPages: 1,
};

// ─── MockTestList ─────────────────────────────────────────────────────────────
export const MOCK_EXAM_LIST = {
  N5: {
    content: [
      { assessmentId: 1, title: 'N5 Đề thi thử #1 — Tổng hợp',           jlptLevel: 'N5', totalQuestions: 100, durationMin: 105, passScore: 65, maxScore: 100, lastAttempt: { attemptedAt: '2026-05-28T09:30:00', score: 72, isPassed: true  } },
      { assessmentId: 2, title: 'N5 Đề thi thử #2 — Trọng tâm từ vựng',  jlptLevel: 'N5', totalQuestions: 40,  durationMin: 35,  passScore: 26, maxScore: 40,  lastAttempt: { attemptedAt: '2026-05-20T14:00:00', score: 20, isPassed: false } },
      { assessmentId: 3, title: 'N5 Đề thi thử #3 — Ngữ pháp & Đọc hiểu',jlptLevel: 'N5', totalQuestions: 60,  durationMin: 60,  passScore: 38, maxScore: 60,  lastAttempt: null },
    ],
    totalPages: 1,
  },
  N4: {
    content: [
      { assessmentId: 4, title: 'N4 Đề thi thử #1 — Tổng hợp', jlptLevel: 'N4', totalQuestions: 100, durationMin: 105, passScore: 65, maxScore: 100, lastAttempt: null },
    ],
    totalPages: 1,
  },
  N3: { content: [], totalPages: 1 },
  N2: { content: [], totalPages: 1 },
  N1: { content: [], totalPages: 1 },
};

// ─── MockTestAttempt ──────────────────────────────────────────────────────────
export const MOCK_ASSESSMENT_DETAIL = {
  assessmentId: 1,
  title: 'N5 Đề thi thử #1 — Tổng hợp',
  jlptLevel: 'N5',
  durationMin: 105,
  sections: [
    {
      sectionName: 'Từ vựng',
      questions: [
        { questionId: 'q1', questionText: '毎朝、わたしは６___に おきます。', optionA: 'じ', optionB: 'ふん', optionC: 'まえ', optionD: 'ごろ', audioUrl: null },
        { questionId: 'q2', questionText: 'この りんごは ___ ですか。', optionA: 'いくら', optionB: 'いつ', optionC: 'どこ', optionD: 'だれ', audioUrl: null },
        { questionId: 'q3', questionText: 'きょうの てんきは___です。', optionA: 'はれ', optionB: 'さかな', optionC: 'ほん', optionD: 'えき', audioUrl: null },
        { questionId: 'q4', questionText: '___で でんしゃに のります。', optionA: 'えき', optionB: 'みせ', optionC: 'がっこう', optionD: 'うち', audioUrl: null },
      ],
    },
    {
      sectionName: 'Ngữ pháp',
      questions: [
        { questionId: 'q5', questionText: 'わたしは まいにち 6じ___おきます。', optionA: 'に', optionB: 'を', optionC: 'が', optionD: 'で', audioUrl: null },
        { questionId: 'q6', questionText: 'あの かばん___わたしの です。', optionA: 'は', optionB: 'を', optionC: 'に', optionD: 'へ', audioUrl: null },
        { questionId: 'q7', questionText: 'としょかん___ほんを かります。', optionA: 'で', optionB: 'に', optionC: 'を', optionD: 'が', audioUrl: null },
      ],
    },
    {
      sectionName: 'Đọc hiểu',
      questions: [
        { questionId: 'q8', questionText: '(Đọc đoạn văn) たなかさんは まいにち なんじに おきますか。', optionA: '6じ', optionB: '7じ', optionC: '8じ', optionD: '9じ', audioUrl: null },
        { questionId: 'q9', questionText: 'たなかさんは なにで かいしゃへ いきますか。', optionA: 'バス', optionB: 'でんしゃ', optionC: 'じてんしゃ', optionD: 'あるいて', audioUrl: null },
        { questionId: 'q10', questionText: 'たなかさんの しゅみは なんですか。', optionA: 'りょうり', optionB: 'どくしょ', optionC: 'スポーツ', optionD: 'おんがく', audioUrl: null },
      ],
    },
  ],
};

// ─── MockTestResults ──────────────────────────────────────────────────────────
export const MOCK_QUIZ_RESULT = {
  attemptId: 1,
  assessmentId: 1,
  assessmentTitle: 'N5 Đề thi thử #1 — Tổng hợp',
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
