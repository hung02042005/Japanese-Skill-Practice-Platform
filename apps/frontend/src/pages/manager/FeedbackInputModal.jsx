import { useState, useEffect, useRef } from 'react';
import './FeedbackInputModal.css';

/**
 * Modal nhập lý do khi manager từ chối hoặc yêu cầu chỉnh sửa nội dung.
 * Props:
 *   isOpen   – boolean
 *   title    – tiêu đề hiển thị
 *   onSubmit – (feedback: string) => void
 *   onCancel – () => void
 */
export default function FeedbackInputModal({ isOpen, title, onSubmit, onCancel }) {
  const [text, setText] = useState('');
  const textareaRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      setText('');
      setTimeout(() => textareaRef.current?.focus(), 50);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  function handleSubmit(e) {
    e.preventDefault();
    const trimmed = text.trim();
    if (!trimmed) return;
    onSubmit(trimmed);
  }

  function handleBackdropClick(e) {
    if (e.target === e.currentTarget) onCancel();
  }

  return (
    <div className="fim-backdrop" onClick={handleBackdropClick} role="dialog" aria-modal="true" aria-labelledby="fim-title">
      <div className="fim-modal">
        <div className="fim-header">
          <h2 id="fim-title" className="fim-title">{title}</h2>
          <button className="fim-close" onClick={onCancel} aria-label="Đóng">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="fim-body">
            <label className="fim-label" htmlFor="fim-textarea">
              Lý do <span className="fim-required">*</span>
            </label>
            <textarea
              id="fim-textarea"
              ref={textareaRef}
              className="fim-textarea"
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="Nhập lý do chi tiết để staff có thể hiểu và chỉnh sửa..."
              rows={5}
              maxLength={2000}
            />
            <span className="fim-char-count">{text.length}/2000</span>
          </div>

          <div className="fim-footer">
            <button type="button" className="fim-btn-cancel" onClick={onCancel}>
              Hủy
            </button>
            <button type="submit" className="fim-btn-submit" disabled={!text.trim()}>
              Xác nhận
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
