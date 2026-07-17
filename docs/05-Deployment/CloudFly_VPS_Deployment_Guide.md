# Hướng Dẫn Triển Khai Hệ Thống Lên CloudFly VPS (Docker Compose)

Dự án JLPT Learning Platform hiện đang được cấu trúc để triển khai dạng "nguyên khối" (Monolithic-style) lên một Cloud Server (VPS) thông qua Docker Compose.

Dựa trên cấu hình hiện tại (Máy chủ CloudFly - Ubuntu 22.04), tài liệu này sẽ hướng dẫn cách thức vận hành CI/CD tự động, cũng như thiết lập các biến môi trường quan trọng như **Google OAuth2** và **Email SMTP**.

---

## 1. Yêu Cầu Máy Chủ (VPS)

- **Nhà cung cấp:** Bất kỳ (hiện tại là CloudFly).
- **Hệ điều hành:** Ubuntu 22.04 LTS (64bit)
- **IP Public:** `222.255.181.207` (Ví dụ hiện hành)
- **Cấu hình tối thiểu:** 1 CPU, 2GB RAM, 40GB SSD.
- **Mở port (Firewall):** Cần đảm bảo mở port `80` (HTTP), `443` (HTTPS) và `22` (SSH).

---

## 2. Thiết Lập Biến Môi Trường (.env) Trên VPS

Hệ thống sẽ **KHÔNG THỂ CHẠY ĐƯỢC** tính năng Gửi Mail hoặc Đăng Nhập Google nếu bạn không tạo file `.env` chứa các "Bí mật" (Secrets) trên máy chủ VPS.

### Bước 2.1: Truy cập vào VPS
Hãy dùng phần mềm Terminal (hoặc MobaXTerm/PuTTY) kết nối SSH vào máy chủ:
```bash
ssh root@222.255.181.207
```

### Bước 2.2: Tạo file `.env`
Di chuyển vào thư mục dự án trên VPS (VD: `/opt/Japanese-Skill-Practice-Platform`), tạo file `.env` nằm ngang hàng với `docker-compose.yml`:

```bash
cd /opt/Japanese-Skill-Practice-Platform
nano .env
```

Dán nội dung sau và điền giá trị thật của bạn:
```env
# 1. CẤU HÌNH DATABASE
MYSQL_ROOT_PASSWORD=MatKhauRootSieuManh_123!
MYSQL_USER=jlpt
MYSQL_PASSWORD=MatKhauDbJLPT_123!

# 2. BẢO MẬT TOKEN
JWT_SECRET=MotChuoiKhoaBiMatRatDaiVaKhoDoan_Cho_HS256

# 3. GOOGLE OAUTH2 (Đăng nhập Google)
# Lấy tại: https://console.cloud.google.com/apis/credentials
GOOGLE_CLIENT_ID=xxxxx.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-yyyyy

# 4. CẤU HÌNH GỬI EMAIL SMTP (VD: Resend, Gmail)
SMTP_HOST=smtp.gmail.com   # (hoặc smtp.resend.com)
SMTP_PORT=587              # (hoặc 465)
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
```
Lưu lại file bằng cách nhấn `Ctrl + X` -> `Y` -> `Enter`.

---

## 3. Quản Lý CI/CD (Staging, Production, Rollback)

Dự án sở hữu một kiến trúc DevOps cực kỳ chặt chẽ với các môi trường độc lập. Bạn cần khai báo các Secrets trên bảng điều khiển Repository Github (Settings -> Secrets and Variables -> Actions) để Github có thể "vào" VPS chạy lệnh:
- `VPS_HOST`: `222.255.181.207`
- `VPS_USERNAME`: `root`
- `VPS_PASSWORD`: `Mật khẩu VPS`
- `PROJECT_PATH`: `/opt/Japanese-Skill-Practice-Platform`

### 3.1. Luồng Staging (Kiểm Thử Trước Khi Lên Sàn)
- Dành cho nhánh `develop`. Khi bạn đẩy code lên `develop`, file `.github/workflows/cd-staging.yml` kích hoạt.
- Hệ thống tạo ra một bộ ứng dụng ảo thứ 2 (cùng ở trên VPS `222.255.181.207` đó), chạy cổng `8082` (Frontend) và DB phụ.
- Đội Tester (hoặc bạn) vào nhánh này trải nghiệm tính năng. Nếu OK, Merge code sang nhánh `branch_for_hung`.

### 3.2. Luồng Production (Phát Hành Thực Tế)
- Dành cho nhánh `branch_for_hung`.
- Code được kéo về thư mục Production, build lại và đẩy ra web cho người dùng thật dùng. Kịch bản này có bước **Smoke Test** an toàn (App sập sẽ dừng cập nhật ngay lập tức để không ảnh hưởng khách hàng).

### 3.3. Tính Năng Rollback Khẩn Cấp
- Nếu bản Production gặp lỗi nghiệm trọng sau khi cập nhật, truy cập vào trang Repo Github -> `Actions` -> Chọn `Rollback Production`.
- Nhập mã `SHA` của Commit chạy tốt trước đó.
- VPS sẽ đổi File cấu trúc Image về phiên bản cũ ngay lập tức (không tốn thời gian Build lại).

### 3.4. Cảnh Báo Uptime Trực Tuyến
File `uptime-check.yml` được cấu hình để gửi gói tin lên Website của bạn mỗi 10 phút. Ngay khi Website sập, Github sẽ báo lỗi đỏ, giúp bạn phản ứng kịp thời trước khi người dùng phàn nàn.
