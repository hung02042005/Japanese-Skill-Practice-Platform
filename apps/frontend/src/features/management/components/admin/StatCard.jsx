import './StatCard.css';

/**
 * Props:
 *   icon     — ReactNode (SVG)
 *   value    — số hoặc string
 *   label    — nhãn mô tả
 *   variant  — 'total' | 'active' | 'banned' | 'new'
 *   loading  — hiện dấu '–' khi true
 */
export function StatCard({ icon, value, label, variant = 'total', loading = false }) {
  return (
    <div className={`stat-card stat-card--${variant}`}>
      <div className="stat-card__icon">{icon}</div>
      <div className="stat-card__body">
        <div className="stat-card__value">{loading ? '–' : value}</div>
        <div className="stat-card__label">{label}</div>
      </div>
    </div>
  );
}
