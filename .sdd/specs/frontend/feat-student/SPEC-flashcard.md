<<<<<<< Updated upstream
# SPEC — Flashcard theo Course → Topic (`/flashcard`)
=======
# SPEC — Quản lý Flashcard (`/flashcard`)
>
>>>>>>> Stashed changes
> **Sprint:** 2 — Core Learning Loop
> **Prefix:** `fls-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §10` · `DESIGN.md` (Hanami Theme) | **Backend ref:** `feat-flashcard-srs/SPEC.md`, `feat-core-learning`
> **Liên quan:** `SPEC-flashcard-session.md` (màn học) · `SPEC-vocabulary.md` · `SPEC-notebook.md`

---

## 1. MÔ TẢ TRANG

Điểm vào học **Flashcard** theo cấu trúc **Course → Topic**. Thẻ lấy từ **kho từ vựng** (`vocabulary`), chia theo **chủ đề (topic)** trong từng **Course** (mỗi JLPT level N5→N1 là một Course).

Luồng:
1. Học viên **chọn Course** (N5 / N4 / N3 / N2 / N1).
2. Trang **hiện danh sách Topic** của course đó (lấy từ kho vocab theo level).
3. Chọn một topic → mở **phiên học Flashcard** (`SPEC-flashcard-session.md`) theo `level + topic`.

> **Thay đổi so với bản cũ:** không còn là trình duyệt "bộ thẻ cá nhân (deck)". Deck cá nhân/hệ thống vẫn tồn tại ở backend (vd Sổ tay "Từ cần ôn lại"), nhưng **điểm vào học chính là Course → Topic**. Sổ tay có lối vào riêng (`/notebook`).

Theo `DESIGN.md`: nền washi, course card bo `--radius-lg` + badge JLPT đúng cặp màu, topic card trắng, progress bar tiến độ topic, `EmptyState` Saku-chan khi rỗng. Pink accent; green CTA "Học".

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav                                                          │
├──────────────────────────────────────────────────────────────────┤
│  Flashcard                                                       │
│  Chọn khoá học để xem chủ đề từ vựng                            │
│                                                                  │
│  ─── chọn Course ────────────────────────────────────────────────  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐    │
│  │ [N5]    │ │ [N4]    │ │ [N3]    │ │ [N2]    │ │ [N1]    │    │
│  │ Sơ cấp  │ │ Sơ-trung│ │ Trung   │ │ Trung-ca│ │ Cao cấp │    │
│  │ 12 chủ đề│ │ 18 chủ đề│ │ ...     │ │ ...     │ │ ...     │    │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘    │
│                                                                  │
│  ─── đã chọn N5 → list Topic ────────────────────────────────────  │
│  ← Đổi khoá học        Khoá N5 · 12 chủ đề                       │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Động từ cơ bản          45 từ · 12 đến hạn      [⚡ Học]   │ │
│  │ [██████████░░░░░░░░] 60% đã học                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Gia đình                28 từ · 0 đến hạn       [⚡ Học]   │ │
│  │ [░░░░░░░░░░░░░░░░░░] Chưa học                              │ │
│  └────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO / SỬA

```
pages/flashcard/
├── Flashcard.jsx     ← viết lại: Course picker → Topic list
└── Flashcard.css

components/student/
├── CourseCard.jsx    ← 1 course (level) — tái dùng nếu đã có
└── TopicCard.jsx     ← 1 topic + tiến độ + nút Học
```

---

## 4. STATE

```js
const [course,  setCourse]  = useState(null);     // level đã chọn: 'N5'… | null
const [topics,  setTopics]  = useState([]);        // topic của course
const [isLoading,setLoading] = useState(false);
const [error,   setError]    = useState('');
const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// Course = JLPT level (tĩnh N5..N1). Tất cả khoá đều truy cập được (không khoá VIP).
const COURSES = [
  { level: 'N5', name: 'Sơ cấp' },
  { level: 'N4', name: 'Sơ-trung cấp' },
  { level: 'N3', name: 'Trung cấp' },
  { level: 'N2', name: 'Trung-cao cấp' },
  { level: 'N1', name: 'Cao cấp' },
];

// Topic của 1 course
// GET /api/vocabulary/topics?level=N5
// Hiện trả: string[]  →  ⚠ cần enrich thành object có tiến độ (xem §10)
// Mong muốn: [{ topic, totalWords, learnedWords, dueCount }]
export async function getVocabTopics(level) { /* đã có */ }
```

> **Học topic** → điều hướng phiên: `navigate('/vocabulary/flashcard?level=' + level + '&topic=' + encodeURIComponent(topic))` — khớp route session hiện có (`SPEC-flashcard-session.md`).

---

## 6. JSX STRUCTURE

```jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ProgressBar } from '../../components/common/ProgressBar';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { getVocabTopics } from '../../api/studentService';
import './Flashcard.css';

const COURSES = [
  { level: 'N5', name: 'Sơ cấp' },
  { level: 'N4', name: 'Sơ-trung cấp' },
  { level: 'N3', name: 'Trung cấp' },
  { level: 'N2', name: 'Trung-cao cấp' },
  { level: 'N1', name: 'Cao cấp' },
];

export default function Flashcard() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [course,  setCourse]  = useState(null);
  const [topics,  setTopics]  = useState([]);
  const [isLoading,setLoading] = useState(false);
  const [error,   setError]    = useState('');

  async function selectCourse(c) {
    setCourse(c); setLoading(true); setError('');
    try {
      const data = await getVocabTopics(c.level);
      // chuẩn hoá: string[] | object[]
      setTopics((data ?? []).map((t) => typeof t === 'string' ? { topic: t } : t));
    } catch {
      setError('Không thể tải chủ đề. Thử lại sau.');
    } finally {
      setLoading(false);
    }
  }

  function studyTopic(topic) {
    navigate(`/vocabulary/flashcard?level=${course.level}&topic=${encodeURIComponent(topic)}`);
  }

  return (
    <div className="fls-page">
      <TopNav activeTab="" />
      <main className="fls-body">
        {!course ? (
          /* ── Course picker ── */
          <>
            <div className="fls-header">
              <h1 className="fls-title">Flashcard</h1>
              <p className="fls-subtitle">Chọn khoá học để xem chủ đề từ vựng</p>
            </div>
            <div className="fls-course-grid">
              {COURSES.map((c) => (
                <button key={c.level} className="fls-course-card"
                  onClick={() => selectCourse(c)} aria-label={`Khoá ${c.level} ${c.name}`}>
                  <span className={`jlpt-badge jlpt-${c.level}`}>{c.level}</span>
                  <span className="fls-course-name">{c.name}</span>
                </button>
              ))}
            </div>
          </>
        ) : (
          /* ── Topic list của course ── */
          <>
            <div className="fls-topic-head">
              <button className="fls-back" onClick={() => { setCourse(null); setTopics([]); }}>← Đổi khoá học</button>
              <span className="fls-topic-scope">
                <span className={`jlpt-badge jlpt-${course.level}`}>{course.level}</span>
                Khoá {course.level} · {topics.length} chủ đề
              </span>
            </div>

            {error && <div className="fls-error" role="alert">{error}</div>}

            {isLoading ? (
              <div className="fls-topic-list">{[1,2,3,4].map((i) => <div key={i} className="fls-skel" aria-hidden="true" />)}</div>
            ) : topics.length === 0 ? (
              <EmptyState title="Khoá này chưa có chủ đề" subtitle="Nội dung đang được cập nhật." mascotVariant="idle" mascotSize={140} />
            ) : (
              <div className="fls-topic-list">
                {topics.map((t) => (
                  <div key={t.topic} className="fls-topic-card">
                    <div className="fls-topic-main">
                      <div className="fls-topic-name">{t.topic}</div>
                      <div className="fls-topic-meta">
                        {t.totalWords != null && <>{t.totalWords} từ</>}
                        {t.dueCount > 0 && <span className="fls-due"> · {t.dueCount} đến hạn</span>}
                      </div>
                      {t.totalWords != null && (
                        <ProgressBar value={t.totalWords ? Math.round((t.learnedWords ?? 0) / t.totalWords * 100) : 0} />
                      )}
                    </div>
                    <button className="fls-study-btn" onClick={() => studyTopic(t.topic)} aria-label={`Học chủ đề ${t.topic}`}>
                      ⚡ Học
                    </button>
                  </div>
                ))}
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

## 7. CSS (rút gọn)

```css
/* ===== Flashcard Course→Topic (SakuJi Hanami Theme) ===== */
.fls-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.fls-body { flex: 1; max-width: 860px; width: 100%; margin: 0 auto; padding: 28px 24px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }
.fls-header { }
.fls-title { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0; }
.fls-subtitle { font-size: 14px; color: var(--color-text-sub); margin: 4px 0 0; }

/* Course grid */
.fls-course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 14px; }
.fls-course-card { display: flex; flex-direction: column; align-items: flex-start; gap: 10px; padding: 18px; background: var(--color-card); border: none; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); cursor: pointer; transition: box-shadow var(--transition), transform var(--transition); text-align: left; }
.fls-course-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.fls-course-name { font-size: 16px; font-weight: 700; color: var(--color-text); }

/* Topic list */
.fls-topic-head { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.fls-back { background: transparent; border: none; color: var(--color-text-sub); font-size: 13px; font-weight: 600; cursor: pointer; }
.fls-back:hover { color: var(--color-primary); }
.fls-topic-scope { display: inline-flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 700; color: var(--color-text); }
.fls-topic-list { display: flex; flex-direction: column; gap: 12px; }
.fls-topic-card { display: flex; align-items: center; gap: 14px; background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); padding: 16px 20px; transition: box-shadow var(--transition); }
.fls-topic-card:hover { box-shadow: var(--shadow-md); }
.fls-topic-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.fls-topic-name { font-size: 16px; font-weight: 700; color: var(--color-text); }
.fls-topic-meta { font-size: 12px; color: var(--color-text-sub); }
.fls-due { color: var(--color-primary); font-weight: 700; }
.fls-study-btn { flex-shrink: 0; height: 38px; padding: 0 18px; background: var(--color-secondary); color: white; border: none; border-radius: var(--radius-full); font-family: var(--font-base); font-size: 14px; font-weight: 700; cursor: pointer; transition: filter var(--transition); }
.fls-study-btn:hover { filter: brightness(1.07); }

.fls-error { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.fls-skel { height: 84px; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

@media (max-width: 767px) { .fls-body { padding: 16px 16px 32px; } .fls-course-grid { grid-template-columns: repeat(2, 1fr); } }
@media (prefers-reduced-motion: reduce) { .fls-page * { animation: none !important; transition-duration: 0ms !important; transform: none !important; } }
```

> JLPT badge dùng class chung `.jlpt-badge`/`.jlpt-N5…N1`.

---

## 8. CÁC TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Chưa chọn course** | Lưới 5 course card (N5–N1), tất cả truy cập được |
| **Đang tải topic** | 4 skeleton |
| **Course rỗng topic** | `EmptyState` "Khoá này chưa có chủ đề" |
| **Có topic** | Danh sách topic card + progress + nút "⚡ Học" |
| **Error** | Banner đỏ |

---

## 9. DOMAIN RULES

- **Course = JLPT level**; thẻ lấy từ kho `vocabulary` theo `level + topic`. **Tất cả course truy cập tự do — không khoá VIP.**
- **Học topic** → `/vocabulary/flashcard?level=&topic=` (phiên Quizlet, `SPEC-flashcard-session.md`); thứ tự thẻ + nhịp do backend.
- **Không trang trắng** — mọi nhánh rỗng render `EmptyState` + Saku-chan.
- **Reduced motion** — tắt animation/transform.

---

## 10. CẦN BACKEND XÁC NHẬN

1. **Enrich `GET /api/vocabulary/topics`**: hiện trả `string[]`. Cần trả `[{ topic, totalWords, learnedWords, dueCount }]` để hiển thị tiến độ + "đến hạn" mỗi topic. Nếu giữ `string[]` → FE ẩn progress/meta (đã có fallback chuẩn hoá trong code).
2. **Course thực sự?** Hiện mô hình Course = JLPT level (tĩnh). Nếu sau này có entity Course riêng (nhiều course/level) → thay `COURSES` tĩnh bằng `GET /api/courses`.

---

## ENTRY POINTS

| Từ đâu | Tới |
|:---|:---|
| TopNav / menu → "Flashcard" | `/flashcard` (course picker) |
| Course → Topic → "⚡ Học" | `/vocabulary/flashcard?level=&topic=` (phiên học) |
| (Sổ tay riêng) | `/notebook` |

---

## OUT OF SCOPE
- ❌ Tạo/sửa/xoá bộ thẻ cá nhân (deck CRUD) — điểm vào học là Course→Topic; Sổ tay "Từ cần ôn lại" quản ở `/notebook`.
- ❌ Học Kanji/Ngữ pháp dạng flashcard — bản này chỉ từ vựng.
- ❌ Course entity động (nhiều course/level) — dùng level tĩnh.
