# SPEC — Dashboard Học Sinh (Student Dashboard)
> **Feature ID:** `feat-dashboard` | **Page:** `Dashboard`
> **Route:** `/dashboard` (private — yêu cầu role STUDENT, redirect `/login` nếu chưa auth)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-01
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `.sdd/specs/backend/feat-learning-analytics/SPEC.md`

---

## 1. TỔNG QUAN TRANG

Dashboard là màn hình chính của học sinh sau khi đăng nhập. Mục tiêu: **định hướng hành trình học ngay từ giây đầu tiên** — học sinh nhìn vào biết ngay mình đang ở đâu, học gì tiếp theo, và streak của mình.

**Cấu trúc tổng thể (3 tầng):**
```
[1] TopNav     — 64px cố định, chạy full-width
[2] Dashboard  — flex row, 3 cột bên dưới TopNav
[3] Background — washi canvas #FAF7F4
```

**3 khu vực nội dung:**
```
[LEFT  220px]  StreakCard + Saku-chan
[CENTER flex:1] Hero Banner + Start Here + Lesson List
[RIGHT 200px]  Quick-Action Cards (Flashcard, Exam, v.v.)
```

**File structure:**
```
apps/frontend/src/
├── components/
│   └── layout/
│       ├── TopNav.jsx            ← dùng chung toàn app
│       ├── TopNav.css
│       ├── DashboardLayout.jsx   ← wrapper 3 cột
│       └── DashboardLayout.css
└── pages/
    └── dashboard/
        ├── Dashboard.jsx         ← page root
        ├── Dashboard.css
        ├── StreakCard.jsx        ← left sidebar card
        ├── HeroBanner.jsx        ← hero/course banner trung tâm
        ├── LessonList.jsx        ← danh sách bài học
        ├── LessonCard.jsx        ← một bài học trong danh sách
        └── QuickActionCard.jsx   ← right sidebar card
```

---

## 2. DESIGN TOKENS ÁP DỤNG

```css
/* Surface */
--color-bg:             #FAF7F4;   /* washi canvas — toàn bộ background */
--color-card:           #FFFFFF;   /* card surfaces */
--color-border:         #E8E0DC;

/* Brand */
--color-primary:        #E8637A;   /* sakura pink — accent, streak gradient */
--color-primary-light:  #F4A7B3;   /* gradient end-stop, petal trang trí */
--color-primary-dark:   #C44E62;   /* hover/active trên pink elements */
--color-primary-bg:     #FFF0F3;   /* hover bg cho nav tab, chip fill */

/* Action */
--color-secondary:      #4CAF50;   /* green — CTA "HỌC TỪ MỚI" */
--color-secondary-bg:   #F1F8E9;   /* stat card background */

/* Achievement */
--color-accent:         #F5C842;   /* gold — streak flame, badges */
--color-accent-bg:      #FFFDE7;   /* stat card "words learned" */

/* Text */
--color-text:           #2D2D2D;
--color-text-sub:       #757575;
--color-text-disabled:  #BDBDBD;

/* Semantic */
--color-error:          #E53935;

/* Radius */
--radius-sm:   8px;
--radius-md:   12px;
--radius-lg:   16px;
--radius-xl:   24px;
--radius-full: 9999px;

/* Shadow */
--shadow-sm:  0 2px 8px rgba(0,0,0,0.07);
--shadow-md:  0 4px 12px rgba(0,0,0,0.10);
--shadow-lg:  0 8px 24px rgba(0,0,0,0.12);
--shadow-petal-glow: 0 2px 10px rgba(232,99,122,0.22);

/* Font */
--font-base: 'Nunito', 'Noto Sans JP', system-ui, sans-serif;

/* Transition */
--transition: 200ms ease;
```

---

## 3. LAYOUT TỔNG THỂ

### 3.1 Page Shell

```
.dashboard-page
  min-height: 100vh
  display: flex, flex-direction: column
  background: var(--color-bg)
  font-family: var(--font-base)
```

### 3.2 TopNav — cố định trên cùng

```
Height:     64px
Position:   sticky, top: 0, z-index: 100
Background: var(--color-card) — white
Border:     border-bottom: 1px solid var(--color-border)
```

### 3.3 Dashboard Body — 3 cột

```
.dashboard-body
  display: flex
  flex: 1
  gap: 24px
  padding: 24px 32px
  max-width: 1440px
  margin: 0 auto
  width: 100%
  align-items: flex-start

.dashboard-left   { width: 220px; flex-shrink: 0; }
.dashboard-center { flex: 1; min-width: 0; }
.dashboard-right  { width: 200px; flex-shrink: 0; }
```

### 3.4 Sơ đồ bố cục đầy đủ

```
┌──────────────────────────────────────────────────────────────────────────┐
│  TopNav (sticky 64px)                                                    │
│  [🌸 SakuJi]  [Ôn tập][Học mới][Kanji][Ngữ pháp][Từ điển][Thi thử]    │
│                                                          [email] [avatar] │
└──────────────────────────────────────────────────────────────────────────┘
┌────────────────────────────────────────────────────────────────────────────┐
│ Background: var(--color-bg) — washi ivory                                  │
│                                                                            │
│  ┌──────────┐  ┌───────────────────────────────────────┐  ┌────────────┐  │
│  │  LEFT    │  │  CENTER                               │  │  RIGHT     │  │
│  │  220px   │  │  flex: 1                              │  │  200px     │  │
│  │          │  │                                       │  │            │  │
│  │[StreakCd]│  │  ┌─────────────────────────────────┐  │  │[QuickAct] │  │
│  │          │  │  │  HeroBanner                     │  │  │           │  │
│  │          │  │  │  (sakura gradient + mascot)     │  │  │[QuickAct] │  │
│  │          │  │  └─────────────────────────────────┘  │  │           │  │
│  │          │  │                                       │  │[QuickAct] │  │
│  │          │  │  ► Start Here                         │  │           │  │
│  │          │  │                                       │  │[StatCard] │  │
│  │          │  │  ┌──── LessonCard (active) ─────┐     │  │           │  │
│  │          │  │  │ ○  Bài 1: Hiragana           │     │  └────────────┘  │
│  │          │  │  │    Nhận biết 46 âm tiết cơ bản│    │                  │
│  │          │  │  └──────────────────────────────┘     │                  │
│  │          │  │  ┌── LessonCard ──────────────────┐   │                  │
│  │          │  │  │ ○  Bài 2: Katakana            │   │                  │
│  │          │  │  └──────────────────────────────┘     │                  │
│  │          │  │  ┌── LessonCard (locked) ─────────┐   │                  │
│  │          │  │  │ 🔒 Bài 3: Kanji cơ bản        │   │                  │
│  │          │  │  └──────────────────────────────┘     │                  │
│  └──────────┘  └───────────────────────────────────────┘  └─────────────┘ │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. TOPNAV — `.topnav`

```
Height:     64px
Background: var(--color-card)
Border:     border-bottom: 1px solid var(--color-border)
Position:   sticky, top: 0, z-index: 100
Padding:    0 32px
Display:    flex, align-items: center, justify-content: space-between
```

### 4.1 Logo — `.topnav-logo`

```
Display: flex, align-items: center, gap: 10px
Link:    href="/"

[AppLogo SVG]: 32px × 32px (Saku-chan mini)

[Wordmark]:
  "Saku" → font: Nunito 800, 22px, color: var(--color-primary)
  "Ji"   → font: Nunito 800, 22px, color: var(--color-text)

Hover: opacity 0.85
```

### 4.2 Navigation Tabs — `.topnav-tabs`

```
Display: flex, align-items: center, gap: 4px
```

**`.topnav-tab`** — mỗi mục điều hướng:
```
Layout:  flex, flex-direction: column, align-items: center, gap: 4px
Padding: 8px 16px
Cursor:  pointer
Border-radius: var(--radius-md) (trên tab block)
Transition: background var(--transition), color var(--transition)

[Icon]:  24px × 24px SVG, currentColor
[Label]: Nunito 600, 14px (label-md)

State Inactive:
  color: var(--color-text-sub)

State Active:
  color: var(--color-primary)
  border-bottom: 2px solid var(--color-primary)

State Hover:
  background: var(--color-primary-bg)
  color: var(--color-text)
```

**Danh sách tabs:**

| Icon | Label | Route |
|:---|:---|:---|
| 📖 BookOpen | Ôn tập | `/review` |
| ✨ Sparkles | Học từ mới | `/learn/new` |
| 漢 Kanji | Kanji | `/kanji` |
| 📝 Note | Ngữ pháp | `/grammar` |
| 🔍 Search | Từ điển | `/dictionary` |
| 📋 Clipboard | Thi thử | `/mock-test` |

### 4.3 User Area — `.topnav-user`

```
Display: flex, align-items: center, gap: 12px

[Email]:
  Font:       Nunito 400, 12px (body-sm)
  Color:      var(--color-text-sub)
  Max-width:  160px
  Overflow:   hidden, text-overflow: ellipsis, white-space: nowrap

[Avatar]:
  Width/Height: 36px
  Border-radius: var(--radius-full)
  Object-fit: cover
  Border: 2px solid var(--color-primary-light)
  Cursor: pointer
  Hover: border-color: var(--color-primary)
```

---

## 5. LEFT SIDEBAR — STREAK CARD

### 5.1 StreakCard — `.streak-card`

```
Background:    linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%)
Border-radius: var(--radius-xl)
Padding:       20px 16px
Width:         100%
Position:      relative
overflow:      hidden
Box-shadow:    var(--shadow-petal-glow)
```

**Cấu trúc nội dung:**
```
┌───────────────────────────────┐
│  🔥  Ngày Streak              │
│                               │
│       42                      │
│   (số ngày — 48px/800)        │
│                               │
│  [tiến độ tuần — 7 chấm]      │
│                               │
│           [Saku-chan 80px]    │
└───────────────────────────────┘
```

**Label "Ngày Streak" — `.streak-label`**
```
Display: flex, align-items: center, gap: 6px
Font:    Nunito 600, 14px
Color:   rgba(255,255,255,0.85)

[Flame icon — 🔥]:
  Khi streak > 0: color: var(--color-accent), opacity: 1
                  animation: pulse 2s infinite
  Khi streak = 0: opacity: 0.30
```

**Số streak — `.streak-number`**
```
Font:       Nunito 800, 48px (number-xl)
Color:      white
Line-height: 1
Margin:     8px 0 4px
```

**Tiến độ tuần — `.streak-week`**
```
Display: flex, gap: 6px, margin: 12px 0

7 chấm tròn (Thứ 2 → CN):
  Ngày đã học: width 20px, height 20px, border-radius: 50%
               background: white, opacity: 0.90
  Ngày chưa học: background: rgba(255,255,255,0.30)
  Ngày hôm nay (đã học): outline: 2px solid white
```

**Saku-chan — `.streak-mascot`**
```
Position: absolute, bottom: 0, right: -4px
Width:    80px (sm variant)
State:    happy khi streak > 0, idle khi streak = 0
```

**Petal trang trí — `.streak-petal`**
```
Position: absolute, top: 8px, right: 12px
Width: 32px, opacity: 0.15, pointer-events: none
```

---

## 6. CENTER — HERO BANNER

### 6.1 HeroBanner — `.hero-banner`

Banner tiêu đề khóa học, chiếm đầu khu vực center. Có nền gradient nhẹ, không đặc như StreakCard.

```
Background:    linear-gradient(135deg, var(--color-primary-bg) 0%, #FFF8FA 100%)
Border-radius: var(--radius-xl)
Padding:       28px 32px
Margin-bottom: 24px
Display:       flex, align-items: center, justify-content: space-between
overflow:      hidden
Position:      relative
Border:        1px solid var(--color-primary-light)
```

**Bố cục nội dung:**
```
┌────────────────────────────────────────────────────────────────┐
│                                         [Saku-chan md 120px]   │
│  [JLPT Badge N5]                                               │
│                                                                │
│  Lộ Trình Tiếng Nhật N5                                       │
│  (heading 30px/800)                                            │
│                                                                │
│  Nắm vững nền tảng: Hiragana, Katakana,                       │
│  từ vựng & ngữ pháp cơ bản                                     │
│  (14px/400)                                                    │
│                                                                │
│  [Progress bar]  12 / 40 bài hoàn thành                       │
│                                                                │
│  [● HỌC TỪ MỚI]                                               │
│                                                                │
│  [🌸 petal trang trí — absolute]                              │
└────────────────────────────────────────────────────────────────┘
```

**JLPT Level Badge — `.hero-badge`**
```
Dùng màu theo cấp độ (xem DESIGN.md § JLPT Level Colours):
  N5: background #E8F5E9, color #2E7D32
  N4: background #E3F2FD, color #1565C0
  N3: background #FFF3E0, color #E65100
  N2: background #F3E5F5, color #6A1B9A
  N1: background #FCE4EC, color #C62828

Border-radius: var(--radius-full)
Padding:       3px 10px
Font:          Nunito 700, 12px (label-sm)
Margin-bottom: 10px
Display:       inline-block
```

**Tiêu đề khóa học — `.hero-title`**
```
Font:          Nunito 800, 28px (display-lg)
Color:         var(--color-text)
Line-height:   1.25
Margin-bottom: 8px
```

**Mô tả — `.hero-desc`**
```
Font:        Nunito 400, 14px (body-md)
Color:       var(--color-text-sub)
Line-height: 1.6
Margin-bottom: 16px
Max-width:   380px
```

**Progress bar — `.hero-progress`**
```
Layout: flex, align-items: center, gap: 12px

[Track]:
  flex: 1, height: 8px
  background: var(--color-border)
  border-radius: var(--radius-full)

[Fill]:
  height: 100%
  background: var(--color-primary)
  border-radius: var(--radius-full)
  transition: width 0.4s ease
  width: calc({completed} / {total} * 100%)

[Label]:
  Font: Nunito 600, 13px
  Color: var(--color-text-sub)
  white-space: nowrap
  Text: "{completed} / {total} bài"
```

**CTA Button — `.hero-cta`**
```
Text:          "HỌC TỪ MỚI"
Background:    var(--color-secondary) — #4CAF50
Color:         white
Font:          Nunito 800, 15px, uppercase, letter-spacing: 0.5px
Border-radius: var(--radius-full)
Padding:       13px 36px
Border:        none
Cursor:        pointer
Shadow:        0 4px 12px rgba(76,175,80,0.30)
Margin-top:    20px
Transition:    filter var(--transition), transform 100ms

Hover:   filter: brightness(1.08)
Active:  transform: scale(0.97)
```

**Saku-chan mascot — `.hero-mascot`**
```
Width:    120px (giữa sm 80px và md 160px)
Position: absolute, right: 28px, bottom: 0
State:    idle (sway 3s ease-in-out infinite)
```

**Petal trang trí — `.hero-petal`**
```
Position: absolute
Fill: var(--color-primary-light), opacity: 0.12
pointer-events: none

Petal 1: top: 10%, left: 55%, width: 28px, rotate: 15deg
Petal 2: top: 40%, left: 70%, width: 18px, rotate: -20deg
Petal 3: bottom: 15%, left: 48%, width: 22px, rotate: 30deg
Animation: petalDrift 5s ease-in-out infinite alternate
```

---

## 7. CENTER — LESSON LIST

### 7.1 Section Header — "Start Here"

```
Display: flex, align-items: center, gap: 12px
Margin-bottom: 12px

[Chip "► Start Here"]:
  Background:    var(--color-primary)
  Color:         white
  Font:          Nunito 700, 12px
  Padding:       4px 12px
  Border-radius: var(--radius-full)
  Letter-spacing: 0.3px

[Đường kẻ divider]:
  flex: 1
  height: 1px
  background: var(--color-border)
```

### 7.2 Lesson List — `.lesson-list`

```
Display:        flex, flex-direction: column, gap: 12px
```

### 7.3 LessonCard — `.lesson-card`

Mỗi bài học một card ngang. Có 3 trạng thái: **active**, **available**, **locked**.

```
┌──────────────────────────────────────────────────────────────────────┐
│  [Thumbnail 52px]  Tiêu đề bài học               [JLPT Badge] [→]   │
│       ○            Mô tả ngắn về nội dung bài                        │
│                    [Progress bar nhỏ — nếu đã bắt đầu]              │
└──────────────────────────────────────────────────────────────────────┘
```

**Base styles:**
```
Display:        flex, align-items: center, gap: 16px
Background:     var(--color-card)
Border-radius:  var(--radius-lg)
Padding:        16px 20px
Box-shadow:     var(--shadow-sm)
Cursor:         pointer
Transition:     box-shadow var(--transition), transform var(--transition), border-color var(--transition)
Border:         1.5px solid transparent
```

**Hover (available):**
```
Box-shadow: var(--shadow-md)
Transform:  translateY(-1px)
Border-color: var(--color-primary-light)
```

**State Active (bài đang học / được chọn):**
```
Border:     1.5px solid var(--color-primary)
Box-shadow: var(--shadow-petal-glow)    /* 0 2px 10px rgba(232,99,122,0.22) */
Padding:    20px 24px                   ← lớn hơn một chút
Background: var(--color-card)

[Active indicator]:
  Đường dọc 3px ở cạnh trái card
  background: var(--color-primary)
  border-radius: 0 3px 3px 0
  height: 100%, position: absolute, left: 0
```

**State Locked:**
```
Opacity:   0.55
Cursor:    default
Background: var(--color-bg)   ← tối hơn một chút

[Lock icon]: thay thế thumbnail, color: var(--color-text-disabled)
```

#### 7.3.1 Thumbnail — `.lesson-thumb`

```
Width:         52px, Height: 52px
Border-radius: var(--radius-full)    ← hình tròn
Flex-shrink:   0
Display:       flex, align-items: center, justify-content: center

Nền mặc định:  var(--color-primary-bg)
Ký tự/Icon:    Font Nunito 800, 20px, color: var(--color-primary)
               Hoặc Noto Sans JP cho ký tự kanji/kana

State Active:
  Background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%)
  Color: white

State Locked:
  Background: var(--color-border)
```

#### 7.3.2 Content — `.lesson-content`

```
Flex: 1, min-width: 0
```

**Tiêu đề — `.lesson-title`**
```
Font:          Nunito 700, 16px
Color:         var(--color-text)
Margin-bottom: 3px
White-space:   nowrap, overflow: hidden, text-overflow: ellipsis

State Locked: color: var(--color-text-disabled)
```

**Mô tả — `.lesson-desc`**
```
Font:          Nunito 400, 13px
Color:         var(--color-text-sub)
White-space:   nowrap, overflow: hidden, text-overflow: ellipsis
Margin-bottom: 8px (chỉ khi có progress bar)
```

**Mini progress bar — `.lesson-progress`** (chỉ hiện khi đã bắt đầu bài)
```
Height:        4px
Background:    var(--color-border)
Border-radius: var(--radius-full)
overflow:      hidden

[Fill]: background var(--color-secondary), width: {pct}%, transition: width 0.3s ease
```

#### 7.3.3 Meta — `.lesson-meta`

```
Display: flex, flex-direction: column, align-items: flex-end, gap: 8px
Flex-shrink: 0

[JLPT Badge]: xem § 6.1 HeroBanner badge
[Arrow icon 16px]: color var(--color-text-disabled), state available/active: var(--color-text-sub)
```

---

## 8. RIGHT SIDEBAR — QUICK ACTION CARDS

### 8.1 QuickActionCard — `.qa-card`

Mỗi card chức năng nhanh: Flashcard, Mock Exam, Từ điển, Báo cáo.

```
┌──────────────────────────────┐
│  [Icon 36px]  Tiêu đề   [→]  │
│               Mô tả phụ      │
└──────────────────────────────┘
```

**Base styles:**
```
Display:        flex, align-items: center, gap: 12px
Background:     var(--color-card)
Border-radius:  var(--radius-lg)
Padding:        14px 16px
Box-shadow:     var(--shadow-sm)
Cursor:         pointer
Transition:     box-shadow var(--transition), transform var(--transition)
Text-decoration: none
```

**Hover:**
```
Box-shadow: var(--shadow-md)
Transform:  translateY(-2px)
```

**Icon container — `.qa-icon`**
```
Width:         40px, Height: 40px
Border-radius: var(--radius-md)
Display:       flex, align-items: center, justify-content: center
Flex-shrink:   0

Màu theo loại:
  Flashcard:  background #FFF0F3, color: var(--color-primary)
  Exam:       background #E3F2FD, color: #1565C0
  Dictionary: background #F1F8E9, color: var(--color-secondary)
  Report:     background var(--color-accent-bg), color: #D4891E
```

**Text area — `.qa-content`**
```
Flex: 1, min-width: 0

[Title]: Nunito 700, 14px, color: var(--color-text), margin-bottom: 2px
[Desc]:  Nunito 400, 12px, color: var(--color-text-sub)
         White-space: nowrap, overflow: hidden, text-overflow: ellipsis
```

**Arrow — `.qa-arrow`**
```
Width: 20px, Height: 20px
Color: var(--color-text-disabled)
Flex-shrink: 0
Transition: color var(--transition), transform var(--transition)

Parent:hover → color: var(--color-primary), transform: translateX(3px)
```

### 8.2 Danh sách Quick Actions

```
Display: flex, flex-direction: column, gap: 10px
```

| Icon | Title | Desc | Route |
|:---|:---|:---|:---|
| 🃏 Flashcard SVG | Ôn Flashcard | Spaced repetition | `/flashcard` |
| 📋 Clipboard | Thi Thử JLPT | Mock exam N5 | `/mock-test` |
| 🔍 Search | Từ Điển | Tra từ nhanh | `/dictionary` |
| 📊 Chart | Tiến Độ | Xem kết quả | `/progress` |

### 8.3 Stat Cards — `.stat-card`

Bên dưới Quick Actions, hiển thị 2 stat card nhỏ.

**`type="words"` — số từ đã học:**
```
Background: var(--color-accent-bg)   — #FFFDE7
Border:     2px solid var(--color-accent)
Border-radius: var(--radius-lg)
Padding:    14px 16px

[Icon 32px top-right]: ⭐ SVG, color: var(--color-accent), opacity: 0.25
[Label]: "Từ đã học", Nunito 400, 12px, color: var(--color-text-sub)
[Value]: Nunito 700, 20px, color: var(--color-text)
```

**`type="days"` — ngày học trong tháng:**
```
Background: var(--color-secondary-bg) — #F1F8E9
Border:     2px solid var(--color-secondary)
Border-radius: var(--radius-lg)
Padding:    14px 16px

[Icon 32px top-right]: 📅 SVG, color: var(--color-secondary), opacity: 0.25
[Label]: "Ngày học tháng này", Nunito 400, 12px, color: var(--color-text-sub)
[Value]: Nunito 700, 20px, color: var(--color-text)
```

---

## 9. TRẠNG THÁI LOADING

Khi API đang gọi (`status === 'loading'`), render skeleton thay cho nội dung thực.

### 9.1 Skeleton Pulse Animation

```css
@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.45; }
}

.skeleton {
  background:    var(--color-border);
  border-radius: var(--radius-md);
  animation:     skeleton-pulse 1.4s ease-in-out infinite;
}
```

### 9.2 Skeleton Layout

```
StreakCard:   skeleton block 220px × 220px, radius: var(--radius-xl)
HeroBanner:   skeleton block full-width × 160px, radius: var(--radius-xl)
LessonCard:   3 skeleton cards 72px height mỗi cái, radius: var(--radius-lg)
QA Cards:     3 skeleton cards 58px height, radius: var(--radius-lg)
```

---

## 10. TRẠNG THÁI EMPTY (chưa có dữ liệu)

Khi danh sách bài học rỗng (khoá học mới, chưa có nội dung):

```
Render <EmptyState /> tại khu vực center:
  SakuChan: md (160px), variant "thinking"
  Title:    "Chưa có bài học nào"
  Desc:     "Bài học đang được cập nhật. Hãy quay lại sau nhé!"
  CTA:      "Xem khoá học khác" → href="/courses"

Không bao giờ render trang trắng.
```

---

## 11. ANIMATIONS

```css
/* Khai báo trong Dashboard.css */

/* Sway cho Saku-chan idle */
@keyframes saku-sway {
  0%, 100% { transform: rotate(-3.5deg); }
  50%       { transform: rotate(3.5deg); }
}

/* Pulse cho flame icon streak */
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.18); }
}

/* Petal trang trí hero */
@keyframes petalDrift {
  0%   { transform: translateY(0px) rotate(0deg);   opacity: 0.12; }
  100% { transform: translateY(-8px) rotate(15deg); opacity: 0.08; }
}

/* Skeleton loading */
@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.45; }
}

/* Lesson card enter */
@keyframes slideUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

.lesson-card {
  animation: slideUp 250ms ease forwards;
}
.lesson-card:nth-child(1) { animation-delay: 0ms; }
.lesson-card:nth-child(2) { animation-delay: 60ms; }
.lesson-card:nth-child(3) { animation-delay: 120ms; }
.lesson-card:nth-child(4) { animation-delay: 180ms; }
/* ... tối đa 8 card animate */

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 12. ROUTE & COMPONENT

```jsx
// App.jsx
import Dashboard from './pages/dashboard/Dashboard';
<Route path="/dashboard"
       element={<PrivateRoute role="STUDENT"><Dashboard /></PrivateRoute>} />
```

```jsx
// pages/dashboard/Dashboard.jsx
import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchDashboardThunk } from '../../store/slices/studentSlice';
import TopNav from '../../components/layout/TopNav';
import StreakCard from './StreakCard';
import HeroBanner from './HeroBanner';
import LessonList from './LessonList';
import QuickActionCard from './QuickActionCard';
import StatCard from './StatCard';
import EmptyState from '../../components/common/EmptyState';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import './Dashboard.css';

function Dashboard() {
  const dispatch = useAppDispatch();
  const { streak, weekDays, course, lessons, wordCount, daysThisMonth, status } =
    useAppSelector((state) => state.student);

  const isLoading = status === 'loading';
  const hasLessons = lessons && lessons.length > 0;

  useEffect(() => {
    dispatch(fetchDashboardThunk());
  }, [dispatch]);

  return (
    <div className="dashboard-page">
      <TopNav activeTab="dashboard" />

      <div className="dashboard-body">
        {/* LEFT */}
        <aside className="dashboard-left">
          {isLoading
            ? <div className="skeleton skeleton--streak" />
            : <StreakCard streak={streak} weekDays={weekDays} />
          }
        </aside>

        {/* CENTER */}
        <main className="dashboard-center">
          {isLoading
            ? <div className="skeleton skeleton--hero" />
            : <HeroBanner course={course} />
          }

          <div className="lesson-section-header">
            <span className="start-here-chip">► Start Here</span>
            <hr className="lesson-divider" />
          </div>

          {isLoading
            ? <>{[1,2,3].map((i) => <div key={i} className="skeleton skeleton--lesson" />)}</>
            : hasLessons
              ? <LessonList lessons={lessons} />
              : <EmptyState
                  mascotVariant="thinking"
                  title="Chưa có bài học nào"
                  description="Bài học đang được cập nhật. Hãy quay lại sau nhé!"
                  ctaLabel="Xem khoá học khác"
                  onCta={() => navigate('/courses')}
                />
          }
        </main>

        {/* RIGHT */}
        <aside className="dashboard-right">
          <div className="qa-list">
            <QuickActionCard type="flashcard" />
            <QuickActionCard type="exam" />
            <QuickActionCard type="dictionary" />
            <QuickActionCard type="progress" />
          </div>
          <div className="stat-list">
            {isLoading
              ? <><div className="skeleton skeleton--stat" /><div className="skeleton skeleton--stat" /></>
              : <>
                  <StatCard type="words" value={wordCount} />
                  <StatCard type="days" value={daysThisMonth} />
                </>
            }
          </div>
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
| Tải dashboard | `GET` | `/api/students/dashboard` | `{ streak, weekDays[], course{}, lessons[], wordCount, daysThisMonth }` |

**Response shape (tham khảo):**
```json
{
  "status": 200,
  "data": {
    "streak": 14,
    "weekDays": [true, true, false, true, true, false, false],
    "course": {
      "id": 1,
      "title": "Lộ Trình Tiếng Nhật N5",
      "jlptLevel": "N5",
      "description": "Nắm vững nền tảng: Hiragana, Katakana...",
      "completedLessons": 12,
      "totalLessons": 40
    },
    "lessons": [
      {
        "id": 1,
        "title": "Hiragana",
        "description": "Nhận biết 46 âm tiết cơ bản",
        "jlptLevel": "N5",
        "status": "active",
        "progress": 0.6,
        "thumbnail": "あ"
      }
    ],
    "wordCount": 248,
    "daysThisMonth": 18
  }
}
```

**Lesson `status` values:**
- `"completed"` — đã hoàn thành
- `"active"` — đang học (card nổi bật)
- `"available"` — mở khóa, chưa bắt đầu
- `"locked"` — chưa mở khóa

---

## 14. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 1200px | Full 3 cột: left 220px + center flex + right 200px |
| 768–1199px | Ẩn `.dashboard-left`; center + right còn lại |
| < 768px | 1 cột duy nhất; TopNav thu gọn thành hamburger; right sidebar ẩn |

```css
/* Tablet */
@media (max-width: 1199px) {
  .dashboard-left { display: none; }
  .dashboard-body { padding: 20px 20px; }
}

/* Mobile */
@media (max-width: 767px) {
  .dashboard-body {
    flex-direction: column;
    padding: 16px;
    gap: 16px;
  }
  .dashboard-right { display: none; }
  .hero-banner { padding: 20px 20px; }
  .hero-mascot { width: 80px; right: 12px; }
  .hero-title  { font-size: 22px; }
}
```

---

## 15. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Landmark roles | `<header>` (TopNav), `<aside>` (sidebars), `<main>` (center) |
| Heading hierarchy | `<h1>` là course title trong HeroBanner; `<h2>` cho section headers |
| Lesson cards | `<button>` hoặc `<a>` bao toàn bộ card; `aria-label="Bài 1: Hiragana"` |
| Locked state | `aria-disabled="true"` + `aria-label="Bị khóa — hoàn thành bài trước"` |
| Active lesson | `aria-current="true"` trên card đang active |
| Streak | `aria-label="Streak hiện tại: 14 ngày"` trên số streak |
| Loading | `aria-busy="true"` trên `<main>` khi loading; skeleton có `aria-hidden="true"` |
| Focus ring | `outline: 2px solid var(--color-primary)` trên mọi interactive element |
| Tab order | TopNav → StreakCard → HeroBanner CTA → Lesson 1 → Lesson 2 → ... → QA Cards |
| Reduced motion | Tắt toàn bộ animation khi `prefers-reduced-motion: reduce` |

---

## 16. OUT OF SCOPE

- ❌ Staff/Admin dashboard (spec riêng)
- ❌ Notification panel / dropdown từ TopNav
- ❌ Course switching (chọn khóa học khác)
- ❌ Dark mode
- ❌ Mobile hamburger menu implementation (spec riêng)
- ❌ Lesson detail page (điều hướng đến khi click card)
