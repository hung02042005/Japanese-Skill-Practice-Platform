# SPEC — Xác nhận Email bằng mã OTP (`/verify-email`)
>
> **Sprint:** 1 — Foundation
> **Prefix:** `ve-` | **activeTab:** `''` | **Guard:** Public (không cần đăng nhập)
> **Phụ thuộc:** `USER-SPEC.md §9.4` | **Backend ref:** `feat-auth/UC-02-register.md`
> **Cập nhật:** 2026-07-12 — thay cơ chế xác minh từ **link (token UUID)** sang **mã OTP 6 số nhập tay**

---

## 1. MÔ TẢ TRANG

Trang xác minh email dùng chung cho 2 lối vào:
1. Sau khi đăng ký thành công, `Register.jsx` chuyển hướng thẳng tới `/verify-email?email={email}`.
2. Từ banner "chưa xác minh" trên trang Đăng nhập (`Login.jsx`), khi login thất bại với lỗi `EMAIL_NOT_VERIFIED`.

Trang không đọc token từ URL nữa — chỉ đọc `?email=` để prefill ô email (vẫn cho sửa tay). Người dùng tự nhập mã OTP 6 số nhận được qua email và bấm "Xác minh". Có nút "Gửi lại mã xác minh" kèm cooldown 60 giây khớp với rate limit phía backend.

---

## 2. MOCKUP

```
Trạng thái nhập mã (mặc định):
┌──────────────────────────────────────────────────────────────┐
│                        [AuthTopBar]                          │
│                                                              │
│          ┌─────────────────────────────────────┐            │
│          │         [SakuChan]                   │            │
│          │      Nhập mã xác minh                │            │
│          │  Chúng tôi đã gửi mã gồm 6 chữ số... │            │
│          │                                     │            │
│          │  Email:  [___________________]      │            │
│          │  Mã xác minh: [ 6 chữ số ]           │            │
│          │                                     │            │
│          │      [ Xác minh ]                    │            │
│          │                                     │            │
│          │  📧 Gửi lại mã xác minh (nếu cooldown│            │
│          │     hiện "Gửi lại mã (45s)")          │            │
│          │  ← Về trang đăng nhập                 │            │
│          └─────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────┘

Trạng thái "success":
│          │  [SakuChan happy]                     │
│          │  Xác minh thành công!                 │
│          │  Email của bạn đã được xác minh...    │
│          │     [ĐĂNG NHẬP NGAY]                  │
```

---

## 3. FILE

```
pages/verify-email/
├── VerifyEmail.jsx    (đã refactor sang OTP entry — không còn auto-verify theo token URL)
└── VerifyEmail.css
```

---

## 4. STATE

```js
const [email, setEmail]     = useState(searchParams.get('email') ?? '');
const [otpCode, setOtpCode] = useState('');

const [state, setState]       = useState('idle');   // 'idle' | 'verifying' | 'success' | 'error'
const [errorMsg, setErrorMsg] = useState('');

const [resendStatus, setResendStatus] = useState('idle'); // idle | loading | sent | error
const [resendMsg, setResendMsg]       = useState('');
const [cooldown, startCooldown]       = useCountdown(); // hooks/useCountdown.js
```

---

## 5. API CALLS

```js
// 1. Xác minh mã OTP
// POST /api/auth/verify-email
// Request: { "email": "user@example.com", "otpCode": "123456" }
// Response 200: { message: "Xác minh email thành công. Bạn có thể đăng nhập." }
// Response 400: { errorCode: "INVALID_OTP" | "OTP_EXPIRED" | "ACCOUNT_NOT_VERIFIABLE" }
// Response 429: { errorCode: "TOO_MANY_ATTEMPTS" }

// 2. Gửi lại mã OTP
// POST /api/auth/resend-verification
// Request: { "email": "user@example.com" }
// Response 200 luôn trả về (kể cả email không tồn tại — chống enumeration)
// Response 429: { errorCode: "TOO_MANY_REQUESTS" } — chưa đủ 60 giây từ lần gửi trước
```

---

## 6. LUỒNG XỬ LÝ

1. `handleVerify` — dispatch `verifyEmailThunk({ email, otpCode })`. Thành công → `state = 'success'`. Thất bại → `state = 'error'`, hiển thị `errorMsg` (không tự động phân loại theo mã lỗi cụ thể, hiển thị message backend trả về).
2. `handleResend` — dispatch `resendVerificationThunk(email)`. Thành công → `resendStatus = 'sent'`, khởi động cooldown 60 giây (`startCooldown(60)`). Nút resend bị disable trong lúc `cooldown > 0`.
3. Không có bước "verifying" tự động khi vào trang — trang luôn ở trạng thái nhập liệu cho tới khi user bấm "Xác minh" (khác với hành vi cũ vốn tự verify ngay khi có `?token=`).

---

## 7. 3 TRẠNG THÁI CHÍNH

| Trạng thái | Mô tả |
|:---|:---|
| Nhập mã (mặc định) | Form email + OTP, nút Xác minh, nút Gửi lại mã (có cooldown) |
| `success` | SakuChan happy, nút "ĐĂNG NHẬP NGAY" |
| lỗi khi xác minh | Banner lỗi ngay trong form (`INVALID_OTP` / `OTP_EXPIRED` / `TOO_MANY_ATTEMPTS`), form vẫn mở để thử lại hoặc gửi lại mã |

---

## 8. DOMAIN RULES

- Mã OTP gồm 6 chữ số, hết hạn sau **10 phút** kể từ lúc backend sinh ra (đăng ký hoặc gửi lại).
- Gửi lại mã bị giới hạn **1 lần / 60 giây** — khớp với cooldown hiển thị trên nút.
- Nhập sai OTP quá **5 lần liên tiếp** → backend xoá mã hiện tại, bắt buộc phải gửi lại mã mới trước khi thử tiếp.
- Resend luôn trả HTTP 200 (trừ khi đang bị rate-limit) kể cả khi email không tồn tại, để tránh user enumeration.
- Không tự động đăng nhập sau khi xác minh thành công — user phải quay lại `/login`.
