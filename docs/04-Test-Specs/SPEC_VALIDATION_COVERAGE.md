# SPEC — Kiểm Tra & Đối Chiếu Validation Toàn Hệ Thống (Defense-in-Depth)

## JLPT E-Learning Platform

> **Mục đích**: Checklist chuẩn để **rà soát mọi feature** xem validation đã **bao hàm đủ ở CẢ 2 lớp (Frontend + Backend)** hay chưa, tìm chỗ hở.
> **Loại**: Spec kiểm chứng (verification spec) — không phải test spec chạy tự động.
> **Cập nhật**: 2026-07-02
> **Nguyên tắc gốc** (xem `CLAUDE.md § React Anti-Patterns`): *Client-trusted Data* — KHÔNG tin giá trị client gửi lên; *Authorization by UI hide* — ẩn UI chỉ là UX. **Backend luôn phải validate lại toàn bộ.**

---

## 1. Mô hình 3 lớp validation (bắt buộc cho mọi feature ghi dữ liệu)

| Lớp | Ở đâu | Trách nhiệm | Vai trò |
|-----|-------|-------------|---------|
| **L1 — FE client-side** | Component/form React | Bắt lỗi rỗng/sai định dạng **trước khi** gọi API; hiện lỗi ngay tại field | **UX** (không phải bảo mật) — giảm round-trip |
| **L2 — BE bean-validation** | DTO `*Request` + `@Valid` tại Controller | `@NotBlank/@Size/@Email/@Pattern/@Min/@Max...` → 400 qua `GlobalExceptionHandler` | **Chặn dữ liệu dị dạng** |
| **L3 — BE business-rule** | Service layer | Khớp mật khẩu, quyền theo role+subscription, khoá câu hỏi khi có attempt, tính điểm server-side, trùng lặp, tồn tại FK, soft-delete | **Chân lý nghiệp vụ** — không được bỏ qua |

> **Quy tắc vàng**: Một feature được coi là **"đã bao hàm"** khi **L2 + L3 đủ** (bắt buộc) và **L1 có** cho mọi form người dùng nhập tay (khuyến nghị mạnh). L1 thiếu = lỗi UX; **L2 hoặc L3 thiếu = lỗ bảo mật/nghiệp vụ** (chặn merge).

### Error format chuẩn (đã có sẵn — `GlobalExceptionHandler`)

- `MethodArgumentNotValidException` (L2) → `400 { status:400, message:"Dữ liệu không hợp lệ.", data:"<field>: <msg>, ..." }`
- `BusinessException` (L3) → status tuỳ lỗi, ví dụ `400 PASSWORD_MISMATCH`, `409` duplicate, `422` business-rule
- FE phải hiển thị `data`/`message` này (toast hoặc banner) — xem `[[project_spec_toast_icon_cleanup]]`.

---

## 2. Định nghĩa "ĐÃ BAO HÀM ĐỦ" — Checklist cho từng endpoint ghi

Mỗi endpoint `POST/PUT/PATCH` phải tick đủ:

- [ ] **L2.1** DTO `*Request` tồn tại (KHÔNG nhận Entity/Map/param rời cho payload phức tạp)
- [ ] **L2.2** Mọi field bắt buộc có `@NotNull/@NotBlank/@NotEmpty`
- [ ] **L2.3** Field có giới hạn: `@Size` (chuỗi), `@Min/@Max/@Positive` (số), `@Email`, `@Pattern` (định dạng)
- [ ] **L2.4** Controller có `@Valid` trước `@RequestBody` (nếu không → L2 vô hiệu hoàn toàn)
- [ ] **L2.5** Collection lồng nhau dùng `@Valid` trên field list để cascade
- [ ] **L3.1** Cross-field (mật khẩu khớp, from ≤ to, tổng %...) check ở Service
- [ ] **L3.2** Authorization = **Role + Subscription/Level** (`[[LESSON-003]]`), không chỉ role
- [ ] **L3.3** Giá trị nhạy cảm (điểm, thời gian, trạng thái) **tính lại server-side**, không tin client
- [ ] **L3.4** Tồn tại FK + chưa soft-delete trước khi thao tác
- [ ] **L1.1** Form FE chặn submit khi field rỗng/sai định dạng + hiện lỗi tại field
- [ ] **L1.2** Nút submit `disabled` khi đang gửi (chống double-submit)

---

## 3. Ma trận coverage theo module

**Chú thích**: ✅ có & đủ · ⚠️ có nhưng chưa đủ · ❌ thiếu (lỗ) · — không áp dụng · ❓ chưa kiểm từng dòng (cần rà thủ công theo checklist §2)

### 3.1 Auth — Student (`/api/auth/**`)

| Endpoint | DTO | L1 FE | L2 @Valid | L2 rules | L3 business | Ghi chú |
|----------|-----|:----:|:---------:|:--------:|:-----------:|--------|
| register | `RegisterRequest` | ⚠️ | ✅ | ✅ NotBlank/Email/Pattern/Size | ✅ khớp pwd (AuthService:272) | **L1 gap**: `Register.jsx` dùng `noValidate`, không chặn submit khi lệch pwd (chỉ hiện icon) |
| login | `LoginRequest` | ✅ | ✅ | ✅ | ✅ lock/rate-limit | OK |
| verify-email | `VerifyEmailRequest` | — | ✅ | ✅ token NotBlank | ✅ | OK |
| resend-verification | `ResendVerificationRequest` | ✅ | ✅ | ✅ Email | ✅ | OK |
| forgot-password | `ForgotPasswordRequest` | ✅ | ✅ | ✅ Email | ✅ | OK |
| reset-password | `ResetPasswordRequest` | ✅ | ✅ | ✅ Pattern pwd | ✅ khớp pwd (AuthService:543) | OK |
| change-password | `ChangePasswordRequest` | ✅ (`ChangePassword.jsx` có `errors`) | ✅ | ✅ Pattern | ✅ khớp + verify current | OK |
| refresh / logout | `RefreshTokenRequest`/`LogoutRequest` | — | ✅ | ✅ NotBlank | ✅ | OK |
| google | `GoogleTokenRequest` | — | ✅ | ✅ NotBlank | ✅ verify token | OK |
| update-profile | `student/UpdateProfileRequest` | ❓ | ✅ | ✅ 3 rule | ✅ | Luồng thật dùng bản `student/*` (đã validate). Bản `auth/UpdateProfileRequest` là **dead code → ĐÃ XOÁ** (2026-07-02) |

### 3.2 Auth — Staff (`/api/staff/auth/**`)

| Endpoint | DTO | L2 @Valid | L2 rules | L3 | Ghi chú |
|----------|-----|:---------:|:--------:|:--:|--------|
| login (2FA/TOTP) | `LoginRequest` | ✅ | ✅ | ✅ TOTP (`[[project_uc35_admin_login]]`) | OK |
| setup-password | `StaffSetupPasswordRequest` | ✅ | ✅ Pattern×2 | ✅ khớp | OK, FE `StaffSetupPassword.jsx` có `noValidate` → rà L1 |
| forgot-password | `StaffForgotPasswordRequest` | ✅ | ✅ Email | ✅ | OK |
| change-temp-password | `ChangeTempPasswordRequest` | ✅ | ✅ Pattern | ✅ khớp (AuthService:642) | OK |

### 3.3 Staff — Quản lý nhân sự (`StaffMemberController`, `staff/*`)

| Endpoint | DTO | L2 @Valid | L2 rules | Ghi chú |
|----------|-----|:---------:|:--------:|--------|
| create-staff | `CreateStaffRequest` | ✅ | ✅ (7 rule) | authz staff_manager `[[project_staff_manager_authz_guard]]` |
| update-staff-info | `UpdateStaffInfoRequest` | ✅ | ⚠️ 1 rule | rà đủ field bắt buộc chưa |
| change-staff-role | `ChangeStaffRoleRequest` | ✅ | ✅ | OK |
| password-reset | — | — | — | **KHÔNG áp dụng**: `StaffPasswordResetRequest` là **JPA `@Entity`** (bảng `staff_password_reset_requests`), không phải request DTO — lọt lưới do trùng tên `*Request.java` |

### 3.4 Staff Content — Authoring (kanji/vocab/lesson/grammar/quiz/exam/question)

| Endpoint | DTO | L2 @Valid | L2 rules | L3 | Ghi chú |
|----------|-----|:---------:|:--------:|:--:|--------|
| create/update Kanji | `Create/UpdateKanjiRequest` | ✅ | ✅5 / ⚠️1 | ✅ | `UpdateKanjiRequest` chỉ 1 rule — rà |
| create/update Lesson | `Create/UpdateLessonRequest` | ✅ | ✅6 | ✅ order_index | OK |
| create/update Vocabulary | `Create/UpdateVocabularyRequest` | ✅ | ✅6 / ⚠️1 | ✅ topicId FK `[[project_vocab_topics_endpoint]]` | `UpdateVocabularyRequest` 1 rule — rà |
| create VocabTopic | `CreateVocabTopicRequest` | ✅ | ✅7 | ✅ trùng topic | OK |
| create/update Grammar | `Create/UpdateGrammarRequest` | ✅ | ✅6 / ⚠️1 | ✅ | `UpdateGrammarRequest` 1 rule — rà |
| create/update Quiz | `Create/UpdateQuizRequest` | ✅ | ✅7 | ✅ lock khi có attempt `[[LESSON-005]]` | OK |
| assign questions (quiz) | `AssignQuestionsRequest` | ✅ | ✅6 | ✅ | OK |
| create/update Exam | `Create/UpdateExamRequest` | ✅ | ✅7 | ✅ | OK |
| assign questions (exam) | `ExamAssignQuestionsRequest` | ✅ | ✅7 | ✅ | OK |
| create/update Question | `Create/UpdateQuestionRequest` | ✅ | ✅8/4 | ✅ đáp án đúng tồn tại | **rà L2.5**: list `answers` có `@Valid` cascade? |
| submit-review (quiz) | `QuizSubmitReviewRequest` (inner) | ✅ | ✅ `@NotNull`×2 | ✅ | **ĐÃ VÁ** (2026-07-02): thêm `@Valid` vào controller (DTO vốn đã có `@NotNull`) |
| submit-review (grammar) | không body | — | — | ✅ | `@RequestParam` — OK |

### 3.5 Content Review / Published (Manager)

| Endpoint | DTO | L2 @Valid | L2 rules | Ghi chú |
|----------|-----|:---------:|:--------:|--------|
| review-action | `ReviewActionRequest` | ✅ | ✅3 | OK |
| request-changes | `RequestChangesRequest` | ✅ | ✅3 | OK |
| change-status | `ChangeStatusRequest` | ✅ | ✅4 | `[[project_contentstatus_enum_lowercase]]` — dùng converter |
| restore | `RestoreContentRequest` | ✅ | ✅1 | OK |

### 3.6 Assessment — Student làm bài (`/api/**` submit)

| Endpoint | DTO | L2 @Valid | L2 rules | L3 (quan trọng) | Ghi chú |
|----------|-----|:---------:|:--------:|:---------------:|--------|
| submit quiz/exam | `SubmitExamRequest` + `AnswerRequest` | ✅ | ✅3/3 | ✅ **điểm tính server-side, thời gian server-side** `[[LESSON-005]]` | **rà L2.5**: `List<AnswerRequest>` có `@Valid`? |
| create quiz/question (assessment tree) | `QuizRequest`/`QuestionRequest` | ✅ | ✅3/5 | ⚠️ trùng cây staffcontent `[[project_quiz_exam_duplication]]` | DEFER gộp |

### 3.7 Flashcard / SRS (`StudentFlashcardController`)

| Endpoint | DTO | L2 @Valid | L2 rules | L3 | Ghi chú |
|----------|-----|:---------:|:--------:|:--:|--------|
| add flashcard | `AddFlashcardRequest` | ✅ | ✅3 | ✅ | OK |
| create deck | `DeckCreateRequest` | ✅ | ⚠️1 | ✅ | rà field bắt buộc |
| review (SM-2) | `ReviewRequest` | ✅ | ⚠️1 | ✅ SM-2 server-side `[[project_nguoi4_flashcard_spec]]` | rating cần `@Min/@Max` |
| review-deck add | `ReviewDeckAddRequest` | ✅ | ✅3 | ✅ | OK |
| bulk-delete | `BulkDeleteRequest` | ✅ | ⚠️1 | ✅ | list ids cần `@NotEmpty` |

### 3.8 Student — Progress / Kanji / Reading / Onboarding

| Endpoint | DTO | L2 @Valid | L2 rules | Ghi chú |
|----------|-----|:---------:|:--------:|--------|
| onboarding | `OnboardingRequest` | ✅ | ⚠️1 | rà field |
| update-profile / update-student | `student/UpdateProfileRequest`/`UpdateStudentRequest` | ✅ | ✅3/4 | OK |
| kanji writing attempt/evaluate | `KanjiWritingAttempt/EvaluateRequest` | ✅ | ✅3 | OK |
| reading answer/submit | `ReadingAnswer/SubmitRequest` | ✅ | ✅2/4 | OK |
| learning-progress | `LearningProgressRequest` | ✅ | ✅ NotBlank×2/NotNull/DecimalMin-Max | **ĐÃ VÁ** (2026-07-02): viết lại DTO + `@Valid`; trước đó `getContentType()/getStatus().toUpperCase()` NPE→500 khi null |

### 3.9 Support / Ticket / Notification

| Endpoint | DTO | L2 @Valid | L2 rules | Ghi chú |
|----------|-----|:---------:|:--------:|--------|
| create ticket | `TicketRequest` | ✅ | ✅5 | OK, FE `CreateTicketModal` có `validate` |
| reply ticket | `TicketReplyRequest` | ✅ | ✅2 | OK |
| assign ticket | `AssignTicketRequest` | ✅ | ⚠️1 | rà |
| manual grade | `ManualGradeRequest` | ✅ | ✅3 | điểm server-side |
| notification rule | `NotificationRuleRequest` | ✅ | ✅9 | `[[project_ticket_notification_refactor]]` |
| send notification | `SendNotificationRequest` | ✅ | ✅6 | OK |
| suspend student | `SuspendStudentRequest` | ✅ | ✅ `@Size(max=500)` + `@Valid` | `reason` giữ **optional** (null hợp lệ), chỉ giới hạn độ dài (2026-07-02) |

### 3.10 Admin — Settings / User

| Endpoint | DTO | L2 @Valid | L2 rules | Ghi chú |
|----------|-----|:---------:|:--------:|--------|
| update setting (single/batch) | `UpdateSetting(sBatch)Request` | ✅ | ✅1/3 | OK, FE `EmailTab`/`NotificationTab` có `noValidate` → rà L1 |
| suspend user | `SuspendUserRequest` | ✅ | ✅2 | OK |

---

## 4. GAP đã xác định (chặn merge → phải vá)

> **Cảnh báo phương pháp**: bảng gap ban đầu (dựng từ grep tên `*Request.java` + đếm `@Valid`) có **3 báo động giả** vì grep không phân biệt Entity với DTO, và không biết field nào cố ý optional. Đã kiểm chứng từng file bằng cách đọc code + service trước khi sửa. **Không vá theo grep mù.**

**Đã xử lý (2026-07-02):**

| # | Vị trí | Kết luận sau khi đọc code | Hành động |
|---|--------|--------------------------|-----------|
| G1 ✅ | `LearningProgressRequest` + controller | **Lỗ thật**: service gọi `getContentType()/getStatus().toUpperCase()` → NPE→500 khi null; `contentId` dùng trong query | Viết lại DTO (`@NotBlank`×2, `@NotNull`, `@DecimalMin/Max`) + `@Valid` ở controller |
| G3 ✅ | `StaffQuizSubmitReviewController` | Lỗ nhẹ: DTO **đã** có `@NotNull`×2, chỉ thiếu `@Valid` nên không được enforce | Thêm `@Valid` |
| G4 ✅ | `auth/dto/request/UpdateProfileRequest` | **Dead code**: không file nào import; luồng thật dùng `student/dto/request/UpdateProfileRequest` (đã validate) | **Xoá file** (anti-pattern Dead Code) |
| ~~G2~~ ❌ | `SuspendStudentRequest` | **Báo động giả**: `reason` cố ý optional (`@RequestBody(required=false)`, comment ghi rõ) | Không đụng |
| ~~G5~~ ❌ | `StaffPasswordResetRequest` | **Báo động giả**: là JPA `@Entity`, không phải request DTO | Không đụng |

**Đã rà xong (2026-07-02) — KHÔNG có lỗ, KHÔNG sửa:**

| # | Vị trí | Kết luận sau khi đọc code |
|---|--------|--------------------------|
| G6 ✅ | List lồng nhau | `SubmitExamRequest.answers`, `ExamAssignQuestionsRequest.assignments`, `AssignQuestionsRequest.assignments` **đều đã có `@NotEmpty + @Valid`** (cascade OK). `CreateQuestionRequest` **không có list con** (câu hỏi phẳng optionA–D) → SPEC nhắc "answers" là giả định sai. **Không sửa.** |
| G7 ✅ | DTO chỉ-1-annotation | Toàn báo động giả: `ReviewRequest.rating` là **chuỗi** (`@Pattern`, không phải số → không cần Min/Max), nullable cố ý; `BulkDeleteRequest.ids` **đã có `@NotEmpty`**; `UpdateKanji/Vocabulary/Grammar` là **partial-update cố ý** (null=giữ nguyên → thêm `@NotBlank` sẽ phá luồng); `UpdateLesson` đã đủ 6 rule; `DeckCreateRequest`/`AssignTicketRequest` đủ. **Không sửa.** |

> **Ghi chú `OnboardingRequest.jlptGoal`** (và `LearningProgressRequest` sau khi vá): enum-invalid vẫn ném `IllegalArgumentException` → hiện rơi vào handler `Exception` chung = **500** (nên là 400). Đây là vấn đề chung nhiều endpoint — cân nhắc thêm handler `IllegalArgumentException` → 400 trong `GlobalExceptionHandler` (ticket riêng, ngoài phạm vi spec này).

### 4.2 Lỗ Frontend (L1) — ưu tiên trung bình (UX)

| # | Vị trí | Vấn đề | Cách vá |
|---|--------|--------|---------|
| F1 | `Register.jsx` | `noValidate`, không chặn submit khi pwd/confirm lệch hay field rỗng — chỉ hiện icon | Thêm state `errors`, chặn `handleSubmit`, disable nút khi invalid |
| F2 | Các form còn `noValidate` **không** có state lỗi: `Login`, `StaffSetupPassword`, `StaffForgotPassword`, `StaffChangeTempPassword`, `ForgotPassword`, `ResetPassword`, admin `EmailTab`/`NotificationTab` | L1 vắng → mọi lỗi rỗng phải round-trip xuống BE | Thêm validate client tối thiểu (required + format) theo mẫu `ChangePassword.jsx` |

> **Ghi nhận tích cực**: các form đã có L1 đầy đủ (state `errors`/`validate`): `UserModals`, `ChangePassword`, `Profile`, `ManagerNotifications`, `CreateTicketModal`, `QuestionFormModal`, `AssessmentFormModal`.

---

## 5. Tiêu chí nghiệm thu (Definition of Done cho SPEC này)

1. **0 endpoint ghi** thiếu `@Valid` khi có `@RequestBody` (hết G1–G3).
2. **0 DTO** `*Request` không có annotation nào (hết G4–G5).
3. Mọi `List<...Request>` lồng nhau có `@Valid` cascade (G6 xác minh xong).
4. Mọi DTO chỉ-1-rule được rà và bổ sung rule thiếu (G7).
5. Mọi form người dùng nhập tay có L1 (required + format) — hoặc ghi chú lý do bỏ qua.
6. **L3 giữ nguyên nguyên tắc**: điểm/thời gian/trạng thái tính server-side; authz = role + subscription.

---

## 6. Cách kiểm chứng (rà lại tự động)

```bash
# BE — DTO không có annotation validation nào (phải rỗng):
cd apps/backend/src/main/java/com/jlpt
for f in $(find . -name "*Request.java"); do
  grep -q "jakarta.validation" "$f" || echo "NO-VALIDATION: $f"
done

# BE — Controller có @RequestBody nhưng thiếu @Valid (đối chiếu số lượng):
for c in $(find . -name "*Controller.java"); do
  b=$(grep -c "@RequestBody" "$c"); v=$(grep -c "@Valid" "$c")
  [ "$b" -gt "$v" ] && echo "MISSING @Valid: $c (body=$b valid=$v)"
done

# FE — form dùng noValidate (ứng viên thiếu L1):
grep -rl "noValidate" apps/frontend/src --include=*.jsx
```

> Chạy 3 lệnh trên trước mỗi PR đụng vào form/endpoint. Kết quả kỳ vọng sau khi vá: 2 lệnh đầu **không in gì**; lệnh 3 chỉ còn form đã ghi chú lý do.

---

## 6b. Trạng thái sau khi vá (2026-07-02)

- **L2 (BE bean-validation) = 100%**: cả 2 detector (§6) **sạch** — 0 DTO thiếu annotation, 0 controller `@RequestBody` thiếu `@Valid`. `mvn test` 64/64 pass.
- **`@RequestParam` phân trang = ĐÃ VÁ (2026-07-02)**: bật `@Validated` ở **20 controller list** + `@Min(0)` cho `page`, `@Min(1) @Max(100)` cho `size` (chặn DoS/OOM do `size` khổng lồ). Thêm handler `ConstraintViolationException` → **400** trong `GlobalExceptionHandler` (nếu không sẽ 500). `mvn test` 64/64 pass.
- **CHƯA phủ (còn lại)**:
  1. **L3 business-rule** — thủ công, đã xác minh các luồng chính (auth, quiz, flashcard) nhưng chưa audit 100% mọi service.
  2. **`@RequestParam` phi-phân-trang & `@PathVariable`** — các param `String level`/`type`/`status`... chưa ràng `@Pattern` (dựa vào enum-convert ở service → nay trả 400 nhờ handler `IllegalArgumentException`, chấp nhận được).
  3. **L1 FE** — nhiều form vẫn `noValidate`, không đồng bộ (F1/F2).

## 7. Kết luận — "Đã bao hàm hết chưa?"

**Gần đủ, sau khi vá các lỗ thật.** Nền tảng vốn tốt: L2 phủ gần hết DTO, `GlobalExceptionHandler` chuẩn hoá lỗi, **L3 nghiệp vụ vững** (khớp pwd, điểm server-side, lock quiz, authz role+subscription).

Rà lại thực tế (đọc code, không tin grep mù): trong 5 "gap BE" nghi ban đầu chỉ có **3 lỗ thật (G1, G3, G4) — ĐÃ VÁ 2026-07-02** (compile Maven xanh); 2 là **báo động giả** (G2 optional, G5 là Entity). Còn **G6/G7 cần rà thủ công** (chưa làm) và **L1 FE không nhất quán** (nhiều form auth `noValidate` không chặn submit — F1/F2, ưu tiên UX).

**Bài học**: checklist tự động (§6) chỉ để **khoanh vùng nghi ngờ**, phải đọc code xác nhận trước khi sửa — nếu không sẽ thêm validation vào Entity hoặc phá field optional (đúng loại "code rác" cần tránh).
