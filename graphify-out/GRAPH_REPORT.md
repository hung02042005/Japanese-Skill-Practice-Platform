# Graph Report - Japanese-Skill-Practice-Platform  (2026-07-17)

## Corpus Check
- 3 files · ~60,466 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 114 nodes · 107 edges · 16 communities (13 shown, 3 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 1 edges (avg confidence: 0.7)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `150f4eec`
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
- 8. GOLDEN PATTERNS
- 10. DEFINITION OF DONE

## God Nodes (most connected - your core abstractions)
1. `CLAUDE.md — JLPT E-Learning System v2.0` - 14 edges
2. `Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]` - 12 edges
3. `ADR (Architecture Decision Records)` - 10 edges
4. `ANTI-PATTERNS` - 7 edges
5. `7. JLPT DOMAIN RULES` - 6 edges
6. `8. GOLDEN PATTERNS` - 6 edges
7. `LESSONS LEARNED` - 6 edges
8. `2. ARCHITECTURE PRINCIPLES` - 5 edges
9. `10. DEFINITION OF DONE` - 5 edges
10. `scripts` - 4 edges

## Surprising Connections (you probably didn't know these)
- `authSlice` --calls--> `authService`  [EXTRACTED]
  apps/frontend/src/store/slices/authSlice.js → apps/frontend/src/api/authService.js

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Deployment Environments** — docker_compose_yml, docker_compose_prod_yml, docker_compose_staging_yml [EXTRACTED 1.00]

## Communities (16 total, 3 thin omitted)

### Community 0 - "package.json"
Cohesion: 0.17
Nodes (11): husky, markdownlint-cli2, devDependencies, husky, markdownlint-cli2, name, private, scripts (+3 more)

### Community 2 - "CLAUDE.md — JLPT E-Learning System v2.0"
Cohesion: 0.09
Nodes (21): ADR STATUS TABLE, Auth & User Module, Backend (Spring Boot), CLAUDE.md — JLPT E-Learning System v2.0, Core Entities, CORE MODULES, DEVELOPMENT WORKFLOW, DOMAIN MODEL (+13 more)

### Community 3 - "Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]"
Cohesion: 0.11
Nodes (18): 11. PROJECT CONTEXT REFERENCES, 1. PROJECT OVERVIEW, 4. PHẠM VI HOẠT ĐỘNG, 5. FORBIDDEN PATTERNS, 6.1. Success Response, 6.2. Error Response, 6.3. Standard HTTP Status Codes, 6. API RESPONSE FORMAT (+10 more)

### Community 4 - "⚪ P3 — Style / theo dõi lâu dài, không khẩn cấp"
Cohesion: 0.33
Nodes (6): LESSON-001: Tách UI Staff và Admin, LESSON-002: Không lưu BLOB trong DB, LESSON-003: Authorization = Role + Subscription, LESSON-005: Quiz câu hỏi phải Lock, LESSON-006: AI không được Silent Fail, LESSONS LEARNED

### Community 8 - "Backend"
Cohesion: 0.50
Nodes (4): 3.1. Java Backend, 3.2. React Frontend, 3.3. API Routes, 3. NAMING CONVENTIONS

### Community 9 - "Sửa Đổi So Với Bản Gốc (đọc trước)"
Cohesion: 0.50
Nodes (4): 9.1. When to Ask, 9.2. High-Risk Operations, 9.3. Refactor Safeguards, 9. XỬ LÝ LỖI & AN TOÀN THAO TÁC

### Community 10 - "ADR (Architecture Decision Records)"
Cohesion: 0.20
Nodes (10): ADR-001: Spring Boot 3.x + Java 21, ADR-002: Monolithic Architecture, ADR-003: JWT + bcrypt (cost 12), ADR-004: Soft Delete toàn hệ thống, ADR-005: DTO Pattern bắt buộc, ADR-006: File Media tại /uploads hoặc S3, ADR-007: OCR chỉ so sánh Similarity %, ADR-008: Global Exception Handler (+2 more)

### Community 11 - "🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng)"
Cohesion: 0.50
Nodes (4): Domain, Key Rules, Tech Stack (Xem `CONSTITUTION.md § ĐIỀU 1`), TL;DR (60 giây)

### Community 12 - "2. ARCHITECTURE PRINCIPLES"
Cohesion: 0.25
Nodes (8): 2.1. API Design, 2.2. DTO Pattern (BẮT BUỘC), 2.3. Logging, 2.4. Frontend / Backend Separation (BẮT BUỘC), 2. ARCHITECTURE PRINCIPLES, Backend chịu trách nhiệm TOÀN BỘ, Cấm tuyệt đối ở Frontend, Frontend CHỈ được phép

### Community 13 - "ANTI-PATTERNS"
Cohesion: 0.29
Nodes (7): ANTI-PATTERNS, Code Anti-Patterns, Database Anti-Patterns, Integration & AI Anti-Patterns, React Anti-Patterns, Spring Boot Anti-Patterns, Testing Anti-Patterns

### Community 15 - "8. GOLDEN PATTERNS"
Cohesion: 0.33
Nodes (6): 8.1. DTO Mapping Pattern, 8.2. Service Layer with Transaction, 8.3. Authorization Check Pattern, 8.4. AI Async Pattern with Fallback, 8.5. Exception Handling Pattern, 8. GOLDEN PATTERNS

### Community 16 - "10. DEFINITION OF DONE"
Cohesion: 0.40
Nodes (5): 10. DEFINITION OF DONE, Code Quality, Database, Domain Rules, Security & Authorization

## Knowledge Gaps
- **83 isolated node(s):** `name`, `private`, `lint:md`, `lint:md:fix`, `prepare` (+78 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CLAUDE.md — JLPT E-Learning System v2.0` connect `CLAUDE.md — JLPT E-Learning System v2.0` to `ADR (Architecture Decision Records)`, `🔍 Báo Cáo Audit & Kế Hoạch Tối Ưu — JLPT E-Learning Platform (v2 — đã kiểm chứng)`, `⚪ P3 — Style / theo dõi lâu dài, không khẩn cấp`, `ANTI-PATTERNS`?**
  _High betweenness centrality (0.162) - this node is a cross-community bridge._
- **Why does `Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]` connect `Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]` to `Backend`, `Sửa Đổi So Với Bản Gốc (đọc trước)`, `2. ARCHITECTURE PRINCIPLES`, `8. GOLDEN PATTERNS`, `10. DEFINITION OF DONE`?**
  _High betweenness centrality (0.142) - this node is a cross-community bridge._
- **Why does `ADR (Architecture Decision Records)` connect `ADR (Architecture Decision Records)` to `CLAUDE.md — JLPT E-Learning System v2.0`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **What connects `name`, `private`, `lint:md` to the rest of the system?**
  _83 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CLAUDE.md — JLPT E-Learning System v2.0` be split into smaller, more focused modules?**
  _Cohesion score 0.09090909090909091 - nodes in this community are weakly interconnected._
- **Should `Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]` be split into smaller, more focused modules?**
  _Cohesion score 0.10526315789473684 - nodes in this community are weakly interconnected._