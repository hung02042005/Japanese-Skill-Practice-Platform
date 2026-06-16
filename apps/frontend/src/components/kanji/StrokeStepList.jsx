/**
 * Danh sách từng nét — click để jump đến nét đó trong Player.
 * @param {{ strokeCount: number, currentStroke: number, onSelectStroke: (n:number)=>void }} props
 */
export default function StrokeStepList({ strokeCount, currentStroke, onSelectStroke }) {
  const total = Math.max(strokeCount || 1, 1);

  return (
    <div className="kd-steplist">
      <h3 className="kd-steplist-title">Danh sách nét</h3>
      <div className="kd-steplist-grid">
        {Array.from({ length: total }, (_, i) => {
          const n = i + 1;
          const isDone = currentStroke >= n;
          const isActive = currentStroke === n;
          return (
            <button
              key={n}
              className={
                'kd-step-item' +
                (isActive ? ' kd-step-item--active' : '') +
                (isDone && !isActive ? ' kd-step-item--done' : '')
              }
              onClick={() => onSelectStroke(n)}
              aria-current={isActive ? 'true' : undefined}
              aria-label={`Nét ${n}`}
            >
              <span className="kd-step-num">{n}</span>
              <span className="kd-step-label">Nét {n}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
