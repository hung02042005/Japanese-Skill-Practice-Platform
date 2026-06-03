import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { getKanjiDetail, submitOcr, getOcrResult, addToFlashcard, markProgress } from '../../api/studentService';
import { DEMO_MODE, MOCK_KANJI_DETAIL_MAP, MOCK_KANJI_DETAIL_DEFAULT } from '../../api/mockData';
import './KanjiPractice.css';

function SimilarityGauge({ value }) {
  const level  = value >= 80 ? 'excellent' : value >= 60 ? 'good' : 'practice';
  const labels = { excellent: 'Xuất sắc 🌸', good: 'Tốt ✓', practice: 'Cần luyện thêm' };
  return (
    <div className={`koc-gauge koc-gauge--${level}`}>
      <div className="koc-gauge-bar-wrap">
        <div className="koc-gauge-bar">
          <div className="koc-gauge-fill" style={{ width: `${value}%` }} />
        </div>
      </div>
      <div className="koc-gauge-info">
        <span className="koc-gauge-pct">{value}%</span>
        <span className="koc-gauge-label">{labels[level]}</span>
      </div>
    </div>
  );
}

export default function KanjiPractice() {
  const { id }   = useParams();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [kanji,       setKanji]    = useState(null);
  const [isLoading,   setLoading]  = useState(true);
  const [error,       setError]    = useState('');
  const [file,        setFile]     = useState(null);
  const [preview,     setPreview]  = useState(null);
  const [ocrState,    setOcrState] = useState('idle');
  const [similarity,  setSim]      = useState(null);
  const [ocrError,    setOcrErr]   = useState('');
  const [isAnalyzing, setAnalyzing]= useState(false);
  const [addedFlash,  setAdded]    = useState(false);
  const pollRef = useRef(null);

  useEffect(() => {
    (async () => {
      setLoading(true);
      setError('');
      try {
        if (DEMO_MODE) {
          const data = MOCK_KANJI_DETAIL_MAP[Number(id)] ?? MOCK_KANJI_DETAIL_DEFAULT;
          setKanji(data);
          setLoading(false);
          return;
        }
        const data = await getKanjiDetail(id);
        setKanji(data);
      } catch (err) {
        if (err?.response?.status === 404) { navigate('/404'); return; }
        setError('Không thể tải thông tin Kanji.');
      } finally {
        setLoading(false);
      }
    })();
    return () => clearInterval(pollRef.current);
  }, [id, navigate]);

  useEffect(() => {
    setFile(null);
    setPreview(null);
    setOcrState('idle');
    setSim(null);
    setOcrErr('');
  }, [id]);

  function handleFileChange(e) {
    const f = e.target.files?.[0];
    if (!f) return;
    if (f.size > 5 * 1024 * 1024) { addToast('error', 'Ảnh tối đa 5MB.'); return; }
    setFile(f);
    setPreview(URL.createObjectURL(f));
    setOcrState('idle');
    setSim(null);
    setOcrErr('');
  }

  async function handleAnalyze() {
    if (!file) return;
    setAnalyzing(true);
    setOcrErr('');
    setOcrState('pending');
    if (DEMO_MODE) {
      setTimeout(() => {
        setSim(75);
        setOcrState('done');
        setAnalyzing(false);
      }, 1500);
      return;
    }
    try {
      const res = await submitOcr(kanji.kanjiId, file);
      startPolling(res.jobId);
    } catch {
      setOcrState('error');
      setOcrErr('Không thể gửi ảnh. Thử lại sau.');
      setAnalyzing(false);
    }
  }

  function startPolling(jobId) {
    let tries = 0;
    pollRef.current = setInterval(async () => {
      tries++;
      if (tries > 30) {
        clearInterval(pollRef.current);
        setOcrState('error');
        setOcrErr('Phân tích quá lâu. Vui lòng thử lại.');
        setAnalyzing(false);
        return;
      }
      try {
        const result = await getOcrResult(jobId);
        if (result.status === 'COMPLETED') {
          clearInterval(pollRef.current);
          setSim(result.similarity);
          setOcrState('done');
          setAnalyzing(false);
          markProgress('kanji', kanji.kanjiId, 'completed').catch(() => {});
        } else if (result.status === 'FAILED') {
          clearInterval(pollRef.current);
          setOcrState('error');
          setOcrErr('AI không thể phân tích ảnh này.');
          setAnalyzing(false);
        }
      } catch { /* keep polling */ }
    }, 1000);
  }

  async function handleAddFlashcard() {
    try {
      if (!DEMO_MODE) await addToFlashcard('kanji', kanji.kanjiId);
      setAdded(true);
      addToast('success', `Đã thêm "${kanji.characterValue}" vào Flashcard!`);
    } catch (err) {
      if (err?.response?.status === 409) { addToast('info', 'Kanji này đã có trong Flashcard.'); return; }
      addToast('error', 'Không thể thêm vào Flashcard.');
    }
  }

  if (isLoading) {
    return (
      <div className="koc-page">
        <TopNav activeTab="kanji" />
        <div className="koc-loading"><div className="koc-spinner-lg" /></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="koc-page">
        <TopNav activeTab="kanji" />
        <div className="koc-body"><div className="koc-error" role="alert">{error}</div></div>
      </div>
    );
  }

  return (
    <div className="koc-page">
      <TopNav activeTab="kanji" />
      <main className="koc-body">
        <div className="koc-nav-header">
          <Link to="/kanji" className="koc-back-link">← Danh sách Kanji</Link>
          <JlptBadge level={kanji.jlptLevel} />
        </div>

        {/* Info card */}
        <div className="koc-info-card">
          <div className="koc-main-row">
            <div className="koc-char-display">
              <span className="koc-char" lang="ja">{kanji.characterValue}</span>
              <span className="koc-stroke-count">{kanji.strokeCount} nét</span>
            </div>
            <div className="koc-readings">
              {kanji.onyomi  && <div className="koc-reading-row"><span className="koc-reading-label">On'yomi:</span><span className="koc-reading-val" lang="ja">{kanji.onyomi}</span></div>}
              {kanji.kunyomi && <div className="koc-reading-row"><span className="koc-reading-label">Kun'yomi:</span><span className="koc-reading-val" lang="ja">{kanji.kunyomi}</span></div>}
              <div className="koc-reading-row"><span className="koc-reading-label">Nghĩa:</span><span className="koc-reading-val">{kanji.meaning}</span></div>
            </div>
            <button
              className={`koc-flash-btn${addedFlash ? ' koc-flash-btn--added' : ''}`}
              onClick={handleAddFlashcard}
              disabled={addedFlash}
              aria-label={`${addedFlash ? 'Đã thêm' : 'Thêm'} "${kanji.characterValue}" vào Flashcard`}
            >
              {addedFlash ? '✓ Đã thêm' : '+ Flashcard'}
            </button>
          </div>

          {kanji.strokeOrderUrl && (
            <div className="koc-stroke-wrap">
              <img
                src={kanji.strokeOrderUrl}
                alt={`Thứ tự nét viết của ${kanji.characterValue}`}
                className="koc-stroke-img"
              />
            </div>
          )}

          {kanji.exampleWord && (
            <div className="koc-example">
              <span className="koc-ex-label">Ví dụ từ:</span>
              <span className="koc-ex-word" lang="ja">{kanji.exampleWord}</span>
              {kanji.exampleReading && <span className="koc-ex-reading" lang="ja">({kanji.exampleReading})</span>}
              {kanji.exampleMeaning && <span className="koc-ex-meaning">— {kanji.exampleMeaning}</span>}
            </div>
          )}
        </div>

        {/* OCR Panel */}
        <section className="koc-ocr-section">
          <h2 className="koc-ocr-title">Luyện viết (OCR)</h2>

          <div className="koc-upload-area">
            {!preview ? (
              <label className="koc-upload-label" htmlFor="koc-file-input">
                <div className="koc-upload-icon" aria-hidden="true">📷</div>
                <span className="koc-upload-text">Tải ảnh chữ viết tay của bạn</span>
                <span className="koc-upload-hint">Hỗ trợ JPG, PNG, WEBP · Tối đa 5MB</span>
                <input
                  id="koc-file-input"
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  className="koc-sr-only"
                  onChange={handleFileChange}
                />
              </label>
            ) : (
              <div className="koc-preview-wrap">
                <img src={preview} alt="Ảnh chữ viết tay" className="koc-preview-img" />
                <button
                  className="koc-change-btn"
                  onClick={() => { setFile(null); setPreview(null); setOcrState('idle'); }}
                >
                  Đổi ảnh
                </button>
              </div>
            )}
          </div>

          {file && ocrState === 'idle' && (
            <button
              className="koc-analyze-btn"
              onClick={handleAnalyze}
              disabled={isAnalyzing}
              aria-busy={isAnalyzing}
            >
              {isAnalyzing && <span className="koc-spinner" aria-hidden="true" />}
              Phân tích ngay →
            </button>
          )}

          {ocrState === 'pending' && (
            <div className="koc-ocr-pending" role="status">
              <div className="koc-spinner-md" />
              <span>AI đang phân tích chữ viết của bạn...</span>
            </div>
          )}

          {ocrState === 'done' && similarity !== null && (
            <div className="koc-ocr-result">
              <SimilarityGauge value={similarity} />
              <button
                className="koc-retry-ocr-btn"
                onClick={() => { setOcrState('idle'); setFile(null); setPreview(null); setSim(null); }}
              >
                Luyện lại
              </button>
            </div>
          )}

          {ocrState === 'error' && (
            <div className="koc-ocr-error" role="alert">
              <span>{ocrError}</span>
              <button
                className="koc-retry-btn"
                onClick={() => { setOcrState('idle'); setSim(null); }}
              >
                Thử lại
              </button>
            </div>
          )}
        </section>

        {/* Footer navigation */}
        <div className="koc-footer">
          {kanji.prevKanjiId
            ? <button className="koc-nav-btn" onClick={() => navigate(`/kanji/${kanji.prevKanjiId}`)}>← Kanji trước</button>
            : <div />
          }
          {kanji.nextKanjiId
            ? <button className="koc-nav-btn koc-nav-btn--next" onClick={() => navigate(`/kanji/${kanji.nextKanjiId}`)}>Kanji tiếp theo →</button>
            : <div />
          }
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
