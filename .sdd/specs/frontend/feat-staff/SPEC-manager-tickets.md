# SPEC — Manager Phân Công Ticket (Triage & Assign)
>
> **Feature ID:** `feat-staff` | **Page:** `ManagerTickets`
> **Route:** `/manager/tickets`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-27
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-support` — UC-29 · `StaffSupportController` (`/api/staff/tickets`)
> **Liên quan:** `SPEC-staff-tickets.md` (staff xử lý ticket được giao), `SPEC-support-tickets.md` (student)

---

## 0. VAI TRÒ & RANH GIỚI LUỒNG

```
Student tạo (OPEN)
   └─▶ Manager DUYỆT + PHÂN CÔNG  ← MÀN NÀY
          status OPEN → ASSIGNED, gán assignedTo = staffX
              └─▶ Staff được giao XỬ LÝ (reply → IN_PROGRESS → close → RESOLVED)
```

Màn này là **trung tâm điều phối của Staff Manager**: xem toàn bộ ticket, lọc nhanh ticket **chưa giao** (`open`), chọn nhân viên để giao, và (tùy chọn) tự phản hồi/đóng. Manager có **toàn quyền** trên mọi ticket (backend không chặn owner), khác với staff thường chỉ thao tác ticket được giao.

> **Phân quyền:** Manager và Staff đều mang `ROLE_STAFF` trong JWT. Quyền "phân công" được backend kiểm tra ở service (`staffRole == STAFF_MANAGER` hoặc admin) → nếu staff thường gọi assign sẽ nhận `403`. Frontend chỉ render màn này dưới `<ManagerRoute>` (đã check `user.staffRole === 'staff_manager'`), KHÔNG tự suy quyền khác.

---

## 1. TỔNG QUAN TRANG

Layout **master-detail** giống màn staff nhưng trọng tâm là **triage**:

- Cột trái: danh sách ticket + tab lọc, **mặc định tab "Chưa giao" (`open`)**.
- Cột phải: chi tiết ticket + panel **Phân công** nổi bật trên đầu + thread + (tùy chọn) reply form.

**Prefix CSS:** `mtk-`
**activeTab:** `'manager-tickets'`
**Guard:** `<ManagerRoute>`
**TopNav:** `ManagerTopNav` (cần thêm 1 tab — xem §13)
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────────────┐
│  ManagerTopNav  activeTab="manager-tickets"  [badge MANAGER]         │
├──────────────────────────────────────────────────────────────────────┤
│  <main className="mtk-body">                                         │
│  ┌─ Hero/Stats ─────────────────────────────────────────────────┐    │
│  │ Chưa giao: 5   ·   Đang xử lý: 12   ·   Đã giải quyết: 30    │    │
│  └──────────────────────────────────────────────────────────────┘    │
│  ┌────────────────────┐  ┌──────────────────────────────────────┐   │
│  │ TICKET QUEUE (360)  │  │ TICKET DETAIL (flex:1)               │   │
│  │ [Search]            │  │ ┌─ PHÂN CÔNG ──────────────────────┐ │   │
│  │ [Chưa giao] [Đang   │  │ │ Giao cho: [Chọn nhân viên ▼]     │ │   │
│  │  xử lý][Tất cả]     │  │ │           [Phân công →]          │ │   │
│  │                     │  │ │ Hiện giao cho: NV Trần C (nếu có)│ │   │
│  │ ┌────────────────┐  │  │ └──────────────────────────────────┘ │   │
│  │ │🔴#45 Lỗi audio │  │  │ [Subject] [Priority][Status]         │   │
│  │ │Nguyễn A ·CHƯA  │  │  │ Học viên: Nguyễn A · N3 · 01/06     │   │
│  │ │GIAO            │  │  │ ┌─ Thread (chat) ──────────────────┐ │   │
│  │ └────────────────┘  │  │ │ [student] … [staff] …            │ │   │
│  │ ...                 │  │ └──────────────────────────────────┘ │   │
│  │                     │  │ [Reply form (tùy chọn)] [Đóng]       │   │
│  └────────────────────┘  └──────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/manager/ManagerTickets.jsx
pages/manager/ManagerTickets.css
components/manager/AssignPanel.jsx        ← dropdown chọn staff + nút phân công (>60 dòng)
components/manager/AssignPanel.css
// Dùng lại từ feat-support:
components/support/TicketStatusBadge.jsx
components/support/PriorityPill.jsx
// Dùng lại thread/reply pattern của staff (SPEC-staff-tickets §7)
```

---

## 4. STATE

```js
const [tickets,      setTickets]  = useState([]);
const [selectedId,   setSelected] = useState(null);
const [detail,       setDetail]   = useState(null);   // TicketDetailResponse + replies[]
const [staffList,    setStaff]    = useState([]);      // assignee picker
const [isLoadingList,setLoadList] = useState(true);
const [isLoadingDtl, setLoadDtl]  = useState(false);
const [error,        setError]    = useState('');
const [statusFilter, setStatus]   = useState('open');  // mặc định ticket chưa giao
const [search,       setSearch]   = useState('');
const [debounced,    setDebounced]= useState('');
const [page,         setPage]     = useState(0);        // 0-based
const [totalPages,   setTotal]    = useState(1);
const [assignTo,     setAssignTo] = useState('');       // staffId đang chọn
const [isAssigning,  setAssigning]= useState(false);
const [replyText,    setReply]    = useState('');
const [isSending,    setSending]  = useState(false);
const timerRef = useRef(null);
const PAGE_SIZE = 20;
```

---

## 5. API — `managerService.js` (mới) hoặc bổ sung `staffService.js`

> Endpoint dùng chung `/api/staff/tickets` (manager mang `ROLE_STAFF`). Đặt trong `managerService.js` cho rõ ngữ cảnh, hoặc tái dùng các hàm trong `staffService.js` (xem `SPEC-staff-tickets.md §5`). KHÔNG trùng lặp logic.

```js
// Danh sách ticket (lọc status/category/priority/q). Trả { content, totalElements, totalPages }.
export async function getTickets({ status, category, priority, q, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  if (category) params.category = category;
  if (priority) params.priority = priority;
  if (q) params.q = q;
  const res = await api.get('/staff/tickets', { params });
  return res.data.data;
}

// Chi tiết ticket + replies[].
export async function getTicketDetail(ticketId) {
  const res = await api.get(`/staff/tickets/${ticketId}`);
  return res.data.data; // TicketDetailResponse
}

// PHÂN CÔNG ticket cho 1 staff. OPEN → ASSIGNED. 403 nếu không phải manager/admin.
export async function assignTicket(ticketId, assignToStaffId) {
  const res = await api.post(`/staff/tickets/${ticketId}/assign`, { assignToStaffId });
  return res.data.data; // TicketResponse
}

// (tùy chọn) Manager tự phản hồi / đóng — dùng chung staffService.
export async function replyTicket(ticketId, message, attachmentUrl) {
  const res = await api.post(`/staff/tickets/${ticketId}/reply`, { message, attachmentUrl });
  return res.data.data;
}
export async function closeTicket(ticketId) {
  const res = await api.post(`/staff/tickets/${ticketId}/close`);
  return res.data.data;
}

// ⚠️ PHỤ THUỘC BACKEND (CHƯA CÓ) — danh sách staff để chọn người giao.
export async function getAssignableStaff() {
  const res = await api.get('/staff/members'); // role=staff, manager/admin only
  return res.data.data; // [{ staffId, fullName, email, assignedOpenCount? }]
}
```

### 5.1 ⚠️ DEPENDENCY BACKEND — endpoint thiếu

Assignee picker **cần** một endpoint trả danh sách nhân viên hỗ trợ. **Hiện chưa tồn tại** (chỉ có `GET /api/admin/users` cho Admin). Cần bổ sung:

```
GET /api/staff/members            @PreAuthorize("hasRole('STAFF')") + service check STAFF_MANAGER
  → 200 { data: [ { staffId, fullName, email, staffRole, assignedOpenCount } ] }
```

Cho tới khi có: tạm fallback dùng `GET /api/admin/users/{type}` nếu manager có quyền, hoặc disable nút "Phân công" + tooltip "Đang chờ API danh sách nhân viên". **Không hard-code danh sách staff ở frontend.**

### 5.2 Hợp đồng dữ liệu

`TicketResponse` / `TicketDetailResponse`: xem `SPEC-support-tickets.md §5.1`. Field quan trọng cho màn này:
`assignedToStaffId`, `assignedToStaffName`, `status` (`open`→chưa giao), `priority`, `replyCount`, `lastReplyAt`.

---

## 6. TICKET QUEUE (cột trái)

### Tab lọc — mặc định "Chưa giao"

```
[Chưa giao (open)] [Đã giao (assigned)] [Đang xử lý (in_progress)] [Tất cả]
```

Mỗi tab có badge đếm. Card hiển thị nhãn **"CHƯA GIAO"** (nền `#FFEAEA`, chữ `--color-error`) khi `status === 'open'` và `assignedToStaffId == null`; nếu đã giao thì hiện "Giao: {assignedToStaffName}".

Priority dot màu: `urgent`→error, `high`→warning, `normal`→secondary, `low`→text-disabled (đồng bộ `PriorityPill`).

Card active: nền `var(--color-primary-bg)`, border-left `3px solid var(--color-primary)`.

Search debounce 400ms → set `debounced` → refetch với `q`.

---

## 7. ASSIGN PANEL (`AssignPanel` — cột phải, trên cùng)

```
┌─ Phân công ──────────────────────────────────────────────┐
│ Giao cho nhân viên                                       │
│ [▼ Chọn nhân viên hỗ trợ]   [Phân công →]                │
│   Trần C — đang giữ 4 ticket                             │
│   Lê D   — đang giữ 1 ticket                             │
│ ───────────────────────────────────────────────────────  │
│ Hiện đang giao cho: Trần C  ·  Đổi người giao ↺          │
└──────────────────────────────────────────────────────────┘
```

- Dropdown nguồn từ `getAssignableStaff()`; mỗi option hiển thị tên + `assignedOpenCount` (gợi ý cân tải).
- Nút **Phân công** disabled khi `!assignTo || isAssigning`.
- Submit: `assignTicket(selectedId, assignTo)` → cập nhật `detail.assignedToStaffId/Name`, `detail.status` (`open`→`assigned`), cập nhật card trong list, toast success "Đã giao ticket cho {tên}".
- Nếu ticket đã `resolved`/`closed`: backend trả `409` → ẩn panel hoặc disable + banner "Ticket đã đóng, không thể phân công."
- Đổi người giao: cho chọn lại + assign lần nữa (backend cho phép gán lại khi chưa đóng).

```css
.mtk-assign-panel {
  background: var(--color-primary-bg);
  border: 1.5px solid var(--color-primary-light);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 16px;
}
.mtk-assign-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.mtk-assign-select {
  flex: 1; min-width: 220px; height: 44px; padding: 0 14px;
  border: 1.5px solid var(--color-border); border-radius: var(--radius-md);
  background: var(--color-card); font-size: 14px; color: var(--color-text);
}
.mtk-assign-btn {
  height: 44px; padding: 0 22px; border-radius: var(--radius-full);
  background: var(--color-secondary); color: #fff; border: none; font-weight: 700; cursor: pointer;
  box-shadow: 0 2px 8px rgba(93,187,105,0.25);
}
.mtk-assign-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.mtk-assign-current { margin-top: 10px; font-size: 13px; color: var(--color-text-sub); }
```

---

## 8. TICKET DETAIL — header + thread + reply

- Header: subject, `PriorityPill`, `TicketStatusBadge`, "Học viên: {name} · {level} · {createdAt}".
- Thread: bubble chat — `senderRole === 'STAFF'` bên phải (nền `--color-primary-bg`), `'STUDENT'` bên trái. Nội dung gốc `detail.content` là bubble student đầu tiên. (Tái dùng pattern `SPEC-staff-tickets.md §7`.)
- Reply form (tùy chọn cho manager): `replyTicket` → backend tự chuyển `OPEN/ASSIGNED → IN_PROGRESS` + notify student. Disabled khi ticket `resolved/closed`.
- Nút **Đóng ticket**: `closeTicket` → `RESOLVED`.

> Auto-scroll thread xuống cuối; tôn trọng `prefers-reduced-motion`.

---

## 9. STATUS BADGE & PRIORITY PILL

Dùng chung `components/support/TicketStatusBadge` và `PriorityPill` (bảng màu xem `SPEC-support-tickets.md §9`). KHÔNG định nghĩa lại bảng màu để giữ đồng bộ toàn hệ thống.

---

## 10. LOADING / ERROR / EMPTY

- **Loading list:** skeleton 5 card. **Loading detail:** skeleton header + 3 bubble.
- **Error list/detail:** banner + nút "Thử lại".
- **Empty list (tab Chưa giao):** `<EmptyState title="Không có ticket chờ phân công" subtitle="Mọi yêu cầu đã được giao cho nhân viên 🌸" mascotVariant="celebrate" mascotSize={140} />`
- **Chưa chọn ticket (cột phải):** Saku-chan `idle` + "Chọn một ticket để phân công hoặc xử lý".
- **Lỗi load staffList:** disable AssignPanel + thông báo (xem §5.1).

---

## 11. INTERACTIONS / FLOW

```
mount → getTickets({status:'open'}) + getAssignableStaff() song song
select card → getTicketDetail(id) → reset assignTo
phân công → assignTicket → cập nhật detail+card (open→assigned) → toast
  (option) reply → replyTicket → append thread, status→in_progress
  (option) đóng → closeTicket → status→resolved + khóa reply/assign
đổi tab/search → refetch list (page=0)
```

---

## 12. RESPONSIVE

```css
@media (max-width: 1199px) { .mtk-body { padding: 24px 20px 40px; } }
@media (max-width: 767px) {
  .mtk-body { flex-direction: column; padding: 0; }
  .mtk-queue-col { width: 100%; max-height: 38vh; overflow-y: auto; border-bottom: 1px solid var(--color-border); }
  .mtk-assign-row { flex-direction: column; align-items: stretch; }
  .mtk-assign-btn { width: 100%; }
}
@media (prefers-reduced-motion: reduce) { .mtk-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 13. THAY ĐỔI NGOÀI MÀN (cần làm kèm)

1. **`ManagerTopNav.jsx`** — thêm 1 tab:
   ```js
   { id: 'manager-tickets', label: 'Hỗ trợ', route: '/manager/tickets', icon: <…/> }
   ```
2. **`App.jsx`** — thêm route:
   ```jsx
   <Route path="/manager/tickets" element={<ManagerRoute><ManagerTickets /></ManagerRoute>} />
   ```
3. **Backend** — tạo `GET /api/staff/members` (xem §5.1). **Blocker** của assignee picker.

---

## 14. ACCESSIBILITY

- [ ] Queue: `role="list"`, card `role="listitem"` + `aria-selected`; tab lọc `role="tablist"`.
- [ ] AssignPanel: `<label htmlFor>` cho select; nút "Phân công" có `aria-busy` khi đang gọi.
- [ ] Thread container `aria-live="polite"`.
- [ ] Reply textarea `aria-required` khi ticket mở; banner đóng `role="status"`.
- [ ] Confirm khi đổi người giao (nếu đã có người): `role="alertdialog"`.
- [ ] Toast lỗi `403`/`409` hiển thị message từ backend, không nuốt lỗi.
```
