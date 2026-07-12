# Danh Mục Lỗi & Thông Báo Validation (Validation Error Catalog)

> **Mục đích**: Liệt kê toàn bộ luật validation hiện có, thông báo lỗi, điều kiện kích hoạt và thuộc chức năng nào.
> **Phạm vi quét**: Backend (DTO `@Valid` + `GlobalExceptionHandler`) và Frontend (`utils/validation.js` + validate inline trong form).
> **Ngày quét**: 2026-07-12 — bản đồ theo mã nguồn hiện tại, không phải spec mong muốn.
> **Xem thêm**: `SPEC_VALIDATION_COVERAGE.md` (checklist coverage 3 lớp).

---

## 1. Cơ chế & Định dạng lỗi (cách lỗi được trả về)

### 1.1 Backend — Global Exception Handler
File: `shared/exception/GlobalExceptionHandler.java`. Mọi lỗi trả về dạng `ApiResponse`:
```json
{ "status": <int>, "message": "<thông báo>", "code": "<mã máy-đọc, tùy chọn>", "data": "<chi tiết field, tùy chọn>" }
```

| Loại lỗi (Exception) | HTTP | `message` trả về | `data` |
|---|---|---|---|
| `MethodArgumentNotValidException` (`@Valid` body fail) | 400 | `Dữ liệu không hợp lệ.` | Chuỗi `field: message, field2: message2` gom từ các annotation |
| `ConstraintViolationException` (`@Validated` param/path fail) | 400 | `Dữ liệu không hợp lệ.` | `field: message` |
| `IllegalArgumentException` | 400 | `Dữ liệu không hợp lệ: <ex.message>` | — |
| `BadRequestException` | 400 | `<ex.message>` | — |
| `BusinessException` | tùy | `<ex.message>` (kèm `code`) | — |
| `BusinessRuleException` | 422 | `<ex.message>` | — |
| `DuplicateResourceException` | 409 | `<ex.message>` | — |
| `ForbiddenException` / `AccessDeniedException` | 403 | `<ex.message>` / `Bạn không có quyền thực hiện thao tác này.` | — |
| `ResourceNotFoundException` | 404 | `<ex.message>` | — |
| `NoResourceFoundException` | 404 | `Không tìm thấy tài nguyên yêu cầu.` | — |
| `ObjectOptimisticLockingFailureException` | 409 | `Dữ liệu đã bị thay đổi hoặc không còn tồn tại do thao tác đồng thời.` | — |
| `Exception` (fallback) | 500 | `Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.` | — |

> ⚠️ Lỗi validation từ `@Valid` KHÔNG trả từng message riêng theo field ra `message`; chúng bị gom vào `data` dạng chuỗi nối bằng dấu phẩy, còn `message` luôn là `Dữ liệu không hợp lệ.`

### 1.2 Frontend — nguồn thông báo
- **`utils/validation.js`**: hàm thuần trả chuỗi tiếng Việt (rỗng = hợp lệ). Dùng để hiện lỗi ngay tại ô nhập (field-level), không cần round-trip.
- **Validate inline trong form**: một số form tự viết message (Register, Login, NotificationTab).
- **Toast**: thông báo tổng khi submit (ví dụ `Vui lòng sửa các trường được đánh dấu.`).
- Quy tắc mật khẩu FE **khớp** regex BE: `^(?=.*[A-Z])(?=.*\d).{8,}$`.

---

## 2. AUTH & USER — Đăng ký / Đăng nhập / Mật khẩu

### 2.1 Đăng ký học viên — `RegisterRequest` + `Register.jsx`
| Field | Luật | Thông báo (BE) | Thông báo (FE) | Khi nào |
|---|---|---|---|---|
| fullName | `@NotBlank` | `Họ tên là bắt buộc` | `Họ tên là bắt buộc` | Bỏ trống |
| fullName | `@Size(min=2)` | `Họ tên phải có ít nhất 2 ký tự` | `Họ tên phải có ít nhất 2 ký tự` | < 2 ký tự |
| email | `@NotBlank` / `@Email` | `Email là bắt buộc` / `Email không hợp lệ` | `Email là bắt buộc` / `Email không hợp lệ` | Trống / sai định dạng |
| password | `@NotBlank` / `@Pattern` | `Mật khẩu là bắt buộc` / `Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa và 1 số` | `Mật khẩu là bắt buộc` / `…ít nhất 8 ký tự` / `…1 chữ hoa` / `…1 chữ số` (FE tách chi tiết hơn) | Trống / không đạt độ mạnh |
| confirmPassword | `@NotBlank` (BE) + so khớp (FE) | `Xác nhận mật khẩu là bắt buộc` | `Vui lòng xác nhận mật khẩu` / `Mật khẩu xác nhận không khớp` | Trống / không khớp password |

> Lưu ý: **so khớp confirm ≠ password** chỉ được check ở FE (`confirmError`). BE chỉ kiểm tra `@NotBlank` cho confirmPassword — logic khớp nằm ở service.

### 2.2 Đăng nhập — `LoginRequest` + `Login.jsx`
| Field | Luật | Thông báo BE | Thông báo FE | Khi nào |
|---|---|---|---|---|
| email | `@NotBlank` / `@Email` | `Email là bắt buộc` / `Email không hợp lệ` | `Email là bắt buộc` / `Email không hợp lệ` | Trống / sai định dạng |
| password | `@NotBlank` | `Mật khẩu là bắt buộc` | `Mật khẩu là bắt buộc` | Bỏ trống |

### 2.3 Quên / Đặt lại mật khẩu (Học viên)
- **`ForgotPasswordRequest`** (`ForgotPassword.jsx`): email — `Email là bắt buộc` / `Email không hợp lệ`.
- **`ResetPasswordRequest`** (`ResetPassword.jsx`):
  - token: `Mã đặt lại mật khẩu là bắt buộc` (trống)
  - newPassword: `Mật khẩu mới là bắt buộc` / `Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa và 1 số`
  - confirmPassword: `Xác nhận mật khẩu là bắt buộc` (BE) + `Mật khẩu xác nhận không khớp` (FE)

### 2.4 Đổi mật khẩu (đã đăng nhập) — `ChangePasswordRequest`
| Field | Luật | Thông báo | Khi nào |
|---|---|---|---|
| currentPassword | `@NotBlank` | `Mật khẩu hiện tại là bắt buộc` | Trống |
| newPassword | `@NotBlank` / `@Pattern` | `Mật khẩu mới là bắt buộc` / `Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa và 1 số` | Trống / yếu |
| confirmPassword | `@NotBlank` | `Xác nhận mật khẩu là bắt buộc` | Trống |

### 2.5 Xác minh email / Gửi lại / Token
| DTO | Field | Thông báo | Khi nào |
|---|---|---|---|
| `VerifyEmailRequest` | token | `Token là bắt buộc` | Trống |
| `ResendVerificationRequest` | email | `Email là bắt buộc` / `Email không hợp lệ` | Trống / sai |
| `RefreshTokenRequest` | refreshToken | `Refresh token là bắt buộc` | Trống |
| `LogoutRequest` | refreshToken | `Refresh token là bắt buộc` | Trống |
| `CheckAccountTypeRequest` | email | `Email là bắt buộc` / `Email không hợp lệ` | Trống / sai |
| `GoogleTokenRequest` | idToken | `ID Token không được để trống` | Trống (đăng nhập Google) |
| `IssueTempPasswordRequest` | requestId | `Mã yêu cầu là bắt buộc` | Null (admin cấp mật khẩu tạm) |

### 2.6 Mật khẩu tạm / Setup (Auth namespace)
| DTO | Field | Thông báo | Khi nào |
|---|---|---|---|
| `ChangeTempPasswordRequest` (auth) | newPassword | `Mật khẩu mới là bắt buộc` / `Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa và 1 số` | Trống / không đạt độ mạnh (đã đồng bộ với các DTO mật khẩu khác) |
| | confirmPassword | `Xác nhận mật khẩu là bắt buộc` | Trống |
| `StaffForgotPasswordRequest` (auth) | email | `Email là bắt buộc` / `Email không hợp lệ` | Trống / sai |
| `StaffSetupPasswordRequest` (auth) | token / newPassword / confirmPassword | `Token là bắt buộc` / `Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa và 1 số` / `Xác nhận mật khẩu là bắt buộc` | Trống / yếu |

---

## 3. STAFF / MANAGER — Quản lý nhân viên

### 3.1 Tạo Staff — `CreateStaffRequest`
| Field | Luật | Thông báo | Khi nào |
|---|---|---|---|
| fullName | `@NotBlank` + `@Size(2..150)` | `Họ tên là bắt buộc và không vượt quá 150 ký tự` | Trống / < 2 / > 150 |
| email | `@NotBlank` + `@Email` + `@Size(max=255)` | `Email không hợp lệ` | Trống / sai / > 255 |
| staffRole | `@NotBlank` + `@Pattern(staff\|staff_manager)` | `Vai trò Staff không hợp lệ` | Trống / ngoài 2 giá trị |

### 3.2 Cập nhật / Đổi vai trò / Mật khẩu tạm (Staff namespace)
| DTO | Field | Thông báo | Khi nào |
|---|---|---|---|
| `UpdateStaffInfoRequest` | fullName | `Họ tên không hợp lệ` | < 2 hoặc > 150 ký tự |
| `ChangeStaffRoleRequest` | staffRole | `Vai trò Staff không hợp lệ` | Trống / ngoài `staff\|staff_manager` |
| `ChangeTempPasswordRequest` (staff) | newPassword | `Mật khẩu mới là bắt buộc` / `Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa và 1 số` | Trống / yếu |
| | confirmPassword | `Xác nhận mật khẩu là bắt buộc` | Trống |
| `StaffForgotPasswordRequest` (staff) | email | `Email là bắt buộc` / `Email không hợp lệ` | Trống / sai |
| `StaffSetupPasswordRequest` (staff) | token/newPassword/confirmPassword | như mục 2.6 | — |

> ℹ️ Vẫn tồn tại **2 cặp** DTO trùng tên (`ChangeTempPasswordRequest` / `StaffForgot` / `StaffSetup`) ở namespace `auth` và `staff`. Sau khi Việt hoá, thông báo đã **thống nhất** giữa 2 bản; nhưng nên gộp về 1 nơi để tránh trùng lặp khi bảo trì.

---

## 4. STUDENT — Hồ sơ & Onboarding

| DTO / Chức năng | Field | Luật | Thông báo | Khi nào |
|---|---|---|---|---|
| `OnboardingRequest` (onboarding) | jlptGoal | `@NotBlank` + `@Pattern(N5..N1)` | `Mục tiêu JLPT không được để trống` / `Cấp độ JLPT không hợp lệ` | Trống / ngoài N5–N1 |
| `UpdateProfileRequest` (sửa hồ sơ) | fullName | `@Size(max=150)` | `Họ tên không được vượt quá 150 ký tự` | > 150 ký tự |
| | phone | `@Size(max=20)` + `@Pattern` | `Số điện thoại không hợp lệ` | > 20 / ký tự lạ |
| | targetJlptLevel | `@Pattern(N5..N1)` | `Cấp độ JLPT không hợp lệ` | Ngoài N5–N1 |
| | avatarUrl | `@Size(max=500)` | `Avatar URL quá dài` | > 500 ký tự |
| `UpdateStudentRequest` (admin sửa học viên) | fullName | `@Size(2..150)` | `Họ tên không hợp lệ` | < 2 / > 150 |
| | phone | `@Size(max=20)` + `@Pattern` | `Số điện thoại không hợp lệ` | > 20 / ký tự lạ |
| | targetJlptLevel | `@Pattern(N5..N1)` | `Cấp độ JLPT không hợp lệ` | Ngoài N5–N1 |

---

## 5. ASSESSMENT — Quiz / Câu hỏi / Nộp bài

### 5.1 Tạo Quiz — `QuizRequest`
| Field | Luật | Thông báo | Khi nào |
|---|---|---|---|
| title | `@NotBlank` | `Tiêu đề là bắt buộc` | Trống |
| durationMin | `@NotNull` | `Thời lượng là bắt buộc` | Null |
| passScore | `@NotNull` | `Điểm đạt là bắt buộc` | Null |
| lessonId/topic/jlptLevel | (không ràng buộc) | — | — |

### 5.2 Tạo Câu hỏi — `QuestionRequest`
| Field | Luật | Thông báo | Khi nào |
|---|---|---|---|
| questionText | `@NotBlank` | `Nội dung câu hỏi là bắt buộc` | Trống |
| questionType | `@NotBlank` | `Loại câu hỏi là bắt buộc` | Trống |
| skill | `@NotBlank` | `Kỹ năng là bắt buộc` | Trống |
| jlptLevel | `@NotBlank` | `Cấp độ JLPT là bắt buộc` | Trống |
| score | `@NotNull` | `Điểm là bắt buộc` | Null |

> ℹ️ `questionType`, `skill`, `jlptLevel` chỉ `@NotBlank` ở DTO (không `@Pattern`) — **có chủ đích**: `QuizService` tự chuẩn hoá `.toUpperCase()`/`.replace("-","_")` rồi ném `BusinessRuleException` (422) `... không hợp lệ: <giá trị>` nếu ngoài enum. Chấp nhận input không phân biệt hoa/thường; thêm `@Pattern` chặt sẽ chặn nhầm giá trị hợp lệ.

### 5.3 Nộp bài — `SubmitExamRequest` + `AnswerRequest`
| DTO | Field | Luật | Thông báo | Khi nào |
|---|---|---|---|---|
| `SubmitExamRequest` | attemptId | `@NotNull` + `@Positive` | `attemptId không hợp lệ` | Null / ≤ 0 |
| | answers | `@NotEmpty` + `@Valid` | `Danh sách đáp án không được rỗng` | List rỗng |
| `AnswerRequest` | questionId | `@NotNull` + `@Positive` | `questionId không hợp lệ` | Null / ≤ 0 |
| | selectedOption | `@Pattern([ABCD])` | `selectedOption phải là A, B, C hoặc D` | Ngoài A/B/C/D |
| | answerText | `@Size(max=1000)` | `Câu trả lời quá dài` | > 1000 ký tự |

---

## 6. ADMIN — Cài đặt hệ thống & Đình chỉ

### 6.1 Đình chỉ người dùng — `SuspendUserRequest`
| Field | Luật | Thông báo | Khi nào |
|---|---|---|---|
| reason | `@NotBlank` + `@Size(10..500)` | `Lý do đình chỉ phải từ 10 đến 500 ký tự` | Trống / < 10 / > 500 |

### 6.2 Cập nhật cài đặt — `UpdateSettingRequest` / `UpdateSettingsBatchRequest`
| DTO | Field | Luật | Thông báo | Khi nào |
|---|---|---|---|---|
| `UpdateSettingRequest` | settingValue | `@NotNull` + `@Size(max=20000)` | `settingValue không được null` / `settingValue không vượt quá 20000 ký tự` | Null / quá dài |
| `UpdateSettingsBatchRequest` | settings | `@NotEmpty` + `@Valid` | `Danh sách cài đặt không được rỗng` | List rỗng |
| `.Item` | settingKey | `@NotBlank` | `settingKey không được rỗng` | Trống |
| `.Item` | settingValue | `@NotNull` + `@Size(max=20000)` | `settingValue không được null` / `settingValue không vượt quá 20000 ký tự` | Null / quá dài |

### 6.3 Gửi thông báo — `SendNotificationRequest`
| Field | Luật | Thông báo | Khi nào |
|---|---|---|---|
| title | `@NotBlank` + `@Size(max=255)` | `Tiêu đề không được để trống` / `Tiêu đề không vượt quá 255 ký tự` | Trống / > 255 |
| content | `@NotBlank` | `Nội dung không được để trống` | Trống |
| notificationType | `@Pattern(news\|warning\|promotion\|system\|achievement\|reminder)` | `Loại thông báo không hợp lệ` | Ngoài danh sách |
| channel | `@Pattern(in_app\|email\|both)` | `Kênh gửi không hợp lệ` | Ngoài danh sách |
| targetJlptLevel | `@Pattern(N1..N5\|ALL)` | `Cấp độ JLPT mục tiêu không hợp lệ` | Ngoài danh sách |

---

## 7. FRONTEND — Validate riêng theo màn hình

### 7.1 `utils/validation.js` (helper dùng chung)
| Hàm | Thông báo trả về | Điều kiện |
|---|---|---|
| `emailError` | `Email là bắt buộc` / `Email không hợp lệ` | Trống / sai regex |
| `passwordError` | `Mật khẩu là bắt buộc` / `…ít nhất 8 ký tự` / `…cần ít nhất 1 chữ hoa` / `…cần ít nhất 1 chữ số` | Theo từng tầng |
| `confirmError` | `Vui lòng xác nhận mật khẩu` / `Mật khẩu xác nhận không khớp` | Trống / lệch |
| `requiredError` | `<label> là bắt buộc` | Trống |
| `portError` | `Cổng là bắt buộc` / `Cổng phải là số nguyên` / `Cổng phải trong khoảng 1–65535` | Trống / không phải số / ngoài 1–65535 |

### 7.2 Admin Settings — `EmailTab.jsx` (cấu hình SMTP + email type)
- Toast tổng khi submit lỗi: `Vui lòng sửa các trường được đánh dấu.`
- Toast lỗi test SMTP: `Lỗi kết nối SMTP: <message BE hoặc "Vui lòng kiểm tra lại cấu hình (hoặc Mật khẩu ứng dụng)">`
- Field-level qua `smtpFieldError` / `emailTypeFieldError`: SMTP Host (`requiredError`), Port (`portError`), Email (`emailError`), Tên hiển thị / Tiêu đề email (`requiredError`).

### 7.3 Admin Settings — `NotificationTab.jsx` (quy tắc thông báo)
| Field | Thông báo | Khi nào |
|---|---|---|
| ruleKey | `Rule key không hợp lệ (chữ thường/số/gạch dưới, bắt đầu bằng chữ, 3–50 ký tự).` | Chỉ khi tạo mới, sai regex `RULE_KEY_RE` |
| description | `Mô tả là bắt buộc.` | Trống |

---

## 8. Ghi chú & Điểm đã xử lý

### 8.1 Đã vá (2026-07-12) ✅
1. **Việt hoá message**: Toàn bộ DTO auth/assessment/notification còn tiếng Anh (`Token is required`, `Title is required`…) hoặc tiếng Việt không dấu (`Tieu de khong duoc de trong`) đã đổi sang tiếng Việt có dấu, đồng bộ giọng văn.
2. **Đồng bộ luật mật khẩu**: `ChangeTempPasswordRequest` (auth) trước chỉ `@Size(min=8)` (thiếu chữ hoa + số) — nay dùng cùng `@Pattern` `^(?=.*[A-Z])(?=.*\d).{8,}$` như mọi DTO mật khẩu khác.

### 8.2 Vẫn còn / lưu ý khi bảo trì
1. **Confirm password**: BE chỉ `@NotBlank`; việc so khớp `newPassword == confirmPassword` nằm ở service/FE, không phải annotation.
2. **Enum ở DTO chỉ `@NotBlank`** (`QuestionRequest`, `QuizRequest`): **giữ nguyên có chủ đích** — service chuẩn hoá case + trả `BusinessRuleException` (422) rõ ràng; siết `@Pattern` sẽ chặn nhầm input hợp lệ (xem §5.2).
3. **`message` gộp**: Client không nên dò chuỗi `message` khi validate fail (luôn `Dữ liệu không hợp lệ.`); chi tiết field nằm trong `data`.
4. **DTO trùng tên** giữa namespace `auth` và `staff` (`StaffForgotPasswordRequest`, `StaffSetupPasswordRequest`, `ChangeTempPasswordRequest`): message nay đã thống nhất, nhưng nên gộp về 1 nơi để giảm trùng lặp.
