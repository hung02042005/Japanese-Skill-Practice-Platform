# SPEC — Trang chủ Từ Vựng (Vocab Home — SakuJi gamified)
>
> **Feature ID:** `feat-student` | **Page:** `VocabHome`
> **Route:** `/vocabulary` (gamified lesson-path home) — private, role STUDENT, redirect `/login` nếu chưa auth
> **UC Coverage:** UC-09 (Học Từ vựng theo Level/Topic), UC-19 (Progress & Streak)
> **Version:** 1.1 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-19
> **Design ref:** `feat-dashboard/SPEC-dashboard.md` — **SakuJi · Hanami** theme (sakura-pink). Layout gamified theo prompt N5 Kanji & Vocab.
> **Backend ref:** `.sdd/specs/backend/feat-core-learning/SPEC.md UC-09`, `feat-learning-analytics/SPEC.md`
>
> **Chủ đề chính:** Dùng **thương hiệu & design system SakuJi** (logo SakuJi, mascot **Saku-chan**, palette sakura-pink, font Nunito + Noto Sans JP). KHÔNG dùng brand/icon riêng khác. Tokens lấy từ `SPEC-dashboard.md §2` (token toàn cục `--color-*`).
> **Quan hệ spec cũ:** `SPEC-vocabulary.md` là màn **danh sách từ** (list/filter/search) tại `/vocabulary?view=list`. Spec này là **entry point gamified của section Từ vựng**; click LessonCard → mở word-list/flashcard session của bài (FR-VH-06).

---

## ACTOR

| Actor | Role | Điều kiện tiền quyết |
|:---|:---|:---|
| **Student** | Học viên đã đăng nhập | `isAuthenticated = true`, `role = STUDENT`, token hợp lệ |

---

## FUNCTIONAL REQUIREMENTS (EARS)

| ID | EARS Requirement |
|:---|:---|
| FR-VH-01 | WHEN an authenticated Student navigates to `/vocabulary`, THE SYSTEM SHALL dispatch `fetchVocabHomeThunk` to `GET /api/students/vocab-home`. |
| FR-VH-02 | WHILE `status === 'loading'`, THE SYSTEM SHALL render skeleton cho StreakCard, UnlockBanner, LessonList, và AccountPanel/CourseList. |
| FR-VH-03 | WHEN `fetchVocabHomeThunk` fulfilled, THE SYSTEM SHALL render TopNav (SakuJi), StreakCard (`streak`, `weekDays`), UnlockBanner (nếu `subscription !== 'VIP'`), section title "N5 Kanji & Vocab", LessonList theo `order_index`, và right sidebar (account + Course List). |
| FR-VH-04 | WHERE `subscription === 'VIP'`, THE SYSTEM SHALL ẩn hoàn toàn UnlockBanner và mở khoá LessonCard bị `locked` do VIP-gating. |
| FR-VH-05 | WHEN a Student clicks UnlockBanner CTA "UNLOCK NOW", THE SYSTEM SHALL navigate to `/subscription`. |
| FR-VH-06 | WHEN a Student clicks a LessonCard with `status === 'active'` hoặc `'available'`, THE SYSTEM SHALL navigate to `/vocabulary?topic={topicId}` (mở word-list / flashcard session của bài). |
| FR-VH-07 | WHEN a Student clicks a LessonCard with `status === 'locked'`, THE SYSTEM SHALL NOT navigate, SHALL set `aria-disabled="true"`, và SHALL hiển thị lock affordance. |
| FR-VH-08 | THE SYSTEM SHALL render đúng **một** LessonCard `active` với green styling nổi bật; các bài còn lại dùng style trắng (`available`) hoặc locked. |
| FR-VH-09 | WHEN a Student clicks "Course List" trên right sidebar, THE SYSTEM SHALL navigate to `/courses`. |
| FR-VH-10 | WHEN a Student clicks một mục TopNav, THE SYSTEM SHALL navigate tới route tương ứng (Ôn tập/Học mới/Kanji/Ngữ pháp/Từ điển/Thi thử). |
| FR-VH-11 | IF `lessons` array rỗng, THE SYSTEM SHALL render `<EmptyState>` với title "Chưa có bài học nào". |
| FR-VH-12 | WHEN `fetchVocabHomeThunk` rejected, THE SYSTEM SHALL hiển thị inline error state với nút "Thử lại". |
| FR-VH-13 | WHILE `streak > 0`, flame icon SHALL active (full opacity + pulse); WHILE `streak === 0`, flame SHALL opacity 0.30 và không animate. |
| FR-VH-14 | WHEN a Student hovers một LessonCard `available`/`active`, THE SYSTEM SHALL phóng to nhẹ (scale ~1.02) + tăng shadow. |

---

## NON-FUNCTIONAL REQUIREMENTS

| ID | Category | Requirement |
|:---|:---|:---|
| NFR-VH-01 | Architecture | Data chỉ đi qua `studentSlice` (Redux thunk) — KHÔNG gọi API trực tiếp trong component (`AGENTS.md §2.4`). |
| NFR-VH-02 | Security | Trạng thái khoá VIP chỉ là UX; backend PHẢI enforce 403 khi truy cập bài chưa mở. Anti-pattern "Authorization by UI hide" (`CLAUDE.md`). |
| NFR-VH-03 | UX | Skeleton xuất hiện < 100ms; không flash blank. |
| NFR-VH-04 | Correctness | Xử lý gracefully `streak=0`, `lessons=[]`, `subscription` undefined. |
| NFR-VH-05 | Brand | Dùng đúng SakuJi: logo wordmark "Saku"+"Ji", mascot Saku-chan, font Nunito/Noto Sans JP, token `--color-*` toàn cục. |
| NFR-VH-06 | Performance | Mascot/illustration SVG hoặc ảnh ≤ 30KB; lazy-load ảnh ngoài viewport. |

---

## ACCEPTANCE CRITERIA

| ID | Given | When | Then |
|:---|:---|:---|:---|
| AC-VH-01 | Student vào /vocabulary, API resolves | Render | TopNav SakuJi; StreakCard; UnlockBanner; title "N5 Kanji & Vocab"; LessonList; right sidebar |
| AC-VH-02 | subscription = VIP | Render | UnlockBanner KHÔNG render; bài VIP-locked mở |
| AC-VH-03 | streak = 0 | Render | Flame opacity 0.30, không pulse |
| AC-VH-04 | streak > 0 | Render | Flame full opacity + pulse |
| AC-VH-05 | Lessons có active + locked | Render | Bài active = green + Saku-chan tròn; locked = trắng dim + lock |
| AC-VH-06 | Click bài available | Click | Navigate /vocabulary?topic={id} |
| AC-VH-07 | Click bài locked | Click | Không navigate, aria-disabled=true |
| AC-VH-08 | Click "UNLOCK NOW" | Click | Navigate /subscription |
| AC-VH-09 | Click "Course List" | Click | Navigate /courses |
| AC-VH-10 | Hover bài available | Hover | Card scale ~1.02 + shadow tăng |
| AC-VH-11 | lessons = [] | Resolves | EmptyState render, không trang trắng |
| AC-VH-12 | width < 768px | Render | 2 sidebar ẩn/stack; 1 cột |
| AC-VH-13 | prefers-reduced-motion | Render | Tắt mọi animation |

---

## 1. TỔNG QUAN TRANG

Trang chủ section **Từ vựng** của **SakuJi** — dashboard học tập cho khoá **N5 Kanji & Vocab**, phong cách Hanami (sakura-pink, dễ thương). Học sinh học **từng bài theo danh sách** (lesson-path), có động lực qua streak + Saku-chan + unlock.

**Cấu trúc tổng thể (3 cột dưới TopNav):**

```
[TopNav]     — full-width: logo SakuJi + tabs + user (dùng chung toàn app)
[LEFT 220px] — StreakCard (sakura gradient) + tiến độ tuần
[CENTER 1fr] — UnlockBanner + title "N5 Kanji & Vocab" + LessonList
[RIGHT 220px]— AccountPanel + Course List card
[Background] — washi canvas var(--color-bg) #FAF7F4
```

**File structure:**

```
apps/frontend/src/
├── components/layout/
│   └── TopNav.jsx                 ← dùng chung (đã có), activeTab="vocabulary"
└── pages/vocabulary/
    ├── VocabHome.jsx              ← page root
    ├── VocabHome.css
    ├── StreakCard.jsx             ← tái dùng từ dashboard nếu được, hoặc local
    ├── UnlockBanner.jsx           ← banner "UNLOCK ALL LESSONS"
    ├── VocabLessonList.jsx
    ├── VocabLessonCard.jsx        ← 1 bài (active/available/locked)
    ├── AccountPanel.jsx
    └── CourseListCard.jsx
```

---

## 2. DESIGN TOKENS

> Dùng **token toàn cục SakuJi Hanami** (định nghĩa đầy đủ tại `SPEC-dashboard.md §2`). Trích những token màn này dùng:

```css
/* Surface */
--color-bg:            #FAF7F4;   /* washi canvas */
--color-card:          #FFFFFF;
--color-border:        #E8E0DC;

/* Brand — sakura pink */
--color-primary:       #E8637A;   /* accent, streak gradient */
--color-primary-light: #F4A7B3;
--color-primary-dark:  #C44E62;
--color-primary-bg:    #FFF0F3;

/* Action — green (active lesson + CTA) */
--color-secondary:     #4CAF50;
--color-secondary-bg:  #F1F8E9;

/* Achievement — gold (streak flame, VIP badge) */
--color-accent:        #F5C842;
--color-accent-bg:     #FFFDE7;

/* Text */
--color-text:          #2D2D2D;
--color-text-sub:      #757575;
--color-text-disabled: #BDBDBD;

/* Radius */
--radius-md: 12px; --radius-lg: 16px; --radius-xl: 24px; --radius-full: 9999px;

/* Shadow */
--shadow-sm: 0 2px 8px rgba(0,0,0,0.07);
--shadow-md: 0 4px 12px rgba(0,0,0,0.10);
--shadow-petal-glow: 0 2px 10px rgba(232,99,122,0.22);
--shadow-green-glow: 0 8px 20px rgba(76,175,80,0.28);

/* Font */
--font-base: 'Nunito', 'Noto Sans JP', system-ui, sans-serif;

/* Transition */
--transition: 200ms ease;
```

> **Mapping prompt → SakuJi:** banner "warm" của prompt → đổi sang **sakura gradient** cho nhất quán thương hiệu; "active lesson green" giữ nguyên (`--color-secondary`); streak flame dùng **gold** `--color-accent`; StreakCard nền **sakura gradient** như dashboard hiện có.

---

## 3. LAYOUT TỔNG THỂ

### 3.1 Page Shell

```
.vh-page
  min-height: 100vh
  display: flex, flex-direction: column
  background: var(--color-bg)
  font-family: var(--font-base)
```

### 3.2 Body grid (desktop ≥ 1024px)

```
.vh-body
  display: grid
  grid-template-columns: 220px 1fr 220px
  gap: 24px
  max-width: 1240px
  margin: 0 auto
  padding: 24px 24px 48px
  width: 100%
  align-items: start
```

### 3.3 Sơ đồ bố cục đầy đủ

```
┌──────────────────────────────────────────────────────────────────────────┐
│  TopNav (sticky 64px)                                                     │
│  [🌸 SakuJi]  Ôn tập  Học mới  Kanji  Ngữ pháp  Từ điển  Thi thử         │
│                                                  user@email.com  [avatar◯] │
└──────────────────────────────────────────────────────────────────────────┘
┌──────────────┐  ┌─────────────────────────────────────┐  ┌──────────────┐
│  StreakCard  │  │  ┌───────────────────────────────┐  │  │  AccountPanel│
│  Ngày Streak │  │  │ UNLOCK ALL LESSONS            │  │  │  [avatar]    │
│   🔥 10      │  │  │            [ UNLOCK NOW ]     │  │  │  Tên · N5    │
│  (Saku-chan) │  │  │   (Saku-chan · sakura grad)   │  │  │  FREE/VIP    │
│  ● ● ○ ● ●   │  │  └───────────────────────────────┘  │  └──────────────┘
│  (tuần)      │  │                                     │  ┌──────────────┐
│              │  │  N5 Kanji & Vocab   ← section title │  │ 📚 Course    │
│              │  │  ┌─────────────────────────────┐    │  │    List    → │
│              │  │  │ ◯ はじめましょう            │ ←  │  └──────────────┘
│              │  │  │   Start with Mochi  (green) │    │                 │
│              │  │  └─────────────────────────────┘    │                 │
│              │  │  ┌─────────────────────────────┐    │                 │
│              │  │  │ ◻ はじめまして              │    │                 │
│              │  │  │   Nice to meet you  (white) │    │                 │
│              │  │  └─────────────────────────────┘    │                 │
│              │  │  ┌─────────────────────────────┐    │                 │
│              │  │  │ 🔒 ロック中  (locked)        │    │                 │
│              │  │  └─────────────────────────────┘    │                 │
└──────────────┘  └─────────────────────────────────────┘                 │
```

---

## 4. TOPNAV (dùng chung)

Tái dùng `components/layout/TopNav.jsx` với `activeTab="vocabulary"`. Chi tiết logo/tabs/user xem `SPEC-dashboard.md §4`.

```
[Logo]: "Saku" (var(--color-primary)) + "Ji" (var(--color-text)), Saku-chan mini 32px
[Tabs]: Ôn tập /review · Học từ mới /learn/new · Kanji /kanji · Ngữ pháp /grammar
        · Từ điển /dictionary · Thi thử /mock-test
        → tab "Từ vựng" /vocabulary active (bổ sung nếu chưa có trong TopNav)
[User]: email + avatar tròn, border var(--color-primary-light)
```

---

## 5. LEFT SIDEBAR — STREAK CARD

> Tái dùng `StreakCard` của dashboard (`SPEC-dashboard.md §5`). Tóm tắt:

```
.vh-streak (= .streak-card)
  Background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%)
  Border-radius: var(--radius-xl)
  Padding: 20px 16px
  Box-shadow: var(--shadow-petal-glow)
  Position: relative, overflow: hidden
```

```
┌───────────────────────────┐
│  🔥  Ngày Streak          │  ← label, flame gold
│       10                  │  ← số streak 48px/800 white
│  ● ● ○ ● ● ○ ○            │  ← tiến độ tuần (7 chấm)
│            [Saku-chan 80]  │  ← mascot bottom-right
└───────────────────────────┘
```

- Flame icon: `streak > 0` → color var(--color-accent), pulse 2s; `streak = 0` → opacity 0.30.
- Số streak: Nunito 800, 48px, white.
- Tuần: 7 chấm — đã học white opacity .9; chưa học rgba(255,255,255,.3); hôm nay outline 2px white.
- Saku-chan: SVG 80px, state happy khi streak > 0.

---

## 6. CENTER — UNLOCK BANNER

> Chỉ render khi `subscription !== 'VIP'` (FR-VH-04).

### 6.1 UnlockBanner — `.vh-unlock`

```
Background:    linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%)
Color:         #FFFFFF
Border-radius: var(--radius-xl)
Padding:       24px 28px
Box-shadow:    var(--shadow-petal-glow)
Position:      relative, overflow: hidden
Display:       flex, align-items: center, justify-content: space-between
Margin-bottom: 24px
Border:        1px solid var(--color-primary-light)
```

**Tiêu đề — `.vh-unlock-title`**

```
Font: Nunito 800, 22px, uppercase, letter-spacing 0.5px
Text: "UNLOCK ALL LESSONS"
Text-shadow: 0 1px 2px rgba(0,0,0,0.12)
```

**Sub — `.vh-unlock-sub`** (tuỳ chọn)

```
Font: Nunito 600, 13px, color rgba(255,255,255,0.9)
Text: "Mở khoá toàn bộ bài N5 Kanji & Vocab"
```

**CTA — `.vh-unlock-cta`**

```
Text: "UNLOCK NOW"
Background: #FFFFFF, color var(--color-primary-dark)
Font: Nunito 800, 14px, uppercase
Border-radius: var(--radius-full), padding 12px 28px, border none
Box-shadow: 0 4px 10px rgba(0,0,0,0.12)
Transition: transform 100ms, filter var(--transition)
Hover: filter brightness(0.98); Active: transform scale(0.96)
onClick → navigate('/subscription')
```

**Saku-chan trang trí — `.vh-unlock-mascot`**

```
Saku-chan SVG, absolute right 20px bottom 0, width ~96px
Animation: saku-sway 3s ease-in-out infinite; pointer-events none
[Petal trang trí]: var(--color-primary-light), opacity 0.15
```

---

## 7. CENTER — SECTION TITLE + LESSON LIST

### 7.1 Section title — `.vh-section-title`

```
Font: Nunito 800, 22px, color var(--color-text)
Text: "N5 Kanji & Vocab"
Margin: 0 0 14px
[Biến thể chip "Start Here"]: tham khảo dashboard §7.1 nếu muốn nhấn bài đầu
```

### 7.2 VocabLessonList — `.vh-lesson-list`

```
Display: flex, flex-direction: column, gap: 14px
```

### 7.3 VocabLessonCard — `.vh-lesson`

3 trạng thái: **active** (green), **available** (trắng), **locked**.

**Layout:**

```
┌──────────────────────────────────────────────────┐
│  [◯ thumb 56px]   Tiêu đề JP            [→ / 🔒]  │
│                   Subtitle EN                      │
└──────────────────────────────────────────────────┘
```

**Base styles:**

```
Display: flex, align-items: center, gap: 16px
Background: var(--color-card)
Border-radius: var(--radius-lg)
Padding: 18px 20px
Box-shadow: var(--shadow-sm)
Cursor: pointer
Border: 1.5px solid transparent
Transition: transform var(--transition), box-shadow var(--transition), border-color var(--transition)
```

**Hover (available/active) — FR-VH-14:**

```
Transform: scale(1.02)
Box-shadow: var(--shadow-md)
Border-color: var(--color-primary-light)
```

**State `active` (green nổi bật):**

```
Background: linear-gradient(135deg, #66BB6A 0%, var(--color-secondary) 100%)
Color: #FFFFFF
Box-shadow: var(--shadow-green-glow)
Padding: 22px 24px
[Title]: color #FFFFFF
[Subtitle]: color rgba(255,255,255,0.9)
[Arrow]: color #FFFFFF
aria-current="true"
```

**State `available` (trắng):**

```
Background: var(--color-card)
[Arrow]: color var(--color-text-disabled) → hover var(--color-secondary)
```

**State `locked`:**

```
Background: var(--color-bg)
Opacity: 0.7, cursor: not-allowed
[Thumb] → lock icon, color var(--color-text-disabled)
aria-disabled="true"
```

#### 7.3.1 Thumbnail — `.vh-lesson-thumb`

```
56px, border-radius var(--radius-full), flex-shrink 0, overflow hidden
Display flex center
active:    nền trắng + Saku-chan tròn (cute round mascot)
available: ảnh minh hoạ tròn (cửa sổ, v.v.) hoặc ký tự kana/kanji
           Font Noto Sans JP 800, 22px, color var(--color-primary)
           (nền var(--color-primary-bg))
locked:    lock icon trên nền var(--color-border)
```

#### 7.3.2 Content — `.vh-lesson-content`

```
Flex 1, min-width 0
[Title] .vh-lesson-title:  Nunito 800, 17px, Noto Sans JP cho JP (vd "はじめましょう"); ellipsis
[Subtitle] .vh-lesson-sub: Nunito 600, 13px, color sub (vd "Start with Mochi"); ellipsis
```

#### 7.3.3 Trailing — `.vh-lesson-trail`

```
flex-shrink 0
available/active: arrow 20px
locked: lock 18px, color var(--color-text-disabled)
```

**Dữ liệu mẫu (theo prompt):**

| status | Title (JP) | Subtitle (EN) | Thumb |
|:---|:---|:---|:---|
| active | はじめましょう | Start with Mochi | Saku-chan tròn |
| available | はじめまして | Nice to meet you | ảnh cửa sổ tròn |
| available | … | … | ảnh minh hoạ tròn |
| locked | — | Locked lesson | 🔒 |

---

## 8. RIGHT SIDEBAR — ACCOUNT + COURSE LIST

### 8.1 AccountPanel — `.vh-account`

```
Background: var(--color-card), border-radius var(--radius-lg)
Padding: 18px 16px, box-shadow var(--shadow-sm)
Display: flex, flex-direction: column, align-items: center, gap: 8px

[Avatar]: 56px tròn, border 2px solid var(--color-primary-light)
[Name]:   Nunito 800, 15px, color var(--color-text)
[Meta]:   Nunito 600, 12px, color var(--color-text-sub) — "N5 · {FREE/VIP}"
[Badge VIP]: nếu VIP → chip nền var(--color-accent-bg), border var(--color-accent), text "VIP"
```

### 8.2 CourseListCard — `.vh-courselist`

```
Background: var(--color-card), border-radius var(--radius-lg)
Padding: 18px 16px, box-shadow var(--shadow-sm)
Display: flex, align-items: center, gap: 12px, cursor pointer
Transition: transform var(--transition), box-shadow var(--transition)
Hover: transform translateY(-2px); box-shadow var(--shadow-md)

[Icon kệ sách 32px]: color var(--color-primary) trên nền var(--color-primary-bg) bo tròn
[Label]: Nunito 800, 15px, color var(--color-text), "Course List"
[Arrow →]: 20px, color var(--color-text-disabled), hover translateX(3px) + color var(--color-primary)
onClick → navigate('/courses')
```

---

## 9. TRẠNG THÁI LOADING (Skeleton)

```
StreakCard:   skeleton 220×210px, radius var(--radius-xl)
UnlockBanner: skeleton full-width × 120px, radius var(--radius-xl)
SectionTitle: skeleton 220×30px
LessonCard:   3 skeleton 84px height, radius var(--radius-lg)
AccountPanel: 220×130px ; CourseList: 220×80px
```

```css
@keyframes vhSkel { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.vh-skel {
  background: linear-gradient(90deg,#f0ebe8 25%,#f8f4f2 50%,#f0ebe8 75%);
  background-size: 200% 100%;
  animation: vhSkel 1.4s ease infinite;
  border-radius: var(--radius-lg);
}
```

---

## 10. TRẠNG THÁI EMPTY

```
Khi lessons = []:
  Render <EmptyState /> tại vùng LessonList:
    Mascot: Saku-chan "thinking", size 160px
    Title:  "Chưa có bài học nào"
    Desc:   "Bài học đang được cập nhật. Hãy quay lại sau nhé!"
    CTA:    "Xem khoá học khác" → navigate('/courses')
  Không bao giờ render trang trắng.
```

---

## 11. ANIMATIONS

```css
@keyframes saku-sway { 0%,100% { transform: rotate(-3.5deg); } 50% { transform: rotate(3.5deg); } }
@keyframes pulse     { 0%,100% { transform: scale(1); } 50% { transform: scale(1.18); } }
@keyframes vhSlideUp { from { opacity:0; transform: translateY(12px); } to { opacity:1; transform: translateY(0); } }

.vh-lesson { animation: vhSlideUp 250ms ease forwards; }
.vh-lesson:nth-child(1){ animation-delay:0ms; }
.vh-lesson:nth-child(2){ animation-delay:60ms; }
.vh-lesson:nth-child(3){ animation-delay:120ms; }
.vh-lesson:nth-child(4){ animation-delay:180ms; }

@media (prefers-reduced-motion: reduce) {
  .vh-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 12. ROUTE & COMPONENT

```jsx
// App.jsx
import VocabHome from './pages/vocabulary/VocabHome';
<Route path="/vocabulary"
       element={<PrivateRoute role="STUDENT"><VocabHome /></PrivateRoute>} />
```

```jsx
// pages/vocabulary/VocabHome.jsx
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchVocabHomeThunk } from '../../store/slices/studentSlice';
import TopNav from '../../components/layout/TopNav';
import StreakCard from './StreakCard';
import UnlockBanner from './UnlockBanner';
import VocabLessonList from './VocabLessonList';
import AccountPanel from './AccountPanel';
import CourseListCard from './CourseListCard';
import EmptyState from '../../components/common/EmptyState';
import './VocabHome.css';

export default function VocabHome() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);
  const { streak, weekDays, lessons, subscription, status } =
    useAppSelector((s) => s.student);

  const isLoading = status === 'loading';
  const isVip = subscription === 'VIP';
  const hasLessons = lessons && lessons.length > 0;

  useEffect(() => { dispatch(fetchVocabHomeThunk()); }, [dispatch]);

  return (
    <div className="vh-page">
      <TopNav activeTab="vocabulary" />
      <div className="vh-body">
        {/* LEFT */}
        <aside className="vh-left">
          {isLoading
            ? <div className="vh-skel vh-skel--streak" aria-hidden="true" />
            : <StreakCard streak={streak} weekDays={weekDays} />}
        </aside>

        {/* CENTER */}
        <main className="vh-center" aria-busy={isLoading}>
          {!isLoading && !isVip && (
            <UnlockBanner onUnlock={() => navigate('/subscription')} />
          )}

          <h1 className="vh-section-title">N5 Kanji &amp; Vocab</h1>

          {isLoading
            ? <>{[1,2,3].map((i) => <div key={i} className="vh-skel vh-skel--lesson" aria-hidden="true" />)}</>
            : hasLessons
              ? <VocabLessonList
                  lessons={lessons}
                  isVip={isVip}
                  onOpen={(topicId) => navigate(`/vocabulary?topic=${topicId}`)} />
              : <EmptyState mascotVariant="thinking" title="Chưa có bài học nào"
                  description="Bài học đang được cập nhật. Hãy quay lại sau nhé!"
                  ctaLabel="Xem khoá học khác" onCta={() => navigate('/courses')} />}
        </main>

        {/* RIGHT */}
        <aside className="vh-right">
          {isLoading
            ? <div className="vh-skel vh-skel--account" aria-hidden="true" />
            : <AccountPanel user={user} subscription={subscription} />}
          <CourseListCard onClick={() => navigate('/courses')} />
        </aside>
      </div>
    </div>
  );
}
```

---

## 13. API

| Action | Method | Endpoint | Response data |
|:---|:---|:---|:---|
| Tải Vocab Home | `GET` | `/api/students/vocab-home` | `{ streak, weekDays[], courseTitle, subscription, lessons[] }` |

**Response shape (tham khảo):**

```json
{
  "status": 200,
  "data": {
    "streak": 10,
    "weekDays": [true, true, false, true, true, false, false],
    "courseTitle": "N5 Kanji & Vocab",
    "subscription": "FREE",
    "lessons": [
      { "topicId": 1, "titleJp": "はじめましょう", "subtitleEn": "Start with Mochi",
        "status": "active",    "thumbnail": "saku-mascot", "vipOnly": false },
      { "topicId": 2, "titleJp": "はじめまして", "subtitleEn": "Nice to meet you",
        "status": "available", "thumbnail": "window",      "vipOnly": false },
      { "topicId": 3, "titleJp": "ロック中", "subtitleEn": "Locked lesson",
        "status": "locked",    "thumbnail": null,          "vipOnly": true }
    ]
  }
}
```

**`status` values:** `"active"` (đúng 1 bài) · `"available"` · `"locked"`
**`vipOnly`:** nếu `true` và `subscription !== 'VIP'` → render locked + dẫn UnlockBanner CTA.

---

## 14. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 1024px | Grid 3 cột: 220 · 1fr · 220 |
| 768–1023px | Ẩn `.vh-left`; center + right |
| < 768px | 1 cột; StreakCard collapse trên cùng; right sidebar ẩn; LessonCard padding nhỏ; TopNav thu gọn |

```css
@media (max-width: 1023px) {
  .vh-body { grid-template-columns: 1fr 220px; }
  .vh-left { display: none; }
}
@media (max-width: 767px) {
  .vh-body { grid-template-columns: 1fr; padding: 16px; gap: 16px; }
  .vh-right { display: none; }
  .vh-lesson { padding: 14px 16px; }
  .vh-unlock-title { font-size: 18px; }
}
```

---

## 15. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Landmark roles | `<header>` (TopNav), `<aside>` (2 sidebar), `<main>` (center) |
| Heading hierarchy | `<h1>` = "N5 Kanji & Vocab" |
| Lesson cards | `<button>`/`<a>` bao toàn card; `aria-label="Bài: はじめましょう — Start with Mochi"` |
| Active lesson | `aria-current="true"` |
| Locked state | `aria-disabled="true"` + `aria-label="Bị khoá — cần VIP"` |
| Streak | `aria-label="Streak hiện tại: 10 ngày"` |
| Unlock CTA | `<button>` thật, `aria-label="Mở khoá tất cả bài học"` |
| Contrast | Text trắng trên green/pink ≥ 4.5:1; thêm text-shadow nếu cần |
| Loading | `aria-busy="true"` trên `<main>`; skeleton `aria-hidden="true"` |
| Focus ring | `outline: 2px solid var(--color-primary)` trên interactive |
| Hover-only affordance | Hành động hover (scale) phải có tương đương focus/keyboard |
| Reduced motion | Tắt saku-sway / pulse / hover-scale |

---

## 16. STYLE GUARDRAILS

Theo phong cách SakuJi Hanami (cute, friendly), TUYỆT ĐỐI tránh:

- ❌ Harsh shadows → chỉ soft shadow `rgba(0,0,0,0.07–0.10)`
- ❌ Dark UI → nền washi sáng `#FAF7F4`
- ❌ Complex clutter → mỗi vùng một nhiệm vụ, whitespace rộng
- ❌ Realistic human faces → chỉ mascot Saku-chan tròn, cute
- ❌ Sharp edges → mọi góc bo ≥ 12px
- ❌ Brand/icon ngoài SakuJi

---

## 17. OUT OF SCOPE

- ❌ Màn danh sách từ chi tiết (`SPEC-vocabulary.md` — `/vocabulary?view=list`)
- ❌ Flashcard session (`SPEC-flashcard-session.md`)
- ❌ Subscription/payment flow (`/subscription`)
- ❌ Course list page (`/courses`)
- ❌ Các section khác trong TopNav (Kanji, Ngữ pháp, …)
- ❌ Dark mode
- ❌ Mascot animation engine nâng cao (chỉ idle sway)

```
