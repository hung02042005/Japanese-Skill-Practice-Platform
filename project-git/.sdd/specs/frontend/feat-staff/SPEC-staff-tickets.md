# SPEC — Staff Hỗ Trợ Học Viên (Support Tickets)
> **Feature ID:** `feat-staff` | **Page:** `StaffTickets`
> **Route:** `/staff/tickets`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-03
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Backend ref:** `feat-support` — UC-29 (Respond to Student Support)

---

## 1. TỔNG QUAN TRANG

Trang quản lý tickets hỗ trợ từ học viên. Layout dạng **master-detail**: cột trái liệt kê tickets, click chọn ticket → cột phải hiển thị lịch sử hội thoại + form trả lời. Staff có thể trả lời, đổi trạng thái, và phân công.

**Prefix CSS:** `tkt-`
**activeTab:** `'staff-tickets'`
**Guard:** `<StaffRoute>`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-tickets"                          │
├──────────────────────────────────────────────────────────────────┤
│  <main className="tkt-body">                                     │
│  ┌────────────────────┐  ┌──────────────────────────────────┐   │
│  │  TICKET LIST       │  │  TICKET DETAIL                   │   │
│  │  (360px, sticky)   │  │  (flex: 1)                       │   │
│  │                    │  │                                  │   │
│  │ [Search tickets]   │  │ [Detail Header]                  │   │
│  │ [Status tabs]      │  │  Subject | Priority | Status     │   │
│  │  Open | Progress   │  │  Học viên: Nguyễn Văn A          │   │
│  │  Resolved | All    │  │  Level: N4 | Gửi: 01/06/2026    │   │
│  │                    │  │                                  │   │
│  │ ┌────────────────┐ │  │ [Thread — tin nhắn dạng chat]   │   │
│  │ │ 🔴 Ticket #45  │ │  │                                  │   │
│  │ │ Lỗi âm thanh..│ │  │  [Student bubble]                │   │
│  │ │ Nguyễn A •2h  │ │  │  "Xin chào, tôi gặp lỗi..."     │   │
│  │ └────────────────┘ │  │                                  │   │
│  │ ┌────────────────┐ │  │  [Staff bubble]                  │   │
│  │ │ 🟡 Ticket #42  │ │  │  "Chào bạn, chúng tôi đã..."   │   │
│  │ │ Hỏi về Level..│ │  │                                  │   │
│  │ └────────────────┘ │  │ [Reply Form]                     │   │
│  │ ...                │  │  [Textarea "Nhập phản hồi..."]   │   │
│  │                    │  │  [Status▼] [Gửi phản hồi →]     │   │
│  └────────────────────┘  └──────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffTickets.jsx
pages/staff/StaffTickets.css
components/staff/TicketList.jsx       ← danh sách ticket trái (>60 dòng)
components/staff/TicketDetail.jsx     ← chi tiết + thread + reply form (>60 dòng)
```

---

## 4. STATE

```js
const [tickets,       setTickets]  = useState([]);
const [selectedId,    setSelected] = useState(null);       // ticket đang xem
const [detail,        setDetail]   = useState(null);       // ticket detail + replies[]
const [isLoadingList, setLoadList] = useState(true);
const [isLoadingDtl,  setLoadDtl]  = useState(false);
const [error,         setError]    = useState('');
const [statusFilter,  setStatus]   = useState('open');     // 'open'|'in_progress'|'resolved'|'closed'|''
const [search,        setSearch]   = useState('');
const [debounced,     setDebounced]= useState('');
const [currentPage,   setPage]     = useState(1);
const [totalPages,    setTotal]    = useState(1);
const [replyText,     setReply]    = useState('');
const [newStatus,     setNewStatus]= useState('');         // status muốn đổi khi reply
const [isSending,     setSending]  = useState(false);
const timerRef = useRef(null);
const threadBottomRef = useRef(null);   // auto-scroll to bottom
const PAGE_SIZE = 20;
```

---

## 5. API — `staffService.js`

```js
// Lấy danh sách tickets (với filter)
export async function getTickets({ status, search, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  if (search) params.search = search;
  const res = await api.get('/staff/tickets', { params });
  return res.data.data; // { content: [ticket], totalPages }
}

// Lấy chi tiết ticket + replies
export async function getTicketDetail(ticketId) {
  const res = await api.get(`/staff/tickets/${ticketId}`);
  return res.data.data;
  // { ticket, replies: [{ replyId, senderType, senderName, message, createdAt }] }
}

// Trả lời ticket (tự động đổi status → 'in_progress')
export async function replyTicket(ticketId, message) {
  const res = await api.post(`/staff/tickets/${ticketId}/reply`, { message });
  return res.data.data; // { replyId, createdAt }
}

// Đổi trạng thái ticket
export async function updateTicketStatus(ticketId, status) {
  const res = await api.put(`/staff/tickets/${ticketId}/status`, { status });
  return res.data.data;
}

// Phân công ticket cho Staff khác
export async function assignTicket(ticketId, staffId) {
  const res = await api.put(`/staff/tickets/${ticketId}/assign`, { staffId });
  return res.data.data;
}
```

---

## 6. TICKET LIST (cột trái)

### Status filter tabs

```
[Mở (open)] [Đang xử lý] [Đã giải quyết] [Tất cả]
```

Mỗi tab có badge số (lấy từ response count).

### Ticket Card

```
┌────────────────────────────────────┐
│ 🔴 [Priority pill]  #45            │
│                                    │
│ Lỗi âm thanh bài luyện shadowing  │
│ (truncate 60 ký tự)                │
│                                    │
│ Nguyễn Văn A  ·  2 giờ trước       │
│ [Chưa đọc badge nếu chưa reply]   │
└────────────────────────────────────┘
```

Priority dot colors:
- `urgent` → `var(--color-error)` 🔴
- `high` → `var(--color-warning)` 🟠
- `normal` → `var(--color-secondary)` 🟢
- `low` → `var(--color-text-disabled)` ⚫

Active ticket (đang xem): background `var(--color-primary-bg)`, border-left `3px solid var(--color-primary)`.

```css
.tkt-ticket-card {
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background var(--transition);
}
.tkt-ticket-card:hover { background: var(--color-primary-bg); }
.tkt-ticket-card--active {
  background: var(--color-primary-bg);
  border-left: 3px solid var(--color-primary);
}
.tkt-ticket-subject { font-size: 14px; font-weight: 600; color: var(--color-text); margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.tkt-ticket-meta    { font-size: 12px; color: var(--color-text-sub); display: flex; gap: 8px; align-items: center; }
```

---

## 7. TICKET DETAIL (cột phải)

### Detail Header

```
┌──────────────────────────────────────────────────────────┐
│ Tiêu đề ticket (font-size: 18px, font-weight: 700)       │
│                                                          │
│ [Priority pill] [Status badge]                           │
│                                                          │
│ Học viên: Nguyễn Văn A | Level: N4 | Gửi: 01/06/2026   │
│ Được phân công: Staff Trần C (nếu có)                   │
│                                                          │
│ [Hành động phải]:  [Phân công ▼] [Đổi trạng thái ▼]    │
└──────────────────────────────────────────────────────────┘
```

### Thread (chat messages)

```jsx
<div className="tkt-thread" ref={threadContainerRef}>
  {detail.replies.map((reply) => (
    <div
      key={reply.replyId}
      className={`tkt-msg ${reply.senderType === 'staff' ? 'tkt-msg--staff' : 'tkt-msg--student'}`}
    >
      <div className="tkt-msg-avatar">
        <UserAvatar name={reply.senderName} size={32} />
      </div>
      <div className="tkt-msg-bubble">
        <span className="tkt-msg-name">{reply.senderName}</span>
        <p className="tkt-msg-text">{reply.message}</p>
        <span className="tkt-msg-time">{formatRelativeTime(reply.createdAt)}</span>
      </div>
    </div>
  ))}
  <div ref={threadBottomRef} />
</div>
```

**Layout bubbles:**
- Student: avatar trái + bubble trái, background `var(--color-bg)`, border `var(--color-border)`
- Staff: bubble phải, background `var(--color-primary-bg)`, border `var(--color-primary-light)`

Auto-scroll to bottom sau mỗi lần load detail và sau khi gửi reply:
```js
useEffect(() => {
  threadBottomRef.current?.scrollIntoView({ behavior: 'smooth' });
}, [detail?.replies]);
```

```css
.tkt-thread { flex: 1; overflow-y: auto; padding: 16px 24px; display: flex; flex-direction: column; gap: 16px; }
.tkt-msg { display: flex; gap: 10px; max-width: 80%; }
.tkt-msg--staff { flex-direction: row-reverse; align-self: flex-end; }
.tkt-msg-bubble { background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 12px 14px; }
.tkt-msg--staff .tkt-msg-bubble { background: var(--color-primary-bg); border-color: var(--color-primary-light); }
.tkt-msg-name { font-size: 12px; font-weight: 700; color: var(--color-text-sub); display: block; margin-bottom: 4px; }
.tkt-msg-text { font-size: 14px; color: var(--color-text); margin: 0; }
.tkt-msg-time { font-size: 11px; color: var(--color-text-disabled); display: block; margin-top: 4px; }
```

### Reply Form

```
┌──────────────────────────────────────────────────────┐
│  [Textarea "Nhập phản hồi cho học viên...", 3 rows]  │
│                                                      │
│  [Đổi trạng thái ▼]   [Gửi phản hồi →]              │
│   open/in_progress/resolved/closed                   │
└──────────────────────────────────────────────────────┘
```

"Đổi trạng thái" dropdown: cho phép chọn trạng thái mới khi gửi reply. Nếu không chọn → giữ nguyên (backend tự đổi sang `in_progress`).

**Submit flow:**
1. `replyTicket(selectedId, replyText)` → thêm reply vào thread local
2. Nếu `newStatus` khác rỗng → `updateTicketStatus(selectedId, newStatus)` → cập nhật header
3. Reset `replyText`, `newStatus`
4. Scroll to bottom
5. Cập nhật ticket card trong list (last_reply_at, status)

**Disabled khi:**
- `replyText.trim() === ''`
- `isSending === true`
- Ticket status = `closed`

**Closed ticket banner:**
```jsx
{detail.ticket.status === 'closed' && (
  <div className="tkt-closed-banner" role="alert">
    Ticket này đã đóng. Không thể gửi thêm phản hồi.
  </div>
)}
```

---

## 8. TRẠNG THÁI KHÔNG CHỌN TICKET (cột phải rỗng)

Khi chưa chọn ticket nào:

```jsx
<div className="tkt-empty-detail">
  <SakuChan variant="idle" size={80} aria-hidden="true" />
  <p>Chọn một ticket để bắt đầu hỗ trợ</p>
</div>
```

---

## 9. STATUS BADGE

| Status | Background | Text | Label |
|:---|:---|:---|:---|
| `open` | `#FFEAEA` | `var(--color-error)` | Mở |
| `in_progress` | `--color-accent-bg` | `--color-warning` | Đang xử lý |
| `resolved` | `--color-secondary-bg` | `--color-secondary` | Đã giải quyết |
| `closed` | `#F0EDEB` | `--color-text-disabled` | Đã đóng |

---

## 10. LOADING / ERROR / EMPTY

- **Loading list:** skeleton 5 ticket card
- **Loading detail:** skeleton thread (3 bubbles)
- **Error list:** error banner + retry
- **Error detail:** "Không thể tải ticket. Vui lòng thử lại." + retry button
- **Empty list:** `<EmptyState title="Không có ticket nào" subtitle="Tất cả yêu cầu hỗ trợ đã được xử lý!" mascotVariant="celebrate" mascotSize={120}>`

---

## 11. RESPONSIVE

```css
@media (max-width: 1199px) {
  .tkt-body { padding: 24px 20px 40px; }
}
@media (max-width: 767px)  {
  .tkt-body     { padding: 0; flex-direction: column; }
  .tkt-list-col { width: 100%; border-right: none; border-bottom: 1px solid var(--color-border); max-height: 40vh; overflow-y: auto; }
  .tkt-detail-col { flex: 1; }
}
```

---

## 12. ACCESSIBILITY

- [ ] Ticket list: `role="list"`, mỗi card là `role="listitem"` + `aria-selected`
- [ ] Thread messages: `aria-live="polite"` trên container để screen reader thông báo khi có reply mới
- [ ] Textarea reply: `aria-label="Nhập phản hồi"` + `aria-required="true"` khi ticket còn mở
- [ ] Closed banner: `role="alert"`
- [ ] Status dropdown: `<label>` ẩn liên kết bằng `htmlFor`
- [ ] Auto-scroll không gây vấn đề với `prefers-reduced-motion`: dùng `behavior: 'auto'` thay `'smooth'` khi motion reduced
