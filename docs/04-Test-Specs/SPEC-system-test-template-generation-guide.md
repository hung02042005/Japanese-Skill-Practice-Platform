# SPEC — Hướng dẫn Generate System Test Document (Template3 — System Test.xlsx)

> **Nguồn**: `Guides  Templates-20260721/Template3_System Test.xlsx` (FPT University — System Test Report / Test Case Tracking template, 5 sheet: `Cover`, `Test Cases`, `Test Statistics`, `Workflow Name1`, `Workflow Name2`)
> **Lưu ý đặt tên trùng số**: Số "Template3" ở đây **khác** "Template3" nhắc tới trong [`SPEC-sds-template-generation-guide.md`](../02-SDD-Architecture/SPEC-sds-template-generation-guide.md) (file đó tham chiếu bộ template cũ `Temp_Document/Template3_SDS Document.docx`). Bộ template hiện tại nằm ở `Guides  Templates-20260721/` và đã được FPT đánh số lại: `Template1_SRS`, `Template2_SDS`, `Template3_System Test`, `Template4_ProjectTracking`, `Template5_AI Usage Report`, `Template6_Weekly Report`, `Template7_Project Presentation`, `Template8_Student Evaluation`. Spec này luôn nói "Template3" theo nghĩa **System Test.xlsx** của bộ mới.
> **Xem cùng**: [`SPEC-sds-template-generation-guide.md`](../02-SDD-Architecture/SPEC-sds-template-generation-guide.md), [`SPEC-issues-report-template-generation-guide.md`](../06-Management/SPEC-issues-report-template-generation-guide.md), [`TEST_SPEC_AUTH_API.md`](TEST_SPEC_AUTH_API.md) (ví dụ test case chi tiết đã có cho module Auth, dùng làm nguồn thật cho 1 Workflow sheet)
> **Đối tượng dùng**: Dev hoặc AI Agent cần lập/khớp bộ System Test Case + Test Report cho dự án này (nộp báo cáo test theo mẫu FPT, hoặc chuẩn bị UAT trước release).

---

## 1. Mục đích

Template3 gốc (`.xlsx`) là bảng theo dõi **test case hệ thống + kết quả test theo từng đợt (round)**, khác với `TEST_SPEC_AUTH_API.md` hiện có (đặc tả test case ở mức API/dev-test, không có cột tracking Pass/Fail/Pending theo round). Spec này quy định cách generate ra bản **Markdown đã điền test case thật** của dự án (không phải bảng mẫu rỗng với `Function A`/`<ID1>` placeholder), để:

- Có 1 bộ System Test Case bao phủ đúng 40 UC thật của dự án (không phải ví dụ minh hoạ "Company form" trong template gốc).
- Test case bám theo Normal Flow/Alternative Flow/Exception **đã đánh số thật** trong `use-cases/Bao_cao_dac_ta_Use_Case.md`, hoặc tái dùng trực tiếp ID đã có trong `TEST_SPEC_AUTH_API.md` khi trùng phạm vi (module Auth) — không tạo ra 2 bộ ID lệch nhau cho cùng 1 luồng.
- Cột trạng thái (Passed/Failed/Pending/N/A) phản ánh đúng thực tế: **mặc định Pending** cho tới khi có người chạy test thật, không tự điền Passed để "cho đẹp số liệu".

---

## 2. Cấu trúc gốc của Template3 (.xlsx)

| Sheet | Nội dung gốc (placeholder) |
|---|---|
| `Cover` | Tiêu đề "SYSTEM TEST REPORT DOCUMENT" + `Project Name` / `Project Code` / `Document Code` / `Creator` / `Issue Date` / `Version` + bảng `RECORD OF CHANGE` (`Effective Date`, `Version`, `Change Item`, `A/D/M`, `Change description`, `Reference`) |
| `Test Cases` | Bảng tổng hợp: `Project Name`, `Project Code`, `Test Environment Setup Description` (môi trường: Server/DB/Browser...) + bảng `No / Function Name / Sheet Name / Description / Pre-Condition` — mỗi dòng là 1 **Function** (UC), cột `Sheet Name` trỏ tới sheet `Workflow NameN` chứa test case chi tiết của function đó (nhiều Function có thể cùng trỏ 1 Workflow sheet) |
| `Test Statistics` | Bảng tổng hợp kết quả: `Project Name/Code/Document Code`, `Creator`, `Reviewer/Approver`, `Issue Date`, `Notes` (module nào nằm trong release nào) + bảng `No / Module code / Passed / Failed / Pending / N/A / Number of test cases`, dòng `Sub total`, `Test coverage %`, `Test successful coverage %` |
| `Workflow NameN` (lặp lại, 1 sheet/module) | Header: `Workflow` (tên module) + `Test requirement` (mô tả ngắn) + `Number of TCs` + bảng thống kê theo `Testing Round` (`Round 1/2/3` × `Passed/Failed/Pending/N/A`) + bảng chi tiết cột `Test Case ID / Test Case Description / Test Case Procedure / Expected Results / Pre-conditions / Round 1 (status, Test date, Tester) / Round 2 (...) / Round 3 (...) / Note`, các dòng test case được **nhóm theo `Scenario X`** (dòng header phụ, không có dữ liệu cột khác) |

Đây là bộ khung bắt buộc giữ nguyên khi generate: 1 `Cover` + 1 `Test Cases` overview + 1 `Test Statistics` + N sheet `Workflow` (1 sheet / module nghiệp vụ).

---

## 3. Ánh xạ Sheet/Cột → nguồn dữ liệu thật trong repo

### 3.1 Cover

| Trường | Lấy dữ liệu thật ở đâu |
|---|---|
| `Project Name` | `Japanese Skill Practice Platform` — dùng thống nhất với `RDS-Japanese-Skill-Practice-Platform.md`, `SDS-Japanese-Skill-Practice-Platform.md`, `Final-Release-Document.md` |
| `Project Code` | Repo **chưa có mã dự án chính thức** nào được khai báo (không tìm thấy "Project Code" trong 3 tài liệu trên) — dùng `jlpt-platform` (tên trong `package.json` gốc) làm quy ước, và ghi rõ trong file là quy ước tự đặt, không phải mã chính thức do trường/khách hàng cấp |
| `Document Code` | `<Project Code>_System-Test_v<version>`, vd `jlpt-platform_System-Test_v1.0` |
| `Creator` | Tên người/agent thực hiện lần tạo/cập nhật này (theo đúng cách `Final-Release-Document.md § RECORD OF CHANGES` ghi `AI Agent (theo yêu cầu user)` khi AI tạo hộ) |
| `Issue Date` | Ngày tạo thật (`currentDate` của phiên làm việc), không bịa ngày |
| `Version` | `v1.0` cho lần khởi tạo đầu tiên, tăng dần khi cập nhật |
| `RECORD OF CHANGE` | 1 dòng cho lần tạo/sửa hiện tại, cùng format `Date / A-M-D / In charge / Change Description` như các tài liệu khác trong `docs/06-Management/` |

### 3.2 Test Cases (overview)

| Trường | Lấy dữ liệu thật ở đâu |
|---|---|
| `Test Environment Setup Description` | Môi trường thật của repo: Backend Spring Boot 3.x/Java 21 (`apps/backend/pom.xml`), MySQL 8 (`ADR-009`), Frontend React 18 (`apps/frontend/package.json`), trình duyệt test (Chrome/Edge bản mới nhất) — lấy version thật từ `pom.xml`/`package.json`, không đoán |
| `No / Function Name / Sheet Name / Description / Pre-Condition` | 1 dòng / UC, lấy từ bảng "2. BẢNG TỔNG HỢP USE CASE" trong [`Bao_cao_dac_ta_Use_Case.md`](../01-SRS-Requirements/use-cases/Bao_cao_dac_ta_Use_Case.md) (40 UC, cột `Function Name` = tên UC tiếng Anh trong bảng đó). Cột `Description` lấy từ mục "Mô tả" trong chi tiết UC tương ứng (§ "3. CHI TIẾT USE CASE..."). Cột `Pre-Condition` lấy từ mục "Điều kiện tiên quyết" của UC đó. Cột `Sheet Name` = tên Workflow sheet theo bảng nhóm ở Mục 3.4 dưới đây |

### 3.3 Test Statistics

| Trường | Lấy dữ liệu thật ở đâu |
|---|---|
| `Notes` | Ghi rõ release/sprint nào bao gồm những module nào — tham chiếu `Issues-Report-*.md` hoặc milestone thật nếu có, không tự bịa tên release |
| `No / Module code / Passed / Failed / Pending / N/A / Number of test cases` | 1 dòng / Workflow sheet (Mục 3.4). **Mặc định toàn bộ `Passed=0, Failed=0, Pending=<tổng số TC>, N/A=0`** cho tới khi có người thực sự chạy test — tuyệt đối không điền Passed khi chưa test thật (áp dụng cùng nguyên tắc "không bịa dữ liệu" như `SPEC-issues-report-template-generation-guide.md`) |
| `Sub total`, `Test coverage %`, `Test successful coverage %` | Giữ nguyên công thức gốc của Excel (`Test coverage = (Passed+Failed+Pending+N/A)/Number of TCs`, `Test successful coverage = Passed/Number of TCs`) — khi convert sang Markdown, tính lại bằng số liệu thật đã điền, không copy nguyên số mẫu |

### 3.4 Workflow NameN (1 sheet / module nghiệp vụ)

Repo tổ chức backend theo `feature/<package>` (xem `CLAUDE.md` — phần này đã lỗi thời trong `CLAUDE.md` root, thực tế là feature-based không phải layer-based). Đề xuất nhóm 40 UC thành các Workflow theo đúng ranh giới package/controller thật đã tồn tại (kiểm chứng bằng `find apps/backend/src/main/java -iname "*Controller.java"`):

| Workflow (đề xuất) | UC | Package/Controller thật tương ứng |
|---|---|---|
| Authentication & Account | UC-01, 02, 03, 04, 05, 18 | `feature/auth` (`AuthController`) |
| Kanji & Kana Learning | UC-07, 08 | `feature/student/kanji`, `feature/student/kana` |
| Grammar & Reading | UC-06, 14 | `feature/student/grammar`, `feature/student/reading` |
| Vocabulary & Dictionary | UC-09, 16 | `feature/learning` (`StudentVocabularyController`), `feature/dictionary` |
| Flashcard Learning (SRS) | UC-12 | `feature/flashcard` |
| Quiz & Mock Exam | UC-10, 11 | `feature/assessment` (`StudentAssessmentController`, `TestAttemptController`) |
| Speaking Practice | UC-13 | `feature/speaking` (`SpeakingController`) |
| Bookmark & Learning Progress | UC-17, 19 | `feature/student/progress` |
| Staff — Student Management | UC-21, 22, 23 | `feature/staffcontent/student` |
| Staff — Content Management | UC-24, 25, 26, 27, 28 | `feature/staffcontent/question`, `.../grammar`, `.../quiz`, `.../learning`, `.../exam` |
| Staff — Support & Notification | UC-29, 30 | `feature/support` (`StaffSupportController`), `feature/notification` |
| Staff — Grade Speaking Submission | UC-31 | `feature/support` (`StaffGradingController`) |
| Staff — Analytics | UC-32 | `feature/staffcontent/dashboard` |
| StaffManager — Content Review | UC-33, 34 | `feature/contentreview`, `feature/publishedcontent` |
| Admin — System | UC-35, 36, 37, 38, 39, 40 | `feature/admin` |

**2 UC không có controller/API thật tại thời điểm viết spec này**: `UC-15 Listening Practice` và `UC-20 AI Handwriting Practice` — không tìm thấy package/controller nào tương ứng (`find ... -iname "*listening*" -o -iname "*handwrit*"` không ra kết quả). Khi generate Workflow sheet cho 2 UC này, **không tự bịa test case cho API chưa tồn tại** — ghi `TODO: chưa có backend implementation cho UC-15/UC-20` và loại khỏi `Test Statistics` cho tới khi có API thật để test.

| Trường trong sheet | Lấy dữ liệu thật ở đâu |
|---|---|
| `Workflow`, `Test requirement` | Tên module ở bảng trên + mô tả ngắn tổng hợp từ mô tả các UC thuộc module đó |
| `Number of TCs` | Đếm thật **sau khi** viết xong bảng test case (không áng chừng trước) |
| Bảng `Testing Round` (Round 1/2/3 × Passed/Failed/Pending/N/A) | Mặc định toàn bộ Pending ở Round 1, để trống Round 2/3 cho tới khi thực sự test đợt đó |
| `Scenario X` (dòng nhóm) | 1 scenario / UC hoặc / nhóm luồng con của UC (Happy path / Validation error / Business error / Security) — **với module Auth, dùng thẳng cấu trúc nhóm đã có sẵn trong [`TEST_SPEC_AUTH_API.md`](TEST_SPEC_AUTH_API.md)** (vd nhóm theo endpoint: `POST /api/auth/login`, `POST /api/auth/register`...) |
| `Test Case ID` | **Với module Auth**: tái dùng nguyên ID đã có trong `TEST_SPEC_AUTH_API.md` (`LGN-01`, `REG-04`, `VFY-02`, `JWT-05`...) — không đặt ID mới trùng lặp. **Với module khác**: đặt ID theo quy ước `<mã viết tắt module>-<số thứ tự>` (vd `KANJI-01`, `FLASH-01`), tự nhất quán trong sheet đó |
| `Test Case Description` | Với Auth: cột "Tên test case" trong `TEST_SPEC_AUTH_API.md`. Với module khác: diễn giải ngắn 1 câu, mô tả *cái gì* được test (không lặp lại Procedure) |
| `Test Case Procedure` | Với Auth: chuyển từ cột "Input" (API-level) sang bước thao tác **trên UI thật** khi test bằng tay (vd "Mở trang `/login`, nhập email `student@test.com`, mật khẩu `Student@123`, bấm Đăng nhập") hoặc giữ dạng gọi API trực tiếp (Postman) nếu system test này là API-level — chọn 1 trong 2 cách và nói rõ trong đầu file, không trộn lẫn không nhất quán trong cùng 1 sheet. Với module khác: dựng từ **Normal Flow/Alternative Flow đã đánh số** trong `Bao_cao_dac_ta_Use_Case.md` (hoặc `RDS-User-Login.md` nếu UC đó đã được nâng cấp rigor) — không tự bịa bước không có trong luồng đặc tả |
| `Expected Results` | Với Auth: cột "Expected response"/"Expected errorCode". Với module khác: suy từ Postcondition của UC + `errorCode` thật ném ra trong code (`grep` Exception class liên quan) |
| `Pre-conditions` | Với Auth: bảng "Cấu hình test data mặc định" trong `TEST_SPEC_AUTH_API.md § 1` (tài khoản `student@test.com`/`staff@test.com`/`admin@test.com`...). Với module khác: điều kiện tiên quyết của UC + trạng thái dữ liệu cần setup trước (vd "học viên đã có subscription VIP", theo `LESSON-003` trong `CLAUDE.md`) |
| `Round 1/2/3` (status/Test date/Tester) | Để `Pending` + để trống ngày/tester cho tới khi có người thật sự chạy — **không điền sẵn Passed/tên tester giả** |
| `Note` | Ghi chú riêng nếu có (vd link bug ticket khi Failed) |

---

## 4. Quy ước đặt tên & vị trí file

- **Vị trí**: `docs/04-Test-Specs/System-Test-<phạm-vi>.md`
  - Bộ đầy đủ toàn hệ thống (tất cả module): `System-Test-Japanese-Skill-Practice-Platform.md`
  - Theo từng đợt release/sprint (nếu tách nhỏ để dễ track theo tiến độ): `System-Test-<yyyy-mm-dd>.md` hoặc `System-Test-iter<N>.md`, cùng quy ước với `Issues-Report-*.md`
- **Không tạo `.xlsx`** trừ khi cần nộp bản chính thức cho giảng viên/khách hàng — mặc định chỉ tạo `.md` (dễ diff/review qua PR); nếu cần `.xlsx` để nộp, convert từ `.md` ở bước cuối cùng (không soạn tay 2 bản song song, tránh lệch dữ liệu).
- Mỗi Workflow trong Mục 3.4 là **1 heading `##`** trong cùng 1 file `.md` (không tách file riêng cho mỗi Workflow) — giữ đúng cấu trúc "1 file tổng, N sheet con" như bản `.xlsx` gốc.
- Đầu file luôn có `RECORD OF CHANGES` + link nguồn tới `Bao_cao_dac_ta_Use_Case.md` và `TEST_SPEC_AUTH_API.md`, giống cách các SPEC khác dẫn nguồn.

---

## 5. Quy trình từng bước để generate 1 System Test document mới

1. **Xác định phạm vi**: toàn hệ thống (15 Workflow ở Mục 3.4) hay chỉ 1 vài module đang cần test (vd chuẩn bị release 1 tính năng mới)? Nếu phạm vi lớn, chia theo đợt 3-4 Workflow/lượt (theo đúng cách `docx-report` skill xử lý batch lớn), không cố làm 1 lượt.
2. **Viết `Cover` + `Test Cases` overview** trước (Mục 3.1, 3.2) — đây là bảng tổng hợp cần đúng trước khi vào chi tiết từng Workflow.
3. **Với module Auth**: chuyển thẳng nội dung từ `TEST_SPEC_AUTH_API.md` sang format Workflow sheet (Mục 3.4) — đây là module duy nhất đã có sẵn test case chi tiết thật, ưu tiên làm trước để có 1 ví dụ đầy đủ tham chiếu cho các module khác.
4. **Với các module còn lại**: với mỗi UC thuộc Workflow đó, đọc kỹ Normal Flow/Alternative Flow/Exception trong `Bao_cao_dac_ta_Use_Case.md`, đối chiếu controller/service thật (`grep -n "public \|@PreAuthorize\|throw new"` trên class tương ứng) để biết chính xác input hợp lệ/không hợp lệ, mã lỗi thật ném ra — không suy đoán field/lỗi chưa tồn tại trong code.
5. **Nhóm test case thành Scenario** theo luồng (Happy path / Validation / Business rule / Authorization) — tối thiểu 1 scenario "happy path" + 1 scenario lỗi cho mỗi UC có input từ người dùng.
6. **Điền `Test Statistics`** sau khi biết tổng số TC thật mỗi Workflow — toàn bộ để `Pending`, tính `Test coverage`/`Test successful coverage` theo công thức ở Mục 3.3.
7. **Review lại**: mỗi `Test Case ID` không trùng giữa các Workflow, mỗi UC trong bảng `Test Cases` overview trỏ đúng `Sheet Name` (Workflow) đã viết, không còn Workflow nào bị bỏ sót so với bảng Mục 3.4.

---

## 6. Checklist trước khi coi là hoàn thành

- [ ] `Cover` có đủ `Project Name/Code/Document Code/Creator/Issue Date/Version` + 1 dòng `RECORD OF CHANGE` cho lần tạo này.
- [ ] `Test Cases` overview liệt kê đủ 40 UC (hoặc đúng phạm vi đã chọn ở Bước 1), cột `Sheet Name` khớp đúng tên Workflow đã viết.
- [ ] `Test Statistics` có 1 dòng/Workflow, số liệu mặc định Pending (không bịa Passed khi chưa test thật), công thức coverage tính đúng theo số liệu đã điền.
- [ ] Mỗi Workflow có đủ: `Test requirement`, `Number of TCs` (đếm thật), bảng `Testing Round`, bảng test case nhóm theo `Scenario`.
- [ ] Module Auth tái dùng đúng ID đã có trong `TEST_SPEC_AUTH_API.md`, không tạo ID trùng lặp mới cho cùng 1 test case.
- [ ] `Test Case Procedure`/`Expected Results` dựng từ Normal/Alternative/Exception Flow thật trong `Bao_cao_dac_ta_Use_Case.md` hoặc code thật — không bịa bước/kết quả không có nguồn.
- [ ] UC-15 (Listening) và UC-20 (AI Handwriting) được đánh dấu `TODO: chưa có backend implementation` thay vì viết test case cho API không tồn tại.
- [ ] Không còn placeholder gốc kiểu `Function A`, `<ID1>`, `<Workflow Name1>` sót lại từ template.
- [ ] File đặt đúng `docs/04-Test-Specs/`, đặt tên theo Mục 4.

---

## 7. Ghi chú tình trạng hiện tại của repo

*(Cập nhật lần đầu khi tạo spec này)*

- Repo hiện có `TEST_SPEC_AUTH_API.md` (test case chi tiết mức API cho toàn bộ module Auth: 18 nhóm endpoint, ~100 test case) và một bộ test tự động JUnit khiêm tốn hơn (`AuthControllerIntegrationTest`, `AuthenticationServiceTest`, `MockExamServiceTest`, `AssessmentControllerIntegrationTest`, `MockExamControllerIntegrationTest`, `AdminUserServiceTest`, `AdminUserServiceChangeRoleTest`, `ManagerDeletedContentServiceTest`, `CourseServiceTest`, `EmailServiceTest`, `JwtProviderTest`) — chỉ phủ 5/15 Workflow ở Mục 3.4 (Auth, Quiz & Mock Exam, Admin, StaffManager Content Review, Grammar/Reading một phần).
- **Chưa có bản System Test theo đúng format Template3** cho bất kỳ module nào — spec này là bước đầu tiên, `TEST_SPEC_AUTH_API.md` (Bước 3 ở Mục 5) là nguồn thật gần nhất để bắt đầu.
- **UC-15 (Listening Practice)** và **UC-20 (AI Handwriting Practice)** chưa có controller/package backend tương ứng tại thời điểm viết spec này — cần xác nhận lại với team trước khi lên kế hoạch test (có thể đang ở giai đoạn thiết kế/chưa triển khai).
- Chưa có "Project Code" chính thức nào được cấp cho dự án trong toàn bộ tài liệu hiện có — Mục 3.1 dùng `jlpt-platform` làm quy ước tạm, cần team xác nhận nếu trường/khách hàng có yêu cầu mã khác.
