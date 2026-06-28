# SPEC — Staff Quản Lý Quiz & Đề Thi (Assessments)
>
> **Feature ID:** `feat-staff` | **Page:** `StaffAssessments`
> **Route:** `/staff/assessments`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-03
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Backend ref:** `feat-content-management` — UC-26 (Quiz), UC-28 (JLPT Mock Exams)

---

## 1. TỔNG QUAN TRANG

Trang quản lý bài trắc nghiệm nhanh (Quiz) và đề thi thử JLPT (Exam). Dùng **tab** phân loại. Mỗi assessment có quy trình: Tạo metadata → Gán câu hỏi → Gửi duyệt. Assessment đã `published` bị khóa hoàn toàn — không thể thay đổi danh sách câu hỏi.

**Prefix CSS:** `sfa-`
**activeTab:** `'staff-assessments'`
**Guard:** `<StaffRoute>`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-assessments"                  │
├──────────────────────────────────────────────────────────────┤
│  <main className="sfa-body">                                 │
│                                                              │
│  [Page Header: "Quiz & Đề Thi"  [+ Tạo mới ▼]]              │
│                                                              │
│  [Type Tabs: [Quiz] [Đề thi thử JLPT]]                       │
│                                                              │
│  [Filter Bar: Search | Level▼ | Status▼]                     │
│                                                              │
│  [Assessment Table]                                          │
│  ┌──────────────────────────────────────────────────────┐    │
│  │ Tiêu đề  │ Level │ Câu hỏi │ Điểm │ Trạng thái │ ⋯ │    │
│  ├──────────┼───────┼─────────┼──────┼────────────┼───┤    │
│  │ Quiz K..  │  N5  │   10    │ 10đ  │ [Nháp]     │ ⋯ │    │
│  │ Mock N4.1 │  N4  │   100   │100đ  │ [Đã duyệt] │ 🔒│    │
│  └──────────────────────────────────────────────────────┘    │
│                                                              │
│  [Pagination]                                                │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffAssessments.jsx
pages/staff/StaffAssessments.css
components/staff/AssessmentFormModal.jsx      ← tạo/sửa metadata (title, level, score, duration...)
components/staff/AssessmentBuilderPage.jsx    ← trang con: gán câu hỏi vào đề (route: /staff/assessments/:id/build)
components/staff/QuestionPickerModal.jsx      ← chọn câu hỏi từ ngân hàng (dùng trong Builder)
```

---

## 4. STATE (danh sách)

```js
const [assessments,  setItems]   = useState([]);
const [isLoading,    setLoading] = useState(true);
const [error,        setError]   = useState('');
const [search,       setSearch]  = useState('');
const [debounced,    setDebounced]= useState('');
const [levelFilter,  setLevel]   = useState('');
const [statusFilter, setStatus]  = useState('');
const [assessType,   setType]    = useState('quiz');   // 'quiz' | 'exam'
const [currentPage,  setPage]    = useState(1);
const [totalPages,   setTotal]   = useState(1);
const [showModal,    setModal]   = useState(false);
const [editItem,     setEditItem]= useState(null);
const timerRef = useRef(null);
const PAGE_SIZE = 20;
```

---

## 5. API — `staffService.js`

```js
// Danh sách assessments
export async function getAssessments({ type, search, level, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (type)   params.type   = type;
  if (search) params.search = search;
  if (level)  params.level  = level;
  if (status) params.status = status;
  const res = await api.get('/staff/assessments', { params });
  return res.data.data; // { content[], totalPages }
}

// Tạo assessment mới
export async function createAssessment(data) {
  const res = await api.post('/staff/assessments', data);
  return res.data.data; // { assessmentId, status: 'draft' }
}

// Cập nhật metadata assessment
export async function updateAssessment(assessmentId, data) {
  const res = await api.put(`/staff/assessments/${assessmentId}`, data);
  return res.data.data;
}

// Lấy chi tiết assessment kèm câu hỏi đã gán
export async function getAssessmentDetail(assessmentId) {
  const res = await api.get(`/staff/assessments/${assessmentId}`);
  return res.data.data;
  // returns: { ...assessment, assignments: [{ assignmentId, question, score, displayOrder, sectionName }] }
}

// Gán câu hỏi vào assessment
export async function assignQuestions(assessmentId, assignments) {
  // assignments: [{ questionId, score, displayOrder, sectionName }]
  const res = await api.post(`/staff/assessments/${assessmentId}/assign-questions`, { assignments });
  return res.data.data;
}

// Xóa câu hỏi khỏi assignment
export async function removeAssignment(assessmentId, assignmentId) {
  const res = await api.delete(`/staff/assessments/${assessmentId}/assignments/${assignmentId}`);
  return res.data.data;
}

// Gửi duyệt assessment
export async function submitAssessmentForReview(assessmentId) {
  const res = await api.post('/staff/contents/submit-review', {
    contentType: 'assessment',
    contentId: assessmentId,
  });
  return res.data.data;
}
```

---

## 6. ASSESSMENT TABLE

| Cột | Nội dung |
|:---|:---|
| Tiêu đề | text + click → navigate `/staff/assessments/:id/build` |
| Level | `<JlptBadge>` |
| Số câu | `assignedCount` / `expectedCount` nếu biết |
| Tổng điểm | `total_score` đ |
| Thời gian | `duration_min` phút |
| Trạng thái | `<ContentStatusBadge>` + 🔒 nếu published |
| Hành động | tùy status |

Hành động:

| Status | Nút hiển thị |
|:---|:---|
| `draft` | [Xây dựng đề] [Sửa] [Gửi duyệt] |
| `pending_review` | [Xem đề] |
| `published` | [Xem đề] 🔒 |
| `rejected` | [Xây dựng đề] [Sửa] [Gửi duyệt lại] |

---

## 7. ASSESSMENT FORM MODAL (`AssessmentFormModal`)

### Fields chung

```
[Tiêu đề *]             — text input
[Loại *]                — radio: Quiz | Đề thi thử JLPT
[Cấp độ JLPT *]         — select N5..N1

=== Nếu Loại = Quiz ===
[Bài học liên kết]      — select (tùy chọn, để gắn quiz vào bài học)
[Chủ đề]                — text input

=== Nếu Loại = Exam ===
(không có lesson_id)

=== Chung ===
[Thời gian làm bài (phút)] — number input, min 5
[Điểm đạt (pass score)]    — number input
[Tổng điểm *]              — number input (bắt buộc, dùng để validate khi gán câu hỏi)
[File nghe (audio_url)]    — file upload (mp3, optional)
```

**Validation:**

- `passScore` ≤ `totalScore`
- `durationMin` ≥ 5

---

## 8. TRANG XÂY DỰNG ĐỀ (`/staff/assessments/:id/build`)

Trang riêng (không phải modal) vì phức tạp. Route con của `/staff/assessments`.

### Layout

```
┌──────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-assessments"                  │
├──────────────────────────────────────────────────────────────┤
│  <main className="sfa-builder-body">                         │
│                                                              │
│  [Builder Header]                                            │
│  ← Quay lại   "Xây dựng: Mock Test N4 Vol.2"   [Gửi duyệt]  │
│                                                              │
│  [Score Progress Bar]                                        │
│  Đã gán: 85/100 điểm  ████████░░  "Còn 15 điểm nữa"        │
│                                                              │
│  [Exam có nhiều phần — Tabs section nếu type=exam]          │
│  [Từ vựng] [Ngữ pháp] [Đọc hiểu] [Nghe hiểu] (+ thêm phần) │
│                                                              │
│  [Assigned Questions List] (drag-to-reorder)                 │
│  ┌────────────────────────────────────────────────────┐      │
│  │ ≡ │ #  │ Câu hỏi                    │ Điểm │ [🗑] │      │
│  ├───┼────┼────────────────────────────┼──────┼──────┤      │
│  │ ≡ │ 1  │ N4 Vocab: '学校' nghĩa là..│  1đ  │ [🗑] │      │
│  └────────────────────────────────────────────────────┘      │
│                                                              │
│  [+ Thêm câu hỏi từ ngân hàng]                              │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### State (AssessmentBuilderPage)

```js
const { id }           = useParams();
const [assessment,     setAssess]    = useState(null);
const [assignments,    setAssigns]   = useState([]);   // [{ assignmentId, question, score, displayOrder, sectionName }]
const [activeSect,     setSection]   = useState('');   // sectionName active (exam only)
const [totalAssigned,  setTotScore]  = useState(0);    // sum of assignment scores
const [showPicker,     setPicker]    = useState(false);
const [isLoading,      setLoading]   = useState(true);
const [isSaving,       setSaving]    = useState(false);
const [error,          setError]     = useState('');
```

### Score Progress Bar

```
Tổng điểm đề = assessment.totalScore (ví dụ 100)
Đã gán = sum(assignments.map(a => a.score)) (ví dụ 85)
Progress = 85/100 = 85%

Màu:
- < 100%: var(--color-warning) amber
- = 100%: var(--color-secondary) green
- > 100%: var(--color-error) red — không cho gửi duyệt
```

```jsx
<div className="sfa-score-bar">
  <span>Điểm đã gán: <strong>{totalAssigned}</strong> / {assessment.totalScore}</span>
  <ProgressBar value={Math.min((totalAssigned / assessment.totalScore) * 100, 100)} />
  {totalAssigned > assessment.totalScore && (
    <span className="sfa-score-error" role="alert">Tổng điểm vượt quá {assessment.totalScore}!</span>
  )}
</div>
```

### Exam Sections (chỉ khi assessType = 'exam')

Tabs hiển thị các `sectionName` duy nhất từ `assignments`. Nút [+ Thêm phần] mở input inline để thêm section mới.

### Drag-to-reorder

Sử dụng HTML5 `draggable` (không import thư viện ngoài):

```jsx
function handleDragStart(e, idx) {
  e.dataTransfer.setData('text/plain', idx.toString());
}
function handleDrop(e, targetIdx) {
  const sourceIdx = parseInt(e.dataTransfer.getData('text/plain'));
  const newList = [...assignments];
  const [moved] = newList.splice(sourceIdx, 1);
  newList.splice(targetIdx, 0, moved);
  // Cập nhật displayOrder
  setAssigns(newList.map((a, i) => ({ ...a, displayOrder: i + 1 })));
}
```

Sau khi reorder → call `assignQuestions(id, updatedAssignments)` để lưu thứ tự.

### Xóa câu hỏi khỏi đề

Click [🗑] → confirm dialog ngắn → `removeAssignment()` → cập nhật local state.

### Nút "Gửi duyệt"

Disabled khi:

- `totalAssigned !== assessment.totalScore`
- `assignments.length === 0`
- `assessment.status === 'published'` (đề đã xuất bản — full readonly)

---

## 9. QUESTION PICKER MODAL (`QuestionPickerModal`)

Modal cho phép chọn câu hỏi từ ngân hàng để thêm vào đề:

```
┌──────────────────────────────────────────────────────┐
│  Chọn câu hỏi từ ngân hàng              [✕]          │
├──────────────────────────────────────────────────────┤
│  [Search] [Kỹ năng▼] [Level▼] [Loại▼]               │
│                                                      │
│  ☐  #12 — "N4 Kanji: '学' có nghĩa gì?" — kanji N4  │
│  ☐  #15 — "Chọn từ đồng nghĩa với..."   — vocab N4   │
│  ✅  #18 — (đã có trong đề)              — grammar N4 │
│  ...                                                 │
│                                                      │
│  [Pagination]                                        │
│                                                      │
├──────────────────────────────────────────────────────┤
│  Đã chọn: 3 câu hỏi                                  │
│  Điểm mỗi câu: [1.0____] (áp dụng cho tất cả)       │
│  Section: [Từ vựng___] (áp dụng cho tất cả)          │
│                                                      │
│                       [Hủy] [Thêm vào đề →]         │
└──────────────────────────────────────────────────────┘
```

**Logic:**

- Câu hỏi đã có trong đề hiển thị ✅ (disabled, không chọn được)
- Multi-select bằng checkbox
- Khi click [Thêm vào đề →] → gọi `assignQuestions()` với toàn bộ câu đã chọn
- Score mặc định `1.0`, user có thể đổi trước khi add

---

## 10. PUBLISHED LOCK STATE

Khi `assessment.status === 'published'`:

- Builder page hiển thị banner: "Đề thi đã xuất bản — danh sách câu hỏi không thể thay đổi."
- Ẩn nút [🗑], [+ Thêm câu hỏi], drag handle
- Ẩn nút [Gửi duyệt]
- Nút [Xây dựng đề] trên danh sách → đổi thành [Xem đề]

---

## 11. LOADING / ERROR / EMPTY

- **Loading danh sách:** skeleton 5 hàng
- **Loading builder:** skeleton câu hỏi + score bar
- **Error:** error banner + retry
- **Empty (danh sách):** EmptyState mascot `thinking`, CTA "Tạo bài đánh giá đầu tiên"
- **Empty (builder — chưa có câu hỏi):** inline message trong bảng: "Chưa có câu hỏi nào. Nhấn [+ Thêm câu hỏi] để bắt đầu."

---

## 12. RESPONSIVE

```css
@media (max-width: 1199px) { .sfa-body { padding: 24px 20px 40px; } }
@media (max-width: 767px)  {
  .sfa-body         { padding: 16px 16px 32px; }
  .sfa-builder-body { flex-direction: column; }
  /* Ẩn cột Thời gian trên mobile */
  .sfa-col-duration { display: none; }
}
```

---

## 13. ACCESSIBILITY

- [ ] Builder table: `aria-grabbed`, `aria-dropeffect` cho drag-and-drop
- [ ] Score progress bar: `role="progressbar"`, `aria-valuenow`, `aria-valuemin="0"`, `aria-valuemax={totalScore}`
- [ ] "Tổng điểm vượt quá" error: `role="alert"`
- [ ] QuestionPickerModal: `role="dialog"`, `aria-modal="true"`, `aria-label="Chọn câu hỏi từ ngân hàng"`
- [ ] Checkbox đã-có-trong-đề: `disabled` + `aria-label="Câu hỏi đã có trong đề"`
