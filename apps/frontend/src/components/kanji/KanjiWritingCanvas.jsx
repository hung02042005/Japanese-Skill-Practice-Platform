import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import HanziWriter from 'hanzi-writer';
import KanjiStrokeLayer from './KanjiStrokeLayer';

/* ── Lưới giấy thư pháp (半紙 / hanshi) ──────────────────────────── */
function WritingGrid() {
  return (
    <svg className="kpw-grid" viewBox="0 0 100 100"
      preserveAspectRatio="none" aria-hidden="true">
      {/* Đường chia 4 góc — xám nhạt */}
      <line x1="25"  y1="0"   x2="25"  y2="100" stroke="#E5DDD8" strokeWidth="0.4" />
      <line x1="75"  y1="0"   x2="75"  y2="100" stroke="#E5DDD8" strokeWidth="0.4" />
      <line x1="0"   y1="25"  x2="100" y2="25"  stroke="#E5DDD8" strokeWidth="0.4" />
      <line x1="0"   y1="75"  x2="100" y2="75"  stroke="#E5DDD8" strokeWidth="0.4" />
      {/* Đường chéo — rất mờ */}
      <line x1="0"   y1="0"   x2="100" y2="100" stroke="#EDE5DE" strokeWidth="0.35" strokeDasharray="3 5" />
      <line x1="100" y1="0"   x2="0"   y2="100" stroke="#EDE5DE" strokeWidth="0.35" strokeDasharray="3 5" />
      {/* Đường trung tâm — đỏ nhạt (như giấy luyện thư pháp thật) */}
      <line x1="50"  y1="0"   x2="50"  y2="100" stroke="#DDB8B8" strokeWidth="0.75" strokeDasharray="5 3" />
      <line x1="0"   y1="50"  x2="100" y2="50"  stroke="#DDB8B8" strokeWidth="0.75" strokeDasharray="5 3" />
      {/* Chấm tâm */}
      <circle cx="50" cy="50" r="1" fill="#DDB8B8" opacity="0.85" />
    </svg>
  );
}

/* ── Direction helper ────────────────────────────────────────────────── */
/**
 * Phân tích hướng viết của nét từ median (tọa độ HanziWriter, Y đi lên).
 * Trả về label tiếng Việt ngắn gọn.
 */
function getDirectionLabel(median) {
  if (!median || median.length < 2) return '';
  const [sx, sy] = median[0];
  const [ex, ey] = median[median.length - 1];
  const dx = ex - sx;
  const dy = ey - sy; // Y trục lên trong HW coords

  const adx = Math.abs(dx), ady = Math.abs(dy);

  if (ady > adx * 2.5) return dy > 0 ? '↑ Lên trên'      : '↓ Xuống';
  if (adx > ady * 2.5) return dx > 0 ? '→ Sang phải'     : '← Sang trái';
  if (dx > 0 && dy < 0) return '↘ Chéo phải xuống';
  if (dx < 0 && dy < 0) return '↙ Chéo trái xuống';
  if (dx > 0 && dy > 0) return '↗ Chéo phải lên';
  return '↖ Chéo trái lên';
}

/* ── Score helpers ───────────────────────────────────────────────────── */
function qualityFromMistakes(mistakes) {
  if (mistakes === 0)  return 'perfect';
  if (mistakes <= 2)   return 'good';
  return 'ok';
}

function QualityBadge({ quality }) {
  if (quality === 'perfect') return <>✔ Hoàn hảo! ⭐⭐⭐</>;
  if (quality === 'good')    return <>✔ Tốt lắm! ⭐⭐</>;
  return <>✔ Đúng rồi ⭐</>;
}

function FinalScore({ mistakesArr }) {
  const total = mistakesArr.length;
  if (!total) return null;
  const totalMistakes = mistakesArr.reduce((s, m) => s + m, 0);
  const avg = totalMistakes / total;
  if (avg < 0.5)  return <span className="kpw-done-stars">⭐⭐⭐ Hoàn hảo</span>;
  if (avg < 1.5)  return <span className="kpw-done-stars">⭐⭐ Tốt</span>;
  return            <span className="kpw-done-stars">⭐ Cần luyện thêm</span>;
}

/* ═══════════════════════════════════════════════════════════════════════
   KanjiWritingCanvas
   Kiến trúc 3 layer:
     Layer 1 — Stroke data   : HanziWriter.loadCharacterData() → SVG paths
     Layer 2 — 3 states      : KanjiStrokeLayer (future ghost / current guide / done←HW)
     Layer 3 — Comparison    : direction label, per-stroke quality badge,
                               final score overlay
   ═══════════════════════════════════════════════════════════════════════ */
export default function KanjiWritingCanvas({ character, strokeCount, onBack, onComplete }) {
  const wrapRef   = useRef(null);  // kpw-canvas-box — đọc kích thước thực
  const hwRef     = useRef(null);  // div HanziWriter render vào
  const writerRef = useRef(null);

  /* ── UI state ──────────────────────────────────────────────────── */
  const [count,        setCount]        = useState(0);
  const [feedback,     setFeedback]     = useState(null);  // 'ok'|'bad'|null
  const [hint,         setHint]         = useState(false);
  const [done,         setDone]         = useState(false);
  const [status,       setStatus]       = useState('loading'); // 'loading'|'ready'|'error'
  const [strokeQuality, setStrokeQuality] = useState(null);   // 'perfect'|'good'|'ok'|null

  /* ── HanziWriter sizing (dùng để đồng bộ KanjiStrokeLayer) ────── */
  const [hwSize,    setHwSize]    = useState(400);
  const [hwPadding, setHwPadding] = useState(16);

  /* ── Character data (từ KanjiStrokeLayer callback) → direction ── */
  const [charData,  setCharData]  = useState(null);

  /* ── Mistake tracking per stroke ────────────────────────────────── */
  const mistakesRef          = useRef([]);   // mistakes[i] = số lần sai ở nét i
  const currentStrokeIdxRef  = useRef(0);    // nét đang được quiz

  const total = Math.max(strokeCount || 1, 1);

  /* ── Direction label cho nét hiện tại ──────────────────────────── */
  const dirLabel = useMemo(() => {
    const med = charData?.medians?.[count];
    return getDirectionLabel(med);
  }, [charData, count]);

  /* ── startQuiz (khi reset hoặc tải xong) ───────────────────────── */
  const startQuiz = useCallback((writer) => {
    // Reset mistake tracking
    mistakesRef.current         = new Array(total).fill(0);
    currentStrokeIdxRef.current = 0;

    writer.quiz({
      showHintAfterMisses:    3,   // animation gợi ý sau 3 lần sai
      markStrokesReadyForQuiz: false,

      onMistake: () => {
        // Ghi nhận lỗi cho nét hiện tại
        mistakesRef.current[currentStrokeIdxRef.current] =
          (mistakesRef.current[currentStrokeIdxRef.current] || 0) + 1;
        setFeedback('bad');
        setTimeout(() => setFeedback(null), 750);
      },

      onCorrectStroke: (strokeData) => {
        const completed = total - strokeData.strokesRemaining;
        currentStrokeIdxRef.current = completed; // chuẩn bị cho nét tiếp theo
        setCount(completed);

        // Quality badge dựa trên số lần sai ở nét vừa hoàn thành
        const m = mistakesRef.current[completed - 1] || 0;
        setStrokeQuality(qualityFromMistakes(m));
        setFeedback('ok');
        setTimeout(() => {
          setFeedback(null);
          setStrokeQuality(null);
        }, 1100);
      },

      onComplete: () => {
        setCount(total);
        setTimeout(() => setDone(true), 450);
        onComplete?.();
      },
    });
  }, [total, onComplete]);

  /* ── Tạo HanziWriter khi character thay đổi ─────────────────────── */
  useEffect(() => {
    if (!hwRef.current || !wrapRef.current) return;

    // Đọc kích thước thực của container
    const size    = Math.round(wrapRef.current.getBoundingClientRect().width) || 400;
    const padding = Math.max(Math.round(size * 0.04), 8);

    // Lưu để truyền vào KanjiStrokeLayer (phải khớp với HanziWriter)
    setHwSize(size);
    setHwPadding(padding);

    hwRef.current.innerHTML = '';
    setCount(0);
    setFeedback(null);
    setDone(false);
    setHint(false);
    setStatus('loading');
    setStrokeQuality(null);
    setCharData(null);

    const writer = HanziWriter.create(hwRef.current, character, {
      width:   size,
      height:  size,
      padding,

      showOutline:   false,      // KanjiStrokeLayer xử lý future strokes
      showCharacter: false,

      strokeColor:        '#2D2D2D',  // nét đã vẽ đúng — ink đậm
      highlightColor:     '#22C55E',  // flash xanh lá khi hoàn thành nét
      drawingWidth:       8,
      highlightOnComplete: true,

      onLoadCharDataSuccess: () => {
        setStatus('ready');
        startQuiz(writer);
      },
      onLoadCharDataError: () => setStatus('error'),
    });

    writerRef.current = writer;

    return () => { writerRef.current = null; };
  }, [character, startQuiz]);

  /* ── Reset ──────────────────────────────────────────────────────── */
  const handleReset = useCallback(() => {
    if (!writerRef.current || status !== 'ready') return;
    writerRef.current.hideCharacter();
    setCount(0);
    setFeedback(null);
    setDone(false);
    setHint(false);
    setStrokeQuality(null);
    startQuiz(writerRef.current);
  }, [status, startQuiz]);

  /* ── Gợi ý (hiện/ẩn chữ mờ) ─────────────────────────────────────── */
  const handleHint = useCallback(() => {
    if (!writerRef.current) return;
    const next = !hint;
    setHint(next);
    if (next) writerRef.current.showCharacter();
    else      writerRef.current.hideCharacter();
  }, [hint]);

  /* ── Bỏ qua nét ─────────────────────────────────────────────────── */
  const handleSkip = useCallback(() => {
    if (done || !writerRef.current) return;
    writerRef.current.skipQuizStroke();
  }, [done]);

  const progress = (count / total) * 100;

  return (
    <div className="kpw-wrap">

      {/* ── Thanh trên ─────────────────────────────────────────────── */}
      <div className="kpw-topbar">
        <button className="kpw-back" onClick={onBack} aria-label="Quay lại màn hình học">
          <ChevronLeftIcon />
          Quay lại
        </button>
        <span className="kpw-topbar-char" lang="ja">{character}</span>
        <span className="kpw-mode-tag">Luyện viết</span>
      </div>

      {/* ── Progress bar ───────────────────────────────────────────── */}
      <div className="kpw-prog-wrap">
        <div className="kpw-prog-bar">
          <div className="kpw-prog-fill" style={{ width: `${progress}%` }} />
        </div>
        <span className="kpw-prog-text" aria-live="polite">
          {count} / {total} nét hoàn thành
        </span>
      </div>

      {/* ── Khu vực canvas ─────────────────────────────────────────── */}
      <div className="kpw-main">
        <div
          ref={wrapRef}
          className={`kpw-canvas-box${
            feedback === 'ok'  ? ' kpw--ok'  :
            feedback === 'bad' ? ' kpw--bad' : ''
          }`}
        >
          {/* ── Layer 1+2 : HanziWriter quiz (input + completed strokes) */}
          <div ref={hwRef} className="kpw-hw" />

          {/* ── Layer 2 : KanjiStrokeLayer — future ghost + current guide */}
          {status !== 'error' && (
            <KanjiStrokeLayer
              character={character}
              completedCount={count}
              containerSize={hwSize}
              hwPadding={hwPadding}
              mode="guide"
              onCharDataLoaded={setCharData}
            />
          )}

          {/* ── Lưới kẻ mờ (pointer-events: none) */}
          <WritingGrid />

          {/* ── Loading overlay */}
          {status === 'loading' && (
            <div className="kpw-status-overlay">
              <div className="kpw-spinner-sm" />
            </div>
          )}

          {/* ── Error overlay */}
          {status === 'error' && (
            <div className="kpw-status-overlay">
              <p className="kpw-status-msg">Ký tự này chưa có dữ liệu nét</p>
            </div>
          )}

          {/* ── Layer 3 : Feedback badge với quality ── */}
          {feedback && (
            <div className={`kpw-badge kpw-badge--${feedback}`} role="status">
              {feedback === 'ok'
                ? <QualityBadge quality={strokeQuality} />
                : '✖ Thử lại'}
            </div>
          )}
        </div>

        {/* ── Layer 3 : Direction label + stroke counter ── */}
        {!done && status === 'ready' && (
          <div className="kpw-stroke-info">
            <span className="kpw-step-num">Nét <strong>{count + 1}</strong> / {total}</span>
            {dirLabel && (
              <span className="kpw-dir-label">
                <DirectionIcon /> {dirLabel}
              </span>
            )}
          </div>
        )}
      </div>

      {/* ── Thanh điều khiển ──────────────────────────────────────── */}
      <div className="kpw-ctrls">
        <button className="kpw-ctrl" onClick={handleReset} title="Đặt lại toàn bộ">
          <ResetIcon />
          <span>Đặt lại</span>
        </button>
        <button
          className={`kpw-ctrl${hint ? ' kpw-ctrl--on' : ''}`}
          onClick={handleHint}
          title={hint ? 'Ẩn gợi ý' : 'Hiện chữ mờ mẫu'}
        >
          <HintIcon />
          <span>Gợi ý</span>
        </button>
        <button
          className="kpw-ctrl"
          onClick={handleSkip}
          disabled={done || status !== 'ready'}
          title="Bỏ qua nét này"
        >
          <SkipIcon />
          <span>Bỏ qua</span>
        </button>
      </div>

      {/* ── Overlay hoàn thành ────────────────────────────────────── */}
      {done && (
        <div className="kpw-done-veil">
          <div className="kpw-done-box">
            <div className="kpw-done-k" lang="ja">{character}</div>
            <h3 className="kpw-done-h">Hoàn thành! 🎉</h3>
            <FinalScore mistakesArr={mistakesRef.current} />
            <p className="kpw-done-p">
              Bạn đã viết đúng {total} nét của 「{character}」
            </p>
            <button className="kpw-btn-retry" onClick={handleReset}>↻ Luyện lại</button>
            <button className="kpw-btn-back"  onClick={onBack}>← Quay lại</button>
          </div>
        </div>
      )}
    </div>
  );
}

/* ── Icons ────────────────────────────────────────────────────────────── */
function ChevronLeftIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <polyline points="15 18 9 12 15 6" />
    </svg>
  );
}
function ResetIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <polyline points="1 4 1 10 7 10" />
      <path d="M3.51 15a9 9 0 1 0 .49-3.51" />
    </svg>
  );
}
function HintIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}
function SkipIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <polygon points="5 4 15 12 5 20 5 4" />
      <line x1="19" y1="5" x2="19" y2="19" />
    </svg>
  );
}
function DirectionIcon() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="5" y1="12" x2="19" y2="12" />
      <polyline points="12 5 19 12 12 19" />
    </svg>
  );
}
