import SakuChan from '../auth/SakuChan';
import './StreakCard.css';

const DAY_LABELS = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];

function getTodayIndex() {
  const day = new Date().getDay();
  return day === 0 ? 6 : day - 1; // 0=Mon … 6=Sun
}

function StreakCard({ streak = 0, weekDays = [] }) {
  const hasStreak  = streak > 0;
  const todayIndex = getTodayIndex();

  return (
    <div className="streak-card" aria-label={`Streak hiện tại: ${streak} ngày`}>
      <div className="streak-header">
        <span
          className={`streak-flame${hasStreak ? ' streak-flame--active' : ''}`}
          aria-hidden="true"
        >
          🔥
        </span>
        <span className="streak-label">Ngày Streak</span>
      </div>

      <div className="streak-number">{streak}</div>
      <div className="streak-unit">ngày liên tiếp</div>

      <div className="streak-week" aria-label="Tiến độ 7 ngày">
        {DAY_LABELS.map((label, i) => (
          <div key={i} className="streak-day-col">
            <div
              className={[
                'streak-dot',
                weekDays[i] ? 'streak-dot--done' : '',
                i === todayIndex ? 'streak-dot--today' : '',
              ].filter(Boolean).join(' ')}
              aria-label={`${label}: ${weekDays[i] ? 'đã học' : 'chưa học'}`}
            />
            <span className="streak-day-label">{label}</span>
          </div>
        ))}
      </div>

      <div className="streak-mascot" aria-hidden="true">
        <SakuChan variant="idle" size={72} />
      </div>
    </div>
  );
}

export default StreakCard;
