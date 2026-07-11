# Báo Cáo Sự Cố Deploy 11/07/2026 & Đánh Giá Quy Trình Deploy

**Phạm vi tài liệu:** Tài liệu này có 2 phần độc lập nhưng liên quan chặt:
- **Phần 1** — tường thuật chi tiết chuỗi sự cố xảy ra khi deploy các fix trong [`implementation_plan.md`](../../implementation_plan.md) lên `sakuji.online` ngày 11/07/2026: lỗi gì, tại sao xảy ra, sửa thế nào, và fix đó là **tạm thời (workaround)** hay **đúng chuẩn lâu dài (proper fix)**.
- **Phần 2** — đánh giá khách quan quy trình CI/CD và vận hành hiện tại của dự án so với chuẩn một đội DevOps/SRE trong doanh nghiệp thực tế, dựa trên bằng chứng thu thập trực tiếp từ VPS trong quá trình xử lý sự cố (không suy đoán).

> Toàn bộ dữ liệu, log, timestamp trong tài liệu này được lấy trực tiếp từ GitHub Actions API và SSH vào VPS thật (`135.149.56.179`) trong quá trình xử lý sự cố — không phải suy đoán lý thuyết.

---

## PHẦN 1 — BÁO CÁO SỰ CỐ DEPLOY

### Bối cảnh

Sau khi hoàn thành 15 fix trong `implementation_plan.md` (auth, transaction, CI/CD gating, docker-compose...), quá trình push code lên `branch_for_hung` và để CI/CD tự động deploy đã gặp **4 lần deploy fail liên tiếp** trước khi hệ thống ổn định trở lại, và phát sinh thêm **1 sự cố dữ liệu thật** cần can thiệp thủ công qua SSH để xử lý an toàn. Đây là timeline đầy đủ:

| # | Commit | Kết quả CD | Lỗi | Thời gian phát hiện → xử lý xong |
|---|--------|-----------|-----|------|
| 1 | `687ef159` | ❌ Fail (ngay lập tức) | Thiếu SSH key | ~vài phút |
| 2 | `b2b2ea25` | ❌ Fail (sau khi build xong) | DB "unhealthy" theo Docker | ~vài phút |
| 3 | `7f25cd2c` | ✅ "Success" nhưng site chết | Backend crash-loop do biến môi trường rỗng | Phát hiện qua báo cáo người dùng, không phải qua giám sát tự động |
| 4 | `01550e87` | ❌ Fail (timeout 150s) | Sai mật khẩu SA — dữ liệu thật kẹt trong volume cũ | Cần SSH trực tiếp để chẩn đoán và phục hồi |
| — | (không qua git) | — | DataGrip không kết nối được (port + password) | Do hệ quả trực tiếp của sự cố #4 |

---

### Sự cố 1: Thiếu SSH Key hợp lệ

**Triệu chứng:**
```
2026/07/11 06:34:23 Error: can't connect without a private SSH key or password
```
CD fail ngay ở bước "Execute SSH commands on VPS", trước khi bất kỳ dòng script deploy nào kịp chạy.

**Nguyên nhân gốc:** Trong lượt audit trước, tôi đã xoá dòng `password: ${{ secrets.VPS_PASSWORD }}` khỏi `cd.yml`, dựa trên khuyến nghị bảo mật hợp lý về lý thuyết ("chỉ nên dùng SSH key, không dùng password") nhưng **chưa xác minh được secret `VPS_SSH_KEY` có thực sự tồn tại và hợp lệ trong GitHub repo secrets hay không** — vì không có quyền xem GitHub Secrets từ môi trường làm việc. Thực tế: secret này **chưa từng được cấu hình**; toàn bộ quy trình deploy trước giờ chỉ hoạt động được nhờ `VPS_PASSWORD`.

**Cách khắc phục:** Revert, khôi phục lại `password: ${{ secrets.VPS_PASSWORD }}` song song với `key: ${{ secrets.VPS_SSH_KEY }}` (action `appleboy/ssh-action` tự ưu tiên key nếu có, fallback về password nếu không).

**Tạm thời hay đúng chuẩn?** ⚠️ **Tạm thời.** Dùng password để SSH vào một máy chủ production là anti-pattern về bảo mật (password có thể bị brute-force, không hỗ trợ MFA, khó xoay vòng/rotate an toàn như key). Đây là fix "khôi phục lại trạng thái cũ để không bị đứng hình", không phải fix đúng chuẩn.

**Việc cần làm để đúng chuẩn:** Tạo cặp khoá SSH mới riêng cho CI/CD (không dùng chung key cá nhân), thêm public key vào `~/.ssh/authorized_keys` của user `jlptadmin` trên VPS, add private key vào GitHub Secret `VPS_SSH_KEY`, xác nhận deploy chạy được chỉ với key, rồi mới xoá `password:` khỏi `cd.yml` lần nữa — và **thu hồi/đổi `VPS_PASSWORD`** sau khi không còn cần dùng nó cho CD nữa (giảm bề mặt tấn công).

---

### Sự cố 2: Docker Compose `depends_on: service_healthy` phơi bày một healthcheck chưa từng được kiểm chứng

**Triệu chứng:**
```
Container jlpt-db Waiting
Container jlpt-db Error dependency db failed to start
dependency failed to start: container jlpt-db is unhealthy
```

**Nguyên nhân gốc:** `docker-compose.yml` đã có sẵn định nghĩa `healthcheck` cho service `db` từ trước (chạy `sqlcmd` kiểm tra kết nối), nhưng **trước đó không có service nào khai báo phụ thuộc (`depends_on`) vào tình trạng health này** — nghĩa là dù healthcheck có fail âm thầm bao lâu, không ai từng biết, vì không có gì bị chặn lại bởi nó. Khi tôi thêm `depends_on: db: condition: service_healthy` cho `backend` (để sửa một bug thật: backend có thể khởi động trước khi DB sẵn sàng), lần đầu tiên hệ thống THỰC SỰ kiểm tra điều kiện này — và phát hiện ra `db` **chưa bao giờ pass healthcheck**.

**Cách khắc phục tức thời:** Hạ `depends_on` của `db` về lại `condition: service_started` (chỉ đợi container khởi động, không đợi healthy).

**Tạm thời hay đúng chuẩn?** ⚠️ **Tạm thời, và là một bước lùi có chủ đích.** Về lâu dài `service_healthy` mới là điều kiện đúng (đảm bảo backend không kết nối vào DB chưa sẵn sàng) — nhưng healthcheck hiện tại (theo dạng nguyên bản) đã kết hợp cả việc "kiểm tra kết nối" và "tạo database nếu chưa có" trong cùng 1 câu lệnh `sqlcmd`, không đúng chuẩn (DDL không nên nằm trong healthcheck). Team đã tự sửa việc này sau đó (xem commit `efedbdd2`) bằng cách tách logic "chờ + tạo DB" ra khỏi healthcheck, đưa vào một bước riêng trong `cd.yml` (retry loop gọi `sqlcmd` trực tiếp trước khi build backend) — đây là hướng đi đúng, dù bản thân bước retry đó vẫn còn phụ thuộc vào đúng mật khẩu SA (xem Sự cố 4).

**Việc cần làm để đúng chuẩn:** Sau khi Sự cố 4 được xử lý dứt điểm (mật khẩu SA đã đồng bộ), nên bật lại `depends_on: db: condition: service_healthy` cho `backend` và giữ healthcheck đơn giản (`SELECT 1`, không kèm DDL), vì giờ nó đã được verify là hoạt động đúng.

---

### Sự cố 3: Biến môi trường rỗng làm backend crash-loop, nhưng CD vẫn báo "thành công"

**Triệu chứng:** Deploy báo `success` trên GitHub Actions, nhưng `https://sakuji.online` trả `502 Bad Gateway` cho **toàn bộ API**, kể cả đăng nhập.

**Nguyên nhân gốc (đây là lỗi tinh vi nhất trong 4 lỗi, đáng chú ý nhất về mặt kỹ thuật):**
1. `docker-compose.prod.yml` được thêm `SMTP_PORT=${SMTP_PORT}` (không có giá trị mặc định) để bật tính năng gửi email.
2. Root `.env` trên VPS **không khai báo `SMTP_PORT`**.
3. Với cú pháp `${VAR}` trần (không có `:-default`), Docker Compose coi biến này là **"tồn tại nhưng rỗng"**, KHÁC với "không tồn tại". Container backend nhận `SMTP_PORT=""`.
4. `application.yml` khai báo `spring.mail.port: ${SMTP_PORT:587}` — cú pháp Spring Boot property placeholder chỉ áp dụng default `587` khi biến môi trường **không tồn tại**; nếu biến tồn tại nhưng rỗng, Spring cố bind chuỗi rỗng vào field kiểu `int` → ném `BindException` ngay giữa lúc khởi tạo `ApplicationContext`.
5. Backend crash ngay khi khởi động → nhưng vì `restart: unless-stopped`, container cứ crash rồi tự khởi động lại liên tục (crash-loop), không bao giờ thực sự lắng nghe port 8080.
6. `docker compose up -d --build` chạy ở chế độ **detached (`-d`)** — lệnh trả về mã thoát 0 ngay khi container được *tạo ra*, KHÔNG đợi xác nhận ứng dụng bên trong *thực sự chạy được*. Vì vậy CD workflow báo "success" dù backend đang chết.

**Cách khắc phục:** Đổi toàn bộ 4 biến `SMTP_*` sang cú pháp có default rõ ràng (`${SMTP_HOST:-smtp.gmail.com}`, `${SMTP_PORT:-587}`, ...) — khớp đúng default đã có sẵn trong `application.yml`, để khi root `.env` chưa khai báo thì hành vi giữ nguyên như trước khi thêm biến này, thay vì tiêm chuỗi rỗng vào một property kiểu số.

**Tạm thời hay đúng chuẩn?** ✅ **Đây là fix đúng chuẩn**, không phải workaround — nó sửa đúng gốc rễ (cú pháp sai khi override biến môi trường kiểu không phải String) và không đánh đổi gì để lấy sự ổn định.

**Vấn đề còn tồn tại (không phải bug, là khoảng trống quy trình):** CD pipeline **không có bước xác minh sau-deploy (post-deploy smoke test)**. Đây chính là lý do một crash-loop nghiêm trọng như thế này có thể "báo xanh" trên GitHub Actions. Xem khuyến nghị P0 ở Phần 2.

---

### Sự cố 4: Mật khẩu SA lệch với dữ liệu đã khởi tạo — sự cố nghiêm trọng nhất, có rủi ro mất dữ liệu thật

**Triệu chứng:** Sau khi team tự cải tiến `cd.yml` để chờ DB rồi mới build backend (commit `efedbdd2`, `071f3474`, `01550e87`), CD vẫn fail — nhưng lần này script chạy đủ **150 giây** (đúng bằng 30 lần retry × 5s) rồi mới báo lỗi, khác hẳn kiểu fail tức thời của 2 sự cố trước.

**Nguyên nhân gốc (xác nhận bằng SSH trực tiếp vào VPS):**
```
docker logs jlpt-db:
Login failed for user 'sa'. Reason: Password did not match that for the login provided.
```
Truy vết bằng timestamp:
- `docker volume inspect sqlserver_data` → `CreatedAt: 2026-06-30T16:18:52Z` (volume dữ liệu được tạo từ 30/06).
- `.env` trên VPS (`/opt/Japanese-Skill-Practice-Platform/.env`) → `stat` cho thấy sửa lần cuối **10/07 lúc 23:46** — tức là **11 ngày sau khi database đã có dữ liệu**.

SQL Server (image chính thức `mcr.microsoft.com/mssql/server`) chỉ đọc biến `MSSQL_SA_PASSWORD` **duy nhất một lần**, lúc khởi tạo dữ liệu lần đầu (khi thư mục data rỗng). Sau đó, đổi biến môi trường không hề đổi mật khẩu thật đã được lưu (hash) trong database hệ thống `master`. Ai đó (không rõ vô tình hay có chủ đích) đã đổi `MSSQL_SA_PASSWORD` trong `.env` vào tối 10/07 — kể từ đó, mọi kết nối dùng mật khẩu mới (kể cả từ chính backend) đều bị SQL Server từ chối, vì mật khẩu thật bên trong `master` database vẫn là mật khẩu cũ.

**Vì sao đây là sự cố nghiêm trọng nhất:** `docker exec jlpt-db du -sh /var/opt/mssql/data` cho thấy **231MB dữ liệu thật** (13 tài khoản học viên, 27 bảng dữ liệu) — không phải database rỗng/test. Một fix sai (xoá volume để "reset cho nhanh") sẽ **xoá vĩnh viễn toàn bộ dữ liệu người dùng thật**.

**Cách khắc phục (được cấp quyền SSH trực tiếp để xử lý — xem chi tiết đầy đủ trong lịch sử hội thoại xử lý sự cố):**

SQL Server for Linux **không có cơ chế "local admin bypass"** như Windows (không thể dùng single-user mode để reset mật khẩu đã quên nếu không biết bất kỳ credential hợp lệ nào — đây là khác biệt quan trọng so với SQL Server on Windows mà nhiều tài liệu online không phân biệt rõ). Giải pháp đúng dựa trên một sự thật quan trọng: **mật khẩu `sa` chỉ nằm trong database hệ thống `master`, KHÔNG nằm trong file dữ liệu ứng dụng** (`JLPT_LearningDB.mdf`/`.ldf`). Quy trình đã thực hiện:

1. **Backup toàn bộ volume** (`tar czf` qua container tạm) trước khi đụng vào bất cứ thứ gì — nguyên tắc bắt buộc trước mọi thao tác có rủi ro mất dữ liệu.
2. Dừng `jlpt-db` cũ (graceful stop), **không xoá volume gốc**.
3. Tạo volume + container SQL Server **hoàn toàn mới**, dùng đúng mật khẩu hiện tại trong `.env` (nên biết chắc mật khẩu này).
4. Copy riêng 2 file `JLPT_LearningDB.mdf` / `JLPT_LearningDB_log.ldf` (đã backup) sang volume mới, đúng quyền sở hữu `10001:10001` (UID/GID chuẩn của user `mssql` trong image chính thức).
5. Chạy `CREATE DATABASE JLPT_LearningDB ON (FILENAME=...), (FILENAME=...) FOR ATTACH;` — gắn dữ liệu cũ vào instance mới có mật khẩu đúng.
6. Verify: đủ 27 bảng, `student_users` = 13 dòng — dữ liệu nguyên vẹn 100%.
7. Hoán đổi nội dung volume mới (đã verify) vào đúng volume `sqlserver_data` mà `docker-compose.yml` đang tham chiếu (để không phải sửa file cấu hình).
8. Recreate `jlpt-db` + rebuild/restart `jlpt-backend` qua `docker compose up -d` bình thường.

**Tạm thời hay đúng chuẩn?** ✅ **Đây là quy trình đúng chuẩn** cho tình huống "mất/lệch mật khẩu instance nhưng còn nguyên file dữ liệu" — đây chính xác là kỹ thuật *"detach/attach"* mà DBA chuyên nghiệp dùng để di trú dữ liệu giữa các instance SQL Server, **không phải mẹo tạm bợ**. Tuy nhiên bản thân việc PHẢI làm thao tác này theo kiểu thủ công, khẩn cấp, không có kịch bản chuẩn bị sẵn — là dấu hiệu rõ ràng của một khoảng trống quy trình (xem Phần 2, mục Backup & Restore).

**Rủi ro/việc còn tồn đọng:**
- File backup (`/opt/db-backup/mssql-data-backup-20260711112907.tar.gz`) và volume `sqlserver_data_recovered` (bản sao đã verify) **vẫn còn trên VPS** làm lưới an toàn — nên dọn dẹp sau khi xác nhận ổn định vài ngày, và **quan trọng hơn: nên thiết lập một quy trình backup định kỳ thật sự** (xem Phần 2), vì hiện tại `crontab -l` cho user vận hành **không có bất kỳ tác vụ backup nào** — bản backup vừa tạo là **bản backup đầu tiên và duy nhất** của database này tính đến thời điểm sự cố.
- Không xác định được ai/khi nào/vì sao `.env` bị đổi mật khẩu ngày 10/07 — không có audit log truy vết thay đổi file cấu hình trên VPS.

---

### Sự cố phụ: Kết nối DataGrip/DBeaver thất bại sau khi database được phục hồi

Đây không phải bug hệ thống, mà là **hệ quả trực tiếp và tất yếu** của Sự cố 4, kèm một lỗi cấu hình client riêng:

1. **Sai port:** Kết nối ban đầu trỏ tới `135.149.56.179:1433` — nhưng `docker-compose.yml` chỉ expose port **`14330`** ra ngoài (`"14330:1433"`), cổng `1433` chỉ tồn tại nội bộ trong mạng Docker. Xác nhận bằng `docker port jlpt-db` và `ss -tlnp` trên VPS: chỉ có `0.0.0.0:14330` đang lắng nghe. → Đây khớp với tài liệu vận hành cũ (`docs/05-Deployment/README.md`) vốn hướng dẫn đào SSH tunnel `-L 14330:127.0.0.1:1433`, nhưng người dùng trong tình huống này lại cấu hình kết nối trực tiếp bằng port nội bộ 1433 thay vì port đã tunnel 14330 — dễ nhầm lẫn vì cả 2 số đều "hợp lý" nếu không đọc kỹ hướng dẫn.
2. **Sai mật khẩu:** Sau khi sửa đúng port, `sa` login vẫn fail vì DataGrip đã lưu mật khẩu **CŨ** (trước khi tôi đồng bộ lại mật khẩu `sa` khớp với `.env` hiện tại ở Sự cố 4) — phải cập nhật lại password đã lưu trong client.

**Bài học quy trình:** Việc "1 mật khẩu SA, nhưng có ít nhất 3 nơi lưu nó khác nhau" (`.env` trên VPS, connection đã lưu trong DataGrip của từng thành viên, và có thể trong đầu người từng đổi nó ngày 10/07) — là một anti-pattern quản lý secret rất phổ biến ở dự án nhỏ, chính là nguyên nhân gốc của toàn bộ chuỗi sự cố 4 sự cố này.

---

### Bài học tổng quát rút ra từ cả 4 sự cố

> **Audit tĩnh (đọc code, `mvn verify`, `docker compose config`) không thể thay thế việc chạy deploy thật và quan sát.** Một fix đúng về mặt logic (đổi `depends_on`, thêm biến môi trường, xoá SSH password thừa) vẫn có thể phá production nếu nó phụ thuộc vào một giả định về **hạ tầng, secret, hoặc dữ liệu** mà không thể kiểm chứng được chỉ bằng cách đọc source code:
> - Secret có thực sự tồn tại trong GitHub Secrets không? (Sự cố 1)
> - Healthcheck có *thực sự* pass trên môi trường thật không, hay chỉ "tồn tại" trên giấy? (Sự cố 2)
> - Biến môi trường "rỗng" và "không tồn tại" là hai trạng thái khác nhau với type binding — chỉ lộ ra khi có kiểu dữ liệu không phải String. (Sự cố 3)
> - `.env` trên server có đang đồng bộ với dữ liệu đã khởi tạo hay không? (Sự cố 4 — nghiêm trọng nhất)
>
> Và quan trọng không kém: **`docker compose up -d` thành công KHÔNG có nghĩa là ứng dụng bên trong chạy được.** Đây là lỗ hổng lớn nhất trong quy trình CD hiện tại (xem khuyến nghị P0 ở Phần 2).

---

## PHẦN 2 — ĐÁNH GIÁ QUY TRÌNH DEPLOY SO VỚI CHUẨN DOANH NGHIỆP

### 2.1. Tổng quan kiến trúc hiện tại

```
GitHub push (branch_for_hung)
   │
   ├─► ci.yml  (build + test backend/frontend)
   │        │
   │        └─► workflow_run (khi ci.yml "completed")
   │                │
   │                └─► cd.yml (nếu conclusion == success)
   │                        │
   │                        └─► SSH vào 1 VPS Azure duy nhất
   │                                │
   │                                └─► git reset --hard + docker compose up -d --build
   │                                        (rebuild backend + frontend TẠI CHỖ, in-place)
```

Một VPS Ubuntu duy nhất chạy cả 4 service (`db`, `redis`, `backend`, `frontend`) qua Docker Compose, đứng sau Cloudflare (DNS + SSL + chống DDoS). Không có môi trường staging/pre-production riêng biệt — mọi merge vào `branch_for_hung` đều đi thẳng ra production sau khi CI pass.

### 2.2. Bảng chấm điểm theo hạng mục (dựa trên bằng chứng thu thập được, không suy đoán)

| Hạng mục | Hiện trạng | Đánh giá | Chuẩn doanh nghiệp thực tế |
|---|---|---|---|
| **CI (build/test tự động)** | Có `ci.yml`, chạy `mvn verify` + `npm run lint/build` | 🟡 Có nhưng sơ sài | Coverage gate 10% quá thấp (thường ≥ 60-70%); frontend **không có bước test** (dòng lệnh bị comment sẵn trong `ci.yml`) |
| **CD tự động** | `workflow_run` gate CI→CD (mới sửa) | 🟢 Đạt chuẩn cơ bản | Đúng hướng — tương đương GitHub Flow đơn giản |
| **Kiến trúc môi trường** | 1 VPS duy nhất, không có staging | 🔴 Chưa đạt | Doanh nghiệp luôn có tối thiểu 1 staging/UAT trước khi ra prod, đặc biệt cho thay đổi schema DB |
| **Chiến lược deploy** | In-place rebuild (`docker compose up -d --build`), không blue-green/canary | 🔴 Chưa đạt | Gây downtime thật trong lúc build lại image (đã quan sát trực tiếp — vài chục giây tới vài phút mỗi lần); không có cách deploy dần dần/rollback tức thời |
| **Rollback** | Không có cơ chế | 🔴 Chưa đạt | Docker image chỉ tag `latest` — build mới **ghi đè** bản cũ. Muốn rollback phải `git revert` + build lại từ đầu, mất vài phút, KHÔNG có "bấm nút quay lại bản trước" |
| **Post-deploy verification** | Không có | 🔴 Chưa đạt — **đây là gap nghiêm trọng nhất** | Chính xác là lý do Sự cố 3 (crash-loop) không bị phát hiện tự động — phải đợi người dùng báo lỗi. Chuẩn doanh nghiệp: CD phải tự gọi health-check endpoint sau khi deploy, tự động rollback nếu fail |
| **Giám sát & cảnh báo (Monitoring/Alerting)** | Không tìm thấy (không Prometheus/Grafana/Sentry/uptime bot nào) | 🔴 Chưa đạt | Không ai biết site sập cho tới khi người dùng thật báo lỗi — với ứng dụng thật có traffic, đây là rủi ro kinh doanh nghiêm trọng |
| **Backup & Restore dữ liệu** | `crontab -l` **rỗng hoàn toàn** — không có backup định kỳ | 🔴 Chưa đạt — **gap nghiêm trọng thứ 2** | Bản backup dùng để xử lý Sự cố 4 là **backup đầu tiên và duy nhất** từng được tạo cho DB này. Chuẩn tối thiểu: `sqlcmd BACKUP DATABASE` tự động hàng ngày + đẩy ra ngoài VPS (S3/Azure Blob) |
| **Quản lý Secret** | GitHub Secrets cho CI/CD + 2 file `.env` khác nhau (root & `apps/backend/.env`) không đồng bộ | 🔴 Chưa đạt | Không có nơi lưu secret tập trung (Vault/Azure Key Vault/1Password), không audit log khi đổi secret (chính là nguyên nhân Sự cố 4 không truy được ai đổi mật khẩu ngày 10/07) |
| **Cách ly mạng DB** | Port DB chỉ bind `14330` nội bộ, không public 1433 | 🟢 Tốt, đúng chuẩn | Đúng nguyên tắc "defense in depth" |
| **HTTPS/SSL/CDN** | Cloudflare + Let's Encrypt | 🟢 Đạt chuẩn | Ổn |
| **Container isolation** | Docker Compose, mỗi service 1 container | 🟢 Đạt chuẩn cơ bản | Phù hợp quy mô hiện tại (chưa cần Kubernetes) |
| **Đồng bộ tài liệu vs thực tế** | `docs/05-Deployment/CI_CD.md` mô tả nhánh `main` + `VPS_SSH_KEY` — thực tế đang chạy `branch_for_hung` + password | 🔴 Chưa đạt | Tài liệu bị "trôi" (doc drift) so với hệ thống thật — rất nguy hiểm vì người mới/AI agent đọc theo sẽ thao tác sai giả định |
| **Audit trail hạ tầng (ai đổi gì, khi nào)** | Không có — VPS chỉ có 1 user `jlptadmin` dùng chung | 🔴 Chưa đạt | Không thể trả lời "ai đổi `MSSQL_SA_PASSWORD` lúc 23:46 ngày 10/07" — vì không có log, không có user riêng biệt cho từng thành viên |

### 2.3. Điểm đã làm tốt (ghi nhận khách quan)

Không phải mọi thứ đều tệ — một số quyết định kiến trúc thực sự đúng hướng và không phải dự án sinh viên nào cũng làm được:
- Có CI/CD tự động thật (nhiều dự án tương đương vẫn deploy tay hoàn toàn).
- Cách ly database khỏi internet công cộng, chỉ truy cập qua SSH tunnel — đúng nguyên tắc bảo mật cơ bản mà nhiều dự án thật cũng bỏ qua.
- Dùng Flyway cho migration thay vì sửa tay schema trên production.
- Có Cloudflare che giấu IP thật + tự động SSL — giảm đáng kể bề mặt tấn công.
- Tài liệu vận hành dạng "cầm tay chỉ việc" (`docs/05-Deployment/README.md`) rất chi tiết, dễ onboard người mới — chỉ là bị lỗi thời so với thực tế hiện tại (cần cập nhật lại, xem 2.4).

### 2.4. Khuyến nghị cải thiện (xếp theo độ ưu tiên)

**P0 — Nên làm ngay, rủi ro cao nhất:**
1. **Thêm bước post-deploy smoke test vào `cd.yml`**: sau `docker compose up -d --build`, gọi `curl` vào `/actuator/health` (hoặc endpoint tương đương) với retry trong ~30-60s; nếu fail, tự động `docker compose logs backend` để đính kèm vào log CD và đánh dấu job **failed** thay vì "success" giả. Đây là fix trực tiếp cho đúng lỗ hổng đã gây ra Sự cố 3.
2. **Thiết lập backup database tự động** (cron hoặc `systemd timer`, tối thiểu 1 lần/ngày, đẩy file ra ngoài VPS — S3/Azure Blob/Google Drive, không chỉ lưu trên chính VPS đó vì nếu VPS hỏng ổ cứng thì mất cả app lẫn backup). Test thử phục hồi ít nhất 1 lần để chắc chắn backup dùng được, không chỉ "có file là xong".
3. **Cập nhật lại `docs/05-Deployment/CI_CD.md`** cho khớp thực tế (nhánh `branch_for_hung` không phải `main`, cơ chế `workflow_run` gate mới, xác nhận rõ đang dùng SSH password hay key) — tài liệu sai lệch với thực tế nguy hiểm hơn không có tài liệu, vì tạo cảm giác an toàn giả.

**P1 — Nên làm trong 1-2 sprint tới:**
4. **Tạo môi trường staging** (dù chỉ là 1 VPS nhỏ/1 bộ container riêng) để test schema migration + biến môi trường mới trước khi chạm production — toàn bộ 4 sự cố vừa rồi đều lẽ ra có thể phát hiện được ở staging trước khi ảnh hưởng người dùng thật.
5. **Tag Docker image theo commit SHA** thay vì chỉ `latest` (vd `japanese-skill-practice-platform-backend:687ef15`), giữ lại vài bản gần nhất → cho phép rollback tức thời bằng cách đổi tag thay vì phải build lại từ đầu.
6. **Tập trung hoá secret quản lý** — ít nhất ghi rõ trong 1 nơi (README nội bộ, không phải chat/trí nhớ) danh sách secret nào tồn tại ở đâu (`GOOGLE_CLIENT_ID`, `JWT_SECRET`, `MSSQL_SA_PASSWORD`...), ai có quyền đổi, và **bắt buộc thông báo cho team khi đổi bất kỳ secret nào production** — đây là nguyên nhân gốc gây ra toàn bộ chuỗi sự cố ngày 11/07.
7. **Cấp SSH key/user riêng cho từng thành viên** thay vì dùng chung 1 tài khoản `jlptadmin` — để có audit trail thật ("ai SSH vào lúc nào"), và thu hồi được quyền của 1 người mà không ảnh hưởng người khác.

**P2 — Cải thiện dài hạn khi dự án lớn hơn:**
8. Thêm giám sát cơ bản (uptime bot miễn phí như UptimeRobot/Healthchecks.io, hoặc tự host Grafana+Prometheus nếu muốn học sâu hơn) — ít nhất để biết site sập TRƯỚC KHI người dùng phải tự báo.
9. Nâng ngưỡng JaCoCo coverage dần theo thời gian (không cần nhảy vọt lên 80% ngay, nhưng 10% gần như không có tác dụng chặn lỗi).
10. Bật lại test frontend trong `ci.yml` (hiện đang comment sẵn `# npm run test`) khi frontend có test suite.
11. Cân nhắc blue-green deploy hoặc rolling update (vd 2 container backend, deploy lần lượt) nếu ứng dụng có traffic đủ lớn để downtime vài chục giây/lần deploy trở thành vấn đề kinh doanh thật.

### 2.5. Kết luận

Quy trình hiện tại **ở mức "đủ dùng cho dự án cá nhân/đồ án có traffic thấp"**, có một số nền tảng đúng hướng (CI/CD tự động, cách ly DB, Flyway, Cloudflare) nhưng **chưa đạt chuẩn một đội vận hành production thực thụ trong doanh nghiệp**, cụ thể thiếu 3 trụ cột quan trọng nhất của SRE/DevOps: **(1) verify sau khi deploy, (2) backup & khả năng phục hồi dữ liệu, (3) giám sát/cảnh báo chủ động**. Cả 4 sự cố ngày 11/07 đều có thể được phát hiện sớm hơn nhiều (ở staging, hoặc ngay sau khi deploy qua smoke test) nếu 3 trụ cột này tồn tại — thay vì phải chờ người dùng thật báo lỗi rồi mới xử lý khẩn cấp qua SSH trực tiếp vào production.
