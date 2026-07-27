# Project Plan — Hệ Thống Học Tiếng Nhật JLPT (SakuJi)

> **Tài liệu:** Kế hoạch tổng thể dự án — tiến độ phát triển & chuẩn bị bảo vệ đồ án.
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-30
> Liên quan: [`constitution.md`](./constitution.md) | [`THESIS_DEFENSE_QNA.md`](./THESIS_DEFENSE_QNA.md) | [`../01-SRS-Requirements/use-cases/`](../01-SRS-Requirements/use-cases/)

---

## 1. Thông Tin Dự Án

| Thuộc tính | Chi tiết |
|-----------|---------|
| **Tên dự án** | Hệ Thống Học Tiếng Nhật JLPT — SakuJi |
| **Loại** | Đồ án tốt nghiệp — Full-stack Web Application |
| **Tech Stack** | Java 21 + Spring Boot 3.x · React 18 · SQL Server · AI (OCR, Speech) |
| **Tổng Use Case** | 40 UC (Student: 20 · Staff: 12 · StaffManager: 2 · Admin: 6) |
| **Backend modules** | 13 feature modules (`auth`, `assessment`, `flashcard`, `learning`, `student`, `staff`, `admin`, `notification`, `support`, `dictionary`, `publishedcontent`, `contentreview`, `staffcontent`) |
| **Frontend pages** | 32 pages (`admin`, `staff`, `manager`, `student`, `quiz`, `mock-test`, `kanji`, `kana`, `grammar`, `vocabulary`, `speaking`, `reading`, `listening`, `flashcard/notebook`, ...) |

---

## 2. Phân Công Nhóm

> Cập nhật phân công theo nhóm thực tế — bảng dưới là template gợi ý.

| Thành viên | Vai trò chính | Module phụ trách |
|-----------|--------------|-----------------|
| **[Dev 1]** | Backend Lead | `auth`, `student`, `assessment`, `flashcard` |
| **[Dev 2]** | Backend | `learning`, `staffcontent`, `contentreview`, `publishedcontent` |
| **[Dev 3]** | Backend | `staff`, `admin`, `notification`, `support`, `dictionary` |
| **[Dev 4]** | Frontend Lead | Student pages: `dashboard`, `lessons`, `quiz`, `mock-test`, `progress` |
| **[Dev 5]** | Frontend | Student pages: `kanji`, `kana`, `grammar`, `vocabulary`, `speaking`, `reading`, `listening`, `notebook` |
| **[Dev 6]** | Frontend | Staff/Admin pages: `staff/*`, `admin/*`, `manager/*` + Integration test |

---

## 3. Tổng Quan Tiến Độ (Status Overview)

> Cập nhật ô `Status` theo thực tế. Legend: ✅ Done · 🔄 In Progress · ⏳ Pending · ❌ Blocked

### 3.1. Backend — Feature Modules

| Module | Chức năng | Phụ trách | Status |
|--------|-----------|-----------|--------|
| `auth` | Login · Register · Forgot Password · JWT · OAuth | Dev 1 | ✅ Done |
| `student` | Profile · Change Password · Subscription check | Dev 1 | ✅ Done |
| `assessment` | Quiz · Mock Exam · Submit · Auto-grade · History | Dev 1 | ✅ Done |
| `flashcard` | SRS Flashcard · Deck · Review queue | Dev 1 | ✅ Done |
| `learning` | Lesson · Kanji · Kana · Vocab · Grammar · Progress | Dev 2 | ✅ Done |
| `staffcontent` | Staff CRUD nội dung · Submit for review | Dev 2 | ✅ Done |
| `contentreview` | StaffManager duyệt · Từ chối · Xuất bản · Lưu trữ | Dev 2 | ✅ Done |
| `publishedcontent` | Lấy nội dung đã publish cho Student | Dev 2 | ✅ Done |
| `staff` | Quản lý học viên · Chấm bài nói · Xem kết quả | Dev 3 | ✅ Done |
| `admin` | Dashboard · Quản lý user · Settings · Reports | Dev 3 | ✅ Done |
| `notification` | Gửi thông báo · Notification rules · Unread count | Dev 3 | ✅ Done |
| `support` | Ticket hỗ trợ · Ticket reply · Assign | Dev 3 | ✅ Done |
| `dictionary` | Tra từ điển · Tìm kiếm Kanji/Vocab | Dev 3 | ✅ Done |
| **AI — OCR** | Nhận diện chữ viết tay Kanji (async job) | Dev 1/2 | 🔄 In Progress |
| **AI — Speech** | Chấm điểm phát âm (async job) | Dev 1/2 | 🔄 In Progress |

### 3.2. Frontend — Pages

| Nhóm | Pages | Phụ trách | Status |
|------|-------|-----------|--------|
| **Auth** | `login`, `register`, `forgot-password`, `verify-email`, `onboarding` | Dev 4/5 | ✅ Done |
| **Student Core** | `dashboard`, `profile`, `settings`, `progress`, `notifications` | Dev 4 | ✅ Done |
| **Học nội dung** | `lessons`, `learn`, `kanji`, `kana`, `grammar`, `vocabulary`, `courses` | Dev 5 | ✅ Done |
| **Luyện tập** | `quiz`, `mock-test`, `reading`, `listening`, `speaking` | Dev 4 | ✅ Done |
| **Tools** | `notebook` (Flashcard), `dictionary`, `support` | Dev 5 | ✅ Done |
| **Staff** | `staff/StaffDashboard`, `StaffContent`, `StaffAssessments`, `StaffQuestions`, `StaffStudents`, `StaffGrading`, `StaffTickets` | Dev 6 | ✅ Done |
| **StaffManager** | `manager/*` (Duyệt nội dung, Quản lý xuất bản) | Dev 6 | 🔄 In Progress |
| **Admin** | `AdminDashboard`, `ManageUsers`, `AdminReports`, `AdminSettings` | Dev 6 | ✅ Done |
| **Misc** | `home`, `blog`, `features`, `error` | Dev 4/5 | ✅ Done |

### 3.3. Integration & Testing

| Hạng mục | Mô tả | Status |
|---------|-------|--------|
| API Integration test | Happy path + error path cho toàn bộ 40+ endpoints | 🔄 In Progress |
| Unit test — Service | Coverage ≥ 80% cho tất cả Service classes | ⏳ Pending |
| E2E test | Luồng Student: Register → Learn → Quiz → Submit | ⏳ Pending |
| Docker Compose | Build toàn bộ stack (backend + frontend + db + redis) | ✅ Done |
| DB Migration | Flyway V1→V6 (init.sql v2.6) | ✅ Done |

---

## 4. Milestones & Deadline

| Milestone | Mô tả | Deadline | Status |
|-----------|-------|----------|--------|
| **M1** | Backend core hoàn chỉnh (Auth + Learning + Assessment) | T4/2026 | ✅ Done |
| **M2** | Frontend Student pages hoàn chỉnh | T5/2026 | ✅ Done |
| **M3** | Staff/Admin portals + Content workflow | T5/2026 | ✅ Done |
| **M4** | AI modules (OCR + Speech) stable | T6/2026 | 🔄 In Progress |
| **M5** | Integration test + Bug fixing + Performance tune | T6/2026 | 🔄 In Progress |
| **M6** | Demo build · Slide · Báo cáo đồ án hoàn chỉnh | T7/2026 | ⏳ Pending |
| **🎯 BẢO VỆ ĐỒ ÁN** | Thuyết trình trước Hội đồng | **[DATE]** | ⏳ |

---

## 5. Kế Hoạch Sprint Còn Lại

### Sprint 6 — AI & Integration (Tuần 1–2 tháng 7)

**Mục tiêu:** Hoàn thiện AI modules + integration test toàn bộ

| Task | Giao cho | Ưu tiên |
|------|---------|---------|
| AI OCR job: timeout + retry 3 lần + fallback + log đầy đủ | Dev 1 | 🔴 P0 |
| AI Speech job: timeout + retry 3 lần + fallback + log đầy đủ | Dev 2 | 🔴 P0 |
| StaffManager pages: duyệt nội dung + quản lý trạng thái xuất bản | Dev 6 | 🔴 P0 |
| Integration test: Auth flow (Register → Verify → Login → Logout) | Dev 3 | 🟠 P1 |
| Integration test: Student Quiz flow (Start → Answer → Submit → Result) | Dev 1 | 🟠 P1 |
| Integration test: Staff content workflow (Create → Submit → Review → Publish) | Dev 2 | 🟠 P1 |
| Unit test coverage ≥ 80% cho Assessment, Flashcard, Learning services | Dev 1/2 | 🟠 P1 |

### Sprint 7 — Polish & Demo Prep (Tuần 3–4 tháng 7)

**Mục tiêu:** Bắt lỗi, tối ưu, chuẩn bị slide và demo

| Task | Giao cho | Ưu tiên |
|------|---------|---------|
| Bug fixing từ integration test | All | 🔴 P0 |
| Performance: kiểm tra query N+1, thêm index nếu cần | Dev 1/3 | 🟠 P1 |
| UI polish: responsive, loading states, error handling | Dev 4/5 | 🟠 P1 |
| Demo data seed: học viên mẫu, bài học, quiz, thi thử | Dev 3 | 🟠 P1 |
| Báo cáo đồ án: viết chương Thiết kế + Cài đặt + Kết quả thử nghiệm | All | 🟠 P1 |
| Slide thuyết trình: 15–20 slide, demo flow | Dev 4 | 🟠 P1 |
| Dry-run bảo vệ nội bộ | All | 🟡 P2 |

---

## 6. Checklist Chuẩn Bị Bảo Vệ

### 6.1. Code & System

- [ ] Toàn bộ API endpoint hoạt động đúng (không có 500 lạ)
- [ ] AI OCR + Speech demo được với file mẫu
- [ ] Docker Compose `docker-compose up` → chạy mượt, không cần manual steps
- [ ] Database có đủ seed data demo (học viên, bài học N3, quiz, 1 lần thi thử)
- [ ] Không có `TODO` comments trong code
- [ ] Không có `System.out.println()` hay `console.log()` trong production code
- [ ] Flyway migrations chạy sạch từ DB trống

### 6.2. Tài Liệu

- [ ] Báo cáo đồ án nộp đúng format theo yêu cầu khoa
- [ ] Slide thuyết trình ≤ 20 slide (đúng 3 phút nội dung chính)
- [ ] Sơ đồ Use Case diagram (Actor - UC)
- [ ] Sơ đồ ERD / Database schema
- [ ] Sơ đồ Kiến trúc hệ thống (hệ thống 3 tier)
- [ ] Sơ đồ luồng chính: Quiz Submit Flow + AI Async Flow

### 6.3. Demo Scenario (Thứ tự demo trước Hội đồng)

1. **[1 phút]** Tổng quan: Giới thiệu hệ thống, tech stack, 4 actor roles
2. **[2 phút]** Student flow: Đăng ký → Đăng nhập → Xem bài học N3 → Làm quiz → Xem kết quả
3. **[2 phút]** AI demo: Luyện viết Kanji (OCR) → Xem similarity % · Luyện nói (Speech) → Xem điểm
4. **[1 phút]** Staff flow: Tạo bài học → Submit duyệt · StaffManager duyệt → Xuất bản
5. **[1 phút]** Admin dashboard: Xem thống kê học viên · Báo cáo
6. **[1 phút]** Highlight bảo mật: JWT · Soft Delete · Audit Log · bcrypt cost 12

### 6.4. Q&A Chuẩn Bị

- [ ] Đọc và ôn tập toàn bộ [`THESIS_DEFENSE_QNA.md`](./THESIS_DEFENSE_QNA.md)
- [ ] Biết giải thích: Tại sao Feature-based thay vì Layer-based?
- [ ] Biết giải thích: Authorization = Role + Subscription (không chỉ Role)
- [ ] Biết giải thích: AI Async flow (submit → job_id → poll → result)
- [ ] Biết giải thích: Immutable quiz attempt (không UPDATE score)
- [ ] Biết giải thích: Soft Delete vs Hard Delete
- [ ] Mở được code minh họa: `QuizSubmitService`, `GlobalExceptionHandler`, một DTO class
- [ ] Mở được DB trong SSMS và chỉ ra schema của bảng `test_attempts`, `flashcards`

---

## 7. Rủi Ro & Phương Án Dự Phòng

| Rủi ro | Xác suất | Ảnh hưởng | Phương án dự phòng |
|-------|----------|-----------|-------------------|
| AI API (OCR/Speech) không ổn định trong buổi demo | Cao | Cao | Chuẩn bị mock response + video demo đã quay sẵn |
| Docker build thất bại trước buổi bảo vệ | Trung bình | Cao | Chạy bằng `mvn spring-boot:run` + `npm run dev` thủ công |
| DB mất dữ liệu seed trước demo | Thấp | Cao | Backup file `seed.sql` sẵn sàng chạy lại |
| Hội đồng hỏi tính năng chưa hoàn thiện | Trung bình | Trung bình | Trả lời thành thật: "Đây là hướng phát triển tương lai" |
| Thành viên vắng buổi bảo vệ | Rất thấp | Rất cao | Mọi thành viên đều hiểu toàn bộ hệ thống, không chỉ phần mình làm |

---

## 8. Definition of Done (DoD) — Tiêu Chí Hoàn Thành

Một tính năng được coi là **Done** khi:

- [ ] Code đã pass lint (`mvn spotless:check` / `npm run lint`)
- [ ] Unit test coverage ≥ 80% cho Service class liên quan
- [ ] Integration test: happy path + ít nhất 1 error path
- [ ] DTO Pattern: không có Entity trả ra API
- [ ] Soft Delete: không có `DELETE FROM` trong SQL
- [ ] JWT bảo vệ: endpoint yêu cầu auth đã được bảo vệ
- [ ] Log: dùng SLF4J, không có `System.out.println()`
- [ ] Không có TODO comments
- [ ] Swagger/OpenAPI documented
- [ ] Reviewer approve PR (≥ 1 người)

---

## 9. Tham Chiếu

| Tài liệu | Nội dung |
|---------|---------|
| [`constitution.md`](./constitution.md) | Tech stack, coding standards, git workflow, CI/CD |
| [`THESIS_DEFENSE_QNA.md`](./THESIS_DEFENSE_QNA.md) | Kịch bản & Q&A bảo vệ đồ án |
| [`../01-SRS-Requirements/use-cases/`](../01-SRS-Requirements/use-cases/) | Đặc tả 40 Use Case chi tiết |
| [`../01-SRS-Requirements/shared_context.md`](../01-SRS-Requirements/shared_context.md) | Ngữ cảnh chung: actors, glossary, business rules |
| [`skills/sql-performance.md`](./skills/sql-performance.md) | Hướng dẫn tối ưu SQL query |
| [`../../CLAUDE.md`](../../CLAUDE.md) | Kiến trúc, ADR, Lessons Learned |
| [`../../AGENTS.md`](../../AGENTS.md) | Domain rules, naming, forbidden patterns, DoD |
| [`../../database/init.sql`](../../database/init.sql) | Schema DB đầy đủ (v2.6) |
