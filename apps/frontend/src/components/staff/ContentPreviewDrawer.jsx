import { useEffect } from 'react';
import { JlptBadge } from '../common/Badges';
import './QuestionPreviewDrawer.css'; // reuse same drawer styles

/* ── Shared meta maps ────────────────────────────────────────────── */
const STATUS_MAP = {
  draft:          { cls: 'sfq-status--draft',     label: 'Nháp' },
  pending_review: { cls: 'sfq-status--pending',   label: 'Chờ duyệt' },
  published:      { cls: 'sfq-status--published', label: 'Đã xuất bản' },
  rejected:       { cls: 'sfq-status--rejected',  label: 'Từ chối' },
  archived:       { cls: 'sfq-status--draft',     label: 'Lưu trữ' },
};

const LESSON_TYPE_LABELS = {
  lesson:    'Bài học',
  reading:   'Đọc hiểu',
  listening: 'Luyện nghe',
  speaking:  'Luyện nói',
};

/* ── Shared footer ───────────────────────────────────────────────── */
function DrawerFooter({ item }) {
  const statusInfo = STATUS_MAP[item.status] ?? { cls: 'sfq-status--draft', label: item.status };
  return (
    <div className="sfq-footer-info">
      <span>
        Trạng thái:{' '}
        <span className={`sfq-status-badge ${statusInfo.cls}`}>
          {statusInfo.label}
        </span>
      </span>
      {item.jlptLevel && (
        <span>Cấp độ: <strong>{item.jlptLevel}</strong></span>
      )}
      {item.updatedAt && (
        <span>Cập nhật: <strong>{item.updatedAt}</strong></span>
      )}
    </div>
  );
}

/* ── Content renderers by type ───────────────────────────────────── */
function CourseBody({ item }) {
  const desc = item.explanation || item.description;
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={item.jlptLevel} />
        <span className="sfq-type-pill">Khóa học</span>
      </div>
      <p className="sfq-question-text">{item.title}</p>
      {desc && (
        <div>
          <p className="sfq-explanation-label">Mô tả</p>
          <p className="sfq-explanation-text">{desc}</p>
        </div>
      )}
      <DrawerFooter item={item} />
    </>
  );
}

function LessonBody({ item }) {
  const typeLabel = LESSON_TYPE_LABELS[item.lessonType] || item.lessonType || 'Bài học';
  const content = item.contentText || item.content;
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={item.jlptLevel} />
        <span className="sfq-type-pill">{typeLabel}</span>
      </div>
      <p className="sfq-question-text">{item.title}</p>
      {content && (
        <div>
          <p className="sfq-explanation-label">Nội dung</p>
          <p className="sfq-explanation-text">{content}</p>
        </div>
      )}
      {item.explanation && (
        <div>
          <p className="sfq-explanation-label">Mô tả / Ghi chú</p>
          <p className="sfq-explanation-text">{item.explanation}</p>
        </div>
      )}
      <DrawerFooter item={item} />
    </>
  );
}

function SpeakingBody({ item }) {
  const questions = item.questions?.length
    ? item.questions
    : [{ promptText: item.contentText, sampleAudioUrl: item.audioUrl }].filter((q) => q.promptText);

  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={item.jlptLevel} />
        <span className="sfq-type-pill">Luyện nói</span>
      </div>
      <p className="sfq-question-text">{item.title}</p>
      {questions.map((question, index) => (
        <div className="sfq-explanation-text" key={question.speakingQuestionId ?? index}>
          <p className="sfq-explanation-label">Câu {index + 1}</p>
          <p lang="ja" style={{ whiteSpace: 'pre-wrap' }}>{question.promptText}</p>
          {question.instruction && <p>{question.instruction}</p>}
          {question.sampleAudioUrl && <audio controls preload="none" src={question.sampleAudioUrl} style={{ width: '100%' }} />}
        </div>
      ))}
      <DrawerFooter item={item} />
    </>
  );
}

function VocabBody({ item }) {
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={item.jlptLevel} />
        {item.partOfSpeech && (
          <span className="sfq-type-pill">{item.partOfSpeech}</span>
        )}
      </div>

      {/* Main word */}
      <div style={{ textAlign: 'center', padding: '16px 0' }}>
        <div style={{ fontSize: 48, fontWeight: 700, color: 'var(--color-text)', lineHeight: 1 }}>
          {item.word}
        </div>
        {item.reading && (
          <div style={{ fontSize: 18, color: 'var(--color-text-sub)', marginTop: 8 }}>
            {item.reading}
          </div>
        )}
      </div>

      {/* Meaning */}
      <div className="sfq-option sfq-option--correct" style={{ justifyContent: 'center', fontSize: 16 }}>
        {item.meaning}
      </div>

      {(item.exampleSentenceJp || item.example) && (
        <div>
          <p className="sfq-explanation-label">Ví dụ</p>
          <p className="sfq-explanation-text" style={{ fontStyle: 'italic' }}>
            {item.exampleSentenceJp || item.example}
          </p>
          {item.exampleSentenceVi && (
            <p className="sfq-explanation-text" style={{ color: 'var(--color-text-sub)', marginTop: 4 }}>
              Dịch: {item.exampleSentenceVi}
            </p>
          )}
        </div>
      )}
      <DrawerFooter item={item} />
    </>
  );
}

function GrammarBody({ item }) {
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={item.jlptLevel} />
        <span className="sfq-type-pill">Ngữ pháp</span>
      </div>

      {/* Pattern */}
      <div style={{ background: 'var(--color-primary-bg)', borderRadius: 12, padding: '16px 20px', textAlign: 'center' }}>
        <div style={{ fontSize: 24, fontWeight: 700, color: 'var(--color-primary-dark)', fontFamily: 'monospace' }}>
          {item.structure || item.pattern}
        </div>
        {(item.formula || item.formation) && (
          <div style={{ fontSize: 13, color: 'var(--color-text-sub)', marginTop: 6 }}>
            Cấu tạo: {item.formula || item.formation}
          </div>
        )}
      </div>

      {/* Meaning */}
      <div className="sfq-option sfq-option--correct" style={{ justifyContent: 'center', fontSize: 15 }}>
        {item.meaning}
      </div>

      {item.usageExplanation && (
        <div>
          <p className="sfq-explanation-label">Giải thích cách dùng</p>
          <p className="sfq-explanation-text">{item.usageExplanation}</p>
        </div>
      )}

      {(item.exampleSentenceJp || item.example) && (
        <div>
          <p className="sfq-explanation-label">Ví dụ</p>
          <p className="sfq-explanation-text" style={{ fontStyle: 'italic' }}>
            {item.exampleSentenceJp || item.example}
          </p>
          {item.exampleSentenceVi && (
            <p className="sfq-explanation-text" style={{ color: 'var(--color-text-sub)', marginTop: 4 }}>
              Dịch: {item.exampleSentenceVi}
            </p>
          )}
        </div>
      )}
      <DrawerFooter item={item} />
    </>
  );
}

function KanjiBody({ item }) {
  const char = item.characterValue || item.character;
  const hasExample = item.exampleWord || item.example;
  return (
    <>
      <div className="sfq-badge-row">
        <JlptBadge level={item.jlptLevel} />
        {item.strokeCount && (
          <span className="sfq-type-pill">{item.strokeCount} nét</span>
        )}
      </div>

      {/* Big kanji character */}
      <div style={{ textAlign: 'center', padding: '20px 0' }}>
        <div style={{ fontSize: 80, fontWeight: 700, color: 'var(--color-text)', lineHeight: 1 }}>
          {char}
        </div>
        <div style={{ fontSize: 14, color: 'var(--color-text-sub)', marginTop: 8 }}>
          {item.meaning}
        </div>
      </div>

      {/* Readings */}
      <div style={{ display: 'flex', gap: 12 }}>
        {item.onyomi && (
          <div style={{ flex: 1, background: '#FCE4EC', borderRadius: 10, padding: '12px 16px', textAlign: 'center' }}>
            <div style={{ fontSize: 11, fontWeight: 700, color: '#C62828', marginBottom: 4 }}>Âm on</div>
            <div style={{ fontSize: 18, fontWeight: 700, color: '#C62828' }}>{item.onyomi}</div>
          </div>
        )}
        {item.kunyomi && (
          <div style={{ flex: 1, background: '#E3F2FD', borderRadius: 10, padding: '12px 16px', textAlign: 'center' }}>
            <div style={{ fontSize: 11, fontWeight: 700, color: '#1565C0', marginBottom: 4 }}>Âm kun</div>
            <div style={{ fontSize: 18, fontWeight: 700, color: '#1565C0' }}>{item.kunyomi}</div>
          </div>
        )}
      </div>

      {hasExample && (
        <div>
          <p className="sfq-explanation-label">Ví dụ</p>
          <p className="sfq-explanation-text">
            {item.exampleWord && <strong>{item.exampleWord}</strong>}
            {item.exampleReading && ` (${item.exampleReading})`}
            {item.exampleMeaning && ` — ${item.exampleMeaning}`}
            {!item.exampleWord && item.example}
          </p>
        </div>
      )}
      <DrawerFooter item={item} />
    </>
  );
}

/* ── ContentPreviewDrawer ────────────────────────────────────────── */
const DRAWER_TITLES = {
  course:     'Xem trước khóa học',
  lesson:     'Xem trước bài học',
  vocabulary: 'Xem trước từ vựng',
  grammar:    'Xem trước ngữ pháp',
  kanji:      'Xem trước Kanji',
  speaking:   'Xem trước bài luyện nói',
};

export default function ContentPreviewDrawer({ item, contentType, onClose }) {
  /* Close on Escape */
  useEffect(() => {
    if (!item) return;
    const handleKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [item, onClose]);

  if (!item) return null;

  const title = DRAWER_TITLES[contentType] ?? 'Xem trước nội dung';

  const renderBody = () => {
    switch (contentType) {
      case 'course':     return <CourseBody  item={item} />;
      case 'lesson':     return <LessonBody  item={item} />;
      case 'vocabulary': return <VocabBody   item={item} />;
      case 'grammar':    return <GrammarBody item={item} />;
      case 'kanji':      return <KanjiBody   item={item} />;
      case 'speaking':   return <SpeakingBody item={item} />;
      default:           return <LessonBody  item={item} />;
    }
  };

  return (
    <>
      {/* Backdrop */}
      <div className="sfq-drawer-backdrop" onClick={onClose} aria-hidden="true" />

      {/* Drawer panel */}
      <aside className="sfq-drawer" role="dialog" aria-modal="true" aria-label={title}>
        {/* Header */}
        <div className="sfq-drawer-header">
          <h2 className="sfq-drawer-header-title">{title}</h2>
          <button
            type="button"
            className="sfq-drawer-close"
            onClick={onClose}
            aria-label="Đóng"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6"  y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="sfq-drawer-body">
          {renderBody()}
        </div>
      </aside>
    </>
  );
}
