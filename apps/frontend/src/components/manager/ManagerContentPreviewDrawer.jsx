import { useEffect } from 'react';
import { JlptBadge } from '../common/Badges';
import '../staff/QuestionPreviewDrawer.css';

/* ── Maps ───────────────────────────────────────────────────────────── */
const STATUS_MAP = {
  draft:          { cls: 'sfq-status--draft',     label: 'Nháp' },
  pending_review: { cls: 'sfq-status--pending',   label: 'Chờ duyệt' },
  published:      { cls: 'sfq-status--published', label: 'Đã xuất bản' },
  rejected:       { cls: 'sfq-status--rejected',  label: 'Từ chối' },
};

const LESSON_TYPE_LABELS = {
  lesson:    'Bài học',
  reading:   'Đọc hiểu',
  listening: 'Luyện nghe',
  speaking:  'Luyện nói',
};

const SKILL_COLORS = {
  vocabulary: { bg: '#E3F2FD', text: '#1565C0' },
  grammar:    { bg: '#F3E5F5', text: '#6A1B9A' },
  kanji:      { bg: '#FCE4EC', text: '#C62828' },
  reading:    { bg: '#E8F5E9', text: '#2E7D32' },
  listening:  { bg: '#FFF3E0', text: '#E65100' },
  mixed:      { bg: '#F0EDEB', text: '#6B625E' },
};
const SKILL_LABELS = {
  vocabulary: 'Từ vựng',
  grammar:    'Ngữ pháp',
  kanji:      'Kanji',
  reading:    'Đọc hiểu',
  listening:  'Nghe',
  mixed:      'Tổng hợp',
};

const QUESTION_TYPE_LABELS = {
  multiple_choice: 'Trắc nghiệm',
  fill_blank:      'Điền vào chỗ trống',
  true_false:      'Đúng / Sai',
};

const DRAWER_TITLES = {
  lesson:     'Xem trước bài học',
  vocabulary: 'Xem trước từ vựng',
  grammar:    'Xem trước ngữ pháp',
  kanji:      'Xem trước Kanji',
  question:   'Xem trước câu hỏi',
  assessment: 'Xem trước quiz / đề thi',
  quiz:       'Xem trước quiz',
  exam:       'Xem trước đề thi',
};

/* ── Shared footer ───────────────────────────────────────────────────── */
function Footer({ data }) {
  const statusInfo = STATUS_MAP[data.status] ?? { cls: 'sfq-status--draft', label: data.status };
  return (
    <div className="sfq-footer-info">
      <span>
        Trạng thái:{' '}
        <span className={`sfq-status-badge ${statusInfo.cls}`}>{statusInfo.label}</span>
      </span>
      {data.submittedBy && <span>Người gửi: <strong>{data.submittedBy}</strong></span>}
      {data.jlptLevel   && <span>Cấp độ: <strong>{data.jlptLevel}</strong></span>}
    </div>
  );
}

/* ── LESSON ─────────────────────────────────────────────────────────── */
function LessonBody({ data }) {
  const d = data.detail ?? {};
  const typeLabel = LESSON_TYPE_LABELS[d.lessonType] ?? d.lessonType ?? 'Bài học';
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={data.jlptLevel} />
        <span className="sfq-type-pill">{typeLabel}</span>
      </div>
      <p className="sfq-question-text">{data.titleOrText}</p>
      {d.contentText && (
        <div>
          <p className="sfq-explanation-label">Nội dung</p>
          <p className="sfq-explanation-text" style={{ whiteSpace: 'pre-wrap' }}>{d.contentText}</p>
        </div>
      )}
      {d.explanation && (
        <div>
          <p className="sfq-explanation-label">Giải thích / Ghi chú</p>
          <p className="sfq-explanation-text">{d.explanation}</p>
        </div>
      )}
      {(d.videoUrl || d.audioUrl) && (
        <div>
          <p className="sfq-explanation-label">Tài nguyên</p>
          {d.videoUrl && <p className="sfq-explanation-text">Video: {d.videoUrl}</p>}
          {d.audioUrl && <p className="sfq-explanation-text">Audio: {d.audioUrl}</p>}
        </div>
      )}
      <Footer data={data} />
    </>
  );
}

/* ── VOCABULARY ─────────────────────────────────────────────────────── */
function VocabBody({ data }) {
  const d = data.detail ?? {};
  const word    = d.word    ?? data.titleOrText;
  const reading = d.furigana;
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={data.jlptLevel} />
        {d.wordType && <span className="sfq-type-pill">{d.wordType}</span>}
        {d.topic    && <span className="sfq-type-pill">{d.topic}</span>}
      </div>

      <div style={{ textAlign: 'center', padding: '16px 0' }}>
        <div style={{ fontSize: 48, fontWeight: 700, color: 'var(--color-text)', lineHeight: 1 }}>{word}</div>
        {reading && (
          <div style={{ fontSize: 18, color: 'var(--color-text-sub)', marginTop: 8 }}>{reading}</div>
        )}
      </div>

      {d.meaning && (
        <div className="sfq-option sfq-option--correct" style={{ justifyContent: 'center', fontSize: 16 }}>
          {d.meaning}
        </div>
      )}

      {(d.exampleSentenceJp || d.exampleSentenceVi) && (
        <div>
          <p className="sfq-explanation-label">Ví dụ</p>
          {d.exampleSentenceJp && (
            <p className="sfq-explanation-text" style={{ fontStyle: 'italic' }}>{d.exampleSentenceJp}</p>
          )}
          {d.exampleSentenceVi && (
            <p className="sfq-explanation-text" style={{ color: 'var(--color-text-sub)', marginTop: 4 }}>
              Dịch: {d.exampleSentenceVi}
            </p>
          )}
        </div>
      )}
      <Footer data={data} />
    </>
  );
}

/* ── GRAMMAR ─────────────────────────────────────────────────────────── */
function GrammarBody({ data }) {
  const d = data.detail ?? {};
  const structure = d.structure ?? data.titleOrText;
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={data.jlptLevel} />
        <span className="sfq-type-pill">Ngữ pháp</span>
      </div>

      <div style={{ background: 'var(--color-primary-bg)', borderRadius: 12, padding: '16px 20px', textAlign: 'center' }}>
        <div style={{ fontSize: 24, fontWeight: 700, color: 'var(--color-primary-dark)', fontFamily: 'monospace' }}>
          {structure}
        </div>
        {d.formula && (
          <div style={{ fontSize: 13, color: 'var(--color-text-sub)', marginTop: 6 }}>
            Cấu tạo: {d.formula}
          </div>
        )}
      </div>

      {d.meaning && (
        <div className="sfq-option sfq-option--correct" style={{ justifyContent: 'center', fontSize: 15 }}>
          {d.meaning}
        </div>
      )}

      {d.usageExplanation && (
        <div>
          <p className="sfq-explanation-label">Giải thích cách dùng</p>
          <p className="sfq-explanation-text">{d.usageExplanation}</p>
        </div>
      )}

      {(d.exampleSentenceJp || d.exampleSentenceVi) && (
        <div>
          <p className="sfq-explanation-label">Ví dụ</p>
          {d.exampleSentenceJp && (
            <p className="sfq-explanation-text" style={{ fontStyle: 'italic' }}>{d.exampleSentenceJp}</p>
          )}
          {d.exampleSentenceVi && (
            <p className="sfq-explanation-text" style={{ color: 'var(--color-text-sub)', marginTop: 4 }}>
              Dịch: {d.exampleSentenceVi}
            </p>
          )}
        </div>
      )}
      <Footer data={data} />
    </>
  );
}

/* ── KANJI ───────────────────────────────────────────────────────────── */
function KanjiBody({ data }) {
  const d   = data.detail ?? {};
  const char = d.characterValue ?? data.titleOrText;
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={data.jlptLevel} />
        {d.strokeCount && <span className="sfq-type-pill">{d.strokeCount} nét</span>}
      </div>

      <div style={{ textAlign: 'center', padding: '20px 0' }}>
        <div style={{ fontSize: 80, fontWeight: 700, color: 'var(--color-text)', lineHeight: 1 }}>{char}</div>
        {d.meaning && (
          <div style={{ fontSize: 14, color: 'var(--color-text-sub)', marginTop: 8 }}>{d.meaning}</div>
        )}
      </div>

      <div style={{ display: 'flex', gap: 12 }}>
        {d.onyomi && (
          <div style={{ flex: 1, background: '#FCE4EC', borderRadius: 10, padding: '12px 16px', textAlign: 'center' }}>
            <div style={{ fontSize: 11, fontWeight: 700, color: '#C62828', marginBottom: 4 }}>Âm on</div>
            <div style={{ fontSize: 18, fontWeight: 700, color: '#C62828' }}>{d.onyomi}</div>
          </div>
        )}
        {d.kunyomi && (
          <div style={{ flex: 1, background: '#E3F2FD', borderRadius: 10, padding: '12px 16px', textAlign: 'center' }}>
            <div style={{ fontSize: 11, fontWeight: 700, color: '#1565C0', marginBottom: 4 }}>Âm kun</div>
            <div style={{ fontSize: 18, fontWeight: 700, color: '#1565C0' }}>{d.kunyomi}</div>
          </div>
        )}
      </div>

      {(d.exampleWord || d.exampleReading || d.exampleMeaning) && (
        <div>
          <p className="sfq-explanation-label">Ví dụ</p>
          <p className="sfq-explanation-text">
            {d.exampleWord    && <strong>{d.exampleWord}</strong>}
            {d.exampleReading && ` (${d.exampleReading})`}
            {d.exampleMeaning && ` — ${d.exampleMeaning}`}
          </p>
        </div>
      )}
      <Footer data={data} />
    </>
  );
}

/* ── QUESTION ────────────────────────────────────────────────────────── */
const OPTION_LETTERS = ['A', 'B', 'C', 'D'];

function QuestionBody({ data }) {
  const d          = data.detail ?? {};
  const skill      = d.skill;
  const skillColor = SKILL_COLORS[skill] ?? SKILL_COLORS.mixed;
  const skillLabel = SKILL_LABELS[skill]  ?? skill;
  const typeLabel  = QUESTION_TYPE_LABELS[d.questionType] ?? d.questionType;

  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={data.jlptLevel} />
        {skill && (
          <span className="sfq-skill-badge" style={{ background: skillColor.bg, color: skillColor.text }}>
            {skillLabel}
          </span>
        )}
        {typeLabel && <span className="sfq-type-pill">{typeLabel}</span>}
      </div>

      <p className="sfq-question-text">{data.titleOrText}</p>

      {d.questionType === 'multiple_choice' && (
        <div className="sfq-options">
          {OPTION_LETTERS.map((letter) => {
            const val = d[`option${letter}`];
            if (!val) return null;
            const isCorrect = d.correctOption === letter;
            return (
              <div key={letter} className={`sfq-option${isCorrect ? ' sfq-option--correct' : ''}`}>
                <span className="sfq-option-letter">{letter}.</span>
                <span>{val}</span>
                {isCorrect && (
                  <span className="sfq-correct-icon" aria-label="Đáp án đúng">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                  </span>
                )}
              </div>
            );
          })}
        </div>
      )}

      {(d.questionType === 'fill_blank' || d.questionType === 'true_false') && d.correctAnswerText && (
        <p className="sfq-answer">
          Đáp án: <strong>{d.correctAnswerText}</strong>
        </p>
      )}

      {d.explanation && (
        <div>
          <p className="sfq-explanation-label">Giải thích</p>
          <p className="sfq-explanation-text">{d.explanation}</p>
        </div>
      )}
      <Footer data={data} />
    </>
  );
}

/* ── ASSESSMENT (quiz / exam) ────────────────────────────────────────── */
function AssessmentBody({ data }) {
  const d = data.detail ?? {};
  const isQuiz = d.assessmentType === 'quiz' || data.contentType === 'quiz';
  const typeLabel = isQuiz ? 'Quiz' : 'Đề thi';
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={data.jlptLevel} />
        <span className="sfq-type-pill">{typeLabel}</span>
      </div>

      <p className="sfq-question-text">{data.titleOrText}</p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 4 }}>
        {d.topic && (
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 16px', background: 'var(--color-bg)', borderRadius: 8, fontSize: 14 }}>
            <span style={{ color: 'var(--color-text-sub)' }}>Chủ đề</span>
            <strong>{d.topic}</strong>
          </div>
        )}
        {d.durationMin != null && (
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 16px', background: 'var(--color-bg)', borderRadius: 8, fontSize: 14 }}>
            <span style={{ color: 'var(--color-text-sub)' }}>Thời gian làm bài</span>
            <strong>{d.durationMin} phút</strong>
          </div>
        )}
        {d.totalScore != null && (
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 16px', background: 'var(--color-bg)', borderRadius: 8, fontSize: 14 }}>
            <span style={{ color: 'var(--color-text-sub)' }}>Tổng điểm</span>
            <strong>{d.totalScore}</strong>
          </div>
        )}
        {d.passScore != null && (
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 16px', background: 'var(--color-secondary-bg)', borderRadius: 8, fontSize: 14 }}>
            <span style={{ color: 'var(--color-text-sub)' }}>Điểm đạt</span>
            <strong style={{ color: 'var(--color-secondary)' }}>{d.passScore}</strong>
          </div>
        )}
      </div>
      <Footer data={data} />
    </>
  );
}

/* ── Main drawer ─────────────────────────────────────────────────────── */
export default function ManagerContentPreviewDrawer({ data, onClose }) {
  useEffect(() => {
    if (!data) return;
    const handleKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [data, onClose]);

  if (!data) return null;

  const ct    = data.contentType ?? '';
  const title = DRAWER_TITLES[ct] ?? 'Xem trước nội dung';

  function renderBody() {
    switch (ct) {
      case 'lesson':     return <LessonBody     data={data} />;
      case 'vocabulary': return <VocabBody      data={data} />;
      case 'grammar':    return <GrammarBody    data={data} />;
      case 'kanji':      return <KanjiBody      data={data} />;
      case 'question':   return <QuestionBody   data={data} />;
      case 'assessment':
      case 'quiz':
      case 'exam':       return <AssessmentBody data={data} />;
      default:           return <LessonBody     data={data} />;
    }
  }

  return (
    <>
      <div className="sfq-drawer-backdrop" onClick={onClose} aria-hidden="true" />
      <aside className="sfq-drawer" role="dialog" aria-modal="true" aria-label={title}>
        <div className="sfq-drawer-header">
          <h2 className="sfq-drawer-header-title">{title}</h2>
          <button type="button" className="sfq-drawer-close" onClick={onClose} aria-label="Đóng">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6"  y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
        <div className="sfq-drawer-body">
          {renderBody()}
        </div>
      </aside>
    </>
  );
}
