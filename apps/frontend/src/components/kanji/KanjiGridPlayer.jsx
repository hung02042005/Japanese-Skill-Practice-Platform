import { useEffect, useRef, useState, useCallback } from 'react';
import HanziWriter from 'hanzi-writer';
import KanjiStrokeLayer from './KanjiStrokeLayer';
import { jpCharDataLoader } from '../../utils/kanjiLookup';

/* ── Practice grid overlay ───────────────────────────────────────────── */
function PracticeGrid() {
  const lines = [];
  for (let i = 1; i < 8; i++) {
    const pct = `${(i / 8) * 100}%`;
    const mid = i === 4;
    lines.push(
      <line key={`h${i}`} x1="0" y1={pct} x2="100%" y2={pct}
        stroke={mid ? '#C5BDB9' : '#E5DDD9'}
        strokeWidth={mid ? '1' : '0.5'}
        strokeDasharray={mid ? '6 4' : undefined} />,
      <line key={`v${i}`} x1={pct} y1="0" x2={pct} y2="100%"
        stroke={mid ? '#C5BDB9' : '#E5DDD9'}
        strokeWidth={mid ? '1' : '0.5'}
        strokeDasharray={mid ? '6 4' : undefined} />
    );
  }
  return (
    <svg className="kgp-grid" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
      {lines}
    </svg>
  );
}

const BOX_SIZE = 240;

/**
 * KanjiGridPlayer
 * Dùng hanzi-writer để hiển thị animation từng nét chính xác theo stroke order.
 * animateStroke(i) → chain → next stroke until all done.
 */
export default function KanjiGridPlayer({ character, strokeCount, onStrokeChange }) {
  const hwRef       = useRef(null); // div mà HanziWriter render SVG vào
  const writerRef   = useRef(null);
  const animatingRef = useRef(false);

  const total = Math.max(strokeCount || 1, 1);
  const [completedStrokes, setCompletedStrokes] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [status, setStatus] = useState('loading'); // 'loading' | 'ready' | 'error'

  /* ── Tạo HanziWriter mỗi khi character thay đổi ─────────────────── */
  useEffect(() => {
    if (!hwRef.current) return;

    animatingRef.current = false;
    setPlaying(false);
    setCompletedStrokes(0);
    onStrokeChange?.(0);
    setStatus('loading');
    hwRef.current.innerHTML = '';

    const writer = HanziWriter.create(hwRef.current, character, {
      width:  BOX_SIZE,
      height: BOX_SIZE,
      padding: 10,
      showOutline:   true,
      showCharacter: false,
      strokeColor:   '#2D2D2D',     // nét đã vẽ xong — ink đậm
      outlineColor:  '#D8D0CC',     // outline mờ làm nền cho các nét chưa đến
      radicalColor:  '#E89AAA',     // bộ thủ — sakura pink
      strokeAnimationSpeed: 1,
      delayBetweenStrokes: 0,
      charDataLoader: jpCharDataLoader,
      onLoadCharDataSuccess: () => setStatus('ready'),
      onLoadCharDataError:   () => setStatus('error'),
    });

    writerRef.current = writer;

    return () => {
      animatingRef.current = false;
      writerRef.current = null;
    };
  }, [character]); // eslint-disable-line react-hooks/exhaustive-deps

  /* ── Chạy từng nét theo chuỗi đệ quy ───────────────────────────── */
  const animateFrom = useCallback((idx) => {
    if (!animatingRef.current || !writerRef.current) return;
    if (idx >= total) {
      setPlaying(false);
      animatingRef.current = false;
      return;
    }

    writerRef.current.animateStroke(idx, {
      onComplete: () => {
        const done = idx + 1;
        setCompletedStrokes(done);
        onStrokeChange?.(done);
        animateFrom(done); // đệ quy sang nét kế tiếp
      },
    });
  }, [total, onStrokeChange]);

  /* ── Nút Play / Tiếp tục / Phát lại ────────────────────────────── */
  const handlePlay = useCallback(() => {
    if (status !== 'ready' || !writerRef.current) return;

    if (completedStrokes >= total) {
      // Đã xong → replay từ đầu
      writerRef.current.hideCharacter();
      setCompletedStrokes(0);
      onStrokeChange?.(0);
      animatingRef.current = true;
      setPlaying(true);
      setTimeout(() => animateFrom(0), 50);
    } else {
      // Tiếp tục từ nét đang dở
      animatingRef.current = true;
      setPlaying(true);
      animateFrom(completedStrokes);
    }
  }, [status, completedStrokes, total, onStrokeChange, animateFrom]);

  const handlePause = useCallback(() => {
    // Dừng chain — nét hiện tại vẫn hoàn thành nhưng không sang nét tiếp
    animatingRef.current = false;
    setPlaying(false);
  }, []);

  const handleReplay = useCallback(() => {
    if (status !== 'ready' || !writerRef.current) return;
    animatingRef.current = false;
    setPlaying(false);
    writerRef.current.hideCharacter();
    setCompletedStrokes(0);
    onStrokeChange?.(0);
    setTimeout(() => {
      if (!writerRef.current) return;
      animatingRef.current = true;
      setPlaying(true);
      animateFrom(0);
    }, 60);
  }, [status, onStrokeChange, animateFrom]);

  const playLabel =
    completedStrokes >= total ? 'Phát lại'
    : completedStrokes > 0   ? 'Tiếp tục'
    : 'Phát';

  return (
    <div className="kgp-wrap">
      {/* The square practice box */}
      <div className="kgp-box" aria-label={`Hướng dẫn viết ${character}, nét ${completedStrokes}/${total}`}>
        {/* HanziWriter SVG render vào đây */}
        <div ref={hwRef} className="kgp-hw" />

        {/* Future strokes ghost — chỉ preview mode, không guide */}
        {status === 'ready' && (
          <KanjiStrokeLayer
            character={character}
            completedCount={completedStrokes}
            containerSize={BOX_SIZE}
            hwPadding={10}
            mode="preview"
          />
        )}

        {/* Lưới kẻ mờ — pointer-events: none */}
        <PracticeGrid />
        {/* Counter góc trên phải */}
        <span className="kgp-counter">
          Nét {completedStrokes === 0 ? '–' : completedStrokes} / {total}
        </span>
      </div>

      {/* ── Dot progress ── */}
      <div className="kgp-dots" aria-hidden="true">
        {Array.from({ length: total }, (_, i) => (
          <span key={i} className={
            'kgp-dot' +
            (i < completedStrokes       ? ' kgp-dot--done' : '') +
            (i === completedStrokes - 1 ? ' kgp-dot--cur'  : '')
          } />
        ))}
      </div>

      {/* ── Controls ── */}
      <div className="kgp-ctrls">
        {status === 'loading' && (
          <span className="kgp-status-text">Đang tải dữ liệu nét…</span>
        )}
        {status === 'error' && (
          <span className="kgp-status-text kgp-status-text--err">
            Ký tự này chưa có dữ liệu nét
          </span>
        )}
        {status === 'ready' && (
          <>
            {!playing ? (
              <button className="kgp-btn kgp-btn--play" onClick={handlePlay}
                aria-label="Phát animation nét chữ">
                <PlayIcon /> {playLabel}
              </button>
            ) : (
              <button className="kgp-btn kgp-btn--pause" onClick={handlePause}
                aria-label="Dừng animation">
                <PauseIcon /> Dừng
              </button>
            )}
            <button className="kgp-btn kgp-btn--ghost" onClick={handleReplay}
              aria-label="Xem lại từ đầu">
              <ReplayIcon /> Xem lại
            </button>
          </>
        )}
      </div>
    </div>
  );
}

/* ── Icons ───────────────────────────────────────────────────────────── */
function PlayIcon() {
  return (
    <svg width="13" height="13" viewBox="0 0 14 14" fill="currentColor" aria-hidden="true">
      <polygon points="2,1 13,7 2,13" />
    </svg>
  );
}
function PauseIcon() {
  return (
    <svg width="13" height="13" viewBox="0 0 14 14" fill="currentColor" aria-hidden="true">
      <rect x="2" y="1" width="4" height="12" />
      <rect x="8" y="1" width="4" height="12" />
    </svg>
  );
}
function ReplayIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <polyline points="1 4 1 10 7 10" />
      <path d="M3.51 15a9 9 0 1 0 .49-3.51" />
    </svg>
  );
}
