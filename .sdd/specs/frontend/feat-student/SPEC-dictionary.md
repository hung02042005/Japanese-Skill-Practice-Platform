# SPEC — Từ Điển (`/dictionary`)
>
> **Sprint:** 2 — Core Learning Loop
> **Prefix:** `dct-` | **activeTab:** `'dictionary'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §10` · `DESIGN.md` (Hanami Theme) | **Backend ref:** `feat-dictionary-bookmark/SPEC.md` (UC-16) · `feat-flashcard-srs` (review-deck/add)
> **Liên quan:** `SPEC-notebook.md` (đích "Lưu vào sổ tay") · `SPEC-vocabulary.md`

---

## 1. MÔ TẢ TRANG

**Từ Điển** là công cụ **tra cứu** tích hợp trên kho nội dung đã xuất bản. Trọng tâm là **kho từ vựng (`vocabulary`)**, kèm tra cứu phụ Kanji, ngữ pháp, bài học. Học viên gõ từ khóa (chữ Nhật / Romaji / tiếng Việt) → nhận kết quả **gom nhóm theo loại**; mở một mục → xem chi tiết (furigana, nghĩa, âm Onyomi/Kunyomi, ví dụ song ngữ, audio).

**Vai trò trong hệ thống (xem `SPEC-notebook.md §1.1`):**

- **Từ điển = tra cứu**, không lưu trữ.
- Từ kết quả/chi tiết, học viên bấm **"Lưu vào sổ tay"** để đưa từ vựng vào Sổ Tay "Từ cần ôn lại" (`/notebook`) — **thay cho** tab bookmark nhúng trong trang cũ.

> **Refactor so với code hiện tại** ([Dictionary.jsx](apps/frontend/src/pages/dictionary/Dictionary.jsx)): (1) bỏ `MOCK_*`, gọi API thật; (2) bỏ tab "Bookmarks" + `INIT_BOOKMARKS` nhúng trong trang — thay bằng nút "📓 Sổ tay" điều hướng `/notebook` và nút "Lưu vào sổ tay" trên từng mục vocab.

Theo `DESIGN.md`: nền **washi**, thẻ trắng, badge JLPT đúng cặp màu, `EmptyState` Saku-chan khi chưa tìm / không có kết quả. Pink chỉ làm accent (tab active, icon "đã lưu"); green `--color-secondary` cho CTA chính.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="dictionary")                                    │
├──────────────────────────────────────────────────────────────────────┤
│  Từ Điển                                            [📓 Sổ tay]       │
│  Tra cứu từ vựng, Kanji, ngữ pháp                                   │
│                                                                      │
│  [🔍  Nhập từ vựng, Kanji, ngữ pháp… (JP / Romaji / tiếng Việt)  ✕] │
│                                                                      │
│  ─── chưa gõ ───────────────────────────────────────────────────────  │
│            [SakuChan thinking 140px]                                 │
│            Gõ để tra cứu trong kho từ vựng SakuJi                   │
│                                                                      │
│  ─── có kết quả ────────────────────────────────────────────────────  │
│  Từ vựng (3)                                                         │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │ [N5] 食べる ・たべる   Ăn                  [♡ Lưu vào sổ tay]  │ │
│  └────────────────────────────────────────────────────────────────┘ │
│  Kanji (1)                                                           │
│  ┌──────────────┐                                                   │
│  │ [N5] 食       │  Thực (ăn) · ショク / た.べる        →           │
│  └──────────────┘                                                   │
│  Ngữ pháp (1) · Bài học (0)                                          │
│                                                                      │
│  ─── chi tiết (mở 1 mục) ──────────────────────────────────────────  │
│  ← Kết quả                                                           │
│  [N5] 食べる ・たべる                          [♥ Đã lưu]           │
│  Ăn   · Động từ nhóm 2                                              │
│  Ví dụ: 朝ごはんを食べる。 / Ăn bữa sáng.       [▶ Phát âm]          │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO / SỬA

```
pages/dictionary/
├── Dictionary.jsx     ← refactor: bỏ mock + tab bookmark, dùng API thật
└── Dictionary.css     ← giữ phần lớn class dct-*; bỏ class tab bookmark

components/student/
├── DictResultGroup.jsx   ← 1 nhóm kết quả (vocab/kanji/grammar/lesson)
└── DictDetailPanel.jsx   ← chi tiết 1 mục + nút Lưu/Phát âm
```

---

## 4. STATE

```js
const [query,     setQuery]     = useState('');
const [debounced, setDebounced] = useState('');   // debounce 350ms
const [results,   setResults]   = useState(null);  // null = chưa tìm; {vocabulary,kanji,grammar,lessons}
const [isLoading, setLoading]   = useState(false);
const [selected,  setSelected]  = useState(null);  // { type, id } đang mở chi tiết
const [detail,    setDetail]    = useState(null);  // dữ liệu chi tiết
const [isDetailLoading, setDL]  = useState(false);
const [savedIds,  setSavedIds]  = useState(new Set()); // 'vocabulary:12' đã lưu vào sổ tay
const [savingId,  setSavingId]  = useState(null);
const [error,     setError]     = useState('');

const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// 1. Tìm kiếm gom nhóm (UC-16) — backend lọc status='published' (FR-DICT-05)
// GET /api/dictionary/search?q={q}&page=0&size=20
// Response: { data: { vocabulary:[{vocabId,word,furigana,meaning,jlptLevel}],
//                     kanji:[{kanjiId,characterValue,meaning,jlptLevel}],
//                     grammar:[{grammarId,structure,meaning,jlptLevel}],
//                     lessons:[{lessonId,title,lessonType,jlptLevel}] } }
export async function searchDictionary(q, page = 0, size = 20) {
  const res = await api.get('/dictionary/search', { params: { q, page, size } });
  return res.data.data;
}

// 2. Chi tiết 1 mục (FR-DICT-03) — endpoint theo loại đã có:
//    vocab  → getVocabularyList({search}) / GET /api/vocabulary/:id (nếu có)
//    kanji  → getKanjiDetail(kanjiId)
//    grammar→ getGrammarList (item) / detail
// Tối thiểu hiển thị từ payload search; gọi detail khi cần audio/ví dụ.

// 3. Lưu vào Sổ tay "Từ cần ôn lại" — dùng chung endpoint review-deck/add
// POST /api/flashcards/review-deck/add  { items:[{contentType:'VOCABULARY', contentId}] }
export async function saveToNotebook(contentType, contentId) {
  const res = await api.post('/flashcards/review-deck/add', {
    items: [{ contentType: contentType.toUpperCase(), contentId }],
  });
  return res.data.data;
}
```

> **Debounce 350ms** trên `query` → `debounced` → gọi `searchDictionary`. Giữ logic debounce/clear đã có trong code.
> **CHỐT (đồng bộ `SPEC-notebook.md §12`):** "Lưu vào sổ tay" dùng `POST /api/flashcards/review-deck/add` với `contentType='VOCABULARY'`. Kanji/ngữ pháp **không** vào sổ tay ở bản này (xem OUT OF SCOPE) — chỉ điều hướng tới trang chi tiết tương ứng.

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import DictResultGroup from '../../components/student/DictResultGroup';
import DictDetailPanel from '../../components/student/DictDetailPanel';
import { searchDictionary, saveToNotebook } from '../../api/studentService';
import './Dictionary.css';

export default function Dictionary() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [query,     setQuery]     = useState('');
  const [debounced, setDebounced] = useState('');
  const [results,   setResults]   = useState(null);
  const [isLoading, setLoading]   = useState(false);
  const [selected,  setSelected]  = useState(null);
  const [savedIds,  setSavedIds]  = useState(new Set());
  const [savingId,  setSavingId]  = useState(null);
  const [error,     setError]     = useState('');
  const timerRef = useRef(null);

  const onQueryChange = useCallback((val) => {
    setQuery(val);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(val.trim()), 350);
  }, []);

  useEffect(() => {
    if (!debounced) { setResults(null); return; }
    let active = true;
    (async () => {
      setLoading(true); setError('');
      try {
        const data = await searchDictionary(debounced);
        if (active) setResults(data);
      } catch {
        if (active) setError('Không thể tìm kiếm. Thử lại sau.');
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => { active = false; };
  }, [debounced]);

  async function handleSave(type, id) {
    const key = `${type}:${id}`;
    if (savedIds.has(key)) return;
    setSavingId(key);
    try {
      await saveToNotebook(type, id);
      setSavedIds((prev) => new Set(prev).add(key));
      addToast('success', 'Đã lưu vào Sổ tay.');
    } catch (err) {
      if (err?.response?.status === 409) { setSavedIds((p) => new Set(p).add(key)); addToast('info', 'Từ này đã có trong sổ tay.'); return; }
      addToast('error', 'Không thể lưu. Thử lại.');
    } finally {
      setSavingId(null);
    }
  }

  const total = results
    ? (results.vocabulary?.length ?? 0) + (results.kanji?.length ?? 0) + (results.grammar?.length ?? 0) + (results.lessons?.length ?? 0)
    : 0;

  return (
    <div className="dct-page">
      <TopNav activeTab="dictionary" />
      <main className="dct-body">
        {/* Header */}
        <div className="dct-header">
          <div>
            <h1 className="dct-title">Từ Điển</h1>
            <p className="dct-subtitle">Tra cứu từ vựng, Kanji, ngữ pháp</p>
          </div>
          <button className="dct-notebook-btn" onClick={() => navigate('/notebook')}>📓 Sổ tay</button>
        </div>

        {/* Search box */}
        <div className="dct-search-wrap">
          <svg className="dct-si" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/><path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/></svg>
          <input
            className="dct-search-input"
            type="search"
            placeholder="Nhập từ vựng, Kanji, ngữ pháp… (JP / Romaji / tiếng Việt)"
            value={query}
            onChange={(e) => onQueryChange(e.target.value)}
            autoFocus
            aria-label="Tìm kiếm từ điển"
          />
          {query && <button className="dct-clear-btn" onClick={() => { setQuery(''); setDebounced(''); }} aria-label="Xóa tìm kiếm">✕</button>}
        </div>

        {/* Detail mở chồng lên */}
        {selected && (
          <DictDetailPanel
            selection={selected}
            isSaved={savedIds.has(`${selected.type}:${selected.id}`)}
            isSaving={savingId === `${selected.type}:${selected.id}`}
            onSave={() => handleSave(selected.type, selected.id)}
            onBack={() => setSelected(null)}
          />
        )}

        {/* Trạng thái body */}
        {!selected && (
          <>
            {error && <div className="dct-error" role="alert">{error}</div>}

            {!debounced && !error && (
              <EmptyState title="Gõ để tra cứu" subtitle="Tìm trong kho từ vựng, Kanji, ngữ pháp của SakuJi." mascotVariant="thinking" mascotSize={140} />
            )}

            {isLoading && (
              <div className="dct-loading" role="status" aria-label="Đang tìm kiếm"><div className="dct-spinner" /></div>
            )}

            {!isLoading && debounced && results && total === 0 && (
              <EmptyState title={`Không tìm thấy "${debounced}"`} subtitle="Thử từ khóa khác (chữ Nhật, Romaji hoặc tiếng Việt)." mascotVariant="idle" mascotSize={140} />
            )}

            {!isLoading && results && total > 0 && (
              <div className="dct-results">
                <DictResultGroup
                  title="Từ vựng" items={results.vocabulary} type="vocabulary"
                  savedIds={savedIds} savingId={savingId}
                  onOpen={(id) => setSelected({ type: 'vocabulary', id })}
                  onSave={(id) => handleSave('vocabulary', id)}
                  canSave
                />
                <DictResultGroup title="Kanji" items={results.kanji} type="kanji"
                  onOpen={(id) => navigate(`/kanji/${id}`)} />
                <DictResultGroup title="Ngữ pháp" items={results.grammar} type="grammar"
                  onOpen={(id) => navigate(`/grammar/${id}`)} />
                <DictResultGroup title="Bài học" items={results.lessons} type="lesson"
                  onOpen={(id) => navigate(`/lessons/${id}`)} />
              </div>
            )}
          </>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 7. DictResultGroup component

```jsx
// components/student/DictResultGroup.jsx
const FIELD = {
  vocabulary: (i) => ({ id: i.vocabId,   main: i.word,           sub: i.furigana, meaning: i.meaning, level: i.jlptLevel }),
  kanji:      (i) => ({ id: i.kanjiId,    main: i.characterValue, sub: '',         meaning: i.meaning, level: i.jlptLevel }),
  grammar:    (i) => ({ id: i.grammarId,  main: i.structure,      sub: '',         meaning: i.meaning, level: i.jlptLevel }),
  lesson:     (i) => ({ id: i.lessonId,   main: i.title,          sub: '',         meaning: '',        level: i.jlptLevel }),
};

export default function DictResultGroup({ title, items = [], type, savedIds, savingId, onOpen, onSave, canSave }) {
  if (!items.length) return null;
  return (
    <section className="dct-group">
      <h2 className="dct-group-title">{title} <span className="dct-group-count">({items.length})</span></h2>
      <div className="dct-group-list">
        {items.map((raw) => {
          const it = FIELD[type](raw);
          const key = `${type}:${it.id}`;
          const saved = savedIds?.has(key);
          return (
            <div key={key} className="dct-result-row">
              <button className="dct-result-main" onClick={() => onOpen(it.id)} aria-label={`Mở ${it.main}`}>
                {it.level && <span className={`jlpt-badge jlpt-${it.level}`}>{it.level}</span>}
                <span className="dct-result-word" lang="ja">{it.main}</span>
                {it.sub && <span className="dct-result-sub" lang="ja">・{it.sub}</span>}
                {it.meaning && <span className="dct-result-meaning">{it.meaning}</span>}
              </button>
              {canSave && (
                <button
                  className={`dct-save-btn${saved ? ' dct-save-btn--saved' : ''}`}
                  onClick={() => onSave(it.id)}
                  disabled={saved || savingId === key}
                  aria-label={saved ? 'Đã lưu vào sổ tay' : 'Lưu vào sổ tay'}
                  aria-pressed={saved}
                >
                  {savingId === key ? <span className="dct-spinner dct-spinner--sm" /> : (saved ? '♥ Đã lưu' : '♡ Lưu vào sổ tay')}
                </button>
              )}
            </div>
          );
        })}
      </div>
    </section>
  );
}
```

> `DictDetailPanel` tương tự: hiển thị main/sub/meaning + ví dụ song ngữ + nút Phát âm (`new Audio(audioUrl).play()`) + nút Lưu/Đã lưu, nút "← Kết quả".

---

## 8. CSS (rút gọn — giữ phần lớn `dct-*` cũ)

```css
/* ===== Từ Điển (SakuJi Hanami Theme) ===== */
.dct-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.dct-body { flex: 1; max-width: 760px; width: 100%; margin: 0 auto; padding: 28px 24px 48px; display: flex; flex-direction: column; gap: 18px; box-sizing: border-box; }

.dct-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.dct-title { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0; }
.dct-subtitle { font-size: 14px; color: var(--color-text-sub); margin: 4px 0 0; }
.dct-notebook-btn { height: 38px; padding: 0 16px; border-radius: var(--radius-full); border: 1.5px solid var(--color-border); background: var(--color-card); color: var(--color-text-sub); font-size: 13px; font-weight: 700; cursor: pointer; }
.dct-notebook-btn:hover { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }

/* Search */
.dct-search-wrap { display: flex; align-items: center; gap: 10px; height: 52px; padding: 0 16px; background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: var(--radius-full); }
.dct-search-wrap:focus-within { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(232,154,170,0.15); }
.dct-si { color: var(--color-text-sub); flex-shrink: 0; }
.dct-search-input { flex: 1; border: none; outline: none; background: transparent; font-family: var(--font-base); font-size: 16px; color: var(--color-text); }
.dct-clear-btn { width: 28px; height: 28px; border: none; background: var(--color-bg); border-radius: var(--radius-full); color: var(--color-text-sub); cursor: pointer; }

/* Results */
.dct-results { display: flex; flex-direction: column; gap: 20px; }
.dct-group-title { font-size: 14px; font-weight: 700; color: var(--color-text); margin: 0 0 8px; }
.dct-group-count { color: var(--color-text-sub); font-weight: 400; }
.dct-group-list { display: flex; flex-direction: column; gap: 8px; }
.dct-result-row { display: flex; align-items: center; gap: 10px; background: var(--color-card); border-radius: var(--radius-md); box-shadow: var(--shadow-sm); padding: 12px 16px; }
.dct-result-main { flex: 1; min-width: 0; display: flex; align-items: center; gap: 10px; background: transparent; border: none; cursor: pointer; text-align: left; }
.dct-result-word { font-size: 18px; font-weight: 700; color: var(--color-text); }
.dct-result-main:hover .dct-result-word { color: var(--color-primary); }
.dct-result-sub { font-size: 13px; color: var(--color-text-sub); }
.dct-result-meaning { font-size: 14px; color: var(--color-text-sub); margin-left: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Save button */
.dct-save-btn { flex-shrink: 0; height: 34px; padding: 0 14px; border-radius: var(--radius-full); border: 1.5px solid var(--color-secondary); background: transparent; color: var(--color-secondary); font-family: var(--font-base); font-size: 13px; font-weight: 700; cursor: pointer; transition: all var(--transition); }
.dct-save-btn:hover:not(:disabled) { background: var(--color-secondary-bg); }
.dct-save-btn--saved { background: var(--color-secondary); color: white; border-color: var(--color-secondary); cursor: default; }

/* Loading / error */
.dct-loading { display: flex; justify-content: center; padding: 40px; }
.dct-spinner { width: 40px; height: 40px; border: 4px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
.dct-spinner--sm { width: 14px; height: 14px; border-width: 2px; }
.dct-error { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 767px) {
  .dct-body { padding: 16px 16px 32px; }
  .dct-result-meaning { display: none; }
}
@media (prefers-reduced-motion: reduce) { .dct-page * { animation: none !important; transition-duration: 0ms !important; } }
```

> JLPT badge dùng class chung `.jlpt-badge`/`.jlpt-N5…N1` (không hard-code hex).

---

## 9. CÁC TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Chưa gõ** | `EmptyState` "Gõ để tra cứu" + Saku-chan `thinking` |
| **Đang tìm** | Spinner giữa khung |
| **Không kết quả** | `EmptyState` "Không tìm thấy …" + Saku-chan `idle` |
| **Có kết quả** | Các nhóm `DictResultGroup`; nhóm rỗng tự ẩn |
| **Error** | Banner đỏ |
| **Mở chi tiết** | `DictDetailPanel` chồng lên, nút "← Kết quả" |
| **Đã lưu sổ tay** | Nút đổi "♥ Đã lưu", disabled |

---

## 10. DOMAIN RULES

- **Chỉ tra nội dung `status='published'`** — backend bảo đảm (FR-DICT-05); frontend không lọc thêm.
- **Đa ngôn ngữ đầu vào**: chữ Nhật (Kanji/Kana), Romaji, tiếng Việt (FR-DICT-04) — gửi nguyên `q`, backend xử lý.
- **Debounce 350ms**, hủy request cũ (`active` flag) tránh race khi gõ nhanh.
- **"Lưu vào sổ tay"** chỉ cho **từ vựng**; gọi `POST /flashcards/review-deck/add` `{contentType:'VOCABULARY'}`. 409 = đã có → coi như đã lưu (không báo lỗi đỏ).
- **Kanji/Ngữ pháp/Bài học**: chỉ điều hướng tới trang chi tiết tương ứng (`/kanji/:id`, `/grammar/:id`, `/lessons/:id`).
- **Chữ Nhật/furigana** render `lang="ja"` (Noto Sans JP).
- **Không trang trắng**: mọi nhánh rỗng render `EmptyState` + Saku-chan.
- **Accent pink** cho tab/hover; CTA "Lưu" dùng green `--color-secondary`.
- **Reduced motion**: tắt animation.

---

## 11. ENTRY POINTS & ĐIỀU HƯỚNG

| Từ đâu | Tới | Ghi chú |
|:---|:---|:---|
| TopNav tab "Từ điển" | `/dictionary` | activeTab |
| Từ điển → "📓 Sổ tay" | `/notebook` | góc header |
| Kết quả/chi tiết vocab → "Lưu vào sổ tay" | (thêm vào sổ) | `review-deck/add`, không rời trang |
| Kết quả Kanji/Ngữ pháp/Bài học → click | trang chi tiết tương ứng | — |

---

## 12. CẦN BACKEND XÁC NHẬN

1. **Endpoint chi tiết vocab** (audio, ví dụ song ngữ) — dùng `GET /api/vocabulary/:id` hay lấy đủ từ payload search? Nếu search đã đủ trường → bỏ call detail.
2. **Phân trang search** — `GET /api/dictionary/search` trả mỗi nhóm tối đa `size`; cần "Xem thêm" theo nhóm không? (bản này: hiển thị trang đầu, chưa load-more).

---

## OUT OF SCOPE

- ❌ Lưu Kanji/Ngữ pháp/Bài học vào sổ tay — sổ tay bản này chỉ chứa **từ vựng** (`SPEC-notebook.md`). Thêm Kanji vào Flashcard dùng luồng riêng ở trang Kanji.
- ❌ Tra cứu bằng OCR/máy ảnh — xem `feat-ai-skills`.
- ❌ Lịch sử tìm kiếm, gợi ý tự động (autocomplete).
- ❌ Load-more / phân trang trong nhóm kết quả.
