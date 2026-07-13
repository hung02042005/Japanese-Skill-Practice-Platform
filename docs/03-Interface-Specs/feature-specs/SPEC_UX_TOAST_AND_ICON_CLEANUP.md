# SPEC — Toast Notifications & Emoji Icon Cleanup

> **Feature ID:** `feat-ux-toast-icon`
> **Loại:** Cross-cutting UI/UX (Frontend + Backend message support)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-07-02

---

## 1. CONTEXT & GOAL

### 1.1 Bối cảnh

Hệ thống đã có sẵn cơ chế toast (`apps/frontend/src/components/common/Toast.jsx` — hook `useToast()` + `<ToastContainer>`), nhưng **chỉ được dùng ở 22 page**. Nhiều luồng quan trọng (đăng nhập, đăng ký, quên/đặt lại mật khẩu, xác minh email, nộp bài thi/quiz, luyện tập reading/listening/speaking, onboarding) **không có phản hồi trực quan** khi thành công/thất bại — người dùng không biết thao tác có thành công hay không. Một số nơi còn dùng `alert()` thô (KanaResetButton, VocabResetButton, KanjiList).

Song song, **rất nhiều chỗ trong UI đang dùng emoji làm icon** (vd các stat tile ở TopNav: `🔥/💤` streak, `⭐` từ đã học, `📅` học trong tháng; các badge cấp độ `📚🌿👑`; feature card `🌸📱🔍🎙️📝📖`; nút ghi âm `🎤🎙`; loa phát âm `🔊`; tiêu đề modal `🔑🗑️🔒✏️`; …). Emoji render **khác nhau tùy OS/font**, không đồng bộ với bộ icon SVG tự chế hiện có (`StudentIcons.jsx`) và trông thiếu chuyên nghiệp.

### 1.2 Mục tiêu

1. **Phủ toast** cho **toàn bộ** thao tác có mutation/API còn thiếu ở frontend — mỗi thao tác quan trọng đều có toast success **hoặc** error.
2. Chuẩn hoá cơ chế toast thành **một provider toàn cục** (thay vì lặp `useToast()` cục bộ ở từng page), để dễ dùng và nhất quán.
3. Đảm bảo **backend trả `message` có nghĩa, tiếng Việt, hướng người dùng** trong `ApiResponse` để FE hiển thị đúng nội dung trên toast (đây là phần "backend" của yêu cầu — backend không render toast, mà cung cấp message).
4. **Thay toàn bộ emoji đang dùng làm icon** trên khắp app (không chỉ khu vực AI) bằng **icon SVG tự chế** theo đúng convention bộ icon hiện có (`StudentIcons.jsx`) cho đồng bộ. Giữ nguyên mọi chữ/nhãn (bao gồm chữ "AI").

### 1.3 Tại sao cần?

- Không có toast → người dùng không biết kết quả thao tác → thử lại nhiều lần, mất niềm tin.
- `alert()` chặn luồng (blocking), xấu, không nhất quán style.
- Message tiếng Anh mặc định ("Operation successful", "Resource not found") lọt ra UI tiếng Việt → thiếu chuyên nghiệp.
- Emoji icon render khác nhau theo OS/font → giao diện không đồng nhất.

### 1.4 Phạm vi (Scope)

**IN:**
- Refactor toast → provider toàn cục + migrate 22 page đang dùng.
- Thêm toast cho tất cả page/flow còn thiếu (xem §4 Coverage Matrix).
- Thay `alert()` → toast.
- Audit & Việt hoá `message` ở các endpoint mutation + `GlobalExceptionHandler`.
- Thay **mọi emoji dùng làm icon** trên toàn app → SVG tự chế (xem inventory §3.4).

**OUT (không làm trong spec này):**
- Không thêm/sửa logic nghiệp vụ của bất kỳ tính năng nào.
- Không gỡ tính năng AI (chỉ đổi icon, giữ nguyên chữ "AI" và toàn bộ hành vi).
- Không đổi các icon SVG tự chế hiện có (`StudentIcons.jsx`) — chỉ **tái sử dụng** và **thêm mới** icon còn thiếu.
- Không đổi emoji nằm trong **comment/JSDoc** (chỉ đổi emoji **render ra UI**).
- Không thêm thư viện icon ngoài (không dùng `lucide-react`).
- Không đụng đến i18n/đa ngôn ngữ (hệ thống hiện chỉ tiếng Việt).

---

## 2. ACTOR

| Actor | Vai trò trong spec |
|:---|:---|
| **Mọi role** (Guest, Student, Staff, Manager, Admin) | Người nhận toast phản hồi |
| **Frontend** | Hiển thị toast, đổi icon |
| **Backend** | Cung cấp `message` tiếng Việt trong `ApiResponse` |

---

## 3. THIẾT KẾ KỸ THUẬT

### 3.1 Toast Provider toàn cục (thay hook cục bộ)

**Hiện trạng:** `useToast()` tạo state cục bộ trong mỗi page, mỗi page tự render `<ToastContainer>`. Lặp code, dễ quên.

**Đích:** Một `ToastProvider` bọc toàn app, mount `<ToastContainer>` **một lần duy nhất**.

```
apps/frontend/src/context/ToastContext.jsx   ← MỚI
apps/frontend/src/components/common/Toast.jsx ← giữ ToastContainer (presentational)
apps/frontend/src/App.jsx                     ← bọc <ToastProvider>
```

- `ToastProvider` giữ state `toasts`, cung cấp qua context: `{ addToast(type, message), success(msg), error(msg), info(msg), removeToast(id) }`.
- `useToast()` **giữ nguyên tên & signature `addToast(type, message)`** để 22 page cũ migrate dễ (chỉ cần: bỏ state cục bộ + bỏ `<ToastContainer>` cục bộ, vẫn gọi `addToast(...)` như cũ).
- Auto-dismiss 3200ms (giữ như hiện tại), tối đa hiển thị đồng thời (đề xuất 4, cái cũ nhất bị đẩy ra).

### 3.2 Chuẩn message hiển thị (FE)

- **Success:** ưu tiên `res.data.message` từ backend; nếu rỗng → dùng message mặc định của flow (vd "Đã lưu thành công").
- **Error:** đọc theo thứ tự `err.response?.data?.message` → `err.message` → fallback `"Có lỗi xảy ra, vui lòng thử lại."`.
- Nên gom helper `getErrorMessage(err)` và `getSuccessMessage(res, fallback)` (đề xuất đặt tại `apps/frontend/src/utils/apiMessage.js`).

### 3.3 Backend message (support toast)

`ApiResponse` đã có field `message`. Cần:
- Mọi endpoint **mutation** (POST/PUT/PATCH/DELETE) trả `message` tiếng Việt có nghĩa qua `ApiResponse.success(message, data)`.
- Việt hoá default trong `ApiResponse`: `"Operation successful"`, `"Created successfully"` và fallback trong `GlobalExceptionHandler` (`"Resource not found"`, generic 500).
- `GlobalExceptionHandler` handler 500 (Exception chung) **không** lộ stacktrace/message kỹ thuật → trả `"Đã xảy ra lỗi hệ thống, vui lòng thử lại sau."`.
- Lỗi `@Valid` (MethodArgumentNotValidException) trả message field đầu tiên hoặc gộp, tiếng Việt.

### 3.4 Emoji → SVG tự chế (custom icon set)

**Nguyên tắc:**
- **KHÔNG dùng `lucide-react`** hay thư viện icon ngoài. Codebase đã có convention icon riêng: component SVG tự chế trong `apps/frontend/src/components/student/StudentIcons.jsx` — mỗi icon là `export function XxxIcon({ size = 24 })` trả về `<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">` dùng `currentColor`, không hard-code màu. Đã có tiền lệ line-icon (`WriteIcon` trong `KanjiPractice.jsx`).
- **Tái sử dụng** icon đã có trong `StudentIcons.jsx` khi trùng ý nghĩa (vd `FlameIcon` cho streak 🔥, `BookIcon`/`PenIcon` nếu có, `MicIcon` cho 🎤/🎙). **Thêm mới** icon còn thiếu theo đúng convention.
- Đề xuất tập trung icon dùng chung vào `apps/frontend/src/components/common/AppIcons.jsx` (icon toàn cục: streak, star, calendar, badge, loa, mic, tìm kiếm…), giữ `StudentIcons.jsx` cho icon riêng student. Miễn cùng chữ ký `{ size }`, `currentColor`, `aria-hidden`.
- Giữ nguyên **toàn bộ chữ/nhãn** (bao gồm chữ "AI"). Chỉ thay glyph emoji → `<Icon/>`.
- Icon mang **ý nghĩa** (không thuần trang trí) khi đứng một mình cần `aria-label` ở phần tử cha hoặc text đi kèm; icon trang trí giữ `aria-hidden`.

**Phân tier (mức ưu tiên):**

- **Tier 1 — BẮT BUỘC** (emoji "sticker" pictographic, lệch tông rõ nhất — gồm đúng ví dụ user đưa):

| File | Emoji | Icon SVG đề xuất |
|:---|:---|:---|
| `components/layout/TopNav.jsx` | `🔥` / `💤` (streak), `⭐` (từ đã học), `📅` (học trong tháng) | `FlameIcon` (tái dùng) / `MoonIcon`, `StarIcon`, `CalendarIcon` |
| `components/layout/AdminTopNav.jsx` | `👑` | `CrownIcon` |
| `components/common/Badges.jsx` | `📚` `🌿` `👑` (cấp độ), `👤` (role) | `BookIcon`, `LeafIcon`, `CrownIcon`, `UserIcon` |
| `components/common/EmptyState.jsx` | `🌸` | `SakuraIcon` |
| `components/notifications/NotificationBell.jsx` | `🌸` | `SakuraIcon` |
| `components/student/CoursePickerBanner.jsx` | `🌸` (trang trí) | `SakuraIcon` |
| `pages/features/Features.jsx` | `🌸📱🔍🎙️📝📖` (6 card, gồm AI·OCR/AI·Speech) | `SakuraIcon`, `PhoneIcon`, `ScanTextIcon`, `MicIcon`, `PenIcon`, `BookIcon` |
| `components/student/QuizCard.jsx` | `📝` | `PenIcon` |
| `components/student/SpeakingCard.jsx` | `🎤` | `MicIcon` |
| `components/student/AudioRecorder.jsx` | `🎙` / `⏹` | `MicIcon` / `StopIcon` |
| `components/student/DictResultGroup.jsx` | `🔊` | `SpeakerIcon` |
| `components/student/NotebookWordCard.jsx` | `🔊` | `SpeakerIcon` |
| `components/admin/UserModals.jsx` | `✅🔑🗑️🔒🌿⭐✏️` (tiêu đề modal) | `CheckIcon`, `KeyIcon`, `TrashIcon`, `LockIcon`, `LeafIcon`, `StarIcon`, `PencilIcon` |
| `components/staff/TicketDetail.jsx` | `🔗` | `LinkIcon` |
| `components/kanji/KanjiWritingCanvas.jsx` | `🎉` `⭐` `↻` | `ConfettiIcon`, `StarIcon`, `RefreshIcon` |

- **Tier 2 — NÊN** (glyph trạng thái/hành động dạng emoji):

| File | Emoji | Icon SVG đề xuất |
|:---|:---|:---|
| `components/common/Toast.jsx` | `✅ ❌ 💬` (icon theo type) | `CheckCircleIcon`, `XCircleIcon`, `InfoIcon` |
| `components/staff/ContentFormModal.jsx` | `➕ ⏳ ✔ ✖` | `PlusIcon`, `SpinnerIcon`, `CheckIcon`, `XIcon` |
| `components/staff/GradingPanel.jsx`, `SubmissionList.jsx`, `AssignQuestionsModal.jsx` | `✅ ✓ ⏱` | `CheckIcon`, `ClockIcon` |
| `components/student/DictDetailPanel.jsx`, `DictResultGroup.jsx` | `♥ ♡` (bookmark), `▶` | `HeartIcon` (filled/outline), `PlayIcon` |
| `components/student/KanaDetailModal.jsx` | `▶ ✓` | `PlayIcon`, `CheckIcon` |

- **Tier 3 — GIỮ NGUYÊN / OPTIONAL** (glyph typographic đã ổn về hiển thị đa nền tảng):
  - Mũi tên `→ ← ↑ ↓ ↗ ↘ ↙ ↖` (điều hướng, hướng nét kanji), chấm `●`, số khoanh tròn `① ② ③ ④ ⑤` (nhãn đáp án), gạch phân cách `─ ═` trong comment.
  - Không bắt buộc đổi; nếu muốn triệt để có thể thay ở đợt sau.

**Chữ ký component mẫu:**
```jsx
export function StarIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 3l2.7 5.5 6 .9-4.3 4.2 1 6-5.4-2.8-5.4 2.8 1-6L3.3 9.4l6-.9L12 3z"
        fill="currentColor" />
    </svg>
  );
}
```

> **Nguồn sự thật (inventory) sống cùng spec này.** Khi implement, rà lại bằng lệnh quét emoji để chắc không sót file mới phát sinh.

---

## 4. TOAST COVERAGE MATRIX (các mục CÒN THIẾU)

> Đây là danh sách bắt buộc phủ toast. Page đã có toast (22) chỉ migrate sang provider, không liệt kê lại.

| # | Page / Component | Thao tác | Success toast | Error toast |
|:--|:---|:---|:---|:---|
| 1 | `login/Login.jsx` | Đăng nhập (email/pw, Google) | (redirect, có thể bỏ success) | ✅ sai mật khẩu/khoá/suspended |
| 2 | `register/Register.jsx` | Đăng ký | ✅ "Đăng ký thành công, kiểm tra email" | ✅ email trùng, pw yếu |
| 3 | `forgot-password/ForgotPassword.jsx` | Gửi link reset | ✅ "Đã gửi email đặt lại mật khẩu" | ✅ |
| 4 | `forgot-password/ResetPassword.jsx` | Đặt lại mật khẩu | ✅ | ✅ token hết hạn |
| 5 | `forgot-password/StaffForgotPassword.jsx` | Yêu cầu reset (staff) | ✅ | ✅ |
| 6 | `forgot-password/StaffSetupPassword.jsx` | Thiết lập mật khẩu | ✅ | ✅ |
| 7 | `forgot-password/StaffChangeTempPassword.jsx` | Đổi mật khẩu tạm | ✅ | ✅ |
| 8 | `verify-email/VerifyEmail.jsx` | Xác minh email (mã OTP) | ✅ | ✅ OTP sai/hết hạn/quá số lần thử |
| 9 | `onboarding/Onboarding.jsx` | Lưu onboarding | ✅ | ✅ |
| 10 | `mock-test/MockTestAttempt.jsx` | Nộp bài thi | ✅ "Đã nộp bài" | ✅ hết giờ/lỗi mạng |
| 11 | `quiz/QuizPage.jsx` | Nộp quiz | ✅ | ✅ |
| 12 | `reading/Reading.jsx` | Nộp bài đọc | ✅ | ✅ |
| 13 | `listening/Listening.jsx` | Nộp bài nghe | ✅ | ✅ |
| 14 | `speaking/SpeakingPage.jsx` | Upload/chấm speaking | ✅ hoàn tất | ✅ thay `setAiError` inline bằng toast (giữ inline nếu cần) |
| 15 | `components/student/KanaResetButton.jsx` | Reset tiến độ Kana | ✅ | ✅ **thay `alert()`** |
| 16 | `components/student/VocabResetButton.jsx` | Reset tiến độ Vocab | ✅ | ✅ **thay `alert()`** |
| 17 | `kanji/KanjiList.jsx` | Reset tiến độ Kanji | ✅ | ✅ **thay `alert()`** |

> **Quy tắc chung:** duyệt lại mọi page còn lại có gọi service mutation nhưng chưa có toast (vd các CRUD ở admin/staff/manager chưa phủ) và bổ sung theo cùng quy tắc §5.

---

## 5. FUNCTIONAL REQUIREMENTS (EARS)

### 5.1 Toast — Frontend

| ID | EARS Requirement |
|:---|:---|
| FR-TOAST-01 | THE SYSTEM SHALL cung cấp một `ToastProvider` toàn cục bọc toàn bộ app tại `App.jsx`, và mount `<ToastContainer>` đúng **một lần**. |
| FR-TOAST-02 | THE SYSTEM SHALL expose hook `useToast()` trả về tối thiểu `addToast(type, message)` với `type ∈ {success, error, info}`. |
| FR-TOAST-03 | WHEN một lời gọi API mutation trả về thành công (2xx), THE SYSTEM SHALL hiển thị toast `success` với nội dung `res.data.message` (fallback message mặc định của flow). |
| FR-TOAST-04 | WHEN một lời gọi API mutation thất bại, THE SYSTEM SHALL hiển thị toast `error` với nội dung `err.response?.data?.message` (fallback `"Có lỗi xảy ra, vui lòng thử lại."`). |
| FR-TOAST-05 | THE SYSTEM SHALL tự động ẩn mỗi toast sau 3200ms và cho phép người dùng đóng thủ công bằng nút ×. |
| FR-TOAST-06 | THE SYSTEM SHALL thay thế toàn bộ `alert()` hiện có (KanaResetButton, VocabResetButton, KanjiList) bằng toast tương ứng. |
| FR-TOAST-07 | THE SYSTEM SHALL phủ toast cho tất cả mục trong Coverage Matrix (§4). |
| FR-TOAST-08 | WHILE nhiều toast cùng hiển thị, THE SYSTEM SHALL xếp chồng (stack) và giới hạn tối đa 4 toast, đẩy cái cũ nhất ra khi vượt. |
| FR-TOAST-09 | IF một flow chỉ điều hướng ngay sau khi thành công (vd Login → Dashboard), THEN success toast là tuỳ chọn, nhưng error toast là **bắt buộc**. |
| FR-TOAST-10 | THE SYSTEM SHALL migrate 22 page đang dùng toast cục bộ sang provider toàn cục mà **không đổi** hành vi hiển thị hiện có. |

### 5.2 Backend message

| ID | EARS Requirement |
|:---|:---|
| FR-MSG-01 | THE SYSTEM SHALL trả `message` tiếng Việt, hướng người dùng, cho mọi endpoint mutation (POST/PUT/PATCH/DELETE) qua `ApiResponse.success(message, data)`. |
| FR-MSG-02 | THE SYSTEM SHALL Việt hoá các message mặc định trong `ApiResponse` (`success`, `created`) và `GlobalExceptionHandler`. |
| FR-MSG-03 | WHEN xảy ra exception không lường trước (500), THE SYSTEM SHALL trả message chung an toàn `"Đã xảy ra lỗi hệ thống, vui lòng thử lại sau."` và **không** lộ stacktrace/chi tiết kỹ thuật ra response. |
| FR-MSG-04 | WHEN request vi phạm `@Valid`, THE SYSTEM SHALL trả HTTP 400 với message tiếng Việt mô tả lỗi field. |
| FR-MSG-05 | THE SYSTEM SHALL giữ nguyên `status` và cấu trúc `{ status, message, data }` hiện có (không breaking change API contract). |

### 5.3 Emoji Icon

| ID | EARS Requirement |
|:---|:---|
| FR-ICON-01 | THE SYSTEM SHALL thay **toàn bộ emoji Tier 1** (§3.4) đang render ra UI bằng component SVG tự chế tương ứng, trên tất cả file trong inventory. |
| FR-ICON-02 | THE SYSTEM SHALL viết mọi icon mới theo đúng convention `StudentIcons.jsx`: SVG `viewBox="0 0 24 24"`, dùng `currentColor`, nhận prop `size`, `aria-hidden="true"`; **KHÔNG** import `lucide-react` hay thư viện icon ngoài. |
| FR-ICON-03 | THE SYSTEM SHALL tái sử dụng icon đã tồn tại trong `StudentIcons.jsx` khi trùng ý nghĩa, thay vì vẽ trùng lặp. |
| FR-ICON-04 | THE SYSTEM SHALL NOT thay đổi bất kỳ chữ/nhãn/badge/tiêu đề văn bản (bao gồm chữ "AI" như "Kết quả đánh giá AI", "Chưa qua AI", "phản hồi từ AI") — chỉ thay glyph emoji. |
| FR-ICON-05 | THE SYSTEM SHALL NOT đổi emoji nằm trong comment/JSDoc; chỉ đổi emoji được render ra UI. |
| FR-ICON-06 | *(Nên)* THE SYSTEM SHOULD thay các emoji **Tier 2** (toast icon, glyph trạng thái/hành động) bằng icon SVG tương ứng. |
| FR-ICON-07 | *(Optional)* THE SYSTEM MAY giữ nguyên **Tier 3** (mũi tên, số khoanh tròn, chấm) — không bắt buộc đổi. |

---

## 6. NON-FUNCTIONAL / RÀNG BUỘC

- **Accessibility:** giữ `role="alert"` + `aria-live="polite"` của ToastContainer; icon lucide phải có `aria-hidden` khi chỉ trang trí.
- **Không regression:** 22 page đang có toast phải hoạt động y như cũ sau migrate.
- **Bundle:** icon là SVG tự chế inline (không thêm dependency), chi phí bundle ≈ 0. Giữ `lucide-react` trong `package.json` như hiện tại nhưng **không dùng** (có thể gỡ ở dọn dẹp riêng nếu muốn — ngoài scope).
- **Style:** toast dùng lại `Toast.css` hiện có; không đổi màu/animation.
- **Lint/format:** chạy `npm run lint` + `npm run format` (FE) và spotless (BE) trước khi commit.

---

## 7. ACCEPTANCE CRITERIA (checklist nghiệm thu)

- [ ] `ToastProvider` bọc app; không còn `<ToastContainer>` render cục bộ trùng lặp.
- [ ] Tất cả 17 mục trong Coverage Matrix hiển thị đúng toast success/error khi thao tác.
- [ ] Không còn `alert()` nào trong `apps/frontend/src` (trừ chỗ có lý do rõ ràng).
- [ ] Login sai mật khẩu → toast error đọc đúng message backend (vd "Sai mật khẩu").
- [ ] Nộp bài thi/quiz thành công → toast success; mất mạng → toast error.
- [ ] Endpoint 500 giả lập → FE hiện "Đã xảy ra lỗi hệ thống…", không lộ stacktrace.
- [ ] Không còn emoji Tier 1/2 render ra UI (quét lại bằng lệnh emoji không còn hit ở các file inventory); mọi icon là SVG tự chế.
- [ ] Stat tile ở TopNav (streak/từ đã học/học trong tháng), badge cấp độ, feature card, nút ghi âm/loa… đều dùng icon SVG đồng bộ.
- [ ] Không import `lucide-react` ở bất kỳ đâu; chữ/nhãn (gồm "AI") giữ nguyên.
- [ ] `npm run build` (FE) + `mvn clean install` (BE) đều xanh.
- [ ] 22 page cũ không đổi hành vi (spot-check vài page).

---

## 8. IMPLEMENTATION NOTES (gợi ý triển khai)

**Thứ tự đề xuất:**
1. **BE trước:** audit message endpoint mutation + Việt hoá `ApiResponse`/`GlobalExceptionHandler` (FR-MSG-*). Nhỏ, độc lập, làm nền cho FE.
2. **Provider:** tạo `ToastContext.jsx`, bọc `App.jsx`, migrate 22 page (FR-TOAST-01/02/10).
3. **Helper:** `utils/apiMessage.js` (getErrorMessage/getSuccessMessage).
4. **Phủ toast** theo Coverage Matrix + thay `alert()` (FR-TOAST-03..09).
5. **Icon:** tạo `AppIcons.jsx`, thay emoji Tier 1 → Tier 2 theo inventory §3.4 (FR-ICON-*). Quét lại emoji cuối cùng để chắc không sót.

**File dự kiến chạm (FE):**
- MỚI: `context/ToastContext.jsx`, `utils/apiMessage.js`, `components/common/AppIcons.jsx` (bộ icon SVG dùng chung)
- SỬA (toast): `App.jsx`, `components/common/Toast.jsx`, 17 file trong Matrix, 22 page migrate.
- SỬA (icon Tier 1/2): tất cả file trong inventory §3.4 — TopNav, AdminTopNav, Badges, EmptyState, NotificationBell, CoursePickerBanner, Features, QuizCard, SpeakingCard, AudioRecorder, DictResultGroup, NotebookWordCard, UserModals, TicketDetail, KanjiWritingCanvas, ContentFormModal, GradingPanel, SubmissionList, AssignQuestionsModal, DictDetailPanel, KanaDetailModal, Toast (icon).

**File dự kiến chạm (BE):**
- `shared/common/ApiResponse.java`
- `shared/exception/GlobalExceptionHandler*.java` (các handler)
- Controller mutation trả message chưa có nghĩa (rà theo từng feature)

---

## 9. OPEN DECISIONS (cần chốt khi implement)

| # | Vấn đề | Đề xuất mặc định |
|:--|:---|:---|
| D1 | Migrate 22 page cũ sang provider **ngay** hay để coexist tạm? | Migrate ngay (dedup), vì signature `addToast` giữ nguyên nên rẻ. |
| D2 | Login có cần success toast không? | Không — điều hướng ngay; chỉ error toast. |
| D3 | Có làm Tier 2 (glyph trạng thái) trong đợt này không? | Có — làm Tier 1 + Tier 2; Tier 3 để sau. |
| D4 | Toast icon ✅❌💬 — đổi sang SVG? | Có (Tier 2, FR-ICON-06). |
| D5 | Đặt icon dùng chung ở đâu? | File mới `components/common/AppIcons.jsx`; tái dùng `StudentIcons.jsx` khi trùng. |
| D6 | Tier 3 (mũi tên, số khoanh tròn ①-⑤) có đổi? | Không trong đợt này (hiển thị đa nền tảng đã ổn). |
