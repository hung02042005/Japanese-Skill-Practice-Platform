function calcStrength(pwd) {
  let score = 0;
  if (pwd.length >= 8)           score += 2;
  if (/[A-Z]/.test(pwd))         score += 2;
  if (/[0-9]/.test(pwd))         score += 2;
  if (/[^A-Za-z0-9]/.test(pwd))  score += 2;
  return score;
}

const STRENGTH_MAP = [
  { min: 0, max: 2, label: 'Yếu',      color: 'var(--color-error)',     pct: 25  },
  { min: 3, max: 4, label: 'Tạm được', color: 'var(--color-warning)',   pct: 50  },
  { min: 5, max: 6, label: 'Khá',      color: 'var(--color-accent)',    pct: 75  },
  { min: 7, max: 8, label: 'Mạnh',     color: 'var(--color-secondary)', pct: 100 },
];

function getStrengthInfo(score) {
  return STRENGTH_MAP.find((s) => score >= s.min && score <= s.max) || STRENGTH_MAP[0];
}

export default function PasswordStrengthBar({ password }) {
  if (!password) return null;
  const info = getStrengthInfo(calcStrength(password));
  return (
    <div id="reg-pwd-strength" className="pwd-strength" aria-live="polite">
      <div className="pwd-strength-track">
        <div
          className="pwd-strength-fill"
          style={{ width: `${info.pct}%`, background: info.color }}
        />
      </div>
      <span className="pwd-strength-label" style={{ color: info.color }}>
        {info.label}
      </span>
    </div>
  );
}
