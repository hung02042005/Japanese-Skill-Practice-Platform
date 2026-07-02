import { useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { ReadingIcon } from '../../components/student/StudentIcons';
import { ConfettiIcon, ThumbsUpIcon, LightbulbIcon, MusicIcon } from '../../components/common/AppIcons';
import './Listening.css';

const LEVELS = ['All', 'N5', 'N4', 'N3'];

const MOCK_LISTENINGS = [
  {
    id: 1,
    title: 'Nghe hiểu: Gọi điện đặt pizza',
    jlptLevel: 'N5',
    questionCount: 2,
    hasAttempted: false,
    audioDescription: 'Đoạn hội thoại giữa nhân viên cửa hàng pizza và khách hàng đặt bánh qua điện thoại.',
    transcript:
      'もしもし、田中ピザです。いらっしゃいませ。\nはい、ピザを一枚注文したいんですが。\nはい、何のピザにしますか？マルゲリータとペペロニがあります。\nマルゲリータをお願いします。\nはい、お届け先の住所を教えてください。\n東京都渋谷区1-2-3です。\n少々お待ちください。30分ほどで届きます。',
    questions: [
      { id: 1, content: '客は何のピザを注文しましたか。', optionA: 'ペペロニ', optionB: 'マルゲリータ', optionC: 'ハワイアン', optionD: 'バーベキュー', correctOption: 'B', explanation: '「マルゲリータをお願いします」と言いました。' },
      { id: 2, content: 'ピザは何分で届きますか。', optionA: '20分', optionB: '25分', optionC: '30分', optionD: '45分', correctOption: 'C', explanation: '「30分ほどで届きます」と言いました。' },
    ],
  },
  {
    id: 2,
    title: 'Nghe hiểu: Thông báo tại nhà ga',
    jlptLevel: 'N4',
    questionCount: 3,
    hasAttempted: false,
    audioDescription: 'Thông báo phát thanh tại nhà ga điện ngầm về thông tin tàu sắp đến.',
    transcript:
      'ただいま、3番線に電車が参ります。危ないですから、黄色い線の内側に下がってください。この電車は各駅停車で、終点は新宿です。発車まで少々お待ちください。お乗りの際は、足元にご注意ください。',
    questions: [
      { id: 3, content: '電車は何番線に来ますか。', optionA: '1番線', optionB: '2番線', optionC: '3番線', optionD: '4番線', correctOption: 'C', explanation: '「3番線に電車が参ります」と言いました。' },
      { id: 4, content: 'どこに下がるように言っていますか。', optionA: '白い線', optionB: '黄色い線の内側', optionC: '出口の前', optionD: 'ホームの端', correctOption: 'B', explanation: '「黄色い線の内側に下がってください」と言いました。' },
      { id: 5, content: 'この電車の終点はどこですか。', optionA: '渋谷', optionB: '池袋', optionC: '秋葉原', optionD: '新宿', correctOption: 'D', explanation: '「終点は新宿です」と言いました。' },
    ],
  },
  {
    id: 3,
    title: 'Nghe hiểu: Dự báo thời tiết',
    jlptLevel: 'N3',
    questionCount: 3,
    hasAttempted: true,
    audioDescription: 'Đoạn dự báo thời tiết phát trên đài truyền hình về thời tiết ngày mai tại vùng Kanto.',
    transcript:
      '明日の関東地方の天気をお伝えします。午前中は雲が多く、一部で小雨が降るでしょう。午後からは次第に晴れ間が広がり、夕方には晴れとなる見込みです。最高気温は本日より3度低い16度の予想です。朝の通勤時は傘をお持ちください。',
    questions: [
      { id: 6, content: '明日の午前中の天気はどうですか。', optionA: '晴れ', optionB: '曇りで小雨', optionC: '大雨', optionD: '雪', correctOption: 'B', explanation: '「午前中は雲が多く、一部で小雨が降るでしょう」と言いました。' },
      { id: 7, content: '明日の最高気温は何度ですか。', optionA: '13度', optionB: '16度', optionC: '19度', optionD: '22度', correctOption: 'B', explanation: '「最高気温は本日より3度低い16度」と言いました。' },
      { id: 8, content: '朝の通勤時に何を持っていくことが推奨されていますか。', optionA: 'コート', optionB: '帽子', optionC: '傘', optionD: 'マフラー', correctOption: 'C', explanation: '「朝の通勤時は傘をお持ちください」と言いました。' },
    ],
  },
];

export default function Listening() {
  const [searchParams] = useSearchParams();
  const [level,           setLevel]          = useState(searchParams.get('level') ?? 'All');
  const [view,            setView]           = useState('list');
  const [lesson,          setLesson]         = useState(null);
  const [answers,         setAnswers]        = useState({});
  const [results,         setResults]        = useState(null);
  const [showTranscript,  setShowTranscript] = useState(false);
  const [audioPlaying,    setAudioPlaying]   = useState(false);
  const audioRef = useRef(null);

  const visibleLessons = level === 'All'
    ? MOCK_LISTENINGS
    : MOCK_LISTENINGS.filter((r) => r.jlptLevel === level);

  function openLesson(r) {
    setLesson(r);
    setAnswers({});
    setResults(null);
    setShowTranscript(false);
    setAudioPlaying(false);
    setView('practice');
  }

  function pickAnswer(questionId, option) {
    if (results) return;
    setAnswers((prev) => ({ ...prev, [questionId]: option }));
  }

  function submit() {
    const res = lesson.questions.map((q) => ({
      questionId:  q.id,
      content:     q.content,
      selected:    answers[q.id] ?? null,
      correct:     q.correctOption,
      isCorrect:   answers[q.id] === q.correctOption,
      explanation: q.explanation,
      options:     { A: q.optionA, B: q.optionB, C: q.optionC, D: q.optionD },
    }));
    const score = res.filter((r) => r.isCorrect).length;
    setResults({ score, total: lesson.questions.length, items: res });
    setShowTranscript(true);
    setView('results');
  }

  const allAnswered = lesson && Object.keys(answers).length === lesson.questions.length;

  return (
    <div className="lst-page">
      <TopNav activeTab="" />
      <main className="lst-body">

        {/* ── LIST view ── */}
        {view === 'list' && (
          <>
            <div className="lst-header">
              <h1 className="lst-title">Luyện Nghe Hiểu</h1>
              <p className="lst-subtitle">Nghe đoạn hội thoại / thông báo tiếng Nhật và trả lời câu hỏi</p>
            </div>

            <div className="lst-levels">
              {LEVELS.map((l) => (
                <button
                  key={l}
                  className={`lst-lvl-btn${level === l ? ' lst-lvl-btn--active' : ''}`}
                  onClick={() => setLevel(l)}
                >
                  {l}
                </button>
              ))}
            </div>

            {visibleLessons.length === 0 ? (
              <EmptyState
                title="Chưa có bài nghe"
                subtitle="Chưa có bài nghe hiểu cho cấp độ này."
                mascotVariant="thinking"
                mascotSize={120}
              />
            ) : (
              <div className="lst-card-grid">
                {visibleLessons.map((r) => (
                  <div key={r.id} className="lst-card" onClick={() => openLesson(r)}>
                    <div className="lst-card-head">
                      <JlptBadge level={r.jlptLevel} />
                      {r.hasAttempted && <span className="lst-done-badge">Đã làm</span>}
                    </div>
                    <h2 className="lst-card-title">{r.title}</h2>
                    <p className="lst-card-desc">{r.audioDescription}</p>
                    <p className="lst-card-meta">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
                        <path d="M15.54 8.46a5 5 0 0 1 0 7.07" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                      </svg>
                      {r.questionCount} câu hỏi
                    </p>
                    <button className="lst-start-btn">
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
            <div className="lst-practice-header">
              <button className="lst-back-btn" onClick={() => setView('list')}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M15 18l-6-6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                Danh sách
              </button>
              <div className="lst-practice-meta">
                <JlptBadge level={lesson.jlptLevel} />
                <h1 className="lst-practice-title">{lesson.title}</h1>
              </div>
            </div>

            {/* Audio player placeholder */}
            <div className="lst-audio-box" ref={audioRef}>
              <div className="lst-audio-icon" aria-hidden="true">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
                  <path d="M15.54 8.46a5 5 0 0 1 0 7.07" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                  <path d="M19.07 4.93a10 10 0 0 1 0 14.14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                </svg>
              </div>
              <div className="lst-audio-info">
                <p className="lst-audio-title">{lesson.title}</p>
                <p className="lst-audio-desc">{lesson.audioDescription}</p>
              </div>
              <button
                className={`lst-play-btn${audioPlaying ? ' lst-play-btn--playing' : ''}`}
                onClick={() => setAudioPlaying((p) => !p)}
                aria-label={audioPlaying ? 'Dừng' : 'Phát audio'}
              >
                {audioPlaying ? (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                    <rect x="6" y="4" width="4" height="16" />
                    <rect x="14" y="4" width="4" height="16" />
                  </svg>
                ) : (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                    <polygon points="5 3 19 12 5 21 5 3" />
                  </svg>
                )}
              </button>
              {audioPlaying && (
                <div className="lst-audio-demo-note" role="status">
                  <MusicIcon size={15} /> Demo UI — audio sẽ kết nối sau khi có backend
                </div>
              )}
            </div>

            <div className="lst-questions">
              {lesson.questions.map((q, idx) => (
                <div key={q.id} className="lst-question">
                  <p className="lst-q-text">
                    <span className="lst-q-num">{idx + 1}</span>
                    {q.content}
                  </p>
                  <div className="lst-options">
                    {['A', 'B', 'C', 'D'].map((opt) => (
                      <button
                        key={opt}
                        className={`lst-opt${answers[q.id] === opt ? ' lst-opt--selected' : ''}`}
                        onClick={() => pickAnswer(q.id, opt)}
                        disabled={!!results}
                      >
                        <span className="lst-opt-label">{opt}</span>
                        {q[`option${opt}`]}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>

            <div className="lst-submit-row">
              <p className="lst-answered-note">
                Đã trả lời: {Object.keys(answers).length} / {lesson.questions.length}
              </p>
              <button className="lst-submit-btn" disabled={!allAnswered} onClick={submit}>
                Nộp bài
              </button>
            </div>
          </>
        )}

        {/* ── RESULTS view ── */}
        {view === 'results' && results && lesson && (
          <>
            <div className="lst-practice-header">
              <button className="lst-back-btn" onClick={() => setView('list')}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M15 18l-6-6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                Danh sách
              </button>
            </div>

            <div className={`lst-score-card${results.score === results.total ? ' lst-score-card--perfect' : ''}`}>
              <div className="lst-score-num">{results.score}<span>/{results.total}</span></div>
              <div className="lst-score-label">
                {results.score === results.total ? <><ConfettiIcon size={18} /> Hoàn hảo!</>
                  : results.score >= results.total / 2 ? <><ThumbsUpIcon size={18} /> Làm tốt!</>
                  : <><ReadingIcon size={18} /> Cần ôn thêm</>}
              </div>
              <div className="lst-score-pct">{Math.round((results.score / results.total) * 100)}% chính xác</div>
            </div>

            {/* Transcript */}
            <div className="lst-transcript-box">
              <button
                className="lst-transcript-toggle"
                onClick={() => setShowTranscript((p) => !p)}
                aria-expanded={showTranscript}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" stroke="currentColor" strokeWidth="2" />
                  <rect x="9" y="3" width="6" height="4" rx="1" stroke="currentColor" strokeWidth="2" />
                </svg>
                {showTranscript ? 'Ẩn transcript' : 'Xem transcript'}
                <svg
                  className={`lst-tr-chevron${showTranscript ? ' lst-tr-chevron--up' : ''}`}
                  width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true"
                >
                  <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </button>
              {showTranscript && (
                <pre className="lst-transcript-text">{lesson.transcript}</pre>
              )}
            </div>

            <div className="lst-results-list">
              {results.items.map((r, idx) => (
                <div key={r.questionId} className={`lst-result-item${r.isCorrect ? ' lst-result-item--ok' : ' lst-result-item--wrong'}`}>
                  <div className="lst-ri-header">
                    <span className="lst-ri-icon" aria-hidden="true">{r.isCorrect ? '✓' : '✗'}</span>
                    <p className="lst-ri-q">
                      <span className="lst-q-num">{idx + 1}</span>
                      {r.content}
                    </p>
                  </div>
                  <div className="lst-ri-opts">
                    {['A', 'B', 'C', 'D'].map((opt) => (
                      <div
                        key={opt}
                        className={[
                          'lst-ri-opt',
                          opt === r.correct ? 'lst-ri-opt--correct' : '',
                          opt === r.selected && opt !== r.correct ? 'lst-ri-opt--wrong' : '',
                        ].join(' ').trim()}
                      >
                        <span className="lst-opt-label">{opt}</span>
                        {r.options[opt]}
                      </div>
                    ))}
                  </div>
                  <p className="lst-ri-explain"><LightbulbIcon size={14} /> {r.explanation}</p>
                </div>
              ))}
            </div>

            <div className="lst-result-actions">
              <button className="lst-retry-btn" onClick={() => openLesson(lesson)}>Làm lại</button>
              <button className="lst-back2-btn" onClick={() => setView('list')}>Bài khác</button>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
