# Kịch Bản & Bộ Câu Hỏi Bảo Vệ Đồ Án Trước Hội Đồng
**Dự án:** Hệ Thống Học Tiếng Nhật JLPT (SakuJi)
**Công nghệ:** Java 21, Spring Boot 3.x, React 18, SQL Server, AI (OCR, Speech Recognition)

Tài liệu này tổng hợp kịch bản thuyết trình ngắn gọn và toàn bộ các câu hỏi "xoáy" mà hội đồng có thể đặt ra, kèm theo gợi ý trả lời chuyên sâu dựa trên kiến trúc và luật nghiệp vụ của dự án.

---

## PHẦN 1: KỊCH BẢN THUYẾT TRÌNH (ELEVATOR PITCH - 3 PHÚT)

**1. Mở đầu (Hook):**
"Kính chào Hội đồng. Hiện nay, việc học tiếng Nhật JLPT đang gặp khó khăn trong việc kết hợp giữa lộ trình bài bản và luyện tập thực hành thông minh. Vì vậy, nhóm chúng em đã xây dựng Hệ thống Học Tiếng Nhật JLPT SakuJi - một nền tảng E-Learning toàn diện từ N5 đến N1."

**2. Điểm nổi bật (Core Value):**
"Hệ thống không chỉ cung cấp bài học lý thuyết mà còn tích hợp các tính năng nâng cao: Flashcard sử dụng thuật toán Lặp lại ngắt quãng (Spaced Repetition), thi thử với cấu trúc y hệt JLPT thực tế, và đặc biệt là tích hợp AI để nhận diện chữ viết Kanji (OCR) và luyện phát âm (Speech Recognition)."

**3. Kiến trúc kỹ thuật (Tech Stack):**
"Về mặt kỹ thuật, hệ thống sử dụng kiến trúc Client-Server. Backend xây dựng bằng Java 21, Spring Boot 3 theo mô hình Feature-based chuyên biệt, kết nối cơ sở dữ liệu SQL Server. Frontend sử dụng React 18 với giao diện thiết kế chuyên biệt cho trải nghiệm người dùng học ngoại ngữ. Hệ thống được bảo mật chặt chẽ bằng JWT và phân quyền phức tạp giữa Học viên, Nhân viên (Staff/Manager), và Quản trị viên (Admin)."

---

## PHẦN 2: NGÂN HÀNG CÂU HỎI TỪ HỘI ĐỒNG (Q&A)

### 2.1. Nhóm Câu Hỏi Về Kiến Trúc & Công Nghệ (Architecture & Tech Stack)

> [!CAUTION]
> Hội đồng thường hỏi xoáy vào việc LÝ DO chọn công nghệ và cách tổ chức code.

**Q1: Tại sao em lại chọn mô hình Feature-based cho Backend thay vì Layer-based truyền thống (Controller, Service, Repository riêng)?**
* **Trả lời:** Ban đầu dự án dùng Layer-based, nhưng khi dự án phình to (với nhiều tính năng như Flashcard, Mock Test, OCR), mô hình Layer khiến code bị phân mảnh, sửa 1 tính năng phải nhảy qua nhiều thư mục. Chúng em quyết định refactor sang Feature-based (nhóm theo tính năng ví dụ `com.jlpt.feature.quiz`) để tăng tính đóng gói (cohesion), dễ scale và dễ maintain hơn. Các module ít bị phụ thuộc chéo.

**Q2: Việc giao tiếp giữa Frontend và Backend thực hiện qua đâu? Em xử lý an toàn dữ liệu như thế nào?**
* **Trả lời:** Giao tiếp qua RESTful APIs trả về JSON. Để an toàn, chúng em áp dụng nghiêm ngặt **DTO Pattern** (Data Transfer Object). Entity của JPA không bao giờ được trả trực tiếp ra API để tránh rò rỉ cấu trúc DB và lỗi vòng lặp (Circular Reference). Dữ liệu gửi lên cũng được Validate nghiêm ngặt qua Jakarta Annotations ở tầng DTO trước khi vào Controller.

**Q3: Tại sao lại chọn SQL Server mà không phải MySQL hay PostgreSQL?**
* **Trả lời:** SQL Server mạnh mẽ trong việc xử lý Transaction và toàn vẹn dữ liệu, đồng thời có cơ chế T-SQL hỗ trợ tốt cho các query phức tạp như tính toán lộ trình học, điểm số và log hoạt động. Hệ thống dùng Flyway để tự động hóa việc quản lý schema (Migration).

### 2.2. Nhóm Câu Hỏi Về Nghiệp Vụ Chuyên Sâu (Domain Business Logic)

> [!IMPORTANT]
> Đây là phần để chứng minh bạn làm đồ án thật, hiểu rõ logic chứ không phải copy code.

**Q4: Hệ thống tính điểm và lưu kết quả bài thi như thế nào? Nếu mạng lag, sinh viên ấn nộp bài 2 lần thì sao?**
* **Trả lời:** Việc tính điểm được thực hiện **100% ở Backend**, Frontend chỉ gửi đáp án lên. Điều này ngăn chặn việc hack điểm ở client.
Để tránh lỗi submit nhiều lần, mỗi lần nộp bài hệ thống tạo một bản ghi MỚI hoàn toàn. Các bài thi đã làm (`is_locked = true`) là bất biến (Immutable), không được phép dùng lệnh UPDATE vào điểm số đã nộp để đảm bảo tính toàn vẹn (Invariant Rule).

**Q5: Quản lý quyền truy cập bài học (Lộ trình N5-N1) như thế nào? Làm sao ngăn sinh viên N5 xem trộm bài N1?**
* **Trả lời:** Frontend có chặn giao diện, nhưng chốt chặn chính nằm ở Backend. Chúng em áp dụng kiểm tra Authorization check **kép**: Kiểm tra Role VÀ Kiểm tra Subscription/Cấp độ. Nếu bài học đánh dấu `is_vip_only` hoặc cấp độ chưa mở khóa, Backend sẽ quăng Exception `403 Forbidden`. Dữ liệu Role được bọc an toàn trong JWT.

**Q6: Em xử lý việc Soft Delete (xóa mềm) như thế nào? Tại sao không xóa thật?**
* **Trả lời:** Trong hệ thống E-Learning, dữ liệu thi, log học tập liên kết chặt chẽ với nhau. Nếu xóa thật (Hard delete) User hay Course sẽ gây lỗi khóa ngoại (Foreign key) hoặc mất dữ liệu thống kê. Chúng em dùng Soft Delete (cờ `is_deleted = true` hoặc `status = INACTIVE`).

### 2.3. Nhóm Câu Hỏi Về Trí Tuệ Nhân Tạo (AI Features)

> [!WARNING]
> Hội đồng rất thích hỏi phần AI để xem đây là AI "xịn" hay chỉ gọi API cơ bản.

**Q7: Tính năng AI chấm điểm Kanji (OCR) của em hoạt động ra sao? Nó có đồng bộ không?**
* **Trả lời:** Vì gọi AI model tốn thời gian, nếu để đồng bộ (Sync) sẽ làm treo ứng dụng và time-out. Chúng em thiết kế theo mô hình **Async (Bất đồng bộ)**. Khi Frontend gửi ảnh, Backend lưu vào lưu trữ, đẩy job vào Queue và trả về `job_id` ngay lập tức. Frontend sẽ polling (gọi API liên tục) với `job_id` để lấy kết quả. 

**Q8: Nếu API của AI bị sập thì hệ thống em xử lý ra sao?**
* **Trả lời:** Chúng em thiết kế **Fallback & Retry**. Nếu gọi AI thất bại, Backend sẽ tự động retry tối đa 3 lần. Nếu vẫn lỗi, hệ thống sẽ log lỗi và trả về Fallback Response để luồng ứng dụng của người dùng không bị crash, đồng thời báo cho Staff để chấm điểm thủ công (override AI score).

### 2.4. Nhóm Câu Hỏi Về Bảo Mật & Tối Ưu Hiệu Năng (Security & Performance)

> [!TIP]
> Câu hỏi phân loại sinh viên Khá và Giỏi.

**Q9: Ứng dụng em bảo mật như thế nào ngoài việc dùng Mật khẩu?**
* **Trả lời:** 
  1. Mật khẩu được băm (hashing) không thể dịch ngược.
  2. Stateless Authentication bằng JWT, có thời hạn (Expiration).
  3. Mọi thao tác ghi/sửa/xóa của Admin/Staff đều được lưu vào Audit Log (ai làm gì, vào lúc nào).
  4. Không lưu thông tin Secret Key, DB Credentials trong Source Code (sử dụng Environment Variables).

**Q10: Nếu ứng dụng có 100,000 học viên cùng làm bài thi một lúc, em nghĩ hệ thống sẽ nghẽn ở đâu và cách giải quyết?**
* **Trả lời:** Hệ thống sẽ nghẽn ở Database do ghi quá nhiều vào bảng `quiz_attempts` và `learning_activity_log`. 
Cách giải quyết: (1) Sử dụng Connection Pooling cho Database. (2) Có thể dùng Redis để cache đề thi vì đề thi hiếm khi thay đổi. (3) Tách việc ghi Log thành một luồng Async (Message Queue) để không làm chậm response nộp bài của sinh viên.

---

## LỜI KHUYÊN KHI TRẢ LỜI HỘI ĐỒNG
1. **Thành thật:** Nếu phần nào chưa làm được hoặc làm chưa sâu, hãy thừa nhận và coi đó là "Hướng phát triển tương lai" (Future works). Hội đồng đánh giá cao thái độ hơn là việc nói quá sự thật.
2. **Luôn lái về Backend:** Dù hội đồng hỏi về giao diện (React), hãy giải thích thêm về cách Backend bắt validate để chứng tỏ sự chặt chẽ của Full-stack.
3. **Mở DB hoặc Code:** Đừng chỉ nói suông. Khi hội đồng hỏi "Em làm DTO thế nào?", hãy mở ngay code class `QuizAttemptDTO` và `GlobalExceptionHandler` để chứng minh.
