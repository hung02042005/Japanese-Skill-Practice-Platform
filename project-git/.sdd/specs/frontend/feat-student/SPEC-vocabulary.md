# SPEC — Học Từ Vựng (`/vocabulary`)
> **UC:** UC-09 — Học Từ vựng theo Level/Topic
> **Sprint:** 3 — Core Content
> **Prefix:** `voc-` | **activeTab:** `'vocabulary'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §9.1` | **Backend ref:** `feat-core-learning/SPEC.md UC-09`
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`

---

## 1. MÔ TẢ TRANG

Danh sách từ vựng lọc theo JLPT level và chủ đề (topic). Mỗi thẻ từ hiển thị từ, cách đọc, nghĩa, loại từ, nút phát âm. Click "Thêm Flashcard" → thêm từ vào bộ Flashcard cá nhân. Đánh dấu đã học để cập nhật tiến độ.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="vocabulary")                                 │
├──────────────────────────────────────────────────────────────────┤
│  語彙 Từ Vựng                                                   │
│  Học từ vựng JLPT theo cấp độ và chủ đề.                        │
│                                                                  │
│  [N5]  [N4]  [N3]  [N2]  [N1]                                   │
│                                                                  │
│  N5: đã học 124 / 800 từ  [██░░░░░] 16%                        │
│                                                                  │
│  Chủ đề: [Tất cả▼]   🔍 [Tìm từ vựng...]                       │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  食べる  たべる    [▶]                          [+FC] [✓] │   │
│  │  Động từ · ăn                                            │   │
│  │  Ví dụ: 毎日ご飯を食べる。(Tôi ăn cơm mỗi ngày.)        │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  学校  がっこう   [▶]                           [+FC] [✓]│   │
│  │  Danh từ · trường học                                    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  [← 1  2  3  →]   Hiển thị 1–20 / 800 từ                       │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/vocabulary/
├── VocabularyList.jsx
└── VocabularyList.css

components/student/
└── VocabCard.jsx   ← card từng từ vựng
```

---

## 4. STATE

```js
const [level,       setLevel]   = useState(user?.jlptLevel ?? 'N5');
const [topic,       setTopic]   = useState('');           // '' = tất cả
const [search,      setSearch]  = useState('');
const [debounced,   setDebounced] = useState('');
const [words,       setWords]   = useState([]);
const [stats,       setStats]   = useState({ completed: 0, total: 0 });
const [topics,      setTopics]  = useState([]);           // danh sách topic
const [isLoading,   setLoading] = useState(true);
const [error,       setError]   = useState('');
const [page,        setPage]    = useState(1);
const [totalPages,  setTotal]   = useState(1);
const [totalElements, setTotalEl] = useState(0);
const [actionState, setAction] = useState({});  // { [vocabId]: 'adding' | 'added' | 'completing' | 'done' }
const timerRef = useRef(null);
const PAGE_SIZE = 20;
```

Debounce search 400ms. Reset `page → 1` khi `level`, `topic`, `debounced` thay đổi.

---

## 5. API CALLS

```js
// GET /api/vocabulary?level=N5&topic=food&search=食&page=0&size=20
// Response:
{
  "data": {
    "content": [
      {
        "vocabId": 1,
        "word": "食べる",
        "reading": "たべる",
        "meaning": "ăn",
        "partOfSpeech": "動詞",
        "jlptLevel": "N5",
        "topic": "food",
        "exampleSentence": "毎日ご飯を食べる。",
        "exampleTranslation": "Tôi ăn cơm mỗi ngày.",
        "audioUrl": "/uploads/vocab/taberu.mp3",
        "isCompleted": false,
        "isInFlashcard": false
      }
    ],
    "totalElements": 800,
    "totalPages": 40,
    "completedCount": 124
  }
}

// GET /api/vocabulary/topics?level=N5
// Response: { "data": ["food", "family", "time", "body", "nature", ...] }

// POST /api/vocabulary/{vocabId}/complete
// Response: { "data": { "vocabId": 1, "isCompleted": true } }

// POST /api/flashcard/add
// Request: { "vocabId": 1 }
// Response: { "data": { "cardId": 55, "message": "Đã thêm vào Flashcard" } }
```

API service (`studentService.js`):
```js
export async function getVocabularyList({ level, topic, search, page = 0, size = 20 } = {}) {
  const params = { level, page, size };
  if (topic)  params.topic  = topic;
  if (search) params.search = search;
  const res = await api.get('/vocabulary', { params });
  return res.data.data;
}

export async function getVocabTopics(level) {
  const res = await api.get('/vocabulary/topics', { params: { level } });
  return res.data.data;
}

export async function markVocabComplete(vocabId) {
  const res = await api.post(`/vocabulary/${vocabId}/complete`);
  return res.data.data;
}

export async function addVocabToFlashcard(vocabId) {
  const res = await api.post('/flashcard/add', { vocabId });
  return res.data.data;
}
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useRef, useCallback } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ProgressBar } from '../../components/common/ProgressBar';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import VocabCard from '../../components/student/VocabCard';
import { getVocabularyList, getVocabTopics, markVocabComplete, addVocabToFlashcard } from '../../api/studentService';
import './VocabularyList.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function VocabularyList() {
  const { user } = useAppSelector((s) => s.auth);
  const [level,       setLevel]   = useState(user?.jlptLevel ?? 'N5');
  const [topic,       setTopic]   = useState('');
  const [search,      setSearch]  = useState('');
  const [debounced,   setDebounced] = useState('');
  const [words,       setWords]   = useState([]);
  const [stats,       setStats]   = useState({ completed: 0, total: 0 });
  const [topics,      setTopics]  = useState([]);
  const [isLoading,   setLoading] = useState(true);
  const [error,       setError]   = useState('');
  const [page,        setPage]    = useState(1);
  const [totalPages,  setTotal]   = useState(1);
  const [totalEl,     setTotalEl] = useState(0);
  const [actionState, setAction]  = useState({});
  const timerRef = useRef(null);

  // Debounce
  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(search), 400);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  // Reset page on filter change
  useEffect(() => { setPage(1); }, [level, topic, debounced]);

  // Fetch topics on level change
  useEffect(() => {
    setTopic('');
    getVocabTopics(level).then(setTopics).catch(() => {});
  }, [level]);

  const fetchWords = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const data = await getVocabularyList({ level, topic, search: debounced, page: page - 1, size: 20 });
      setWords(data.content);
      setTotal(data.totalPages);
      setTotalEl(data.totalElements);
      setStats({ completed: data.completedCount ?? 0, total: data.totalElements });
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải từ vựng.');
    } finally {
      setLoading(false);
    }
  }, [level, topic, debounced, page]);

  useEffect(() => { fetchWords(); }, [fetchWords]);

  const handleComplete = async (vocabId) => {
    setAction((prev) => ({ ...prev, [vocabId]: 'completing' }));
    try {
      await markVocabComplete(vocabId);
      setWords((prev) => prev.map((w) => w.vocabId === vocabId ? { ...w, isCompleted: true } : w));
      setStats((prev) => ({ ...prev, completed: prev.completed + 1 }));
      setAction((prev) => ({ ...prev, [vocabId]: 'done' }));
    } catch {
      setAction((prev) => { const s = { ...prev }; delete s[vocabId]; return s; });
    }
  };

  const handleAddFlashcard = async (vocabId) => {
    setAction((prev) => ({ ...prev, [`fc_${vocabId}`]: 'adding' }));
    try {
      await addVocabToFlashcard(vocabId);
      setWords((prev) => prev.map((w) => w.vocabId === vocabId ? { ...w, isInFlashcard: true } : w));
      setAction((prev) => ({ ...prev, [`fc_${vocabId}`]: 'added' }));
    } catch {
      setAction((prev) => { const s = { ...prev }; delete s[`fc_${vocabId}`]; return s; });
    }
  };

  const progressPct = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <div className="voc-page">
      <TopNav activeTab="vocabulary" />
      <main className="voc-body">
        <div className="voc-header">
          <h1 className="voc-title"><span lang="ja">語彙</span> Từ Vựng</h1>
          <p className="voc-subtitle">Học từ vựng JLPT theo cấp độ và chủ đề.</p>
        </div>

        {/* Level tabs */}
        <div className="voc-level-tabs" role="tablist" aria-label="Chọn cấp độ JLPT">
          {LEVELS.map((l) => (
            <button key={l} role="tab" aria-selected={level === l}
              className={`voc-level-tab${level === l ? ' voc-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}>{l}</button>
          ))}
        </div>

        {/* Stats */}
        {!isLoading && (
          <div className="voc-stats">
            <JlptBadge level={level} />
            <span className="voc-stats-text">đã học <strong>{stats.completed}</strong> / {stats.total} từ</span>
            <div className="voc-stats-bar"><ProgressBar value={progressPct} /></div>
            <span className="voc-stats-pct">{progressPct}%</span>
          </div>
        )}

        {/* Filters */}
        <div className="voc-filters">
          <label className="visually-hidden" htmlFor="voc-topic-select">Chủ đề</label>
          <select id="voc-topic-select" className="voc-select" value={topic} onChange={(e) => setTopic(e.target.value)}>
            <option value="">Tất cả chủ đề</option>
            {topics.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
          <div className="voc-search-wrap">
            <label className="visually-hidden" htmlFor="voc-search">Tìm từ vựng</label>
            <input id="voc-search" type="search" className="voc-search" placeholder="🔍 Tìm từ vựng..."
              value={search} onChange={(e) => setSearch(e.target.value)} />
          </div>
        </div>

        {error && (
          <div className="voc-error" role="alert">{error}
            <button className="voc-retry" onClick={fetchWords}>Thử lại</button>
          </div>
        )}

        {/* Word list */}
        {isLoading ? (
          <div className="voc-list">
            {Array.from({ length: 5 }).map((_, i) => <div key={i} className="voc-card-skel" aria-hidden="true" />)}
          </div>
        ) : words.length === 0 ? (
          <EmptyState title="Không tìm thấy từ vựng" subtitle="Thử thay đổi bộ lọc hoặc từ khóa." mascotVariant="thinking" mascotSize={120} />
        ) : (
          <div className="voc-list">
            {words.map((w) => (
              <VocabCard
                key={w.vocabId}
                word={w}
                actionState={actionState}
                onComplete={handleComplete}
                onAddFlashcard={handleAddFlashcard}
              />
            ))}
          </div>
        )}

        {!isLoading && totalPages > 1 && (
          <>
            <p className="voc-count-text">Hiển thị {(page - 1) * 20 + 1}–{Math.min(page * 20, totalEl)} / {totalEl} từ</p>
            <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
          </>
        )}
      </main>
    </div>
  );
}
```

### VocabCard component

```jsx
// components/student/VocabCard.jsx
export default function VocabCard({ word, actionState, onComplete, onAddFlashcard }) {
  const audioRef = useRef(null);
  const isAdding    = actionState[`fc_${word.vocabId}`] === 'adding';
  const isAdded     = word.isInFlashcard || actionState[`fc_${word.vocabId}`] === 'added';
  const isCompleting= actionState[word.vocabId] === 'completing';

  return (
    <div className={`voc-card${word.isCompleted ? ' voc-card--done' : ''}`}>
      <div className="voc-card-main">
        <div className="voc-card-word">
          <span className="voc-word" lang="ja">{word.word}</span>
          <span className="voc-reading" lang="ja">{word.reading}</span>
          {word.audioUrl && (
            <>
              <button className="voc-btn-audio" onClick={() => audioRef.current?.play()} aria-label={`Nghe phát âm ${word.word}`}>▶</button>
              <audio ref={audioRef} src={word.audioUrl} preload="none" />
            </>
          )}
        </div>
        <div className="voc-card-info">
          <span className="voc-pos">{word.partOfSpeech}</span>
          <span className="voc-meaning">{word.meaning}</span>
        </div>
        {word.exampleSentence && (
          <div className="voc-example">
            <span lang="ja">{word.exampleSentence}</span>
            {word.exampleTranslation && <span className="voc-example-trans"> ({word.exampleTranslation})</span>}
          </div>
        )}
      </div>
      <div className="voc-card-actions">
        <button
          className={`voc-btn-fc${isAdded ? ' voc-btn-fc--added' : ''}`}
          onClick={() => !isAdded && !isAdding && onAddFlashcard(word.vocabId)}
          disabled={isAdded || isAdding}
          aria-label={isAdded ? 'Đã thêm Flashcard' : 'Thêm vào Flashcard'}
        >
          {isAdding ? '...' : isAdded ? '✓ FC' : '+ FC'}
        </button>
        <button
          className={`voc-btn-done${word.isCompleted ? ' voc-btn-done--active' : ''}`}
          onClick={() => !word.isCompleted && !isCompleting && onComplete(word.vocabId)}
          disabled={word.isCompleted || isCompleting}
          aria-label={word.isCompleted ? 'Đã học' : 'Đánh dấu đã học'}
        >
          {word.isCompleted ? '✓' : isCompleting ? '...' : '✓'}
        </button>
      </div>
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Vocabulary List ===== */
.voc-page  { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.voc-body  { flex: 1; max-width: 900px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.voc-header  { display: flex; flex-direction: column; gap: 4px; }
.voc-title   { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.voc-subtitle{ font-size: 14px; color: var(--color-text-sub); margin: 0; }

/* Level tabs */
.voc-level-tabs  { display: flex; gap: 0; border-bottom: 2px solid var(--color-border); }
.voc-level-tab   { padding: 10px 20px; font-size: 15px; font-weight: 700; color: var(--color-text-sub); background: transparent; border: none; border-bottom: 2px solid transparent; margin-bottom: -2px; cursor: pointer; transition: color var(--transition), border-color var(--transition); }
.voc-level-tab--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

/* Stats */
.voc-stats     { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.voc-stats-text{ font-size: 14px; color: var(--color-text-sub); }
.voc-stats-bar { flex: 1; min-width: 120px; max-width: 280px; }
.voc-stats-pct { font-size: 14px; font-weight: 700; color: var(--color-text); }

/* Filters */
.voc-filters    { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.voc-select     { height: 40px; padding: 0 12px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-bg); min-width: 160px; }
.voc-search-wrap{ flex: 1; min-width: 220px; }
.voc-search     { width: 100%; height: 40px; padding: 0 14px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-bg); box-sizing: border-box; }

.voc-error { display: flex; align-items: center; justify-content: space-between; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.voc-retry { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* Word list */
.voc-list { display: flex; flex-direction: column; gap: 10px; }

/* VocabCard */
.voc-card { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: var(--radius-lg); padding: 16px 18px; transition: border-color var(--transition), box-shadow var(--transition); }
.voc-card:hover { border-color: var(--color-primary-light); box-shadow: var(--shadow-sm); }
.voc-card--done { border-color: var(--color-secondary); background: var(--color-secondary-bg); }

.voc-card-main  { flex: 1; display: flex; flex-direction: column; gap: 6px; }
.voc-card-word  { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.voc-word       { font-size: 20px; font-weight: 700; color: var(--color-text); }
.voc-reading    { font-size: 14px; color: var(--color-text-sub); }
.voc-btn-audio  { background: transparent; border: 1.5px solid var(--color-primary-light); border-radius: var(--radius-full); color: var(--color-primary); width: 28px; height: 28px; font-size: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; }

.voc-card-info  { display: flex; align-items: center; gap: 8px; }
.voc-pos        { font-size: 11px; font-weight: 700; color: var(--color-text-sub); background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-sm); padding: 2px 8px; }
.voc-meaning    { font-size: 15px; color: var(--color-text); font-weight: 500; }

.voc-example      { font-size: 13px; color: var(--color-text-sub); line-height: 1.5; }
.voc-example-trans{ font-style: italic; }

.voc-card-actions { display: flex; flex-direction: column; gap: 6px; align-items: flex-end; flex-shrink: 0; }
.voc-btn-fc  { height: 28px; padding: 0 12px; background: var(--color-primary-bg); border: 1.5px solid var(--color-primary-light); border-radius: var(--radius-full); color: var(--color-primary); font-size: 12px; font-weight: 700; cursor: pointer; white-space: nowrap; transition: all var(--transition); }
.voc-btn-fc--added { background: var(--color-secondary-bg); border-color: var(--color-secondary); color: var(--color-secondary); cursor: default; }
.voc-btn-fc:disabled { opacity: 0.6; }

.voc-btn-done { width: 28px; height: 28px; border-radius: 50%; border: 2px solid var(--color-border); background: transparent; font-size: 14px; cursor: pointer; color: var(--color-text-sub); transition: all var(--transition); }
.voc-btn-done--active { background: var(--color-secondary); border-color: var(--color-secondary); color: white; cursor: default; }
.voc-btn-done:disabled { opacity: 0.6; }

.voc-card-skel { height: 100px; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }

.voc-count-text { font-size: 13px; color: var(--color-text-sub); text-align: center; }

@media (max-width: 767px) {
  .voc-body    { padding: 16px 16px 32px; }
  .voc-card    { flex-direction: column; }
  .voc-card-actions { flex-direction: row; align-items: center; }
}
@media (prefers-reduced-motion: reduce) { .voc-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 8. ACCESSIBILITY

- [ ] Level tabs dùng `role="tablist"` + `role="tab"` + `aria-selected`
- [ ] `<select>` và `<input>` có `<label>` ẩn bằng `visually-hidden`
- [ ] Audio không autoplay, luôn cần user click ▶
- [ ] Nút "+FC" có `aria-label` đầy đủ khi đã thêm / chưa thêm
- [ ] Skeleton rows có `aria-hidden="true"`
