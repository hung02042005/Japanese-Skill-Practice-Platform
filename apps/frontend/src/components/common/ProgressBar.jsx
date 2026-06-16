import './ProgressBar.css';

/**
 * Props:
 *   value   — số hiện tại
 *   max     — giá trị tối đa (default 100)
 *   label   — aria-label mô tả (optional)
 *   height  — độ cao thanh bar px (default 8)
 *   color   — CSS color cho fill, mặc định dùng --color-primary
 */
export function ProgressBar({ value = 0, max = 100, label, height = 8, color }) {
  const pct = max > 0 ? Math.min(100, Math.round((value / max) * 100)) : 0;

  return (
    <div
      className="progress-bar__track"
      style={{ height }}
      aria-label={label}
    >
      <div
        className="progress-bar__fill"
        style={{ width: `${pct}%`, ...(color ? { background: color } : {}) }}
        role="progressbar"
        aria-valuenow={pct}
        aria-valuemin={0}
        aria-valuemax={100}
      />
    </div>
  );
}
