# 🔬 feat_flow — Phân Tích Luồng Tính Năng

> Thư mục chứa các tài liệu **phân tích cấu trúc – luồng – kết nối** của từng tính năng (đọc trực tiếp từ source code).
> File được **giữ phẳng** (không chia thư mục con) vì workflow [`analyze-feature`](../../../.agents/workflows/analyze-feature.md) ghi file mới trực tiếp vào đây, và các file **tham chiếu chéo lẫn nhau** theo tên. Nhóm logic dưới đây chỉ để tra cứu.

Quy ước tên: `<chủ-đề>-feature-analysis.md` (kebab-case). Hậu tố `-legacy` = bản phân tích cũ/rút gọn hơn của cùng chủ đề — xem [§ Bản legacy / nghi trùng](#-bản-legacy--nghi-trùng).

---

## 🔐 Auth

| File | Nội dung |
|---|---|
| [authentication-feature-analysis.md](authentication-feature-analysis.md) | Xác thực (đăng nhập/JWT) — bản đầy đủ |
| [authentication-feature-analysis-legacy.md](authentication-feature-analysis-legacy.md) | 🕰️ Bản cũ của Auth |

## 🛠️ Admin

| File | Nội dung |
|---|---|
| [admin-dashboard-feature-analysis.md](admin-dashboard-feature-analysis.md) · [legacy](admin-dashboard-feature-analysis-legacy.md) | Trang tổng quan quản trị |
| [admin-reports-feature-analysis.md](admin-reports-feature-analysis.md) · [legacy](admin-reports-feature-analysis-legacy.md) | Analytics & Reporting |
| [admin-panel-login-feature-analysis.md](admin-panel-login-feature-analysis.md) · [legacy](admin-panel-login-feature-analysis-legacy.md) | Đăng nhập Admin Panel |
| [admin-user-management-feature-analysis.md](admin-user-management-feature-analysis.md) · [legacy](admin-user-management-feature-analysis-legacy.md) | Quản lý người dùng |
| [admin-notification-rules-feature-analysis.md](admin-notification-rules-feature-analysis.md) · [legacy](admin-notification-rules-feature-analysis-legacy.md) | Quy tắc thông báo tự động |
| [admin-settings-email-security-feature-analysis.md](admin-settings-email-security-feature-analysis.md) · [legacy](admin-settings-email-security-feature-analysis-legacy.md) | Cài đặt hệ thống (Email & Bảo mật) |
| [admin-system-feature-analysis.md](admin-system-feature-analysis.md) | Bảo trì hệ thống, audit log (điểm nối chung của nhiều màn Admin) |

## 👔 Staff

| File | Nội dung |
|---|---|
| [staff-student-management-feature-analysis.md](staff-student-management-feature-analysis.md) | Staff quản lý tài khoản học viên (của người khác) |
| [staff-student-management-feature-analysis-legacy-1.md](staff-student-management-feature-analysis-legacy-1.md) | 🕰️ Bản cũ (`manage_student_accounts`) |
| [staff-student-management-feature-analysis-legacy-2.md](staff-student-management-feature-analysis-legacy-2.md) | 🕰️ Bản cũ (`manager_student_account`) |
| [suspend-activate-account-feature-analysis.md](suspend-activate-account-feature-analysis.md) | Tạm khoá / mở khoá tài khoản |

## 🎓 Student

| File | Nội dung |
|---|---|
| [student-management-feature-analysis.md](student-management-feature-analysis.md) | Student tự quản lý hồ sơ cá nhân |
| [student-ticket-support-feature-analysis.md](student-ticket-support-feature-analysis.md) | Gửi ticket hỗ trợ |
| [student-learning-flow.md](student-learning-flow.md) | Luồng học tập tổng thể của học viên |

## 📚 Nội dung học

| File | Nội dung |
|---|---|
| [kana-feature-analysis.md](kana-feature-analysis.md) | Kana |
| [kanji-feature-analysis.md](kanji-feature-analysis.md) | Kanji |
| [vocabulary-feature-analysis.md](vocabulary-feature-analysis.md) | Từ vựng |
| [flashcard-feature-analysis.md](flashcard-feature-analysis.md) | Flashcard |
| [notebook-feature-analysis.md](notebook-feature-analysis.md) | Sổ tay |
| [dictionary-feature-analysis.md](dictionary-feature-analysis.md) | Từ điển |
| [flashcard-notebook-dictionary-feature-analysis.md](flashcard-notebook-dictionary-feature-analysis.md) | Phân tích gộp Flashcard · Sổ tay · Từ điển |

## 📝 Đánh giá

| File | Nội dung |
|---|---|
| [assessment-mock-test-feature-analysis.md](assessment-mock-test-feature-analysis.md) | Quiz & thi thử |

## 📖 Hướng dẫn

| File | Nội dung |
|---|---|
| [feature-defense-guide.md](feature-defense-guide.md) | Hướng dẫn bảo vệ/trình bày phân tích tính năng |

## 🏗️ Staff CRUD (tạo nội dung)

Thư mục [`feature-StaffCRUD/`](feature-StaffCRUD/): luồng tạo nội dung của Staff — [CreateExam](feature-StaffCRUD/CreateExam.md) · [CreateGammar](feature-StaffCRUD/CreateGammar.md) · [CreateKanji](feature-StaffCRUD/CreateKanji.md) · [CreateQuession](feature-StaffCRUD/CreateQuession.md) · [CreateQuiz](feature-StaffCRUD/CreateQuiz.md) · [CreateSpeaking](feature-StaffCRUD/CreateSpeaking.md) · [CreateVocab](feature-StaffCRUD/CreateVocab.md)

---

## 🔎 Bản legacy / nghi trùng

Trong quá trình dọn dẹp phát hiện **2 thế hệ** tài liệu phân tích cùng chủ đề: bản mới (đầy đủ, tiêu đề *"Phân Tích Feature…"*) và bản cũ/rút gọn (tiêu đề tiếng Anh *"… Feature Analysis"*). Theo yêu cầu, **không gộp/xóa nội dung** — bản cũ được đổi tên `-legacy` để bạn tự rà soát và quyết định.

| Chủ đề | Bản hiện hành | Bản legacy (tên gốc) |
|---|---|---|
| Auth | `authentication-feature-analysis.md` | `authentication-feature-analysis-legacy.md` (gốc `auth_feature_analysis.md`) |
| Admin dashboard | `admin-dashboard-feature-analysis.md` | `…-legacy.md` (gốc `admin_dashboard_…`) |
| Admin reports | `admin-reports-feature-analysis.md` | `…-legacy.md` (gốc `admin_reports_…`) |
| Admin login | `admin-panel-login-feature-analysis.md` | `admin-panel-login-…-legacy.md` (gốc `admin_login_…`) |
| Admin user mgmt | `admin-user-management-feature-analysis.md` | `…-legacy.md` (gốc `user_management_…`) |
| Notification rules | `admin-notification-rules-feature-analysis.md` | `…-legacy.md` (gốc `notification_rules_…`) |
| Admin settings | `admin-settings-email-security-feature-analysis.md` | `…-legacy.md` (gốc `admin_settings_…`) |
| Staff quản lý HV | `staff-student-management-feature-analysis.md` | `…-legacy-1.md` (gốc `manage_student_accounts_…`), `…-legacy-2.md` (gốc `manager_student_account_…`) |

> ✅ **Việc cần làm (bạn quyết định):** với mỗi cặp trên, xác nhận bản `-legacy` đã bị bản hiện hành thay thế hoàn toàn hay chưa; nếu rồi thì `git rm` bản legacy để dứt điểm.
