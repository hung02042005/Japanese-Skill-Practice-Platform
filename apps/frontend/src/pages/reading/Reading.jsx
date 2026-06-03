import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import './Reading.css';

const LEVELS = ['All', 'N5', 'N4', 'N3', 'N2', 'N1'];

const MOCK_READINGS = [
  {
    id: 1,
    title: 'Đọc hiểu: Một ngày của Tanaka',
    jlptLevel: 'N5',
    questionCount: 3,
    hasAttempted: false,
    passage:
      'たなかさくらさんは まいにち ６じに おきます。あさごはんを たべて、でんしゃで かいしゃに いきます。かいしゃは とおきょうに あります。しごとは ８じから ５じまでです。しゅうまつは こうえんで さんぽを します。とても たのしい せいかつです。',
    questions: [
      { id: 1, content: 'たなかさんは まいにち なんじに おきますか。', optionA: '５じ', optionB: '６じ', optionC: '７じ', optionD: '８じ', correctOption: 'B', explanation: '「まいにち６じにおきます」と書いてあります。' },
      { id: 2, content: 'たなかさんは どうやって かいしゃに いきますか。', optionA: 'バスで', optionB: '自転車で', optionC: 'でんしゃで', optionD: 'あるいて', correctOption: 'C', explanation: '「でんしゃでかいしゃにいきます」とあります。' },
      { id: 3, content: 'たなかさんは しゅうまつに なにを しますか。', optionA: '映画を見ます', optionB: '買い物します', optionC: 'こうえんでさんぽします', optionD: '友達と会います', correctOption: 'C', explanation: '「しゅうまつはこうえんでさんぽをします」とあります。' },
    ],
  },
  {
    id: 2,
    title: 'Đọc hiểu: Thư mời tiệc',
    jlptLevel: 'N4',
    questionCount: 3,
    hasAttempted: true,
    passage:
      '来週の土曜日、私の家でパーティーをします。友達を20人招待しました。午後3時から始まります。飲み物と食べ物を用意します。プレゼントは必要ありません。ぜひ来てください。楽しみにしています。',
    questions: [
      { id: 4, content: 'パーティーはいつですか。', optionA: '今週の土曜日', optionB: '来週の土曜日', optionC: '来週の日曜日', optionD: '来月の土曜日', correctOption: 'B', explanation: '「来週の土曜日」と書いてあります。' },
      { id: 5, content: 'パーティーには何人が招待されましたか。', optionA: '10人', optionB: '15人', optionC: '20人', optionD: '25人', correctOption: 'C', explanation: '「友達を20人招待しました」とあります。' },
      { id: 6, content: 'パーティーに何を持って行く必要がありますか。', optionA: '飲み物', optionB: '食べ物', optionC: 'プレゼント', optionD: '何も必要ない', correctOption: 'D', explanation: '「プレゼントは必要ありません」とあります。' },
    ],
  },
  {
    id: 3,
    title: 'Đọc hiểu: Bản tin thời tiết',
    jlptLevel: 'N3',
    questionCount: 4,
    hasAttempted: false,
    passage:
      '明日の天気は全国的に曇りがちですが、午後から関東地方では晴れてくるでしょう。気温は平年並みで、最高気温は東京で18度の予想です。北部では夕方から雨の可能性があります。外出の際は傘を持参することをお勧めします。週末にかけて天候は回復する見込みです。',
    questions: [
      { id: 7, content: '明日の午前中の天気はどうですか。', optionA: '晴れ', optionB: '雨', optionC: '曇り', optionD: '雪', correctOption: 'C', explanation: '「午後から晴れてくる」とあり、午前は曇りです。' },
      { id: 8, content: '東京の明日の最高気温は何度ですか。', optionA: '15度', optionB: '18度', optionC: '20度', optionD: '22度', correctOption: 'B', explanation: '「最高気温は東京で18度」とあります。' },
      { id: 9, content: '夕方から雨になるのはどこですか。', optionA: '関東地方', optionB: '南部', optionC: '北部', optionD: '全国', correctOption: 'C', explanation: '「北部では夕方から雨の可能性があります」とあります。' },
      { id: 10, content: '外出するとき、何を持っていくことが勧められていますか。', optionA: '帽子', optionB: 'コート', optionC: 'サングラス', optionD: '傘', correctOption: 'D', explanation: '「外出の際は傘を持参することをお勧めします」とあります。' },
    ],
  },
];

export default function Reading() {
  const [searchParams] = useSearchParams();
  const [level,   setLevel]   = useState(searchParams.get('level') ?? 'All');
  const [view,    setView]    = useState('list');
  const [lesson,  setLesson]  = useState(null);
  const [answers, setAnswers] = useState({});
  const [results, setResults] = useState(null);

  const visibleLessons = level === 'All'
    ? MOCK_READINGS
    : MOCK_READINGS.filter((r) => r.jlptLevel === level);

  function openLesson(r) {
    setLesson(r);
    setAnswers({});
    setResults(null);
    setView('practice');
  }

  function pickAnswer(questionId, option) {
    if (results) return;
    setAnswers((prev) => ({ ...prev, [questionId]: option }));
  }

  function submit() {
    const res = lesson.questions.map((q) => ({
      questionId: q.id,
      content:    q.content,
      selected:   answers[q.id] ?? null,
      correct:    q.correctOption,
      isCorrect:  answers[q.id] === q.correctOption,
      explanation: q.explanation,
      options: { A: q.optionA, B: q.optionB, C: q.optionC, D: q.optionD },
    }));
    const score = res.filter((r) => r.isCorrect).length;
    setResults({ score, total: lesson.questions.length, items: res });
    setView('results');
  }

  const allAnswered = lesson && Object.keys(answers).length === lesson.questions.length;

  return (
    <div className="rdg-page">
      <TopNav activeTab="" />
      <main className="rdg-body">

        {/* ── LIST view ── */}
        {view === 'list' && (
          <>
            <div className="rdg-header">
              <h1 className="rdg-title">Luyện Đọc Hiểu</h1>
              <p className="rdg-subtitle">Đọc đoạn văn tiếng Nhật và trả lời câu hỏi trắc nghiệm</p>
            </div>

            <div className="rdg-levels">
              {LEVELS.map((l) => (
                <button
                  key={l}
                  className={`rdg-lvl-btn${level === l ? ' rdg-lvl-btn--active' : ''}`}
                  onClick={() => setLevel(l)}
                >
                  {l}
                </button>
              ))}
            </div>

            {visibleLessons.length === 0 ? (
              <EmptyState
                title="Chưa có bài đọc"
                subtitle="Chưa có bài đọc hiểu cho cấp độ này."
                mascotVariant="thinking"
                mascotSize={120}
              />
            ) : (
              <div className="rdg-card-grid">
                {visibleLessons.map((r) => (
                  <div key={r.id} className="rdg-card" onClick={() => openLesson(r)}>
                    <div className="rdg-card-head">
                      <JlptBadge level={r.jlptLevel} />
                      {r.hasAttempted && <span className="rdg-done-badge">Đã làm</span>}
                    </div>
                    <h2 className="rdg-card-title">{r.title}</h2>
                    <p className="rdg-card-meta">{r.questionCount} câu hỏi</p>
                    <button className="rdg-start-btn">
                      {r.hasAttempted ? 'Làm lại' : 'Bắt đầu'}
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {/* ── PRACTICE view ── */}
        {view === 'practice' && lesson && (
          <>
            <div className="rdg-practice-header">
              <button className="rdg-back-btn" onClick={() => setView('list')} aria-label="Quay lại">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M15 18l-6-6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                Danh sách
              </button>
              <div className="rdg-practice-meta">
                <JlptBadge level={lesson.jlptLevel} />
                <h1 className="rdg-practice-title">{lesson.title}</h1>
              </div>
            </div>

            <div className="rdg-passage-box">
              <p className="rdg-passage-label">Đoạn văn</p>
              <p className="rdg-passage-text">{lesson.passage}</p>
            </div>

            <div className="rdg-questions">
              {lesson.questions.map((q, idx) => (
                <div key={q.id} className="rdg-question">
                  <p className="rdg-q-text">
                    <span className="rdg-q-num">{idx + 1}</span>
                    {q.content}
                  </p>
                  <div className="rdg-options">
                    {['A', 'B', 'C', 'D'].map((opt) => (
                      <button
                        key={opt}
                        className={`rdg-opt${answers[q.id] === opt ? ' rdg-opt--selected' : ''}`}
                        onClick={() => pickAnswer(q.id, opt)}
                        disabled={!!results}
                      >
                        <span className="rdg-opt-label">{opt}</span>
                        {q[`option${opt}`]}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>

            <div className="rdg-submit-row">
              <p className="rdg-answered-note">
                Đã trả lời: {Object.keys(answers).length} / {lesson.questions.length}
              </p>
              <button
                className="rdg-submit-btn"
                disabled={!allAnswered}
                onClick={submit}
              >
                Nộp bài
              </button>
            </div>
          </>
        )}

        {/* ── RESULTS view ── */}
        {view === 'results' && results && lesson && (
          <>
            <div className="rdg-practice-header">
              <button className="rdg-back-btn" onClick={() => setView('list')} aria-label="Về danh sách">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M15 18l-6-6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                Danh sách
              </button>
            </div>

            <div className={`rdg-score-card${results.score === results.total ? ' rdg-score-card--perfect' : ''}`}>
              <div className="rdg-score-num">{results.score}<span>/{results.total}</span></div>
              <div className="rdg-score-label">
                {results.score === results.total
                  ? '🎉 Hoàn hảo!'
                  : results.score >= results.total / 2
                  ? '👍 Làm tốt!'
                  : '📚 Cần ôn thêm'}
              </div>
              <div className="rdg-score-pct">
                {Math.round((results.score / results.total) * 100)}% chính xác
              </div>
            </div>

            <div className="rdg-results-list">
              {results.items.map((r, idx) => (
                <div key={r.questionId} className={`rdg-result-item${r.isCorrect ? ' rdg-result-item--ok' : ' rdg-result-item--wrong'}`}>
                  <div className="rdg-ri-header">
                    <span className="rdg-ri-icon" aria-hidden="true">{r.isCorrect ? '✓' : '✗'}</span>
                    <p className="rdg-ri-q">
                      <span className="rdg-q-num">{idx + 1}</span>
                      {r.content}
                    </p>
                  </div>
                  <div className="rdg-ri-opts">
                    {['A', 'B', 'C', 'D'].map((opt) => (
                      <div
                        key={opt}
                        className={[
                          'rdg-ri-opt',
                          opt === r.correct  ? 'rdg-ri-opt--correct'  : '',
                          opt === r.selected && opt !== r.correct ? 'rdg-ri-opt--wrong' : '',
                        ].join(' ').trim()}
                      >
                        <span className="rdg-opt-label">{opt}</span>
                        {r.options[opt]}
                      </div>
                    ))}
                  </div>
                  <p className="rdg-ri-explain">💡 {r.explanation}</p>
                </div>
              ))}
            </div>

            <div className="rdg-result-actions">
              <button className="rdg-retry-btn" onClick={() => openLesson(lesson)}>Làm lại</button>
              <button className="rdg-back2-btn" onClick={() => setView('list')}>Bài khác</button>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
