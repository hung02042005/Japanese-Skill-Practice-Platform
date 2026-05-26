japanese-elearning-project/
│
├── 1.sdd/                         # Toàn bộ "não" đặc tả của dự án (SDD artifacts) [3]
│   ├── constitution.md            # "Hiến pháp" dự án (Hard rules, bảo mật, kiến trúc) [3, 4]
│   ├── shared_context.md          # Nguồn sự thật chung để đồng bộ giữa các AI Agent [3, 5]
│   ├── constraints/               # Ràng buộc chi tiết cho AI Agent [3, 6]
│   │   ├── global.md              # Ràng buộc Tech stack (Node.js, React), Naming convention [7]
│   │   ├── business.md            # Ràng buộc nghiệp vụ (Ví dụ: quy tắc JWT, Soft delete) [8]
│   │   └── safety.md              # Ràng buộc an toàn (Ví dụ: Cấm xóa DB production) [9]
│   ├── specs/                     # Đặc tả tính năng chi tiết (Feature specs) [3, 10]
│   │   ├── _template.md           # Template mẫu để tạo Spec mới [10]
│   │   ├── feat-auth/             # Đặc tả module Đăng nhập/Đăng ký [11, 12]
│   │   │   ├── SPEC.md            # File đặc tả đã chốt (Locked spec) [3, 13]
│   │   │   ├── PLAN.md            # Kế hoạch thực thi do AI lập [3, 14]
│   │   │   └── TASKS.md           # Danh sách các task nhỏ đã chia [3, 15]
│   │   ├── feat-mock-test/        # Đặc tả tính năng Thi thử JLPT Mock Test [16]
│   │   └── feat-flashcard/        # Đặc tả tính năng Quản lý Flashcard [17]
│   ├── skills/                    # Thư viện kỹ năng chuyên sâu (SKILL.md) cho Agent [3, 18]
│   │   └── sql-performance.md     # Ví dụ: Kỹ năng tối ưu truy vấn Database [19]
│   ├── rfcs/                      # Lưu trữ các quyết định thay đổi kiến trúc (ADR) [3, 20]
│   └── reviews/                   # Lưu kết quả AI review Spec để phát hiện lỗi [3, 10]
│
├── 2.agents/                      # Thư mục chứa cấu hình AI Agent [3]
│   ├── AGENTS.md                  # Định nghĩa Persona, công nghệ, giới hạn của Agent [3, 21]
│   ├── CLAUDE.md                  # Project DNA, bài học kinh nghiệm, ngữ cảnh làm việc [3, 22]
│   └── .agentignore               # Danh sách file cấm AI đọc để tránh nhiễu ngữ cảnh [3, 23]
│
├── 3.src/                         # Source Code cốt lõi (Áp dụng Clean Architecture) [3]
│   ├── domain/                    # Chứa Entities cốt lõi (Student, Course, MockTest...) [3]
│   ├── usecase/                   # Chứa Business logic (Quy tắc tính điểm thi, xếp loại) [3]
│   ├── interface/                 # HTTP routes, Controllers (Express/React) [3]
│   └── infra/                     # Data access, Prisma setup, cấu hình kết nối PostgreSQL [3]
│
├── 4.tests/                       # Thư mục kiểm thử tự động [3]
│   ├── unit/                      # Unit tests cho các logic độc lập (Không DB/Mạng) [3]
│   ├── integration/               # Integration tests (Kiểm tra API kết hợp Database) [3]
│   └── e2e/                       # End-to-End tests cho toàn bộ luồng sử dụng [3]
│
├── 5.docs/                        # Tài liệu kỹ thuật của dự án [3]
│   ├── api/                       # File OpenAPI / Swagger specs cho các endpoints [3]
│   └── architecture/              # Sơ đồ thiết kế hệ thống, Database Schema [3]
│
├── 6.github/                      # Pipelines CI/CD để tự động hóa [3, 24]
│   ├── workflows/                 # GitHub Actions workflows
│   │   ├── constitution-check.yml # Validation gate chặn commit vi phạm quy tắc [25]
│   │   └── consistency-gate.yml  # Kiểm tra độ đồng nhất giữa Code và Spec [26]
│   └── PULL_REQUEST_TEMPLATE/     # 🌟 [THÊM MỚI] Thư mục chứa các mẫu Pull Request
│       └── prompt_change.md       # 🌟 [THÊM MỚI] Template bắt buộc điền khi sửa file AGENTS.md
│
├── plan.md                        # File theo dõi tiến độ Task hiện tại (Plan-Act-Check) [3, 27]
├── AGENTS.md                      # Symlink trỏ về 2.agents/AGENTS.md [3]
└── CLAUDE.md                      # Symlink trỏ về 2.agents/CLAUDE.md [3]