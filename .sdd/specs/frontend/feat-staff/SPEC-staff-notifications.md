# SPEC — Gửi Thông Báo Học Viên (Broadcast Notifications)
>
> **Feature ID:** `feat-staff` | **Page:** `ManagerNotifications` (dùng chung cho Staff/Manager)
> **Route:** `/manager/notifications` (Staff có thể tái dùng tại `/staff/notifications` nếu mở route)
> **Version:** 2.0 | **Status:** Draft (thay thế v1.0 — căn lại đúng backend đã port)
> **Author:** Team | **Last Updated:** 2026-06-27
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-support` — UC-30 · `StaffNotificationController` (`POST /api/staff/notifications`)
> **Liên quan:** `SPEC-notifications.md` (student NHẬN thông báo), `AdminNotificationRuleController` (rule tự động — màn riêng)

---

## 0. KHÁC BIỆT SO VỚI v1.0 (⚠️ ĐỌC TRƯỚC)

Trang hiện tại [ManagerNotifications.jsx](../../../apps/frontend/src/pages/manager/ManagerNotifications.jsx) **đang mock 100%** (`MOCK_SENT`, `setTimeout` giả lập gửi). Backend thật **chỉ có 1 endpoint**: `POST /api/staff/notifications` (broadcast bất đồng bộ, trả `jobId`). Không có history/stats/estimate.

| v1.0 / mock hiện tại (SAI) | v2.0 (ĐÚNG backend) |
|:---|:---|
| `GET /staff/notifications` (history table) | ❌ **Không tồn tại** — bỏ bảng lịch sử (hoặc đánh dấu "sắp có") |
| `GET /staff/notifications/estimate` | ❌ **Không tồn tại** — bỏ "ước tính người nhận" |
| stats `todayCount/weekCount` | ❌ **Không tồn tại** — bỏ stats row |
| channel `'in-app'` / `'both'` | ✅ `in_app` \| `email` \| `both` (underscore!) |
| thiếu `notificationType` | ✅ `news\|warning\|promotion\|system\|achievement\|reminder` (mặc định `system`) |
| `setTimeout` giả lập | ✅ `POST` thật, nhận `{ jobId }` (202), fire-and-forget |

Kết quả: màn này thu gọn về **một form soạn & gửi** (compose-only).

---

## 1. TỔNG QUAN TRANG

Màn soạn và **broadcast** thông báo hệ thống đến học viên. Người gửi (Staff Manager) nhập tiêu đề + nội dung, chọn **loại**, **kênh** (in-app / email / cả hai), **đối tượng** theo JLPT level (hoặc tất cả), và **lịch gửi** (ngay / hẹn giờ). Backend xử lý **bất đồng bộ**: trả `jobId` ngay, không block UI, **không poll** trạng thái job ở frontend.

**Prefix CSS:** `nfs-`
**activeTab:** `'manager-notifications'`
**Guard:** `<ManagerRoute>` (hiện tại). Nếu mở cho Staff thường: `<StaffRoute>` + route `/staff/notifications`.
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────┐
│  ManagerTopNav  activeTab="manager-notifications"            │
├──────────────────────────────────────────────────────────────┤
│  <main className="nfs-body">                                 │
│  [StaffPageHero accent="pink" "Gửi Thông Báo"]              │
│                                                              │
│  ┌─ Compose Card ─────────────────────────────────────────┐  │
│  │ Tiêu đề *            [____________________]  0/255      │  │
│  │ Nội dung *           [____________________] (5 dòng)    │  │
│  │                                                        │  │
│  │ Loại thông báo *                                       │  │
│  │  ○Tin tức ○Cảnh báo ○Khuyến mãi ●Hệ thống ○Thành tích  │  │
│  │  ○Nhắc nhở                                              │  │
│  │ Kênh gửi *           ●In-app ○Email ○Cả hai            │  │
│  │ Đối tượng            ●Tất cả  ○Theo Level [N5▼]        │  │
│  │ Thời gian gửi        ●Gửi ngay ○Hẹn giờ [date][time]   │  │
│  │ ─────────────────────────────────────────────────────  │  │
│  │                              [Gửi thông báo →]         │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  [Bảng "Lịch sử gửi" — CHỜ BACKEND, ẩn ở v2.0]              │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/manager/ManagerNotifications.jsx   ← thay máu từ mock → API thật
pages/manager/ManagerNotifications.css
// Form đủ nhỏ → giữ inline trong page; không cần modal (không có list phía sau)
```

---

## 4. STATE

```js
const [form, setForm] = useState({
  title:            '',
  content:          '',
  notificationType: 'system',     // news|warning|promotion|system|achievement|reminder
  channel:          'in_app',     // in_app|email|both
  targetAll:        true,
  targetLevel:      'N5',         // chỉ dùng khi !targetAll
  scheduleNow:      true,
  scheduledDate:    '',
  scheduledTime:    '',
});
const [errors,    setErrors]   = useState({});
const [isSending, setSending]  = useState(false);
const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API — `staffService.js` (hoặc `managerService.js`)

```js
import api from './authService';

// Broadcast async — trả { jobId }. Status 202.
// payload: { title, content, notificationType, channel, targetJlptLevel, scheduledAt? }
export async function sendBroadcast(payload) {
  const res = await api.post('/staff/notifications', payload);
  return res.data.data; // { jobId }
}
```

### 5.1 Hợp đồng request (đúng `SendNotificationRequest`)

```jsonc
{
  "title": "Bảo trì hệ thống",            // bắt buộc, ≤255
  "content": "Hệ thống bảo trì 23:00…",   // bắt buộc
  "notificationType": "system",           // mặc định "system"
  "channel": "in_app",                    // mặc định "in_app"
  "targetJlptLevel": "ALL",               // N1|N2|N3|N4|N5|ALL — mặc định "ALL"
  "scheduledAt": "2026-06-28T23:00:00"    // optional (LocalDateTime ISO, bỏ nếu gửi ngay)
}
```

> `targetJlptLevel`: gửi `"ALL"` khi `targetAll`; ngược lại gửi `form.targetLevel`. `scheduledAt`: chỉ thêm khi `!scheduleNow`, ghép `scheduledDate + 'T' + scheduledTime + ':00'`.

---

## 6. COMPOSE FORM

### Validation client-side (khớp Bean Validation backend)

| Field | Ràng buộc | Message |
|:---|:---|:---|
| `title` | bắt buộc, ≤255 | "Tiêu đề không được để trống" / "Tối đa 255 ký tự" |
| `content` | bắt buộc | "Nội dung không được để trống" |
| `notificationType` | ∈ 6 loại (radio) | — |
| `channel` | ∈ `in_app/email/both` (radio) | — |
| `scheduledAt` (khi hẹn giờ) | phải là tương lai | "Thời gian hẹn phải ở tương lai" |

### NotificationType radio (label VI)

| value | label |
|:---|:---|
| `news` | Tin tức |
| `warning` | Cảnh báo |
| `promotion` | Khuyến mãi |
| `system` | Hệ thống |
| `achievement` | Thành tích |
| `reminder` | Nhắc nhở |

### Submit flow

1. `validate()` → có lỗi: set `errors`, dừng.
2. Build payload (§5.1).
3. **Confirm dialog** nếu `targetAll === true` (gửi toàn bộ học viên):
   `"Bạn sắp broadcast đến TẤT CẢ học viên. Hành động không thể hoàn tác. [Hủy] [Xác nhận gửi]"` (`role="alertdialog"`).
4. `sendBroadcast(payload)` → nhận `{ jobId }`.
5. Toast success: `"Đã gửi yêu cầu broadcast (Job ${jobId}). Hệ thống đang xử lý trong nền."`
6. Reset form về mặc định.

**Không poll job** — fire-and-forget (backend `@Async`).

---

## 7. CHANNEL PILL (hiển thị lựa chọn / nhãn)

| Channel | Label | Style |
|:---|:---|:---|
| `in_app` | In-app | outline gray (`--color-border`) |
| `email` | Email | outline blue (`#1565C0`) |
| `both` | In-app + Email | nền `--color-primary-bg`, viền `--color-primary-light`, chữ `--color-primary-dark` |

```css
.nfs-channel-pill { display: inline-flex; align-items: center; gap: 4px; padding: 2px 10px; border-radius: var(--radius-full); font-size: 11px; font-weight: 700; }
.nfs-channel--in_app { border: 1.5px solid var(--color-border); color: var(--color-text-sub); }
.nfs-channel--email  { border: 1.5px solid #1565C0; color: #1565C0; }
.nfs-channel--both   { background: var(--color-primary-bg); border: 1.5px solid var(--color-primary-light); color: var(--color-primary-dark); }
```

---

## 8. LỊCH SỬ GỬI — ⚠️ CHỜ BACKEND

Backend **chưa có** endpoint trả lịch sử thông báo đã gửi. Ở v2.0:

- **Bỏ** bảng lịch sử mock + stats row.
- (Tùy chọn) hiển thị placeholder dưới form: `<EmptyState title="Lịch sử gửi sắp ra mắt" subtitle="Tính năng xem lại các thông báo đã broadcast đang được phát triển." mascotVariant="thinking" mascotSize={120} />`

**Future (out of scope):** đề xuất backend bổ sung `GET /api/staff/notifications?page=&size=` trả danh sách broadcast đã gửi (group theo `ruleKey`/batch) + `deliveredCount`. Khi có, dựng lại bảng theo cấu trúc v1.0 (đã lưu trong git history).

---

## 9. LOADING / ERROR / EMPTY

- **Sending:** nút "Gửi thông báo" `aria-busy`, disabled, spinner.
- **Error:** toast `error` với message từ backend (`err.response.data.message`); giữ nguyên form để gửi lại.
- **Empty:** không áp dụng (form luôn hiển thị).

---

## 10. CSS KEY CLASSES

```css
.nfs-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.nfs-body { flex: 1; max-width: 760px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.nfs-compose-card { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: 0 2px 8px rgba(0,0,0,0.07); padding: 24px 28px; display: flex; flex-direction: column; gap: 18px; }
.nfs-field { display: flex; flex-direction: column; gap: 6px; }
.nfs-radios { display: flex; flex-wrap: wrap; gap: 8px; }
.nfs-radio { display: inline-flex; align-items: center; gap: 6px; padding: 8px 14px; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); font-size: 13px; font-weight: 600; color: var(--color-text-sub); cursor: pointer; }
.nfs-radio--on { border-color: var(--color-primary); background: var(--color-primary-bg); color: var(--color-primary-dark); }
.nfs-send-btn { align-self: flex-end; height: 44px; padding: 0 26px; border-radius: var(--radius-full); background: var(--color-secondary); color: #fff; border: none; font-weight: 700; cursor: pointer; box-shadow: 0 2px 8px rgba(93,187,105,0.25); }
.nfs-send-btn:disabled { opacity: 0.6; cursor: not-allowed; }
```

---

## 11. RESPONSIVE

```css
@media (max-width: 1199px) { .nfs-body { padding: 24px 20px 40px; } }
@media (max-width: 767px)  {
  .nfs-body { padding: 16px 16px 32px; }
  .nfs-compose-card { padding: 18px 16px; }
  .nfs-send-btn { width: 100%; }
}
@media (prefers-reduced-motion: reduce) { .nfs-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 12. ACCESSIBILITY

- [ ] Radio groups: `<fieldset>` + `<legend>` cho mỗi nhóm (Loại, Kênh, Đối tượng, Thời gian).
- [ ] Date/time hẹn giờ: `min={today}` chặn ngày quá khứ.
- [ ] Confirm broadcast toàn bộ: `role="alertdialog"`, `aria-modal="true"`, focus trap, Escape hủy.
- [ ] Nút gửi: `aria-busy` khi đang gửi.
- [ ] `addToast(type, message)` đúng signature `useToast` (mock cũ dùng đúng — giữ nguyên).
- [ ] Mọi `<input>`/`<textarea>` có `<label htmlFor>`.
```
