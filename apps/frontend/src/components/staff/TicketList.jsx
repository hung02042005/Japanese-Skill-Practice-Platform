import './TicketList.css';

const PRIORITY_DOT_COLOR = {
  urgent: 'var(--color-error)',
  high: 'var(--color-warning)',
  normal: 'var(--color-secondary)',
  low: 'var(--color-text-disabled)',
};

const STATUS_TABS = [
  { value: 'open', label: 'Mở' },
  { value: 'in_progress', label: 'Đang xử lý' },
  { value: 'resolved', label: 'Đã giải quyết' },
  { value: '', label: 'Tất cả' },
];

function relativeTime(isoStr) {
  const now = new Date('2026-06-03T12:00:00');
  const d = new Date(isoStr);
  const diff = Math.floor((now - d) / 60000);
  if (diff < 60) return `${diff} phút trước`;
  if (diff < 1440) return `${Math.floor(diff / 60)} giờ trước`;
  return `${Math.floor(diff / 1440)} ngày trước`;
}

export default function TicketList({
  tickets,
  selectedId,
  statusFilter,
  onSelect,
  onStatusFilterChange,
}) {
  return (
    <div className="tkt-list-col">
      <div className="tkt-status-tabs" role="tablist" aria-label="Lọc trạng thái ticket">
        {STATUS_TABS.map((tab) => {
          const count =
            tab.value === ''
              ? tickets.length
              : tickets.filter((t) => t.status === tab.value).length;
          const isActive = statusFilter === tab.value;
          return (
            <button
              key={tab.value}
              role="tab"
              aria-selected={isActive}
              className={`tkt-status-tab${isActive ? ' tkt-status-tab--active' : ''}`}
              onClick={() => onStatusFilterChange(tab.value)}
            >
              {tab.label}
              {count > 0 && (
                <span style={{ marginLeft: 4, opacity: 0.7 }}>({count})</span>
              )}
            </button>
          );
        })}
      </div>

      <div className="tkt-list-scroll" role="listbox" aria-label="Danh sách ticket">
        {tickets.length === 0 ? (
          <div className="tkt-empty-list">Không có ticket nào.</div>
        ) : (
          tickets.map((ticket) => (
            <div
              key={ticket.id}
              role="option"
              aria-selected={selectedId === ticket.id}
              className={`tkt-ticket-card${selectedId === ticket.id ? ' tkt-ticket-card--active' : ''}`}
              onClick={() => onSelect(ticket.id)}
            >
              <div className="tkt-card-top">
                <span
                  className="tkt-priority-dot"
                  style={{ background: PRIORITY_DOT_COLOR[ticket.priority] ?? 'var(--color-text-disabled)' }}
                  aria-label={`Ưu tiên: ${ticket.priority}`}
                />
                <span className="tkt-subject" title={ticket.subject}>
                  {ticket.subject}
                </span>
                <span className="tkt-id">#{ticket.id}</span>
              </div>
              <div className="tkt-card-meta">
                <span>{ticket.studentName}</span>
                <span aria-hidden="true">·</span>
                <span>{relativeTime(ticket.createdAt)}</span>
                {ticket.hasUnread && (
                  <span className="tkt-unread-badge" aria-label="Có tin chưa đọc">
                    Chưa đọc
                  </span>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
