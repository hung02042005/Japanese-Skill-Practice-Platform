import './PriorityPill.css';

const PRIORITY_MAP = {
  urgent: { label: 'Khẩn',        cls: 'urgent' },
  high:   { label: 'Cao',         cls: 'high' },
  normal: { label: 'Bình thường', cls: 'normal' },
  low:    { label: 'Thấp',        cls: 'low' },
};

export default function PriorityPill({ priority }) {
  const meta = PRIORITY_MAP[priority] ?? PRIORITY_MAP.normal;
  return (
    <span className={`tks-prio tks-prio--${meta.cls}`}>
      <span className="tks-prio-dot" aria-hidden="true" />
      {meta.label}
    </span>
  );
}
