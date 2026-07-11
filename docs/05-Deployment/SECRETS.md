# Danh Mục Secrets — JLPT Learning Platform

> **File này KHÔNG chứa giá trị thật của bất kỳ secret nào.** Mục đích duy nhất: biết
> **secret nào tồn tại, nằm ở đâu, ai được phép đổi, và lần đổi gần nhất là khi nào/vì
> sao** — để không lặp lại nguyên nhân gốc của Sự cố 4 (11/07/2026): `.env` trên VPS bị
> đổi `MSSQL_SA_PASSWORD` lúc 23:46 mà không ai biết, không có audit trail, không có
> thông báo. Xem [`Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md`](./Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md).

**Quy tắc bắt buộc (P1.6):**

> ⚠️ **Bất kỳ ai đổi một secret trong bảng dưới đây — trên GitHub Secrets HAY trên VPS —
> đều phải báo trong group chat chung của team TRƯỚC KHI đổi**, kèm lý do, và cập nhật
> lại cột "Lần đổi gần nhất" trong file này ngay sau khi đổi xong. Không cần công cụ
> phức tạp (Vault/Key Vault) — quy tắc giao tiếp đơn giản này đã đủ ngăn chính xác loại
> sự cố đã xảy ra.

---

## 1. GitHub Repository Secrets

Cấu hình tại `Settings → Secrets and variables → Actions` trên GitHub, dùng trong
`.github/workflows/cd.yml` và `rollback.yml`.

| Secret | Dùng ở đâu | Ai có quyền đổi | Lần đổi gần nhất |
|---|---|---|---|
| `VPS_HOST` | SSH vào VPS (`cd.yml`, `rollback.yml`) | Chủ repo (Admin) | 2026-06 — khởi tạo ban đầu |
| `VPS_USERNAME` | SSH vào VPS | Chủ repo (Admin) | 2026-06 — khởi tạo ban đầu |
| `VPS_PASSWORD` | SSH vào VPS — **phương thức đang hoạt động thật** (xem P1.7, mục tiêu dài hạn là bỏ hẳn secret này) | Chủ repo (Admin) | 2026-07-11 — đồng bộ lại sau Sự cố 4 (mật khẩu VPS user `jlptadmin`, không liên quan `MSSQL_SA_PASSWORD`) |
| `VPS_SSH_KEY` | SSH vào VPS (khai báo song song với password, **chưa từng được cấu hình thật** — xem `CI_CD.md`) | Chủ repo (Admin) | Chưa từng thiết lập |
| `PROJECT_PATH` | Đường dẫn thư mục project trên VPS mà `cd.yml` `cd` vào | Chủ repo (Admin) | 2026-06 — khởi tạo ban đầu |
| `CLOUDFLARE_ZONE_ID` | Purge cache Cloudflare sau deploy | Chủ repo (Admin) | 2026-06 — khởi tạo ban đầu |
| `CLOUDFLARE_API_TOKEN` | Purge cache Cloudflare sau deploy | Chủ repo (Admin) | 2026-06 — khởi tạo ban đầu |

---

## 2. Root `.env` trên VPS (`${PROJECT_PATH}/.env`)

Đọc bởi `docker-compose.yml` + `docker-compose.prod.yml` qua cú pháp `${VAR}` khi
`docker compose up`. **Không nằm trong git** (đã `.gitignore`) — chỉ tồn tại trên VPS.
Mẫu đầy đủ các biến (không có giá trị thật): [`.env.example`](../../.env.example).

| Biến | Dùng cho | Ai có quyền đổi | Lần đổi gần nhất |
|---|---|---|---|
| `MSSQL_SA_PASSWORD` | Mật khẩu `sa` của SQL Server — dùng bởi cả container `db` (khởi tạo) và `backend` (kết nối) | jlptadmin (VPS) | 2026-07-11 — khôi phục sau Sự cố 4 (đồng bộ lại khớp dữ liệu volume thật, xem chi tiết trong Incident Report) |
| `JWT_SECRET` | Ký JWT access/refresh token | jlptadmin (VPS) | 2026-06 — khởi tạo ban đầu |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth2 đăng nhập | jlptadmin (VPS) | 2026-06 — khởi tạo ban đầu |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | Gửi email xác minh / đặt lại mật khẩu | jlptadmin (VPS) | 2026-07-11 — sửa thành `${VAR:-default}` sau Sự cố 3 (SMTP_PORT rỗng gây crash-loop) |

> ⚠️ **Lưu ý quan trọng (đã ghi trong `Deploy_Diagram.md` mục 7):** 4 biến `SMTP_*` ở
> trên chỉ là **giá trị khởi tạo ban đầu**. `AdminSettingsService` đọc cấu hình SMTP
> thật từ bảng `system_settings` trong database, cho phép đổi qua trang quản trị mà
> **không cần deploy lại code và không đi qua file này**. Khi debug lỗi email, luôn
> kiểm tra cả 2 nơi.

---

## 3. `apps/backend/.env` trên VPS (tuỳ chọn, `required: false` trong `docker-compose.yml`)

Chỉ cần nếu muốn cấu hình các biến sau **khác** với giá trị đã inject qua
`docker-compose.prod.yml` ở mục 2. Hiện tại thư mục này **không tồn tại trên VPS** —
mọi cấu hình production đi qua root `.env`. Mẫu: [`apps/backend/.env.example`](../../apps/backend/.env.example).

| Biến | Ghi chú |
|---|---|
| `AI_OCR_API_KEY` / `AI_SPEECH_API_KEY` | Chưa dùng trong production — các module AI OCR/Speech chưa tích hợp provider thật |
| `PAYMENT_GATEWAY_KEY` | Chưa dùng — chưa tích hợp thanh toán thật |

---

## 4. Quy trình khi thêm secret mới

1. Thêm dòng mới vào bảng phù hợp (mục 1/2/3) ở file này **cùng lúc** với commit thêm
   biến đó vào code (`docker-compose*.yml`, `application.yml`, workflow) — không thêm
   secret vào hạ tầng thật rồi quên cập nhật tài liệu.
2. Nếu là secret dùng trên VPS: cập nhật `.env.example` tương ứng (giá trị mẫu, không
   phải giá trị thật) để người sau biết cần khai báo biến gì khi setup VPS mới.
3. Báo trong group chat chung trước khi set giá trị thật lên GitHub Secrets hoặc `.env`
   VPS, theo đúng quy tắc ở đầu file.
