# TEST-PROCESS-COVERAGE — Quy Trình Test & Ma Trận Bao Phủ

> **Phạm vi:** Toàn bộ Test Cases (TC) suy ra từ đặc tả Use Case (`UC-*`) và SPEC feature.
> **Mục đích:** Chuẩn hoá *cách* sinh test từ spec để đảm bảo **độ bao phủ có thể chứng minh** (mọi AC + mọi BR + mọi luồng lỗi đều có ít nhất một TC truy vết ngược về spec).
> **Cập nhật:** 2026-07-21

---

## 1. Nguyên Tắc Bao Phủ (Coverage Contract)

Một feature được coi là **"đủ test"** khi thỏa **đồng thời** 4 điều kiện — đây là *cổng chất lượng* (quality gate), không phải mục tiêu % dòng lệnh:

| # | Điều kiện | Cách kiểm chứng |
|:---:|:---|:---|
| C1 | **Mỗi `AC-xx-nn`** (Acceptance Criteria) có ≥ 1 TC tham chiếu trực tiếp | Bảng "Coverage Checklist" cuối mỗi file TC |
| C2 | **Mỗi `BR-xx-nn`** (Business Rule) có ≥ 1 TC tham chiếu trực tiếp | Bảng "Coverage Checklist" cuối mỗi file TC |
| C3 | **Mỗi dòng "Luồng Lỗi"** trong spec (§3.2/§9) có ≥ 1 TC (API-level) khẳng định đúng HTTP code + error code | Nhóm "API/Controller Tests" |
| C4 | **Mỗi bất biến bảo mật** (không lộ đáp án/hash, authz, immutability) có ≥ 1 test `@Tag("security")` KHÔNG được `@Disabled` | Nhóm "Security Invariant Tests" |

> Coverage % dòng lệnh (JaCoCo) là *chỉ báo phụ*, không thay thế C1–C4. Ngưỡng khuyến nghị: **≥ 80% line / ≥ 70% branch** ở Service Layer.

---

## 2. Quy Trình Sinh Test Từ Spec (7 bước)

```
┌──────────────────────────────────────────────────────────────────────┐
│ (1) TRÍCH   → Đọc UC spec: liệt kê toàn bộ AC, BR, Validation Rules,   │
│               Luồng lỗi (§3.2/§9). Đây là "danh sách nghĩa vụ".        │
│ (2) PHÂN TẦNG→ Gán mỗi nghĩa vụ vào tầng test rẻ nhất chứng minh được  │
│               nó (Unit < Integration < API < E2E). Xem §3.               │
│ (3) THIẾT KẾ → Với mỗi nghĩa vụ: chọn kỹ thuật (equivalence partition,  │
│               boundary value, state transition, negative test).         │
│ (4) ĐỊNH ID  → Đặt ID theo §4; gắn "Tham chiếu" = AC/BR nguồn.          │
│ (5) DỮ LIỆU  → Định nghĩa fixture tối thiểu (Test Data Summary).        │
│ (6) TRUY VẾT → Điền Coverage Checklist: AC/BR → TC. Ô trống = gap.      │
│ (7) THỰC THI → mvn test / npm test theo tag; gate C1–C4 phải xanh.      │
└──────────────────────────────────────────────────────────────────────┘
```

**Quy tắc "tầng rẻ nhất":** logic thuần (tính điểm, clamp, validate) test ở **Unit**; ràng buộc DB (UNIQUE, CHECK, upsert) test ở **Integration**; HTTP contract (status code, shape response, authz filter) test ở **API**; hành vi người dùng test ở **Frontend/E2E**. Không lặp lại cùng một assertion ở nhiều tầng.

---

## 3. Các Tầng Test & Công Cụ

| Tầng | Tag | Stack | Chịu trách nhiệm chứng minh |
|:---|:---|:---|:---|
| **Unit — Service** | `@Tag("unit")` | JUnit 5 + Mockito | Business logic, tính toán, nhánh điều kiện, ném exception đúng loại |
| **Integration — Repo/DB** | `@Tag("integration")` | Testcontainers (MySQL 8) hoặc H2, Flyway | Query đúng, ràng buộc UNIQUE/CHECK/FK, upsert không tạo duplicate, `@Transactional` rollback |
| **API — Controller** | `@Tag("api")` | `@WebMvcTest` + MockMvc | HTTP status, error code, shape DTO, `@Valid`, security filter (401/403), rate limit |
| **Security Invariant** | `@Tag("security")` | MockMvc + JSON path assert | Không lộ `passwordHash`/`correctOption`/`two_factor_secret`; authz Role+Level; immutability |
| **Frontend — Component** | Jest + RTL | React Testing Library | Validation phía client (chỉ UX), render loading/error, điều hướng, KHÔNG tin business logic ở FE |

> **Ranh giới đúng của FE-test** (theo `CLAUDE.md` React Anti-patterns): FE test chỉ khẳng định *UX* (hiển thị lỗi, chặn submit rỗng, redirect). Mọi tính toán nghiệp vụ (điểm số, quyền) được test ở backend — FE giả định backend là nguồn chân lý.

### 3.1 Lệnh chạy

```bash
# Backend — theo tag
mvn test                          # tất cả
mvn test -Dgroups="unit"          # chỉ unit (nhanh, chạy mỗi commit)
mvn test -Dgroups="api,security"  # contract + bảo mật (bắt buộc trước merge)
mvn verify -Dgroups="integration" # cần Docker/Testcontainers

# Frontend
npm run test                      # Jest + RTL (KHÔNG chạy prettier — xem ghi chú dự án)
```

---

## 4. Quy Ước Đặt Tên & Ưu Tiên

**ID test:** `TC-{L}-{UC}-{seq}` — `L` = tầng (`U`=Unit, `I`=Integration, `A`=API, `S`=Security, `F`=Frontend).
Ví dụ `TC-A-13-06` = API test thứ 6 của UC-13.

**File:** `TC-UC-{number}-{slug}.md` đặt trong `feat-testing/` (đồng nhất với các TC hiện có: TC-UC-01…TC-UC-34).

**Độ ưu tiên:**

| Mức | Ý nghĩa | Ví dụ |
|:---:|:---|:---|
| **P0** | Bảo mật / mất-hỏng dữ liệu / core money-path. Fail = chặn release | Lộ đáp án, authz thủng, tính điểm sai, hard-delete |
| **P1** | Chức năng chính của UC. Fail = tính năng hỏng | Happy path, fallback AI, upsert progress |
| **P2** | Phụ trợ / UX | Thông báo, phân trang, dropdown |

---

## 5. Kỹ Thuật Thiết Kế Test Áp Dụng

- **Equivalence Partitioning** — nhóm giá trị tương đương (level ∈ {N5..N1} hợp lệ / ngoài tập → 422).
- **Boundary Value** — biên: file 10MB vs 10MB+1B; score = 0 / = maxScore / vượt biên; login_attempts 4→5.
- **State Transition** — `pending → ai_graded`; `in_progress → submitted` (chặn nộp lại); `active → suspended → deleted`.
- **Negative / Error-path** — mỗi dòng Luồng Lỗi = 1 test khẳng định HTTP + error code + KHÔNG side-effect (không tạo bản ghi).
- **Security Invariant** — assert *sự vắng mặt* của field nhạy cảm bằng cách duyệt đệ quy JSON response.

---

## 6. Ma Trận Bao Phủ Tổng (Master Coverage Matrix)

> Cột "TC file" trỏ tới file trong thư mục này. Trạng thái: ✅ đã có TC · 🆕 tạo trong đợt này · ⬜ chưa có.

| UC | Tên | Feature | #AC | #BR | TC file | TT |
|:---|:---|:---|:---:|:---:|:---|:---:|
| UC-01 | Đăng nhập | feat-auth | 7+ | 9 | `TC-UC-01-login.md` | ✅ |
| UC-02 | Đăng ký | feat-auth | — | — | `TC-UC-02-register.md` | ✅ |
| UC-03 | Reset password (student) | feat-auth | — | — | `TC-UC-03-reset-password.md` | ✅ |
| UC-04 | Hồ sơ người dùng | feat-auth | — | — | `TC-UC-04-user-profile.md` | ✅ |
| UC-05 | Đổi mật khẩu | feat-auth | — | — | `TC-UC-05-change-password.md` | ✅ |
| **UC-06** | **Đặt lại MK Staff (admin-mediated)** | feat-auth | 9 | 12 | `TC-UC-06-staff-reset-password.md` | 🆕 |
| **UC-06** | **Học Ngữ pháp** | feat-core-learning | 8 | 7 | `TC-UC-06-grammar.md` | 🆕 |
| **UC-07** | **Học Kanji** | feat-core-learning | 4 | 7 | `TC-UC-07-kanji.md` | 🆕 |
| **UC-08** | **Học Kana** | feat-core-learning | 6 | 7 | `TC-UC-08-kana.md` | 🆕 |
| **UC-09** | **Học Từ vựng** | feat-core-learning | 5 | 8 | `TC-UC-09-vocabulary.md` | 🆕 |
| **UC-10** | **Thi thử JLPT** | feat-mock-test | 13 | 13 | `TC-UC-10-mock-exam.md` | 🆕 |
| **UC-11** | **Quiz & Luyện tập** | feat-assessment | 8 | 8 | `TC-UC-11-quiz-practice.md` | 🆕 |
| **UC-13** | **Luyện nói & chấm AI** | feat-ai-skills | 8 | 9 | `TC-UC-13-speaking-practice.md` | 🆕 |
| **UC-14** | **Luyện Đọc hiểu** | feat-reading-listening | 10 | 9 | `TC-UC-14-reading.md` | 🆕 |
| **UC-15** | **Luyện Nghe hiểu** | feat-reading-listening | 12 | 12 | `TC-UC-15-listening.md` | 🆕 |
| UC-18 | Đăng xuất | feat-auth | — | — | `TC-UC-18-logout.md` | ✅ |
| **UC-20** | **Luyện viết tay AI (OCR)** | feat-ai-skills | 9 | 10 | `TC-UC-20-handwriting-ocr.md` | 🆕 |
| UC-24 | Quản lý ngân hàng câu hỏi | feat-content-management | — | — | `TC-UC-24-...md` | ✅ |
| UC-25 | Quản lý ngữ pháp | feat-content-management | — | — | `TC-UC-25-...md` | ✅ |
| UC-26 | Quản lý quiz | feat-content-management | — | — | `TC-UC-26-...md` | ✅ |
| UC-27 | Quản lý nội dung học | feat-content-management | — | — | `TC-UC-27-...md` | ✅ |
| UC-28 | Quản lý đề thi JLPT | feat-content-management | — | — | `TC-UC-28-...md` | ✅ |
| UC-33 | Duyệt nội dung | feat-content-review | — | — | `TC-UC-33-...md` | ✅ |
| UC-34 | Quản lý trạng thái nội dung | feat-content-review | — | — | `TC-UC-34-...md` | ✅ |
| **UC-35** | **Đăng nhập Admin** | feat-system-admin | 6 | 6 | `TC-UC-35-admin-login.md` | 🆕 |
| **UC-37** | **Quản lý người dùng (Admin)** | feat-system-admin | 9 | 9 | `TC-UC-37-manage-user.md` | 🆕 |

> **Lưu ý xung đột số hiệu:** Có **hai** UC-06 khác nhau trong spec (Staff-reset ở `feat-auth`, Grammar ở `feat-core-learning`). File TC được phân biệt bằng slug để tránh nhầm.

---

## 7. Chủ Đề Test Xuyên Suốt (Cross-cutting — kiểm cho MỌI UC)

Những nghĩa vụ này lặp lại ở nhiều UC; mỗi file TC nhắc lại tối thiểu một biến thể:

| Chủ đề | Khẳng định | Xuất hiện tại UC |
|:---|:---|:---|
| **Không lộ đáp án** | Response GET không chứa `correctOption`/`correctAnswerText` | 10, 11, 14, 15 |
| **Tính điểm server-side** | Client gửi score bị bỏ qua; điểm lấy từ DB | 10, 11, 14, 15 |
| **Immutability attempt** | Nộp lại tạo bản ghi MỚI, không UPDATE cũ; 422 nếu re-submit | 10, 11, 14, 15 |
| **AI không silent-fail** | Timeout→retry 3x→fallback message + full log; không lộ raw error | 13, 20 |
| **File không BLOB** | Cột lưu URL/path, không binary (ADR-006) | 13, 20 |
| **Soft delete** | `status='deleted'`, bản ghi vẫn tồn tại (ADR-004) | 37 |
| **Upsert progress** | UNIQUE(student,type,content) — không duplicate | 06-grammar, 07, 08, 09 |
| **Không lộ thông tin nhạy cảm** | Không có `passwordHash`/`two_factor_secret` trong response | 01, 35, 37 |
| **Chống enumeration** | Lỗi đăng nhập/forgot chung chung, luôn 200/401 giống nhau | 01, 06-staff, 35 |
| **Authz Role + Level/VIP** | 403 khi thiếu role hoặc thiếu VIP (LESSON-003) | 06-grammar, 07, 09, 10, 14, 15, 37 |
| **last_activity_date** | Cập nhật khi truy cập nội dung (streak) | 06-grammar, 07, 08, 09, 14, 15 |

---

## 8. Định Nghĩa "Done" Cho Một File TC

Một file `TC-UC-*.md` hoàn chỉnh khi có đủ:

1. Header: Feature, UC, nguồn AC/BR, ngày.
2. Các mục theo tầng (Unit / Integration / API / Security / Frontend) — bỏ tầng không áp dụng nhưng ghi rõ lý do.
3. Mỗi TC: `ID`, `Tham chiếu` (AC/BR), `Loại`, `Ưu tiên`, `Setup/Steps/Expected`.
4. **Test Data Summary** — fixture tối thiểu.
5. **Coverage Checklist** — bảng AC/BR → TC, không còn ô trống (mọi AC & BR đều "✅").

---

## 9. Tài Liệu Liên Quan

- Đặc tả UC: `feature-specs/backend/<feature>/UC-*.md`
- Chuẩn validation 3 lớp: `docs/04-Test-Specs/SPEC_VALIDATION_COVERAGE.md`
- Quy tắc domain & golden patterns: `AGENTS.md`
- Kiến trúc, ADR, anti-patterns: `CLAUDE.md`
