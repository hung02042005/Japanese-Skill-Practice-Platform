# Kế Hoạch Migration: SQL Server → MySQL 8

> **Trạng thái**: ✅ **ĐÃ THỰC HIỆN** (2026-07-16) — Phase 0-4 + 6 xong, đã verify trên MySQL 8.4 thật.
> **Phase 5 (chuyển dữ liệu production): BỎ** — quyết định không migrate dữ liệu, DB dựng lại từ migration.
> **Ngày lập**: 2026-07-16
> **Phạm vi**: Toàn bộ backend, infra (Docker/CI-CD), test, docs
> **Quyết định đã chốt**: QĐ-1 utf8mb4 ✅ | QĐ-2 Cách A (ép container UTC) ✅ | MySQL 8.4 LTS ✅

---

## ⚠️ ĐÍNH CHÍNH — 3 chỗ bản kế hoạch này viết SAI/THIẾU

Phát hiện khi thực thi. Ghi lại để người đọc sau không bị dẫn sai:

| # | Kế hoạch nói | Thực tế |
|---|---|---|
| 1 | Mục 6: *"Không có `TOP`, `OFFSET/FETCH`..."* | **SAI** — V2 có **24 câu `SELECT TOP`**. Sai vì lúc khảo sát tôi grep nhầm trên file đã bị convert dở (đã xoá `TOP`), không phải bản gốc ở HEAD. |
| 2 | Không hề nhắc **filtered index** | **THIẾU — và đây là vấn đề lớn nhất.** V1 có **7 filtered index** (`CREATE INDEX ... WHERE`); MySQL không hỗ trợ. 2 cái là UNIQUE gắn với nghiệp vụ → bỏ `WHERE` sẽ phá logic chứ không chỉ giảm hiệu năng. |
| 3 | Không hề nhắc toán tử `+` nối chuỗi | **THIẾU — nguy hiểm nhất về dữ liệu.** V2 dùng `'...' + LOWER(romaji) + '...'`. Trong MySQL `+` là **phép cộng số học** → ghi `0` vào `audio_url` cho toàn bộ 142 dòng kana **mà không báo lỗi**. |

**Bài học**: khảo sát phải chạy trên bản ở HEAD, không chạy trên working tree đang dở.

---

## 0. TL;DR — Đọc phần này trước

**Phát hiện quan trọng nhất**: Working tree hiện tại **đã có một bản convert dở dang và ĐANG HỎNG** (6 file migration bị sửa, chưa commit). Đây không phải dự án bắt đầu từ số 0 — việc đầu tiên phải làm là **quyết định xử lý đống thay đổi chưa commit này**.

Bản convert dở đó đã làm đúng phần DDL, nhưng làm hỏng phần procedural:

| Trạng thái | Chi tiết |
|---|---|
| ✅ Đã convert đúng | `IDENTITY(1,1)`→`AUTO_INCREMENT`, `NVARCHAR`→`VARCHAR`, `NVARCHAR(MAX)`→`LONGTEXT` (26 cột), `DATETIME2`→`DATETIME`, `BIT`→`BOOLEAN`, xoá `GO` |
| ❌ **SQL hỏng** | `V2`: 41 câu `SET @x = (SELECT ... ;` **thiếu dấu `)` đóng** → syntax error |
| ❌ **SQL hỏng** | `V2`: 21 khối `IF NOT EXISTS(...) INSERT` — T-SQL procedural, **không chạy được** ngoài stored procedure trong MySQL |
| ❌ **SQL hỏng** | `V3`: 12 khối `IF NOT EXISTS` tương tự |
| ❌ **SQL hỏng** | `V25`: còn nguyên `sys.check_constraints`, `OBJECT_ID()`, `EXEC()`, `dbo.` + cũng thiếu `)` |
| ⚠️ **Bug ngầm** | 38 chỗ `SYSUTCDATETIME()` → `CURRENT_TIMESTAMP`: **đổi từ UTC sang giờ local của server** |
| ⚠️ **Thiếu** | **Chưa khai báo `utf8mb4`** ở bất kỳ bảng nào — nghiêm trọng với nội dung tiếng Nhật |

**Khuyến nghị**: `git checkout` revert 6 file migration về HEAD, làm lại theo Phase 2 bên dưới một cách có kiểm soát. Lý do: bản hiện tại là kết quả find/replace máy móc, không thể review từng dòng để tin được, và nó thiếu 2 quyết định thiết kế quan trọng (charset + timezone) mà nếu vá sau sẽ phải sửa lại toàn bộ schema.

**Tin tốt**: Tầng Java gần như không phải sửa. 35/35 entity dùng `GenerationType.IDENTITY` (MySQL hỗ trợ qua `AUTO_INCREMENT`), không có dialect hard-code, các native query đều dùng cú pháp portable (`LIKE`, `LOWER`, `YEAR`, `MONTH`, `CAST(... AS DATE)`, `COUNT`) — chạy được nguyên trên MySQL.

**Ước lượng**: 2–3 ngày công cho backend + infra. Rủi ro chính nằm ở dữ liệu production, không ở code.

---

## 1. Hai Quyết Định Thiết Kế Phải Chốt Trước Khi Code

Hai điểm này ảnh hưởng tới toàn bộ schema — chốt sai thì phải làm lại từ đầu.

### QĐ-1: Charset & Collation — `utf8mb4` (BẮT BUỘC)

Đây là hệ thống học **tiếng Nhật**: kanji, kana, và cả emoji trong nội dung bài học/ticket. Bản convert hiện tại đổi `NVARCHAR`→`VARCHAR` mà **không khai báo charset**.

- SQL Server `NVARCHAR` = Unicode **mặc định**.
- MySQL `VARCHAR` = Unicode **chỉ khi** charset là `utf8mb4`.

Nếu server MySQL cấu hình mặc định `latin1` (phổ biến ở MySQL 5.x và nhiều image cũ), **toàn bộ kanji sẽ thành `?` khi ghi** — mất dữ liệu im lặng, không báo lỗi.

**Chốt**:
- Charset: `utf8mb4`
- Collation: `utf8mb4_unicode_ci` — `_ci` (case-insensitive) là **bắt buộc**, vì code đang so sánh kiểu `LOWER(status) = LOWER(:status)` và `LIKE` với giả định không phân biệt hoa/thường như SQL Server.
- Khai báo ở **cả 3 tầng** để không phụ thuộc default của server:
  1. Server: `--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci` (docker-compose)
  2. Mỗi `CREATE TABLE`: `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`
  3. JDBC URL: `?characterEncoding=utf8`

> **Lưu ý index**: `utf8mb4` = 4 byte/ký tự, giới hạn index của InnoDB là 3072 byte → cột index tối đa `VARCHAR(768)`.
> **Đã kiểm tra: không có cột nào vi phạm** (rộng nhất là `VARCHAR(500)` và không được index). Không cần xử lý gì.

### QĐ-2: Timezone — `SYSUTCDATETIME()` không tương đương `CURRENT_TIMESTAMP`

Bản convert hiện tại thay 38 chỗ `SYSUTCDATETIME()` (UTC) bằng `CURRENT_TIMESTAMP` (giờ local của server). Với VPS chạy `Asia/Ho_Chi_Minh` thì **mọi timestamp lệch +7 giờ** so với dữ liệu cũ.

Ảnh hưởng trực tiếp: `locked_until` (khoá tài khoản), `last_login_at`, OTP/token expiry, `last_studied_at` (dùng cho thống kê streak theo `YEAR`/`MONTH`).

**Chọn 1 trong 2** (khuyến nghị **Cách A**):

- **Cách A — ép container MySQL chạy UTC**: set `TZ=UTC` + `--default-time-zone=+00:00`. Giữ nguyên `CURRENT_TIMESTAMP` trong SQL, ngữ nghĩa khớp lại với `SYSUTCDATETIME()` cũ. Ít sửa code nhất, nhất quán với dữ liệu lịch sử.
- **Cách B — dùng `UTC_TIMESTAMP()`** ở cả 38 chỗ. Đúng tường minh nhưng phải sửa nhiều và vẫn phải xử lý riêng cho `DEFAULT` của cột.

Đồng thời thêm `serverTimezone=UTC` vào JDBC URL để driver không tự suy diễn timezone.

---

## 2. Phân Rã Công Việc Theo Phase

### Phase 0 — Dọn dẹp & chuẩn bị (bắt buộc làm trước)

| # | Việc | File |
|---|---|---|
| 0.1 | Revert bản convert hỏng về HEAD | `git checkout HEAD -- apps/backend/src/main/resources/db/migration/` |
| 0.2 | Tạo branch riêng `feat/migrate-mysql` | — |
| 0.3 | Backup DB SQL Server production (`.bak` + verify restore được) | VPS |
| 0.4 | Chốt QĐ-1 và QĐ-2 ở mục 1 | — |

> 0.3 không được bỏ qua: Phase 5 (chuyển dữ liệu) là bước duy nhất không rollback được bằng git.

### Phase 1 — Dependency & cấu hình kết nối

| # | Việc | File | Chi tiết |
|---|---|---|---|
| 1.1 | Đổi JDBC driver | `apps/backend/pom.xml:70-74` | `com.microsoft.sqlserver:mssql-jdbc` → `com.mysql:mysql-connector-j` |
| 1.2 | Đổi Flyway module | `apps/backend/pom.xml:86-89` | `flyway-sqlserver` → `flyway-mysql` |
| 1.3 | Đổi datasource | `application.yml:6-9` | URL → `jdbc:mysql://127.0.0.1:3306/JLPT_LearningDB?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC`; driver → `com.mysql.cj.jdbc.Driver` |
| 1.4 | Xem lại `ddl-auto` | `application.yml:14` | Đang là `update` — **nên đổi sang `validate`**. `update` trên MySQL sẽ tự sinh bảng thiếu charset, phá vỡ QĐ-1 và giẫm chân Flyway |

> **1.4 quan trọng hơn vẻ ngoài**: `ddl-auto: update` đang che giấu sai lệch giữa entity và migration. Khi đổi sang MySQL, nó sẽ âm thầm tạo cột `latin1`. Đổi sang `validate` để Flyway là nguồn sự thật duy nhất (đúng ADR — mọi schema change qua Flyway).

### Phase 2 — Viết lại migration (phần nặng nhất)

Không dùng find/replace. Xử lý theo từng nhóm vấn đề:

| # | Vấn đề | Số chỗ | Cách xử lý |
|---|---|---|---|
| 2.1 | Kiểu dữ liệu + `GO` + `IDENTITY` | ~591 dòng V1 | Áp dụng lại như bản cũ đã làm (phần này **đã đúng**, có thể tham chiếu `git stash` bản cũ) |
| 2.2 | **Thêm charset mỗi bảng** | ~35 bảng | Thêm `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci` — **bản cũ thiếu hoàn toàn** |
| 2.3 | Timezone | 38 chỗ | Theo QĐ-2 |
| 2.4 | `IF NOT EXISTS (SELECT 1 ...) INSERT` | 21 (V2) + 12 (V3) | Đổi sang **`INSERT ... ON DUPLICATE KEY UPDATE`**, hoặc `INSERT ... SELECT ... WHERE NOT EXISTS (...)`. Yêu cầu cột định danh (`email`, `setting_key`) có UNIQUE constraint |
| 2.5 | `SET @x = (SELECT ...;` thiếu `)` | 41 (V2) | MySQL **có** hỗ trợ user variable `SET @x = (SELECT ...)` — chỉ cần sửa đúng dấu ngoặc. Nhưng cần đảm bảo Flyway chạy cùng 1 session (xem cảnh báo dưới) |
| 2.6 | `V25`: `sys.check_constraints` / `OBJECT_ID` / `EXEC` / `dbo.` | 1 file | **Viết lại hoàn toàn.** MySQL 8 đặt tên CHECK constraint khác; drop bằng `ALTER TABLE tickets DROP CHECK <tên>`. Tra tên thật qua `information_schema.table_constraints` thay cho `sys.check_constraints` |
| 2.7 | `V26`, `V27` | 2 file nhỏ | Rà lại theo cùng nguyên tắc |

> ⚠️ **Bẫy ở 2.5**: user variable `@x` chỉ tồn tại trong phạm vi **connection**. Flyway mặc định chạy cả file trên 1 connection nên OK — nhưng nếu sau này tách file thì biến sẽ mất. Cân nhắc thay bằng subquery trực tiếp trong `INSERT ... SELECT` để bỏ hẳn phụ thuộc vào biến session.

> ⚠️ **MySQL không có transactional DDL**: SQL Server rollback được cả migration khi lỗi giữa chừng; MySQL thì **không** — migration lỗi ở dòng 200/591 sẽ để lại schema dở dang, phải xoá DB làm lại. Vì vậy **bắt buộc test trên DB trống nhiều lần** trước khi đụng tới staging.

> ℹ️ **CHECK constraint**: schema dùng `CHECK (status IN (...))` rất nhiều. MySQL **chỉ thực thi CHECK từ 8.0.16+** — bản cũ hơn *parse rồi bỏ qua im lặng*. → **Bắt buộc MySQL ≥ 8.0.16**, nên pin `mysql:8.4` (LTS).

### Phase 3 — Infrastructure

| # | Việc | File |
|---|---|---|
| 3.1 | Đổi image `mcr.microsoft.com/mssql/server:2022-latest` → `mysql:8.4`; env `ACCEPT_EULA`/`MSSQL_SA_PASSWORD`/`MSSQL_PID` → `MYSQL_ROOT_PASSWORD`/`MYSQL_DATABASE`/`MYSQL_USER`/`MYSQL_PASSWORD`; port `14330:1433` → `13306:3306`; command thêm `--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --default-time-zone=+00:00` | `docker-compose.yml` |
| 3.2 | Healthcheck: `sqlcmd -Q "SELECT 1"` → `mysqladmin ping -h localhost` | `docker-compose.yml` (+ staging) |
| 3.3 | Volume `sqlserver_data:/var/opt/mssql` → `mysql_data:/var/lib/mysql` | cả 3 compose |
| 3.4 | Tương tự cho staging (port `14331:1433` → `13307:3306`, volume `sqlserver_data_staging`) | `docker-compose.staging.yml:29-43` |
| 3.5 | Đổi `DATABASE_PASSWORD=${MSSQL_SA_PASSWORD}` → biến MySQL mới | `docker-compose.prod.yml:26`, `staging.yml:69` |
| 3.6 | Đổi `MSSQL_SA_PASSWORD` → `MYSQL_ROOT_PASSWORD` + `MYSQL_PASSWORD` | `.env.example:4-6`, `apps/backend/.env.example` |

> **3.1 — điểm dễ bỏ sót**: `MYSQL_DATABASE` tự tạo DB lúc container init → **bỏ được hẳn bước "chờ rồi CREATE DATABASE" trong CI/CD** (mục 4.1).

> ⚠️ **Đổi tên volume = mất dữ liệu dev.** Volume `sqlserver_data` sẽ mồ côi; không tự động chuyển sang `mysql_data`. Đây là điều mong muốn ở dev nhưng **tuyệt đối không được xảy ra bất ngờ ở prod** — xem Phase 5.

### Phase 4 — CI/CD

| # | Việc | File |
|---|---|---|
| 4.1 | Bỏ khối `until docker exec ... sqlcmd ... IF NOT EXISTS(SELECT * FROM sys.databases ...) CREATE DATABASE`; thay bằng `mysqladmin ping` (DB đã do `MYSQL_DATABASE` tạo) | `cd.yml:82`, `cd-staging.yml:71` |
| 4.2 | Đổi thông báo lỗi biến env | `cd.yml:58` |
| 4.3 | Cập nhật secrets trên VPS + GitHub | `docs/05-Deployment/SECRETS.md` |

### Phase 5 — Chuyển dữ liệu Production (rủi ro cao nhất)

Đây là bước **không rollback được bằng git**. Chỉ làm sau khi Phase 1–4 đã xanh trên staging.

1. Bật maintenance mode / chấp nhận downtime.
2. Backup SQL Server (`.bak`) + **verify restore được** trên máy khác.
3. Export dữ liệu — **không dùng tool "generate script" mặc định của SSMS** (sinh T-SQL `IDENTITY_INSERT`, không chạy được trên MySQL). Ưu tiên export **CSV per-table với encoding UTF-8**, import bằng `LOAD DATA INFILE`.
4. **Verify kanji/kana không bị mangle** — query thử vài bản ghi có kanji ở `kanji.character_value`, `example_sentence_jp`. Đây là lỗi hay gặp nhất khi chuyển sang MySQL.
5. Đối chiếu `COUNT(*)` từng bảng giữa 2 DB.
6. Reset `AUTO_INCREMENT` từng bảng về `MAX(id)+1` — **nếu quên, insert mới sẽ lỗi duplicate key**.
7. Kiểm tra `flyway_schema_history`: quyết định baseline lại hay import lịch sử cũ.

> **Về `flyway_schema_history`**: DB production hiện có bảng này với checksum của migration **bản SQL Server**. Sau khi viết lại thành MySQL, checksum sẽ lệch → Flyway báo lỗi validate. Config hiện tại có `validate-on-migrate: false` nên sẽ **không báo lỗi mà âm thầm bỏ qua** — nguy hiểm hơn. Xử lý sạch nhất: **DB MySQL mới → baseline lại từ đầu**, vì đây là DB hoàn toàn mới chứ không phải DB cũ được nâng cấp.

### Phase 6 — Test & Docs

| # | Việc | File |
|---|---|---|
| 6.1 | `MODE=MSSQLServer` → `MODE=MySQL` | `apps/backend/src/test/resources/application.yml:7` |
| 6.2 | **Cân nhắc bỏ H2, dùng Testcontainers MySQL** | như trên |
| 6.3 | Sửa comment "SQL Server" | `StudentContentProgressRepository.java:132` |
| 6.4 | Cập nhật `database/init.sql` (schema SQL Server legacy) — hoặc **xoá nếu đã chết** (Flyway V1 đã là nguồn sự thật) | `database/init.sql` |
| 6.5 | Cập nhật docs (~30 file: `CLAUDE.md`, `AGENTS.md`, `CONSTITUTION.md`, `JLPT_database.md`, `05-Deployment/*`) | — |
| 6.6 | Thêm ADR-009 "SQL Server → MySQL 8" ghi lại quyết định + lý do | `CLAUDE.md` |

> **6.2 đáng cân nhắc nghiêm túc**: H2 với `MODE=MySQL` chỉ *giả lập* MySQL — nó **không** bắt được đúng các thứ đang là rủi ro chính ở đây: hành vi `utf8mb4`, CHECK constraint, `ON DUPLICATE KEY UPDATE`, timezone. Test xanh trên H2 mà production vẫn hỏng là kịch bản rất thực tế. Testcontainers chạy MySQL thật, đắt hơn vài giây/lần chạy nhưng test đúng thứ cần test.

---

## 3. Thứ Tự Thực Hiện

```
Phase 0 (dọn + backup + chốt QĐ-1, QĐ-2)
   └─> Phase 1 (pom + application.yml)
          └─> Phase 2 (viết lại migration)  ← nặng nhất
                 └─> Phase 3 (docker-compose)
                        └─> test local: docker compose up trên DB TRỐNG, chạy lại nhiều lần
                               └─> Phase 4 (CI/CD) ─> deploy staging ─> smoke test
                                      └─> Phase 5 (dữ liệu production)  ← không rollback được
                                             └─> Phase 6 (test + docs)
```

Phase 6.1–6.3 có thể làm song song với Phase 2.

---

## 4. Định Nghĩa "Xong" (Definition of Done) — ✅ ĐÃ VERIFY trên MySQL 8.4.10 thật

- [x] `docker compose up` từ **volume trống** → Flyway chạy hết V1→V27 (`Successfully applied 6 migrations ... now at version v27`)
- [x] Chạy lại V2/V3/V26 trên DB đã có data → **idempotent**, row count không đổi
- [x] Mọi bảng (27/27) đều `utf8mb4_unicode_ci` — query `information_schema` trả về rỗng khi lọc bảng khác charset
- [x] Kanji round-trip đúng (`山`/`日`/`語`/`海` + onyomi katakana + kunyomi hiragana), **emoji `🌸`/`🎉` cũng đúng** — không có `?`
- [x] `mvn test` — **115/115 pass, BUILD SUCCESS**
- [x] `/actuator/health` → `{"status":"UP"}`
- [x] Login end-to-end student + admin → 200 + JWT (bcrypt hash từ seed hoạt động)
- [x] Đọc nội dung tiếng Nhật qua API: `/api/kanji?level=N5`, `/api/kana`, `/api/dictionary/search?q=山` → 200, dữ liệu đúng
- [x] `ddl-auto: validate` pass → 35 entity khớp schema MySQL, không có drift
- [x] CHECK constraint **được thực thi thật** (insert giá trị sai → ERROR 3819); V25 `'assigned'` được chấp nhận, giá trị lạ bị chặn
- [x] Timezone: `NOW()` = `UTC_TIMESTAMP()` = 16:00 UTC khi giờ máy 23:00 (+07) → container chạy UTC đúng
- [x] `active_name_key`/`review_deck_key`: 5/5 test nghiệp vụ pass (chặn trùng tên; cho tạo lại sau soft-delete; 1 review deck/student; deck thường không bị ràng buộc; ON DELETE CASCADE vẫn chạy)
- [ ] ~~`COUNT(*)` khớp giữa SQL Server cũ và MySQL mới~~ — **N/A**, không migrate dữ liệu
- [ ] ~~Reset `AUTO_INCREMENT`~~ — **N/A**, DB dựng mới từ migration

---

## 5. Bảng Rủi Ro

| Rủi ro | Mức | Giảm thiểu |
|---|---|---|
| **Kanji thành `?`** do thiếu utf8mb4 | 🔴 Cao | QĐ-1: khai báo charset cả 3 tầng; test insert kanji trong DoD |
| **Timestamp lệch 7h** (UTC→local) | 🔴 Cao | QĐ-2: ép container UTC |
| **Mất dữ liệu prod** ở Phase 5 | 🔴 Cao | Backup + verify restore **trước**; đổi tên volume có chủ đích |
| Migration lỗi giữa chừng (không có transactional DDL) | 🟡 TB | Test nhiều lần trên DB trống trước khi lên staging |
| CHECK constraint bị bỏ qua im lặng | 🟡 TB | Pin `mysql:8.4` (≥ 8.0.16) |
| Checksum `flyway_schema_history` lệch | 🟡 TB | Baseline lại trên DB mới |
| `ddl-auto: update` tự tạo bảng sai charset | 🟡 TB | Đổi sang `validate` (1.4) |
| Quên reset AUTO_INCREMENT | 🟡 TB | Có trong DoD |
| H2 `MODE=MySQL` không bắt được lỗi thật | 🟢 Thấp | Cân nhắc Testcontainers (6.2) |

---

## 6. Những Thứ KHÔNG Phải Sửa (đã kiểm chứng)

Ghi lại để không mất thời gian rà lại:

- **35/35 entity** dùng `GenerationType.IDENTITY` → MySQL map sang `AUTO_INCREMENT`, **không sửa gì**. ✅ Đã xác nhận: `ddl-auto: validate` pass, không có drift entity↔schema.
- **Không có** Hibernate dialect hard-code ở đâu → Hibernate tự nhận diện từ driver
- **Native query trong code Java đều portable**: `LIKE`, `LOWER()`, `YEAR()`, `MONTH()`, `CAST(x AS DATE)`, `COUNT(DISTINCT ...)` — cú pháp giống nhau ở cả 2 DBMS
- **Không có** cột index nào > 768 ký tự → không vướng giới hạn index utf8mb4
- **Không có** identifier trùng từ khoá dành riêng của MySQL
- **Frontend**: không có tham chiếu DB nào
- `NVARCHAR(MAX)` → `LONGTEXT` (26 cột)

> ⚠️ Bản đầu của mục này còn ghi *"không có `TOP`/`MERGE`/`ISNULL`..."* — phần `TOP` là **SAI**, xem mục Đính Chính đầu file. `MERGE`, `ISNULL`, `CHARINDEX`, `DATEADD`, `NEWID`, `UNIQUEIDENTIFIER` thì đúng là không có.

---

## 7. Câu Hỏi — trạng thái

| # | Câu hỏi | Kết quả |
|---|---|---|
| 1 | Xử lý working tree đang hỏng | ✅ **Đã revert về HEAD** rồi viết lại có kiểm soát |
| 2 | QĐ-2 timezone | ✅ **Cách A** — ép container UTC (`--default-time-zone=+00:00` + `TZ=UTC`) |
| 3 | Dữ liệu production | ✅ **Không migrate** → Phase 5 bỏ hẳn |
| 4 | Testcontainers | ⏳ **CHƯA** — vẫn dùng H2 `MODE=MySQL`. Xem cảnh báo mục 6.2: H2 không bắt được lỗi utf8mb4/CHECK/timezone thật |
| 5 | `database/init.sql` | ⏳ **CHƯA XỬ LÝ** — vẫn là schema SQL Server. Không code nào tham chiếu (chỉ docs), và **đã lỗi thời từ trước** (header ghi "V1→V6" trong khi thực tế đã tới V27). Cần quyết định: xoá hay viết lại |
| 6 | MySQL 8.4 LTS | ✅ **Dùng 8.4** (thoả >= 8.0.16) |

## 8. Việc còn lại

1. **`apps/backend/.env`** (gitignored, không nằm trong git) vẫn trỏ `DATABASE_URL` vào **Azure SQL Server** (`jlpt-sqlserver.database.windows.net`). Driver MySQL không nhận URL này → phải cập nhật thủ công trên từng máy dev và trên VPS. `docker-compose.yml` nay đã tự khai báo `DATABASE_*` trỏ vào service `db` nên `docker compose up` không còn phụ thuộc file này.
2. **Secrets trên VPS + GitHub**: đổi `MSSQL_SA_PASSWORD` → `MYSQL_ROOT_PASSWORD` / `MYSQL_USER` / `MYSQL_PASSWORD` (xem `docs/05-Deployment/SECRETS.md`).
3. **Volume cũ** `sqlserver_data` / `sqlserver_data_staging` trên VPS sẽ mồ côi sau khi deploy — xoá tay khi đã chắc chắn không cần.
4. Quyết định câu 4 và 5 ở bảng trên.
5. Còn ~30 file docs nhắc SQL Server (`docs/05-Deployment/*`, `JLPT_database.md`, `sql-performance.md`...) — mới cập nhật `CLAUDE.md`, `AGENTS.md`, `constitution.md`.
