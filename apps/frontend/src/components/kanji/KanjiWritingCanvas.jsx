import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import HanziWriter from 'hanzi-writer';
import KanjiStrokeLayer from './KanjiStrokeLayer';
import { ConfettiIcon } from '../common/AppIcons';
import { evaluateKanjiStroke, saveKanjiWritingAttempt } from '../../api/studentService';
import { jpCharDataLoader } from '../../utils/kanjiLookup';

/* ── Lưới giấy thư pháp (半紙 / hanshi) ──────────────────────────── */
function WritingGrid() {
  return (
    <svg className="kpw-grid" viewBox="0 0 100 100"
      preserveAspectRatio="none" aria-hidden="true">
      <line x1="25"  y1="0"   x2="25"  y2="100" stroke="#E5DDD8" strokeWidth="0.4" />
      <line x1="75"  y1="0"   x2="75"  y2="100" stroke="#E5DDD8" strokeWidth="0.4" />
      <line x1="0"   y1="25"  x2="100" y2="25"  stroke="#E5DDD8" strokeWidth="0.4" />
      <line x1="0"   y1="75"  x2="100" y2="75"  stroke="#E5DDD8" strokeWidth="0.4" />
      <line x1="0"   y1="0"   x2="100" y2="100" stroke="#EDE5DE" strokeWidth="0.35" strokeDasharray="3 5" />
      <line x1="100" y1="0"   x2="0"   y2="100" stroke="#EDE5DE" strokeWidth="0.35" strokeDasharray="3 5" />
      <line x1="50"  y1="0"   x2="50"  y2="100" stroke="#DDB8B8" strokeWidth="0.75" strokeDasharray="5 3" />
      <line x1="0"   y1="50"  x2="100" y2="50"  stroke="#DDB8B8" strokeWidth="0.75" strokeDasharray="5 3" />
      <circle cx="50" cy="50" r="1" fill="#DDB8B8" opacity="0.85" />
    </svg>
  );
}

/* ── Hiển thị quality badge — data từ API, không tính ở đây ─────── */
function QualityBadge({ quality }) {
  if (!quality || quality === 'loading') return <>⏳</>;
  if (quality === 'perfect') return <>✔ Hoàn hảo! ⭐⭐⭐</>;
  if (quality === 'good')    return <>✔ Tốt lắm! ⭐⭐</>;
  return <>✔ Đúng rồi ⭐</>;
}

/* ── Hiển thị điểm cuối — data từ API saveAttempt ───────────────── */
function FinalScore({ apiResult }) {
  if (!apiResult) return null;
  if (apiResult.finalQuality === 'perfect') return <span className="kpw-done-stars">⭐⭐⭐ Hoàn hảo</span>;
  if (apiResult.finalQuality === 'good')    return <span className="kpw-done-stars">⭐⭐ Tốt</span>;
  return <span className="kpw-done-stars">⭐ Cần luyện thêm</span>;
}

/* ═══════════════════════════════════════════════════════════════════════
   KanjiWritingCanvas
   Kiến trúc 3 layer:
     Layer 1 — HanziWriter quiz : animation + xác nhận nét đúng/sai
     Layer 2 — KanjiStrokeLayer : ghost strokes + current guide
     Layer 3 — API result       : quality badge + final score từ backend
   Business logic (DTW scoring, chất lượng, lưu DB) = BACKEND ONLY
   ═══════════════════════════════════════════════════════════════════════ */
export default function KanjiWritingCanvas({ kanjiId, character, strokeCount, onBack, onComplete }) {
  const wrapRef   = useRef(null);  // kpw-canvas-box — đọc kích thước thực
  const hwRef     = useRef(null);  // div HanziWriter render vào
  const writerRef = useRef(null);

  /* ── UI state ──────────────────────────────────────────────────── */
  const [count,        setCount]        = useState(0);
  const [feedback,     setFeedback]     = useState(null);  // 'ok'|'bad'|null
  const [hint,         setHint]         = useState(false);
  const [done,         setDone]         = useState(false);
  const [status,       setStatus]       = useState('loading');
  const [strokeQuality, setStrokeQuality] = useState(null); // từ API

  /* ── HanziWriter sizing ────────────────────────────────────────── */
  const [hwSize,    setHwSize]    = useState(400);
  const [hwPadding, setHwPadding] = useState(16);

  /* ── Character data cho KanjiStrokeLayer + hướng nét gợi ý ─────── */
  const [charData, setCharData] = useState(null);

  /* ── Kết quả lần luyện viết — từ API saveAttempt ───────────────── */
  const [finalScore, setFinalScore] = useState(null);

  /* ── Refs cho values cần thiết bên trong HanziWriter callbacks ─── */
  const charDataRef  = useRef(null);
  const hwSizeRef    = useRef(400);
  const kanjiIdRef   = useRef(kanjiId);
  const strokeResRef = useRef([]);  // [{strokeIndex, dtwScore, quality, direction}]

  useEffect(() => { charDataRef.current = charData; },  [charData]);
  useEffect(() => { hwSizeRef.current   = hwSize; },    [hwSize]);
  useEffect(() => { kanjiIdRef.current  = kanjiId; },   [kanjiId]);

  /* ── Pointer capture — ghi lại đường vẽ của người dùng ─────────── */
  const currentUserPathRef = useRef([]);
  const isDrawingRef       = useRef(false);

  useEffect(() => {
    const el = hwRef.current;
    if (!el) return;

    const onDown = (e) => {
      const rect = el.getBoundingClientRect();
      const x = e.clientX - rect.left;
      // Flip Y để khớp trục Y-up của HanziWriter medians
      const y = rect.height - (e.clientY - rect.top);
      currentUserPathRef.current = [[x, y]];
      isDrawingRef.current = true;
    };
    const onMove = (e) => {
      if (!isDrawingRef.current) return;
      const rect = el.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = rect.height - (e.clientY - rect.top);
      currentUserPathRef.current.push([x, y]);
    };
    const onUp = () => { isDrawingRef.current = false; };

    el.addEventListener('pointerdown', onDown, { passive: true });
    el.addEventListener('pointermove', onMove, { passive: true });
    el.addEventListener('pointerup',   onUp,   { passive: true });

    return () => {
      el.removeEventListener('pointerdown', onDown);
      el.removeEventListener('pointermove', onMove);
      el.removeEventListener('pointerup',   onUp);
    };
  }, [status]); // re-attach sau khi HanziWriter load xong

  const total = Math.max(strokeCount || 1, 1);

  /* ── Hướng nét TIẾP THEO để gợi ý người dùng (UX display, không phải scoring) */
  const nextStrokeDirLabel = useMemo(() => {
    const med = charData?.medians?.[count];
    if (!med || med.length < 2) return '';
    const [sx, sy] = med[0];
    const [ex, ey] = med[med.length - 1];
    const dx = ex - sx, dy = ey - sy;
    const adx = Math.abs(dx), ady = Math.abs(dy);
    if (ady > adx * 2.5) return dy > 0 ? '↑ Lên trên'       : '↓ Xuống';
    if (adx > ady * 2.5) return dx > 0 ? '→ Sang phải'      : '← Sang trái';
    if (dx > 0 && dy < 0) return '↘ Chéo phải xuống';
    if (dx < 0 && dy < 0) return '↙ Chéo trái xuống';
    if (dx > 0 && dy > 0) return '↗ Chéo phải lên';
    return '↖ Chéo trái lên';
  }, [charData, count]);

  /* ── startQuiz ─────────────────────────────────────────────────── */
  const startQuiz = useCallback((writer) => {
    strokeResRef.current = new Array(total).fill(null);

    writer.quiz({
      showHintAfterMisses:     3,
      markStrokesReadyForQuiz: false,

      onMistake: () => {
        // Feedback UX tức thì — không liên quan đến scoring
        setFeedback('bad');
        setTimeout(() => setFeedback(null), 750);
      },

      onCorrectStroke: (strokeData) => {
        const completed  = total - strokeData.strokesRemaining;
        const strokeIdx  = completed - 1;

        // Capture path ngay trước khi pointerdown reset nó
        const userPath      = [...currentUserPathRef.current];
        const referencePath = charDataRef.current?.medians?.[strokeIdx] ?? [];

        setCount(completed);
        setFeedback('ok');
        setStrokeQuality('loading'); // chờ API

        // Gọi backend DTW — không block HanziWriter
        evaluateKanjiStroke({ strokeIndex: strokeIdx, userPath, referencePath })
          .then((result) => {
            strokeResRef.current[strokeIdx] = result;
            setStrokeQuality(result.quality);
            setTimeout(() => { setFeedback(null); setStrokeQuality(null); }, 1000);
          })
          .catch(() => {
            setStrokeQuality('ok'); // fallback an toàn
            setTimeout(() => { setFeedback(null); setStrokeQuality(null); }, 800);
          });
      },

      onComplete: () => {
        setCount(total);
        const strokes = strokeResRef.current
          .map((r, i) => ({
            strokeIndex: i,
            dtwScore:    r?.dtwScore  ?? 0,
            quality:     r?.quality   ?? 'ok',
            direction:   r?.direction ?? '',
          }));

        // Lưu kết quả luyện viết lên backend
        saveKanjiWritingAttempt({
          kanjiId:        kanjiIdRef.current,
          characterValue: character,
          totalStrokes:   total,
          strokes,
        })
          .then((result) => { setFinalScore(result); })
          .catch(() => { setFinalScore(null); })
          .finally(() => {
            setDone(true);
            if (onComplete) onComplete();
          });
      },
    });
  }, [total, character, onComplete]);

  /* ── Tạo HanziWriter khi character thay đổi ─────────────────────── */
  useEffect(() => {
    if (!hwRef.current || !wrapRef.current) return;

    const size    = Math.round(wrapRef.current.getBoundingClientRect().width) || 400;
    const padding = Math.max(Math.round(size * 0.04), 8);

    setHwSize(size);
    setHwPadding(padding);
    hwSizeRef.current = size;

    hwRef.current.innerHTML = '';
    setCount(0);
    setFeedback(null);
    setDone(false);
    setHint(false);
    setStatus('loading');
    setStrokeQuality(null);
    setCharData(null);
    setFinalScore(null);
    strokeResRef.current = [];

    const writer = HanziWriter.create(hwRef.current, character, {
      width:   size,
      height:  size,
      padding,
      showOutline:        false,
      showCharacter:      false,
      strokeColor:        '#2D2D2D',
      highlightColor:     '#22C55E',
      drawingWidth:       8,
      highlightOnComplete: true,
      charDataLoader:     jpCharDataLoader,
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
    setFinalScore(null);
    strokeResRef.current = [];
    startQuiz(writerRef.current);
  }, [status, startQuiz]);

  /* ── Gợi ý ──────────────────────────────────────────────────────── */
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
          <div ref={hwRef} className="kpw-hw" />

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

          <WritingGrid />

          {status === 'loading' && (
            <div className="kpw-status-overlay">
              <div className="kpw-spinner-sm" />
            </div>
          )}

          {status === 'error' && (
            <div className="kpw-status-overlay">
              <p className="kpw-status-msg">Ký tự này chưa có dữ liệu nét</p>
            </div>
          )}

          {/* Quality badge — content từ API response */}
          {feedback && (
            <div className={`kpw-badge kpw-badge--${feedback}`} role="status">
              {feedback === 'ok'
                ? <QualityBadge quality={strokeQuality} />
                : '✖ Thử lại'}
            </div>
          )}
        </div>

        {/* Hướng nét tiếp theo — gợi ý UX, tính từ charData.medians */}
        {!done && status === 'ready' && (
          <div className="kpw-stroke-info">
            <span className="kpw-step-num">Nét <strong>{count + 1}</strong> / {total}</span>
            {nextStrokeDirLabel && (
              <span className="kpw-dir-label">
                <DirectionIcon /> {nextStrokeDirLabel}
              </span>
            )}
          </div>
        )}
      </div>

      {/* ── Thanh điều khiển ──────────────────────────────────────── */}
      <div className="kpw-ctrls">
        <button className="kpw-ctrl" onClick={handleReset} title="Đặt lại toàn bộ">
          <ResetIcon /><span>Đặt lại</span>
        </button>
        <button
          className={`kpw-ctrl${hint ? ' kpw-ctrl--on' : ''}`}
          onClick={handleHint}
          title={hint ? 'Ẩn gợi ý' : 'Hiện chữ mờ mẫu'}
        >
          <HintIcon /><span>Gợi ý</span>
        </button>
        <button
          className="kpw-ctrl"
          onClick={handleSkip}
          disabled={done || status !== 'ready'}
          title="Bỏ qua nét này"
        >
          <SkipIcon /><span>Bỏ qua</span>
        </button>
      </div>

      {/* ── Overlay hoàn thành — điểm từ API saveAttempt ─────────── */}
      {done && (
        <div className="kpw-done-veil">
          <div className="kpw-done-box">
            <div className="kpw-done-k" lang="ja">{character}</div>
            <h3 className="kpw-done-h">Hoàn thành! <ConfettiIcon size={22} /></h3>
            <FinalScore apiResult={finalScore} />
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
