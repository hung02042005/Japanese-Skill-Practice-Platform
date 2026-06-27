# SPEC — Thông Báo (Notifications) — Bell + `/notifications`
>
> **Sprint:** Support — Student Care
> **Prefix:** `ntf-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-support` — UC-30 · `NotificationController` (`/api/notifications`)
> **Liên quan:** `SPEC-support-tickets.md` (reply/đóng ticket → sinh notification), staff broadcast & admin rule chỉ là nguồn tạo — student chỉ **đọc**.

---

## 1. MÔ TẢ

Hệ thống thông báo in-app cho học viên gồm **2 bề mặt**:

1. **NotificationBell** — chuông trên `TopNav`, hiển thị badge số chưa đọc, mở **dropdown panel** xem nhanh 5–8 thông báo mới nhất, đánh dấu đã đọc khi click, nút "Đánh dấu tất cả đã đọc", link "Xem tất cả".
2. **Trang `/notifications`** — danh sách đầy đủ có phân trang, lọc đã đọc/chưa đọc (client-side), đánh dấu đã đọc / tất cả.

Học viên **chỉ đọc** thông báo của chính mình. Nguồn sinh thông báo (staff broadcast async, admin rule tự động, hệ thống reply ticket / chấm điểm speaking) đều ở backend — frontend không tạo notification.

---

## 2. MOCKUP

### 2.1 Bell + dropdown (trong TopNav)

```
TopNav ……………………………  [🔔③]  [Avatar ▾]
                              │
       ┌──────────────────────┴──────────────────────┐
       │  Thông báo            [Đánh dấu tất cả đã đọc]│
       ├──────────────────────────────────────────────┤
       │ ● ⚙️ Ticket của bạn có phản hồi mới          │
       │     Nhân viên vừa phản hồi… · 2 giờ trước    │
       ├──────────────────────────────────────────────┤
       │   ⭐ Bài nói đã được chấm điểm                │
       │     Điểm của bạn: 82/100 · hôm qua            │
       ├──────────────────────────────────────────────┤
       │ ● ⚠️ Bảo trì hệ thống 23:00                  │
       │     Hệ thống sẽ bảo trì… · 2 ngày trước       │
       ├──────────────────────────────────────────────┤
       │                Xem tất cả →                   │
       └──────────────────────────────────────────────┘
```

`●` = chấm chưa đọc (pink). Dòng chưa đọc nền `var(--color-primary-bg)`.

### 2.2 Trang `/notifications`

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                           │
├──────────────────────────────────────────────────────────────────┤
│  ← Quay lại Dashboard                                            │
│  Thông Báo                          [Đánh dấu tất cả đã đọc]     │
│  [Tất cả]  [Chưa đọc ③]                                         │
│  ──────────────────────────────────────────────────────────────  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ ● ⚙️  Ticket của bạn có phản hồi mới        2 giờ trước    │ │
│  │      Nhân viên hỗ trợ vừa phản hồi ticket: Lỗi audio…       │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │   ⭐  Bài nói của bạn đã được chấm điểm     hôm qua        │ │
│  │      Điểm của bạn: 82/100                                   │ │
│  └────────────────────────────────────────────────────────────┘ │
│  [‹ Trước]   Trang 1 / 4   [Sau ›]                              │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
components/notifications/
├── NotificationBell.jsx       ← chuông + badge + dropdown (mount trong TopNav)
├── NotificationBell.css
├── NotificationItem.jsx        ← 1 dòng thông báo (dùng chung bell + page)
└── NotificationTypeIcon.jsx    ← icon theo notificationType (bảng §8)
pages/notifications/
├── Notifications.jsx           ← trang đầy đủ + phân trang
└── Notifications.css
context/ (hoặc store/) NotificationContext.jsx  ← optional: cache unreadCount toàn app
```

> `NotificationBell` được đặt trong `components/layout/TopNav.jsx`, bên trái `UserAvatar`. Vì badge số chưa đọc xuất hiện trên mọi trang student, nên đặt state ở **context** (hoặc Redux slice) để chia sẻ; nếu giữ tối giản, fetch độc lập trong bell mỗi lần mount + polling nhẹ.

---

## 4. STATE

### 4.1 `NotificationBell.jsx`

```js
const [open,        setOpen]    = useState(false);
const [items,       setItems]   = useState([]);     // 5–8 mục mới nhất
const [unread,      setUnread]  = useState(0);
const [isLoading,   setLoading] = useState(false);
const [error,       setError]   = useState('');
const panelRef = useRef(null);   // click-outside để đóng
const POLL_MS  = 60000;          // refresh unread mỗi 60s khi tab active
```

### 4.2 `Notifications.jsx`

```js
const [items,      setItems]   = useState([]);
const [unread,     setUnread]  = useState(0);
const [isLoading,  setLoading] = useState(true);
const [error,      setError]   = useState('');
const [tab,        setTab]     = useState('all');   // 'all' | 'unread' (lọc client-side)
const [page,       setPage]    = useState(0);        // 0-based
const [totalPages, setTotal]   = useState(1);
const PAGE_SIZE = 20;
```

---

## 5. API CALLS — thêm vào `api/studentService.js`

```js
// ─── Thông báo — UC-30 (NotificationController) ───────────────────────────────

// Danh sách thông báo của tôi. Trả { content, totalElements, totalPages, unreadCount }.
export async function getMyNotifications({ page = 0, size = 20 } = {}) {
  const res = await api.get('/notifications', { params: { page, size } });
  return res.data.data;
}

// Đánh dấu 1 thông báo đã đọc. 403 nếu không phải của mình.
export async function markNotificationRead(notificationId) {
  const res = await api.post(`/notifications/${notificationId}/read`);
  return res.data; // { status, message, data: null }
}

// Đánh dấu tất cả đã đọc → { markedCount }.
export async function markAllNotificationsRead() {
  const res = await api.post('/notifications/read-all');
  return res.data.data; // { markedCount }
}
```

> **Lưu ý:** backend KHÔNG có endpoint riêng chỉ trả `unreadCount`; số chưa đọc nằm trong field `unreadCount` của `GET /notifications`. Bell gọi `getMyNotifications({ page:0, size:8 })` rồi đọc cả `content` (preview) lẫn `unreadCount` trong **một** request (tránh chatty API — DESIGN/Integration anti-pattern).

### 5.1 Hợp đồng dữ liệu (`NotificationResponse`)

```jsonc
{
  "notificationId": 88,
  "title": "Ticket của bạn có phản hồi mới",
  "content": "Nhân viên hỗ trợ vừa phản hồi ticket: Lỗi audio",
  "notificationType": "system",   // news|warning|promotion|system|achievement|reminder
  "channel": "in_app",            // in_app|email|both
  "isRead": false,
  "isAuto": true,
  "ruleKey": "ticket_reply_12",
  "readAt": null,
  "createdAt": "2026-06-26T08:00:00"
}
```

Field `null` bị lược (`@JsonInclude(NON_NULL)`) → dùng optional chaining; `isRead`/`isAuto` là `Boolean`.

---

## 6. NOTIFICATION BELL — HÀNH VI

- **Mount:** fetch `getMyNotifications({ page:0, size:8 })` → set `items`, `unread`.
- **Badge:** hiện khi `unread > 0`; số > 9 hiển thị `9+`. Badge nền `var(--color-primary)`, chữ trắng, `border-radius: var(--radius-full)`.
- **Polling:** mỗi `POLL_MS` (60s) refetch khi `document.visibilityState === 'visible'`; cleanup interval trong `useEffect` return (tránh memory leak — React anti-pattern).
- **Mở dropdown:** toggle `open`; click ngoài panel (`panelRef`) hoặc Escape → đóng.
- **Click 1 item chưa đọc:** optimistic `isRead=true` + `unread--` → gọi `markNotificationRead(id)`; nếu lỗi → revert + toast. Sau đó điều hướng theo `ruleKey` (§7).
- **"Đánh dấu tất cả đã đọc":** optimistic set tất cả `isRead=true`, `unread=0` → `markAllNotificationsRead()`; lỗi → refetch.
- **"Xem tất cả →":** `navigate('/notifications')`, đóng dropdown.

---

## 7. ĐIỀU HƯỚNG THEO `ruleKey` (deep-link)

`ruleKey` được backend đặt theo nguồn (xem `SupportTicketService`):

| Tiền tố `ruleKey` | Ý nghĩa | Điều hướng khi click |
|:---|:---|:---|
| `ticket_reply_{replyId}` | Staff phản hồi ticket | `/support/tickets/{ticketId}`* |
| `ticket_resolved_{ticketId}` | Ticket đã đóng | `/support/tickets/{ticketId}` |
| `speaking_graded_{submissionId}` | Bài nói đã chấm | `/speaking` (hoặc trang kết quả nếu có) |
| khác / broadcast | Tin tức, cảnh báo, khuyến mãi | Không điều hướng — chỉ mở rộng nội dung in-app |

> *`ticket_reply_*` chứa `replyId` chứ không phải `ticketId`. Vì payload notification hiện không kèm `ticketId`, frontend **không** suy ra ticketId từ `ruleKey`. Hành vi an toàn: click các notification loại ticket → điều hướng tới trang danh sách `/support` (người dùng tự mở ticket có badge "phản hồi mới"). Nếu sau này backend bổ sung `targetUrl`/`ticketId` vào DTO, cập nhật mapping trực tiếp. **Không hard-code parse fragile.**

---

## 8. NOTIFICATION TYPE ICON (đồng bộ với `SPEC-staff-notifications.md`)

| Type | Icon | Background | Text |
|:---|:---|:---|:---|
| `news` | 📰 newspaper | `#E3F2FD` | `#1565C0` |
| `warning` | ⚠️ triangle | `var(--color-accent-bg)` | `var(--color-warning)` |
| `promotion` | 🎁 gift | `var(--color-secondary-bg)` | `var(--color-secondary)` |
| `system` | ⚙️ gear | `#F0EDEB` | `var(--color-text-sub)` |
| `achievement` | ⭐ star | `var(--color-accent-bg)` | `#B7670A` |
| `reminder` | 🔔 bell | `var(--color-primary-bg)` | `var(--color-primary-dark)` |

Icon vẽ bằng `<svg>` inline `currentColor` (không dùng emoji thật ngoài mockup), kích thước 20px trong khung tròn 36px nền theo cột Background.

---

## 9. CSS (khung chính)

```css
/* ===== Notification Bell ===== */
.ntf-bell { position: relative; }
.ntf-bell-btn {
  width: 44px; height: 44px; border-radius: var(--radius-full);
  background: transparent; border: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center; color: var(--color-text-sub);
}
.ntf-bell-btn:hover { background: var(--color-primary-bg); color: var(--color-primary); }
.ntf-badge {
  position: absolute; top: 4px; right: 4px; min-width: 18px; height: 18px;
  padding: 0 5px; border-radius: var(--radius-full);
  background: var(--color-primary); color: #fff;
  font-size: 11px; font-weight: 800; line-height: 18px; text-align: center;
}

/* Dropdown panel — Level 3 floating */
.ntf-panel {
  position: absolute; top: 52px; right: 0; width: 360px; max-height: 70vh; overflow-y: auto;
  background: var(--color-card); border-radius: var(--radius-lg);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12); z-index: 9999;
}
.ntf-panel-head { display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; border-bottom: 1px solid var(--color-border); }
.ntf-panel-title { font-size: 15px; font-weight: 700; color: var(--color-text); }
.ntf-markall { font-size: 12px; font-weight: 600; color: var(--color-primary); background: none; border: none; cursor: pointer; }

/* ===== Notification Item (dùng chung bell + page) ===== */
.ntf-item { display: flex; gap: 12px; padding: 12px 16px; border-bottom: 1px solid var(--color-border);
  cursor: pointer; transition: background var(--transition); }
.ntf-item:hover  { background: var(--color-bg); }
.ntf-item--unread { background: var(--color-primary-bg); }
.ntf-item--unread:hover { background: var(--color-primary-bg); filter: brightness(0.985); }
.ntf-dot   { width: 8px; height: 8px; border-radius: var(--radius-full); background: var(--color-primary); flex-shrink: 0; margin-top: 6px; }
.ntf-icon  { width: 36px; height: 36px; border-radius: var(--radius-full); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.ntf-body  { flex: 1; min-width: 0; }
.ntf-title { font-size: 14px; font-weight: 700; color: var(--color-text); }
.ntf-text  { font-size: 13px; color: var(--color-text-sub); margin-top: 2px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.ntf-time  { font-size: 11px; color: var(--color-text-disabled); margin-top: 4px; }
.ntf-footer { text-align: center; padding: 12px; }
.ntf-viewall { font-size: 13px; font-weight: 700; color: var(--color-primary); text-decoration: none; }

/* ===== Page ===== */
.ntf-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.ntf-page-body { flex: 1; max-width: 760px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 12px; box-sizing: border-box; }
.ntf-page-card { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm, 0 2px 8px rgba(0,0,0,0.07)); overflow: hidden; }

@media (max-width: 767px) {
  .ntf-panel { position: fixed; top: 64px; left: 8px; right: 8px; width: auto; }
  .ntf-page-body { padding: 16px 16px 32px; }
}
@media (prefers-reduced-motion: reduce) { .ntf-bell *, .ntf-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 10. 3 TRẠNG THÁI

| Trạng thái | Bell dropdown | Trang `/notifications` |
|:---|:---|:---|
| **Loading** | Skeleton 4 dòng trong panel | Skeleton 6 dòng |
| **Error** | Dòng "Không tải được thông báo" + nút thử lại nhỏ | Banner lỗi + retry |
| **Empty** | "Bạn chưa có thông báo nào" + Saku-chan `sm` `idle` | `<EmptyState title="Chưa có thông báo" subtitle="Khi có cập nhật mới, SakuJi sẽ báo cho bạn ở đây." mascotVariant="idle" mascotSize={140} />` |

Tab "Chưa đọc" rỗng dù "Tất cả" có dữ liệu → EmptyState riêng "Bạn đã đọc hết thông báo!" mascot `happy`.

---

## 11. INTERACTIONS / FLOW

```
TopNav mount → Bell fetch (page0,size8) → badge unread
Bell click   → mở panel
  item chưa đọc click → optimistic read + unread-- → markNotificationRead → điều hướng (§7)
  "đánh dấu tất cả"   → optimistic all read → markAllNotificationsRead
  "Xem tất cả"        → navigate('/notifications')
Polling 60s (tab visible) → refetch unread

/notifications mount → getMyNotifications(page) → list + unreadCount
  tab all/unread → lọc client-side trên content trang hiện tại
  phân trang → setPage → refetch
```

> Lọc "Chưa đọc" là **client-side trên trang hiện tại** (backend không có filter `isRead`). Ghi chú rõ giới hạn: chỉ lọc trong các mục đã tải. Nếu cần lọc toàn cục, đề xuất backend bổ sung `?unread=true` (out of scope spec này).

---

## 12. DOMAIN RULES

- Học viên chỉ đọc thông báo của mình; `markNotificationRead` của notification người khác → `403` (đã chặn backend). Optimistic update phải revert khi nhận lỗi.
- `notificationType`, `channel` là enum chuỗi **chữ thường**; so khớp đúng case.
- `channel='email'`/`'both'` chỉ là metadata gửi mail phía backend — **không** ảnh hưởng hiển thị in-app; mọi notification trong list đều là in-app feed.
- `isAuto`/`ruleKey` dùng cho điều hướng & debug; không bắt buộc hiển thị.
- Số `unreadCount` luôn lấy từ backend (`GET /notifications`), không tự đếm client để tránh lệch — sau optimistic update vẫn đồng bộ lại ở lần fetch kế tiếp.

---

## 13. ACCESSIBILITY

- [ ] Bell button: `aria-label="Thông báo, {unread} chưa đọc"`, `aria-expanded`, `aria-haspopup="menu"`.
- [ ] Badge số: ẩn với screen reader (`aria-hidden`) vì đã nằm trong `aria-label` của bell.
- [ ] Panel: `role="menu"`; mỗi item `role="menuitem"` + `tabIndex`; điều hướng phím mũi tên; Escape đóng + focus trả về bell.
- [ ] Click-outside không phá keyboard nav; focus trap nhẹ trong panel khi mở.
- [ ] Item chưa đọc: `aria-label` kèm "chưa đọc"; chấm `●` là `aria-hidden`.
- [ ] Trang: tabs `role="tablist"`/`role="tab"` + `aria-selected`.
- [ ] Toast lỗi optimistic dùng `role="alert"`; polling không gây announce ồn ào (cập nhật badge im lặng).
- [ ] Tôn trọng `prefers-reduced-motion` cho animation chuông/petal.
```
