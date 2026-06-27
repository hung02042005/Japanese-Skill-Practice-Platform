import './TicketStatusBadge.css';

const STATUS_MAP = {
  open:        { label: 'Đang mở',       cls: 'open' },
  assigned:    { label: 'Đã tiếp nhận',  cls: 'assigned' },
  in_progress: { label: 'Đang xử lý',    cls: 'in_progress' },
  resolved:    { label: 'Đã giải quyết', cls: 'resolved' },
  closed:      { label: 'Đã đóng',       cls: 'closed' },
};

export default function TicketStatusBadge({ status }) {
  const meta = STATUS_MAP[status] ?? { label: status ?? '—', cls: 'open' };
  return <span className={`tks-status tks-status--${meta.cls}`}>{meta.label}</span>;
}
