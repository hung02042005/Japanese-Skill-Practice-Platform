# BÁO CÁO ĐẶC TẢ USE CASE

## HỆ THỐNG HỌC TIẾNG NHẬT JLPT
>
> **Tổng hợp đặc tả chi tiết 40 Use Case**
> *Phân theo 4 nhóm tác nhân: Student, Staff, StaffManager, Admin*

---

| Thông tin tài liệu | Chi tiết |
| :--- | :--- |
| **Tài liệu** | Đặc tả Use Case |
| **Phiên bản** | 1.1 |
| **Tổng số Use Case** | 40 (Student: 20 \| Staff: 12 \| StaffManager: 2 \| Admin: 6) |
| **Ngày cập nhật** | 26/05/2026 |

---

## MỤC LỤC

1. [GIỚI THIỆU](#1-giới-thiệu)
   - 1.1. [Mục đích tài liệu](#11-mục-đích-tài-liệu)
   - 1.2. [Phạm vi hệ thống](#12-phạm-vi-hệ-thống)
   - 1.3. [Phân loại tác nhân](#13-phân-loại-tác-nhân)
2. [BẢNG TỔNG HỢP USE CASE](#2-bảng-tổng-hợp-use-case)
   - 2.1. [Nhóm Student (Học viên)](#21-nhóm-student-học-viên)
   - 2.2. [Nhóm Staff (Nhân viên)](#22-nhóm-staff-nhân-viên)
   - 2.3. [Nhóm StaffManager (Quản lý nội dung)](#23-nhóm-staffmanager-quản-lý-nội-dung)
   - 2.4. [Nhóm Admin (Quản trị viên)](#24-nhóm-admin-quản-trị-viên)
3. [CHI TIẾT USE CASE - NHÓM STUDENT](#3-chi-tiết-use-case---nhóm-student)
4. [CHI TIẾT USE CASE - NHÓM STAFF](#4-chi-tiết-use-case---nhóm-staff)
5. [CHI TIẾT USE CASE - NHÓM STAFFMANAGER](#5-chi-tiết-use-case---nhóm-staffmanager)
6. [CHI TIẾT USE CASE - NHÓM ADMIN](#6-chi-tiết-use-case---nhóm-admin)
7. [KẾT LUẬN](#7-kết-luận)

---

## 1. GIỚI THIỆU

### 1.1. Mục đích tài liệu

Tài liệu này tổng hợp đặc tả chi tiết các Use Case của hệ thống học tiếng Nhật JLPT, dùng làm cơ sở phân tích thiết kế hệ thống, phát triển phần mềm và kiểm thử nghiệm thu. Mỗi Use Case mô tả rõ tác nhân tham gia, tiền điều kiện, luồng xử lý cơ bản và các bảng dữ liệu liên quan.

### 1.2. Phạm vi hệ thống

Hệ thống học tiếng Nhật JLPT là nền tảng học trực tuyến hỗ trợ học viên ôn luyện tiếng Nhật từ cấp độ N5 đến N1. Nền tảng cung cấp đầy đủ các kỹ năng học tập gồm: học Kanji - Kana - Từ vựng - Ngữ pháp, làm bài thi thử JLPT, luyện nghe - nói - đọc - viết với sự hỗ trợ của AI (Speech Recognition, OCR).

### 1.3. Phân loại tác nhân

Hệ thống có 4 nhóm tác nhân chính với vai trò và quyền hạn riêng biệt:

- **Student (Học viên):** Đối tượng người dùng cuối, sử dụng hệ thống để học tập và luyện thi JLPT (20 Use Case).
- **Staff (Nhân viên):** Phụ trách soạn thảo nội dung học tập, gửi nội dung để duyệt, chấm bài thủ công, hỗ trợ học viên (12 Use Case).
- **StaffManager (Quản lý nội dung):** Duyệt, từ chối, yêu cầu chỉnh sửa, xuất bản, ẩn hoặc lưu trữ nội dung do Staff gửi (2 Use Case).
- **Admin (Quản trị viên):** Quản lý hệ thống ở cấp cao nhất - phân quyền người dùng, cấu hình kỹ thuật, báo cáo toàn diện (6 Use Case).

---

## 2. BẢNG TỔNG HỢP USE CASE

### 2.1. Nhóm Student (Học viên)

*Tổng số: 20 Use Case bao gồm xác thực tài khoản, học nội dung, làm bài thi - kiểm tra, ôn tập SRS Flashcard, luyện nói - viết với AI và theo dõi tiến độ học tập.*

| Mã UC | Tên Use Case | Tác nhân | Nhóm chức năng |
| :--- | :--- | :--- | :--- |
| **UC-01** | User Login (Đăng nhập) | Student | Học viên |
| **UC-02** | User Register (Đăng ký tài khoản) | Student (Khách) | Học viên |
| **UC-03** | Reset Password (Khôi phục mật khẩu) | Student (hoặc Khách) | Học viên |
| **UC-04** | User Profile (Hồ sơ cá nhân) | Student | Học viên |
| **UC-05** | Change Password (Đổi mật khẩu) | Student | Học viên |
| **UC-06** | Learn Grammar (Học ngữ pháp) | Student | Học viên |
| **UC-07** | Learn Kanji (Học chữ Hán) | Student | Học viên |
| **UC-08** | Learn Kana (Học bảng chữ cái Kana) | Student | Học viên |
| **UC-09** | Vocabulary (Học từ vựng) | Student | Học viên |
| **UC-10** | Take JLPT Mock Test (Làm bài thi thử JLPT) | Student | Học viên |
| **UC-11** | Practice & Quiz (Luyện tập trắc nghiệm) | Student | Học viên |
| **UC-12** | Flashcard Learning (Học bằng thẻ ghi nhớ) | Student | Học viên |
| **UC-13** | Speaking Practice & AI Grading (Luyện nói & AI chấm điểm) | Student | Học viên |
| **UC-14** | Reading Practice (Luyện đọc hiểu) | Student | Học viên |
| **UC-15** | Listening Practice (Luyện nghe hiểu) | Student | Học viên |
| **UC-16** | Dictionary & Search (Tìm kiếm & Tra từ điển) | Student | Học viên |
| **UC-17** | Bookmark Learning (Đánh dấu nội dung) | Student | Học viên |
| **UC-18** | Logout (Đăng xuất) | Student | Học viên |
| **UC-19** | Learning Progress & Stats (Tiến độ học tập) | Student | Học viên |
| **UC-20** | AI Handwriting Practice (Luyện viết chữ bằng AI) | Student | Học viên |

### 2.2. Nhóm Staff (Nhân viên)

*Tổng số: 12 Use Case được phân thành 4 nhóm chức năng: Quản lý học viên, Quản lý nội dung, Hỗ trợ và Phân tích kết quả.*

| Mã UC | Tên Use Case | Tác nhân | Nhóm chức năng |
| :--- | :--- | :--- | :--- |
| **UC-21** | View Student Progress (Theo dõi học viên) | Staff | I. Quản lý Học viên |
| **UC-22** | Manage Student Accounts (Quản lý học viên) | Staff | I. Quản lý Học viên |
| **UC-23** | Suspend or Activate Account (Khóa / Mở tài khoản) | Staff | I. Quản lý Học viên |
| **UC-24** | Manage Question Bank (Quản lý ngân hàng câu hỏi) | Staff | II. Quản lý Nội dung |
| **UC-25** | Manage Grammar Content (Quản lý nội dung ngữ pháp) | Staff | II. Quản lý Nội dung |
| **UC-26** | Manage Quiz (Quản lý bài Quiz) | Staff | II. Quản lý Nội dung |
| **UC-27** | Manage Learning Content (Quản lý nội dung bài học) | Staff | II. Quản lý Nội dung |
| **UC-28** | Manage JLPT Mock Exams (Quản lý đề thi thử JLPT) | Staff | II. Quản lý Nội dung |
| **UC-29** | Respond to Student Support (Hỗ trợ người dùng) | Staff | III. Hỗ trợ & Tác vụ trực tiếp |
| **UC-30** | Send Notifications (Gửi thông báo) | Staff | III. Hỗ trợ & Tác vụ trực tiếp |
| **UC-31** | Grade Speaking Submission (Chấm bài nói) | Staff | III. Hỗ trợ & Tác vụ trực tiếp |
| **UC-32** | View Quiz Results (Xem kết quả kiểm tra) | Staff | IV. Phân tích |

### 2.3. Nhóm StaffManager (Quản lý nội dung)

*Tổng số: 2 Use Case bổ sung để kiểm soát chất lượng nội dung trước khi học viên nhìn thấy.*

| Mã UC | Tên Use Case | Tác nhân | Nhóm chức năng |
| :--- | :--- | :--- | :--- |
| **UC-33** | Review Submitted Content (Duyệt nội dung Staff gửi) | StaffManager | V. Duyệt nội dung |
| **UC-34** | Manage Published Content Status (Quản lý trạng thái xuất bản) | StaffManager | V. Duyệt nội dung |

### 2.4. Nhóm Admin (Quản trị viên)

*Tổng số: 6 Use Case cốt lõi về quản trị hệ thống ở cấp độ cao nhất, bao gồm phân quyền người dùng, cấu hình kỹ thuật, báo cáo và cài đặt thông báo tự động.*

| Mã UC | Tên Use Case | Tác nhân | Nhóm chức năng |
| :--- | :--- | :--- | :--- |
| **UC-35** | Login System (Đăng nhập Admin Panel) | Admin | Quản trị viên |
| **UC-36** | View Dashboard (Bảng điều khiển) | Admin | Quản trị viên |
| **UC-37** | User Management (Quản lý người dùng) | Admin | Quản trị viên |
| **UC-38** | Report Screen (Báo cáo & Thống kê) | Admin | Quản trị viên |
| **UC-39** | Settings (Cài đặt hệ thống) | Admin | Quản trị viên |
| **UC-40** | Notification Rules (Quản lý quy tắc thông báo) | Admin | Quản trị viên |

---

## 3. CHI TIẾT USE CASE - NHÓM STUDENT

Phần này đặc tả chi tiết 20 Use Case dành cho học viên, sắp xếp theo trình tự thao tác từ xác thực tài khoản đến học tập và đánh giá kết quả.

### UC-01: User Login (Đăng nhập)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-01 |
| **Tên Use Case** | User Login (Đăng nhập) |
| **Tác nhân** | Student |
| **Mô tả** | Học viên đăng nhập vào hệ thống bằng Email/Password hoặc thông qua tài khoản Google OAuth. |
| **Tiền điều kiện** | Tài khoản đã được đăng ký và hoạt động. |

- **Luồng cơ bản:**
  1. Học viên nhập Email và Mật khẩu.
  2. Nhấn 'Đăng nhập'.
  3. Hệ thống xác thực thông tin.
  4. Nếu hợp lệ $\rightarrow$ lưu session/token và chuyển hướng đến trang Dashboard.
- **Luồng thay thế:**
  - *Đăng nhập OAuth (Google):* Hệ thống xác thực qua OAuth provider, tự tạo/liên kết tài khoản và chuyển vào Dashboard.
  - *Sai mật khẩu:* Hiện thông báo lỗi và cho thử lại (tối đa 5 lần liên tiếp).
- **Hậu điều kiện:** Học viên đăng nhập thành công.
- **Bảng dữ liệu liên quan:** `student_users` (oauth_provider, oauth_provider_id, oauth_provider_email), `auth_tokens` (token_type='session')

---

### UC-02: User Register (Đăng ký tài khoản)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-02 |
| **Tên Use Case** | User Register (Đăng ký tài khoản) |
| **Tác nhân** | Student (Khách) |
| **Mô tả** | Người dùng tạo tài khoản mới để bắt đầu học tập trên hệ thống. |
| **Tiền điều kiện** | Email đăng ký chưa tồn tại trên hệ thống. |

- **Luồng cơ bản:**
  1. Người dùng nhập: Họ tên, Email, Mật khẩu, Xác nhận mật khẩu.
  2. Hệ thống kiểm tra định dạng email và độ mạnh của mật khẩu.
  3. Hệ thống gửi email chứa mã OTP gồm 6 chữ số để xác minh tài khoản (hết hạn sau 10 phút).
  4. Người dùng nhập mã OTP vào trang xác minh, xác minh tài khoản thành công.
- **Luồng thay thế:**
  - *Email đã được sử dụng:* Hệ thống báo lỗi và gợi ý chuyển sang màn hình Đăng nhập.
  - *Mã OTP sai/hết hạn:* Hệ thống báo lỗi; cho phép yêu cầu gửi lại mã mới (tối đa 1 lần/60 giây). Nhập sai quá 5 lần liên tiếp → mã bị vô hiệu hoá, bắt buộc gửi lại mã mới.
- **Hậu điều kiện:** Tài khoản mới được tạo ở trạng thái hoạt động.
- **Bảng dữ liệu liên quan:** `student_users` (status='active'), `auth_tokens` (token_type='email_verification', token_value = mã OTP 6 chữ số)

---

### UC-03: Reset Password (Khôi phục mật khẩu)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-03 |
| **Tên Use Case** | Reset Password (Khôi phục mật khẩu) |
| **Tác nhân** | Student (hoặc Khách) |
| **Mô tả** | Học viên khôi phục lại mật khẩu khi bị quên. |
| **Tiền điều kiện** | Email yêu cầu khôi phục tồn tại trong hệ thống. |

- **Luồng cơ bản:**
  1. Nhập địa chỉ Email trên màn hình quên mật khẩu.
  2. Hệ thống gửi mã/liên kết đặt lại mật khẩu về email (có hiệu lực trong 15 phút).
  3. Học viên click vào liên kết, nhập mật khẩu mới và xác nhận mật khẩu mới.
  4. Hệ thống cập nhật mật khẩu mới và hủy bỏ token reset.
- **Bảng dữ liệu liên quan:** `student_users`, `auth_tokens` (token_type='password_reset', expires_at)

---

### UC-04: User Profile (Hồ sơ cá nhân)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-04 |
| **Tên Use Case** | User Profile (Hồ sơ cá nhân) |
| **Tác nhân** | Student |
| **Mô tả** | Học viên xem và chỉnh sửa thông tin cá nhân. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Học viên truy cập trang Profile.
  2. Hệ thống hiển thị: Ảnh đại diện, Họ tên, Email, Cấp độ JLPT hiện tại, Ngày tham gia hệ thống.
  3. Học viên chỉnh sửa thông tin (Họ tên, ảnh đại diện, số điện thoại, cấp độ JLPT mục tiêu) và click 'Lưu'.
  4. Hệ thống cập nhật và phản hồi thành công.
- **Bảng dữ liệu liên quan:** `student_users` (avatar_url, full_name, phone, current_jlpt_level, target_jlpt_level)

---

### UC-05: Change Password (Đổi mật khẩu)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-05 |
| **Tên Use Case** | Change Password (Đổi mật khẩu) |
| **Tác nhân** | Student |
| **Mô tả** | Học viên thay đổi mật khẩu khi đang đăng nhập. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Vào mục 'Đổi mật khẩu' trong trang cài đặt tài khoản.
  2. Nhập: Mật khẩu hiện tại, Mật khẩu mới, Xác nhận mật khẩu mới.
  3. Hệ thống xác thực mật khẩu cũ. Nếu đúng, cập nhật mật khẩu mới.
- **Bảng dữ liệu liên quan:** `student_users` (password_hash)

---

### UC-06: Learn Grammar (Học ngữ pháp)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-06 |
| **Tên Use Case** | Learn Grammar (Học ngữ pháp) |
| **Tác nhân** | Student |
| **Mô tả** | Học các cấu trúc ngữ pháp tiếng Nhật phân theo cấp độ JLPT. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Chọn cấp độ JLPT (N5 - N1) và chọn danh sách điểm ngữ pháp.
  2. Hệ thống hiển thị: cấu trúc, công thức, giải thích nghĩa và cách dùng.
  3. Hiển thị danh sách các câu ví dụ minh họa kèm dịch nghĩa tiếng Việt.
  4. Học viên học xong cấu trúc ngữ pháp và đánh dấu đã học.
- **Bảng dữ liệu liên quan:** `grammar_points` (structure, formula, meaning, usage_explanation, jlpt_level, examples JSON), `student_content_progress` (content_type='grammar')

---

### UC-07: Learn Kanji (Học chữ Hán)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-07 |
| **Tên Use Case** | Learn Kanji (Học chữ Hán) |
| **Tác nhân** | Student |
| **Mô tả** | Học chữ Hán (Kanji) bao gồm nghĩa, cách đọc, nét vẽ tĩnh và các từ vựng ví dụ đi kèm. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Chọn cấp độ JLPT và hiển thị danh sách chữ Kanji cần học.
  2. Click chọn chữ Kanji cụ thể để xem chi tiết: ký tự, số nét vẽ, hình ảnh thứ tự nét viết tĩnh.
  3. Xem cách đọc Onyomi (âm Hán), Kunyomi (âm Nhật), nghĩa tiếng Việt và danh sách từ vựng ví dụ.
  4. Học viên đánh dấu đã học xong hoặc thêm chữ Kanji này vào bộ Flashcard cá nhân.
- **Bảng dữ liệu liên quan:** `kanji` (character_value, onyomi, kunyomi, meaning, stroke_count, jlpt_level, stroke_order_url, example_word, example_reading, example_meaning), `student_content_progress` (content_type='kanji'), `flashcards` (deck_name, content_type='kanji')

---

### UC-08: Learn Kana (Học bảng chữ cái Kana)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-08 |
| **Tên Use Case** | Learn Kana (Học bảng chữ cái Kana) |
| **Tác nhân** | Student |
| **Mô tả** | Học bảng chữ cái tiếng Nhật Hiragana và Katakana với hướng dẫn nét vẽ và phát âm. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Chọn học bảng Hiragana hoặc Katakana.
  2. Hệ thống hiển thị bảng chữ cái.
  3. Nhấn vào từng chữ cái để nghe audio phát âm chuẩn và xem hình ảnh tĩnh chỉ thứ tự nét vẽ.
  4. Đánh dấu trạng thái học xong.
- **Bảng dữ liệu liên quan:** `kana_characters` (audio_url, stroke_order_url), `student_content_progress` (content_type='kana')

---

### UC-09: Vocabulary (Học từ vựng)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-09 |
| **Tên Use Case** | Vocabulary (Học từ vựng) |
| **Tác nhân** | Student |
| **Mô tả** | Học từ vựng tiếng Nhật theo chủ đề hoặc cấp độ JLPT. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Chọn chủ đề học (Nhà hàng, Du lịch...) hoặc theo cấp độ JLPT.
  2. Hệ thống hiển thị danh sách từ vựng kèm Furigana, ý nghĩa tiếng Việt, câu ví dụ có dịch nghĩa và audio mẫu.
  3. Đánh dấu từ vựng đã học hoặc lưu vào bộ thẻ Flashcard.
- **Bảng dữ liệu liên quan:** `vocabulary` (word, furigana, meaning, jlpt_level, topic, audio_url, example_sentence), `student_content_progress` (content_type='vocabulary')

---

### UC-10: Take JLPT Mock Test (Làm bài thi thử JLPT)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-10 |
| **Tên Use Case** | Take JLPT Mock Test (Làm bài thi thử JLPT) |
| **Tác nhân** | Student |
| **Mô tả** | Làm bài thi thử giả lập toàn diện cấu trúc đề thi JLPT thực tế với đồng hồ đếm ngược. |
| **Tiền điều kiện** | Học viên đã đăng nhập, thiết bị phát âm thanh hoạt động (phục vụ phần thi nghe). |

- **Luồng cơ bản:**
  1. Chọn đề thi thử theo cấp độ JLPT mong muốn.
  2. Hệ thống khởi chạy giao diện làm bài thi và kích hoạt đếm ngược thời gian.
  3. Làm lần lượt các phần: Chữ Hán - Từ vựng $\rightarrow$ Ngữ pháp - Đọc hiểu $\rightarrow$ Nghe hiểu.
  4. Nhấn 'Nộp bài' hoặc hệ thống tự động thu bài khi hết giờ.
  5. Xem điểm tổng quát, điểm từng kỹ năng và trạng thái đạt/trượt (Pass/Fail).
- **Bảng dữ liệu liên quan:** `assessments` (assessment_type='exam'), `question_assignments` (parent_type='assessment'), `questions` (option_a/b/c/d inline), `test_attempts` (attempt_type='exam'), `attempt_answers`

---

### UC-11: Practice & Quiz (Luyện tập trắc nghiệm)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-11 |
| **Tên Use Case** | Practice & Quiz (Luyện tập trắc nghiệm) |
| **Tác nhân** | Student |
| **Mô tả** | Làm bài kiểm tra nhanh (quiz) theo bài học hoặc luyện tập câu hỏi ngẫu nhiên theo kỹ năng và xem giải thích ngay sau khi hoàn thành. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Chọn làm bài Quiz theo bài học hoặc chọn luyện tập ngẫu nhiên theo một kỹ năng cụ thể.
  2. Hệ thống tạo danh sách 10-20 câu hỏi trắc nghiệm/điền khuyết.
  3. Học viên trả lời từng câu hỏi.
  4. Nhấn 'Nộp bài' $\rightarrow$ Hệ thống chấm điểm ngay lập tức.
  5. Hiển thị danh sách câu đúng/sai kèm giải thích chi tiết để học viên tự học.
- **Bảng dữ liệu liên quan:** `assessments` (assessment_type='quiz'), `questions` (option_a/b/c/d inline), `question_assignments` (parent_type='assessment'), `test_attempts` (attempt_type='quiz'/'practice'), `attempt_answers`

---

### UC-12: Flashcard Learning (Học bằng thẻ ghi nhớ)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-12 |
| **Tên Use Case** | Flashcard Learning (Học bằng thẻ ghi nhớ) |
| **Tác nhân** | Student |
| **Mô tả** | Học và ôn tập từ vựng/Kanji bằng thẻ ghi nhớ dựa trên thuật toán Spaced Repetition (SRS). |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Học viên chọn bộ thẻ (Deck) cá nhân hoặc bộ thẻ hệ thống đã lưu.
  2. Hệ thống hiển thị mặt trước của thẻ. Học viên đoán nghĩa rồi click 'Lật thẻ' để xem mặt sau.
  3. Học viên đánh giá mức độ nhớ: Dễ (Easy), Khó (Hard), Sai (Wrong).
  4. Hệ thống tự động tính toán và lên lịch ôn tập tiếp theo dựa trên đánh giá.
- **Bảng dữ liệu liên quan:** `flashcards` (deck_name, is_system, last_rating, interval_days, ease_factor, repetition_count, next_review_date, last_reviewed_at)

---

### UC-13: Speaking Practice & AI Grading (Luyện nói & AI chấm điểm)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-13 |
| **Tên Use Case** | Speaking Practice & AI Grading (Luyện nói & AI chấm điểm) |
| **Tác nhân** | Student |
| **Mô tả** | Học viên thực hành kỹ năng nói theo phương pháp Shadowing và AI chấm điểm phát âm tự động, phản hồi lỗi sai ngay lập tức. |
| **Tiền điều kiện** | Học viên đã đăng nhập. Thiết bị có micro hoạt động tốt. |

- **Luồng cơ bản:**
  1. Chọn một bài tập luyện nói Speaking.
  2. Hệ thống hiển thị văn bản mẫu tiếng Nhật và phát tệp âm thanh mẫu của người bản xứ.
  3. Học viên click 'Ghi âm' và đọc theo văn bản.
  4. Nhấn 'Nộp bài' $\rightarrow$ Hệ thống gửi tệp ghi âm đến AI engine chấm điểm.
  5. AI phân tích phát âm và trả về điểm phát âm (0-100), điểm trôi chảy, tô màu các âm/từ bị đọc sai và đưa ra lời khuyên.
- **Bảng dữ liệu liên quan:** `lessons` (lesson_type='speaking', audio_url), `student_submissions` (submission_type='speaking', recording_url, status='ai_graded', ai_overall_score, ai_pronunciation_score, ai_fluency_score, ai_highlighted_errors, ai_suggestions, final_score)

---

### UC-14: Reading Practice (Luyện đọc hiểu)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-14 |
| **Tên Use Case** | Reading Practice (Luyện đọc hiểu) |
| **Tác nhân** | Student |
| **Mô tả** | Học viên luyện đọc hiểu bằng cách đọc các đoạn văn tiếng Nhật và trả lời câu hỏi trắc nghiệm đi kèm. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Chọn bài đọc hiểu theo cấp độ JLPT phù hợp.
  2. Hệ thống hiển thị đoạn văn bản tiếng Nhật.
  3. Học viên đọc đoạn văn và chọn đáp án cho các câu hỏi trắc nghiệm bên dưới.
  4. Nhấn 'Nộp bài' $\rightarrow$ Xem kết quả đúng/sai và phần giải nghĩa chi tiết của đoạn văn.
- **Bảng dữ liệu liên quan:** `lessons` (lesson_type='reading'), `question_assignments` (parent_type='lesson'), `questions` (option_a/b/c/d inline), `test_attempts` (attempt_type='reading'), `attempt_answers`

---

### UC-15: Listening Practice (Luyện nghe hiểu)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-15 |
| **Tên Use Case** | Listening Practice (Luyện nghe hiểu) |
| **Tác nhân** | Student |
| **Mô tả** | Luyện kỹ năng nghe hiểu tiếng Nhật thông qua audio và câu hỏi trắc nghiệm. |
| **Tiền điều kiện** | Học viên đã đăng nhập. Có loa hoặc tai nghe. |

- **Luồng cơ bản:**
  1. Chọn bài tập luyện nghe theo cấp độ JLPT.
  2. Phát audio nghe hiểu. Học viên có thể tạm dừng hoặc phát lại audio.
  3. Trả lời các câu hỏi trắc nghiệm tương ứng.
  4. Nhấn 'Nộp bài' $\rightarrow$ Hệ thống chấm điểm, hiển thị transcript và giải thích chi tiết.
- **Bảng dữ liệu liên quan:** `lessons` (lesson_type='listening', audio_url), `question_assignments` (parent_type='lesson'), `questions` (option_a/b/c/d inline), `test_attempts` (attempt_type='listening'), `attempt_answers`

---

### UC-16: Dictionary & Search (Tìm kiếm & Tra từ điển)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-16 |
| **Tên Use Case** | Dictionary & Search (Tìm kiếm & Tra từ điển) |
| **Tác nhân** | Student |
| **Mô tả** | Tìm kiếm bài học/ngữ pháp/từ vựng/Kanji toàn hệ thống và tra cứu từ điển từ vựng chi tiết Nhật - Việt. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Học viên nhập từ khóa cần tìm kiếm vào ô tìm kiếm trên thanh điều hướng.
  2. Hệ thống hiển thị kết quả phân tách rõ ràng theo các nhóm: Bài học, Chữ Hán, Từ vựng, Ngữ pháp.
  3. Chọn xem chi tiết một từ vựng để hiển thị giao diện từ điển: Kanji, Furigana, ý nghĩa, loại từ, phát âm, ví dụ.
- **Bảng dữ liệu liên quan:** `vocabulary`, `kanji`, `grammar_points`, `lessons`

---

### UC-17: Bookmark Learning (Đánh dấu nội dung)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-17 |
| **Tên Use Case** | Bookmark Learning (Đánh dấu nội dung) |
| **Tác nhân** | Student |
| **Mô tả** | Học viên đánh dấu (bookmark) các bài học, từ vựng, ngữ pháp, Kanji quan trọng để dễ dàng truy cập và ôn tập lại. |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Trong khi học bài, học viên click biểu tượng 'Bookmark'.
  2. Hệ thống thêm nội dung đó vào danh sách lưu trữ cá nhân.
  3. Học viên truy cập trang 'Bookmarks' để duyệt nhanh các nội dung đã lưu, lọc theo phân loại.
  4. Có thể nhấn bỏ bookmark để gỡ khỏi danh sách bất cứ lúc nào.
- **Bảng dữ liệu liên quan:** `student_content_progress` (is_bookmarked=1, bookmark_note, bookmarked_at)

---

### UC-18: Logout (Đăng xuất)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-18 |
| **Tên Use Case** | Logout (Đăng xuất) |
| **Tác nhân** | Student |
| **Mô tả** | Đăng xuất khỏi hệ thống và vô hiệu hóa phiên làm việc hiện tại. |
| **Tiền điều kiện** | Học viên đang ở trạng thái đăng nhập. |

- **Luồng cơ bản:**
  1. Học viên nhấn chọn nút 'Đăng xuất' trên menu tài khoản.
  2. Hệ thống thu hồi và xóa session/token hiện tại khỏi cơ sở dữ liệu.
  3. Chuyển hướng học viên về trang Đăng nhập.
- **Bảng dữ liệu liên quan:** `auth_tokens` (token_type='session')

---

### UC-19: Learning Progress & Stats (Tiến độ học tập)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-19 |
| **Tên Use Case** | Learning Progress & Stats (Tiến độ học tập) |
| **Tác nhân** | Student |
| **Mô tả** | Học viên theo dõi lịch sử, biểu đồ tiến độ học tập và chuỗi ngày học tập liên tiếp (streak). |
| **Tiền điều kiện** | Học viên đã đăng nhập. |

- **Luồng cơ bản:**
  1. Học viên vào trang Dashboard cá nhân / Tiến trình học tập.
  2. Hệ thống hiển thị: số ngày học liên tiếp (Streak), số lượng bài học/Kanji/từ vựng đã hoàn thành.
  3. Biểu đồ thống kê điểm thi thử JLPT qua các lần thi.
  4. Sơ đồ Radar/Cột thống kê điểm mạnh/yếu của các kỹ năng (Nghe, Đọc, Từ vựng, Ngữ pháp, Phát âm).
- **Bảng dữ liệu liên quan:** `vw_student_learning_stats`, `student_users`, `test_attempts`, `student_content_progress`

---

### UC-20: AI Handwriting Practice (Luyện viết chữ bằng AI)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-20 |
| **Tên Use Case** | AI Handwriting Practice (Luyện viết chữ bằng AI) |
| **Tác nhân** | Student |
| **Mô tả** | Học viên luyện viết tay Kanji/Kana trực tiếp trên màn hình, hệ thống dùng AI OCR để kiểm tra độ chính xác và đưa ra kết quả tức thì. |
| **Tiền điều kiện** | Học viên đã đăng nhập và truy cập nội dung học Kanji hoặc Kana. |

- **Luồng cơ bản:**
  1. Học viên chọn một chữ Kanji hoặc chữ Kana cụ thể để luyện viết.
  2. Hệ thống hiển thị hình ảnh hướng dẫn nét viết tĩnh làm mẫu chuẩn (`stroke_order_url`).
  3. Học viên sử dụng chuột, ngón tay hoặc bút cảm ứng vẽ/viết lại chữ đó trên vùng Canvas.
  4. Học viên nhấn nút 'Submit'. Hệ thống kết xuất Canvas thành ảnh và gửi tới dịch vụ AI OCR.
  5. Hệ thống nhận diện ký tự viết tay, so sánh với ký tự chuẩn và trả về: Trạng thái Đúng/Sai và Điểm tương đồng (%).
- **Phạm vi kỹ thuật & Giới hạn demo:**
  - *Tính năng được khẳng định:* Nhận diện chữ viết tay dựa trên trí tuệ nhân tạo (AI-based handwriting recognition) hoặc Nhận dạng ký tự hỗ trợ luyện viết chữ (Character recognition for handwriting practice).
  - *Giới hạn kỹ thuật (Tránh phóng đại trước Hội đồng phản biện):*
    - ❌ Không khẳng định "Hệ thống đánh giá đúng/sai thứ tự của từng nét vẽ đơn lẻ".
    - ❌ Không khẳng định "AI phân tích hướng kéo bút (stroke direction)".
    - ❌ Không khẳng định "AI chấm điểm thẩm mỹ thư pháp (calligraphy quality)".
- **Bảng dữ liệu liên quan:** `kanji`, `kana_characters`, `student_content_progress`, `student_submissions` (submission_type='handwriting', target_type='kanji'/'kana', expected_character, recognized_character, similarity_percent, is_correct)

---

## 4. CHI TIẾT USE CASE - NHÓM STAFF

Phần này đặc tả 12 Use Case của Nhân viên, được tổ chức theo 4 vai trò tác vụ chính: Quản lý học viên, Quản lý nội dung học tập, Hỗ trợ và Phân tích kết quả học tập.

### I. STUDENT MANAGEMENT (Quản lý Học viên)

#### UC-21: View Student Progress (Theo dõi học viên)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-21 |
| **Tên Use Case** | View Student Progress (Theo dõi học viên) |
| **Tác nhân** | Staff |
| **Mô tả** | Xem chi tiết tiến độ học tập, biểu đồ năng lực và lịch sử hoạt động của từng học viên. |
| **Tiền điều kiện** | Staff đã đăng nhập thành công. |

- **Luồng cơ bản:**
  1. Tìm kiếm học viên cần xem theo Tên hoặc Email.
  2. Hệ thống hiển thị: số Kanji, từ vựng, ngữ pháp đã học; chuỗi ngày học liên tục (streak); điểm số thi thử JLPT gần nhất và biểu đồ phát triển năng lực.
- **Bảng dữ liệu liên quan:** `student_users`, `vw_student_learning_stats`, `test_attempts`, `student_content_progress`

#### UC-22: Manage Student Accounts (Quản lý học viên)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-22 |
| **Tên Use Case** | Manage Student Accounts (Quản lý học viên) |
| **Tác nhân** | Staff |
| **Mô tả** | Xem danh sách, tra cứu hồ sơ và cập nhật thông tin trạng thái cơ bản của học viên. |
| **Tiền điều kiện** | Staff đã đăng nhập. |

- **Luồng cơ bản:**
  1. Vào danh sách học viên.
  2. Sử dụng bộ lọc (trạng thái tài khoản, cấp độ JLPT hiện tại) hoặc tìm kiếm theo email/tên.
  3. Click xem chi tiết hồ sơ học viên.
- **Bảng dữ liệu liên quan:** `student_users`

#### UC-23: Suspend or Activate Account (Khóa / Mở tài khoản)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-23 |
| **Tên Use Case** | Suspend or Activate Account (Khóa / Mở tài khoản) |
| **Tác nhân** | Staff |
| **Mô tả** | Tạm khóa tài khoản học viên vi phạm quy chế hoặc mở khóa hoạt động trở lại. |
| **Tiền điều kiện** | Staff đã đăng nhập. Học viên tồn tại trong hệ thống. |

- **Luồng cơ bản:**
  1. Tìm kiếm và truy cập hồ sơ học viên vi phạm.
  2. Nhấn 'Khóa tài khoản'. Nhập lý do khóa (bắt buộc).
  3. Hệ thống chuyển trạng thái tài khoản thành 'suspended', thu hồi mọi session đăng nhập hiện tại và gửi email thông báo lý do.
  4. *Luồng mở lại:* Staff nhấn 'Mở khóa', hệ thống chuyển trạng thái về 'active'.
- **Bảng dữ liệu liên quan:** `student_users` (status, suspend_reason), `auth_tokens` (token_type='session')

---

### II. CONTENT MANAGEMENT (Quản lý Nội dung)

#### UC-24: Manage Question Bank (Quản lý ngân hàng câu hỏi)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-24 |
| **Tên Use Case** | Manage Question Bank (Quản lý ngân hàng câu hỏi) |
| **Tác nhân** | Staff |
| **Mô tả** | Soạn thảo, chỉnh sửa và gửi duyệt câu hỏi trắc nghiệm/điền khuyết dùng chung cho các bài thi thử JLPT và bài luyện tập Quiz. |
| **Tiền điều kiện** | Staff đã đăng nhập. |

- **Luồng cơ bản:**
  1. Vào trang 'Quản lý ngân hàng câu hỏi'.
  2. Xem danh sách câu hỏi kèm theo bộ lọc kỹ năng, cấp độ JLPT, loại câu hỏi.
  3. Thêm mới câu hỏi: nhập câu hỏi, gán đáp án đúng/sai, thêm giải thích lý do đáp án đúng.
  4. Lưu câu hỏi ở trạng thái `draft` hoặc gửi duyệt để chuyển sang `pending_review`.
  5. StaffManager duyệt thì câu hỏi mới được chuyển sang `published`; nếu từ chối, câu hỏi quay về trạng thái cần chỉnh sửa kèm phản hồi.
- **Bảng dữ liệu liên quan:** `questions` (option_a, option_b, option_c, option_d, correct_option, correct_answer_text), `admin_audit_logs`

#### UC-25: Manage Grammar Content (Quản lý nội dung ngữ pháp)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-25 |
| **Tên Use Case** | Manage Grammar Content (Quản lý nội dung ngữ pháp) |
| **Tác nhân** | Staff |
| **Mô tả** | Soạn thảo, cập nhật và gửi duyệt thư viện các cấu trúc ngữ pháp tiếng Nhật theo từng cấp độ JLPT. |
| **Tiền điều kiện** | Staff đã đăng nhập. |

- **Luồng cơ bản:**
  1. Vào mục quản lý ngữ pháp.
  2. Thêm mới ngữ pháp: nhập cấu trúc, công thức kết hợp, ý nghĩa, giải thích cách dùng, chọn cấp độ JLPT và danh sách câu ví dụ minh họa Nhật - Việt.
  3. Chỉnh sửa nội dung ngữ pháp hoặc gửi yêu cầu lưu trữ/ẩn nội dung không phù hợp.
  4. Gửi nội dung hoàn tất cho StaffManager duyệt trước khi học viên nhìn thấy.
- **Bảng dữ liệu liên quan:** `grammar_points` (structure, formula, meaning, usage_explanation, jlpt_level, example_sentence_jp, example_sentence_vi), `admin_audit_logs`

#### UC-26: Manage Quiz (Quản lý bài Quiz)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-26 |
| **Tên Use Case** | Manage Quiz (Quản lý bài Quiz) |
| **Tác nhân** | Staff |
| **Mô tả** | Tạo, chỉnh sửa và gửi duyệt các bài kiểm tra ngắn (quiz) theo bài học để kiểm tra kiến thức nhanh. |
| **Tiền điều kiện** | Staff đã đăng nhập. |

- **Luồng cơ bản:**
  1. Chọn 'Tạo Quiz mới', nhập tiêu đề, chủ đề, cấp độ.
  2. Tìm kiếm và liên kết câu hỏi từ ngân hàng câu hỏi sang bài Quiz (thông qua bảng assignments).
  3. Gửi quiz sang trạng thái `pending_review` sau khi đã đủ câu hỏi, đáp án và giải thích.
  4. Quiz chỉ chuyển sang `published` và hiển thị cho học viên sau khi StaffManager duyệt.
- **Bảng dữ liệu liên quan:** `assessments` (assessment_type='quiz'), `question_assignments` (parent_type='assessment'), `admin_audit_logs`

#### UC-27: Manage Learning Content (Quản lý nội dung bài học)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-27 |
| **Tên Use Case** | Manage Learning Content (Quản lý nội dung bài học) |
| **Tác nhân** | Staff |
| **Mô tả** | Xây dựng bản nháp cấu trúc các khóa học JLPT, bài học, từ vựng và Kanji; gửi StaffManager duyệt trước khi xuất bản. |
| **Tiền điều kiện** | Staff đã đăng nhập. |

- **Luồng cơ bản:**
  1. *Khóa học & Bài học:* Thêm/sửa bản nháp khóa học và bài học (tiêu đề, video bài giảng YouTube/Vimeo, file đính kèm PDF, sắp xếp thứ tự).
  2. *Từ vựng & Kanji:* Thêm/sửa thông tin từ vựng (cách đọc, Furigana, ví dụ, audio) và Kanji (Onyomi, Kunyomi, ví dụ JSON, stroke_order_url).
  3. Gửi nội dung sang trạng thái `pending_review`; nội dung chưa được duyệt không xuất hiện trong danh sách học của Student.
- **Bảng dữ liệu liên quan:** `courses` (title, jlpt_level, thumbnail_url, status), `lessons` (course_id, lesson_type, title, video_url, audio_url, display_order, status), `vocabulary`, `kanji`, `admin_audit_logs`

#### UC-28: Manage JLPT Mock Exams (Quản lý đề thi thử JLPT)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-28 |
| **Tên Use Case** | Manage JLPT Mock Exams (Quản lý đề thi thử JLPT) |
| **Tác nhân** | Staff |
| **Mô tả** | Soạn thảo và gửi duyệt đề thi thử JLPT hoàn chỉnh giả lập cấu trúc đề thi thật (chứa cả tệp tin audio cho phần nghe). |
| **Tiền điều kiện** | Staff đã đăng nhập. Ngân hàng câu hỏi có sẵn dữ liệu. |

- **Luồng cơ bản:**
  1. Thêm đề thi mới: Nhập tên đề thi, cấp độ JLPT, thời gian làm bài.
  2. Thiết lập cấu trúc các phần thi (Lưu dưới dạng JSON cột sections).
  3. Chọn câu hỏi từ ngân hàng câu hỏi gán vào từng phần thi và thiết lập điểm số cho mỗi câu.
  4. Đăng tải file audio nghe hiểu cho phần thi nghe và gửi đề sang trạng thái `pending_review`.
  5. Đề thi chỉ được `published` sau khi StaffManager duyệt đầy đủ cấu trúc, câu hỏi, audio và thang điểm.
- **Bảng dữ liệu liên quan:** `assessments` (assessment_type='exam', duration_min, pass_score, total_score, audio_url), `question_assignments` (parent_type='assessment', section_name, score, display_order), `admin_audit_logs`

---

### III. SUPPORT (Hỗ trợ & Tác vụ trực tiếp)

#### UC-29: Respond to Student Support (Hỗ trợ người dùng)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-29 |
| **Tên Use Case** | Respond to Student Support (Hỗ trợ người dùng) |
| **Tác nhân** | Staff |
| **Mô tả** | Tiếp nhận, trả lời các yêu cầu trợ giúp kỹ thuật hoặc phản hồi bài học từ học viên qua hệ thống Ticket. |
| **Tiền điều kiện** | Staff đã đăng nhập. Học viên đã gửi ticket hỗ trợ. |

- **Luồng cơ bản:**
  1. Vào danh sách yêu cầu hỗ trợ (ticket).
  2. Chọn ticket đang chờ xử lý, xem nội dung chi tiết.
  3. Nhập phản hồi giải đáp thắc mắc và gửi.
  4. Đổi trạng thái ticket thành 'Resolved'.
- **Bảng dữ liệu liên quan:** `tickets`, `ticket_replies`

#### UC-30: Send Notifications (Gửi thông báo)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-30 |
| **Tên Use Case** | Send Notifications (Gửi thông báo) |
| **Tác nhân** | Staff |
| **Mô tả** | Soạn thảo và gửi thông báo khẩn cấp hoặc tin tức đến toàn bộ học viên hoặc các nhóm học viên cụ thể. |
| **Tiền điều kiện** | Staff đã đăng nhập. |

- **Luồng cơ bản:**
  1. Nhập Tiêu đề, Nội dung, chọn Loại thông báo (Tin tức, Cảnh báo, Khuyến mãi).
  2. Chọn đối tượng nhận (Tất cả / Theo cấp độ / Học viên cụ thể) và kênh truyền tải (In-app, Email, hoặc Cả hai).
  3. Nhấn 'Gửi' hoặc hẹn giờ phát thông báo.
- **Bảng dữ liệu liên quan:** `notifications` (student_id, title, content, notification_type, channel, scheduled_at, sent_at)

#### UC-31: Grade Speaking Submission (Chấm bài nói)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-31 |
| **Tên Use Case** | Grade Speaking Submission (Chấm bài nói) |
| **Tác nhân** | Staff |
| **Mô tả** | Nghe file âm thanh ghi âm Shadowing của học viên, chấm điểm thủ công (thang điểm 10) và viết lời nhận xét chi tiết. |
| **Tiền điều kiện** | Staff đã đăng nhập. Có bài nộp Speaking mới của học viên. |

- **Luồng cơ bản:**
  1. Vào danh sách 'Bài luyện nói cần chấm'.
  2. Chọn bài nộp của học viên, xem đề bài, audio mẫu và trình phát file ghi âm của học viên.
  3. Staff nghe file ghi âm và nhập điểm số, viết nhận xét chi tiết.
  4. Click 'Lưu điểm & Gửi nhận xét' $\rightarrow$ Hệ thống cập nhật trạng thái bài nộp thành 'graded' và gửi thông báo tự động cho học viên.
- **Bảng dữ liệu liên quan:** `student_submissions`, `notifications`

---

### IV. ANALYTICS (Phân tích)

#### UC-32: View Quiz Results (Xem kết quả kiểm tra)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-32 |
| **Tên Use Case** | View Quiz Results (Xem kết quả kiểm tra) |
| **Tác nhân** | Staff |
| **Mô tả** | Xem danh sách, thống kê kết quả làm bài tập trắc nghiệm và đề thi thử của học viên để đánh giá hiệu suất tiếp thu. |
| **Tiền điều kiện** | Staff đã đăng nhập. |

- **Luồng cơ bản:**
  1. Chọn bài Quiz hoặc bài thi cần xem thống kê.
  2. Hệ thống hiển thị danh sách học viên đã thực hiện kèm điểm số, thời gian làm và ngày nộp bài.
  3. Xem chi tiết bài làm của từng học viên (xem câu hỏi đúng/sai).
  4. Xem phân tích tỷ lệ trả lời đúng/sai của từng câu hỏi để cải thiện đề thi.
- **Bảng dữ liệu liên quan:** `test_attempts`, `attempt_answers`, `student_users`

---

## 5. CHI TIẾT USE CASE - NHÓM STAFFMANAGER

Phần này đặc tả các Use Case dành cho StaffManager, role quản lý nội dung nằm trong nhóm tài khoản Staff nhưng có quyền duyệt và điều phối chất lượng nội dung cao hơn Staff thường.

### UC-33: Review Submitted Content (Duyệt nội dung Staff gửi)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-33 |
| **Tên Use Case** | Review Submitted Content (Duyệt nội dung Staff gửi) |
| **Tác nhân** | StaffManager |
| **Mô tả** | StaffManager xem hàng đợi nội dung do Staff gửi, kiểm tra chất lượng và quyết định duyệt, từ chối hoặc yêu cầu chỉnh sửa. |
| **Tiền điều kiện** | StaffManager đã đăng nhập; có nội dung ở trạng thái `pending_review`. |

- **Luồng cơ bản:**
  1. StaffManager mở màn hình Review Queue.
  2. Lọc nội dung theo loại, cấp độ JLPT, người gửi, trạng thái hoặc thời gian gửi.
  3. Mở chi tiết nội dung, xem snapshot, lịch sử chỉnh sửa và thông tin người gửi.
  4. Chọn `Approve`, `Reject` hoặc `Request Changes`.
  5. Nếu duyệt, hệ thống cập nhật nội dung sang trạng thái được phép publish và ghi nhận reviewer.
  6. Nếu từ chối hoặc yêu cầu sửa, StaffManager nhập phản hồi bắt buộc để Staff chỉnh lại.
- **Luồng thay thế:**
  - *Nội dung đã được reviewer khác xử lý:* Hệ thống báo xung đột và tải lại trạng thái mới nhất.
  - *Thiếu câu hỏi/đáp án/audio/thang điểm:* Hệ thống không cho duyệt và yêu cầu phản hồi chỉnh sửa.
- **Hậu điều kiện:** Quyết định duyệt được lưu, Staff nhận thông báo, và audit log được ghi.
- **Bảng dữ liệu liên quan:** `courses`, `lessons`, `grammar_points`, `vocabulary`, `kanji`, `questions`, `assessments` (assessment_type='quiz'/'exam'), `admin_audit_logs`

### UC-34: Manage Published Content Status (Quản lý trạng thái xuất bản)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-34 |
| **Tên Use Case** | Manage Published Content Status (Quản lý trạng thái xuất bản) |
| **Tác nhân** | StaffManager |
| **Mô tả** | StaffManager xuất bản nội dung đã duyệt, tạm ẩn nội dung có vấn đề, lưu trữ nội dung cũ hoặc khôi phục nội dung đủ điều kiện. |
| **Tiền điều kiện** | StaffManager đã đăng nhập; nội dung tồn tại và không bị xóa mềm. |

- **Luồng cơ bản:**
  1. StaffManager mở danh sách nội dung đã duyệt hoặc đã xuất bản.
  2. Chọn hành động `Publish`, `Unpublish`, `Archive` hoặc `Restore`.
  3. Nhập lý do với các hành động ảnh hưởng tới nội dung đang hiển thị cho học viên.
  4. Hệ thống cập nhật trạng thái nội dung và ghi lại thời điểm, người thực hiện, lý do.
  5. Hệ thống cập nhật khả năng hiển thị của nội dung trong trang học, tìm kiếm, quiz hoặc đề thi.
- **Luồng thay thế:**
  - *Nội dung đang được dùng trong quiz/exam đã published:* Hệ thống cảnh báo ảnh hưởng và yêu cầu xác nhận nghiệp vụ trước khi ẩn.
  - *Staff thường cố gắng publish/unpublish:* Hệ thống từ chối do không đủ quyền.
- **Hậu điều kiện:** Chỉ nội dung `published` hợp lệ được hiển thị cho Student; mọi thay đổi trạng thái được audit.
- **Bảng dữ liệu liên quan:** `courses`, `lessons`, `grammar_points`, `vocabulary`, `kanji`, `questions`, `assessments`, `admin_audit_logs`

---

## 6. CHI TIẾT USE CASE - NHÓM ADMIN

Phần này đặc tả 6 Use Case cốt lõi của Quản trị viên - người chịu trách nhiệm cao nhất về kỹ thuật, phân quyền người dùng, cấu hình và báo cáo toàn diện của hệ thống.

### UC-35: Login System (Đăng nhập Admin Panel)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-35 |
| **Tên Use Case** | Login System (Đăng nhập Admin Panel) |
| **Tác nhân** | Admin |
| **Mô tả** | Admin đăng nhập vào bảng điều khiển quản trị hệ thống (Admin Panel) bằng Email/Password, nhận JWT trực tiếp sau khi xác thực thành công. |
| **Tiền điều kiện** | Tài khoản Admin đã được kích hoạt trên hệ thống. |

- **Luồng cơ bản:**
  1. Admin truy cập trang đăng nhập chung, nhập Email và Mật khẩu.
  2. Hệ thống kiểm tra thông tin tài khoản.
  3. Thông tin chính xác $\rightarrow$ Cấp JWT access token và refresh token, chuyển hướng đến Admin Dashboard.
- **Bảng dữ liệu liên quan:** `admin_users`, `auth_tokens` (token_type='refresh')

---

### UC-36: View Dashboard (Bảng điều khiển)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-36 |
| **Tên Use Case** | View Dashboard (Bảng điều khiển) |
| **Tác nhân** | Admin |
| **Mô tả** | Admin xem tổng quan sức khỏe hệ thống và các số liệu thống lai nhanh về người dùng cũng như lượng tương tác. |
| **Tiền điều kiện** | Admin đã đăng nhập thành công. |

- **Luồng cơ bản:**
  1. Admin vào trang chủ quản trị (Dashboard).
  2. Hệ thống tổng hợp dữ liệu thời gian thực và hiển thị: tổng số học viên, số tài khoản đăng ký mới trong tháng.
  3. Số lượt làm bài thi/quiz ngày hôm nay; biểu đồ thống kê hoạt động truy cập hệ thống 30 ngày qua.
  4. Thống kê nhanh số lượng khóa học đang hoạt động.
- **Bảng dữ liệu liên quan:** `admin_users`, `staff_users`, `student_users`, `auth_tokens`, `test_attempts`, `student_content_progress`, `courses`

---

### UC-37: User Management (Quản lý người dùng)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-37 |
| **Tên Use Case** | User Management (Quản lý người dùng) |
| **Tác nhân** | Admin |
| **Mô tả** | Admin quản lý thông tin toàn bộ tài khoản người dùng và thực hiện phân quyền (Student, Staff, StaffManager, Admin) trong hệ thống. |
| **Tiền điều kiện** | Admin đã đăng nhập thành công. |

- **Luồng cơ bản:**
  1. Vào mục 'Quản lý người dùng'.
  2. Hệ thống hiển thị danh sách tất cả các tài khoản kèm bộ lọc theo vai trò (Role) và trạng thái hoạt động.
  3. Click 'Tạo tài khoản' để tạo tài khoản Nhân viên (Staff), StaffManager hoặc Admin mới.
  4. Chỉnh sửa thông tin tài khoản, thay đổi vai trò phân quyền (ví dụ: chuyển Staff thường lên StaffManager).
  5. Đổi trạng thái tài khoản (Kích hoạt / Tạm khóa / Xóa).
  6. Thực hiện đặt lại mật khẩu thủ công cho tài khoản khi có yêu cầu hỗ trợ.
- **Bảng dữ liệu liên quan:** `admin_users`, `staff_users`, `student_users`, `auth_tokens`

---

### UC-38: Report Screen (Báo cáo & Thống kê)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-38 |
| **Tên Use Case** | Report Screen (Báo cáo & Thống kê) |
| **Tác nhân** | Admin |
| **Mô tả** | Admin truy cập màn hình báo cáo chi tiết về kết quả học tập và hoạt động hệ thống, cho phép xuất dữ liệu. |
| **Tiền điều kiện** | Admin đã đăng nhập thành công. |

- **Luồng cơ bản:**
  1. Vào trang 'Báo cáo & Thống kê'.
  2. Chọn loại báo cáo: tăng trưởng người dùng, hiệu suất học tập (tỷ lệ hoàn thành khóa học, phổ điểm JLPT), hoạt động hệ thống.
  3. Thiết lập bộ lọc thời gian cần báo cáo.
  4. Hệ thống tạo biểu đồ và bảng dữ liệu tương ứng.
  5. Nhấn 'Xuất file' để xuất dữ liệu định dạng PDF/CSV/Excel.
- **Bảng dữ liệu liên quan:** `admin_users`, `staff_users`, `student_users`, `auth_tokens`, `test_attempts`, `student_content_progress`, `courses`, `lessons` *(báo cáo on-the-fly trực tiếp từ dữ liệu gốc)*

---

### UC-39: Settings (Cài đặt hệ thống)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-39 |
| **Tên Use Case** | Settings (Cài đặt hệ thống) |
| **Tác nhân** | Admin |
| **Mô tả** | Admin cấu hình các cài đặt hệ thống kỹ thuật cốt lõi cho toàn bộ nền tảng. |
| **Tiền điều kiện** | Admin đã đăng nhập thành công. |

- **Luồng cơ bản:**
  1. Vào trang 'Cài đặt hệ thống'.
  2. Chỉnh sửa các thông số kỹ thuật: tên nền tảng, logo, ngôn ngữ hiển thị mặc định.
  3. Cấu hình SMTP Mail Server phục vụ gửi email kích hoạt, khôi phục mật khẩu.
  4. Cài đặt quy định: giới hạn đăng nhập sai tối đa (ví dụ 5 lần), thời gian hết hạn phiên (session timeout).
  5. Trạng thái hệ thống: bật/tắt chế độ bảo trì, bật/tắt cho phép đăng ký tài khoản tự do.
  6. Nhấn 'Lưu cấu hình' để áp dụng cho toàn hệ thống.
- **Bảng dữ liệu liên quan:** `system_settings`

---

### UC-40: Notification Rules (Quản lý quy tắc thông báo)

| Thuộc tính | Mô tả chi tiết |
| :--- | :--- |
| **Mã Use Case** | UC-40 |
| **Tên Use Case** | Notification Rules (Quản lý quy tắc thông báo) |
| **Tác nhân** | Admin |
| **Mô tả** | Admin quản lý danh sách các thông báo toàn hệ thống và thiết lập các kịch bản, quy tắc gửi thông báo tự động (Notification Rules). |
| **Tiền điều kiện** | Admin đã đăng nhập thành công. |

- **Luồng cơ bản:**
  1. Vào trang 'Cấu hình Thông báo'.
  2. Gửi thông báo thủ công: soạn nội dung và gửi hàng loạt cho toàn bộ học viên hoặc nhóm học viên nhất định.
  3. Thiết lập Notification Rules: cấu hình các kịch bản kích hoạt thông báo tự động khi học viên đạt cột mốc (streak 10 ngày, nhắc ôn Flashcard, thông báo kết quả thi...).
  4. Soạn thảo template thông báo động có chứa biến (lưu dưới dạng JSON) và bật/tắt trạng thái hoạt động của từng quy tắc.
- **Bảng dữ liệu liên quan:** `notifications` (is_auto=1, rule_key, channel), `system_settings` (setting_group='auto_notification')

---

## 7. KẾT LUẬN

Tài liệu đã tổng hợp và đặc tả chi tiết 40 Use Case của hệ thống học tiếng Nhật JLPT, phân theo 4 nhóm tác nhân với vai trò rõ ràng:

1. **Nhóm Student (20 UC):** Đảm bảo trải nghiệm học tập toàn diện từ học Kana đến luyện thi JLPT cao cấp, tích hợp công nghệ AI (Speech Recognition, OCR) để nâng cao hiệu quả luyện kỹ năng nói và viết.
2. **Nhóm Staff (12 UC):** Phụ trách soạn thảo nội dung học liệu, gửi duyệt, chấm bài thủ công và hỗ trợ học viên.
3. **Nhóm StaffManager (2 UC):** Phụ trách duyệt, xuất bản, ẩn, lưu trữ và kiểm soát chất lượng nội dung Staff tạo trước khi học viên nhìn thấy.
4. **Nhóm Admin (6 UC):** Tập trung vào quản trị kỹ thuật, phân quyền, báo cáo và cấu hình hệ thống - đảm bảo nền tảng vận hành ổn định và mở rộng được.

Việc phân chia trách nhiệm rõ ràng giữa Admin, StaffManager và Staff giúp hệ thống dễ bảo trì, giảm rủi ro nội dung chưa kiểm duyệt xuất hiện với học viên, dễ mở rộng quy mô đội ngũ và phù hợp với mô hình SaaS giáo dục hiện đại.

Tài liệu này là cơ sở để nhóm phát triển tiến hành thiết kế cơ sở dữ liệu chi tiết, thiết kế kiến trúc phần mềm, xây dựng giao diện người dùng và lập kế hoạch kiểm thử nghiệm thu.
