# Graph Report - Japanese-Skill-Practice-Platform  (2026-07-14)

## Corpus Check
- 4 files · ~10,737 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 168 nodes · 159 edges · 18 communities (14 shown, 4 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 1 edges (avg confidence: 0.7)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `2e201bc5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- package.json
- authService
- CLAUDE.md — JLPT E-Learning System v2.0
- Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]
- ⚪ P3 — Style / theo dõi lâu dài, không khẩn cấp
- Redis Cache
- SQL Server (Primary DB)
- Backend
- Sửa Đổi So Với Bản Gốc (đọc trước)
- ADR (Architecture Decision Records)
- 🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng)
- 2. ARCHITECTURE PRINCIPLES
- ANTI-PATTERNS
- 7. JLPT DOMAIN RULES
- 8. GOLDEN PATTERNS
- 10. DEFINITION OF DONE
- Implementation Plan

## God Nodes (most connected - your core abstractions)
1. `CLAUDE.md — JLPT E-Learning System v2.0` - 14 edges
2. `Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]` - 12 edges
3. `Sửa Đổi So Với Bản Gốc (đọc trước)` - 11 edges
4. `ADR (Architecture Decision Records)` - 9 edges
5. `🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng)` - 9 edges
6. `Backend` - 9 edges
7. `ANTI-PATTERNS` - 7 edges
8. `7. JLPT DOMAIN RULES` - 6 edges
9. `8. GOLDEN PATTERNS` - 6 edges
10. `LESSONS LEARNED` - 6 edges

## Surprising Connections (you probably didn't know these)
- `authSlice` --calls--> `authService`  [EXTRACTED]
  apps/frontend/src/store/slices/authSlice.js → apps/frontend/src/api/authService.js

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Deployment Environments** — docker_compose_yml, docker_compose_prod_yml, docker_compose_staging_yml [EXTRACTED 1.00]

## Communities (18 total, 4 thin omitted)

### Community 0 - "package.json"
Cohesion: 0.17
Nodes (11): husky, markdownlint-cli2, devDependencies, husky, markdownlint-cli2, name, private, scripts (+3 more)

### Community 2 - "CLAUDE.md — JLPT E-Learning System v2.0"
Cohesion: 0.06
Nodes (31): ADR STATUS TABLE, Auth & User Module, Backend (Spring Boot), CLAUDE.md — JLPT E-Learning System v2.0, Core Entities, CORE MODULES, DEVELOPMENT WORKFLOW, Domain (+23 more)

### Community 3 - "Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]"
Cohesion: 0.10
Nodes (20): 11. PROJECT CONTEXT REFERENCES, 1. PROJECT OVERVIEW, 3.1. Java Backend, 3.2. React Frontend, 3.3. API Routes, 3. NAMING CONVENTIONS, 4. PHẠM VI HOẠT ĐỘNG, 5. FORBIDDEN PATTERNS (+12 more)

### Community 4 - "⚪ P3 — Style / theo dõi lâu dài, không khẩn cấp"
Cohesion: 0.11
Nodes (19): BUG-01: Sai import `@Transactional` (đã hạ severity — xem mục 1), BUG-02: `resendVerification` thiếu trong `authService.js` phía frontend, BUG-03: `UserDetailsImpl.isEnabled()` cho phép PENDING login, BUG-04: `StudentUser` Builder Default là ACTIVE, BUG-05: `StaffRoute.jsx`/`ManagerRoute.jsx` trust client role, BUG-06: `docker-compose.yml` — backend không đợi DB ready — **fix ban đầu đã REVERT sau khi verify thực tế**, BUG-07: CD không gate theo kết quả CI (không phải "thiếu CI" — xem mục 2), BUG-08: Rate limit `checkAccountType` dùng in-memory Map (+11 more)

### Community 8 - "Backend"
Cohesion: 0.13
Nodes (15): Backend, DevOps / Config, Frontend, [MODIFY] [application.yml](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/backend/src/main/resources/application.yml), [MODIFY] `apps/backend/.env.example`, [MODIFY] [authService.js](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/frontend/src/api/authService.js), [MODIFY] [authSlice.js](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/apps/frontend/src/store/slices/authSlice.js), [MODIFY] [docker-compose.prod.yml](file:///c:/Users/Tien%20Dat/OneDrive/Documents/GitHub/Japanese-Skill-Practice-Platform/docker-compose.prod.yml) (+7 more)

### Community 9 - "Sửa Đổi So Với Bản Gốc (đọc trước)"
Cohesion: 0.18
Nodes (11): 10. Phát hiện mới: `updateProfileThunk`/BUG-10 nếu xoá `authService.updateProfile` sẽ crash code chết, 1. BUG-01 hạ từ P0/CRITICAL xuống P3 — đã verify bằng `mvn dependency:tree`, 2. BUG-07 sai hoàn toàn — `ci.yml` đã tồn tại, 3. BUG-10 & BUG-16 (authSlice.js) — code chết (dead code), chưa từng bị gọi, 4. BUG-14 (bcrypt cost 12) — khuyến nghị gốc đi ngược ADR-003 của chính dự án, 5. BUG-08 (rate limit in-memory) — hạ tầng Redis chưa thực sự tồn tại trong backend, 6. BUG-12 mở rộng — JWT expiration bị hardcode, không đọc từ env dù có tên biến, 7. BUG-09 (SMTP thiếu ở prod) — đúng, nhưng cần làm rõ 2 cơ chế `.env` khác nhau (+3 more)

### Community 10 - "ADR (Architecture Decision Records)"
Cohesion: 0.22
Nodes (9): ADR-001: Spring Boot 3.x + Java 21, ADR-002: Monolithic Architecture, ADR-003: JWT + bcrypt (cost 12), ADR-004: Soft Delete toàn hệ thống, ADR-005: DTO Pattern bắt buộc, ADR-006: File Media tại /uploads hoặc S3, ADR-007: OCR chỉ so sánh Similarity %, ADR-008: Global Exception Handler (+1 more)

### Community 11 - "🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng)"
Cohesion: 0.22
Nodes (8): Automated Tests, 🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng), Manual Verification, 🚑 Sự cố Production sau khi deploy (2026-07-11) — đã xử lý xong, Tóm tắt Priority (đã cập nhật), 🚨 User Review Required, Verification Plan (đã sửa lại các bước sai), Việc cần quyết định trước khi bắt tay code

### Community 12 - "2. ARCHITECTURE PRINCIPLES"
Cohesion: 0.25
Nodes (8): 2.1. API Design, 2.2. DTO Pattern (BẮT BUỘC), 2.3. Logging, 2.4. Frontend / Backend Separation (BẮT BUỘC), 2. ARCHITECTURE PRINCIPLES, Backend chịu trách nhiệm TOÀN BỘ, Cấm tuyệt đối ở Frontend, Frontend CHỈ được phép

### Community 13 - "ANTI-PATTERNS"
Cohesion: 0.29
Nodes (7): ANTI-PATTERNS, Code Anti-Patterns, Database Anti-Patterns, Integration & AI Anti-Patterns, React Anti-Patterns, Spring Boot Anti-Patterns, Testing Anti-Patterns

### Community 14 - "7. JLPT DOMAIN RULES"
Cohesion: 0.33
Nodes (6): 7.1. Luật Điểm số & Bài thi, 7.2. Luật Lộ trình học, 7.3. Luật Subscription & Phân quyền, 7.4. Luật Soft Delete, 7.5. Luật AI Features (OCR & Speech), 7. JLPT DOMAIN RULES

### Community 15 - "8. GOLDEN PATTERNS"
Cohesion: 0.33
Nodes (6): 8.1. DTO Mapping Pattern, 8.2. Service Layer with Transaction, 8.3. Authorization Check Pattern, 8.4. AI Async Pattern with Fallback, 8.5. Exception Handling Pattern, 8. GOLDEN PATTERNS

### Community 16 - "10. DEFINITION OF DONE"
Cohesion: 0.40
Nodes (5): 10. DEFINITION OF DONE, Code Quality, Database, Domain Rules, Security & Authorization

## Knowledge Gaps
- **124 isolated node(s):** `name`, `private`, `lint:md`, `lint:md:fix`, `prepare` (+119 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng)` connect `🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng)` to `Backend`, `Sửa Đổi So Với Bản Gốc (đọc trước)`, `⚪ P3 — Style / theo dõi lâu dài, không khẩn cấp`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **Why does `CLAUDE.md — JLPT E-Learning System v2.0` connect `CLAUDE.md — JLPT E-Learning System v2.0` to `ADR (Architecture Decision Records)`, `ANTI-PATTERNS`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Why does `Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]` connect `Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]` to `10. DEFINITION OF DONE`, `2. ARCHITECTURE PRINCIPLES`, `7. JLPT DOMAIN RULES`, `8. GOLDEN PATTERNS`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **What connects `name`, `private`, `lint:md` to the rest of the system?**
  _124 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CLAUDE.md — JLPT E-Learning System v2.0` be split into smaller, more focused modules?**
  _Cohesion score 0.0625 - nodes in this community are weakly interconnected._
- **Should `Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]` be split into smaller, more focused modules?**
  _Cohesion score 0.09523809523809523 - nodes in this community are weakly interconnected._
- **Should `⚪ P3 — Style / theo dõi lâu dài, không khẩn cấp` be split into smaller, more focused modules?**
  _Cohesion score 0.10526315789473684 - nodes in this community are weakly interconnected._