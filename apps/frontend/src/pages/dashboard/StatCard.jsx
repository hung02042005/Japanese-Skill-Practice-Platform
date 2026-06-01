const CONFIGS = {
  words: {
    label: 'Từ đã học',
    icon:  '⭐',
    mod:   'words',
  },
  days: {
    label: 'Ngày học tháng này',
    icon:  '📅',
    mod:   'days',
  },
};

function StatCard({ type, value = 0 }) {
  const cfg = CONFIGS[type];
  if (!cfg) return null;

  return (
    <div className={`stat-card stat-card--${cfg.mod}`}>
      <span className="stat-card-icon" aria-hidden="true">{cfg.icon}</span>
      <div className="stat-card-value">{value}</div>
      <div className="stat-card-label">{cfg.label}</div>
    </div>
  );
}

export default StatCard;
