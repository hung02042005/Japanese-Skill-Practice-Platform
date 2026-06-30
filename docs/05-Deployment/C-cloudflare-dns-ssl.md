# Cấu Hình Cloudflare DNS + SSL cho sakuji.online

> **Áp dụng cho:** Approach B (VPS thật). Sau khi hoàn thành [B-deploy-vps.md](B-deploy-vps.md), làm bước này để trỏ `sakuji.online` về VPS và bật HTTPS tự động.
>
> **Thời gian:** ~15 phút thực hiện + chờ DNS lan truyền (5–30 phút).

---

## Tổng Quan

Cloudflare đóng 2 vai trò:

1. **DNS** — dịch `sakuji.online` → IP của VPS
2. **Proxy + SSL** — nhận HTTPS từ trình duyệt, tự lo chứng chỉ SSL miễn phí, forward HTTP sang VPS

VPS không cần cài Certbot hay Let's Encrypt. Cloudflare lo hết phần SSL.

```
Trình duyệt ──HTTPS──► Cloudflare ──HTTP──► VPS:80 (Nginx)
              (SSL tại CF)         (mạng nội bộ CF→VPS)
```

---

## Phần 1: Xóa Cấu Hình Tunnel Cũ (Nếu Đang Dùng Approach A)

Nếu trước đây đã dùng Cloudflare Tunnel (Approach A), cần xóa CNAME record tunnel cũ trước.

### Bước 1.1 — Xóa CNAME tunnel trên Cloudflare Dashboard

1. Đăng nhập [dash.cloudflare.com](https://dash.cloudflare.com)
2. Chọn domain **sakuji.online**
3. Vào tab **DNS** → **Records**
4. Tìm record dạng:

   ```
   Type: CNAME
   Name: sakuji.online (hoặc @)
   Content: <UUID>.cfargotunnel.com
   ```

5. Nhấn **Edit** → **Delete** → xác nhận xóa

### Bước 1.2 — Vô hiệu hóa tunnel (tùy chọn)

Nếu muốn giữ tunnel cũ nhưng tắt đi:

1. Vào **Cloudflare Zero Trust** → **Networks** → **Tunnels**
2. Tìm tunnel `jlpt-tunnel` → nhấn menu (3 chấm) → **Disable**

---

## Phần 2: Tạo A Record Trỏ Về VPS

### Bước 2.1 — Thêm A Record

1. Đăng nhập [dash.cloudflare.com](https://dash.cloudflare.com) → chọn **sakuji.online**
2. Vào tab **DNS** → **Records** → nhấn **Add record**

Thêm lần lượt 2 record sau:

**Record 1 — domain gốc:**

| Trường | Giá trị |
|---|---|
| Type | `A` |
| Name | `@` (đại diện cho `sakuji.online`) |
| IPv4 address | `<IP-VPS>` (thay bằng IP thật của VPS) |
| Proxy status | **Proxied** (biểu tượng đám mây màu cam) |
| TTL | Auto |

**Record 2 — www subdomain:**

| Trường | Giá trị |
|---|---|
| Type | `A` |
| Name | `www` |
| IPv4 address | `<IP-VPS>` |
| Proxy status | **Proxied** (màu cam) |
| TTL | Auto |

> **Bắt buộc bật Proxied (màu cam)** cho cả 2 record. Đây là điều quan trọng nhất:
> - Màu cam: Cloudflare làm proxy → lo SSL, ẩn IP thật VPS, bật WAF
> - Màu xám: Trỏ thẳng về VPS, bypass Cloudflare → phải tự lo SSL

### Bước 2.2 — Kiểm tra DNS đã lan truyền

Sau khi thêm record, chờ 5–30 phút rồi kiểm tra:

```bash
# Từ máy tính cá nhân hoặc VPS
nslookup sakuji.online
# Phải trả về IP của Cloudflare (không phải IP VPS thật — vì đang qua proxy)

# Hoặc kiểm tra online:
# https://dnschecker.org → nhập sakuji.online → kiểm tra toàn cầu
```

> IP trả về sẽ là IP của Cloudflare (dạng `104.x.x.x` hoặc `172.x.x.x`), không phải IP VPS. Đây là đúng — Cloudflare đang ẩn IP thật.

---

## Phần 3: Cấu Hình SSL/TLS

### Bước 3.1 — Chọn SSL Mode

1. Trên Cloudflare Dashboard → **sakuji.online** → tab **SSL/TLS** → **Overview**
2. Chọn mode **Flexible**

**Giải thích các mode:**

| Mode | Trình duyệt → Cloudflare | Cloudflare → VPS | Khi nào dùng |
|---|---|---|---|
| **Flexible** | HTTPS (mã hóa) | HTTP (không mã hóa) | VPS chỉ có HTTP — trường hợp của mình |
| Full | HTTPS | HTTPS (chấp nhận self-signed) | VPS có SSL tự ký |
| Full (Strict) | HTTPS | HTTPS (phải cert hợp lệ) | VPS dùng Let's Encrypt |

Chọn **Flexible** vì VPS của chúng ta chỉ expose port 80 (HTTP) — Cloudflare lo phần HTTPS với người dùng.

### Bước 3.2 — Bật Always Use HTTPS

Vào **SSL/TLS** → **Edge Certificates** → bật:

- **Always Use HTTPS**: ON (tự động redirect HTTP → HTTPS)
- **Minimum TLS Version**: TLS 1.2
- **Opportunistic Encryption**: ON

### Bước 3.3 — Bật HTTP Strict Transport Security (HSTS)

Vào **SSL/TLS** → **Edge Certificates** → **HTTP Strict Transport Security (HSTS)**:

Nhấn **Enable HSTS** → cấu hình:

| Setting | Giá trị |
|---|---|
| Status | Enabled |
| Max Age | 6 months (15768000) |
| Include subdomains | Bật |
| Preload | Không bật (có thể bật sau khi test ổn) |

---

## Phần 4: Cấu Hình Bảo Mật Cloudflare

### Bước 4.1 — Firewall Rules cơ bản

Vào **Security** → **WAF** → **Custom rules** → **Create rule**:

**Rule 1: Block SQL injection và XSS phổ biến**

```
Rule name: Block common attacks
Expression: (cf.threat_score gt 30)
Action: Block
```

**Rule 2: Rate limit login endpoint**

Vào **Security** → **WAF** → **Rate limiting rules** → **Create rule**:

```
Rule name: Rate limit login
Expression: (http.request.uri.path eq "/api/auth/login")
Characteristics: IP Address
Period: 1 minute
Requests: 10
Action: Block
Duration: 10 minutes
```

### Bước 4.2 — Bật Bot Fight Mode

Vào **Security** → **Bots** → bật **Bot Fight Mode** (miễn phí).

### Bước 4.3 — Cài đặt Cache

Vào **Caching** → **Configuration**:

- **Caching Level**: Standard
- **Browser Cache TTL**: 4 hours

Vào **Rules** → **Page Rules** → **Create Page Rule**:

```
URL: sakuji.online/api/*
Settings: Cache Level = Bypass
```

(Không cache API response — chỉ cache static files của React)

---

## Phần 5: Kiểm Tra Toàn Bộ

Sau khi cấu hình xong, kiểm tra từng mục:

```bash
# 1. HTTPS hoạt động
curl -I https://sakuji.online
# Mong đợi: HTTP/2 200

# 2. HTTP tự redirect HTTPS
curl -I http://sakuji.online
# Mong đợi: HTTP/1.1 301 + Location: https://sakuji.online/

# 3. www redirect về domain gốc
curl -I https://www.sakuji.online
# Mong đợi: 301 + Location: https://sakuji.online/

# 4. Backend phản hồi qua domain
curl https://sakuji.online/actuator/health
# Mong đợi: {"status":"UP"}

# 5. Port DB và Redis KHÔNG bị public (chạy từ máy khác)
curl -m 5 http://sakuji.online:1433 ; echo "Exit: $?"
curl -m 5 http://sakuji.online:6379 ; echo "Exit: $?"
# Mong đợi: Connection timeout / refused — KHÔNG kết nối được
```

**Kiểm tra SSL bằng công cụ online:**

- [ssllabs.com/ssltest](https://www.ssllabs.com/ssltest/) — nhập `sakuji.online` → phải đạt **Grade A**

---

## Cấu Hình Google OAuth Cho Domain Mới

Sau khi `sakuji.online` đã hoạt động qua HTTPS, cập nhật Google OAuth:

1. Vào [console.cloud.google.com](https://console.cloud.google.com)
2. **APIs & Services** → **Credentials** → chọn OAuth 2.0 Client ID
3. Thêm vào **Authorized JavaScript origins:**

   ```
   https://sakuji.online
   ```

4. Thêm vào **Authorized redirect URIs:**

   ```
   https://sakuji.online/api/auth/google/callback
   ```

   (Điều chỉnh path theo route thực tế của project)

5. Nhấn **Save** — đợi 5 phút có hiệu lực

---

## Xử Lý Sự Cố

### sakuji.online vẫn vào không được sau 30 phút

Kiểm tra:

```bash
# Từ VPS — xem Nginx có đang chạy không
docker compose ps
curl http://localhost
```

Nếu VPS OK nhưng domain không vào được:

```bash
# Kiểm tra DNS đã cập nhật chưa
dig sakuji.online A
# Phải trả về IP của Cloudflare (104.x.x.x), không phải IP VPS
```

Nếu dig vẫn trả về IP cũ → chưa lan truyền, chờ thêm. DNS có thể mất đến 24 giờ ở một số ISP.

### Lỗi 521 (Web server is down)

Cloudflare kết nối được đến VPS nhưng VPS không phản hồi.

```bash
# SSH vào VPS
docker compose ps
docker compose logs frontend --tail=20
```

Nếu Nginx container bị tắt:

```bash
docker compose start frontend
```

### Lỗi 522 (Connection timed out)

Cloudflare không kết nối được đến VPS.

Kiểm tra:
1. VPS còn chạy không (ping IP VPS từ máy tính)
2. UFW cho phép port 80: `ufw status`
3. Nginx container đang Up: `docker compose ps`

### Lỗi SSL (ERR_SSL_PROTOCOL_ERROR)

SSL Mode sai. Vào Cloudflare → SSL/TLS → đảm bảo đang chọn **Flexible** (không phải Off hoặc Full).
