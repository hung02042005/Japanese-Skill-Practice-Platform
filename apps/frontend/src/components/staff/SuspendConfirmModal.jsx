import { useEffect } from 'react';

export default function SuspendConfirmModal({
  modal,
  reason,
  onReasonChange,
  isActioning,
  onConfirm,
  onClose,
}) {
  const { student, action } = modal;
  const isSuspend = action === 'suspend';

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
              Lý do (không bắt buộc):
            </label>
            <input
              id="sst-reason-input"
              type="text"
              className="sst-modal-reason-input"
              placeholder="Nhập lý do..."
              value={reason}
              onChange={(e) => onReasonChange(e.target.value)}
              maxLength={255}
            />
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
            disabled={isActioning}
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
