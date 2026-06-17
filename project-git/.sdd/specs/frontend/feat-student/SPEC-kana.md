# SPEC — Học Kana (`/kana`)
> **UC:** UC-08 — Học Hiragana / Katakana
> **Sprint:** 3 — Core Content
> **Prefix:** `kna-` | **activeTab:** `'kana'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §8.2` | **Backend ref:** `feat-core-learning/SPEC.md UC-08`
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`

---

## 1. MÔ TẢ TRANG

Bảng chữ Kana (Hiragana + Katakana) chia theo hàng âm. Tab chuyển giữa Hiragana / Katakana. Mỗi ô hiển thị ký tự + romaji, click → mở `KanaDetailModal` với phát âm (audio) và stroke order (GIF). Nút "Đánh dấu đã học" cập nhật trạng thái ngay.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="kana")                                       │
├──────────────────────────────────────────────────────────────────┤
│  かな Kana                                                      │
│  Học bảng chữ Hiragana và Katakana cơ bản.                      │
│                                                                  │
│  [Hiragana]  [Katakana]                                          │
│                                                                  │
│  Đã học: 36 / 46  [████████░░] 78%                              │
│                                                                  │
│  Hàng A       あ    い    う    え    お                          │
│               a     i     u     e     o                          │
│                                                                  │
│  Hàng KA    か✓   き✓   く    け    こ                           │
│               ka    ki    ku    ke    ko                          │
│  ...                                                             │
│                                                                  │
│  ─── KanaDetailModal (khi click) ───                            │
│  ┌──────────────────────────────────────────────┐               │
│  │  あ  (a)                          [×]         │               │
│  │  [▶ Phát âm]  [Stroke order GIF]             │               │
│  │  Ví dụ từ: あした (ashita — ngày mai)         │               │
│  │            [✓ Đánh dấu đã học]               │               │
│  └──────────────────────────────────────────────┘               │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/kana/
├── KanaList.jsx
└── KanaList.css

components/student/
└── KanaDetailModal.jsx   ← modal chi tiết ký tự
```

---

## 4. STATE

```js
const [script,      setScript]  = useState('hiragana');   // 'hiragana' | 'katakana'
const [chars,       setChars]   = useState([]);           // KanaChar[]
const [stats,       setStats]   = useState({ completed: 0, total: 46 });
const [isLoading,   setLoading] = useState(true);
const [error,       setError]   = useState('');
const [selected,    setSelected]= useState(null);         // KanaChar | null (modal)
const [isSaving,    setSaving]  = useState(false);
```

`KanaChar`:
```js
{
  kanaId: number,
  character: string,      // 'あ'
  romaji: string,         // 'a'
  row: string,            // 'A', 'KA', 'SA', ...
  audioUrl: string,       // path đến file mp3
  strokeGifUrl: string,   // path đến stroke order GIF
  exampleWord: string,    // 'あした'
  exampleReading: string, // 'ashita'
  exampleMeaning: string, // 'ngày mai'
  isCompleted: boolean
}
```

---

## 5. API CALLS

```js
// GET /api/kana?script=hiragana
// Response:
{
  "data": {
    "script": "hiragana",
    "characters": [
      {
        "kanaId": 1,
        "character": "あ",
        "romaji": "a",
        "row": "A",
        "audioUrl": "/uploads/kana/hiragana/a.mp3",
        "strokeGifUrl": "/uploads/kana/hiragana/a-stroke.gif",
        "exampleWord": "あした",
        "exampleReading": "ashita",
        "exampleMeaning": "ngày mai",
        "isCompleted": false
      }
    ],
    "completedCount": 36,
    "totalCount": 46
  }
}

// POST /api/kana/{kanaId}/complete
// Request: (empty body)
// Response: { "data": { "kanaId": 1, "isCompleted": true } }
```

API service (`studentService.js`):
```js
export async function getKanaList(script) {
  const res = await api.get('/kana', { params: { script } });
  return res.data.data;
}

export async function markKanaComplete(kanaId) {
  const res = await api.post(`/kana/${kanaId}/complete`);
  return res.data.data;
}
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useMemo } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import KanaDetailModal from '../../components/student/KanaDetailModal';
import { getKanaList, markKanaComplete } from '../../api/studentService';
import './KanaList.css';

const SCRIPTS = [
  { id: 'hiragana', label: 'Hiragana' },
  { id: 'katakana', label: 'Katakana' },
];

export default function KanaList() {
  const [script,    setScript]  = useState('hiragana');
  const [chars,     setChars]   = useState([]);
  const [stats,     setStats]   = useState({ completed: 0, total: 46 });
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');
  const [selected,  setSelected]= useState(null);
  const [isSaving,  setSaving]  = useState(false);

  useEffect(() => {
    setLoading(true); setError('');
    getKanaList(script)
      .then((data) => {
        setChars(data.characters);
        setStats({ completed: data.completedCount, total: data.totalCount });
      })
      .catch((err) => setError(err?.response?.data?.message ?? 'Không thể tải bảng chữ Kana.'))
      .finally(() => setLoading(false));
  }, [script]);

  // Group characters by row
  const rows = useMemo(() => {
    const map = {};
    chars.forEach((c) => {
      if (!map[c.row]) map[c.row] = [];
      map[c.row].push(c);
    });
    return Object.entries(map); // [['A', [...]], ['KA', [...]]]
  }, [chars]);

  const handleComplete = async (kana) => {
    if (kana.isCompleted || isSaving) return;
    setSaving(true);
    try {
      await markKanaComplete(kana.kanaId);
      setChars((prev) =>
        prev.map((c) => (c.kanaId === kana.kanaId ? { ...c, isCompleted: true } : c))
      );
      setStats((prev) => ({ ...prev, completed: prev.completed + 1 }));
      setSelected((prev) => prev ? { ...prev, isCompleted: true } : null);
    } finally {
      setSaving(false);
    }
  };

  const progressPct = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <div className="kna-page">
      <TopNav activeTab="kana" />
      <main className="kna-body">
        <div className="kna-header">
          <h1 className="kna-title"><span lang="ja">かな</span> Kana</h1>
          <p className="kna-subtitle">Học bảng chữ Hiragana và Katakana cơ bản.</p>
        </div>

        {/* Script tabs */}
        <div className="kna-tabs" role="tablist" aria-label="Chọn bảng chữ">
          {SCRIPTS.map((s) => (
            <button
              key={s.id}
              role="tab"
              aria-selected={script === s.id}
              className={`kna-tab${script === s.id ? ' kna-tab--active' : ''}`}
              onClick={() => setScript(s.id)}
            >
              {s.label}
            </button>
          ))}
        </div>

        {/* Progress */}
        {!isLoading && (
          <div className="kna-progress-bar">
            <span className="kna-progress-text">
              Đã học <strong>{stats.completed}</strong> / {stats.total}
            </span>
            <div className="kna-progress-track"><ProgressBar value={progressPct} /></div>
            <span className="kna-progress-pct">{progressPct}%</span>
          </div>
        )}

        {error && (
          <div className="kna-error" role="alert">
            {error}
            <button className="kna-retry" onClick={() => setScript(script)}>Thử lại</button>
          </div>
        )}

        {/* Kana table by rows */}
        {isLoading ? (
          <div className="kna-skel" aria-hidden="true">
            {Array.from({ length: 10 }).map((_, i) => (
              <div key={i} className="kna-skel-row" />
            ))}
          </div>
        ) : (
          <div className="kna-table">
            {rows.map(([rowName, cells]) => (
              <div key={rowName} className="kna-row">
                <span className="kna-row-label">{rowName}</span>
                <div className="kna-row-cells">
                  {cells.map((c) => (
                    <button
                      key={c.kanaId}
                      className={`kna-cell${c.isCompleted ? ' kna-cell--done' : ''}`}
                      onClick={() => setSelected(c)}
                      aria-label={`${c.character} (${c.romaji})${c.isCompleted ? ' — đã học' : ''}`}
                    >
                      <span className="kna-char" lang="ja">{c.character}</span>
                      <span className="kna-romaji">{c.romaji}</span>
                      {c.isCompleted && <span className="kna-tick" aria-hidden="true">✓</span>}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {selected && (
        <KanaDetailModal
          kana={selected}
          isSaving={isSaving}
          onComplete={handleComplete}
          onClose={() => setSelected(null)}
        />
      )}
    </div>
  );
}
```

### KanaDetailModal

```jsx
// components/student/KanaDetailModal.jsx
export default function KanaDetailModal({ kana, isSaving, onComplete, onClose }) {
  const audioRef = useRef(null);
  // Close on Escape
  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div className="kna-modal-backdrop" role="dialog" aria-modal="true" aria-label={`Chi tiết: ${kana.character}`} onClick={onClose}>
      <div className="kna-modal" onClick={(e) => e.stopPropagation()}>
        <button className="kna-modal-close" onClick={onClose} aria-label="Đóng">×</button>
        <div className="kna-modal-char" lang="ja">{kana.character}</div>
        <div className="kna-modal-romaji">({kana.romaji})</div>
        <div className="kna-modal-actions-row">
          <button
            className="kna-btn-audio"
            onClick={() => audioRef.current?.play()}
            aria-label="Nghe phát âm"
          >
            ▶ Phát âm
          </button>
          <audio ref={audioRef} src={kana.audioUrl} preload="none" />
        </div>
        {kana.strokeGifUrl && (
          <img
            className="kna-stroke-gif"
            src={kana.strokeGifUrl}
            alt={`Thứ tự nét của ${kana.character}`}
          />
        )}
        {kana.exampleWord && (
          <div className="kna-example">
            <span className="kna-example-word" lang="ja">{kana.exampleWord}</span>
            <span className="kna-example-reading">({kana.exampleReading})</span>
            <span className="kna-example-meaning">— {kana.exampleMeaning}</span>
          </div>
        )}
        <button
          className={`kna-btn-complete${kana.isCompleted ? ' kna-btn-complete--done' : ''}`}
          onClick={() => onComplete(kana)}
          disabled={kana.isCompleted || isSaving}
          aria-label={kana.isCompleted ? 'Đã học' : 'Đánh dấu đã học'}
        >
          {kana.isCompleted ? '✓ Đã học' : isSaving ? 'Đang lưu...' : '✓ Đánh dấu đã học'}
        </button>
      </div>
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Kana List ===== */
.kna-page  { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.kna-body  { flex: 1; max-width: 900px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.kna-header  { display: flex; flex-direction: column; gap: 4px; }
.kna-title   { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.kna-subtitle{ font-size: 14px; color: var(--color-text-sub); margin: 0; }

/* Tabs */
.kna-tabs { display: flex; gap: 0; border-bottom: 2px solid var(--color-border); }
.kna-tab  { padding: 10px 24px; font-size: 15px; font-weight: 700; color: var(--color-text-sub); background: transparent; border: none; border-bottom: 2px solid transparent; margin-bottom: -2px; cursor: pointer; transition: color var(--transition), border-color var(--transition); }
.kna-tab--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }
.kna-tab:hover:not(.kna-tab--active) { color: var(--color-text); }

/* Progress */
.kna-progress-bar  { display: flex; align-items: center; gap: 12px; }
.kna-progress-text { font-size: 14px; color: var(--color-text-sub); white-space: nowrap; }
.kna-progress-track{ flex: 1; max-width: 300px; }
.kna-progress-pct  { font-size: 14px; font-weight: 700; color: var(--color-text); }

/* Error */
.kna-error { display: flex; align-items: center; justify-content: space-between; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.kna-retry { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* Table */
.kna-table { display: flex; flex-direction: column; gap: 4px; }
.kna-row   { display: flex; align-items: center; gap: 8px; padding: 6px 0; border-bottom: 1px solid var(--color-border); }
.kna-row:last-child { border-bottom: none; }
.kna-row-label { font-size: 11px; font-weight: 700; color: var(--color-text-sub); width: 36px; text-align: right; flex-shrink: 0; }
.kna-row-cells { display: flex; flex-wrap: wrap; gap: 6px; }

/* Kana cell */
.kna-cell {
  position: relative; display: flex; flex-direction: column; align-items: center; justify-content: center;
  width: 64px; height: 64px; border: 2px solid var(--color-border); border-radius: var(--radius-md);
  background: var(--color-card); cursor: pointer;
  transition: border-color var(--transition), background var(--transition), transform var(--transition);
}
.kna-cell:hover { border-color: var(--color-primary-light); background: var(--color-primary-bg); transform: scale(1.05); }
.kna-cell--done { background: var(--color-primary-bg); border-color: var(--color-primary-light); }
.kna-char   { font-size: 22px; font-weight: 700; color: var(--color-text); line-height: 1; }
.kna-cell--done .kna-char { color: var(--color-primary); }
.kna-romaji { font-size: 11px; color: var(--color-text-sub); }
.kna-tick   { position: absolute; bottom: 3px; right: 5px; font-size: 10px; color: var(--color-primary); font-weight: 700; }

/* Skeleton */
.kna-skel     { display: flex; flex-direction: column; gap: 10px; }
.kna-skel-row { height: 76px; border-radius: var(--radius-md); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }

/* Modal */
.kna-modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 16px; }
.kna-modal { background: var(--color-card); border-radius: var(--radius-xl); padding: 32px 28px 24px; max-width: 360px; width: 100%; display: flex; flex-direction: column; align-items: center; gap: 16px; position: relative; box-shadow: var(--shadow-xl); }
.kna-modal-close { position: absolute; top: 12px; right: 14px; background: transparent; border: none; font-size: 20px; color: var(--color-text-sub); cursor: pointer; line-height: 1; padding: 4px; }
.kna-modal-char { font-size: 72px; font-weight: 700; color: var(--color-text); line-height: 1; }
.kna-modal-romaji { font-size: 20px; color: var(--color-text-sub); }
.kna-modal-actions-row { display: flex; gap: 12px; }
.kna-btn-audio { height: 36px; padding: 0 16px; background: var(--color-primary-bg); border: 1.5px solid var(--color-primary-light); border-radius: var(--radius-full); color: var(--color-primary); font-size: 13px; font-weight: 700; cursor: pointer; }
.kna-stroke-gif { width: 120px; height: 120px; object-fit: contain; border: 1px solid var(--color-border); border-radius: var(--radius-md); }
.kna-example { font-size: 14px; color: var(--color-text-sub); text-align: center; }
.kna-example-word { font-size: 18px; font-weight: 700; color: var(--color-text); margin-right: 6px; }
.kna-btn-complete { width: 100%; height: 44px; background: var(--color-primary); color: white; border: none; border-radius: var(--radius-full); font-size: 15px; font-weight: 700; cursor: pointer; transition: filter var(--transition); }
.kna-btn-complete:hover:not(:disabled) { filter: brightness(1.08); }
.kna-btn-complete--done { background: var(--color-secondary); cursor: default; }
.kna-btn-complete:disabled { opacity: 0.65; cursor: not-allowed; }

@media (max-width: 767px) {
  .kna-body  { padding: 16px 16px 32px; }
  .kna-cell  { width: 52px; height: 52px; }
  .kna-char  { font-size: 18px; }
}
@media (prefers-reduced-motion: reduce) { .kna-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 8. ACCESSIBILITY

- [ ] Script tabs dùng `role="tablist"` + `role="tab"` + `aria-selected`
- [ ] Modal có `role="dialog"`, `aria-modal="true"`, Escape đóng, focus trap
- [ ] Stroke GIF có `alt` mô tả đầy đủ
- [ ] Audio player không autoplay — user phải click ▶
- [ ] Nút "Đánh dấu đã học" disable đúng khi đã học hoặc đang lưu
