# Constraints — Safety (Operational)

> **Phạm vi**: Ràng buộc an toàn vận hành cho hệ thống JLPT E-Learning.
> Các quy tắc này bảo vệ hệ thống, dữ liệu và người dùng khỏi các thao tác
> nguy hiểm, sự cố không thể phục hồi, và hành vi không xác định.
> Liên quan: [`global.md`](./global.md) (ràng buộc kỹ thuật/tech stack), [`business.md`](./business.md) (ràng buộc nghiệp vụ), [`shared_context.md`](../shared_context.md).

---

## 1. Bảo toàn Dữ liệu (Data Safety)

| ID | Rule | Mức độ | Rationale |
|----|------|--------|-----------|
| SAFE-DATA-01 | **TUYỆT ĐỐI KHÔNG** chạy `DELETE FROM` trực tiếp trên DB production — chỉ Soft Delete | 🔴 CRITICAL | Dữ liệu xóa vật lý không thể phục hồi |
| SAFE-DATA-02 | **TUYỆT ĐỐI KHÔNG** chạy `UPDATE` hàng loạt (bulk UPDATE không có `WHERE`) trên production | 🔴 CRITICAL | Có thể ghi đè toàn bộ bảng |
| SAFE-DATA-03 | **TUYỆT ĐỐI KHÔNG** `TRUNCATE TABLE` trên bất kỳ bảng nào đang có dữ liệu | 🔴 CRITICAL | Không thể rollback |
| SAFE-DATA-04 | Mọi schema change phải có Flyway migration — **cấm** `DDL` thủ công trực tiếp lên DB | 🔴 CRITICAL | Không reproducible, mất sync giữa code và DB |
| SAFE-DATA-05 | Bản ghi `quiz_attempt` đã ở trạng thái `SUBMITTED` là **bất biến** — không được UPDATE | 🔴 CRITICAL | Đảm bảo tính công bằng và truy vết điểm số |
| SAFE-DATA-06 | `user_progress` chỉ được tăng, không giảm thủ công (trừ Admin + audit log rõ ràng) | 🟠 HIGH | Tránh mất công tiến độ của học viên |
| SAFE-DATA-07 | Backup DB trước mọi migration quan trọng ở môi trường production | 🟠 HIGH | Phòng ngừa lỗi migration không rollback được |

---

## 2. An toàn Triển khai (Deployment Safety)

| ID | Rule | Mức độ | Rationale |
|----|------|--------|-----------|
| SAFE-DEPLOY-01 | **TUYỆT ĐỐI KHÔNG** commit trực tiếp vào branch `main` hoặc `production` | 🔴 CRITICAL | Mọi change phải qua Pull Request + Review |
| SAFE-DEPLOY-02 | **TUYỆT ĐỐI KHÔNG** đặt `spring.jpa.hibernate.ddl-auto=create` hoặc `update` ở production | 🔴 CRITICAL | Hibernate auto-DDL có thể drop/alter table không kiểm soát |
| SAFE-DEPLOY-03 | **TUYỆT ĐỐI KHÔNG** đọc hoặc thay đổi file `.env`, `application-prod.yml`, secrets trong source control | 🔴 CRITICAL | Lộ credentials production |
| SAFE-DEPLOY-04 | Thay đổi > 3 files hoặc > 200 dòng → **phải backup hoặc xác nhận** trước khi thực hiện | 🟠 HIGH | Giảm rủi ro refactor lớn |
| SAFE-DEPLOY-05 | Không deploy lên production trong giờ cao điểm (8:00–22:00) trừ hotfix khẩn cấp | 🟡 MEDIUM | Giảm ảnh hưởng đến người dùng đang học |
| SAFE-DEPLOY-06 | Mọi environment variable nhạy cảm phải qua Docker secret hoặc `.env` (không vào image) | 🟠 HIGH | Tránh lộ credentials trong Docker layer |

---

## 3. An toàn Xác thực & Phân quyền (Auth Safety)

| ID | Rule | Mức độ | Rationale |
|----|------|--------|-----------|
| SAFE-AUTH-01 | **TUYỆT ĐỐI KHÔNG** bypass JWT filter hoặc Spring Security config | 🔴 CRITICAL | Mọi API đều phải được bảo vệ |
| SAFE-AUTH-02 | **TUYỆT ĐỐI KHÔNG** tin tưởng dữ liệu từ client mà không validate lại ở backend | 🔴 CRITICAL | Frontend là untrusted client |
| SAFE-AUTH-03 | **TUYỆT ĐỐI KHÔNG** cấp quyền VIP khi chưa xác nhận thanh toán thành công | 🔴 CRITICAL | Chống gian lận / revenue leak |
| SAFE-AUTH-04 | **TUYỆT ĐỐI KHÔNG** lưu JWT token hoặc password dưới dạng plain text | 🔴 CRITICAL | Bảo mật thông tin xác thực |
| SAFE-AUTH-05 | **TUYỆT ĐỐI KHÔNG** gộp chung logic/UI của Admin và Staff | 🟠 HIGH | Staff không được thực hiện thao tác Admin |
| SAFE-AUTH-06 | Kiểm tra subscription hết hạn phải **real-time**, không chỉ dựa vào token payload | 🟠 HIGH | Token hợp lệ nhưng subscription có thể đã hết hạn |

---

## 4. An toàn AI Features (AI Safety)

| ID | Rule | Mức độ | Rationale |
|----|------|--------|-----------|
| SAFE-AI-01 | **TUYỆT ĐỐI KHÔNG** để AI call **silent fail** — phải có timeout + retry + fallback + log | 🔴 CRITICAL | Học viên phải luôn nhận được phản hồi (LESSON-006) |
| SAFE-AI-02 | AI timeout: **30 giây** per attempt; retry tối đa **3 lần** | 🟠 HIGH | Tránh treo request vô thời hạn |
| SAFE-AI-03 | Kết quả AI (`ai_score_suggestion`) phải validate trước khi lưu DB | 🟠 HIGH | AI có thể trả kết quả bất hợp lệ |
| SAFE-AI-04 | File ảnh/audio gửi lên AI phải kiểm tra kích thước và định dạng trước khi xử lý | 🟡 MEDIUM | Tránh DOS bằng file quá lớn |
| SAFE-AI-05 | Mọi AI job phải lưu trạng thái (`PENDING/PROCESSING/DONE/FAILED`) — không "fire and forget" | 🟠 HIGH | Học viên cần poll được kết quả |
| SAFE-AI-06 | Lỗi AI phải log đầy đủ: `jobId`, `userId`, `errorType`, `attemptCount`, `timestamp` | 🟡 MEDIUM | Phục vụ debug và monitoring |

---

## 5. An toàn File & Storage (File Safety)

| ID | Rule | Mức độ | Rationale |
|----|------|--------|-----------|
| SAFE-FILE-01 | **TUYỆT ĐỐI KHÔNG** lưu file media (ảnh/audio) dưới dạng BLOB trong DB (ADR-006) | 🔴 CRITICAL | DB phình, backup chậm (LESSON-002) |
| SAFE-FILE-02 | Validate file type và file size trước khi accept upload | 🟠 HIGH | Chống upload file độc hại hoặc quá lớn |
| SAFE-FILE-03 | File upload phải lưu tại volume cố định (`jlpt-uploads`) — không lưu trong container layer | 🟠 HIGH | Tránh mất file khi restart container |
| SAFE-FILE-04 | Chạy cleanup job định kỳ để xóa orphan files (file không còn DB reference) | 🟡 MEDIUM | Tránh storage leak |
| SAFE-FILE-05 | Tên file lưu trữ phải được sanitize (không chứa path traversal như `../`) | 🟠 HIGH | Bảo mật file system |

---

## 6. An toàn Vận hành (Operational Safety)

| ID | Rule | Mức độ | Rationale |
|----|------|--------|-----------|
| SAFE-OPS-01 | **TUYỆT ĐỐI KHÔNG** hardcode secret, API key, password trong source code | 🔴 CRITICAL | Lộ credentials khi code bị leak |
| SAFE-OPS-02 | **TUYỆT ĐỐI KHÔNG** log sensitive data: password, JWT token, thông tin cá nhân (PII) | 🔴 CRITICAL | Vi phạm bảo mật dữ liệu người dùng |
| SAFE-OPS-03 | Thao tác quan trọng của Admin/Staff phải có **audit log** đầy đủ (ai, khi nào, hành động gì) | 🟠 HIGH | Truy vết trách nhiệm |
| SAFE-OPS-04 | Hệ thống phải trả response có nghĩa khi AI/external service bị lỗi — không trả empty hoặc 500 không giải thích | 🟠 HIGH | UX cho học viên |
| SAFE-OPS-05 | Subscription change (nâng cấp, hạ cấp, gia hạn, hủy) phải ghi audit log | 🟠 HIGH | Truy vết tranh chấp thanh toán |
| SAFE-OPS-06 | Khi không thể chạy công cụ phân tích / không đủ thông tin → **nêu rõ giới hạn** thay vì đoán mò | 🟡 MEDIUM | Tránh implement sai spec |

---

## 7. Ngưỡng Cảnh báo & Xử lý Sự cố

| Tình huống | Hành động bắt buộc |
|------------|-------------------|
| Schema change cần rollback ở production | Chạy Flyway repair → rollback migration → restore backup nếu cần |
| AI service down hoàn toàn | Trả fallback response với message rõ ràng; log incident; không treo request |
| Phát hiện data inconsistency (điểm âm, progress sai) | **Không sửa trực tiếp DB** — mở issue, phân tích nguyên nhân, viết migration có audit |
| Subscription bị cấp sai (free → VIP không qua payment) | Audit log ngay; rollback thủ công qua Admin UI; báo cáo |
| Phát hiện secret bị lộ trong code | Rotate key ngay lập tức; force-push rewrite history nếu cần; thông báo team |
| UC spec không rõ ràng hoặc business rule mâu thuẫn | **Dừng implement, hỏi người phụ trách** — không tự đoán |

---

## Tham chiếu

| Nguồn | Nội dung liên quan |
|-------|---------------------|
| `CLAUDE.md § LESSONS LEARNED` | LESSON-001 → LESSON-006 (nguồn gốc của các SAFE rules) |
| `AGENTS.md § 4` | Phạm vi hoạt động — cấm tuyệt đối |
| `AGENTS.md § 5` | Forbidden Patterns |
| `AGENTS.md § 9` | Xử lý lỗi & an toàn thao tác |
| `constraints/global.md` | Ràng buộc kỹ thuật (tech stack, naming, hiệu năng) |
| `constraints/business.md` | Ràng buộc nghiệp vụ (điểm số, lộ trình, subscription) |
