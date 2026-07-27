import { useEffect } from 'react';
import { JlptBadge } from '@/shared/components/common/Badges';

const SKILL_LABELS = {
  vocabulary: 'Từ vựng',
  grammar:    'Ngữ pháp',
  kanji:      'Kanji',
  listening:  'Nghe',
  mixed:      'Tổng hợp',
};

const STATUS_META = {
  draft:          { label: 'Nháp',         bg: '#F0EDEB',                        color: 'var(--color-text-sub)' },
  pending_review: { label: 'Chờ duyệt',    bg: 'var(--color-accent-bg)',          color: 'var(--color-warning)' },
  published:      { label: 'Đã xuất bản',  bg: 'var(--color-secondary-bg)',       color: 'var(--color-secondary)' },
  rejected:       { label: 'Từ chối',      bg: '#FFEAEA',                         color: 'var(--color-error)' },
};

export default function AssessmentPreviewModal({ item, mode, onClose }) {
  const isQuiz = mode === 'quiz';

  useEffect(() => {
    if (!item) return;
    const handleKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [item, onClose]);

  if (!item) return null;

  const statusInfo = STATUS_META[item.status] ?? STATUS_META.draft;

  return (
    <div
      className="sfa-modal-backdrop"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      role="dialog"
      aria-modal="true"
      aria-label={item.title}
    >
      <div className="sfa-modal sfa-preview-modal">
        <div className="sfa-modal-header">
          <h2 className="sfa-modal-title">{item.title}</h2>
          <button type="button" className="sfa-modal-close" onClick={onClose} aria-label="Đóng">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div className="sfa-modal-body">
          {/* Badge row */}
          <div className="sfa-preview-badges">
            <JlptBadge level={item.jlptLevel} />
            <span className="sfa-preview-badge" style={{ background: statusInfo.bg, color: statusInfo.color }}>
              {statusInfo.label}
            </span>
            {isQuiz && item.skill && (
              <span className="sfa-preview-badge sfa-preview-badge--skill">
                {SKILL_LABELS[item.skill] ?? item.skill}
              </span>
            )}
          </div>

          {/* Detail rows */}
          <div className="sfa-preview-details">
            <div className="sfa-preview-row">
              <span className="sfa-preview-label">Loại</span>
              <span className="sfa-preview-value">{isQuiz ? 'Quiz' : 'Đề thi'}</span>
            </div>
            <div className="sfa-preview-row">
              <span className="sfa-preview-label">Cấp độ JLPT</span>
              <span className="sfa-preview-value">{item.jlptLevel}</span>
            </div>
            {isQuiz && item.skill && (
              <div className="sfa-preview-row">
                <span className="sfa-preview-label">Kỹ năng</span>
                <span className="sfa-preview-value">{SKILL_LABELS[item.skill] ?? item.skill}</span>
              </div>
            )}
            <div className="sfa-preview-row">
              <span className="sfa-preview-label">Số câu hỏi</span>
              <span className="sfa-preview-value">{item.questionCount} câu</span>
            </div>
            {!isQuiz && item.duration && (
              <div className="sfa-preview-row">
                <span className="sfa-preview-label">Thời gian</span>
                <span className="sfa-preview-value">{item.duration} phút</span>
              </div>
            )}
            <div className="sfa-preview-row">
              <span className="sfa-preview-label">Trạng thái</span>
              <span className="sfa-preview-value">{statusInfo.label}</span>
            </div>
            <div className="sfa-preview-row">
              <span className="sfa-preview-label">Cập nhật</span>
              <span className="sfa-preview-value">{item.updatedAt}</span>
            </div>
            {item.description && (
              <div className="sfa-preview-row sfa-preview-row--desc">
                <span className="sfa-preview-label">Mô tả</span>
                <span className="sfa-preview-value">{item.description}</span>
              </div>
            )}
          </div>
        </div>

        <div className="sfa-modal-footer">
          <button type="button" className="sfa-btn-ghost" onClick={onClose}>Đóng</button>
        </div>
      </div>
    </div>
  );
}
