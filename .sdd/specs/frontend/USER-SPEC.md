# USER-SPEC — Hướng dẫn tạo trang Student (Frontend)
>
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-02
> **Mục đích:** Đọc tài liệu này là đủ để tạo bất kỳ trang student mới nào mà không cần hỏi thêm.
> **Design ref:** `DESIGN.md` | **Admin ref:** `MASTER-SPEC.md` | **Flow ref:** `FRONTEND-FLOW.md`
> **Backend refs:** `feat-core-learning`, `feat-flashcard-srs`, `feat-assessment`, `feat-learning-analytics`

---

## 0. QUY TẮC SỐ 1

> **Tận dụng component có sẵn trước. Tạo mới chỉ khi thật sự không có.**

Kiểm tra thứ tự: `components/student/` → `components/common/` → `components/auth/` → `components/layout/`.

> **Redux trước, local state sau.** Dữ liệu đã có trong Redux (`authSlice`, `studentSlice`) thì không fetch lại qua local state.

---

## 1. CẤU TRÚC FILE

### 1.1 Quy tắc đặt tên

| Loại | Convention | Ví dụ |
|:---|:---|:---|
| Page file | `PascalCase.jsx` + cùng tên `.css` | `LessonDetail.jsx` |
| Component | `PascalCase.jsx`, không có `.css` riêng trừ khi tự dùng | `FlashcardCard.jsx` |
| CSS class | `[prefix]-[block]` hoặc `[prefix]-[block]__[element]` | `.lsn-card`, `.lsn-card__title` |
| Prefix | 3 chữ cái viết tắt của page | `lsn-` cho Lesson, `rev-` cho Review |
| API function | `camelCase` trong `studentService.js` | `getLessonDetail` |

### 1.2 Vị trí file

```
apps/frontend/src/
├── pages/
│   ├── onboarding/
│   │   └── Onboarding.jsx + Onboarding.css
│   ├── lessons/
│   │   └── LessonDetail.jsx + LessonDetail.css
│   ├── learn/
│   │   └── LearnNew.jsx + LearnNew.css
│   ├── review/
│   │   └── Review.jsx + Review.css
│   ├── flashcard/
│   │   └── Flashcard.jsx + Flashcard.css
│   ├── mock-test/
│   │   ├── MockTestList.jsx + MockTestList.css
│   │   ├── MockTestAttempt.jsx + MockTestAttempt.css
│   │   └── MockTestResults.jsx + MockTestResults.css
│   ├── progress/
│   │   └── Progress.jsx + Progress.css
│   ├── profile/
│   │   └── Profile.jsx + Profile.css
│   ├── settings/
│   │   └── ChangePassword.jsx + ChangePassword.css
│   ├── subscription/
│   │   ├── Subscription.jsx + Subscription.css
│   │   └── SubscriptionSuccess.jsx + SubscriptionSuccess.css
│   ├── kanji/
│   │   ├── KanjiList.jsx + KanjiList.css
│   │   └── KanjiPractice.jsx + KanjiPractice.css
│   ├── certificates/
│   │   └── Certificates.jsx + Certificates.css
│   └── error/
│       ├── NotFound.jsx + NotFound.css   ← /404
│       └── Forbidden.jsx                 ← /403 (dùng lại NotFound.css)
│
├── components/student/
│   ├── (đã có) StreakCard.jsx, HeroBanner.jsx, LessonCard.jsx, ...
│   ├── FlashcardCard.jsx         ← thẻ flashcard lật được
│   ├── KanjiGrid.jsx             ← lưới kanji với JLPT filter
│   ├── ExamQuestionPanel.jsx     ← panel câu hỏi thi
│   ├── ExamNavigator.jsx         ← bảng nhảy câu
│   ├── SkillRadarChart.jsx       ← biểu đồ radar năng lực
│   ├── ProgressHistoryRow.jsx    ← hàng lịch sử bài thi
│   ├── VipLockOverlay.jsx        ← overlay khóa VIP
│   └── SubscriptionPlanCard.jsx  ← card gói đăng ký
│
└── api/
    └── studentService.js  ← thêm function mới vào đây, không tạo file mới
```

### 1.3 Quy tắc tách component

Giống MASTER-SPEC §1.3. Thêm quy tắc đặc thù student:

- **Exam mode** (`/mock-test/:id/attempt`): page tự quản lý toàn bộ, KHÔNG dùng `TopNav`.
- **Flashcard session** (`/review`, `/flashcard`): dùng local state cho phiên ôn tập, không push vào Redux.
- **OCR result** (`/kanji/:id`): poll kết quả bằng `useRef` interval, cleanup khi unmount.

---

## 2. GIẢI PHẪU MỘT TRANG STUDENT

### 2.1 Layout chuẩn (có TopNav)

```
┌──────────────────────────────────────────────┐
│  TopNav (sticky 64px, white)                 │  activeTab="[tab-id]"
├──────────────────────────────────────────────┤
│  <main className="[prefix]-body">            │
│    max-width: 1200px, margin: auto           │
│    padding: 28px 32px 48px                   │
│    [Nội dung trang]                          │
└──────────────────────────────────────────────┘
```

> **Khác với admin:** Không có `AdminPageHeader`. Student pages đặt tiêu đề trang trong `<h1>` bên trong `<main>`.

### 2.2 Layout exam mode (không có TopNav)

```
┌──────────────────────────────────────────────┐
│  ExamTopBar (64px): Logo | Timer | Nộp bài   │
├──────────────────────────────────────────────┤
│  ExamProgressBar (4px, sticky below topbar)  │
├──────────────────────────────────────────────┤
│  ExamQuestionPanel     │  ExamNavigator      │
│  (flex: 1)             │  (280px, sticky)    │
└──────────────────────────────────────────────┘
```

### 2.3 Template JSX chuẩn (trang có TopNav)

```jsx
import TopNav                    from '../../components/layout/TopNav';
import { EmptyState }            from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import './MyPage.css';

export default function MyPage() {
  const [data,      setData]    = useState(null);
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');
  const { toasts, addToast, removeToast } = useToast();

  // Dữ liệu từ Redux (không fetch lại nếu đã có)
  const { user } = useAppSelector((state) => state.auth);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const result = await apiCall();
      setData(result);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải dữ liệu.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  return (
    <div className="myp-page">
      <TopNav activeTab="[tab-id]" />

      <main className="myp-body" aria-busy={isLoading}>
        <h1 className="myp-title">Tiêu Đề Trang</h1>

        {error && (
          <div className="myp-error-banner" role="alert">
            <span>{error}</span>
            <button className="myp-retry-btn" onClick={fetchData}>Thử lại</button>
          </div>
        )}

        {isLoading && <div className="myp-skel" aria-hidden="true" />}

        {!isLoading && !error && !data && (
          <EmptyState
            title="Chưa có dữ liệu"
            subtitle="Mô tả gợi ý hành động."
            mascotVariant="thinking"
            mascotSize={120}
          />
        )}

        {!isLoading && !error && data && (
          /* Nội dung thực */
          <MyComponent data={data} addToast={addToast} />
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

### 2.4 Template CSS chuẩn

```css
/* ===== [TênTrang] (SakuJi Hanami Theme) ===== */

.myp-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  font-family: var(--font-base);
}

.myp-body {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 32px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-sizing: border-box;
}

.myp-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 4px;
}

@media (max-width: 1199px) { .myp-body { padding: 24px 20px 40px; } }
@media (max-width: 767px)  { .myp-body { padding: 16px 16px 32px; } }

@media (prefers-reduced-motion: reduce) {
  .myp-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 3. DESIGN TOKENS

Giống MASTER-SPEC §3 — dùng cùng CSS variables. Xem `DESIGN.md` hoặc `MASTER-SPEC.md §3`.

**Bổ sung cho student pages:**

```css
/* VIP badge */
--color-vip:     #F7C948   /* gold accent */
--color-vip-bg:  #FFF7DD   /* gold tint */
```

---

## 4. COMPONENT INVENTORY

### 4.1 Layout

| Component | Import | Props chính |
|:---|:---|:---|
| `TopNav` | `components/layout/TopNav` | `activeTab: string` |

**activeTab values** (phải khớp với `NAV_TABS` trong `TopNav.jsx`):

```
''            → /dashboard (không active tab nào)
'review'      → /review
'learn'       → /learn/new
'kanji'       → /kanji
'grammar'     → /grammar
'dictionary'  → /dictionary
'mock-test'   → /mock-test
```

### 4.2 Student Components (có sẵn)

| Component | Import | Dùng ở đâu |
|:---|:---|:---|
| `StreakCard` | `components/student/StreakCard` | Dashboard, Progress |
| `HeroBanner` | `components/student/HeroBanner` | Dashboard |
| `LessonCard` | `components/student/LessonCard` | Dashboard, LearnNew |
| `LessonList` | `components/student/LessonList` | Dashboard, LearnNew |
| `QuickActionCard` | `components/student/QuickActionCard` | Dashboard |
| `MiniStatCard` | `components/student/MiniStatCard` | Dashboard, Progress |

### 4.3 Common Components (tái dùng)

| Component | Import | Props |
|:---|:---|:---|
| `EmptyState` | `components/common/EmptyState` | `title`, `subtitle`, `mascotVariant`, `mascotSize`, `children` |
| `Pagination` | `components/common/Pagination` | `currentPage` (1-based), `totalPages`, `onChange(page)` |
| `ProgressBar` | `components/common/ProgressBar` | `value` (0–100), `variant` |
| `StatusBadge` | `components/common/Badges` | `status` |
| `JlptBadge` | `components/common/Badges` | `level: 'N5'\|'N4'\|'N3'\|'N2'\|'N1'` |
| `UserAvatar` | `components/common/UserAvatar` | `src`, `name`, `size` |
| `useToast` + `ToastContainer` | `components/common/Toast` | — |

### 4.4 VIP Lock Pattern

Dùng ở mọi nơi có `is_vip_only`:

```jsx
{lesson.isVipOnly && !user.isVip && (
  <div className="vip-overlay" aria-label="Nội dung VIP">
    <span className="vip-lock-icon" aria-hidden="true">⭐</span>
    <span className="vip-lock-text">VIP</span>
    <button
      className="vip-upgrade-btn"
      onClick={() => navigate('/subscription')}
    >
      Nâng cấp
    </button>
  </div>
)}
```

```css
.vip-overlay {
  position: absolute; inset: 0;
  background: rgba(255, 247, 221, 0.90);
  border-radius: inherit;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  backdrop-filter: blur(2px);
  flex-direction: column;
}
.vip-lock-icon  { font-size: 24px; }
.vip-lock-text  { font-size: 12px; font-weight: 700; color: #B7670A; }
.vip-upgrade-btn {
  background: var(--color-accent);
  color: #7A4A00;
  border: none;
  border-radius: var(--radius-full);
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
```

---

## 5. STATE MANAGEMENT PATTERN

### 5.1 Redux vs Local State

```
Redux (studentSlice / authSlice):          Local useState:
├── user profile                           ├── form fields
├── streak, wordCount, daysThisMonth       ├── isLoading, error
├── course + lesson list (dashboard)       ├── currentPage
└── (future) flashcard due count           ├── flashcard session queue
                                           ├── exam answers (+ localStorage backup)
                                           └── OCR job polling state
```

### 5.2 Data fetching chuẩn (xem MASTER-SPEC §5.1)

Giống admin, nhưng import từ `studentService.js`:

```js
import { getLessonDetail } from '../../api/studentService';
```

### 5.3 Exam draft — localStorage backup

```js
const DRAFT_KEY = `exam_draft_${examId}`;

// Lưu mỗi lần chọn đáp án
localStorage.setItem(DRAFT_KEY, JSON.stringify(answers));

// Restore khi mount
const saved = localStorage.getItem(DRAFT_KEY);
if (saved) setAnswers(JSON.parse(saved));

// Xóa sau khi nộp bài thành công
localStorage.removeItem(DRAFT_KEY);
```

### 5.4 OCR Polling

```js
const pollRef = useRef(null);

async function startOcrPoll(jobId) {
  let retries = 0;
  pollRef.current = setInterval(async () => {
    retries++;
    if (retries > 30) {       // 30s timeout
      clearInterval(pollRef.current);
      setOcrError('Không thể phân tích. Vui lòng thử lại.');
      return;
    }
    const result = await getOcrResult(jobId);
    if (result.status === 'COMPLETED') {
      clearInterval(pollRef.current);
      setOcrResult(result);
    }
  }, 1000);
}

useEffect(() => () => clearInterval(pollRef.current), []);
```

---

## 6. LOADING / ERROR / EMPTY — 3 TRẠNG THÁI BẮT BUỘC

Xem MASTER-SPEC §6 — dùng lại hoàn toàn.

**Thêm cho student pages:**

### 6.1 Flashcard queue rỗng (trạng thái đặc biệt)

```jsx
{!isLoading && dueCards.length === 0 && (
  <EmptyState
    title="Tuyệt vời! Hết thẻ hôm nay rồi 🎉"
    subtitle={`Lần ôn tập tiếp theo: ${nextReviewDate}`}
    mascotVariant="celebrate"
    mascotSize={160}
  >
    <button className="rev-btn rev-btn--primary" onClick={() => navigate('/dashboard')}>
      Về Dashboard
    </button>
  </EmptyState>
)}
```

### 6.2 VIP expired mid-session modal

```jsx
{vipExpired && (
  <div className="vip-expired-modal-backdrop" role="dialog" aria-modal="true" aria-label="VIP hết hạn">
    <div className="vip-expired-modal">
      <span className="vip-expired-icon" aria-hidden="true">⭐</span>
      <h2>Gói VIP đã hết hạn</h2>
      <p>Gia hạn để tiếp tục học nội dung nâng cao.</p>
      <button className="vip-btn--renew" onClick={() => navigate('/subscription')}>
        Gia hạn ngay
      </button>
      <button className="vip-btn--later" onClick={() => navigate('/dashboard')}>
        Để sau
      </button>
    </div>
  </div>
)}
```

---

## 7. FORM PATTERN

Xem MASTER-SPEC §7 — dùng lại hoàn toàn.
Thay prefix `myp-` bằng prefix của trang (ví dụ `prf-` cho Profile).

---

## 8. DOMAIN RULES — CHECKLIST FRONTEND

> Nguồn: `AGENTS.md §7` — **Bắt buộc kiểm tra trước khi coi trang là xong.**

| Rule | Áp dụng ở | Frontend phải làm |
|:---|:---|:---|
| Score chỉ từ backend | `/mock-test/:id/attempt`, `/mock-test/:id/results` | Chỉ gửi `{ questionId, selectedOption }[]`, không gửi score |
| QuizAttempt bất biến | `/mock-test/:id/results` | Không có nút "Sửa kết quả" |
| Timer server-side | `/mock-test/:id/attempt` | Client timer chỉ hiển thị; submit khi timeout từ server |
| `user_progress` chỉ tăng | `/lessons/:id`, `/learn/new` | Không cho phép giảm `progressPercent` |
| Lesson locked nếu chưa xong bài trước | `/lessons/:id`, `/dashboard` | Hiển thị icon khóa, không navigate |
| `is_vip_only` check real-time | Mọi trang có nội dung VIP | Gọi API để check, không dựa vào cache quá 5 phút |
| AI async: timeout 30s, retry 3 lần | `/kanji/:id` | Polling với interval 1s, maxRetry 30 |
| AI score = `ai_score_suggestion` (không phải final) | `/kanji/:id` | Chỉ hiển thị %, không cho edit |
| File ảnh OCR → /uploads, không BLOB | `/kanji/:id` | Upload file, không encode base64 vào request body |
| Subscription hết hạn → modal | Mọi trang VIP | Bắt 403 `VIP_EXPIRED` → show modal gia hạn |

---

## 9. PAGE SPECS — SPRINT 1: FOUNDATION

### 9.1 `/onboarding` — Thiết lập lần đầu

**Trigger:** Redirect từ `/login` khi `user.onboardingCompleted === false`
**Guard:** STUDENT role, redirect `/dashboard` nếu onboarding đã xong
**Prefix:** `onb-` | **activeTab:** `''` | **Mascot:** `'happy'`

**Layout:** Card trung tâm (max-width 520px), 3 bước dạng stepper.

```
Bước 1: Chọn JLPT level mục tiêu (N5/N4/N3/N2/N1) — button group
Bước 2: Thời gian học mỗi ngày (5/10/15/20 phút) — radio pills
Bước 3: Kỹ năng ưu tiên (Kanji / Vocab / Grammar / Tất cả) — multi-select chips
                                               ↓
                                    [Bắt đầu học →]
```

**State:**

```js
const [step,           setStep]    = useState(1);   // 1|2|3
const [jlptGoal,       setJlpt]    = useState('N5');
const [dailyMinutes,   setMinutes] = useState(10);
const [focusSkills,    setSkills]  = useState([]);
const [isSubmitting,   setSubmit]  = useState(false);
```

**API:** `POST /api/students/onboarding` → `{ jlptGoal, dailyMinutes, focusSkills[] }`

- Thành công → `navigate('/dashboard')`
- Lỗi → toast `error`

**Không có** loading/empty state (trang tĩnh). Chỉ cần spinner trên button submit.

---

### 9.2 `/profile` — Hồ sơ cá nhân

**Prefix:** `prf-` | **activeTab:** `''`

**Layout:** 2 cột (avatar sidebar 240px | form chính)

```
[Avatar + upload button]    [Form: fullName, phone, dateOfBirth, bio]
[Tên hiển thị]              [Trường read-only: email, jlptLevel]
[Email (read-only)]         [Nút: Lưu thay đổi]
[Level hiện tại]
                            ↓ link riêng
                            [Đổi mật khẩu →]
```

**State:**

```js
const { user } = useAppSelector((s) => s.auth);   // từ Redux
const [form,        setForm]    = useState({ fullName: '', phone: '', dateOfBirth: '', bio: '' });
const [isSaving,    setSaving]  = useState(false);
const [avatarFile,  setAvatar]  = useState(null);
const [avatarPreview, setPreview] = useState(null);
```

**API:**

- `GET /api/students/me` — prefill form (dùng dữ liệu Redux nếu đủ)
- `PUT /api/students/me` — save
- `POST /api/students/me/avatar` — multipart upload, nhận `avatarUrl`

**Validation (client):**

- `fullName`: 2–100 ký tự
- `phone`: regex `^\d{9,15}$` hoặc rỗng
- `bio`: tối đa 500 ký tự

**Read-only fields:** `email`, `jlptLevel`, `role`

---

### 9.3 `/settings/password` — Đổi mật khẩu

**Prefix:** `pwd-` | **activeTab:** `''`

**Layout:** Card trung tâm, max-width 420px.

```
[Mật khẩu hiện tại   ] [👁]
[Mật khẩu mới        ] [👁] + PasswordStrengthBar
[Xác nhận mật khẩu   ] [👁]
[Đổi mật khẩu]
```

Tái dùng `EyeIcon` và `PasswordStrengthBar` từ `components/auth/`.

**State:** form fields + `isSaving`

**API:** `POST /api/auth/change-password` → `{ currentPassword, newPassword }`

- 200 → toast success + `dispatch(logoutThunk())` + `navigate('/login')`
- 401 → "Mật khẩu hiện tại không đúng"

**Validation:** `newPassword` ≥ 8 ký tự, có chữ hoa, chữ thường, số.

---

### 9.4 `/verify-email` — Xác nhận email

**Prefix:** `vem-` | **activeTab:** `''` | **Không cần auth**

**Layout:** Trang trung tâm, single card 480px.

**Logic:**

```js
const [token] = useSearchParams();   // ?token=xxx
const [state, setState] = useState('verifying'); // 'verifying'|'success'|'error'|'expired'

useEffect(() => {
  verifyEmailToken(token).then(() => setState('success'))
    .catch((err) => {
      if (err.code === 'TOKEN_EXPIRED') setState('expired');
      else setState('error');
    });
}, [token]);
```

**4 states:**

- `verifying`: spinner + "Đang xác nhận tài khoản..."
- `success`: mascot `'celebrate'` + "Tài khoản đã xác nhận!" + CTA "Đăng nhập"
- `expired`: mascot `'thinking'` + "Link đã hết hạn" + button "Gửi lại email"
- `error`: mascot `'wrong'` + "Link không hợp lệ" + link /register

---

### 9.5 `/403` và `/404` — Trang lỗi

**Prefix:** `err-` | Không cần auth

**Dùng chung 1 CSS file:** `error/Error.css`

```jsx
// NotFound.jsx
export default function NotFound() {
  return (
    <div className="err-page">
      <AppLogo />
      <EmptyState
        title="404 — Không tìm thấy trang"
        subtitle="Trang bạn tìm không tồn tại hoặc đã bị di chuyển."
        mascotVariant="thinking"
        mascotSize={160}
      >
        <button className="err-btn" onClick={() => navigate(-1)}>← Quay lại</button>
        <Link className="err-link" to="/dashboard">Về Dashboard</Link>
      </EmptyState>
    </div>
  );
}

// Forbidden.jsx — tương tự, đổi text thành "403 — Không có quyền truy cập"
```

---

## 10. PAGE SPECS — SPRINT 2: CORE LEARNING LOOP

### 10.1 `/lessons/:id` — Chi tiết bài học

**Prefix:** `lsn-` | **activeTab:** `'learn'`

**Layout:**

```
[Breadcrumb: Dashboard > Khoá học > Tên bài]

[LessonHeader: title, jlptLevel badge, progressBar 0-100%]

[Tabs: Nội dung | Từ vựng | Ngữ pháp | Luyện tập]

[Tab content area]

[Footer: ← Bài trước | Đánh dấu hoàn thành | Bài tiếp theo →]
```

**State:**

```js
const { id }          = useParams();
const [lesson,        setLesson]  = useState(null);
const [activeTab,     setTab]     = useState('content');
const [isCompleting,  setComplete]= useState(false);
const [isLoading,     setLoading] = useState(true);
const [error,         setError]   = useState('');
```

**API:**

- `GET /api/lessons/:id` — lấy nội dung bài học
- `POST /api/learning-progress` — `{ contentType: 'lesson', contentId: id, status: 'completed', progressPercent: 100 }`
- `POST /api/flashcards` — khi click "Thêm vào Flashcard" trên từ vựng/kanji

**VIP check:** Nếu `lesson.isVipOnly && !user.isVip` → redirect `/subscription`.

**Domain rules:**

- `progressPercent` chỉ tăng — không gửi giảm.
- Lesson locked (bài trước chưa xong) → hiển thị overlay lock, disable footer buttons.

**Responsive:** Tab content scroll ngang trên mobile.

---

### 10.2 `/learn/new` — Học từ mới (Next lesson)

**Prefix:** `lnw-` | **activeTab:** `'learn'`

**Logic:** Lấy bài học tiếp theo chưa hoàn thành trong JLPT level của user. Nếu không có → EmptyState "Hoàn thành khoá học!".

**Layout:**

```
[PageHeader: "Học Từ Mới" + jlptLevel badge]

[NextLessonCard: title, description, estimatedTime, progressPercent]
  → click → navigate(`/lessons/${lesson.id}`)

[Divider "Bài học gợi ý"]

[LessonList: 3 bài tiếp theo chưa học]
```

**API:** `GET /api/students/next-lesson` → `{ lesson, suggestedLessons[] }`

**Empty state:** mascot `'celebrate'`, "Bạn đã học hết bài của level này!" + CTA "Lên level tiếp theo".

---

### 10.3 `/review` — Ôn tập Flashcard SRS (Daily Queue)

**Prefix:** `rev-` | **activeTab:** `'review'`

**Layout:** Trang tập trung, không có sidebar.

```
[Header: "Ôn tập hôm nay" | x thẻ còn lại | X% hoàn thành]
[ProgressBar tổng queue]

[FlashcardCard — hiển thị mặt trước]
  ↓ click "Lật thẻ"
[FlashcardCard — mặt sau: nghĩa, ví dụ, audio]
  ↓ 3 nút đánh giá
[Không nhớ] [Khó] [Dễ]
```

**State:**

```js
const [queue,       setQueue]   = useState([]);   // flashcard[]
const [currentIdx,  setIdx]     = useState(0);
const [isFlipped,   setFlipped] = useState(false);
const [isLoading,   setLoading] = useState(true);
const [isSubmitting,setSubmit]  = useState(false);
const [done,        setDone]    = useState(false);
```

**API:**

- `GET /api/flashcards?dueOnly=true&size=50` — lấy queue
- `GET /api/flashcards/:id/reveal` — khi flip
- `POST /api/flashcards/:id/review` → `{ rating: 'easy'|'hard'|'wrong' }`

**Flip animation:**

```css
.rev-card-inner {
  transition: transform 0.4s ease;
  transform-style: preserve-3d;
}
.rev-card-inner--flipped { transform: rotateY(180deg); }
.rev-card-front, .rev-card-back { backface-visibility: hidden; }
.rev-card-back { transform: rotateY(180deg); }
```

**Empty state (done):** mascot `'celebrate'`, nextReviewDate. Xem §6.1.
**Empty state (không có thẻ):** mascot `'idle'`, "Chưa có thẻ nào để ôn tập." + CTA "/flashcard".

**Domain rules:** Đánh giá `wrong` → reset interval về 1 ngày (backend xử lý, frontend chỉ gọi API).

---

### 10.4 `/flashcard` — Quản lý bộ thẻ (Flashcard Browser)

**Prefix:** `fls-` | **activeTab:** `''`

**Layout:**

```
[Header: "Bộ Thẻ Của Tôi" | [+ Tạo bộ thẻ mới]]

[DeckList]
  DeckCard: tên, số thẻ, dueToday badge, [Ôn tập] [Xem thẻ]
  DeckCard: ...

[Khi chọn deck → panel xem tất cả thẻ trong deck]
```

**State:**

```js
const [decks,       setDecks]   = useState([]);
const [activeDecks, setActive]  = useState(null);
const [deckCards,   setCards]   = useState([]);
const [showCreate,  setCreate]  = useState(false);
const [newDeckName, setName]    = useState('');
const [isLoading,   setLoading] = useState(true);
```

**API:**

- `GET /api/flashcard-decks`
- `GET /api/flashcards?deckName={name}`
- `POST /api/flashcard-decks` → `{ deckName }`
- `DELETE /api/flashcard-decks/:name` (chỉ cho deck không phải system)

**Empty state (không có deck):** mascot `'thinking'`, "Chưa có bộ thẻ nào." + CTA "Thêm từ bài học".

---

## 11. PAGE SPECS — SPRINT 3: ASSESSMENT LOOP

### 11.1 `/mock-test` — Chọn đề thi

**Prefix:** `mkt-` | **activeTab:** `'mock-test'`

**Layout:**

```
[Header: "Thi Thử JLPT"]

[JLPT Level Filter: N5 | N4 | N3 | N2 | N1]

[ExamList]
  ExamCard: title, jlptLevel badge, questionCount, durationMin, passScore, lastAttempt
  [Bắt đầu thi] → navigate(`/mock-test/${id}/attempt`)
```

**API:** `GET /api/assessments?type=exam&level={level}&page=0&size=10`

**Empty state:** mascot `'thinking'`, "Chưa có đề thi cho level này."

---

### 11.2 `/mock-test/:id/attempt` — Làm bài thi (EXAM MODE)

**Prefix:** `mxa-` | **Không dùng TopNav** — dùng `ExamTopBar` riêng.

**Layout đặc biệt (fullscreen):**

```
┌─────────────────────────────────────────────────────────────────────┐
│  ExamTopBar: [Logo SakuJi]  [⏱ 89:23]  [Nộp bài (primary)]        │
├─────────────────────────────────────────────────────────────────────┤
│  ProgressBar: (23/100 câu)                                          │
├──────────────────────────────────────────────┬──────────────────────┤
│  QuestionPanel (flex:1)                      │  Navigator (280px)   │
│  [QuestionNumber: Câu 23/100]                │  [grid 10 cột]       │
│  [SectionBadge: Từ vựng]                     │  answered = pink     │
│  [QuestionText]                              │  current  = bordered │
│  ○ A. Option A                               │  empty    = ghost    │
│  ○ B. Option B                               │                      │
│  ○ C. Option C                               │  [Nộp bài →]        │
│  ○ D. Option D                               │                      │
│  [← Câu trước]  [Câu tiếp theo →]           │                      │
└──────────────────────────────────────────────┴──────────────────────┘
```

**State:**

```js
const { id }          = useParams();
const [exam,          setExam]     = useState(null);
const [questions,     setQuestions]= useState([]);
const [answers,       setAnswers]  = useState({});  // { questionId: selectedOption }
const [currentQ,      setCurrentQ] = useState(0);
const [timeLeft,      setTimeLeft] = useState(null); // seconds
const [isSubmitting,  setSubmit]   = useState(false);
const [submitted,     setSubmitted]= useState(false);
const timerRef = useRef(null);
```

**Timer (client display only):**

```js
useEffect(() => {
  if (!exam) return;
  setTimeLeft(exam.durationMin * 60);
  timerRef.current = setInterval(() => {
    setTimeLeft((t) => {
      if (t <= 1) { clearInterval(timerRef.current); handleSubmit(); return 0; }
      return t - 1;
    });
  }, 1000);
  return () => clearInterval(timerRef.current);
}, [exam]);
```

**API:**

- `GET /api/assessments/:id` — load đề thi + câu hỏi (không có `correct_option`)
- `POST /api/quiz-attempts` → `{ assessmentId, answers: [{ questionId, selectedOption }] }`
  - Thành công → `navigate(`/mock-test/${id}/results?attemptId=${result.attemptId}`)`

**localStorage backup:** dùng pattern §5.3.

**Domain rules:**

- Không gửi `score` từ client.
- Timer chạy client-side chỉ để hiển thị; backend validate thời gian.
- Mất mạng → hiển thị toast warning "Mất kết nối, đang thử lại..." + retry POST khi reconnect.

**ConfirmSubmit modal:** hiển thị số câu chưa trả lời trước khi nộp.

---

### 11.3 `/mock-test/:id/results` — Kết quả thi

**Prefix:** `mxr-` | **activeTab:** `'mock-test'`

**Layout:**

```
[ResultHero: Điểm xx/100 | PASSED/FAILED badge | Ngày thi]

[SectionScores: Từ vựng xx% | Ngữ pháp xx% | Đọc hiểu xx%]

[Comparison: vs lần thi trước (nếu có)]

[QuestionReview Table:]
  Câu | Kỹ năng | Đáp án của bạn | Đáp án đúng | Kết quả
  ...
  
[CTA: [Thi lại] [Xem bài học liên quan] [Về Dashboard]]
```

**API:** `GET /api/quiz-attempts/:attemptId` (từ query param `?attemptId=xxx`)

**Domain rules:**

- Kết quả bất biến — không có nút "Sửa".
- `section_scores` parse từ JSON field trong response.

---

### 11.4 `/progress` — Tiến độ học tập

**Prefix:** `prg-` | **activeTab:** `''`

**Layout:**

```
[Header: "Tiến Độ Của Tôi"]

[StatRow: streak | wordCount | lessonsCompleted | daysStudied]

[Radar Chart: năng lực 5 kỹ năng (Vocab/Kanji/Grammar/Reading/Listening)]

[CompletionGrid: Kanji xx/yy | Vocab xx/yy | Grammar xx/yy | Kana xx/yy]

[ExamHistory Table:]
  Ngày | Đề thi | Điểm | Pass/Fail
  ...
  [Pagination]
```

**Dùng `SkillRadarChart`** — component mới cần tạo (dùng `<svg>` thuần, không import chart library ngoài).

**API:**

- `GET /api/students/me/stats` — streak, wordCount, completions, radarData
- `GET /api/quiz-attempts/me?page=0&size=10` — lịch sử bài thi

**Empty states:**

- Radar: hiển thị chart rỗng (tất cả 0%) với label.
- ExamHistory: mascot `'thinking'`, "Chưa có bài thi nào. Thử sức với đề thi thử ngay!"

**SkillRadarChart spec (SVG thuần):**

```
5 kỹ năng = 5 đỉnh polygon. Giá trị 0–100.
Màu fill: rgba(232, 154, 170, 0.25) — sakura pink tint.
Stroke: var(--color-primary) 2px.
Grid lines: 5 mức (20/40/60/80/100%), stroke var(--color-border).
Labels: font 12px, color var(--color-text-sub).
```

---

## 12. PAGE SPECS — SPRINT 4: MONETIZATION & RETENTION

### 12.1 `/subscription` — Nâng cấp VIP

**Prefix:** `sub-` | **activeTab:** `''`

**Layout:**

```
[Header: "Nâng Cấp VIP" + subtitle so sánh FREE vs VIP]

[PlanGrid (3 cards): Monthly | Quarterly | Annual]
  PlanCard: tên gói, giá/tháng, badge "Tiết kiệm xx%", features list, [Đăng ký ngay]

[CurrentPlan banner (nếu đã VIP): "Gói của bạn hết hạn: dd/MM/yyyy" + [Gia hạn]]

[FeatureComparison table: FREE vs VIP]
```

**State:**

```js
const [plans,       setPlans]   = useState([]);
const [currentPlan, setCurrent] = useState(null);   // null nếu FREE
const [isLoading,   setLoading] = useState(true);
const [selectedPlan, setSelected] = useState(null);
const [isRedirecting, setRedirect] = useState(false);
```

**API:**

- `GET /api/subscriptions/plans`
- `POST /api/subscriptions/checkout` → `{ planId }` → response `{ paymentUrl }`
  - `window.location.href = paymentUrl` để redirect sang gateway

**VIP user:** Hiển thị ngày hết hạn, nút gia hạn thay vì nút đăng ký.

---

### 12.2 `/subscription/success` — Thanh toán thành công

**Prefix:** `sus-` | **activeTab:** `''`

**Logic:** Nhận `?orderId=xxx` từ query param → verify với backend.

```
[CelebrationHero: mascot 'celebrate' | "Chúc mừng! Bạn đã là VIP!"]
[ExpiryInfo: "Tài khoản VIP hiệu lực đến: dd/MM/yyyy"]
[CTA: [Bắt đầu học ngay →] → /dashboard]
```

**API:** `GET /api/subscriptions/verify?orderId=xxx`

**Nếu verify fail** → hiển thị "Đang xử lý thanh toán..." + retry sau 3s. Sau 10s thất bại → "Vui lòng liên hệ hỗ trợ" + link support.

---

### 12.3 `/kanji` — Danh sách Kanji

**Prefix:** `knj-` | **activeTab:** `'kanji'`

**Layout:**

```
[Header: "Kanji" + JLPT Level Filter tabs]

[Stats: đã học xx/yy kanji ở level này]

[KanjiGrid — lưới 8 cột desktop / 4 cột mobile]
  KanjiCell: character (32px), completed tick, click → /kanji/:id
  
[Pagination]
```

**State:**

```js
const [level,       setLevel]  = useState('N5');  // default level của user
const [kanji,       setKanji]  = useState([]);
const [isLoading,   setLoading]= useState(true);
const [currentPage, setPage]   = useState(1);
const [totalPages,  setTotal]  = useState(1);
```

**API:** `GET /api/kanji?level=N5&page=0&size=50`

**Completed styling:**

```css
.knj-cell--completed {
  background: var(--color-primary-bg);
  border: 2px solid var(--color-primary-light);
}
.knj-cell--completed::after {
  content: '✓';
  position: absolute; bottom: 2px; right: 4px;
  font-size: 10px; color: var(--color-primary);
}
```

---

### 12.4 `/kanji/:id` — Luyện viết Kanji (OCR)

**Prefix:** `koc-` | **activeTab:** `'kanji'`

**Layout:**

```
[KanjiHeader: character (80px), onyomi, kunyomi, meaning, strokeCount]
[StrokeOrderImage: static URL]

[OCR Practice Panel]
  ┌─────────────────────────────┐
  │  [Upload ảnh chữ viết tay]  │
  │  hoặc kéo thả vào đây       │
  │  [Phân tích →]              │
  └─────────────────────────────┘

  Trạng thái phân tích:
  PENDING  → spinner "Đang phân tích..."
  DONE     → SimilarityGauge + [Luyện lại]
  ERROR    → fallback message + [Thử lại]
  
[ExampleWord: word | reading | meaning]

[Buttons: ← Kanji trước | [Thêm Flashcard] | Kanji tiếp theo →]
```

**State:**

```js
const { id }        = useParams();
const [kanji,       setKanji]    = useState(null);
const [file,        setFile]     = useState(null);
const [ocrJob,      setJob]      = useState(null);   // { jobId, status }
const [ocrResult,   setResult]   = useState(null);   // { similarity }
const [ocrError,    setOcrError] = useState('');
const [isAnalyzing, setAnalyzing]= useState(false);
const pollRef = useRef(null);
```

**API (OCR async):**

1. `POST /api/ai/ocr/submit` → `{ kanjiId, imageFile: multipart }` → `{ jobId }`
2. Poll `GET /api/ai/ocr/:jobId` mỗi 1s, timeout 30s (§5.4)

**SimilarityGauge:**

```
≥ 80%:  Xuất sắc (green)
60–79%: Tốt (accent gold)
< 60%:  Cần luyện thêm (error red)
```

**Domain rules:**

- File upload → multipart, không base64 BLOB.
- `ai_score_suggestion` chỉ hiển thị — không có override cho student.
- Fallback message sau timeout: "Không thể phân tích ngay lúc này. Vui lòng thử lại sau."

---

### 12.5 `/certificates` — Chứng chỉ

**Prefix:** `crt-` | **activeTab:** `''`

**Layout:**

```
[Header: "Chứng Chỉ Của Tôi"]

[CertificateList]
  CertCard: level badge, "Hoàn thành N5", tên học viên, ngày đạt
  [Tải PDF] [Chia sẻ LinkedIn]

[LockedCerts (nếu chưa đủ điều kiện)]
  LockedCertCard: opacity 0.5, progress "xx/yy bài đã hoàn thành"
```

**API:**

- `GET /api/certificates/me` — danh sách chứng chỉ đã đạt
- `GET /api/certificates/me/progress` — tiến độ các level chưa đạt
- `GET /api/certificates/:id/download` → `{ pdfUrl }` → `window.open(pdfUrl)`

**Empty state:** mascot `'idle'`, "Hoàn thành toàn bộ bài học một level để nhận chứng chỉ!"

---

## 13. API LAYER PATTERN

Mọi function API thêm vào `api/studentService.js`. Không tạo file mới.

```js
import api from './authService';    // axios instance đã có JWT interceptor

// Pattern chuẩn
export async function getLessons({ jlptLevel, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (jlptLevel) params.level = jlptLevel;
  const res = await api.get('/lessons', { params });
  return res.data.data;
}

export async function getLessonDetail(lessonId) {
  const res = await api.get(`/lessons/${lessonId}`);
  return res.data.data;
}

export async function markProgress(contentType, contentId, status = 'completed') {
  const res = await api.post('/learning-progress', { contentType, contentId, status, progressPercent: 100 });
  return res.data.data;
}

export async function submitOcr(kanjiId, imageFile) {
  const form = new FormData();
  form.append('kanjiId', kanjiId);
  form.append('imageFile', imageFile);
  const res = await api.post('/ai/ocr/submit', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data.data;   // { jobId }
}

export async function getOcrResult(jobId) {
  const res = await api.get(`/ai/ocr/${jobId}`);
  return res.data.data;   // { status, similarity }
}
```

---

## 14. RESPONSIVE PATTERN

Giống MASTER-SPEC §11. Thêm đặc thù student:

```css
/* Kanji grid */
.knj-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px; }

@media (max-width: 1199px) { .knj-grid { grid-template-columns: repeat(6, 1fr); } }
@media (max-width: 767px)  { .knj-grid { grid-template-columns: repeat(4, 1fr); } }

/* Exam layout — mobile: navigator di chuyển xuống dưới */
@media (max-width: 767px) {
  .mxa-layout { flex-direction: column; }
  .mxa-navigator { width: 100%; order: 2; position: static; }
  .mxa-question-panel { order: 1; }
}

/* Subscription plans */
.sub-plan-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }

@media (max-width: 1199px) { .sub-plan-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px)  { .sub-plan-grid { grid-template-columns: 1fr; } }
```

---

## 15. ACCESSIBILITY CHECKLIST

Ngoài admin checklist (MASTER-SPEC §12), thêm cho student:

- [ ] **Flashcard:** `aria-live="polite"` trên phần điểm số/câu trả lời để screen reader thông báo khi lật
- [ ] **Exam timer:** `aria-live="off"` (không thông báo từng giây), chỉ thông báo khi còn 5 phút: `aria-live="assertive"`
- [ ] **Audio player** (vocabulary/kana): `<audio>` với `<track kind="captions">` hoặc ít nhất `aria-label`
- [ ] **OCR upload:** input `type="file"` với `accept="image/*"` và `aria-label`
- [ ] **Radar chart SVG:** `role="img"` + `aria-label` tóm tắt dữ liệu
- [ ] **Kanji character:** bọc trong `<span lang="ja">` cho screen reader đọc đúng ngôn ngữ
- [ ] **Flashcard flip:** button "Lật thẻ" phải có `aria-label` thay đổi theo trạng thái

---

## 16. CHECKLIST TẠO TRANG MỚI

Khi nhận yêu cầu tạo một trang student mới:

**Bước 1 — Chuẩn bị:**

- [ ] Xác định `activeTab` string cho TopNav
- [ ] Xác định prefix CSS (3 chữ cái)
- [ ] Xác định dùng Redux hay local state
- [ ] Xác định API endpoints cần (từ `studentService.js`)

**Bước 2 — Thêm API:**

- [ ] Thêm functions vào `api/studentService.js`

**Bước 3 — Tạo file:**

- [ ] `pages/[feature]/[PageName].jsx` — page shell
- [ ] `pages/[feature]/[PageName].css` — CSS đầy đủ
- [ ] `components/student/[Component].jsx` — nếu cần component mới > 60 dòng

**Bước 4 — Đăng ký route:**

- [ ] Thêm vào `App.jsx`: `<Route path="/[path]" element={<PrivateRoute><MyPage /></PrivateRoute>} />`

**Bước 5 — Kiểm tra:**

- [ ] 3 trạng thái: loading skeleton / error banner / empty state
- [ ] VIP lock check nếu trang có nội dung VIP
- [ ] Domain rules checklist (§8) áp dụng đúng
- [ ] Responsive 3 breakpoint
- [ ] Accessibility checklist (§15)
- [ ] Không hard-code hex color
- [ ] Không import icon/chart library ngoài
- [ ] API calls trong `studentService.js`, không gọi `axios` trực tiếp trong component

---

## 17. VÍ DỤ NHANH — Trang `/review`

```
Prefix:     rev-
activeTab:  'review'
State:      queue[], currentIdx, isFlipped, done, isLoading
API:        GET /api/flashcards?dueOnly=true
            GET /api/flashcards/:id/reveal
            POST /api/flashcards/:id/review
Components: FlashcardCard (mới), ProgressBar (common)
States:     loading spinner / empty (done for today) / empty (no cards)
Domain:     Đánh giá do backend tính SM-2, frontend chỉ gửi 'easy'|'hard'|'wrong'
```

Page file (`Review.jsx`) chỉ có:

```jsx
export default function Review() {
  // queue state + fetch
  return (
    <div className="rev-page">
      <TopNav activeTab="review" />
      <main className="rev-body">
        {done ? <DoneScreen nextDate={nextDate} /> : (
          <>
            <ReviewHeader total={queue.length} current={currentIdx} />
            <FlashcardCard
              card={queue[currentIdx]}
              isFlipped={isFlipped}
              onFlip={() => setFlipped(true)}
              onRate={handleRate}
            />
          </>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

<!-- USER-SPEC.md v1.0 — 2026-06-02 -->
