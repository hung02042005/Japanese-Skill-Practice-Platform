import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { createTicket, getMyTickets, getMyTicketDetail, replyToMyTicket } from '../../api/studentService';
import './MySupportTickets.css';

const STATUS_LABELS = {
  open: 'Mở',
  in_progress: 'Đang xử lý',
  resolved: 'Đã giải quyết',
  closed: 'Đã đóng',
};

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

export default function MySupportTickets() {
  const { toasts, addToast, removeToast } = useToast();

  const [tickets, setTickets] = useState([]);
  const [isLoadingList, setLoadingList] = useState(true);
  const [selectedId, setSelected] = useState(null);
  const [detail, setDetail] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [subject, setSubject] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState('technical');
  const [priority, setPriority] = useState('normal');
  const [creating, setCreating] = useState(false);

  const [replyText, setReply] = useState('');
  const [sending, setSending] = useState(false);

  const loadTickets = useCallback(async () => {
    setLoadingList(true);
    try {
      const res = await getMyTickets();
      setTickets(res.content ?? []);
    } catch {
      addToast('error', 'Không thể tải danh sách ticket.');
    } finally {
      setLoadingList(false);
    }
  }, [addToast]);

  useEffect(() => {
    loadTickets();
  }, [loadTickets]);

  useEffect(() => {
    if (!selectedId) {
      setDetail(null);
      return;
    }
    let active = true;
    getMyTicketDetail(selectedId)
      .then((d) => active && setDetail(d))
      .catch(() => active && addToast('error', 'Không thể tải chi tiết ticket.'));
    return () => { active = false; };
  }, [selectedId, addToast]);

  async function handleCreate(e) {
    e.preventDefault();
    if (!subject.trim() || !content.trim()) return;
    setCreating(true);
    try {
      const created = await createTicket({ subject, content, category, priority });
      setSubject('');
      setContent('');
      setShowForm(false);
      addToast('success', 'Tạo ticket hỗ trợ thành công');
      await loadTickets();
      setSelected(created.ticketId);
    } catch {
      addToast('error', 'Không thể tạo ticket. Vui lòng thử lại.');
    } finally {
      setCreating(false);
    }
  }

  async function handleReply(e) {
    e.preventDefault();
    if (!replyText.trim() || !selectedId) return;
    setSending(true);
    try {
      await replyToMyTicket(selectedId, replyText);
      setReply('');
      const refreshed = await getMyTicketDetail(selectedId);
      setDetail(refreshed);
      addToast('success', 'Gửi phản hồi thành công');
    } catch {
      addToast('error', 'Không thể gửi phản hồi. Ticket có thể đã đóng.');
    } finally {
      setSending(false);
    }
  }

  const isClosed = detail?.status === 'closed' || detail?.status === 'resolved';

  return (
    <div className="sup-page">
      <TopNav activeTab="" />
      <main className="sup-body">
        <div className="sup-header-bar">
          <h1 className="sup-title">Hỗ Trợ Của Tôi</h1>
          <button type="button" className="sup-new-btn" onClick={() => setShowForm((v) => !v)}>
            {showForm ? 'Đóng' : '+ Tạo ticket mới'}
          </button>
        </div>

        {showForm && (
          <form className="sup-create-card" onSubmit={handleCreate}>
            <div className="sup-field">
              <label className="sup-field-label" htmlFor="sup-subject">Tiêu đề *</label>
              <input
                id="sup-subject"
                className="sup-input"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                placeholder="Mô tả ngắn gọn vấn đề của bạn..."
                maxLength={255}
              />
            </div>
            <div className="sup-field">
              <label className="sup-field-label" htmlFor="sup-content">Nội dung *</label>
              <textarea
                id="sup-content"
                className="sup-textarea"
                rows={4}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Mô tả chi tiết vấn đề bạn đang gặp..."
              />
            </div>
            <div className="sup-row">
              <div className="sup-field">
                <label className="sup-field-label" htmlFor="sup-category">Danh mục</label>
                <select id="sup-category" className="sup-select" value={category} onChange={(e) => setCategory(e.target.value)}>
                  <option value="technical">Kỹ thuật</option>
                  <option value="content">Nội dung học tập</option>
                  <option value="billing">Thanh toán</option>
                  <option value="other">Khác</option>
                </select>
              </div>
              <div className="sup-field">
                <label className="sup-field-label" htmlFor="sup-priority">Mức độ ưu tiên</label>
                <select id="sup-priority" className="sup-select" value={priority} onChange={(e) => setPriority(e.target.value)}>
                  <option value="low">Thấp</option>
                  <option value="normal">Thường</option>
                  <option value="high">Cao</option>
                  <option value="urgent">Khẩn cấp</option>
                </select>
              </div>
            </div>
            <div className="sup-form-footer">
              <button type="submit" className="sup-submit-btn" disabled={creating || !subject.trim() || !content.trim()}>
                {creating ? 'Đang gửi...' : 'Gửi ticket'}
              </button>
            </div>
          </form>
        )}

        <div className="sup-master-detail">
          <div className="sup-list-col">
            {isLoadingList ? (
              <div className="sup-loading">Đang tải...</div>
            ) : tickets.length === 0 ? (
              <EmptyState
                title="Bạn chưa có ticket nào"
                subtitle="Tạo ticket mới nếu bạn cần hỗ trợ kỹ thuật hoặc thắc mắc học tập."
              />
            ) : (
              tickets.map((t) => (
                <div
                  key={t.ticketId}
                  className={`sup-ticket-card${selectedId === t.ticketId ? ' sup-ticket-card--active' : ''}`}
                  onClick={() => setSelected(t.ticketId)}
                >
                  <div className="sup-ticket-top">
                    <span className="sup-ticket-subject">{t.subject}</span>
                    <span className="sup-ticket-status">{STATUS_LABELS[t.status] ?? t.status}</span>
                  </div>
                  <div className="sup-ticket-meta">
                    <span>{formatDateTime(t.createdAt)}</span>
                    <span>·</span>
                    <span>{t.replyCount ?? 0} phản hồi</span>
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="sup-detail-col">
            {!detail ? (
              <div className="sup-empty-detail">
                <p>Chọn một ticket để xem chi tiết</p>
              </div>
            ) : (
              <>
                <div className="sup-detail-header">
                  <h2 className="sup-detail-subject">{detail.subject}</h2>
                  <span className="sup-detail-status">{STATUS_LABELS[detail.status] ?? detail.status}</span>
                </div>
                <p className="sup-detail-content">{detail.content}</p>

                <div className="sup-thread">
                  {(detail.replies ?? []).map((r) => (
                    <div key={r.replyId} className={`sup-msg${r.senderRole === 'STAFF' ? ' sup-msg--staff' : ''}`}>
                      <span className="sup-msg-name">{r.senderName}</span>
                      <p className="sup-msg-text">{r.message}</p>
                      <span className="sup-msg-time">{formatDateTime(r.createdAt)}</span>
                    </div>
                  ))}
                </div>

                {isClosed ? (
                  <div className="sup-closed-banner" role="alert">
                    Ticket này đã đóng. Không thể gửi thêm phản hồi.
                  </div>
                ) : (
                  <form className="sup-reply-form" onSubmit={handleReply}>
                    <textarea
                      className="sup-reply-textarea"
                      rows={3}
                      placeholder="Nhập phản hồi của bạn..."
                      value={replyText}
                      onChange={(e) => setReply(e.target.value)}
                    />
                    <button type="submit" className="sup-reply-btn" disabled={sending || !replyText.trim()}>
                      {sending ? 'Đang gửi...' : 'Gửi phản hồi'}
                    </button>
                  </form>
                )}
              </>
            )}
          </div>
        </div>

        <Link to="/notifications" className="sup-notif-link">Xem thông báo của tôi →</Link>
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
