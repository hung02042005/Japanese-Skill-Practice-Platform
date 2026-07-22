# SPEC — Admin Settings › Email › Nội Dung Email (Email Content / Body)

> **Feature ID:** `feat-admin` | **Page:** `AdminSettings` › Tab **Email**
> **Route:** `/admin/settings?tab=email`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-07-22
> **Bổ sung cho:** `SPEC-admin-settings.md § 5` (Tab Email / SMTP)
> **Backend ref:** `EmailService.java`, `AdminSettingsService.java`, `feat-system-admin/SPEC.md § UC-39`

---

## 1. VẤN ĐỀ & MỤC TIÊU

### 1.1 Hiện trạng (gap)

| Thành phần | Hiện tại | Vấn đề |
|:---|:---|:---|
| UI `EmailTab` | Mỗi loại email chỉ cấu hình `from_email`, `from_name`, `subject` | **Không có ô Nội dung email** — admin không thể sửa nội dung gửi cho học viên |
| DB `system_settings` | 3 nhóm `email_register` / `email_otp` / `email_reset` chỉ có 3 key trên | Thiếu key lưu nội dung (`body_html`) |
| `EmailService` | **Hard-code** cả `subject` lẫn HTML body trong Java (`buildVerificationOtpEmailBody`, `buildOtpEmailBody`, `buildPasswordResetEmailBody`, …) | Ngay cả `subject` admin đã lưu trong DB **cũng bị bỏ qua**. Muốn đổi 1 chữ phải sửa code + build lại |

> **Nghĩa là:** phần cấu hình email hiện tại là **UI giả** — người dùng bấm Lưu thành công nhưng email gửi ra không đổi. Spec này biến nó thành cấu hình thật.

### 1.2 Mục tiêu

1. **FE:** Thêm ô **"Nội dung email"** cho **mọi** loại email trong tab Email — nhập **văn bản thường** (không phải HTML).
2. **DB:** Thêm key `body_text` cho mỗi nhóm email; seed = đoạn lời nhắn thân thiện mặc định.
3. **BE:** `EmailService` **đọc `subject` + `body_text` từ DB**, thay biến placeholder (`{{platform_name}}`…), escape + bọc vào **khung HTML cố định trong code** (header/OTP box/nút/footer). Fallback đoạn mặc định khi DB trống (LESSON-006 — không silent fail).
4. **Phủ "tất cả":** chuẩn hoá danh mục loại email để bao trọn mọi luồng gửi mail đang tồn tại trong `EmailService`.

> **📌 Quyết định thiết kế (v1.1):** admin **không nhập HTML thô**. Ban đầu lưu cả trang HTML vào `body_html` → giao diện hiện "code" khó dùng, dễ vỡ layout email. Đổi sang: khung đẹp cố định trong `EmailService`; admin chỉ sửa **đoạn lời nhắn** (`body_text`). `EmailService.textToHtml()` escape văn bản + tách đoạn theo dòng trống thành `<p>`. Mã OTP / nút / link do khung tự chèn — admin không đụng tới.

---

## 2. DANH MỤC LOẠI EMAIL ("cho tất cả")

`EmailService` hiện gửi 7 loại mail. Spec chuẩn hoá thành **6 nhóm cấu hình** (`setting_group`), gộp 2 luồng dùng chung template OTP:

| # | `setting_group` | Tiêu đề card (UI) | Hàm gửi trong `EmailService` | Trạng thái nhóm |
|:--|:---|:---|:---|:---|
| 1 | `email_register` | Email Xác Nhận Đăng Ký | `sendVerificationEmail` | ✅ Đã có → thêm `body_html` |
| 2 | `email_otp` | Email Mã Xác Thực (OTP) | `sendOtpEmail` | ✅ Đã có → thêm `body_html` |
| 3 | `email_reset` | Email Đặt Lại Mật Khẩu (Học viên) | `sendPasswordResetEmail` | ✅ Đã có → thêm `body_html` |
| 4 | `email_staff_invite` | Email Mời Staff (Thiết lập mật khẩu) | `sendStaffInvitationEmail` | 🆕 Nhóm mới |
| 5 | `email_staff_temp_pwd` | Email Mật Khẩu Tạm Thời (Staff) | `sendStaffTempPassword` | 🆕 Nhóm mới |
| 6 | `email_admin_notify` | Email Thông Báo Admin (Staff reset) | `notifyAdminPasswordReset` | 🆕 Nhóm mới |

> **Ngoài phạm vi nhóm cấu hình theo template:** `sendNotificationEmail` (UC-30 — thông báo hệ thống) đã lấy `title`/`content` động từ dữ liệu runtime, **không** đưa vào tab này (nội dung do rule/notification quyết định, không phải template tĩnh). Chỉ khung bao (header/footer) là cố định — ghi rõ ở § OUT OF SCOPE.

> ⚠️ **Quyết định mở rộng nhóm (3 → 6):** nếu team muốn giữ tối giản MVP, có thể chỉ làm 3 nhóm đã tồn tại (`email_register`, `email_otp`, `email_reset`) ở Phase 1 và đưa nhóm 4–6 sang Phase 2. Đánh dấu rõ trong § 9 Rollout.

---

## 3. MÔ HÌNH DỮ LIỆU

### 3.1 Key mới cho mỗi nhóm email

Mỗi `setting_group` email có bộ key sau:

| `setting_key` | `value_type` | Ý nghĩa | Bắt buộc |
|:---|:---|:---|:---|
| `from_email` | string | Email người gửi (đã có) | ✅ |
| `from_name` | string | Tên hiển thị (đã có) | ✅ |
| `subject` | string | Tiêu đề — **cho phép chứa placeholder** | ✅ |
| `body_text` | string | **Lời nhắn dạng văn bản thường** — cho phép chứa placeholder `{{...}}` | ✅ (🆕) |
| `enabled` | boolean | Bật/tắt loại email này (mặc định `true`) | ❌ (tuỳ chọn Phase 2) |

- `body_text` lưu ở cột `setting_value` kiểu `LONGTEXT` — đã hỗ trợ sẵn (xem `V1__init_schema.sql`), **không cần đổi schema**.
- Khung HTML (header/OTP box/nút/footer) **không lưu DB** — cố định trong `EmailService`.
- **Charset:** đảm bảo `utf8mb4` (ADR-009 điều 2) — nội dung chứa kanji/emoji.

### 3.2 Migration

- `V29__seed_email_body_content.sql` — bản đầu (đã seed `body_html`, nay bỏ).
- `V30__replace_email_body_with_text.sql` — **DELETE** `body_html` của 3 nhóm + **INSERT IGNORE** `body_text` mặc định (idempotent theo `UQ_setting`).

```sql
-- Ví dụ nhóm email_otp
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
  ('email_otp', 'body_text',
   'Sử dụng mã bên dưới để hoàn tất xác thực...', 'string');
```

> **Lưu ý MySQL (ADR-009):** seed là văn bản thuần (không HTML) → tránh hẳn việc escape dấu ngoặc trong `'Segoe UI'`. Test trên DB trống vì DDL/seed không rollback được giữa chừng.

---

## 4. BIẾN PLACEHOLDER (Template Variables)

Cú pháp thống nhất: **`{{tên_biến}}`** (double curly). BE thay bằng `String.replace` (`substituteRaw`) **trước** khi escape + bọc `<p>`.

> **Chỉ dùng trong `body_text`/`subject`.** Mã OTP, nút bấm, link đặt lại **không phải placeholder** — khung tự chèn, admin không nhập.

| Nhóm | Placeholder khả dụng trong lời nhắn |
|:---|:---|
| `email_register` | `{{expiry_minutes}}` (mặc định 10) |
| `email_otp` | — (chỉ biến chung) |
| `email_reset` | `{{expiry_hours}}` (mặc định 1) |

**Biến dùng chung mọi nhóm:** `{{platform_name}}`, `{{current_year}}`, `{{support_email}}`.

**Quy tắc BE:**

- Placeholder không khớp biến nào → **giữ nguyên literal** (không xoá).
- `body_text` trống trong DB → BE dùng đoạn mặc định (`DEFAULT_*_CONTENT`) + vẫn gửi (LESSON-006).

---

## 5. THAY ĐỔI BACKEND

### 5.1 `EmailService` — đọc template từ DB

Thêm hàm resolve dùng chung, thay cho các `build*EmailBody` hard-code:

```java
/** Lấy template email theo nhóm; fallback default nếu trống/thiếu placeholder bắt buộc. */
private ResolvedEmail resolveTemplate(String group, Map<String,String> vars, EmailDefaults fallback) {
    String subject = settingRepository.findBySettingGroupAndSettingKey(group, "subject")
            .map(SystemSetting::getSettingValue).filter(StringUtils::hasText)
            .orElse(fallback.subject());
    String body = settingRepository.findBySettingGroupAndSettingKey(group, "body_html")
            .map(SystemSetting::getSettingValue).filter(StringUtils::hasText)
            .orElse(fallback.bodyHtml());
    // validate placeholder bắt buộc → nếu thiếu, dùng fallback + log ERROR
    return new ResolvedEmail(substitute(subject, vars), substitute(body, vars));
}
```

- `substitute()` thay `{{key}}` → value cho mọi entry trong `vars` + biến dùng chung.
- **Giữ nguyên** các `build*EmailBody` hiện tại làm **default fallback** (đổi tên thành hằng/`EmailDefaults`), **không xoá** — đảm bảo không silent fail khi DB trống.
- Mỗi hàm `send*` đổi thành: build `vars` → `resolveTemplate` → `sendHtmlEmail`.

### 5.2 `AdminSettingsService` — cho phép nhóm mới

- Bổ sung `ALLOWED_GROUPS`: thêm `email_staff_invite`, `email_staff_temp_pwd`, `email_admin_notify` (xem `AdminSettingsService.java:22`).
- Không cần logic đặc biệt cho `body_html` (đã là string thường); `isPassword()` không match nên lưu bình thường.

### 5.3 Bảo mật nội dung

- `body_html` do **Admin** nhập (role ADMIN, `@PreAuthorize` đã có) → chấp nhận HTML thô (self-XSS scope hẹp, không phải input người dùng cuối).
- Giá trị runtime nhét vào placeholder (OTP, tên staff…) **phải HTML-escape** trước khi substitute để tránh phá layout / injection qua tên hiển thị.

---

## 6. THAY ĐỔI FRONTEND

### 6.1 `EmailTab.jsx` — thêm field Nội dung

Trong `EMAIL_TYPE_FIELDS`, bổ sung field `body_html`:

```js
const EMAIL_TYPE_FIELDS = [
  { key: 'from_email', label: 'Email người gửi', type: 'email',    fullWidth: true  },
  { key: 'from_name',  label: 'Tên hiển thị',    type: 'text',     fullWidth: false },
  { key: 'subject',    label: 'Tiêu đề email',   type: 'text',     fullWidth: false },
  { key: 'body_html',  label: 'Nội dung email',  type: 'textarea', fullWidth: true,
    rows: 12, mono: true },          // 🆕
];
```

- Render nhánh `type === 'textarea'` trong `EmailTypeCard` (hiện chỉ có `<input>`).
- **Textarea:** `rows=12`, font mono (`--font-mono`), `resize: vertical`, `white-space: pre`, min-height ~240px.
- Giữ nguyên luồng save hiện có: `updateSettings(group, EMAIL_TYPE_FIELDS.map(...))` — vì gửi cả nhóm nên `body_html` tự đi kèm, **không cần đổi `adminService.js`**.

### 6.2 Bảng biến placeholder (UX)

Dưới textarea, hiển thị **chip danh sách biến khả dụng của nhóm đó** (click chip = chèn `{{biến}}` tại con trỏ):

```
[ {{otp_code}} ] [ {{expiry_minutes}} ] [ {{platform_name}} ] [ {{current_year}} ]
Hint: "Bấm để chèn biến. Hệ thống sẽ tự thay bằng giá trị thật khi gửi."
```

Map biến theo `group` (đối chiếu § 4). Định nghĩa 1 object `PLACEHOLDERS_BY_GROUP` trong `EmailTab.jsx`.

### 6.3 Xem trước (Preview) — tuỳ chọn Phase 1.5

- Nút **"Xem trước"** mở modal render `body_html` (đã thay biến bằng giá trị mẫu: `123456`, link giả…) trong `<iframe sandbox>` để cô lập CSS/script.
- Nếu chưa làm ở Phase 1 → ghi vào § 9.

### 6.4 Validation (client L1 — BE vẫn validate lại)

Cập nhật `emailTypeFieldError(key, value)`:

| key | Rule |
|:---|:---|
| `subject` | Bắt buộc, 1–200 ký tự |
| `body_html` | Bắt buộc, ≥ 20 ký tự; **phải chứa mọi placeholder bắt buộc của nhóm** (vd nhóm `email_otp` phải có `{{otp_code}}`) → nếu thiếu, báo lỗi inline: *"Nội dung phải chứa biến {{otp_code}}"* |
| `from_email` | Email hợp lệ |
| `from_name` | Bắt buộc |

---

## 7. API

**Không phát sinh endpoint mới.** Dùng lại UC-39:

```
GET /api/admin/settings/{group}          — group ∈ 6 nhóm email → trả cả body_html
PUT /api/admin/settings/{group}          — lưu cả nhóm (gồm body_html) atomic
```

- `getByGroup` trả `body_html` bình thường (không phải password nên không bị mask).
- Payload PUT: `{ settings: [{settingKey:'body_html', settingValue:'<html>...'}, ...] }`.

---

## 8. LOADING / ERROR / EMPTY

| Case | Xử lý |
|:---|:---|
| Đang tải template | Skeleton cho từng field (giữ `FieldSkeleton`), textarea → skeleton cao |
| Lưu thành công | Toast: *"Đã lưu cài đặt \"{title}\" thành công"* |
| Lỗi lưu | Toast đỏ + message từ BE |
| Body trống ở DB (runtime) | BE fallback template mặc định + log — **email vẫn gửi** |
| Thiếu placeholder bắt buộc | Chặn ở FE (inline error) **và** BE (fallback + log ERROR) |

---

## 9. ROLLOUT / PHASING

| Phase | Nội dung |
|:---|:---|
| **P1** | `body_html` cho 3 nhóm đã có (`email_register`, `email_otp`, `email_reset`) + BE đọc template + fallback + FE textarea + chip biến |
| **P1.5** | Nút Xem trước (iframe sandbox) |
| **P2** | 3 nhóm mới (`email_staff_invite`, `email_staff_temp_pwd`, `email_admin_notify`) + key `enabled` bật/tắt |

---

## 10. OUT OF SCOPE

- ❌ WYSIWYG editor (rich text) — Phase 1 dùng textarea HTML thô.
- ❌ Đa ngôn ngữ template (i18n theo locale người nhận).
- ❌ Cấu hình template cho `sendNotificationEmail` (UC-30) — nội dung do rule quyết định; chỉ khung header/footer cố định, không đưa vào tab này.
- ❌ Lịch sử phiên bản template / rollback (audit settings) — Phase 2.
- ❌ Đính kèm file trong email.
- ❌ Gửi email test tới địa chỉ tuỳ ý từ tab này (khác với "Kiểm tra kết nối SMTP" đã có).

---

## 11. ACCEPTANCE CRITERIA

1. ✅ Admin mở `/admin/settings?tab=email` thấy ô **"Nội dung email"** ở mỗi card loại email, đã nạp sẵn HTML mặc định.
2. ✅ Sửa `subject` + `body_html`, bấm Lưu → gọi `PUT /api/admin/settings/{group}` thành công, reload giữ giá trị mới.
3. ✅ Gửi thật (đăng ký/OTP/reset…) → email nhận được dùng **đúng subject + body admin đã lưu**, placeholder được thay bằng giá trị thật.
4. ✅ Xoá trắng `body_html` trong DB → email vẫn gửi bằng template mặc định (không lỗi, có log).
5. ✅ Lưu `body_html` thiếu `{{otp_code}}` cho nhóm OTP → FE báo lỗi inline; nếu lọt xuống BE → BE fallback + log ERROR, email vẫn có mã.
6. ✅ 3 nhóm mới (nếu làm P2) được `ALLOWED_GROUPS` chấp nhận, không trả 400 `INVALID_SETTING_GROUP`.

---

## 12. FILE ẢNH HƯỞNG

**Backend:**
```
apps/backend/src/main/java/com/jlpt/shared/email/EmailService.java        (đọc template + substitute + fallback)
apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java (ALLOWED_GROUPS += 3 nhóm mới)
apps/backend/src/main/resources/db/migration/V29__seed_email_body_content.sql (🆕 seed)
```

**Frontend:**
```
apps/frontend/src/components/admin/settings/EmailTab.jsx   (field body_html + chip biến + validation + textarea render)
apps/frontend/src/utils/validation.js                      (nếu cần helper mới cho body/placeholder)
```

**Docs:**
```
docs/03-Interface-Specs/feature-specs/frontend/feat-admin/SPEC-admin-settings.md (cập nhật § 5 tham chiếu spec này)
```
