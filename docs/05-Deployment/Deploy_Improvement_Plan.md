# Kế Hoạch Cải Thiện Quy Trình Deploy

**Dựa trên:** [`Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md`](./Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md) — Phần 2, mục 2.4.

**Nguyên tắc thực hiện:** Làm theo đúng thứ tự P0 → P1 → P2. Mỗi mục P0 phải xong và verify được trước khi coi dự án "an toàn hơn trước" — 3 mục P0 chính là 3 lỗ hổng đã trực tiếp gây ra/làm trầm trọng thêm 4 sự cố ngày 11/07/2026. Không tự ý bỏ qua P0 để nhảy sang P1/P2 cho "vui" hay "dễ làm trước".

**Trạng thái:** ⚪ Chưa bắt đầu — đây là kế hoạch, chưa triển khai.

---

## 🔴 P0 — Phải làm ngay (đóng đúng 3 lỗ hổng đã gây sự cố thật)

### P0.1 — Post-deploy smoke test trong `cd.yml`

**Vấn đề đang giải quyết:** Sự cố 3 (SMTP_PORT rỗng) — `docker compose up -d --build` báo "success" dù backend crash-loop, vì lệnh chỉ đợi container được *tạo*, không đợi app *sống*.

**Thiết kế:**
1. Sau bước `docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build`, thêm bước retry-poll gọi `curl` vào endpoint sức khoẻ nội bộ, vd:
   ```bash
   echo "Đang xác minh backend thực sự sống..."
   HEALTHY=0
   for i in $(seq 1 20); do
     if curl -sf -m 5 http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"'; then
       HEALTHY=1
       break
     fi
     sleep 5
   done
   if [ "$HEALTHY" -ne 1 ]; then
     echo "LỖI NGHIÊM TRỌNG: backend không lên healthy sau 100s. Log backend:"
     docker logs jlpt-backend --tail 100
     exit 1
   fi
   echo "Backend xác nhận UP."
   ```
2. Vì `set -e` đã có sẵn ở đầu script `cd.yml`, `exit 1` ở bước này sẽ khiến **cả job CD được đánh dấu failed** trên GitHub Actions — không còn "báo xanh giả" nữa.
3. Cân nhắc thêm 1 bước tương tự kiểm tra `frontend` (curl vào `http://127.0.0.1:80` hoặc domain thật) để bắt cả lỗi Nginx/frontend, không chỉ backend.

**File cần sửa:** `.github/workflows/cd.yml`

**Việc cần xác nhận trước khi code:** endpoint `/actuator/health` có đang bật public/nội bộ không (`management.endpoints.web.exposure.include: health` đã có trong `application.yml`) — xác nhận response thật của nó khi backend healthy để viết đúng điều kiện `grep`.

**Verify sau khi làm:** Cố tình push 1 commit làm backend crash lúc khởi động (vd đặt lại `SMTP_PORT=` rỗng như sự cố cũ) vào 1 nhánh test → xác nhận `cd.yml` tự phát hiện và báo **failed**, không còn báo "success" giả.

---

### P0.2 — Backup database tự động, đẩy ra ngoài VPS

**Vấn đề đang giải quyết:** Sự cố 4 — `crontab -l` rỗng hoàn toàn, bản backup dùng để cứu dữ liệu là bản **đầu tiên và duy nhất** từng tồn tại.

**Thiết kế:**
1. Script backup (`/opt/scripts/backup-db.sh` trên VPS):
   ```bash
   #!/bin/bash
   set -e
   source /opt/Japanese-Skill-Practice-Platform/.env
   TS=$(date +%Y%m%d_%H%M%S)
   docker exec jlpt-db /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -Q \
     "BACKUP DATABASE JLPT_LearningDB TO DISK = N'/var/opt/mssql/backup/JLPT_LearningDB_$TS.bak' WITH COMPRESSION;"
   docker cp jlpt-db:/var/opt/mssql/backup/JLPT_LearningDB_$TS.bak /opt/db-backup/
   # Xoá bản backup native bên trong container sau khi đã copy ra ngoài
   docker exec jlpt-db rm /var/opt/mssql/backup/JLPT_LearningDB_$TS.bak
   # Giữ tối đa 14 bản gần nhất trên VPS
   ls -1t /opt/db-backup/JLPT_LearningDB_*.bak | tail -n +15 | xargs -r rm --
   ```
   (Lưu ý: cần mount thêm 1 volume/thư mục `/var/opt/mssql/backup` cho service `db` trong `docker-compose.yml` nếu chưa có, để `BACKUP DATABASE` có chỗ ghi.)
2. Đẩy file `.bak` ra **ngoài VPS** (không chỉ lưu trên chính máy có thể hỏng) — chọn 1 trong các cách, ưu tiên theo độ đơn giản:
   - `rclone` đồng bộ lên Google Drive/S3/Azure Blob (miễn phí ở quy mô nhỏ).
   - Hoặc đơn giản nhất: thêm bước trong `cd.yml` dùng `scp`/`rsync` kéo bản backup mới nhất về máy khác định kỳ (ít khuyến nghị hơn vì phụ thuộc máy đó luôn bật).
3. Đăng ký `systemd timer` (khuyến nghị hơn `crontab` vì có log qua `journalctl`) chạy `backup-db.sh` hàng ngày, giờ ít traffic (vd 3h sáng).
4. **Test phục hồi thật ít nhất 1 lần**: restore 1 bản `.bak` vào 1 container SQL Server tạm, xác nhận mở được, count đúng số dòng — không tin "có file backup" là đủ, phải tin "backup dùng phục hồi được".

**File/hạ tầng cần thêm:** `/opt/scripts/backup-db.sh` (trên VPS), 1 `systemd` unit + timer (trên VPS), có thể cần sửa `docker-compose.yml` để mount thư mục backup cho `db`.

**Verify sau khi làm:** Chạy thử `backup-db.sh` thủ công 1 lần, xác nhận file `.bak` xuất hiện cả trên VPS lẫn nơi lưu ngoài; `systemctl list-timers` thấy job xuất hiện đúng lịch.

---

### P0.3 — Đồng bộ lại `docs/05-Deployment/CI_CD.md` với thực tế

**Vấn đề đang giải quyết:** Tài liệu hiện mô tả nhánh `main` + `VPS_SSH_KEY` — thực tế đang chạy `branch_for_hung` + SSH password (khôi phục sau Sự cố 1), và mô tả CD "chỉ chạy khi lý tưởng CI đã pass" — thực tế giờ đã **gate cứng** qua `workflow_run`. Tài liệu sai sự thật nguy hiểm hơn không có tài liệu vì tạo cảm giác an toàn giả cho người đọc sau (kể cả AI agent đọc theo).

**Việc cần làm:**
1. Cập nhật mục 1, 2, 3, 5 trong `CI_CD.md`: đổi `main` → `branch_for_hung`, mô tả đúng cơ chế `workflow_run`, ghi rõ hiện đang dùng `password` **và** `key` (song song, vì `VPS_SSH_KEY` chưa được thiết lập — xem P1.7), cập nhật đúng port DB (`14330`, không phải `1433` public).
2. Thêm 1 mục mới "Lịch sử sự cố" trỏ link sang `Incident_Report_2026-07-11_va_Danh_Gia_Quy_Trinh_Deploy.md` để người đọc sau biết tra cứu khi gặp lỗi tương tự.
3. Rà lại `docs/05-Deployment/README.md` (hướng dẫn DBeaver) — mục Bước 2 đang ghi ví dụ mật khẩu cứng trong tài liệu (`JlptProd@2026!Secure`) — nên xoá/thay bằng placeholder, tránh gây nhầm lẫn tưởng đó là mật khẩu thật.

**File cần sửa:** `docs/05-Deployment/CI_CD.md`, `docs/05-Deployment/README.md`

**Verify:** Đọc lại toàn bộ `CI_CD.md` sau khi sửa, đối chiếu từng câu với `.github/workflows/cd.yml`/`ci.yml` thật — không còn câu nào mô tả sai hành vi hiện tại.

---

## 🟡 P1 — Nên làm trong 1-2 sprint tới

### P1.4 — Môi trường staging

**Mục tiêu:** Bất kỳ thay đổi schema DB hay biến môi trường mới nào được test ở staging trước khi chạm production — cả 4 sự cố ngày 11/07 lẽ ra phát hiện được ở đây.

**Thiết kế tối thiểu (không cần VPS riêng ngay từ đầu):**
1. Thêm `docker-compose.staging.yml` (tương tự `docker-compose.prod.yml` nhưng đổi port ra ngoài khác, DB name khác, ví dụ `JLPT_LearningDB_staging`) chạy **cùng VPS** nhưng khác container name/port — cách rẻ nhất để bắt đầu, chấp nhận rủi ro "cùng máy vật lý" đổi lấy chi phí bằng 0.
2. Thêm 1 nhánh `develop` (hoặc `staging`) trigger workflow deploy riêng vào bộ container staging này.
3. Quy trình mới: merge vào `develop` trước → tự động deploy staging → test tay/tự động → merge `develop` → `branch_for_hung` mới thực sự lên production.
4. Khi ngân sách cho phép, tách sang VPS vật lý riêng để không chia sẻ tài nguyên với production.

**File cần thêm:** `docker-compose.staging.yml`, `.github/workflows/cd-staging.yml`

---

### P1.5 — Tag Docker image theo commit SHA (cho phép rollback)

**Vấn đề đang giải quyết:** Image hiện chỉ tag `latest`, build mới ghi đè bản cũ — không có "nút quay lại bản trước".

**Thiết kế:**
1. Sửa `cd.yml`: build image với tag là commit SHA (`docker compose build` rồi `docker tag ...:latest ...:$GITHUB_SHA`), đồng thời giữ `latest` trỏ tới bản mới nhất.
2. Giữ lại N bản gần nhất (vd 5), dọn bản cũ hơn bằng `docker image prune` có điều kiện lọc theo tag.
3. Thêm 1 workflow thủ công (`workflow_dispatch`) riêng `rollback.yml`: nhận input là commit SHA muốn quay lại, SSH vào VPS, đổi tag đang chạy sang SHA đó, `docker compose up -d` lại — không cần build lại từ đầu, rollback trong vài giây thay vì vài phút.

**File cần thêm/sửa:** `.github/workflows/cd.yml`, thêm mới `.github/workflows/rollback.yml`

---

### P1.6 — Tài liệu hoá và quy trình thông báo khi đổi secret

**Vấn đề đang giải quyết:** Nguyên nhân gốc của Sự cố 4 — không ai biết `.env` trên VPS bị đổi `MSSQL_SA_PASSWORD` lúc 23:46 ngày 10/07, không có audit trail, không có quy trình thông báo team.

**Việc cần làm:**
1. Tạo `docs/05-Deployment/SECRETS.md` (không chứa giá trị thật, chỉ chứa **danh mục**): tên biến, dùng ở đâu (`GitHub Secrets` / root `.env` VPS / `apps/backend/.env`), ai là người có quyền đổi, lần đổi gần nhất + lý do (ghi tay, cập nhật mỗi lần đổi — đơn giản nhưng hiệu quả hơn không có gì).
2. Quy tắc bắt buộc: **bất kỳ ai đổi secret trên VPS đều phải báo trong group/channel chung của team trước khi đổi**, kèm lý do — không cần công cụ phức tạp (Vault/Key Vault) ngay, quy tắc giao tiếp đơn giản đã đủ ngăn được chính xác sự cố vừa xảy ra.

**File cần thêm:** `docs/05-Deployment/SECRETS.md`

---

### P1.7 — SSH access riêng cho từng thành viên (thay vì dùng chung `jlptadmin`)

**Vấn đề đang giải quyết:** Không audit được ai SSH vào VPS lúc nào — liên quan trực tiếp tới việc không truy được ai đổi mật khẩu SA.

**Việc cần làm:**
1. Tạo user Linux riêng cho từng thành viên có quyền deploy (vd `useradd -m -G docker <ten>`), mỗi người tự tạo cặp SSH key riêng, add public key vào `authorized_keys` của user đó.
2. Cấu hình `sudo` có giới hạn nếu cần quyền root cho tác vụ cụ thể, tránh cấp full sudo tuỳ tiện.
3. Sau khi mọi người đã chuyển sang user riêng, cân nhắc khoá hẳn đăng nhập bằng password cho `jlptadmin` (`PasswordAuthentication no` trong `sshd_config`) — lúc đó `cd.yml` mới thực sự có thể bỏ hẳn `password:` (xem lại P0 trong `Incident_Report...md`, mục "Việc cần làm để đúng chuẩn" của Sự cố 1) mà không sợ tự khoá luôn đường vào của chính mình.

**Việc cần làm trên VPS:** tạo user, cấu hình `sshd_config`, cấp key.

---

## 🟢 P2 — Cải thiện dài hạn khi dự án lớn hơn

| # | Việc cần làm | Ghi chú |
|---|---|---|
| P2.8 | Thêm uptime monitoring miễn phí (UptimeRobot/Healthchecks.io) trỏ vào `https://sakuji.online` + `/actuator/health` | Rẻ nhất, nhanh nhất trong nhóm P2 — nên làm sớm dù xếp P2 |
| P2.9 | Nâng dần ngưỡng JaCoCo coverage trong `pom.xml` (hiện `0.10`) | Tăng từng bước, không nhảy vọt gây shock team |
| P2.10 | Bật lại `npm run test` trong `ci.yml` (hiện đang comment) khi frontend có test suite thật | Cần viết test frontend trước |
| P2.11 | Cân nhắc rolling update/blue-green nếu downtime vài chục giây mỗi lần deploy bắt đầu ảnh hưởng thật tới người dùng | Chỉ cần khi traffic đủ lớn, chưa cấp thiết ở quy mô hiện tại |

---

## Bảng theo dõi tiến độ

| Mục | Trạng thái | Ghi chú |
|---|---|---|
| P0.1 — Post-deploy smoke test | 🟢 Xong, verify qua deploy thật | `/actuator/health` từng trả `DOWN` giả do `MailHealthIndicator` — đã tắt riêng (`management.health.mail.enabled: false`) trước khi bật smoke test, tránh false-positive fail mọi lần deploy |
| P0.2 — Backup DB tự động | 🟡 Gần xong (quyết định hoãn có chủ đích) | Script (`/opt/scripts/backup-db.sh`) + systemd timer (`backup-db.timer`, 3h sáng hàng ngày, giữ 14 bản gần nhất) đã chạy thật trên VPS, **đã test restore thành công** (27 bảng, 14 dòng `student_users` khớp dữ liệu sống, container/volume test đã dọn dẹp sạch). **Chưa làm — hoãn theo yêu cầu chủ dự án (11/07/2026):** đẩy bản backup ra ngoài VPS (rclone/S3/Google Drive...), cần credential cloud storage mà AI không tự có. **Rủi ro còn tồn tại:** nếu VPS hỏng ổ cứng/mất toàn bộ, backup nằm cùng máy cũng mất theo — ưu tiên làm nốt phần này trước khi coi P0 hoàn tất 100%. |
| P0.3 — Đồng bộ tài liệu CI/CD | 🟢 Xong | `CI_CD.md` viết lại khớp `cd.yml`/`ci.yml` thật + thêm mục "Lịch sử sự cố"; `README.md` xoá mật khẩu ví dụ cứng, **sửa thêm 1 lỗi phát hiện được**: lệnh SSH tunnel hướng dẫn sai cổng phía VPS (`1433` → phải là `14330`) |
| P1.4 — Staging environment | 🟢 Xong, verify qua 2 lần deploy thật (GitHub Actions, không chỉ SSH tay) | `docker-compose.staging.yml` (standalone, KHÔNG override) + `cd-staging.yml` (gate qua CI trên nhánh `develop`). Container/port/volume/image-repo tách biệt hoàn toàn khỏi production. Lần deploy thật ĐẦU TIÊN qua GitHub Actions fail 2 lần liên tiếp, mỗi lần lộ ra 1 bug thật (xem `Deploy_Diagram.md` mục 8 "Bài học thực tế" để đọc chi tiết): (1) `/opt` chỉ ghi được bởi root → phải bootstrap quyền 1 lần; (2) migration `V2__mock_data.sql` hard-code `USE JLPT_LearningDB` → sửa luôn cho cả production (không chỉ staging); (3) `nginx.conf` hard-code upstream hostname `backend` → phải giữ nguyên tên service Compose là `backend` thay vì `backend-staging`. Lần deploy thứ 3 (commit `b68b3311`, qua đúng GitHub Actions, không phải chạy tay) **pass hoàn toàn** — `CD Staging (Deploy to VPS)`: success. Xác nhận cuối: `jlpt-backend-staging` + `jlpt-frontend-staging` chạy song song với `jlpt-backend`/`jlpt-frontend` production trên cùng VPS, cả 2 `/actuator/health` đều UP cùng lúc. ⚠️ Cổng staging (8081/8082) hiện chỉ truy cập được từ trong VPS hoặc qua SSH tunnel — muốn xem qua trình duyệt cần mở thêm rule firewall/NSG cho `8082`, chưa làm (cần xác nhận chủ dự án trước khi mở thêm cổng public trên hạ tầng production thật). |
| P1.5 — Tag image theo SHA + rollback workflow | 🟢 Xong, verify qua deploy thật | `cd.yml` gắn tag `jlpt-backend`/`jlpt-frontend` theo `commit SHA` **sau khi** smoke test pass (chỉ SHA "đã biết chạy được" mới thành ứng viên rollback), giữ 5 bản gần nhất, ghi `deploy-history.log`. Đã verify thật trên VPS: deploy commit `f9e70f6e` → tag `jlpt-backend:f9e70f6e...` xuất hiện đúng, `deploy-history.log` ghi đúng dòng, `/actuator/health` UP. `rollback.yml` (`workflow_dispatch`, nhận input SHA) đã viết xong nhưng **chưa test thật bằng 1 lần rollback thực tế** (hiện chỉ có 1 SHA lịch sử nên chưa có bản "trước đó" để rollback về — sẽ tự nhiên được test khi có ≥ 2 lần deploy SHA khác nhau và cần dùng thật). |
| P1.6 — SECRETS.md + quy trình thông báo | 🟢 Xong | `docs/05-Deployment/SECRETS.md` liệt kê đủ 7 GitHub Secrets + 4 biến root `.env` + 2 biến `apps/backend/.env` (không kèm giá trị thật), có cột "ai đổi được"/"lần đổi gần nhất", và quy tắc bắt buộc báo trước khi đổi secret. |
| P1.7 — SSH riêng từng người | ⚪ Chưa làm — cần thông tin từ chủ dự án | Repo hiện có nhánh riêng của ít nhất 5 người (`branch_tienDat`, `Branch_for_Minh`, `branch_Duy`, `branch_for_Lanh`, `hung`) → đúng là có nhiều người cần SSH riêng thật. Nhưng để tạo user Linux + `authorized_keys` thật trên VPS cần **public key SSH của từng người** — không thể tự suy đoán/tạo hộ. Cần chủ dự án cung cấp danh sách tên + public key trước khi làm tiếp. |
| P2.8-11 | ⚪ Chưa làm | |
