# 🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng)

> **Ghi chú v2**: Bản này review lại toàn bộ claim của bản audit gốc bằng cách đọc trực tiếp source code, chạy `mvn dependency:tree`, và trace luồng gọi thực tế trên frontend. **4 bug bị đánh giá sai mức độ nghiêm trọng** (1 severity quá cao, 1 không tồn tại/sai hoàn toàn, 2 là dead code chưa từng được gọi), và có **3 phát hiện mới** không có trong bản gốc. Chi tiết ở mục "Sửa Đổi So Với Bản Gốc" ngay bên dưới — đọc mục này trước khi review phần còn lại.

---

## 🚨 User Review Required

> [!CAUTION]
> BUG-02 (`resendVerification` thiếu ở frontend) và BUG-03 (`isEnabled()` cho PENDING) là 2 vấn đề còn giá trị thực sự cao trong nhóm cũ gọi là "critical". BUG-01 (`jakarta.transaction.Transactional`) đã được kiểm chứng là **không gây mất rollback trong thực tế** — xem giải thích bên dưới trước khi quyết định độ ưu tiên.

> [!WARNING]
> BUG-07 gốc ("không có CI") là **sai** — `.github/workflows/ci.yml` đã tồn tại và chạy trên push vào `branch_for_hung`. Vấn đề thật là CD **không đợi** CI xong (2 workflow chạy song song, độc lập). Fix phải là gate `cd.yml` bằng `workflow_run`, không phải tạo file CI mới.

> [!IMPORTANT]
> BUG-10 và BUG-16 gốc (liên quan `authSlice.js`) mô tả bug trong code **chưa bao giờ được gọi** (`fetchProfileThunk`, `updateProfileThunk` không có component nào dispatch). Luồng Profile thật sự chạy qua `setUser` (đã merge đúng) + `studentService.updateProfile`. Vẫn nên sửa vì đây là bẫy cho người sau, nhưng đừng báo cáo đây là "bug đang live" — verification step gốc ("login ADMIN → vào /profile → quay lại /admin") sẽ **không** phát hiện ra bug vì route đó không gọi code lỗi.

---

## Sửa Đổi So Với Bản Gốc (đọc trước)

### 1. BUG-01 hạ từ P0/CRITICAL xuống P3 — đã verify bằng `mvn dependency:tree`

Bản gốc khẳng định `jakarta.transaction.Transactional` "không hoạt động với Spring's transaction management" → mất rollback. **Đây là hiểu sai phổ biến nhưng không đúng trong trường hợp cụ thể này.**

Đã chạy:
```
cd apps/backend && mvn dependency:tree -Dincludes=jakarta.transaction
...
com.jlpt:jlpt-backend:jar:2.0.0
\- org.springframework.boot:spring-boot-starter-data-jpa:jar:3.3.3:compile
   \- org.hibernate.orm:hibernate-core:jar:6.5.2.Final:compile
      \- jakarta.transaction:jakarta.transaction-api:jar:2.0.1:compile
```

`jakarta.transaction-api` **có mặt trên classpath** (transitive qua `hibernate-core`). Cơ chế của Spring (`AnnotationTransactionAttributeSource`) tự động kiểm tra class này có tồn tại không; nếu có, nó đăng ký thêm `JtaTransactionAnnotationParser` và **vẫn tạo Spring-managed transaction bình thường** (qua `PlatformTransactionManager` đã cấu hình, không phải JTA thật) cho method đánh dấu `@jakarta.transaction.Transactional`. Với annotation trần (không có attribute) như trong `QuizService`/`MockExamService`, hành vi rollback mặc định (rollback khi `RuntimeException`/`Error`, không rollback khi checked exception) **giống hệt** `org.springframework.transaction.annotation.Transactional`.

→ `submitQuiz`/`submitExam` **vẫn rollback đúng** khi có exception giữa chừng. Đây không phải bug data-integrity.

**Vẫn nên sửa, nhưng vì lý do khác:**
- Đổi sang `org.springframework...Transactional` để có thể dùng `readOnly = true`, `rollbackFor`, `propagation`, `timeout` — những attribute mà bản JTA annotation **không hỗ trợ** (bị parser bỏ qua toàn bộ, chỉ tạo `DefaultTransactionAttribute()`).
- Nhất quán với 38 file khác trong codebase đều dùng `org.springframework...Transactional` (đã grep xác nhận — đây là 2 file duy nhất lệch chuẩn).
- Tránh nhầm lẫn cho người đọc sau/AI code review khác — nếu jakarta.transaction-api **bị gỡ khỏi classpath** trong tương lai (vd đổi ORM), method sẽ mất transaction hoàn toàn mà không có lỗi compile nào báo trước → time-bomb im lặng.

**→ Đổi priority: P0 CRITICAL → P3 (code hygiene / defensive), nhưng vẫn giữ trong danh sách fix.**

### 2. BUG-07 sai hoàn toàn — `ci.yml` đã tồn tại

Đã kiểm tra `.github/workflows/`: có cả `ci.yml` (tạo lúc 13:02) và `cd.yml` (tạo lúc 22:07, sau đó). `ci.yml` chạy `backend-ci` (`mvn -B clean verify`) và `frontend-ci` (`npm run lint && npm run build`) trên push tới `branch_for_hung`, `main`, `develop` và trên PR.

**Vấn đề thật**: `cd.yml` cũng trigger trên `push: branches: [branch_for_hung]` — **cùng sự kiện, chạy độc lập, không có `needs` hay `workflow_run`**. Chính comment trong `cd.yml` (dòng 7-9) đã tự thú nhận: *"Chỉ chạy CD khi CI thành công... Not strictly enforced across files unless configured"*. Nghĩa là nếu CI fail (test/build lỗi), CD **vẫn deploy** code đó lên production vì 2 workflow không liên quan nhau.

**Fix đúng**: đổi trigger của `cd.yml` từ `on: push` sang `on: workflow_run` theo dõi `ci.yml`, chỉ chạy khi `conclusion == 'success'`. KHÔNG tạo file `ci.yml` mới (đã có, tạo lại sẽ conflict).

### 3. BUG-10 & BUG-16 (authSlice.js) — code chết (dead code), chưa từng bị gọi

Đã trace: `fetchProfileThunk` và `updateProfileThunk` được định nghĩa trong `authSlice.js` nhưng **không có component nào import/dispatch chúng** (grep toàn bộ `apps/frontend/src` chỉ thấy chúng xuất hiện trong chính `authSlice.js`). `Profile.jsx` (nơi lẽ ra dùng chúng) thực tế:
- Đọc `user` từ Redux store trực tiếp (không fetch lại).
- Gọi `updateProfile`/`uploadAvatar` từ `studentService.js` (không phải `authService.js`).
- Cập nhật state qua action `setUser` — action này **đã merge đúng** (`state.user = { ...state.user, ...action.payload }`, dòng 253 authSlice.js), không có bug mất `role`.

Vậy `BUG-16` ("`fetchProfileThunk` ghi đè mất role") là **có thật trong code nhưng không live** — chỉ kích hoạt nếu sau này có ai dispatch `fetchProfileThunk`. Theo đúng anti-pattern bảng trong `CLAUDE.md` ("Dead Code | Code không bao giờ gọi | Xóa ngay, dùng Git recover"), lựa chọn đúng là **cân nhắc xoá hẳn 2 thunk này** thay vì vá lỗi cho code không ai dùng — trừ khi có kế hoạch dùng lại sắp tới. Đã hỏi ở mục quyết định bên dưới.

Verification step gốc ("Login ADMIN → /admin → /profile → gọi profile API → quay lại /admin") **sai** vì luồng UI thật không gọi `fetchProfileThunk` — test đó sẽ pass bất kể có sửa bug hay không, gây cảm giác an toàn giả.

### 4. BUG-14 (bcrypt cost 12) — khuyến nghị gốc đi ngược ADR-003 của chính dự án

`CLAUDE.md § ADR-003` quy định rõ: *"bcrypt cost không dưới 10"*. Cost 12 hiện tại **tuân thủ** yêu cầu này và là mức khuyến nghị phổ biến hiện nay (OWASP Password Storage Cheat Sheet khuyến nghị work factor cao khi phần cứng đủ mạnh — giảm xuống 10 làm giảm khả năng chống brute-force). Hạ cost để đổi lấy performance là **đánh đổi bảo mật ngược hướng với chính constitution của dự án**, và tác động thực tế (~100-150ms mỗi lần hash) không đáng kể trừ khi có benchmark cho thấy đây là bottleneck thật.

**→ Khuyến nghị: KHÔNG đổi, giữ nguyên cost=12. Xoá khỏi danh sách fix, hoặc note "no action needed — meets ADR-003".**

### 5. BUG-08 (rate limit in-memory) — hạ tầng Redis chưa thực sự tồn tại trong backend

Bản gốc viết "nên dùng Redis (phù hợp với infrastructure đã có Redis)" — nhưng đã kiểm tra `pom.xml`: **không có `spring-boot-starter-data-redis`**, và grep toàn bộ `src/main/java` cho `RedisTemplate`/`@EnableCaching`/`spring.data.redis` → **0 kết quả**. `docker-compose.yml` có service `redis` và `docker-compose.prod.yml` set `SPRING_DATA_REDIS_HOST`, nhưng đó là hạ tầng container chạy sẵn, **backend Java chưa có một dòng code nào kết nối tới nó**.

→ Fix "dùng Redis" không phải một-dòng-đổi-import như các bug khác — cần thêm dependency, cấu hình `RedisConnectionFactory`, và xử lý fallback khi Redis down (tránh biến 1 lỗi hạ tầng phụ thành DoS cho toàn bộ `checkAccountType`). Đã ghi lại đúng scope trong phần Proposed Changes.

### 6. BUG-12 mở rộng — JWT expiration bị hardcode, không đọc từ env dù có tên biến

Đã đọc `application.yml`:
```yaml
jwt:
  secret: ${JWT_SECRET:changeme_secret_key_needs_to_be_long_enough_for_hs256}
  access-expiration-ms: 900000        # ← literal, KHÔNG có ${...}
  refresh-expiration-ms: 604800000    # ← literal, KHÔNG có ${...}
```
Không chỉ `.env.example` có tên biến sai (`JWT_EXPIRATION_HOURS` thay vì đúng field) — mà `access-expiration-ms`/`refresh-expiration-ms` **hoàn toàn không đọc biến môi trường nào cả**, kể cả nếu đặt đúng tên. Đây là vi phạm trực tiếp anti-pattern "Hardcoded Config" mà chính `CLAUDE.md` liệt kê. Muốn đổi thời hạn token giữa dev/prod hiện tại **phải sửa code** chứ không cấu hình được qua `.env`.

### 7. BUG-09 (SMTP thiếu ở prod) — đúng, nhưng cần làm rõ 2 cơ chế `.env` khác nhau

Có 2 file `.env` độc lập trong hệ thống, dễ nhầm:
- **Root `.env`** (tại `PROJECT_PATH/.env` trên VPS): dùng để Docker Compose substitue `${VAR}` ngay trong YAML (`MSSQL_SA_PASSWORD`, `GOOGLE_CLIENT_ID`, `JWT_SECRET`...). `cd.yml` **có kiểm tra file này tồn tại** trước khi deploy (dòng 47-51).
- **`apps/backend/.env`**: nạp qua `env_file:` trực tiếp vào container, **không được `cd.yml` kiểm tra hay tạo tự động**.

Base `application.yml` fallback `SMTP_USERNAME`/`SMTP_PASSWORD` về rỗng (`${SMTP_USERNAME:}`), và `docker-compose.prod.yml` không set `SMTP_*` qua `environment:`. Nếu ops chưa từng tạo `apps/backend/.env` thủ công trên VPS, SMTP auth sẽ dùng username/password rỗng → mail không gửi được (verify email, reset password) mà **không có log lỗi rõ ràng khi container khởi động**, chỉ fail khi có request gửi mail đầu tiên.

**Fix nhất quán với pattern hiện tại**: thêm `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` vào `docker-compose.prod.yml` dùng cú pháp `${SMTP_HOST}` (giống cách `JWT_SECRET`/`GOOGLE_CLIENT_ID` đang làm) — kéo từ **root `.env`**, không phải tạo thêm `apps/backend/.env`. Đồng thời note vào runbook: root `.env` trên VPS phải có `SMTP_HOST`/`SMTP_PORT`/`SMTP_USERNAME`/`SMTP_PASSWORD`.

### 8. Lỗi đánh số nội bộ trong bản gốc

Mục "🚨 User Review Required" ở đầu bản gốc gọi 2 bug là "BUG-04" và "BUG-05", nhưng phần thân chi tiết ("Phân Tích Lỗi Theo Mức Độ") lại đánh số **BUG-01** và **BUG-02** cho đúng 2 vấn đề đó (rồi BUG-04 lại được dùng lại cho một bug hoàn toàn khác — StudentUser builder default). Đây là lỗi tự mâu thuẫn trong chính tài liệu, dễ gây nhầm khi trao đổi với người review khác (vd. codex) — đã thống nhất lại số ID xuyên suốt bản v2 này (giữ theo số ở phần thân, vì Proposed Changes/Priority table đều tham chiếu theo số đó).

### 9. Phát hiện mới: endpoint & payload chính xác cho fix BUG-02

Bản gốc chỉ nói "thêm `resendVerification(email)`" mà không chỉ rõ contract, dễ implement sai. Đã trace ngược từ `AuthController`:
- Endpoint: `POST /api/auth/resend-verification`
- Backend DTO (`ResendVerificationRequest`): `{ "email": "<string>" }` (bắt buộc, `@Email` validate)
- Response: `ApiResponse<Void>` (200 OK, im lặng kể cả email không tồn tại/không PENDING — theo comment "User Enumeration" trong `AuthService.resendVerification`, dòng 409-411)

### 10. Phát hiện mới: `updateProfileThunk`/BUG-10 nếu xoá `authService.updateProfile` sẽ crash code chết

Nếu chỉ xoá `updateProfile` khỏi `authService.js` như bản gốc đề xuất (để hết trùng với `studentService.js`) mà không đụng tới `authSlice.js`, thì `updateProfileThunk` (dòng 193: `authService.updateProfile(data)`) sẽ **tham chiếu tới function không còn tồn tại** → lỗi compile/runtime ngay cả khi thunk đó chưa từng được gọi (import sẽ vẫn resolve nhưng gọi sẽ throw `TypeError`, và nếu dùng TypeScript/strict lint sẽ fail build). Bản gốc không đề cập việc này. Phải sửa đồng thời cả 2 file, hoặc xoá luôn thunk (xem quyết định bên dưới).

---

## Phân Tích Lỗi Theo Mức Độ (đã cập nhật priority theo phần trên)

---

### 🔴 P0 — Ảnh hưởng Security / Feature hoàn toàn broken

#### BUG-02: `resendVerification` thiếu trong `authService.js` phía frontend

**Vị trí:**
- [authService.js](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/frontend/src/api/authService.js) — không export `resendVerification`
- [authSlice.js:20](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/frontend/src/store/slices/authSlice.js#L20) gọi `authService.resendVerification(email)` → `TypeError: authService.resendVerification is not a function`

**Vấn đề:** Đã xác nhận file không có function này (đọc toàn bộ file, 164 dòng). `resendVerificationThunk` sẽ crash bất cứ khi nào user bấm "Gửi lại email xác minh".

**Fix (contract đã verify — xem mục 9):**
```js
export async function resendVerification(email) {
  const response = await api.post('/auth/resend-verification', { email });
  return response.data;
}
```

---

#### BUG-03: `UserDetailsImpl.isEnabled()` cho phép PENDING login

**Vị trí:** [UserDetailsImpl.java:53-56](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/backend/src/main/java/com/jlpt/shared/security/UserDetailsImpl.java#L53-L56)

**Đã verify:** `authenticationManager.authenticate()` hiện chỉ được gọi ở **duy nhất 1 nơi** — `AuthService.handleStudentLogin()` (dòng 231), và dòng 227 đã check PENDING → throw đúng trước khi authenticate. Nên rủi ro "path khác bypass" mang tính phòng ngừa hơn là exploit đang tồn tại. Vẫn nên giữ fix vì đây là single point of failure: nếu sau này có thêm 1 luồng login khác (vd JWT filter refresh dùng lại `UserDetailsService`) mà quên check PENDING, lỗ hổng sẽ mở ngay lập tức mà không ai nhận ra vì `isEnabled()` trông như "đúng chuẩn Spring".

**Action:** Thêm comment giải thích rõ ràng invariant này + cân nhắc thêm unit test cho `UserDetailsImpl.isEnabled()` để invariant có test bảo vệ, không chỉ dựa vào comment.

---

### 🟡 P1 — Bất hợp lý nghiệp vụ / Deploy risk thật

#### BUG-06: `docker-compose.yml` — backend không đợi DB ready — **fix ban đầu đã REVERT sau khi verify thực tế**

**Vị trí:** [docker-compose.yml:46-48](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/docker-compose.yml#L46-L48)

**Đã verify:** `db` **đã có `healthcheck`** đầy đủ (dòng 16-20) nhưng không ai tham chiếu tới nó. `backend.depends_on` chỉ có `redis: condition: service_started`. Trên VPS lần đầu deploy (volume rỗng, MSSQL cần thời gian init lớn hơn container thường), Flyway sẽ cố connect trước khi DB sẵn sàng → crash loop.

**Fix ban đầu:**
```yaml
depends_on:
  redis:
    condition: service_started
  db:
    condition: service_healthy
```

**Kết quả deploy thật (commit `b2b2ea25`, CD run `29143152615`): FAIL.** Log:
```
Container jlpt-db Waiting
Container jlpt-db Error dependency db failed to start
dependency failed to start: container jlpt-db is unhealthy
```
Nghĩa là healthcheck của `db` **tồn tại sẵn từ trước** (không phải do fix này thêm vào) nhưng **chưa từng thực sự được kiểm chứng pass trên VPS thật** — vì trước đây không service nào phụ thuộc vào nó nên không ai để ý nó fail. Có thể do sai path `sqlcmd`/`mssql-tools18` so với image thật đang chạy, mật khẩu SA trong `$$MSSQL_SA_PASSWORD` không khớp container đang chạy, hoặc 10 x 10s (100s) không đủ thời gian.

**→ Đã revert về `service_started`** (vẫn tốt hơn trạng thái gốc — trước đây backend không chờ `db` chút nào) để deploy chạy được ngay. Việc cần làm tiếp (cần quyền SSH VPS, không tự làm được từ đây): `docker inspect jlpt-db --format='{{json .State.Health}}'` để xem lý do healthcheck fail thật, sửa đúng gốc, xác nhận 1 lần deploy chạy được với `service_healthy` rồi mới bật lại.

**Bài học (cùng dạng với BUG-13):** một fix nhìn hợp lý trên giấy (dùng healthcheck đã có sẵn) vẫn có thể phá deploy nếu chưa từng verify healthcheck đó THẬT SỰ pass trên môi trường production — audit tĩnh không thấy được lớp lỗi vận hành này, chỉ lộ ra khi chạy deploy thật.

---

#### BUG-09: SMTP env thiếu ở `docker-compose.prod.yml` (xem mục 7 ở trên để hiểu rõ 2 loại `.env`)

**Fix:**
```yaml
# docker-compose.prod.yml, backend.environment
- SMTP_HOST=${SMTP_HOST}
- SMTP_PORT=${SMTP_PORT}
- SMTP_USERNAME=${SMTP_USERNAME}
- SMTP_PASSWORD=${SMTP_PASSWORD}
```
Kèm ghi chú vào README/runbook deploy: root `.env` trên VPS phải bổ sung 4 biến này.

---

#### BUG-07: CD không gate theo kết quả CI (không phải "thiếu CI" — xem mục 2)

**Vị trí:** [cd.yml](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/.github/workflows/cd.yml), [ci.yml](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/.github/workflows/ci.yml) (đã tồn tại)

**Fix:** Đổi trigger của `cd.yml`:
```yaml
on:
  workflow_run:
    workflows: ["CI (Build & Test)"]
    branches: [branch_for_hung]
    types: [completed]

jobs:
  deploy:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    ...
```

---

### 🟢 P2 — Code Quality / Defense-in-depth (không phải bug đang khai thác được)

#### BUG-04: `StudentUser` Builder Default là ACTIVE

**Vị trí:** [StudentUser.java:37](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/backend/src/main/java/com/jlpt/feature/student/StudentUser.java#L37)

**Đã verify:** Cả 3 nơi dùng `StudentUser.builder()` hiện tại (`AuthService` register, `AuthService` Google OAuth, `DevDataSeeder`) đều **set `.status()` tường minh** — không có exploit đang tồn tại. Đây là fail-safe cho tương lai (nếu ai thêm builder call mới quên set status).

**Fix:** Đổi `@Builder.Default` → `StudentStatus.PENDING` (rẻ, an toàn, không ảnh hưởng code hiện tại vì cả 3 nơi đều override).

---

#### BUG-10 & BUG-16: `authSlice.js` — `fetchProfileThunk`/`updateProfileThunk` là dead code (xem mục 3, 10)

**Cần quyết định trước khi code** (đã hỏi ở cuối phần này) — 2 hướng:
- **(A) Xoá hẳn** `fetchProfileThunk`, `updateProfileThunk` và các `extraReducers` liên quan khỏi `authSlice.js`, xoá `getProfile`/`updateProfile` khỏi `authService.js` luôn (đúng theo anti-pattern "Dead Code" trong `CLAUDE.md`). `Profile.jsx` không cần đổi gì (đã dùng `setUser` + `studentService`).
- **(B) Sửa cho đúng** (nếu có kế hoạch dùng lại sau này): merge state đúng cách trong `fetchProfileThunk.fulfilled`/`updateProfileThunk.fulfilled` (giữ `role`/`staffRole`), và trỏ `updateProfileThunk` sang gọi `studentService.updateProfile({ fullName, phone })` thay vì `authService.updateProfile(data)` (vì `authService.updateProfile` sẽ bị xoá theo BUG-10 gốc).

Khuyến nghị: **(A)**, trừ khi có lý do dùng Redux thunk cho profile trong roadmap gần.

---

#### BUG-11: Thiếu `@Transactional(readOnly = true)` trong GET methods

**Vị trí (đã verify chính xác dòng):** `QuizService.listAssessmentsForStaff()` (dòng 171), `QuizService.getQuestionsOfAssessment()` (dòng 181), `MockExamService.getExamHistory()` (dòng 154), `MockExamService.getExamReview()` (dòng 172) — cả 4 method này hiện **không có `@Transactional` nào cả** (không chỉ thiếu `readOnly`).

---

#### BUG-12: `.env.example` lỗi thời + JWT expiration hardcode không đọc env (mở rộng — xem mục 6)

**Fix:**
1. `apps/backend/.env.example`: xoá `JWT_EXPIRATION_HOURS`/`JWT_REFRESH_EXPIRATION_HOURS`, thêm đúng tên biến sẽ dùng ở bước 2.
2. `application.yml`:
```yaml
jwt:
  secret: ${JWT_SECRET:changeme_secret_key_needs_to_be_long_enough_for_hs256}
  access-expiration-ms: ${JWT_ACCESS_EXPIRATION_MS:900000}
  refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
```

---

### ⚪ P3 — Style / theo dõi lâu dài, không khẩn cấp

#### BUG-01: Sai import `@Transactional` (đã hạ severity — xem mục 1)

Fix: đổi import trong `QuizService.java` và `MockExamService.java` sang `org.springframework.transaction.annotation.Transactional`. Không khẩn cấp về data-integrity, nhưng làm cùng lúc với BUG-11 (thêm `readOnly = true`) vì đụng cùng file, cùng lúc.

#### BUG-05: `StaffRoute.jsx`/`ManagerRoute.jsx` trust client role

UI-only guard, backend đã enforce qua `@PreAuthorize`/`hasRole` trong `SecurityConfig.java` (đã verify: `/api/admin/**` → `ROLE_ADMIN`, `/api/staff/**` và `/api/manager/**` → `ROLE_STAFF`, manager phân biệt ở service layer). Chỉ cần thêm comment, không cần đổi code.

#### BUG-08: Rate limit `checkAccountType` dùng in-memory Map

**Scope thật lớn hơn 1 dòng** (xem mục 5): cần thêm `spring-boot-starter-data-redis` vào `pom.xml`, cấu hình connection tới service `redis` đã có sẵn trong `docker-compose.yml`, và xử lý fallback (không để Redis down làm crash toàn bộ endpoint `checkAccountType`). Nếu không có thời gian, có thể để nguyên in-memory nhưng ghi rõ giới hạn (rate limit reset khi restart/scale ngang nhiều instance) vào code comment thay vì âm thầm chấp nhận.

#### BUG-13: `VPS_PASSWORD` trong CD workflow — **ĐÃ REVERT sau khi verify thực tế bằng cách deploy thật**

Ban đầu không xác minh được từ code liệu secret `VPS_PASSWORD` có thực sự được set hay không, nên đã xoá dòng `password:` theo khuyến nghị (chỉ dùng SSH key). Sau khi push commit `687ef159` và để CD chạy thật: **run fail ngay lập tức** với log `Error: can't connect without a private SSH key or password` — bằng chứng trực tiếp rằng secret `VPS_SSH_KEY` **chưa từng được cấu hình** trong repo, và deploy trước giờ chỉ hoạt động được nhờ `VPS_PASSWORD`.

→ Đã khôi phục lại dòng `password: ${{ secrets.VPS_PASSWORD }}` trong `cd.yml` (song song với `key:`) để deploy chạy lại được ngay. **Việc cần làm tiếp** (không thể tự làm từ đây, cần quyền truy cập repo settings): thiết lập `VPS_SSH_KEY` cho đúng (tạo keypair, add public key vào `~/.ssh/authorized_keys` trên VPS, add private key vào GitHub repo secret), xác nhận 1 lần deploy chạy được chỉ với `key:`, rồi mới xoá `password:` lần nữa.

**Bài học**: khi một khuyến nghị bảo mật phụ thuộc vào giả định về hạ tầng/secret không kiểm chứng được từ code (ở đây là "chắc VPS_SSH_KEY đã có sẵn"), phải coi đó là rủi ro thật và verify bằng cách chạy thử trước khi coi là xong — audit tĩnh không thấy được lớp lỗi này.

#### BUG-14: BCrypt cost=12 — **KHÔNG cần fix** (xem mục 4, đi ngược ADR-003 nếu hạ xuống)

---

## Proposed Changes (đã cập nhật)

### Backend

#### [MODIFY] [QuizService.java](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java)
- BUG-01: đổi import `jakarta.transaction.Transactional` → `org.springframework.transaction.annotation.Transactional`
- BUG-11: thêm `@Transactional(readOnly = true)` cho `listAssessmentsForStaff` (dòng 171) và `getQuestionsOfAssessment` (dòng 181)

#### [MODIFY] [MockExamService.java](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java)
- BUG-01: đổi import tương tự
- BUG-11: thêm `@Transactional(readOnly = true)` cho `getExamHistory` (dòng 154) và `getExamReview` (dòng 172)

#### [MODIFY] [StudentUser.java](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/backend/src/main/java/com/jlpt/feature/student/StudentUser.java)
- BUG-04: `@Builder.Default private StudentStatus status = StudentStatus.ACTIVE` → `StudentStatus.PENDING`

#### [MODIFY] [UserDetailsImpl.java](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/backend/src/main/java/com/jlpt/shared/security/UserDetailsImpl.java)
- BUG-03: thêm comment giải thích invariant; cân nhắc test riêng cho `isEnabled()`

#### [MODIFY] [application.yml](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/backend/src/main/resources/application.yml)
- BUG-12: đổi `access-expiration-ms`/`refresh-expiration-ms` sang đọc từ env với default giữ nguyên giá trị cũ

#### [MODIFY] [docker-compose.yml](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/docker-compose.yml)
- BUG-06: thêm `db: condition: service_healthy` vào `depends_on` của `backend`

#### [MODIFY] [docker-compose.prod.yml](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/docker-compose.prod.yml)
- BUG-09: thêm `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` (dùng `${...}` kéo từ root `.env`, xem mục 7)

#### [MODIFY] `apps/backend/.env.example`
- BUG-12: xoá `JWT_EXPIRATION_HOURS`/`JWT_REFRESH_EXPIRATION_HOURS`, thêm `JWT_ACCESS_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS`, thêm `SMTP_*` nếu chưa đủ

---

### Frontend

#### [MODIFY] [authService.js](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/frontend/src/api/authService.js)
- BUG-02: thêm `export async function resendVerification(email)` gọi `POST /auth/resend-verification` với body `{ email }` (xem contract đã verify ở mục 9)
- BUG-10: xoá `updateProfile`/`getProfile` **CHỈ SAU KHI** đã xử lý xong `authSlice.js` (xem dưới) để tránh reference lỗi

#### [MODIFY] [authSlice.js](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/frontend/src/store/slices/authSlice.js)
- BUG-10/16: chọn hướng (A) xoá `fetchProfileThunk`, `updateProfileThunk` và `extraReducers` liên quan (khuyến nghị), hoặc hướng (B) sửa merge + trỏ sang `studentService.updateProfile`

---

### DevOps / Config

#### [MODIFY] `.github/workflows/cd.yml`
- BUG-07: đổi `on: push` → `on: workflow_run` theo dõi `ci.yml`, thêm `if: github.event.workflow_run.conclusion == 'success'` (xem mục 2 — **không tạo file CI mới**, `ci.yml` đã có sẵn)

---

## Verification Plan (đã sửa lại các bước sai)

### Automated Tests
- `mvn clean verify -pl apps/backend` — unit tests + JaCoCo (đã tồn tại trong `ci.yml`, chạy local trước khi push)
- `npm run lint && npm run build --prefix apps/frontend`

### Manual Verification
1. **BUG-02 fix**: Gọi `POST /api/auth/resend-verification` với email PENDING hợp lệ → 200, nhận lại email mới. Gọi với email không tồn tại → vẫn 200 (không lộ enumeration, theo comment sẵn có trong `AuthService`).
2. **BUG-01/BUG-11 fix**: Viết 1 test giả lập exception giữa `attemptAnswerRepository.saveAll()` và `testAttemptRepository.save(attempt)` trong `calculateScore`/`gradeAndPersist` → xác nhận DB rollback cả 2 (test này **nên tồn tại dù BUG-01 không nghiêm trọng như tưởng**, vì nó bảo vệ invariant thật của domain, không phụ thuộc annotation nào).
3. **BUG-04 fix**: Unit test tạo `StudentUser.builder().email(...).fullName(...).build()` (không set status) → assert `status == PENDING`.
4. **BUG-06 fix**: `docker compose down -v && docker compose up` (volume rỗng, mô phỏng VPS lần đầu) → xác nhận backend không crash-loop trong lúc DB đang init.
5. **BUG-09 fix**: Deploy với root `.env` có đủ `SMTP_*` → test luồng quên mật khẩu thực sự nhận được email.
6. **BUG-07 fix**: Push 1 commit cố tình làm `mvn verify` fail vào `branch_for_hung` (nhánh test) → xác nhận `cd.yml` **không chạy** cho tới khi CI fail hẳn (không deploy).
7. **BUG-10/16**: Nếu chọn hướng (A) xoá code — chỉ cần `npm run build` pass và grep lại không còn tham chiếu `fetchProfileThunk`/`updateProfileThunk` ở đâu. Nếu hướng (B) — cần dựng lại 1 nơi gọi thunk (hiện chưa có) để test được, hoặc viết unit test thuần cho reducer.

---

## Tóm tắt Priority (đã cập nhật)

| Priority | Bug | Mô tả | File | Thay đổi so với bản gốc |
|----------|-----|--------|------|--------------------------|
| P0 🔴 | BUG-02 | `resendVerification` bị null — feature broken | `authService.js` | giữ nguyên, bổ sung contract chính xác |
| P0 🔴 | BUG-03 | PENDING user `isEnabled=true` | `UserDetailsImpl.java` | giữ nguyên |
| P1 🟡 | BUG-06 | Backend không wait DB ready | `docker-compose.yml` | giữ nguyên |
| P1 🟡 | BUG-09 | SMTP env thiếu production | `docker-compose.prod.yml` | làm rõ 2 cơ chế `.env` |
| P1 🟡 | BUG-07 | CD không gate theo CI | `cd.yml` | **sửa lại hoàn toàn** — CI đã tồn tại, fix khác hẳn bản gốc |
| P2 🟢 | BUG-04 | Builder default ACTIVE | `StudentUser.java` | hạ mô tả rủi ro — defense-in-depth, không phải exploit sống |
| P2 🟢 | BUG-10/16 | `authSlice.js` dead code | `authSlice.js` | **downgrade từ P1 → P2** — chưa từng được gọi, cần quyết định xoá hay sửa |
| P2 🟢 | BUG-11 | Thiếu `readOnly=true`/`@Transactional` | `QuizService`, `MockExamService` | xác nhận thiếu hẳn annotation, không chỉ thiếu attribute |
| P2 🟢 | BUG-12 | `.env.example` + JWT expiration hardcode | `application.yml`, `.env.example` | **mở rộng scope** — không chỉ là doc lỗi thời |
| P3 ⚪ | BUG-01 | Sai import `@Transactional` | `QuizService`, `MockExamService` | **downgrade từ P0 → P3** — đã verify không mất rollback |
| P3 ⚪ | BUG-05 | StaffRoute/ManagerRoute trust client | FE routes | giữ nguyên, chỉ cần comment |
| P3 ⚪ | BUG-08 | Rate limit in-memory | `AuthService.java` | **mở rộng scope** — Redis chưa tích hợp trong backend, không phải 1 dòng đổi |
| P3 ⚪ | BUG-13 | SSH password auth | `cd.yml` | giữ nguyên |
| ❌ Bỏ | ~~BUG-14~~ | ~~BCrypt cost=12~~ | `SecurityConfig.java` | **xoá khỏi kế hoạch** — tuân thủ đúng ADR-003, hạ cost là đi lùi bảo mật |

---

## Việc cần quyết định trước khi bắt tay code

1. **BUG-10/16**: xoá dead code (`fetchProfileThunk`/`updateProfileThunk`) hay sửa cho đúng để dành cho tương lai?
2. **BUG-08**: có đủ thời gian tích hợp Redis thật (thêm dependency + config) trong đợt fix này, hay chỉ document giới hạn của in-memory rate limit và để lại backlog?
3. **BUG-13**: xác nhận với người quản lý VPS xem secret `VPS_PASSWORD` có đang được set trong GitHub repo secrets không, trước khi quyết định xoá dòng `password:` khỏi `cd.yml`.
