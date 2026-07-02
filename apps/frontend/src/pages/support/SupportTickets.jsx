import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { ToastContainer, useToast } from '../../components/common/Toast';
import TicketStatusBadge from '../../components/support/TicketStatusBadge';
import PriorityPill from '../../components/support/PriorityPill';
import CreateTicketModal from '../../components/support/CreateTicketModal';
import { getMyTickets } from '../../api/studentService';
import { formatRelativeTime } from '../../utils/date';
import './SupportTickets.css';

const FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'open', label: 'Đang mở' },
  { value: 'in_progress', label: 'Đang xử lý' },
  { value: 'resolved', label: 'Đã giải quyết' },
  { value: 'closed', label: 'Đã đóng' },
];
const PAGE_SIZE = 10;

export default function SupportTickets() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [tickets, setTickets] = useState([]);
  const [isLoading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatus] = useState('');
  const [page, setPage] = useState(0); // 0-based
  const [totalPages, setTotal] = useState(1);
  const [showCreate, setCreate] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getMyTickets({ status: statusFilter, page, size: PAGE_SIZE });
      setTickets(data?.content ?? []);
      setTotal(data?.totalPages ?? 1);
    } catch {
      setError('Không thể tải danh sách yêu cầu hỗ trợ.');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, page]);

  useEffect(() => { load(); }, [load]);

  function changeFilter(value) {
    setStatus(value);
    setPage(0);
  }

  function handleCreated(ticket) {
    setCreate(false);
    addToast('success', 'Đã gửi yêu cầu hỗ trợ');
    navigate(`/support/tickets/${ticket.ticketId}`);
  }

  return (
    <div className="tks-page">
      <TopNav activeTab="" />

      <main className="tks-body">
        <Link to="/dashboard" className="tks-back-link">← Quay lại Dashboard</Link>

        <div className="tks-head">
          <h1 className="tks-title">Hỗ Trợ Của Tôi</h1>
          <button type="button" className="tks-create-btn" onClick={() => setCreate(true)}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
            Tạo yêu cầu mới
          </button>
        </div>

        <div className="tks-filters" role="tablist" aria-label="Lọc trạng thái">
          {FILTERS.map((f) => (
            <button
              key={f.value || 'all'}
              type="button"
              role="tab"
              aria-selected={statusFilter === f.value}
              className={`tks-filter-tab${statusFilter === f.value ? ' tks-filter-tab--active' : ''}`}
              onClick={() => changeFilter(f.value)}
            >
              {f.label}
            </button>
          ))}
        </div>

        {isLoading && (
          <div className="tks-list" aria-hidden="true">
            {Array.from({ length: 4 }).map((_, i) => <div key={i} className="tks-skel-card" />)}
          </div>
        )}

        {!isLoading && error && (
          <div className="tks-error" role="alert">
            <p>{error}</p>
            <button type="button" className="tks-retry" onClick={load}>Thử lại</button>
          </div>
        )}

        {!isLoading && !error && tickets.length === 0 && (
          <EmptyState
            title="Chưa có yêu cầu hỗ trợ"
            subtitle="Gặp khó khăn? Hãy tạo yêu cầu, đội ngũ SakuJi luôn sẵn sàng giúp bạn."
            mascotVariant="idle"
            mascotSize={140}
          >
            <button type="button" className="tks-create-btn tks-empty-cta" onClick={() => setCreate(true)}>
              Tạo yêu cầu mới
            </button>
          </EmptyState>
        )}

        {!isLoading && !error && tickets.length > 0 && (
          <>
            <div className="tks-list" role="list">
              {tickets.map((t) => (
                <Link
                  key={t.ticketId}
                  to={`/support/tickets/${t.ticketId}`}
                  className="tks-card"
                  role="listitem"
                  aria-label={`${t.subject} — ${t.status}`}
                >
                  <div className="tks-card-top">
                    <PriorityPill priority={t.priority} />
                    {t.category && <span className="tks-card-cat">{t.category}</span>}
                    <TicketStatusBadge status={t.status} />
                  </div>
                  <div className="tks-card-subject">{t.subject}</div>
                  <div className="tks-card-meta">
                    {(t.replyCount ?? 0) > 0 && <span>{t.replyCount} phản hồi · </span>}
                    cập nhật {formatRelativeTime(t.lastReplyAt ?? t.createdAt)}
                  </div>
                </Link>
              ))}
            </div>

            <Pagination
              currentPage={page + 1}
              totalPages={totalPages}
              onChange={(p) => setPage(p - 1)}
            />
          </>
        )}
      </main>

      {showCreate && (
        <CreateTicketModal onClose={() => setCreate(false)} onCreated={handleCreated} />
      )}

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
