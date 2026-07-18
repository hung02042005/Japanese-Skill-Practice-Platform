# Sơ Đồ Deploy — JLPT Learning Platform

> Sơ đồ được vẽ dựa trên trạng thái thật của các file cấu hình `.github/workflows/*.yml` và `docker-compose*.yml` chạy trên máy chủ VPS Ubuntu 22.04 (CloudFly). Đây là quy trình CI/CD hoàn chỉnh từ Test tới Deploy.

```mermaid
flowchart TD
    Dev["👤 Developer"]

    Dev -->|"git push origin develop<br/>(code mới, CHƯA chạm production)"| CI_S["ci.yml — CI"]
    Dev -->|"git push origin branch_for_hung<br/>(chỉ sau khi đã test ở staging)"| CI_P["ci.yml — CI"]

    Gate_S{{"workflow_run(develop)<br/>conclusion == success"}}
    Gate_P{{"workflow_run(branch_for_hung)<br/>conclusion == success"}}
    CI_S -.-> Gate_S
    CI_P -.-> Gate_P

    CD_S["cd-staging.yml<br/>(Deploy to Staging)"]
    CD_P["cd.yml<br/>(Deploy to Production)"]
    Gate_S --> CD_S
    Gate_P --> CD_P

    subgraph VPS["CloudFly VPS 222.255.181.207 — Docker Compose (2 stack độc lập)"]
        direction LR
        subgraph PROD["Production stack"]
            direction TB
            FE["jlpt-frontend<br/>:80 / :443"]
            BE["jlpt-backend<br/>127.0.0.1:8080"]
            DB[("jlpt-db<br/>volume mysql_data<br/>:13306 → 3306")]
            Redis[("jlpt-redis<br/>127.0.0.1:6379")]
        end
        subgraph STG["Staging stack"]
            direction TB
            FE_S["jlpt-frontend-staging<br/>:8082"]
            BE_S["jlpt-backend-staging<br/>127.0.0.1:8081"]
            DB_S[("jlpt-db-staging<br/>volume mysql_data_staging<br/>:13307 → 3306")]
            Redis_S[("jlpt-redis-staging<br/>127.0.0.1:6380")]
        end
    end

    CD_S -->|"build + smoke test<br/>(Xác nhận Web sống)"| STG
    CD_P -->|"build + smoke test"| PROD
    CD_P -->|"chỉ SAU KHI smoke test pass"| Tags["Image tag theo commit SHA<br/>jlpt-backend:SHA / jlpt-frontend:SHA<br/>(giữ 5 bản gần nhất)"]
    Rollback["rollback.yml<br/>(workflow_dispatch, người chọn SHA)"]
    Tags -.->|"đọc deploy-history.log<br/>để biết SHA nào từng chạy tốt"| Rollback
    Rollback -.->|"docker tag SHA→latest<br/>KHÔNG build lại → vài chục giây"| PROD

    Backup["backup-db.timer (systemd, 3h sáng)<br/>→ backup-db.sh → /opt/db-backup/*.sql<br/>giữ 14 bản"]

    CF["☁️ Cloudflare<br/>DNS · SSL/HTTPS · chống DDoS · cache"]

    Users["🌐 Người dùng (trình duyệt)<br/>https://sakuji.online"]
    Google["Google OAuth2<br/>(đăng nhập)"]
    SMTP["Gmail / Resend SMTP<br/>(email xác minh / reset mật khẩu)"]

    PROD -.-> Backup
    PROD --> CF
    CF --> Users
    Users -.-> Google
    Users -.-> SMTP

    classDef new fill:#3a2f14,stroke:#e8a33d,color:#f3d9a0,stroke-width:1.5px;
    classDef risk fill:#3a1f1c,stroke:#e2604f,color:#f4b8ae,stroke-width:1.5px;
    classDef gate fill:#132b2d,stroke:#4fb3bf,color:#a9e2e8,stroke-width:1.5px;
    class CD_S,Tags,Rollback,STG,FE_S,BE_S,DB_S,Redis_S new;
    class Backup risk;
    class Gate_S,Gate_P gate;
```

> **Đọc sơ đồ:** Hệ thống rẽ ra 2 luồng rõ rệt. Nhánh `develop` dẫn code chạy tới **staging** (để Reviewer kiểm tra, đường nét liền phía trên bên trái). Nhánh `branch_for_hung` đưa code chạy thẳng lên **production** (bên phải).  
> Tính năng `rollback.yml` (đường nét đứt) được thiết kế khẩn cấp, chỉ chạy khi có người quản trị chủ động bấm nút "Run workflow" để đưa phiên bản của ứng dụng lùi về quá khứ mà không cần Build lại tốn thời gian.

---

## Chú Giải Chi Tiết Các Mắt Xích Kỹ Thuật

Mục tiêu của phần này là giải thích **vai trò, chức năng, và lý do tồn tại** của từng thiết lập, giúp bạn nắm vững kiến trúc CloudFly VPS.

### 1. Nguồn — Git & chiến lược nhánh
**Vai trò:** Nhánh `develop` và `branch_for_hung` đóng vai trò là công tắc kích hoạt CI/CD. Cấu trúc 2 nhánh này giải quyết bài toán: Không phải mọi code nào dev đẩy lên cũng an toàn. Mọi sự thay đổi phải trải qua môi trường "Staging" cho nhóm tự trải nghiệm thử trước khi Merge sang "Production" cho User dùng.

### 2. CI (Continuous Integration) — `ci.yml`
**Vai trò:** Đây là người gác cổng số 1.
- Backend CI kiểm tra xem bộ Source Code Java có biên dịch được thành file JAR hay không. Đồng thời tính tỉ lệ phần trăm Test Coverage (JaCoCo).
- Frontend CI chạy `npm run lint` để kiểm tra lỗi cú pháp và chạy thử quá trình Build giao diện tĩnh cho ReactJS.

### 3. Gate (Chốt kiểm soát CI -> CD)
Trong `cd.yml` và `cd-staging.yml` có thiết lập quan trọng sau:
```yaml
on:
  workflow_run:
    workflows: ["CI (Build & Test)"]
    branches: ["branch_for_hung"]
    types: [completed]
```
Lệnh này ngăn cản việc triển khai (Deploy) nếu như Code bị lỗi cú pháp ở bước CI. Code lỗi sẽ dừng lại tại Github, ngăn chặn tuyệt đối việc Server cố gắng kéo 1 bản code lỗi về và bị sập.

### 4. CD (Triển khai liên tục qua SSH)
Thay vì Developer phải đăng nhập bằng Putty/MobaXTerm vào VPS `222.255.181.207`, kịch bản CD thông qua `appleboy/ssh-action` sẽ tự động:
1. Đăng nhập SSH bằng IP, Tài khoản và Mật khẩu bạn đặt ở phần Secrets của Repository.
2. Di chuyển đến thư mục dự án trên VPS (`/opt/...`).
3. Dùng lệnh `git reset --hard` để ép Server nhận 100% bản code mới (xóa bỏ mọi file rác do can thiệp tay cục bộ).
4. Khởi động DB và Redis trước.
5. Cập nhật (Build) Backend và Frontend.
6. **Smoke Test:** Kịch bản sẽ không mù quáng thông báo "Deploy thành công" nếu Nginx và Spring Boot chưa thực sự phản hồi Web ở cổng 8080 và 80. Nó sẽ Ping liên tục tới khi Spring Boot báo `UP` thì mới đánh dấu Workflow màu xanh lá cây trên trang chủ Github.

### 5. Tag Image và Tính Năng Rollback
Mỗi một lần ứng dụng Deploy thành công qua bước Smoke Test, hệ thống sẽ gán 1 cái thẻ (Tag) vào Container chứa mã SHA của Commit đó. Ví dụ `jlpt-backend:f9e70f6e`.
Nhờ cơ chế này, chúng ta lưu được 5 phiên bản khỏe mạnh gần nhất.

Khi một ngày có biến cố xảy ra với code mới, thay vì hoảng loạn chờ Build lại 10 phút, bạn chỉ cần mở file `.github/workflows/rollback.yml`, nhập tay cái mã SHA cũ kia vào. Hệ thống sẽ ngay lập tức đổi nhãn dán, ép Server chạy lại cái Image cũ kia ngay trong vài chục giây.

### 6. Cloudflare DNS & Caching
Máy chủ VPS `222.255.181.207` hiện nay không tiếp xúc trực tiếp Internet. Tất cả người dùng đều đi qua Domain `sakuji.online` (Cloudflare). Ở bước CD thứ 6, sau khi Build xong code mới, tự động sẽ có một đoạn Script gọi API lên Cloudflare để xóa `Cache`, ép trình duyệt người dùng load lại File JS, CSS mới nhất, tránh tình trạng "Dev sửa Code nhưng Web khách hiển thị cái cũ".

### 7. Uptime Monitoring (Theo Dõi Máy Chủ Chết/Sống)
File `.github/workflows/uptime-check.yml` được cấu hình để mỗi 10 phút sẽ đánh thức Github Server, thực hiện lệnh `curl` gửi Ping tới trang chủ của bạn. Nếu trang bị ngỏm (doVPS bị treo ổ cứng, cạn RAM...), quy trình Github sẽ chớp đỏ cảnh báo để bạn biết website đang có vấn đề.

---
*(Lưu ý cuối cùng về Bảo mật: File `.env` chứa mật khẩu Database và API Key của Google, Email chỉ được phép tạo trên VPS. Tuyệt đối không commit nó lên Github. Nếu sau này có đổi Mật khẩu, hãy nhắn tin báo trong nhóm nội bộ ngay).*
