# Shared Context — JLPT E-Learning System

> **Mục đích**: Tài liệu ngữ cảnh chung được tham chiếu bởi **toàn bộ** các file SRS trong thư mục này.
> Mọi spec, use-case, constraint và test plan đều dựa trên các định nghĩa và giả định trong file này.
> Liên quan: [`constraints/global.md`](./constraints/global.md) | [`constraints/safety.md`](./constraints/safety.md) | [`use-cases/Bao_cao_dac_ta_Use_Case.md`](./use-cases/Bao_cao_dac_ta_Use_Case.md).

---

## 1. Tổng Quan Dự Án

| Thuộc tính | Chi tiết |
|-----------|---------|
| **Tên hệ thống** | Japanese Skill Practice Platform (JLPT E-Learning) |
| **Phiên bản SRS** | 1.1 |
| **Phạm vi** | Nền tảng học trực tuyến luyện thi tiếng Nhật — cấp độ N5 → N1 |
| **Loại phần mềm** | Web Application (Monolithic, SPA + REST API) |
| **Ngôn ngữ tài liệu** | Tiếng Việt (mã định danh giữ bằng tiếng Anh) |
| **Ngày cập nhật** | 26/05/2026 |

### Mục tiêu hệ thống

Cung cấp nền tảng học tiếng Nhật toàn diện hỗ trợ học viên:

- Học **Kanji, Kana, Từ vựng, Ngữ pháp** theo cấp độ JLPT (N5–N1).
- Luyện tập qua **Flashcard SRS** (Spaced Repetition System).
- Thi thử đề **JLPT Mock Exam** với chấm điểm tự động.
- Luyện **nói** (Speaking + AI Speech Recognition) và **viết tay** (Handwriting + OCR AI).
- Tra cứu **từ điển** tích hợp.
- Theo dõi **tiến độ học tập** cá nhân theo thời gian.

---

## 2. Stakeholders

| # | Stakeholder | Vai trò | Mối quan tâm chính |
|---|-------------|---------|-------------------|
| 1 | **Product Owner** | Định hướng sản phẩm | Đúng nghiệp vụ, đủ use case, chất lượng nội dung |
| 2 | **Dev Team** | Thiết kế & phát triển | Spec rõ ràng, không mâu thuẫn, nhất quán |
| 3 | **QA Team** | Kiểm thử | Tiêu chí chấp nhận rõ, edge case đủ |
| 4 | **System Admin** | Vận hành hạ tầng | Bảo mật, hiệu năng, khả năng deploy |
| 5 | **Student (End User)** | Người học | UX mượt, AI chính xác, tiến độ được lưu |
| 6 | **Staff (Content Creator)** | Soạn nội dung | Workflow rõ, công cụ CRUD đơn giản |

---

## 3. Tác Nhân (Actors)

Hệ thống có **4 tác nhân chính** với phân quyền độc lập:

| Actor | Mô tả | Quyền hạn |
|-------|-------|-----------|
| **Student** | Học viên cuối — người dùng chính | Học, luyện tập, thi thử, xem tiến độ |
| **Staff** | Nhân viên nội dung | CRUD nội dung học tập, chấm bài nói, hỗ trợ học viên |
| **StaffManager** | Quản lý kiểm duyệt nội dung | Duyệt / từ chối / xuất bản nội dung Staff gửi |
| **Admin** | Quản trị viên hệ thống | Phân quyền, cấu hình kỹ thuật, báo cáo toàn diện |

> ⚠️ **Rule**: UI và logic của Admin và Staff **phải tách biệt hoàn toàn** — không dùng chung màn hình hay component (LESSON-001, SAFE-AUTH-05).

### Ma trận phân quyền tóm tắt

| Chức năng | Student | Staff | StaffManager | Admin |
|-----------|:-------:|:-----:|:------------:|:-----:|
| Học nội dung | ✅ | ✅ | ✅ | ✅ |
| CRUD nội dung học tập | ❌ | ✅ (Draft) | ✅ (Published) | ✅ |
| Duyệt nội dung | ❌ | ❌ | ✅ | ✅ |
| Quản lý tài khoản | ❌ | ✅ (hạn chế) | ❌ | ✅ (full) |
| Cấu hình hệ thống | ❌ | ❌ | ❌ | ✅ |
| Xem báo cáo hệ thống | ❌ | ✅ (giới hạn) | ✅ (giới hạn) | ✅ (full) |

---

## 4. Phạm Vi Hệ Thống (System Boundary)

### 4.1. Trong phạm vi (In Scope)

- Xác thực người dùng (Login / Register / Forgot Password / JWT)
- Học nội dung theo cấp độ JLPT (N5–N1): Kanji, Kana, Vocab, Grammar
- Flashcard với thuật toán Spaced Repetition (SRS)
- Làm bài JLPT Mock Exam (auto-grading)
- Bài Quiz theo chủ đề (trắc nghiệm)
- Luyện nói với AI (Speech Recognition) và chấm điểm
- Luyện viết tay Kanji với AI (OCR similarity %)
- Luyện đọc hiểu và nghe hiểu
- Tra cứu từ điển tích hợp
- Đánh dấu (Bookmark) nội dung yêu thích
- Xem và theo dõi tiến độ học tập cá nhân
- Subscription VIP với kiểm soát nội dung theo cấp độ
- Gửi thông báo (Staff → Student)
- Quản lý người dùng, nội dung, đề thi (Staff/Admin)
- Duyệt và xuất bản nội dung (StaffManager)
- Báo cáo và thống kê (Admin)

### 4.2. Ngoài phạm vi (Out of Scope)

- Thanh toán trực tiếp (payment gateway) — subscription được cấp thủ công qua Admin
- Mobile native app (iOS/Android) — chỉ Web
- Forum / cộng đồng thảo luận
- Tích hợp LMS bên ngoài
- Gamification nâng cao (leaderboard, badges)

---

## 5. Cấp Độ JLPT (Domain Model)

| Cấp độ | Mô tả | Đặc điểm nội dung |
|--------|-------|------------------|
| **N5** | Sơ cấp — Hiragana & Katakana cơ bản | ~800 từ vựng, ~100 Kanji |
| **N4** | Sơ cấp nâng cao | ~1,500 từ vựng, ~300 Kanji |
| **N3** | Trung cấp | ~3,750 từ vựng, ~650 Kanji |
| **N2** | Trung-Cao cấp | ~6,000 từ vựng, ~1,000 Kanji |
| **N1** | Cao cấp — Tương đương bản ngữ | ~10,000 từ vựng, ~2,000 Kanji |

> **Rule**: Học viên chỉ được truy cập nội dung tương ứng với subscription của mình.
> Authorization = **Role** + **Subscription Level** (LESSON-003).

---

## 6. Bảng Thuật Ngữ (Glossary)

| Thuật ngữ | Định nghĩa |
|-----------|-----------|
| **JLPT** | Japanese Language Proficiency Test — kỳ thi năng lực tiếng Nhật chuẩn quốc tế |
| **Kanji** | Chữ Hán (漢字) sử dụng trong tiếng Nhật |
| **Kana** | Bảng chữ cái tiếng Nhật — gồm Hiragana (ひらがな) và Katakana (カタカナ) |
| **Vocabulary** | Từ vựng tiếng Nhật theo chủ đề và cấp độ |
| **Grammar** | Ngữ pháp tiếng Nhật theo mẫu câu và cấp độ |
| **Flashcard** | Thẻ ghi nhớ — một mặt là câu hỏi/từ, mặt kia là đáp án/nghĩa |
| **SRS** | Spaced Repetition System — thuật toán ôn tập theo chu kỳ giúp ghi nhớ lâu dài |
| **Mock Exam** | Đề thi thử JLPT mô phỏng kỳ thi thật, có thời gian và chấm tự động |
| **Quiz** | Bài kiểm tra nhanh theo chủ đề, dạng trắc nghiệm |
| **OCR** | Optical Character Recognition — AI nhận diện chữ viết tay Kanji |
| **Speech Recognition** | AI nhận diện và đánh giá phát âm trong luyện nói (shadowing) |
| **Similarity %** | Phần trăm độ tương đồng giữa chữ viết tay và ký tự chuẩn (đầu ra OCR) |
| **Subscription** | Gói đăng ký học — Free (N5 miễn phí) hoặc VIP (N4–N1) |
| **Staff** | Nhân viên nội dung — soạn thảo và quản lý tài liệu học |
| **StaffManager** | Quản lý nội dung — kiểm duyệt và xuất bản tài liệu của Staff |
| **Admin** | Quản trị viên hệ thống — toàn quyền cấu hình và quản lý |
| **Soft Delete** | Xóa mềm — không xóa vật lý khỏi DB mà đánh dấu `is_deleted = true` |
| **DTO** | Data Transfer Object — đối tượng truyền dữ liệu giữa các tầng, tách biệt với Entity |
| **JWT** | JSON Web Token — token xác thực stateless |
| **Audit Log** | Nhật ký theo dõi các thao tác quan trọng (ai, khi nào, làm gì) |
| **AI Job** | Tác vụ AI bất đồng bộ có trạng thái: `PENDING → PROCESSING → DONE/FAILED` |
| **UC** | Use Case — đặc tả một kịch bản tương tác giữa Actor và hệ thống |

---

## 7. Business Rules Cốt Lõi

Các quy tắc nghiệp vụ sau đây áp dụng **toàn hệ thống** và có mức độ ưu tiên cao nhất:

| ID | Business Rule | Nguồn |
|----|--------------|-------|
| **BR-01** | Authorization = Role + Subscription Level — chỉ kiểm tra role là **không đủ** | LESSON-003 |
| **BR-02** | Điểm bài thi/quiz phải tính toán **server-side** — không tin tưởng điểm từ client | GLOB-ARCH-03 |
| **BR-03** | Bản ghi `quiz_attempt` đã `SUBMITTED` là **bất biến** — không được sửa đổi | SAFE-DATA-05 |
| **BR-04** | Tiến độ học tập (`user_progress`) chỉ tăng — không giảm thủ công | SAFE-DATA-06 |
| **BR-05** | VIP subscription chỉ được kích hoạt sau khi xác nhận thanh toán thành công | SAFE-AUTH-03 |
| **BR-06** | Kiểm tra subscription phải **real-time** — không chỉ dựa vào JWT payload | SAFE-AUTH-06 |
| **BR-07** | Nội dung của Staff phải qua duyệt bởi StaffManager trước khi học viên thấy | UC-33 |
| **BR-08** | File ảnh/audio lưu tại `/app/uploads` (dev) hoặc S3 (prod) — không lưu BLOB trong DB | ADR-006 |
| **BR-09** | AI call phải có timeout (30s) + retry (≤3 lần) + fallback — không silent fail | SAFE-AI-01 |
| **BR-10** | Mọi thao tác Admin/Staff quan trọng phải ghi audit log đầy đủ | SAFE-OPS-03 |

---

## 8. Mô Hình Dữ Liệu Cốt Lõi (Conceptual)

```
User (id, email, passwordHash, role, subscriptionLevel, isDeleted)
  │
  ├─── Course (id, jlptLevel, title, isDeleted)
  │       └─── Lesson (id, type[KANJI|KANA|VOCAB|GRAMMAR], content, isDeleted)
  │
  ├─── FlashcardDeck (id, userId, lessonId)
  │       └─── FlashcardItem (id, front, back, nextReviewAt, easeFactor)
  │
  ├─── QuizAttempt (id, userId, quizId, score, status[IN_PROGRESS|SUBMITTED], submittedAt)
  │
  ├─── MockExamAttempt (id, userId, examId, score, status, submittedAt)
  │
  ├─── AiJob (id, userId, type[OCR|SPEECH], status[PENDING|PROCESSING|DONE|FAILED], result)
  │
  ├─── UserProgress (id, userId, courseId, completedLessons, lastStudiedAt)
  │
  └─── Subscription (id, userId, plan[FREE|VIP], startDate, endDate, isActive)
```

> **Lưu ý**: Mọi bảng nghiệp vụ đều có `created_at`, `updated_at`, `is_deleted`.
> Schema chi tiết xem tại `database/` (Flyway migrations).

---

## 9. Giả Định & Phụ Thuộc (Assumptions & Dependencies)

### 9.1. Giả định

| # | Giả định |
|---|---------|
| A1 | Người dùng truy cập qua Web Browser — không phải mobile native app |
| A2 | Kết nối Internet ổn định — hệ thống không hỗ trợ offline mode |
| A3 | Subscription được quản lý thủ công qua Admin UI — chưa tích hợp payment gateway |
| A4 | AI service (OCR, Speech) là external API — có thể tạm thời không khả dụng |
| A5 | Ngôn ngữ giao diện: Tiếng Việt (học viên Việt Nam là đối tượng chính) |
| A6 | File media: ảnh ≤ 5MB, audio ≤ 20MB |
| A7 | Hệ thống phục vụ concurrent users ở mức vừa phải (không phải hàng triệu) |

### 9.2. Phụ thuộc ngoài (External Dependencies)

| Dịch vụ | Mục đích | Fallback |
|---------|---------|---------|
| AI OCR API | Nhận diện chữ viết tay Kanji | Trả thông báo lỗi rõ ràng |
| AI Speech API | Đánh giá phát âm | Trả thông báo lỗi rõ ràng |
| SMTP Server | Gửi email đặt lại mật khẩu | Queue và retry |
| Redis | Cache subscription, session | Fallback về DB query |

---

## 10. Tham Chiếu

| Tài liệu | Nội dung |
|---------|---------|
| [`constraints/global.md`](./constraints/global.md) | Ràng buộc kỹ thuật: tech stack, naming, hiệu năng, bảo mật |
| [`constraints/safety.md`](./constraints/safety.md) | Ràng buộc an toàn vận hành: data, deployment, auth, AI, file |
| [`use-cases/Bao_cao_dac_ta_Use_Case.md`](./use-cases/Bao_cao_dac_ta_Use_Case.md) | Đặc tả 40 Use Case chi tiết |
| [`CLAUDE.md`](../../CLAUDE.md) | Kiến trúc hệ thống, ADR, Lessons Learned, Anti-patterns |
| [`AGENTS.md`](../../AGENTS.md) | Domain rules, naming conventions, forbidden patterns, DoD |
| `database/` | Flyway migrations (schema chi tiết) |
| `docker-compose.yml` | Cấu hình container, port, volume |
