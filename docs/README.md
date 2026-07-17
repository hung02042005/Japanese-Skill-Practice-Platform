# 📚 Tài Liệu Dự Án — JLPT E-Learning Platform

> **Đây là bản đồ điều hướng cho toàn bộ thư mục `docs/`.**
> Người mới vào dự án nên bắt đầu từ đây. Mỗi mục bên dưới trỏ thẳng tới file cần đọc kèm mô tả một dòng.

Tài liệu được tổ chức theo **vòng đời phát triển phần mềm (SDLC)**, đánh số `01 → 06`:

| Thư mục | Giai đoạn | Trả lời câu hỏi |
|---|---|---|
| [`01-SRS-Requirements/`](01-SRS-Requirements/) | Đặc tả yêu cầu | Hệ thống *phải làm gì*? Ràng buộc gì? |
| [`02-SDD-Architecture/`](02-SDD-Architecture/) | Thiết kế hệ thống | Kiến trúc, CSDL, UI/UX *thiết kế ra sao*? |
| [`03-Interface-Specs/`](03-Interface-Specs/) | Đặc tả giao diện & tính năng | Từng tính năng/API/màn hình *hoạt động thế nào*? |
| [`04-Test-Specs/`](04-Test-Specs/) | Đặc tả kiểm thử | *Kiểm chứng* đúng/sai bằng cách nào? |
| [`05-Deployment/`](05-Deployment/) | Triển khai | *Đưa lên production* thế nào? |
| [`06-Management/`](06-Management/) | Quản trị dự án | Hiến pháp dự án, kỹ năng, bảo vệ đồ án |

> 📌 **Tài liệu gốc ở thư mục repo (ngoài `docs/`):** `CLAUDE.md` (bản đồ kiến trúc & ADR), `AGENTS.md` (domain rules), và **[Hiến pháp dự án →](06-Management/constitution.md)** (stack, security, chuẩn code).

---

## 🚀 Bắt đầu từ đâu?

| Bạn là… | Đọc trước |
|---|---|
| **Người mới vào dự án** | File này → [`06-Management/constitution.md`](06-Management/constitution.md) → [`02-SDD-Architecture/system-design/SoDoDuAn.md`](02-SDD-Architecture/system-design/SoDoDuAn.md) |
| **Backend dev** | [`02` Kiến trúc CSDL](02-SDD-Architecture/database-design/JLPT_database.md) → SPEC của feature backend bạn phụ trách (§ 03 bên dưới) |
| **Frontend dev** | [`USER-SPEC`](03-Interface-Specs/feature-specs/frontend/USER-SPEC.md) + [Design System](03-Interface-Specs/feature-specs/frontend/feat-frontend-design/SPEC.md) → SPEC màn hình cụ thể |
| **QA / Tester** | [`04` Chiến lược test](03-Interface-Specs/feature-specs/backend/feat-testing/SPEC.md) → các file `TC-UC-*` |
| **DevOps** | [`05` Hướng dẫn triển khai CloudFly](05-Deployment/CloudFly_VPS_Deployment_Guide.md) |
| **Chuẩn bị bảo vệ đồ án** | [`THESIS_DEFENSE_QNA`](06-Management/THESIS_DEFENSE_QNA.md) |

---

## 01 — SRS · Đặc tả yêu cầu

| File | Nội dung |
|---|---|
| [use-cases/Bao_cao_dac_ta_Use_Case.md](01-SRS-Requirements/use-cases/Bao_cao_dac_ta_Use_Case.md) | Báo cáo đặc tả toàn bộ Use Case |
| [shared_context.md](01-SRS-Requirements/shared_context.md) | Bối cảnh chung của hệ thống |
| [constraints/business.md](01-SRS-Requirements/constraints/business.md) | Ràng buộc nghiệp vụ |
| [constraints/global.md](01-SRS-Requirements/constraints/global.md) | Ràng buộc kỹ thuật toàn cục |
| [constraints/safety.md](01-SRS-Requirements/constraints/safety.md) | Ràng buộc an toàn/vận hành |

## 02 — SDD · Thiết kế hệ thống

| File | Nội dung |
|---|---|
| [system-design/SoDoDuAn.md](02-SDD-Architecture/system-design/SoDoDuAn.md) | Sơ đồ tổng thể dự án |
| [system-design/refactor-layer-to-feature.md](02-SDD-Architecture/system-design/refactor-layer-to-feature.md) | Tái cơ cấu backend: layer-based → feature-based |
| [database-design/JLPT_database.md](02-SDD-Architecture/database-design/JLPT_database.md) | Thiết kế CSDL nền tảng |
| [database-design/MYSQL_MIGRATION_PLAN.md](02-SDD-Architecture/database-design/MYSQL_MIGRATION_PLAN.md) | Kế hoạch migration SQL Server → MySQL 8 (xem ADR-009) |
| [ui-ux-design/DESIGN.md](02-SDD-Architecture/ui-ux-design/DESIGN.md) | Hệ thống thiết kế UI/UX (Hanami Theme) |

## 03 — Interface Specs · Đặc tả giao diện & tính năng

> Thư mục lớn nhất. Chia làm 3 nhóm: **spec liên tính năng** (gốc), **backend** (theo feature), **frontend** (theo màn hình/role).
> Mỗi feature backend thường có bộ: `SPEC` (đặc tả) · `PLAN` (kế hoạch) · `TASKS` (công việc) · `UC-*` (use case) · `TRACEABILITY` (truy vết).

### 03.a — Spec liên tính năng (gốc)

| File | Nội dung |
|---|---|
| [_template.md](03-Interface-Specs/feature-specs/_template.md) | Mẫu SPEC dùng chung |
| [SPEC_CHUNG_FLASHCARD_SRS.md](03-Interface-Specs/feature-specs/SPEC_CHUNG_FLASHCARD_SRS.md) | Spec chung: Flashcard SRS + Dictionary + Bookmark |
| [SPEC_NGUOI4_FLASHCARD_SRS.md](03-Interface-Specs/feature-specs/SPEC_NGUOI4_FLASHCARD_SRS.md) | Spec chi tiết Flashcard SRS + Dictionary + Bookmark |
| [SPEC_BACKEND_FLASHCARD_NOTEBOOK_DICTIONARY.md](03-Interface-Specs/feature-specs/SPEC_BACKEND_FLASHCARD_NOTEBOOK_DICTIONARY.md) | Backend: Flashcard · Sổ tay · Từ điển |
| [AUDIT_CODE_RAC_FLASHCARD_NOTEBOOK_DICTIONARY.md](03-Interface-Specs/feature-specs/AUDIT_CODE_RAC_FLASHCARD_NOTEBOOK_DICTIONARY.md) | Audit code rác Flashcard/Notebook/Dictionary |
| [SPEC_UI_CLEANUP_STAFF_MANAGER.md](03-Interface-Specs/feature-specs/SPEC_UI_CLEANUP_STAFF_MANAGER.md) | Dọn UI thừa Staff & Manager |
| [SPEC_UX_TOAST_AND_ICON_CLEANUP.md](03-Interface-Specs/feature-specs/SPEC_UX_TOAST_AND_ICON_CLEANUP.md) | Toast notifications & dọn emoji icon |

### 03.b — Backend (theo feature)

| Feature | Mô tả | Tài liệu |
|---|---|---|
| **feat-auth** | Xác thực & quản lý tài khoản | [SPEC](03-Interface-Specs/feature-specs/backend/feat-auth/SPEC.md) · [PLAN](03-Interface-Specs/feature-specs/backend/feat-auth/PLAN.md) · [TASKS](03-Interface-Specs/feature-specs/backend/feat-auth/TASKS.md) · UC: [01-login](03-Interface-Specs/feature-specs/backend/feat-auth/UC-01-login.md), [02-register](03-Interface-Specs/feature-specs/backend/feat-auth/UC-02-register.md), [03-reset-password](03-Interface-Specs/feature-specs/backend/feat-auth/UC-03-reset-password.md), [04-user-profile](03-Interface-Specs/feature-specs/backend/feat-auth/UC-04-user-profile.md), [05-change-password](03-Interface-Specs/feature-specs/backend/feat-auth/UC-05-change-password.md), [06-staff-reset](03-Interface-Specs/feature-specs/backend/feat-auth/UC-06-staff-reset-password.md), [18-logout](03-Interface-Specs/feature-specs/backend/feat-auth/UC-18-logout.md) |
| **feat-core-learning** | Nội dung học cốt lõi | [SPEC](03-Interface-Specs/feature-specs/backend/feat-core-learning/SPEC.md) · UC: [06-grammar](03-Interface-Specs/feature-specs/backend/feat-core-learning/UC-06-grammar.md), [07-kanji](03-Interface-Specs/feature-specs/backend/feat-core-learning/UC-07-kanji.md), [08-kana](03-Interface-Specs/feature-specs/backend/feat-core-learning/UC-08-kana.md), [09-vocabulary](03-Interface-Specs/feature-specs/backend/feat-core-learning/UC-09-vocabulary.md) |
| **feat-kanji** | Học Kanji | [SPEC](03-Interface-Specs/feature-specs/backend/feat-kanji/SPEC.md) · [PLAN](03-Interface-Specs/feature-specs/backend/feat-kanji/PLAN.md) · [TASKS](03-Interface-Specs/feature-specs/backend/feat-kanji/TASKS.md) |
| **feat-vocabulary** | Học từ vựng | [SPEC](03-Interface-Specs/feature-specs/backend/feat-vocabulary/SPEC.md) · [PLAN](03-Interface-Specs/feature-specs/backend/feat-vocabulary/PLAN.md) · [TASKS](03-Interface-Specs/feature-specs/backend/feat-vocabulary/TASKS.md) |
| **feat-flashcard-srs** | Flashcard + thuật toán SRS | [SPEC](03-Interface-Specs/feature-specs/backend/feat-flashcard-srs/SPEC.md) · [SPEC-review-deck](03-Interface-Specs/feature-specs/backend/feat-flashcard-srs/SPEC-review-deck.md) · [ALGO-session-ordering](03-Interface-Specs/feature-specs/backend/feat-flashcard-srs/ALGO-session-ordering.md) |
| **feat-dictionary-bookmark** | Từ điển & bookmark | [SPEC](03-Interface-Specs/feature-specs/backend/feat-dictionary-bookmark/SPEC.md) |
| **feat-assessment** | Quiz & kiểm tra | [SPEC](03-Interface-Specs/feature-specs/backend/feat-assessment/SPEC.md) · [PLAN](03-Interface-Specs/feature-specs/backend/feat-assessment/PLAN.md) · [TASKS](03-Interface-Specs/feature-specs/backend/feat-assessment/TASKS.md) · UC: [11-quiz-practice](03-Interface-Specs/feature-specs/backend/feat-assessment/UC-11-quiz-practice.md) |
| **feat-mock-test** | Thi thử JLPT | [SPEC](03-Interface-Specs/feature-specs/backend/feat-mock-test/SPEC.md) · [PLAN](03-Interface-Specs/feature-specs/backend/feat-mock-test/PLAN.md) · [TASKS](03-Interface-Specs/feature-specs/backend/feat-mock-test/TASKS.md) · UC: [10-mock-exam](03-Interface-Specs/feature-specs/backend/feat-mock-test/UC-10-mock-exam.md) |
| **feat-reading-listening** | Luyện đọc & nghe | [SPEC](03-Interface-Specs/feature-specs/backend/feat-reading-listening/SPEC.md) · UC: [14-reading](03-Interface-Specs/feature-specs/backend/feat-reading-listening/UC-14-reading.md), [15-listening](03-Interface-Specs/feature-specs/backend/feat-reading-listening/UC-15-listening.md) |
| **feat-ai-skills** | Kỹ năng AI (nói + OCR viết tay) | [SPEC](03-Interface-Specs/feature-specs/backend/feat-ai-skills/SPEC.md) · UC: [13-speaking](03-Interface-Specs/feature-specs/backend/feat-ai-skills/UC-13-speaking-practice.md), [20-handwriting-ocr](03-Interface-Specs/feature-specs/backend/feat-ai-skills/UC-20-handwriting-ocr.md) |
| **feat-content-management** | Quản lý nội dung (Staff) | [SPEC](03-Interface-Specs/feature-specs/backend/feat-content-management/SPEC.md) · [PLAN](03-Interface-Specs/feature-specs/backend/feat-content-management/PLAN.md) · [TASKS](03-Interface-Specs/feature-specs/backend/feat-content-management/TASKS.md) · [TRACEABILITY](03-Interface-Specs/feature-specs/backend/feat-content-management/TRACEABILITY.md) · UC: [24-question-bank](03-Interface-Specs/feature-specs/backend/feat-content-management/UC-24-manage-question-bank.md), [25-grammar](03-Interface-Specs/feature-specs/backend/feat-content-management/UC-25-manage-grammar-content.md), [26-quiz](03-Interface-Specs/feature-specs/backend/feat-content-management/UC-26-manage-quiz.md), [27-learning-content](03-Interface-Specs/feature-specs/backend/feat-content-management/UC-27-manage-learning-content.md), [28-mock-exams](03-Interface-Specs/feature-specs/backend/feat-content-management/UC-28-manage-jlpt-mock-exams.md) *(mỗi UC có PLAN-UC-xx / TASKS-UC-xx riêng)* |
| **feat-content-review** | Kiểm duyệt nội dung (Manager) | [SPEC](03-Interface-Specs/feature-specs/backend/feat-content-review/SPEC.md) · [PLAN](03-Interface-Specs/feature-specs/backend/feat-content-review/PLAN.md) · [TASKS](03-Interface-Specs/feature-specs/backend/feat-content-review/TASKS.md) · UC: [33-review](03-Interface-Specs/feature-specs/backend/feat-content-review/UC-33-review-submitted-content.md), [34-published-status](03-Interface-Specs/feature-specs/backend/feat-content-review/UC-34-manage-published-content-status.md) |
| **feat-student-management** | Quản lý học viên (Staff) | [SPEC](03-Interface-Specs/feature-specs/backend/feat-student-management/SPEC.md) |
| **feat-support** | Hỗ trợ, thông báo, chấm tay | [SPEC](03-Interface-Specs/feature-specs/backend/feat-support/SPEC.md) |
| **feat-learning-analytics** | Phân tích & báo cáo học tập | [SPEC](03-Interface-Specs/feature-specs/backend/feat-learning-analytics/SPEC.md) |
| **feat-system-admin** | Quản trị hệ thống (Admin) | [SPEC](03-Interface-Specs/feature-specs/backend/feat-system-admin/SPEC.md) · UC: [35-admin-login](03-Interface-Specs/feature-specs/backend/feat-system-admin/UC-35-admin-login.md), [37-manage-user](03-Interface-Specs/feature-specs/backend/feat-system-admin/UC-37-manage-user.md) |
| **feat-testing** | Chiến lược test & test cases | [SPEC](03-Interface-Specs/feature-specs/backend/feat-testing/SPEC.md) · `TC-UC-*`: [01](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-01-login.md) · [02](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-02-register.md) · [03](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-03-reset-password.md) · [04](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-04-user-profile.md) · [05](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-05-change-password.md) · [18](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-18-logout.md) · [24](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-24-manage-question-bank.md) · [25](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-25-manage-grammar-content.md) · [26](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-26-manage-quiz.md) · [27](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-27-manage-learning-content.md) · [28](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-28-manage-jlpt-mock-exams.md) · [33](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-33-review-submitted-content.md) · [34](03-Interface-Specs/feature-specs/backend/feat-testing/TC-UC-34-manage-published-content-status.md) |

### 03.c — Frontend

**Tài liệu tổng quan (đọc trước):**

| File | Nội dung |
|---|---|
| [FRONTEND-FLOW.md](03-Interface-Specs/feature-specs/frontend/FRONTEND-FLOW.md) | Luồng điều hướng frontend tổng thể |
| [USER-SPEC.md](03-Interface-Specs/feature-specs/frontend/USER-SPEC.md) | Hướng dẫn tạo trang Student |
| [MASTERFrontend-Student-Staff-SPEC.md](03-Interface-Specs/feature-specs/frontend/MASTERFrontend-Student-Staff-SPEC.md) | Master-spec trang Student & Staff |
| [MASTERFrontend-SPEC.md](03-Interface-Specs/feature-specs/frontend/MASTERFrontend-SPEC.md) | Master-spec trang Admin |
| [feat-frontend-design/SPEC.md](03-Interface-Specs/feature-specs/frontend/feat-frontend-design/SPEC.md) | Design System |
| [feat-frontend-redux/SPEC.md](03-Interface-Specs/feature-specs/frontend/feat-frontend-redux/SPEC.md) | Kiến trúc React + Redux Toolkit |

**feat-landing-page** — [SPEC](03-Interface-Specs/feature-specs/frontend/feat-landing-page/SPEC.md) · [login](03-Interface-Specs/feature-specs/frontend/feat-landing-page/SPEC-login.md) · [register](03-Interface-Specs/feature-specs/frontend/feat-landing-page/SPEC-register.md) · [forgot-password](03-Interface-Specs/feature-specs/frontend/feat-landing-page/SPEC-forgot-password.md) · [reset-password](03-Interface-Specs/feature-specs/frontend/feat-landing-page/SPEC-reset-password.md)

**feat-dashboard** — [SPEC-dashboard (Student Dashboard)](03-Interface-Specs/feature-specs/frontend/feat-dashboard/SPEC-dashboard.md)

**feat-student** — các màn hình học viên:

| Nhóm | Màn hình |
|---|---|
| Học tập | [vocab-home](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-vocab-home.md) · [vocabulary](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-vocabulary.md) · [learn-new](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-learn-new.md) · [course-list](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-course-list.md) · [lesson-detail](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-lesson-detail.md) · [kana](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-kana.md) · [kanji-list](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-kanji-list.md) · [kanji-practice](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-kanji-practice.md) · [speaking](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-speaking.md) |
| Flashcard / ôn tập | [flashcard](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-flashcard.md) · [flashcard-session](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-flashcard-session.md) · [review](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-review.md) · [notebook](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-notebook.md) · [dictionary](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-dictionary.md) |
| Quiz / thi | [quiz](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-quiz.md) · [mock-test-list](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-mock-test-list.md) · [mock-test-attempt](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-mock-test-attempt.md) · [mock-test-results](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-mock-test-results.md) |
| Tài khoản / khác | [onboarding](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-onboarding.md) · [profile](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-profile.md) · [progress](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-progress.md) · [certificates](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-certificates.md) · [change-password](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-change-password.md) · [verify-email](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-verify-email.md) · [notifications](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-notifications.md) · [subscription](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-subscription.md) · [subscription-success](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-subscription-success.md) · [support-tickets](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-support-tickets.md) · [error-pages](03-Interface-Specs/feature-specs/frontend/feat-student/SPEC-error-pages.md) |

**feat-staff** — [staff-dashboard](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-dashboard.md) · [staff-content](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-content.md) · [staff-questions](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-questions.md) · [staff-assessments](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-assessments.md) · [staff-grading](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-grading.md) · [staff-students](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-students.md) · [staff-tickets](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-tickets.md) · [staff-notifications](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-notifications.md) · [staff-review-queue](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-staff-review-queue.md) · [manager-tickets](03-Interface-Specs/feature-specs/frontend/feat-staff/SPEC-manager-tickets.md)

**feat-admin** — [SPEC (Overview)](03-Interface-Specs/feature-specs/frontend/feat-admin/SPEC.md) · [admin-dashboard](03-Interface-Specs/feature-specs/frontend/feat-admin/SPEC-admin-dashboard.md) · [admin-users](03-Interface-Specs/feature-specs/frontend/feat-admin/SPEC-admin-users.md) · [admin-settings](03-Interface-Specs/feature-specs/frontend/feat-admin/SPEC-admin-settings.md) · [admin-icon-system](03-Interface-Specs/feature-specs/frontend/feat-admin/SPEC-admin-icon-system.md)

## 04 — Test Specs

| File | Nội dung |
|---|---|
| [TEST_SPEC_AUTH_API.md](04-Test-Specs/TEST_SPEC_AUTH_API.md) | Đặc tả test API xác thực |
| [SPEC_VALIDATION_COVERAGE.md](04-Test-Specs/SPEC_VALIDATION_COVERAGE.md) | Đối chiếu validation toàn hệ thống (defense-in-depth) |
| [VALIDATION_ERROR_CATALOG.md](04-Test-Specs/VALIDATION_ERROR_CATALOG.md) | Danh mục lỗi & thông báo validation |
| [SPEC_DEAD_CODE_AUDIT.md](04-Test-Specs/SPEC_DEAD_CODE_AUDIT.md) | Rà soát màn frontend thừa & logic backend thừa |

## 05 — Deployment

| File | Nội dung |
|---|---|
| [README.md](05-Deployment/README.md) | Tổng quan tài liệu triển khai |
| [CloudFly_VPS_Deployment_Guide.md](05-Deployment/CloudFly_VPS_Deployment_Guide.md) | **Hướng dẫn triển khai CloudFly VPS (Docker Compose)** — bản hiện hành |
| [Deploy_Diagram.md](05-Deployment/Deploy_Diagram.md) | Sơ đồ deploy (mermaid, CI/CD staging → production) |
| [Docker_Cheatsheet.md](05-Deployment/Docker_Cheatsheet.md) | Cẩm nang lệnh Docker / Docker Compose khi vận hành VPS |

## 06 — Management

| File | Nội dung |
|---|---|
| [constitution.md](06-Management/constitution.md) | 📜 **Hiến pháp dự án** — stack, security, chuẩn code (đọc bắt buộc) |
| [THESIS_DEFENSE_QNA.md](06-Management/THESIS_DEFENSE_QNA.md) | Kịch bản & câu hỏi bảo vệ đồ án |
| [skills/codegraph_prompts.md](06-Management/skills/codegraph_prompts.md) | Bộ prompt dùng CodeGraph |
| [skills/sql-performance.md](06-Management/skills/sql-performance.md) | Hướng dẫn tối ưu SQL (MySQL 8) |

## Ở gốc `docs/`

| File | Nội dung |
|---|---|
| [implementation_plan.md](implementation_plan.md) | Báo cáo audit & kế hoạch tối ưu nền tảng |

---

## 🧹 Ghi chú bảo trì

Các điểm cần dọn dẹp tiếp — chưa xử lý để tránh mất dữ liệu ngoài ý muốn:

- **File "MOVED":** [feat-flashcard/SPEC.md](03-Interface-Specs/feature-specs/backend/feat-flashcard/SPEC.md) chỉ là con trỏ "đã chuyển"; nội dung thật ở [feat-flashcard-srs/](03-Interface-Specs/feature-specs/backend/feat-flashcard-srs/SPEC.md). Cân nhắc gộp/xóa.
- **Đặt tên chưa nhất quán:** một số file dùng tiền tố theo người (`SPEC_NGUOI4_*`) hoặc dính liền (`MASTERFrontend-*`) thay vì theo feature. Không cấp bách, nhưng nên chuẩn hóa dần.

> Khi thêm/xóa/di chuyển file trong `docs/`, hãy cập nhật lại file `README.md` này để bản đồ luôn khớp thực tế.
