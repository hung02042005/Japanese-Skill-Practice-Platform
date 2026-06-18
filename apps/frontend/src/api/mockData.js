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
      <h2>Hiragana là gì?</h2>
      <p>Hiragana (ひらがな) là một trong ba bảng chữ cái tiếng Nhật, gồm <strong>46 ký tự</strong> cơ bản.</p>
      <h3>Nhóm nguyên âm (あ行)</h3>
      <ul><li>あ (a)　い (i)　う (u)　え (e)　お (o)</li></ul>
      <h3>Nhóm か行</h3>
      <ul><li>か (ka)　き (ki)　く (ku)　け (ke)　こ (ko)</li></ul>
      <h3>Nhóm さ行</h3>
      <ul><li>さ (sa)　し (shi)　す (su)　せ (se)　そ (so)</li></ul>
      <p>💡 <strong>Mẹo học:</strong> Học mỗi ngày 1 hàng (5 ký tự), luyện viết và đọc thành tiếng để nhớ lâu hơn!</p>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 2,
    vocabulary: [
      { vocabId: 1, word: 'こんにちは', reading: 'konnichiwa', meaning: 'Xin chào (buổi trưa/chiều)', exampleSentence: 'こんにちは、田中さん。', exampleTranslation: 'Xin chào, anh Tanaka.' },
      { vocabId: 2, word: 'ありがとう', reading: 'arigatou', meaning: 'Cảm ơn', exampleSentence: 'ありがとうございます。', exampleTranslation: 'Xin cảm ơn rất nhiều.' },
      { vocabId: 3, word: 'すみません', reading: 'sumimasen', meaning: 'Xin lỗi / Cho hỏi', exampleSentence: 'すみません、駅はどこですか？', exampleTranslation: 'Xin lỗi, ga tàu ở đâu ạ?' },
    ],
    grammarPoints: [
      { grammarId: 1, structure: '〜は〜です', formula: 'Danh từ + は + Danh từ + です', meaning: 'A là B', exampleJp: 'わたしは学生です。', exampleVi: 'Tôi là học sinh.' },
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
      <h2>Katakana là gì?</h2>
      <p>Katakana (カタカナ) là bảng chữ cái thứ hai của tiếng Nhật, gồm <strong>46 ký tự</strong>. Dùng chủ yếu để phiên âm từ nước ngoài.</p>
      <h3>Nhóm nguyên âm (ア行)</h3>
      <ul><li>ア (a)　イ (i)　ウ (u)　エ (e)　オ (o)</li></ul>
      <h3>Nhóm カ行</h3>
      <ul><li>カ (ka)　キ (ki)　ク (ku)　ケ (ke)　コ (ko)</li></ul>
      <p>💡 <strong>Ví dụ từ ngoại lai:</strong> コーヒー (kōhī - cà phê)、テレビ (terebi - tivi)</p>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: 1, nextLessonId: 3,
    vocabulary: [
      { vocabId: 10, word: 'コーヒー', reading: 'kōhī', meaning: 'Cà phê', exampleSentence: 'コーヒーを一杯ください。', exampleTranslation: 'Cho tôi một ly cà phê.' },
      { vocabId: 11, word: 'テレビ', reading: 'terebi', meaning: 'Tivi', exampleSentence: 'テレビを見ます。', exampleTranslation: 'Tôi xem tivi.' },
    ],
    grammarPoints: [],
  },
  3: {
    lessonId: 3,
    title: 'Từ vựng N5',
    jlptLevel: 'N5',
    lessonType: 'VOCAB',
    estimatedMinutes: 20,
    progressPercent: 0,
    progressStatus: 'not_started',
    isLocked: false,
    contentHtml: `
      <h2>800 từ vựng N5 thông dụng nhất</h2>
      <p>Đây là danh sách các từ vựng cơ bản nhất trong kỳ thi JLPT N5. Nắm vững những từ này giúp bạn giao tiếp trong các tình huống hàng ngày.</p>
      <h3>Chủ đề: Gia đình</h3>
      <ul>
        <li>お父さん (otōsan) — bố</li>
        <li>お母さん (okāsan) — mẹ</li>
        <li>兄 (ani) — anh trai</li>
        <li>姉 (ane) — chị gái</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: 2, nextLessonId: null,
    vocabulary: [
      { vocabId: 20, word: 'お父さん', reading: 'otōsan', meaning: 'Bố (kính ngữ)', exampleSentence: 'お父さんはどこですか？', exampleTranslation: 'Bố đang ở đâu ạ?' },
      { vocabId: 21, word: 'お母さん', reading: 'okāsan', meaning: 'Mẹ (kính ngữ)', exampleSentence: 'お母さんは料理が上手です。', exampleTranslation: 'Mẹ nấu ăn rất giỏi.' },
      { vocabId: 22, word: '学校', reading: 'gakkō', meaning: 'Trường học', exampleSentence: '学校へ行きます。', exampleTranslation: 'Tôi đi học.' },
    ],
    grammarPoints: [],
  },
  6: {
    lessonId: 6,
    title: 'Từ vựng N4',
    jlptLevel: 'N4',
    lessonType: 'VOCAB',
    estimatedMinutes: 25,
    progressPercent: 30,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>1.500 từ vựng N4 giao tiếp hàng ngày</h2>
      <p>Ở cấp độ N4, bạn cần nắm vững các từ vựng dùng trong giao tiếp hàng ngày và các tình huống phổ biến.</p>
      <h3>Chủ đề: Công việc & Xã hội</h3>
      <ul>
        <li>会社 (kaisha) — công ty</li>
        <li>社員 (shain) — nhân viên công ty</li>
        <li>会議 (kaigi) — cuộc họp</li>
        <li>仕事 (shigoto) — công việc</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 7,
    vocabulary: [
      { vocabId: 30, word: '会社', reading: 'kaisha', meaning: 'Công ty', exampleSentence: '会社に勤めています。', exampleTranslation: 'Tôi đang làm việc ở công ty.' },
      { vocabId: 31, word: '会議', reading: 'kaigi', meaning: 'Cuộc họp', exampleSentence: '午後に会議があります。', exampleTranslation: 'Buổi chiều có cuộc họp.' },
      { vocabId: 32, word: '電話', reading: 'denwa', meaning: 'Điện thoại', exampleSentence: '電話をかけます。', exampleTranslation: 'Tôi gọi điện thoại.' },
    ],
    grammarPoints: [
      { grammarId: 10, structure: '〜ている', formula: 'Động từ て形 + いる', meaning: 'Đang làm / Trạng thái kết quả', exampleJp: '今、仕事をしています。', exampleVi: 'Bây giờ tôi đang làm việc.' },
    ],
  },
  7: {
    lessonId: 7,
    title: 'Ngữ pháp N4',
    jlptLevel: 'N4',
    lessonType: 'GRAMMAR',
    estimatedMinutes: 30,
    progressPercent: 0,
    progressStatus: 'not_started',
    isLocked: false,
    contentHtml: `
      <h2>Các mẫu câu N4 phổ biến</h2>
      <p>Ngữ pháp N4 bao gồm các cấu trúc câu phức tạp hơn N5, cho phép diễn đạt ý kiến, điều kiện và cảm xúc.</p>
      <h3>Mẫu câu điều kiện</h3>
      <ul>
        <li>〜たら — nếu...thì (điều kiện đã hoàn thành)</li>
        <li>〜ば — nếu...thì (điều kiện giả định)</li>
        <li>〜と — nếu...thì (điều kiện tự nhiên)</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: 6, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 20, structure: '〜たら', formula: 'Động từ た形 + ら', meaning: 'Nếu/Khi (điều kiện)', exampleJp: '家に帰ったら、電話します。', exampleVi: 'Khi về nhà, tôi sẽ gọi điện.' },
      { grammarId: 21, structure: '〜ながら', formula: 'Động từ ます形 (bỏ ます) + ながら', meaning: 'Vừa...vừa...', exampleJp: '音楽を聴きながら勉強します。', exampleVi: 'Tôi vừa nghe nhạc vừa học bài.' },
    ],
  },
  9: {
    lessonId: 9,
    title: 'Từ vựng N3',
    jlptLevel: 'N3',
    lessonType: 'VOCAB',
    estimatedMinutes: 30,
    progressPercent: 10,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>3.000 từ vựng N3 trung cấp thực tế</h2>
      <p>N3 là cấp độ trung cấp, yêu cầu vốn từ vựng phong phú hơn để hiểu các văn bản thực tế như tin tức, truyện ngắn.</p>
      <h3>Chủ đề: Xã hội & Văn hóa</h3>
      <ul>
        <li>社会 (shakai) — xã hội</li>
        <li>文化 (bunka) — văn hóa</li>
        <li>経済 (keizai) — kinh tế</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 10,
    vocabulary: [
      { vocabId: 40, word: '社会', reading: 'shakai', meaning: 'Xã hội', exampleSentence: '社会に貢献したい。', exampleTranslation: 'Tôi muốn đóng góp cho xã hội.' },
      { vocabId: 41, word: '経済', reading: 'keizai', meaning: 'Kinh tế', exampleSentence: '経済が発展しています。', exampleTranslation: 'Kinh tế đang phát triển.' },
    ],
    grammarPoints: [],
  },
  10: {
    lessonId: 10,
    title: 'Ngữ pháp N3',
    jlptLevel: 'N3',
    lessonType: 'GRAMMAR',
    estimatedMinutes: 35,
    progressPercent: 0,
    progressStatus: 'not_started',
    isLocked: false,
    contentHtml: `
      <h2>Cấu trúc câu phức hợp N3</h2>
      <p>Ngữ pháp N3 bao gồm các cấu trúc phức tạp dùng để diễn đạt nguyên nhân, mục đích, và các mối quan hệ logic.</p>
      <h3>Các mẫu câu quan trọng</h3>
      <ul>
        <li>〜ために — để, vì mục đích</li>
        <li>〜によって — theo, bằng cách</li>
        <li>〜に対して — đối với</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: 9, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 30, structure: '〜ために', formula: 'Động từ từ điển / Danh từ の + ために', meaning: 'Để (mục đích)', exampleJp: '日本語を話すために、毎日練習します。', exampleVi: 'Để nói tiếng Nhật, tôi luyện tập mỗi ngày.' },
      { grammarId: 31, structure: '〜によって', formula: 'Danh từ + によって', meaning: 'Theo / Tùy theo / Bằng cách', exampleJp: '人によって意見が違います。', exampleVi: 'Tùy người mà ý kiến khác nhau.' },
    ],
  },
  4: {
    lessonId: 4, title: 'Ngữ pháp N5', jlptLevel: 'N5', lessonType: 'GRAMMAR',
    estimatedMinutes: 20, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Ngữ pháp cơ bản N5</h2><p>Các mẫu câu nền tảng giúp bạn giao tiếp trong các tình huống đơn giản hàng ngày.</p><h3>Mẫu câu cơ bản</h3><ul><li>〜は〜です — A là B</li><li>〜が好きです — Tôi thích...</li><li>〜ましょう — Hãy cùng...</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 3, nextLessonId: 5,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 3, structure: '〜が好きです', formula: 'Danh từ + が好きです', meaning: 'Thích...', exampleJp: '音楽が好きです。', exampleVi: 'Tôi thích âm nhạc.' },
      { grammarId: 4, structure: '〜ましょう', formula: 'Động từ ます形 (bỏ ます) + ましょう', meaning: 'Hãy cùng làm...', exampleJp: '一緒に食べましょう。', exampleVi: 'Hãy cùng nhau ăn nào.' },
      { grammarId: 5, structure: '〜たい', formula: 'Động từ ます形 (bỏ ます) + たい', meaning: 'Muốn làm...', exampleJp: '日本へ行きたいです。', exampleVi: 'Tôi muốn đi Nhật.' },
    ],
  },
  5: {
    lessonId: 5, title: 'Kanji N5', jlptLevel: 'N5', lessonType: 'KANJI',
    estimatedMinutes: 25, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>103 Kanji N5 theo bộ thủ</h2><p>Kanji N5 là những chữ Hán cơ bản nhất, thường gặp trong cuộc sống hàng ngày.</p><h3>Nhóm thiên nhiên</h3><ul><li>日 (nhật) — ngày, mặt trời</li><li>月 (nguyệt) — tháng, mặt trăng</li><li>山 (sơn) — núi</li><li>川 (xuyên) — sông</li></ul><h3>Nhóm số đếm</h3><ul><li>一二三四五六七八九十</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 4, nextLessonId: 17,
    vocabulary: [
      { vocabId: 25, word: '日本語', reading: 'nihongo', meaning: 'Tiếng Nhật', exampleSentence: '日本語を勉強しています。', exampleTranslation: 'Tôi đang học tiếng Nhật.' },
      { vocabId: 26, word: '山田さん', reading: 'Yamada-san', meaning: 'Anh/chị Yamada', exampleSentence: '山田さんはどこですか？', exampleTranslation: 'Anh Yamada đang ở đâu?' },
    ],
    grammarPoints: [],
  },
  8: {
    lessonId: 8, title: 'Kanji N4', jlptLevel: 'N4', lessonType: 'KANJI',
    estimatedMinutes: 30, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>181 Kanji N4 theo chủ đề</h2><p>Kanji N4 bao gồm các chữ Hán thường dùng trong văn bản giao tiếp và môi trường làm việc.</p><h3>Nhóm gia đình</h3><ul><li>父 (phụ) — cha</li><li>母 (mẫu) — mẹ</li><li>兄 (huynh) — anh trai</li><li>姉 (tỷ) — chị gái</li></ul><h3>Nhóm xã hội</h3><ul><li>会社 (kaisha) — công ty</li><li>社員 (shain) — nhân viên</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 7, nextLessonId: 19,
    vocabulary: [
      { vocabId: 35, word: '会社員', reading: 'kaishain', meaning: 'Nhân viên công ty', exampleSentence: '私は会社員です。', exampleTranslation: 'Tôi là nhân viên công ty.' },
    ],
    grammarPoints: [],
  },
  11: {
    lessonId: 11, title: 'Đọc hiểu N3', jlptLevel: 'N3', lessonType: 'READING',
    estimatedMinutes: 35, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Phân tích văn bản tin tức & truyện ngắn N3</h2><p>Đọc hiểu N3 yêu cầu hiểu nội dung chính, ý kiến của tác giả và chi tiết cụ thể trong các đoạn văn thực tế.</p><h3>Chiến lược đọc hiểu</h3><ul><li>Đọc câu hỏi trước để định hướng tìm thông tin</li><li>Chú ý các từ nối như でも、しかし、だから</li><li>Tìm câu chủ đề ở đầu hoặc cuối đoạn văn</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 10, nextLessonId: 21,
    vocabulary: [
      { vocabId: 42, word: 'しかし', reading: 'shikashi', meaning: 'Nhưng, tuy nhiên', exampleSentence: '雨が降った。しかし、試合は続いた。', exampleTranslation: 'Trời mưa. Tuy nhiên, trận đấu vẫn tiếp tục.' },
    ],
    grammarPoints: [],
  },
  13: {
    lessonId: 13, title: 'Ngữ pháp N2', jlptLevel: 'N2', lessonType: 'GRAMMAR',
    estimatedMinutes: 40, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Cấu trúc câu nâng cao N2</h2><p>Ngữ pháp N2 bao gồm các mẫu câu học thuật và lịch sự ngữ thường thấy trong báo chí, văn bản chính thức.</p><h3>Các mẫu câu nâng cao</h3><ul><li>〜にもかかわらず — mặc dù, bất chấp</li><li>〜に際して — vào dịp, nhân dịp</li><li>〜にあたって — khi, vào lúc</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 12, nextLessonId: 14,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 32, structure: '〜にもかかわらず', formula: 'Động từ / Danh từ + にもかかわらず', meaning: 'Mặc dù, bất chấp', exampleJp: '雨にもかかわらず、彼は来た。', exampleVi: 'Mặc dù trời mưa, anh ấy vẫn đến.' },
      { grammarId: 33, structure: '〜に際して', formula: 'Danh từ / Động từ từ điển + に際して', meaning: 'Vào dịp, nhân dịp', exampleJp: '入学に際して、準備をする。', exampleVi: 'Nhân dịp nhập học, cần chuẩn bị.' },
    ],
  },
  14: {
    lessonId: 14, title: 'Đọc hiểu N2', jlptLevel: 'N2', lessonType: 'READING',
    estimatedMinutes: 40, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Báo chí, luận văn & văn bản chính thức N2</h2><p>Đọc hiểu N2 yêu cầu phân tích văn bản phức tạp, nhận biết lập luận và đánh giá quan điểm tác giả.</p><h3>Loại văn bản thường gặp</h3><ul><li>Bài báo tin tức (ニュース記事)</li><li>Bài luận ý kiến (意見文)</li><li>Thông báo chính thức (公告)</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 13, nextLessonId: 23,
    vocabulary: [
      { vocabId: 52, word: '論じる', reading: 'ronjiru', meaning: 'Lập luận, bàn luận', exampleSentence: '問題について論じました。', exampleTranslation: 'Chúng tôi đã bàn luận về vấn đề đó.' },
    ],
    grammarPoints: [],
  },
  16: {
    lessonId: 16, title: 'Ngữ pháp N1', jlptLevel: 'N1', lessonType: 'GRAMMAR',
    estimatedMinutes: 50, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Cú pháp văn học, học thuật & cổ ngữ N1</h2><p>Ngữ pháp N1 bao gồm các cấu trúc cổ điển, văn học và học thuật ở mức độ bản ngữ.</p><h3>Cổ ngữ & văn học</h3><ul><li>〜べからず — không được (cấm đoán cổ điển)</li><li>〜ずして — không làm mà vẫn...</li><li>〜にほかならない — không gì khác ngoài...</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 15, nextLessonId: 25,
    vocabulary: [],
    grammarPoints: [
      { grammarId: 40, structure: '〜にほかならない', formula: 'Danh từ / Động từ + にほかならない', meaning: 'Không gì khác ngoài, chính là', exampleJp: 'これは努力の結果にほかならない。', exampleVi: 'Đây không gì khác chính là kết quả của sự nỗ lực.' },
    ],
  },
  17: {
    lessonId: 17, title: 'Đọc hiểu N5', jlptLevel: 'N5', lessonType: 'READING',
    estimatedMinutes: 15, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Bài đọc ngắn & câu hỏi hiểu nội dung N5</h2><p>Đọc hiểu N5 bao gồm các đoạn văn ngắn về chủ đề quen thuộc như thời tiết, gia đình, thức ăn.</p><h3>Bài đọc mẫu</h3><p>わたしは まいにち あさ 7じに おきます。それから シャワーを あびて、パンを たべます。8じに がっこうへ いきます。</p><p><strong>Câu hỏi:</strong> Người này đến trường lúc mấy giờ?</p>`,
    audioUrl: null, imageUrl: null, prevLessonId: 5, nextLessonId: 18,
    vocabulary: [
      { vocabId: 27, word: 'まいにち', reading: 'mainichi', meaning: 'Hàng ngày, mỗi ngày', exampleSentence: 'まいにち 日本語を 勉強します。', exampleTranslation: 'Tôi học tiếng Nhật hàng ngày.' },
    ],
    grammarPoints: [],
  },
  18: {
    lessonId: 18, title: 'Nghe hiểu N5', jlptLevel: 'N5', lessonType: 'LISTENING',
    estimatedMinutes: 15, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Hội thoại & câu hỏi nghe hiểu cơ bản N5</h2><p>Nghe hiểu N5 bao gồm các đoạn hội thoại ngắn trong tình huống hàng ngày — mua sắm, hỏi đường, tự giới thiệu.</p><h3>Chủ đề nghe hiểu N5</h3><ul><li>Giới thiệu bản thân</li><li>Hỏi và chỉ đường đơn giản</li><li>Mua đồ tại cửa hàng</li><li>Hỏi về thời gian và địa điểm</li></ul><p>💡 <strong>Mẹo:</strong> Nghe 2-3 lần trước khi trả lời câu hỏi.</p>`,
    audioUrl: null, imageUrl: null, prevLessonId: 17, nextLessonId: null,
    vocabulary: [
      { vocabId: 28, word: 'すみません', reading: 'sumimasen', meaning: 'Xin lỗi / Cho hỏi', exampleSentence: 'すみません、駅はどこですか？', exampleTranslation: 'Xin lỗi, ga tàu ở đâu ạ?' },
    ],
    grammarPoints: [],
  },
  19: {
    lessonId: 19, title: 'Đọc hiểu N4', jlptLevel: 'N4', lessonType: 'READING',
    estimatedMinutes: 25, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Đoạn văn thực tế & câu hỏi hiểu nội dung N4</h2><p>Đọc hiểu N4 gồm các đoạn văn về cuộc sống công sở, thư cá nhân và thông báo ngắn.</p><h3>Loại bài đọc N4</h3><ul><li>Email/thư ngắn (メール・手紙)</li><li>Thông báo nơi làm việc</li><li>Bài viết blog đơn giản</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 8, nextLessonId: 20,
    vocabulary: [
      { vocabId: 36, word: '連絡', reading: 'renraku', meaning: 'Liên lạc, thông báo', exampleSentence: '後で連絡します。', exampleTranslation: 'Tôi sẽ liên lạc sau.' },
    ],
    grammarPoints: [],
  },
  20: {
    lessonId: 20, title: 'Nghe hiểu N4', jlptLevel: 'N4', lessonType: 'LISTENING',
    estimatedMinutes: 25, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Hội thoại hàng ngày & câu hỏi nghe hiểu N4</h2><p>Nghe hiểu N4 bao gồm hội thoại về công việc, cuộc sống xã hội và các tình huống phổ biến hơn.</p><h3>Chủ đề nghe hiểu N4</h3><ul><li>Hội thoại tại nơi làm việc</li><li>Cuộc trò chuyện về kế hoạch</li><li>Thông báo tại ga, siêu thị</li><li>Phỏng vấn ngắn</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 19, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [],
  },
  21: {
    lessonId: 21, title: 'Kanji N3', jlptLevel: 'N3', lessonType: 'KANJI',
    estimatedMinutes: 35, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>367 Kanji N3 theo nhóm ngữ nghĩa</h2><p>Kanji N3 bao gồm các chữ Hán trung cấp thường xuất hiện trong báo chí, sách giáo khoa và văn bản xã hội.</p><h3>Nhóm cảm xúc & tâm lý</h3><ul><li>感 (cảm) — cảm giác</li><li>情 (tình) — tình cảm</li><li>悲 (bi) — buồn</li><li>喜 (hỷ) — vui mừng</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 11, nextLessonId: 22,
    vocabulary: [
      { vocabId: 43, word: '感情', reading: 'kanjō', meaning: 'Cảm xúc, tình cảm', exampleSentence: '感情をコントロールする。', exampleTranslation: 'Kiểm soát cảm xúc của mình.' },
    ],
    grammarPoints: [],
  },
  22: {
    lessonId: 22, title: 'Nghe hiểu N3', jlptLevel: 'N3', lessonType: 'LISTENING',
    estimatedMinutes: 35, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Hội thoại phức tạp & tình huống thực tế N3</h2><p>Nghe hiểu N3 bao gồm các cuộc hội thoại và bài phát biểu trong các tình huống xã hội đa dạng.</p><h3>Chủ đề nghe hiểu N3</h3><ul><li>Tranh luận & ý kiến cá nhân</li><li>Tin tức ngắn trên radio</li><li>Hội thoại trong bệnh viện, ngân hàng</li><li>Mô tả sự kiện & câu chuyện</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 21, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [],
  },
  23: {
    lessonId: 23, title: 'Kanji N2', jlptLevel: 'N2', lessonType: 'KANJI',
    estimatedMinutes: 45, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>367 Kanji N2 theo lĩnh vực học thuật</h2><p>Kanji N2 tập trung vào các chữ Hán trong văn bản học thuật, khoa học và môi trường chuyên nghiệp.</p><h3>Nhóm khoa học & học thuật</h3><ul><li>研 (nghiên) — nghiên cứu</li><li>究 (cứu) — tìm hiểu</li><li>論 (luận) — lý luận</li><li>証 (chứng) — bằng chứng</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 14, nextLessonId: 24,
    vocabulary: [
      { vocabId: 53, word: '研究者', reading: 'kenkyūsha', meaning: 'Nhà nghiên cứu', exampleSentence: '彼は著名な研究者です。', exampleTranslation: 'Ông ấy là nhà nghiên cứu nổi tiếng.' },
    ],
    grammarPoints: [],
  },
  24: {
    lessonId: 24, title: 'Nghe hiểu N2', jlptLevel: 'N2', lessonType: 'LISTENING',
    estimatedMinutes: 45, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Bài giảng, phỏng vấn & nội dung học thuật N2</h2><p>Nghe hiểu N2 yêu cầu hiểu các bài giảng dài, phỏng vấn chuyên gia và nội dung thông tin phức tạp.</p><h3>Chủ đề nghe hiểu N2</h3><ul><li>Bài giảng đại học ngắn</li><li>Phỏng vấn chuyên gia</li><li>Chương trình radio/podcast thực tế</li><li>Thảo luận nhóm chuyên nghiệp</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 23, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [],
  },
  25: {
    lessonId: 25, title: 'Đọc hiểu N1', jlptLevel: 'N1', lessonType: 'READING',
    estimatedMinutes: 55, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Văn bản học thuật, văn học & triết học N1</h2><p>Đọc hiểu N1 là thử thách cao nhất — bao gồm các văn bản triết học, khoa học xã hội và văn học tiêu biểu.</p><h3>Loại văn bản N1</h3><ul><li>Bài báo học thuật (論文)</li><li>Tiểu luận văn học (随筆)</li><li>Phân tích triết học (哲学的考察)</li><li>Bình luận xã hội (社会評論)</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 16, nextLessonId: 26,
    vocabulary: [
      { vocabId: 62, word: '考察', reading: 'kōsatsu', meaning: 'Xem xét, nghiên cứu', exampleSentence: 'この問題について考察する。', exampleTranslation: 'Xem xét vấn đề này.' },
    ],
    grammarPoints: [],
  },
  26: {
    lessonId: 26, title: 'Kanji N1', jlptLevel: 'N1', lessonType: 'KANJI',
    estimatedMinutes: 60, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>1.162 Kanji N1 — bao gồm kanji hiếm gặp</h2><p>Kanji N1 bao gồm các chữ Hán hiếm gặp, kanji trong tên địa danh, tên người và văn bản cổ điển.</p><h3>Nhóm kanji hiếm gặp</h3><ul><li>鬱 (uất) — u uất, bực bội</li><li>麒 (kỳ) — kỳ lân</li><li>纏 (triền) — cuốn quanh</li></ul><p>💡 <strong>Lưu ý:</strong> Tập trung vào âm đọc và ngữ cảnh sử dụng thay vì học thuộc từng nét.</p>`,
    audioUrl: null, imageUrl: null, prevLessonId: 25, nextLessonId: 27,
    vocabulary: [],
    grammarPoints: [],
  },
  27: {
    lessonId: 27, title: 'Nghe hiểu N1', jlptLevel: 'N1', lessonType: 'LISTENING',
    estimatedMinutes: 60, progressPercent: 0, progressStatus: 'not_started',
    contentHtml: `<h2>Bài phát biểu, tranh luận & nội dung phức tạp N1</h2><p>Nghe hiểu N1 yêu cầu hiểu các bài diễn thuyết phức tạp, tranh luận học thuật và nội dung văn hóa sâu sắc.</p><h3>Chủ đề nghe hiểu N1</h3><ul><li>Diễn thuyết chính thức & chính trị</li><li>Tranh luận triết học & khoa học</li><li>Phim tài liệu & chương trình văn hóa</li><li>Hội nghị chuyên ngành</li></ul>`,
    audioUrl: null, imageUrl: null, prevLessonId: 26, nextLessonId: null,
    vocabulary: [],
    grammarPoints: [],
  },
  12: {
    lessonId: 12,
    title: 'Từ vựng N2',
    jlptLevel: 'N2',
    lessonType: 'VOCAB',
    estimatedMinutes: 40,
    progressPercent: 5,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>6.000 từ vựng N2 cao cấp & học thuật</h2>
      <p>N2 yêu cầu vốn từ vựng học thuật và chuyên ngành, phù hợp với môi trường làm việc và học tập chuyên sâu.</p>
      <h3>Chủ đề: Học thuật & Nghiên cứu</h3>
      <ul>
        <li>研究 (kenkyū) — nghiên cứu</li>
        <li>論文 (ronbun) — luận văn, bài báo</li>
        <li>実験 (jikken) — thí nghiệm</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 13,
    vocabulary: [
      { vocabId: 50, word: '研究', reading: 'kenkyū', meaning: 'Nghiên cứu', exampleSentence: '新しい研究を始めました。', exampleTranslation: 'Tôi đã bắt đầu nghiên cứu mới.' },
      { vocabId: 51, word: '論文', reading: 'ronbun', meaning: 'Luận văn / Bài báo', exampleSentence: '論文を書いています。', exampleTranslation: 'Tôi đang viết luận văn.' },
    ],
    grammarPoints: [],
  },
  15: {
    lessonId: 15,
    title: 'Từ vựng N1',
    jlptLevel: 'N1',
    lessonType: 'VOCAB',
    estimatedMinutes: 50,
    progressPercent: 2,
    progressStatus: 'in_progress',
    isLocked: false,
    contentHtml: `
      <h2>10.000 từ vựng N1 & thành ngữ bản ngữ</h2>
      <p>N1 là cấp độ cao nhất, yêu cầu hiểu các văn bản phức tạp, ngôn ngữ văn học và thành ngữ như người bản ngữ.</p>
      <h3>Thành ngữ phổ biến</h3>
      <ul>
        <li>猫の手も借りたい — Bận đến mức muốn mượn cả tay mèo (cực kỳ bận)</li>
        <li>七転び八起き — Thất bại bảy lần đứng dậy tám lần (kiên trì)</li>
      </ul>
    `,
    audioUrl: null, imageUrl: null,
    prevLessonId: null, nextLessonId: 16,
    vocabulary: [
      { vocabId: 60, word: '模索', reading: 'mosaku', meaning: 'Mò mẫm, tìm kiếm', exampleSentence: '解決策を模索しています。', exampleTranslation: 'Chúng tôi đang tìm kiếm giải pháp.' },
      { vocabId: 61, word: '洞察', reading: 'dōsatsu', meaning: 'Nhận thức sâu sắc, thấu hiểu', exampleSentence: '鋭い洞察力を持っている。', exampleTranslation: 'Anh ấy có khả năng nhận thức sắc bén.' },
    ],
    grammarPoints: [],
  },
};

export const MOCK_LESSON_DETAIL = MOCK_LESSON_DETAIL_MAP[1];

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
    { attemptId: 1, assessmentTitle: 'N5 Đề thi thử #1', jlptLevel: 'N5', totalScore: 72, maxScore: 100, isPassed: true,  submittedAt: '2026-05-28T09:30:00' },
    { attemptId: 2, assessmentTitle: 'N5 Đề thi thử #2', jlptLevel: 'N5', totalScore: 58, maxScore: 100, isPassed: false, submittedAt: '2026-05-20T14:00:00' },
    { attemptId: 3, assessmentTitle: 'N5 Đề thi thử #1', jlptLevel: 'N5', totalScore: 65, maxScore: 100, isPassed: true,  submittedAt: '2026-05-15T10:15:00' },
  ],
  totalPages: 1,
};

// ─── MockTestList ─────────────────────────────────────────────────────────────
export const MOCK_EXAM_LIST = {
  N5: {
    content: [
      { assessmentId: 1, title: 'N5 Đề thi thử #1 — Tổng hợp',           jlptLevel: 'N5', questionCount: 100, durationMin: 105, passScore: 65, totalScore: 100 },
      { assessmentId: 2, title: 'N5 Đề thi thử #2 — Trọng tâm từ vựng',  jlptLevel: 'N5', questionCount: 40,  durationMin: 35,  passScore: 26, totalScore: 40  },
      { assessmentId: 3, title: 'N5 Đề thi thử #3 — Ngữ pháp & Đọc hiểu',jlptLevel: 'N5', questionCount: 60,  durationMin: 60,  passScore: 38, totalScore: 60  },
    ],
    totalPages: 1,
  },
  N4: {
    content: [
      { assessmentId: 4, title: 'N4 Đề thi thử #1 — Tổng hợp', jlptLevel: 'N4', questionCount: 100, durationMin: 105, passScore: 65, totalScore: 100 },
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
  totalScore: 72,
  maxScore: 100,
  isPassed: true,
  sectionScores: {
    languageKnowledge: 45,
    reading: 18,
    listening: 9,
  },
  results: [
    { questionId: 1,  questionText: '毎朝、わたしは６___に おきます。',        optionA: 'じ', optionB: 'ふん', optionC: 'まえ', optionD: 'ごろ', selectedOption: 'A', correctOption: 'A', isCorrect: true,  score: 7.2, explanation: '「6じ」là cách đọc giờ đúng.' },
    { questionId: 2,  questionText: 'この りんごは ___ ですか。',                optionA: 'いくら', optionB: 'いつ', optionC: 'どこ', optionD: 'だれ', selectedOption: 'A', correctOption: 'A', isCorrect: true,  score: 7.2, explanation: '「いくら」dùng để hỏi giá.' },
    { questionId: 3,  questionText: 'きょうの てんきは___です。',                optionA: 'はれ', optionB: 'さかな', optionC: 'ほん', optionD: 'えき', selectedOption: 'B', correctOption: 'A', isCorrect: false, score: 0,   explanation: '「はれ」nghĩa là "nắng", phù hợp để tả thời tiết.' },
    { questionId: 4,  questionText: '___で でんしゃに のります。',               optionA: 'えき', optionB: 'みせ', optionC: 'がっこう', optionD: 'うち', selectedOption: 'A', correctOption: 'A', isCorrect: true,  score: 7.2, explanation: '「えき」(nhà ga) là nơi lên tàu điện.' },
    { questionId: 5,  questionText: 'わたしは まいにち 6じ___おきます。',         optionA: 'に', optionB: 'を', optionC: 'が', optionD: 'で', selectedOption: 'A', correctOption: 'A', isCorrect: true,  score: 7.2, explanation: 'Trợ từ「に」chỉ thời điểm.' },
    { questionId: 6,  questionText: 'あの かばん___わたしの です。',             optionA: 'は', optionB: 'を', optionC: 'に', optionD: 'へ', selectedOption: 'D', correctOption: 'A', isCorrect: false, score: 0,   explanation: 'Trợ từ「は」đánh dấu chủ đề câu.' },
    { questionId: 7,  questionText: 'としょかん___ほんを かります。',             optionA: 'で', optionB: 'に', optionC: 'を', optionD: 'が', selectedOption: 'A', correctOption: 'A', isCorrect: true,  score: 7.2, explanation: 'Trợ từ「で」chỉ địa điểm diễn ra hành động.' },
    { questionId: 8,  questionText: 'たなかさんは まいにち なんじに おきますか。', optionA: '6じ', optionB: '7じ', optionC: '8じ', optionD: '9じ', selectedOption: 'B', correctOption: 'B', isCorrect: true,  score: 6,   explanation: 'Theo đoạn văn, Tanaka thức dậy lúc 7 giờ.' },
    { questionId: 9,  questionText: 'たなかさんは なにで かいしゃへ いきますか。', optionA: 'バス', optionB: 'でんしゃ', optionC: 'じてんしゃ', optionD: 'あるいて', selectedOption: 'C', correctOption: 'B', isCorrect: false, score: 0,   explanation: 'Theo đoạn văn, Tanaka đi làm bằng tàu điện.' },
    { questionId: 10, questionText: 'たなかさんの しゅみは なんですか。',          optionA: 'りょうり', optionB: 'どくしょ', optionC: 'スポーツ', optionD: 'おんがく', selectedOption: null, correctOption: 'B', isCorrect: false, score: 0,  explanation: 'Theo đoạn văn, sở thích của Tanaka là đọc sách.' },
  ],
};
