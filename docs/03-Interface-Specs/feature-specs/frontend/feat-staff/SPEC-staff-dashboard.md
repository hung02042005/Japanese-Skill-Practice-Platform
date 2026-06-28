# SPEC — Staff Dashboard
>
> **Feature ID:** `feat-staff` | **Page:** `StaffDashboard`
> **Route:** `/staff`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-03
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Backend ref:** `feat-content-management`, `feat-support`

---

## 1. TỔNG QUAN TRANG

Trang đích khi Staff đăng nhập. Hiển thị tổng quan công việc: số nội dung đang soạn thảo, tickets chưa xử lý, bài nói chờ chấm, và lối vào nhanh tới các tác vụ chính.

**Prefix CSS:** `sfd-`
**activeTab:** `'staff-dashboard'`
**Guard:** `<StaffRoute>` — chỉ cho role `staff` hoặc `admin`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-dashboard"                │
├──────────────────────────────────────────────────────────┤
│  <main className="sfd-body">                             │
│                                                          │
│  [Greeting: "Xin chào, {name} 👋"  |  ngày hôm nay]    │
│                                                          │
│  [Stat Row — 4 cards]                                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ Nháp     │ │ Chờ duyệt│ │ Tickets  │ │ Bài nói  │   │
│  │ (draft)  │ │(pending) │ │  mở      │ │ chờ chấm │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│                                                          │
│  [Quick Actions — 4 cards]                               │
│  ┌────────────────┐  ┌────────────────┐                 │
│  │ + Soạn nội dung│  │ Ngân hàng câu  │                 │
│  │  học           │  │ hỏi            │                 │
│  └────────────────┘  └────────────────┘                 │
│  ┌────────────────┐  ┌────────────────┐                 │
│  │ Tickets hỗ trợ │  │ Chấm bài nói   │                 │
│  └────────────────┘  └────────────────┘                 │
│                                                          │
│  [Recent Activity — 5 dòng gần nhất]                    │
│  Ngày | Loại | Tiêu đề | Trạng thái                     │
│  ...                                                     │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffDashboard.jsx
pages/staff/StaffDashboard.css
```

Không cần component riêng — tất cả inline (trang chủ yếu là stats + links).

---

## 4. STATE

```js
const { user } = useAppSelector((s) => s.auth);   // tên staff từ Redux

const [stats,    setStats]   = useState(null);
const [activity, setActivity]= useState([]);
const [isLoading, setLoading]= useState(true);
const [error,    setError]   = useState('');
```

---

## 5. API — `staffService.js`

```js
// GET /api/staff/dashboard
export async function getStaffDashboard() {
  const res = await api.get('/staff/dashboard');
  return res.data.data;
  // returns: {
  //   draftCount: number,
  //   pendingReviewCount: number,
  //   openTicketCount: number,
  //   pendingGradingCount: number,
  //   recentActivity: [{ date, type, title, status }]
  // }
}
```

---

## 6. STAT ROW — `.sfd-stat-row`

4 cards ngang, mỗi card:

| Card | Icon | Màu border | Link |
|:---|:---|:---|:---|
| Nháp (draft) | Bút chì | `--color-border` | `/staff/content?status=draft` |
| Chờ duyệt | Đồng hồ | `--color-warning` | `/staff/content?status=pending_review` |
| Tickets mở | Hộp thư | `--color-primary` | `/staff/tickets` |
| Bài nói chờ chấm | Micro | `--color-accent` | `/staff/grading` |

```jsx
<div className="sfd-stat-row">
  <div className="sfd-stat-card sfd-stat-card--draft" onClick={() => navigate('/staff/content?status=draft')}>
    <div className="sfd-stat-icon" aria-hidden="true">
      <svg>/* pencil icon */</svg>
    </div>
    <div className="sfd-stat-body">
      <span className="sfd-stat-value">{stats.draftCount}</span>
      <span className="sfd-stat-label">Bài đang soạn</span>
    </div>
  </div>
  {/* 3 cards tương tự */}
</div>
```

```css
.sfd-stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.sfd-stat-card {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  border: 1.5px solid transparent;
  transition: box-shadow var(--transition), border-color var(--transition), transform var(--transition);
}
.sfd-stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.sfd-stat-card--draft    { border-color: var(--color-border); }
.sfd-stat-card--pending  { border-color: var(--color-warning); }
.sfd-stat-card--ticket   { border-color: var(--color-primary); }
.sfd-stat-card--grading  { border-color: var(--color-accent); }

.sfd-stat-icon { width: 44px; height: 44px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; }
.sfd-stat-value { font-size: 28px; font-weight: 800; color: var(--color-text); line-height: 1; }
.sfd-stat-label { font-size: 12px; color: var(--color-text-sub); margin-top: 4px; }
```

---

## 7. QUICK ACTION CARDS — `.sfd-actions-grid`

4 cards dạng lớn (2 cột × 2 hàng), mỗi card có icon + tiêu đề + mô tả ngắn + link:

```jsx
const ACTIONS = [
  { icon: <SvgEdit />, title: 'Soạn Nội Dung', desc: 'Tạo bài học, từ vựng, ngữ pháp mới', href: '/staff/content' },
  { icon: <SvgBank />, title: 'Ngân Hàng Câu Hỏi', desc: 'Thêm và quản lý câu hỏi trắc nghiệm', href: '/staff/questions' },
  { icon: <SvgTicket />, title: 'Hỗ Trợ Học Viên', desc: 'Xử lý tickets đang chờ phản hồi', href: '/staff/tickets' },
  { icon: <SvgMic />, title: 'Chấm Bài Nói', desc: 'Chấm điểm thủ công bài luyện speaking', href: '/staff/grading' },
];
```

```css
.sfd-actions-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.sfd-action-card {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  border: 1.5px solid transparent;
  transition: all var(--transition);
  text-decoration: none;
  color: inherit;
}
.sfd-action-card:hover { box-shadow: var(--shadow-md); border-color: var(--color-primary-light); transform: translateY(-2px); }
.sfd-action-card__icon { width: 48px; height: 48px; border-radius: var(--radius-md); background: var(--color-primary-bg); display: flex; align-items: center; justify-content: center; color: var(--color-primary); flex-shrink: 0; }
.sfd-action-card__title { font-size: 15px; font-weight: 700; color: var(--color-text); }
.sfd-action-card__desc  { font-size: 12px; color: var(--color-text-sub); margin-top: 2px; }
```

---

## 8. RECENT ACTIVITY — `.sfd-activity`

Danh sách 5 hành động gần nhất (tạo, cập nhật, gửi duyệt, chấm điểm...).

```
┌──────────────────────────────────────────────────────────────────┐
│  Hoạt động gần đây                                               │
├───────────────┬──────────────┬───────────────────────┬──────────┤
│  Thời gian    │  Loại        │  Tiêu đề              │ Trạng thái│
├───────────────┼──────────────┼───────────────────────┼──────────┤
│  2 giờ trước  │  Câu hỏi     │  N5 Kanji: '水' ...   │  Nháp    │
│  Hôm qua      │  Bài học     │  Bài 3 — Kanji N5     │  Chờ     │
│  2 ngày trước │  Đề thi      │  Mock Test N4 Vol.2   │  Đã duyệt│
└───────────────┴──────────────┴───────────────────────┴──────────┘
```

Status badge mapping:

- `draft` → `.sfd-status--draft` (gray, `--color-text-disabled`)
- `pending_review` → `.sfd-status--pending` (amber, `--color-warning`)
- `published` → `.sfd-status--published` (green, `--color-secondary`)
- `rejected` → `.sfd-status--rejected` (red, `--color-error`)

---

## 9. LOADING / ERROR / EMPTY

- **Loading:** skeleton 4 stat cards + 4 action placeholders
- **Error:** banner + "Thử lại"
- **Empty (activity):** chỉ ẩn phần activity, không cần EmptyState — hiện text "Chưa có hoạt động nào."

---

## 10. RESPONSIVE

```css
@media (max-width: 1199px) { .sfd-stat-row { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px)  {
  .sfd-stat-row     { grid-template-columns: 1fr 1fr; }
  .sfd-actions-grid { grid-template-columns: 1fr; }
}
```

---

## 11. ACCESSIBILITY

- [ ] Mỗi stat card: `role="button"` + `aria-label` rõ ràng (ví dụ: "Xem 3 bài đang soạn thảo")
- [ ] Activity table có `<caption>` ẩn: "Hoạt động gần đây"
- [ ] Stat value số lớn có `aria-label` đủ context ("3 bài đang soạn thảo")
