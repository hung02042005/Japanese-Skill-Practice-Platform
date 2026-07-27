# Japanese Skill Practice Platform

## Final Release Document

> Dựa trên `Temp_Document/Template5_Final Release Document.pdf` (FPT University template), generate theo [`SPEC-final-release-template-generation-guide.md`](SPEC-final-release-template-generation-guide.md).

---

## RECORD OF CHANGES

| Date | A*/M/D | In charge | Change Description |
|---|---|---|---|
| 2026-07-21 | A | AI Agent (theo yêu cầu user) | Khởi tạo Final Release Document từ Template5: Deliverable Package + Installation Guides (đủ, verify từ code thật), User Manual Overview + 8 workflow (chưa có screenshot thật — xem Mục "TODO trước khi release") |

*A - Added, M - Modified, D - Deleted*

---

## Table of Contents

- [I. Deliverable Package](#i-deliverable-package)
- [II. Installation Guides](#ii-installation-guides)
- [III. User Manual](#iii-user-manual)
  - [1. Overview](#1-overview)
  - [2. Đăng ký / Đăng nhập](#2-đăng-ký--đăng-nhập)
  - [3. Luyện viết Kanji (Student)](#3-luyện-viết-kanji-student)
  - [4. Ôn Flashcard — SRS (Student)](#4-ôn-flashcard--srs-student)
  - [5. Làm Quiz / Mock Test (Student)](#5-làm-quiz--mock-test-student)
  - [6. Luyện nói — Shadowing (Student)](#6-luyện-nói--shadowing-student)
  - [7. Soạn nội dung & Gửi duyệt (Staff)](#7-soạn-nội-dung--gửi-duyệt-staff)
  - [8. Chấm bài nói (Staff — UC-31)](#8-chấm-bài-nói-staff--uc-31)
  - [9. Duyệt nội dung (Staff Manager — UC-33)](#9-duyệt-nội-dung-staff-manager--uc-33)
- [TODO trước khi release chính thức](#todo-trước-khi-release-chính-thức)

---

## I. Deliverable Package

| No. | File | Notes |
|---|---|---|
| 1 | [`apps/backend/src/main/resources/db/migration/`](../../apps/backend/src/main/resources/db/migration/) (`V1__init_schema.sql` → `V28__seed_speaking_lessons.sql`) | Database — Flyway migration (cấu trúc bảng + seed data), thay cho 1 file `_final.sql` duy nhất vì dự án dùng versioned migration |
| 2 | [`docs/01-SRS-Requirements/SRS-Japanese-Skill-Practice-Platform.md`](../01-SRS-Requirements/SRS-Japanese-Skill-Practice-Platform.md) (+ bản `.docx`, `-EN.md` cùng thư mục) | Final SRS (Requirement) Document |
| 3 | [`docs/02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md`](../02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md) | Final SDS (Design) Document — 6 luồng: Auth, Quiz Submission, Flashcard SRS, Kanji Writing Evaluation, Speaking Grading, Content Review |
| 4 | *(TODO — xem Mục cuối)* | Final Product Backlog — chưa có file riêng đúng format Template1 (Project Tracking); `docs/06-Management/plan.md` hiện giữ vai trò tương đương nhưng thiếu cột `Planned/Actual iteration` + link SRS/SDS bắt buộc |
| 5 | [`docs/06-Management/Issues-Report-2026-07-15.md`](Issues-Report-2026-07-15.md) | Final issues tracking — hiện mới cover PR `#2`–`#8` (2026-06-28 → 2026-07-15), **cần cập nhật tiếp** tới sát ngày release thật |
| 6 | *(TODO — xem Mục cuối)* | Tagged source code — repo chưa có git tag nào tại thời điểm viết tài liệu này |
| 7 | *(TODO — xem Mục cuối)* | Demonstration video — chưa có |

> Dự án không dùng mã nhóm dạng `XYZ-*` (không tìm thấy quy ước này trong repo) nên bảng trên dùng thẳng đường dẫn file thật thay vì tiền tố `XYZ_`.

---

## II. Installation Guides

Chi tiết đầy đủ nằm ở [`docs/05-Deployment/`](../05-Deployment/) ([`README.md`](../05-Deployment/README.md), [`Docker_Cheatsheet.md`](../05-Deployment/Docker_Cheatsheet.md), [`CloudFly_VPS_Deployment_Guide.md`](../05-Deployment/CloudFly_VPS_Deployment_Guide.md)) — mục này chỉ tóm tắt đường chạy nhanh nhất để mở được source code.

### Chạy nhanh bằng Docker Compose (khuyến nghị)

```bash
cp .env.example .env               # điền giá trị thật (DB, JWT secret, SMTP...)
docker compose up -d               # services: db, redis, backend, frontend
```

`docker-compose.yml` định nghĩa 4 service chính: `db` (MySQL), `redis`, `backend` (Spring Boot), `frontend` (React qua Nginx).

### Chạy riêng từng phần (dev local)

**Backend** (Java 21 + Spring Boot 3.x, Maven):

```bash
cd apps/backend
cp .env.example .env                # hoặc cấu hình application-dev.yml theo GIT_RULES.md — không commit file có secret thật
mvn clean install
mvn spring-boot:run
```

**Frontend** (React 18 + Vite):

```bash
cd apps/frontend
npm install
npm run dev          # dev server (Vite)
npm run build        # build production
```

> Tuân thủ `GIT_RULES.md § 1`: không commit `.env`, `.env.local`; chỉ cập nhật biến mới vào `.env.example`.

---

## III. User Manual

### 1. Overview

**Japanese Skill Practice Platform** (JLPT E-Learning System) là hệ thống học & luyện thi tiếng Nhật JLPT (N5 → N1), tích hợp AI. Hệ thống có 3 vai trò:

- **Student**: học Kana/Kanji/Vocab/Grammar, luyện viết Kanji, ôn Flashcard (SRS), làm Quiz/Mock Test, luyện nói (Shadowing), theo dõi tiến độ.
- **Staff**: soạn nội dung học/câu hỏi/đề thi, gửi duyệt, chấm bài nói của Student, hỗ trợ ticket.
- **Staff Manager**: duyệt nội dung Staff gửi lên (approve/reject/request changes) trước khi công khai cho Student; xem/khôi phục nội dung đã xoá mềm.
- **Admin**: quản lý user, cấu hình hệ thống, xem audit log, dashboard thống kê *(workflow Admin chưa được viết chi tiết ở tài liệu này — xem Mục TODO)*.

Kiến trúc & chi tiết kỹ thuật của từng luồng dưới đây được đặc tả đầy đủ (class/sequence diagram, SQL) tại [`SDS-Japanese-Skill-Practice-Platform.md`](../02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md) — tài liệu này chỉ tập trung góc nhìn end-user.

---

### 2. Đăng ký / Đăng nhập

**Mục đích**: Student tạo tài khoản mới hoặc đăng nhập để truy cập nội dung học; Staff/Admin đăng nhập bằng tài khoản được cấp sẵn.

**Các bước (Student)**:

1. Vào trang chủ (`/`) → bấm vào liên kết đăng nhập, hoặc truy cập thẳng `/register` để đăng ký.
2. Tại trang **Đăng ký** (`/register`): nhập Họ tên (placeholder mẫu *"Nguyễn Văn A"*), Email (*"email@example.com"*), Mật khẩu (tối thiểu 8 ký tự, 1 chữ hoa, 1 chữ số), nhập lại mật khẩu → submit.
3. Hệ thống gửi email xác minh (bất đồng bộ) — Student mở email, bấm link xác minh (`/verify-email`).
4. Tại trang **Đăng nhập** (`/login`): nhập Email + Mật khẩu, hoặc dùng nút đăng nhập Google (OAuth) nếu có cấu hình.
5. Nếu quên mật khẩu, bấm liên kết trên trang Login để tới `/forgot-password` → nhập email nhận link reset (`/reset-password`).
6. Đăng nhập thành công → chuyển tới `/dashboard`; nếu là tài khoản mới, có thể được đưa qua `/onboarding` trước.

**Đăng nhập Staff/Admin**: dùng cùng trang `/login` (hệ thống tự nhận diện loại tài khoản theo email); Staff lần đầu nhận mật khẩu tạm phải đổi tại `/staff/change-temp-password`.

*(Screenshot: **TODO** — chưa chụp từ ứng dụng đang chạy)*

---

### 3. Luyện viết Kanji (Student)

**Mục đích**: Student luyện viết tay từng chữ Kanji trên canvas, hệ thống chấm độ giống nét vẽ theo thời gian thực (thuật toán DTW — xem `SDS § 4`).

**Các bước**:

1. Từ Dashboard hoặc menu, vào danh sách Kanji (`/kanji`).
2. Chọn 1 chữ Kanji → vào màn luyện viết (`/kanji/:id`).
3. Vẽ từng nét trên canvas theo đúng thứ tự; sau mỗi nét, hệ thống chấm ngay (`perfect`/`good`/`ok`/`bad`) và hiển thị phản hồi tức thời (không cần đợi vẽ xong cả chữ).
4. Sau khi hoàn thành đủ số nét, hệ thống lưu lại 1 lượt luyện tập (điểm DTW trung bình + chất lượng tổng) để Student xem lại lịch sử.

*(Screenshot: **TODO** — chưa chụp từ ứng dụng đang chạy)*

---

### 4. Ôn Flashcard — SRS (Student)

**Mục đích**: Ôn tập từ vựng theo thuật toán lặp lại ngắt quãng (SM-2), trộn thẻ MỚI và thẻ ĐẾN HẠN ÔN trong cùng 1 phiên.

**Các bước**:

1. Vào `/vocabulary` → chọn 1 sổ tay (deck) hoặc 1 chủ đề giáo trình → vào phiên ôn (`/vocabulary/flashcard`).
2. Với thẻ MỚI: lật thẻ xem nghĩa/furigana/câu ví dụ.
3. Với thẻ ÔN TẬP (trắc nghiệm): chọn nghĩa đúng trong 2 lựa chọn — hệ thống tự chấm đúng/sai (không tin đáp án client gửi lên).
4. Với thẻ lật tay (Kanji/Grammar/thẻ tự tạo): tự đánh giá mức độ nhớ (tương ứng `EASY`/`HARD`/`WRONG` ở backend) để hệ thống tính lại lịch ôn tiếp theo.
5. Cuối phiên, nếu có từ trả lời sai, hệ thống gợi ý bấm nút **"Thêm vào 'Từ cần ôn lại'"** để đưa các từ đó vào sổ ôn riêng.

*(Screenshot: **TODO** — chưa chụp từ ứng dụng đang chạy)*

---

### 5. Làm Quiz / Mock Test (Student)

**Mục đích**: Làm bài quiz theo chủ đề hoặc thi thử JLPT mock test, chấm điểm hoàn toàn ở server (không tin điểm từ client — xem `LESSON-005`/`SDS § 2`).

**Các bước**:

1. Vào `/quiz` (quiz theo chủ đề) hoặc `/mock-test` (danh sách đề thi thử).
2. Chọn 1 đề → bắt đầu làm bài (`/mock-test/:id/attempt`) — hệ thống tạo 1 lượt làm bài mới, tính thời gian còn lại theo `durationMin` của đề.
3. Trả lời từng câu, có thể chuyển qua lại giữa các câu trong thời gian cho phép.
4. Bấm nộp bài — hệ thống khoá lượt làm bài (chống nộp trùng), chấm điểm server-side, chuyển sang trang kết quả (`/mock-test/:id/results`).
5. Xem lại chi tiết từng câu đúng/sai sau khi nộp.

*(Screenshot: **TODO** — chưa chụp từ ứng dụng đang chạy)*

---

### 6. Luyện nói — Shadowing (Student)

**Mục đích**: Student luyện phát âm theo phương pháp Shadowing (nghe & nhại lại), ghi âm và nộp bài để chấm điểm.

**Các bước**:

1. Vào `/speaking` → chọn bài luyện nói.
2. Bước 1: nghe/đọc theo câu mẫu.
3. Bước 2 — **"Ghi âm giọng đọc của bạn"**: bấm ghi âm, đọc theo mẫu, dừng ghi âm.
4. Bấm nút **"Nộp bài →"** để gửi bản ghi âm lên hệ thống chấm điểm.
5. Sau khi có điểm (AI chấm trước, Staff có thể chấm lại thủ công — xem Mục 8), Student nhận thông báo kết quả tại `/notifications`.

*(Screenshot: **TODO** — chưa chụp từ ứng dụng đang chạy)*

---

### 7. Soạn nội dung & Gửi duyệt (Staff)

**Mục đích**: Staff tạo mới/chỉnh sửa nội dung học (câu hỏi, quiz, đề thi, ngữ pháp, từ vựng) ở trạng thái nháp, sau đó gửi cho Staff Manager duyệt trước khi công khai cho Student.

**Các bước**:

1. Đăng nhập bằng tài khoản Staff → vào `/staff` (Staff Dashboard).
2. Tuỳ loại nội dung, vào đúng trang quản lý: `/staff/content` (bài học/ngữ pháp/từ vựng), `/staff/questions` (ngân hàng câu hỏi), `/staff/assessments` (quiz/đề thi).
3. Tạo mới hoặc chỉnh sửa nội dung ở trạng thái nháp (`draft`).
4. Gửi nội dung để duyệt — nội dung chuyển sang trạng thái chờ duyệt (`pending_review`), xuất hiện trong hàng đợi của Staff Manager (Mục 9).
5. Nếu bị **"Từ chối"** hoặc yêu cầu sửa, nội dung quay lại trạng thái nháp/`rejected` kèm feedback của Manager để Staff chỉnh sửa lại.

*(Screenshot: **TODO** — chưa chụp từ ứng dụng đang chạy)*

---

### 8. Chấm bài nói (Staff — UC-31)

**Mục đích**: Staff xem lại các bài nói của Student đã được AI chấm điểm trước, và có thể ghi đè điểm/nhận xét thủ công.

**Các bước**:

1. Đăng nhập Staff → vào `/staff/grading`.
2. Danh sách hiển thị các bài nói đã qua AI chấm (`status = ai_graded`), kèm điểm AI (phát âm, lưu loát, gợi ý cải thiện).
3. Chọn 1 bài → nghe lại bản ghi âm, xem chi tiết điểm AI.
4. Nhập điểm chấm thủ công (bắt buộc nếu muốn giữ nguyên điểm AI, nhập lại đúng điểm đó) + nhận xét → lưu.
5. Sau khi lưu, Student nhận thông báo "Bài nói của bạn đã được chấm điểm".

> Chỉ chấm được bài `submission_type = SPEAKING` và đang ở trạng thái `AI_GRADED`; bài đã chấm rồi không chấm lại được lần 2 (xem `SDS § 5`).

*(Screenshot: **TODO** — chưa chụp từ ứng dụng đang chạy)*

---

### 9. Duyệt nội dung (Staff Manager — UC-33)

**Mục đích**: Staff Manager kiểm duyệt nội dung do Staff soạn trước khi công khai cho Student.

**Các bước**:

1. Đăng nhập bằng tài khoản Staff Manager → vào `/manager` (Manager Dashboard) → bấm mục **"Duyệt Nội Dung"**, hoặc vào thẳng `/manager/review-queue` — trang **"Hàng Đợi Duyệt"**.
2. Danh sách hiển thị nội dung Staff đã gửi (đang chờ duyệt), có thể lọc theo loại (bài học/câu hỏi/quiz/ngữ pháp/từ vựng...).
3. Xem chi tiết 1 nội dung → chọn 1 trong 3 hành động:
   - Bấm **"Duyệt"** → nội dung được công khai (`published`) cho Student.
   - Bấm **"Yêu cầu sửa"** → trả nội dung về cho Staff kèm feedback, không cần feedback bắt buộc như Từ chối.
   - Bấm **"Từ chối"** → bắt buộc nhập feedback, nội dung chuyển trạng thái `rejected`.
4. Manager không thể tự duyệt nội dung do chính mình tạo (hệ thống chặn — xem `SDS § 6`).
5. Xem nội dung đã bị xoá mềm tại `/manager/deleted-topics` nếu cần khôi phục.

*(Screenshot: **TODO** — chưa chụp từ ứng dụng đang chạy)*

---

## TODO trước khi release chính thức

- [ ] Chạy ứng dụng thật (local hoặc staging), chụp **screenshot thật** cho từng workflow ở Mục III và thay các dòng `(Screenshot: TODO...)` bằng ảnh thật (lưu tại `docs/06-Management/assets/final-release/<workflow-slug>/`).
- [ ] Viết thêm workflow cho **Admin** (quản lý user, cấu hình hệ thống, audit log, dashboard) — chưa có trong bản này.
- [ ] Tạo **git tag** cho bản release (`git tag -a vX.Y.Z -m "..."` rồi `git push origin vX.Y.Z`) và điền link vào Mục I dòng 6.
- [ ] Quay & upload **video demo**, điền link vào Mục I dòng 7.
- [ ] Tạo file **Final Product Backlog** riêng (theo `Temp_Document/Template1_Project Tracking.xlsx` — chưa có spec guide tương ứng, cần viết trước) và điền vào Mục I dòng 4.
- [ ] Cập nhật [`Issues-Report-2026-07-15.md`](Issues-Report-2026-07-15.md) tới sát ngày release (hiện chỉ cover PR `#2`–`#8`).
