import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { ToastContainer, useToast } from '../../components/common/Toast';
import NotificationItem from '../../components/notifications/NotificationItem';
import {
  getMyNotifications,
  markNotificationRead,
  markAllNotificationsRead,
} from '../../api/studentService';
import './Notifications.css';

const PAGE_SIZE = 20;

function routeFor(ruleKey) {
  if (!ruleKey) return null;
  if (ruleKey.startsWith('ticket_')) return '/support';
  if (ruleKey.startsWith('speaking_graded_')) return '/speaking';
  return null;
}

export default function Notifications() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [items, setItems] = useState([]);
  const [unread, setUnread] = useState(0);
  const [isLoading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('all'); // 'all' | 'unread'
  const [page, setPage] = useState(0);
  const [totalPages, setTotal] = useState(1);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getMyNotifications({ page, size: PAGE_SIZE });
      setItems(data?.content ?? []);
      setUnread(data?.unreadCount ?? 0);
      setTotal(data?.totalPages ?? 1);
    } catch {
      setError('Không thể tải thông báo.');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  async function handleItemClick(n) {
    if (n.isRead === false) {
      setItems((arr) => arr.map((x) => (x.notificationId === n.notificationId ? { ...x, isRead: true } : x)));
      setUnread((c) => Math.max(0, c - 1));
      try {
        await markNotificationRead(n.notificationId);
      } catch {
        load();
      }
    }
    const target = routeFor(n.ruleKey);
    if (target) navigate(target);
  }

  async function handleMarkAll() {
    setItems((arr) => arr.map((x) => ({ ...x, isRead: true })));
    setUnread(0);
    try {
      const { markedCount } = await markAllNotificationsRead();
      addToast('success', `Đã đánh dấu ${markedCount ?? 0} thông báo là đã đọc`);
    } catch {
      addToast('error', 'Không thể đánh dấu. Vui lòng thử lại.');
      load();
    }
  }

  const visible = tab === 'unread' ? items.filter((n) => n.isRead === false) : items;

  return (
    <div className="ntf-page">
      <TopNav activeTab="" />

      <main className="ntf-page-body">
        <Link to="/dashboard" className="ntf-back-link">← Quay lại Dashboard</Link>

        <div className="ntf-page-head">
          <h1 className="ntf-page-title">Thông Báo</h1>
          {unread > 0 && (
            <button type="button" className="ntf-markall-btn" onClick={handleMarkAll}>
              Đánh dấu tất cả đã đọc
            </button>
          )}
        </div>

        <div className="ntf-tabs" role="tablist" aria-label="Lọc thông báo">
          <button
            type="button" role="tab" aria-selected={tab === 'all'}
            className={`ntf-tab${tab === 'all' ? ' ntf-tab--active' : ''}`}
            onClick={() => setTab('all')}
          >
            Tất cả
          </button>
          <button
            type="button" role="tab" aria-selected={tab === 'unread'}
            className={`ntf-tab${tab === 'unread' ? ' ntf-tab--active' : ''}`}
            onClick={() => setTab('unread')}
          >
            Chưa đọc {unread > 0 && <span className="ntf-tab-count">{unread}</span>}
          </button>
        </div>

        {isLoading && (
          <div className="ntf-page-card" aria-hidden="true">
            {Array.from({ length: 6 }).map((_, i) => <div key={i} className="ntf-page-skel" />)}
          </div>
        )}

        {!isLoading && error && (
          <div className="ntf-page-error" role="alert">
            <p>{error}</p>
            <button type="button" className="ntf-retry" onClick={load}>Thử lại</button>
          </div>
        )}

        {!isLoading && !error && visible.length === 0 && (
          tab === 'unread'
            ? <EmptyState title="Bạn đã đọc hết thông báo!" subtitle="Không còn thông báo chưa đọc nào 🌸" mascotVariant="happy" mascotSize={140} />
            : <EmptyState title="Chưa có thông báo" subtitle="Khi có cập nhật mới, SakuJi sẽ báo cho bạn ở đây." mascotVariant="idle" mascotSize={140} />
        )}

        {!isLoading && !error && visible.length > 0 && (
          <>
            <div className="ntf-page-card" role="list">
              {visible.map((n) => (
                <NotificationItem key={n.notificationId} notification={n} onClick={handleItemClick} />
              ))}
            </div>
            {tab === 'all' && (
              <Pagination currentPage={page + 1} totalPages={totalPages} onChange={(p) => setPage(p - 1)} />
            )}
          </>
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
