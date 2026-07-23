import './ReviewFeedbackModal.css';

const ACTION_META = {
  reject_content: { label: 'Bị từ chối', className: 'rfm-badge--rejected' },
  request_changes_content: { label: 'Yêu cầu chỉnh sửa', className: 'rfm-badge--changes' },
};

function formatDateTime(value) {
  if (!value) return '';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

/**
 * Modal đọc phản hồi của manager (từ chối / yêu cầu chỉnh sửa).
 * Props:
 *   isOpen     – boolean
 *   onClose    – () => void
 *   feedback   – string   (nội dung phản hồi)
 *   actionType – string   ("reject_content" | "request_changes_content")
 *   reviewedAt – string   (ISO datetime)
 */
export default function ReviewFeedbackModal({ isOpen, onClose, feedback, actionType, reviewedAt }) {
  if (!isOpen) return null;

  const meta = ACTION_META[actionType];
  const hasNoFeedback = !feedback && !actionType;

  function handleBackdropClick(e) {
    if (e.target === e.currentTarget) onClose();
  }

  return (
    <div className="rfm-backdrop" onClick={handleBackdropClick} role="dialog" aria-modal="true" aria-labelledby="rfm-title">
      <div className="rfm-modal">
        <div className="rfm-header">
          <div className="rfm-header-left">
            <h2 id="rfm-title" className="rfm-title">Phản hồi từ Manager</h2>
            {meta && <span className={`rfm-badge ${meta.className}`}>{meta.label}</span>}
          </div>
          <button className="rfm-close" onClick={onClose} aria-label="Đóng">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
            </svg>
          </button>
        </div>

        <div className="rfm-body">
          {hasNoFeedback ? (
            <div className="rfm-no-feedback">
              Nội dung này chưa có phản hồi nào từ manager.
            </div>
          ) : (
            <>
              {reviewedAt && (
                <p className="rfm-date">Thời gian: {formatDateTime(reviewedAt)}</p>
              )}
              <div className="rfm-feedback-box">
                {feedback}
              </div>
            </>
          )}
        </div>

        <div className="rfm-footer">
          <button className="rfm-btn-close" onClick={onClose}>Đóng</button>
        </div>
      </div>
    </div>
  );
}
