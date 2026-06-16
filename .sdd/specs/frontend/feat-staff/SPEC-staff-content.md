# SPEC — Staff Quản Lý Học Liệu (Content Management)
> **Feature ID:** `feat-staff` | **Page:** `StaffContent`
> **Route:** `/staff/content`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-03
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Backend ref:** `feat-content-management` — UC-25 (Ngữ pháp), UC-27 (Học liệu: Khóa học, Bài học, Kanji, Từ vựng)

---

## 1. TỔNG QUAN TRANG

Trang quản lý toàn bộ học liệu do Staff soạn thảo. Dùng hệ thống **tab ngang** để chuyển giữa các loại nội dung: Khóa học, Bài học, Từ vựng, Ngữ pháp, Kanji. Mọi item đều hiển thị trạng thái kiểm duyệt và nút gửi duyệt.

**Prefix CSS:** `sfc-`
**activeTab:** `'staff-content'`
**Guard:** `<StaffRoute>`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-content"                      │
├──────────────────────────────────────────────────────────────┤
│  <main className="sfc-body">                                 │
│                                                              │
│  [Page Header: "Quản Lý Học Liệu"  [+ Tạo mới ▼]]           │
│                                                              │
│  [Content Type Tabs]                                         │
│  [Khóa học] [Bài học] [Từ vựng] [Ngữ pháp] [Kanji]          │
│                                                              │
│  [Filter Bar]                                                │
│  [Search: "Tìm kiếm..."] [Level: N5▼] [Status: Tất cả▼]     │
│                                                              │
│  [Content Table]                                             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Tiêu đề     │ Level │ Loại │ Trạng thái │ Ngày tạo │ ⋯│  │
│  ├─────────────┼───────┼──────┼────────────┼──────────┼──┤  │
│  │ Bài học N5.1│ N5    │ Bài  │ [Nháp]     │ 01/06    │⋯│  │
│  │ ...         │ ...   │ ...  │ ...        │ ...      │ │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  [Pagination]                                                │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffContent.jsx
pages/staff/StaffContent.css
components/staff/ContentFormModal.jsx       ← modal tạo/sửa (>60 dòng JSX)
components/staff/ContentStatusActions.jsx   ← nút "Gửi duyệt" / "Sửa" / "Xóa"
```

---

## 4. CONTENT TYPE TABS

| Tab | Loại | API endpoint |
|:---|:---|:---|
| Khóa học | `course` | `GET /api/staff/courses` |
| Bài học | `lesson` | `GET /api/staff/lessons` |
| Từ vựng | `vocabulary` | `GET /api/staff/vocabulary` |
| Ngữ pháp | `grammar` | `GET /api/staff/grammar-points` |
| Kanji | `kanji` | `GET /api/staff/kanji` |

```jsx
const CONTENT_TABS = [
  { id: 'course',     label: 'Khóa học' },
  { id: 'lesson',     label: 'Bài học' },
  { id: 'vocabulary', label: 'Từ vựng' },
  { id: 'grammar',    label: 'Ngữ pháp' },
  { id: 'kanji',      label: 'Kanji' },
];

const [activeContentTab, setContentTab] = useState('lesson');
```

---

## 5. STATE

```js
const [items,       setItems]    = useState([]);
const [isLoading,   setLoading]  = useState(true);
const [error,       setError]    = useState('');
const [search,      setSearch]   = useState('');
const [debounced,   setDebounced]= useState('');
const [levelFilter, setLevel]    = useState('');        // '' | 'N5'|'N4'|'N3'|'N2'|'N1'
const [statusFilter, setStatus]  = useState('');        // '' | 'draft'|'pending_review'|'published'|'rejected'
const [currentPage, setPage]     = useState(1);
const [totalPages,  setTotal]    = useState(1);
const [activeContentTab, setContentTab] = useState('lesson');
const [showModal,   setModal]    = useState(false);
const [editItem,    setEditItem] = useState(null);      // null = tạo mới, object = sửa
const timerRef = useRef(null);
const PAGE_SIZE = 20;
```

Debounce search (400ms) — xem `MASTERFrontend §5.4`.

Reset `currentPage → 1` khi `search`, `levelFilter`, `statusFilter`, hoặc `activeContentTab` thay đổi.

---

## 6. API — `staffService.js`

```js
// Lấy danh sách theo loại
export async function getStaffContent(type, { search, level, status, page = 0, size = 20 } = {}) {
  const endpointMap = {
    course:     '/staff/courses',
    lesson:     '/staff/lessons',
    vocabulary: '/staff/vocabulary',
    grammar:    '/staff/grammar-points',
    kanji:      '/staff/kanji',
  };
  const params = { page, size };
  if (search) params.search = search;
  if (level)  params.level  = level;
  if (status) params.status = status;
  const res = await api.get(endpointMap[type], { params });
  return res.data.data; // { content[], totalPages, totalElements }
}

// Tạo mới (payload khác nhau theo type)
export async function createContent(type, data) {
  const endpointMap = {
    course:     '/staff/courses',
    lesson:     '/staff/lessons',
    vocabulary: '/staff/vocabulary',
    grammar:    '/staff/grammar-points',
    kanji:      '/staff/kanji',
  };
  const res = await api.post(endpointMap[type], data);
  return res.data.data;
}

// Cập nhật
export async function updateContent(type, id, data) {
  const endpointMap = {
    course:     `/staff/courses/${id}`,
    lesson:     `/staff/lessons/${id}`,
    vocabulary: `/staff/vocabulary/${id}`,
    grammar:    `/staff/grammar-points/${id}`,
    kanji:      `/staff/kanji/${id}`,
  };
  const res = await api.put(endpointMap[type], data);
  return res.data.data;
}

// Gửi duyệt
export async function submitForReview(contentType, contentId) {
  const res = await api.post('/staff/contents/submit-review', { contentType, contentId });
  return res.data.data; // { contentId, status: 'pending_review' }
}
```

---

## 7. CONTENT TABLE

Mỗi tab hiển thị bảng với cột khác nhau tùy loại. Cột chung:

| Cột | Nội dung |
|:---|:---|
| Tiêu đề / Ký tự | Text hoặc ký tự chính |
| Level | `<JlptBadge level={item.jlptLevel} />` |
| Trạng thái | `<ContentStatusBadge status={item.status} />` |
| Ngày cập nhật | `dd/MM/yyyy` |
| Hành động | `<ContentStatusActions>` |

**Cột riêng theo tab:**

- **Khóa học:** thêm cột `is_vip_only` (⭐ icon)
- **Bài học:** thêm cột `lesson_type` (lesson / reading / listening / speaking)
- **Từ vựng:** thêm cột `reading` (cách đọc)
- **Ngữ pháp:** thêm cột `pattern` (cấu trúc ngữ pháp)
- **Kanji:** thêm cột `onyomi`, `kunyomi`

### ContentStatusBadge

| Status | Background | Text | Label |
|:---|:---|:---|:---|
| `draft` | `#F0EDEB` | `--color-text-sub` | Nháp |
| `pending_review` | `--color-accent-bg` | `--color-warning` | Chờ duyệt |
| `published` | `--color-secondary-bg` | `--color-secondary` | Đã xuất bản |
| `rejected` | `#FFEAEA` | `--color-error` | Từ chối |
| `archived` | `#EEE` | `--color-text-disabled` | Lưu trữ |

### ContentStatusActions (component)

Hành động thay đổi tùy `status` của item:

| Status hiện tại | Nút hiển thị |
|:---|:---|
| `draft` | [Sửa] [Gửi duyệt →] |
| `pending_review` | [Xem] _(chờ duyệt, không cho sửa)_ |
| `published` | [Xem] |
| `rejected` | [Sửa] [Gửi duyệt lại →] |

```jsx
function ContentStatusActions({ item, contentType, onEdit, onSubmit }) {
  return (
    <div className="sfc-actions">
      {(item.status === 'draft' || item.status === 'rejected') && (
        <button className="sfc-btn-icon" onClick={() => onEdit(item)} aria-label="Sửa">
          <svg>{/* edit icon */}</svg>
        </button>
      )}
      {(item.status === 'draft' || item.status === 'rejected') && (
        <button className="sfc-btn--submit" onClick={() => onSubmit(item)} aria-label="Gửi duyệt">
          Gửi duyệt
        </button>
      )}
      {(item.status === 'pending_review' || item.status === 'published') && (
        <button className="sfc-btn-icon" onClick={() => onEdit(item)} aria-label="Xem chi tiết">
          <svg>{/* eye icon */}</svg>
        </button>
      )}
    </div>
  );
}
```

---

## 8. CONTENT FORM MODAL (`ContentFormModal`)

Modal tạo mới / sửa nội dung. Form fields thay đổi theo `contentType`:

### Fields chung tất cả loại
- `title` / `character` — text input (bắt buộc)
- `jlptLevel` — select N5/N4/N3/N2/N1 (bắt buộc)

### Fields riêng theo loại

**Khóa học (course):**
```
title (bắt buộc), description (textarea), jlptLevel, is_vip_only (checkbox), thumbnail_url (upload)
```

**Bài học (lesson):**
```
title, lesson_type (select: lesson|reading|listening|speaking), course_id (select từ danh sách),
jlptLevel, content_text (textarea), video_url, audio_url, explanation (textarea)
```

**Từ vựng (vocabulary):**
```
word (bắt buộc), reading (hiragana/katakana), meaning (bắt buộc), jlptLevel,
part_of_speech (danh từ/động từ/...), example_sentence, audio_url
```

**Ngữ pháp (grammar_points):**
```
pattern (cấu trúc, bắt buộc), meaning (bắt buộc), jlptLevel,
formation (cách chia), example_sentences[] (có thể thêm nhiều)
```

**Kanji:**
```
character (1 ký tự, bắt buộc), onyomi, kunyomi, meaning (bắt buộc), jlptLevel,
stroke_count (number), example_words[] (word + reading)
```

**Modal header:**
- Tạo mới: "Tạo [Loại] Mới"
- Sửa: "Sửa [Tên item]"

**Modal footer:**
- [Hủy] + [Lưu nháp] + [Lưu và gửi duyệt]

"Lưu nháp" → `createContent()` / `updateContent()` với `status = 'draft'`
"Lưu và gửi duyệt" → `createContent()` rồi `submitForReview()` (2 lần gọi API)

---

## 9. NÚT "TẠO MỚI" — DROPDOWN

Nút `[+ Tạo mới ▼]` ở page header mở dropdown chọn loại:

```
┌─────────────────┐
│ + Khóa học      │
│ + Bài học       │
│ + Từ vựng       │
│ + Ngữ pháp      │
│ + Kanji         │
└─────────────────┘
```

Click item → set `editItem = null`, `activeContentTab` sang loại tương ứng, `setModal(true)`.

---

## 10. LOADING / ERROR / EMPTY

- **Loading:** skeleton 5 hàng bảng với `aria-hidden="true"`
- **Error:** `sfc-error-banner` + retry
- **Empty:** `<EmptyState title="Chưa có nội dung nào" subtitle="Bắt đầu soạn thảo bài học đầu tiên." mascotVariant="thinking" mascotSize={120}>`

---

## 11. CSS KEY CLASSES

```css
.sfc-page  { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.sfc-body  { flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.sfc-page-header { display: flex; align-items: center; justify-content: space-between; }
.sfc-page-title  { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0; }

.sfc-content-tabs { display: flex; gap: 4px; border-bottom: 1px solid var(--color-border); padding-bottom: 0; }
.sfc-content-tab  { padding: 10px 20px; font-size: 14px; font-weight: 600; color: var(--color-text-sub); border: none; background: transparent; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -1px; transition: all var(--transition); }
.sfc-content-tab--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }
.sfc-content-tab:hover:not(.sfc-content-tab--active) { background: var(--color-primary-bg); border-radius: var(--radius-sm) var(--radius-sm) 0 0; }

.sfc-filter-bar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.sfc-search { flex: 1; min-width: 240px; height: 40px; padding: 0 14px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-bg); }
.sfc-select { height: 40px; padding: 0 12px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-bg); min-width: 140px; }

.sfc-table { width: 100%; border-collapse: collapse; background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); overflow: hidden; }
.sfc-table th { font-size: 12px; font-weight: 700; color: var(--color-text-sub); padding: 12px 16px; text-align: left; border-bottom: 1px solid var(--color-border); background: #FAFAFA; }
.sfc-table td { font-size: 14px; color: var(--color-text); padding: 14px 16px; border-bottom: 1px solid var(--color-border); vertical-align: middle; }
.sfc-table tr:last-child td { border-bottom: none; }
.sfc-table tr:hover td { background: var(--color-primary-bg); }

.sfc-actions { display: flex; gap: 8px; align-items: center; }
.sfc-btn-icon { width: 32px; height: 32px; border-radius: var(--radius-sm); border: 1.5px solid var(--color-border); background: transparent; cursor: pointer; display: flex; align-items: center; justify-content: center; color: var(--color-text-sub); transition: all var(--transition); }
.sfc-btn-icon:hover { background: var(--color-primary-bg); border-color: var(--color-primary-light); color: var(--color-primary); }
.sfc-btn--submit { height: 32px; padding: 0 14px; background: var(--color-secondary); color: white; border: none; border-radius: var(--radius-full); font-size: 12px; font-weight: 700; cursor: pointer; transition: filter var(--transition); }
.sfc-btn--submit:hover { filter: brightness(1.07); }
```

---

## 12. RESPONSIVE

```css
@media (max-width: 1199px) { .sfc-body { padding: 24px 20px 40px; } }
@media (max-width: 767px)  {
  .sfc-body    { padding: 16px 16px 32px; }
  .sfc-filter-bar { flex-direction: column; align-items: stretch; }
  .sfc-content-tabs { overflow-x: auto; }
  .sfc-table th, .sfc-table td { padding: 10px 12px; }
}
```

---

## 13. ACCESSIBILITY

- [ ] Content type tabs dùng `role="tablist"` + `role="tab"` + `aria-selected`
- [ ] Bảng có `<caption className="visually-hidden">` mô tả loại nội dung đang hiển thị
- [ ] Modal `ContentFormModal` có `role="dialog"`, `aria-modal="true"`, Escape đóng
- [ ] Nút "Gửi duyệt" có `aria-label` đầy đủ: "Gửi duyệt {tên item}"
- [ ] `<select>` filter có `<label>` ẩn liên kết bằng `htmlFor`
