import PriorityPill from '@/features/public/components/PriorityPill';
import TicketStatusBadge from '@/features/public/components/TicketStatusBadge';
import { formatRelativeTime } from '@/shared/utils/date';
import './TicketList.css';

/**
 * Danh sách ticket (dùng chung Staff + Manager).
 * Props:
 *   tickets, selectedId, statusFilter, tabs[{value,label}],
 *   onSelect(ticketId), onStatusFilterChange(value), isLoading
 */
export default function TicketList({
  tickets,
  selectedId,
  statusFilter,
  tabs,
  onSelect,
  onStatusFilterChange,
  isLoading = false,
}) {
  return (
    <div className="tkt-list-col">
      <div className="tkt-status-tabs" role="tablist" aria-label="Lọc trạng thái ticket">
        {tabs.map((tab) => {
          const isActive = statusFilter === tab.value;
          return (
            <button
              key={tab.value || 'all'}
              type="button"
              role="tab"
              aria-selected={isActive}
              className={`tkt-status-tab${isActive ? ' tkt-status-tab--active' : ''}`}
              onClick={() => onStatusFilterChange(tab.value)}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      <div className="tkt-list-scroll" role="listbox" aria-label="Danh sách ticket">
        {isLoading ? (
          Array.from({ length: 5 }).map((_, i) => <div key={i} className="tkt-skel-card" aria-hidden="true" />)
        ) : tickets.length === 0 ? (
          <div className="tkt-empty-list">Không có ticket nào.</div>
        ) : (
          tickets.map((t) => {
            const unassigned = t.status === 'open' && !t.assignedToStaffId;
            return (
              <div
                key={t.ticketId}
                role="option"
                aria-selected={selectedId === t.ticketId}
                tabIndex={0}
                className={`tkt-ticket-card${selectedId === t.ticketId ? ' tkt-ticket-card--active' : ''}`}
                onClick={() => onSelect(t.ticketId)}
                onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); onSelect(t.ticketId); } }}
              >
                <div className="tkt-card-top">
                  <PriorityPill priority={t.priority} />
                  <span className="tkt-id">#{t.ticketId}</span>
                  <TicketStatusBadge status={t.status} />
                </div>
                <div className="tkt-subject" title={t.subject}>{t.subject}</div>
                <div className="tkt-card-meta">
                  <span>{t.studentName}</span>
                  <span aria-hidden="true">·</span>
                  <span>{formatRelativeTime(t.lastReplyAt ?? t.createdAt)}</span>
                  {unassigned
                    ? <span className="tkt-unread-badge">Chưa giao</span>
                    : t.assignedToStaffName && <span className="tkt-assigned">Giao: {t.assignedToStaffName}</span>}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
