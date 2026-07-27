# COMPLETE SYSTEM TEST SUITE — SAKUJI JLPT LEARNING PLATFORM

| Thuộc tính | Giá trị |
| :--- | :--- |
| Phiên bản | 3.1 |
| Ngày thiết kế | 26/07/2026 (v3.0) — hiệu chỉnh 26/07/2026 (v3.1) |
| Loại tài liệu | System Test Specification + Presentation Outline |
| Mục tiêu | Gần 100% functional coverage cho toàn bộ vai trò, route, API và workflow as-built |
| Nguồn yêu cầu | `docs/00-Tong-Quan-He-Thong.md` |
| Nguồn Use Case | `docs/01-SRS-Requirements/use-cases/Bao_cao_dac_ta_Use_Case.md` (40 UC — nguồn chuẩn cho mọi mã UC-01..UC-40 trong tài liệu này) |
| Nguồn Database | `docs/02-SDD-Architecture/database-design/JLPT_database.md` |
| Nguồn triển khai | `apps/frontend/src/App.jsx`, REST controllers, services, entities và Flyway migrations |
| Trạng thái | Test design hoàn chỉnh; chưa phải test execution report |

> “Covered” trong tài liệu này nghĩa là đã có test scenario được thiết kế và truy vết. Chỉ đánh dấu PASS/FAIL sau khi thực thi trên môi trường system test.

> **Change log v3.1:** Ma trận truy vết (Section 9) ở v3.0 đánh số UC-01..UC-40 không khớp thứ tự thật của `Bao_cao_dac_ta_Use_Case.md` (ví dụ UC-01 bị gán nhầm thành "Register" thay vì "User Login"). v3.1 đối chiếu lại từng UC theo đúng tên trong tài liệu đặc tả, đồng thời đối chiếu với mã nguồn hiện có để phát hiện 4 khoảng trống thật giữa đặc tả và as-built (UC-14, UC-15, UC-17 chưa triển khai; UC-32, UC-38 chỉ triển khai một phần). Bốn Test ID mới (STU-039–STU-041, STF-026, ADM-016) được thêm để xác nhận và theo dõi các khoảng trống này thay vì gán gượng ép vào Test ID không liên quan.

---

## PHẦN A — SYSTEM TEST SPECIFICATION

## 1. Mục tiêu và phạm vi

### 1.1. Mục tiêu

- Xác nhận toàn bộ 40 use case hoạt động đúng từ UI đến API và database.
- Xác nhận 5 nhóm tác nhân: Khách, Student, Staff, StaffManager và Admin.
- Bao phủ positive, negative, boundary, invalid input, authentication và authorization.
- Bao phủ mọi route React và mọi REST endpoint family hiện có.
- Kiểm tra workflow, CRUD, state transition, audit, concurrent access và error handling.
- Xác nhận các giới hạn triển khai được hiển thị/xử lý trung thực.
- Tạo regression baseline để tự động hóa bằng Playwright + REST client + MySQL assertions.

### 1.2. Trong phạm vi

- Frontend React trên Chrome/Firefox/Edge.
- Spring Boot REST API.
- MySQL 8.4 với Flyway migration.
- JWT access/refresh token.
- Email OTP/reset và `email_outbox`.
- Upload avatar/audio và media path.
- Google OAuth qua sandbox/test account.
- Desktop responsive và kiểm tra mobile cơ bản.

### 1.3. Ngoài phạm vi hoặc chỉ contract test

- Speech-recognition engine chưa được tích hợp: test contract submit/poll/manual grading.
- Redis chưa được backend sử dụng: chỉ smoke container, không kỳ vọng cache/session.
- Reading/Listening độc lập, export PDF/CSV/Excel và bảng `courses` không tồn tại.
- Penetration test chuyên sâu và load test quy mô production cần kế hoạch riêng.

---

## 2. Tác nhân, tính năng và quyền

| Tác nhân | Tính năng được phép | Khu vực bị cấm |
| :--- | :--- | :--- |
| Khách | Trang công khai; đăng ký; verify; login; forgot/reset; Staff setup/reset | Tất cả route/API protected |
| Student | Dashboard, onboarding, profile, lesson, Kana, Kanji, vocabulary, grammar, quiz, mock test, flashcard, notebook, dictionary, speaking, progress, support, notification | `/staff/**`, `/manager/**`, `/admin/**` |
| Staff | Dashboard Staff; Student support/status; content/question/quiz/exam authoring; submit review; ticket; broadcast; speaking grading | Student private data ngoài nghiệp vụ; Manager review/publish; Admin |
| StaffManager | Quyền Staff; review; publish/archive/restore; assign ticket; broadcast | Admin settings/users; tự duyệt nội dung của mình |
| Admin | Dashboard, user lifecycle/role, settings, SMTP test, audit, notification-rule API | Student learning data theo cách giả mạo Student; Staff authoring nếu không có Staff authority |

---

## 3. Chiến lược và mức kiểm thử

| Lớp | Công cụ đề xuất | Mục tiêu |
| :--- | :--- | :--- |
| Browser E2E | Playwright | Route, menu, button, form, loading/error/empty, redirect |
| API system | REST Assured/Newman | HTTP contract, validation, auth, concurrency, idempotency |
| Database | MySQL client/Testcontainers | FK/check/unique, state, audit, soft delete, outbox |
| Security | OWASP ZAP + scripted cases | JWT manipulation, injection, XSS, upload, CORS |
| Email | MailHog/WireMock SMTP | OTP/reset/template/retry/outbox |
| File | Temporary upload storage | Type/size/path/traversal and cleanup |
| Compatibility | Playwright projects | Chrome, Firefox, Edge và responsive viewport |

### 3.1. Môi trường chuẩn

- MySQL 8.4, charset `utf8mb4`, timezone UTC.
- Backend chạy profile system-test, Flyway từ database rỗng.
- Frontend production build phục vụ qua HTTP.
- SMTP sandbox; Google OAuth test client.
- Clock có thể điều khiển để test expiry/schedule.
- Mỗi test độc lập dữ liệu hoặc rollback/reset bằng API/fixture an toàn.

### 3.2. Bộ dữ liệu chuẩn

| Mã | Dữ liệu |
| :--- | :--- |
| `U-STU-A` | Student active, verified, N5 |
| `U-STU-P` | Student pending verification |
| `U-STU-S` | Student suspended |
| `U-STA-A` | Staff active |
| `U-MGR-A` | StaffManager active |
| `U-ADM-A` | Admin active |
| `PWD-OK` | Mật khẩu hợp lệ dài 12 ký tự, có hoa/thường/số/ký tự |
| `JWT-X` | Token expired/malformed/wrong-signature |
| `JP-TEXT` | `日本語を勉強します 🌸` |
| `FILE-AUDIO` | WAV/MP3 hợp lệ trong giới hạn |
| `FILE-BAD` | EXE đổi đuôi WAV hoặc file vượt giới hạn |
| `SQL-XSS` | `' OR 1=1 --`, `<script>alert(1)</script>` |

---

## 4. Tiêu chí vào/ra

### 4.1. Entry criteria

- Build frontend/backend thành công.
- Flyway migrate database rỗng thành công.
- Test accounts và seed tối thiểu sẵn sàng.
- SMTP/OAuth/file test doubles hoạt động.
- Không còn blocker deployment.

### 4.2. Exit criteria

- 100% test P0/P1 đã chạy; không còn lỗi Critical/High mở.
- 100% requirement/role/route/API family có Test ID.
- Functional pass rate ≥ 98%; phần còn lại có risk acceptance.
- Không có permission bypass, cross-account data leak hoặc token vulnerability mức High.
- Audit và database invariants đạt 100%.
- Regression suite tự động chạy ổn định ít nhất ba lần liên tiếp.

---

## 5. Detailed System Test Cases

Quy ước Priority: `P0` critical, `P1` high, `P2` medium, `P3` low.

### 5.1. Public và Authentication

| Test ID | Role | Module | Feature | Preconditions | Test Steps | Test Data | Expected Result | Priority | Type |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| AUTH-001 | Khách | Public | Landing/menu | App running | Mở `/`; kiểm tra logo, menu, CTA; đi tới Features/Blog/Login/Register | N/A | Trang render không lỗi; menu/CTA điều hướng đúng; không lộ menu protected | P1 | Smoke, UI |
| AUTH-002 | Khách | Auth | Register positive | Email chưa tồn tại | Mở Register; nhập hợp lệ; submit | email mới, `PWD-OK`, `JP-TEXT` name | API 201/200; Student `pending`; email OTP được gửi; UI chuyển verify | P0 | Functional, Integration |
| AUTH-003 | Khách | Auth | Register invalid/boundary | None | Submit rỗng; email sai; password dưới/đúng/trên giới hạn; name Unicode dài | invalid/boundary set | Client và server cùng chặn; field message rõ; không tạo DB row | P1 | Negative, Boundary |
| AUTH-004 | Khách | Auth | Register duplicate/race | Email đã có | Gửi hai register đồng thời cùng email | same email | Chỉ một account; request còn lại 409/validation; không duplicate | P0 | Concurrent, Database |
| AUTH-005 | Khách | Auth | Verify OTP positive | `U-STU-P`, OTP còn hạn | Mở `/verify-email`; nhập OTP; submit | OTP đúng 6 số | `email_verified_at` được set; token không tái sử dụng; có thể login | P0 | Functional |
| AUTH-006 | Khách | Auth | Verify/resend negative | Pending account | Nhập OTP sai, hết hạn, đã dùng; resend liên tục | invalid OTP | Không verify; thông báo an toàn; rate/expiry được tôn trọng; OTP mới vô hiệu OTP cũ nếu thiết kế | P1 | Negative, Security |
| AUTH-007 | Mọi role | Auth | Login positive | Account active | Login lần lượt Student/Staff/Manager/Admin | valid credentials | Token đúng actor/role; redirect `/dashboard`, `/staff`, `/manager`, `/admin` | P0 | Smoke, Functional |
| AUTH-008 | Mọi role | Auth | Login invalid | Account exists | Sai password; email lạ; rỗng; SQL/XSS | invalid set | 401/validation; message không tiết lộ account; không login | P0 | Negative, Security |
| AUTH-009 | Mọi role | Auth | Lock/suspend/deleted | Account locked/suspended/deleted | Login | accounts by state | Bị từ chối; không cấp token; lý do phù hợp không lộ dữ liệu nhạy cảm | P0 | Permission, Security |
| AUTH-010 | Staff | Staff Auth | Setup password | Valid setup token | Mở setup; nhập/confirm password; submit | `PWD-OK` | Password hash lưu; token revoked; login Staff thành công | P0 | Functional |
| AUTH-011 | Staff | Staff Auth | Temp-password workflow | Admin issued temp password | Login temp; thử truy cập Staff page; đổi password | temp + new | Chỉ limited session; buộc đổi; sau đổi temp password hết hiệu lực | P0 | Security, Integration |
| AUTH-012 | Staff | Staff Auth | Forgot request | Active Staff | Submit forgot nhiều lần | staff email | Reset request pending duy nhất hợp lệ; Admin thấy request; chống account enumeration | P1 | Functional, Security |
| AUTH-013 | Khách | Auth | Forgot/reset Student | Verified Student | Request reset; dùng token; login password mới | valid token | Email gửi; password đổi; reset token one-time; phiên cũ xử lý theo policy | P0 | Functional |
| AUTH-014 | Khách | Auth | Reset invalid | Reset token invalid/expired | Submit password mới | expired/tampered token | Không đổi password; 400/401; audit/log không chứa token/password | P1 | Negative, Security |
| AUTH-015 | Student | Auth | Google OAuth | OAuth test account | Login Google lần đầu và lần sau | valid Google token | Account link/create đúng; unique provider ID; JWT Student được cấp | P1 | Integration |
| AUTH-016 | Khách | Auth | Google token attack | None | Gửi fake token/wrong audience/email collision | forged token | 401/409; không takeover/link sai account | P0 | Security |
| AUTH-017 | Mọi role | Session | Refresh positive | Valid refresh token | Hết access; gọi protected API; refresh; retry | valid refresh | Access mới hợp lệ; request retry thành công; refresh actor không đổi | P0 | Functional |
| AUTH-018 | Mọi role | Session | Refresh invalid/replay | Expired/revoked/wrong-signature | Gọi refresh đồng thời/replay | `JWT-X` | 401; không cấp token; rotation/revocation theo code; không đổi role | P0 | Security, Concurrent |
| AUTH-019 | Mọi role | Session | Logout | Logged in | Logout; dùng lại access/refresh | tokens | Refresh/session bị revoke theo policy; UI xóa session và về Login | P0 | Functional, Security |
| AUTH-020 | Mọi role | Session | Timeout | Session gần hết hạn | Điều khiển clock qua expiry; thao tác UI/API | expired access/refresh | 401; refresh nếu còn hạn, nếu không thì login; không mất form ngoài thông báo hợp lý | P0 | Security, UI |
| AUTH-021 | Mọi role | Security | JWT manipulation | Any valid token | Sửa role/sub/expiry; đổi alg; dùng token actor khác | tampered tokens | Tất cả bị 401/403; không truy cập hoặc ghi DB | P0 | Security |
| AUTH-022 | Khách | Route Guard | Direct protected URL | Logged out | Mở từng route protected bằng URL | route inventory | Redirect Login/403; không render dữ liệu protected trước redirect | P0 | Permission, UI |

### 5.2. Student

| Test ID | Role | Module | Feature | Preconditions | Test Steps | Test Data | Expected Result | Priority | Type |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| STU-001 | Student | Onboarding | Save goals | `U-STU-A`, chưa onboarding | Chọn goal/minutes/skills; submit; reload | N5, 30 phút | Dữ liệu lưu; dashboard phản ánh; không tạo trùng | P1 | Functional, Integration |
| STU-002 | Student | Onboarding | Invalid/boundary | Logged in | Gửi goal sai, minutes 0/âm/rất lớn, skills lạ; bypass UI | invalid set | 400; không cập nhật DB | P1 | Negative, API |
| STU-003 | Student | Dashboard | Load summary | Có/không có activity | Mở `/dashboard`; kiểm tra stats và next lesson | seeded activity | Loading→data; số liệu đúng view/query; empty state đúng | P0 | Smoke, UI, Database |
| STU-004 | Student | Profile | View/update | Logged in | Mở profile; sửa name/phone Unicode; reload | boundary values | GET/PUT đúng account; DB update; validation chiều dài/phone | P1 | Functional |
| STU-005 | Student | Profile | Avatar upload | Logged in | Upload ảnh hợp lệ; sau đó file xấu/quá lớn/path traversal | image + `FILE-BAD` | Ảnh hợp lệ hiển thị; file xấu 400/415; không thực thi/ghi ngoài storage | P0 | Security, Integration |
| STU-006 | Student | Profile | Change password | Logged in | Sai current; mismatch; password yếu; hợp lệ | password set | Chỉ hợp lệ mới đổi; hash bcrypt; mật khẩu không log/response | P0 | Security |
| STU-007 | Student | Profile | Change email OTP | Logged in | Request OTP; confirm đúng; thử sai/hết hạn/email trùng | email set | Email đổi sau OTP; unique giữ nguyên; token one-time; audit phù hợp | P0 | Functional, Security |
| STU-008 | Student | Learning | Course/JLPT list | Published content exists | Mở `/courses`; chọn level | N5–N1 | Danh sách lộ trình đúng; không phụ thuộc bảng `courses`; UI responsive | P1 | Functional, UI |
| STU-009 | Student | Lesson | Lesson detail/progress | Published lesson | Mở `/lessons/:id`; hoàn thành | valid lesson | Nội dung/media đúng; progress upsert unique và 100%; next lesson đổi | P0 | Functional, Integration |
| STU-010 | Student | Lesson | Hidden/missing lesson | Draft/deleted/unknown ID | Direct URL/API | IDs | 403/404; không lộ content draft/deleted | P0 | Permission |
| STU-011 | Student | Kana | List/progress | Kana seed exists | Mở `/kana`; lọc/tương tác; đánh dấu progress | Hiragana/Katakana | Đủ dữ liệu, audio fallback; progress content_type=kana | P1 | Functional, UI |
| STU-012 | Student | Vocabulary | Home/topics/list | Published vocab | Mở vocab; đổi level/topic/search/page | JP query, N5 | Filter/pagination đúng; Unicode search; empty/loading/error đúng | P0 | Functional, UI |
| STU-013 | Student | Vocabulary | Invalid filters | Logged in | level lạ, page âm, size 0/rất lớn, SQL/XSS | invalid set | 400 hoặc safe defaults; không injection/XSS | P1 | Negative, Security |
| STU-014 | Student | Grammar | List/detail | Published grammar | Mở `/grammar`; filter; detail | N5 | Chỉ published; dữ liệu/Unicode đúng; 404 unknown | P1 | Functional |
| STU-015 | Student | Kanji | List/detail | Published Kanji | Mở list; pagination; detail | N5 Kanji | Meaning/readings/stroke info đúng; filter/boundary đúng | P0 | Functional |
| STU-016 | Student | Kanji Writing | Evaluate stroke | Detail open | Vẽ path đúng/sai/ngắn; submit stroke | valid + short + extreme coords | DTW score/quality/direction hợp lệ; path quá ngắn xử lý an toàn | P0 | Functional, Boundary |
| STU-017 | Student | Kanji Writing | Save attempt | Các nét đã đánh giá | Submit full attempt hai lần | stroke JSON | Row lưu đúng Student, score/quality; JSON hợp lệ; duplicate behavior xác định | P1 | Integration, Database |
| STU-018 | Student | Assessment | List quiz | Published quiz exists | Mở `/quiz`; filter/list | N5 | Chỉ quiz published/not deleted; empty/error state đúng | P0 | Smoke |
| STU-019 | Student | Assessment | Start quiz | Eligible Student | Start một quiz | assessment ID | Tạo attempt `in_progress`; câu hỏi không lộ đáp án đúng | P0 | Functional, Security |
| STU-020 | Student | Assessment | Submit/score | In-progress attempt | Submit mixed answers | correct/wrong/blank | Backend tính score; attempt submitted; answer rows đúng; score 0..max | P0 | Functional, Database |
| STU-021 | Student | Assessment | Invalid/replay/concurrent submit | Attempt submitted/expired/foreign | Submit lại, hai tab, ID người khác | answer set | Chỉ một transition; không double attempt mutation; 403/409/422 phù hợp | P0 | Concurrent, Security |
| STU-022 | Student | Mock Exam | Full journey | Published exam | List→start→answer→submit→results→review | exam fixture | Điểm phần/tổng đúng; history/review đúng Student | P0 | Regression, Integration |
| STU-023 | Student | Mock Exam | Timeout/auto-submit | Exam running | Advance clock/hết thời gian; submit trễ | timed exam | Auto-submitted/blocked theo service; server time authoritative | P0 | Boundary, Security |
| STU-024 | Student | Attempts | History/review permission | Multiple Students | List history; open own/other/in-progress attempt | IDs | Chỉ own attempts; in-progress không review; correct pagination | P0 | Permission |
| STU-025 | Student | Flashcard | Start SRS session | Topic has vocab | Start session, inspect cards | newLimit 0/1/max | Deck/cards created idempotently; bounds handled; answer không lộ sớm | P0 | Functional |
| STU-026 | Student | Flashcard | Review ratings | Active session | Review easy/hard/wrong; last card | rating set | interval/ease/repetition/next date đúng; session result đúng | P0 | Functional, Database |
| STU-027 | Student | Flashcard | Invalid review | Card foreign/deleted; bad rating | POST review | invalid IDs/rating | 400/403/404; no SRS mutation | P1 | Negative, Permission |
| STU-028 | Student | Notebook | Deck/card operations | Logged in | List decks/cards; search/sort/due; add word | vocab IDs | Data thuộc Student; review deck unique; pagination/sort đúng | P1 | Functional |
| STU-029 | Student | Notebook | Delete/bulk delete | Own cards exist | Delete one; bulk empty/duplicate/foreign IDs | ID set | Soft delete own only; result count đúng; foreign unchanged | P1 | Functional, Security |
| STU-030 | Student | Dictionary | Search | Published content | Search chung và theo type | Japanese/romaji/Vi, empty | Kết quả đúng loại; Unicode; empty query validation; save vocab works | P1 | Functional |
| STU-031 | Student | Speaking | List/upload/poll | Speaking lesson published | List; upload valid audio; poll pending | `FILE-AUDIO` | Submission pending; jobId=submissionId; Student chỉ poll own job | P0 | Integration |
| STU-032 | Student | Speaking | Invalid upload | Logged in | Missing exercise, wrong lesson, bad MIME, huge/empty file | `FILE-BAD` | 400/404/415; no orphan submission/file | P0 | Negative, Security |
| STU-033 | Student | Speaking | Graded result | Staff graded submission | Poll job; reload page | graded job | Completed score/feedback shown; manual score used | P0 | Functional |
| STU-034 | Student | Progress | Upsert/reset | Logged in | Mark learning/completed; lower progress; reset type | 0,100,101,-1 | Bounds validated; unique row; monotonic rule theo service; reset scoped | P1 | Boundary, Database |
| STU-035 | Student | Support | Ticket full lifecycle | Logged in | Create/list/detail/reply/close | subject/content/attachment | State and replies correct; notification/audit as implemented | P0 | Functional |
| STU-036 | Student | Support | Ticket isolation/validation | Two Students | Open/reply/close other ticket; XSS/long content | IDs + payload | 403/404; content escaped; lengths validated | P0 | Permission, Security |
| STU-037 | Student | Notification | Inbox/read | Notifications exist | List filters/pages; read one; read all | own/foreign IDs | Own only; timestamps set; operations idempotent; unread count correct | P1 | Functional, Database |
| STU-038 | Student | UI | Menus/buttons/states | Logged in | Đi qua toàn bộ Student menu; click CTA/button/modal; force API 500/offline | route inventory | Menu đúng quyền; button enabled rules; loading/error/empty/retry accessible | P1 | UI, Regression |
| STU-039 | Student | Gap-check | Reading Practice tồn tại? (UC-14) | Logged in | Rà route inventory và menu Student; thử gọi API nghi ngờ `/api/reading/**` bằng token hợp lệ | route/API probe | Xác nhận hiện KHÔNG có route/menu/API luyện đọc độc lập; UI không quảng cáo tính năng chưa có; ghi nhận gap để Product xác nhận roadmap | P2 | Gap, Regression |
| STU-040 | Student | Gap-check | Listening Practice tồn tại? (UC-15) | Logged in | Tương tự STU-039 cho `/api/listening/**` và mọi entry point nghe hiểu độc lập | route/API probe | Xác nhận hiện KHÔNG có route/menu/API luyện nghe độc lập; không có audio player đứng riêng ngoài Speaking/Mock Test | P2 | Gap, Regression |
| STU-041 | Student | Gap-check | Bookmark Learning tồn tại? (UC-17) | Đang xem lesson/grammar/kanji/quiz | Tìm nút "Bookmark"/"Đánh dấu" trên các trang nội dung (không phải Notebook từ vựng); gọi API nghi ngờ `is_bookmarked`/`/api/bookmarks/**` | route/API probe | Xác nhận hiện KHÔNG có cờ bookmark áp dụng cho lesson/grammar/kanji/quiz; Notebook (STU-028) chỉ lưu thẻ từ vựng, không phải bookmark đa loại nội dung theo đặc tả UC-17; ghi nhận gap | P2 | Gap, Functional |

### 5.3. Staff

| Test ID | Role | Module | Feature | Preconditions | Test Steps | Test Data | Expected Result | Priority | Type |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| STF-001 | Staff | Dashboard | Metrics/tasks | Staff logged in | Mở `/staff`; compare API/DB | seeded content/tickets | KPI/task list chính xác; no Admin/Manager action | P1 | Smoke, Database |
| STF-002 | Staff | Students | List/filter/progress | Students exist | Search/filter/page; open progress | status/level/query | Correct data; no password/token; pagination đúng | P1 | Functional |
| STF-003 | Staff | Students | Suspend/activate | Target active/suspended | Suspend with reason; activate; concurrent change | reason boundaries | State valid; audit row; UI refresh; conflict handled | P0 | Functional, Concurrent |
| STF-004 | Staff | Questions | Create positive | Staff logged in | Create MC/fill/true-false | valid per type | Draft created_by Staff; correct fields; UI row appears | P0 | Functional |
| STF-005 | Staff | Questions | Validation/boundary | None | Missing correct answer/options; bad level/type; long text/XSS | invalid set | 400; no partial row; safe rendering | P0 | Negative, Security |
| STF-006 | Staff | Questions | List/detail/update | Own/other draft exists | Filter; detail; update editable content | valid update | Correct list; update timestamp/content; ownership/state rules enforced | P1 | Functional |
| STF-007 | Staff | Questions | Submit review/locked | Complete/incomplete/used question | Submit; edit pending/published/attempt-used question | fixtures | Valid → pending; incomplete blocked; immutable result integrity | P0 | Workflow |
| STF-008 | Staff | Grammar | CRUD/submit | Staff logged in | Create, list, detail, update, submit | valid grammar | Draft→pending; creator set; validation/audit as expected | P1 | Functional |
| STF-009 | Staff | Vocabulary Topic | List/create unique | Staff logged in | Create topic; duplicate slug/title; boundary order | N5 topic | Unique constraints surfaced as 409/validation; no 500 | P1 | Functional, Database |
| STF-010 | Staff | Learning Content | Lesson update | Own lesson draft | List/filter; update text/media/status | valid/invalid | Fields update; cannot directly publish as Staff; validation works | P0 | Permission |
| STF-011 | Staff | Learning Content | Vocabulary CRUD | Topic exists | Create/list/detail/update/submit review | JP vocab | FK topic valid; draft→pending; Unicode stored utf8mb4 | P0 | Functional, Database |
| STF-012 | Staff | Learning Content | Kanji CRUD | Staff logged in | Create/list/detail/update/submit | Kanji duplicate/valid | Unique character enforced; state/creator correct | P0 | Functional |
| STF-013 | Staff | Speaking Authoring | Create/update/detail | Staff logged in | Create speaking lesson/questions; edit; get own | valid/boundary | Lesson/questions transactionally saved; order correct | P1 | Functional |
| STF-014 | Staff | Quiz | Create/list/detail/update | Questions exist | CRUD quiz | score/duration/level boundaries | Assessment type=quiz; validation; UI state correct | P0 | Functional |
| STF-015 | Staff | Quiz | Assign questions | Draft quiz | Assign/reorder/duplicate/foreign question | assignments | Unique prevents duplicate; score/display order correct; atomic failure | P0 | Integration, Database |
| STF-016 | Staff | Exam | Create/list/detail/update | Questions exist | CRUD exam | valid exam | Assessment type=exam; filters and fields correct | P0 | Functional |
| STF-017 | Staff | Exam | Assign/submit review | Draft exam | Assign sections; submit complete/incomplete | assignment set | Complete → pending; invalid structure blocked; no partial update | P0 | Workflow |
| STF-018 | Staff | Review Feedback | View feedback | Content rejected/change requested | Open feedback modal/API | content type/id | Latest relevant feedback shown only to creator/allowed Staff | P1 | Permission |
| STF-019 | Staff | Tickets | List/detail/reply/close | Ticket assigned to Staff | Process ticket | reply/attachment | assigned→in_progress→resolved; notification; audit | P0 | Workflow |
| STF-020 | Staff | Tickets | Unauthorized ticket | Ticket assigned other Staff | Direct detail/reply/close | ticket ID | Staff thường bị 403; Manager exception tested separately | P0 | Permission |
| STF-021 | Staff | Broadcast | Send/schedule | Staff allowed | Send now/by level/channel; schedule future | valid payload | Job accepted; notifications generated/delivered; UI confirmation | P1 | Integration |
| STF-022 | Staff | Broadcast | Invalid/security | None | Empty/long title; past schedule; invalid channel/level; XSS | invalid set | 400; no job; stored/rendered content safe | P1 | Negative, Security |
| STF-023 | Staff | Grading | List/detail | Speaking submissions exist | Filter pending/graded; open detail/audio | submissions | Data correct; only speaking filter; audio authorized | P0 | Functional |
| STF-024 | Staff | Grading | Grade/override | Pending/AI-graded submission | Grade min/max; regrade/concurrent grade | -1,0,10,11 | Range enforced; valid manual score/feedback persisted; audit/notification; conflict handled | P0 | Boundary, Concurrent |
| STF-025 | Staff | UI | Menus/buttons/states | Staff logged in | Traverse Staff pages/actions; error/empty/loading | route inventory | Only Staff menu; Manager/Admin hidden; buttons follow status/ownership | P1 | UI, Regression |
| STF-026 | Staff | Gap-check | View Quiz Results tồn tại? (UC-32) | Quiz có nhiều attempt của nhiều Student | Tìm màn hình/API thống kê kết quả theo từng quiz/đề thi (tỷ lệ đúng/sai từng câu, điểm trung bình); nếu không có, xác nhận Staff chỉ xem được qua `/staff/students` (STF-002, theo từng Student) | quiz với ≥3 attempt | Xác nhận hiện KHÔNG có màn hình/API tổng hợp kết quả theo quiz cho Staff; chỉ có xem theo từng Student; ghi nhận gap so với UC-32 và đề xuất bổ sung endpoint `/api/staff/assessments/{id}/results` | P1 | Gap, Functional |

### 5.4. StaffManager

| Test ID | Role | Module | Feature | Preconditions | Test Steps | Test Data | Expected Result | Priority | Type |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| MGR-001 | Manager | Dashboard | Navigation/summary | Manager logged in | Mở `/manager`; inspect cards/menu | pending content/tickets | Manager dashboard loads; Staff capabilities preserved where linked | P1 | Smoke |
| MGR-002 | Manager | Review | Queue/filter/page | Pending all content types | Filter type/level/page | lesson/grammar/vocab/kanji/question/assessment | Queue complete; correct creator/snapshot metadata; no deleted item | P0 | Functional |
| MGR-003 | Manager | Review | Detail | Pending item | Open detail directly/UI | content IDs | Correct handler/type; snapshot and creator shown; unknown/type mismatch 404/400 | P0 | Functional |
| MGR-004 | Manager | Review | Approve | Other Staff pending content | Approve | valid item | pending→published; approver/published_at set; audit row; Student can see | P0 | Workflow, Database |
| MGR-005 | Manager | Review | Reject/request changes | Pending content | Reject/change without and with feedback | empty/valid feedback | Feedback required; valid transition; Staff can read feedback; audit | P0 | Negative, Workflow |
| MGR-006 | Manager | Review | Self-review prohibition | Manager-created content pending | Approve/reject own item via UI/API | own ID | 403/422; status unchanged; event logged safely | P0 | Permission |
| MGR-007 | Manager | Review | Concurrent reviewers | Two Managers | Both approve/reject same version simultaneously | same item | Exactly one succeeds; other gets conflict/latest state; one final transition | P0 | Concurrent |
| MGR-008 | Manager | Publish | List/detail/filter | Published/archived items | Open pipeline; filter/detail | type/level | Correct content/state; Staff normal cannot access | P0 | Functional, Permission |
| MGR-009 | Manager | Publish | State transitions | Approved/published/archived | Publish, unpublish/archive, restore | reason boundary | Only allowed transitions; reason/audit stored; Student visibility updates | P0 | Workflow |
| MGR-010 | Manager | Publish | Invalid transition | Draft/deleted/in-use content | Direct status calls; missing reason | invalid transitions | 409/422; no state corruption; dependency warning handled | P0 | Negative |
| MGR-011 | Manager | Trash | List/restore | Soft-deleted content | Filter type; restore once/twice/concurrently | IDs | Restored correctly; repeat idempotent/conflict; audit | P1 | Functional, Concurrent |
| MGR-012 | Manager | Tickets | Assign | Open ticket, Staff list | Load assignees; assign; reassign | Staff IDs | open→assigned; correct assignee; counts refresh; audit/notification | P0 | Workflow |
| MGR-013 | Manager | Tickets | Invalid assign | Suspended/nonexistent Staff; resolved ticket | Assign | invalid IDs | 400/404/409; status unchanged | P1 | Negative |
| MGR-014 | Manager | Broadcast | Send all/schedule | Manager logged in | Confirm “all”; send and schedule | channels/levels | Confirmation required; accepted job; no duplicate click submission | P1 | UI, Integration |
| MGR-015 | Manager | UI/Permission | Menus/direct API | Manager logged in | Traverse manager menu; call Admin APIs; hidden actions | routes/tokens | Manager routes work; Admin 403; no Admin menu; Staff normal Manager API denied by service | P0 | Permission, Regression |

### 5.5. Admin

| Test ID | Role | Module | Feature | Preconditions | Test Steps | Test Data | Expected Result | Priority | Type |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| ADM-001 | Admin | Dashboard | KPI | Admin logged in; seeded DB | Mở `/admin`; compare summary with SQL | users/attempts/content | Accurate KPI; no sensitive password/token; error state works | P0 | Smoke, Database |
| ADM-002 | Admin | Audit | List/filter/page | Audit rows exist | Mở `/admin/reports`; filter action/target/page | action set | UI/API/DB consistent; actor/action/time displayed; no export button | P0 | Functional |
| ADM-003 | Admin | Users | List/filter/detail | Mixed users | Filter type/status/level/role/search; detail | query set | Pagination/filters correct; deleted handled; secrets hidden | P0 | Functional |
| ADM-004 | Admin | Users | Create Staff | Admin logged in | Create Staff/Manager; duplicate/invalid email/role | valid/invalid | Correct role/status/temp setup; unique email; audit/email | P0 | Functional |
| ADM-005 | Admin | Users | Update Student/Staff | Targets exist | Edit allowed fields; invalid/overlong/XSS | payload set | Only allowed fields update; role not changed by generic update; safe output | P1 | Security |
| ADM-006 | Admin | Users | Suspend/activate | Active/suspended target | Suspend reason min/max; activate; self-target cases | boundary reason | State transition valid; sessions handled by policy; audit | P0 | Workflow |
| ADM-007 | Admin | Users | Reset password | Target exists | Reset Student; inspect response/email | user ID | No plaintext persisted/logged; secure reset flow; audit | P0 | Security |
| ADM-008 | Admin | Staff Reset | Request lifecycle | Pending Staff request | List; issue temp password; repeat/expired | request IDs | Completed once; Staff must change password; audit/email | P0 | Workflow |
| ADM-009 | Admin | Users | Soft delete/restore | Target exists | Delete; login/direct API; restore | Student/Staff | status deleted; access blocked; related data retained; restore works; audit | P0 | Functional, Database |
| ADM-010 | Admin | Users | Change Staff role | Staff/Manager target | Promote/demote; concurrent changes; invalid role | roles | staff_role updated only valid values; permissions change after new auth; audit | P0 | Permission, Concurrent |
| ADM-011 | Admin | Settings | Read group | Settings seeded | Open settings; fetch allowed/unknown group | system/email/... | Editable metadata correct; unknown group handled; secret values masked if applicable | P1 | Functional |
| ADM-012 | Admin | Settings | Update one/batch | Admin logged in | Update string/int/bool/time; invalid type; batch partial error | boundary values | Validation by type; batch atomic; updated_by/time set; audit | P0 | Integration, Database |
| ADM-013 | Admin | Settings | SMTP test | SMTP sandbox | Test success/failure/timeout/injection | SMTP payloads | Clear result; no credential leak; failures handled/outbox policy as designed | P1 | Integration, Security |
| ADM-014 | Admin | Notification Rules | List/create/update | Rule settings available | CRUD through `/api/admin/notifications/rules` | valid/duplicate/invalid | Rules list/create/update; duplicate key conflict; audit; API-only limitation documented | P1 | Functional, API |
| ADM-015 | Admin | UI/Permission | Menus/direct API | Admin logged in | Traverse Admin routes; attempt Staff/Manager/Student actions | routes/tokens | Admin UI correct; non-authorized domain endpoints 403; no privilege confusion | P0 | Permission, Regression |
| ADM-016 | Admin | Gap-check | Report/export scope vs UC-38 | Admin logged in | Mở `/admin/reports`; tìm nút xuất PDF/CSV/Excel và biểu đồ tăng trưởng người dùng/hiệu suất học tập theo mô tả UC-38 | N/A | Xác nhận hiện trạng `/admin/reports` chỉ hiển thị Audit Log (ADM-002), KHÔNG có export hay biểu đồ growth/performance; ghi nhận deviation so với đặc tả UC-38, cần Product xác nhận đây là giảm phạm vi có chủ đích hay backlog còn thiếu | P1 | Gap, Regression |

### 5.6. Cross-role, Security, Reliability và Database

| Test ID | Role | Module | Feature | Preconditions | Test Steps | Test Data | Expected Result | Priority | Type |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SEC-001 | Mọi role | Authorization | Cross-role matrix | Tokens for all roles | Gọi `/students`, `/staff`, `/manager`, `/admin` bằng từng token | token matrix | Allowed đúng role; còn lại 403; no data leak | P0 | Permission, Security |
| SEC-002 | Mọi role | Authorization | IDOR | Two actors each role | Thay user/content/ticket/attempt/submission IDs | foreign IDs | 403/404; không đọc/sửa dữ liệu của actor khác | P0 | Security |
| SEC-003 | Khách | API | No token/malformed | None | Gọi mọi protected API không token/malformed | no/invalid token | 401 JSON chuẩn; không redirect HTML; DB unchanged | P0 | Security, API |
| SEC-004 | Mọi role | Input | SQL injection | App running | Gửi SQL payload vào search/filter/body | `SQL-XSS` | Query parameterized; no auth bypass/data change/500 | P0 | Security |
| SEC-005 | Mọi role | Input | Stored/reflected XSS | Writable text fields | Lưu payload; mở mọi nơi hiển thị | XSS payload | Escaped/sanitized; CSP/browser không execute | P0 | Security, UI |
| SEC-006 | Mọi role | API | Mass assignment | Authenticated | Thêm fields role/status/score/creator/approvedBy vào DTO | forged fields | Ignored/rejected; server-owned fields unchanged | P0 | Security |
| SEC-007 | Mọi role | API | Method/content type | Authenticated | Dùng sai HTTP method/content-type/malformed JSON | request variants | 405/415/400; response chuẩn; no stack trace | P1 | API, Negative |
| SEC-008 | Mọi role | CORS/Headers | Browser security | App deployed | Request allowed/disallowed Origin; inspect headers/cookies | origins | CORS allowlist đúng; secure headers; token không lộ URL/log | P1 | Security |
| SEC-009 | Student/Staff | Upload | Malicious file | Logged in | Double extension, MIME spoof, huge, traversal filename | `FILE-BAD` | Reject; random safe filename; no code execution/path escape | P0 | Security |
| SEC-010 | Mọi role | Error | 400/401/403/404/409/422/500 | Fault injection | Trigger each error from UI/API | error fixtures | Status/message/data chuẩn; no stack/SQL/secrets; UI recoverable | P0 | Integration, UI |
| SEC-011 | System | Database | Flyway clean install | Empty MySQL 8.4 | Start backend; inspect schema | migrations V1..V32 | 28 tables + 1 view; checks/index/FK; Hibernate validate passes | P0 | Smoke, Database |
| SEC-012 | System | Database | Charset/timezone | Migrated DB | Store `JP-TEXT`; compare timestamps | Japanese/emoji | utf8mb4 round-trip; UTC timestamps consistent | P0 | Database |
| SEC-013 | System | Database | Constraints | Migrated DB | Violate user email, topic unique, actor checks, sender checks, progress unique | invalid SQL/API | Constraint blocks; API maps to controlled error; transaction rollback | P0 | Database, Negative |
| SEC-014 | System | Database | Soft delete/cascade | Related fixture | Soft delete via API; verify relations; controlled hard-delete fixture | entity graph | Soft delete retains history; cascade only documented dependents | P0 | Database |
| SEC-015 | Mọi role | Audit | Audit completeness | Perform critical actions | Query audit rows | action list | Exactly one actor; action/target/time/IP meaningful; no secret payload | P0 | Database, Security |
| SEC-016 | System | Email | Retry/outbox | SMTP fails | Trigger email; fail retries; restore SMTP; retry worker | failure fixture | Attempt count/error/status correct; eventual sent; no duplicate mail | P0 | Integration |
| SEC-017 | Mọi role | Concurrency | Optimistic/state race | Two sessions | Concurrent review, grade, submit, suspend, settings batch | same target | One valid final state; conflicts explicit; no lost update/duplicate audit | P0 | Concurrent |
| SEC-018 | Mọi role | UI | Accessibility | Pages loaded | Keyboard navigation; labels; focus; contrast; modal trap; screen reader smoke | axe scan | No critical WCAG A/AA issue; error linked to field; focus restored | P2 | UI, Regression |
| SEC-019 | Mọi role | UI | Responsive/browser | Test environments | Run critical flows on 3 browsers and 360/768/1440 widths | viewports | No clipping/blocking; menus/forms/tables usable | P2 | Regression |
| SEC-020 | System | Reliability | Restart/recovery | Running jobs/submissions/outbox | Restart backend during pending state | pending records | DB state retained; no corrupt/duplicate processing; app health recovers | P1 | Reliability |
| SEC-021 | System | Performance | Basic SLA | Production-like dataset | Measure login/list/search/submit/dashboard | p95 sample | Meets agreed SLA; pagination prevents unbounded payload; no N+1 blocker | P2 | Performance |
| SEC-022 | Mọi role | Privacy | Logs/response | Logging enabled | Execute auth/upload/error flows; inspect logs/network | secrets/tokens | No password, OTP, full token, SMTP secret or stack trace exposed | P0 | Security |

---

## 6. UI Route Coverage Matrix

Mỗi route được kiểm tra về render, menu/CTA, route guard, refresh/direct URL, loading, error, empty và responsive state thông qua Test ID được ánh xạ.

| Route | Vai trò | Test ID |
| :--- | :--- | :--- |
| `/`, `/tinh-nang`, `/blog`, `*` | Khách | AUTH-001, SEC-018, SEC-019 |
| `/login` | Khách | AUTH-007–AUTH-009 |
| `/register`, `/verify-email` | Khách | AUTH-002–AUTH-006 |
| `/forgot-password`, `/reset-password` | Khách | AUTH-013, AUTH-014 |
| `/staff/forgot-password`, `/staff/setup-password`, `/staff/change-temp-password` | Staff | AUTH-010–AUTH-012 |
| `/403` | Mọi role | AUTH-022, SEC-001 |
| `/dashboard`, `/onboarding` | Student | STU-001–STU-003 |
| `/profile`, `/settings/change-password`, `/settings/change-email` | Student | STU-004–STU-007 |
| `/courses`, `/lessons/:id` | Student | STU-008–STU-010 |
| `/kana` | Student | STU-011 |
| `/vocabulary`, `/vocabulary/flashcard` | Student | STU-012, STU-025–STU-027 |
| `/grammar` | Student | STU-014 |
| `/kanji`, `/kanji/:id` | Student | STU-015–STU-017 |
| `/quiz` | Student | STU-018–STU-021 |
| `/mock-test`, `/mock-test/:id/attempt`, `/mock-test/:id?/results` | Student | STU-022–STU-024 |
| `/dictionary`, `/notebook` | Student | STU-028–STU-030 |
| `/speaking` | Student | STU-031–STU-033 |
| `/progress` | Student | STU-034 |
| `/support`, `/support/tickets/:ticketId` | Student | STU-035, STU-036 |
| `/notifications` | Student | STU-037 |
| `/staff` | Staff | STF-001 |
| `/staff/content` | Staff | STF-008–STF-013 |
| `/staff/questions` | Staff | STF-004–STF-007 |
| `/staff/assessments` | Staff | STF-014–STF-017 |
| `/staff/tickets` | Staff | STF-019, STF-020 |
| `/staff/grading` | Staff | STF-023, STF-024 |
| `/staff/students` | Staff | STF-002, STF-003 |
| `/manager` | StaffManager | MGR-001 |
| `/manager/review-queue` | StaffManager | MGR-002–MGR-007 |
| `/manager/content-pipeline` | StaffManager | MGR-008–MGR-010 |
| `/manager/deleted-topics` | StaffManager | MGR-011 |
| `/manager/notifications` | StaffManager | MGR-014 |
| `/manager/tickets` | StaffManager | MGR-012, MGR-013 |
| `/admin` | Admin | ADM-001 |
| `/admin/users` | Admin | ADM-003–ADM-010 |
| `/admin/settings` | Admin | ADM-011–ADM-013 |
| `/admin/reports` | Admin | ADM-002, ADM-016 |

> **Route không tồn tại (gap so với đặc tả — xem §9):** không có route Student độc lập cho UC-14 Reading/UC-15 Listening; không có route/nút bookmark đa nội dung cho UC-17 (chỉ có `/notebook` cho vocabulary); không có route thống kê theo quiz cho UC-32 (Staff chỉ xem qua `/staff/students`). STU-039–STU-041 và STF-026 kiểm chứng đúng thực trạng "không tồn tại" này.

---

## 7. API Coverage Matrix

### 7.1. Auth, Student và learning APIs

| API family/endpoints | Methods | Test ID |
| :--- | :--- | :--- |
| `/api/auth/check-account-type`, `/login`, `/register` | POST | AUTH-002–AUTH-009 |
| `/api/auth/refresh`, `/logout` | POST | AUTH-017–AUTH-021 |
| `/api/auth/verify-email`, `/resend-verification` | POST | AUTH-005, AUTH-006 |
| `/api/auth/forgot-password`, `/reset-password`, `/google` | POST | AUTH-013–AUTH-016 |
| `/api/staff/auth/setup-password`, `/forgot-password`, `/login`, `/change-temp-password` | POST | AUTH-007, AUTH-010–AUTH-012 |
| `/api/students/dashboard`, `/me/stats`, `/next-lesson` | GET | STU-003 |
| `/api/students/onboarding`, `/me/avatar` | POST | STU-001, STU-002, STU-005 |
| `/api/students/vocab-home`, `/courses`, `/me` | GET | STU-004, STU-008, STU-012 |
| `/api/students/me`, `/me/password`, `/me/email` | PUT | STU-004, STU-006, STU-007 |
| `/api/students/me/email/otp` | POST | STU-007 |
| `/api/lessons/{lessonId}` | GET | STU-009, STU-010 |
| `/api/learning-progress`, `/reset` | POST, DELETE | STU-009, STU-011, STU-034 |
| `/api/kana` | GET | STU-011 |
| `/api/vocabulary/topics`, `/api/vocabulary` | GET | STU-012, STU-013 |
| `/api/grammar-points`, `/{grammarId}` | GET | STU-014 |
| `/api/kanji`, `/{kanjiId}` | GET | STU-015 |
| `/api/kanji/writing/evaluate-stroke`, `/writing/attempt` | POST | STU-016, STU-017 |
| `/api/assessments`, `/{id}/start`, `/{id}/submit` | GET, POST | STU-018–STU-023 |
| `/api/test-attempts`, `/{attemptId}/review` | GET | STU-022–STU-024 |
| `/api/flashcards/session`, `/{id}/review` | POST | STU-025–STU-027 |
| `/api/notebook/decks`, `/cards` | GET | STU-028 |
| `/api/notebook/cards/{id}` | DELETE | STU-029 |
| `/api/notebook/cards/bulk-delete`, `/words` | POST | STU-028, STU-029 |
| `/api/dictionary/search`, `/search/{type}` | GET | STU-030 |
| `/api/speaking/exercises`, `/{jobId}` | GET | STU-031, STU-033 |
| `/api/speaking/submit` | POST multipart | STU-031, STU-032 |
| `/api/support/tickets`, `/{ticketId}` | POST/GET | STU-035, STU-036 |
| `/api/support/tickets/{ticketId}/reply`, `/close` | POST | STU-035, STU-036 |
| `/api/notifications`, `/{id}/read`, `/read-all` | GET, POST | STU-037 |

### 7.2. Staff và Manager APIs

| API family/endpoints | Methods | Test ID |
| :--- | :--- | :--- |
| `/api/staff/dashboard` | GET | STF-001 |
| `/api/staff/students`, `/{id}/progress`, `/{id}/suspend`, `/{id}/activate` | GET, POST | STF-002, STF-003 |
| `/api/staff/questions`, `/{id}` | POST, GET, PUT | STF-004–STF-006 |
| `/api/staff/questions/{id}/submit-review` | POST | STF-007 |
| `/api/staff/grammar`, `/{id}` | POST, GET, PUT | STF-008 |
| `/api/staff/grammar/{id}/submit-review` | POST | STF-008 |
| `/api/staff/vocabulary-topics` | GET, POST | STF-009 |
| `/api/staff/lessons` | GET | STF-010 |
| `/api/staff/lessons/{id}` | PUT | STF-010 |
| `/api/staff/vocabulary`, `/{id}` | POST, GET, PUT | STF-011 |
| `/api/staff/kanji`, `/{id}` | POST, GET, PUT | STF-012 |
| `/api/staff/speaking-lessons`, `/{id}` | POST, GET, PUT | STF-013 |
| `/api/staff/assessments`, `/{id}`, `/{id}/assign-questions` | POST, GET, PUT | STF-014, STF-015 |
| `/api/staff/exams`, `/{id}`, `/{id}/assign-questions` | POST, GET, PUT | STF-016, STF-017 |
| `/api/staff/contents/submit-review` | POST | STF-017, STF-018 |
| `/api/staff/content/{id}/feedback` | GET | STF-018 |
| `/api/staff/tickets`, `/{id}` | GET | STF-019, STF-020 |
| `/api/staff/tickets/{id}/assign`, `/reply`, `/close` | POST | STF-019, STF-020, MGR-012, MGR-013 |
| `/api/staff/members` | GET | MGR-012 |
| `/api/staff/notifications` | POST | STF-021, STF-022, MGR-014 |
| `/api/staff/submissions`, `/{id}`, `/{id}/grade` | GET, POST | STF-023, STF-024 |
| `/api/manager/review-queue`, `/contents/{id}` | GET | MGR-002, MGR-003 |
| `/api/manager/reviews`, `/reviews/request-changes` | POST | MGR-004–MGR-007 |
| `/api/manager/published-contents`, `/{id}` | GET | MGR-008 |
| `/api/manager/published-contents/{id}/status`, `/restore` | PUT, POST | MGR-009, MGR-010 |
| `/api/manager/deleted-contents`, `/{type}/{id}/restore` | GET, POST | MGR-011 |

### 7.3. Admin APIs

| API family/endpoints | Methods | Test ID |
| :--- | :--- | :--- |
| `/api/admin/dashboard` | GET | ADM-001 |
| `/api/admin/audit-logs` | GET | ADM-002, SEC-015 |
| `/api/admin/users`, `/users/{type}/{id}` | GET | ADM-003 |
| `/api/admin/staff` | POST | ADM-004 |
| `/api/admin/users/student/{id}`, `/users/staff/{id}` | PUT | ADM-005 |
| `/api/admin/users/{type}/{id}/suspend`, `/activate` | POST | ADM-006 |
| `/api/admin/users/{type}/{id}/reset-password` | POST | ADM-007 |
| `/api/admin/staff/reset-requests`, `/staff/{id}/issue-temp-password` | GET, POST | ADM-008 |
| `/api/admin/users/{type}/{id}`, `/restore` | DELETE, POST | ADM-009 |
| `/api/admin/staff/{id}/role` | PUT | ADM-010 |
| `/api/admin/settings/{group}` | GET, PUT | ADM-011, ADM-012 |
| `/api/admin/settings/{group}/{key}` | PUT | ADM-012 |
| `/api/admin/settings/smtp/test` | POST | ADM-013 |
| `/api/admin/notifications/rules`, `/{ruleKey}` | GET, POST, PUT | ADM-014 |

### 7.4. Non-production/debug endpoint

| Endpoint | Test ID | Kỳ vọng |
| :--- | :--- | :--- |
| `/api/debug-smtp/outbox` | SEC-001, SEC-022 | Không được public ở production; nếu bật test profile phải giới hạn Admin/test-only và không lộ secret |

---

## 8. Workflow và State-transition Coverage

| Entity/Workflow | States/transitions phải test | Test ID |
| :--- | :--- | :--- |
| User | pending→active; active↔suspended; active→deleted→active | AUTH-005, ADM-006, ADM-009 |
| Staff password reset | pending→completed/expired/cancelled | AUTH-012, ADM-008 |
| Auth token | active→expired/revoked; refresh/replay | AUTH-017–AUTH-021 |
| Content | draft→pending_review→published/rejected; published→archived/deleted→restore | STF-007–STF-018, MGR-004–MGR-011 |
| Test attempt | in_progress→submitted/auto_submitted/abandoned | STU-019–STU-024 |
| Submission | pending/ai_graded→graded/rejected | STU-031–STU-033, STF-023, STF-024 |
| Progress | learning→completed/reviewing; scoped reset | STU-009, STU-034 |
| Flashcard | new→reviewed; easy/hard/wrong scheduling; soft delete | STU-025–STU-029 |
| Ticket | open→assigned→in_progress→resolved/closed | STU-035, STF-019, MGR-012, MGR-013 |
| Notification | scheduled→sent/delivered→read | STF-021, MGR-014, STU-037 |
| Email outbox | pending→sent/failed with retry count | SEC-016 |

---

## 9. Requirement Traceability Matrix

> **Đối chiếu theo đúng thứ tự UC-01..UC-40 của `Bao_cao_dac_ta_Use_Case.md`** (v3.0 trước đó đánh số theo thứ tự tự phát, không khớp thứ tự thật — ví dụ UC-01 từng bị gán nhầm thành "Register" thay vì "User Login"). Cột **Trạng thái** phân biệt: ✅ **Implemented** (có tính năng + Test ID xác nhận hành vi thật), ⚠️ **Partial** (tính năng tồn tại nhưng hẹp hơn đặc tả), ❌ **Gap** (chưa triển khai — Test ID chỉ xác nhận sự vắng mặt, không xác nhận hành vi nghiệp vụ).

### 9.1. Nhóm Student (UC-01 → UC-20)

| UC | Tên theo đặc tả | Role | Test Case | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| UC-01 | User Login (kể cả Google OAuth) | Student (+ mọi role dùng chung cơ chế login) | AUTH-007–AUTH-009, AUTH-015, AUTH-016 | ✅ Implemented |
| UC-02 | User Register | Khách | AUTH-002–AUTH-004 | ✅ Implemented |
| UC-03 | Reset Password | Khách/Student | AUTH-013, AUTH-014 | ✅ Implemented |
| UC-04 | User Profile | Student | STU-004, STU-005 | ✅ Implemented |
| UC-05 | Change Password | Student | STU-006 | ✅ Implemented |
| UC-06 | Learn Grammar | Student | STU-014 | ✅ Implemented |
| UC-07 | Learn Kanji | Student | STU-015 | ✅ Implemented |
| UC-08 | Learn Kana | Student | STU-011 | ✅ Implemented |
| UC-09 | Vocabulary | Student | STU-012, STU-013 | ✅ Implemented |
| UC-10 | Take JLPT Mock Test | Student | STU-022–STU-024 | ✅ Implemented |
| UC-11 | Practice & Quiz | Student | STU-018–STU-021 | ✅ Implemented |
| UC-12 | Flashcard Learning | Student | STU-025–STU-027 | ✅ Implemented |
| UC-13 | Speaking Practice & AI Grading | Student/Staff | STU-031–STU-033, STF-023, STF-024 | ✅ Implemented (AI engine: contract-only, xem §1.3) |
| UC-14 | Reading Practice | Student | STU-039 | ❌ **Gap — không có route/API luyện đọc độc lập trong code hiện tại** |
| UC-15 | Listening Practice | Student | STU-040 | ❌ **Gap — không có route/API luyện nghe độc lập trong code hiện tại** |
| UC-16 | Dictionary & Search | Student | STU-030 | ✅ Implemented |
| UC-17 | Bookmark Learning | Student | STU-041 | ❌ **Gap — không có `is_bookmarked`/API bookmark trong backend; Notebook (STU-028/029) chỉ là deck từ vựng, không phải bookmark đa loại nội dung** |
| UC-18 | Logout | Mọi role | AUTH-019 | ✅ Implemented |
| UC-19 | Learning Progress & Stats | Student | STU-003, STU-034 | ✅ Implemented |
| UC-20 | AI Handwriting Practice | Student | STU-016, STU-017 | ✅ Implemented (as-built: DTW stroke-matching, không phải OCR ảnh) |

### 9.2. Nhóm Staff (UC-21 → UC-32)

| UC | Tên theo đặc tả | Role | Test Case | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| UC-21 | View Student Progress | Staff | STF-002 | ✅ Implemented |
| UC-22 | Manage Student Accounts | Staff | STF-002 | ✅ Implemented |
| UC-23 | Suspend or Activate Account | Staff | STF-003 | ✅ Implemented |
| UC-24 | Manage Question Bank | Staff | STF-004–STF-007 | ✅ Implemented |
| UC-25 | Manage Grammar Content | Staff | STF-008 | ✅ Implemented |
| UC-26 | Manage Quiz | Staff | STF-014, STF-015 | ✅ Implemented |
| UC-27 | Manage Learning Content | Staff | STF-009–STF-013 | ✅ Implemented |
| UC-28 | Manage JLPT Mock Exams | Staff | STF-016, STF-017 | ✅ Implemented |
| UC-29 | Respond to Student Support | Staff (+ Student phía gửi ticket) | STF-019, STF-020 (Student: STU-035, STU-036) | ✅ Implemented |
| UC-30 | Send Notifications | Staff | STF-021, STF-022 | ✅ Implemented |
| UC-31 | Grade Speaking Submission | Staff | STF-023, STF-024 | ✅ Implemented |
| UC-32 | View Quiz Results | Staff | STF-026 | ⚠️ **Partial — không có màn hình/API thống kê kết quả theo từng quiz/đề thi; Staff chỉ xem được theo từng Student qua STF-002** |

### 9.3. Nhóm StaffManager (UC-33 → UC-34)

| UC | Tên theo đặc tả | Role | Test Case | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| UC-33 | Review Submitted Content | StaffManager | MGR-002–MGR-007 | ✅ Implemented |
| UC-34 | Manage Published Content Status | StaffManager | MGR-008–MGR-011 | ✅ Implemented |

### 9.4. Nhóm Admin (UC-35 → UC-40)

| UC | Tên theo đặc tả | Role | Test Case | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| UC-35 | Login System (Admin Panel) | Admin | AUTH-007, AUTH-009 | ✅ Implemented |
| UC-36 | View Dashboard | Admin | ADM-001 | ✅ Implemented |
| UC-37 | User Management | Admin | ADM-003–ADM-010 | ✅ Implemented |
| UC-38 | Report Screen (báo cáo, thống kê, export PDF/CSV/Excel) | Admin | ADM-002, ADM-016 | ⚠️ **Partial — `/admin/reports` hiện chỉ là Audit Log; không có export file hay biểu đồ tăng trưởng/hiệu suất như đặc tả mô tả** |
| UC-39 | Settings | Admin | ADM-011–ADM-013 | ✅ Implemented |
| UC-40 | Notification Rules | Admin | ADM-014 | ✅ Implemented |

### 9.5. Tính năng đã triển khai nhưng KHÔNG thuộc 40 UC gốc

Các tính năng dưới đây tồn tại trong as-built và đã có Test ID, nhưng không tương ứng 1-1 với UC nào trong `Bao_cao_dac_ta_Use_Case.md`. Liệt kê riêng để tránh gán gượng ép làm sai lệch traceability.

| Tính năng thực tế | Test Case | Ghi chú |
| :--- | :--- | :--- |
| Onboarding (chọn mục tiêu JLPT lần đầu) | STU-001, STU-002 | Bổ sung UX, không phải UC riêng trong đặc tả |
| Change Email qua OTP | STU-007 | Mở rộng của UC-04/UC-05; đặc tả gốc chỉ nói đổi tên/avatar/mật khẩu |
| Notebook (deck/card từ vựng cá nhân) | STU-028, STU-029 | Gần với UC-17 nhưng phạm vi hẹp hơn (chỉ vocabulary) — xem UC-17 ở trên |
| Notification inbox phía Student | STU-037 | Mặt nhận của UC-30/UC-40; đặc tả gốc chỉ mô tả phía gửi (Staff/Admin) |
| Staff temp-password/setup-password workflow | AUTH-010–AUTH-012, ADM-008 | Hạ tầng vận hành tài khoản Staff do Admin tạo, không phải UC học viên/nghiệp vụ |
| Refresh token/session timeout | AUTH-017, AUTH-018, AUTH-020 | Cơ chế kỹ thuật hỗ trợ UC-01, không phải UC độc lập |

### 9.6. Non-functional requirements

| Requirement | Feature | Role | Test Case |
| :--- | :--- | :--- | :--- |
| NFR-SEC | Authentication/authorization/security | Mọi role | AUTH-021, SEC-001–SEC-010, SEC-022 |
| NFR-DATA | Schema/integrity/audit | System | SEC-011–SEC-016 |
| NFR-REL | Concurrency/recovery | System | SEC-017, SEC-020 |
| NFR-UI | Accessibility/compatibility | Mọi role | SEC-018, SEC-019 |
| NFR-PERF | Basic performance | System | SEC-021 |

---

## 10. Database Validation Matrix

| Bảng/View | Validation | Test ID |
| :--- | :--- | :--- |
| User tables | Unique email, status, role, lock fields | AUTH-004, AUTH-009, ADM-004–ADM-010 |
| `staff_password_reset_requests` | State/expiry/Admin completer | AUTH-012, ADM-008 |
| `auth_tokens` | Exactly one actor, expiry/revoke/cascade | AUTH-017–AUTH-021, SEC-013 |
| Content tables | Creator/approver/status/published timestamp | STF-004–STF-018, MGR-004–MGR-011 |
| `speaking_questions` | Lesson FK/order | STF-013 |
| `kanji_writing_attempts` | Student, DTW score, JSON text, soft delete | STU-016, STU-017 |
| Assessment tables | Assignment unique, attempt/answer score and cascade | STU-019–STU-024, STF-014–STF-017 |
| `student_submissions` | Owner/status/manual grading | STU-031–STU-033, STF-023, STF-024 |
| `student_content_progress` | Unique polymorphic content/progress range | STU-009, STU-034 |
| Flashcard tables | Review deck unique, SRS fields, soft delete | STU-025–STU-029 |
| Ticket tables | State, assignee, exactly one reply sender | STU-035, STF-019, MGR-012, SEC-013 |
| `notifications` | Owner/creator/schedule/read timestamps | STU-037, STF-021, MGR-014 |
| `system_settings` | Group/key unique, typed value, updated_by | ADM-011, ADM-012 |
| `admin_audit_logs` | Exactly one actor, action/target | SEC-015 |
| `email_outbox` | Retry/status/error/timestamps | SEC-016 |
| `vw_student_learning_stats` | Aggregates match base tables | STU-003, ADM-001 |

---

## 11. Coverage Review — Iteration 1

### 11.1. Automated gap-detection checklist

| Review item | Kết quả thiết kế | Bằng chứng |
| :--- | :--- | :--- |
| Uncovered roles | 0 | 5 actor groups present in test tables |
| Uncovered requirements (có Test ID) | 0 | UC-01..UC-40 đều có ít nhất một Test ID — xem §9 |
| UC chưa triển khai trong as-built | 3/40 (UC-14, UC-15, UC-17) | Xác nhận bằng grep code (`reading`/`listening`/`bookmark` không tồn tại ở backend) và test STU-039–STU-041 |
| UC triển khai một phần | 2/40 (UC-32, UC-38) | STF-026, ADM-016 xác nhận phạm vi hẹp hơn đặc tả |
| Uncovered UI routes | 0 | All routes in `App.jsx` mapped |
| Uncovered API families | 0 | All controller endpoint families mapped |
| Uncovered workflows | 0 | State-transition matrix |
| Uncovered permission rules | 0 | AUTH-022, SEC-001–SEC-003 plus role cases |
| Uncovered CRUD | 0 known | Staff content, Admin users/settings/rules covered |
| Uncovered database groups | 0 | 28 tables + view mapped by group/table |

### 11.2. Additional tests generated after review

Các gap thường bị bỏ sót đã được thêm vào suite:

- Direct URL và hidden-menu access: AUTH-022, MGR-015, ADM-015.
- Token role/sub/expiry manipulation và refresh replay: AUTH-018, AUTH-021.
- Concurrent register/submit/review/grade/state update: AUTH-004, STU-021, MGR-007, STF-024, SEC-017.
- Mass assignment và IDOR: SEC-002, SEC-006.
- File MIME spoof/path traversal: STU-005, STU-032, SEC-009.
- Database charset/UTC/check constraints: SEC-011–SEC-013.
- Error states and accessibility: SEC-010, SEC-018.
- Debug endpoint production exposure: API section 7.4.
- **Spec-vs-implementation gap verification (v3.1, đối chiếu lại với `Bao_cao_dac_ta_Use_Case.md`):** STU-039 (UC-14 Reading), STU-040 (UC-15 Listening), STU-041 (UC-17 Bookmark), STF-026 (UC-32 View Quiz Results), ADM-016 (UC-38 Report/export).

### 11.3. Review conclusion

Mọi UC trong đặc tả gốc đều có ít nhất một Test ID truy vết được (traceability = 100%). Tuy nhiên **không phải mọi UC đều có tính năng thật để test hành vi nghiệp vụ** — 3 UC (14, 15, 17) chưa được triển khai và 2 UC (32, 38) chỉ triển khai một phần; các Test ID tương ứng (STU-039–041, STF-026, ADM-016) hiện chỉ xác nhận đúng thực trạng này, không xác nhận nghiệp vụ đầy đủ theo đặc tả. Đây là gap có chủ đích cần Product/PO xác nhận trước khi tính là "PASS" trong báo cáo nghiệm thu. Coverage thực tế còn lại chỉ được xác nhận sau khi automation được triển khai và chạy.

---

## 12. Coverage Summary

| Dimension | Designed coverage |
| :--- | ---: |
| Use cases — có Test ID truy vết | 40/40 = 100% |
| Use cases — triển khai đầy đủ và test được hành vi thật | 35/40 = 87,5% |
| Use cases — triển khai một phần (Partial) | 2/40 = 5% (UC-32, UC-38) |
| Use cases — chưa triển khai (Gap) | 3/40 = 7,5% (UC-14, UC-15, UC-17) |
| Actor groups | 5/5 = 100% |
| React routes | 100% route inventory |
| REST endpoint families | 100% controller inventory |
| Core workflows/state machines | 100% identified workflows |
| Database tables/views | 28/28 tables + 1/1 view |
| Permission zones | 4/4 protected zones + public |
| Positive/negative/boundary/security | Present for every critical module |
| Estimated functional requirement coverage | **100% traceability, ≈92% functional-behavior coverage (35/40 UC đầy đủ + 2/40 partial)** |

Không tuyên bố 100% execution/code coverage vì:

- Một test case có thể cần nhiều data permutations.
- Runtime/environment defects chỉ xuất hiện khi chạy.
- Các third-party sandbox có hành vi ngoài kiểm soát.
- 3/40 UC (UC-14 Reading, UC-15 Listening, UC-17 Bookmark) chưa có tính năng để test hành vi nghiệp vụ — chỉ test được sự vắng mặt (§9.1, §9.5, §13).

---

## 13. Untested Areas, Duplicate Tests và Missing Edge Cases

### 13.1. Untested areas trước khi automation

- Toàn bộ test trong tài liệu là thiết kế; cần triển khai/chạy Playwright và API suite.
- Speech AI không thể test vì chưa tích hợp; chỉ test contract/manual grading.
- Redis application behavior không thể test vì backend chưa dùng Redis.
- Production S3/CDN chưa có cấu hình xác nhận.
- Disaster recovery, backup restore và production-scale load cần kế hoạch riêng.
- **UC-14 Reading Practice và UC-15 Listening Practice** (đặc tả gốc): không có tính năng độc lập trong as-built để test hành vi thật; STU-039/STU-040 chỉ xác nhận sự vắng mặt.
- **UC-17 Bookmark Learning**: không có `is_bookmarked`/API bookmark trong backend; STU-041 chỉ xác nhận sự vắng mặt, Notebook không thay thế được vì phạm vi khác (chỉ vocabulary).
- **UC-32 View Quiz Results (theo quiz/đề thi)**: chưa có màn hình/API tổng hợp cho Staff; STF-026 chỉ xác nhận Staff hiện phải xem từng Student một qua STF-002.
- **UC-38 Report Screen (export + biểu đồ growth/performance)**: `/admin/reports` hiện chỉ là Audit Log; ADM-016 chỉ xác nhận thực trạng, không test được export vì tính năng chưa tồn tại.

### 13.2. Duplicate/overlapping tests có chủ đích

| Overlap | Lý do giữ lại |
| :--- | :--- |
| AUTH-022 và SEC-001 | Một test browser route guard, một test API matrix |
| STU-020 và STU-022 | Quiz scoring và full mock-exam journey khác risk |
| STF-019 và MGR-012 | Staff xử lý và Manager assign là hai quyền khác nhau |
| ADM-002 và SEC-015 | UI/API audit report và database audit completeness |
| Role UI cases và SEC-001 | Hidden menu không chứng minh API authorization |

Không có duplicate test hoàn toàn giống nhau; có thể tái sử dụng fixture/helper.

### 13.3. Missing edge cases cần bổ sung khi làm rõ yêu cầu

- Chính sách chính xác khi refresh token rotation/reuse.
- Ngưỡng file avatar/audio và danh sách MIME chính thức.
- SLA response time và concurrent-user target.
- Chính sách có cho phép regrade speaking nhiều lần hay không.
- Chính sách giảm progress/reset và revoke phiên sau đổi mật khẩu/suspend.
- Kỳ vọng idempotency khi Student gửi cùng writing attempt hai lần.
- Quy tắc restore content về draft/archived/published theo từng loại.

Các trường hợp trên đã có test khung; expected result cần chốt với Product/Architect.

### 13.4. Suggested additional tests

- Property-based tests cho score, SRS và DTW.
- Contract tests frontend service ↔ OpenAPI.
- Mutation testing cho authorization/service rules.
- Visual regression cho 54 route ở ba viewport.
- Chaos test SMTP/storage/database transient failure.
- Soak test notification broadcast và email outbox.
- Backup/restore rehearsal cho MySQL.
- OWASP ZAP authenticated scan cho bốn role.
- Sau khi Product xác nhận scope cho UC-14/UC-15/UC-17/UC-32/UC-38 (§9, §13.1): viết lại đầy đủ positive/negative/boundary test cho từng tính năng khi được triển khai, thay vì chỉ giữ Test ID xác nhận-vắng-mặt như hiện tại.

---

## 14. Automation Roadmap

| Phase | Phạm vi | Exit |
| :--- | :--- | :--- |
| 1 | P0 auth, route guard, assessment, review, Admin user | Critical smoke chạy mỗi PR |
| 2 | Toàn bộ API family + DB assertions | P0/P1 API regression |
| 3 | Browser journeys cho 4 role | UI route/menu/button coverage |
| 4 | Security, concurrency, email/file | Risk coverage |
| 5 | Compatibility, accessibility, performance | Release qualification |

Tag đề xuất: `@smoke`, `@p0`, `@student`, `@staff`, `@manager`, `@admin`, `@security`, `@db`, `@slow`.

---

## PHẦN B — PROFESSIONAL PRESENTATION

## 15. Presentation Design System

- Theme: nền trắng, accent blue `#2563EB` và purple `#7C3AED`.
- Title: 34 pt; heading: 26 pt; body: 20 pt; table: tối thiểu 16 pt.
- Font: Inter/Segoe UI; code/API dùng JetBrains Mono.
- Mỗi slide tối đa 3–7 bullet; bảng dài được chia thành nhiều slide.
- Footer: section, slide number, “System Test Suite v3.0”.
- Transition mặc định: Fade 0.3s; workflow dùng Morph; không dùng hiệu ứng gây nhiễu.

---

## 16. Slide-by-slide Outline

### Slide 1 — Complete System Test Suite

- SakuJi JLPT Learning Platform
- 40 use cases, 5 actor groups
- 28 tables, 1 view
- Near-100% functional test design

**Speaker notes:** Giới thiệu mục tiêu: không chỉ báo cáo test hiện có mà là blueprint system test đầy đủ, có traceability và coverage audit.

**Suggested visual:** Hero graphic gồm browser, API shield và database.

**Transition:** Fade to agenda.

**Time:** 1 phút.

### Slide 2 — Agenda

- System and testing objectives
- Roles and functional scope
- Test strategy and environments
- Role-based test coverage
- Security, data and reliability
- Traceability and roadmap

**Speaker notes:** Nêu cấu trúc trình bày từ bối cảnh đến cách thực thi.

**Suggested visual:** Six-step horizontal timeline.

**Transition:** Morph to system overview.

**Time:** 1 phút.

### Slide 3 — System Overview

- JLPT learning from N5 to N1
- React 18 frontend
- Spring Boot 3.3 / Java 21 API
- MySQL 8.4 with Flyway
- Modular monolith by feature

**Speaker notes:** Tóm tắt nền tảng cần test và nguồn sự thật của chức năng/schema.

**Suggested visual:** Three-tier architecture diagram.

**Transition:** Fade.

**Time:** 1.5 phút.

### Slide 4 — Problem Statement

- Large role and permission surface
- 40 cross-module use cases
- Complex state transitions
- UI, API and database consistency required
- Existing unit tests cannot prove E2E behavior

**Speaker notes:** Nhấn mạnh vì sao cần System Test thay vì chỉ unit/integration coverage.

**Suggested visual:** Risk heat map by layer.

**Transition:** Wipe to objectives.

**Time:** 1.5 phút.

### Slide 5 — Test Objectives

- Cover every role, page and endpoint
- Validate positive and failure paths
- Prevent privilege and IDOR defects
- Verify workflow and database invariants
- Build repeatable regression automation

**Speaker notes:** Liên hệ exit criteria và near-100% designed coverage.

**Suggested visual:** Target/bullseye with five objectives.

**Transition:** Fade.

**Time:** 1 phút.

### Slide 6 — Actors and Roles

- Guest: public/authentication
- Student: learning and personal data
- Staff: authoring and support
- StaffManager: review and coordination
- Admin: users, settings and audit

**Speaker notes:** Giải thích StaffManager dùng ROLE_STAFF cộng staff_role, nên cần kiểm tra hai lớp quyền.

**Suggested visual:** Role hierarchy and permission zones.

**Transition:** Morph into context.

**Time:** 1.5 phút.

### Slide 7 — System Context

- Browser communicates through REST/JWT
- Backend owns business decisions
- MySQL persists state and audit
- SMTP/OAuth/storage are integrations
- Redis container is not yet application-integrated

**Speaker notes:** Phân biệt implemented dependency và infrastructure-only component.

**Suggested visual:** Context diagram from Section 3.

**Transition:** Fade.

**Time:** 1.5 phút.

### Slide 8 — Test Architecture

- Playwright browser E2E
- REST Assured/Newman API suite
- MySQL/Testcontainers assertions
- MailHog/WireMock integration
- OWASP ZAP security checks

**Speaker notes:** Trình bày test pyramid mở rộng theo system-test layers.

**Suggested visual:** Layered test architecture.

**Transition:** Zoom to environment.

**Time:** 1.5 phút.

### Slide 9 — Environment and Test Data

- MySQL 8.4, UTC, utf8mb4
- Fresh Flyway migration
- Dedicated account per role/state
- Controlled clock for expiry/schedule
- SMTP and OAuth sandbox

**Speaker notes:** Giải thích data isolation và tại sao cần controlled clock cho session/exam.

**Suggested visual:** Environment topology and data cards.

**Transition:** Fade.

**Time:** 1.5 phút.

### Slide 10 — Authentication Coverage

- Register and OTP verification
- Multi-role login and redirect
- Staff temporary-password flow
- Forgot/reset and Google OAuth
- Refresh, logout and timeout
- Token manipulation and replay

**Speaker notes:** Highlight AUTH-001..AUTH-022 và các P0 security cases.

**Suggested visual:** Authentication sequence diagram.

**Transition:** Morph to authorization.

**Time:** 2 phút.

### Slide 11 — Authorization Matrix

- Direct URL guard
- API role isolation
- Cross-account IDOR
- Hidden menu is not authorization
- Manager service-level role check
- Expired and forged token handling

**Speaker notes:** Demo conceptual matrix Student/Staff/Manager/Admin versus four protected zones.

**Suggested visual:** 4×4 allowed/forbidden matrix.

**Transition:** Fade.

**Time:** 2 phút.

### Slide 12 — Student Functional Coverage

- Profile, onboarding and dashboard
- Learning content and progress
- Quiz and mock exam
- Flashcard, notebook and dictionary
- Kanji DTW and speaking upload
- Support tickets and notifications

**Speaker notes:** Student có surface lớn nhất; các test STU-001..STU-038 bao phủ UI/API/DB.

**Suggested visual:** Student journey map.

**Transition:** Slide to assessment deep dive.

**Time:** 2 phút.

### Slide 13 — Assessment Workflow Tests

- Published assessment discovery
- Server-side attempt creation
- Answer submission and scoring
- Timeout and auto-submit
- Duplicate/concurrent submit
- History and review isolation

**Speaker notes:** Nhấn mạnh score invariant và mỗi submission tạo attempt/state hợp lệ.

**Suggested visual:** Attempt state machine.

**Transition:** Morph.

**Time:** 2 phút.

### Slide 14 — Learning Algorithms

- Flashcard SRS: easy/hard/wrong
- Interval, ease and next-review validation
- Kanji DTW score and quality
- Short/extreme stroke boundaries
- Database persistence and idempotency

**Speaker notes:** Không gọi Kanji writing là OCR; test property-based được đề xuất.

**Suggested visual:** Split slide: SRS timeline + DTW path comparison.

**Transition:** Fade.

**Time:** 2 phút.

### Slide 15 — Staff Coverage

- Student status and progress
- Question and grammar authoring
- Vocabulary, Kanji and speaking content
- Quiz/exam and assignments
- Review submission and feedback
- Ticket, broadcast and grading

**Speaker notes:** CRUD đi cùng validation, ownership và workflow state.

**Suggested visual:** Feature grid with Staff icons.

**Transition:** Morph to review.

**Time:** 2 phút.

### Slide 16 — Content Review Workflow

- Draft submitted by Staff
- Manager review queue and snapshot
- Approve, reject or request changes
- Self-review is forbidden
- Concurrent review conflict
- Publish/archive/restore and audit

**Speaker notes:** Đây là workflow tích hợp rủi ro cao nhất sau auth/assessment.

**Suggested visual:** Swimlane Staff → Manager → System → Student.

**Transition:** Fade.

**Time:** 2 phút.

### Slide 17 — StaffManager Coverage

- Review all content types
- Published-content pipeline
- Deleted-content restore
- Staff assignment for tickets
- Broadcast with confirmation
- Admin functions remain forbidden

**Speaker notes:** Giải thích quyền kế thừa và các negative direct API tests.

**Suggested visual:** Manager control center mockup.

**Transition:** Fade.

**Time:** 1.5 phút.

### Slide 18 — Admin Coverage

- Dashboard and audit report
- User lifecycle and role changes
- Staff password-reset requests
- Typed and atomic settings
- SMTP test
- Notification-rule API

**Speaker notes:** Admin Reports hiện là audit log, không phải analytics export.

**Suggested visual:** Admin workflow cards.

**Transition:** Morph to data.

**Time:** 2 phút.

### Slide 19 — Database Validation

- 28 tables and one statistics view
- FK, unique and CHECK constraints
- Soft delete and documented cascade
- Polymorphic relations checked by service
- utf8mb4 and UTC validation
- Flyway clean-install smoke

**Speaker notes:** Schema correctness được test bằng API và direct read-only SQL assertions.

**Suggested visual:** Simplified ER diagram.

**Transition:** Fade.

**Time:** 2 phút.

### Slide 20 — API Coverage

- Auth and Student endpoint families
- Staff authoring and support
- Manager review/publish
- Admin users/settings/audit
- Standard error contract
- Debug endpoint production restriction

**Speaker notes:** Mỗi controller family trỏ về ít nhất một Test ID; full endpoint inventory nằm ở Section 7.

**Suggested visual:** API coverage donut by role.

**Transition:** Wipe to security.

**Time:** 1.5 phút.

### Slide 21 — Security Validation

- JWT tampering and replay
- IDOR and mass assignment
- SQL injection and XSS
- Malicious upload and traversal
- CORS and secure headers
- Secret-free logs and responses

**Speaker notes:** Security test không được thay bằng việc menu bị ẩn.

**Suggested visual:** OWASP shield with attack vectors.

**Transition:** Fade.

**Time:** 2 phút.

### Slide 22 — Reliability and Concurrency

- Competing review decisions
- Duplicate exam submission
- Concurrent register/grade/update
- Transaction rollback
- Restart with pending outbox/submission
- Explicit conflict responses

**Speaker notes:** Mục tiêu là một final state hợp lệ, không lost update hoặc duplicate audit.

**Suggested visual:** Race-condition sequence diagram.

**Transition:** Fade.

**Time:** 1.5 phút.

### Slide 23 — UI Quality

- Every React route mapped
- Menu/button/status validation
- Loading, empty, error and retry
- Keyboard and screen-reader smoke
- Responsive and browser compatibility
- Route refresh and direct access

**Speaker notes:** Route coverage matrix biến yêu cầu “every page/button/menu” thành checklist tự động hóa.

**Suggested visual:** Responsive device frames.

**Transition:** Morph to traceability.

**Time:** 1.5 phút.

### Slide 24 — Traceability and Coverage

- 40/40 use cases traced (35 full + 2 partial + 3 documented gap)
- 5/5 actor groups covered
- 100% route inventory
- 100% API-family inventory
- 28/28 tables + 1 view
- ≈92% designed functional-behavior coverage

**Speaker notes:** Phân biệt traceability (100%, mọi UC có Test ID) với functional coverage thật (≈92%, vì UC-14/15/17 chưa triển khai và UC-32/38 chỉ một phần — xem §9 và §13).

**Suggested visual:** Coverage dashboard with five gauges.

**Transition:** Fade.

**Time:** 1.5 phút.

### Slide 25 — Limitations and Open Decisions

- Speaking AI not integrated
- Redis not used by backend
- Production storage not validated
- SLA and file limits need agreement
- Some state/idempotency policies need clarification

**Speaker notes:** Các gap không bị che giấu; chúng quyết định expected result cuối cho một số boundary tests.

**Suggested visual:** Risk register table.

**Transition:** Fade.

**Time:** 1.5 phút.

### Slide 26 — Automation Roadmap

- Phase 1: critical P0 smoke
- Phase 2: API and database regression
- Phase 3: browser journeys
- Phase 4: security and concurrency
- Phase 5: compatibility and performance

**Speaker notes:** Đề xuất triển khai tăng dần để có giá trị sớm, tránh chờ toàn bộ suite.

**Suggested visual:** Five-stage roadmap.

**Transition:** Morph to conclusion.

**Time:** 1.5 phút.

### Slide 27 — Conclusion

- Complete test design baseline
- Role and permission isolation emphasized
- Business workflows fully traced
- Data integrity validated end-to-end
- Ready for automation implementation

**Speaker notes:** Chốt rằng suite đủ để bắt đầu sign-off có kiểm soát, chưa phải bằng chứng release cho đến khi chạy.

**Suggested visual:** Checklist with readiness indicator.

**Transition:** Fade.

**Time:** 1 phút.

### Slide 28 — Q&A

- Questions
- Coverage challenges
- Automation priorities
- Ownership and timeline

**Speaker notes:** Mời Product, Dev và QA chốt open decisions, ưu tiên P0 và lịch triển khai.

**Suggested visual:** Minimal question-mark graphic.

**Transition:** None.

**Time:** 3 phút.

---

## 17. Presentation Section-to-Slide Mapping

| Markdown section | Slide |
| :--- | :--- |
| Title/metadata | 1 |
| Mục tiêu và phạm vi | 4–5 |
| Tác nhân, tính năng và quyền | 6 |
| Chiến lược và mức kiểm thử | 8 |
| Môi trường và dữ liệu | 9 |
| Entry/Exit criteria | 5, 24 |
| Public và Authentication tests | 10 |
| Student tests | 12–14 |
| Staff tests | 15 |
| StaffManager tests | 16–17 |
| Admin tests | 18 |
| Cross-role/Security/Reliability | 11, 21–22 |
| UI Route Coverage | 23 |
| API Coverage | 20 |
| Workflow/State transitions | 13, 16, 22 |
| Requirement Traceability | 24 |
| Database Validation | 19 |
| Coverage Review/Summary | 24 |
| Untested/Duplicate/Missing edges | 25 |
| Automation Roadmap | 26 |
| Conclusion | 27 |
| Q&A | 28 |

Tất cả heading H1–H3 của tài liệu được ánh xạ tới ít nhất một slide hoặc được trình bày như nội dung chi tiết hỗ trợ cho slide tương ứng.

---

## 18. Presentation Coverage Report

| Kiểm tra | Kết quả |
| :--- | :--- |
| Logical flow preserved | PASS |
| Important test-suite sections represented | PASS |
| Architecture slide | Slide 7–8 |
| Roles/use cases | Slide 6, 10–18 |
| Workflows/state machines | Slide 13, 16, 22 |
| Database/ER | Slide 19 |
| API | Slide 20 |
| Security | Slide 21 |
| Testing/coverage | Slide 8–24 |
| Deployment/environment | Slide 9 |
| Limitations | Slide 25 |
| Future improvements/roadmap | Slide 26 |
| Conclusion/Q&A | Slide 27–28 |
| Speaker notes | 28/28 slides |
| Suggested visuals | 28/28 slides |
| Transition suggestions | 28/28 slides |
| Time estimates | 28/28 slides |

**Tổng thời lượng ước tính:** khoảng **46,5 phút**, gồm 43,5 phút trình bày và 3 phút Q&A.

**Coverage xác nhận:** 100% nội dung quan trọng của System Test Specification đã được chuyển thành presentation outline; các bảng test chi tiết được tham chiếu thay vì dán nguyên văn lên slide.

---

## PHẦN C — TEST EXECUTION REPORT

## 19. Thông tin lần chạy

| Thuộc tính | Kết quả |
| :--- | :--- |
| Thời điểm | 26/07/2026, Asia/Saigon |
| Source under test | Working tree hiện tại, bao gồm các thay đổi chưa commit |
| Java | 21.0.11 |
| Maven | 3.9.16 |
| Node.js | 24.16.0 |
| npm | 11.13.0 |
| Backend test database | H2 in-memory |
| Docker/MySQL system runtime | Không khả dụng — máy không cài Docker |
| Browser E2E runner | Không có Playwright/Cypress trong repo |

> Báo cáo này không thay đổi source code. Kết quả phản ánh chính xác working tree tại thời điểm chạy, không nhất thiết trùng với branch đã commit.

---

## 20. Kết quả thực thi

| Hạng mục | Lệnh/hoạt động | Kết quả | Bằng chứng |
| :--- | :--- | :--- | :--- |
| Backend unit + integration | `mvn test jacoco:report` | **PASS** | 120 tests; 0 failures; 0 errors; 0 skipped |
| Backend package | `mvn package -DskipTests` | **PASS** | Tạo `target/jlpt-backend-2.0.0.jar` |
| Backend coverage report | JaCoCo | **PASS** | `target/site/jacoco/index.html` |
| Frontend unit/component | `npm.cmd run test` | **PASS** | 18 tests thuộc 5 files |
| Frontend lint | `npm.cmd run lint` | **PASS** | 0 error, 0 warning |
| Frontend production build | `npm.cmd run build` | **PASS** | 384 modules transformed |
| Frontend static preview | Vite preview port 4173 | **PASS (static only)** | `/`, auth và protected route paths đều trả SPA shell HTTP 200 |
| Full-stack Docker | `docker compose` | **BLOCKED** | `docker` command không tồn tại |
| MySQL 8.4/Flyway clean install | Theo SEC-011 | **NOT EXECUTED** | Không có Docker/MySQL test runtime |
| Browser route/menu/button E2E | Theo UI matrix | **NOT EXECUTED** | Chưa có Playwright/Cypress |
| Cross-role API matrix | SEC-001..SEC-003 | **PARTIAL** | Một số auth/assessment integration tests; chưa quét mọi endpoint |
| External OAuth/SMTP/storage | Contract/system test | **PARTIAL/NOT EXECUTED** | Email dùng mock; OAuth/storage thật chưa chạy |

### 20.1. Backend test classes

| Nhóm | Test classes | Số test |
| :--- | :--- | ---: |
| Admin | `AdminUserServiceTest`, `AdminUserServiceChangeRoleTest` | 14 |
| Assessment | `AssessmentControllerIntegrationTest`, `MockExamControllerIntegrationTest`, `MockExamServiceTest` | 23 |
| Authentication/Security | `AuthControllerIntegrationTest`, `AuthenticationServiceTest`, `JwtProviderTest` | 13 |
| Content review/publish | `AssessmentContentHandlerTest`, `ManagerDeletedContentServiceTest` | 8 |
| Flashcard | `FlashcardSrsServiceSm2Test` | 18 |
| Notification/Email | `NotificationDispatcherTest`, `EmailServiceTest` | 11 |
| Speaking | `SpeakingAuthoringServiceTest` | 5 |
| Staff content | Exam/quiz review, vocabulary topic tests | 12 |
| Student course | `CourseServiceTest` | 4 |
| Support | `SupportTicketAssignTest`, `SupportTicketServiceTest` | 12 |
| **Tổng** | 21 test classes | **120** |

### 20.2. Frontend tests

| Test file | Số test | Kết quả |
| :--- | ---: | :--- |
| `apiMessage.test.js` | 6 | PASS |
| `authSlice.test.js` | 6 | PASS |
| `useCountdown.test.js` | 3 | PASS |
| `Register.test.jsx` | 1 | PASS |
| `Login.test.jsx` | 2 | PASS |
| **Tổng** | **18** | **PASS** |

---

## 21. Coverage thực tế

### 21.1. Backend JaCoCo

| Metric | Covered / Total | Coverage |
| :--- | ---: | ---: |
| Instructions | 8.300 / 33.543 | 24,74% |
| Branches | 339 / 2.482 | 13,66% |
| Lines | 1.955 / 7.666 | 25,50% |
| Complexity | 501 / 2.648 | 18,92% |
| Methods | 415 / 1.383 | 30,01% |
| Classes analyzed | 269 | N/A |

### 21.2. SystemTest.md execution coverage

| Dimension | Designed | Fully executed trong lần này |
| :--- | ---: | ---: |
| Detailed Test IDs | 137 | Chưa có full-system runner để thực thi nguyên vẹn |
| Use cases | 40/40 mapped | Một phần được unit/integration xác nhận |
| UI routes | 100% mapped | Chỉ static SPA-shell smoke; chưa browser behavior |
| API families | 100% mapped | Chỉ các API có integration tests hiện hữu |
| Database | 28 tables + 1 view mapped | H2 test subset; chưa MySQL/Flyway system validation |

Không được dùng con số 138 automated tests để suy ra 137 System Test IDs đã PASS. Hai tập test có mục tiêu và độ sâu khác nhau.

---

## 22. Test ID được xác nhận một phần bởi automation hiện có

| Test ID/nhóm | Bằng chứng hiện có | Mức xác nhận |
| :--- | :--- | :--- |
| AUTH-007, AUTH-008 | Auth controller/service tests | API/service partial |
| AUTH-009, AUTH-011 | Account lock và temp-password service tests | Service partial |
| AUTH-021 | Malformed JWT test | Security unit partial |
| STU-018..STU-024 | Assessment/mock-exam controller/service tests | API integration mạnh |
| STU-025, STU-026 | Flashcard SM-2 tests | Algorithm/service mạnh |
| STU-035, STF-019, MGR-012 | Support ticket create/close/assign tests | Service partial |
| STF-009, MGR-011 | Vocabulary topic delete/restore tests | Service partial |
| STF-013 | Speaking authoring test | Service partial |
| STF-017 | Quiz/exam submit-review tests | Service partial |
| MGR-003..MGR-005 | Assessment content handler tests | Handler partial |
| ADM-003..ADM-010 | Admin user service tests | Service partial |
| SEC-016 | Email retry/outbox + notification dispatch tests | Service strong |

Các test UI, route guard trình duyệt, cross-role toàn endpoint, MySQL constraint và external integration vẫn cần chạy riêng.

---

## 23. Phát hiện và cảnh báo

| ID | Mức độ | Phát hiện | Ảnh hưởng/đề xuất |
| :--- | :--- | :--- | :--- |
| OBS-001 | High | Backend line coverage chỉ 25,50%, branch 13,66% | Chưa đạt mục tiêu Definition of Done 80%; ưu tiên các P0/P1 chưa có test |
| OBS-002 | High | Không có browser E2E suite | Chưa xác nhận menu/button/form/redirect/role guard theo UI matrix |
| OBS-003 | High | Không có Docker trên máy chạy | Chưa xác nhận MySQL 8.4, Flyway, charset, UTC và full-stack contracts |
| OBS-004 | Medium | Frontend main JS chunk 753,43 kB (gzip 241,42 kB) | Theo dõi performance; tiếp tục lazy-load/tách bundle |
| OBS-005 | Medium | Test context cảnh báo `spring.jpa.open-in-view` đang bật | Cân nhắc tắt rõ ràng để tránh query ngoài transaction/N+1 |
| OBS-006 | Low | Spring test log cảnh báo `commons-logging.jar` conflict | Dọn dependency nếu xuất hiện trong production classpath |
| OBS-007 | Medium | Console test hiển thị một số tiếng Việt bị mojibake | Kiểm tra encoding console/log pipeline; API/database UTF-8 cần test MySQL thật |
| OBS-008 | Medium | Test chạy default profile và `DevDataSeeder` tạo demo credentials | Nên dùng profile `test`, tắt seeder và tránh ghi credentials vào log |

Các log ERROR trong `EmailServiceTest`, `NotificationDispatcherTest`, `JwtProviderTest` và score-invariant test là negative-path được chủ động tạo; chúng không làm test fail.

---

## 24. Release Assessment

**Kết luận lần chạy: CONDITIONAL — BUILD/UNIT/INTEGRATION BASELINE PASS, FULL SYSTEM SIGN-OFF CHƯA ĐỦ.**

- Backend tests/package: PASS.
- Frontend tests/lint/build: PASS.
- Không phát hiện test failure trong 138 automated tests hiện có.
- Không thể kết luận 137 System Test IDs PASS vì chưa có browser E2E và full-stack MySQL runtime.
- Chưa nên production sign-off trước khi hoàn thành tối thiểu:
  1. Docker/Testcontainers MySQL 8.4 + Flyway validation.
  2. Playwright P0 cho Auth, Student assessment, Staff→Manager review và Admin user lifecycle.
  3. Cross-role 401/403 matrix.
  4. Profile/upload/notebook/dictionary/speaking/notification/Admin settings integration tests.
  5. Coverage gate tăng dần từ line 40%/branch 25%, sau đó tới 80%.
