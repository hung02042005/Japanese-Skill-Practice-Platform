# SPEC — Hướng dẫn Generate Final Release Document (Template5) dạng Markdown

> **Nguồn**: `Temp_Document/Template5_Final Release Document.pdf` (FPT University template)
> **Xem cùng**: [`SPEC-sds-template-generation-guide.md`](../02-SDD-Architecture/SPEC-sds-template-generation-guide.md) (Template3/SDS), [`SPEC-issues-report-template-generation-guide.md`](SPEC-issues-report-template-generation-guide.md) (Template4/Issues Report)
> **Đối tượng dùng**: Dev hoặc AI Agent cần soạn tài liệu bàn giao/nộp bài cuối kỳ (Final Release) cho dự án này.

---

## 1. Mục đích

Template5 gốc là tài liệu **bàn giao cuối cùng** (nộp bảo vệ đồ án / release cho khách hàng): liệt kê toàn bộ deliverable, hướng dẫn cài đặt, và user manual theo từng workflow thực tế của ứng dụng. Spec này quy định cách generate `.md` **trỏ đúng vào deliverable thật đã có trong repo** (không liệt kê file chưa tồn tại, không tự tạo link YouTube/tag giả), vì:

- Nhiều deliverable Template5 yêu cầu (SRS final, SDS final, Issues Report) **đã được tạo ở các bước trước** trong dự án này (theo `SPEC-sds-template-generation-guide.md` và `SPEC-issues-report-template-generation-guide.md`) — chỉ cần trỏ link, không viết lại.
- Có deliverable Template5 yêu cầu nhưng **repo hiện chưa có** (git tag đã đóng gói, video demo) — spec phải nêu rõ cách tạo thật thay vì bịa link.
- User Manual bắt buộc có **screenshot thật** từ ứng dụng đang chạy — không được mô tả suông hoặc dùng ảnh placeholder.

---

## 2. Cấu trúc gốc của Template5 (.docx/.pdf)

| # | Mục | Nội dung gốc (placeholder) |
|---|---|---|
| — | Cover page | `<<PROJECT NAME>>` + "Final Release Document" |
| — | Table of Contents | Tự động theo heading |
| I | Deliverable Package | Bảng `No / File / Notes` (DB script, SRS final, SDS final, Product Backlog, Issues Report...) + link tag source code + link video demo |
| II | Installation Guides | Hướng dẫn cài đặt & cấu hình để chạy được source code |
| III | User Manual → 1. Overview | Mô tả tổng quan ứng dụng + tóm tắt các workflow |
| III | User Manual → 2. `<<Workflow Name N>>` | Mục đích workflow + hướng dẫn từng bước kèm screenshot thật, lặp lại cho mỗi workflow |

---

## 3. Ánh xạ mục → nguồn dữ liệu thật trong repo

### 3.1. Bảng Deliverable Package

| Dòng gốc Template5 | Deliverable thật trong repo này | Trạng thái |
|---|---|---|
| `XYZ_DB_final.sql` | Không dùng 1 file SQL dump duy nhất — dự án dùng **Flyway migration** (`apps/backend/src/main/resources/db/migration/V1__*.sql` → `V28__*.sql`). Trỏ tới thư mục migration thay vì 1 file `_final.sql` | ✅ Có sẵn |
| `XYZ_SRS_final.docx` | [`docs/01-SRS-Requirements/SRS-Japanese-Skill-Practice-Platform.md`](../01-SRS-Requirements/SRS-Japanese-Skill-Practice-Platform.md) (+ bản `.docx`/`-EN.md` cùng thư mục) | ✅ Có sẵn |
| `XYZ_SDS_final.docx` | [`docs/02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md`](../02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md) (generate theo `SPEC-sds-template-generation-guide.md`) | ✅ Có sẵn |
| `XYZ_Final Product Backlog.xlsx` | Chưa có file riêng đúng format Template1 (Project Tracking) cho toàn dự án — `docs/06-Management/plan.md` hiện đang giữ vai trò tương đương (bảng module × status) nhưng chưa đủ cột `Planned/Actual iteration`, `link SRS/SDS` theo yêu cầu Template5 | ⚠️ Cần bổ sung — xem Mục 7 |
| `XYZ_Issues Report.xlsx` | [`docs/06-Management/Issues-Report-2026-07-15.md`](Issues-Report-2026-07-15.md) (generate theo `SPEC-issues-report-template-generation-guide.md`) | ✅ Có sẵn (nhưng mới cover 7 PR đầu, cần cập nhật tiếp tới thời điểm release) |
| Tagged source code | Repo **chưa có git tag nào** (`git tag -l` rỗng tại thời điểm viết spec) | ❌ Chưa có — tạo bằng `git tag -a vX.Y.Z -m "..."` rồi `git push origin vX.Y.Z` khi thực sự release, không tự bịa link |
| Demo video | Chưa có video nào được ghi nhận trong repo | ❌ Chưa có — không tự tạo link YouTube giả; để trống + ghi chú "TODO: quay & upload" |

### 3.2. Installation Guides

Không viết lại từ đầu — dự án **đã có** tài liệu cài đặt/triển khai chi tiết ở `docs/05-Deployment/`:

| Nội dung cần | Tài liệu thật đã có |
|---|---|
| Chạy local bằng Docker Compose | [`docker-compose.yml`](../../docker-compose.yml) + [`docs/05-Deployment/Docker_Cheatsheet.md`](../05-Deployment/Docker_Cheatsheet.md) |
| Biến môi trường cần cấu hình | [`.env.example`](../../.env.example), [`apps/backend/.env.example`](../../apps/backend/.env.example) |
| Build & chạy backend (Spring Boot/Maven) | [`apps/backend/pom.xml`](../../apps/backend/pom.xml) — lệnh chuẩn: `mvn clean install`, `mvn spring-boot:run` (theo `CLAUDE.md § DEVELOPMENT WORKFLOW`) |
| Build & chạy frontend (React) | `apps/frontend/package.json` — lệnh chuẩn: `npm install`, `npm run dev`/`npm run build` |
| Triển khai production/staging | [`docs/05-Deployment/CloudFly_VPS_Deployment_Guide.md`](../05-Deployment/CloudFly_VPS_Deployment_Guide.md), [`docs/05-Deployment/Deploy_Diagram.md`](../05-Deployment/Deploy_Diagram.md) |

→ Mục `II. Installation Guides` của Final Release Document chỉ nên là **bản tóm tắt điều hướng** (link tới các file trên), không copy nguyên văn — tránh 2 nguồn sự thật lệch nhau khi 1 bên cập nhật mà bên kia không.

### 3.3. User Manual — Overview + Workflow

`Workflow Name N` phải là **luồng nghiệp vụ thật, theo góc nhìn end-user** (không phải theo package code như SDS). Với dự án này, map theo vai trò (role) và luồng chính đã xác thực bằng code trong `SDS-Japanese-Skill-Practice-Platform.md`:

| Role | Workflow thật (ưu tiên viết trước) |
|---|---|
| Student | Đăng ký/Đăng nhập (kể cả Google OAuth) → Học Kanji/Kana/Vocab/Grammar → Luyện viết Kanji (canvas) → Ôn Flashcard (SRS) → Làm Quiz/Mock Test → Luyện nói (Shadowing) → Xem tiến độ/Dashboard |
| Staff | Soạn nội dung (câu hỏi/quiz/ngữ pháp/từ vựng) → Gửi duyệt → Chấm bài nói (UC-31) → Hỗ trợ ticket Student |
| Staff Manager | Duyệt hàng đợi nội dung (approve/reject/request changes) → Xem nội dung đã xoá mềm (deleted topics) |
| Admin | Quản lý user → Cấu hình hệ thống (`system_settings`) → Xem audit log → Dashboard thống kê |

Mỗi workflow **bắt buộc kèm screenshot thật** chụp từ ứng dụng đang chạy (`npm run dev` + backend chạy local, hoặc môi trường staging) — không dùng mô tả chay hoặc ảnh mockup từ SRS.

---

## 4. Quy ước đặt tên & vị trí file

- **Vị trí**: `docs/06-Management/Final-Release-Document.md` (file duy nhất — không đánh version vào tên, version nằm trong `RECORD OF CHANGES` bên trong file, theo đúng convention 2 file trước).
- Ảnh screenshot lưu tại `docs/06-Management/assets/final-release/<workflow-slug>/<số-thứ-tự>.png`, tham chiếu bằng path tương đối `![mô tả](assets/final-release/...)` — không nhúng base64 vào `.md`.
- Không tạo `.docx` song song trừ khi cần bản nộp chính thức; nếu cần, convert từ `.md` ở bước cuối (giữ 1 nguồn sự thật).

---

## 5. Quy trình từng bước để generate Final Release Document

1. **Kiểm tra deliverable đã có** theo bảng Mục 3.1 — deliverable nào chưa có (`Final Product Backlog`, git tag, video demo) thì báo cho user biết cần làm trước, **không tự bịa để lấp chỗ trống**.
2. **Mục I (Deliverable Package)**: liệt kê bảng `No / File / Notes` trỏ tới path thật trong repo (dùng markdown link tương đối), không dùng tên file kiểu `XYZ_...` — thay `XYZ` bằng mã dự án thật nếu nhóm có (vd `G?-JLPT`), nếu không có mã nhóm thì bỏ prefix, dùng thẳng tên file thật.
3. **Mục II (Installation Guides)**: viết bản tóm tắt điều hướng theo bảng Mục 3.2, kèm 2-3 lệnh chạy nhanh (`docker compose up`, hoặc `mvn spring-boot:run` + `npm run dev`) để dev mới đọc xong chạy được ngay, chi tiết đầy đủ vẫn để ở `docs/05-Deployment/`.
4. **Mục III.1 (Overview)**: tóm tắt 1 đoạn về hệ thống (domain, 3 role, AI module) — lấy từ `CLAUDE.md § TL;DR`, không viết lại từ đầu.
5. **Mục III.2+ (từng Workflow)**: với mỗi workflow ở bảng Mục 3.3:
   - Mở app thật (dùng skill `run` nếu có, hoặc `npm run dev` + backend local) và **tự tay đi qua workflow** để chụp screenshot thật.
   - Viết hướng dẫn từng bước bám theo màn hình thật (tên nút, tên trường nhập) — không suy đoán UI nếu chưa chạy thử.
   - Nếu workflow có business rule quan trọng (vd chấm điểm server-side, lock quiz khi có attempt), ghi chú ngắn kèm link sang mục tương ứng trong `SDS-Japanese-Skill-Practice-Platform.md` để người đọc kỹ thuật tra cứu thêm.
6. **Không tự tạo** link git tag hay video demo nếu chưa tồn tại — để `TODO` rõ ràng trong file, kèm lệnh cần chạy để tạo (Mục 3.1).

---

## 6. Checklist trước khi coi là hoàn thành

- [ ] Mục I liệt kê đúng deliverable thật đang có trong repo (không tên file `XYZ_...` gốc từ template).
- [ ] Deliverable nào chưa có (git tag, video, Product Backlog riêng) được đánh dấu rõ `TODO`, không có link giả.
- [ ] Mục II không copy nguyên văn `docs/05-Deployment/` — chỉ tóm tắt + link, tránh 2 nguồn sự thật.
- [ ] Mỗi Workflow trong Mục III có ít nhất 1 screenshot thật (không phải mockup từ SRS, không phải ảnh trống).
- [ ] Tên nút/trường nhập trong hướng dẫn khớp với UI thật tại thời điểm viết (đã tự chạy thử, không suy đoán).
- [ ] File đặt tại `docs/06-Management/Final-Release-Document.md`, ảnh đặt tại `docs/06-Management/assets/final-release/`.
- [ ] Có `RECORD OF CHANGES` ghi version + ngày mỗi lần cập nhật file (vì đây là tài liệu sẽ sửa nhiều lần tới sát ngày release).

---

## 7. Ghi chú tình trạng hiện tại của repo (tại thời điểm viết spec)

- **Chưa có git tag** nào (`git tag -l` rỗng) — cần tạo trước khi hoàn thiện Mục I.
- **Chưa có video demo**.
- **Chưa có file Product Backlog riêng** đúng format Template1 (Project Tracking) cho toàn dự án; `docs/06-Management/plan.md` có bảng trạng thái module nhưng thiếu cột `Planned/Actual iteration` và link SRS/SDS bắt buộc theo Template5 — cần bổ sung hoặc tạo `Final Product Backlog.md` riêng (generate theo `Temp_Document/Template1_Project Tracking.xlsx`, hiện chưa có spec guide tương ứng).
- **Chưa có screenshot thật** nào được chụp cho User Manual — bước này bắt buộc chạy ứng dụng thật trước khi viết Mục III, chưa thể tạo ngay trong phiên làm việc chỉ đọc code tĩnh.
