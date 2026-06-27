import NotificationTypeIcon from './NotificationTypeIcon';
import { formatRelativeTime } from '../../utils/date';
import './NotificationItem.css';

/**
 * Props:
 *   notification — NotificationResponse
 *   onClick      — (notification) => void
 *   as           — 'button' (mặc định) cho keyboard/menuitem
 */
export default function NotificationItem({ notification: n, onClick }) {
  const unread = n.isRead === false;
  return (
    <button
      type="button"
      role="menuitem"
      className={`ntf-item${unread ? ' ntf-item--unread' : ''}`}
      onClick={() => onClick?.(n)}
      aria-label={`${n.title}${unread ? ' — chưa đọc' : ''}`}
    >
      {unread && <span className="ntf-dot" aria-hidden="true" />}
      <NotificationTypeIcon type={n.notificationType} />
      <span className="ntf-body">
        <span className="ntf-title">{n.title}</span>
        <span className="ntf-text">{n.content}</span>
        <span className="ntf-time">{formatRelativeTime(n.createdAt)}</span>
      </span>
    </button>
  );
}
