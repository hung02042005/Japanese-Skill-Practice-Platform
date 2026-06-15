# SPEC — Từ Vựng / Learning Path (`/vocabulary`)
> **UC:** UC-09 — Học Từ vựng theo Level/Topic
> **Sprint:** 3 — Core Content
> **Prefix:** `voc-` | **activeTab:** `'vocabulary'` | **Guard:** PrivateRoute (STUDENT)
> **Version:** 2.0 | **Status:** Draft | **Last Updated:** 2026-06-14
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Layout ref:** `feat-dashboard/SPEC-dashboard.md` (cùng khung 3 cột + TopNav + StreakCard)
> **Convention ref:** `CONSTITUTION.md` (ĐIỀU 1, §2.2, §2.5) · `AGENTS.md` (§2.4, §3.2, §6, NEVER #11–12) · `CLAUDE.md` (React Anti-patterns)
> **Backend ref:** `feat-core-learning/SPEC.md UC-09`

---

## 0. THAY ĐỔI SO VỚI v1.0

> v1.0 là một **danh sách phẳng 1 cột** (level tabs → filter → list thẻ từ → pagination).
> v2.0 chuyển sang **"Vocab Learning Hub" 3 cột** theo prompt layout + DESIGN.md:
>
> | | v1.0 | v2.0 |
> |:---|:---|:---|
> | Khung trang | 1 cột, max-width 900px | 3 cột: Streak \| Path \| Course List (giống Dashboard) |
> | Đơn vị hiển thị ở trung tâm | từng **từ** (word card) | từng **chủ đề / bài** (path card) |
> | Tiêu đề | `<h1>` text thường | **Title pill** bo tròn lớn + 2 khối màu trang trí |
> | Định hướng | filter + search | **learning path** dọc, card đầu = "START HERE" (active) |
> | Màn từng từ (reading/audio/+FC) | chính là trang này | tách xuống **topic detail** (`?topic=`), xem § 11.2 — out of scope ở đây |
>
> Lý do: prompt yêu cầu khung học theo lộ trình (streak ở trái, danh sách bài ở giữa, điều hướng khóa học ở phải) — đồng bộ với Dashboard để học sinh nhận diện cùng một mental model.

---

## ACTOR

| Actor | Role | Điều kiện tiền quyết |
|:---|:---|:---|
| **Student** | Học viên đã đăng nhập | `isAuthenticated = true`, `role = STUDENT`, token hợp lệ |

---

## FUNCTIONAL REQUIREMENTS (EARS)

| ID | EARS Requirement |
|:---|:---|
| FR-VOC-01 | WHEN một Student đã đăng nhập điều hướng tới `/vocabulary`, THE SYSTEM SHALL gọi `GET /api/vocabulary/path?level={level}` để tải danh sách chủ đề (path cards) của cấp độ. |
| FR-VOC-02 | THE SYSTEM SHALL khởi tạo `level` theo thứ tự ưu tiên: query param `?level=` → `user.jlptLevel` → `'N5'`. |
| FR-VOC-03 | WHILE đang tải (`isLoading`), THE SYSTEM SHALL render skeleton cho StreakCard, Title pill, và ≥3 path card; KHÔNG để vùng trắng. |
| FR-VOC-04 | WHEN dữ liệu path trả về, THE SYSTEM SHALL render đúng thứ tự `order`, đánh dấu **một** card `status='active'` là card "START HERE" (style nổi bật), các card còn lại đồng nhất. |
| FR-VOC-05 | WHEN Student đổi cấp độ ở level selector, THE SYSTEM SHALL nạp lại path của cấp độ mới và cập nhật text Title pill thành `"{level} Kanji & Vocab"`. |
| FR-VOC-06 | WHEN Student click một path card có `status ∈ {active, available, completed}`, THE SYSTEM SHALL điều hướng tới topic detail `?topic={slug}` (xem § 11.2). |
| FR-VOC-07 | WHEN Student click một path card `status='locked'`, THE SYSTEM SHALL KHÔNG điều hướng và set `aria-disabled="true"`. |
| FR-VOC-08 | WHEN Student click "Course List Card" ở sidebar phải, THE SYSTEM SHALL điều hướng tới `/courses`. |
| FR-VOC-09 | IF mảng path rỗng, THE SYSTEM SHALL render `<EmptyState>` (Saku-chan `thinking`) ở vùng center thay cho danh sách. |
| FR-VOC-10 | WHEN API rejected, THE SYSTEM SHALL hiển thị error inline ở vùng center kèm nút "Thử lại". |

## NON-FUNCTIONAL REQUIREMENTS

| ID | Category | Requirement |
|:---|:---|:---|
| NFR-VOC-01 | Reuse | TopNav, StreakCard, JlptBadge, EmptyState **dùng lại** component sẵn có — không sao chép. (CLAUDE.md: tránh "God Component") |
| NFR-VOC-02 | Component | Path card tách thành **1 component tái sử dụng** `VocabPathCard` với prop `active` (cùng component cho card active & inactive — prompt §3). |
| NFR-VOC-03 | Tokens | Mọi màu/spacing/radius tham chiếu `--color-*`, `--radius-*`, `--space-*`; KHÔNG hard-code hex (DESIGN.md "Don't"). |
| NFR-VOC-04 | UX | Skeleton xuất hiện ≤100ms; không flash trắng. Mọi component API-backed phải có `isLoading` + `error` (CLAUDE.md React Anti-patterns). |
| NFR-VOC-05 | Security | Route bọc `<PrivateRoute role="STUDENT">`; role khác redirect dashboard tương ứng. **Ẩn UI chỉ là UX** — quyền truy cập level/VIP do **backend** quyết định (403), xem §A.3. |
| NFR-VOC-06 | Separation | **KHÔNG** đặt business logic ở frontend: trạng thái khóa/mở, chọn card `active`, đếm từ đã học, kiểm tra quyền level/VIP — tất cả do backend tính, frontend chỉ render (AGENTS.md §2.4, NEVER #11–12). Xem §A.3. |
| NFR-VOC-07 | Styling | Per-page `.css` co-located + CSS custom properties; **KHÔNG** CSS-in-JS (CONSTITUTION §ĐIỀU 1) và **KHÔNG** utility-class-heavy markup (DESIGN.md). Xem §A.1. |
| NFR-VOC-08 | Code limit | `VocabularyList.jsx` ≤ **300 dòng**, mỗi handler ≤ **40 dòng** (CONSTITUTION §2.2) → đó là lý do tách `VocabPathCard`. Không TODO comment trong code merge (§2.3). |
| NFR-VOC-09 | Data layer | Gọi API qua `api/studentService.js` (không `fetch` inline trong component); streak/weekDays đọc từ `studentSlice` đã có. (CLAUDE.md: "Direct API in Component") |

## ACCEPTANCE CRITERIA

| ID | Given | When | Then |
|:---|:---|:---|:---|
| AC-VOC-01 | Path có 1 card `active` + nhiều `available` | Trang render | Card active to hơn, đậm hơn, có tag "START HERE" đè cạnh trên |
| AC-VOC-02 | Card `locked` | Click | Không điều hướng, `aria-disabled="true"`, icon khóa |
| AC-VOC-03 | streak = 0 | Render | StreakCard: flame opacity 0.30, Saku-chan `idle` |
| AC-VOC-04 | Đổi level N5 → N4 | Click pill N4 | Title pill = "N4 Kanji & Vocab", path nạp lại, JLPT badge đổi màu N4 |
| AC-VOC-05 | path = [] | Data resolve | EmptyState render, không trang trắng |
| AC-VOC-06 | width < 768px | Render | Ẩn 2 sidebar, 1 cột, TopNav thu gọn |
| AC-VOC-07 | Click Course List Card | Click | Điều hướng `/courses` |

---

## A. TUÂN THỦ QUY ƯỚC DỰ ÁN

> Tham chiếu: `CONSTITUTION.md`, `AGENTS.md`, `CLAUDE.md`, `DESIGN.md`. Phần này chốt các điểm dễ vi phạm cho trang này.

### A.1 Stack & Styling
- React 18 + npm (CONSTITUTION §ĐIỀU 1). Component **PascalCase + `.jsx`**, hook **`use*` camelCase**, util **camelCase** (AGENTS §3.2).
- **Styling — CHỐT: dùng plain CSS.** Mỗi trang/component một file `.css` co-located (`VocabularyList.css`, `VocabPathCard` style gộp trong file trang), **CSS custom properties** (`--color-*`, `--radius-*`, `--space-*`) làm design token. **KHÔNG** CSS-in-JS, **KHÔNG** Tailwind utility-class trong markup. Khớp với toàn bộ codebase hiện có (`Dashboard.css`, `TopNav.css`, `VocabularyList.css`) và DESIGN.md ("Per-page CSS files… no utility-class-heavy markup").
- Class đặt theo tiền tố trang `voc-` (BEM-lite: `voc-path-card`, `voc-path-card--active`) như §3–§9.

### A.2 Giới hạn & sạch code (CONSTITUTION §2.2–§2.3)
- `VocabularyList.jsx` ≤ 300 dòng; handler ≤ 40 dòng → tách `VocabPathCard` + dùng lại StreakCard/TopNav để giữ page mỏng.
- Không `console.log`, không TODO comment trong code merge. Comment giải thích **TẠI SAO**, không phải LÀM GÌ.
- Cleanup mọi `useEffect` có timer/subscription (CLAUDE.md: "Memory Leaks").

### A.3 Phân tách Frontend / Backend (BẮT BUỘC — AGENTS §2.4, CONSTITUTION §2.5, NEVER #11–12)

> Trang này là **untrusted client** — chỉ render. Mọi quyết định nghiệp vụ nằm ở backend.

| Trách nhiệm | Bên xử lý | Ghi chú cho trang Vocab |
|:---|:---|:---|
| Trạng thái card `active / available / completed / locked` | **Backend** | Frontend render đúng `status` trả về, **không** tự suy luận khóa/mở (Domain Rule 7.2 — mở bài theo `lesson_order`). |
| Chọn card "START HERE" (`active`) | **Backend** | Backend chỉ ra đúng 1 card `active`; frontend không tự đoán "bài kế tiếp". |
| Đếm `completedWords / totalWords`, % tiến độ | **Backend** | Frontend chỉ hiển thị số nhận về (Domain Rule 7.2.2 — progress chỉ tăng, không sửa ở client). |
| Quyền truy cập **level** (N5→N1) + **VIP** (`is_vip_only`) | **Backend** | Đổi level ở UI chỉ là *request*; nếu student không có quyền, backend trả **403** → frontend hiển thị thông báo, **KHÔNG** tự mở (NEVER #5 cấm lẫn lộn level; Rule 7.3 check Role + subscription). |
| Ẩn/hiện nội dung VIP | **Backend** | "Authorization by UI hide" là anti-pattern — backend phải 401/403, không chỉ ẩn UI. |
| UI state: level đang chọn, hover, loading/error, điều hướng | **Frontend** | Được phép (AGENTS §2.4 cột Frontend). |
| Validation UX (không có ở trang này) | **Frontend** | Không thay thế validation backend. |

**Hệ quả EARS bổ sung:**
- FR-VOC-05 (đổi level): nếu API trả **403/422**, hiển thị thông báo "Cấp độ này yêu cầu nâng cấp / chưa mở khóa" thay vì danh sách — **không** render path của level bị cấm.
- FR-VOC-07 (locked): `status` đến từ backend; frontend chỉ chặn điều hướng + `aria-disabled`, không tự tính điều kiện mở.

### A.4 API envelope & lỗi (AGENTS §6)
- Mọi response theo chuẩn `{ "status", "message", "data" }`. Frontend đọc `res.data.data`.
- Xử lý mã lỗi: `401` → điều hướng `/login`; `403/422` → thông báo quyền/level; `5xx` → error inline + "Thử lại". Route `/api/[resource]` kebab-case số nhiều (AGENTS §3.3) — xem §11.

---

## 1. TỔNG QUAN TRANG

"Vocab Learning Hub" — màn hình lộ trình học từ vựng theo cấp độ JLPT. Học sinh nhìn vào biết ngay: **streak của mình (trái)**, **mình đang ở bài/chủ đề nào và học gì tiếp theo (giữa)**, **lối tắt sang danh sách khóa học (phải)**.

**Cấu trúc tổng thể (3 tầng), đồng bộ Dashboard:**
```
[1] TopNav     — 64px sticky, full-width (dùng lại component)
[2] Body       — flex row, 3 cột dưới TopNav
[3] Background — washi canvas var(--color-bg) #FAF7F4
```

**3 khu vực nội dung:**
```
[LEFT  220px ] StreakCard + Saku-chan            (dùng lại từ Dashboard)
[CENTER flex:1] Title pill → START HERE tag → Path of VocabPathCard
[RIGHT 200px ] Course List Card (điều hướng) + chỗ trống cho card tương lai
```

---

## 2. DESIGN TOKENS ÁP DỤNG

```css
/* Surface */
--color-bg:             #FAF7F4;   /* washi canvas — background toàn trang */
--color-card:           #FFFFFF;
--color-border:         #E8E0DC;

/* Brand (sakura pink) */
--color-primary:        #E8637A;   /* accent, active indicator, START HERE chip */
--color-primary-light:  #F4A7B3;   /* gradient end-stop, viền hover */
--color-primary-dark:   #C44E62;
--color-primary-bg:     #FFF0F3;   /* nền chip, thumbnail mặc định, hover */

/* Action / Achievement */
--color-secondary:      #4CAF50;   /* progress hoàn thành, badge "đã học" */
--color-secondary-bg:   #F1F8E9;
--color-accent:         #F5C842;   /* flame, sao */
--color-accent-bg:      #FFFDE7;

/* Text */
--color-text:           #2D2D2D;
--color-text-sub:       #757575;
--color-text-disabled:  #BDBDBD;
--color-error:          #E53935;

/* Radius */
--radius-sm: 8px; --radius-md: 12px; --radius-lg: 16px; --radius-xl: 24px; --radius-full: 9999px;

/* Shadow */
--shadow-sm: 0 2px 8px rgba(0,0,0,0.07);
--shadow-md: 0 4px 12px rgba(0,0,0,0.10);
--shadow-petal-glow: 0 2px 10px rgba(232,99,122,0.22);

/* Font + transition */
--font-base: 'Nunito', 'Noto Sans JP', system-ui, sans-serif;
--transition: 200ms ease;
```

**JLPT level colours** (cho `JlptBadge` & khối màu trang trí Title pill — DESIGN.md §JLPT):

| Level | Background | Text |
|---|---|---|
| N5 | `#E8F5E9` | `#2E7D32` |
| N4 | `#E3F2FD` | `#1565C0` |
| N3 | `#FFF3E0` | `#E65100` |
| N2 | `#F3E5F5` | `#6A1B9A` |
| N1 | `#FCE4EC` | `#C62828` |

---

## 3. LAYOUT TỔNG THỂ + MOCKUP

### 3.1 Page shell & body

```
.voc-page
  min-height: 100vh; display: flex; flex-direction: column;
  background: var(--color-bg); font-family: var(--font-base);

.voc-body                       /* giống .dashboard-body */
  display: flex; flex: 1; gap: 24px;
  padding: 24px 32px; max-width: 1440px; margin: 0 auto; width: 100%;
  align-items: flex-start;

.voc-left   { width: 220px; flex-shrink: 0; }
.voc-center { flex: 1; min-width: 0; }
.voc-right  { width: 200px; flex-shrink: 0; }
```

### 3.2 Sơ đồ bố cục đầy đủ

```
┌────────────────────────────────────────────────────────────────────────────┐
│ TopNav (sticky 64px)                                                       │
│ [🌸 SakuJi]  [Ôn tập][Học mới][Kanji][Ngữ pháp][Từ điển][Thi thử]   [email][◯]│
└────────────────────────────────────────────────────────────────────────────┘
┌────────────────────────────────────────────────────────────────────────────┐
│ Background: washi #FAF7F4                                                   │
│  ┌──────────┐  ┌──────────────────────────────────────┐  ┌──────────────┐  │
│  │  LEFT    │  │  CENTER                              │  │  RIGHT       │  │
│  │  220px   │  │  flex:1                              │  │  200px       │  │
│  │          │  │   ▓                          ▓       │  │              │  │
│  │[Streak ] │  │  ╭────────────────────────────────╮  │  │ ┌──────────┐ │  │
│  │[ Card  ] │  │  │      N5 Kanji & Vocab          │  │  │ │📚 Course │ │  │
│  │[Saku 🌸] │  │  ╰────────────────────────────────╯  │  │ │  List  → │ │  │
│  │          │  │   ▓ (khối màu trang trí dưới pill) ▓  │  │ └──────────┘ │  │
│  │          │  │                                      │  │              │  │
│  │          │  │  ╭ START HERE╮ (đè cạnh trên card 1) │  │  (trống —    │  │
│  │          │  │  ┌──────────────────────────────┐    │  │   card sau)  │  │
│  │          │  │  │ ◯  たべる / 食べる   ← ACTIVE │    │  │              │  │
│  │          │  │  │     Bài 1 · Động từ ăn uống   │    │  │              │  │
│  │          │  │  └──────────────────────────────┘    │  │              │  │
│  │          │  │  ┌──────────────────────────────┐    │  │              │  │
│  │          │  │  │ ◯  がっこう / 学校            │    │  │              │  │
│  │          │  │  │     Bài 2 · Trường học        │    │  │              │  │
│  │          │  │  └──────────────────────────────┘    │  │              │  │
│  │          │  │  ┌──────────────────────────────┐    │  │              │  │
│  │          │  │  │ 🔒 かぞく / 家族   ← LOCKED   │    │  │              │  │
│  │          │  │  └──────────────────────────────┘    │  │              │  │
│  └──────────┘  └──────────────────────────────────────┘  └──────────────┘ │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. TOPNAV (dùng lại)

Dùng nguyên `components/layout/TopNav.jsx` với `activeTab="vocabulary"`. Chi tiết style/tabs/user-dropdown xem `feat-dashboard/SPEC-dashboard.md §4`. **Không** định nghĩa lại trong spec này.

```jsx
<TopNav activeTab="vocabulary" />
```

---

## 5. LEFT SIDEBAR — STREAK CARD (dùng lại)

Dùng lại `StreakCard` (gradient sakura, số streak `number-xl`, 7 chấm tuần, Saku-chan `sm`). Spec đầy đủ: `feat-dashboard/SPEC-dashboard.md §5`.

```jsx
<aside className="voc-left">
  {isLoading ? <div className="skeleton skeleton--streak" />
             : <StreakCard streak={streak} weekDays={weekDays} />}
</aside>
```

Phần còn lại của sidebar trái để trống (chừa cho card tương lai — prompt §2.1).

---

## 6. CENTER — TITLE PILL BANNER

> Prompt §2.2.a — thanh "pill" bo tròn 2 đầu rất lớn, text căn giữa; 2 khối màu trang trí ở 2 góc làm lớp nền phía sau.

### 6.1 Khối bao — `.voc-titlewrap`
```
position: relative;
margin-bottom: 28px;            /* chừa chỗ cho START HERE tag bên dưới */
display: flex; justify-content: center;
```

### 6.2 Pill — `.voc-title-pill`
```
position: relative; z-index: 2;
display: inline-flex; align-items: center; justify-content: center;
min-height: 56px; padding: 0 48px;
background: var(--color-card);
border: 1.5px solid var(--color-primary-light);
border-radius: var(--radius-full);            /* pill — DESIGN.md: CTA & nhãn dạng petal */
box-shadow: var(--shadow-sm);

[Text]:
  font: 800 22px var(--font-base);   /* heading-lg/display-lg vibe */
  color: var(--color-text);
  text-content: "{level} Kanji & Vocab"
  (phần "{level}" tô màu var(--color-primary))
```

### 6.3 Khối màu trang trí — `.voc-title-blob` (lớp dưới)
```
position: absolute; z-index: 1; top: 50%; transform: translateY(-50%);
width: 64px; height: 64px; border-radius: var(--radius-lg);
opacity: 0.55; pointer-events: none;
filter: blur(2px);

.voc-title-blob--left  { left: 8%;  background: var(--color-primary-bg); rotate: -8deg; }
.voc-title-blob--right { right: 8%; background: var(--color-accent-bg);  rotate: 10deg; }
```
> Màu blob có thể lấy theo JLPT level (bảng §2) để củng cố tín hiệu cấp độ.

### 6.4 Level selector — `.voc-levels` (đặt trên Title pill)
Đổi cấp độ học. Hàng pill nhỏ, căn giữa, ngay trên Title pill.
```
display: flex; gap: 8px; justify-content: center; margin-bottom: 14px;
role="tablist"

.voc-level-pill                       /* mỗi N5..N1 */
  min-height: 32px; padding: 0 16px;
  border-radius: var(--radius-full);
  font: 700 13px var(--font-base);
  background: var(--color-card); color: var(--color-text-sub);
  border: 1.5px solid var(--color-border); cursor: pointer;
  transition: var(--transition);

.voc-level-pill:hover    { background: var(--color-primary-bg); }
.voc-level-pill--active  { background: var(--color-primary); color:#fff; border-color: var(--color-primary); }
```

---

## 7. CENTER — "START HERE" TAG

> Prompt §2.2.b — label nhỏ dạng speech-bubble, nằm trên-trái card đầu tiên, đè lên cạnh trên card đó.

```
.voc-start-tag
  position: absolute; z-index: 3;
  top: -12px; left: 16px;                 /* đè ~12px lên cạnh trên card active */
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 12px;
  background: var(--color-primary);
  color: #fff;
  font: 700 11px var(--font-base); letter-spacing: 0.4px; text-transform: uppercase;
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-petal-glow);

  /* đuôi speech-bubble nhỏ chỉ xuống card */
  &::after {
    content: ''; position: absolute; bottom: -4px; left: 16px;
    width: 8px; height: 8px; background: var(--color-primary);
    transform: rotate(45deg);
  }
```
Tag chỉ render trên card có `status='active'`. `VocabPathCard active` đặt `position: relative` để tag (absolute) neo theo nó.

---

## 8. CENTER — PATH CARDS (`VocabPathCard`)

> Prompt §2.2.c + §3 — danh sách card dọc, full-width center, **1 component tái sử dụng** dùng chung cho card active & inactive. Mỗi card dạng row: avatar tròn (trái) + 2 dòng text (phải).

### 8.1 Danh sách — `.voc-path-list`
```
position: relative;          /* neo START HERE tag */
display: flex; flex-direction: column; gap: 12px;
/* scroll dọc khi dài hơn viewport: vùng cha cuộn tự nhiên theo trang */
```

### 8.2 Card — `.voc-path-card`
Component: `components/student/VocabPathCard.jsx`, prop: `{ card, active, onOpen }`.

```
┌──────────────────────────────────────────────────────────────┐
│ [Avatar ◯ 52px]  食べる たべる             [JLPT N5] [→]     │
│                  Bài 1 · Động từ ăn uống · 0/24 từ           │
└──────────────────────────────────────────────────────────────┘
```

**Base:**
```
display: flex; align-items: center; gap: 16px;
background: var(--color-card);
border: 1.5px solid transparent;
border-radius: var(--radius-lg);
padding: 16px 20px;
box-shadow: var(--shadow-sm);
cursor: pointer;
transition: box-shadow var(--transition), transform var(--transition), border-color var(--transition);
```

**Hover (available/active):**
```
box-shadow: var(--shadow-md); transform: translateY(-1px);
border-color: var(--color-primary-light);
```

**Variant `active` (card "current/active", nổi bật hơn — prompt: to hơn/đậm hơn):**
```
position: relative;                /* neo START HERE tag (§7) */
border: 1.5px solid var(--color-primary);
box-shadow: var(--shadow-petal-glow);
padding: 22px 24px;                /* lớn hơn các card sau */

[Dải dọc cạnh trái]:
  ::before { content:''; position:absolute; left:0; top:0; bottom:0;
             width:3px; background: var(--color-primary); border-radius:0 3px 3px 0; }
```

**Variant `locked`:**
```
opacity: 0.55; cursor: default; background: var(--color-bg);
[Avatar] → icon khóa 🔒, màu var(--color-text-disabled)
aria-disabled="true"
```

#### 8.2.1 Avatar tròn — `.voc-pc-avatar`
```
width: 52px; height: 52px; border-radius: var(--radius-full); flex-shrink: 0;
display: flex; align-items: center; justify-content: center;
background: var(--color-primary-bg);
font: 800 22px var(--font-base); color: var(--color-primary);   /* hoặc ảnh thumbnail */
lang="ja" cho ký tự Nhật (kế thừa Noto Sans JP)

active:  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%); color:#fff;
locked:  background: var(--color-border);
```

#### 8.2.2 Nội dung text — `.voc-pc-body` (2 dòng — prompt §2.2.c)
```
flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 3px;

[Dòng 1 — tiêu đề lớn .voc-pc-title]:
  font: 700 18px var(--font-base); color: var(--color-text);
  (active: 20px); lang="ja" cho từ tiếng Nhật;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;

[Dòng 2 — mô tả nhạt .voc-pc-sub]:
  font: 400 13px var(--font-base); color: var(--color-text-sub);
  nội dung: "Bài {order} · {meaning/topic} · {completed}/{total} từ"
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
```

#### 8.2.3 Meta phải — `.voc-pc-meta`
```
display: flex; flex-direction: column; align-items: flex-end; gap: 8px; flex-shrink: 0;

[JlptBadge level={card.level}]   ← dùng lại component
[Arrow → 16px]: color var(--color-text-disabled); active/available: var(--color-text-sub);
                parent:hover → translateX(3px), color var(--color-primary)
```

#### 8.2.4 Mini progress (tùy chọn, khi `completed > 0`)
Thanh 4px dưới dòng 2, fill `var(--color-secondary)`, width `{completed/total*100}%` — giống `.lesson-progress` Dashboard §7.3.2.

---

## 9. RIGHT SIDEBAR — COURSE LIST CARD

> Prompt §2.3 — card row ở top sidebar phải: icon (trái) + label "Course List" (giữa) + mũi tên (phải); toàn card click được → trang khóa học. Phần còn lại để trống.

```
.voc-courselist-card  (thẻ <a href="/courses"> hoặc <button>)
  display: flex; align-items: center; gap: 12px;
  background: var(--color-card);
  border-radius: var(--radius-lg);
  padding: 14px 16px;
  box-shadow: var(--shadow-sm);
  text-decoration: none; cursor: pointer;
  transition: box-shadow var(--transition), transform var(--transition);

hover: box-shadow: var(--shadow-md); transform: translateY(-2px);

[Icon 40px .voc-cl-icon]:
  width:40px; height:40px; border-radius: var(--radius-md);
  background: var(--color-primary-bg); color: var(--color-primary);
  display:flex; align-items:center; justify-content:center;  (📚 SVG 22px)

[Label .voc-cl-label]:
  flex: 1; min-width: 0;
  font: 700 14px var(--font-base); color: var(--color-text);
  "Danh sách khóa học"

[Arrow .voc-cl-arrow]:
  width:20px; height:20px; color: var(--color-text-disabled); flex-shrink:0;
  parent:hover → color var(--color-primary), translateX(3px)
```
Toàn card có `aria-label="Mở danh sách khóa học"`. Bên dưới để trống (placeholder card tương lai — prompt §2.3).

---

## 10. STATE & DATA FLOW

```js
const navigate = useNavigate();
const { user } = useAppSelector((s) => s.auth);
const { streak = 0, weekDays = [] } = useAppSelector((s) => s.student);  // cho StreakCard

const [searchParams, setSearchParams] = useSearchParams();
const [level,    setLevel]   = useState(searchParams.get('level') ?? user?.jlptLevel ?? 'N5');
const [cards,    setCards]   = useState([]);     // path cards (topic groups)
const [isLoading,setLoading] = useState(true);
const [error,    setError]   = useState('');

const LEVELS = ['N5','N4','N3','N2','N1'];

// Mở topic detail (màn từ phẳng v1.0 — §11.2). Card locked đã chặn ở VocabPathCard.
const openTopic = (card) => navigate(`/vocabulary?level=${level}&topic=${card.slug}`);
```

- Đổi `level` → cập nhật `?level=` (giữ deep-link) + nạp lại path.
- `active` card = card có `status==='active'` (backend chỉ ra **đúng 1**; frontend KHÔNG tự suy luận "bài kế tiếp" — §A.3).
- Dữ liệu StreakCard lấy từ `studentSlice` (đã có sẵn, không gọi thêm API).

**Handler (≤40 dòng — NFR-VOC-08), phân biệt lỗi quyền (§A.3/§11):**
```js
const fetchPath = useCallback(async () => {
  setLoading(true); setError('');
  try {
    setCards(await getVocabPath(level));
  } catch (err) {
    const code = err?.response?.status;
    if (code === 401) return navigate('/login');
    if (code === 403 || code === 422) {
      setCards([]);
      setError('Cấp độ này chưa mở khóa hoặc cần nâng cấp tài khoản.'); // backend quyết định, FE chỉ hiển thị
    } else {
      setError(err?.response?.data?.message ?? 'Không thể tải lộ trình từ vựng.');
    }
  } finally { setLoading(false); }
}, [level, navigate]);

useEffect(() => { fetchPath(); }, [fetchPath]);
```

---

## 11. API

### 11.1 Path cards (trang này)
```
GET /api/vocabulary/path?level=N5        (route kebab-case số nhiều — AGENTS §3.3)
Response (envelope chuẩn AGENTS §6):
{
  "status": 200,
  "message": "OK",
  "data": [
    {
      "topicId": 12, "slug": "food",
      "titleJa": "食べ物", "titleVi": "Đồ ăn",
      "order": 1, "level": "N5",
      "totalWords": 24, "completedWords": 0,
      "status": "active"            // active | available | completed | locked  ← BACKEND quyết định (§A.3)
    },
    { "topicId": 13, "slug": "school", "titleJa": "学校", "titleVi": "Trường học",
      "order": 2, "level": "N5", "totalWords": 18, "completedWords": 0, "status": "available" }
  ]
}
```

> `status`, `completedWords`, và việc level/VIP có được truy cập hay không đều do **backend** tính (Role + subscription/level, Domain Rule 7.2/7.3). Frontend không suy luận lại — xem §A.3.

**Mã lỗi cần handle:**

| Code | Ý nghĩa | Frontend |
|:---|:---|:---|
| 200 | OK | Render path |
| 401 | Token thiếu/hết hạn | Điều hướng `/login` |
| 403 | Không có quyền level/VIP | Thông báo "Cấp độ này chưa mở khóa / cần nâng cấp", không render path |
| 422 | Level không hợp lệ với student | Như 403 |
| 5xx | Lỗi hệ thống | Error inline + nút "Thử lại" |

Service (`api/studentService.js` — không `fetch` inline trong component, NFR-VOC-09):
```js
export async function getVocabPath(level) {
  const res = await api.get('/vocabulary/path', { params: { level } });
  return res.data.data;   // bóc envelope { status, message, data }
}
```

### 11.2 Topic detail (màn kế tiếp — OUT OF SCOPE spec này)
Click path card → `/vocabulary?topic={slug}&level={level}`. Màn này tái sử dụng **danh sách từ phẳng của v1.0** (word card: reading/audio/+FC/đánh dấu đã học) qua các API đã có:
`GET /api/vocabulary?level=&topic=&search=&page=&size=`, `GET /api/vocabulary/topics`,
`POST /api/vocabulary/{id}/complete`, `POST /api/flashcard/add`.
→ Sẽ tách thành `SPEC-vocabulary-topic.md` riêng.

---

## 12. LOADING SKELETON

| Vùng | Skeleton |
|:---|:---|
| Left | 1 block 220×220, `--radius-xl` |
| Center | Title pill block 220×56 `--radius-full` + 4 path block 76px `--radius-lg` |
| Right | 1 block 68px `--radius-lg` |

```css
@keyframes skeleton-pulse { 0%,100%{opacity:1;} 50%{opacity:.45;} }
.skeleton { background: var(--color-border); border-radius: var(--radius-md);
            animation: skeleton-pulse 1.4s ease-in-out infinite; }
```
Skeleton có `aria-hidden="true"`; `<main>` set `aria-busy="true"` khi loading.

---

## 13. EMPTY STATE

Khi `cards.length === 0` (cấp độ chưa có chủ đề):
```jsx
<EmptyState
  mascotVariant="thinking" mascotSize={160}
  title="Chưa có chủ đề từ vựng"
  subtitle="Nội dung cấp độ này đang được cập nhật. Hãy quay lại sau nhé!"
/>
```
**Không bao giờ** render trang trắng (DESIGN.md `empty-state`).

---

## 14. ANIMATIONS

```css
/* Path card lần lượt trượt lên */
@keyframes slideUp { from{opacity:0; transform:translateY(12px);} to{opacity:1; transform:translateY(0);} }
.voc-path-card { animation: slideUp 250ms ease forwards; }
.voc-path-card:nth-child(1){animation-delay:0ms;}
.voc-path-card:nth-child(2){animation-delay:60ms;}
.voc-path-card:nth-child(3){animation-delay:120ms;}
.voc-path-card:nth-child(4){animation-delay:180ms;}

@media (prefers-reduced-motion: reduce) {
  .voc-page * { animation: none !important; transition-duration: 0ms !important; }
}
```
Saku-chan & flame animation kế thừa từ StreakCard.

---

## 15. RESPONSIVE

> Prompt §3 — sidebar trái/phải ẩn hoặc đẩy xuống dưới trên mobile; TopNav center → hamburger / scroll ngang.

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 1200px | Full 3 cột: left 220px + center flex + right 200px |
| 768–1199px | Ẩn `.voc-left`; còn center + right |
| < 768px | 1 cột; ẩn `.voc-right`; TopNav thu gọn hamburger; Title pill `padding: 0 24px`, font 18px |

```css
@media (max-width: 1199px) {
  .voc-left { display: none; }
  .voc-body { padding: 20px; }
}
@media (max-width: 767px) {
  .voc-body  { flex-direction: column; padding: 16px; gap: 16px; }
  .voc-right { display: none; }
  .voc-title-pill { padding: 0 24px; min-height: 48px; font-size: 18px; }
  .voc-title-blob { display: none; }
  .voc-path-card--active { padding: 18px 16px; }
}
```
Touch target mọi card/pill ≥ 44px (DESIGN.md §Touch Targets).

---

## 16. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Landmark | `<aside>` (2 sidebar), `<main>` (center), TopNav `<header>` |
| Heading | `<h1>` = Title pill "{level} Kanji & Vocab" |
| Level selector | `role="tablist"` + `role="tab"` + `aria-selected` trên `.voc-level-pill` |
| Path card | `<button>`/`<a>` bao card; `aria-label="Bài {order}: {titleVi}"`; active → `aria-current="true"` |
| Locked card | `aria-disabled="true"` + `aria-label="Bị khóa — hoàn thành bài trước"` |
| START HERE tag | `aria-hidden="true"` (trang trí); thông tin "bắt đầu ở đây" đã có ở `aria-current` |
| Course List | `aria-label="Mở danh sách khóa học"` |
| Loading | `aria-busy="true"` trên `<main>`; skeleton `aria-hidden="true"` |
| Focus ring | `outline: 2px solid var(--color-primary)` mọi phần tử tương tác |
| Reduced motion | Tắt animation khi `prefers-reduced-motion: reduce` |
| Ký tự Nhật | `lang="ja"` trên từ/tiêu đề tiếng Nhật để fallback Noto Sans JP |

---

## 17. FILE STRUCTURE / COMPONENT

```
apps/frontend/src/
├── pages/vocabulary/
│   ├── VocabularyList.jsx        ← page root (Vocab Learning Hub 3 cột)
│   └── VocabularyList.css
├── components/student/
│   └── VocabPathCard.jsx         ← MỚI — card lộ trình (prop active/locked)
├── components/layout/TopNav.jsx  ← dùng lại
├── pages/dashboard/StreakCard.jsx← dùng lại
└── components/common/
    ├── Badges.jsx (JlptBadge)    ← dùng lại
    └── EmptyState.jsx            ← dùng lại
```

**Skeleton JSX page root:**
```jsx
<div className="voc-page">
  <TopNav activeTab="vocabulary" />
  <div className="voc-body">
    <aside className="voc-left">
      {isLoading ? <div className="skeleton skeleton--streak" />
                 : <StreakCard streak={streak} weekDays={weekDays} />}
    </aside>

    <main className="voc-center" aria-busy={isLoading}>
      <div className="voc-levels" role="tablist" aria-label="Chọn cấp độ JLPT">
        {LEVELS.map((l) => (
          <button key={l} role="tab" aria-selected={level === l}
            className={`voc-level-pill${level === l ? ' voc-level-pill--active' : ''}`}
            onClick={() => setLevel(l)}>{l}</button>
        ))}
      </div>

      <div className="voc-titlewrap">
        <span className="voc-title-blob voc-title-blob--left" aria-hidden="true" />
        <span className="voc-title-blob voc-title-blob--right" aria-hidden="true" />
        <h1 className="voc-title-pill"><span className="voc-title-lv">{level}</span> Kanji &amp; Vocab</h1>
      </div>

      {error ? (
        <div className="voc-error" role="alert">{error}
          <button className="voc-retry" onClick={fetchPath}>Thử lại</button></div>
      ) : isLoading ? (
        <div className="voc-path-list">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="skeleton skeleton--path" aria-hidden="true" />))}
        </div>
      ) : cards.length === 0 ? (
        <EmptyState mascotVariant="thinking" mascotSize={160}
          title="Chưa có chủ đề từ vựng"
          subtitle="Nội dung cấp độ này đang được cập nhật. Hãy quay lại sau nhé!" />
      ) : (
        <div className="voc-path-list">
          {cards.map((c) => (
            <VocabPathCard key={c.topicId} card={c}
              active={c.status === 'active'} onOpen={openTopic} />))}
        </div>
      )}
    </main>

    <aside className="voc-right">
      <a className="voc-courselist-card" href="/courses" aria-label="Mở danh sách khóa học">
        <span className="voc-cl-icon" aria-hidden="true">{/* 📚 SVG */}</span>
        <span className="voc-cl-label">Danh sách khóa học</span>
        <span className="voc-cl-arrow" aria-hidden="true">{/* → SVG */}</span>
      </a>
    </aside>
  </div>
</div>
```

**VocabPathCard.jsx (khung):**
```jsx
export default function VocabPathCard({ card, active, onOpen }) {
  const locked = card.status === 'locked';
  const cls = `voc-path-card${active ? ' voc-path-card--active' : ''}${locked ? ' voc-path-card--locked' : ''}`;
  return (
    <button type="button" className={cls}
      aria-current={active ? 'true' : undefined}
      aria-disabled={locked || undefined}
      aria-label={`Bài ${card.order}: ${card.titleVi}`}
      onClick={() => !locked && onOpen(card)}>
      {active && <span className="voc-start-tag" aria-hidden="true">START HERE</span>}
      <span className="voc-pc-avatar" lang="ja" aria-hidden="true">
        {locked ? '🔒' : card.titleJa.charAt(0)}
      </span>
      <span className="voc-pc-body">
        <span className="voc-pc-title" lang="ja">{card.titleJa}</span>
        <span className="voc-pc-sub">
          Bài {card.order} · {card.titleVi} · {card.completedWords}/{card.totalWords} từ
        </span>
      </span>
      <span className="voc-pc-meta">
        <JlptBadge level={card.level} />
        <span className="voc-pc-arrow" aria-hidden="true">{/* → SVG */}</span>
      </span>
    </button>
  );
}
```

---

## 18. OUT OF SCOPE

- ❌ Màn **topic detail / word list** (reading, audio, +Flashcard, đánh dấu đã học) → `SPEC-vocabulary-topic.md` riêng (tái dùng API v1.0, §11.2).
- ❌ Backend endpoint `GET /api/vocabulary/path` (định nghĩa ở backend spec).
- ❌ Mobile hamburger menu implementation (spec TopNav riêng).
- ❌ Search/filter chủ đề (đẩy về topic detail).
- ❌ Dark mode.
```