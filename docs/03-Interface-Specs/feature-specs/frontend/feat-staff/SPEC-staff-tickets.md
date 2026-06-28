# SPEC — Staff Xử Lý Ticket Được Giao (Support Tickets)
>
> **Feature ID:** `feat-staff` | **Page:** `StaffTickets`
> **Route:** `/staff/tickets`
> **Version:** 2.0 | **Status:** Draft (thay thế v1.0 — căn lại đúng backend đã port)
> **Author:** Team | **Last Updated:** 2026-06-27
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-support` — UC-29 · `StaffSupportController` (`/api/staff/tickets`)
> **Liên quan:** `SPEC-manager-tickets.md` (manager phân công), `SPEC-support-tickets.md` (student)

---

## 0. KHÁC BIỆT SO VỚI v1.0 (⚠️ ĐỌC TRƯỚC)

v1.0 mô tả các endpoint **không tồn tại** ở backend đã port. Trang hiện tại ([StaffTickets.jsx](../../../apps/frontend/src/pages/staff/StaffTickets.jsx)) **đang chạy mock 100%**. v2.0 căn lại đúng `StaffSupportController`:

| v1.0 (SAI) | v2.0 (ĐÚNG backend) |
|:---|:---|
| `PUT /staff/tickets/{id}/status` | ❌ Không tồn tại — staff reply **tự** chuyển `→ in_progress` |
| `PUT /staff/tickets/{id}/assign` `{ staffId }` | ➡️ **Manager-only**, `POST .../assign` `{ assignToStaffId }` → xem `SPEC-manager-tickets.md`. Staff thường gọi sẽ `403`. |
| `reply { message }` đổi status thủ công | `POST .../reply` `{ message, attachmentUrl? }` — status đổi tự động |
| `senderType` | `senderRole` = `STUDENT` \| `STAFF` |
| dropdown "Đổi trạng thái" khi reply | **Bỏ** — không có endpoint set status; chỉ có nút **Đóng** (`→ resolved`) |

---

## 1. TỔNG QUAN TRANG

Màn của **nhân viên hỗ trợ (STAFF)**: xem ticket, đọc luồng hội thoại và **phản hồi ticket mình được giao**. Không phân công (đó là việc của Manager). Layout **master-detail**: cột trái danh sách, cột phải chi tiết + thread + form trả lời.

**Quyền (backend tự enforce):** staff chỉ reply/đóng được ticket có `assignedTo == mình` (hoặc nếu là Staff Manager). Reply ticket không được giao → `403`. Frontend phản chiếu: ẩn/disable form khi không phải người được giao.

**Prefix CSS:** `tkt-`
**activeTab:** `'staff-tickets'`
**Guard:** `<StaffRoute>`
**TopNav:** `StaffTopNav`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-tickets"                          │
├──────────────────────────────────────────────────────────────────┤
│  <main className="tkt-body">                                     │
│  ┌────────────────────┐  ┌──────────────────────────────────┐   │
│  │  TICKET LIST (360)  │  │  TICKET DETAIL (flex:1)          │   │
│  │ [Search]            │  │ [Subject] [Priority][Status]     │   │
│  │ [Được giao cho tôi] │  │ Học viên: Nguyễn A · N3          │   │
│  │ [Đang xử lý][Tất cả]│  │ Được giao: NV (Bạn) / Trần C     │   │
│  │ ┌────────────────┐  │  │ ┌─ Thread (chat) ──────────────┐ │   │
│  │ │🔴#45 Lỗi audio │  │  │ │ [student]… [staff]…          │ │   │
│  │ │Nguyễn A ·2h    │  │  │ └──────────────────────────────┘ │   │
│  │ └────────────────┘  │  │ [Reply form]  [Đóng ticket]      │   │
│  └────────────────────┘  └──────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffTickets.jsx     ← thay máu từ mock → API thật
pages/staff/StaffTickets.css
components/staff/TicketList.jsx   ← danh sách trái (cập nhật field: senderRole, status backend)
components/staff/TicketDetail.jsx ← chi tiết + thread + reply (bỏ dropdown status)
// Dùng lại: components/support/TicketStatusBadge.jsx, PriorityPill.jsx
```

---

## 4. STATE

```js
const [tickets,       setTickets]  = useState([]);
const [selectedId,    setSelected] = useState(null);
const [detail,        setDetail]   = useState(null);   // TicketDetailResponse + replies[]
const [isLoadingList, setLoadList] = useState(true);
const [isLoadingDtl,  setLoadDtl]  = useState(false);
const [error,         setError]    = useState('');
const [statusFilter,  setStatus]   = useState('');     // ''|'open'|'assigned'|'in_progress'|'resolved'|'closed'
const [search,        setSearch]   = useState('');
const [debounced,     setDebounced]= useState('');
const [page,          setPage]     = useState(0);       // 0-based khớp backend
const [totalPages,    setTotal]    = useState(1);
const [replyText,     setReply]    = useState('');
const [attachUrl,     setAttach]   = useState('');
const [isSending,     setSending]  = useState(false);
const [isClosing,     setClosing]  = useState(false);
const timerRef        = useRef(null);
const threadBottomRef = useRef(null);
const PAGE_SIZE = 20;
```

---

## 5. API — `staffService.js` (cần BỔ SUNG — hiện chưa có hàm ticket nào)

```js
import api from './authService';

// Danh sách ticket. Trả { content, totalElements, totalPages }.
export async function getTickets({ status, category, priority, q, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (status)   params.status   = status;
  if (category) params.category = category;
  if (priority) params.priority = priority;
  if (q)        params.q        = q;
  const res = await api.get('/staff/tickets', { params });
  return res.data.data;
}

// Chi tiết + replies[].
export async function getTicketDetail(ticketId) {
  const res = await api.get(`/staff/tickets/${ticketId}`);
  return res.data.data; // TicketDetailResponse
}

// Phản hồi. Backend tự chuyển OPEN/ASSIGNED → IN_PROGRESS + notify student.
// 403 nếu không phải người được giao (và không phải manager).
export async function replyTicket(ticketId, { message, attachmentUrl } = {}) {
  const res = await api.post(`/staff/tickets/${ticketId}/reply`, { message, attachmentUrl });
  return res.data.data; // TicketReplyResponse
}

// Đóng ticket → RESOLVED + audit log + notify student.
export async function closeTicket(ticketId) {
  const res = await api.post(`/staff/tickets/${ticketId}/close`);
  return res.data.data; // TicketResponse
}
```

> **KHÔNG** thêm hàm `assignTicket`/`updateTicketStatus` ở màn staff — assign là manager-only (`SPEC-manager-tickets.md`), và không có endpoint set-status.

### 5.1 Hợp đồng dữ liệu

`TicketResponse` / `TicketDetailResponse` / `TicketReplyResponse`: xem `SPEC-support-tickets.md §5.1`. Lưu ý:
- `status`: `open | assigned | in_progress | resolved | closed` (chữ thường).
- Reply: `senderRole` = `STUDENT | STAFF` (KHÔNG phải `senderType`); `senderName`, `message`, `attachmentUrl?`, `createdAt`.
- Field `null` bị lược (`@JsonInclude(NON_NULL)`) → dùng optional chaining.

---

## 6. TICKET LIST (cột trái)

### Tab lọc

```
[Tất cả] [Chưa giao (open)] [Đã giao (assigned)] [Đang xử lý (in_progress)] [Đã giải quyết (resolved)]
```

Mỗi tab badge đếm. Card: `PriorityPill` + `#id` + subject (truncate) + "{studentName} · {relativeTime}". Khi `assignedToStaffName` có → meta nhỏ "Giao: {tên}".

Priority dot & active card: như v1.0 (nền `--color-primary-bg`, border-left `3px solid --color-primary`). Search debounce 400ms → refetch `q`.

---

## 7. TICKET DETAIL (cột phải)

### Header
Subject (18px/700) · `PriorityPill` · `TicketStatusBadge` · "Học viên: {name} · {level} · Gửi {createdAt}" · "Được giao: {assignedToStaffName ?? '— chưa giao'}".

### Thread (chat)
Bubble theo `senderRole`:
- `STAFF` → bên phải, nền `var(--color-primary-bg)`, viền `--color-primary-light`. Nếu là chính mình hiển thị tên + "(Bạn)".
- `STUDENT` → bên trái, nền `var(--color-bg)`, viền `--color-border`, kèm `UserAvatar`.
- Nội dung gốc `detail.content` = bubble student đầu tiên.

Auto-scroll xuống cuối sau load + sau gửi reply (tôn trọng `prefers-reduced-motion`).

### Reply form
```
[Textarea "Nhập phản hồi cho học viên…", 3 rows]
[🔗 Đính kèm URL (tùy chọn)]
                         [Đóng ticket]   [Gửi phản hồi →]
```

**Submit reply:** `replyTicket(selectedId, { message, attachmentUrl })` → append vào thread → reset → scroll → cập nhật card (status có thể `→ in_progress`, lastReplyAt). Disabled khi `replyText.trim()===''` hoặc `isSending` hoặc ticket `resolved/closed`.

**Đóng:** confirm dialog → `closeTicket(selectedId)` → `status='resolved'` → khóa form.

**Quyền:** nếu reply trả `403` → toast "Bạn không được giao ticket này" + (nếu muốn) ẩn form. Có thể chủ động ẩn form khi `assignedToStaffId` khác id staff hiện tại và staff không phải manager — nhưng **luôn** xử lý `403` từ backend làm nguồn chân lý.

### Banner khóa
```jsx
{['resolved','closed'].includes(detail.status) && (
  <div className="tkt-closed-banner" role="status">
    Ticket đã đóng. Không thể gửi thêm phản hồi.
  </div>
)}
```

---

## 8. TRẠNG THÁI CHƯA CHỌN TICKET

```jsx
<div className="tkt-empty-detail">
  <SakuChan variant="idle" size={80} />
  <p>Chọn một ticket để bắt đầu hỗ trợ</p>
</div>
```

---

## 9. STATUS BADGE & PRIORITY PILL

Dùng chung `components/support/TicketStatusBadge` + `PriorityPill` (bảng màu `SPEC-support-tickets.md §9`). Bổ sung nhãn `assigned` = "Đã tiếp nhận".

---

## 10. LOADING / ERROR / EMPTY

- Loading list: skeleton 5 card. Loading detail: skeleton 3 bubble.
- Error list/detail: banner + retry. Reply `403`/`409`: toast message backend.
- Empty list: `<EmptyState title="Không có ticket nào" subtitle="Tất cả yêu cầu đã được xử lý!" mascotVariant="celebrate" mascotSize={120} />`

---

## 11. RESPONSIVE

```css
@media (max-width: 1199px) { .tkt-body { padding: 24px 20px 40px; } }
@media (max-width: 767px) {
  .tkt-body     { padding: 0; flex-direction: column; }
  .tkt-list-col { width: 100%; border-right: none; border-bottom: 1px solid var(--color-border); max-height: 40vh; overflow-y: auto; }
  .tkt-detail-col { flex: 1; }
}
@media (prefers-reduced-motion: reduce) { .tkt-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 12. ACCESSIBILITY

- [ ] List `role="list"`, card `role="listitem"` + `aria-selected`; tab lọc `role="tablist"`.
- [ ] Thread `aria-live="polite"`.
- [ ] Textarea reply: `<label htmlFor>` + `aria-required` khi ticket mở.
- [ ] Closed banner `role="status"`; confirm đóng `role="alertdialog"` + focus trap + Escape.
- [ ] Auto-scroll `behavior:'auto'` khi `prefers-reduced-motion`.
- [ ] `addToast(type, message)` đúng signature của `useToast` (v1.0 dùng sai `addToast({type,message})`).
```
