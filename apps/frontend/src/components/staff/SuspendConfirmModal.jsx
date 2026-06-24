import { useEffect } from 'react';

export default function SuspendConfirmModal({
  modal,
  reason,
  onReasonChange,
  isActioning,
  actionError,
  onConfirm,
  onClose,
}) {
  const { student, action } = modal;
  const isSuspend = action === 'suspend';
  const reasonLen = reason?.trim().length ?? 0;
  const reasonValid = !isSuspend || (reasonLen >= 10 && reasonLen <= 500);

  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div
      className="sst-modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="sst-modal-title"
      onClick={onClose}
    >
      <div className="sst-modal" onClick={(e) => e.stopPropagation()}>
        <div className="sst-modal-icon">{isSuspend ? '⚠️' : '✅'}</div>

        <h3 id="sst-modal-title" className="sst-modal-title">
          {isSuspend ? 'Xác nhận Suspend' : 'Kích hoạt lại tài khoản'}
        </h3>

        <p className="sst-modal-desc">
          {isSuspend
            ? `Bạn sắp khoá tài khoản của ${student.fullName}. Học viên sẽ không thể đăng nhập cho đến khi được kích hoạt lại.`
            : `Bạn sắp kích hoạt lại tài khoản của ${student.fullName}. Học viên sẽ có thể đăng nhập bình thường.`}
        </p>

        {isSuspend && (
          <div className="sst-modal-reason">
            <label htmlFor="sst-reason-input" className="sst-modal-reason-label">
              Lý do đình chỉ{' '}
              <span aria-hidden="true" style={{ color: 'var(--color-error, #ef4444)' }}>*</span>{' '}
              <span style={{ fontWeight: 400, opacity: 0.7 }}>(10–500 ký tự)</span>
            </label>
            <textarea
              id="sst-reason-input"
              className="sst-modal-reason-input"
              placeholder="Nhập lý do đình chỉ tài khoản... (bắt buộc, tối thiểu 10 ký tự)"
              value={reason}
              onChange={(e) => onReasonChange(e.target.value)}
              maxLength={500}
              rows={3}
              style={{ resize: 'vertical', width: '100%' }}
            />
            <div style={{ fontSize: '0.75rem', textAlign: 'right', opacity: 0.6 }}>
              {reasonLen}/500 ký tự
              {reasonLen > 0 && reasonLen < 10 && (
                <span style={{ color: 'var(--color-error, #ef4444)', marginLeft: '0.5rem' }}>
                  (cần thêm {10 - reasonLen} ký tự)
                </span>
              )}
            </div>
          </div>
        )}

        {actionError && (
          <div role="alert" style={{
            color: 'var(--color-error, #ef4444)',
            background: 'rgba(239,68,68,0.08)',
            borderRadius: '6px',
            padding: '8px 12px',
            fontSize: '0.85rem',
            marginTop: '8px',
          }}>
            {actionError}
          </div>
        )}

        <div className="sst-modal-actions">
          <button
            className="sst-modal-btn-cancel"
            onClick={onClose}
            disabled={isActioning}
          >
            Hủy
          </button>
          <button
            className={`sst-modal-btn-confirm${isSuspend ? ' sst-modal-btn-confirm--danger' : ''}`}
            onClick={onConfirm}
            disabled={isActioning || (isSuspend && !reasonValid)}
            aria-label={
              isSuspend
                ? `Xác nhận suspend ${student.fullName}`
                : `Xác nhận kích hoạt ${student.fullName}`
            }
          >
            {isActioning
              ? 'Đang xử lý...'
              : isSuspend
              ? 'Xác nhận Suspend'
              : 'Kích hoạt lại'}
          </button>
        </div>
      </div>
    </div>
  );
}
