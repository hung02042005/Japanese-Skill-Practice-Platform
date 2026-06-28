# SPEC — Staff Ngân Hàng Câu Hỏi (Question Bank)
>
> **Feature ID:** `feat-staff` | **Page:** `StaffQuestions`
> **Route:** `/staff/questions`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-03
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Backend ref:** `feat-content-management` — UC-24 (Manage Question Bank)

---

## 1. TỔNG QUAN TRANG

Trang quản lý kho câu hỏi trắc nghiệm. Staff tạo câu hỏi mới, xem danh sách, lọc theo kỹ năng / cấp độ / trạng thái, và gửi duyệt. Câu hỏi đã có bài làm (`is_locked = true`) không được phép sửa — chỉ được tạo phiên bản mới.

**Prefix CSS:** `sfq-`
**activeTab:** `'staff-questions'`
**Guard:** `<StaffRoute>`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-questions"                    │
├──────────────────────────────────────────────────────────────┤
│  <main className="sfq-body">                                 │
│                                                              │
│  [Page Header: "Ngân Hàng Câu Hỏi"  [+ Thêm câu hỏi]]       │
│                                                              │
│  [Filter Bar]                                                │
│  [Search] [Kỹ năng▼] [Level▼] [Loại câu▼] [Trạng thái▼]    │
│                                                              │
│  [Summary Bar: Tổng: xxx câu | Đã duyệt: xx | Nháp: xx]    │
│                                                              │
│  [Question Table]                                            │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ # │ Câu hỏi (truncated)  │ Kỹ năng │ Lv │ Loại │ TT │⋯│ │
│  ├───┼──────────────────────┼─────────┼────┼──────┼────┼─┤ │
│  │ 1 │ N5 Kanji: '水' đọc ..│ kanji   │ N5 │ MCQ  │🔒 │⋯│ │
│  │ 2 │ Chọn từ đồng nghĩa ..│ vocab   │ N4 │ MCQ  │📝 │⋯│ │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  [Pagination]                                                │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffQuestions.jsx
pages/staff/StaffQuestions.css
components/staff/QuestionFormModal.jsx      ← modal tạo/sửa câu hỏi (>60 dòng JSX)
components/staff/QuestionPreviewDrawer.jsx  ← xem trước câu hỏi dạng slide-in
```

---

## 4. STATE

```js
const [questions,    setQuestions] = useState([]);
const [isLoading,    setLoading]   = useState(true);
const [error,        setError]     = useState('');
const [search,       setSearch]    = useState('');
const [debounced,    setDebounced] = useState('');
const [skillFilter,  setSkill]     = useState('');   // ''|'vocabulary'|'grammar'|'kanji'|'reading'|'listening'|'mixed'
const [levelFilter,  setLevel]     = useState('');
const [typeFilter,   setType]      = useState('');   // ''|'multiple_choice'|'fill_blank'|'true_false'
const [statusFilter, setStatus]    = useState('');
const [currentPage,  setPage]      = useState(1);
const [totalPages,   setTotal]     = useState(1);
const [showModal,    setModal]     = useState(false);
const [editQuestion, setEditQ]     = useState(null);    // null = tạo mới
const [previewQ,     setPreviewQ]  = useState(null);    // câu hỏi đang xem trước
const timerRef = useRef(null);
const PAGE_SIZE = 20;
```

Reset `currentPage → 1` khi bất kỳ filter nào thay đổi.

---

## 5. API — `staffService.js`

```js
// Lấy danh sách câu hỏi
export async function getQuestions({ search, skill, level, type, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (search) params.search = search;
  if (skill)  params.skill  = skill;
  if (level)  params.level  = level;
  if (type)   params.type   = type;
  if (status) params.status = status;
  const res = await api.get('/staff/questions', { params });
  return res.data.data; // { content[], totalPages, totalElements, draftCount, publishedCount }
}

// Tạo câu hỏi mới → status = 'draft'
export async function createQuestion(data) {
  const res = await api.post('/staff/questions', data);
  return res.data.data;
}

// Cập nhật câu hỏi (nếu not locked)
export async function updateQuestion(questionId, data) {
  const res = await api.put(`/staff/questions/${questionId}`, data);
  return res.data.data;
}

// Gửi duyệt câu hỏi
export async function submitQuestionForReview(questionId) {
  const res = await api.post('/staff/contents/submit-review', {
    contentType: 'question',
    contentId: questionId,
  });
  return res.data.data;
}
```

---

## 6. QUESTION TABLE

### Các cột

| Cột | Nội dung |
|:---|:---|
| # | `question_id` |
| Câu hỏi | `question_text` truncate 80 ký tự, click → mở `QuestionPreviewDrawer` |
| Kỹ năng | `<SkillBadge skill={q.skill} />` |
| Level | `<JlptBadge level={q.jlptLevel} />` |
| Loại | MCQ / Điền / Đ-S |
| Trạng thái | `<ContentStatusBadge>` + 🔒 nếu `isLocked` |
| Hành động | [Xem] [Sửa]*[Gửi duyệt]* |

`*` Ẩn nếu `isLocked === true` — thay bằng nút [Tạo phiên bản mới]

### SkillBadge

| Kỹ năng | Background | Text |
|:---|:---|:---|
| `vocabulary` | `#E3F2FD` | `#1565C0` |
| `grammar` | `#F3E5F5` | `#6A1B9A` |
| `kanji` | `#FCE4EC` | `#C62828` |
| `reading` | `#E8F5E9` | `#2E7D32` |
| `listening` | `#FFF3E0` | `#E65100` |
| `mixed` | `#F0EDEB` | `#6B625E` |

### Cột Loại câu hỏi (type pill)

- `multiple_choice` → "Trắc nghiệm" (neutral gray)
- `fill_blank` → "Điền vào chỗ trống" (blue tint)
- `true_false` → "Đúng / Sai" (green tint)

### Locked indicator

Câu hỏi có `isLocked = true`: hiển thị icon 🔒 (SVG inline, 16px) trong cột trạng thái + tooltip "Câu hỏi đã được học viên làm, không thể sửa". Row background nhạt hơn (`opacity: 0.8`).

---

## 7. QUESTION FORM MODAL (`QuestionFormModal`)

### Fields

```
[Câu hỏi *]              — textarea, bắt buộc
[Loại câu hỏi *]         — radio: Trắc nghiệm | Điền vào | Đúng/Sai
[Kỹ năng *]              — select: vocabulary|grammar|kanji|reading|listening|mixed
[Cấp độ JLPT *]          — select: N5|N4|N3|N2|N1
[Đính kèm audio]         — file upload (mp3/wav, optional)
[Đính kèm hình ảnh]      — file upload (jpg/png, optional)

=== Nếu loại = Trắc nghiệm ===
[Đáp án A *]             — text input
[Đáp án B *]             — text input
[Đáp án C]               — text input
[Đáp án D]               — text input
[Đáp án đúng *]          — radio: A | B | C | D

=== Nếu loại = Điền vào ===
[Đáp án đúng *]          — text input (correct_answer_text)

=== Nếu loại = Đúng/Sai ===
[Đáp án đúng *]          — radio: Đúng | Sai

[Giải thích]             — textarea (optional)
```

### Modal width: 600px

### Validation client-side

- `questionText`: không rỗng
- `jlptLevel`: phải là N5/N4/N3/N2/N1
- `skill`: phải chọn 1 kỹ năng
- Với MCQ: ít nhất A và B phải có giá trị; `correctOption` phải được chọn

### Footer actions

- [Hủy]
- [Lưu nháp] — `createQuestion()` / `updateQuestion()`
- [Lưu & Gửi duyệt] — `createQuestion()` rồi `submitQuestionForReview()`

---

## 8. QUESTION PREVIEW DRAWER (`QuestionPreviewDrawer`)

Slide-in từ phải, width 480px, hiển thị câu hỏi như học viên thấy:

```
┌─────────────────────────────────────────┐
│  Xem trước câu hỏi               [✕]   │
├─────────────────────────────────────────┤
│  [JlptBadge] [SkillBadge] [TypePill]   │
│                                         │
│  [Audio player nếu có audio_url]        │
│  [Image nếu có image_url]               │
│                                         │
│  Câu hỏi:                               │
│  "N5 Kanji: '水' đọc là gì?"           │
│                                         │
│  ○ A. mizu                              │
│  ○ B. kawa                              │
│  ○ C. yama                              │
│  ○ D. ki                                │
│                                         │
│  Đáp án đúng: ✅ A. mizu                │
│                                         │
│  Giải thích:                            │
│  "'水' là nước, đọc là mizu."           │
│                                         │
│  Trạng thái: [Nháp]  ID: #105          │
└─────────────────────────────────────────┘
```

```css
.sfq-drawer-backdrop {
  position: fixed; inset: 0; background: rgba(0,0,0,0.30); z-index: 200;
}
.sfq-drawer {
  position: fixed; top: 0; right: 0; bottom: 0;
  width: 480px; background: var(--color-card);
  box-shadow: var(--shadow-lg);
  display: flex; flex-direction: column;
  animation: drawerIn 0.22s ease;
}
@keyframes drawerIn { from { transform: translateX(100%); } to { transform: translateX(0); } }

.sfq-drawer-header { padding: 20px 24px; border-bottom: 1px solid var(--color-border); display: flex; align-items: center; justify-content: space-between; }
.sfq-drawer-body   { padding: 24px; flex: 1; overflow-y: auto; }
```

---

## 9. SUMMARY BAR

```
Tổng: 247 câu | Đã xuất bản: 180 | Chờ duyệt: 12 | Nháp: 55
```

Hiển thị khi `!isLoading && !error` và lấy từ response API (`totalElements`, `publishedCount`...).

```css
.sfq-summary { font-size: 13px; color: var(--color-text-sub); display: flex; gap: 20px; flex-wrap: wrap; }
.sfq-summary strong { color: var(--color-text); }
```

---

## 10. LOCKED QUESTION — TẠO PHIÊN BẢN MỚI

Khi click [Tạo phiên bản mới] trên câu hỏi đã locked:

1. Mở `QuestionFormModal` với tất cả fields đã prefill từ câu hỏi gốc
2. Header modal: "Tạo phiên bản mới từ Câu hỏi #105"
3. Lưu → `createQuestion()` (tạo câu hỏi mới độc lập, không liên kết câu cũ)
4. Toast success: "Đã tạo phiên bản mới. Câu hỏi gốc #105 vẫn được giữ nguyên."

---

## 11. LOADING / ERROR / EMPTY

- **Loading:** skeleton 5 hàng bảng
- **Error:** error banner + retry
- **Empty:** `<EmptyState title="Chưa có câu hỏi nào" subtitle="Thêm câu hỏi đầu tiên vào ngân hàng." mascotVariant="thinking" mascotSize={120}>`

---

## 12. RESPONSIVE

```css
@media (max-width: 1199px) { .sfq-body { padding: 24px 20px 40px; } }
@media (max-width: 767px)  {
  .sfq-body        { padding: 16px 16px 32px; }
  .sfq-filter-bar  { flex-direction: column; }
  .sfq-drawer      { width: 100%; }
  /* Ẩn cột "Loại" trên mobile */
  .sfq-col-type    { display: none; }
}
```

---

## 13. ACCESSIBILITY

- [ ] Bảng có `<caption className="visually-hidden">Ngân hàng câu hỏi</caption>`
- [ ] Icon 🔒 có `aria-label="Câu hỏi đã khóa — không thể sửa"`
- [ ] Drawer có `role="dialog"`, `aria-modal="true"`, `aria-label="Xem trước câu hỏi"`, focus trap, Escape đóng
- [ ] Radio buttons trong form modal liên kết bằng `name` attribute chung
- [ ] File upload input có `aria-label` và `accept` attribute
