import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import NotificationItem from './NotificationItem';
import {
  getMyNotifications,
  markNotificationRead,
  markAllNotificationsRead,
} from '@/shared/api/studentService';
import './NotificationBell.css';

const POLL_MS = 60000;
const PREVIEW_SIZE = 8;

// Điều hướng theo ruleKey (xem SPEC-notifications §7). Không suy ticketId từ replyId.
function routeFor(ruleKey) {
  if (!ruleKey) return null;
  if (ruleKey.startsWith('ticket_')) return '/support';
  if (ruleKey.startsWith('speaking_graded_')) return '/speaking';
  return null;
}

export default function NotificationBell() {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState([]);
  const [unread, setUnread] = useState(0);
  const [isLoading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const wrapRef = useRef(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getMyNotifications({ page: 0, size: PREVIEW_SIZE });
      setItems(data?.content ?? []);
      setUnread(data?.unreadCount ?? 0);
    } catch {
      setError('Không tải được thông báo');
    } finally {
      setLoading(false);
    }
  }, []);

  // Tải lần đầu + polling khi tab hiển thị.
  useEffect(() => {
    load();
    const id = setInterval(() => {
      if (document.visibilityState === 'visible') load();
    }, POLL_MS);
    return () => clearInterval(id);
  }, [load]);

  // Click ngoài + Escape để đóng.
  useEffect(() => {
    if (!open) return;
    function onOutside(e) {
      if (wrapRef.current && !wrapRef.current.contains(e.target)) setOpen(false);
    }
    function onKey(e) {
      if (e.key === 'Escape') setOpen(false);
    }
    document.addEventListener('mousedown', onOutside);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onOutside);
      document.removeEventListener('keydown', onKey);
    };
  }, [open]);

  async function handleItemClick(n) {
    if (n.isRead === false) {
      // Optimistic
      setItems((arr) => arr.map((x) => (x.notificationId === n.notificationId ? { ...x, isRead: true } : x)));
      setUnread((c) => Math.max(0, c - 1));
      try {
        await markNotificationRead(n.notificationId);
      } catch {
        load(); // revert bằng cách đồng bộ lại
      }
    }
    const target = routeFor(n.ruleKey);
    setOpen(false);
    if (target) navigate(target);
  }

  async function handleMarkAll() {
    setItems((arr) => arr.map((x) => ({ ...x, isRead: true })));
    setUnread(0);
    try {
      await markAllNotificationsRead();
    } catch {
      load();
    }
  }

  return (
    <div className="ntf-bell" ref={wrapRef}>
      <button
        type="button"
        className="ntf-bell-btn"
        onClick={() => setOpen((v) => !v)}
        aria-label={`Thông báo, ${unread} chưa đọc`}
        aria-expanded={open}
        aria-haspopup="menu"
      >
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <path d="M13.7 21a2 2 0 0 1-3.4 0" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        </svg>
        {unread > 0 && <span className="ntf-badge" aria-hidden="true">{unread > 9 ? '9+' : unread}</span>}
      </button>

      {open && (
        <div className="ntf-panel" role="menu" aria-label="Danh sách thông báo">
          <div className="ntf-panel-head">
            <span className="ntf-panel-title">Thông báo</span>
            {unread > 0 && (
              <button type="button" className="ntf-markall" onClick={handleMarkAll}>
                Đánh dấu tất cả đã đọc
              </button>
            )}
          </div>

          {isLoading && (
            <div className="ntf-panel-skel" aria-hidden="true">
              {Array.from({ length: 4 }).map((_, i) => <div key={i} className="ntf-skel-row" />)}
            </div>
          )}

          {!isLoading && error && (
            <div className="ntf-panel-msg">
              {error}. <button type="button" className="ntf-markall" onClick={load}>Thử lại</button>
            </div>
          )}

          {!isLoading && !error && items.length === 0 && (
            <div className="ntf-panel-msg">Bạn chưa có thông báo nào.</div>
          )}

          {!isLoading && !error && items.length > 0 && (
            <div className="ntf-panel-list">
              {items.map((n) => (
                <NotificationItem key={n.notificationId} notification={n} onClick={handleItemClick} />
              ))}
            </div>
          )}

          <div className="ntf-footer">
            <button
              type="button"
              className="ntf-viewall"
              onClick={() => { setOpen(false); navigate('/notifications'); }}
            >
              Xem tất cả →
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
