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

/* ===== Design Tokens — MochiKanji ===== */
:root {
  /* --- Color Primary (Orange) --- */
  --color-primary:       #F5A623;
  --color-primary-light: #FFD580;
  --color-primary-dark:  #D4891E;
  --color-primary-bg:    #FFF8EC;

  /* --- Color Secondary (Green) --- */
  --color-secondary:     #4CAF50;
  --color-secondary-light: #A5D6A7;
  --color-secondary-bg:  #F1F8E9;

  /* --- Color Accent (Yellow) --- */
  --color-accent:        #FFD600;
  --color-accent-bg:     #FFFDE7;

  /* --- Neutral --- */
  --color-bg:            #F4F4F4;
  --color-card:          #FFFFFF;
  --color-border:        #E0E0E0;
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
  ├── .topnav-logo (logo image + text "MochiKanji")
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

## 12. HERO PAGE — LANDING SECTION

> **Reference layout:** ảnh MochiKanji hero (landscape bg + mascot trái + headline/CTA phải)
> **File:** `pages/home/Home.jsx` + `Home.css` + `HeroSection.jsx`

---

### 12.1 Tổng quan

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  TopNav tối giản: [Logo + "SakuJi"] ──────────────── [Nút Đăng Nhập]       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────────────┐        ┌───────────────────────────────────┐     │
│   │  Torii Gate + Saku   │        │  "Học 1000 Kanji trong 1 tháng"  │     │
│   │  (mascot idle anim)  │        │                                   │     │
│   │                      │        │  [BẮT ĐẦU HỌC]   ← green pill   │     │
│   │                      │        │  [Tôi đã có tài khoản] ← outline │     │
│   └──────────────────────┘        └───────────────────────────────────┘     │
│                                                                             │
│  ─────── background: sky + sakura trees + grass + Fuji silhouette ───────  │
│                         ↓  (scroll indicator)                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Route:** `/` (public) → `HomePage` — KHÔNG redirect thẳng sang `/login` nữa.

**Khi user đã đăng nhập:** tự redirect sang `/dashboard`.

---

### 12.2 Cấu trúc file

```
pages/home/
├── Home.jsx          ← Page wrapper, check auth redirect
├── Home.css
└── HeroSection.jsx   ← Sub-component hero full-viewport
```

Thêm route vào `App.jsx`:

```jsx
import Home from './pages/home/Home';

// Thay dòng: <Route path="/" element={<Navigate to="/login" replace />} />
// Bằng:
<Route path="/" element={<Home />} />
```

---

### 12.3 Home.jsx

```jsx
// pages/home/Home.jsx
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import HeroSection from './HeroSection';
import './Home.css';

function Home() {
  const navigate = useNavigate();
  const { token } = useAppSelector((state) => state.auth);

  useEffect(() => {
    if (token) navigate('/dashboard', { replace: true });
  }, [token, navigate]);

  return (
    <div className="home-root">
      <HeroSection />
    </div>
  );
}

export default Home;
```

---

### 12.4 TopNav tối giản (Hero variant)

Khác với `TopNav` dashboard (6 tabs) — hero page dùng variant **tối giản**:

```
┌─────────────────────────────────────────────────────────────┐
│  .hero-nav                                                   │
│  [Logo 36px + "SakuJi" wordmark]          [Đăng Nhập btn]  │
└─────────────────────────────────────────────────────────────┘
```

```
Class: .hero-nav
Position: absolute, top: 0, left: 0, right: 0, z-index: 100
Height: 64px
Background: transparent (overlay trên background ảnh)
Padding: 0 48px

.hero-nav-logo  : flex items-center gap: 8px
                  logo img 36px + text "SakuJi" font-size 22px, font-weight 800
                  color: var(--color-primary)   ← sakura pink / orange

.hero-nav-login : btn-secondary variant
                  border: 2px solid var(--color-card)
                  color: var(--color-card)      ← trắng để nổi trên bg tối
                  background: rgba(255,255,255,0.15) (glass effect nhẹ)
                  border-radius: var(--radius-full)
                  padding: 8px 24px
                  Hover: background rgba(255,255,255,0.30)
```

---

### 12.5 HeroSection — Visual Layout

#### Background layers (CSS stack từ dưới lên)

| Layer | Mô tả | CSS |
|:---|:---|:---|
| Sky | Gradient trời xanh nhạt → trắng | `background: linear-gradient(180deg, #B3E5FC 0%, #E8F5E9 60%, #C8E6C9 100%)` |
| Fuji | SVG/PNG núi Fuji mờ phía sau | `position: absolute`, center-right, `opacity: 0.55`, `bottom: 20%` |
| Sakura trees | SVG/PNG hàng cây hai bên | `position: absolute`, left 0 + right 0, `bottom: 0` |
| Grass | SVG dải cỏ xanh | `position: absolute`, `bottom: 0`, `width: 100%` |
| Falling petals | 6–8 SVG petal shapes | `position: absolute`, randomised, `animation: petal-fall`, `opacity: 0.45` |

```
.hero-section
  position: relative
  min-height: 100vh
  overflow: hidden
  background: linear-gradient(180deg, #B3E5FC 0%, #E8F5E9 60%, #C8E6C9 100%)
```

#### Content area (trên background)

```
.hero-content
  position: relative
  z-index: 10
  display: flex
  align-items: center
  justify-content: space-between
  padding: 100px 80px 80px   ← top 100px để tránh hero-nav
  max-width: 1280px
  margin: 0 auto
  min-height: 100vh
```

---

### 12.6 Cột trái — Mascot + Torii Gate

```
.hero-left
  flex: 0 0 420px
  position: relative
  display: flex
  align-items: flex-end
  justify-content: center
  min-height: 480px

.hero-torii
  position: absolute
  left: 0, bottom: 0
  width: 200px
  z-index: 1

.hero-mascot
  position: relative
  z-index: 2
  width: 280px          ← Saku-chan ở kích thước lg (300px)
  animation: saku-idle-sway 3s ease-in-out infinite
  filter: drop-shadow(0 8px 16px rgba(0,0,0,0.12))
```

**Animation `saku-idle-sway`:**

```css
@keyframes saku-idle-sway {
  0%, 100% { transform: rotate(-2deg) translateY(0);   }
  50%       { transform: rotate(2deg)  translateY(-8px); }
}
```

**Spark decorations** (giống ảnh tham chiếu — 2–3 sparkle icons quanh mascot):

```
.hero-spark
  position: absolute
  font-size: 20px
  color: var(--color-accent)     ← gold
  animation: spark-pulse 1.8s ease-in-out infinite
  pointer-events: none
```

---

### 12.7 Cột phải — Headline + CTAs

```
.hero-right
  flex: 1
  display: flex
  flex-direction: column
  align-items: flex-start
  gap: 24px
  max-width: 520px
  padding-left: 60px
```

#### Headline

```
.hero-headline
  font-family: var(--font-base)
  font-size: 42px
  font-weight: 800
  line-height: 1.2
  color: #3E2723        ← nâu đậm để đọc được trên bg sáng
  text-shadow: 0 2px 4px rgba(255,255,255,0.6)
```

Text mẫu: `"Học 1000 Kanji trong 1 tháng"`

Từ nhấn mạnh (`"1000 Kanji"`): `color: var(--color-primary)` — sakura pink / cam

#### Sub-headline (optional)

```
.hero-sub
  font-size: 16px
  font-weight: 400
  color: #5D4037        ← nâu vừa
  max-width: 400px
  line-height: 1.6
```

#### CTA group

```
.hero-cta-group
  display: flex
  flex-direction: column
  gap: 12px
  width: 100%
  max-width: 320px
```

**Nút 1 — Primary CTA "BẮT ĐẦU HỌC":**

```
background: var(--color-secondary)   ← green #4CAF50
color: #FFFFFF
border-radius: var(--radius-full)    ← pill
padding: 16px 0
width: 100%                          ← full width trong 320px
font-size: 16px
font-weight: 800
text-transform: uppercase
letter-spacing: 0.8px
box-shadow: var(--shadow-md)
Hover: filter brightness(1.08)
Active: transform scale(0.97)
```

**Nút 2 — Secondary "Tôi đã có tài khoản":**

```
background: rgba(255,255,255,0.85)
border: 2px solid rgba(255,255,255,0.9)
color: #5D4037
border-radius: var(--radius-full)
padding: 14px 0
width: 100%
font-size: 15px
font-weight: 700
backdrop-filter: blur(4px)
Hover: background rgba(255,255,255,1)
```

---

### 12.8 Scroll Indicator

```
.hero-scroll-indicator
  position: absolute
  bottom: 32px
  left: 50%
  transform: translateX(-50%)
  z-index: 10
  width: 40px
  height: 40px
  border-radius: var(--radius-full)
  background: rgba(255,255,255,0.70)
  backdrop-filter: blur(4px)
  display: flex
  align-items: center
  justify-content: center
  cursor: pointer
  animation: bounce-down 1.6s ease-in-out infinite
  box-shadow: var(--shadow-sm)
```

Icon: `↓` (ChevronDown 20px, `color: #5D4037`).

```css
@keyframes bounce-down {
  0%, 100% { transform: translateX(-50%) translateY(0);    }
  50%       { transform: translateX(-50%) translateY(6px); }
}
```

Khi click: `window.scrollTo({ top: window.innerHeight, behavior: 'smooth' })`.

---

### 12.9 Falling Petal Animation

6 petals, mỗi cái là `<span className="hero-petal">🌸</span>` (hoặc SVG inline):

```css
.hero-petal {
  position: absolute;
  top: -40px;
  font-size: 18px;
  opacity: 0;
  pointer-events: none;
  animation: petal-fall linear infinite;
}

@keyframes petal-fall {
  0%   { opacity: 0;    transform: translateY(-40px) rotate(0deg);   }
  10%  { opacity: 0.6;                                               }
  90%  { opacity: 0.4;                                               }
  100% { opacity: 0;    transform: translateY(110vh) rotate(360deg); }
}
```

Mỗi petal có `left` và `animation-duration` + `animation-delay` khác nhau (inline style):

| # | left | duration | delay |
|---|------|----------|-------|
| 1 | 8%   | 7s  | 0s    |
| 2 | 22%  | 9s  | 1.2s  |
| 3 | 40%  | 6s  | 0.4s  |
| 4 | 58%  | 8s  | 2.0s  |
| 5 | 74%  | 7s  | 0.8s  |
| 6 | 90%  | 10s | 1.6s  |

---

### 12.10 Responsive

| Breakpoint | Thay đổi |
|:---|:---|
| Desktop ≥ 1200px | 2 cột, mascot 280px, headline 42px |
| Tablet 768–1199px | 2 cột, mascot 200px, headline 32px, padding giảm |
| Mobile < 768px | 1 cột (mascot trên, headline dưới), mascot 160px, headline 28px, hero-left `min-height: 280px` |

```css
@media (max-width: 768px) {
  .hero-content   { flex-direction: column; align-items: center; padding: 80px 24px 60px; }
  .hero-left      { flex: 0 0 auto; width: 100%; min-height: 280px; }
  .hero-right     { padding-left: 0; align-items: center; text-align: center; }
  .hero-headline  { font-size: 28px; }
  .hero-cta-group { max-width: 100%; }
}
```

---

### 12.11 HeroSection.jsx — Skeleton

```jsx
// pages/home/HeroSection.jsx
import { useNavigate } from 'react-router-dom';

const PETALS = [
  { left: '8%',  duration: '7s',  delay: '0s'   },
  { left: '22%', duration: '9s',  delay: '1.2s' },
  { left: '40%', duration: '6s',  delay: '0.4s' },
  { left: '58%', duration: '8s',  delay: '2.0s' },
  { left: '74%', duration: '7s',  delay: '0.8s' },
  { left: '90%', duration: '10s', delay: '1.6s' },
];

function HeroSection() {
  const navigate = useNavigate();

  const handleScrollDown = () =>
    window.scrollTo({ top: window.innerHeight, behavior: 'smooth' });

  return (
    <section className="hero-section">
      {/* Hero TopNav */}
      <nav className="hero-nav">
        <div className="hero-nav-logo">
          <img src="/assets/logo.png" alt="SakuJi Logo" width={36} height={36} />
          <span>SakuJi</span>
        </div>
        <button className="hero-nav-login" onClick={() => navigate('/login')}>
          Đăng Nhập
        </button>
      </nav>

      {/* Falling petals */}
      {PETALS.map((p, i) => (
        <span
          key={i}
          className="hero-petal"
          style={{ left: p.left, animationDuration: p.duration, animationDelay: p.delay }}
        >
          🌸
        </span>
      ))}

      {/* Content */}
      <div className="hero-content">
        {/* Left — Mascot */}
        <div className="hero-left">
          <img src="/assets/torii-gate.png" className="hero-torii" alt="" aria-hidden="true" />
          <img src="/assets/saku-chan-happy.png" className="hero-mascot" alt="Saku-chan mascot" />
          <span className="hero-spark" style={{ top: '30%', right: '20%' }}>✦</span>
          <span className="hero-spark" style={{ top: '20%', left: '30%', animationDelay: '0.6s' }}>✦</span>
        </div>

        {/* Right — Headline + CTAs */}
        <div className="hero-right">
          <h1 className="hero-headline">
            Học <span className="hero-headline-accent">1000 Kanji</span>{' '}
            trong 1 tháng
          </h1>
          <p className="hero-sub">
            Luyện thi JLPT N5 → N1 với hệ thống spaced repetition, OCR nhận dạng
            chữ viết tay, và AI luyện hội thoại.
          </p>
          <div className="hero-cta-group">
            <button className="hero-btn-primary" onClick={() => navigate('/register')}>
              BẮT ĐẦU HỌC
            </button>
            <button className="hero-btn-secondary" onClick={() => navigate('/login')}>
              Tôi đã có tài khoản
            </button>
          </div>
        </div>
      </div>

      {/* Scroll indicator */}
      <button className="hero-scroll-indicator" onClick={handleScrollDown} aria-label="Cuộn xuống">
        ↓
      </button>
    </section>
  );
}

export default HeroSection;
```

---

### 12.12 Checklist triển khai

- [ ] Tạo `pages/home/Home.jsx` + `HeroSection.jsx` + `Home.css`
- [ ] Thêm route `/` → `<Home />` trong `App.jsx`, xoá `<Navigate to="/login">`
- [ ] Chuẩn bị assets: `logo.png`, `torii-gate.png`, `saku-chan-happy.png`, `sakura-tree-left.svg`, `sakura-tree-right.svg`, `fuji-silhouette.svg`
- [ ] Kiểm tra: user chưa login → thấy hero; user đã login → redirect dashboard
- [ ] Kiểm tra responsive tại 375px, 768px, 1280px

---

## 11. OUT OF SCOPE

- ❌ Thay đổi Auth pages hiện có (Login, Register, ForgotPassword, ResetPassword)
- ❌ Dark mode
- ❌ Lottie animations
- ❌ CSS Modules hoặc Styled Components — giữ CSS file per page
- ❌ Admin UI (spec riêng)
- ❌ Staff UI (spec riêng)
