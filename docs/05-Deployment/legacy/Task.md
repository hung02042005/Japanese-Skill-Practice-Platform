# Deployment Task Tracker — JLPT E-Learning Platform v2.0

> **Loại tài liệu**: Danh sách công việc triển khai
> **Phiên bản**: 2.0.0
> **Cập nhật**: 2026-06-23
> **Tham chiếu**: [Plan.md](Plan.md) | [Spec.md](Spec.md) | [README.md](README.md)

---

## Trạng Thái Ký Hiệu

| Ký hiệu | Trạng thái |
|---------|-----------|
| `[ ]` | Chưa làm |
| `[~]` | Đang làm |
| `[x]` | Hoàn thành |
| `[!]` | Bị block / cần xử lý |

---

## Phase 0 — Chuẩn Bị Trước Deploy

### 0.1 Kiểm Tra Code

```
[ ] 0.1.1  Chạy mvn test — tất cả pass, coverage ≥ 80%
           Lệnh: cd apps/backend && mvn test
           Kết quả: ___________________________________

[ ] 0.1.2  Chạy npm run test — tất cả pass
           Lệnh: cd apps/frontend && npm run test
           Kết quả: ___________________________________

[ ] 0.1.3  Chạy mvn spotless:apply — không còn lỗi style
           Lệnh: cd apps/backend && mvn spotless:apply
           Kết quả: ___________________________________

[ ] 0.1.4  Chạy npm run lint — không có lỗi ESLint
           Lệnh: cd apps/frontend && npm run lint
           Kết quả: ___________________________________

[ ] 0.1.5  Build thử backend JAR thành công
           Lệnh: cd apps/backend && mvn clean package -DskipTests
           Output: apps/backend/target/backend-2.0.0.jar ___MB

[ ] 0.1.6  Build thử frontend thành công
           Lệnh: cd apps/frontend && npm run build
           Output: apps/frontend/dist/ ___MB
```

### 0.2 Chuẩn Bị Credentials

```
[ ] 0.2.1  Sinh JWT_SECRET (≥ 64 ký tự hex)
           Lệnh: openssl rand -hex 32
           Lưu vào: apps/backend/.env

[ ] 0.2.2  Đăng ký Redirect URI production trong Google Cloud Console
           Console: console.cloud.google.com → Credentials → OAuth 2.0
           URI cần thêm: https://yourdomain.com/auth/callback

[ ] 0.2.3  Tạo Gmail App Password cho SMTP
           Bước: Google Account → Security → 2-Step Verification → App Passwords
           Lưu App Password vào: apps/backend/.env (SMTP_PASSWORD)

[ ] 0.2.4  Trỏ DNS A record về IP server
           Domain: yourdomain.com → <server-ip>
           Kiểm tra: nslookup yourdomain.com
           Propagate time: ≤ 24h

[ ] 0.2.5  Điền đầy đủ file apps/backend/.env.production
           Checklist bên trong:
             [ ] DATABASE_URL
             [ ] DATABASE_USERNAME
             [ ] DATABASE_PASSWORD
             [ ] JWT_SECRET
             [ ] GOOGLE_CLIENT_ID
             [ ] GOOGLE_CLIENT_SECRET
             [ ] SMTP_USERNAME
             [ ] SMTP_PASSWORD

[ ] 0.2.6  Điền đầy đủ file apps/frontend/.env.production
             [ ] VITE_API_BASE_URL=https://api.yourdomain.com/api
             [ ] VITE_GOOGLE_CLIENT_ID
```

### 0.3 Git Release

```
[ ] 0.3.1  Merge tất cả feature branch vào main
           Lệnh: git checkout main && git merge <feature-branch>

[ ] 0.3.2  Tạo tag release
           Lệnh: git tag v2.0.0 && git push origin v2.0.0

[ ] 0.3.3  Tạo nhánh release
           Lệnh: git checkout -b release/v2.0.0
```

---

## Phase 1 — Chuẩn Bị Server

### 1.1 Kết Nối & Cập Nhật

```
[ ] 1.1.1  Kết nối SSH vào server
           Lệnh: ssh user@<server-ip>

[ ] 1.1.2  Update & upgrade hệ thống
           Lệnh: sudo apt update && sudo apt upgrade -y
```

### 1.2 Cài Đặt Java 21

```
[ ] 1.2.1  Thêm Adoptium repository
[ ] 1.2.2  Cài temurin-21-jre
           Lệnh: sudo apt install -y temurin-21-jre
[ ] 1.2.3  Xác nhận phiên bản
           Lệnh: java -version
           Mong đợi: openjdk version "21"
```

### 1.3 Cài Đặt Nginx

```
[ ] 1.3.1  Cài Nginx
           Lệnh: sudo apt install -y nginx
[ ] 1.3.2  Enable & start
           Lệnh: sudo systemctl enable nginx && sudo systemctl start nginx
[ ] 1.3.3  Xác nhận chạy
           Lệnh: sudo systemctl status nginx
```

### 1.4 Cài Đặt SQL Server 2022

```
[ ] 1.4.1  Thêm Microsoft repository
[ ] 1.4.2  Cài mssql-server
           Lệnh: sudo apt install -y mssql-server
[ ] 1.4.3  Cấu hình SQL Server (chọn Express, đặt SA password)
           Lệnh: sudo /opt/mssql/bin/mssql-conf setup
[ ] 1.4.4  Cài sqlcmd tools
           Lệnh: sudo ACCEPT_EULA=Y apt install -y mssql-tools18
[ ] 1.4.5  Enable & start SQL Server
           Lệnh: sudo systemctl enable mssql-server && sudo systemctl start mssql-server
[ ] 1.4.6  Kiểm tra SQL Server hoạt động
           Lệnh: sqlcmd -S localhost -U sa -P '<password>' -Q "SELECT @@VERSION"
```

### 1.5 Tạo User & Thư Mục

```
[ ] 1.5.1  Tạo system user jlpt
           Lệnh: sudo useradd -r -s /bin/false jlpt
[ ] 1.5.2  Tạo thư mục ứng dụng
           Lệnh: sudo mkdir -p /opt/jlpt/uploads /var/log/jlpt /var/www/jlpt/dist
[ ] 1.5.3  Phân quyền thư mục
           Lệnh: sudo chown -R jlpt:jlpt /opt/jlpt /var/log/jlpt
```

### 1.6 Cài Certbot

```
[ ] 1.6.1  Cài certbot
           Lệnh: sudo apt install -y certbot python3-certbot-nginx
```

---

## Phase 2 — Cài Đặt Database

### 2.1 Tạo Database & User

```
[ ] 2.1.1  Kết nối SQL Server với tài khoản sa
[ ] 2.1.2  Tạo database JLPT_LearningDB
           SQL: CREATE DATABASE JLPT_LearningDB;
[ ] 2.1.3  Tạo user jlpt_user (không dùng sa)
           SQL: CREATE LOGIN jlpt_user WITH PASSWORD = '<password>';
[ ] 2.1.4  Cấp quyền tối thiểu cho jlpt_user
           SQL: ALTER ROLE db_datareader ADD MEMBER jlpt_user;
                ALTER ROLE db_datawriter ADD MEMBER jlpt_user;
                ALTER ROLE db_ddladmin ADD MEMBER jlpt_user;
```

### 2.2 Kiểm Tra

```
[ ] 2.2.1  Test kết nối với jlpt_user
           Lệnh: sqlcmd -S localhost -U jlpt_user -P '<password>' \
                   -Q "SELECT name FROM sys.databases WHERE name = 'JLPT_LearningDB'"
           Mong đợi: trả về JLPT_LearningDB
```

---

## Phase 3 — Deploy Ứng Dụng

### 3.1 Build & Upload

```
[ ] 3.1.1  Build backend JAR (production)
           Lệnh: cd apps/backend && mvn clean package -DskipTests

[ ] 3.1.2  Build frontend (production)
           Lệnh: cd apps/frontend && npm run build

[ ] 3.1.3  Upload JAR lên server
           Lệnh: scp apps/backend/target/backend-2.0.0.jar user@<ip>:/opt/jlpt/

[ ] 3.1.4  Upload file .env lên server
           Lệnh: scp apps/backend/.env.production user@<ip>:/opt/jlpt/.env
           Đặt quyền: chmod 600 /opt/jlpt/.env

[ ] 3.1.5  Upload frontend dist lên server
           Lệnh: rsync -avz --delete apps/frontend/dist/ user@<ip>:/var/www/jlpt/dist/
```

### 3.2 Systemd Service

```
[ ] 3.2.1  Tạo file /etc/systemd/system/jlpt-backend.service
           Tham chiếu: Plan.md § 3.3
[ ] 3.2.2  Reload systemd daemon
           Lệnh: sudo systemctl daemon-reload
[ ] 3.2.3  Enable & start backend
           Lệnh: sudo systemctl enable jlpt-backend && sudo systemctl start jlpt-backend
[ ] 3.2.4  Xác nhận Flyway migration chạy thành công
           Lệnh: sudo journalctl -u jlpt-backend -n 100
           Tìm: "Successfully applied X migration(s)"
[ ] 3.2.5  Xác nhận backend đang chạy trên port 8080
           Lệnh: curl http://localhost:8080/api/actuator/health
           Mong đợi: {"status":"UP"}
```

### 3.3 Nginx Configuration

```
[ ] 3.3.1  Tạo Nginx config tại /etc/nginx/sites-available/jlpt
           Tham chiếu: Plan.md § 3.4
[ ] 3.3.2  Enable site
           Lệnh: sudo ln -sf /etc/nginx/sites-available/jlpt /etc/nginx/sites-enabled/
[ ] 3.3.3  Tắt default site
           Lệnh: sudo rm -f /etc/nginx/sites-enabled/default
[ ] 3.3.4  Test config Nginx
           Lệnh: sudo nginx -t
           Mong đợi: configuration file test is successful
[ ] 3.3.5  Reload Nginx
           Lệnh: sudo systemctl reload nginx
```

### 3.4 SSL Certificate

```
[ ] 3.4.1  Xác nhận DNS đã propagate (A record trỏ đúng server)
           Lệnh: nslookup yourdomain.com
[ ] 3.4.2  Cấp SSL với Certbot
           Lệnh: sudo certbot --nginx -d yourdomain.com
[ ] 3.4.3  Xác nhận auto-renew hoạt động
           Lệnh: sudo certbot renew --dry-run
```

---

## Phase 4 — Smoke Test & Go Live

### 4.1 Infrastructure Tests

```
[ ] 4.1.1  HTTP → HTTPS redirect hoạt động
           Test: curl -I http://yourdomain.com
           Mong đợi: 301 Location: https://yourdomain.com

[ ] 4.1.2  HTTPS phản hồi 200
           Test: curl -I https://yourdomain.com
           Mong đợi: 200 OK

[ ] 4.1.3  API health check qua HTTPS
           Test: curl https://yourdomain.com/api/actuator/health
           Mong đợi: {"status":"UP"}

[ ] 4.1.4  Security headers có mặt
           Test: curl -I https://yourdomain.com
           Mong đợi: X-Frame-Options, X-Content-Type-Options, Strict-Transport-Security
```

### 4.2 Functional Tests

```
[ ] 4.2.1  Đăng ký tài khoản mới → nhận email xác nhận
[ ] 4.2.2  Đăng nhập email/password → vào được dashboard
[ ] 4.2.3  Đăng nhập Google OAuth → redirect đúng
[ ] 4.2.4  Xem danh sách khóa học theo JLPT level
[ ] 4.2.5  Làm quiz → submit → có kết quả điểm
[ ] 4.2.6  Upload ảnh OCR → nhận jobId → poll status
[ ] 4.2.7  Refresh JWT token → nhận access token mới
[ ] 4.2.8  Truy cập route không tồn tại → React 404 page
[ ] 4.2.9  Staff login → truy cập được content management
[ ] 4.2.10 Admin login → truy cập được admin panel
```

### 4.3 Bảo Mật

```
[ ] 4.3.1  Port 8080 không accessible từ bên ngoài
           Test: curl http://<server-ip>:8080 (từ máy ngoài → phải timeout)
[ ] 4.3.2  Port 1433 không accessible từ bên ngoài
           Test: telnet <server-ip> 1433 (phải bị từ chối)
[ ] 4.3.3  File .env không trả về qua HTTP
           Test: curl https://yourdomain.com/.env (phải 404)
```

### 4.4 Go Live

```
[ ] 4.4.1  Tất cả task Phase 4.1 + 4.2 + 4.3 đã pass
[ ] 4.4.2  Screenshot hoặc ghi lại kết quả smoke test
[ ] 4.4.3  Thông báo team deploy thành công
[ ] 4.4.4  Ghi lại thời điểm go live: ______________________
```

---

## Post-Deploy — Thiết Lập Vận Hành

```
[ ] P.1   Cấu hình log rotation cho /var/log/jlpt/
          File: /etc/logrotate.d/jlpt

[ ] P.2   Cấu hình cron backup database hàng ngày lúc 02:00
          Cron: 0 2 * * * sqlcmd -S localhost -U sa -P <pw> \
                -Q "BACKUP DATABASE JLPT_LearningDB TO DISK='/backup/jlpt_$(date +%Y%m%d).bak'"

[ ] P.3   Verify backup có thể restore được
          Test restore vào database khác

[ ] P.4   Thiết lập monitoring health check
          Check: https://yourdomain.com/api/actuator/health mỗi 30s

[ ] P.5   Ghi lại tất cả credentials vào password manager của team
          (Không lưu trong file text trên server)
```

---

## Ghi Chú Deploy

| Ngày | Phiên bản | Người thực hiện | Ghi chú |
|------|-----------|----------------|---------|
| | v2.0.0 | | Deploy lần đầu |
| | | | |

---

## Liên Kết Nhanh

- [Spec.md](Spec.md) — Đặc tả kỹ thuật chi tiết
- [Plan.md](Plan.md) — Lệnh và hướng dẫn từng bước
- [README.md](README.md) — Tổng quan và troubleshooting
