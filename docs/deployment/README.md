# Hướng Dẫn Triển Khai (Deployment) — JLPT E-Learning

Tài liệu này là **Nguồn chân lý duy nhất (Single Source of Truth)** cho toàn bộ quy trình đưa dự án lên Server thực tế. Chúng ta sử dụng kiến trúc **Docker** để tự động hóa và đồng nhất môi trường, giúp việc triển khai trở nên cực kỳ đơn giản.

*Lưu ý: Các tài liệu cài đặt thủ công cũ (bare-metal) đã được chuyển vào thư mục `legacy/` để tham khảo nếu cần.*

---

## 1. Tổng Quan Kiến Trúc

Hệ thống được đóng gói hoàn toàn trong Docker với 3 thành phần (Containers) chính:

```text
Người dùng
    │ (Cổng 80)
    ▼
┌─────────────────────────────────┐
│           [Nginx]               │
│  Phục vụ giao diện React tĩnh   │
└───────────────┬─────────────────┘
                │ Proxy các request /api/*
                ▼
┌─────────────────────────────────┐
│     [Spring Boot Backend]       │
│  Xử lý logic, Auth, Chấm điểm   │
└───────────────┬─────────────────┘
                │ Lưu trữ dữ liệu
                ▼
┌─────────────────────────────────┐
│   [SQL Server (hoặc Azure)]     │
│        Database Hệ Thống        │
└─────────────────────────────────┘
```

---

## 2. Yêu Cầu Máy Chủ (VPS)

Để hệ thống (đặc biệt là Spring Boot và Database) hoạt động mượt mà, máy chủ cần cấu hình tối thiểu:

- **CPU:** Tối thiểu 2 vCPU (Khuyến nghị 4 vCPU).
- **RAM:** Tối thiểu 4 GB (Khuyến nghị 8 GB nếu chạy chung SQL Server trên cùng VPS).
- **Disk:** 20 GB SSD trở lên.
- **OS:** Ubuntu 22.04 LTS (Hoặc Windows có cài Docker Desktop).

---

## 3. Hướng Dẫn Triển Khai (Core)

> **Điều kiện kiên quyết:** Máy chủ phải được cài đặt sẵn [Docker](https://docs.docker.com/engine/install/) và [Docker Compose](https://docs.docker.com/compose/install/).

### Bước 1: Chuẩn bị mã nguồn
Clone dự án về máy chủ:
```bash
git clone <repo-url>
cd Japanese-Skill-Practice-Platform
```

### Bước 2: Thiết lập Biến Môi Trường (Cực kỳ quan trọng)
Tạo file `.env` cho Backend:
```bash
cp apps/backend/.env.example apps/backend/.env
```
Mở file `apps/backend/.env` và điền các thông tin bảo mật:
- `DATABASE_URL`: Đường dẫn tới SQL Server (VD: Azure SQL).
- `DATABASE_USERNAME` & `DATABASE_PASSWORD`
- `JWT_SECRET`: Chuỗi bảo mật tự sinh (dài ít nhất 64 ký tự).
- `GOOGLE_CLIENT_ID` & `GOOGLE_CLIENT_SECRET`: Lấy từ Google Cloud Console.

### Bước 3: Khởi chạy bằng 1 lệnh duy nhất
Tại thư mục gốc của dự án (nơi có file `docker-compose.yml`), chạy:
```bash
docker-compose up -d --build
```
Lệnh này sẽ tự động:
1. Tải các image cần thiết (Java, Node, Nginx).
2. Build code React ra file tĩnh.
3. Build code Java ra file `.jar`.
4. Bật tất cả các dịch vụ lên.

Website của bạn sẽ lập tức có thể truy cập tại địa chỉ IP của VPS ở cổng `80`.

---

## 4. Kiểm Tra Hệ Thống (Smoke Tests)

Sau khi hệ thống báo đèn xanh chạy thành công, hãy tự tay kiểm tra các luồng nghiệp vụ quan trọng sau để đảm bảo không có lỗi:

- [ ] Đăng ký tài khoản mới → Cột user trong DB tăng lên.
- [ ] Đăng nhập email/password → Vào được dashboard.
- [ ] **Đăng nhập Google OAuth** → Redirect thành công (Nếu lỗi origin_mismatch, xem phần Xử lý sự cố).
- [ ] Xem danh sách khóa học theo cấp độ (N5-N1).
- [ ] Làm bài Quiz → Nộp bài → Nhận được điểm và lưu lịch sử.
- [ ] **Tải ảnh OCR** lên mạng → File lưu thành công vào Volume `jlpt-uploads` và nhận về `jobId`.
- [ ] Đăng nhập tài khoản Staff/Admin → Vào được trang quản lý.

---

## 5. Cẩm Nang Lệnh Docker (Cheatsheet)

Bạn chỉ cần nhớ các lệnh sau để quản lý hệ thống hàng ngày:

| Lệnh | Chức năng & Tình huống sử dụng |
|------|--------------------------------|
| `docker-compose up -d --build` | **Cập nhật code mới:** Chạy mỗi khi bạn sửa code, đổi cấu hình `.env` hoặc kéo code mới từ Git về. Nó tự động build và chạy lại phần bị thay đổi. |
| `docker-compose down` | **Tắt toàn bộ hệ thống:** Dừng mọi container, giải phóng mạng. Dữ liệu (uploads, DB) vẫn được giữ an toàn trong Volume. |
| `docker-compose logs -f` | **Xem màn hình Log:** Dùng để soi lỗi API, xem log truy cập Nginx. Nhấn `Ctrl+C` để thoát xem. |
| `docker ps` | Hiển thị các hộp (container) đang chạy để biết cái nào đang sập, cái nào sống. |
| `docker exec -it <tên_container> /bin/sh` | **Chui vào máy ảo:** Dùng khi muốn soi cấu hình hoặc kiểm tra file được lưu trực tiếp trong container. |
| `docker system prune` | **Dọn Rác:** Xóa các image cũ, container tắt để giải phóng hàng GB dung lượng. Chạy khi máy báo đầy ổ cứng. |

---

## 6. Xử Lý Sự Cố Thường Gặp (Troubleshooting)

### 6.1 Lỗi Google API (400: origin_mismatch)
- **Triệu chứng:** Khi bấm Đăng nhập bằng Google, hiện ra lỗi 400.
- **Nguyên nhân:** Tên miền hoặc IP hiện tại (ví dụ: `http://localhost` hay `http://ip-may-chu`) chưa được khai báo vào Google.
- **Khắc phục:** Vào Google Cloud Console → API & Services → Credentials. Tìm đến OAuth Client ID của bạn và thêm đường dẫn hiện tại vào **CẢ 2 mục**: "Authorized JavaScript origins" và "Authorized redirect URIs". Đợi 5 phút và thử lại trên trình duyệt Ẩn danh.

### 6.2 Lỗi Kẹt Cổng (Ports are not available)
- **Triệu chứng:** Báo lỗi `listen tcp 0.0.0.0:8080: bind`.
- **Nguyên nhân:** Có một ứng dụng khác (hoặc bản build chạy bằng tay `java -jar`) đang chiếm dụng cổng 8080 hoặc 80.
- **Khắc phục:** Tắt ứng dụng đang chạy (hoặc tắt cửa sổ Terminal cũ) rồi bật lại Docker.

### 6.3 Code mới không nhận (Vẫn chạy bản cũ)
- **Khắc phục:** Hãy chắc chắn bạn luôn kèm cờ `--build` khi bật lại: `docker-compose up -d --build`. Lệnh này ép Docker phải phá bỏ cache cũ và đóng gói lại code mới nhất.
