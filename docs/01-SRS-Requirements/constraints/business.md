# Constraints — Business Rules

> **Phạm vi**: Ràng buộc nghiệp vụ (business constraints) cho hệ thống JLPT E-Learning.
> Đây là **luật nghiệp vụ sống còn** — mọi spec, code, test phải tuân thủ; vi phạm = bug nghiêm trọng.
> Liên quan: [`global.md`](./global.md) (ràng buộc kỹ thuật/tech stack), [`safety.md`](./safety.md) (ràng buộc an toàn vận hành), [`shared_context.md`](../shared_context.md).

---

## 1. Authentication & Authorization

| ID | Rule | Rationale |
|----|------|-----------|
| BIZ-AUTH-01 | Mọi API (trừ public: login/register/forgot-password) **phải** đi qua JWT filter | Ngăn truy cập trái phép |
| BIZ-AUTH-02 | Password hash bằng bcrypt, cost **≥ 10** (chuẩn dự án: 12) | Chống brute-force / rainbow table |
| BIZ-AUTH-03 | Phân quyền phải check **cả Role VÀ Subscription/Level** — không chỉ Role | Học viên N5 không được truy cập nội dung N2 dù đã login đúng role STUDENT |
| BIZ-AUTH-04 | UI ẩn nút/menu chỉ là UX — backend **luôn** trả `401/403` khi không đủ quyền | Cấm "Authorization by UI hide" |
| BIZ-AUTH-05 | Không gộp chung UI/logic giữa Staff và Admin | Staff không được thực hiện thao tác cấp Admin (xem LESSON-001) |
| BIZ-AUTH-06 | Subscription hết hạn phải được kiểm tra **real-time**, cache tối đa **5 phút** | Tránh học viên dùng VIP "ảo" sau khi hết hạn |

## 2. Subscription & Monetization

| ID | Rule | Rationale |
|----|------|-----------|
| BIZ-SUB-01 | Nội dung có `is_vip_only = true` chỉ hiển thị/khả dụng khi `user.subscription = VIP` và còn hiệu lực | Bảo vệ doanh thu (revenue leak) |
| BIZ-SUB-02 | Mọi thay đổi subscription (nâng cấp, hạ cấp, gia hạn, hủy) **phải** ghi audit log (ai, khi nào, từ trạng thái gì sang trạng thái gì) | Truy vết tranh chấp thanh toán |
| BIZ-SUB-03 | Không tự động cấp quyền VIP khi chưa xác nhận thanh toán thành công | Chống gian lận / lỗi cấp quyền |
| BIZ-SUB-04 | Hạ cấp/hết hạn subscription không xóa lịch sử học tập đã có | Bảo toàn dữ liệu tiến trình của học viên |

## 3. Điểm số & Bài thi (Quiz / Mock Exam)

| ID | Rule | Rationale |
|----|------|-----------|
| BIZ-EXAM-01 | `score` luôn `>= 0` và `<= max_score` | Toàn vẹn dữ liệu điểm |
| BIZ-EXAM-02 | Điểm số **chỉ được tính ở Service layer (backend)** — client không bao giờ gửi score lên | Frontend là untrusted client |
| BIZ-EXAM-03 | Mỗi lần nộp bài tạo **bản ghi attempt MỚI**, không update đè lên attempt cũ | Giữ lịch sử làm bài đầy đủ |
| BIZ-EXAM-04 | Bài đã nộp (`SUBMITTED`) là **bất biến** — không cho sửa điểm/đáp án sau khi nộp | Tránh gian lận, đảm bảo công bằng |
| BIZ-EXAM-05 | Thời gian làm bài phải validate **server-side**; client không tự khai báo thời gian còn lại | Chống gian lận thời gian |
| BIZ-EXAM-06 | Quiz/Exam đã có ít nhất 1 attempt → câu hỏi bị **lock**; sửa nội dung phải tạo **version mới** | Đảm bảo điểm số nhất quán giữa các học viên (LESSON-005) |
| BIZ-EXAM-07 | Không trộn lẫn câu hỏi/đề thi giữa các cấp độ JLPT (N1–N5) | Sai cấp độ → sai mục tiêu luyện thi |

## 4. Lộ trình học (Learning Path)

| ID | Rule | Rationale |
|----|------|-----------|
| BIZ-PATH-01 | Bài học tiếp theo chỉ mở khóa khi đã hoàn thành bài trước theo `lesson_order` | Đảm bảo lộ trình học có thứ tự |
| BIZ-PATH-02 | `user_progress` chỉ được **tăng**, không giảm thủ công (trừ thao tác Admin có audit log rõ ràng) | Tránh mất công tiến độ của học viên |
| BIZ-PATH-03 | Mọi hoạt động học tập (xem bài, làm flashcard, nộp quiz...) phải ghi vào `learning_activity_log` | Phục vụ thống kê & phân tích tiến độ (UC-19, UC-38) |

## 5. Quy trình Nội dung (Content Workflow)

| ID | Rule | Rationale |
|----|------|-----------|
| BIZ-CONTENT-01 | Nội dung do **Staff** tạo phải qua bước **Review** của **StaffManager** trước khi publish (UC-33, UC-34) | Kiểm soát chất lượng nội dung trước khi đến học viên |
| BIZ-CONTENT-02 | Nội dung đang ở trạng thái `PENDING_REVIEW`/`DRAFT` không hiển thị cho Student | Tránh học viên thấy nội dung chưa kiểm duyệt |
| BIZ-CONTENT-03 | CRUD nội dung học tập (Course/Lesson/Quiz/Question) chỉ Staff/Admin được thực hiện | Student không có quyền chỉnh sửa nội dung |

## 6. AI Features (OCR & Speech Recognition)

| ID | Rule | Rationale |
|----|------|-----------|
| BIZ-AI-01 | OCR chỉ trả về **similarity %** so với ký tự chuẩn, không phân tích stroke order (ADR-007) | Giảm complexity, tập trung core value |
| BIZ-AI-02 | Kết quả AI là `ai_score_suggestion` — **Staff có quyền override** bằng `final_score` (UC-31) | AI có thể sai, con người là quyết định cuối |
| BIZ-AI-03 | Gọi AI **không bao giờ** silent fail: phải có timeout + retry (tối đa 3 lần) + fallback response + log đầy đủ | Học viên phải luôn nhận được phản hồi, dù AI lỗi (LESSON-006) |
| BIZ-AI-04 | Mọi tác vụ AI chạy **bất đồng bộ** — trả `job_id` ngay, học viên poll kết quả sau | UX không bị block khi AI xử lý lâu |
| BIZ-AI-05 | File ảnh/audio đầu vào AI lưu tại `/uploads` hoặc S3 — **không** lưu BLOB trong DB (ADR-006) | Tránh phình DB, backup chậm (LESSON-002) |

## 7. Dữ liệu & Audit

| ID | Rule | Rationale |
|----|------|-----------|
| BIZ-DATA-01 | Toàn hệ thống dùng **Soft Delete** (`is_deleted = true` hoặc `status = INACTIVE`) — cấm `DELETE FROM` (ADR-004) | Phục hồi dữ liệu, truy vết lịch sử |
| BIZ-DATA-02 | Mọi bảng nghiệp vụ quan trọng phải có `created_at`, `updated_at`, `created_by` | Truy vết thay đổi |
| BIZ-DATA-03 | Thao tác quan trọng của Admin/Staff (xóa, khóa tài khoản, đổi subscription, duyệt nội dung) phải có audit log | Trách nhiệm giải trình (accountability) |
| BIZ-DATA-04 | Entity (JPA) không được trả trực tiếp ra API — luôn qua DTO (ADR-005) | Tránh information leakage |

## 8. Hỗ trợ & Thông báo

| ID | Rule | Rationale |
|----|------|-----------|
| BIZ-SUPPORT-01 | Yêu cầu hỗ trợ từ Student phải được Staff phản hồi và có trạng thái theo dõi (OPEN/IN_PROGRESS/CLOSED) (UC-29) | Đảm bảo SLA hỗ trợ học viên |
| BIZ-SUPPORT-02 | Thông báo hệ thống gửi theo **rule** do Admin cấu hình (UC-40), không gửi tùy ý từ code | Kiểm soát spam, đúng đối tượng nhận |

---

## Tham chiếu

| Nguồn | Nội dung liên quan |
|-------|---------------------|
| `CLAUDE.md` | ADR-001 → ADR-008, Lessons Learned, Anti-patterns |
| `AGENTS.md § 7` | JLPT Domain Rules đầy đủ (nguồn gốc của bảng trên) |
| `docs/01-SRS-Requirements/use-cases/Bao_cao_dac_ta_Use_Case.md` | UC-01 → UC-40 |
| `constraints/global.md` | Tech stack, naming convention |
| `constraints/safety.md` | Ràng buộc an toàn vận hành (vd. cấm xóa DB production) |
