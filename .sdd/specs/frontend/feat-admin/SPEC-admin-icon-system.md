# SPEC — Admin Icon System (Hệ thống Icon SVG)
>
> **Feature ID:** `feat-admin`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-02
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Source file:** `apps/frontend/src/components/admin/ManageUsersIcons.jsx`

---

## 1. NGUYÊN TẮC THIẾT KẾ

### 1.1 Xuất phát từ brand SakuJi

Logo SakuJi được tạo từ hai hình học cốt lõi:

- **Ellipse nghiêng** — cánh hoa sakura (petal), đặc trưng của mascot Saku-chan
- **Hình tròn** — mắt Saku-chan, nhụy hoa, crown jewels

Icon admin kế thừa DNA này: các icon **stroke-based** (phác thảo, không solid) với **ellipse petal làm điểm nhấn trang trí** ở opacity thấp (0.08–0.22). Kết quả: icon trông như "phần mở rộng" của mascot, không phải bộ icon generic nhập từ bên ngoài.

### 1.2 Quy tắc kỹ thuật

| Thuộc tính | Quy tắc |
|:---|:---|
| ViewBox | `0 0 24 24` (standard) hoặc `0 0 28 28` (stat icons) |
| Stroke width | `2px` cho action/nav icons; `1.8px` cho tab icons; `2.2px` cho large |
| Stroke cap | `round` |
| Stroke join | `round` |
| Fill | `none` (stroke only) + low-opacity fill accent khi cần |
| Color | `currentColor` — kế thừa từ CSS parent |
| Petal accent | `<ellipse>` fill `currentColor` opacity `0.08–0.22` — trang trí, không semantic |
| Aria | `aria-hidden="true"` trên mọi icon decorative |
| Size | Navigation: 20×20 \| Actions: 13×13 \| Stat: 26×26 \| Tab: 16×16 \| Header: 14×14 |

### 1.3 Không được làm

- ❌ Dùng emoji (🚫, ✅, 🔑) làm icon — không scale, không accessible, không consistent
- ❌ Import icon từ thư viện ngoài (react-icons, heroicons) — phá vỡ visual DNA SakuJi
- ❌ Hard-code màu hex trong SVG — dùng `currentColor` hoặc CSS variable
- ❌ Thêm `<title>` hoặc `<desc>` vào icon decorative — đã có `aria-hidden`
- ❌ Icon solid fill hoàn toàn — brand SakuJi luôn nhẹ nhàng, thoáng

---

## 2. FILE ICON DUY NHẤT

Toàn bộ icon admin tập trung trong một file:

```
apps/frontend/src/components/admin/ManageUsersIcons.jsx
```

Không tạo file icon riêng cho từng trang — tránh phân tán, dễ maintain.

**Import pattern:**

```jsx
import {
  // Stat icons
  STAT_ICONS,           // object map: { total, active, suspended, pending }
  // Tab icons
  TAB_ICONS,            // object map: { student, staff, admin }
  // Action icons
  IcBan, IcCheck, IcKey, IcSwap, IcTrash,
  // Header/chrome icons
  IcAdminChip, IcAddStaff, IcSearchGlass,
  // Modal icon
  IcBloomCheck,
  // NEW — Dashboard
  IcChart, IcSystemHealth,
  // NEW — Settings
  IcMail, IcShield, IcWrench, IcBell, IcPlus, IcEdit,
} from '../../components/admin/ManageUsersIcons';
```

---

## 3. INVENTORY — ICONS HIỆN CÓ

### 3.1 Stat Card Icons (28×28 viewBox)

Dùng trong stat cards của ManageUsers và AdminDashboard. Export qua `STAT_ICONS` object.

---

**`StatIconUsers`** — `STAT_ICONS.total`

```svg
<!--  Hai người chồng lên, ellipse petal nhỏ góc trên phải -->
<svg width="26" height="26" viewBox="0 0 28 28" fill="none">
  <!-- người chính: circle + path -->
  <circle cx="11" cy="9.5" r="4.5" fill="currentColor" opacity="0.15"/>
  <circle cx="11" cy="9.5" r="3.2" stroke="currentColor" strokeWidth="1.8"/>
  <path d="M3 24c0-4.418 3.582-8 8-8s8 3.582 8 8" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
  <!-- người phụ (mờ hơn) -->
  <circle cx="20" cy="10.5" r="2.8" fill="currentColor" opacity="0.08"/>
  <circle cx="20" cy="10.5" r="1.9" stroke="currentColor" strokeWidth="1.3" opacity="0.5"/>
  <path d="M16.5 24c.3-3 2.2-5.5 4.5-6.3" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" opacity="0.45"/>
  <!-- petal accent -->
  <ellipse cx="23.5" cy="3.5" rx="1.5" ry="2.5" fill="currentColor" opacity="0.2" transform="rotate(-20 23.5 3.5)"/>
</svg>
```

---

**`StatIconActive`** — `STAT_ICONS.active`

```svg
<!-- Hoa 5 cánh (5 ellipse) + circle tâm + checkmark -->
<svg width="26" height="26" viewBox="0 0 28 28" fill="none">
  <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(0 14 14)"/>
  <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(72 14 14)"/>
  <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(144 14 14)"/>
  <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(216 14 14)"/>
  <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(288 14 14)"/>
  <circle cx="14" cy="14" r="5.5" fill="currentColor" opacity="0.13"/>
  <circle cx="14" cy="14" r="4" fill="currentColor" opacity="0.28"/>
  <path d="M10.5 14l2.5 2.5 4.5-5" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
</svg>
```

> **Giải thích:** 5 ellipse = 5 cánh hoa sakura đối xứng (DNA petal của SakuJi). Circle tâm + checkmark = "hoạt động tốt". Pattern này lấy cảm hứng từ nhị hoa của bông anh đào.

---

**`StatIconSuspended`** — `STAT_ICONS.suspended`

```svg
<!-- Khóa + ellipse petal góc trên -->
<svg width="26" height="26" viewBox="0 0 28 28" fill="none">
  <rect x="5" y="13" width="18" height="12" rx="3" fill="currentColor" opacity="0.1"/>
  <rect x="5" y="13" width="18" height="12" rx="3" stroke="currentColor" strokeWidth="1.8"/>
  <path d="M9.5 13V10a4.5 4.5 0 0 1 9 0v3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
  <circle cx="14" cy="19.5" r="1.8" fill="currentColor" opacity="0.5"/>
  <path d="M14 19.5v1.8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
  <ellipse cx="21.5" cy="4" rx="1.4" ry="2.4" fill="currentColor" opacity="0.18" transform="rotate(-18 21.5 4)"/>
</svg>
```

---

**`StatIconPending`** — `STAT_ICONS.pending`

```svg
<!-- Đồng hồ + 3 chấm nhỏ bên dưới (đang chờ) -->
<svg width="26" height="26" viewBox="0 0 28 28" fill="none">
  <circle cx="14" cy="14" r="10.5" fill="currentColor" opacity="0.07"/>
  <circle cx="14" cy="14" r="9.5" stroke="currentColor" strokeWidth="1.8"/>
  <path d="M14 7v7l4.5 2.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
  <circle cx="8"  cy="25.5" r="1.1" fill="currentColor" opacity="0.3"/>
  <circle cx="12" cy="26.8" r="1.1" fill="currentColor" opacity="0.2"/>
  <circle cx="16" cy="27.2" r="1.1" fill="currentColor" opacity="0.12"/>
</svg>
```

---

### 3.2 Row Action Icons (24×24 viewBox, render 13×13)

---

**`IcBan`** — Đình chỉ tài khoản

```svg
<svg width="13" height="13" viewBox="0 0 24 24" fill="none">
  <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2.2"/>
  <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" stroke="currentColor" strokeWidth="2.2"/>
</svg>
```

---

**`IcCheck`** — Kích hoạt tài khoản

```svg
<svg width="13" height="13" viewBox="0 0 24 24" fill="none">
  <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
</svg>
```

---

**`IcKey`** — Đặt lại mật khẩu

```svg
<!-- Chìa khóa: circle (đầu) + path thân + 2 răng -->
<svg width="13" height="13" viewBox="0 0 24 24" fill="none">
  <circle cx="7.5" cy="15.5" r="5.5" stroke="currentColor" strokeWidth="2"/>
  <path d="M21 2l-9.5 9.5"  stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
  <path d="M19 4l-2 2"      stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
  <path d="M16 7l-2 2"      stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
</svg>
```

---

**`IcSwap`** — Đổi vai trò

```svg
<!-- Hai mũi tên ngược chiều (lên/xuống) -->
<svg width="13" height="13" viewBox="0 0 24 24" fill="none">
  <path d="M7 16H3l4-4 4 4H7V8"  stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  <path d="M17 8h4l-4 4-4-4h4v8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
</svg>
```

---

**`IcTrash`** — Xóa mềm

```svg
<svg width="13" height="13" viewBox="0 0 24 24" fill="none">
  <path d="M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6M10 11v6M14 11v6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
</svg>
```

---

### 3.3 Header / Chrome Icons

---

**`IcAdminChip`** — Crown chip cho AdminPageHeader

```svg
<!-- Crown shape đặc trưng SakuJi: 3 đỉnh + base + circle nhụy trên cùng -->
<svg width="13" height="13" viewBox="0 0 22 18" fill="none">
  <path d="M2 14L5 5l4.5 4L11 2l1.5 7L17 5l3 9H2z" fill="currentColor" opacity="0.82" strokeLinejoin="round"/>
  <rect x="2" y="14.5" width="18" height="2.5" rx="1.25" fill="currentColor" opacity="0.82"/>
  <circle cx="11" cy="2" r="1.3" fill="currentColor"/>
</svg>
```

---

**`IcAddStaff`** — Tạo Staff mới

```svg
<!-- Cây đang mọc (+ dưới → lên trên): cách điệu hóa "Staff mới phát triển" -->
<svg width="14" height="14" viewBox="0 0 24 24" fill="none">
  <path d="M12 21v-8M12 13C9 13 5.5 10.5 4 6c3.5.5 6.5 3.5 8 7M12 13c3 0 6.5-2.5 8-7-3.5.5-6.5 3.5-8 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  <path d="M12 7V2M9.5 4.5h5" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
</svg>
```

---

**`IcSearchGlass`** — Thanh tìm kiếm

```svg
<!-- Kính lúp + hào quang tâm nhỏ (gợi ánh sáng tìm kiếm) -->
<svg width="17" height="17" viewBox="0 0 24 24" fill="none">
  <circle cx="10.5" cy="10.5" r="7.5" stroke="currentColor" strokeWidth="2"/>
  <circle cx="10.5" cy="10.5" r="3" fill="currentColor" opacity="0.1"/>
  <ellipse cx="10.5" cy="7.5" rx="1.1" ry="1.8" fill="currentColor" opacity="0.18"/>
  <ellipse cx="10.5" cy="7.5" rx="1.1" ry="1.8" fill="currentColor" opacity="0.18" transform="rotate(90 10.5 10.5)"/>
  <path d="M16.5 16.5l4.2 4.2" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round"/>
</svg>
```

---

### 3.4 Tab Icons — TAB_ICONS (16×16)

---

**`TAB_ICONS.student`** — Học viên

```svg
<!-- Mũ tốt nghiệp + ngọn bên phải (học viên đang lớn lên) -->
<svg width="16" height="16" viewBox="0 0 24 24" fill="none">
  <path d="M12 3L2 9l10 6 10-6-10-6z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" fill="currentColor" opacity="0.12"/>
  <path d="M6 12.5v4C6 18.43 8.686 20 12 20s6-1.57 6-3.5v-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
  <path d="M20.5 9.5v5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
  <ellipse cx="20.5" cy="15.5" rx="1.3" ry="2" fill="currentColor" opacity="0.45" transform="rotate(12 20.5 15.5)"/>
</svg>
```

> Ellipse nghiêng ở đầu gậy = cánh hoa SakuJi accent.

---

**`TAB_ICONS.staff`** — Nhân viên

```svg
<!-- Người + lá (staff như người chăm sóc vườn hoa) -->
<svg width="16" height="16" viewBox="0 0 24 24" fill="none">
  <circle cx="12" cy="7" r="3.5" stroke="currentColor" strokeWidth="1.8"/>
  <path d="M4.5 21c0-4.14 3.36-7.5 7.5-7.5s7.5 3.36 7.5 7.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
  <path d="M17.5 11c0-2.5 3-4.5 5.5-4.5C22.5 9 20.5 11 17.5 11z" fill="currentColor" opacity="0.38" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round"/>
  <path d="M17.5 11c.5-1.5 2.5-3 4-3" stroke="currentColor" strokeWidth="1" strokeLinecap="round" opacity="0.5"/>
</svg>
```

---

**`TAB_ICONS.admin`** — Quản trị

```svg
<!-- Ngôi sao 5 cánh + ellipse nhỏ tâm trên (kế thừa crown jewel từ AdminTopNav) -->
<svg width="16" height="16" viewBox="0 0 24 24" fill="none">
  <path d="M12 2l2.6 8.4H22l-6.3 4.6 2.4 8-6.1-4.4-6.1 4.4 2.4-8L2 10.4h7.4L12 2z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" strokeLinecap="round" fill="currentColor" opacity="0.13"/>
  <ellipse cx="12" cy="2.8" rx="1.1" ry="1.7" fill="currentColor" opacity="0.42"/>
</svg>
```

---

### 3.5 Modal Icon

---

**`IcBloomCheck`** — Xác nhận thành công (dùng màu cụ thể, không currentColor)

```svg
<!-- Hoa 5 cánh đầy đủ + checkmark — cùng pattern với StatIconActive nhưng dùng --color-primary -->
<svg width="18" height="18" viewBox="0 0 24 24" fill="none">
  <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(0 12 12)"/>
  <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(72 12 12)"/>
  <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(144 12 12)"/>
  <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(216 12 12)"/>
  <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(288 12 12)"/>
  <circle cx="12" cy="12" r="4.5" fill="var(--color-primary)" opacity="0.14"/>
  <path d="M9 12l2.2 2.2 4-4.5" stroke="var(--color-primary)" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"/>
</svg>
```

---

## 4. ICONS MỚI CẦN THÊM

Các icon dưới đây **chưa có** trong `ManageUsersIcons.jsx`. Thêm vào cuối file, sau các export hiện tại.

---

### 4.1 `IcChart` — Biểu đồ / Analytics (Dashboard stat)

```jsx
export function IcChart() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* 3 cột bar chart */}
      <rect x="3"  y="12" width="4" height="9" rx="1.5" fill="currentColor" opacity="0.18"/>
      <rect x="10" y="6"  width="4" height="15" rx="1.5" fill="currentColor" opacity="0.18"/>
      <rect x="17" y="9"  width="4" height="12" rx="1.5" fill="currentColor" opacity="0.18"/>
      <rect x="3"  y="12" width="4" height="9" rx="1.5" stroke="currentColor" strokeWidth="1.8"/>
      <rect x="10" y="6"  width="4" height="15" rx="1.5" stroke="currentColor" strokeWidth="1.8"/>
      <rect x="17" y="9"  width="4" height="12" rx="1.5" stroke="currentColor" strokeWidth="1.8"/>
      {/* đường baseline */}
      <path d="M2 21h20" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      {/* petal accent nhỏ góc trên phải */}
      <ellipse cx="22" cy="4" rx="1.2" ry="2" fill="currentColor" opacity="0.18" transform="rotate(-15 22 4)"/>
    </svg>
  );
}
```

---

### 4.2 `IcSystemHealth` — Trạng thái hệ thống (Dashboard)

```jsx
export function IcSystemHealth() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Pulse / heartbeat line */}
      <path d="M2 12h4l2-5 3 10 3-8 2 3h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      {/* Glow dot tại peak */}
      <circle cx="9" cy="7" r="1.5" fill="currentColor" opacity="0.35"/>
      {/* Petal trang trí */}
      <ellipse cx="21" cy="4" rx="1.3" ry="2.2" fill="currentColor" opacity="0.15" transform="rotate(-20 21 4)"/>
    </svg>
  );
}
```

---

### 4.3 `IcMail` — Email / SMTP (Settings tab)

```jsx
export function IcMail() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Phong bì */}
      <rect x="2" y="4" width="20" height="16" rx="2.5" fill="currentColor" opacity="0.08"/>
      <rect x="2" y="4" width="20" height="16" rx="2.5" stroke="currentColor" strokeWidth="2"/>
      {/* Chữ V (nếp gấp) */}
      <path d="M2 7l10 7 10-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      {/* Petal nhỏ góc phong bì */}
      <ellipse cx="20.5" cy="3" rx="1" ry="1.8" fill="currentColor" opacity="0.20" transform="rotate(-15 20.5 3)"/>
    </svg>
  );
}
```

---

### 4.4 `IcShield` — Bảo mật (Settings tab)

```jsx
export function IcShield() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Khiên */}
      <path d="M12 2l8 3v7c0 4.5-3.5 8.5-8 10-4.5-1.5-8-5.5-8-10V5l8-3z"
            fill="currentColor" opacity="0.10"
            stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
      {/* Checkmark bên trong */}
      <path d="M8.5 12l2.5 2.5 5-5.5" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}
```

---

### 4.5 `IcWrench` — Bảo trì / Cài đặt (Settings tab)

```jsx
export function IcWrench() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Tay cầm */}
      <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
            fill="currentColor" opacity="0.08"/>
      {/* Petal trang trí nhỏ */}
      <ellipse cx="21" cy="4" rx="1" ry="1.8" fill="currentColor" opacity="0.18" transform="rotate(-30 21 4)"/>
    </svg>
  );
}
```

---

### 4.6 `IcBell` — Thông báo (Settings tab)

```jsx
export function IcBell() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Chuông */}
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
            fill="currentColor" opacity="0.10"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      {/* Đầu chuông */}
      <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      {/* Clapper dot */}
      <circle cx="12" cy="3" r="1.5" fill="currentColor" opacity="0.30"/>
      {/* Petal rung chuông */}
      <ellipse cx="20" cy="5" rx="1" ry="1.8" fill="currentColor" opacity="0.15" transform="rotate(15 20 5)"/>
    </svg>
  );
}
```

---

### 4.7 `IcPlus` — Thêm mới (Tạo notification rule)

```jsx
export function IcPlus() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
    </svg>
  );
}
```

---

### 4.8 `IcEdit` — Chỉnh sửa (Settings inline edit, Rule edit)

```jsx
export function IcEdit() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Bút chì */}
      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"
            fill="currentColor" opacity="0.12"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}
```

---

## 5. NAVIGATION ICONS (AdminTopNav)

Icons trong `AdminTopNav.jsx` là các SVG inline. Chúng theo cùng phong cách (2px stroke, round cap) nhưng nằm trong ADMIN_TABS array, không export từ ManageUsersIcons.

| Tab | Icon shape | Mô tả |
|:---|:---|:---|
| Tổng quan | 4 rect squares (2×2 grid) | Bảng điều khiển dạng ô |
| Người dùng | Hai người (group) | Cộng đồng người dùng |
| Nội dung | File với text lines | Tài liệu/bài học |
| Báo cáo | 3 bar chart lines | Số liệu báo cáo |
| Cài đặt | Gear (cog) | Cài đặt hệ thống |

---

## 6. CROWN BADGE (AdminTopNav)

Crown trong badge ADMIN của AdminTopNav:

```svg
<svg width="11" height="9" viewBox="0 0 11 9" fill="none">
  <!-- Crown: 3 đỉnh + base -->
  <path d="M0.5 8.5L2 3.5L5.5 6.5L9 3.5L10.5 8.5H0.5Z" fill="currentColor"/>
  <!-- 3 jewel circles (trái / giữa / phải) -->
  <circle cx="0.5"  cy="2.5" r="1.5" fill="currentColor"/>
  <circle cx="5.5"  cy="1"   r="1.5" fill="currentColor"/>
  <circle cx="10.5" cy="2.5" r="1.5" fill="currentColor"/>
</svg>
```

Color: `var(--color-admin-crown)` = `#E8637A` — deep petal, không thay đổi.

---

## 7. OBJECT MAPS (export)

Sau khi thêm icons mới, cập nhật 2 object maps:

```jsx
// Giữ nguyên STAT_ICONS và TAB_ICONS hiện có
export const STAT_ICONS = {
  total:     <StatIconUsers />,
  active:    <StatIconActive />,
  suspended: <StatIconSuspended />,
  pending:   <StatIconPending />,
};

export const TAB_ICONS = {
  student: <TabIconStudent />,
  staff:   <TabIconStaff />,
  admin:   <TabIconAdmin />,
};

// NEW — Settings tab icons
export const SETTINGS_TAB_ICONS = {
  system:        <IcWrench />,
  email:         <IcMail />,
  security:      <IcShield />,
  notifications: <IcBell />,
};

// NEW — Dashboard stat icons (thêm vào STAT_ICONS hoặc riêng)
export const DASHBOARD_ICONS = {
  chart:  <IcChart />,
  health: <IcSystemHealth />,
};
```

---

## 8. CHECKLIST ĐỒNG BỘ

Trước khi implement page mới, kiểm tra:

- [ ] Tất cả icon trong page đều dùng `currentColor`
- [ ] Không có emoji hoặc ký tự Unicode dạng icon
- [ ] Không import từ react-icons, heroicons, lucide
- [ ] Mọi icon decorative có `aria-hidden="true"`
- [ ] Action buttons có `aria-label` mô tả rõ hành động + target
- [ ] Size icon đúng chuẩn (xem §1.2)
- [ ] Stroke style đồng nhất (round cap, round join, 2px)
- [ ] Petal accent (ellipse) ở opacity ≤ 0.22 nếu dùng làm trang trí

---

## 9. GHI CHÚ VỀ SAKU-CHAN VÀ ICON

Saku-chan mascot **không phải icon** — không thu nhỏ dưới 80px và không dùng làm button icon. Saku-chan chỉ xuất hiện qua `<SakuChan />` component với các variant: `idle`, `happy`, `thinking`, `correct`, `wrong`, `celebrate`.

Icon trong admin panel là các SVG thuần túy lấy cảm hứng từ **ngôn ngữ hình học** của Saku-chan (ellipse petal, circle tròn mềm), không phải bản thu nhỏ của mascot.
