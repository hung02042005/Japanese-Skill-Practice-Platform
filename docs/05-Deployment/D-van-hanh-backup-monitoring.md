# Vận Hành, Backup và Monitoring

> **Đọc sau khi** đã deploy xong (Approach A hoặc B). Tài liệu này hướng dẫn cách giữ hệ thống ổn định, backup dữ liệu, và biết ngay khi website có vấn đề.

---

## Phần 1: Monitoring — Biết Ngay Khi Website Sập

### Bước 1.1 — Cài UptimeRobot (miễn phí)

UptimeRobot kiểm tra website mỗi 5 phút và gửi email/SMS ngay khi sập.

1. Đăng ký tại [uptimerobot.com](https://uptimerobot.com) — tài khoản Free đủ dùng
2. Nhấn **+ Add New Monitor**
3. Cấu hình:

   | Trường | Giá trị |
   |---|---|
   | Monitor Type | **HTTP(s)** |
   | Friendly Name | JLPT sakuji.online |
   | URL (or IP) | `https://sakuji.online/actuator/health` |
   | Monitoring Interval | **5 minutes** |
   | Alert Contacts | Email của cả nhóm |

4. Nhấn **Create Monitor**

Kết quả: Khi website sập quá 5 phút → toàn bộ nhóm nhận email cảnh báo. Khi khôi phục → nhận email thông báo online trở lại.

### Bước 1.2 — Dashboard trạng thái (tùy chọn)

UptimeRobot có thể tạo **Status Page** công khai. Hữu ích khi muốn người dùng tự kiểm tra trạng thái:

1. Trên UptimeRobot → **Status Pages** → **Create Status Page**
2. Thêm monitor `JLPT sakuji.online` vào
3. Được URL dạng: `https://stats.uptimerobot.com/xxxx` — chia sẻ cho người dùng

---

## Phần 2: Backup Database Tự Động

### Bước 2.1 — Tạo thư mục backup trong container

Làm bước này **một lần** khi mới setup:

```bash
# Tạo thư mục backup bên trong SQL Server container
docker exec jlpt-db mkdir -p /var/opt/mssql/backup
```

### Bước 2.2 — Test backup thủ công

```bash
# Đọc password từ .env (chạy tại thư mục project)
source .env

# Chạy backup
docker exec jlpt-db /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C \
  -Q "BACKUP DATABASE JLPT_LearningDB TO DISK = N'/var/opt/mssql/backup/manual_$(date +%Y%m%d_%H%M%S).bak' WITH FORMAT"

# Kiểm tra file backup đã tạo
docker exec jlpt-db ls -lh /var/opt/mssql/backup/
```

Nếu thấy file `.bak` → backup hoạt động đúng.

### Bước 2.3 — Cài cron backup tự động hằng ngày (chỉ cho VPS)

```bash
crontab -e
# Chọn editor (nano nếu được hỏi)
```

Thêm vào cuối file (thay `/opt/Japanese-Skill-Practice-Platform` bằng đường dẫn project thật):

```cron
# Backup DB lúc 2:00 sáng mỗi ngày
0 2 * * * cd /opt/Japanese-Skill-Practice-Platform && source .env && docker exec jlpt-db /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "BACKUP DATABASE JLPT_LearningDB TO DISK = N'/var/opt/mssql/backup/daily_$(date +\%Y\%m\%d).bak' WITH FORMAT" >> /var/log/jlpt-backup.log 2>&1

# Xóa backup cũ hơn 7 ngày để tránh đầy disk
0 3 * * * docker exec jlpt-db find /var/opt/mssql/backup -name "daily_*.bak" -mtime +7 -delete
```

Lưu và thoát. Kiểm tra cron đã được thêm:

```bash
crontab -l
```

### Bước 2.4 — Sao lưu backup ra ngoài VPS (khuyến nghị)

Backup nằm trong volume Docker — nếu VPS bị xóa hoặc crash, backup cũng mất. Nên copy ra ngoài định kỳ:

**Option A: Copy về máy tính cá nhân**

```bash
# Chạy từ máy tính cá nhân (không phải VPS)
# Copy toàn bộ thư mục backup từ VPS về local
docker exec jlpt-db tar czf - /var/opt/mssql/backup/ | \
  ssh root@<IP-VPS> "cat" > ~/jlpt-backup-$(date +%Y%m%d).tar.gz
```

**Option B: Dùng rclone đẩy lên Google Drive (tự động)**

```bash
# Cài rclone trên VPS
curl https://rclone.org/install.sh | bash

# Cấu hình Google Drive
rclone config
# Làm theo hướng dẫn để thêm remote "gdrive"

# Thêm vào crontab, chạy sau job backup:
30 2 * * * docker exec jlpt-db tar czf - /var/opt/mssql/backup/ | rclone rcat gdrive:jlpt-backups/daily_$(date +\%Y\%m\%d).tar.gz
```

---

## Phần 3: Restore Database Khi Cần

### Restore từ file backup

```bash
# Liệt kê các file backup có sẵn
docker exec jlpt-db ls -lh /var/opt/mssql/backup/

# Restore từ file backup cụ thể (thay tên file cho đúng)
source .env

docker exec jlpt-db /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C \
  -Q "RESTORE DATABASE JLPT_LearningDB FROM DISK = N'/var/opt/mssql/backup/daily_20260630.bak' WITH REPLACE"
```

> **Lưu ý:** RESTORE sẽ ghi đè database hiện tại. Nên backup lại lần nữa trước khi restore nếu còn dữ liệu muốn giữ.

---

## Phần 4: Quy Trình Cập Nhật Code

### Update thông thường (không thay đổi schema DB)

```bash
# 1. SSH vào VPS (hoặc mở PowerShell nếu dùng Approach A)
cd /opt/Japanese-Skill-Practice-Platform   # VPS
# hoặc
cd "C:\Users\Tien Dat\...\Japanese-Skill-Practice-Platform"   # Windows

# 2. Pull code mới
git pull origin branch_for_hung

# 3. Rebuild và restart (DB data không mất)
docker compose up -d --build

# 4. Kiểm tra
docker compose ps
curl http://localhost:8080/actuator/health
```

### Update có migration schema DB

Flyway tự động chạy migration khi backend start. Quy trình:

```bash
# 1. Backup trước khi migrate
source .env
docker exec jlpt-db /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C \
  -Q "BACKUP DATABASE JLPT_LearningDB TO DISK = N'/var/opt/mssql/backup/pre_migration_$(date +%Y%m%d_%H%M%S).bak' WITH FORMAT"

# 2. Pull và rebuild
git pull origin branch_for_hung
docker compose up -d --build

# 3. Xem log Flyway migration
docker compose logs backend | grep -i "flyway\|migration"
# Phải thấy: "Successfully applied X migration(s)"
```

---

## Phần 5: Cheatsheet Lệnh Hay Dùng

### Quản lý container

```bash
# Xem trạng thái tất cả container
docker compose ps

# Xem log realtime tất cả
docker compose logs -f

# Xem log chỉ backend
docker compose logs -f backend

# Xem log chỉ DB
docker compose logs -f db

# Restart một service cụ thể (không ảnh hưởng service khác)
docker compose restart backend
docker compose restart frontend

# Tắt toàn bộ (data vẫn được giữ trong volume)
docker compose down

# Khởi động lại toàn bộ
docker compose up -d

# Rebuild và restart (sau khi pull code mới)
docker compose up -d --build
```

### Kiểm tra tài nguyên

```bash
# Xem CPU/RAM từng container theo realtime
docker stats

# Xem disk usage
df -h

# Xem dung lượng Docker đang chiếm
docker system df
```

### Dọn dẹp disk khi đầy

```bash
# Xóa image cũ không dùng (KHÔNG ảnh hưởng data)
docker image prune -f

# Xóa build cache
docker builder prune -f

# Xóa container đã tắt
docker container prune -f

# KHÔNG chạy: docker volume prune — sẽ xóa mất database và uploads
```

### Truy cập trực tiếp vào container

```bash
# Vào shell của backend (debug)
docker exec -it jlpt-backend /bin/sh

# Chạy câu lệnh SQL trực tiếp
source .env
docker exec -it jlpt-db /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C \
  -Q "SELECT name FROM sys.databases"

# Vào Redis CLI
docker exec -it jlpt-redis redis-cli
```

---

## Phần 6: Checklist Sau Mỗi Lần Update Code

Sau khi `docker compose up -d --build`, chạy checklist này để xác nhận không có regression:

```bash
# Health check backend
curl https://sakuji.online/actuator/health
# ✅ {"status":"UP"}

# Frontend load được
curl -I https://sakuji.online
# ✅ HTTP/2 200

# Không có container nào bị Exit
docker compose ps
# ✅ Tất cả ở trạng thái "Up"
```

Kiểm tra thủ công trên trình duyệt:

- [ ] Đăng nhập tài khoản thường → vào dashboard được
- [ ] Xem danh sách khóa học → hiển thị đúng
- [ ] Làm quiz 1 câu → nộp → nhận điểm
- [ ] Đăng nhập Staff → vào được trang quản lý
- [ ] Đăng nhập Admin → vào được trang admin

---

## Phần 7: Xử Lý Sự Cố Khẩn Cấp

### Website sập hoàn toàn (UptimeRobot báo DOWN)

```bash
# 1. SSH vào VPS ngay
ssh root@<IP-VPS>

# 2. Kiểm tra container
docker compose ps
# Xem container nào Exit

# 3. Xem log container bị sập
docker compose logs <tên-container> --tail=50

# 4. Thử restart
docker compose restart <tên-container>

# 5. Nếu không được, restart toàn bộ
docker compose down && docker compose up -d
```

### Mất toàn bộ data (volume bị xóa nhầm)

Đây là tình huống nghiêm trọng nhất. Chỉ có thể restore từ backup:

```bash
# 1. Tạo lại volume (tự tạo khi docker compose up)
docker compose up -d db

# 2. Đợi SQL Server khởi động
sleep 60

# 3. Copy file backup vào container nếu backup nằm ngoài
docker cp /path/to/backup.bak jlpt-db:/var/opt/mssql/backup/

# 4. Restore
source .env
docker exec jlpt-db /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C \
  -Q "RESTORE DATABASE JLPT_LearningDB FROM DISK = N'/var/opt/mssql/backup/backup.bak' WITH REPLACE"

# 5. Start lại backend
docker compose up -d backend
```

### VPS hết credit / bị tắt

Nếu dùng Azure for Students và hết $100 credit:

1. Xuất backup database ngay trước khi bị xóa
2. Chuyển sang provider khác (Contabo $7/tháng)
3. Tạo VPS mới, làm lại [B-deploy-vps.md](B-deploy-vps.md)
4. Copy backup vào và restore

> **Phòng ngừa:** UptimeRobot sẽ báo khi website sập. Azure cũng gửi email cảnh báo trước khi hết credit. Luôn để ý email của tài khoản Azure.
