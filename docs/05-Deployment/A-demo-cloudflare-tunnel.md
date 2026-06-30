# Approach A — Demo Đồ Án với Cloudflare Tunnel

> **Dùng khi:** Muốn đưa website lên `https://sakuji.online` ngay hôm nay, không tốn tiền, chấp nhận website chỉ online khi máy tính đang bật.
>
> **Không dùng khi:** Cần website chạy 24/7 liên tục khi tắt máy → xem [B-deploy-vps.md](B-deploy-vps.md).

---

## Cách Hoạt Động

```
Người dùng
    │ https://sakuji.online
    ▼
Cloudflare (SSL + WAF)
    │ đường hầm mã hóa (outbound)
    ▼
cloudflared daemon (chạy trên máy bạn)
    │ http://localhost:80
    ▼
Docker Compose (Nginx → Spring Boot → SQL Server + Redis)
```

Cloudflared tạo kết nối **từ trong ra ngoài** đến Cloudflare — không cần mở port trên router/modem, không cần IP tĩnh.

---

## Yêu Cầu Máy Tính

| Yêu cầu | Tối thiểu |
|---|---|
| RAM | 8 GB (SQL Server chiếm ~2.5 GB) |
| Disk trống | 15 GB (Docker images + data) |
| OS | Windows 10/11 (64-bit) |
| Docker Desktop | Đã cài, đang chạy |
| cloudflared | Đã cài (hướng dẫn bên dưới) |
| Tunnel đã tạo | `jlpt-tunnel` trên Cloudflare Zero Trust |

---

## Phần 1: Cài Đặt Một Lần (Chỉ làm lần đầu)

### Bước 1.1 — Cài cloudflared

Tải bản mới nhất từ [github.com/cloudflare/cloudflared/releases](https://github.com/cloudflare/cloudflared/releases), chọn file `cloudflared-windows-amd64.msi`, cài đặt bình thường.

Kiểm tra:

```powershell
cloudflared --version
# Phải ra: cloudflared version 202x.x.x
```

### Bước 1.2 — Đăng nhập Cloudflare

```powershell
cloudflared tunnel login
# Trình duyệt mở ra → đăng nhập tài khoản Cloudflare → chọn domain sakuji.online → Authorize
```

Sau khi xong, file `cert.pem` được lưu tự động vào `C:\Users\<tên>\AppData\Roaming\cloudflared\`.

### Bước 1.3 — Tạo tunnel có tên

```powershell
cloudflared tunnel create jlpt-tunnel
```

Lệnh này tạo tunnel `jlpt-tunnel` và sinh ra file credentials (dạng UUID `.json`) trong thư mục cloudflared. Ghi lại UUID hiển thị trên màn hình (dạng `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).

### Bước 1.4 — Tạo file cấu hình tunnel

Tạo file `config.yml` tại `C:\Users\<tên>\.cloudflared\config.yml`:

```yaml
tunnel: jlpt-tunnel
credentials-file: C:\Users\<tên>\.cloudflared\<UUID>.json

ingress:
  - hostname: sakuji.online
    service: http://localhost:80
  - service: http_status:404
```

Thay `<tên>` bằng tên user Windows của bạn (ví dụ: `Tien Dat`), thay `<UUID>` bằng UUID từ Bước 1.3.

### Bước 1.5 — Trỏ DNS Cloudflare về tunnel

```powershell
cloudflared tunnel route dns jlpt-tunnel sakuji.online
```

Lệnh này tự động tạo CNAME record trên Cloudflare DNS trỏ `sakuji.online` về tunnel. Không cần làm gì thêm trên Cloudflare Dashboard.

### Bước 1.6 — Cài cloudflared thành Windows Service (quan trọng nhất)

Bước này giúp tunnel **tự động chạy khi bật máy**, không cần mở PowerShell thủ công mỗi lần.

Mở PowerShell với quyền **Administrator**:

```powershell
cloudflared service install
```

Kiểm tra service đã cài:

```powershell
Get-Service cloudflared
# Phải thấy: Status = Running (hoặc Stopped nếu chưa start lần này)

# Start ngay nếu đang Stopped:
Start-Service cloudflared
```

### Bước 1.7 — Bật Docker Desktop tự chạy khi boot

Mở **Docker Desktop** → Settings (bánh răng góc trên phải) → tab **General** → bật:

- ✅ **Start Docker Desktop when you sign in to your computer**

---

## Phần 2: Cấu Hình Project (Chỉ làm lần đầu)

### Bước 2.1 — Tạo file .env gốc

```powershell
cd "C:\Users\Tien Dat\OneDrive\Documents\GitHub\Japanese-Skill-Practice-Platform"
Copy-Item .env.example .env
```

Mở file `.env` và đặt mật khẩu SQL Server:

```env
MSSQL_SA_PASSWORD=JlptDemo@2026!
```

> Mật khẩu phải có: ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.

### Bước 2.2 — Tạo file .env backend

```powershell
Copy-Item apps\backend\.env.example apps\backend\.env
```

Mở file `apps\backend\.env` và điền:

```env
# Kết nối SQL Server (dùng tên service trong docker-compose)
DATABASE_URL=jdbc:sqlserver://db:1433;databaseName=JLPT_LearningDB;encrypt=true;trustServerCertificate=true
DATABASE_USERNAME=sa
DATABASE_PASSWORD=JlptDemo@2026!

# JWT — tạo chuỗi ngẫu nhiên dài ít nhất 64 ký tự
JWT_SECRET=thay_bang_chuoi_ngau_nhien_dai_64_ky_tu_o_day_khong_dung_cai_nay

# Email (dùng Gmail App Password — xem hướng dẫn bên dưới)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=email_nhom@gmail.com
SMTP_PASSWORD=xxxx_xxxx_xxxx_xxxx

# Google OAuth (lấy từ Google Cloud Console)
GOOGLE_CLIENT_ID=xxxxxxxxxx.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-xxxxxxxxxx
```

> **Tạo JWT_SECRET nhanh:** Mở PowerShell và chạy:
> ```powershell
> -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 64 | ForEach-Object {[char]$_})
> ```

> **Gmail App Password:** Vào myaccount.google.com → Security → 2-Step Verification → App passwords → tạo mật khẩu cho "Mail".

### Bước 2.3 — Cập nhật CORS trong application.yml

Mở `apps\backend\src\main\resources\application.yml`, sửa:

```yaml
app:
  frontend-url: https://sakuji.online
  cors:
    allowed-origins: https://sakuji.online
```

### Bước 2.4 — Cập nhật Google OAuth Authorized Origins

Vào [Google Cloud Console](https://console.cloud.google.com) → APIs & Services → Credentials → chọn OAuth Client ID → thêm vào:

- **Authorized JavaScript origins:** `https://sakuji.online`
- **Authorized redirect URIs:** `https://sakuji.online/api/auth/google/callback` (điều chỉnh theo route thực tế của project)

---

## Phần 3: Chạy Hệ Thống

### Bước 3.1 — Khởi động Docker

Đảm bảo Docker Desktop đang chạy (biểu tượng cá voi xanh ở thanh taskbar).

```powershell
cd "C:\Users\Tien Dat\OneDrive\Documents\GitHub\Japanese-Skill-Practice-Platform"
docker compose up -d --build
```

Lần đầu chạy sẽ mất **5–10 phút** để tải image và build. Các lần sau nhanh hơn nhiều.

### Bước 3.2 — Kiểm tra các container đang chạy

```powershell
docker compose ps
```

Phải thấy đủ 4 container ở trạng thái **Up**:

```
NAME            STATUS
jlpt-db         Up (healthy)
jlpt-redis      Up
jlpt-backend    Up
jlpt-frontend   Up
```

> Nếu `jlpt-db` hiển thị `Up (health: starting)` → đợi thêm 30–60 giây rồi kiểm tra lại. SQL Server khởi động chậm.

### Bước 3.3 — Kiểm tra backend phản hồi

```powershell
curl http://localhost:8080/actuator/health
# Mong đợi: {"status":"UP"}

curl http://localhost
# Mong đợi: trả về HTML trang React
```

### Bước 3.4 — Kiểm tra tunnel đang chạy

```powershell
Get-Service cloudflared
# Phải thấy: Status = Running
```

Nếu service chưa chạy:

```powershell
Start-Service cloudflared
```

### Bước 3.5 — Kiểm tra website công khai

Mở trình duyệt ẩn danh (InPrivate/Incognito) và truy cập `https://sakuji.online`.

Checklist:
- [ ] Trang chủ load được, có ổ khóa xanh HTTPS
- [ ] Đăng ký tài khoản thử → thành công
- [ ] Đăng nhập → vào được dashboard

---

## Phần 4: Vận Hành Hằng Ngày

### Khi mở máy (tự động — không cần làm gì)

Sau khi đã cài service ở Bước 1.6 và Docker auto-start ở Bước 1.7:

1. Bật máy tính
2. Docker Desktop tự khởi động
3. cloudflared service tự chạy
4. Sau ~60–90 giây, `https://sakuji.online` online trở lại

### Khi cần cập nhật code mới

```powershell
cd "C:\Users\Tien Dat\OneDrive\Documents\GitHub\Japanese-Skill-Practice-Platform"
git pull
docker compose up -d --build
```

### Khi cần xem log để debug

```powershell
# Xem log tất cả
docker compose logs -f

# Xem log chỉ backend
docker compose logs -f backend

# Xem log tunnel cloudflare
Get-EventLog -LogName Application -Source cloudflared -Newest 20
```

### Khi cần tắt hoàn toàn

```powershell
docker compose down
Stop-Service cloudflared
```

---

## Xử Lý Sự Cố

### Website không vào được (ERR_CONNECTION_TIMED_OUT)

1. Kiểm tra Docker đang chạy: `docker compose ps` — cả 4 container phải Up
2. Kiểm tra tunnel: `Get-Service cloudflared` — phải Running
3. Kiểm tra mạng: máy tính phải có internet
4. Thử restart tunnel: `Restart-Service cloudflared`

### Lỗi 502 Bad Gateway

Backend chưa khởi động xong. Chạy:

```powershell
docker compose logs backend --tail=50
```

Nếu thấy lỗi kết nối DB → đợi thêm 60 giây để SQL Server boot xong, rồi:

```powershell
docker compose restart backend
```

### Lỗi Database Connection / SQL Server sập

RAM máy đầy. SQL Server bị OOM. Giải phóng RAM:

```powershell
# Đóng Chrome, Zalo, các app không cần thiết
docker compose restart db
# Đợi 30 giây
docker compose restart backend
```

### Lỗi Google OAuth (400: redirect_uri_mismatch)

Vào Google Cloud Console → OAuth Client → kiểm tra `https://sakuji.online` đã có trong Authorized JavaScript origins và Authorized redirect URIs chưa. Đợi 5 phút sau khi thêm rồi thử lại bằng trình duyệt ẩn danh.

### Tunnel không kết nối được sau khi reinstall

Xóa service cũ và cài lại:

```powershell
# Mở PowerShell với quyền Administrator
cloudflared service uninstall
cloudflared service install
Start-Service cloudflared
```

---

## Hạn Chế Của Approach A

| Hạn chế | Mức độ ảnh hưởng |
|---|---|
| Website chết khi tắt máy hoặc mất điện | Cao — không phù hợp production |
| Hiệu năng phụ thuộc RAM máy cá nhân (cạnh tranh với Chrome, Zalo...) | Trung bình |
| Đường truyền tải file chậm nếu mạng nhà yếu | Trung bình |
| Không thể scale khi nhiều người dùng đồng thời | Cao |

Khi đã bảo vệ xong đồ án và muốn duy trì website ổn định → chuyển sang [B-deploy-vps.md](B-deploy-vps.md).
