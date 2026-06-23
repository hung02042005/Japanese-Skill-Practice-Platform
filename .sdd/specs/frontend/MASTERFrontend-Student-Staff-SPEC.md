# MASTER-SPEC — Hướng dẫn tạo trang Student & Staff (Frontend)
>
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-02
> **Mục đích:** Đọc tài liệu này là đủ để tạo bất kỳ trang student/staff mới mà không cần hỏi thêm.
> **Admin ref:** `.sdd/specs/frontend/MASTERFrontend-SPEC.md` | **Backend ref:** `.sdd/specs/backend/`

---

## 0. QUY TẮC SỐ 1

> **Tận dụng component có sẵn trước. Tạo mới chỉ khi thật sự không có.**

Kiểm tra theo thứ tự: `components/student/` → `components/common/` → `components/layout/` → `components/auth/`

**Điểm khác biệt cốt lõi so với Admin:**

| Tiêu chí | Student | Staff | Admin |
|:---|:---|:---|:---|
| Nav component | `TopNav` | `TopNav` (tạm) hoặc `StaffTopNav` khi có | `AdminTopNav` |
| State | Redux (`studentSlice` / thunk) | Redux hoặc local state | Local state + `useCallback` |
| API service | `studentService.js` | `staffService.js` (tạo khi cần) | `adminService.js` |
| Page header | Không có gradient header — layout tự do | Không có gradient header | `AdminPageHeader` |
| Route guard | `<PrivateRoute>` | `<StaffRoute>` (cần tạo) | `<AdminRoute>` |

---

## 1. CẤU TRÚC FILE

### 1.1 Quy tắc đặt tên

| Loại | Convention | Ví dụ |
|:---|:---|:---|
| Page file | `PascalCase.jsx` + cùng tên `.css` | `LearnNew.jsx` |
| Component | `PascalCase.jsx`, không có `.css` riêng trừ khi phức tạp | `FlashcardCard.jsx` |
| CSS class | `[prefix]-[block]` hoặc `[prefix]-[block]__[element]` | `.fc-card`, `.fc-card__front` |
| Prefix | 3 chữ cái viết tắt của feature | `fc-` cho Flashcard, `ex-` cho Exam |
| API function | `camelCase` trong `studentService.js` hoặc `staffService.js` | `getFlashcardsDue` |
| Redux thunk | `fetch[Name]Thunk` trong `store/slices/[name]Slice.js` | `fetchDashboardThunk` |

### 1.2 Vị trí file

```
apps/frontend/src/
├── pages/
│   ├── student/               ← pages của học viên (học tập, thi thử, v.v.)
│   │   ├── LearnNew.jsx
│   │   └── LearnNew.css
│   ├── staff/                 ← pages của staff (quản lý nội dung, chấm điểm)
│   │   ├── StaffDashboard.jsx
│   │   └── StaffDashboard.css
│   ├── dashboard/             ← Dashboard.jsx (đang dùng)
│   ├── profile/               ← Profile.jsx (đang dùng)
│   └── settings/              ← ChangePassword.jsx (đang dùng)
│
├── components/
│   ├── student/               ← student-specific sub-components
│   │   ├── HeroBanner.jsx
│   │   ├── StreakCard.jsx
│   │   ├── MiniStatCard.jsx
│   │   ├── LessonCard.jsx
│   │   ├── LessonList.jsx
│   │   ├── LessonVocabCard.jsx
│   │   ├── LessonGrammarPoint.jsx
│   │   ├── FlashcardCard.jsx
│   │   ├── QuickActionCard.jsx
│   │   ├── SkillRadarChart.jsx
│   │   └── ExamTopBar.jsx
│   ├── staff/                 ← staff-specific sub-components (tạo khi cần)
│   ├── common/                ← dùng chung mọi role
│   └── layout/
│       ├── TopNav.jsx         ← dùng cho student (và tạm cho staff)
│       └── AdminTopNav.jsx
│
├── store/
│   └── slices/
│       └── studentSlice.js    ← Redux slice: thêm thunk mới vào đây
│
└── api/
    ├── studentService.js      ← thêm function mới vào đây
    └── staffService.js        ← tạo khi build trang staff đầu tiên
```

### 1.3 Quy tắc tách component

**Tách ra file riêng khi:**

- Component > 60 dòng JSX
- Dùng lại ở ≥ 2 nơi
- Có animation hoặc interaction phức tạp (flip card, timer, v.v.)

**Giữ inline khi:**

- Component < 30 dòng
- Chỉ render UI thuần, không có state
- Là skeleton hoặc helper nhỏ

**Quy tắc page file:** Page JSX chỉ chứa:

1. Imports
2. Redux dispatch + selector (hoặc local state nếu trang không cần global state)
3. Return JSX: `TopNav` + `<div className="xxx-body">` gọi components
4. Không có logic nghiệp vụ, không có sub-component dài

---

## 2. GIẢI PHẪU MỘT TRANG STUDENT

### 2.1 Layout chuẩn — trang học tập (đơn giản)

```
┌──────────────────────────────────────────────┐
│  TopNav (sticky 64px, white)                 │  activeTab="[tab-id]"
├──────────────────────────────────────────────┤
│  <div className="[prefix]-body">             │
│    max-width: 1100px, margin: auto           │
│    padding: 28px 24px 48px                   │
│    [Nội dung trang]                          │
└──────────────────────────────────────────────┘
```

### 2.2 Layout chuẩn — trang 3 cột (như Dashboard)

```
┌──────────────────────────────────────────────────────┐
│  TopNav                                              │
├──────────────────────────────────────────────────────┤
│  <div className="[prefix]-body">                     │
│  ┌──────────┐  ┌────────────────────┐  ┌──────────┐  │
│  │  aside   │  │  <main>            │  │  aside   │  │
│  │  (left)  │  │  (center)          │  │  (right) │  │
│  │  240px   │  │  flex: 1           │  │  240px   │  │
│  └──────────┘  └────────────────────┘  └──────────┘  │
│  </div>                                              │
└──────────────────────────────────────────────────────┘
```

### 2.3 Template JSX — trang đơn giản

```jsx
import { useEffect, useCallback, useState } from 'react';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { MyFeatureComponent } from '../../components/student/MyFeatureComponent';
import { getMyData } from '../../api/studentService';
import './MyPage.css';

export default function MyPage() {
  const [data,      setData]    = useState(null);
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');
  const { toasts, addToast, removeToast } = useToast();

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const result = await getMyData();
      setData(result);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải dữ liệu.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  return (
    <div className="mfp-page">
      <TopNav activeTab="[tab-id]" />

      <div className="mfp-body">
        {error && (
          <div className="mfp-error-banner" role="alert">
            <span>{error}</span>
            <button className="mfp-retry-btn" onClick={fetchData}>Thử lại</button>
          </div>
        )}

        {isLoading
          ? <div className="mfp-skeleton" aria-hidden="true" />
          : !data
            ? <EmptyState title="Chưa có dữ liệu" subtitle="..." mascotVariant="thinking" />
            : <MyFeatureComponent data={data} addToast={addToast} />
        }
      </div>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

### 2.4 Template JSX — trang dùng Redux thunk

```jsx
import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchMyDataThunk } from '../../store/slices/studentSlice';
import TopNav from '../../components/layout/TopNav';
import './MyReduxPage.css';

export default function MyReduxPage() {
  const dispatch = useAppDispatch();
  const { myData, status } = useAppSelector((state) => state.student);
  const isLoading = status === 'loading';

  useEffect(() => {
    dispatch(fetchMyDataThunk());
  }, [dispatch]);

  return (
    <div className="mrp-page">
      <TopNav activeTab="[tab-id]" />
      <div className="mrp-body">
        {/* content */}
      </div>
    </div>
  );
}
```

**Khi nào dùng Redux vs local state:**

- **Redux**: Dữ liệu dùng ở nhiều component / nhiều trang (dashboard stats, user info, streak)
- **Local state + `useCallback`**: Dữ liệu chỉ dùng trong 1 trang (flashcard session, exam answers)

### 2.5 Template CSS chuẩn

```css
/* ===== [TênTrang] — SakuJi Hanami Theme ===== */

.mfp-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  font-family: var(--font-base);
}

.mfp-body {
  flex: 1;
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 24px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-sizing: border-box;
}

/* 3-cột layout (nếu cần) */
.mfp-body--3col {
  display: grid;
  grid-template-columns: 240px 1fr 240px;
  gap: 20px;
  align-items: start;
}

@media (max-width: 1199px) { .mfp-body { padding: 24px 20px 40px; } }
@media (max-width: 767px)  {
  .mfp-body { padding: 16px 16px 32px; }
  .mfp-body--3col { grid-template-columns: 1fr; }
}

@media (prefers-reduced-motion: reduce) {
  .mfp-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 3. DESIGN TOKENS — BẢNG THAM CHIẾU NHANH

> **Dùng chung với Admin.** Không bao giờ hard-code màu hex.

```css
/* === Màu brand === */
--color-primary:       #E89AAA   /* sakura pink */
--color-primary-light: #F7CBD4
--color-primary-dark:  #D84F68
--color-primary-bg:    #FFF0F3
--color-secondary:     #5DBB69   /* green — CTA */
--color-secondary-bg:  #F4FBF5
--color-accent:        #F7C948   /* gold — streak, achievement */
--color-accent-bg:     #FFF7DD

/* === Surface === */
--color-bg:            #FAF7F4
--color-card:          #FFFFFF
--color-border:        #E8E0DC

/* === Text === */
--color-text:          #2D2D2D
--color-text-sub:      #6B625E
--color-text-disabled: #B7ABA5

/* === Semantic === */
--color-error:         #E57373
--color-warning:       #F4A261

/* === Shape === */
--radius-sm:    8px
--radius-md:    12px
--radius-lg:    16px
--radius-xl:    24px
--radius-full:  9999px

/* === Shadow === */
--shadow-sm:    0 2px 8px rgba(0,0,0,0.07)
--shadow-md:    0 4px 12px rgba(0,0,0,0.10)
--shadow-lg:    0 8px 24px rgba(0,0,0,0.12)

/* === Misc === */
--font-base:    'Nunito', 'Noto Sans JP', system-ui, sans-serif
--transition:   200ms ease
```

**Màu thêm cho Student UI:**

```css
/* Streak / Achievement */
--color-streak:        #FF6B35   /* cam đậm — streak flame */
--color-streak-bg:     #FFF3EE   /* nền streak card */
--color-gold:          #F7C948   /* giống --color-accent */

/* Flashcard */
--color-fc-front:      var(--color-card)
--color-fc-back:       #FFF0F3   /* pink tint */
```

---

## 4. COMPONENT INVENTORY — CÓ SẴN, DÙNG LUÔN

### 4.1 Layout

| Component | Import | Props chính |
|:---|:---|:---|
| `TopNav` | `components/layout/TopNav` | `activeTab: string` |

**activeTab values** (phải khớp với `NAV_TABS` trong `TopNav.jsx`):

```
'review'      → /review          (Ôn tập)
'learn'       → /learn/new       (Học từ mới)
'kanji'       → /kanji           (Kanji)
'grammar'     → /grammar         (Ngữ pháp)
'dictionary'  → /dictionary      (Từ điển)
'mock-test'   → /mock-test       (Thi thử)
```

Nếu trang không thuộc tab nào (dashboard, profile, v.v.), truyền `activeTab=""`.

### 4.2 Student Components

| Component | Import | Props chính | Ghi chú |
|:---|:---|:---|:---|
| `HeroBanner` | `components/student/HeroBanner` | `course` | Banner khóa học đang học |
| `StreakCard` | `components/student/StreakCard` | `streak`, `weekDays[]` | Streak 7 ngày + SakuChan mascot |
| `MiniStatCard` | `components/student/MiniStatCard` | `type: 'words'\|'days'`, `value` | Stat nhỏ phía sidebar |
| `LessonCard` | `components/student/LessonCard` | `lesson` | Card bài học đơn lẻ |
| `LessonList` | `components/student/LessonList` | `lessons[]` | Danh sách bài học |
| `LessonVocabCard` | `components/student/LessonVocabCard` | `vocab` | Card từ vựng trong bài |
| `LessonGrammarPoint` | `components/student/LessonGrammarPoint` | `point` | Điểm ngữ pháp trong bài |
| `FlashcardCard` | `components/student/FlashcardCard` | `card`, `onRate` | Flashcard với flip animation |
| `QuickActionCard` | `components/student/QuickActionCard` | `type: 'flashcard'\|'exam'\|'dictionary'\|'progress'` | Card hành động nhanh |
| `SkillRadarChart` | `components/student/SkillRadarChart` | `skills` | Biểu đồ radar kỹ năng |
| `ExamTopBar` | `components/student/ExamTopBar` | `timeLeft`, `questionIndex`, `total` | Thanh trên trang thi |

### 4.3 Common Components (dùng cho cả Student & Staff)

| Component | Import | Props |
|:---|:---|:---|
| `EmptyState` | `components/common/EmptyState` | `title`, `subtitle`, `mascotVariant`, `mascotSize`, `children` |
| `Pagination` | `components/common/Pagination` | `currentPage` (1-based), `totalPages`, `onChange(page)` |
| `ProgressBar` | `components/common/ProgressBar` | `value: number (0-100)` |
| `StatusBadge` | `components/common/Badges` | `status: 'active'\|'suspended'\|'pending'\|'deleted'` |
| `JlptBadge` | `components/common/Badges` | `level: 'N5'\|'N4'\|'N3'\|'N2'\|'N1'` |
| `RoleBadge` | `components/common/Badges` | `userType`, `staffRole` |
| `UserAvatar` | `components/common/UserAvatar` | `src`, `name`, `size` |
| `useToast` | `components/common/Toast` | returns `{ toasts, addToast, removeToast }` |
| `ToastContainer` | `components/common/Toast` | `toasts`, `onRemove` |
| `AppLogo` | `components/common/AppLogo` | `size` |

### 4.4 SakuChan Mascot

```jsx
import SakuChan from '../../components/auth/SakuChan';

<SakuChan variant="idle" size={80} />
```

**variant values:** `'idle'` | `'happy'` | `'thinking'` | `'correct'` | `'wrong'` | `'celebrate'`

- Trang đang tải → `'thinking'`
- Đúng / hoàn thành → `'correct'` hoặc `'celebrate'`
- Sai / lỗi → `'wrong'`
- Mặc định → `'idle'`

**addToast usage:**

```js
addToast('success', 'Hoàn thành bài học!');
addToast('error',   'Có lỗi: ' + err.message);
addToast('info',    'Đang tải...');
```

### 4.5 Icons

Student và Staff pages **không có file icon riêng**. Dùng inline SVG trực tiếp:

```jsx
{/* Pattern chuẩn cho icon trong student/staff */}
<svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
  <path d="..." stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
</svg>
```

**Quy tắc:**

- Không import từ thư viện ngoài (lucide-react, heroicons, v.v.)
- SVG inline, `aria-hidden="true"`, `currentColor` cho stroke/fill
- Size chuẩn: 20px cho icon trong nội dung, 22px cho icon nav, 16px cho icon nhỏ
- Nếu dùng icon nhiều lần trong cùng feature → tạo `[Feature]Icons.jsx` kiểu như `ManageUsersIcons.jsx`

---

## 5. STATE MANAGEMENT PATTERN

### 5.1 Redux — dùng khi dữ liệu là global / shared

```jsx
// Đọc state
const { streak, wordCount, status } = useAppSelector((state) => state.student);
const isLoading = status === 'loading';

// Dispatch thunk
const dispatch = useAppDispatch();
useEffect(() => { dispatch(fetchDashboardThunk()); }, [dispatch]);
```

**Các state có sẵn trong `studentSlice`:**

```
state.student.streak          — số ngày streak
state.student.weekDays        — mảng bool 7 ngày
state.student.wordCount       — tổng từ đã học
state.student.daysThisMonth   — số ngày học trong tháng
state.student.course          — khóa học hiện tại
state.student.lessons         — danh sách bài học
state.student.status          — 'idle' | 'loading' | 'succeeded' | 'failed'
```

**Thêm thunk mới vào `studentSlice.js`** — không tạo slice mới cho cùng domain.

### 5.2 Local state — dùng khi dữ liệu chỉ dùng trong 1 trang

```jsx
const [data,      setData]    = useState(null);
const [isLoading, setLoading] = useState(true);
const [error,     setError]   = useState('');

const fetchData = useCallback(async () => {
  setLoading(true);
  setError('');
  try {
    const result = await apiCall();
    setData(result);
  } catch (err) {
    setError(err?.response?.data?.message ?? 'Không thể tải dữ liệu.');
    setData(null);
  } finally {
    setLoading(false);
  }
}, [/* deps */]);

useEffect(() => { fetchData(); }, [fetchData]);
```

### 5.3 Pagination state

```jsx
const [currentPage, setCurrentPage] = useState(1);
const [totalPages,  setTotalPages]  = useState(1);
const PAGE_SIZE = 20;

// Reset page khi filter thay đổi
useEffect(() => { setCurrentPage(1); }, [filter]);

// API dùng page - 1 (0-based cho backend)
const params = { page: currentPage - 1, size: PAGE_SIZE };
```

### 5.4 Debounce search

```jsx
const [search,    setSearch]    = useState('');
const [debounced, setDebounced] = useState('');
const timerRef = useRef(null);

useEffect(() => {
  clearTimeout(timerRef.current);
  timerRef.current = setTimeout(() => {
    setDebounced(search);
    setCurrentPage(1);
  }, 400);
  return () => clearTimeout(timerRef.current);
}, [search]);
```

### 5.5 Exam / Session state (client-heavy)

Với trang thi / flashcard session (state phức tạp, nhiều bước):

```jsx
const [sessionState, setSession] = useState({
  currentIndex: 0,
  answers: {},
  isSubmitted: false,
  score: null,
});

function handleAnswer(questionId, answerId) {
  setSession((prev) => ({
    ...prev,
    answers: { ...prev.answers, [questionId]: answerId },
  }));
}
```

---

## 6. LOADING / ERROR / EMPTY — 3 TRẠNG THÁI BẮT BUỘC

### 6.1 Loading — Skeleton

```jsx
{isLoading && (
  <div className="mfp-skeleton-wrap">
    {Array.from({ length: 4 }).map((_, i) => (
      <div key={i} className="mfp-card-skel" aria-hidden="true" />
    ))}
  </div>
)}
```

```css
.mfp-card-skel {
  height: 120px;
  border-radius: var(--radius-lg);
  background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%);
  background-size: 200% 100%;
  animation: skelPulse 1.4s ease infinite;
}
@keyframes skelPulse {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
```

### 6.2 Error — Banner + Retry

```jsx
{error && (
  <div className="mfp-error-banner" role="alert">
    <span>{error}</span>
    <button className="mfp-retry-btn" onClick={fetchData}>Thử lại</button>
  </div>
)}
```

```css
.mfp-error-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #FFEAEA;
  border: 1px solid var(--color-error);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  font-size: 13px;
  color: var(--color-error);
}
.mfp-retry-btn {
  background: transparent;
  border: 1.5px solid var(--color-error);
  border-radius: var(--radius-full);
  color: var(--color-error);
  font-size: 12px;
  font-weight: 700;
  padding: 4px 12px;
  cursor: pointer;
  white-space: nowrap;
}
```

### 6.3 Empty — EmptyState component

```jsx
{!isLoading && !error && (!data || data.length === 0) && (
  <EmptyState
    title="Chưa có dữ liệu"
    subtitle="Hãy bắt đầu học để thấy tiến trình của bạn nhé!"
    mascotVariant="thinking"
    mascotSize={120}
  >
    <button className="mfp-btn mfp-btn--primary" onClick={() => navigate('/learn/new')}>
      Bắt đầu học
    </button>
  </EmptyState>
)}
```

---

## 7. FORM PATTERN

### 7.1 Input field chuẩn

```jsx
<div className="mfp-field">
  <label className="mfp-field-label" htmlFor="field-id">Tên trường</label>
  <input
    id="field-id"
    className={`mfp-input${fieldError ? ' mfp-input--err' : ''}`}
    type="text"
    value={value}
    onChange={(e) => setValue(e.target.value)}
    placeholder="Placeholder..."
    aria-invalid={!!fieldError}
    aria-describedby={fieldError ? 'field-id-err' : undefined}
  />
  {fieldError && (
    <span id="field-id-err" className="mfp-field-error">{fieldError}</span>
  )}
</div>
```

```css
.mfp-field { display: flex; flex-direction: column; gap: 6px; }
.mfp-field-label { font-size: 13px; font-weight: 600; color: var(--color-text); }

.mfp-input {
  height: 44px;
  padding: 0 14px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg);
  font-family: var(--font-base);
  font-size: 14px;
  color: var(--color-text);
  width: 100%;
  box-sizing: border-box;
  transition: border-color var(--transition), box-shadow var(--transition);
}
.mfp-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232,154,170,0.15);
  background: var(--color-card);
}
.mfp-input--err { border-color: var(--color-error); background: #FEF2F2; }
.mfp-field-error { font-size: 12px; color: var(--color-error); }
```

### 7.2 Buttons chuẩn

```css
.mfp-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 44px;
  padding: 0 22px;
  border-radius: var(--radius-full);
  font-family: var(--font-base);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: filter var(--transition), transform var(--transition);
  white-space: nowrap;
}
.mfp-btn:disabled    { opacity: 0.60; cursor: not-allowed; }
.mfp-btn:active:not(:disabled) { transform: scale(0.97); }

/* Primary — green */
.mfp-btn--primary {
  background: var(--color-secondary);
  color: white;
  border: none;
  box-shadow: 0 2px 8px rgba(93,187,105,0.25);
}
.mfp-btn--primary:hover:not(:disabled) { filter: brightness(1.07); }

/* Pink / Brand */
.mfp-btn--brand {
  background: var(--color-primary);
  color: white;
  border: none;
  box-shadow: 0 2px 8px rgba(232,154,170,0.30);
}
.mfp-btn--brand:hover:not(:disabled) { filter: brightness(1.05); }

/* Outline */
.mfp-btn--outline {
  background: transparent;
  border: 1.5px solid var(--color-primary);
  color: var(--color-primary);
}
.mfp-btn--outline:hover:not(:disabled) { background: var(--color-primary-bg); }

/* Ghost */
.mfp-btn--ghost {
  background: transparent;
  border: 1.5px solid var(--color-border);
  color: var(--color-text-sub);
}
.mfp-btn--ghost:hover:not(:disabled) { color: var(--color-text); background: var(--color-bg); }

/* Danger */
.mfp-btn--danger { background: var(--color-error); color: white; border: none; }
```

### 7.3 Loading spinner trong button

```jsx
<button className="mfp-btn mfp-btn--primary" disabled={saving}>
  {saving && <span className="mfp-spinner mfp-spinner--white" aria-hidden="true" />}
  {saving ? 'Đang lưu…' : 'Lưu'}
</button>
```

```css
.mfp-spinner {
  display: inline-block;
  width: 18px; height: 18px;
  border: 2.5px solid var(--color-primary-light);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}
.mfp-spinner--white { border-color: rgba(255,255,255,0.35); border-top-color: white; }
@keyframes spin { to { transform: rotate(360deg); } }
```

---

## 8. CARD PATTERN (Student-specific)

Student pages dùng cards nhiều hơn tables. Pattern card chuẩn:

```jsx
<div className="mfp-card" onClick={() => handleSelect(item)}>
  <div className="mfp-card__header">
    <JlptBadge level={item.level} />
    <span className="mfp-card__meta">{item.category}</span>
  </div>
  <div className="mfp-card__body">
    <h3 className="mfp-card__title">{item.title}</h3>
    <p className="mfp-card__sub">{item.description}</p>
  </div>
  <div className="mfp-card__footer">
    <ProgressBar value={item.progress} />
    <span className="mfp-card__progress-label">{item.progress}%</span>
  </div>
</div>
```

```css
.mfp-card {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 20px;
  cursor: pointer;
  transition: box-shadow var(--transition), transform var(--transition);
  border: 1.5px solid transparent;
}
.mfp-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--color-primary-light);
}
.mfp-card:active { transform: translateY(0); box-shadow: var(--shadow-sm); }

.mfp-card__title { font-size: 16px; font-weight: 700; color: var(--color-text); margin: 0 0 4px; }
.mfp-card__sub   { font-size: 13px; color: var(--color-text-sub); margin: 0; }
.mfp-card__meta  { font-size: 11px; color: var(--color-text-sub); }
.mfp-card__header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.mfp-card__footer { display: flex; align-items: center; gap: 8px; margin-top: 12px; }

/* Grid cards */
.mfp-card-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
@media (max-width: 1199px) { .mfp-card-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px)  { .mfp-card-grid { grid-template-columns: 1fr; } }
```

---

## 9. MODAL PATTERN

Dùng chung pattern với Admin. Prefix thay `myp-` thành `[prefix]-`:

```jsx
function MyModal({ onClose, onSaved, addToast }) {
  const backdropRef = useRef(null);

  useEffect(() => {
    function onKey(e) { if (e.key === 'Escape') onClose(); }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  function handleBackdrop(e) { if (e.target === backdropRef.current) onClose(); }

  return (
    <div
      className="mfp-modal-backdrop"
      ref={backdropRef}
      onClick={handleBackdrop}
      role="dialog"
      aria-modal="true"
      aria-label="Tiêu đề modal"
    >
      <div className="mfp-modal">
        <div className="mfp-modal-header">
          <h3 className="mfp-modal-title">Tiêu đề</h3>
          <button className="mfp-modal-close" onClick={onClose} aria-label="Đóng">✕</button>
        </div>
        <div className="mfp-modal-body">
          {/* nội dung */}
        </div>
        <div className="mfp-modal-footer">
          <button className="mfp-btn mfp-btn--ghost" onClick={onClose}>Hủy</button>
          <button className="mfp-btn mfp-btn--primary">Xác nhận</button>
        </div>
      </div>
    </div>
  );
}
```

```css
.mfp-modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.40);
  backdrop-filter: blur(2px);
  z-index: 300;
  display: flex; align-items: center; justify-content: center;
  padding: 16px;
}
.mfp-modal {
  background: var(--color-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: 28px;
  width: 100%; max-width: 480px;
  max-height: 90vh; overflow-y: auto;
  animation: modalIn 0.22s ease;
}
@keyframes modalIn {
  from { opacity: 0; transform: scale(0.93) translateY(8px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}
.mfp-modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.mfp-modal-title  { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }
.mfp-modal-close  {
  width: 32px; height: 32px; border-radius: var(--radius-full);
  border: none; background: var(--color-bg); color: var(--color-text-sub); cursor: pointer;
}
.mfp-modal-close:hover { background: #FFEAEA; color: var(--color-error); }
.mfp-modal-body  { display: flex; flex-direction: column; gap: 16px; }
.mfp-modal-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding-top: 16px; margin-top: 8px;
  border-top: 1px solid var(--color-border);
}
```

---

## 10. API LAYER PATTERN

### 10.1 Student — thêm vào `studentService.js`

```js
// Pattern chuẩn
export async function getMyFeatureData({ param1, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (param1) params.param1 = param1;
  const res = await api.get('/students/my-endpoint', { params });
  return res.data.data;
}

export async function createMyFeatureItem(data) {
  const res = await api.post('/students/my-endpoint', data);
  return res.data.data;
}
```

**`api` là axios instance từ `authService.js`** — đã có interceptor JWT tự động.

### 10.2 Staff — tạo `staffService.js` khi cần trang staff đầu tiên

```js
// apps/frontend/src/api/staffService.js
import api from './authService';

export async function getStaffDashboard() {
  const res = await api.get('/staff/dashboard');
  return res.data.data;
}

export async function getAssignedContent({ page = 0, size = 20 } = {}) {
  const res = await api.get('/staff/content', { params: { page, size } });
  return res.data.data;
}
```

### 10.3 Upload file (avatar, OCR image)

```js
export async function uploadSomething(file) {
  const form = new FormData();
  form.append('file', file);
  const res = await api.post('/endpoint', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data.data;
}
```

### 10.4 Async job pattern (OCR, AI)

```jsx
// Submit job → poll kết quả
const [jobId,     setJobId]    = useState(null);
const [jobResult, setResult]   = useState(null);
const [polling,   setPolling]  = useState(false);
const pollRef = useRef(null);

async function handleSubmit(file) {
  const { jobId: id } = await submitOcr(kanjiId, file);
  setJobId(id);
  setPolling(true);
}

useEffect(() => {
  if (!polling || !jobId) return;
  pollRef.current = setInterval(async () => {
    const result = await getOcrResult(jobId);
    if (result.status !== 'PENDING') {
      clearInterval(pollRef.current);
      setPolling(false);
      setResult(result);
    }
  }, 2000);
  return () => clearInterval(pollRef.current);
}, [polling, jobId]);
```

---

## 11. STAFF FRONTEND — HƯỚNG DẪN KHI XÂY DỰNG

Hiện tại chưa có trang staff learning management. Khi xây dựng:

### 11.1 Xác định layout

Staff là internal tool → ưu tiên table/list hơn card. Dùng layout đơn giản 2 cột hoặc single column.

### 11.2 Nav cho Staff

Hiện dùng tạm `TopNav` (student nav). Khi có ≥ 3 trang staff riêng → tạo `StaffTopNav.jsx`:

```
components/layout/StaffTopNav.jsx
components/layout/StaffTopNav.css
```

Tabs dự kiến cho StaffTopNav:

```
'staff-dashboard' → /staff
'staff-content'   → /staff/content      (quản lý bài học, từ vựng)
'staff-exams'     → /staff/exams        (quản lý đề thi)
'staff-grading'   → /staff/grading      (chấm bài thủ công nếu có)
```

### 11.3 Route guard cho Staff

Hiện chưa có `StaffRoute`. Tạo `components/common/StaffRoute.jsx` theo pattern của `AdminRoute.jsx`:

```jsx
// Chỉ cho phép role === 'staff' hoặc 'admin'
export function StaffRoute({ children }) {
  const { user } = useAppSelector((state) => state.auth);
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== 'staff' && user.role !== 'admin') return <Navigate to="/forbidden" replace />;
  return children;
}
```

### 11.4 Staff page template

```jsx
import TopNav from '../../components/layout/TopNav'; // hoặc StaffTopNav khi có
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { getAssignedContent } from '../../api/staffService';
import './StaffContent.css';

export default function StaffContent() {
  // ... pattern giống admin (local state + useCallback)
  return (
    <div className="sfc-page">
      <TopNav activeTab="" />
      <div className="sfc-body">
        {/* filter bar */}
        {/* table hoặc card list */}
        {/* pagination */}
      </div>
      <ToastContainer ... />
    </div>
  );
}
```

---

## 12. RESPONSIVE PATTERN

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 1200px | Full layout |
| 768–1199px | 2 cột → 1 cột, padding giảm |
| < 768px | 1 cột, card full-width, modal full-screen |

```css
/* 3-cột → 1 cột trên mobile */
.mfp-body--3col {
  display: grid;
  grid-template-columns: 240px 1fr 240px;
  gap: 20px;
}
@media (max-width: 1199px) { .mfp-body--3col { grid-template-columns: 1fr 240px; } }
@media (max-width: 767px)  { .mfp-body--3col { grid-template-columns: 1fr; } }

/* Aside hiện dưới trên mobile */
@media (max-width: 767px) {
  .dashboard-left { order: 3; }  /* streak → cuối */
  .dashboard-right { order: 2; } /* quick actions → sau main */
}
```

---

## 13. ACCESSIBILITY CHECKLIST

- [ ] `<header role="banner">` — đã có trong TopNav
- [ ] Mỗi trang có đúng **1 thẻ `<h1>`** — viết tường minh trong page
- [ ] Mọi `<input>` có `<label>` liên kết bằng `htmlFor` / `id`
- [ ] Mọi icon button có `aria-label` mô tả đủ
- [ ] Modal có `role="dialog"`, `aria-modal="true"`, Escape đóng modal, click backdrop đóng modal
- [ ] Loading state có `aria-hidden="true"` trên skeleton
- [ ] Error message có `role="alert"`
- [ ] SakuChan và decorative SVG có `aria-hidden="true"`
- [ ] Màu sắc không phải thông tin duy nhất
- [ ] `@media (prefers-reduced-motion: reduce)` tắt animation
- [ ] Trang thi: `ExamTopBar` phải có `aria-live="polite"` cho bộ đếm thời gian

---

## 14. JLPT LEVEL COLORS

Dùng cho `JlptBadge` và filter UI (giống admin):

| Level | Background | Text |
|:---|:---|:---|
| N5 | `#E8F5E9` | `#2E7D32` |
| N4 | `#E3F2FD` | `#1565C0` |
| N3 | `#FFF3E0` | `#E65100` |
| N2 | `#F3E5F5` | `#6A1B9A` |
| N1 | `#FCE4EC` | `#C62828` |

---

## 15. CHECKLIST TẠO TRANG MỚI

### Student page

**Bước 1 — Chuẩn bị:**

- [ ] Xác định `activeTab` string cho TopNav (xem §4.1)
- [ ] Xác định prefix CSS (3 chữ cái, ví dụ `fc-`)
- [ ] Xác định: dùng Redux hay local state? (xem §5.1 vs §5.2)
- [ ] Xác định API endpoints cần (thêm vào `studentService.js`)

**Bước 2 — Thêm API:**

- [ ] Thêm functions vào `api/studentService.js`
- [ ] Nếu cần Redux: thêm thunk vào `store/slices/studentSlice.js`

**Bước 3 — Tạo file:**

- [ ] `pages/student/[Name].jsx` — page shell
- [ ] `pages/student/[Name].css` — CSS đầy đủ
- [ ] `components/student/[Name]Component.jsx` — mỗi section lớn = 1 file

**Bước 4 — Đăng ký route:**

- [ ] Thêm `<Route path="/[path]" element={<PrivateRoute><MyPage /></PrivateRoute>} />` vào `App.jsx`

**Bước 5 — Kiểm tra:**

- [ ] 3 trạng thái: loading skeleton / error banner / empty state
- [ ] Responsive 3 breakpoint
- [ ] Accessibility checklist (§13)
- [ ] Không hard-code hex color
- [ ] Không dùng icon từ thư viện ngoài

### Staff page

Giống Student, ngoài ra:

- [ ] Dùng `<StaffRoute>` thay `<PrivateRoute>` (tạo nếu chưa có)
- [ ] Thêm API vào `staffService.js` (tạo nếu chưa có)
- [ ] Nếu là trang staff thứ 3+: tạo `StaffTopNav.jsx`

---

## 16. VÍ DỤ NHANH

### Trang `/learn/new` (Học từ mới)

```
Prefix:      lnw-
activeTab:   'learn'
State:       local (useCallback) — từ vựng chỉ dùng trong trang này
API:         getVocabularyList({ level, page, size })
Components:  VocabFilterBar, VocabGrid (cards), VocabDetailModal
States:      vocabs[], isLoading, error, currentPage, totalPages, levelFilter
```

### Trang `/review` (Ôn tập / Flashcard)

```
Prefix:      rev-
activeTab:   'review'
State:       Redux (streak từ studentSlice) + local (session state)
API:         getFlashcardsDue(), rateFlashcard()
Components:  FlashcardCard (có sẵn), SessionSummary
States:      cards[], currentIndex, sessionComplete, score
```

### Trang staff `/staff/content`

```
Prefix:      sfc-
activeTab:   'staff-content' (khi có StaffTopNav)
State:       local state + useCallback
API:         staffService.getAssignedContent(), staffService.updateLesson()
Components:  ContentFilterBar, LessonTable, LessonFormModal
States:      lessons[], isLoading, error, currentPage, search, levelFilter
```
