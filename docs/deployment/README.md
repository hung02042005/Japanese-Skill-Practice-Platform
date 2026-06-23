# Hướng Dẫn Deploy — JLPT E-Learning Platform v2.0

> **Stack**: Spring Boot 3.3.3 (Java 21) + React 18 (Vite) + SQL Server

---

## Mục Lục

1. [Yêu Cầu Hệ Thống](#1-yêu-cầu-hệ-thống)
2. [Biến Môi Trường](#2-biến-môi-trường)
3. [Deploy Local (Development)](#3-deploy-local-development)
4. [Deploy Production — Thủ Công](#4-deploy-production--thủ-công)
5. [Deploy với Docker](#5-deploy-với-docker)
6. [Database & Migration](#6-database--migration)
7. [CI/CD (GitHub Actions)](#7-cicd-github-actions)
8. [Checklist Trước Khi Deploy](#8-checklist-trước-khi-deploy)
9. [Xử Lý Sự Cố Thường Gặp](#9-xử-lý-sự-cố-thường-gặp)

---

## 1. Yêu Cầu Hệ Thống

### Môi Trường Phát Triển (Dev)

| Công cụ | Phiên bản tối thiểu | Ghi chú |
|---------|---------------------|---------|
| Java JDK | 21 | Eclipse Temurin hoặc Oracle JDK |
| Maven | 3.9+ | Hoặc dùng `./mvnw` wrapper |
| Node.js | 18 LTS+ | Khuyến nghị 20 LTS |
| npm | 9+ | Đi kèm Node.js |
| SQL Server | 2019+ | Hoặc SQL Server Express (dev) |

### Môi Trường Production (Server)

| Tài nguyên | Tối thiểu | Khuyến nghị |
|-----------|-----------|-------------|
| CPU | 2 vCPU | 4 vCPU |
| RAM | 4 GB | 8 GB |
| Disk | 20 GB SSD | 50 GB SSD |
| OS | Ubuntu 22.04 LTS | Ubuntu 22.04 LTS |

### Cổng Mặc Định

| Service | Cổng |
|---------|------|
| Backend (Spring Boot) | `8080` |
| Frontend (Vite dev) | `3000` |
| SQL Server | `1433` |
| SMTP (Gmail) | `587` |

---

## 2. Biến Môi Trường

### Backend — `apps/backend/.env`

Tạo file từ template:
```bash
cp apps/backend/.env.example apps/backend/.env
```

| Biến | Bắt buộc | Ví dụ | Mô tả |
|------|----------|-------|-------|
| `DATABASE_URL` | ✅ | `jdbc:sqlserver://localhost:1433;databaseName=JLPT_LearningDB;encrypt=true;trustServerCertificate=true` | JDBC connection string |
| `DATABASE_USERNAME` | ✅ | `sa` | SQL Server username |
| `DATABASE_PASSWORD` | ✅ | `StrongPass@123` | SQL Server password |
| `JWT_SECRET` | ✅ | chuỗi hex 64 ký tự | Dùng `openssl rand -hex 32` để sinh |
| `GOOGLE_CLIENT_ID` | ✅ | `xxx.apps.googleusercontent.com` | Google OAuth 2.0 |
| `GOOGLE_CLIENT_SECRET` | ✅ | `GOCSPX-xxx` | Google OAuth 2.0 |
| `SMTP_USERNAME` | ✅ | `jlptelearningplatform@gmail.com` | Gmail gửi mail |
| `SMTP_PASSWORD` | ✅ | App Password Gmail | Bật 2FA rồi tạo App Password |
| `AI_OCR_API_KEY` | ⬜ | — | OCR service API key |
| `AI_SPEECH_API_KEY` | ⬜ | — | Speech recognition API key |
| `PAYMENT_GATEWAY_KEY` | ⬜ | — | Cổng thanh toán (VIP subscription) |

> **Bảo mật**: Không commit file `.env` vào Git. File này đã có trong `.gitignore`.

> **Sinh JWT_SECRET an toàn**:
> ```bash
> openssl rand -hex 32
> ```

### Frontend — `apps/frontend/.env`

Tạo file từ template:
```bash
cp apps/frontend/.env.example apps/frontend/.env
```

| Biến | Bắt buộc | Ví dụ |
|------|----------|-------|
| `VITE_API_BASE_URL` | ✅ | `http://localhost:8080/api` (dev) hoặc `https://api.yourdomain.com/api` (prod) |
| `VITE_GOOGLE_CLIENT_ID` | ✅ | `xxx.apps.googleusercontent.com` |

---

## 3. Deploy Local (Development)

### Bước 1 — Clone & Chuẩn Bị

```bash
git clone <repo-url>
cd Japanese-Skill-Practice-Platform
```

### Bước 2 — Thiết Lập Database

1. Cài và khởi động SQL Server 2019+
2. Tạo database:
   ```sql
   CREATE DATABASE JLPT_LearningDB;
   ```
3. Flyway sẽ tự chạy migration khi backend khởi động lần đầu.
4. (Tùy chọn) Seed dữ liệu mẫu:
   ```bash
   # Chạy file seed trong SQL Server Management Studio
   database/seeds/
   ```

### Bước 3 — Khởi Động Backend

```bash
cd apps/backend

# Tạo file .env
cp .env.example .env
# Điền các giá trị vào .env

# Chạy
./mvnw spring-boot:run
# Hoặc: mvn spring-boot:run
```

Backend sẵn sàng tại: `http://localhost:8080`

### Bước 4 — Khởi Động Frontend

```bash
cd apps/frontend

# Cài dependencies
npm install

# Tạo file .env
cp .env.example .env
# Điền VITE_API_BASE_URL và VITE_GOOGLE_CLIENT_ID

# Chạy dev server
npm run dev
```

Frontend sẵn sàng tại: `http://localhost:3000`

> **Shortcut**: Dùng script tổng hợp tại gốc dự án:
> ```bash
> run_project.bat   # Windows
> ```

### Bước 5 — Kiểm Tra

```bash
# Kiểm tra backend health
curl http://localhost:8080/api/actuator/health

# Mở trình duyệt
http://localhost:3000
```

---

## 4. Deploy Production — Thủ Công

### 4.1 Build Backend (JAR)

```bash
cd apps/backend

# Build (bỏ qua test nếu đã chạy trước)
mvn clean package -DskipTests

# File output: target/backend-2.0.0.jar
```

### 4.2 Build Frontend (Static Files)

```bash
cd apps/frontend

# Đặt VITE_API_BASE_URL = URL production thật
echo "VITE_API_BASE_URL=https://api.yourdomain.com/api" > .env.production
echo "VITE_GOOGLE_CLIENT_ID=xxx.apps.googleusercontent.com" >> .env.production

npm install
npm run build

# Output: dist/
```

### 4.3 Upload Lên Server

```bash
# Upload JAR
scp apps/backend/target/backend-2.0.0.jar user@server:/opt/jlpt/

# Upload frontend build
scp -r apps/frontend/dist/ user@server:/var/www/jlpt/
```

### 4.4 Chạy Backend trên Server

```bash
# Tạo systemd service
sudo nano /etc/systemd/system/jlpt-backend.service
```

```ini
[Unit]
Description=JLPT E-Learning Backend
After=network.target

[Service]
User=jlpt
WorkingDirectory=/opt/jlpt
EnvironmentFile=/opt/jlpt/.env
ExecStart=/usr/bin/java -jar -Xms512m -Xmx1g backend-2.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable jlpt-backend
sudo systemctl start jlpt-backend
sudo systemctl status jlpt-backend
```

### 4.5 Cấu Hình Nginx (Reverse Proxy)

```nginx
# /etc/nginx/sites-available/jlpt

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

    # Frontend (React SPA)
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
    }

    # File uploads
    location /uploads/ {
        alias /opt/jlpt/uploads/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/jlpt /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 4.6 SSL với Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```

---

## 5. Deploy với Docker

> **Lưu ý**: Docker Compose hiện cấu hình PostgreSQL. Backend mặc định dùng SQL Server.
> Cần đặt `DATABASE_URL` trỏ đúng vào container DB trong `.env`.

### 5.1 Cấu Trúc Docker (Đề Xuất)

```
docs/deployment/
└── docker-compose.yml      # DB + Redis services
apps/backend/
└── Dockerfile              # Spring Boot image
apps/frontend/
└── Dockerfile              # Nginx + React build
```

### 5.2 Dockerfile — Backend

```dockerfile
# apps/backend/Dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S jlpt && adduser -S jlpt -G jlpt

COPY target/backend-*.jar app.jar

RUN chown jlpt:jlpt app.jar
USER jlpt

EXPOSE 8080
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-jar", "app.jar"]
```

### 5.3 Dockerfile — Frontend

```dockerfile
# apps/frontend/Dockerfile
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine AS runtime
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
```

### 5.4 docker-compose.yml Đầy Đủ

```yaml
# docs/deployment/docker-compose.yml
version: '3.8'

services:
  db:
    image: mcr.microsoft.com/mssql/server:2022-latest
    environment:
      ACCEPT_EULA: Y
      MSSQL_SA_PASSWORD: ${DATABASE_PASSWORD}
      MSSQL_PID: Express
    ports:
      - "1433:1433"
    volumes:
      - mssql_data:/var/opt/mssql
    healthcheck:
      test: ["CMD", "/opt/mssql-tools/bin/sqlcmd", "-S", "localhost", "-U", "sa", "-P", "${DATABASE_PASSWORD}", "-Q", "SELECT 1"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ../../apps/backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    env_file:
      - ../../apps/backend/.env
    depends_on:
      db:
        condition: service_healthy
    volumes:
      - uploads_data:/app/uploads

  frontend:
    build:
      context: ../../apps/frontend
      dockerfile: Dockerfile
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend

volumes:
  mssql_data:
  uploads_data:
```

### 5.5 Khởi Chạy Docker

```bash
# Build và chạy tất cả services
cd docs/deployment
docker compose up -d --build

# Xem logs
docker compose logs -f backend

# Dừng
docker compose down
```

---

## 6. Database & Migration

### Flyway — Tự Động Migration

Flyway chạy tự động khi backend khởi động. File migration đặt tại:
```
apps/backend/src/main/resources/db/migration/
└── V1__init_schema.sql
└── V2__add_indexes.sql
└── ...
```

**Quy tắc đặt tên**: `V{version}__{description}.sql`

### Khởi Tạo Database Thủ Công

```bash
# Chạy file init nếu cần reset từ đầu
sqlcmd -S localhost -U sa -P <password> -i database/init.sql
```

### Backup Database (Production)

```bash
# Backup tự động hàng ngày
sqlcmd -S localhost -U sa -P <password> \
  -Q "BACKUP DATABASE JLPT_LearningDB TO DISK='/backup/jlpt_$(date +%Y%m%d).bak'"
```

---

## 7. CI/CD (GitHub Actions)

### Cấu Trúc Workflows Đề Xuất

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      - name: Run backend tests
        run: |
          cd apps/backend
          mvn test

  test-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: apps/frontend/package-lock.json
      - name: Run frontend tests
        run: |
          cd apps/frontend
          npm ci
          npm run test

  build:
    needs: [test-backend, test-frontend]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - name: Build backend JAR
        run: |
          cd apps/backend
          mvn clean package -DskipTests
      - name: Build frontend
        run: |
          cd apps/frontend
          npm ci
          npm run build
        env:
          VITE_API_BASE_URL: ${{ secrets.VITE_API_BASE_URL }}
          VITE_GOOGLE_CLIENT_ID: ${{ secrets.VITE_GOOGLE_CLIENT_ID }}
```

### GitHub Secrets Cần Thiết

Vào **Settings → Secrets and variables → Actions** và thêm:

| Secret | Dùng trong |
|--------|-----------|
| `DATABASE_URL` | Backend |
| `DATABASE_USERNAME` | Backend |
| `DATABASE_PASSWORD` | Backend |
| `JWT_SECRET` | Backend |
| `GOOGLE_CLIENT_ID` | Backend + Frontend |
| `GOOGLE_CLIENT_SECRET` | Backend |
| `SMTP_PASSWORD` | Backend |
| `VITE_API_BASE_URL` | Frontend build |

---

## 8. Checklist Trước Khi Deploy

### Backend

- [ ] Tất cả biến môi trường đã được điền vào `.env`
- [ ] `JWT_SECRET` là chuỗi ngẫu nhiên đủ mạnh (≥ 64 hex chars)
- [ ] `bcrypt` cost ≥ 10 (kiểm tra trong `SecurityConfig`)
- [ ] Flyway migration chạy không lỗi
- [ ] `mvn test` pass (JaCoCo coverage ≥ 80%)
- [ ] Log level production: `WARN` hoặc `ERROR` (không để `DEBUG`)
- [ ] CORS `allowed-origins` đặt đúng domain production
- [ ] Tắt `spring.jpa.show-sql: true` trên production

### Frontend

- [ ] `VITE_API_BASE_URL` trỏ đúng API production
- [ ] `npm run build` thành công, không có lỗi TypeScript/lint
- [ ] `npm run test` pass
- [ ] Không có `console.log` debug trong production build

### Infrastructure

- [ ] SSL/TLS đã cấu hình (HTTPS bắt buộc)
- [ ] Nginx reverse proxy hoạt động
- [ ] Cổng 8080 không mở public (chỉ Nginx truy cập)
- [ ] Firewall chỉ mở 80, 443 (và 22 cho SSH)
- [ ] Backup database đã được lên lịch
- [ ] Thư mục `/uploads` có đủ dung lượng và quyền ghi

### Security

- [ ] File `.env` không nằm trong Git
- [ ] SQL Server không dùng tài khoản `sa` trên production (tạo user riêng)
- [ ] Gmail App Password (không dùng password chính)
- [ ] Google OAuth Redirect URI đã thêm domain production

---

## 9. Xử Lý Sự Cố Thường Gặp

### Backend không khởi động được

```bash
# Xem logs chi tiết
systemctl status jlpt-backend
journalctl -u jlpt-backend -n 100

# Kiểm tra kết nối DB
sqlcmd -S localhost -U sa -P <password> -Q "SELECT 1"
```

**Nguyên nhân thường gặp:**
- Sai `DATABASE_URL` hoặc `DATABASE_PASSWORD`
- SQL Server chưa khởi động
- Port 8080 đã bị chiếm: `lsof -i :8080`

### Flyway migration lỗi

```bash
# Xem lỗi migration
grep -i "flyway\|migration" /var/log/jlpt/app.log

# Sửa lỗi rồi repair checksum (nếu đã sửa file migration)
# Không sửa file migration đã chạy — tạo file V mới
```

### Frontend hiện trang trắng (SPA routing)

Nginx phải có `try_files $uri $uri/ /index.html;` trong location `/` — xem mục 4.5.

### CORS lỗi khi gọi API

Kiểm tra `app.cors.allowed-origins` trong `application.yml` phải chứa domain frontend:
```yaml
app:
  cors:
    allowed-origins: https://yourdomain.com
```

### Email không gửi được

1. Bật 2-Step Verification trên Gmail
2. Vào Google Account → Security → App passwords → Tạo mới
3. Dùng App Password (16 ký tự) cho `SMTP_PASSWORD`

### JWT lỗi sau khi deploy

Nếu đổi `JWT_SECRET`, tất cả token cũ sẽ invalid — người dùng phải đăng nhập lại. Không đổi secret trên production khi không cần thiết.

---

*Cập nhật lần cuối: 2026-06-23 | Phiên bản: 2.0.0*
