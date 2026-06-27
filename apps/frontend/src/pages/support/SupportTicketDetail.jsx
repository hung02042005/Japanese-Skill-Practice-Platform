import { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useParams } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { UserAvatar } from '../../components/common/UserAvatar';
import { ToastContainer, useToast } from '../../components/common/Toast';
import TicketStatusBadge from '../../components/support/TicketStatusBadge';
import PriorityPill from '../../components/support/PriorityPill';
import { getTicketDetail, replyTicket, closeMyTicket } from '../../api/studentService';
import { formatRelativeTime, formatDateTime } from '../../utils/date';
import './SupportTicketDetail.css';

const CLOSED_STATUSES = ['resolved', 'closed'];

export default function SupportTicketDetail() {
  const { ticketId } = useParams();
  const { toasts, addToast, removeToast } = useToast();

  const [detail, setDetail] = useState(null);
  const [isLoading, setLoading] = useState(true);
  const [error, setError] = useState(''); // '' | 'NOT_FOUND' | 'FORBIDDEN' | 'GENERIC'
  const [replyText, setReply] = useState('');
  const [attachUrl, setAttach] = useState('');
  const [isSending, setSending] = useState(false);
  const [isClosing, setClosing] = useState(false);
  const [showConfirm, setConfirm] = useState(false);
  const threadBottomRef = useRef(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getTicketDetail(ticketId);
      setDetail(data);
    } catch (err) {
      const status = err?.response?.status;
      if (status === 404) setError('NOT_FOUND');
      else if (status === 403) setError('FORBIDDEN');
      else setError('GENERIC');
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    threadBottomRef.current?.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth' });
  }, [detail?.replies?.length]);

  const isClosed = detail && CLOSED_STATUSES.includes(detail.status);

  async function handleSend(e) {
    e.preventDefault();
    if (!replyText.trim() || isSending) return;
    setSending(true);
    try {
      const reply = await replyTicket(ticketId, {
        message: replyText.trim(),
        attachmentUrl: attachUrl.trim() || undefined,
      });
      setDetail((d) => ({ ...d, replies: [...(d.replies ?? []), reply] }));
      setReply('');
      setAttach('');
      addToast('success', 'Đã gửi phản hồi');
    } catch (err) {
      const msg = err?.response?.data?.message ?? 'Không thể gửi phản hồi. Vui lòng thử lại.';
      addToast('error', msg);
      if (err?.response?.status === 409) load(); // ticket vừa bị đóng → đồng bộ
    } finally {
      setSending(false);
    }
  }

  async function handleClose() {
    setConfirm(false);
    setClosing(true);
    try {
      const updated = await closeMyTicket(ticketId);
      setDetail((d) => ({ ...d, status: updated.status, resolvedAt: updated.resolvedAt }));
      addToast('success', 'Đã đóng ticket');
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Không thể đóng ticket.');
    } finally {
      setClosing(false);
    }
  }

  function renderError() {
    const map = {
      NOT_FOUND: 'Không tìm thấy ticket này.',
      FORBIDDEN: 'Bạn không có quyền xem ticket này.',
      GENERIC: 'Không thể tải ticket. Vui lòng thử lại.',
    };
    return (
      <div className="tkd-error" role="alert">
        <p>{map[error]}</p>
        {error === 'GENERIC' && <button type="button" className="tkd-retry" onClick={load}>Thử lại</button>}
        <Link to="/support" className="tkd-back-link">← Về danh sách hỗ trợ</Link>
      </div>
    );
  }

  return (
    <div className="tkd-page">
      <TopNav activeTab="" />

      <main className="tkd-body">
        <Link to="/support" className="tkd-back-link">← Quay lại danh sách hỗ trợ</Link>

        {isLoading && (
          <div className="tkd-skel" aria-hidden="true">
            <div className="tkd-skel-head" />
            {Array.from({ length: 3 }).map((_, i) => <div key={i} className="tkd-skel-bubble" />)}
          </div>
        )}

        {!isLoading && error && renderError()}

        {!isLoading && !error && detail && (
          <>
            <header className="tkd-header">
              <h1 className="tkd-subject">{detail.subject}</h1>
              <div className="tkd-badges">
                <PriorityPill priority={detail.priority} />
                {detail.category && <span className="tkd-cat">{detail.category}</span>}
                <TicketStatusBadge status={detail.status} />
              </div>
              <div className="tkd-meta">
                {detail.assignedToStaffName && <>Được hỗ trợ bởi: <strong>{detail.assignedToStaffName}</strong> · </>}
                Gửi lúc {formatDateTime(detail.createdAt)}
              </div>
            </header>

            <div className="tkd-thread" aria-live="polite">
              {/* Nội dung gốc — bubble đầu của học viên */}
              <div className="tkd-msg tkd-msg--me">
                <div className="tkd-msg-bubble">
                  <span className="tkd-msg-name">Bạn</span>
                  <p className="tkd-msg-text">{detail.content}</p>
                  <span className="tkd-msg-time">{formatRelativeTime(detail.createdAt)}</span>
                </div>
              </div>

              {(detail.replies ?? []).map((r) => {
                const isMe = r.senderRole === 'STUDENT';
                return (
                  <div key={r.replyId} className={`tkd-msg${isMe ? ' tkd-msg--me' : ''}`}>
                    {!isMe && <UserAvatar name={r.senderName} userType="staff" size={32} />}
                    <div className="tkd-msg-bubble">
                      <span className="tkd-msg-name">{isMe ? 'Bạn' : r.senderName}</span>
                      <p className="tkd-msg-text">{r.message}</p>
                      {r.attachmentUrl && (
                        <a className="tkd-msg-attach" href={r.attachmentUrl} target="_blank" rel="noreferrer">
                          🔗 Tệp đính kèm
                        </a>
                      )}
                      <span className="tkd-msg-time">{formatRelativeTime(r.createdAt)}</span>
                    </div>
                  </div>
                );
              })}
              <div ref={threadBottomRef} />
            </div>

            {isClosed ? (
              <div className="tkd-closed-banner" role="status">
                Ticket này đã được xử lý/đóng. Không thể gửi thêm phản hồi.
              </div>
            ) : (
              <form className="tkd-reply" onSubmit={handleSend}>
                <label className="tkd-sr-only" htmlFor="tkd-reply-input">Nhập phản hồi</label>
                <textarea
                  id="tkd-reply-input"
                  className="tkd-reply-input"
                  rows={3}
                  value={replyText}
                  onChange={(e) => setReply(e.target.value)}
                  placeholder="Nhập phản hồi của bạn…"
                  aria-required="true"
                />
                <input
                  className="tkd-attach-input"
                  type="url"
                  value={attachUrl}
                  onChange={(e) => setAttach(e.target.value)}
                  placeholder="🔗 Đính kèm URL (tùy chọn)"
                  maxLength={500}
                />
                <div className="tkd-reply-actions">
                  <button
                    type="button"
                    className="tkd-close-btn"
                    onClick={() => setConfirm(true)}
                    disabled={isClosing}
                  >
                    {isClosing ? 'Đang đóng…' : 'Đóng ticket'}
                  </button>
                  <button
                    type="submit"
                    className="tkd-send-btn"
                    disabled={!replyText.trim() || isSending}
                    aria-busy={isSending}
                  >
                    {isSending ? 'Đang gửi…' : 'Gửi phản hồi →'}
                  </button>
                </div>
              </form>
            )}
          </>
        )}
      </main>

      {showConfirm && (
        <div className="tkd-confirm-overlay" onMouseDown={() => setConfirm(false)}>
          <div className="tkd-confirm" role="alertdialog" aria-modal="true" aria-labelledby="tkd-confirm-title" onMouseDown={(e) => e.stopPropagation()}>
            <h3 id="tkd-confirm-title" className="tkd-confirm-title">Đóng ticket này?</h3>
            <p className="tkd-confirm-text">Bạn sẽ không thể gửi thêm phản hồi sau khi đóng.</p>
            <div className="tkd-confirm-actions">
              <button type="button" className="tkd-close-btn" onClick={() => setConfirm(false)}>Hủy</button>
              <button type="button" className="tkd-send-btn tkd-confirm-danger" onClick={handleClose}>Xác nhận đóng</button>
            </div>
          </div>
        </div>
      )}

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
