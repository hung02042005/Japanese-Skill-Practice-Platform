# SPEC — Staff Gửi Thông Báo (Notifications)
> **Feature ID:** `feat-staff` | **Page:** `StaffNotifications`
> **Route:** `/staff/notifications`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-03
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Backend ref:** `feat-support` — UC-30 (Send Notifications)

---

## 1. TỔNG QUAN TRANG

Trang soạn thảo và quản lý thông báo hệ thống gửi đến học viên. Staff soạn nội dung, chọn kênh (in-app / email / cả hai), chọn nhóm đối tượng theo JLPT level, và lên lịch gửi (ngay lập tức hoặc hẹn giờ). Xử lý backend async — gửi xong trả `jobId`, không block UI.

**Prefix CSS:** `nfs-`
**activeTab:** `'staff-notifications'`
**Guard:** `<StaffRoute>`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-notifications"                │
├──────────────────────────────────────────────────────────────┤
│  <main className="nfs-body">                                 │
│                                                              │
│  [Page Header: "Thông Báo"  [+ Soạn thông báo mới]]          │
│                                                              │
│  [Stats Row: Đã gửi hôm nay / Tổng học viên nhận]           │
│                                                              │
│  [Notification History Table]                                │
│  ┌──────────────────────────────────────────────────────┐    │
│  │ Tiêu đề │ Loại │ Kênh │ Đối tượng │ Thời gian │ TT │    │
│  ├─────────┼──────┼──────┼───────────┼───────────┼────┤    │
│  │ Bảo trì │ ⚠️  │ Cả hai│ Tất cả   │ 01/06 23:00│✓  │    │
│  └──────────────────────────────────────────────────────┘    │
│                                                              │
│  [Pagination]                                                │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffNotifications.jsx
pages/staff/StaffNotifications.css
components/staff/NotificationComposeModal.jsx   ← modal soạn thông báo (>60 dòng)
```

---

## 4. STATE

```js
const [notifications, setItems]    = useState([]);
const [isLoading,     setLoading]  = useState(true);
const [error,         setError]    = useState('');
const [currentPage,   setPage]     = useState(1);
const [totalPages,    setTotal]    = useState(1);
const [showCompose,   setCompose]  = useState(false);
const PAGE_SIZE = 20;
```

---

## 5. API — `staffService.js`

```js
// Lấy lịch sử thông báo đã gửi
export async function getNotificationHistory({ page = 0, size = 20 } = {}) {
  const res = await api.get('/staff/notifications', { params: { page, size } });
  return res.data.data;
  // { content: [{ notificationId, title, notificationType, channel, targetLevel, sentAt, deliveredCount }], totalPages }
}

// Gửi thông báo mới (async job)
export async function sendNotification(data) {
  const res = await api.post('/staff/notifications', data);
  return res.data.data; // { jobId }
  // data: { title, content, notificationType, channel, targetJlptLevel?, scheduledAt? }
}
```

---

## 6. NOTIFICATION HISTORY TABLE

| Cột | Nội dung |
|:---|:---|
| Tiêu đề | text truncate 60 ký tự |
| Loại | `<NotificationTypeBadge type={n.notificationType} />` |
| Kênh | `<ChannelPill channel={n.channel} />` |
| Đối tượng | Level (N5/N4/... hoặc "Tất cả") |
| Gửi lúc | "ngay lập tức" hoặc thời gian lên lịch |
| Đã gửi | `deliveredCount` học viên |

### NotificationTypeBadge

| Type | Icon SVG | Background | Text |
|:---|:---|:---|:---|
| `news` | 📰 newspaper | `#E3F2FD` | `#1565C0` |
| `warning` | ⚠️ triangle | `--color-accent-bg` | `--color-warning` |
| `promotion` | 🎁 gift | `--color-secondary-bg` | `--color-secondary` |
| `system` | ⚙️ gear | `#F0EDEB` | `--color-text-sub` |
| `achievement` | ⭐ star | `--color-accent-bg` | `#B7670A` |
| `reminder` | 🔔 bell | `--color-primary-bg` | `--color-primary-dark` |

### ChannelPill

| Channel | Label | Style |
|:---|:---|:---|
| `in_app` | In-app | outline gray |
| `email` | Email | outline blue |
| `both` | In-app + Email | solid primary-bg |

---

## 7. NOTIFICATION COMPOSE MODAL (`NotificationComposeModal`)

```
┌──────────────────────────────────────────────────────┐
│  Soạn thông báo mới                           [✕]   │
├──────────────────────────────────────────────────────┤
│                                                      │
│  [Tiêu đề *]                                         │
│  [________________________________]                  │
│                                                      │
│  [Nội dung *]                                        │
│  [________________________________]                  │
│  [________________________________]  (textarea 4r)   │
│  [________________________________]                  │
│                                                      │
│  [Loại thông báo *]                                  │
│  ○ Tin tức  ○ Cảnh báo  ○ Khuyến mãi               │
│  ○ Hệ thống  ○ Thành tích  ○ Nhắc nhở               │
│                                                      │
│  [Kênh gửi *]                                        │
│  ○ Chỉ In-app  ○ Chỉ Email  ○ Cả hai                │
│                                                      │
│  [Đối tượng nhận]                                    │
│  ● Tất cả học viên                                  │
│  ○ Theo Level JLPT:  [N5▼]                           │
│                                                      │
│  [Thời gian gửi]                                     │
│  ● Gửi ngay                                         │
│  ○ Hẹn giờ:  [2026-06-05]  [23:00]                  │
│                                                      │
│  ─────────────────────────────────────────────────   │
│  Ước tính: ~1,240 học viên sẽ nhận thông báo này    │
│                                                      │
│                       [Hủy] [Gửi thông báo →]       │
└──────────────────────────────────────────────────────┘
```

### State (trong modal)

```js
const [form, setForm] = useState({
  title:            '',
  content:          '',
  notificationType: 'news',
  channel:          'in_app',
  targetAll:        true,
  targetLevel:      'N5',
  scheduleNow:      true,
  scheduledDate:    '',
  scheduledTime:    '',
});
const [estimatedCount, setEstimated] = useState(null);
const [isSending,      setSending]   = useState(false);
const [sendError,      setSendError] = useState('');
```

### Ước tính số người nhận

Fetch khi `targetAll` hoặc `targetLevel` thay đổi:

```js
useEffect(() => {
  // GET /api/staff/notifications/estimate?level={targetLevel}&all={targetAll}
  // returns: { count: 1240 }
  estimateRecipients({ all: form.targetAll, level: form.targetLevel })
    .then((r) => setEstimated(r.count))
    .catch(() => setEstimated(null));
}, [form.targetAll, form.targetLevel]);
```

### Validation client-side:
- `title`: 1–255 ký tự
- `content`: không rỗng
- Nếu `scheduleNow = false`: `scheduledDate` và `scheduledTime` phải là tương lai

### Submit flow

1. Build payload: `{ title, content, notificationType, channel, targetJlptLevel (nếu không all), scheduledAt (nếu hẹn giờ) }`
2. Gọi `sendNotification(payload)` → nhận `{ jobId }`
3. Đóng modal
4. Toast success: "Đang gửi thông báo (Job #job_notification_951). Quá trình diễn ra trong nền."
5. Refresh danh sách (gọi lại `getNotificationHistory()`)

**Không** poll job status trên UI — gửi là async fire-and-forget từ phía frontend.

### Confirm dialog trước khi gửi toàn bộ

Nếu `targetAll = true` và `estimatedCount > 500`, hiển thị confirm dialog:

```
"Bạn sắp gửi thông báo đến {estimatedCount} học viên.
Hành động này không thể hoàn tác.
[Hủy]  [Xác nhận gửi]"
```

---

## 8. STATS ROW

```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ Đã gửi hôm nay  │  │ Tuần này         │  │ Tổng học viên   │
│     3 thông báo │  │    12 thông báo  │  │   nhận tuần này │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

Lấy từ response header của `getNotificationHistory()` (server trả thêm field `todayCount`, `weekCount`).

---

## 9. LOADING / ERROR / EMPTY

- **Loading:** skeleton 5 hàng bảng + skeleton 3 stat cards
- **Error:** error banner + retry
- **Empty:** `<EmptyState title="Chưa có thông báo nào" subtitle="Soạn và gửi thông báo đầu tiên đến học viên." mascotVariant="idle" mascotSize={120}>`

---

## 10. CSS KEY CLASSES

```css
.nfs-page  { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.nfs-body  { flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.nfs-stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.nfs-stat-card {
  background: var(--color-card); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm); padding: 20px;
}
.nfs-stat-value { font-size: 26px; font-weight: 800; color: var(--color-text); }
.nfs-stat-label { font-size: 12px; color: var(--color-text-sub); margin-top: 4px; }

.nfs-channel-pill {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 2px 10px; border-radius: var(--radius-full);
  font-size: 11px; font-weight: 700;
}
.nfs-channel--in_app { border: 1.5px solid var(--color-border); color: var(--color-text-sub); }
.nfs-channel--email  { border: 1.5px solid #1565C0; color: #1565C0; }
.nfs-channel--both   { background: var(--color-primary-bg); border: 1.5px solid var(--color-primary-light); color: var(--color-primary-dark); }
```

---

## 11. RESPONSIVE

```css
@media (max-width: 1199px) { .nfs-body { padding: 24px 20px 40px; } }
@media (max-width: 767px)  {
  .nfs-body       { padding: 16px 16px 32px; }
  .nfs-stats-row  { grid-template-columns: 1fr 1fr; }
  /* ẩn cột "Đã gửi" trên mobile */
  .nfs-col-delivered { display: none; }
}
```

---

## 12. ACCESSIBILITY

- [ ] Radio groups trong modal: `<fieldset>` + `<legend>` cho mỗi nhóm (Loại, Kênh, Đối tượng, Thời gian)
- [ ] Estimated count: `aria-live="polite"` để screen reader thông báo khi số cập nhật
- [ ] Confirm dialog: `role="alertdialog"`, `aria-modal="true"`, focus trap
- [ ] Date/time inputs hẹn giờ: `min={new Date().toISOString().slice(0,10)}` để chặn ngày quá khứ
- [ ] Modal `NotificationComposeModal`: `role="dialog"`, `aria-modal="true"`, Escape đóng
