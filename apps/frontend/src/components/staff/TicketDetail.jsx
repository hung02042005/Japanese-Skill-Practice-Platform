import { useState, useEffect, useRef } from 'react';
import SakuChan from '../../components/auth/SakuChan';
import './TicketDetail.css';

const PRIORITY_CONFIG = {
  urgent: { bg: '#FFEAEA', color: 'var(--color-error)', label: 'Khẩn cấp' },
  high:   { bg: 'var(--color-accent-bg)', color: 'var(--color-warning)', label: 'Cao' },
  normal: { bg: 'var(--color-secondary-bg)', color: 'var(--color-secondary)', label: 'Thường' },
  low:    { bg: 'var(--color-bg)', color: 'var(--color-text-disabled)', label: 'Thấp' },
};

const STATUS_CONFIG = {
  open:        { bg: '#FFEAEA', color: 'var(--color-error)', label: 'Mở' },
  in_progress: { bg: 'var(--color-accent-bg)', color: 'var(--color-warning)', label: 'Đang xử lý' },
  resolved:    { bg: 'var(--color-secondary-bg)', color: 'var(--color-secondary)', label: 'Đã giải quyết' },
  closed:      { bg: '#F0EDEB', color: 'var(--color-text-disabled)', label: 'Đã đóng' },
};

function formatDateTime(isoStr) {
  const d = new Date(isoStr);
  return d.toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

function getInitials(name) {
  if (!name) return '?';
  const parts = name.trim().split(' ');
  return parts[parts.length - 1].charAt(0).toUpperCase();
}

export default function TicketDetail({ ticket, replies, onReply, onStatusChange }) {
  const [replyText, setReply] = useState('');
  const [newStatus, setNewStatus] = useState('');
  const threadBottomRef = useRef(null);

  useEffect(() => {
    setReply('');
    setNewStatus('');
  }, [ticket?.id]);

  useEffect(() => {
    if (threadBottomRef.current) {
      threadBottomRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [ticket?.id, replies]);

  if (!ticket) {
    return (
      <div className="tkt-detail-col">
        <div className="tkt-empty-detail">
          <SakuChan variant="idle" size={80} aria-hidden="true" />
          <p>Chọn một ticket để bắt đầu hỗ trợ học viên</p>
        </div>
      </div>
    );
  }

  const priorityCfg = PRIORITY_CONFIG[ticket.priority] ?? PRIORITY_CONFIG.normal;
  const statusCfg = STATUS_CONFIG[ticket.status] ?? STATUS_CONFIG.open;
  const isClosed = ticket.status === 'closed';

  function handleSend() {
    if (!replyText.trim()) return;
    onReply(replyText.trim(), newStatus || null);
    setReply('');
    setNewStatus('');
  }

  return (
    <div className="tkt-detail-col">
      {/* Header */}
      <div className="tkt-detail-header">
        <h2 className="tkt-detail-subject">{ticket.subject}</h2>
        <div className="tkt-badge-row">
          <span
            className="tkt-priority-pill"
            style={{ background: priorityCfg.bg, color: priorityCfg.color }}
          >
            {priorityCfg.label}
          </span>
          <span
            className="tkt-status-badge"
            style={{ background: statusCfg.bg, color: statusCfg.color }}
          >
            {statusCfg.label}
          </span>
        </div>
        <div className="tkt-detail-meta">
          <span>Học viên: {ticket.studentName}</span>
          <span aria-hidden="true">|</span>
          <span>Level: {ticket.jlptLevel}</span>
          <span aria-hidden="true">|</span>
          <span>Gửi: {formatDateTime(ticket.createdAt)}</span>
        </div>
      </div>

      {/* Thread */}
      <div className="tkt-thread" role="log" aria-live="polite" aria-label="Cuộc hội thoại">
        {replies.map((reply) => {
          const isStaff = reply.senderType === 'staff';
          return (
            <div
              key={reply.replyId}
              className={`tkt-msg${isStaff ? ' tkt-msg--staff' : ' tkt-msg--student'}`}
            >
              <div
                className="tkt-msg-avatar"
                aria-hidden="true"
                style={
                  isStaff
                    ? { background: 'var(--color-primary-bg)', color: 'var(--color-primary-dark)' }
                    : { background: 'var(--color-secondary-bg)', color: 'var(--color-secondary)' }
                }
              >
                {getInitials(reply.senderName)}
              </div>
              <div className="tkt-msg-bubble">
                <span className="tkt-msg-name">{reply.senderName}</span>
                <p className="tkt-msg-text">{reply.message}</p>
                <span className="tkt-msg-time">{formatDateTime(reply.createdAt)}</span>
              </div>
            </div>
          );
        })}
        <div ref={threadBottomRef} />
      </div>

      {/* Reply form or closed banner */}
      {isClosed ? (
        <div className="tkt-closed-banner" role="alert">
          Ticket này đã đóng. Không thể gửi thêm phản hồi.
        </div>
      ) : (
        <form
          className="tkt-reply-form"
          onSubmit={(e) => { e.preventDefault(); handleSend(); }}
          aria-label="Form phản hồi"
        >
          <textarea
            className="tkt-reply-textarea"
            rows={3}
            placeholder="Nhập phản hồi cho học viên..."
            value={replyText}
            onChange={(e) => setReply(e.target.value)}
            aria-label="Nội dung phản hồi"
          />
          <div className="tkt-reply-footer">
            <select
              className="tkt-status-select"
              value={newStatus}
              onChange={(e) => setNewStatus(e.target.value)}
              aria-label="Thay đổi trạng thái ticket"
            >
              <option value="">-- Giữ nguyên --</option>
              <option value="open">Mở (open)</option>
              <option value="in_progress">Đang xử lý (in_progress)</option>
              <option value="resolved">Đã giải quyết (resolved)</option>
              <option value="closed">Đóng (closed)</option>
            </select>
            <button
              type="submit"
              className="tkt-reply-btn"
              disabled={!replyText.trim()}
              aria-label="Gửi phản hồi"
            >
              {/* Send icon */}
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M22 2L11 13" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M22 2L15 22L11 13L2 9L22 2Z" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              Gửi phản hồi
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
