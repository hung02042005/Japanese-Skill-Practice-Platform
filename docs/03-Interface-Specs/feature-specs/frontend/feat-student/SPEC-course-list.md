# SPEC — Danh sách Khoá học (Course List — chọn cấp độ JLPT)
>
> **Feature ID:** `feat-student` | **Page:** `CourseList`
> **Route:** `/courses` — private, role STUDENT, redirect `/login` nếu chưa auth
> **UC Coverage:** UC-09 (Học theo Level), UC-08 (Chọn khoá học theo cấp độ JLPT)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-20
> **Design ref:** `feat-student/SPEC-vocab-home.md` — **SakuJi · Hanami** theme (sakura-pink). Dùng chung token & TopNav.
> **Backend ref:** `.sdd/specs/backend/feat-core-learning/SPEC.md` (Course theo `jlpt_level` N5→N1, `is_vip_only`)
>
> **Bối cảnh:** Trang này là đích đến của nút **"Course List"** ở right sidebar trang Từ vựng (`SPEC-vocab-home.md FR-VH-09 / §8.2 CourseListCard → navigate('/courses')`). Khi Student bấm "Course List" sẽ mở trang này, hiển thị **các khoá học theo cấp độ JLPT (N5, N4, N3, N2, N1)** dưới dạng lưới thẻ. Chọn một cấp → quay lại trang Từ vựng đã đổi sang cấp đó.
> **Quan hệ spec:** Bổ sung mục **"Course list page (/courses)"** vốn nằm trong OUT OF SCOPE của `SPEC-vocab-home.md §17`.

---

## ACTOR

| Actor | Role | Điều kiện tiền quyết |
|:---|:---|:---|
| **Student** | Học viên đã đăng nhập | `isAuthenticated = true`, `role = STUDENT`, token hợp lệ |

---

## FUNCTIONAL REQUIREMENTS (EARS)

| ID | EARS Requirement |
|:---|:---|
| FR-CL-01 | WHEN an authenticated Student navigates to `/courses`, THE SYSTEM SHALL dispatch `fetchCoursesThunk` để `GET /api/students/courses`. |
| FR-CL-02 | WHILE `coursesStatus === 'loading' \| 'idle'`, THE SYSTEM SHALL render skeleton lưới gồm ≥ 4 CourseCard placeholder. |
| FR-CL-03 | WHEN `fetchCoursesThunk` fulfilled, THE SYSTEM SHALL render TopNav (SakuJi, `activeTab="vocabulary"`), tiêu đề "Khoá học theo cấp độ", và một **CourseGrid** gồm các **CourseCard** theo thứ tự cấp độ **N5 → N4 → N3 → N2 → N1**. |
| FR-CL-04 | THE SYSTEM SHALL render mỗi CourseCard với: nhãn cấp (vd "N5"), tên khoá (`title`), mô tả ngắn (`description`), thanh tiến độ (`completedLessons/totalLessons`), và badge VIP nếu `vipOnly === true`. |
| FR-CL-05 | WHERE một khoá có `vipOnly === true` AND `subscription !== 'VIP'`, THE SYSTEM SHALL render CourseCard ở trạng thái `locked` (dim + lock icon) và KHÔNG cho mở. |
| FR-CL-06 | WHEN a Student clicks một CourseCard không bị khoá, THE SYSTEM SHALL navigate to `/vocabulary?level={jlptLevel}` (quay lại trang Từ vựng theo cấp đã chọn). |
| FR-CL-07 | WHEN a Student clicks một CourseCard `locked`, THE SYSTEM SHALL NOT navigate, SHALL set `aria-disabled="true"`, và SHALL gợi ý nâng cấp (link tới `/subscription`). |
| FR-CL-08 | THE SYSTEM SHALL đánh dấu cấp hiện tại của Student (`currentLevel`) bằng chip "Đang học" trên CourseCard tương ứng. |
| FR-CL-09 | WHEN a Student clicks nút "Quay lại" / breadcrumb "Từ vựng", THE SYSTEM SHALL navigate to `/vocabulary`. |
| FR-CL-10 | IF `courses` array rỗng, THE SYSTEM SHALL render `<EmptyState>` với title "Chưa có khoá học nào". |
| FR-CL-11 | WHEN `fetchCoursesThunk` rejected, THE SYSTEM SHALL hiển thị inline error state với nút "Thử lại". |
| FR-CL-12 | WHEN a Student hovers một CourseCard không bị khoá, THE SYSTEM SHALL phóng to nhẹ (scale ~1.02) + tăng shadow. |

---

## NON-FUNCTIONAL REQUIREMENTS

| ID | Category | Requirement |
|:---|:---|:---|
| NFR-CL-01 | Architecture | Data chỉ đi qua `studentSlice` (Redux thunk) — KHÔNG gọi API trực tiếp trong component (`AGENTS.md §2.4`). |
| NFR-CL-02 | Security | Trạng thái khoá VIP chỉ là UX; backend PHẢI enforce 403 khi truy cập khoá chưa mở. Anti-pattern "Authorization by UI hide" (`CLAUDE.md`). |
| NFR-CL-03 | UX | Skeleton xuất hiện < 100ms; không flash blank. |
| NFR-CL-04 | Correctness | Xử lý gracefully `courses=[]`, `subscription` undefined, `totalLessons=0` (không chia 0). |
| NFR-CL-05 | Brand | Dùng đúng SakuJi: logo SakuJi, mascot Saku-chan, font Nunito/Noto Sans JP, token `--color-*` toàn cục. |
| NFR-CL-06 | Performance | Lưới render ≤ 6 thẻ; lazy-load ảnh/thumbnail ngoài viewport. |

---

## ACCEPTANCE CRITERIA

| ID | Given | When | Then |
|:---|:---|:---|:---|
| AC-CL-01 | Student bấm "Course List" ở /vocabulary | Navigate | Mở /courses; render lưới CourseCard N5→N1 |
| AC-CL-02 | API resolves | Render | Mỗi thẻ có nhãn cấp, tên, mô tả, progress bar |
| AC-CL-03 | Khoá vipOnly + subscription FREE | Render | Thẻ locked (dim + lock), không mở được |
| AC-CL-04 | Click thẻ N4 (mở khoá) | Click | Navigate /vocabulary?level=N4 |
| AC-CL-05 | Click thẻ locked | Click | Không navigate, aria-disabled=true, gợi ý VIP |
| AC-CL-06 | currentLevel = N5 | Render | Thẻ N5 có chip "Đang học" |
| AC-CL-07 | Click "Quay lại"/breadcrumb | Click | Navigate /vocabulary |
| AC-CL-08 | courses = [] | Resolves | EmptyState render, không trang trắng |
| AC-CL-09 | API rejected | Render | Inline error + nút "Thử lại" gọi lại thunk |
| AC-CL-10 | Hover thẻ mở khoá | Hover | Card scale ~1.02 + shadow tăng |
| AC-CL-11 | width < 768px | Render | Lưới 1 cột; TopNav thu gọn |
| AC-CL-12 | prefers-reduced-motion | Render | Tắt mọi animation |

---

## 1. TỔNG QUAN TRANG

Trang **chọn khoá học theo cấp độ JLPT** của **SakuJi**. Student vào từ nút "Course List" trên trang Từ vựng. Mỗi cấp độ (N5→N1) là một **khoá học** hiển thị dạng thẻ trong lưới; chọn một cấp → đổi ngữ cảnh học sang cấp đó (quay về `/vocabulary?level=…`).

**Cấu trúc tổng thể (dưới TopNav, 1 cột nội dung canh giữa):**

```
[TopNav]      — full-width: logo SakuJi + tabs + user (dùng chung toàn app)
[Header]      — breadcrumb "Từ vựng / Khoá học" + tiêu đề "Khoá học theo cấp độ"
[CourseGrid]  — lưới thẻ cấp độ: N5, N4, N3, N2, N1
[Background]  — washi canvas var(--color-bg) #FAF7F4
```

**File structure:**

```
apps/frontend/src/
├── components/layout/
│   └── TopNav.jsx              ← dùng chung (đã có), activeTab="vocabulary"
└── pages/courses/
    ├── CourseList.jsx          ← page root, route /courses
    ├── CourseList.css
    ├── CourseGrid.jsx          ← lưới responsive các CourseCard
    └── CourseCard.jsx          ← 1 khoá theo cấp (available/locked)
```

> **Tái dùng:** Nếu đã có component thẻ khoá ở dashboard (`SPEC-dashboard.md`), ưu tiên tái dùng/biến thể thay vì tạo mới.

---

## 2. DESIGN TOKENS

> Dùng **token toàn cục SakuJi Hanami** (định nghĩa đầy đủ tại `SPEC-dashboard.md §2`, trích trong `SPEC-vocab-home.md §2`). Những token màn này dùng nhiều:

```css
--color-bg:            #FAF7F4;   /* washi canvas */
--color-card:          #FFFFFF;
--color-border:        #E8E0DC;

--color-primary:       #E8637A;   /* accent, progress fill, chip "Đang học" */
--color-primary-light: #F4A7B3;
--color-primary-dark:  #C44E62;
--color-primary-bg:    #FFF0F3;

--color-secondary:     #4CAF50;   /* progress hoàn thành cao / CTA mở khoá */
--color-secondary-bg:  #F1F8E9;

--color-accent:        #F5C842;   /* badge VIP */
--color-accent-bg:     #FFFDE7;

--color-text:          #2D2D2D;
--color-text-sub:      #757575;
--color-text-disabled: #BDBDBD;

--radius-md: 12px; --radius-lg: 16px; --radius-xl: 24px; --radius-full: 9999px;

--shadow-sm: 0 2px 8px rgba(0,0,0,0.07);
--shadow-md: 0 4px 12px rgba(0,0,0,0.10);
--shadow-petal-glow: 0 2px 10px rgba(232,99,122,0.22);

--font-base: 'Nunito', 'Noto Sans JP', system-ui, sans-serif;
--transition: 200ms ease;
```

> **Màu nhãn cấp (level badge):** mỗi cấp một sắc thái nhẹ để dễ phân biệt, vẫn trong palette sakura/Hanami:
>
> - N5 → `--color-secondary` (green, nhập môn)
> - N4 → `#42A5F5` (xanh dương nhạt)
> - N3 → `--color-primary` (sakura)
> - N2 → `#AB47BC` (tím nhạt)
> - N1 → `--color-primary-dark` (đỏ sậm, cao cấp)
> Nhãn render trên nền sáng tương ứng (alpha ~12%), chữ đậm. Không bắt buộc — có thể đồng nhất sakura nếu muốn tối giản.

---

## 3. LAYOUT TỔNG THỂ

### 3.1 Page Shell

```
.cl-page
  min-height: 100vh
  display: flex, flex-direction: column
  background: var(--color-bg)
  font-family: var(--font-base)
```

### 3.2 Content container

```
.cl-content
  max-width: 1040px
  margin: 0 auto
  padding: 24px 24px 48px
  width: 100%
```

### 3.3 CourseGrid (desktop ≥ 1024px)

```
.cl-grid
  display: grid
  grid-template-columns: repeat(3, 1fr)   /* 3 cột */
  gap: 20px
```

### 3.4 Sơ đồ bố cục

```
┌──────────────────────────────────────────────────────────────────────────┐
│  TopNav (sticky 64px) — [🌸 SakuJi]  Ôn tập  Học mới  Kanji  …  [avatar◯]  │
└──────────────────────────────────────────────────────────────────────────┘
   Từ vựng  ›  Khoá học                                  ← breadcrumb (FR-CL-09)
   Khoá học theo cấp độ                                  ← h1 tiêu đề

   ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
   │ [N5]   green  │  │ [N4]   blue   │  │ [N3]  sakura  │
   │ Tiếng Nhật N5 │  │ Tiếng Nhật N4 │  │ Tiếng Nhật N3 │
   │ Hiragana,…    │  │ Giao tiếp…    │  │ Trung cấp…    │
   │ ▓▓▓▓░░ 5/40   │  │ ░░░░░░ 0/52   │  │ ░░░░░░ 0/60   │
   │ • Đang học    │  │               │  │  🔒 VIP       │
   └───────────────┘  └───────────────┘  └───────────────┘
   ┌───────────────┐  ┌───────────────┐
   │ [N2]   purple │  │ [N1]   đỏ sậm │
   │ Tiếng Nhật N2 │  │ Tiếng Nhật N1 │
   │ Cao cấp…      │  │ Bản ngữ…      │
   │ ░░░░░░ 0/70   │  │ ░░░░░░ 0/80   │
   │  🔒 VIP       │  │  🔒 VIP       │
   └───────────────┘  └───────────────┘
```

---

## 4. TOPNAV (dùng chung)

Tái dùng `components/layout/TopNav.jsx` với `activeTab="vocabulary"`. Chi tiết logo/tabs/user xem `SPEC-dashboard.md §4`.

---

## 5. HEADER

### 5.1 Breadcrumb — `.cl-breadcrumb`

```
Font: Nunito 600, 13px, color var(--color-text-sub)
"Từ vựng" là <button>/<Link> → navigate('/vocabulary')  (FR-CL-09)
Dạng: [Từ vựng] › Khoá học   (mục cuối không bấm được, color var(--color-text))
```

### 5.2 Tiêu đề — `.cl-title`

```
<h1> Font: Nunito 800, 26px, color var(--color-text)
Text: "Khoá học theo cấp độ"
Sub (tuỳ chọn) .cl-subtitle: Nunito 600, 14px, color sub — "Chọn cấp JLPT bạn muốn học"
Margin: 4px 0 20px
```

---

## 6. COURSEGRID + COURSECARD

### 6.1 CourseGrid — `.cl-grid`

Lưới responsive (xem §3.3 + §10). Render lần lượt theo thứ tự cấp N5→N1 (BE đã sort, FE giữ nguyên thứ tự `courses`).

### 6.2 CourseCard — `.cl-card`

2 trạng thái chính: **available** (mở) và **locked** (khoá VIP). Cấp đang học có chip "Đang học".

**Layout:**

```
┌─────────────────────────────────────────┐
│  [N5]                          [VIP?]    │ ← level badge + badge VIP (góc phải)
│  Tiếng Nhật N5                            │ ← title
│  Hiragana, Katakana, từ vựng cơ bản      │ ← description (2 dòng, ellipsis)
│  ▓▓▓▓▓░░░░░  5/40 bài                     │ ← progress bar + nhãn
│  • Đang học                    [→ / 🔒]  │ ← chip current + trailing
└─────────────────────────────────────────┘
```

**Base styles:**

```
Display: flex, flex-direction: column, gap: 10px
Background: var(--color-card)
Border-radius: var(--radius-lg)
Padding: 20px
Box-shadow: var(--shadow-sm)
Border: 1.5px solid transparent
Cursor: pointer
Transition: transform var(--transition), box-shadow var(--transition), border-color var(--transition)
Min-height: 180px
```

**Hover (available) — FR-CL-12:**

```
Transform: scale(1.02)
Box-shadow: var(--shadow-md)
Border-color: var(--color-primary-light)
```

**State `locked` (FR-CL-05):**

```
Opacity: 0.7, cursor: not-allowed
Trailing icon → lock, color var(--color-text-disabled)
aria-disabled="true"
Hiển thị dòng nhỏ "Cần VIP để mở" + link "Nâng cấp" → navigate('/subscription')
KHÔNG áp hiệu ứng hover scale
```

#### 6.2.1 Level badge — `.cl-card-level`

```
Chip bo tròn var(--radius-full), padding 2px 10px
Font: Nunito 800, 13px
Màu theo §2 (mỗi cấp một sắc thái; nền alpha ~12%, chữ đậm màu cấp)
```

#### 6.2.2 Title / Description

```
[Title] .cl-card-title: Nunito 800, 17px, color var(--color-text), ellipsis 1 dòng
[Desc]  .cl-card-desc:  Nunito 600, 13px, color var(--color-text-sub),
        line-clamp 2 dòng
```

#### 6.2.3 Progress — `.cl-card-progress`

```
Track:  height 8px, background var(--color-border), border-radius var(--radius-full)
Fill:   width = completedLessons/totalLessons*100% (0% nếu totalLessons=0 — tránh chia 0)
        background var(--color-primary) (hoặc var(--color-secondary) khi ≥ 80%)
Label:  Nunito 600, 12px, color sub — "{completed}/{total} bài"
```

#### 6.2.4 Footer — chip "Đang học" + trailing

```
[Chip "Đang học"] .cl-card-current: chỉ render WHERE jlptLevel === currentLevel (FR-CL-08)
   nền var(--color-primary-bg), text var(--color-primary-dark), Nunito 700 12px, dot ●
[Trailing] available → arrow 20px (hover translateX +3px, color var(--color-primary))
           locked    → lock 18px, color var(--color-text-disabled)
```

#### 6.2.5 Badge VIP — `.cl-card-vip`

```
Chỉ render WHERE vipOnly === true
Chip nền var(--color-accent-bg), border 1px var(--color-accent), text "VIP",
Nunito 800 11px, color #8A6D00, góc phải-trên
```

---

## 7. TRẠNG THÁI LOADING (Skeleton)

```
CourseGrid: render 5 .cl-skel--card (180px height, radius var(--radius-lg)) trong cùng lưới
Tiêu đề:    skeleton 240×30px (tuỳ chọn)
```

Dùng lại lớp shimmer `.vh-skel` của `SPEC-vocab-home.md §9` (đổi tiền tố `cl-skel`) để đồng nhất animation.

---

## 8. TRẠNG THÁI EMPTY (FR-CL-10)

```
Khi courses = []:
  Render <EmptyState />:
    Mascot: Saku-chan "thinking", size 160px
    Title:  "Chưa có khoá học nào"
    Desc:   "Khoá học đang được cập nhật. Hãy quay lại sau nhé! 🌸"
    CTA:    "Về trang Từ vựng" → navigate('/vocabulary')
  Không bao giờ render trang trắng.
```

---

## 9. TRẠNG THÁI ERROR (FR-CL-11)

```
Khi coursesStatus === 'failed':
  .cl-error role="alert":
    Text: coursesError || "Không thể tải danh sách khoá học."
    Button "Thử lại" → dispatch(fetchCoursesThunk())
```

---

## 10. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 1024px | Lưới 3 cột |
| 768–1023px | Lưới 2 cột |
| < 768px | Lưới 1 cột; padding 16px; TopNav thu gọn |

```css
@media (max-width: 1023px) { .cl-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px)  { .cl-grid { grid-template-columns: 1fr; } .cl-content { padding: 16px; } }
```

---

## 11. ANIMATIONS

```css
@keyframes clSlideUp { from { opacity:0; transform: translateY(12px); } to { opacity:1; transform: translateY(0); } }
.cl-card { animation: clSlideUp 250ms ease forwards; }
.cl-card:nth-child(1){ animation-delay:0ms; }
.cl-card:nth-child(2){ animation-delay:50ms; }
.cl-card:nth-child(3){ animation-delay:100ms; }
.cl-card:nth-child(4){ animation-delay:150ms; }
.cl-card:nth-child(5){ animation-delay:200ms; }

@media (prefers-reduced-motion: reduce) {
  .cl-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 12. ROUTE & COMPONENT

```jsx
// App.jsx — thay route placeholder /courses (nếu có) bằng CourseList
import CourseList from './pages/courses/CourseList';
<Route path="/courses"
       element={<PrivateRoute role="STUDENT"><CourseList /></PrivateRoute>} />
```

```jsx
// pages/courses/CourseList.jsx
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchCoursesThunk } from '../../store/slices/studentSlice';
import TopNav from '../../components/layout/TopNav';
import CourseGrid from './CourseGrid';
import { EmptyState } from '../../components/common/EmptyState';
import './CourseList.css';

export default function CourseList() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const { courses, currentLevel, subscription, coursesStatus, coursesError } =
    useAppSelector((s) => s.student);

  const isLoading   = coursesStatus === 'loading' || coursesStatus === 'idle';
  const isFailed    = coursesStatus === 'failed';
  const hasCourses  = Array.isArray(courses) && courses.length > 0;
  const isVip       = subscription === 'VIP';

  useEffect(() => { dispatch(fetchCoursesThunk()); }, [dispatch]);

  return (
    <div className="cl-page">
      <TopNav activeTab="vocabulary" />

      <div className="cl-content">
        <nav className="cl-breadcrumb" aria-label="Breadcrumb">
          <button type="button" onClick={() => navigate('/vocabulary')}>Từ vựng</button>
          <span aria-hidden="true"> › </span>
          <span aria-current="page">Khoá học</span>
        </nav>

        <h1 className="cl-title">Khoá học theo cấp độ</h1>
        <p className="cl-subtitle">Chọn cấp JLPT bạn muốn học</p>

        {isLoading ? (
          <div className="cl-grid" aria-busy="true">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="cl-skel cl-skel--card" aria-hidden="true" />
            ))}
          </div>
        ) : isFailed ? (
          <div className="cl-error" role="alert">
            <p>{coursesError || 'Không thể tải danh sách khoá học.'}</p>
            <button type="button" onClick={() => dispatch(fetchCoursesThunk())}>Thử lại</button>
          </div>
        ) : hasCourses ? (
          <CourseGrid
            courses={courses}
            currentLevel={currentLevel}
            isVip={isVip}
            onOpen={(level) => navigate(`/vocabulary?level=${encodeURIComponent(level)}`)}
            onUpgrade={() => navigate('/subscription')}
          />
        ) : (
          <EmptyState
            mascotVariant="thinking"
            mascotSize={160}
            title="Chưa có khoá học nào"
            subtitle="Khoá học đang được cập nhật. Hãy quay lại sau nhé! 🌸"
          >
            <button type="button" className="cl-empty-cta" onClick={() => navigate('/vocabulary')}>
              Về trang Từ vựng
            </button>
          </EmptyState>
        )}
      </div>
    </div>
  );
}
```

```jsx
// pages/courses/CourseCard.jsx (rút gọn)
export default function CourseCard({ course, isCurrent, isVip, onOpen, onUpgrade }) {
  const { jlptLevel, title, description, completedLessons = 0, totalLessons = 0, vipOnly } = course;
  const locked = vipOnly && !isVip;
  const pct = totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0;

  const handleClick = () => { if (!locked) onOpen(jlptLevel); };

  return (
    <button
      type="button"
      className={`cl-card${locked ? ' cl-card--locked' : ''}`}
      onClick={handleClick}
      aria-disabled={locked || undefined}
      aria-label={`Khoá ${jlptLevel}: ${title}${locked ? ' — cần VIP' : ''}`}
    >
      <span className={`cl-card-level cl-card-level--${jlptLevel.toLowerCase()}`}>{jlptLevel}</span>
      {vipOnly && <span className="cl-card-vip">VIP</span>}
      <span className="cl-card-title">{title}</span>
      <span className="cl-card-desc">{description}</span>
      <span className="cl-card-progress">
        <span className="cl-card-progress-track">
          <span className="cl-card-progress-fill" style={{ width: `${pct}%` }} />
        </span>
        <span className="cl-card-progress-label">{completedLessons}/{totalLessons} bài</span>
      </span>
      <span className="cl-card-footer">
        {isCurrent && <span className="cl-card-current">● Đang học</span>}
        {locked
          ? <span className="cl-card-upgrade" onClick={(e) => { e.stopPropagation(); onUpgrade(); }}>Cần VIP · Nâng cấp</span>
          : <span className="cl-card-arrow" aria-hidden="true">→</span>}
      </span>
    </button>
  );
}
```

---

## 13. API

| Action | Method | Endpoint | Response data |
|:---|:---|:---|:---|
| Tải danh sách khoá | `GET` | `/api/students/courses` | `{ currentLevel, subscription, courses[] }` |

**Response shape (tham khảo):**

```json
{
  "status": 200,
  "data": {
    "currentLevel": "N5",
    "subscription": "FREE",
    "courses": [
      { "jlptLevel": "N5", "title": "Tiếng Nhật N5", "description": "Hiragana, Katakana, từ vựng & ngữ pháp cơ bản", "completedLessons": 5, "totalLessons": 40, "vipOnly": false },
      { "jlptLevel": "N4", "title": "Tiếng Nhật N4", "description": "Giao tiếp hàng ngày, mở rộng từ vựng & Kanji", "completedLessons": 0, "totalLessons": 52, "vipOnly": false },
      { "jlptLevel": "N3", "title": "Tiếng Nhật N3", "description": "Trung cấp: cấu trúc phức hợp, đọc hiểu thực tế", "completedLessons": 0, "totalLessons": 60, "vipOnly": true },
      { "jlptLevel": "N2", "title": "Tiếng Nhật N2", "description": "Cao cấp & học thuật", "completedLessons": 0, "totalLessons": 70, "vipOnly": true },
      { "jlptLevel": "N1", "title": "Tiếng Nhật N1", "description": "Trình độ bản ngữ", "completedLessons": 0, "totalLessons": 80, "vipOnly": true }
    ]
  }
}
```

> **Fallback dev:** giống `fetchVocabHomeThunk`/`fetchDashboardThunk`, `fetchCoursesThunk` nên có DEMO fallback (suy ra từ `learningPath` cấp độ sẵn có trong `studentSlice`) để trang chạy được khi backend chưa sẵn sàng.
> **`courses` thứ tự:** BE trả N5→N1; FE giữ nguyên thứ tự.
> **`vipOnly`:** nếu `true` và `subscription !== 'VIP'` → render `locked` + dẫn `/subscription`.

---

## 14. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Landmark roles | `<header>` (TopNav), `<nav>` (breadcrumb), `<main>`/`.cl-content` cho nội dung |
| Heading hierarchy | `<h1>` = "Khoá học theo cấp độ" |
| Course cards | `<button>` bao toàn thẻ; `aria-label="Khoá N4: Tiếng Nhật N4"` |
| Current level | chip "Đang học" + `aria-current` phù hợp |
| Locked state | `aria-disabled="true"` + nhãn "cần VIP"; link nâng cấp focus được |
| Progress | `role="progressbar"` với `aria-valuenow/min/max` trên track |
| Loading | `aria-busy="true"` trên lưới; skeleton `aria-hidden="true"` |
| Focus ring | `outline: 2px solid var(--color-primary)` trên interactive |
| Reduced motion | Tắt clSlideUp + hover-scale |
| Contrast | Text ≥ 4.5:1; nhãn cấp đủ tương phản trên nền alpha |

---

## 15. STYLE GUARDRAILS

Theo phong cách SakuJi Hanami (cute, friendly), TUYỆT ĐỐI tránh:

- ❌ Harsh shadows → chỉ soft shadow `rgba(0,0,0,0.07–0.10)`
- ❌ Dark UI → nền washi sáng `#FAF7F4`
- ❌ Sharp edges → mọi góc bo ≥ 12px
- ❌ Realistic human faces → chỉ mascot Saku-chan tròn
- ❌ Brand/icon ngoài SakuJi

---

## 16. OUT OF SCOPE

- ❌ Màn chi tiết bài học trong từng khoá (`SPEC-vocab-home.md` đảm nhiệm khi vào `/vocabulary?level=…`)
- ❌ Flashcard session (`SPEC-flashcard-session.md`)
- ❌ Subscription/payment flow (`/subscription`)
- ❌ Quản lý/CRUD khoá học (Staff/Admin)
- ❌ Dark mode

```
