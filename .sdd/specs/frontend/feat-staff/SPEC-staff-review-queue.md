# SPEC — StaffManager Hàng Đợi Duyệt Nội Dung (Review Queue)
>
> **Feature ID:** `feat-staff` | **Page:** `StaffReviewQueue`
> **Route:** `/staff/review-queue`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-03
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Backend ref:** `feat-content-review` — UC-33 (Review Queue), UC-34 (Manage Published Status)

---

## 1. TỔNG QUAN TRANG

**Chỉ dành cho StaffManager** (`staff_role = 'staff_manager'`). Trang hiển thị tất cả nội dung đang ở trạng thái `pending_review` và cho phép Approve / Reject / Request Changes. Ngoài ra, StaffManager có thể quản lý trạng thái (archive/unpublish) của nội dung đã `published`.

**Quy tắc 4 mắt:** StaffManager không thể tự duyệt nội dung của chính mình (backend enforce 403 `SELF_REVIEW_DENIED`).

**Prefix CSS:** `rqe-`
**activeTab:** `'staff-review'`
**Guard:** `<StaffManagerRoute>` — chỉ cho `staff_role === 'staff_manager'` hoặc `admin`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-review"                       │
├──────────────────────────────────────────────────────────────┤
│  <main className="rqe-body">                                 │
│                                                              │
│  [Page Header: "Hàng Đợi Duyệt" | Badge: "12 chờ duyệt"]   │
│                                                              │
│  [View Tabs: [Chờ duyệt (12)] [Đã xuất bản]]                │
│                                                              │
│  [Filter Bar: Type▼ | Level▼ | Submitted by▼]               │
│                                                              │
│  [Review Queue Table]                                        │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Loại │ Tiêu đề / Text        │ Người tạo │ Gửi lúc │ ⋯│  │
│  ├──────┼───────────────────────┼───────────┼─────────┼──┤  │
│  │ Câu  │ N5 Kanji: '水' đọc..  │ Nguyễn B  │ 2 giờ  │⋯│  │
│  │ Bài  │ Bài 3 — Kanji N5     │ Trần C    │ Hôm qua│⋯│  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  [Pagination]                                                │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffReviewQueue.jsx
pages/staff/StaffReviewQueue.css
components/staff/ReviewActionModal.jsx     ← modal Approve / Reject / Request Changes
components/staff/PublishedContentTable.jsx ← bảng nội dung đã xuất bản (tab "Đã xuất bản")
```

---

## 4. STATE

```js
const [viewTab,      setViewTab]  = useState('pending');  // 'pending' | 'published'
const [items,        setItems]    = useState([]);
const [isLoading,    setLoading]  = useState(true);
const [error,        setError]    = useState('');
const [typeFilter,   setType]     = useState('');   // '' | 'question'|'lesson'|'course'|'grammar'|'vocabulary'|'kanji'|'assessment'
const [levelFilter,  setLevel]    = useState('');
const [currentPage,  setPage]     = useState(1);
const [totalPages,   setTotal]    = useState(1);
const [pendingCount, setPending]  = useState(0);     // badge trên tab
const [reviewTarget, setTarget]   = useState(null);  // item đang được review
const [showModal,    setModal]    = useState(false);
const PAGE_SIZE = 20;
```

---

## 5. API — `staffService.js`

```js
// Lấy hàng đợi chờ duyệt
export async function getReviewQueue({ type, level, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (type)  params.type  = type;
  if (level) params.level = level;
  const res = await api.get('/manager/review-queue', { params });
  return res.data.data;
  // { content: [{ contentId, contentType, titleOrText, submittedBy, submittedAt, jlptLevel }], totalElements, totalPages }
}

// Lấy nội dung đã published (tab "Đã xuất bản")
export async function getPublishedContent({ type, level, page = 0, size = 20 } = {}) {
  const params = { page, size, status: 'published' };
  if (type)  params.type  = type;
  if (level) params.level = level;
  const res = await api.get('/manager/contents', { params });
  return res.data.data;
}

// Approve nội dung
export async function approveContent(contentType, contentId, feedback = '') {
  const res = await api.post('/manager/reviews', {
    contentType,
    contentId,
    action: 'APPROVE',
    feedback,
  });
  return res.data.data;
}

// Reject nội dung (đưa về draft)
export async function rejectContent(contentType, contentId, feedback) {
  const res = await api.post('/manager/reviews', {
    contentType,
    contentId,
    action: 'REJECT',
    feedback,
  });
  return res.data.data;
}

// Yêu cầu chỉnh sửa (trả về draft kèm lý do)
export async function requestChanges(contentType, contentId, feedback) {
  const res = await api.post('/manager/reviews/request-changes', {
    contentType,
    contentId,
    feedback,
  });
  return res.data.data;
}

// Thay đổi trạng thái nội dung đã published
export async function updateContentStatus(contentId, contentType, status, reason) {
  const res = await api.put(`/manager/contents/${contentId}/status`, {
    contentType,
    status,
    reason,
  });
  return res.data.data;
}
```

---

## 6. REVIEW QUEUE TABLE (tab "Chờ duyệt")

| Cột | Nội dung |
|:---|:---|
| Loại | `<ContentTypeBadge type={item.contentType} />` |
| Tiêu đề / Câu hỏi | text truncate 80 ký tự |
| Level | `<JlptBadge>` (nếu có) |
| Người tạo | `submittedBy` (tên Staff) |
| Gửi lúc | relative time ("2 giờ trước", "Hôm qua") |
| Hành động | [Duyệt ✓] [Từ chối ✗] [Yêu cầu sửa ✎] |

### ContentTypeBadge

| Loại | Label | Background | Text |
|:---|:---|:---|:---|
| `question` | Câu hỏi | `#EDE7F6` | `#4527A0` |
| `lesson` | Bài học | `#E3F2FD` | `#1565C0` |
| `course` | Khóa học | `#E8F5E9` | `#2E7D32` |
| `grammar` | Ngữ pháp | `#F3E5F5` | `#6A1B9A` |
| `vocabulary` | Từ vựng | `#FFF3E0` | `#E65100` |
| `kanji` | Kanji | `#FCE4EC` | `#C62828` |
| `assessment` | Đánh giá | `--color-primary-bg` | `--color-primary-dark` |

### Action Buttons

```jsx
<div className="rqe-actions">
  <button
    className="rqe-btn rqe-btn--approve"
    onClick={() => { setTarget(item); setModal('approve'); }}
    aria-label={`Duyệt: ${item.titleOrText}`}
  >
    <svg>{/* check icon */}</svg> Duyệt
  </button>
  <button
    className="rqe-btn rqe-btn--reject"
    onClick={() => { setTarget(item); setModal('reject'); }}
    aria-label={`Từ chối: ${item.titleOrText}`}
  >
    <svg>{/* x icon */}</svg> Từ chối
  </button>
  <button
    className="rqe-btn rqe-btn--changes"
    onClick={() => { setTarget(item); setModal('changes'); }}
    aria-label={`Yêu cầu sửa: ${item.titleOrText}`}
  >
    <svg>{/* edit icon */}</svg> Yêu cầu sửa
  </button>
</div>
```

```css
.rqe-btn { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: var(--radius-full); font-size: 12px; font-weight: 700; cursor: pointer; border: none; transition: filter var(--transition); }
.rqe-btn--approve { background: var(--color-secondary); color: white; }
.rqe-btn--reject  { background: var(--color-error);     color: white; }
.rqe-btn--changes { background: var(--color-accent);    color: #7A4A00; }
.rqe-btn:hover { filter: brightness(1.08); }
```

---

## 7. REVIEW ACTION MODAL (`ReviewActionModal`)

Modal xác nhận + nhập feedback. Hiển thị tóm tắt nội dung đang xử lý.

```
┌──────────────────────────────────────────────────────┐
│  [Tiêu đề tùy action]                         [✕]   │
├──────────────────────────────────────────────────────┤
│  Nội dung: [ContentTypeBadge] [JlptBadge]            │
│  "[Tiêu đề / text ngắn của item]"                    │
│  Tác giả: Nguyễn Văn B  |  Gửi: 2 giờ trước         │
│                                                      │
│  ─────────────────────────────────────────────────   │
│                                                      │
│  === Approve ===                                     │
│  [Nhận xét (tùy chọn)] — textarea                   │
│                                                      │
│  === Reject / Request Changes ===                    │
│  [Lý do phản hồi *] — textarea (bắt buộc)           │
│  Hint: "Nội dung này sẽ được trả về nháp cho Staff." │
│                                                      │
│                          [Hủy] [Xác nhận →]         │
└──────────────────────────────────────────────────────┘
```

**Tiêu đề modal theo action:**

- `approve` → "Duyệt và xuất bản nội dung"
- `reject` → "Từ chối nội dung"
- `changes` → "Yêu cầu chỉnh sửa"

**Nút xác nhận:**

- `approve` → màu green (`--color-secondary`)
- `reject` → màu red (`--color-error`)
- `changes` → màu amber (`--color-warning`)

**Sau khi confirm:**

- Gọi API tương ứng
- Xóa item khỏi danh sách local (optimistic update)
- Cập nhật `pendingCount` trên badge
- Toast success: "Đã duyệt thành công" / "Đã từ chối" / "Đã gửi yêu cầu sửa"

**Xử lý lỗi 403 `SELF_REVIEW_DENIED`:**
Toast error: "Bạn không thể tự duyệt nội dung của chính mình." + không thay đổi state.

---

## 8. PUBLISHED CONTENT TABLE (tab "Đã xuất bản")

Bảng quản lý trạng thái nội dung đang xuất bản. Cho phép Archive / Unpublish:

| Cột | Nội dung |
|:---|:---|
| Loại | `<ContentTypeBadge>` |
| Tiêu đề | text |
| Level | `<JlptBadge>` |
| Ngày xuất bản | `dd/MM/yyyy` |
| Hành động | [Lưu trữ] [Thu hồi] |

**Xử lý lỗi 409 `RESOURCE_IN_USE`:**

```jsx
// Khi click "Thu hồi" và nhận 409:
addToast('error', 'Không thể thu hồi: Câu hỏi này đang được dùng trong đề thi đang hoạt động.');
// Hiển thị danh sách exam liên quan (từ error response) trong alert nhỏ
```

---

## 9. PENDING COUNT BADGE

Header tab "Chờ duyệt" hiển thị badge đỏ với số lượng:

```jsx
<button className={`rqe-view-tab ${viewTab === 'pending' ? 'rqe-view-tab--active' : ''}`}
        onClick={() => setViewTab('pending')}>
  Chờ duyệt
  {pendingCount > 0 && <span className="rqe-badge">{pendingCount}</span>}
</button>
```

```css
.rqe-badge {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 20px; height: 20px; padding: 0 6px;
  background: var(--color-error); color: white;
  border-radius: var(--radius-full); font-size: 11px; font-weight: 700;
  margin-left: 6px;
}
```

---

## 10. LOADING / ERROR / EMPTY

- **Loading:** skeleton 5 hàng
- **Error:** error banner + retry
- **Empty (pending):** `<EmptyState title="Không có gì cần duyệt" subtitle="Tất cả nội dung đã được xử lý. Tuyệt vời!" mascotVariant="celebrate" mascotSize={120}>`
- **Empty (published):** `<EmptyState title="Chưa có nội dung nào được xuất bản" mascotVariant="thinking">`

---

## 11. RESPONSIVE

```css
@media (max-width: 767px) {
  .rqe-actions { flex-direction: column; gap: 6px; }
  .rqe-btn     { width: 100%; justify-content: center; }
  /* Ẩn cột "Người tạo" trên mobile */
  .rqe-col-creator { display: none; }
}
```

---

## 12. ACCESSIBILITY

- [ ] View tabs dùng `role="tablist"` + `role="tab"` + `aria-selected`
- [ ] Badge đỏ có `aria-label="12 nội dung chờ duyệt"` trên tab
- [ ] Modal có `role="dialog"`, `aria-modal="true"`, focus trap, Escape đóng
- [ ] Textarea "Lý do" có `aria-required="true"` khi action = reject/changes
- [ ] Toast lỗi SELF_REVIEW_DENIED: `role="alert"`
