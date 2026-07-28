# 🔬 feat_flow — Phân Tích Luồng Tính Năng

> Thư mục chứa các tài liệu **phân tích cấu trúc – luồng – kết nối** của từng tính năng (đọc trực tiếp từ source code).
> File được **giữ phẳng** (không chia thư mục con) vì workflow [`analyze-feature`](../../../.agents/workflows/analyze-feature.md) ghi file mới trực tiếp vào đây, và các file **tham chiếu chéo lẫn nhau** theo tên. Nhóm logic dưới đây chỉ để tra cứu.

Quy ước tên: `<chủ-đề>-feature-analysis.md` (kebab-case).

---

## 🔐 Auth

| File | Nội dung |
|---|---|
| [authentication-feature-analysis.md](authentication-feature-analysis.md) | Xác thực (đăng nhập/JWT) |

## 🛠️ Admin

| File | Nội dung |
|---|---|
| [admin-dashboard-feature-analysis.md](admin-dashboard-feature-analysis.md) | Trang tổng quan quản trị |
| [admin-reports-feature-analysis.md](admin-reports-feature-analysis.md) | Analytics & Reporting |
| [admin-panel-login-feature-analysis.md](admin-panel-login-feature-analysis.md) | Đăng nhập Admin Panel |
| [admin-user-management-feature-analysis.md](admin-user-management-feature-analysis.md) | Quản lý người dùng |
| [admin-notification-rules-feature-analysis.md](admin-notification-rules-feature-analysis.md) | Quy tắc thông báo tự động |
| [admin-settings-email-security-feature-analysis.md](admin-settings-email-security-feature-analysis.md) | Cài đặt hệ thống (Email & Bảo mật) |
| [admin-system-feature-analysis.md](admin-system-feature-analysis.md) | Bảo trì hệ thống, audit log (điểm nối chung của nhiều màn Admin) |

## 👔 Staff

| File | Nội dung |
|---|---|
| [staff-student-management-feature-analysis.md](staff-student-management-feature-analysis.md) | Staff quản lý tài khoản học viên (của người khác) |
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

> 🧹 **Đã dọn:** trước đây thư mục có 2 thế hệ phân tích cùng chủ đề (bản mới đầy đủ + bản cũ rút gọn theo template 7 mục). Các bản cũ trùng chủ đề đã được **xóa** (còn khôi phục được từ lịch sử git). Mỗi chủ đề nay chỉ còn **một** tài liệu phân tích hiện hành ở trên.
