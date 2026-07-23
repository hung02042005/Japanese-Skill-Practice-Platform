import { useState, useEffect, useRef } from 'react';
import SakuChan from '@/shared/components/common/SakuChan';
import PriorityPill from '@/features/public/components/PriorityPill';
import TicketStatusBadge from '@/features/public/components/TicketStatusBadge';
import { formatDateTime, formatRelativeTime } from '@/shared/utils/date';
import { LinkIcon } from '@/shared/components/common/AppIcons';
import './TicketDetail.css';

const CLOSED_STATUSES = ['resolved', 'closed'];

function getInitial(name) {
  if (!name) return '?';
  const parts = name.trim().split(' ');
  return parts[parts.length - 1].charAt(0).toUpperCase();
}

/**
 * Chi tiết ticket (dùng chung Staff + Manager) — backend shape.
 * Props:
 *   detail            — TicketDetailResponse + replies[] (senderRole STUDENT|STAFF)
 *   onReply(message, attachmentUrl) => Promise<boolean>  (true = thành công → clear form)
 *   onClose() => Promise<void>
 *   isSending, isClosing
 *   headerExtra       — node chèn dưới header (AssignPanel cho Manager)
 *   isLoading         — skeleton
 */
export default function TicketDetail({
  detail,
  onReply,
  onClose,
  isSending = false,
  isClosing = false,
  headerExtra = null,
  isLoading = false,
}) {
  const [replyText, setReply] = useState('');
  const [attachUrl, setAttach] = useState('');
  const [showConfirm, setConfirm] = useState(false);
  const threadBottomRef = useRef(null);

  useEffect(() => {
    setReply('');
    setAttach('');
    setConfirm(false);
  }, [detail?.ticketId]);

  useEffect(() => {
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    threadBottomRef.current?.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth' });
  }, [detail?.replies?.length]);

  if (isLoading) {
    return (
      <div className="tkt-detail-col">
        <div className="tkt-skel-head" aria-hidden="true" />
        <div className="tkt-skel-bubble" aria-hidden="true" />
        <div className="tkt-skel-bubble tkt-skel-bubble--right" aria-hidden="true" />
        <div className="tkt-skel-bubble" aria-hidden="true" />
      </div>
    );
  }

  if (!detail) {
    return (
      <div className="tkt-detail-col">
        <div className="tkt-empty-detail">
          <SakuChan variant="idle" size={80} aria-hidden="true" />
          <p>Chọn một ticket để bắt đầu hỗ trợ học viên</p>
        </div>
      </div>
    );
  }

  const isClosed = CLOSED_STATUSES.includes(detail.status);

  async function handleSend(e) {
    e.preventDefault();
    if (!replyText.trim() || isSending) return;
    const ok = await onReply(replyText.trim(), attachUrl.trim() || undefined);
    if (ok) { setReply(''); setAttach(''); }
  }

  // Nội dung gốc = bubble student đầu tiên
  const thread = [
    { replyId: 'origin', senderRole: 'STUDENT', senderName: detail.studentName, message: detail.content, createdAt: detail.createdAt },
    ...(detail.replies ?? []),
  ];

  return (
    <div className="tkt-detail-col">
      <div className="tkt-detail-header">
        <h2 className="tkt-detail-subject">{detail.subject}</h2>
        <div className="tkt-badge-row">
          <PriorityPill priority={detail.priority} />
          {detail.category && <span className="tkt-detail-cat">{detail.category}</span>}
          <TicketStatusBadge status={detail.status} />
        </div>
        <div className="tkt-detail-meta">
          <span>Học viên: {detail.studentName}</span>
          <span aria-hidden="true">|</span>
          <span>Gửi: {formatDateTime(detail.createdAt)}</span>
          <span aria-hidden="true">|</span>
          <span>Được giao: {detail.assignedToStaffName ?? '— chưa giao'}</span>
        </div>
      </div>

      {headerExtra}

      <div className="tkt-thread" role="log" aria-live="polite" aria-label="Cuộc hội thoại">
        {thread.map((r) => {
          const isStaff = r.senderRole === 'STAFF';
          return (
            <div key={r.replyId} className={`tkt-msg${isStaff ? ' tkt-msg--staff' : ' tkt-msg--student'}`}>
              <div
                className="tkt-msg-avatar"
                aria-hidden="true"
                style={isStaff
                  ? { background: 'var(--color-primary-bg)', color: 'var(--color-primary-dark)' }
                  : { background: 'var(--color-secondary-bg)', color: 'var(--color-secondary)' }}
              >
                {getInitial(r.senderName)}
              </div>
              <div className="tkt-msg-bubble">
                <span className="tkt-msg-name">{r.senderName}</span>
                <p className="tkt-msg-text">{r.message}</p>
                {r.attachmentUrl && (
                  <a className="tkt-msg-attach" href={r.attachmentUrl} target="_blank" rel="noreferrer"><LinkIcon size={14} /> Tệp đính kèm</a>
                )}
                <span className="tkt-msg-time">{formatRelativeTime(r.createdAt)}</span>
              </div>
            </div>
          );
        })}
        <div ref={threadBottomRef} />
      </div>

      {isClosed ? (
        <div className="tkt-closed-banner" role="status">
          Ticket đã đóng. Không thể gửi thêm phản hồi.
        </div>
      ) : (
        <form className="tkt-reply-form" onSubmit={handleSend} aria-label="Form phản hồi">
          <textarea
            className="tkt-reply-textarea"
            rows={3}
            placeholder="Nhập phản hồi cho học viên..."
            value={replyText}
            onChange={(e) => setReply(e.target.value)}
            aria-label="Nội dung phản hồi"
            aria-required="true"
          />
          <input
            className="tkt-attach-input"
            type="url"
            placeholder="Đính kèm URL (tùy chọn)"
            value={attachUrl}
            maxLength={500}
            onChange={(e) => setAttach(e.target.value)}
          />
          <div className="tkt-reply-footer">
            <button
              type="button"
              className="tkt-close-btn"
              onClick={() => setConfirm(true)}
              disabled={isClosing}
            >
              {isClosing ? 'Đang đóng…' : 'Đóng ticket'}
            </button>
            <button type="submit" className="tkt-reply-btn" disabled={!replyText.trim() || isSending} aria-busy={isSending}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M22 2L11 13" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M22 2L15 22L11 13L2 9L22 2Z" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              {isSending ? 'Đang gửi…' : 'Gửi phản hồi'}
            </button>
          </div>
        </form>
      )}

      {showConfirm && (
        <div className="tkt-confirm-overlay" onMouseDown={() => setConfirm(false)}>
          <div className="tkt-confirm" role="alertdialog" aria-modal="true" aria-labelledby="tkt-confirm-title" onMouseDown={(e) => e.stopPropagation()}>
            <h3 id="tkt-confirm-title" className="tkt-confirm-title">Đóng ticket này?</h3>
            <p className="tkt-confirm-text">Ticket sẽ chuyển sang trạng thái đã giải quyết và học viên được thông báo.</p>
            <div className="tkt-confirm-actions">
              <button type="button" className="tkt-close-btn" onClick={() => setConfirm(false)}>Hủy</button>
              <button type="button" className="tkt-reply-btn tkt-confirm-danger" onClick={async () => { setConfirm(false); await onClose(); }}>
                Xác nhận đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
