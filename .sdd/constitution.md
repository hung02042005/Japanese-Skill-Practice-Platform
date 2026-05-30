# 📜 CONSTITUTION.md — Hệ Thống Học Tiếng Nhật JLPT

**Ngày phê duyệt:** 2026-05-27 | **Nhóm:** JLPT Dev Team | **Phiên bản:** 2.0

> **QUY TẮC TỐI THƯỢNG:** Mọi thay đổi đối với tài liệu này đều yêu cầu sự nhất trí (100% đồng thuận) của toàn bộ nhóm.

---

## ĐIỀU 1 — STACK CÔNG NGHỆ (Không thể thay đổi)

| Layer | Công nghệ | Ghi chú |
|-------|-----------|---------|
| Backend | Java 21 + Spring Boot 3.x | Maven |
| Frontend | React 18 + TypeScript | npm |
| Database | SQL Server | KHÔNG dùng NoSQL |
| ORM | Spring Data JPA + Hibernate | |
| Migration | Flyway / Liquibase | Mọi thay đổi schema BẮT BUỘC có migration |
| Auth | JWT + bcrypt (cost >= 12) + 2FA (Admin) | |
| Testing | JUnit 5 + Mockito / Jest | |
| Styling | Tailwind CSS | KHÔNG dùng CSS-in-JS |

---

## ĐIỀU 2 — TIÊU CHUẨN CODE (Coding Standards)

### 2.1. Định dạng & Linting

| Môi trường | Tool | Config | Lệnh |
|------------|------|--------|------|
| Frontend | Prettier | `frontend/.prettierrc` | `npm run format` |
| Frontend | ESLint + Airbnb | `frontend/.eslintrc.json` | `npm run lint` |
| Backend | Spotless | `backend/spotless.xml` | `mvn spotless:apply` |
| Backend | SonarLint | IDE Plugin | |

> **⚠️ CI ENFORCEMENT**: 0 warnings allowed — PR sẽ bị block nếu có cảnh báo linting.

### 2.2. Giới hạn độ dài

| Metric | Giới hạn | Enforcement |
|--------|----------|-------------|
| Function/Method | **40 dòng** | Bắt buộc refactor nếu vượt quá |
| File | **300 dòng** | Bắt buộc tách file nếu vượt quá |
| PR Size | **400 dòng** | Bắt buộc chia nhỏ nếu vượt quá |

### 2.3. Comments & TODO

- ✅ Comments giải thích **TẠI SAO** (Why), không phải LÀM GÌ (What)
- ❌ **TUYỆT ĐỐI KHÔNG** có TODO comments trong code merge
- ❌ KHÔNG commit code với `console.log()` / `System.out.println()`

### 2.4. TypeScript/Java Rules

| Ngôn ngữ | Rule | Enforcement |
|----------|------|-------------|
| TypeScript | `noImplicitAny: true` | strict mode |
| Java | `@Transactional` chỉ ở Service layer | SonarLint rule |
| Java | `@Valid` trên mọi `@RequestBody` | Review |

---

## ĐIỀU 3 — CHÍNH SÁCH BẢO MẬT (Tuyệt đối tuân thủ)

### 3.1. Authentication & Authorization

| Rule | Implementation | Violation |
|------|---------------|-----------|
| Password hashing | `bcrypt` cost >= **12** | **NEVER** dưới 10 |
| JWT tokens | Short expiry + Refresh token | Blacklist khi logout |
| Admin 2FA | Bắt buộc TOTP | Không được bypass |

### 3.2. Data Protection

| Rule | Implementation |
|------|---------------|
| Secrets management | Chỉ trong `.env` — **KHÔNG** commit lên source |
| SQL Injection | Chỉ JPA/Hibernate parameterized queries — **ZERO TOLERANCE** |
| Input validation | Jakarta Bean Validation (Backend) / Zod (Frontend) |

### 3.3. File Upload

- ✅ Validate extension (whitelist)
- ✅ Validate size (max 10MB)
- ✅ Scan for malicious content
- ✅ Store in `/uploads` (dev) hoặc S3 (prod) — **KHÔNG** lưu BLOB

### 3.4. CORS

```java
// ✅ Allowed (dev)
origins = ["http://localhost:3000"]

// ❌ FORBIDDEN (production)
origins = ["*"]  // TUYỆT ĐỐI KHÔNG
```

---

## ĐIỀU 4 — QUY TRÌNH GIT (Git Workflow)

### 4.1. Branch Naming

```
feat/[name]     → Tính năng mới (VD: feat/kanji-ocr)
fix/[name]      → Sửa lỗi (VD: fix/payment-webhook)
chore/[name]    → Maintenance (VD: chore/update-deps)
spec/[name]     → Viết spec (VD: spec/quiz-module)
```

### 4.2. Commit Format (Conventional Commits)

```
[type]([scope]): [description]

Types: feat | fix | docs | style | refactor | test | chore
Max: 72 ký tự

Ví dụ:
feat(auth): add JWT login with 2FA for admin
fix(payment): prevent duplicate VIP upgrade on webhook
test(quiz): add unit test for score calculation
```

### 4.3. Pull Request Rules

| Rule | Giá trị |
|------|---------|
| Approval required | **1 approval** tối thiểu |
| Branch protection | `main` — **KHÔNG push trực tiếp** |
| Max PR size | **400 lines** |
| CI status | **Tất cả checks phải pass** |
| Todo check | **KHÔNG có TODO comments** |

### 4.4. Emergency Hotfix

```
1. Tạo branch: hotfix/[issue-name]
2. Merge với 1 approval
3. Sau khi deploy → Post-mortem meeting bắt buộc
4. Sửa đổi CONSTITUTION nếu cần thiết
```

---

## ĐIỀU 5 — YÊU CẦU KIỂM THỬ (Testing Requirements)

### 5.1. Coverage Thresholds

| Type | Minimum | Scope |
|------|---------|-------|
| Unit Tests | **80%** | Business logic (Services, utilities) |
| Integration Tests | **100%** | Tất cả API endpoints |
| E2E Tests | Khuyến khích | Critical user flows |

### 5.2. Test Requirements

| Requirement | Backend | Frontend |
|-------------|---------|----------|
| Happy path | ✅ Bắt buộc | ✅ Bắt buộc |
| Error path | ✅ Bắt buộc | ✅ Bắt buộc |
| Mock external calls | ✅ | N/A |
| No slow tests | ✅ H2 in-memory | ✅ Mock API |

### 5.3. CI Enforcement

> ❌ **KHÔNG merge** nếu:
> - Coverage < 80%
> - Tests fail
> - Breaking existing tests

---

## ĐIỀU 6 — QUY TẮC SỬ DỤNG AI AGENT

### 6.1. Pre-work Checklist

- [ ] Đọc `AGENTS.md` → Project context, Domain Rules
- [ ] Đọc `CONSTITUTION.md` → Tech stack, Security, Standards
- [ ] Đọc `CLAUDE.md` → Architecture, ADR, Anti-patterns
- [ ] Đọc Use Cases (`/docs/use-cases/`) → Business logic

### 6.2. Human Oversight Rules

| Rule | Description |
|------|-------------|
| **Evaluate plans** | Review AI plan TRƯỚC KHI chấp thuận code |
| **Human-led refactor** | Refactor sau mỗi 3-5 tasks do AI làm |
| **Explainability** | **KHÔNG bao giờ** approve code bạn không hiểu |

### 6.3. AI Output Validation

Trước khi commit, verify:
- [ ] Domain Rules (AGENTS.md § 7) không bị vi phạm
- [ ] Security Rules (ĐIỀU 3) được tuân thủ
- [ ] Không có TODO comments
- [ ] Tests coverage >= 80%
- [ ] DTO pattern được sử dụng

---

## ĐIỀU 7 — QUY TRÌNH REVIEW & ESCALATION

### 7.1. Review Cadence

| Type | Frequency | Day | Time (ICT) |
|------|-----------|-----|------------|
| Spec Review | Weekly | Monday | 10:00 |
| Code Review | Weekly | Thursday | 14:00 |
| Retrospective | Monthly | Last Friday | 15:00 |

### 7.2. Escalation Process

```
┌─────────────────────────────────────────────────────────┐
│  Issue                              →    Solution       │
├─────────────────────────────────────────────────────────┤
│  Unclear spec                       →    Ask user       │
│  Architecture change needed         →    Vote (100%)    │
│  Emergency (production down)        →    1 approval     │
│                                         + Post-mortem   │
│  AI output suspicious               →    Ask team       │
└─────────────────────────────────────────────────────────┘
```

### 7.3. Constitution Amendment

- Thay đổi → 100% team consensus
- Version bump → Increment major/minor
- Changelog → Ghi rõ ai提议, ai đồng ý, ngày

---

## ĐIỀU 8 — ENFORCEMENT & CI/CD

### 8.1. Automated Enforcement

| Check | Tool | Action |
|-------|------|--------|
| Lint (0 warnings) | ESLint / Spotless | CI fail |
| Type check | TypeScript strict | CI fail |
| Coverage >= 80% | JaCoCo / Jest | CI fail |
| Security scan | Snyk / OWASP | CI fail |
| Secret detection | GitLeaks | CI fail |

### 8.2. Manual Enforcement

| Check | Who | When |
|-------|-----|------|
| Code review | Team member | Every PR |
| Spec review | Team lead | Before coding |
| Security review | Tech lead | VIP/Payment features |

---

## ĐIỀU 9 — CONFIG FILE LOCATIONS

### 9.1. Frontend Configs

```
frontend/
├── .prettierrc           # Code formatter
├── .eslintrc.json        # Linting rules
├── .eslintignore         # Ignore patterns
├── tsconfig.json         # TypeScript strict mode
└── tailwind.config.js    # Tailwind CSS config
```

### 9.2. Backend Configs

```
backend/
├── spotless.xml          # Code formatter
├── pom.xml               # Maven config
├── src/main/resources/
│   ├── application.yml   # App config (no secrets)
│   └── db/migration/     # Flyway migrations
```

### 9.3. Environment Variables

```bash
# .env (KHÔNG commit lên git)
DATABASE_URL=
JWT_SECRET=
STRIPE_API_KEY=
```

---

## VERSION HISTORY

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | [DATE] | [TEAM] | Initial |
| 2.0 | 2026-05-27 | Architect | Consolidated, added enforcement |
