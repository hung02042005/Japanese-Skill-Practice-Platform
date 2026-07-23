import { StarIcon, CalendarIcon } from '@/shared/components/common/StudentIcons';
import './MiniStatCard.css';

const CONFIGS = {
  words: { label: 'Từ đã học',          Icon: StarIcon,    mod: 'words' },
  days:  { label: 'Ngày học tháng này', Icon: CalendarIcon, mod: 'days'  },
};

function MiniStatCard({ type, value = 0 }) {
  const cfg = CONFIGS[type];
  if (!cfg) return null;

  return (
    <div className={`db-stat-card db-stat-card--${cfg.mod}`}>
      <span className="db-stat-card__icon" aria-hidden="true">
        <cfg.Icon size={28} />
      </span>
      <div className="db-stat-card__value">{value}</div>
      <div className="db-stat-card__label">{cfg.label}</div>
    </div>
  );
}

export default MiniStatCard;
