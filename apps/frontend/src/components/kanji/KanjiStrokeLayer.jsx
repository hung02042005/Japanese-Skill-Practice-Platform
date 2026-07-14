import { useState, useEffect, useMemo, useRef } from 'react';
import HanziWriter from 'hanzi-writer';
import { jpCharDataLoader } from '../../utils/kanjiLookup';

/**
 * KanjiStrokeLayer — SVG overlay với 3 trạng thái nét viết
 *
 * Layer 1 — Stroke data   : lấy SVG path từ HanziWriter.loadCharacterData()
 * Layer 2 — 3 trạng thái  :
 *   "future"  → nét chưa tới : xám mờ, đứt đoạn (dashed)
 *   "current" → nét đang vẽ  : xanh, glow, mũi tên hướng + brush tip
 *   "done"    → nét đã xong  : do HanziWriter xử lý (không render ở đây)
 * Layer 3 — Comparison     : direction arrow từ median, brush tip chạy theo path
 *
 * Hệ tọa độ HanziWriter: 1024×1024, trục Y đi lên (Y=0 ở dưới).
 * Transform sang SVG viewBox:
 *   translate(padding, size−padding) scale(scale, −scale)
 *
 * Props:
 *   character        — kanji cần hiển thị
 *   completedCount   — số nét đã hoàn thành (= index nét "current")
 *   containerSize    — kích thước thực (px) của canvas container
 *   hwPadding        — padding đúng với HanziWriter.create() (tính tự động nếu bỏ trống)
 *   mode             — 'guide'   : guide + future (quiz mode)
 *                      'preview' : chỉ future ghosts (demo mode)
 *   onCharDataLoaded — (data) => void  gọi khi dữ liệu nét được tải xong
 */
export default function KanjiStrokeLayer({
  character,
  completedCount = 0,
  containerSize = 400,
  hwPadding,
  mode = 'guide',
  onCharDataLoaded,
}) {
  const [charData, setCharData] = useState(null);
  // Stable unique ID per instance (tránh conflict filter id)
  const uidRef = useRef(`ksl${Math.floor(Math.random() * 1e6)}`);
  const uid = uidRef.current;

  const padding = hwPadding ?? Math.max(Math.round(containerSize * 0.04), 8);

  // Tải dữ liệu stroke từ HanziWriter (có cache nội bộ — ko load lại nếu cùng ký tự)
  useEffect(() => {
    let cancelled = false;
    setCharData(null);

    HanziWriter.loadCharacterData(character, { charDataLoader: jpCharDataLoader })
      .then(data => {
        if (!cancelled) {
          setCharData(data);
          onCharDataLoaded?.(data);
        }
      })
      .catch(() => {
        if (!cancelled) setCharData({ strokes: [], medians: [] });
      });

    return () => { cancelled = true; };
  }, [character]); // eslint-disable-line react-hooks/exhaustive-deps

  /**
   * Transform từ hệ tọa độ HanziWriter sang SVG viewBox.
   *
   * HanziWriter CHARACTER_BOUNDS: from = {x:0, y:-124}, to = {x:1024, y:900}
   * → preScaledWidth = 1024, preScaledHeight = 1024
   * → scale = (containerSize - 2*padding) / 1024   (container luôn vuông)
   *
   * xOffset = -from.x * scale + padding = padding          (from.x = 0)
   * yOffset = -from.y * scale + padding = 124*scale + padding  (from.y = -124)
   *
   * SVG group transform:
   *   translate(xOffset, containerSize - yOffset) scale(scale, -scale)
   */
  const gTransform = useMemo(() => {
    const scale = (containerSize - 2 * padding) / 1024;
    const HW_FROM_Y = -124;                              // CHARACTER_BOUNDS[0].y
    const yOffset   = (-HW_FROM_Y) * scale + padding;   // = 124*scale + padding
    return `translate(${padding}, ${containerSize - yOffset}) scale(${scale}, ${-scale})`;
  }, [containerSize, padding]);

  if (!charData || !charData.strokes?.length) return null;

  const { strokes, medians } = charData;
  const total = strokes.length;
  const curIdx = completedCount;                             // index nét "current"
  const hasCurrent = mode === 'guide' && curIdx < total;     // chỉ guide mode mới show current

  return (
    <svg
      viewBox={`0 0 ${containerSize} ${containerSize}`}
      className="ksl-svg"
      aria-hidden="true"
    >
      <defs>
        {/* Blue glow filter cho current stroke */}
        <filter id={`${uid}-glow`} x="-40%" y="-40%" width="180%" height="180%">
          <feGaussianBlur in="SourceGraphic" stdDeviation="9" result="b" />
          <feColorMatrix
            in="b" type="matrix"
            values="0 0 0 0 0.10  0 0 0 0 0.32  0 0 0 0 0.96  0 0 0 0.48 0"
            result="cb"
          />
          <feMerge>
            <feMergeNode in="cb" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>

      <g transform={gTransform}>

        {/* ── LAYER: Future strokes — ghost dashed ─────────────────────── */}
        {strokes.map((d, i) => i > curIdx && (
          <path
            key={`${uid}-f${i}`}
            d={d}
            fill="none"
            stroke="#BEB5B0"
            strokeWidth={54}
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeDasharray="70 34"
            opacity={0.36}
          />
        ))}

        {/* ── Stroke order numbers (for next 4 future strokes) ── */}
        {strokes.map((d, i) => {
          if (i <= curIdx || i > curIdx + 4) return null;
          const startPt = medians?.[i]?.[0];
          if (!startPt) return null;
          const [nx, ny] = startPt;
          // Closeness effect: next stroke is slightly larger/more visible
          const isNext = i === curIdx + 1;
          const r    = isNext ? 48 : 38;
          const fs   = isNext ? 58 : 46;
          const op   = isNext ? 0.72 : 0.48;
          return (
            <g key={`${uid}-n${i}`}>
              <circle cx={nx} cy={ny} r={r + 8}
                fill="white" opacity={op * 0.85} />
              <circle cx={nx} cy={ny} r={r}
                fill="none" stroke="#9CA3AF" strokeWidth={6} opacity={op} />
              <text
                x={nx} y={ny}
                fontSize={fs}
                fill="#6B7280"
                textAnchor="middle"
                dominantBaseline="central"
                fontWeight="800"
                fontFamily="system-ui, sans-serif"
                opacity={op}
              >
                {i + 1}
              </text>
            </g>
          );
        })}

        {/* ── LAYER: Current stroke guide (blue) + direction + brush ───── */}
        {hasCurrent && (
          <g>
            {/* Outer glow halo */}
            <path
              d={strokes[curIdx]}
              fill="none"
              stroke="#3B82F6"
              strokeWidth={92}
              strokeLinecap="round"
              strokeLinejoin="round"
              opacity={0.10}
            />
            {/* Guide path — light blue */}
            <path
              d={strokes[curIdx]}
              fill="none"
              stroke="#93C5FD"
              strokeWidth={55}
              strokeLinecap="round"
              strokeLinejoin="round"
              opacity={0.46}
            />

            {/* Direction hint từ median (mũi tên + pulsing dot) */}
            {medians?.[curIdx]?.length >= 2 && (
              <DirectionHint median={medians[curIdx]} idx={curIdx} />
            )}

            {/* Brush tip animator — chạy dọc theo stroke path */}
            <BrushTip pathData={strokes[curIdx]} />
          </g>
        )}

      </g>
    </svg>
  );
}

/* ── DirectionHint ───────────────────────────────────────────────────── */
/**
 * Hiển thị:
 *  1. Dot xanh pulsing tại điểm bắt đầu nét
 *  2. Đường mũi tên chỉ hướng viết (dùng 2-3 điểm đầu của median)
 */
function DirectionHint({ median, idx }) {
  const [sx, sy] = median[0];
  const refIdx = Math.min(2, median.length - 1);
  const [rx, ry] = median[refIdx];

  const dx = rx - sx, dy = ry - sy;
  const dist = Math.sqrt(dx * dx + dy * dy) || 1;
  const ux = dx / dist, uy = dy / dist;

  const lineLen = 90;
  const lx = sx + ux * lineLen, ly = sy + uy * lineLen;

  const headFwd  = 38;
  const headWing = 22;
  const perp     = { x: -uy, y: ux };
  const tip = [lx + ux * headFwd,            ly + uy * headFwd];
  const w1  = [lx + perp.x * headWing,       ly + perp.y * headWing];
  const w2  = [lx - perp.x * headWing,       ly - perp.y * headWing];

  return (
    <g>
      {/* Outer pulsing ring */}
      <circle cx={sx} cy={sy} r={44} fill="none" stroke="#60A5FA" strokeWidth={13}>
        <animate attributeName="r"       values="44;88;44"    dur="1.6s" repeatCount="indefinite" />
        <animate attributeName="opacity" values="0.55;0;0.55" dur="1.6s" repeatCount="indefinite" />
      </circle>
      {/* Stroke number — overlaid on start dot */}
      <circle cx={sx} cy={sy} r={50} fill="#2563EB" opacity={0.88} />
      <text
        x={sx} y={sy}
        fontSize={62}
        fill="white"
        textAnchor="middle"
        dominantBaseline="central"
        fontWeight="800"
        fontFamily="system-ui, sans-serif"
      >
        {idx + 1}
      </text>
      {/* Direction line */}
      <line
        x1={sx} y1={sy} x2={lx} y2={ly}
        stroke="#3B82F6" strokeWidth={13} strokeLinecap="round" opacity={0.75}
      />
      {/* Arrowhead */}
      <polygon
        points={`${tip[0]},${tip[1]} ${w1[0]},${w1[1]} ${w2[0]},${w2[1]}`}
        fill="#3B82F6" opacity={0.80}
      />
    </g>
  );
}

/* ── BrushTip ────────────────────────────────────────────────────────── */
/**
 * Animated brush tip di chuyển dọc theo stroke path.
 * animateMotion path dùng tọa độ HanziWriter (cùng không gian với <g transform>).
 * Browser tự áp transform của group lên, kết quả đúng trên màn hình.
 */
function BrushTip({ pathData }) {
  return (
    <g>
      {/* Trailing glow */}
      <circle r={34} fill="#93C5FD" opacity={0.48}>
        <animateMotion path={pathData} dur="2s" repeatCount="indefinite" calcMode="linear" />
      </circle>
      {/* Tip core */}
      <circle r={19} fill="#1D4ED8" opacity={0.86}>
        <animateMotion path={pathData} dur="2s" repeatCount="indefinite" calcMode="linear" />
      </circle>
    </g>
  );
}
