# Deployment Plan — JLPT E-Learning Platform v2.0

> **Loại tài liệu**: Kế hoạch triển khai theo giai đoạn
> **Phiên bản**: 2.0.0
> **Cập nhật**: 2026-06-23

---

## Tổng Quan Giai Đoạn

```
Phase 0          Phase 1           Phase 2           Phase 3          Phase 4
Chuẩn Bị   →   Server Setup  →   Database     →   Deploy App   →  Go Live
(Ngày 1)        (Ngày 1-2)        (Ngày 2)          (Ngày 2-3)      (Ngày 3)
```

---

## Phase 0 — Chuẩn Bị Trước Deploy

**Mục tiêu**: Đảm bảo toàn bộ code và môi trường sẵn sàng trước khi chạm vào server.

### 0.1 Kiểm Tra Code

| # | Việc cần làm | Công cụ | Kết quả mong đợi |
|---|-------------|---------|-----------------|
| 0.1.1 | Chạy toàn bộ unit test backend | `mvn test` | 100% pass, coverage ≥ 80% |
| 0.1.2 | Chạy toàn bộ unit test frontend | `npm run test` | 100% pass |
| 0.1.3 | Lint + format backend | `mvn spotless:apply` | Không có lỗi style |
| 0.1.4 | Lint frontend | `npm run lint` | Không có lỗi ESLint |
| 0.1.5 | Build thử backend | `mvn clean package` | JAR tạo thành công |
| 0.1.6 | Build thử frontend | `npm run build` | `dist/` tạo thành công |

### 0.2 Chuẩn Bị Credentials

| # | Việc cần làm | Công cụ |
|---|-------------|---------|
| 0.2.1 | Sinh JWT_SECRET | `openssl rand -hex 32` |
| 0.2.2 | Đăng ký Google OAuth — thêm domain production vào Redirect URI | Google Cloud Console |
| 0.2.3 | Tạo Gmail App Password cho SMTP | Google Account → Security |
| 0.2.4 | Chuẩn bị domain (A record trỏ về IP server) | DNS provider |
| 0.2.5 | Điền đầy đủ file `.env` production | Text editor |

### 0.3 Tạo Nhánh Release

```bash
git checkout -b release/v2.0.0
git tag v2.0.0
git push origin release/v2.0.0 --tags
```

---

## Phase 1 — Chuẩn Bị Server

**Mục tiêu**: Cài đặt toàn bộ dependencies và cấu hình hệ thống trên server mới.

**Công cụ**: SSH, apt, systemd

### 1.1 Cài Đặt Runtime

```bash
# Kết nối SSH
ssh user@<server-ip>

# Cập nhật hệ thống
sudo apt update && sudo apt upgrade -y

# Java 21 (Eclipse Temurin)
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" \
  | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-21-jre

# Xác nhận
java -version   # openjdk version "21"
```

### 1.2 Cài Đặt Nginx

```bash
sudo apt install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

### 1.3 Cài Đặt SQL Server 2022

```bash
# Import Microsoft key
curl https://packages.microsoft.com/keys/microsoft.asc | sudo apt-key add -
curl https://packages.microsoft.com/config/ubuntu/22.04/mssql-server-2022.list \
  | sudo tee /etc/apt/sources.list.d/mssql-server.list

sudo apt update
sudo apt install -y mssql-server

# Cấu hình (chọn Express, đặt SA password)
sudo /opt/mssql/bin/mssql-conf setup

# Cài sqlcmd tools
curl https://packages.microsoft.com/config/ubuntu/22.04/prod.list \
  | sudo tee /etc/apt/sources.list.d/mssql-release.list
sudo apt update
sudo ACCEPT_EULA=Y apt install -y mssql-tools18 unixodbc-dev
echo 'export PATH="$PATH:/opt/mssql-tools18/bin"' >> ~/.bashrc
source ~/.bashrc

sudo systemctl enable mssql-server
sudo systemctl start mssql-server
```

### 1.4 Tạo User & Thư Mục

```bash
# Tạo system user cho app
sudo useradd -r -s /bin/false jlpt

# Thư mục ứng dụng
sudo mkdir -p /opt/jlpt/uploads
sudo mkdir -p /var/log/jlpt
sudo mkdir -p /var/www/jlpt/dist

# Phân quyền
sudo chown -R jlpt:jlpt /opt/jlpt
sudo chown -R jlpt:jlpt /var/log/jlpt
sudo chown -R www-data:www-data /var/www/jlpt
```

### 1.5 Cài Certbot (SSL)

```bash
sudo apt install -y certbot python3-certbot-nginx
```

---

## Phase 2 — Cài Đặt Database

**Mục tiêu**: Tạo database, user, và chạy migration lần đầu.

**Công cụ**: sqlcmd, Flyway (chạy tự động qua Spring Boot)

### 2.1 Tạo Database và User

```sql
-- Kết nối với tài khoản sa
sqlcmd -S localhost -U sa -P '<sa-password>'

-- Tạo database
CREATE DATABASE JLPT_LearningDB;
GO

-- Tạo user riêng (không dùng sa trên production)
CREATE LOGIN jlpt_user WITH PASSWORD = '<strong-password>';
GO

USE JLPT_LearningDB;
GO

CREATE USER jlpt_user FOR LOGIN jlpt_user;
GO

-- Cấp quyền tối thiểu (không SYSADMIN)
ALTER ROLE db_datareader ADD MEMBER jlpt_user;
ALTER ROLE db_datawriter ADD MEMBER jlpt_user;
ALTER ROLE db_ddladmin ADD MEMBER jlpt_user;   -- cần cho Flyway
GO
```

### 2.2 Kiểm Tra Kết Nối

```bash
sqlcmd -S localhost -U jlpt_user -P '<password>' \
  -Q "SELECT name FROM sys.databases WHERE name = 'JLPT_LearningDB'"
```

### 2.3 Migration

Flyway sẽ tự động chạy migration khi backend khởi động lần đầu (Phase 3). Không cần chạy thủ công.

---

## Phase 3 — Deploy Ứng Dụng

**Mục tiêu**: Upload và khởi động backend + frontend.

**Công cụ**: scp/rsync, systemd, Nginx, Certbot

### 3.1 Build Trên Máy Dev

```bash
# Build backend
cd apps/backend
mvn clean package -DskipTests
# → target/backend-2.0.0.jar

# Build frontend
cd ../frontend
VITE_API_BASE_URL=https://api.yourdomain.com/api \
VITE_GOOGLE_CLIENT_ID=xxx.apps.googleusercontent.com \
npm run build
# → dist/
```

### 3.2 Upload Lên Server

```bash
# Upload JAR
scp apps/backend/target/backend-2.0.0.jar user@<server-ip>:/opt/jlpt/

# Upload file .env
scp apps/backend/.env.production user@<server-ip>:/opt/jlpt/.env

# Upload frontend
rsync -avz --delete apps/frontend/dist/ user@<server-ip>:/var/www/jlpt/dist/
```

### 3.3 Tạo Systemd Service (Backend)

```bash
sudo tee /etc/systemd/system/jlpt-backend.service > /dev/null << 'EOF'
[Unit]
Description=JLPT E-Learning Backend
After=network.target mssql-server.service
Requires=mssql-server.service

[Service]
Type=simple
User=jlpt
WorkingDirectory=/opt/jlpt
EnvironmentFile=/opt/jlpt/.env
ExecStart=/usr/bin/java -Xms512m -Xmx1g -jar /opt/jlpt/backend-2.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/var/log/jlpt/app.log
StandardError=append:/var/log/jlpt/error.log

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable jlpt-backend
sudo systemctl start jlpt-backend

# Theo dõi Flyway migration và startup
sudo journalctl -u jlpt-backend -f
```

### 3.4 Cấu Hình Nginx

```bash
sudo tee /etc/nginx/sites-available/jlpt > /dev/null << 'EOF'
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name yourdomain.com;

    ssl_certificate     /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Bảo mật headers
    add_header X-Frame-Options "SAMEORIGIN";
    add_header X-Content-Type-Options "nosniff";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";

    client_max_body_size 10M;

    # Frontend SPA
    root /var/www/jlpt/dist;
    index index.html;
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Backend API
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 10s;
        proxy_read_timeout 60s;
    }

    # File uploads
    location /uploads/ {
        alias /opt/jlpt/uploads/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
EOF

sudo ln -sf /etc/nginx/sites-available/jlpt /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

### 3.5 Cấp SSL

```bash
# Chạy sau khi DNS đã propagate và Nginx đang chạy
sudo certbot --nginx -d yourdomain.com
```

---

## Phase 4 — Smoke Test & Go Live

**Mục tiêu**: Xác nhận hệ thống hoạt động đúng trước khi thông báo người dùng.

**Công cụ**: curl, trình duyệt, Postman

### 4.1 Smoke Test — Infrastructure

```bash
# HTTPS redirect
curl -I http://yourdomain.com
# Expect: 301 → https

# HTTPS hoạt động
curl -I https://yourdomain.com
# Expect: 200 OK

# Backend health
curl https://yourdomain.com/api/actuator/health
# Expect: {"status":"UP"}
```

### 4.2 Smoke Test — Chức Năng

| # | Test case | Cách test | Kết quả mong đợi |
|---|-----------|-----------|-----------------|
| 4.2.1 | Đăng ký tài khoản mới | Trình duyệt | Nhận email xác nhận |
| 4.2.2 | Đăng nhập email/password | Trình duyệt | Vào được dashboard |
| 4.2.3 | Đăng nhập Google OAuth | Trình duyệt | Redirect đúng, vào dashboard |
| 4.2.4 | Xem danh sách khóa học | Trình duyệt | Hiển thị đúng theo level |
| 4.2.5 | Làm quiz | Trình duyệt | Submit được, có kết quả |
| 4.2.6 | Upload file (OCR) | Trình duyệt | Nhận jobId, poll được status |
| 4.2.7 | Refresh JWT | Postman | Nhận access token mới |
| 4.2.8 | Truy cập trang không tồn tại | Trình duyệt | React 404 page (không phải Nginx 404) |

### 4.3 Kiểm Tra Security Headers

```bash
curl -I https://yourdomain.com | grep -E "Strict|X-Frame|X-Content"
```

### 4.4 Go Live

```bash
# Thông báo đội khi tất cả smoke test pass
echo "✅ Deploy v2.0.0 hoàn tất — $(date)"
```

---

## Rollback Plan

Nếu phase 3 hoặc 4 gặp vấn đề nghiêm trọng:

```bash
# Dừng service mới
sudo systemctl stop jlpt-backend

# Khôi phục JAR cũ (nếu có backup)
sudo cp /opt/jlpt/backend-2.0.0.jar.bak /opt/jlpt/backend-2.0.0.jar

# Hoặc deploy lại version cũ từ Git tag
git checkout tags/v1.x.x
mvn clean package -DskipTests
scp target/backend-*.jar user@server:/opt/jlpt/

sudo systemctl start jlpt-backend
```

---

## Lịch Deploy Định Kỳ (Sau Go Live)

| Loại | Tần suất | Thời điểm | Công việc |
|------|----------|-----------|-----------|
| Hotfix | Khi cần | Bất kỳ lúc nào | Build + upload JAR/dist → restart |
| Minor release | 2 tuần/lần | Tối (ít user) | Full phase 3-4 |
| Major release | Theo sprint | Cuối sprint | Full phase 0-4 |
| SSL renew | 90 ngày | Auto (certbot cron) | Không cần thủ công |
| DB backup verify | Hàng tuần | Thứ 2 sáng | Restore thử vào DB test |
