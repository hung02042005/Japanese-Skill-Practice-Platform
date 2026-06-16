japanese-elearning-project/
│
├── .sdd/                              # ✅ [ĐỔI] Bỏ prefix "1." → tự ẩn trên Linux/Mac
│   ├── constitution.md                # "Hiến pháp" dự án (Hard rules, bảo mật, kiến trúc)
│   ├── shared_context.md             # Ngữ cảnh dùng chung để đồng bộ giữa các AI Agent
│   │
│   ├── constraints/                   # Ràng buộc chi tiết cho AI Agent
│   │   ├── global.md                  # Tech stack (Java, React), Naming convention
│   │   ├── business.md                # Ràng buộc nghiệp vụ (JWT, Soft delete, JLPT rules)
│   │   └── safety.md                  # Ràng buộc an toàn (Cấm xóa DB production)
│   │
│   ├── specs/                         # Đặc tả tính năng chi tiết
│   │   ├── _template.md               # Template mẫu để tạo Spec mới
│   │   ├── feat-auth/                 # Module Đăng nhập / Đăng ký
│   │   │   ├── SPEC.md                # Đặc tả đã chốt (Locked)
│   │   │   ├── PLAN.md                # Kế hoạch thực thi do AI lập
│   │   │   └── TASKS.md               # Danh sách task nhỏ đã chia
│   │   ├── feat-mock-test/            # Tính năng Thi thử JLPT Mock Test
│   │   └── feat-flashcard/            # Tính năng Quản lý Flashcard
│   │
│   ├── skills/                        # ✅ [GIỮ] Thư viện kỹ năng chuyên sâu cho Agent
│   │   └── sql-performance.md         # Kỹ năng tối ưu truy vấn Database
│   │
│   ├── rfcs/                          # Lưu trữ các quyết định kiến trúc (ADR)
│   └── reviews/                       # ✅ [GIỮ] Kết quả AI review Spec để phát hiện lỗi
│
├── .agents/                           # ✅ [ĐỔI] Bỏ prefix "2." → tự ẩn
│   ├── AGENTS.md                      # Persona, công nghệ, giới hạn của Agent
│   ├── CLAUDE.md                      # Project DNA, bài học kinh nghiệm, ngữ cảnh
│   └── .agentignore                   # File cấm AI đọc để tránh nhiễu ngữ cảnh
│
├── .github/                           # ✅ [ĐỔI] Bỏ prefix "6." → dùng tên chuẩn GitHub
│   ├── workflows/
│   │   ├── constitution-check.yml     # Validation gate chặn commit vi phạm quy tắc
│   │   └── consistency-gate.yml       # Kiểm tra độ đồng nhất giữa Code và Spec
│   └── PULL_REQUEST_TEMPLATE/
│       └── prompt_change.md           # Template bắt buộc điền khi sửa AGENTS.md
│
├── apps/                              # 🌟 [THAY ĐỔI LỚN] Monorepo — mã nguồn chính
│   │
│   ├── backend/                       # Toàn bộ code Java / Spring Boot
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/jlpt/
│   │   │   │   │   ├── controller/    # REST Controllers
│   │   │   │   │   ├── service/       # Business Logic
│   │   │   │   │   ├── repository/    # JPA Repositories
│   │   │   │   │   ├── entity/        # JPA Entities
│   │   │   │   │   ├── dto/           # Request / Response DTOs
│   │   │   │   │   ├── mapper/        # Entity ↔ DTO Mappers
│   │   │   │   │   ├── config/        # Spring Security, JWT, CORS config
│   │   │   │   │   └── exception/     # Global Exception Handler
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       └── db/migration/  # Flyway migration scripts (V1__, V2__...)
│   │   │   └── test/                  # Unit & Integration Tests
│   │   │       └── java/com/jlpt/
│   │   └── pom.xml
│   │
│   └── frontend/                      # Toàn bộ code React / TypeScript
│       ├── src/
│       │   ├── components/            # UI Components (PascalCase.tsx)
│       │   ├── pages/                 # Page-level components
│       │   ├── hooks/                 # Custom Hooks (useXxx.ts)
│       │   ├── api/                   # API Client functions
│       │   ├── types/                 # TypeScript Types (XxxType.ts)
│       │   ├── schemas/               # Zod validation schemas
│       │   └── utils/                 # Utility functions
│       ├── cypress/                   # E2E Testing
│       └── package.json
│
├── database/                          # 🌟 [THÊM MỚI] Quản lý Database tập trung
│   ├── init.sql                       # Script tạo DB ban đầu (PostgreSQL / SQL Server)
│   ├── seeds/                         # Dữ liệu mẫu
│   │   ├── kanji_seed.sql             # Dữ liệu Kanji N5–N1
│   │   ├── vocabulary_seed.sql        # Dữ liệu Từ vựng
│   │   └── users_seed.sql             # Tài khoản test (Admin, Student, Staff)
│   └── erd-diagram.png                # Sơ đồ thiết kế Database
│
├── docs/                              # ✅ [ĐỔI] Bỏ prefix "5."
│   ├── api/                           # API Contract (Swagger / OpenAPI JSON)
│   ├── architecture/                  # Sơ đồ luồng hệ thống
│   └── deployment/                    # 🌟 [THÊM MỚI] Hướng dẫn deploy lên server
│
├── plan.md                            # Master Plan — quản lý Task hiện tại
├── docker-compose.yml                 # 🌟 [THÊM MỚI] Môi trường Dev (PostgreSQL, Redis...)
├── .env.example                       # 🌟 [THÊM MỚI] Template biến môi trường
├── AGENTS.md                          # Symlink → .agents/AGENTS.md
└── CLAUDE.md                          # Symlink → .agents/CLAUDE.md