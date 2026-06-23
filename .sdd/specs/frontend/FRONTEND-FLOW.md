# FRONTEND-FLOW.md — SakuJi E-learning
>
> **Mục đích:** Bản đồ toàn bộ trang frontend — luồng người dùng, logic điều hướng, gap analysis
> **Version:** 1.1 | **Last Updated:** 2026-06-02
> **Domain rules:** AGENTS.md §7 (JLPT Domain Rules) · AGENTS.md §5 (Forbidden Patterns) · AGENTS.md §7.1 (Score Rules) · AGENTS.md §7.3 (Subscription)
> **Đọc cùng:** `feat-frontend-design/SPEC.md` (Design System) · `feat-frontend-redux/SPEC.md` (State Architecture)

---

## 1. INVENTORY — TOÀN BỘ TRANG

### 1.1 Trạng thái hiện tại

| Route | Trang | Role | Auth | Spec tồn tại | Status |
|:---|:---|:---|:---|:---|:---|
| `/` | Landing Page (Home) | Guest | Không | ✅ `feat-landing-page` | Draft |
| `/login` | Đăng nhập | Guest | Không | ✅ `feat-frontend-redux` §3.4 | Implemented |
| `/register` | Đăng ký | Guest | Không | ✅ `feat-frontend-redux` §3.5 | Implemented |
| `/forgot-password` | Quên mật khẩu | Guest | Không | ✅ `feat-frontend-redux` §3.6 | Implemented |
| `/reset-password` | Đặt lại mật khẩu | Guest | Không | ✅ `feat-frontend-redux` §3.7 | Implemented |
| `/dashboard` | Dashboard học sinh | STUDENT | ✅ | ✅ `feat-dashboard` | Draft |
| `/learn/new` | Học từ mới | STUDENT | ✅ | ❌ **MISSING** | — |
| `/review` | Ôn tập (Flashcard SRS) | STUDENT | ✅ | ❌ **MISSING** | — |
| `/kanji` | Luyện Kanji (OCR) | STUDENT | ✅ | ❌ **MISSING** | — |
| `/grammar` | Ngữ pháp | STUDENT | ✅ | ❌ **MISSING** | — |
| `/dictionary` | Từ điển | STUDENT | ✅ | ❌ **MISSING** | — |
| `/mock-test` | Thi thử JLPT | STUDENT | ✅ | ❌ **MISSING** | — |
| `/mock-test/:id/attempt` | Làm bài thi | STUDENT | ✅ | ❌ **MISSING** | — |
| `/mock-test/:id/results` | Kết quả thi | STUDENT | ✅ | ❌ **MISSING** | — |
| `/flashcard` | Flashcard SRS | STUDENT | ✅ | ❌ **MISSING** | — |
| `/lessons/:id` | Chi tiết bài học | STUDENT | ✅ | ❌ **MISSING** | — |
| `/progress` | Tiến độ học tập | STUDENT | ✅ | ❌ **MISSING** | — |
| `/profile` | Hồ sơ cá nhân | STUDENT | ✅ | ❌ **MISSING** | — |
| `/settings/password` | Đổi mật khẩu | STUDENT | ✅ | ❌ **MISSING** | — |
| `/subscription` | Nâng cấp VIP | STUDENT | ✅ | ❌ **MISSING** | — |
| `/subscription/success` | Thanh toán thành công | STUDENT | ✅ | ❌ **MISSING** | — |
| `/onboarding` | Thiết lập lần đầu | STUDENT | ✅ | ❌ **MISSING** | — |
| `/certificates` | Chứng chỉ | STUDENT | ✅ | ❌ **MISSING** | — |
| `/courses` | Danh sách khoá học | STUDENT | ✅ | ❌ **MISSING** | — |
| `/staff/*` | Giao diện nhân viên | STAFF | ✅ | ❌ **MISSING** | — |
| `/admin/*` | Giao diện quản trị | ADMIN | ✅ | ❌ **MISSING** | — |
| `/403` | Forbidden | ANY | — | ❌ **MISSING** | — |
| `/404` | Not Found | ANY | — | ❌ **MISSING** | — |

**Tóm tắt:** 5/33 trang đã có spec. 28 trang còn lại chưa có spec frontend.

---

## 2. ĐÁNH GIÁ LOGIC TỪNG TRANG

### 2.1 Landing Page (`/`)

**Logic:** ✅ Hợp lý

- Là điểm vào đúng cho khách chưa đăng nhập
- Tự redirect `/dashboard` khi đã đăng nhập
- Chứa đủ 5 section: TopBar → Hero → Feature-A → Feature-B → Footer
- **Vấn đề nhỏ:** Nav links "Tính năng / Bảng giá / Blog" trỏ đến trang chưa có spec. Đề xuất: dùng `href="#features"` (scroll anchor trong cùng trang) thay vì route riêng ở giai đoạn đầu.

### 2.2 Auth Pages (Login / Register / Forgot / Reset)

**Logic:** ✅ Hợp lý — đã implement

- Login thành công → `/dashboard` ✅
- Register thành công → success screen (không auto-login) ✅ — bảo mật đúng
- **Vấn đề:** Sau khi xác nhận email, user phải quay lại `/login`. Chưa có trang "Email Confirmed" trung gian dẫn thẳng vào app.
- **Thiếu:** Sau login lần đầu (new user), không có bước Onboarding để chọn JLPT level. User vào thẳng Dashboard với dữ liệu trống.

### 2.3 Dashboard (`/dashboard`)

**Logic:** ✅ Hợp lý — thiết kế tốt

- 3 cột (Streak | Course + Lessons | Quick Actions) phù hợp với thông tin cần thiết nhất
- Skeleton loading ✅
- Empty state ✅
- **Vấn đề:** Click LessonCard → route `/lessons/:id` chưa có spec. Dashboard có thể navigate đến trang không tồn tại.
- **Vấn đề:** Không có CTA hoặc badge cho VIP content bị khoá. User FREE không biết tại sao một số lesson locked (do prerequisites hay do VIP?).

### 2.4 Các trang học (chưa có spec)

**Logic cần đánh giá sau khi spec được tạo.**

- `/learn/new`: Học từ vựng / bài học mới — cần rõ: hiển thị lesson nào? Theo JLPT level?
- `/review`: Ôn tập SRS — cần rõ: hiển thị flashcard queue của ngày hôm nay
- `/kanji`: OCR practice — cần rõ: chọn kanji cần luyện trước hay vẽ tự do?

---

## 3. USER JOURNEY MAPS

### JOURNEY 1 — Khám phá & Đăng ký (New User Acquisition)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  ENTRY: URL trực tiếp hoặc search engine                                    │
└─────────────────────────────────────────────────────────────────────────────┘
         ↓
[/] Landing Page
  • Xem Hero: "Memorize 1000 Kanji in 1 month"
  • Xem Feature-A: Spaced Repetition demo
  • Xem Feature-B: Vocab, Giao tiếp, Từ điển
         ↓ click "Get started"
[/register] Đăng ký
  • Nhập: fullName, email, password, confirmPassword
  • Validate client-side (format, strength)
  • POST /auth/register
         ↓ thành công (201)
[/register] Success Screen
  • "Kiểm tra email để xác nhận tài khoản"
  • [Gửi lại email] button
         ↓ user mở email, click link xác nhận
[Email Verified] ← ⚠️ MISSING: cần trang /verify-email?token=xxx
  • Backend kích hoạt account (status: pending → active)
  • Hiển thị: "Tài khoản đã được xác nhận!"
  • CTA: "Đăng nhập ngay →"
         ↓
[/login] Đăng nhập lần đầu
         ↓ thành công
[/onboarding] ← ⚠️ MISSING: cần trang setup lần đầu
  • Chọn JLPT Level mục tiêu (N5 / N4 / N3 / N2 / N1)
  • Chọn thời gian học mỗi ngày (5 / 10 / 15 / 20 phút)
  • Chọn kỹ năng ưu tiên (Kanji / Vocabulary / Grammar / All)
  • POST /students/onboarding
         ↓
[/dashboard] Dashboard (đã có data mặc định cho level đã chọn)
```

**Gaps:** `/verify-email`, `/onboarding`

---

### JOURNEY 2 — Đăng nhập hàng ngày (Returning User)

```
[/] Landing Page
  ↓ click "Đăng nhập"
[/login]
  ↓ nhập email + password → thành công
[/dashboard]
  ↓ thấy streak, tiếp tục bài học

     OR nếu quên mật khẩu:
[/login] → click "Quên mật khẩu?"
  ↓
[/forgot-password] → nhập email → POST /auth/forgot-password
  ↓ success screen
[Email] → click link reset
  ↓
[/reset-password?token=xxx]
  ↓ nhập mật khẩu mới → thành công
[/login] → đăng nhập lại
  ↓
[/dashboard]
```

**Logic:** ✅ Đã đủ spec. Không có gap.

---

### JOURNEY 3 — Học tập hàng ngày (Daily Study Loop)

```
[/dashboard]
  ├── Xem streak + tuần hiện tại
  ├── Xem progress khoá học
  ├── Click "HỌC TỪ MỚI" (CTA trong HeroBanner)
  │     ↓
  │   [/learn/new] ← ⚠️ MISSING SPEC
  │     • Hiển thị bài học tiếp theo theo lộ trình
  │     • Content: từ vựng, grammar, kana/kanji mới
  │     • Kết thúc bài → ghi vào learning_activity_log
  │     ↓ hoàn thành
  │   [/dashboard] (streak +1 nếu ngày đầu tiên học)
  │
  ├── Click "Ôn tập" tab → TopNav
  │     ↓
  │   [/review] ← ⚠️ MISSING SPEC
  │     • Hiển thị flashcard queue SRS của ngày hôm nay
  │     • Lật thẻ → tự đánh giá Easy / Hard / Again
  │     • Kết thúc queue → "Done for today!" screen
  │     ↓
  │   [/dashboard]
  │
  └── Click một LessonCard cụ thể
        ↓
      [/lessons/:id] ← ⚠️ MISSING SPEC
        • Nội dung bài học (text, audio, image)
        • Bài tập nhỏ cuối bài
        ↓ hoàn thành
      [/dashboard] (lesson progress updated)
```

**Gaps:** `/learn/new`, `/review`, `/lessons/:id`

---

### JOURNEY 4 — Luyện Kanji với OCR (AI Feature)

> **Domain rules applied (AGENTS.md §7.5):** Kết quả AI phải validate trước khi lưu DB. AI score là `ai_score_suggestion` — Staff có thể override với `final_score`. Lỗi AI phải log đầy đủ + fallbackk response. AI call: timeout 30s + retry max 3 lần. AI call phải **async** (trả `job_id` ngay, poll kết quả). OCR chỉ so sánh **similarity %** — không phân tích stroke order (ADR-007). File ảnh/audio lưu tại /uploads hoặc S3 — KHÔNG lưu BLOB.

```
[/dashboard] → click "Kanji" tab
  ↓
[/kanji] ← ⚠️ MISSING SPEC
  • Danh sách kanji theo JLPT level
  • Filter: N5 / N4 / N3 / N2 / N1
  • Chọn một kanji để luyện tập
  ↓
[/kanji/:character] ← ⚠️ MISSING SPEC (OCR Practice page)
  • Hiển thị kanji chuẩn + stroke order (static image)
  • Khu vực vẽ (canvas) hoặc upload ảnh chữ viết tay
  • POST /api/ai/ocr/submit → trả { jobId, status: PENDING }
  • Poll GET /api/ai/ocr/:jobId
  ↓ kết quả sẵn sàng
  • Hiển thị similarity % + so sánh hình ảnh
  • Rating: Xuất sắc (≥80%) / Tốt (60-79%) / Cần luyện (<60%)
  • AI score là `ai_score_suggestion` — chỉ Staff mới thấy và có thể ghi đè `final_score`
  ↓
[/kanji] (quay lại danh sách)

  HOẶC nếu AI lỗi (timeout / fail sau 3 retries):
  • Hiển thị fallbackk message: "Không thể phân tích ngay lúc này, vui lòng thử lại sau."
  • Nphêt "Thử lại" → POST /api/ai/ocr/submit lại

**Gaps:** `/kanji`, `/kanji/:character`

---

### JOURNEY 5 — Nâng cấp VIP (Subscription Upgrade)

> **Domain rules applied (AGENTS.md §7.3):** Authorization check CẢ Role VÀ subscription/level. Subscription hết hạn check real-time — cache tối đa 5 phút. Thay đổi subscription phải có audit log. Cấm cấp quyền VIP mà không kiểm tra subscription.


```

Trigger 1: User click vào lesson có badge "VIP"
Trigger 2: User thấy banner "Mở khoá toàn bộ nội dung" trên dashboard
Trigger 3: User vào menu "Bảng giá" từ TopBar landing

  ↓
[/subscription] ← ⚠️ MISSING SPEC
  • Hiển thị 2–3 gói: Monthly / Quarterly / Annual
  • So sánh FREE vs VIP features
  • CTA "Đăng ký ngay"
  ↓ chọn gói
[/subscription/checkout] ← ⚠️ MISSING SPEC (hoặc redirect thẳng sang gateway)
  • Review gói + giá
  • Nhập thông tin thanh toán (VNPay / Stripe)
  ↓ redirect sang payment gateway
[Payment Gateway — external]
  ↓ callbackk webhook → backend process
[/subscription/success?orderId=xxx] ← ⚠️ MISSING SPEC
  • "Chúc mừng! Tài khoản VIP đã được kích hoạt"
  • Hiển thị ngày hết hạn
  • CTA: "Bắt đầu học ngay →"
  ↓
[/dashboard] (nội dung VIP đã mở khoá)

  HOẶC nếu thanh toán thất bại:
[/subscription/failed] ← ⚠️ MISSING SPEC
  • Thông báo lỗi + hỗ trợ
  • CTA "Thử lại"

```

**Gaps:** `/subscription`, `/subscription/success`, `/subscription/failed`
**⚠️ Edge case — Subscription hết hạn mid-session:**
- Backend trả 403 kèm `reason: "VIP_EXPIRED"` khi user đang học
- Frontend hiển thị modal "Gói VIP của bạn đã hết hạn" + CTA "Gia hạn ngay → /subscription"
- User không bị mất dữ liệu bài học hiện tại (đã lưu vào DB)
- Backend real-time check, không dựa vào frontend cache


---

### JOURNEY 6 — Thi thử JLPT (Mock Exam)

> **Domain rules applied (AGENTS.md §7.1):** quiz_attempt.score luôn >= 0 và <= quiz.max_score. Mỗi lần nộp bài tạo bản ghi MỚI — không UPDATE. Điểm số chỉ được tính bởi Service layer — Client không gửi score. Kết quả đã nộp là bất biến. Thời gian làm bài server-side validation. Quiz đã có attempt → lock câu hỏi (is_locked = true).


```

[/dashboard] → click QuickActionCard "Thi Thử JLPT"
  ↓ OR click tab "Thi thử" trong TopNav
[/mock-test] ← ⚠️ MISSING SPEC
  • Chọn đề thi: N5 / N4 / N3 / N2 / N1
  • Thông tin: số câu, thời gian, quy định
  • CTA: "Bắt đầu thi"
  ↓
[/mock-test/:id/attempt] ← ⚠️ MISSING SPEC (Fullscreen exam layout)
  • TopNav tối giản: Logo + Countdown timer
  • Progress bar tổng số câu
  • Content area: câu hỏi + options
  • Navigator panel: nhảy đến câu bất kỳ
  • CTA: "Nộp bài"
  ↓ submit → POST /api/quiz-attempts
[/mock-test/:id/results] ← ⚠️ MISSING SPEC
  • Điểm số: xx/100
  • Phân tích theo kỹ năng: Từ vựng, Ngữ pháp, Đọc hiểu
  • So sánh với lần thi trước (nếu có)
  • CTA: "Xem đáp án chi tiết" | "Thi lại" | "Về dashboard"
  ↓
[/progress] (cập nhật lịch sử thi)

```

**Gaps:** `/mock-test`, `/mock-test/:id/attempt`, `/mock-test/:id/results`
**⚠️ Edge case — Mất kết nối giữa bài thi:**
- Answers đã chọn lưu vào Redux + localStorage (cache tạm, không thay thế DB)
- Khi kết nối lại: kiểm tra `localStorage.getItem(`exam_draft_{id}`)`
- Nếu có: hiển thị "Bài thi của bạn chưa được nộp. Tiếp tục?" → resume
- Submitting mà không có answers: backend reject với `status: 400`
- Timer vẫn chạy ở backend — không dựa vào client clock


---

### JOURNEY 7 — Quản lý hồ sơ & Đổi mật khẩu

```

[/dashboard] → click avatar (góc phải TopNav)
  ↓ dropdown: Hồ sơ / Đổi mật khẩu / Đăng xuất
[/profile] ← ⚠️ MISSING SPEC
  • Xem: avatar, fullName, email (read-only), jlpt_level
  • Sửa: fullName, phone, date_of_birth, bio, target_jlpt_level
  • Upload avatar
  • PUT /students/me
  ↓ click "Đổi mật khẩu"
[/settings/password] ← ⚠️ MISSING SPEC
  • current_password + new_password + confirm
  • POST /auth/change-password
  ↓ thành công → toast "Đổi mật khẩu thành công"
[/profile]

```

**Gaps:** `/profile`, `/settings/password`

---

### JOURNEY 8 — Nhận Chứng chỉ (Certificate)

```

Trigger: User hoàn thành toàn bộ bài trong một JLPT level

[/progress] ← ⚠️ MISSING SPEC
  • Radar chart kỹ năng
  • Lịch sử thi thử
  • Completions theo loại (Kanji, Vocab, Grammar, Kana)
  • Badge "N5 Completed" nếu đủ điều kiện
  ↓ click "Nhận chứng chỉ"
[/certificates] ← ⚠️ MISSING SPEC
  • Danh sách chứng chỉ đã đạt
  • Xem trước chứng chỉ (preview card với tên, level, ngày)
  • CTA: "Tải xuống PDF" | "Chia sẻ LinkedIn" | "Chia sẻ Facebook"
  ↓
[Certificate Downloaded / Shared]

```

**Gaps:** `/progress`, `/certificates`

---


### JOURNEY 9 — Quản trị hệ thống (Admin Login)

> **Domain rules applied (AGENTS.md §7.3):** Thay đổi subscription phải có audit log.

```

[/login] (dùng chung với Student/Staff)
  • Nhập: email + password
  • POST /api/auth/login
  ↓ thành công (role = ADMIN → nhận JWT trực tiếp)
[/admin]
  • Quản lý người dùng, khóa học, nội dung
  • Audit log viewer
  • Hệ thống: thanh toán, cấu hình, báo cáo

```

**Gaps:** /admin/dashboard (spec ở SPEC-admin-dashboard.md)

## 4. FLOW TỔNG THỂ — TOÀN BỘ HỆ THỐNG

```

                    ┌──────────────────────────┐
                    │   / (Landing Page)        │
                    │   Public — Guest          │
                    └──────┬───────────────┬────┘
                           │               │
               "Get started"               "Đăng nhập"
                           ↓               ↓
              ┌────────────────┐   ┌───────────────────┐
              │  /register     │   │  /login            │
              └────────┬───────┘   └────────┬──────────┘
                       ↓ email               │ success
              ┌─────────────────┐            │
              │  /verify-email  │────────────┘
              │  ⚠️ MISSING     │
              └─────────────────┘
                                             ↓
                              ┌───────────────────────────┐
                              │  /onboarding               │
                              │  ⚠️ MISSING (first login)  │
                              └──────────────┬────────────┘
                                             ↓
┌────────────────────────────────────────────────────────────────────────────────┐
│                          /dashboard (HOME BASE)                                │
│  StreakCard | HeroBanner (course + progress) | LessonList | QuickActions       │
└───┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┬─────────┘
    ↓          ↓          ↓          ↓          ↓          ↓          ↓
/learn/new  /review    /kanji    /grammar  /dictionary /mock-test  /progress
⚠️MISSING  ⚠️MISSING ⚠️MISSING ⚠️MISSING  ⚠️MISSING  ⚠️MISSING  ⚠️MISSING
    ↓          ↓          ↓                           ↓          ↓
/lessons   /flashcard  /kanji/:id                /mock-test  /certificates
/:id       ⚠️MISSING  ⚠️MISSING               /:id/results ⚠️MISSING
⚠️MISSING                                       ⚠️MISSING

                              ↓ (avatar click)
              ┌──────────────────────────────────────┐
              │  /profile  |  /settings/password      │
              │  ⚠️MISSING |  ⚠️MISSING               │
              └──────────────────────────────────────┘

                              ↓ (locked VIP content)
              ┌──────────────────────────────────────┐
              │  /subscription → /subscription/success → /subscription/failed
              │  ⚠️MISSING     ⚠️MISSING              │
              └──────────────────────────────────────┘

```

---

## 5. GAP ANALYSIS — TRANG CẦN SPEC THEO ƯU TIÊN

### Priority 1 — Blocking (cần trước khi ship)

| Trang | Lý do cấp bách |
|:---|:---|
| `/onboarding` | User mới đăng nhập vào dashboard trống — mất orientation hoàn toàn |
| `/lessons/:id` | Dashboard có lesson list nhưng click không đến đâu |
| `/profile` | `fetchProfileThunk` + `updateProfileThunk` đã implement, không có trang để dùng |
| `/settings/password` | UC-05 (Change Password) đã có backend spec, thiếu frontend |
| `/403` `/404` | PrivateRoute đang redirect `/403` nhưng trang không tồn tại |

### Priority 2 — Core Learning Loop

| Trang | Lý do |
|:---|:---|
| `/learn/new` | CTA chính của dashboard, không thể null |
| `/review` | Tab "Ôn tập" trong TopNav, daily study loop |
| `/flashcard` | QuickActionCard trong dashboard trỏ đến đây |
| `/mock-test` | Tab "Thi thử" + QuickActionCard |
| `/mock-test/:id/results` | Kết quả sau thi — bắt buộc để loop hoàn chỉnh |
| `/progress` | QuickActionCard "Tiến Độ" trỏ đến đây |
| `/verify-email` | Hiện tại không có trang confirm email, UX xấu |

### Priority 3 — Monetization & Advanced

| Trang | Lý do |
|:---|:---|
| `/subscription` | Không có đường nâng cấp VIP từ frontend |
| `/subscription/success` | Cần feedbackk sau payment |
| `/kanji` | OCR feature — AI core value |
| `/certificates` | Motivation & retention |

### Priority 4 — Nice-to-have

| Trang | Lý do |
|:---|:---|
| `/grammar` | Tab TopNav, chưa có gì |
| `/dictionary` | Feature có giá trị nhưng không blocking |
| `/courses` | Browse khoá học, referenced từ EmptyState |
| `/staff/*` | Backend spec tồn tại, chưa có frontend |
| `/admin/*` | Backend spec tồn tại, chưa có frontend |

---

## 6. KIẾN NGHỊ SẮP XẾP THỨ TỰ TRIỂN KHAI

```

Sprint 1 — Foundation (đã có + gaps blocking)
  ✅ Landing, Login, Register, Forgot/Reset Password
  ▶ /onboarding
  ▶ /profile
  ▶ /settings/password
  ▶ /403, /404
  ▶ /verify-email (email confirmation page)

Sprint 2 — Core Learning Loop
  ▶ /lessons/:id (lesson detail + content player)
  ▶ /learn/new (next lesson navigator)
  ▶ /review (SRS flashcard queue)
  ▶ /flashcard (standalone flashcard browser)

Sprint 3 — Assessment Loop
  ▶ /mock-test (exam selection)
  ▶ /mock-test/:id/attempt (fullscreen exam)
  ▶ /mock-test/:id/results (score + analysis)
  ▶ /progress (radar chart + history)

Sprint 4 — Monetization & Retention
  ▶ /subscription (VIP upgrade)
  ▶ /subscription/success
  ▶ /kanji + /kanji/:id (OCR practice)
  ▶ /certificates

Sprint 5 — Administration
  ▶ /staff/*(content management, grading)
  ▶ /admin/* (user management, analytics)
  ▶ /grammar, /dictionary

```

---

## 7. NAVIGATION STRUCTURE (TopNav Tabs → Routes)

### Student TopNav (Dashboard layout)
| Tab | Icon | Route | Spec Status |
|:---|:---|:---|:---|
| Ôn tập | 📖 | `/review` | ⚠️ Missing |
| Học từ mới | ✨ | `/learn/new` | ⚠️ Missing |
| Kanji | 漢 | `/kanji` | ⚠️ Missing |
| Ngữ pháp | 📝 | `/grammar` | ⚠️ Missing |
| Từ điển | 🔍 | `/dictionary` | ⚠️ Missing |
| Nâng cấp | ⭐ | `/subscription` | ⚠️ Missing |
| Thi thử | 📋 | `/mock-test` | ⚠️ Missing |

### Landing TopBar links → destinations
| Link | Route hiện tại | Đề xuất (ngắn hạn) |
|:---|:---|:---|
| Tính năng | `/features` (chưa tồn tại) | `/#features` scroll anchor |
| Bảng giá | `/pricing` (chưa tồn tại) | `/subscription` (Sprint 4) |
| Blog | `/blog` (chưa tồn tại) | Ẩn hoặc link ngoài |

---

## 8. ROUTE GUARD MATRIX

| Route | Guard | Redirect nếu fail |
|:---|:---|:---|
| `/` | `isAuthenticated → dashboard` | — |
| `/login` `/register` | `isAuthenticated → dashboard` | — |
| `/dashboard` | `STUDENT role required` | `/login` |
| `/learn/new` `/review` `/kanji` | `STUDENT role required` | `/login` |
| `/mock-test/*` | `STUDENT role required` | `/login` |
| `/subscription` | `STUDENT role required` | `/login` |
| `/lessons/:id` | `STUDENT role + lesson not locked` | `/dashboard` |
| `/staff/*` | `STAFF role required` | `/403` |
| `/admin/*` | `ADMIN role` | `/403` |

---



## 9. DOMAIN RULE CROSS-REFERENCE (AGENTS.md)

| Rule | § | Áp dụng tại | Frontend impact |
|:---|:---:|:---|:---|
| Score `>= 0` và `<= max_score` | 7.1 | /mock-test/:id/attempt, /progress | Frontend KHÔNG gửi score, chỉ hiển thị kết quả từ backkkend |
| QuizAttempt bất biến (không UPDATE) | 7.1 | /mock-test/:id, /mock-test/:id/results | Frontend không cho sửa kết quả sau khi nộp |
| Thời gian làm bài server-side | 7.1 | /mock-test/:id/attempt | Timer từ backkkend, KHÔNG tự đãm cắt client |
| Quiz lock khi đã thi | 7.1 | /mock-test/:id | Frontend không cho chỉnh sửa quiz đã có attempt |
| `user_progress` chỉ tăng | 7.2 | /lessons/:id, /learn/new, /dashboard | Frontend hiển thị progress, không gửi PATCH |
| Chỉ mở khó bài khi hoàn thành bài trước | 7.2 | /lessons/:id, /dashboard, /learn/new | Hiển thị locked/unlocked badge |
| `is_vip_only` kiểm tra real-time | 7.3 | /dashboard, /lessons/:id, /kanji | Badge VIP trên card, CTA Nâng cấp khi FREE |
| Subscription cache tối đa 5 phút | 7.3 | /dashboard, /lessons/:id | Luôn gọi API, không dựa vào cache của 5 phút |
| Thay đổi subscription phải có audit log | 7.3 | /subscription, /subscription/success | Frontend hiển thị thông báo xác nhận |
| Admin đăng nhập trực tiếp bằng Email/Password | 7.3 | /admin/* | Login → nhận JWT trực tiếp → redirect /admin |
| Kết quả AI phải validate trước khi lưu DB | 7.5 | /kanji, /kanji/:character | Hiển thị similarity % raw, chờ API validate |
| AI score = `ai_score_suggestion`, Staff override | 7.5 | /kanji/:character | Chỉ Staff thấy override button |
| AI async: timeout 30s, retry 3 lần | 7.5 | /kanji/:character | Polling spinner, fallbackk message khi timeout |
| OCR chỉ similarity %, không stroke order | 7.5 | /kanji/:character | Chỉ hiển thị % match |
| File ảnh/audio lưu /uploads hoặc S3 | 7.5 | /kanji/:character | Upload file, nhận URL về backend, KHÔNG lưu BLOB |
| Soft Delete (`is_deleted = true`) | 7.4 | Mọi trang list API | Frontend không hiển mục đã xoá |
| Frontend KHÔNG đặt business logic | 5.11 | Tất cả | Backend chịu trách nhiệm. Frontend render + gọi API |

---



## 10. ERROR & EDGE CASE MATRIX

| Trang | Loading | Empty | Error | Edge Cases |
|:---|:---|:---|:---|:---|
| / | Skeleton Hero | N/A | Toast lỗi load | N/A |
| /login | Button disabled `ng đăng nhập...` | N/A | `api-error` div từ backkkend | Clear error khi gõ lại input |
| /register | Button disabled | N/A | api-error (409 email trùng) | Success screen, không auto-login |
| /verify-email | Loading spinner | Token invalid screen | Link không hợp lệ | Token hết hạn → gửi lại email |
| /forgot-password | Button disabled | N/A | api-error | Success screen kể cả email tồn tại |
| /reset-password | Button disabled | No token screen | api-error | Token hết hạn, link invalid |
| /dashboard | Skeleton 3 cột | EmptyState mascot | Toast error | VIP hết hạn → modal upgrade |
| /learn/new | Loading spinner | EmptyState | Error toast | Lesson bị khóa → redirect dashboard |
| /review | Loading queue | Done for today | Error toast | Không có thẻ để ôn tập hôm nay |
| /kanji | Loading list | EmptyState chọn level | Error toast | Lọc filter → rỗng |
| /kanji/:character | Spinner Analyzing | Canvas rỗng | Fallbackk cannot analyze | Timeout 30s + retry 3 |
| /mock-test | Loading list | EmptyState no exam | Error toast | Hết time → auto-submit |
| /mock-test/:id/attempt | Fullscreen loading | N/A | Modal disconnect | Mất mạng → draft + resume |
| /mock-test/:id/results | Loading result | N/A | Error toast | Kết quả bất biến |
| /subscription | Loading plans | N/A | Error toast | Hết hạn → modal gia hạn |
| /subscription/success | Loading order | N/A | Error toast | Payment fail → /subscription/failed |
| /profile | Loading form | N/A | Error toast | Email/role read-only |
| /settings/password | Button disabled | N/A | api-error | Đăng xuất sau đổi |
| /lessons/:id | Loading lesson | N/A | Error toast | VIP locked → redirect /subscription |
| /progress | Loading chart | EmptyState | Error toast | Không có data → chart rỗng |
| /admin/* | Loading admin | N/A | Error toast | Session 15 phút (access token) |
| /403 | N/A | N/A | Static Forbidden | N/A |
| /404 | N/A | N/A | Static Not Found | N/A |

---
<!-- FRONTEND-FLOW.md v1.1 — 2026-06-02 -->
