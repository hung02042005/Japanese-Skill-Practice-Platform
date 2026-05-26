# Constitution — Hiến Pháp Dự Án

> **Tài liệu bất biến.** Mọi AI Agent và thành viên nhóm PHẢI tuân thủ tuyệt đối.
> Muốn thay đổi file này phải được toàn team approve qua Pull Request.

---

## ⚙️ Kiến trúc (Architecture)

- Áp dụng **Clean Architecture**: `domain → usecase → interface → infra`
- Dependency chỉ đi một chiều, tầng trong KHÔNG được import tầng ngoài
- Frontend và Backend hoàn toàn tách biệt (SPA + REST API)

## 🛠️ Tech Stack (BẤT BIẾN)

| Layer     | Technology                    |
|-----------|-------------------------------|
| Frontend  | React 18 + TypeScript + Vite  |
| Backend   | Node.js 20 + Express + TypeScript |
| ORM       | Prisma                        |
| Database  | PostgreSQL 15                 |
| Cache     | Redis                         |
| Testing   | Jest + Supertest              |
| CI/CD     | GitHub Actions                |

## 🔐 Bảo mật (Security)

- JWT Access Token hết hạn sau **15 phút**
- JWT Refresh Token hết hạn sau **7 ngày**, lưu trong `httpOnly cookie`
- Không bao giờ commit `.env`, secret keys lên Git
- Mọi public endpoint phải có **rate limiting**
- Input validation bắt buộc ở tầng `interface`

## 🗄️ Quy tắc Database

- Mọi entity chính PHẢI có trường `deletedAt DateTime?` (**Soft Delete**)
- Query mặc định PHẢI filter `WHERE deletedAt IS NULL`
- **KHÔNG** dùng `DELETE FROM` trong production code

## 📐 Naming Convention

- Folders: `kebab-case`
- Files TypeScript: `camelCase.ts` (logic), `PascalCase.ts` (class/component)
- Variables/Functions: `camelCase`
- Classes/Types/Interfaces: `PascalCase`
- Constants: `UPPER_SNAKE_CASE`
- Database columns: `snake_case`

## 🚀 Versioning

- Semantic Versioning: `MAJOR.MINOR.PATCH`
- Deploy production CHỈ qua GitHub Actions CI/CD, KHÔNG deploy thủ công
