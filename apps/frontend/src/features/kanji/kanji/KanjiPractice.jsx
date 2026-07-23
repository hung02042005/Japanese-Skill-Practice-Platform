import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import TopNav from '@/shared/components/layout/TopNav';
import { JlptBadge } from '@/shared/components/common/Badges';
import { ToastContainer, useToast } from '@/shared/components/common/Toast';
import KanjiGridPlayer from '@/features/kanji/components/KanjiGridPlayer';
import KanjiWritingCanvas from '@/features/kanji/components/KanjiWritingCanvas';
import { getKanjiDetail, markProgress } from '@/shared/api/studentService';
import './KanjiPractice.css';

export default function KanjiPractice() {
  const { id }   = useParams();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [kanji,      setKanji]     = useState(null);
  const [isLoading,  setLoading]   = useState(true);
  const [error,      setError]     = useState('');
  const [mode,       setMode]      = useState('learn'); // 'learn' | 'write'
  const [, setCurrentStroke] = useState(0);
  const markedRef = useRef(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      setMode('learn');
      setCurrentStroke(0);
      markedRef.current = false;
      try {
        const data = await getKanjiDetail(id);
        if (!cancelled) setKanji(data);
      } catch (err) {
        if (cancelled) return;
        if (err?.response?.status === 404) { navigate('/404'); return; }
        setError('Không thể tải thông tin Kanji.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [id, navigate]);

  const handleStrokeChange = useCallback(n => setCurrentStroke(n), []);

  const handleWritingComplete = useCallback(async () => {
    if (markedRef.current) return;
    markedRef.current = true;
    try {
      await markProgress('kanji', kanji.kanjiId, 'completed', 100);
      addToast('success', `Đã đánh dấu "${kanji.characterValue}" là đã học!`);
    } catch {
      markedRef.current = false;
    }
  }, [kanji, addToast]);

  // ── Loading ──────────────────────────────────────────────────────────────
  if (isLoading) {
    return (
      <div className="kp-page">
        <TopNav activeTab="kanji" />
        <div className="kp-loading">
          <div className="kp-spinner" />
          <p className="kp-loading-text">Đang tải Kanji…</p>
        </div>
      </div>
    );
  }

  // ── Error ─────────────────────────────────────────────────────────────────
  if (error) {
    return (
      <div className="kp-page">
        <TopNav activeTab="kanji" />
        <div className="kp-body">
          <div className="kp-error" role="alert">{error}</div>
        </div>
      </div>
    );
  }

  // ── Writing mode ──────────────────────────────────────────────────────────
  if (mode === 'write') {
    return (
      <div className="kp-page kp-page--write">
        <KanjiWritingCanvas
          kanjiId={kanji.kanjiId}
          character={kanji.characterValue}
          strokeCount={kanji.strokeCount}
          onBack={() => setMode('learn')}
          onComplete={handleWritingComplete}
        />
        <ToastContainer toasts={toasts} onRemove={removeToast} />
      </div>
    );
  }

  // ── Learning view ─────────────────────────────────────────────────────────
  const onyomiList  = kanji.onyomi  ? kanji.onyomi.split('、').map(s => s.trim()).filter(Boolean)  : [];
  const kunyomiList = kanji.kunyomi ? kanji.kunyomi.split('、').map(s => s.trim()).filter(Boolean) : [];

  return (
    <div className="kp-page">
      <TopNav activeTab="kanji" />

      <main className="kp-body">
        {/* Breadcrumb */}
        <div className="kp-breadcrumb">
          <Link to="/kanji" className="kp-back-link">← Danh sách Kanji</Link>
          <JlptBadge level={kanji.jlptLevel} />
        </div>

        {/* ── Section 1: Kanji grid player ── */}
        <section className="kp-grid-section">
          <div className="kp-grid-card">
            {/* Stroke counter badge */}
            <div className="kp-stroke-badge">
              {kanji.strokeCount} nét
            </div>

            {/* KanjiGridPlayer — centre box with animation */}
            <KanjiGridPlayer
              character={kanji.characterValue}
              strokeCount={kanji.strokeCount}
              onStrokeChange={handleStrokeChange}
            />
          </div>
        </section>

        {/* ── Section 2: Reading + meaning info ── */}
        <section className="kp-info-section">
          {/* Meaning chip */}
          <div className="kp-meaning-row">
            <span className="kp-meaning-label">Nghĩa</span>
            <span className="kp-meaning-value">{kanji.meaning}</span>
          </div>

          {/* Reading cards */}
          <div className="kp-readings">
            {onyomiList.length > 0 && (
              <div className="kp-reading-card kp-reading-card--on">
                <span className="kp-reading-label">On&apos;yomi</span>
                <div className="kp-reading-values">
                  {onyomiList.map((r, i) => (
                    <span key={i} className="kp-reading-chip kp-reading-chip--on" lang="ja">{r}</span>
                  ))}
                </div>
              </div>
            )}
            {kunyomiList.length > 0 && (
              <div className="kp-reading-card kp-reading-card--kun">
                <span className="kp-reading-label">Kun&apos;yomi</span>
                <div className="kp-reading-values">
                  {kunyomiList.map((r, i) => (
                    <span key={i} className="kp-reading-chip kp-reading-chip--kun" lang="ja">{r}</span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Example word */}
          {kanji.exampleWord && (
            <div className="kp-example">
              <span className="kp-example-label">Ví dụ</span>
              <div className="kp-example-content">
                <span className="kp-example-word" lang="ja">{kanji.exampleWord}</span>
                {kanji.exampleReading && (
                  <span className="kp-example-reading" lang="ja">（{kanji.exampleReading}）</span>
                )}
                {kanji.exampleMeaning && (
                  <span className="kp-example-meaning">— {kanji.exampleMeaning}</span>
                )}
              </div>
            </div>
          )}
        </section>

        {/* ── Start learning CTA ── */}
        <div className="kp-cta-wrap">
          <button
            id="btn-start-learning"
            className="kp-start-btn"
            onClick={() => setMode('write')}
            aria-label={`Bắt đầu luyện viết Kanji ${kanji.characterValue}`}
          >
            <WriteIcon />
            Bắt đầu luyện viết
          </button>         
        </div>

        {/* ── Prev / Next nav ── */}
        <div className="kp-footer">
          {kanji.prevKanjiId ? (
            <button className="kp-nav-btn" onClick={() => navigate(`/kanji/${kanji.prevKanjiId}`)}>
              ← Kanji trước
            </button>
          ) : <div />}
          {kanji.nextKanjiId ? (
            <button className="kp-nav-btn kp-nav-btn--next" onClick={() => navigate(`/kanji/${kanji.nextKanjiId}`)}>
              Kanji tiếp theo →
            </button>
          ) : <div />}
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}

function WriteIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
    </svg>
  );
}

