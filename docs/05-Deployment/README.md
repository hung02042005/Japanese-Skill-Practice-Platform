# Tài Liệu Deploy — JLPT E-Learning (sakuji.online)

> **Cập nhật:** 2026-06-30 | **Stack:** Spring Boot 3.3 · Java 21 · React · SQL Server 2022 · Redis · Docker · Nginx

---

## Kiến Trúc Hệ Thống

```
Người dùng
    │ HTTPS
    ▼
┌──────────────────────────────────┐
│  Cloudflare (đã có, MIỄN PHÍ)   │
│  CDN · WAF · DDoS · SSL          │
│  Domain: sakuji.online           │
└────────────────┬─────────────────┘
                 │ HTTP (ẩn IP thật)
                 ▼
┌──────────────────────────────────┐
│  Máy chạy hệ thống               │
│  (Laptop cá nhân HOẶC VPS)       │
│                                  │
│  ┌───────────────────────────┐   │
│  │     Docker Compose        │   │
│  │  Nginx (frontend :80)     │   │
│  │  Spring Boot (:8080)      │   │
│  │  SQL Server (:1433)       │   │
│  │  Redis (:6379)            │   │
│  └───────────────────────────┘   │
└──────────────────────────────────┘
```

Cloudflare đứng trước, lo toàn bộ SSL/HTTPS và bảo mật — máy chạy hệ thống chỉ cần HTTP cổng 80.

---

## Chọn Approach Phù Hợp

Có **2 cách deploy**, chọn theo tình huống:

| Tiêu chí | Approach A: Demo (Laptop) | Approach B: VPS Thật |
|---|---|---|
| **Mục đích** | Bảo vệ đồ án, demo ngắn hạn | Website chạy 24/7 ổn định |
| **Chi phí** | 0 đồng | ~$7–24/tháng (≈₫175k–600k) |
| **Uptime** | Chỉ khi máy bật | 24/7 tự động |
| **Độ phức tạp** | Đơn giản | Trung bình |
| **Khi máy tắt** | Website chết | Website vẫn sống |
| **Phù hợp khi** | Còn trong thời gian bảo vệ, chưa có ngân sách | Muốn website thật sự ổn định |

---

## Bảng Chi Phí Provider (nếu chọn Approach B)

| Provider | Gói | Giá/tháng | RAM | Ghi chú |
|---|---|---|---|---|
| **Azure for Students** | B2s VM | **$0** (dùng $100 credit) | 4 GB | Dùng email trường, không cần thẻ |
| **Contabo** | Cloud VPS S | ~$7 (~₫175k) | 8 GB | Rẻ nhất, datacenter EU/US |
| **Hetzner** | CX32 | ~$8 (~₫200k) | 8 GB | Ổn định, datacenter EU |
| **DigitalOcean** | Basic 4GB | $24 (~₫600k) | 4 GB | Docs tốt, Singapore (gần VN) |
| **Vultr** | Regular 4GB | $24 (~₫600k) | 4 GB | Singapore available |
| **VNG Cloud** | VM | ~₫200k | 2 GB ⚠️ | Trong nước, nhưng RAM hơi thấp |

> **Khuyến nghị:** Nếu có email trường → dùng **Azure for Students** ($100 free, đủ chạy 3 tháng).
> Sau khi hết credit → chuyển sang **Contabo** ($7/tháng, 8GB RAM, đủ thoải mái).

---

## Cấu Trúc Tài Liệu

```
docs/05-Deployment/
├── README.md                       ← Bạn đang đọc — tổng quan & chọn approach
│
├── A-demo-cloudflare-tunnel.md     ← Approach A: Demo đồ án (laptop + Cloudflare Tunnel)
│                                      Dùng khi: muốn online ngay, 0 đồng
│
├── B-deploy-vps.md                 ← Approach B: Deploy lên VPS thật
│                                      Dùng khi: cần 24/7, đã chọn được provider
│
├── C-cloudflare-dns-ssl.md         ← Cấu hình Cloudflare (dùng chung cho cả A và B khi lên VPS)
│                                      Đổi DNS sakuji.online → trỏ về VPS
│
├── D-van-hanh-backup-monitoring.md ← Vận hành hằng ngày, backup DB, monitoring
│                                      Đọc sau khi đã deploy xong
│
└── legacy/                         ← Tài liệu cũ, chỉ để tham khảo
```

---

## Thứ Tự Đọc Tài Liệu

### Nếu chọn Approach A (Demo ngay)

```
README.md → A-demo-cloudflare-tunnel.md → D-van-hanh-backup-monitoring.md
```

### Nếu chọn Approach B (VPS thật)

```
README.md → B-deploy-vps.md → C-cloudflare-dns-ssl.md → D-van-hanh-backup-monitoring.md
```

---

## Yêu Cầu Tối Thiểu

Dù chọn approach nào, máy chạy hệ thống phải đáp ứng:

| Tài nguyên | Tối thiểu | Khuyến nghị | Lý do |
|---|---|---|---|
| **RAM** | 4 GB | 8 GB | SQL Server cần 2GB riêng |
| **CPU** | 2 vCPU | 4 vCPU | JVM + SQL Server nặng |
| **Disk** | 20 GB SSD | 50 GB | DB + uploads + Docker images |
| **OS** | Ubuntu 22.04 (VPS) / Windows 10+ (laptop) | Ubuntu 22.04 | |
| **Kiến trúc** | x86-64 | x86-64 | SQL Server không chạy trên ARM |

---

## Checklist Nhanh Sau Khi Deploy

Sau khi hệ thống lên, kiểm tra tất cả các mục sau:

- [ ] `https://sakuji.online` mở được, không có cảnh báo SSL
- [ ] Đăng ký tài khoản mới → nhận được email xác nhận
- [ ] Đăng nhập email/password → vào được dashboard
- [ ] Đăng nhập Google OAuth → redirect thành công
- [ ] Xem danh sách khóa học theo cấp độ N5–N1
- [ ] Làm bài Quiz → nộp → nhận điểm, lưu vào lịch sử
- [ ] Upload ảnh OCR → nhận `jobId`, poll kết quả
- [ ] Đăng nhập Staff → vào được trang quản lý nội dung
- [ ] Đăng nhập Admin → vào được trang quản trị người dùng
