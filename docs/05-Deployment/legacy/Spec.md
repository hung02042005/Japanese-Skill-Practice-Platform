# Deployment Specification — JLPT E-Learning Platform v2.0

> **Loại tài liệu**: Đặc tả kỹ thuật deploy
> **Phiên bản**: 2.0.0
> **Cập nhật**: 2026-06-23

---

## 1. Tổng Quan Hệ Thống

### 1.1 Kiến Trúc Deploy

```
Internet
    │
    ▼
┌─────────────┐
│  Nginx 1.24 │  ← Reverse Proxy + SSL Termination + Static Files
│  Port 80/443│
└──────┬──────┘
       │
       ├──── /         → React SPA (static files tại /var/www/jlpt/dist)
       └──── /api/*    → Spring Boot Backend (localhost:8080)
                              │
                    ┌─────────┼──────────┐
                    ▼         ▼          ▼
              MySQL 8      /uploads   Gmail SMTP
              Port 3306   (File store)  Port 587
```

### 1.2 Công Nghệ & Phiên Bản

| Layer | Công nghệ | Phiên bản | Ghi chú |
|-------|-----------|-----------|---------|
| Frontend | React | 18.3.1 | Build bằng Vite 5.4 |
| Build tool FE | Vite | 5.4.0 | Output: `dist/` |
| Backend | Spring Boot | 3.3.3 | Java 21, Maven |
| Runtime | JRE | 21 (Temurin) | Eclipse Temurin |
| Database | MySQL 8 | LTS | Port 3306 |
| Migration | Flyway | Core + MySQL | Auto-run khi boot |
| Reverse Proxy | Nginx | 1.24+ | SSL termination |
| SSL | Let's Encrypt | Certbot | Auto-renew 90 ngày |
| Auth | JWT + Google OAuth | JJWT 0.12.6 | Stateless |
| Email | Gmail SMTP | TLS 587 | App Password |
| Container | Docker + Compose | 24+ | Tùy chọn |

---

## 2. Yêu Cầu Hạ Tầng

### 2.1 Server Production

| Thông số | Tối thiểu | Khuyến nghị | Lý do |
|---------|-----------|-------------|-------|
| CPU | 2 vCPU | 4 vCPU | Spring Boot JVM + MySQL 8 |
| RAM | 4 GB | 8 GB | JVM heap 1GB + MySQL 8 2GB |
| Disk | 20 GB SSD | 50 GB SSD | DB + uploads + logs |
| OS | Ubuntu 22.04 LTS | Ubuntu 22.04 LTS | LTS support đến 2027 |
| Bandwidth | 100 Mbps | 1 Gbps | Audio/image transfer |

### 2.2 Phân Bổ Tài Nguyên

| Service | RAM Heap | CPU |
|---------|----------|-----|
| JVM (Spring Boot) | `-Xms512m -Xmx1g` | ~1 vCPU |
| MySQL 8 | ~1.5 GB | ~1 vCPU |
| Nginx | ~50 MB | Minimal |
| OS overhead | ~512 MB | ~0.5 vCPU |

### 2.3 Cổng Mạng

| Cổng | Service | Public | Ghi chú |
|------|---------|--------|---------|
| 22 | SSH | ✅ | Chỉ IP whitelist |
| 80 | HTTP | ✅ | Redirect sang 443 |
| 443 | HTTPS | ✅ | Nginx + SSL |
| 8080 | Backend | ❌ | Chỉ localhost (Nginx proxy) |
| 3306 | MySQL 8 | ❌ | Chỉ localhost |
| 587 | SMTP | ❌ | Outbound only |

---

## 3. Đặc Tả Môi Trường

### 3.1 Môi Trường Development (Local)

| Thông số | Giá trị |
|---------|---------|
| Backend URL | `http://localhost:8080` |
| Frontend URL | `http://localhost:3000` |
| Database | MySQL 8 Local `JLPT_LearningDB` |
| JWT Expiry | Access: 15 phút / Refresh: 7 ngày |
| Log Level | `DEBUG` |
| Flyway | Auto-migrate ON |
| CORS | `http://localhost:3000`, `http://localhost:5173` |

### 3.2 Môi Trường Production

| Thông số | Giá trị |
|---------|---------|
| Backend URL | `https://api.yourdomain.com` (internal: `localhost:8080`) |
| Frontend URL | `https://yourdomain.com` |
| Database | MySQL 8 Production (user riêng, không dùng `root`) |
| JWT Expiry | Access: 15 phút / Refresh: 7 ngày |
| Log Level | `WARN` |
| Flyway | Auto-migrate ON |
| CORS | `https://yourdomain.com` |
| SSL | Let's Encrypt, auto-renew |

---

## 4. Đặc Tả Biến Môi Trường

### 4.1 Backend (`apps/backend/.env`)

```env
# Database — MySQL 8
DATABASE_URL=jdbc:mysql://localhost:13306/JLPT_LearningDB?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
DATABASE_USERNAME=jlpt_user
DATABASE_PASSWORD=<strong-password>

# JWT — sinh bằng: openssl rand -hex 32
JWT_SECRET=<64-char-hex-string>

# Google OAuth 2.0
GOOGLE_CLIENT_ID=<xxx>.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-<xxx>

# Gmail SMTP — dùng App Password (không dùng password chính)
SMTP_USERNAME=jlptelearningplatform@gmail.com
SMTP_PASSWORD=<16-char-app-password>

# AI Services (nếu dùng)
AI_OCR_API_KEY=
AI_SPEECH_API_KEY=

# Payment (VIP subscription)
PAYMENT_GATEWAY_KEY=
```

### 4.2 Frontend (`apps/frontend/.env.production`)

```env
VITE_API_BASE_URL=https://api.yourdomain.com/api
VITE_GOOGLE_CLIENT_ID=<xxx>.apps.googleusercontent.com
```

---

## 5. Đặc Tả Build

### 5.1 Backend Build

| Thông số | Giá trị |
|---------|---------|
| Lệnh build | `mvn clean package -DskipTests` |
| Output | `apps/backend/target/backend-2.0.0.jar` |
| Java version | 21 (phải match runtime) |
| Test coverage | ≥ 80% (JaCoCo) |
| Code style | Spotless (chạy `mvn spotless:apply` trước) |

### 5.2 Frontend Build

| Thông số | Giá trị |
|---------|---------|
| Lệnh build | `npm run build` |
| Output | `apps/frontend/dist/` |
| Node version | 20 LTS |
| Bundler | Vite 5.4 |
| API proxy | Chỉ dùng lúc dev — prod dùng `VITE_API_BASE_URL` |

---

## 6. Đặc Tả Database

### 6.1 MySQL 8

| Thông số | Dev | Production |
|---------|-----|-----------|
| Phiên bản | 8.0+ | 8.4 LTS |
| Database name | `JLPT_LearningDB` | `JLPT_LearningDB` |
| User | `root` | `jlpt_user` (riêng, không phải root) |
| Port | 13306 | 13306 (không mở public) |
| SSL | `useSSL=false` | `useSSL=true` + cert hợp lệ |

### 6.2 Migration (Flyway)

- **Vị trí**: `apps/backend/src/main/resources/db/migration/`
- **Naming**: `V{n}__{description}.sql`
- **Tự động**: Chạy khi backend khởi động
- **Không sửa**: File migration đã chạy — tạo file V mới nếu cần thay đổi
- **Baseline**: `flyway.baseline-on-migrate=true` (production)

### 6.3 Backup

| Loại | Tần suất | Lưu giữ | Công cụ |
|------|----------|---------|---------|
| Full backup | Hàng ngày (02:00) | 30 ngày | MySQL Dump + cron |
| Transaction log | 6 giờ/lần | 7 ngày | MySQL |
| Offsite | Hàng tuần | 3 tháng | rsync → S3/GCS |

---

## 7. Đặc Tả Security

### 7.1 Authentication

| Cơ chế | Đặc tả |
|--------|-------|
| JWT | Access token: 15 phút, Refresh token: 7 ngày |
| bcrypt | Cost factor ≥ 10 (bắt buộc theo ADR-003) |
| Google OAuth | Redirect URI phải đăng ký đúng domain production |
| Session | Stateless (không lưu session server-side) |

### 7.2 HTTPS & TLS

- Tất cả traffic phải qua HTTPS trên production
- HTTP tự động redirect sang HTTPS (301)
- SSL cert: Let's Encrypt (auto-renew via cron)
- TLS version: TLS 1.2+ (disable TLS 1.0/1.1)

### 7.3 CORS

- Chỉ whitelist domain frontend production trong `allowed-origins`
- Không dùng `*` wildcard trên production

---

## 8. Đặc Tả Logging & Monitoring

### 8.1 Log Configuration

| Môi trường | Level | Output |
|-----------|-------|--------|
| Development | `DEBUG` | Console |
| Production | `WARN` | File `/var/log/jlpt/app.log` |
| Error | `ERROR` | File + alert |

### 8.2 Log Rotation

```
/var/log/jlpt/*.log {
    daily
    rotate 30
    compress
    missingok
    notifempty
}
```

### 8.3 Health Check

- **Endpoint**: `GET /api/actuator/health`
- **Interval**: 30 giây
- **Timeout**: 5 giây
- **Threshold**: 3 lần fail → alert

---

## 9. Ràng Buộc & Giới Hạn

| Ràng buộc | Giá trị | Ghi chú |
|-----------|---------|---------|
| MySQL 8 RAM | 1.4 GB | Nâng cấp cấu hình nếu vượt |
| Upload file size | 10 MB | Cấu hình trong Spring Boot + Nginx |
| JWT không revoke được | — | Đây là thiết kế (stateless) |
| Flyway không rollback tự động | — | Cần viết migration ngược nếu cần |
| AI calls đồng bộ | Timeout 30s | Async + jobId cho OCR/Speech |

---

*Tài liệu này phản ánh kiến trúc và quyết định trong `CLAUDE.md` (ADR-001 → ADR-008)*
