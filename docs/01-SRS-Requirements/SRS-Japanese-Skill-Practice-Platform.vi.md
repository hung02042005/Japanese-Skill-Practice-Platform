# ĐẶC TẢ YÊU CẦU PHẦN MỀM

**Project Name:** Japanese Skill Practice Platform  
**Version:** 1.0  
**Location/Date:** Hanoi, July 2026  
**Template:** `Guides  Templates-20260721/Template1_SRS Document.docx`

**Table of Contents**

- [I. Record of Changes](#i-record-of-changes)
- [II. Software Requirement Specification](#ii-software-requirement-specification)
- [1. Overall Requirements](#1-overall-requirements)
- [2. Use Case Specifications](#2-use-case-specifications)
- [3. Functional Requirements](#3-functional-requirements)
- [4. Non-Functional Requirements](#4-non-functional-requirements)
- [5. Requirement Appendix](#5-requirement-appendix)

# I. Record of Changes

| Date | A/M/D | In charge | Change Description |
| --- | --- | --- | --- |
| 2026-07-23 | A | AI Agent | Generated bilingual SRS according to Template1 DOCX structure. |
| 2026-07-23 | M | AI Agent | Revised to include missing Template1 sections: screen descriptions, fuller UC specifications, functional field descriptions, NFRs, appendix. |
| 2026-07-24 | M | AI Agent | Real Word Table of Contents field; split 1.2 into 6 sub-processes with swim-lane diagrams; added screen images to section 3 (real screenshots for Auth screens, wireframe mockups for screens requiring a logged-in session not reachable in the build environment). |
| 2026-07-24 | M | AI Agent | Expanded Use Case Specifications (section 2): longer step-by-step Normal Flow and named Alternative Flows for all 15 UC groups, written in simple, easy-to-understand English. |
| 2026-07-24 | M | AI Agent | Split 1.3.3 Use Case Diagrams into one diagram per actor (Guest, Student, Staff, StaffManager, Admin), matching Template1's per-actor layout; added Guest to the Actors table. |

*A - Added, M - Modified, D - Deleted*

# II. Software Requirement Specification

## 1. Overall Requirements

### 1.1 Context Diagram

Japanese Skill Practice Platform là hệ thống web học và luyện thi tiếng Nhật JLPT từ N5 đến N1, gồm React 18 frontend, Spring Boot 3/Java 21 backend và MySQL 8. Hệ thống hỗ trợ học Kanji, Kana, từ vựng, ngữ pháp, flashcard SRS, quiz, mock exam, AI OCR/Speech, hỗ trợ học viên, kiểm duyệt nội dung và quản trị.

![Context Diagram](../07-Release-Documents/diagrams/rds/package-diagram.png)

### 1.2 Main Business Processes

#### 1.2.1 Authentication and Account Lifecycle

Register, verify email, login, reset/change password, logout, token/session management.

![Authentication and Account Lifecycle](../07-Release-Documents/diagrams/rds/bp-1-authentication.png)

#### 1.2.2 Student Learning Path

Browse level content, unlock lessons by order, complete lessons, bookmark and track progress.

![Student Learning Path](../07-Release-Documents/diagrams/rds/bp-2-learning-path.png)

#### 1.2.3 Assessment and SRS Review

Take quizzes/mock exams, backend grading, immutable attempts, flashcard spaced repetition.

![Assessment and SRS Review](../07-Release-Documents/diagrams/rds/bp-3-assessment-srs.png)

#### 1.2.4 AI Practice Processing

Upload image/audio, create asynchronous AI jobs, timeout/retry/fallback, poll result.

![AI Practice Processing](../07-Release-Documents/diagrams/rds/bp-4-ai-processing.png)

#### 1.2.5 Content Governance

Staff drafts content, StaffManager reviews and publishes, audit and notification.

![Content Governance](../07-Release-Documents/diagrams/rds/bp-5-content-governance.png)

#### 1.2.6 System Administration

Manage users, settings, subscriptions, reports, and notification rules.

![System Administration](../07-Release-Documents/diagrams/rds/bp-6-system-admin.png)


### 1.3 User Requirements

#### 1.3.1 Actors

| # | Actor | Description |
| --- | --- | --- |
| 1 | Guest | Người dùng chưa đăng nhập: có thể đăng ký tài khoản, đăng nhập, hoặc đặt lại mật khẩu. |
| 2 | Student | Học viên cuối sử dụng hệ thống để học JLPT, luyện tập, thi thử, dùng AI OCR/Speech và theo dõi tiến độ. |
| 3 | Staff | Nhân viên nội dung quản lý học liệu, ngân hàng câu hỏi, bài quiz/exam, hỗ trợ học viên và chấm bài nói. |
| 4 | StaffManager | Quản lý nội dung, duyệt/từ chối/yêu cầu sửa, xuất bản/ẩn/lưu trữ nội dung do Staff gửi. |
| 5 | Admin | Quản trị viên hệ thống, quản lý tài khoản, cấu hình, báo cáo, notification rules và audit. |
| 6 | External AI Service | Dịch vụ OCR và Speech Recognition được gọi bất đồng bộ để trả kết quả gợi ý. |
| 7 | SMTP Server | Dịch vụ gửi email xác minh tài khoản, đặt lại mật khẩu và thông báo. |


#### 1.3.2 Use Cases (UC)

| ID | Use Case | Feature | Use Case Description |
| --- | --- | --- | --- |
| UC-01 | User Login | Authentication | Đăng nhập bằng email/password hoặc Google OAuth, nhận JWT và chuyển tới đúng dashboard theo role. |
| UC-02 | User Register | Authentication | Tạo tài khoản Student, gửi OTP xác minh email và kích hoạt tài khoản. |
| UC-03 | Reset Password | Authentication | Yêu cầu email đặt lại mật khẩu, xác minh token và cập nhật mật khẩu mới. |
| UC-04 | User Profile | Student Account | Xem và cập nhật hồ sơ cá nhân, avatar, số điện thoại, cấp độ JLPT mục tiêu. |
| UC-05 | Change Password | Student Account | Đổi mật khẩu khi đang đăng nhập sau khi xác thực mật khẩu hiện tại. |
| UC-06 | Learn Grammar | Core Learning | Học ngữ pháp theo JLPT level với cấu trúc, giải thích, ví dụ và tiến độ. |
| UC-07 | Learn Kanji | Core Learning | Học Kanji theo level, nghĩa, onyomi/kunyomi, số nét và ví dụ. |
| UC-08 | Learn Kana | Core Learning | Học Hiragana/Katakana với âm đọc và bài luyện nhận diện. |
| UC-09 | Vocabulary | Core Learning | Học từ vựng theo lesson/topic/level và thêm vào flashcard/notebook. |
| UC-10 | Take JLPT Mock Test | Assessment | Làm đề thi thử có thời gian; backend chấm điểm và lưu attempt mới. |
| UC-11 | Practice & Quiz | Assessment | Làm quiz theo chủ đề/bài học; backend tính điểm và trả kết quả. |
| UC-12 | Flashcard Learning | SRS Review | Ôn tập flashcard theo thuật toán spaced repetition và cập nhật lịch ôn. |
| UC-13 | Speaking Practice & AI Grading | AI Skills | Nộp audio luyện nói, nhận job_id, AI chấm gợi ý và Staff có thể override. |
| UC-14 | Reading Practice | Skills Practice | Luyện đọc hiểu theo cấp độ và lưu kết quả luyện tập. |
| UC-15 | Listening Practice | Skills Practice | Luyện nghe hiểu với audio và câu hỏi liên quan. |
| UC-16 | Dictionary & Search | Learning Tools | Tra cứu từ vựng/ngữ pháp/Kanji và lọc theo cấp độ. |
| UC-17 | Bookmark Learning | Learning Tools | Đánh dấu nội dung yêu thích hoặc cần ôn lại. |
| UC-18 | Logout | Authentication | Đăng xuất, thu hồi/huỷ token phiên hiện tại. |
| UC-19 | Learning Progress & Stats | Analytics | Xem tiến độ học, streak, tỷ lệ hoàn thành và kết quả luyện tập. |
| UC-20 | AI Handwriting Practice | AI Skills | Nộp ảnh/canvas Kanji viết tay, nhận similarity % bất đồng bộ. |
| UC-21 | View Student Progress | Staff Student Management | Staff xem tiến độ học viên phục vụ hỗ trợ và tư vấn. |
| UC-22 | Manage Student Accounts | Staff Student Management | Staff quản lý thông tin học viên trong phạm vi được phân quyền. |
| UC-23 | Suspend or Activate Account | Staff Student Management | Khoá/mở tài khoản học viên theo quy trình và audit. |
| UC-24 | Manage Question Bank | Content Management | Tạo/sửa/lưu nháp câu hỏi, đáp án, cấp độ, kỹ năng. |
| UC-25 | Manage Grammar Content | Content Management | Quản lý điểm ngữ pháp và gửi duyệt trước khi publish. |
| UC-26 | Manage Quiz | Content Management | Quản lý quiz; khi đã có attempt thì lock câu hỏi hoặc tạo version mới. |
| UC-27 | Manage Learning Content | Content Management | Quản lý course/lesson/Kanji/Kana/vocab/reading/listening/speaking. |
| UC-28 | Manage JLPT Mock Exams | Content Management | Tạo và quản lý đề thi thử JLPT theo cấp độ và kỹ năng. |
| UC-29 | Respond to Student Support | Support | Tiếp nhận và phản hồi ticket hỗ trợ của học viên. |
| UC-30 | Send Notifications | Notification | Gửi thông báo in-app/email tới học viên hoặc nhóm học viên. |
| UC-31 | Grade Speaking Submission | Manual Grading | Nghe audio, xem AI suggestion, nhập final_score và feedback. |
| UC-32 | View Quiz Results | Analytics | Xem kết quả quiz/mock test và phân tích câu hỏi. |
| UC-33 | Review Submitted Content | Content Review | StaffManager duyệt/từ chối/yêu cầu sửa nội dung đang pending_review. |
| UC-34 | Manage Published Content Status | Content Review | Publish/unpublish/archive/restore nội dung đã duyệt. |
| UC-35 | Login System | Admin | Admin đăng nhập vào khu vực quản trị bằng tài khoản được cấp. |
| UC-36 | View Dashboard | Admin | Xem tổng quan hệ thống, người dùng, nội dung, lượt học và bài thi. |
| UC-37 | User Management | Admin | Quản lý tài khoản Student/Staff/StaffManager/Admin và phân quyền. |
| UC-38 | Report Screen | Admin | Xem và xuất báo cáo học tập, sử dụng hệ thống và nội dung. |
| UC-39 | Settings | Admin | Cấu hình hệ thống, SMTP, session, maintenance và tham số vận hành. |
| UC-40 | Notification Rules | Admin | Quản lý rule/template thông báo tự động theo sự kiện. |


#### 1.3.3 Use Case Diagrams

##### 1.3.3.1 UCs for Guest

![UCs for Guest](../07-Release-Documents/diagrams/rds/uc-guest.png)

##### 1.3.3.2 UCs for Student

![UCs for Student](../07-Release-Documents/diagrams/rds/uc-student.png)

##### 1.3.3.3 UCs for Staff

![UCs for Staff](../07-Release-Documents/diagrams/rds/uc-staff.png)

##### 1.3.3.4 UCs for StaffManager

![UCs for StaffManager](../07-Release-Documents/diagrams/rds/uc-staffmanager.png)

##### 1.3.3.5 UCs for Admin

![UCs for Admin](../07-Release-Documents/diagrams/rds/uc-admin.png)

### 1.4 System Functionalities

#### 1.4.1 Screens Flow

![Screens Flow](../07-Release-Documents/diagrams/rds/screens-flow.png)

**Screen Descriptions**

| # | Feature | Screen (Route) | Description |
| --- | --- | --- | --- |
| 1 | Public & Auth | /, /login, /register, /forgot-password, /reset-password, /verify-email | Trang vào hệ thống, xác thực, đăng ký, xác minh email và khôi phục mật khẩu. |
| 2 | Student Dashboard | /dashboard, /onboarding, /profile, /settings/* | Tổng quan học tập, hồ sơ, thiết lập mục tiêu và cài đặt tài khoản. |
| 3 | Core Learning | /lessons/:id, /kanji, /kanji/:id, /grammar, /kana, /vocabulary | Học Kanji, Kana, từ vựng, ngữ pháp và bài học theo cấp độ JLPT. |
| 4 | Assessment & Review | /quiz, /mock-test, /mock-test/:id/attempt, /mock-test/:id/results, /vocabulary/flashcard, /notebook | Làm quiz/mock test, xem kết quả, ôn flashcard SRS và sổ tay. |
| 5 | AI Skills | /speaking, /kanji/:id/practice | Luyện nói bằng audio và luyện viết tay Kanji bằng OCR/similarity. |
| 6 | Support & Notification | /support, /support/tickets/:id, /notifications | Gửi ticket hỗ trợ, theo dõi phản hồi và xem thông báo. |
| 7 | Staff Area | /staff/* | Quản lý nội dung, câu hỏi, bài đánh giá, chấm speaking, ticket và học viên. |
| 8 | Manager Area | /manager/* | Duyệt nội dung, theo dõi pipeline, quản lý nội dung đã xóa mềm và ticket. |
| 9 | Admin Area | /admin/* | Quản lý người dùng, cài đặt hệ thống, báo cáo và notification rules. |


#### 1.4.2 Screen Authorization

| Screen | Student | Staff | StaffManager | Admin | Notes |
| --- | --- | --- | --- | --- | --- |
| Public | X | X | X | X | Login/register/forgot/reset/home; public API only for /api/auth/* |
| Student dashboard & learning | X |  |  |  | Requires STUDENT role plus subscription/level checks. |
| Quiz, mock test, flashcard, AI practice | X |  |  |  | Score/time/AI validation handled server-side. |
| Staff dashboard/content/questions/assessments/grading |  | X |  |  | Requires STAFF role. |
| Manager review queue/content pipeline |  |  | X | X | Requires StaffManager or Admin; service layer validates staff_role. |
| Admin users/settings/reports/notification rules |  |  |  | X | Requires ADMIN role. |


#### 1.4.3 Non-UI Functions

| # | Feature | System Function | Description |
| --- | --- | --- | --- |
| 1 | Authentication | AuthEventListener | Gửi email xác minh tài khoản và reset password bất đồng bộ. |
| 2 | Speaking | SpeakingAsyncProcessor | Xử lý bài nộp speaking sau khi nhận request; trả job/status để frontend poll. |
| 3 | Notification | NotificationDispatcher | Fan-out thông báo in-app/email và retry email trong outbox. |
| 4 | Assessment | Quiz/Exam scoring service | Tính điểm, validate thời gian, tạo attempt mới, ghi audit. |
| 5 | Subscription | Subscription validation service | Kiểm tra quyền VIP real-time/cache tối đa 5 phút. |


### 1.5 Entity Relationship Diagram

![Entity Relationship Diagram](../07-Release-Documents/diagrams/rds/er-diagram.png)

**Entities Description**

| # | Entity | Description |
| --- | --- | --- |
| 1 | student_users | Tài khoản học viên, OAuth, cấp độ JLPT, subscription và thống kê học tập. |
| 2 | staff_users | Tài khoản Staff và StaffManager, vai trò nội bộ, trạng thái. |
| 3 | admin_users | Tài khoản quản trị hệ thống. |
| 4 | auth_tokens | Session, refresh token, email verification, password reset. |
| 5 | courses | Khóa học JLPT theo level, trạng thái, VIP flag. |
| 6 | lessons | Bài học thuộc course, gồm lesson/reading/listening/speaking. |
| 7 | kanji / kana_characters / vocabulary / grammar_points | Nội dung học cốt lõi. |
| 8 | questions / assessments / question_assignments | Ngân hàng câu hỏi, quiz và đề thi thử. |
| 9 | test_attempts / attempt_answers | Lịch sử làm bài và câu trả lời. |
| 10 | student_submissions | Bài speaking/handwriting, AI score suggestion và final score. |
| 11 | student_content_progress / flashcards | Tiến độ học, bookmark và SRS flashcard. |
| 12 | tickets / ticket_replies / notifications / system_settings / admin_audit_logs | Hỗ trợ, thông báo, cấu hình và audit. |


## 2. Use Case Specifications

### 2.1 Authentication

#### 2.1.1 UC-01 User Login

| Primary Actors | Student, Staff, StaffManager, Admin | Secondary Actors | Google OAuth Provider |
| --- | --- | --- | --- |
| Description | Người dùng mở trang đăng nhập, nhập email và mật khẩu (hoặc bấm Đăng nhập bằng Google) để vào hệ thống. Sau khi đăng nhập thành công, người dùng được vào đúng khu vực chức năng theo vai trò của mình. |  |  |
| Preconditions | Tài khoản đã tồn tại, đang hoạt động (active) và không bị xóa mềm. |  |  |
| Postconditions | Người dùng đăng nhập thành công, hệ thống lưu lại lượt đăng nhập, và người dùng được đưa tới đúng trang chủ theo vai trò. |  |  |
| Normal Sequence/Flow | 1. Người dùng mở trang Đăng nhập.<br>2. Người dùng nhập email và mật khẩu.<br>3. Người dùng bấm nút Đăng nhập.<br>4. Hệ thống kiểm tra email và mật khẩu có đúng không.<br>5. Hệ thống kiểm tra tài khoản còn hoạt động, chưa bị khóa.<br>6. Hệ thống tạo JWT token và refresh token.<br>7. Hệ thống lưu lại lượt đăng nhập vào nhật ký (audit log).<br>8. Hệ thống đưa người dùng tới đúng trang chủ theo vai trò (Student/Staff/StaffManager/Admin). |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Đăng nhập bằng Google: Người dùng bấm 'Đăng nhập bằng Google' thay vì nhập mật khẩu. Hệ thống nhờ Google xác minh người dùng, nếu đúng thì quay lại bước 6 của luồng chính.<br>Nhánh 2 - Sai email hoặc mật khẩu: Hệ thống hiện thông báo 'Email hoặc mật khẩu không đúng' và cho phép thử lại.<br>Nhánh 3 - Email chưa xác minh: Hệ thống yêu cầu người dùng xác minh email trước khi đăng nhập.<br>Nhánh 4 - Tài khoản bị khóa: Nếu nhập sai mật khẩu 5 lần liên tiếp, hệ thống khóa tài khoản 15 phút và hiện cảnh báo.<br>Nhánh 5 - Không đủ quyền truy cập trang: Nếu người dùng đã đăng nhập nhưng mở một trang không thuộc vai trò của mình, hệ thống hiện 'Bạn không có quyền truy cập'. |  |  |

#### 2.1.2 UC-02 User Register

| Primary Actors | Student | Secondary Actors | SMTP Server |
| --- | --- | --- | --- |
| Description | Một người dùng mới (Guest) tạo tài khoản Student bằng cách nhập họ tên, email và mật khẩu. Hệ thống kiểm tra thông tin rồi gửi một mã OTP tới email để xác minh email đó là thật. |  |  |
| Preconditions | Email chưa được dùng để đăng ký tài khoản nào khác. |  |  |
| Postconditions | Một tài khoản Student mới được tạo. Tài khoản ở trạng thái chưa xác minh cho tới khi người dùng nhập đúng mã OTP; sau đó tài khoản chuyển sang trạng thái hoạt động. |  |  |
| Normal Sequence/Flow | 1. Người dùng mở trang Đăng ký.<br>2. Người dùng nhập họ tên, email, mật khẩu và xác nhận mật khẩu.<br>3. Người dùng bấm nút Đăng ký.<br>4. Hệ thống kiểm tra tất cả các trường hợp lệ (đúng định dạng email, mật khẩu đủ mạnh, hai mật khẩu khớp nhau).<br>5. Hệ thống kiểm tra email chưa từng được dùng.<br>6. Hệ thống tạo tài khoản mới với trạng thái 'chưa xác minh'.<br>7. Hệ thống tạo mã OTP gồm 6 chữ số kèm thời hạn sử dụng.<br>8. Hệ thống gửi mã OTP tới email của người dùng.<br>9. Người dùng nhập mã OTP vào trang Xác minh Email.<br>10. Hệ thống kiểm tra mã OTP đúng và kích hoạt tài khoản. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Email đã được đăng ký: Hệ thống hiện 'Email này đã được sử dụng' và không tạo tài khoản mới.<br>Nhánh 2 - Mã OTP sai hoặc đã hết hạn: Hệ thống hiện lỗi và cho phép nhập lại hoặc gửi lại mã mới.<br>Nhánh 3 - Gửi lại OTP quá nhiều lần: Nếu người dùng xin gửi lại mã quá nhiều lần trong thời gian ngắn, hệ thống tạm chặn yêu cầu mới (rate limit). |  |  |

#### 2.1.3 UC-03 Reset Password

| Primary Actors | Student, Guest | Secondary Actors | SMTP Server |
| --- | --- | --- | --- |
| Description | Người dùng quên mật khẩu có thể yêu cầu hệ thống gửi một email chứa link đặt lại mật khẩu, sau đó chọn mật khẩu mới. |  |  |
| Preconditions | Email nhập vào thuộc về một tài khoản đang tồn tại và đang hoạt động. |  |  |
| Postconditions | Mật khẩu được đổi và lưu dưới dạng hash mới. Token/link reset cũ không thể dùng lại lần nữa. |  |  |
| Normal Sequence/Flow | 1. Người dùng mở trang Quên mật khẩu.<br>2. Người dùng nhập email và bấm Gửi.<br>3. Hệ thống kiểm tra email có tồn tại không.<br>4. Hệ thống tạo một reset token kèm thời hạn sử dụng.<br>5. Hệ thống gửi email chứa link đặt lại mật khẩu (có kèm token).<br>6. Người dùng bấm vào link và mở trang Đặt lại mật khẩu.<br>7. Người dùng nhập mật khẩu mới và xác nhận lại.<br>8. Hệ thống kiểm tra token còn hợp lệ, chưa hết hạn.<br>9. Hệ thống lưu mật khẩu mới (đã hash) và đánh dấu token đã dùng. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Token hết hạn hoặc đã dùng rồi: Hệ thống báo lỗi và yêu cầu người dùng xin link reset mới.<br>Nhánh 2 - Mật khẩu mới quá yếu: Hệ thống hiện rõ quy tắc mật khẩu và yêu cầu chọn mật khẩu mạnh hơn.<br>Nhánh 3 - Tài khoản đang bị khóa: Hệ thống vẫn cho đổi mật khẩu, nhưng báo rõ tài khoản vẫn bị khóa cho tới khi hết thời gian khóa. |  |  |

### 2.2 Student Learning

#### 2.2.1 UC-06/07/08/09 Learn Content

| Primary Actors | Student | Secondary Actors | None |
| --- | --- | --- | --- |
| Description | Student duyệt xem nội dung học (Ngữ pháp, Kanji, Kana, hoặc Từ vựng) theo cấp độ JLPT của mình. Student có thể đọc bài học, nghe audio, xem ví dụ, rồi đánh dấu hoàn thành hoặc bookmark để xem lại sau. |  |  |
| Preconditions | Student đã đăng nhập và có quyền truy cập đúng cấp độ JLPT (khớp với subscription/level của mình). |  |  |
| Postconditions | Nếu Student hoàn thành bài học, tiến độ học tập được cập nhật (chỉ tăng, không bao giờ giảm). Nếu Student bookmark, nội dung được lưu vào danh sách của Student. |  |  |
| Normal Sequence/Flow | 1. Student mở menu học tập (Ngữ pháp, Kanji, Kana, hoặc Từ vựng).<br>2. Student chọn cấp độ JLPT và chủ đề.<br>3. Hệ thống kiểm tra subscription và level của Student.<br>4. Hệ thống kiểm tra bài học trước đó (theo thứ tự) đã hoàn thành chưa.<br>5. Hệ thống hiển thị nội dung bài học (chữ, audio, hình ảnh, ví dụ).<br>6. Student đọc/nghe nội dung.<br>7. Student bấm 'Đánh dấu hoàn thành' hoặc 'Bookmark'.<br>8. Hệ thống lưu tiến độ hoặc bookmark của Student. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Nội dung VIP, tài khoản Free: Hệ thống hiện thông báo cần nâng cấp VIP mới xem được, kèm link nâng cấp.<br>Nhánh 2 - Bài học trước chưa hoàn thành: Hệ thống hiện bài học đang bị khóa và giải thích cần hoàn thành bài nào trước.<br>Nhánh 3 - Nội dung chưa được publish: Hệ thống không hiển thị bài học này (Staff/Manager chưa duyệt/publish). |  |  |

#### 2.2.2 UC-12 Flashcard Learning

| Primary Actors | Student | Secondary Actors | None |
| --- | --- | --- | --- |
| Description | Student ôn tập bằng flashcard theo phương pháp lặp lại ngắt quãng (SRS). Hệ thống chọn ra các thẻ đến hạn ôn, Student trả lời hoặc tự đánh giá mức độ nhớ, rồi hệ thống tính ngày ôn tiếp theo. |  |  |
| Preconditions | Student có ít nhất một bộ flashcard (deck) còn hoạt động, chưa bị xóa. |  |  |
| Postconditions | Ngày ôn tiếp theo (next_review_at) và hệ số dễ nhớ (ease factor) của từng thẻ đã ôn được cập nhật theo câu trả lời của Student. |  |  |
| Normal Sequence/Flow | 1. Student mở trang Flashcard/Sổ tay.<br>2. Hệ thống tìm các thẻ đến hạn ôn hôm nay.<br>3. Hệ thống hiện từng thẻ một (mặt câu hỏi trước).<br>4. Student cố nhớ câu trả lời, rồi lật thẻ để kiểm tra.<br>5. Student tự đánh giá (ví dụ: Quên, Khó, Tốt, Dễ).<br>6. Hệ thống tính ngày ôn tiếp theo theo công thức SRS.<br>7. Hệ thống lưu lịch ôn mới và chuyển sang thẻ kế tiếp. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Không có thẻ nào đến hạn: Hệ thống hiện thông báo thân thiện như 'Hôm nay không có thẻ cần ôn' và gợi ý học nội dung mới.<br>Nhánh 2 - Deck đã bị xóa: Hệ thống báo cho Student biết bộ thẻ này không còn khả dụng. |  |  |

### 2.3 Assessment

#### 2.3.1 UC-10 Take JLPT Mock Test

| Primary Actors | Student | Secondary Actors | None |
| --- | --- | --- | --- |
| Description | Student làm một đề thi thử JLPT đầy đủ, có tính giờ. Hệ thống theo dõi thời gian trên server, thu thập câu trả lời của Student, và tính điểm sau khi Student nộp bài hoặc khi hết giờ. |  |  |
| Preconditions | Đề thi đã được publish và phù hợp với cấp độ JLPT/subscription của Student. |  |  |
| Postconditions | Một bản ghi attempt mới được lưu kèm điểm số, và bản ghi này không thể bị sửa sau đó (bất biến). |  |  |
| Normal Sequence/Flow | 1. Student mở danh sách Mock Test và chọn một đề.<br>2. Hệ thống kiểm tra Student có đủ quyền truy cập (level/subscription).<br>3. Hệ thống tạo một attempt mới và bắt đầu đếm giờ phía server.<br>4. Student trả lời câu hỏi lần lượt theo từng phần.<br>5. Student bấm Nộp bài (hoặc hết giờ).<br>6. Hệ thống kiểm tra thời gian thực tế đã dùng trên server (không dựa vào đồng hồ máy Student).<br>7. Hệ thống tính điểm từng phần và tổng điểm.<br>8. Hệ thống lưu attempt ở trạng thái bất biến (không thể sửa sau này).<br>9. Hệ thống hiển thị màn hình kết quả cho Student. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Hết giờ: Hệ thống tự động nộp các câu Student đã chọn tới thời điểm đó.<br>Nhánh 2 - Student cố nộp bài 2 lần: Hệ thống chặn lần nộp thứ hai và hiện lại kết quả đã có.<br>Nhánh 3 - Đề bị unpublish trong lúc đang làm: Hệ thống vẫn cho Student hoàn thành attempt đang làm dở, nhưng đề sẽ không xuất hiện cho lượt làm mới.<br>Nhánh 4 - Thiếu quyền VIP: Hệ thống chặn Student bắt đầu làm bài và hiện thông báo nâng cấp. |  |  |

#### 2.3.2 UC-11 Practice & Quiz

| Primary Actors | Student | Secondary Actors | None |
| --- | --- | --- | --- |
| Description | Student làm một bài quiz ngắn về một bài học/chủ đề, dùng để luyện tập hằng ngày, không phải thi thử đầy đủ. Hệ thống chấm điểm ngay sau khi nộp và hiện kết quả. |  |  |
| Preconditions | Quiz đã được publish. Các câu hỏi trong quiz đúng cấp độ JLPT của Student. |  |  |
| Postconditions | Một attempt mới và kết quả được lưu và hiển thị cho Student. |  |  |
| Normal Sequence/Flow | 1. Student mở một bài học/chủ đề và bấm 'Bắt đầu Quiz'.<br>2. Hệ thống gửi danh sách câu hỏi (không kèm đáp án đúng).<br>3. Student trả lời từng câu.<br>4. Student bấm Nộp bài.<br>5. Hệ thống kiểm tra đáp án và tính điểm trên server.<br>6. Hệ thống lưu attempt mới.<br>7. Hệ thống hiện điểm số và câu nào đúng/sai. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Quiz đã bị archive: Hệ thống không cho làm bài mới và hiện thông báo.<br>Nhánh 2 - Câu hỏi bị khóa sau lần làm đầu tiên: Nếu Staff cố sửa một quiz đã có người làm, hệ thống chặn sửa và yêu cầu tạo phiên bản mới.<br>Nhánh 3 - Dữ liệu nhập không hợp lệ: Nếu thiếu câu trả lời hoặc sai định dạng, hệ thống báo lỗi và không cho nộp bài. |  |  |

### 2.4 AI Skills

#### 2.4.1 UC-13 Speaking Practice & AI Grading

| Primary Actors | Student | Secondary Actors | AI Speech Service, Staff |
| --- | --- | --- | --- |
| Description | Student ghi âm hoặc tải lên một file audio trả lời cho bài luyện nói. Hệ thống gửi audio cho dịch vụ AI xử lý ở chế độ nền, sau đó hiện điểm AI gợi ý. Một Staff có thể xem lại và xác nhận điểm cuối cùng. |  |  |
| Preconditions | File audio hợp lệ (đúng định dạng/kích thước). Bài luyện nói đã được publish. |  |  |
| Postconditions | Một bản ghi submission được lưu. Trạng thái AI luôn rõ ràng (PENDING, PROCESSING, DONE, hoặc FAILED) để Student luôn biết chuyện gì đang xảy ra. |  |  |
| Normal Sequence/Flow | 1. Student ghi âm hoặc tải lên file audio cho bài luyện nói.<br>2. Student bấm Nộp bài.<br>3. Hệ thống lưu file audio và tạo một job với trạng thái PENDING.<br>4. Hệ thống trả lời ngay cho Student kèm job_id (không bắt Student chờ).<br>5. Ở chế độ nền, hệ thống gửi audio cho dịch vụ AI chấm giọng nói.<br>6. Dịch vụ AI trả về điểm gợi ý.<br>7. Hệ thống cập nhật trạng thái job thành DONE và lưu điểm AI gợi ý.<br>8. Student kiểm tra kết quả bằng cách tải lại trang (polling).<br>9. Một Staff có thể mở bài nộp, nghe audio, rồi xác nhận hoặc sửa điểm cuối cùng. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Dịch vụ AI chậm hoặc lỗi: Hệ thống chờ tới một mốc thời gian (timeout) rồi thử lại (tối đa 3 lần). Nếu vẫn lỗi, trạng thái job chuyển thành FAILED và Student thấy thông báo rõ ràng, không phải màn hình trắng.<br>Nhánh 2 - Sai định dạng file: Hệ thống từ chối file trước khi upload và giải thích các định dạng được phép.<br>Nhánh 3 - Staff sửa điểm AI: Staff có thể ghi đè điểm AI gợi ý bằng điểm cuối cùng của mình kèm nhận xét. |  |  |

#### 2.4.2 UC-20 AI Handwriting Practice

| Primary Actors | Student | Secondary Actors | OCR AI Service |
| --- | --- | --- | --- |
| Description | Student viết tay một chữ Kanji (trên canvas hoặc tải ảnh lên) và nhờ hệ thống kiểm tra xem gần giống chữ đúng tới mức nào. AI chỉ so sánh hình dạng nét vẽ, không kiểm tra thứ tự nét. |  |  |
| Preconditions | Ảnh/canvas vẽ hợp lệ. Chữ Kanji mục tiêu tồn tại trong hệ thống. |  |  |
| Postconditions | Một tỉ lệ giống nhau (similarity %) được lưu và hiển thị cho Student. |  |  |
| Normal Sequence/Flow | 1. Student mở một chữ Kanji và bấm 'Luyện viết'.<br>2. Student viết chữ Kanji trên canvas (hoặc tải ảnh lên).<br>3. Student bấm Nộp bài.<br>4. Hệ thống lưu ảnh và tạo một OCR job.<br>5. Hệ thống gửi ảnh cho dịch vụ AI OCR.<br>6. Dịch vụ AI so sánh hình dạng với chữ đúng và trả về tỉ lệ giống nhau.<br>7. Hệ thống hiện kết quả kèm nhận xét đơn giản (ví dụ: 'Khá tốt' hoặc 'Cần luyện thêm'). |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Dịch vụ AI lỗi: Hệ thống hiện lỗi rõ ràng và cho phép Student thử lại.<br>Nhánh 2 - Ảnh quá lớn: Hệ thống từ chối file và báo giới hạn kích thước.<br>Nhánh 3 - Không kiểm tra thứ tự nét: Đây là hành vi có chủ đích, không phải lỗi — hệ thống chỉ kiểm tra hình dạng cuối cùng. |  |  |

### 2.5 Staff Content Management

#### 2.5.1 UC-24/25/26/27/28 Manage Content

| Primary Actors | Staff | Secondary Actors | StaffManager |
| --- | --- | --- | --- |
| Description | Một Staff tạo hoặc sửa nội dung học: bài học, ngữ pháp, từ vựng, Kanji, câu hỏi, quiz, hoặc đề thi thử. Khi nội dung đã sẵn sàng, Staff gửi cho StaffManager duyệt trước khi hiển thị cho Student. |  |  |
| Preconditions | Tài khoản Staff đang hoạt động và có quyền quản lý loại nội dung này. |  |  |
| Postconditions | Nội dung được lưu ở trạng thái draft (đang soạn) hoặc pending_review (đã gửi duyệt). Nếu là thao tác quan trọng, hành động được ghi vào audit log. |  |  |
| Normal Sequence/Flow | 1. Staff mở màn hình quản lý nội dung (Bài học, Câu hỏi, hoặc Đề thi).<br>2. Staff tạo mới hoặc mở một mục có sẵn để sửa.<br>3. Staff nhập các trường nội dung (tiêu đề, cấp độ JLPT, nội dung, ví dụ...).<br>4. Hệ thống kiểm tra các trường theo quy tắc validate và business rule.<br>5. Staff bấm 'Lưu nháp' hoặc 'Gửi duyệt'.<br>6. Hệ thống lưu nội dung ở trạng thái draft hoặc pending_review. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Nội dung đã có Student làm bài: Hệ thống khóa việc sửa phiên bản đó và yêu cầu Staff tạo phiên bản mới.<br>Nhánh 2 - Thiếu dữ liệu bắt buộc: Hệ thống đánh dấu các trường còn thiếu và không cho lưu cho tới khi bổ sung đủ.<br>Nhánh 3 - Sai cấp độ JLPT: Hệ thống không cho phép trộn nội dung của nhiều cấp độ khác nhau trong cùng một bài học. |  |  |

### 2.6 Content Review

#### 2.6.1 UC-33/34 Review and Publish Content

| Primary Actors | StaffManager | Secondary Actors | Staff |
| --- | --- | --- | --- |
| Description | Một StaffManager kiểm tra nội dung mà Staff đã gửi duyệt. StaffManager có thể duyệt (Approve), từ chối (Reject), hoặc yêu cầu sửa lại (Request Changes). Sau khi được duyệt, StaffManager có thể publish để Student xem được, hoặc sau này unpublish/archive/khôi phục. |  |  |
| Preconditions | Nội dung đang ở trạng thái pending_review (để duyệt/từ chối/yêu cầu sửa), hoặc đã published (để unpublish/archive/khôi phục). |  |  |
| Postconditions | Trạng thái nội dung được cập nhật. Hệ thống ghi audit log và gửi thông báo cho Staff đã gửi nội dung đó. |  |  |
| Normal Sequence/Flow | 1. StaffManager mở hàng đợi duyệt (Review Queue).<br>2. StaffManager mở một mục nội dung để xem chi tiết đầy đủ.<br>3. StaffManager chọn hành động: Duyệt, Từ chối, hoặc Yêu cầu sửa.<br>4. Nếu là Từ chối hoặc Yêu cầu sửa, StaffManager nhập lý do.<br>5. Hệ thống cập nhật trạng thái nội dung và lưu audit log.<br>6. Hệ thống gửi thông báo cho Staff về quyết định này.<br>7. Với nội dung đã duyệt, StaffManager có thể bấm Publish để hiển thị cho Student. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Nội dung đã được xử lý rồi: Nếu một reviewer khác đã duyệt/từ chối nội dung này trước, hệ thống hiện thông báo và làm mới danh sách.<br>Nhánh 2 - Không đủ quyền: Nếu người dùng là Staff nhưng không phải StaffManager, hệ thống chặn hành động với lỗi 403.<br>Nhánh 3 - Thiếu lý do: Hệ thống bắt buộc nhập lý do cho các hành động Từ chối, Yêu cầu sửa, Unpublish, và Archive. |  |  |

### 2.7 Administration

#### 2.7.1 UC-37/39/40 Admin Management

| Primary Actors | Admin | Secondary Actors | SMTP Server |
| --- | --- | --- | --- |
| Description | Một Admin quản lý tài khoản người dùng, cấu hình hệ thống, và các quy tắc thông báo tự động. Đây là các thao tác quản trị hằng ngày như đổi vai trò người dùng, cập nhật cấu hình SMTP, hoặc tạo một quy tắc thông báo mới. |  |  |
| Preconditions | Admin đã đăng nhập bằng một tài khoản Admin hợp lệ. |  |  |
| Postconditions | Dữ liệu đã thay đổi (user, setting, hoặc rule) được lưu lại, và thay đổi được ghi vào audit log. |  |  |
| Normal Sequence/Flow | 1. Admin mở khu vực quản trị (Users, Settings, hoặc Notification Rules).<br>2. Admin tìm kiếm hoặc lọc để tìm đúng bản ghi cần sửa.<br>3. Admin mở bản ghi và cập nhật thông tin.<br>4. Hệ thống kiểm tra dữ liệu mới theo quy tắc validate.<br>5. Admin bấm Lưu.<br>6. Hệ thống lưu thay đổi và ghi một dòng audit log. |  |  |
| Alternative Sequences/Flows | Nhánh 1 - Hạ quyền Admin cuối cùng: Hệ thống chặn hành động này để hệ thống luôn còn ít nhất một tài khoản Admin.<br>Nhánh 2 - Giá trị setting không hợp lệ: Hệ thống báo lỗi và không lưu setting đó.<br>Nhánh 3 - Quy tắc thông báo có thể gây spam: Hệ thống cảnh báo Admin nếu một rule sẽ gửi quá nhiều thông báo trong thời gian ngắn. |  |  |

## 3. Functional Requirements

### 3.1 User Authentication

#### 3.1.1 User Register

![User Register](../07-Release-Documents/diagrams/rds/screenshots/screen-register.png)

*Ảnh chụp thật từ ứng dụng đang chạy: /register*

| Field Name | Description |
| --- | --- |
| Full name | Bắt buộc, tối đa 100 ký tự. |
| Email | Bắt buộc, đúng định dạng email, duy nhất trong student_users. |
| Password | Bắt buộc, theo policy bảo mật; lưu dạng bcrypt hash. |
| Confirm password | Bắt buộc, phải khớp Password. |
| OTP | 6 chữ số, hết hạn sau thời gian cấu hình, dùng để xác minh email. |

#### 3.1.2 User Login

![User Login](../07-Release-Documents/diagrams/rds/screenshots/screen-login.png)

*Ảnh chụp thật từ ứng dụng đang chạy: /login*

| Field Name | Description |
| --- | --- |
| Email | Bắt buộc, đúng định dạng email. |
| Password | Bắt buộc, không log plaintext. |
| Remember session | Tùy chọn UX; backend vẫn kiểm soát token/session. |
| Google login | Tùy chọn OAuth nếu GOOGLE_CLIENT_ID được cấu hình. |

#### 3.1.3 Password Reset

![Password Reset](../07-Release-Documents/diagrams/rds/screenshots/screen-forgot-password.png)

*Ảnh chụp thật từ ứng dụng đang chạy: /forgot-password*

| Field Name | Description |
| --- | --- |
| Email | Bắt buộc, nhận link/token reset. |
| Reset token | Token một lần, có thời hạn và bị vô hiệu hóa sau khi dùng. |
| New password | Theo policy bảo mật. |
| Confirm new password | Phải khớp New password. |

### 3.2 Student Learning and Assessment

#### 3.2.1 Lesson Detail

![Lesson Detail](../07-Release-Documents/diagrams/rds/wf-lesson-detail.png)

*Wireframe mockup (môi trường build không có backend/DB để chụp ảnh thật).*

| Field Name | Description |
| --- | --- |
| JLPT level | N5, N4, N3, N2, N1; backend kiểm tra subscription/level. |
| Lesson status | LOCKED, AVAILABLE, COMPLETED. |
| Content | Text, ví dụ, audio_url/image_url, câu hỏi liên quan. |
| Complete action | Gửi event hoàn thành; backend cập nhật progress. |

#### 3.2.2 Quiz / Mock Test Attempt

![Quiz / Mock Test Attempt](../07-Release-Documents/diagrams/rds/wf-quiz-attempt.png)

*Wireframe mockup (môi trường build không có backend/DB để chụp ảnh thật).*

| Field Name | Description |
| --- | --- |
| Assessment | Quiz hoặc exam ở trạng thái published. |
| Question list | Câu hỏi từ question_assignments đã publish. |
| Timer | Hiển thị UX; backend validate thời gian server-side. |
| Answer payload | Danh sách câu trả lời; không bao gồm score. |
| Result | Score, max_score, đúng/sai, submitted_at do backend trả. |

#### 3.2.3 AI Practice

![AI Practice](../07-Release-Documents/diagrams/rds/wf-ai-practice.png)

*Wireframe mockup (môi trường build không có backend/DB để chụp ảnh thật).*

| Field Name | Description |
| --- | --- |
| File/canvas input | Ảnh hoặc audio hợp lệ, giới hạn kích thước theo cấu hình. |
| job_id | Mã job AI bất đồng bộ để poll kết quả. |
| AI status | PENDING, PROCESSING, DONE, FAILED. |
| AI result | OCR similarity % hoặc speech score suggestion. |
| Final score | Điểm cuối do Staff/logic được phép xác nhận. |

### 3.3 Staff, Manager and Admin Operations

#### 3.3.1 Staff Content Management

![Staff Content Management](../07-Release-Documents/diagrams/rds/wf-staff-content.png)

*Wireframe mockup (môi trường build không có backend/DB để chụp ảnh thật).*

| Field Name | Description |
| --- | --- |
| Content type | Course, Lesson, Grammar, Vocabulary, Kanji, Question, Assessment. |
| Status | draft, pending_review, published, archived. |
| JLPT level | N5..N1, bắt buộc không trộn level. |
| Submit for review | Chỉ Staff/Admin có quyền; ghi audit khi cần. |

#### 3.3.2 Manager Review Queue

![Manager Review Queue](../07-Release-Documents/diagrams/rds/wf-manager-review-queue.png)

*Wireframe mockup (môi trường build không có backend/DB để chụp ảnh thật).*

| Field Name | Description |
| --- | --- |
| Filter | Loại nội dung, JLPT level, người gửi, trạng thái, ngày gửi. |
| Review action | Approve, Reject, Request Changes. |
| Reason/feedback | Bắt buộc khi reject/request changes hoặc unpublish/archive. |
| Audit data | actor, action, target, reason, timestamp. |

#### 3.3.3 Admin User Management

![Admin User Management](../07-Release-Documents/diagrams/rds/wf-admin-user-management.png)

*Wireframe mockup (môi trường build không có backend/DB để chụp ảnh thật).*

| Field Name | Description |
| --- | --- |
| Role | STUDENT, STAFF, STAFF_MANAGER, ADMIN. |
| Status | ACTIVE, SUSPENDED/INACTIVE, SOFT_DELETED. |
| Subscription | FREE/VIP và thời hạn hiệu lực. |
| Reset password | Tạo token/tạm mật khẩu theo policy; ghi audit. |

## 4. Non-Functional Requirements

### 4.1 External Interfaces

- REST API prefix `/api/[resource]`; standard response `{ "status": number, "message": string, "data": object }`.
- MySQL 8 with utf8mb4 and UTC timestamps.
- File storage in `/uploads` or S3-compatible storage; no BLOB for media.
- SMTP for verification/reset/notification email with retry.
- OCR/Speech AI service invoked asynchronously with timeout, retry and fallback.

### 4.2 Quality Attributes

#### 4.2.1 Usability

Responsive web UI, Vietnamese-first learning experience, clear loading/error/empty states, and role-specific navigation.

#### 4.2.2 Performance

Common API responses should average under 2 seconds in normal conditions. AI requests return `job_id` immediately and process in the background.

#### 4.2.3 Security

JWT is mandatory for private APIs; bcrypt cost >= 10; backend checks Role + Subscription/Level; DTOs are mandatory for API responses; secrets are not stored in source control.

#### 4.2.4 Reliability, Auditability and Maintainability

Soft delete is used for important data, submitted attempts are immutable, important Staff/Admin operations are audited, global exception handling returns standard JSON, and business logic stays in backend Services.

## 5. Requirement Appendix

### 5.1 Business Rules

| ID | Rule Definition |
| --- | --- |
| BR-01 | Authorization phải kiểm tra cả Role và Subscription/Level; UI chỉ là UX, backend vẫn trả 401/403. |
| BR-02 | Điểm quiz/mock exam chỉ được tính tại backend Service layer; client không gửi score. |
| BR-03 | Score luôn nằm trong khoảng 0..max_score và mỗi lần nộp tạo attempt mới. |
| BR-04 | Attempt đã SUBMITTED là bất biến; không sửa điểm hoặc đáp án sau khi nộp. |
| BR-05 | Quiz/Exam đã có attempt thì câu hỏi bị lock; sửa nội dung phải tạo version mới. |
| BR-06 | Bài học tiếp theo chỉ mở khi bài trước hoàn thành; user_progress chỉ tăng. |
| BR-07 | Nội dung Staff tạo phải qua StaffManager review trước khi publish. |
| BR-08 | Nội dung VIP chỉ hiển thị khi subscription VIP còn hiệu lực; cache subscription tối đa 5 phút. |
| BR-09 | AI task chạy async, trả job_id, timeout + retry tối đa 3 lần + fallback rõ ràng. |
| BR-10 | File ảnh/audio lưu ở /uploads hoặc S3; không lưu BLOB trong DB. |
| BR-11 | Soft delete bắt buộc cho dữ liệu quan trọng; không hard delete. |
| BR-12 | Thao tác quan trọng của Admin/Staff/StaffManager phải ghi audit log. |


### 5.2 System Messages

| # | Message code | Message Type | Context | Content |
| --- | --- | --- | --- | --- |
| 1 | MSG01 | Inline | No search result | Không có kết quả phù hợp. |
| 2 | MSG02 | Field error | Required field is empty | Trường này là bắt buộc. |
| 3 | MSG03 | Toast | Save success | Lưu dữ liệu thành công. |
| 4 | MSG04 | Toast | Delete/soft delete success | Cập nhật trạng thái thành công. |
| 5 | MSG05 | Inline | Invalid login | Email hoặc mật khẩu không đúng. |
| 6 | MSG06 | Inline | Forbidden | Bạn không có quyền truy cập chức năng này. |
| 7 | MSG07 | Toast | AI job accepted | Bài nộp đã được tiếp nhận và đang xử lý. |
| 8 | MSG08 | Toast | AI failed | Không thể xử lý AI lúc này. Vui lòng thử lại sau. |


### 5.3 Other Requirements

- No hard delete for important business data.
- No frontend scoring, authorization or subscription business logic.
- No schema change without migration.
- No hardcoded secrets/password/API keys.
- Source references: `AGENTS.md`, `CLAUDE.md`, `docs/01-SRS-Requirements/shared_context.md`, `docs/01-SRS-Requirements/use-cases/Bao_cao_dac_ta_Use_Case.md`.
