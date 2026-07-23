const SKILLS = [
  { key: 'vocabulary',    label: 'Từ vựng' },
  { key: 'grammar',       label: 'Ngữ pháp' },
  { key: 'listening',     label: 'Nghe hiểu' },
  { key: 'pronunciation', label: 'Phát âm' },
];

const SIZE    = 240;
const CENTER  = SIZE / 2;
const RADIUS  = 90;
const LEVELS  = [20, 40, 60, 80, 100];

function polarToXY(angle, r) {
  const a = (angle - 90) * (Math.PI / 180);
  return { x: CENTER + r * Math.cos(a), y: CENTER + r * Math.sin(a) };
}

export default function SkillRadarChart({ data = {} }) {
  const n = SKILLS.length;
  const angleStep = 360 / n;

  const gridPolygons = LEVELS.map((lvl) => {
    const pts = SKILLS.map((_, i) => {
      const p = polarToXY(i * angleStep, (lvl / 100) * RADIUS);
      return `${p.x},${p.y}`;
    }).join(' ');
    return (
      <polygon
        key={lvl}
        points={pts}
        fill="none"
        stroke="var(--color-border)"
        strokeWidth="1"
      />
    );
  });

  const axes = SKILLS.map((_, i) => {
    const end = polarToXY(i * angleStep, RADIUS);
    return (
      <line
        key={i}
        x1={CENTER} y1={CENTER}
        x2={end.x} y2={end.y}
        stroke="var(--color-border)"
        strokeWidth="1"
      />
    );
  });

  const dataPoints = SKILLS.map((sk, i) => {
    const val = Math.min(100, Math.max(0, data[sk.key] ?? 0));
    const p   = polarToXY(i * angleStep, (val / 100) * RADIUS);
    return `${p.x},${p.y}`;
  }).join(' ');

  const dots = SKILLS.map((sk, i) => {
    const val = Math.min(100, Math.max(0, data[sk.key] ?? 0));
    const p   = polarToXY(i * angleStep, (val / 100) * RADIUS);
    return <circle key={sk.key} cx={p.x} cy={p.y} r="4" fill="var(--color-primary)" />;
  });

  const labels = SKILLS.map((sk, i) => {
    const p = polarToXY(i * angleStep, RADIUS + 22);
    return (
      <text
        key={sk.key}
        x={p.x} y={p.y}
        textAnchor="middle"
        dominantBaseline="middle"
        fontSize="11"
        fill="var(--color-text-sub)"
        fontFamily="var(--font-base)"
      >
        {sk.label}
      </text>
    );
  });

  const ariaLabel = SKILLS
    .map((s) => `${s.label} ${data[s.key] ?? 0}%`)
    .join(', ');

  return (
    <svg
      width={SIZE}
      height={SIZE + 40}
      viewBox={`0 -20 ${SIZE} ${SIZE + 40}`}
      role="img"
      aria-label={`Biểu đồ radar năng lực: ${ariaLabel}`}
    >
      {gridPolygons}
      {axes}
      <polygon
        points={dataPoints}
        fill="rgba(232,154,170,0.25)"
        stroke="var(--color-primary)"
        strokeWidth="2"
        strokeLinejoin="round"
      />
      {dots}
      {labels}
    </svg>
  );
}
