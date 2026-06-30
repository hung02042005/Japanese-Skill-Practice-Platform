# Approach B — Deploy Lên VPS Thật (24/7)

> **Dùng khi:** Muốn `https://sakuji.online` chạy ổn định 24/7, không phụ thuộc máy cá nhân.
>
> **Thời gian thực hiện:** ~1–2 giờ lần đầu.

---

## Phần 0: Chọn Và Đăng Ký Provider

### Option 0A — Azure for Students (Khuyến nghị nếu có email trường)

**Điều kiện:** Email trường đại học đang hoạt động (`.edu.vn` hoặc email trường cấp).

**Bước thực hiện:**

1. Vào [azure.microsoft.com/free/students](https://azure.microsoft.com/free/students)
2. Nhấn **Activate now** → đăng nhập bằng email trường
3. Xác minh trạng thái sinh viên (thường tự động, đôi khi cần ảnh thẻ sinh viên)
4. Nhận **$100 credit** — không cần nhập thẻ ngân hàng
5. Vào [portal.azure.com](https://portal.azure.com) → **Create a resource** → **Virtual Machine**

**Cấu hình VM khuyến nghị:**

| Mục | Chọn |
|---|---|
| Region | Southeast Asia (Singapore) — gần VN nhất |
| Image | Ubuntu Server 22.04 LTS |
| Size | **B2s** (2 vCPU, 4 GB RAM) — đủ chạy toàn bộ stack |
| Authentication | SSH public key (tạo mới hoặc dùng key có sẵn) |
| Inbound ports | Allow: SSH (22), HTTP (80), HTTPS (443) |

> $100 credit đủ chạy VM B2s khoảng **2–3 tháng** tùy cấu hình thêm.

---

### Option 0B — Contabo (Rẻ nhất nếu không có email trường)

**Giá:** ~$7/tháng (~₫175.000) cho 8 GB RAM, 4 vCPU, 200 GB SSD.

**Bước thực hiện:**

1. Vào [contabo.com](https://contabo.com) → **Cloud VPS** → chọn gói **Cloud VPS S**
2. Chọn Location: **Singapore** (Asia region)
3. OS: **Ubuntu 22.04**
4. Thanh toán (cần thẻ quốc tế Visa/Mastercard hoặc PayPal)
5. Nhận email chứa IP và mật khẩu root trong vòng 15–30 phút

---

### Option 0C — DigitalOcean (Nếu có GitHub Student Pack)

**GitHub Student Pack** tặng **$200 credit DigitalOcean** (120 ngày).

1. Vào [education.github.com/pack](https://education.github.com/pack) → đăng ký bằng email trường
2. Sau khi được duyệt → vào mục DigitalOcean → claim credit
3. Tạo **Droplet** tại [cloud.digitalocean.com](https://cloud.digitalocean.com):
   - Region: Singapore
   - OS: Ubuntu 22.04 LTS
   - Size: **Basic — 4 GB RAM / 2 vCPU** ($24/tháng, dùng credit)
4. Thêm SSH key → Create Droplet

---

## Phần 1: Cài Đặt Môi Trường Server

Sau khi có VPS, mọi bước dưới đây đều giống nhau dù dùng provider nào.

### Bước 1.1 — SSH vào VPS

```bash
# Thay <IP-VPS> bằng IP thật từ nhà cung cấp
ssh root@<IP-VPS>

# Nếu dùng SSH key (Azure / DigitalOcean):
ssh -i ~/.ssh/id_rsa azureuser@<IP-VPS>
```

### Bước 1.2 — Cập nhật hệ thống

```bash
apt update && apt upgrade -y
```

### Bước 1.3 — Cài Docker

```bash
# Cài Docker Engine chính thức
curl -fsSL https://get.docker.com | sh

# Cài Docker Compose plugin (v2)
apt install docker-compose-plugin -y

# Kiểm tra
docker --version
# Phải ra: Docker version 26.x.x

docker compose version
# Phải ra: Docker Compose version v2.x.x
```

### Bước 1.4 — Cấu hình Firewall (UFW)

```bash
# Cài UFW nếu chưa có
apt install ufw -y

# Cho phép SSH (quan trọng — làm trước khi enable, không thì bị lock out)
ufw allow ssh
ufw allow 22/tcp

# Cho phép HTTP (Cloudflare proxy → VPS)
ufw allow 80/tcp

# KHÔNG mở 443 nếu dùng Cloudflare Flexible SSL (CF nhận 443, forward HTTP về VPS)
# Chỉ mở 443 nếu cài Let's Encrypt trực tiếp trên VPS

# Kích hoạt
ufw enable
# Gõ "y" khi được hỏi

# Kiểm tra
ufw status verbose
```

> **Lưu ý quan trọng:** Docker tự thêm rule vào iptables và **bypass UFW**. Vì vậy project đã cấu hình sẵn các port nội bộ (DB :1433, Redis :6379, Backend :8080) bind vào `127.0.0.1` trong `docker-compose.yml` — không bị public dù UFW không chặn được Docker. Chỉ Nginx (port 80) mới được expose ra ngoài.

### Bước 1.5 — Cài các tool cần thiết

```bash
apt install git curl nano -y
```

---

## Phần 2: Deploy Project

### Bước 2.1 — Clone repository

```bash
cd /opt
git clone https://github.com/hung02042005/Japanese-Skill-Practice-Platform.git
cd Japanese-Skill-Practice-Platform
```

> Nếu repo private, cần cài SSH key trên server hoặc dùng GitHub Personal Access Token.

### Bước 2.2 — Tạo file .env gốc

```bash
cp .env.example .env
nano .env
```

Điền vào:

```env
MSSQL_SA_PASSWORD=JlptProd@2026!Secure
```

> Mật khẩu phải: ≥12 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt. Dùng mật khẩu mạnh hơn example ở đây.

### Bước 2.3 — Tạo file .env backend

```bash
cp apps/backend/.env.example apps/backend/.env
nano apps/backend/.env
```

Điền đầy đủ:

```env
# Database — dùng tên service "db" (docker internal DNS)
DATABASE_URL=jdbc:sqlserver://db:1433;databaseName=JLPT_LearningDB;encrypt=true;trustServerCertificate=true
DATABASE_USERNAME=sa
DATABASE_PASSWORD=JlptProd@2026!Secure

# JWT Secret — tạo ngẫu nhiên
JWT_SECRET=<chuoi_ngau_nhien_64_ky_tu>

# Email SMTP
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=email_nhom@gmail.com
SMTP_PASSWORD=<gmail_app_password>

# Google OAuth
GOOGLE_CLIENT_ID=<id>.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-<secret>
```

**Tạo JWT_SECRET ngẫu nhiên:**

```bash
openssl rand -base64 64 | tr -d '\n'
# Copy kết quả vào JWT_SECRET
```

### Bước 2.4 — Cập nhật CORS cho domain production

Mở file cấu hình backend:

```bash
nano apps/backend/src/main/resources/application.yml
```

Sửa phần `app`:

```yaml
app:
  frontend-url: https://sakuji.online
  cors:
    allowed-origins: https://sakuji.online
```

### Bước 2.5 — Khởi chạy toàn bộ hệ thống

```bash
docker compose up -d --build
```

Lần đầu mất **10–20 phút** (tải image Java, Node, SQL Server ~1.5 GB, build code). Các lần sau chỉ mất 2–5 phút nếu code không đổi nhiều.

Theo dõi tiến trình:

```bash
docker compose logs -f
# Nhấn Ctrl+C để thoát xem log, hệ thống vẫn chạy nền
```

---

## Phần 3: Kiểm Tra Hệ Thống

### Bước 3.1 — Kiểm tra container

```bash
docker compose ps
```

Kết quả mong đợi:

```
NAME            STATUS
jlpt-db         Up (healthy)
jlpt-redis      Up
jlpt-backend    Up
jlpt-frontend   Up
```

> Nếu `jlpt-db` vẫn `health: starting` sau 2 phút → SQL Server đang khởi động, bình thường với lần đầu. Chờ thêm.

### Bước 3.2 — Kiểm tra backend health

```bash
curl http://localhost:8080/actuator/health
# Mong đợi: {"status":"UP"}
```

Nếu backend chưa lên:

```bash
docker compose logs backend --tail=50
# Xem lỗi cụ thể — thường là lỗi kết nối DB hoặc sai .env
```

### Bước 3.3 — Kiểm tra frontend

```bash
curl -I http://localhost
# Phải trả về: HTTP/1.1 200 OK
```

### Bước 3.4 — Kiểm tra từ internet

Chưa cấu hình Cloudflare DNS nên chưa vào được qua `sakuji.online`. Kiểm tra tạm bằng IP:

```bash
# Từ máy tính cá nhân của bạn:
curl -I http://<IP-VPS>
# Phải trả về: HTTP/1.1 200 OK
```

Bước tiếp theo: [C-cloudflare-dns-ssl.md](C-cloudflare-dns-ssl.md) — trỏ `sakuji.online` về VPS.

---

## Phần 4: Quy Trình Cập Nhật Code

Mỗi khi có code mới cần deploy lên production:

```bash
# 1. SSH vào VPS
ssh root@<IP-VPS>

# 2. Vào thư mục project
cd /opt/Japanese-Skill-Practice-Platform

# 3. Backup database trước (an toàn)
docker exec jlpt-db /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C \
  -Q "BACKUP DATABASE JLPT_LearningDB TO DISK = N'/var/opt/mssql/backup/pre_deploy_$(date +%Y%m%d_%H%M%S).bak'"

# 4. Pull code mới
git pull origin branch_for_hung

# 5. Rebuild và restart (chỉ restart service bị thay đổi, DB data không mất)
docker compose up -d --build

# 6. Kiểm tra
docker compose ps
curl http://localhost:8080/actuator/health
```

---

## Xử Lý Sự Cố

### Backend không start — lỗi kết nối DB

```bash
docker compose logs backend | grep -i "error\|exception"
```

Nguyên nhân thường gặp:
- `DATABASE_PASSWORD` trong `.env` không khớp với `MSSQL_SA_PASSWORD`
- SQL Server chưa boot xong → restart backend: `docker compose restart backend`
- `DATABASE_URL` dùng `localhost` thay vì `db` (tên service docker-compose)

### Hết dung lượng disk

```bash
df -h
# Xem disk usage

# Xóa Docker images cũ không dùng (KHÔNG xóa volumes — sẽ mất data)
docker image prune -f
docker builder prune -f
```

### Quên mật khẩu VPS

Vào Dashboard của provider → **Reset password** hoặc **Access Console** → đặt lại mật khẩu.

### VPS hết RAM (OOM Killer giết SQL Server)

```bash
# Xem RAM đang dùng
free -h

# Xem process nào dùng nhiều RAM
docker stats --no-stream
```

Nếu SQL Server bị kill:

```bash
docker compose restart db
# Đợi 60 giây
docker compose restart backend
```

Giải pháp lâu dài: nâng cấp lên gói RAM cao hơn, hoặc thêm swap:

```bash
# Tạo 2GB swap (giải pháp tạm)
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```
