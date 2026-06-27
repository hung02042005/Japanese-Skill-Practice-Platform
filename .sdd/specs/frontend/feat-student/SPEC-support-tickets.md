# SPEC — Hỗ Trợ / Ticket (`/support`)
>
> **Sprint:** Support — Student Care
> **Prefix:** `tks-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-support` — UC-29 · `SupportController` (`/api/support`)
> **Liên quan:** `SPEC-notifications.md` (phản hồi ticket → tạo notification cho student)

---

## 1. MÔ TẢ TRANG

Trang gửi và theo dõi ticket hỗ trợ của học viên. Học viên tạo ticket mới (tiêu đề, nội dung, danh mục, mức độ ưu tiên), xem danh sách ticket của mình lọc theo trạng thái, mở chi tiết để đọc luồng hội thoại (thread) với nhân viên hỗ trợ, gửi phản hồi, và tự đóng ticket của mình.

Toàn bộ quyền chỉ trong phạm vi **ticket của chính học viên** — backend đã chặn ở `SupportController` bằng `@PreAuthorize("hasRole('STUDENT')")` + ownership check (trả `403` nếu mở ticket người khác). Frontend **không** được tự suy quyền; mọi lỗi quyền hiển thị theo `403` từ backend.

**Bố cục:** một trang danh sách (`/support`) + một trang chi tiết (`/support/tickets/:ticketId`) + modal tạo ticket. Không dùng master-detail vì luồng student đơn giản hơn staff.

---

## 2. MOCKUP

### 2.1 Danh sách ticket — `/support`

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ← Quay lại Dashboard                                            │
│                                                                  │
│  Hỗ Trợ Của Tôi                       [+ Tạo yêu cầu mới]        │
│  ──────────────────────────────────────────────────────────────  │
│                                                                  │
│  [Tất cả] [Đang mở] [Đang xử lý] [Đã giải quyết] [Đã đóng]      │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 🔴 KHẨN  · Kỹ thuật                       [Đang mở]        │ │
│  │ Lỗi không nghe được audio bài shadowing                    │ │
│  │ 3 phản hồi · cập nhật 2 giờ trước                          │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 🟢 BÌNH THƯỜNG · Tài khoản              [Đã giải quyết]    │ │
│  │ Hỏi về cách nâng cấp VIP                                    │ │
│  │ 1 phản hồi · cập nhật 01/06/2026                           │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  [‹ Trước]   Trang 1 / 3   [Sau ›]                              │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 Chi tiết ticket — `/support/tickets/:ticketId`

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                           │
├──────────────────────────────────────────────────────────────────┤
│  ← Quay lại danh sách hỗ trợ                                     │
│                                                                  │
│  Lỗi không nghe được audio bài shadowing                        │
│  [🔴 KHẨN] [Kỹ thuật] [Đang xử lý]                              │
│  Được hỗ trợ bởi: NV Trần C   ·   Gửi lúc 01/06/2026 09:12      │
│  ──────────────────────────────────────────────────────────────  │
│                                                                  │
│  ┌─ Thread ───────────────────────────────────────────────────┐ │
│  │                                       [Bạn]                 │ │
│  │                          ┌─────────────────────────────┐    │ │
│  │                          │ Mình bấm play nhưng không... │    │ │
│  │                          │ 09:12                        │    │ │
│  │                          └─────────────────────────────┘    │ │
│  │  [NV Trần C]                                                │ │
│  │  ┌─────────────────────────────┐                           │ │
│  │  │ Chào bạn, vui lòng thử...    │                           │ │
│  │  │ 10:03                        │                           │ │
│  │  └─────────────────────────────┘                           │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌─ Phản hồi ─────────────────────────────────────────────────┐ │
│  │ [Textarea "Nhập phản hồi của bạn...", 3 dòng]              │ │
│  │ [🔗 Đính kèm URL (tùy chọn)]                               │ │
│  │                          [Đóng ticket]  [Gửi phản hồi →]   │ │
│  └────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

### 2.3 Modal tạo ticket

```
┌──────────────────────────────────────────────┐
│  Tạo yêu cầu hỗ trợ                    [✕]   │
├──────────────────────────────────────────────┤
│  Tiêu đề *                                   │
│  [__________________________________]  0/255 │
│                                              │
│  Danh mục                                    │
│  [Tài khoản ▼]  (Tài khoản/Kỹ thuật/Học tập/ │
│                  Thanh toán/Khác)            │
│                                              │
│  Mức độ ưu tiên                              │
│  ○ Thấp  ● Bình thường  ○ Cao  ○ Khẩn        │
│                                              │
│  Nội dung *                                  │
│  [__________________________________]        │
│  [__________________________________] (5 dòng)│
│                                              │
│              [Hủy]   [Gửi yêu cầu →]         │
└──────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/support/
├── SupportTickets.jsx        ← danh sách + filter + pagination
├── SupportTickets.css
├── SupportTicketDetail.jsx   ← chi tiết + thread + reply form
└── SupportTicketDetail.css
components/support/
├── CreateTicketModal.jsx     ← modal tạo ticket (>60 dòng)
├── CreateTicketModal.css
├── TicketStatusBadge.jsx     ← badge trạng thái (dùng chung 2 trang)
└── PriorityPill.jsx          ← pill mức độ ưu tiên (dùng chung)
```

Bổ sung API vào `api/studentService.js` (mục §5). Route đăng ký trong `App.jsx` dưới `PrivateRoute` STUDENT.

---

## 4. STATE

### 4.1 `SupportTickets.jsx`

```js
const [tickets,      setTickets]  = useState([]);
const [isLoading,    setLoading]  = useState(true);
const [error,        setError]    = useState('');
const [statusFilter, setStatus]   = useState('');     // ''|'open'|'in_progress'|'resolved'|'closed'
const [page,         setPage]     = useState(0);       // 0-based khớp backend
const [totalPages,   setTotal]    = useState(1);
const [showCreate,   setCreate]   = useState(false);
const { toasts, addToast, removeToast } = useToast();
const PAGE_SIZE = 10;
```

> Lưu ý phân trang: backend nhận `page` **0-based** (`@RequestParam(defaultValue = "0")`). Nhãn hiển thị cho người dùng = `page + 1`.

### 4.2 `SupportTicketDetail.jsx`

```js
const { ticketId } = useParams();
const [detail,    setDetail]   = useState(null);   // TicketDetailResponse + replies[]
const [isLoading, setLoading]  = useState(true);
const [error,     setError]    = useState('');      // '' | 'NOT_FOUND' | 'FORBIDDEN' | 'GENERIC'
const [replyText, setReply]    = useState('');
const [attachUrl, setAttach]   = useState('');
const [isSending, setSending]  = useState(false);
const [isClosing, setClosing]  = useState(false);
const threadBottomRef = useRef(null);
const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS — thêm vào `api/studentService.js`

```js
// ─── Hỗ trợ / Ticket — UC-29 (SupportController) ──────────────────────────────

// Danh sách ticket của tôi. status optional. Trả { content, totalElements, totalPages }.
export async function getMyTickets({ status, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  const res = await api.get('/support/tickets', { params });
  return res.data.data;
}

// Chi tiết 1 ticket + replies[]. 403 nếu không phải ticket của mình; 404 nếu không tồn tại.
export async function getTicketDetail(ticketId) {
  const res = await api.get(`/support/tickets/${ticketId}`);
  return res.data.data; // TicketDetailResponse
}

// Tạo ticket mới. priority: 'low'|'normal'|'high'|'urgent' (mặc định normal).
export async function createTicket({ subject, content, category, priority }) {
  const res = await api.post('/support/tickets', { subject, content, category, priority });
  return res.data.data; // TicketResponse (status 201)
}

// Gửi phản hồi vào ticket của mình. attachmentUrl optional (≤500 ký tự).
export async function replyTicket(ticketId, { message, attachmentUrl } = {}) {
  const res = await api.post(`/support/tickets/${ticketId}/reply`, { message, attachmentUrl });
  return res.data.data; // TicketReplyResponse
}

// Học viên tự đóng ticket của mình → status 'closed'.
export async function closeMyTicket(ticketId) {
  const res = await api.post(`/support/tickets/${ticketId}/close`);
  return res.data.data; // TicketResponse
}
```

### 5.1 Hợp đồng dữ liệu (đúng theo DTO backend)

`TicketResponse` (list item):

```jsonc
{
  "ticketId": 45, "studentId": 7,
  "studentName": "Nguyễn Văn A", "studentEmail": "a@mail.com",
  "subject": "Lỗi audio", "content": "…", "category": "Kỹ thuật",
  "priority": "urgent",            // low | normal | high | urgent
  "status": "in_progress",         // open | assigned | in_progress | resolved | closed
  "assignedToStaffId": 3, "assignedToStaffName": "Trần C",
  "replyCount": 3,
  "lastReplyAt": "2026-06-01T10:03:00",
  "createdAt": "2026-06-01T09:12:00",
  "resolvedAt": null
}
```

`TicketDetailResponse` = các field trên (trừ `replyCount`) **+** `replies: TicketReplyResponse[]`:

```jsonc
{ "replyId": 12, "senderName": "Trần C", "senderRole": "STAFF",   // STUDENT | STAFF
  "message": "Chào bạn…", "attachmentUrl": null, "createdAt": "2026-06-01T10:03:00" }
```

> Các field `null` bị backend lược bỏ (`@JsonInclude(NON_NULL)`) → frontend phải dùng optional chaining và fallback, **không** giả định field luôn có mặt.

---

## 6. COMPONENT BREAKDOWN

| Component | Nguồn | Notes |
|:---|:---|:---|
| `TopNav` | `components/layout/TopNav` | `activeTab=""` |
| `EmptyState` | `components/common/EmptyState` | Saku-chan khi danh sách rỗng |
| `SakuChan` | `components/common/SakuChan` | Trạng thái rỗng thread / chưa chọn |
| `UserAvatar` | `components/common/UserAvatar` | Avatar trong bubble thread |
| `TicketStatusBadge` | `components/support/TicketStatusBadge` | Badge trạng thái (bảng §9) |
| `PriorityPill` | `components/support/PriorityPill` | Pill ưu tiên (bảng §9) |
| `CreateTicketModal` | `components/support/CreateTicketModal` | Modal tạo ticket |
| `useToast` + `ToastContainer` | `components/common/Toast` | Feedback |
| `formatRelativeTime` | `utils/date` | "2 giờ trước" |

---

## 7. TICKET DETAIL — THREAD & REPLY

### 7.1 Thread bubble

- Reply có `senderRole === 'STUDENT'` (chính học viên) → bubble **phải**, nền `var(--color-primary-bg)`, viền `var(--color-primary-light)`, tên hiển thị "Bạn".
- Reply có `senderRole === 'STAFF'` → bubble **trái**, nền `var(--color-card)`, viền `var(--color-border)`, kèm `UserAvatar` của nhân viên.
- Nội dung gốc của ticket (`detail.content`) render như **bubble đầu tiên** của học viên, trước mảng `replies`.

Auto-scroll xuống cuối sau khi load và sau khi gửi reply:

```js
useEffect(() => {
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  threadBottomRef.current?.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth' });
}, [detail?.replies?.length]);
```

### 7.2 Reply form — submit flow

1. Validate: `replyText.trim() !== ''`; nếu có `attachUrl` thì độ dài ≤ 500.
2. `setSending(true)` → `replyTicket(ticketId, { message, attachmentUrl })`.
3. Append reply trả về vào `detail.replies` (cập nhật local, không cần refetch toàn bộ).
4. Reset `replyText`, `attachUrl`; scroll to bottom; toast `success` "Đã gửi phản hồi".
5. Lỗi `409 TICKET_CLOSED` → toast `error` với message từ backend + refetch detail để đồng bộ trạng thái.

### 7.3 Khóa khi ticket đã đóng

Backend chặn reply khi `status ∈ {resolved, closed}` (trả `409 TICKET_CLOSED`). Frontend phản chiếu UX:

```jsx
{['resolved', 'closed'].includes(detail.status) && (
  <div className="tks-closed-banner" role="status">
    Ticket này đã được xử lý/đóng. Không thể gửi thêm phản hồi.
  </div>
)}
```

Khi đó: ẩn (hoặc disable) textarea + nút "Gửi phản hồi" và nút "Đóng ticket".

### 7.4 Nút "Đóng ticket"

- Hiện khi `status ∉ {resolved, closed}`.
- Mở confirm dialog (`role="alertdialog"`): "Bạn chắc chắn muốn đóng ticket này? Bạn sẽ không gửi thêm phản hồi được." → `closeMyTicket(ticketId)` → cập nhật `detail.status='closed'` + toast success.

---

## 8. CREATE TICKET MODAL

### 8.1 State (trong modal)

```js
const [form, setForm] = useState({
  subject:  '',
  category: 'Tài khoản',
  priority: 'normal',
  content:  '',
});
const [errors,    setErrors]   = useState({});
const [isSaving,  setSaving]    = useState(false);
```

### 8.2 Validation client-side (khớp Bean Validation backend)

| Field | Ràng buộc | Message |
|:---|:---|:---|
| `subject` | bắt buộc, ≤ 255 | "Tiêu đề không được để trống" / "Tối đa 255 ký tự" |
| `content` | bắt buộc | "Nội dung không được để trống" |
| `category` | ≤ 50 | "Danh mục tối đa 50 ký tự" |
| `priority` | ∈ `low/normal/high/urgent` | (radio, luôn hợp lệ) |

> Danh mục là chuỗi tự do ở backend; frontend gợi ý dropdown cố định (Tài khoản/Kỹ thuật/Học tập/Thanh toán/Khác) để đồng nhất dữ liệu, nhưng vẫn gửi giá trị chuỗi.

### 8.3 Submit flow

1. Validate → nếu có lỗi, set `errors`, dừng.
2. `createTicket(form)` → nhận `TicketResponse` (201).
3. Đóng modal, toast success "Đã gửi yêu cầu hỗ trợ".
4. Điều hướng tới `/support/tickets/{ticketId}` (hoặc refetch danh sách nếu ở trang list).
5. Lỗi validation từ server (`400`) → map message vào toast.

---

## 9. STATUS BADGE & PRIORITY PILL (dùng chung — đồng bộ với staff)

### TicketStatusBadge

| Status | Nhãn | Background | Text |
|:---|:---|:---|:---|
| `open` | Đang mở | `#FFEAEA` | `var(--color-error)` |
| `assigned` | Đã tiếp nhận | `var(--color-primary-bg)` | `var(--color-primary-dark)` |
| `in_progress` | Đang xử lý | `var(--color-accent-bg)` | `var(--color-warning)` |
| `resolved` | Đã giải quyết | `var(--color-secondary-bg)` | `var(--color-secondary)` |
| `closed` | Đã đóng | `#F0EDEB` | `var(--color-text-disabled)` |

### PriorityPill

| Priority | Nhãn | Dot | Style |
|:---|:---|:---|:---|
| `urgent` | KHẨN | `var(--color-error)` 🔴 | nền `#FFEAEA`, chữ `--color-error` |
| `high` | CAO | `var(--color-warning)` 🟠 | nền `--color-accent-bg`, chữ `--color-warning` |
| `normal` | BÌNH THƯỜNG | `var(--color-secondary)` 🟢 | nền `--color-secondary-bg`, chữ `--color-secondary` |
| `low` | THẤP | `var(--color-text-disabled)` ⚫ | nền `var(--color-bg)`, chữ `--color-text-sub` |

Cả hai dùng `border-radius: var(--radius-full)`, `font-size: 11px` (≥12px floor không áp dụng cho badge/chip theo Typography §`label-sm`; dùng 11px chỉ cho pill, **không** cho nội dung đọc).

> Theo DESIGN.md: nhãn badge KHÔNG viết hoa, **trừ** không áp dụng cho CTA. Nhãn priority "KHẨN/CAO…" ở đây giữ chữ thường-có-dấu là chuẩn; ví dụ mockup viết hoa chỉ để dễ đọc — implementation theo bảng (sentence case), không uppercase.

---

## 10. CSS (khung chính — Hanami theme)

```css
/* ===== Support Tickets (SakuJi Hanami Theme) ===== */
.tks-page  { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.tks-body  { flex: 1; max-width: 880px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 16px; box-sizing: border-box; }

.tks-head      { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.tks-title     { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0; }

/* CTA tạo ticket — pill xanh (btn-primary của DESIGN) */
.tks-create-btn {
  display: inline-flex; align-items: center; gap: 6px;
  height: 44px; padding: 0 24px;
  background: var(--color-secondary); color: #fff; border: none;
  border-radius: var(--radius-full);
  font-weight: 700; font-size: 14px; cursor: pointer;
  box-shadow: 0 2px 8px rgba(93,187,105,0.25);
  transition: filter var(--transition), transform var(--transition);
}
.tks-create-btn:hover  { filter: brightness(1.07); }
.tks-create-btn:active { transform: scale(0.97); }

/* Filter tabs */
.tks-filters { display: flex; gap: 8px; flex-wrap: wrap; }
.tks-filter-tab {
  min-height: 36px; padding: 0 16px; border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border); background: var(--color-card);
  color: var(--color-text-sub); font-size: 13px; font-weight: 600; cursor: pointer;
}
.tks-filter-tab--active { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }

/* Ticket card */
.tks-card {
  background: var(--color-card); border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm, 0 2px 8px rgba(0,0,0,0.07));
  padding: 16px 18px; cursor: pointer; text-decoration: none; color: inherit;
  display: flex; flex-direction: column; gap: 6px;
  transition: box-shadow var(--transition), transform var(--transition);
}
.tks-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.10); transform: translateY(-1px); }
.tks-card-top   { display: flex; align-items: center; gap: 8px; }
.tks-card-subject { font-size: 15px; font-weight: 700; color: var(--color-text); }
.tks-card-meta  { font-size: 12px; color: var(--color-text-sub); }

/* Thread */
.tks-thread { display: flex; flex-direction: column; gap: 14px; padding: 16px 0; }
.tks-msg { display: flex; gap: 10px; max-width: 82%; }
.tks-msg--me     { flex-direction: row-reverse; align-self: flex-end; }
.tks-msg-bubble  { background: var(--color-card); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 12px 14px; }
.tks-msg--me .tks-msg-bubble { background: var(--color-primary-bg); border-color: var(--color-primary-light); }
.tks-msg-name { font-size: 12px; font-weight: 700; color: var(--color-text-sub); display: block; margin-bottom: 4px; }
.tks-msg-text { font-size: 14px; color: var(--color-text); margin: 0; white-space: pre-wrap; word-break: break-word; }
.tks-msg-time { font-size: 11px; color: var(--color-text-disabled); display: block; margin-top: 4px; }

/* Reply form */
.tks-reply { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); padding: 16px; display: flex; flex-direction: column; gap: 10px; }
.tks-reply-actions { display: flex; justify-content: flex-end; gap: 10px; }
.tks-send-btn { /* btn-primary pill xanh, giống .tks-create-btn */ }
.tks-close-btn {
  height: 44px; padding: 0 20px; border-radius: var(--radius-full);
  background: transparent; border: 2px solid var(--color-border);
  color: var(--color-text-sub); font-weight: 700; cursor: pointer;
}
.tks-closed-banner {
  background: #F0EDEB; color: var(--color-text-sub);
  border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px;
}

@media (max-width: 767px) {
  .tks-body { padding: 16px 16px 32px; }
  .tks-head { flex-direction: column; align-items: stretch; }
  .tks-create-btn { width: 100%; justify-content: center; }
  .tks-msg { max-width: 92%; }
}
@media (prefers-reduced-motion: reduce) {
  .tks-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

> **Không** hard-code hex ngoài bảng màu DESIGN; các hex như `#FFEAEA`, `#F0EDEB` là tint trạng thái đã được dùng nhất quán trong `SPEC-staff-tickets.md` — giữ đồng bộ. Mọi màu thương hiệu dùng `var(--color-*)`.

---

## 11. 3 TRẠNG THÁI (mỗi component API-backed)

| Trạng thái | List (`SupportTickets`) | Detail (`SupportTicketDetail`) |
|:---|:---|:---|
| **Loading** | Skeleton 4 ticket card | Skeleton header + 3 bubble |
| **Error** | Banner lỗi + nút "Thử lại" | `404` → "Không tìm thấy ticket"; `403` → "Bạn không có quyền xem ticket này"; khác → banner + retry |
| **Empty** | `<EmptyState title="Chưa có yêu cầu hỗ trợ" subtitle="Gặp khó khăn? Hãy tạo yêu cầu, đội ngũ SakuJi luôn sẵn sàng." mascotVariant="idle" mascotSize={140} cta={{ label:'Tạo yêu cầu mới', onClick: openCreate }} />` | Không áp dụng (ticket luôn có nội dung gốc) |

**Không bao giờ render trang trắng** — luôn có skeleton/EmptyState theo DESIGN §Do's.

---

## 12. INTERACTIONS / FLOW

```
/support mount → getMyTickets({status, page}) → render list
Đổi filter tab → setStatus + setPage(0) → refetch
Click card     → navigate(`/support/tickets/${ticketId}`)
[+ Tạo yêu cầu] → mở CreateTicketModal
  submit → createTicket → đóng modal → navigate detail + toast

/support/tickets/:id mount → getTicketDetail(id)
  403 → error='FORBIDDEN' ; 404 → error='NOT_FOUND'
Gửi phản hồi → replyTicket → append reply → scroll bottom
Đóng ticket  → confirm → closeMyTicket → status='closed' + khóa form
```

---

## 13. DOMAIN RULES

- Học viên **chỉ** thao tác trên ticket của chính mình — không có endpoint xem ticket người khác; mọi quyền do backend quyết định (`403`). Frontend không tự lọc bằng `studentId` client-side (DESIGN/CLAUDE anti-pattern "Authorization by UI hide").
- `priority`/`status`/`senderRole` là enum chuỗi **chữ thường** từ backend (`getValue()`); so sánh đúng case (`'urgent'`, không `'URGENT'`).
- Ticket `resolved` (staff đóng) và `closed` (student đóng) đều khóa phản hồi.
- `attachmentUrl` chỉ là URL chuỗi (≤500) — KHÔNG upload file ở luồng này (theo backend hiện tại chỉ nhận URL); nếu cần đính kèm file, dùng module upload riêng rồi dán URL.
- Phân trang `page` 0-based; `totalPages` lấy từ response.

---

## 14. ACCESSIBILITY

- [ ] Mỗi ticket card là link (`<Link>`) có `aria-label` gồm tiêu đề + trạng thái.
- [ ] Filter tabs: `role="tablist"`, mỗi tab `role="tab"` + `aria-selected`.
- [ ] Thread container `aria-live="polite"` để announce reply mới sau khi gửi.
- [ ] Textarea phản hồi: `<label>` liên kết `htmlFor`, `aria-required` khi ticket còn mở.
- [ ] Closed banner: `role="status"`.
- [ ] Confirm đóng ticket: `role="alertdialog"`, `aria-modal="true"`, focus trap, Escape để hủy.
- [ ] Modal tạo ticket: `role="dialog"`, `aria-modal="true"`, Escape đóng, focus trả về nút mở.
- [ ] Auto-scroll dùng `behavior:'auto'` khi `prefers-reduced-motion`.
```
