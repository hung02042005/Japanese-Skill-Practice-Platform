# Hệ Thống CI/CD (Continuous Integration / Continuous Deployment)

Tài liệu này mô tả toàn bộ luồng CI/CD của dự án **Japanese Skill Practice Platform (sakuji.online)** được tự động hóa thông qua **GitHub Actions**. Bất kỳ AI Agent hoặc Developer nào làm việc với dự án này cần tuân thủ quy trình dưới đây để đảm bảo code luôn được kiểm tra và tự động deploy chính xác.

> **Cập nhật 11/07/2026:** Tài liệu này đã được đồng bộ lại cho khớp 100% với `.github/workflows/ci.yml` và `.github/workflows/cd.yml` thật (P0.3, xem `Deploy_Improvement_Plan.md`), sau khi bản trước đó mô tả sai một số điểm quan trọng (nhánh `main` thay vì `branch_for_hung`, cơ chế gate CI→CD, phương thức xác thực SSH). Xem mục 6 để biết chi tiết lịch sử.

---

## 1. Tổng Quan Về Luồng Workflow

Hệ thống sử dụng 2 luồng (workflows) riêng biệt được định nghĩa trong thư mục `.github/workflows/`:

1. **`ci.yml` (Continuous Integration)**: Chạy trên mọi `Push` vào nhánh `main`, `develop`, **`branch_for_hung`** (nhánh deploy chính hiện tại của dự án) và trên mọi `Pull Request` vào `main`/`develop`. Nhiệm vụ là build + chạy test backend/frontend.
2. **`cd.yml` (Continuous Deployment)**: **KHÔNG** trigger trực tiếp bằng `push`. Nó dùng sự kiện `workflow_run`, nghĩa là chỉ khởi chạy **SAU KHI** `ci.yml` chạy xong trên nhánh `branch_for_hung`, và job `deploy` chỉ thực sự thực thi khi `github.event.workflow_run.conclusion == 'success'`. Nếu CI fail, CD sẽ **không chạy** (trước đây 2 workflow chạy song song độc lập, code lỗi vẫn deploy được — đã sửa, xem mục 6).

```
push → branch_for_hung
   │
   └─► ci.yml (build + test backend & frontend)
            │
            └─► workflow_run "CI (Build & Test)" completed
                     │
                     └─► cd.yml   (chỉ chạy nếu conclusion == success)
```

---

## 2. Quy trình làm việc (Dành cho Developer / AI)

Khi thực hiện sửa lỗi hoặc thêm tính năng mới, hãy tuân theo quy trình tự động hóa sau:

### Bước 1: Viết Code & Kiểm thử Local
- Thay đổi code ở các thư mục `apps/backend` (Java/Spring Boot) hoặc `apps/frontend` (React/Vite).
- AI Agent **KHÔNG CẦN** phải chạy các lệnh deploy thủ công (như SSH hay docker build từ xa) sau khi code xong. Chỉ cần thực hiện commit + push — trừ khi đang xử lý sự cố production thật cần chẩn đoán trực tiếp trên VPS (xem mục 6, có thể cần được cấp quyền SSH riêng).

### Bước 2: Đẩy Code (Push)
- Commit thay đổi: `git commit -m "feat/fix: mô tả ngắn gọn"`
- Đẩy lên kho lưu trữ GitHub: `git push origin branch_for_hung`

### Bước 3: GitHub Actions tự động tiếp quản
- **CI (`ci.yml`):** chia làm 2 job độc lập:
  - **`backend-ci`:** Cài đặt JDK 21 → `mvn -B clean verify` (build + test + JaCoCo coverage check, ngưỡng hiện tại chỉ **10%** — xem khuyến nghị P2.9 trong `Deploy_Improvement_Plan.md`).
  - **`frontend-ci`:** Cài đặt Node 20 → `npm ci` → `npm run lint` → `npm run build`. **Chưa có bước chạy test frontend** (dòng `npm run test` đang bị comment sẵn trong file — xem P2.10).
- **CD (`cd.yml`):** chỉ chạy khi `ci.yml` báo `success` trên `branch_for_hung`. Job `deploy` thực hiện tuần tự:
  1. SSH vào VPS (xem mục 3 về phương thức xác thực).
  2. `git fetch` + `git reset --hard origin/branch_for_hung` tại `PROJECT_PATH`.
  3. Kiểm tra file `.env` tồn tại (bắt buộc để `docker compose` chạy).
  4. Khởi động `db` + `redis` trước (`docker compose up -d db redis`).
  5. Chờ `db` thực sự nhận kết nối + tự tạo database `JLPT_LearningDB` nếu chưa có (retry loop `sqlcmd`, tối đa 150 giây).
  6. Build lại image và khởi động `backend` + `frontend` (`docker compose up -d --build backend frontend`).
  7. **Post-deploy smoke test (P0.1):** gọi `curl` vào `http://127.0.0.1:8080/actuator/health` (backend) và `http://127.0.0.1:80` (frontend/Nginx), retry trong ~100s/~30s. Nếu không lên healthy, job **fail thật** kèm log 100 dòng cuối của container — không còn tình trạng "deploy báo thành công nhưng site chết" như sự cố 11/07/2026.
  8. Dọn dangling images (`docker image prune -f`).
  9. Purge cache Cloudflare (step riêng, chạy trên GitHub Actions runner, không qua SSH).

> **Lưu ý cho AI Agent:** Sau khi hoàn thành một yêu cầu của User và code đã được push, nhiệm vụ coi như hoàn tất — CD tự lo liệu, **và giờ đã tự xác minh app thực sự sống** trước khi báo thành công. Nếu CD vẫn báo fail sau khi smoke test được thêm, đọc kỹ log — nhiều khả năng là lỗi thật (không phải lỗi "báo động giả" như trước đây).

---

## 3. Cấu Hình Bảo Mật (GitHub Secrets)

Hệ thống yêu cầu các biến môi trường (Secrets) bắt buộc phải được cài đặt trên GitHub Repository (`Settings > Secrets and variables > Actions`) để CD hoạt động. Đừng ghi đè hoặc lộ các biến này trong source code:

| Secret Name | Mô Tả | Trạng thái thực tế |
|-------------|--------|-----|
| `VPS_HOST` | Địa chỉ IP của VPS (`135.149.56.179`) | ✅ Đang dùng |
| `VPS_USERNAME` | User trên VPS có quyền chạy docker | ✅ Đang dùng |
| `VPS_PASSWORD` | Mật khẩu SSH của `VPS_USERNAME` | ✅ **Đang là phương thức xác thực SSH THẬT SỰ đang hoạt động** |
| `VPS_SSH_KEY` | Private Key SSH (`id_rsa`/`id_ed25519`) | ⚠️ Có khai báo trong `cd.yml` nhưng **secret này chưa từng được cấu hình đúng trong GitHub** — đã xác nhận bằng sự cố thật ngày 11/07/2026 (xoá `password:` một lần, CD fail ngay với `can't connect without a private SSH key or password`). Xem P1.7 trong `Deploy_Improvement_Plan.md` để chuyển hẳn sang key. |
| `PROJECT_PATH` | Đường dẫn tuyệt đối tới thư mục code trên VPS (`/opt/Japanese-Skill-Practice-Platform`) | ✅ Đang dùng |
| `CLOUDFLARE_ZONE_ID` / `CLOUDFLARE_API_TOKEN` | Dùng để purge cache Cloudflare sau mỗi lần deploy | ✅ Đang dùng |

**KHÔNG dùng SSH key làm phương thức duy nhất cho tới khi đã tạo và xác nhận `VPS_SSH_KEY` hoạt động thật** — xoá `password:` khi key chưa sẵn sàng sẽ chặn đứng mọi lần deploy tiếp theo.

---

## 4. Tương Tác Giữa Các Môi Trường

- **Biến môi trường Frontend:** Luôn dùng biến `${APP_FRONTEND_URL}` trong cấu hình backend để link tới frontend (vd: xác thực email) có thể linh hoạt đổi từ `localhost` sang `https://sakuji.online` trên production.
- **Docker Production:** CD gọi lệnh kết hợp cả 2 file `docker-compose.yml` (base) và `docker-compose.prod.yml` (production config). Đừng sửa port expose (443/80) trực tiếp trong base compose file.
- **Port Database:** `docker-compose.yml` chỉ expose DB ra ngoài ở cổng **`14330`** (`"14330:1433"`) — cổng nội bộ `1433` KHÔNG được public. Khi cấu hình DataGrip/DBeaver kết nối trực tiếp (không qua SSH tunnel), phải dùng port `14330`, không phải `1433`.
- **Biến môi trường kiểu không phải String (int, boolean...):** khi thêm override trong `docker-compose.prod.yml`, LUÔN dùng cú pháp có default `${VAR:-default}`, không dùng `${VAR}` trần — nếu biến chưa khai báo trong root `.env`, Compose sẽ substitute ra chuỗi rỗng (khác "không tồn tại"), và Spring Boot bind chuỗi rỗng vào field kiểu số sẽ crash ngay lúc khởi động (xem sự cố `SMTP_PORT` ngày 11/07/2026, mục 6).

---

## 5. Xử lý sự cố (Troubleshooting)

- **Nếu CD fail ở bước SSH:** Kiểm tra `VPS_PASSWORD` trước (đây là phương thức đang hoạt động thật). Chỉ kiểm tra `VPS_SSH_KEY`/`~/.ssh/authorized_keys` nếu đã chủ động chuyển sang dùng key theo P1.7.
- **Nếu CD fail ở bước chờ DB (`sqlcmd` retry 150 giây):** Rất có thể là **mật khẩu SA trong `.env` không khớp với dữ liệu đã khởi tạo trong volume `sqlserver_data`** — SQL Server chỉ đọc `MSSQL_SA_PASSWORD` một lần lúc khởi tạo, đổi biến sau đó không đổi được mật khẩu thật. Xem chi tiết cách chẩn đoán và khôi phục không mất dữ liệu (kỹ thuật detach/attach) trong `Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md`, Sự cố 4. **KHÔNG xoá volume `sqlserver_data`** để "reset cho nhanh" — có thể mất dữ liệu thật.
- **Nếu CD fail ở bước smoke test (P0.1, mới thêm):** Đọc log `docker logs jlpt-backend`/`jlpt-frontend` được đính kèm ngay trong log CD — đây là lỗi thật (app không lên được), không phải lỗi mạng/SSH. Nguyên nhân phổ biến: biến môi trường mới thêm bị rỗng nhưng property đích không phải String (xem mục 4).
- **Nếu CI fail ở Backend:** Kiểm tra lại cú pháp Java 21 hoặc xem các bài Unit Test có bị fail do thiếu fallback database env không.
- **Nếu Docker đầy ổ cứng:** CD script đã có tích hợp `docker image prune -f` để dọn dẹp các dangling images sau mỗi lần build. Tuy nhiên, nếu thiếu RAM, hãy nhắc User xem xét swap memory.

---

## 6. Lịch sử sự cố & tài liệu liên quan

Ngày 11/07/2026, quá trình deploy một loạt fix đã gặp 4 lần CD fail liên tiếp (thiếu SSH key, healthcheck DB chưa từng verify, biến môi trường rỗng làm crash-loop, và mật khẩu SA lệch với dữ liệu đã khởi tạo — sự cố nghiêm trọng nhất, cần khôi phục dữ liệu qua kỹ thuật detach/attach). Đọc đầy đủ tại:

- [`Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md`](./Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md) — tường thuật chi tiết từng sự cố + đánh giá quy trình deploy so với chuẩn doanh nghiệp.
- [`Deploy_Improvement_Plan.md`](./Deploy_Improvement_Plan.md) — kế hoạch cải thiện theo độ ưu tiên P0/P1/P2, đang triển khai dần (post-deploy smoke test và backup tự động — P0.1, P0.2 — đã hoàn tất tính đến lần cập nhật tài liệu này).
