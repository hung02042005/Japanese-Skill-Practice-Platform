# SPEC — Frontend Design System
> **Feature ID:** `feat-frontend-design`
> **Version:** 1.1 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-05-31

---

## 1. CONTEXT & GOAL

### 1.1 Bối cảnh
Frontend hiện có: React 18 + Vite, React Router DOM, Redux Toolkit, Axios, Tailwind CSS.
Auth pages (login, register, forgot-password, reset-password) đã dựng với màu blue (`#2563eb`).
Cần định nghĩa Design System để các feature tiếp theo (dashboard, learning, quiz, kanji…) xây nhất quán theo thương hiệu MochiKanji (màu cam, phong cách kawaii).

### 1.2 Mục tiêu
- Định nghĩa Design Tokens dùng CSS variables trong `index.css`
- Chuẩn hóa pattern: mỗi page có `PageName.jsx` + `PageName.css` trong `pages/<feature>/`
- Xác định thư mục `components/` tối thiểu cần tạo
- Thống nhất cách dùng Redux Toolkit slice + Axios service cho các feature mới

### 1.3 Lưu ý tương thích
Auth pages hiện dùng màu blue — **không thay đổi** trong scope này. Design system mới áp dụng cho các page mới (dashboard trở đi).

---

## 2. TECH STACK THỰC TẾ

| Layer | Công nghệ | Ghi chú |
|:---|:---|:---|
| Framework | React 18 + Vite | `apps/frontend/` |
| Routing | React Router DOM | `<BrowserRouter>` trong `App.jsx` |
| State | Redux Toolkit | `store/store.js` + `store/slices/*.js` |
| API | Axios | `api/*.js` — instance tại `api/authService.js` |
| Styling | CSS file per page + Tailwind (khai báo `index.css`) | Không dùng CSS Modules |
| Test | Jest + setup tại `src/test/setup.js` | |

---

## 3. DESIGN TOKENS

Định nghĩa tại `apps/frontend/src/index.css` trong `:root`, bổ sung vào sau `@tailwind utilities`.

```css
/* apps/frontend/src/index.css */
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  * { @apply border-gray-200; }
  body { @apply bg-gray-50 text-gray-900; }
}

/* ===== Design Tokens — SakuJi ===== */
:root {
  /* --- Color Primary (Sakura Pink) --- */
  --color-primary:       #E8637A;
  --color-primary-light: #F4A7B3;
  --color-primary-dark:  #C44E62;
  --color-primary-bg:    #FFF0F3;

  /* --- Color Secondary (Green) --- */
  --color-secondary:     #4CAF50;
  --color-secondary-light: #A5D6A7;
  --color-secondary-bg:  #F1F8E9;

  /* --- Color Accent (Gold) --- */
  --color-accent:        #F5C842;
  --color-accent-bg:     #FFFDE7;

  /* --- Neutral --- */
  --color-bg:            #FAF7F4;
  --color-card:          #FFFFFF;
  --color-border:        #E8E0DC;
  --color-text:          #2D2D2D;
  --color-text-sub:      #757575;
  --color-text-disabled: #BDBDBD;

  /* --- Semantic --- */
  --color-error:         #E53935;
  --color-success:       #43A047;
  --color-warning:       #FB8C00;

  /* --- Border Radius --- */
  --radius-sm:   8px;
  --radius-md:   12px;
  --radius-lg:   16px;
  --radius-xl:   24px;
  --radius-full: 9999px;

  /* --- Shadow --- */
  --shadow-sm:  0 1px 3px rgba(0,0,0,0.08);
  --shadow-md:  0 4px 12px rgba(0,0,0,0.10);
  --shadow-lg:  0 8px 24px rgba(0,0,0,0.12);

  /* --- Font --- */
  --font-base: 'Nunito', 'Noto Sans JP', system-ui, sans-serif;

  /* --- Transition --- */
  --transition: 200ms ease;
}
```

---

## 4. CẤU TRÚC FILE (Tuân theo cấu trúc hiện có)

```
apps/frontend/src/
├── App.jsx                        ← Routes tổng (đã có)
├── main.jsx                       ← Entry point (đã có)
├── index.css                      ← Tailwind + Design Tokens
│
├── api/
│   ├── authService.js             ← Đã có (login, register, logout…)
│   ├── studentService.js          ← Thêm mới khi làm dashboard
│   ├── learningService.js         ← Thêm mới khi làm learning
│   └── quizService.js             ← Thêm mới khi làm quiz
│
├── store/
│   ├── store.js                   ← Đã có — thêm reducer mới vào đây
│   ├── hooks.js                   ← Đã có (useAppDispatch, useAppSelector)
│   └── slices/
│       ├── authSlice.js           ← Đã có
│       ├── studentSlice.js        ← Thêm mới
│       └── learningSlice.js       ← Thêm mới
│
├── components/                    ← Tạo mới — shared components
│   ├── layout/
│   │   ├── TopNav.jsx             ← Nav bar student
│   │   ├── TopNav.css
│   │   ├── DashboardLayout.jsx    ← 3-column wrapper
│   │   └── DashboardLayout.css
│   └── common/
│       ├── Button.jsx
│       ├── Button.css
│       ├── LoadingSpinner.jsx
│       ├── LoadingSpinner.css
│       ├── EmptyState.jsx
│       └── EmptyState.css
│
├── pages/
│   ├── login/                     ← Đã có
│   │   ├── Login.jsx
│   │   └── Login.css
│   ├── register/                  ← Đã có
│   │   ├── Register.jsx
│   │   └── Register.css
│   ├── forgot-password/           ← Đã có
│   │   ├── ForgotPassword.jsx
│   │   ├── ForgotPassword.css
│   │   ├── ResetPassword.jsx
│   │   └── ResetPassword.css
│   ├── dashboard/                 ← Thêm mới
│   │   ├── Dashboard.jsx
│   │   ├── Dashboard.css
│   │   ├── StreakCard.jsx         ← Sub-component, đặt cùng thư mục
│   │   └── StatCard.jsx
│   ├── learn/                     ← Thêm mới
│   │   ├── LearnNew.jsx
│   │   └── LearnNew.css
│   ├── review/                    ← Thêm mới
│   │   ├── Review.jsx
│   │   └── Review.css
│   ├── kanji/                     ← Thêm mới
│   │   ├── Kanji.jsx
│   │   └── Kanji.css
│   └── quiz/                      ← Thêm mới
│       ├── Quiz.jsx
│       └── Quiz.css
│
└── test/
    └── setup.js                   ← Đã có
```

**Quy tắc đặt file:**
- Sub-component chỉ dùng trong 1 page → đặt cùng thư mục với page đó
- Sub-component dùng ở nhiều page → đặt vào `components/`
- Mỗi `.jsx` có file `.css` cùng tên, cùng thư mục
- Không dùng CSS Modules — dùng BEM-like class prefix để tránh xung đột (`.dashboard-`, `.streak-`, `.stat-`…)

---

## 5. PATTERN CHUẨN

### 5.1 Page Component
```jsx
// pages/dashboard/Dashboard.jsx
import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchDashboardThunk } from '../../store/slices/studentSlice';
import DashboardLayout from '../../components/layout/DashboardLayout';
import StreakCard from './StreakCard';
import StatCard from './StatCard';
import './Dashboard.css';

function Dashboard() {
  const dispatch = useAppDispatch();
  const { streak, wordCount, status } = useAppSelector((state) => state.student);
  const isLoading = status === 'loading';

  useEffect(() => {
    dispatch(fetchDashboardThunk());
  }, [dispatch]);

  if (isLoading) return <LoadingSpinner />;

  return (
    <DashboardLayout>
      {/* left slot */}
      <StreakCard streak={streak} />

      {/* main slot */}
      <div className="dashboard-main">
        {/* content */}
      </div>

      {/* right slot */}
      <div className="dashboard-right">
        <StatCard type="words" value={wordCount} />
        <StatCard type="streak" value={streak} />
      </div>
    </DashboardLayout>
  );
}

export default Dashboard;
```

### 5.2 Redux Slice
```js
// store/slices/studentSlice.js
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getDashboard } from '../../api/studentService';

export const fetchDashboardThunk = createAsyncThunk(
  'student/fetchDashboard',
  async (_, { rejectWithValue }) => {
    try {
      const res = await getDashboard();
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi tải dữ liệu');
    }
  }
);

const studentSlice = createSlice({
  name: 'student',
  initialState: { streak: 0, wordCount: 0, status: 'idle', error: null },
  reducers: {
    clearError: (state) => { state.error = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboardThunk.pending, (state) => { state.status = 'loading'; })
      .addCase(fetchDashboardThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.streak = action.payload.streak;
        state.wordCount = action.payload.wordCount;
      })
      .addCase(fetchDashboardThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      });
  },
});

export const { clearError } = studentSlice.actions;
export default studentSlice.reducer;
```

### 5.3 API Service
```js
// api/studentService.js — theo pattern của authService.js
import api from './authService'; // dùng lại axios instance đã có interceptor

export async function getDashboard() {
  const res = await api.get('/students/dashboard');
  return res.data;
}
```

### 5.4 Route Registration
```jsx
// App.jsx — thêm route mới vào đây
import Dashboard from './pages/dashboard/Dashboard';

<Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
```

---

## 6. LAYOUT PATTERNS

### 6.1 Dashboard Layout (3 cột)

```
┌──────────────────────────────────────────────────────────────────────┐
│                      TopNav (height: 64px)                           │
│ [Logo]  [Ôn tập][Học từ mới][Giao tiếp][Kanji][Sự kiện][Từ điển]   │
│                                                  [email][avatar]     │
└──────────────────────────────────────────────────────────────────────┘
┌──────────┬───────────────────────────────────────┬──────────────────┐
│ LEFT     │                                       │ RIGHT            │
│ 220px    │           MAIN (flex: 1)              │ 200px            │
│          │                                       │                  │
│ Streak   │  [Mascot / content / quiz]            │ [StatCards]      │
│ Card     │                                       │                  │
│          │  [CTA button]                         │                  │
└──────────┴───────────────────────────────────────┴──────────────────┘

Responsive:
- ≥ 1200px : 3 cột
- 768–1199px: ẩn left sidebar
- < 768px  : 1 cột, cả 2 sidebar ẩn
```

**CSS class pattern:**
```css
/* DashboardLayout.css */
.dashboard-layout { display: flex; flex-direction: column; min-height: 100vh; }
.dashboard-body   { display: flex; flex: 1; gap: 24px; padding: 24px; background: var(--color-bg); }
.dashboard-left   { width: 220px; flex-shrink: 0; }
.dashboard-main   { flex: 1; min-width: 0; }
.dashboard-right  { width: 200px; flex-shrink: 0; display: flex; flex-direction: column; gap: 16px; }
```

### 6.2 Auth Layout (đã có — không thay đổi)
Centered card, max-width 440px, màu blue hiện tại.

### 6.3 Exam Layout (fullscreen, không sidebar)
```
TopNav tối giản (chỉ logo + timer)
Progress bar toàn chiều rộng
Content area centered, max-width 720px
```

---

## 7. COMPONENT SPECS

### 7.1 TopNav
```
File: components/layout/TopNav.jsx + TopNav.css
Class prefix: .topnav-

Height: 64px
Background: #FFFFFF
Border-bottom: 1px solid var(--color-border)
Shadow: var(--shadow-sm)

Structure:
  .topnav-root
  ├── .topnav-logo (logo image + text "SakuJi")
  ├── .topnav-tabs
  │   └── .topnav-tab (×6) — icon 24px + label text-sm
  │       States: --active (color-primary + border-bottom 2px), --hover (bg color-primary-bg)
  └── .topnav-user (email truncated + avatar 36px)
```

### 7.2 StreakCard
```
File: pages/dashboard/StreakCard.jsx + (style trong Dashboard.css)
Class prefix: .streak-

Props: streak (number)
Background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%)
Border-radius: var(--radius-xl)
Padding: 16px
Width: 100% (trong left column 220px)

Khi streak === 0: flame icon opacity 0.35
Khi streak > 0: flame icon sáng + animation pulse 2s infinite
```

### 7.3 StatCard
```
File: pages/dashboard/StatCard.jsx
Props: type ('words' | 'streak'), value (number)
Class prefix: .stat-

type='words' : bg var(--color-accent-bg), border 2px solid var(--color-accent)
type='streak': bg var(--color-secondary-bg), border 2px solid var(--color-secondary)

Border-radius: var(--radius-lg)
Padding: 16px
```

### 7.4 Button (shared)
```
File: components/common/Button.jsx + Button.css
Class prefix: .btn-

Props: variant ('primary'|'secondary'|'ghost'), size ('sm'|'md'|'lg'), disabled, loading, onClick

.btn-primary  : bg var(--color-secondary), color white, border-radius var(--radius-full)
.btn-secondary: border 2px solid var(--color-primary), color var(--color-primary), bg transparent
.btn-ghost    : bg transparent, color var(--color-text-sub)

Size md: padding 12px 32px, font-size 14px, font-weight 700
Active: transform scale(0.97)
Loading: show spinner, disabled pointer-events
```

### 7.5 LoadingSpinner (shared)
```
File: components/common/LoadingSpinner.jsx
Props: size ('sm'|'md'|'lg')

Sizes: sm=20px, md=40px, lg=60px
Color: var(--color-primary)
Animation: spin 0.8s linear infinite
```

### 7.6 EmptyState (shared)
```
File: components/common/EmptyState.jsx
Props: mascotVariant, title, description, ctaLabel, onCta

Hiển thị mascot (img) + title + description + CTA button
Dùng khi list rỗng, OCR chưa upload, v.v.
```

---

## 8. COLOR & VISUAL (các page mới)

```css
/* Màu nav tab active */
.topnav-tab--active {
  color: var(--color-primary);
  border-bottom: 2px solid var(--color-primary);
}

/* Card chung */
.card {
  background: var(--color-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  padding: 24px;
}

/* JLPT Level Badge */
.jlpt-n5 { background: #E8F5E9; color: #2E7D32; }
.jlpt-n4 { background: #E3F2FD; color: #1565C0; }
.jlpt-n3 { background: #FFF3E0; color: #E65100; }
.jlpt-n2 { background: #F3E5F5; color: #6A1B9A; }
.jlpt-n1 { background: #FCE4EC; color: #C62828; }
/* Shape: border-radius var(--radius-full); padding: 3px 10px; font-size: 12px; font-weight: 700 */

/* CTA button màu green — "HỌC TỪ MỚI" */
.btn-cta {
  background: var(--color-secondary);
  color: #fff;
  border-radius: var(--radius-full);
  padding: 14px 48px;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  box-shadow: var(--shadow-md);
}
```

---

## 9. NAVIGATION (Route map)

```jsx
/* App.jsx — cấu trúc route đầy đủ */
<BrowserRouter>
  <Routes>
    {/* Auth (đã có) */}
    <Route path="/login"           element={<Login />} />
    <Route path="/register"        element={<Register />} />
    <Route path="/forgot-password" element={<ForgotPassword />} />
    <Route path="/reset-password"  element={<ResetPassword />} />

    {/* Student (thêm mới) */}
    <Route path="/dashboard"  element={<PrivateRoute role="STUDENT"><Dashboard /></PrivateRoute>} />
    <Route path="/learn/new"  element={<PrivateRoute role="STUDENT"><LearnNew /></PrivateRoute>} />
    <Route path="/review"     element={<PrivateRoute role="STUDENT"><Review /></PrivateRoute>} />
    <Route path="/kanji"      element={<PrivateRoute role="STUDENT"><Kanji /></PrivateRoute>} />
    <Route path="/quiz/:id"   element={<PrivateRoute role="STUDENT"><Quiz /></PrivateRoute>} />

    {/* Staff */}
    <Route path="/staff/*"    element={<PrivateRoute role="STAFF"><StaffLayout /></PrivateRoute>} />

    {/* Admin */}
    <Route path="/admin/*"    element={<PrivateRoute role="ADMIN"><AdminLayout /></PrivateRoute>} />

    <Route path="/" element={<Navigate to="/login" replace />} />
  </Routes>
</BrowserRouter>
```

**PrivateRoute** — component cần tạo tại `components/layout/PrivateRoute.jsx`:
- Đọc token từ Redux `state.auth`
- Nếu chưa đăng nhập → redirect `/login`
- Nếu sai role → redirect `/403`

---

## 10. INTERACTION STANDARDS

| Tình huống | Xử lý |
|:---|:---|
| API đang gọi | `status === 'loading'` → hiện `<LoadingSpinner />` |
| API lỗi | Toast hoặc inline error (theo pattern của Login.jsx — `api-error` div) |
| List rỗng | `<EmptyState />` với mascot + CTA |
| Unauthorized | Redux dispatch logout → navigate `/login` (đã có trong interceptor) |
| Form invalid | `fieldErrors` state + `.has-error` class (theo pattern Login.jsx) |

---

## 11. OUT OF SCOPE

- ❌ Thay đổi Auth pages hiện có (Login, Register, ForgotPassword, ResetPassword)
- ❌ Dark mode
- ❌ Lottie animations
- ❌ CSS Modules hoặc Styled Components — giữ CSS file per page
- ❌ Admin UI (spec riêng)
- ❌ Staff UI (spec riêng)
