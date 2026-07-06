# Hệ Thống CI/CD (Continuous Integration / Continuous Deployment)

Tài liệu này mô tả toàn bộ luồng CI/CD của dự án **Japanese Skill Practice Platform (sakuji.online)** được tự động hóa thông qua **GitHub Actions**. Bất kỳ AI Agent hoặc Developer nào làm việc với dự án này cần tuân thủ quy trình dưới đây để đảm bảo code luôn được kiểm tra và tự động deploy chính xác.

---

## 1. Tổng Quan Về Luồng Workflow

Hệ thống sử dụng 2 luồng (workflows) riêng biệt được định nghĩa trong thư mục `.github/workflows/`:

1. **`ci.yml` (Continuous Integration)**: Chạy trên mọi `Push` hoặc `Pull Request` vào nhánh `main` và `develop`. Nhiệm vụ là rà soát lỗi (Lint), Build và chạy Test.
2. **`cd.yml` (Continuous Deployment)**: CHỈ chạy khi có `Push` vào nhánh `main`. Nhiệm vụ là tự động truy cập vào VPS thông qua SSH để cập nhật code mới và khởi động lại Docker.

---

## 2. Quy trình làm việc (Dành cho Developer / AI)

Khi thực hiện sửa lỗi hoặc thêm tính năng mới, hãy tuân theo quy trình tự động hóa sau:

### Bước 1: Viết Code & Kiểm thử Local
- Thay đổi code ở các thư mục `apps/backend` (Java/Spring Boot) hoặc `apps/frontend` (React/Vite).
- AI Agent **KHÔNG CẦN** phải chạy các lệnh deploy thủ công (như SSH hay docker build từ xa) sau khi code xong. Chỉ cần thực hiện commit.

### Bước 2: Đẩy Code (Push)
- Commit thay đổi: `git commit -m "feat/fix: mô tả ngắn gọn"`
- Đẩy lên kho lưu trữ GitHub: `git push origin main` (hoặc tạo Pull Request).

### Bước 3: GitHub Actions tự động tiếp quản
- **Bắt đầu CI:** GitHub Actions lập tức khởi chạy `ci.yml`. Nó sẽ chia làm 2 luồng độc lập:
  - **Backend:** Cài đặt JDK 21 -> `mvn clean verify` (Đảm bảo Java code không lỗi cú pháp và test coverage passed).
  - **Frontend:** Cài đặt Node 20 -> `npm ci` -> `npm run lint` -> `npm run build`.
- **Bắt đầu CD:** Khi và chỉ khi branch là `main` (và lý tưởng là CI đã pass), GitHub Actions chạy `cd.yml`.
  - Kết nối vào server qua chứng chỉ bí mật (`VPS_SSH_KEY`).
  - Đi đến thư mục dự án trên server (`PROJECT_PATH`).
  - Chạy `git pull` để đồng bộ code mới nhất.
  - Chạy `docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build`.

> **Lưu ý cho AI Agent:** Sau khi hoàn thành một yêu cầu của User và code đã được merge, nhiệm vụ của AI coi như hoàn tất. Không cần yêu cầu User chạy script deploy thủ công, hệ thống sẽ tự lo liệu.

---

## 3. Cấu Hình Bảo Mật (GitHub Secrets)

Hệ thống yêu cầu các biến môi trường (Secrets) bắt buộc phải được cài đặt trên GitHub Repository (`Settings > Secrets and variables > Actions`) để CD hoạt động. Đừng ghi đè hoặc lộ các biến này trong source code:

| Secret Name | Mô Tả |
|-------------|--------|
| `VPS_HOST` | Địa chỉ IP của VPS (ví dụ: `192.168.1.1` hoặc `sakuji.online`) |
| `VPS_USERNAME` | User trên VPS có quyền chạy docker (ví dụ: `root`, `ubuntu`) |
| `VPS_SSH_KEY` | Nội dung Private Key (`id_rsa` hoặc `id_ed25519`) để SSH |
| `PROJECT_PATH` | Đường dẫn tuyệt đối tới thư mục code trên VPS |

---

## 4. Tương Tác Giữa Các Môi Trường

- **Biến môi trường Frontend (BUG-10 Fix):** Luôn dùng biến `${APP_FRONTEND_URL}` trong file cấu hình `.yml` backend để link tới frontend (vd: xác thực email) có thể linh hoạt đổi từ `localhost` sang `https://sakuji.online` trên production.
- **Docker Production:** CD gọi lệnh kết hợp cả 2 file `docker-compose.yml` (base) và `docker-compose.prod.yml` (production config). Đừng sửa port expose (443/80) trực tiếp trong base compose file.

## 5. Xử lý sự cố (Troubleshooting)

- **Nếu CD fail ở bước SSH:** Yêu cầu User kiểm tra lại `VPS_SSH_KEY` và xem file `~/.ssh/authorized_keys` trên VPS đã thêm Public Key chưa.
- **Nếu CI fail ở Backend:** Kiểm tra lại cú pháp Java 21 hoặc xem các bài Unit Test có bị fail do thiếu fallback database env không.
- **Nếu Docker đầy ổ cứng:** CD script đã có tích hợp `docker image prune -f` để dọn dẹp các dangling images sau mỗi lần build. Tuy nhiên, nếu thiếu RAM, hãy nhắc User xem xét swap memory.
