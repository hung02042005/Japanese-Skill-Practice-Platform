import { useState, useEffect, useCallback } from 'react';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { getMyNotifications, markNotificationRead, markAllNotificationsRead } from '../../api/studentService';
import './MyNotifications.css';

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

const TYPE_ICONS = {
  news: '📰', warning: '⚠️', promotion: '🎁', system: '⚙️', achievement: '🏆', reminder: '⏰',
};

export default function MyNotifications() {
  const { toasts, addToast, removeToast } = useToast();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getMyNotifications();
      setNotifications(res.content ?? []);
      setUnreadCount(res.unreadCount ?? 0);
    } catch {
      addToast('error', 'Không thể tải thông báo.');
    } finally {
      setLoading(false);
    }
  }, [addToast]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleMarkRead(notificationId) {
    try {
      await markNotificationRead(notificationId);
      setNotifications((prev) =>
        prev.map((n) => (n.notificationId === notificationId ? { ...n, isRead: true } : n)),
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch {
      addToast('error', 'Không thể đánh dấu đã đọc.');
    }
  }

  async function handleMarkAllRead() {
    try {
      await markAllNotificationsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
      addToast('success', 'Đã đánh dấu tất cả là đã đọc');
    } catch {
      addToast('error', 'Không thể đánh dấu tất cả đã đọc.');
    }
  }

  return (
    <div className="ntf-page">
      <TopNav activeTab="" />
      <main className="ntf-body">
        <div className="ntf-header-bar">
          <h1 className="ntf-title">
            Thông Báo
            {unreadCount > 0 && <span className="ntf-unread-badge">{unreadCount}</span>}
          </h1>
          {unreadCount > 0 && (
            <button type="button" className="ntf-markall-btn" onClick={handleMarkAllRead}>
              Đánh dấu tất cả đã đọc
            </button>
          )}
        </div>

        {isLoading ? (
          <div className="ntf-loading">Đang tải...</div>
        ) : notifications.length === 0 ? (
          <EmptyState
            title="Chưa có thông báo nào"
            subtitle="Các thông báo về học tập và hệ thống sẽ xuất hiện ở đây."
          />
        ) : (
          <div className="ntf-list">
            {notifications.map((n) => (
              <div
                key={n.notificationId}
                className={`ntf-item${n.isRead ? '' : ' ntf-item--unread'}`}
                onClick={() => !n.isRead && handleMarkRead(n.notificationId)}
              >
                <span className="ntf-icon" aria-hidden="true">{TYPE_ICONS[n.notificationType] ?? '🔔'}</span>
                <div className="ntf-item-body">
                  <div className="ntf-item-title">{n.title}</div>
                  <p className="ntf-item-content">{n.content}</p>
                  <span className="ntf-item-time">{formatDateTime(n.createdAt)}</span>
                </div>
                {!n.isRead && <span className="ntf-dot" aria-label="Chưa đọc" />}
              </div>
            ))}
          </div>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
