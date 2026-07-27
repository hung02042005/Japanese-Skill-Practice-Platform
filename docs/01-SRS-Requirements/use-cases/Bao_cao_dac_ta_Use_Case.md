# BÁO CÁO ĐẶC TẢ USE CASE

## HỆ THỐNG HỌC VÀ LUYỆN TẬP TIẾNG NHẬT JLPT

| Thuộc tính | Giá trị |
| :--- | :--- |
| Tên tài liệu | Báo cáo đặc tả Use Case theo hiện trạng triển khai |
| Phiên bản | 2.0 |
| Ngày cập nhật | 26/07/2026 |
| Phạm vi đối chiếu | `apps/frontend/src` và `apps/backend/src/main/java` |
| Trạng thái | As-built (phản ánh code hiện tại) |

> Tài liệu này mô tả các chức năng có route giao diện và/hoặc API trong code hiện tại. Các ý tưởng chưa được triển khai không được xem là use case khả dụng.

---

## 1. GIỚI THIỆU

### 1.1. Mục đích

Tài liệu là căn cứ để phát triển, kiểm thử và nghiệm thu hệ thống. Mỗi use case được đối chiếu với route React, REST API, phân quyền Spring Security và dữ liệu nghiệp vụ đang tồn tại trong source code.

### 1.2. Phạm vi hệ thống hiện tại

Hệ thống cung cấp:

- Xác thực tài khoản Student, Staff, StaffManager và Admin bằng JWT.
- Học Kana, Kanji, từ vựng, ngữ pháp, bài học; luyện flashcard, quiz, thi thử và speaking.
- Theo dõi tiến độ, tra từ điển, quản lý sổ tay, ticket hỗ trợ và thông báo.
- Soạn nội dung, quản lý ngân hàng câu hỏi, quiz, đề thi, chấm bài và hỗ trợ học viên.
- Duyệt, xuất bản, ẩn, lưu trữ và khôi phục nội dung.
- Quản trị người dùng, dashboard, cấu hình hệ thống, quy tắc thông báo và audit log.

### 1.3. Tác nhân và phân quyền

| Tác nhân | Quyền chính | Cơ chế bảo vệ |
| :--- | :--- | :--- |
| Khách | Xem trang công khai, đăng ký, đăng nhập, khôi phục mật khẩu | Các endpoint auth công khai |
| Student | Học tập, làm bài, quản lý hồ sơ và dữ liệu cá nhân | `ROLE_STUDENT`, `PrivateRoute` |
| Staff | Soạn nội dung, chấm bài, hỗ trợ học viên | `ROLE_STAFF`, `StaffRoute` |
| StaffManager | Toàn bộ quyền Staff và các nghiệp vụ duyệt/điều phối | `ROLE_STAFF` kết hợp `staffRole=staff_manager`, `ManagerRoute` |
| Admin | Quản trị tài khoản, cấu hình và audit | `ROLE_ADMIN`, `AdminRoute` |
| Dịch vụ ngoài | Google OAuth, email SMTP, dịch vụ đánh giá giọng nói | Được backend gọi trong các luồng tương ứng |

### 1.4. Quy ước chung

- API trả về cấu trúc `status`, `message`, `data`.
- Endpoint được bảo vệ yêu cầu JWT hợp lệ; sai vai trò trả về `403`.
- Xóa tài khoản/nội dung là xóa mềm; dữ liệu có thể được khôi phục nếu nghiệp vụ hỗ trợ.
- Điểm bài thi và kết quả AI do backend xử lý; client không được tự quyết định điểm cuối cùng.
- Danh sách lớn dùng phân trang và có thể lọc theo các tham số của từng API.

---

## 2. BẢNG TỔNG HỢP USE CASE

| Nhóm | Mã | Use case hiện có |
| :--- | :--- | :--- |
| Xác thực chung | UC-01 → UC-05 | Đăng ký/xác minh email, đăng nhập, làm mới/đăng xuất, khôi phục mật khẩu, thiết lập mật khẩu Staff |
| Student | UC-06 → UC-22 | Onboarding; dashboard; hồ sơ; khóa học/bài học; Kana; từ vựng; ngữ pháp; Kanji/luyện viết; quiz; thi thử; flashcard/sổ tay; từ điển; speaking; tiến độ; hỗ trợ; thông báo |
| Staff | UC-23 → UC-32 | Dashboard; học viên; câu hỏi; ngữ pháp; học liệu; quiz/đề thi; gửi duyệt; ticket; broadcast; chấm bài |
| StaffManager | UC-33 → UC-36 | Hàng đợi duyệt; quản lý xuất bản; thùng rác nội dung; điều phối ticket và broadcast |
| Admin | UC-37 → UC-40 | Dashboard/audit; người dùng; cài đặt; quy tắc thông báo |

Tổng cộng: **40 use case phản ánh code hiện tại**.

---

## 3. ĐẶC TẢ USE CASE XÁC THỰC CHUNG

### UC-01: Đăng ký và xác minh email

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tác nhân | Khách |
| Tiền điều kiện | Email chưa được dùng cho tài khoản Student |
| Kích hoạt | Khách mở `/register` |
| Luồng chính | 1. Nhập thông tin đăng ký. 2. Frontend gọi `POST /api/auth/register`. 3. Hệ thống tạo tài khoản chưa xác minh và gửi mã/email xác minh. 4. Người dùng mở `/verify-email`, gửi mã qua `POST /api/auth/verify-email`. 5. Tài khoản được kích hoạt. |
| Luồng thay thế | Có thể yêu cầu gửi lại qua `POST /api/auth/resend-verification`; email trùng hoặc mã sai/hết hạn bị từ chối. |
| Hậu điều kiện | Student có tài khoản đã xác minh và có thể đăng nhập |

### UC-02: Đăng nhập theo loại tài khoản

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tác nhân | Student, Staff, StaffManager, Admin |
| Tiền điều kiện | Tài khoản tồn tại, hoạt động và đã đáp ứng yêu cầu xác minh/thiết lập mật khẩu |
| Luồng chính | 1. Người dùng nhập email, mật khẩu tại `/login`. 2. Hệ thống kiểm tra loại tài khoản qua `POST /api/auth/check-account-type`. 3. Student/Admin đăng nhập qua `POST /api/auth/login`; Staff/StaffManager qua `POST /api/staff/auth/login`. 4. Backend trả access token, refresh token và vai trò. 5. Frontend chuyển đến dashboard phù hợp. |
| Luồng thay thế | Sai thông tin, tài khoản bị khóa/xóa hoặc chưa xác minh: từ chối đăng nhập; Staff dùng mật khẩu tạm được chuyển đến trang đổi mật khẩu. |
| Hậu điều kiện | Phiên xác thực được tạo |

### UC-03: Làm mới phiên và đăng xuất

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tác nhân | Người dùng đã đăng nhập |
| Luồng chính | 1. Khi access token hết hạn, client gửi refresh token đến `POST /api/auth/refresh`. 2. Backend xác thực token và cấp access token mới. 3. Khi đăng xuất, client gọi `POST /api/auth/logout`. 4. Token tương ứng bị vô hiệu hóa và dữ liệu phiên phía client được xóa. |
| Luồng thay thế | Refresh token sai, hết hạn hoặc đã bị thu hồi: yêu cầu đăng nhập lại. |
| Hậu điều kiện | Phiên được gia hạn hoặc kết thúc an toàn |

### UC-04: Quên và đặt lại mật khẩu

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tác nhân | Student/Admin chưa đăng nhập |
| Luồng chính | 1. Nhập email tại `/forgot-password`. 2. Gọi `POST /api/auth/forgot-password`. 3. Hệ thống gửi liên kết/mã đặt lại. 4. Người dùng mở `/reset-password`, nhập mật khẩu mới. 5. Gọi `POST /api/auth/reset-password`. |
| Luồng thay thế | Email không hợp lệ hoặc token sai/hết hạn: không đổi mật khẩu. |
| Hậu điều kiện | Mật khẩu mới được lưu, token đặt lại không còn hiệu lực |

### UC-05: Thiết lập và khôi phục mật khẩu Staff

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tác nhân | Staff, StaffManager |
| Luồng chính | Staff mới mở `/staff/setup-password` và gọi `POST /api/staff/auth/setup-password`; Staff quên mật khẩu mở `/staff/forgot-password` và gọi `POST /api/staff/auth/forgot-password`; sau khi Admin cấp mật khẩu tạm, Staff dùng `/staff/change-temp-password` và `POST /api/staff/auth/change-temp-password` để đặt mật khẩu chính thức. |
| Luồng thay thế | Token thiết lập hoặc mật khẩu tạm không hợp lệ/hết hạn: hệ thống từ chối. |
| Hậu điều kiện | Staff có mật khẩu chính thức và đăng nhập được |

---

## 4. ĐẶC TẢ USE CASE NHÓM STUDENT

### UC-06: Thiết lập mục tiêu học tập (Onboarding)

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tiền điều kiện | Student đã đăng nhập |
| Luồng chính | 1. Mở `/onboarding`. 2. Chọn mục tiêu JLPT, số phút học mỗi ngày và kỹ năng ưu tiên. 3. Gọi `POST /api/students/onboarding`. 4. Backend lưu cấu hình học tập. |
| Hậu điều kiện | Dashboard và lộ trình có dữ liệu cá nhân hóa ban đầu |

### UC-07: Xem dashboard và thống kê cá nhân

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tiền điều kiện | Student đã đăng nhập |
| Luồng chính | Mở `/dashboard`; frontend lấy tổng quan từ `GET /api/students/dashboard`, thống kê từ `GET /api/students/me/stats` và bài tiếp theo từ `GET /api/students/next-lesson`. |
| Luồng thay thế | Chưa có hoạt động: hiển thị trạng thái bắt đầu học thay vì số liệu rỗng lỗi. |

### UC-08: Quản lý hồ sơ và thông tin bảo mật

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Xem hồ sơ bằng `GET /api/students/me`. 2. Cập nhật họ tên/số điện thoại bằng `PUT /api/students/me`. 3. Tải avatar qua `POST /api/students/me/avatar`. 4. Đổi mật khẩu qua `PUT /api/students/me/password`. 5. Đổi email bằng bước gửi OTP `POST /api/students/me/email/otp` và xác nhận `PUT /api/students/me/email`. |
| Luồng thay thế | Mật khẩu hiện tại sai, OTP sai/hết hạn, email trùng hoặc file avatar không hợp lệ: không cập nhật. |
| Hậu điều kiện | Hồ sơ được cập nhật và dữ liệu bảo mật được xác minh ở backend |

### UC-09: Chọn khóa học và xem bài học

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/courses`, lấy danh sách qua `GET /api/students/courses`. 2. Chọn bài học `/lessons/:id`. 3. Frontend gọi `GET /api/lessons/{lessonId}`. 4. Student học nội dung và gửi tiến độ qua `POST /api/learning-progress`. |
| Luồng thay thế | Bài học không tồn tại, chưa xuất bản hoặc không được truy cập: trả `404/403`. |

### UC-10: Học Kana

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Student mở `/kana`; frontend gọi `GET /api/kana`; hệ thống trả danh sách Hiragana/Katakana và dữ liệu học hiện có. |
| Hậu điều kiện | Student có thể ghi nhận tiến độ qua API learning progress |

### UC-11: Học từ vựng theo cấp độ/chủ đề

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/vocabulary`. 2. Lấy trang tổng quan từ `GET /api/students/vocab-home`. 3. Lấy chủ đề qua `GET /api/vocabulary/topics`. 4. Lọc/tìm từ qua `GET /api/vocabulary`. 5. Đánh dấu hoàn thành bằng learning progress. |
| Luồng thay thế | Không có dữ liệu phù hợp bộ lọc: trả danh sách rỗng có phân trang. |

### UC-12: Học ngữ pháp

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/grammar`. 2. Lọc theo JLPT và phân trang bằng `GET /api/grammar-points`. 3. Mở chi tiết qua `GET /api/grammar-points/{grammarId}`. |
| Luồng thay thế | Điểm ngữ pháp không tồn tại/không xuất bản: trả `404`. |

### UC-13: Học Kanji và luyện viết

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/kanji`, lấy danh sách bằng `GET /api/kanji`. 2. Mở `/kanji/:id`, lấy chi tiết qua `GET /api/kanji/{kanjiId}`. 3. Vẽ từng nét. 4. Gửi đường nét và mẫu tham chiếu tới `POST /api/kanji/writing/evaluate-stroke`. 5. Backend tính mức tương đồng bằng DTW. 6. Khi hoàn tất, lưu phiên qua `POST /api/kanji/writing/attempt`. |
| Luồng thay thế | Dữ liệu nét không hợp lệ: không chấm/lưu; frontend cho phép vẽ lại. |
| Hậu điều kiện | Kết quả luyện viết được lưu ở backend |

### UC-14: Làm quiz

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/quiz`. 2. Lấy assessment loại quiz từ `GET /api/assessments`. 3. Bắt đầu bằng `POST /api/assessments/{id}/start`. 4. Trả lời và nộp qua `POST /api/assessments/{id}/submit`. 5. Backend tính điểm và tạo attempt mới. |
| Luồng thay thế | Assessment không khả dụng, attempt không hợp lệ hoặc hết thời gian: backend từ chối. |
| Hậu điều kiện | Điểm và câu trả lời được lưu bất biến theo attempt |

### UC-15: Làm đề thi thử và xem kết quả

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Chọn đề tại `/mock-test`. 2. Bắt đầu đề tại `/mock-test/:id/attempt`. 3. Nộp bài qua assessment API. 4. Mở `/mock-test/:id/results`. 5. Lấy lịch sử qua `GET /api/test-attempts` và chi tiết đúng/sai qua `GET /api/test-attempts/{attemptId}/review`. |
| Luồng thay thế | Không được xem attempt của người khác; attempt chưa nộp không có review hoàn chỉnh. |

### UC-16: Ôn tập bằng Flashcard SRS

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/vocabulary/flashcard`. 2. Tạo/lấy phiên bằng `POST /api/flashcards/session`. 3. Lật thẻ và chọn mức độ nhớ. 4. Gửi đánh giá đến `POST /api/flashcards/{id}/review`. 5. Backend cập nhật lịch ôn SRS. |
| Hậu điều kiện | Lần ôn tiếp theo được tính và lưu ở backend |

### UC-17: Quản lý sổ tay từ vựng

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/notebook`. 2. Lấy deck bằng `GET /api/notebook/decks`, thẻ bằng `GET /api/notebook/cards`. 3. Lưu từ qua `POST /api/notebook/words`. 4. Gỡ một thẻ qua `DELETE /api/notebook/cards/{id}` hoặc gỡ nhiều thẻ qua `POST /api/notebook/cards/bulk-delete`. |
| Ghi chú | Đây là sổ tay từ/flashcard; code hiện tại không có bookmark tổng quát cho mọi loại nội dung. |

### UC-18: Tra từ điển

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/dictionary`. 2. Nhập từ khóa và gọi `GET /api/dictionary/search`. 3. Có thể giới hạn loại dữ liệu bằng `GET /api/dictionary/search/{type}`. 4. Student có thể lưu kết quả phù hợp vào sổ tay. |
| Luồng thay thế | Từ khóa rỗng/không hợp lệ bị từ chối; không tìm thấy trả danh sách rỗng. |

### UC-19: Luyện nói và nhận đánh giá AI

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/speaking`. 2. Lấy bài luyện qua `GET /api/speaking/exercises`. 3. Ghi âm và gửi multipart tới `POST /api/speaking/submit`. 4. Backend trả job ID. 5. Frontend truy vấn `GET /api/speaking/{jobId}` đến khi có kết quả. |
| Luồng thay thế | File sai định dạng/quá giới hạn hoặc dịch vụ AI lỗi: job trả trạng thái lỗi/fallback. |
| Hậu điều kiện | Bài nộp và gợi ý điểm AI được lưu để Staff có thể chấm lại |

### UC-20: Xem và cập nhật tiến độ

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Student mở `/progress`; hệ thống tổng hợp thống kê cá nhân. Mỗi hoạt động hoàn thành gửi `POST /api/learning-progress` với loại nội dung, ID, trạng thái và phần trăm. |
| Luồng phụ | `DELETE /api/learning-progress/reset` cho phép đặt lại tiến độ theo loại nội dung được API hỗ trợ. |

### UC-21: Gửi và theo dõi ticket hỗ trợ

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/support`. 2. Tạo ticket qua `POST /api/support/tickets`. 3. Xem danh sách/chi tiết qua `GET /api/support/tickets` và `GET /api/support/tickets/{ticketId}`. 4. Phản hồi qua `POST .../reply`. 5. Đóng ticket qua `POST .../close`. |
| Phân quyền | Student chỉ được thao tác ticket của mình. |

### UC-22: Xem và đánh dấu thông báo

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/notifications`. 2. Lấy danh sách qua `GET /api/notifications`. 3. Đánh dấu một thông báo đã đọc qua `POST /api/notifications/{id}/read` hoặc tất cả qua `POST /api/notifications/read-all`. |
| Hậu điều kiện | Trạng thái đã đọc được lưu theo Student |

---

## 5. ĐẶC TẢ USE CASE NHÓM STAFF

### UC-23: Xem dashboard Staff

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Staff mở `/staff`; frontend gọi `GET /api/staff/dashboard`; hệ thống trả các số liệu và tác vụ nội dung/hỗ trợ cần xử lý. |

### UC-24: Theo dõi và thay đổi trạng thái Student

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/staff/students`. 2. Lọc danh sách qua `GET /api/staff/students`. 3. Xem tiến độ qua `GET /api/staff/students/{id}/progress`. 4. Khóa hoặc kích hoạt qua `POST .../suspend` và `POST .../activate`. |
| Luồng thay thế | Không tìm thấy Student hoặc chuyển trạng thái không hợp lệ: từ chối. |
| Hậu điều kiện | Trạng thái thay đổi được audit |

### UC-25: Quản lý ngân hàng câu hỏi

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Tại `/staff/questions`, Staff tạo, lọc, xem và sửa câu hỏi qua `/api/staff/questions`; gắn đáp án/nội dung cần thiết; gửi duyệt qua `POST /api/staff/questions/{id}/submit-review`. |
| Ràng buộc | Câu hỏi đã bị khóa bởi attempt không được sửa theo cách làm thay đổi kết quả cũ. |

### UC-26: Quản lý nội dung ngữ pháp

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Tại `/staff/content`, Staff tạo, xem, lọc, sửa grammar qua `/api/staff/grammar`; sau đó gửi duyệt qua `POST /api/staff/grammar/{id}/submit-review`. |

### UC-27: Quản lý bài học, từ vựng, Kanji và chủ đề

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Staff xem/sửa lesson; tạo/sửa/xem vocabulary và Kanji qua `/api/staff`; xem/tạo chủ đề từ vựng qua `/api/staff/vocabulary-topics`; có thể tạo/sửa/xem speaking lesson qua `/api/staff/speaking-lessons`. |
| Ghi chú | Code hiện tại có API cập nhật lesson nhưng không có endpoint tạo lesson mới trong controller này. |

### UC-28: Quản lý Quiz

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Tại `/staff/assessments`, Staff tạo, lọc, xem, sửa quiz qua `/api/staff/assessments`; gán câu hỏi qua `POST /api/staff/assessments/{id}/assign-questions`. |
| Hậu điều kiện | Quiz ở trạng thái nháp/cập nhật và có thể gửi duyệt |

### UC-29: Quản lý đề thi thử

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Staff tạo, lọc, xem, sửa exam qua `/api/staff/exams`; gán câu hỏi qua `POST /api/staff/exams/{id}/assign-questions`. |
| Ràng buộc | Backend kiểm tra loại assessment là exam và dữ liệu phân bổ câu hỏi hợp lệ. |

### UC-30: Gửi nội dung để duyệt và xem phản hồi

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Staff hoàn tất nội dung. 2. Gửi `POST /api/staff/contents/submit-review` với `contentType`, `contentId` (hoặc endpoint chuyên biệt của question/grammar). 3. Nội dung chuyển sang chờ duyệt. 4. Staff xem phản hồi qua `GET /api/staff/content/{contentId}/feedback`. 5. Nếu bị yêu cầu sửa, Staff chỉnh sửa và gửi lại. |

### UC-31: Xử lý ticket và gửi broadcast

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Tại `/staff/tickets`, Staff lọc/xem ticket qua `/api/staff/tickets`, phản hồi ticket được giao và đóng ticket. Staff/Manager có thể gửi thông báo broadcast bất đồng bộ qua `POST /api/staff/notifications`, chọn loại, kênh, cấp JLPT và thời gian gửi. |
| Phân quyền | Staff thường chỉ xử lý ticket được giao; quyền phân công thuộc StaffManager/Admin. |

### UC-32: Chấm bài Speaking

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/staff/grading`. 2. Lấy bài speaking qua `GET /api/staff/submissions?type=speaking`. 3. Xem chi tiết và điểm AI qua `GET /api/staff/submissions/{id}`. 4. Nhập điểm/nhận xét thủ công. 5. Gửi `POST /api/staff/submissions/{id}/grade`. |
| Hậu điều kiện | Điểm thủ công là điểm cuối, Student nhận thông báo và thao tác được audit |

---

## 6. ĐẶC TẢ USE CASE NHÓM STAFFMANAGER

### UC-33: Duyệt nội dung trong Review Queue

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tiền điều kiện | Người dùng có `ROLE_STAFF` và `staffRole=staff_manager` |
| Luồng chính | 1. Mở `/manager/review-queue`. 2. Lọc hàng đợi qua `GET /api/manager/review-queue`. 3. Xem snapshot chi tiết qua `GET /api/manager/contents/{id}`. 4. Duyệt/từ chối bằng `POST /api/manager/reviews`, hoặc yêu cầu sửa bằng `POST /api/manager/reviews/request-changes`. |
| Luồng thay thế | Manager không được tự duyệt nội dung của mình; nội dung đã đổi trạng thái gây xung đột và phải tải lại. |
| Hậu điều kiện | Trạng thái, reviewer, feedback và audit log được cập nhật |

### UC-34: Quản lý pipeline và trạng thái xuất bản

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/manager/content-pipeline`. 2. Lấy nội dung qua `GET /api/manager/published-contents`. 3. Xem chi tiết. 4. Đổi trạng thái publish/unpublish/archive bằng `PUT /api/manager/published-contents/{id}/status`. 5. Khôi phục bản lưu trữ bằng endpoint restore. |
| Ràng buộc | Chỉ nội dung hợp lệ, đã qua duyệt mới được publish; thao tác quan trọng yêu cầu lý do và audit. |

### UC-35: Khôi phục nội dung đã xóa mềm

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/manager/deleted-topics`. 2. Lấy dữ liệu qua `GET /api/manager/deleted-contents`. 3. Lọc theo loại. 4. Khôi phục qua `POST /api/manager/deleted-contents/{type}/{id}/restore`. |
| Hậu điều kiện | Nội dung không còn ở trạng thái xóa mềm; trạng thái xuất bản tuân theo service tương ứng |

### UC-36: Điều phối hỗ trợ và truyền thông

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Tại `/manager/tickets`, Manager xem ticket và danh sách Staff từ `GET /api/staff/members`. 2. Phân công bằng `POST /api/staff/tickets/{id}/assign`. 3. Có thể phản hồi/đóng ticket. 4. Tại `/manager/notifications`, soạn broadcast gửi ngay hoặc hẹn giờ qua `POST /api/staff/notifications`. |
| Hậu điều kiện | Ticket có người phụ trách; broadcast được tạo thành job nền |

---

## 7. ĐẶC TẢ USE CASE NHÓM ADMIN

### UC-37: Xem dashboard và nhật ký kiểm toán

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Tiền điều kiện | Admin đã đăng nhập |
| Luồng chính | 1. Mở `/admin`, lấy tổng quan qua `GET /api/admin/dashboard`. 2. Mở `/admin/reports` để xem lịch sử hành động. 3. Lọc và phân trang audit log qua `GET /api/admin/audit-logs`. |
| Ghi chú | Trang “Reports” hiện tại là **lịch sử hành động/audit log**; chưa có báo cáo học tập, biểu đồ tùy biến hoặc xuất PDF/CSV/Excel. |

### UC-38: Quản lý người dùng

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Tại `/admin/users`, Admin: xem/lọc Student và Staff; xem chi tiết; tạo Staff; sửa Student/Staff; khóa/kích hoạt; reset mật khẩu; xóa mềm/khôi phục; đổi vai trò Staff ↔ StaffManager; xử lý yêu cầu reset mật khẩu Staff và cấp mật khẩu tạm. |
| API chính | `/api/admin/users`, `/api/admin/staff`, `/api/admin/staff/reset-requests` |
| Hậu điều kiện | Thay đổi tài khoản được lưu và audit |

### UC-39: Quản lý cài đặt hệ thống

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | 1. Mở `/admin/settings`. 2. Lấy cấu hình theo nhóm qua `GET /api/admin/settings/{group}`. 3. Sửa một cấu hình hoặc cập nhật atomically cả nhóm qua `PUT`. 4. Kiểm tra SMTP qua `POST /api/admin/settings/smtp/test`. |
| Ràng buộc | Chỉ các nhóm/key được backend cho phép mới được đọc/sửa; dữ liệu được validate theo kiểu cấu hình. |

### UC-40: Quản lý quy tắc thông báo

| Thuộc tính | Đặc tả |
| :--- | :--- |
| Luồng chính | Admin lấy danh sách rule qua `GET /api/admin/notifications/rules`, tạo rule qua `POST`, cập nhật rule theo `ruleKey` qua `PUT /api/admin/notifications/rules/{ruleKey}`. |
| Ghi chú | Code hiện tại có backend API cho rule; chưa có route React Admin riêng trong `App.jsx` để thao tác màn hình này. Broadcast thủ công được thực hiện ở giao diện Manager/Staff. |

---

## 8. CÁC CHỨC NĂNG KHÔNG THUỘC PHẠM VI AS-BUILT

Các nội dung sau từng xuất hiện trong tài liệu cũ nhưng không có luồng triển khai độc lập tương ứng trong code hiện tại:

- Module Reading Practice riêng.
- Module Listening Practice riêng.
- Bookmark tổng quát cho mọi loại nội dung (hiện chỉ có notebook từ vựng/flashcard).
- Staff xem báo cáo phân tích quiz theo từng câu hỏi.
- Admin tạo báo cáo học tập tùy biến và xuất PDF/CSV/Excel.
- Admin gửi thông báo thủ công từ một màn hình Admin riêng.

Không dùng các chức năng trên làm tiêu chí nghiệm thu cho đến khi có route, API và nghiệp vụ backend tương ứng.

---

## 9. MA TRẬN TRUY VẾT TRIỂN KHAI

| Khu vực | Route giao diện chính | API/controller tiêu biểu |
| :--- | :--- | :--- |
| Auth | `/login`, `/register`, `/forgot-password`, `/verify-email` | `/api/auth`, `/api/staff/auth` |
| Student | `/dashboard`, `/courses`, `/kana`, `/vocabulary`, `/grammar`, `/kanji`, `/quiz`, `/mock-test`, `/speaking` | `StudentController`, các controller learning/assessment/speaking |
| Student tiện ích | `/dictionary`, `/notebook`, `/support`, `/notifications`, `/profile`, `/progress` | dictionary, flashcard, support, notification, progress controllers |
| Staff | `/staff/*` | `/api/staff/**` |
| StaffManager | `/manager/*` | `/api/manager/**` và các thao tác điều phối `/api/staff/**` |
| Admin | `/admin`, `/admin/users`, `/admin/settings`, `/admin/reports` | `/api/admin/**` |

---

## 10. KẾT LUẬN

Bản đặc tả này thay thế danh sách use case định hướng trước đây bằng mô tả theo code đang chạy. Phạm vi hiện tại gồm 40 use case, bao phủ xác thực, học tập, quản trị nội dung, hỗ trợ, duyệt xuất bản và quản trị hệ thống. Khi thêm hoặc bỏ route/API nghiệp vụ, tài liệu này phải được cập nhật đồng thời để tiếp tục là nguồn tham chiếu as-built.
