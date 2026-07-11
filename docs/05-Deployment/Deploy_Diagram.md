# Sơ Đồ Deploy — JLPT Learning Platform

> Vẽ dựa trên trạng thái thật của `.github/workflows/*.yml`, `docker-compose*.yml` và cấu hình đã xác minh trực tiếp trên VPS (`135.149.56.179`) tính đến 11/07/2026 — không phải sơ đồ lý thuyết. Xem thêm [`Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md`](./Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md) và [`Deploy_Improvement_Plan.md`](./Deploy_Improvement_Plan.md).

```mermaid
flowchart TD
    Dev["👤 Developer<br/>git push origin branch_for_hung"]

    subgraph CI["ci.yml — CI (Build & Test)"]
        direction LR
        BeCI["backend-ci<br/>JDK 21 → mvn clean verify"]
        FeCI["frontend-ci<br/>Node 20 → npm ci → lint → build"]
    end

    Gate{{"workflow_run<br/>chỉ tiếp tục nếu conclusion == success"}}

    subgraph CD["cd.yml — CD (Deploy to Production)"]
        direction TB
        SSH["SSH vào VPS<br/>(VPS_PASSWORD — VPS_SSH_KEY khai báo nhưng chưa cấu hình thật)"]
        Pull["git reset --hard origin/branch_for_hung"]
        DbUp["Khởi động db + redis<br/>chờ DB nhận kết nối (tối đa 150s)"]
        BeUp["Build + khởi động backend + frontend"]
        Smoke["🆕 P0.1 — Smoke test<br/>curl /actuator/health + trang chủ, retry tới khi UP thật"]
        Purge["Purge cache Cloudflare"]
        SSH --> Pull --> DbUp --> BeUp --> Smoke --> Purge
    end

    subgraph VPS["Azure VPS 135.149.56.179 — Docker Compose"]
        direction LR
        FE["jlpt-frontend<br/>Nginx<br/>:80 / :443"]
        BE["jlpt-backend<br/>Spring Boot 3 / Java 21<br/>127.0.0.1:8080"]
        DB[("jlpt-db<br/>SQL Server 2022<br/>volume sqlserver_data<br/>:14330 → 1433")]
        Redis[("jlpt-redis<br/>Cache/session<br/>127.0.0.1:6379")]
    end

    Backup["🆕 P0.2 — backup-db.timer (systemd, 3h sáng)<br/>→ backup-db.sh → /opt/db-backup/*.bak<br/>giữ 14 bản, đã test restore thành công<br/>⚠️ chưa đẩy ra NGOÀI VPS"]

    CF["☁️ Cloudflare<br/>DNS · SSL/HTTPS · chống DDoS · cache"]

    Users["🌐 Người dùng (trình duyệt)<br/>https://sakuji.online"]
    Google["Google OAuth2<br/>(đăng nhập)"]
    SMTP["Gmail SMTP<br/>(email xác minh / reset mật khẩu)"]

    Dev --> CI
    BeCI -.-> Gate
    FeCI -.-> Gate
    Gate --> CD
    CD --> VPS
    VPS -.-> Backup
    VPS --> CF
    CF --> Users
    Users -.-> Google
    Users -.-> SMTP

    classDef new fill:#3a2f14,stroke:#e8a33d,color:#f3d9a0,stroke-width:1.5px;
    classDef risk fill:#3a1f1c,stroke:#e2604f,color:#f4b8ae,stroke-width:1.5px;
    classDef gate fill:#132b2d,stroke:#4fb3bf,color:#a9e2e8,stroke-width:1.5px;
    class Smoke new;
    class Backup risk;
    class Gate gate;
```

---

## Chú giải theo từng giai đoạn

### 1. Nguồn
`git push origin branch_for_hung` — nhánh này vừa là nhánh làm việc chính vừa là nhánh trigger deploy production (không tách `develop`/`main` riêng — xem khuyến nghị P1.4 về staging).

### 2. CI — `ci.yml`
Chạy song song 2 job độc lập trên mọi push vào `branch_for_hung`, `main`, `develop`:
- **`backend-ci`**: JDK 21 → `mvn clean verify` (build + test + JaCoCo, ngưỡng coverage hiện chỉ 10%).
- **`frontend-ci`**: Node 20 → `npm ci` → `npm run lint` → `npm run build` (chưa có bước test frontend).

### 3. Gate — `workflow_run`
CD **không** trigger trực tiếp bằng `push`. Nó chỉ khởi chạy sau khi `ci.yml` báo `completed`, và job `deploy` chỉ thực thi khi `conclusion == 'success'`. Đây là điểm đã sửa hôm 11/07/2026 — trước đó CD chạy song song độc lập với CI, code lỗi vẫn deploy được.

### 4. CD — `cd.yml`
SSH vào VPS rồi thực hiện tuần tự:
1. `git reset --hard origin/branch_for_hung`
2. Khởi động `db` + `redis`, chờ DB nhận kết nối (retry tối đa 150 giây)
3. Build + khởi động `backend` + `frontend`
4. **🆕 P0.1 — Smoke test**: gọi `curl` vào `/actuator/health` và trang chủ, retry tới khi xác nhận UP thật (trước đây `docker compose up -d` báo "thành công" ngay cả khi backend crash-loop bên trong — xem sự cố SMTP_PORT trong `Incident_Report...md`)
5. Purge cache Cloudflare

### 5. Hạ tầng — Azure VPS (Docker Compose)
Một VPS duy nhất (chưa có staging riêng), 4 container:

| Container | Vai trò | Port ra ngoài |
|---|---|---|
| `jlpt-frontend` | Nginx — phục vụ React build + reverse-proxy `/api` | `:80` / `:443` |
| `jlpt-backend` | Spring Boot 3 / Java 21 | `127.0.0.1:8080` (không public trực tiếp) |
| `jlpt-db` | SQL Server 2022, volume `sqlserver_data` | `:14330 → 1433` (nội bộ `1433` không public) |
| `jlpt-redis` | Cache/session | `127.0.0.1:6379` |

**🆕 P0.2 — Backup tự động:** `backup-db.timer` (systemd, chạy 3h sáng hàng ngày) → `backup-db.sh` → lưu vào `/opt/db-backup/*.bak`, giữ 14 bản gần nhất. Đã test phục hồi thành công (27 bảng, dữ liệu khớp thật).

> ⚠️ **Rủi ro còn tồn tại:** bản backup hiện chỉ nằm trên chính VPS này — chưa đẩy ra ngoài (rclone/S3/Google Drive...). Nếu VPS hỏng ổ cứng, mất cả ứng dụng lẫn backup cùng lúc. Đang chờ chủ dự án cung cấp credential cloud storage để tự động hoá bước này.

### 6. Biên — Cloudflare
Đứng trước VPS: ẩn IP thật, cấp SSL/HTTPS tự động, chống DDoS, cache static asset, quản lý DNS cho `sakuji.online`.

### 7. Người dùng
Truy cập qua HTTPS, cộng 2 phụ thuộc ngoài:
- **Google OAuth2** — cho luồng "Đăng nhập với Google"
- **Gmail SMTP** — gửi email xác minh tài khoản / đặt lại mật khẩu (cấu hình qua trang admin, lưu trong bảng `system_settings`, không chỉ qua biến môi trường `.env`)
