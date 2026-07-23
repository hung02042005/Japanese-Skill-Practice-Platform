import { useEffect } from 'react';
import { JlptBadge } from '@/shared/components/common/Badges';
import './QuestionPreviewDrawer.css';

/* ── Skill badge data ────────────────────────────────────────────── */
const SKILL_COLORS = {
  vocabulary: { bg: '#E3F2FD', text: '#1565C0' },
  grammar:    { bg: '#F3E5F5', text: '#6A1B9A' },
  kanji:      { bg: '#FCE4EC', text: '#C62828' },
  listening:  { bg: '#FFF3E0', text: '#E65100' },
  mixed:      { bg: '#F0EDEB', text: '#6B625E' },
};

const SKILL_LABELS = {
  vocabulary: 'Từ vựng',
  grammar:    'Ngữ pháp',
  kanji:      'Kanji',
  listening:  'Nghe',
  mixed:      'Tổng hợp',
};

const TYPE_LABELS = {
  multiple_choice: 'Trắc nghiệm',
  fill_blank:      'Điền vào',
  true_false:      'Đúng/Sai',
};

const STATUS_MAP = {
  draft:          { cls: 'sfq-status--draft',     label: 'Nháp' },
  pending_review: { cls: 'sfq-status--pending',   label: 'Chờ duyệt' },
  published:      { cls: 'sfq-status--published', label: 'Đã xuất bản' },
  rejected:       { cls: 'sfq-status--rejected',  label: 'Từ chối' },
};

/* ── Option letters in order ─────────────────────────────────────── */
const OPTION_LETTERS = ['A', 'B', 'C', 'D'];

/* ── QuestionPreviewDrawer ───────────────────────────────────────── */
export default function QuestionPreviewDrawer({ question, onClose }) {
  /* Close on Escape key */
  useEffect(() => {
    if (!question) return;
    const handleKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [question, onClose]);

  if (!question) return null;

  const skillColor = SKILL_COLORS[question.skill] ?? SKILL_COLORS.mixed;
  const skillLabel = SKILL_LABELS[question.skill] ?? question.skill;
  const typeLabel  = TYPE_LABELS[question.questionType] ?? question.questionType;
  const statusInfo = STATUS_MAP[question.status] ?? { cls: 'sfq-status--draft', label: question.status };

  return (
    <>
      {/* Backdrop */}
      <div
        className="sfq-drawer-backdrop"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Drawer panel */}
      <aside className="sfq-drawer" role="dialog" aria-modal="true" aria-label="Xem trước câu hỏi">
        {/* Header */}
        <div className="sfq-drawer-header">
          <h2 className="sfq-drawer-header-title">Xem trước câu hỏi</h2>
          <button
            type="button"
            className="sfq-drawer-close"
            onClick={onClose}
            aria-label="Đóng"
          >
            {/* X icon */}
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6"  y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="sfq-drawer-body">
          {/* Badge row */}
          <div className="sfq-badge-row">
            <JlptBadge level={question.jlptLevel} />
            <span
              className="sfq-skill-badge"
              style={{ background: skillColor.bg, color: skillColor.text }}
            >
              {skillLabel}
            </span>
            <span className="sfq-type-pill">{typeLabel}</span>
          </div>

          {/* Question text */}
          <p className="sfq-question-text">{question.questionText}</p>

          {/* ── Multiple choice options ─────────────────────── */}
          {question.questionType === 'multiple_choice' && (
            <div className="sfq-options">
              {OPTION_LETTERS.map((letter) => {
                const optValue = question['option' + letter];
                if (!optValue) return null;
                const isCorrect = question.correctOption === letter;
                return (
                  <div
                    key={letter}
                    className={`sfq-option${isCorrect ? ' sfq-option--correct' : ''}`}
                  >
                    <span className="sfq-option-letter">{letter}.</span>
                    <span>{optValue}</span>
                    {isCorrect && (
                      <span className="sfq-correct-icon" aria-label="Đáp án đúng">
                        {/* Check icon */}
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

          {/* ── Fill blank / True/False answer ──────────────── */}
          {(question.questionType === 'fill_blank' || question.questionType === 'true_false') && (
            <p className="sfq-answer">
              Đáp án: <strong>{question.correctAnswerText}</strong>
            </p>
          )}

          {/* Explanation */}
          {question.explanation && (
            <div>
              <p className="sfq-explanation-label">Giải thích</p>
              <p className="sfq-explanation-text">{question.explanation}</p>
            </div>
          )}

          {/* Footer info */}
          <div className="sfq-footer-info">
            <span>
              Trạng thái:{' '}
              <span className={`sfq-status-badge ${statusInfo.cls}`}>
                {statusInfo.label}
              </span>
            </span>
            <span>
              ID: <strong>#{question.questionId ?? question.id}</strong>
            </span>
          </div>
        </div>
      </aside>
    </>
  );
}
