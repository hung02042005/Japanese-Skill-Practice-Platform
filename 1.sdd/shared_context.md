# Shared Context — Ngữ Cảnh Chung

> **Nguồn sự thật chung** để đồng bộ giữa tất cả AI Agent và thành viên nhóm.
> Cập nhật file này mỗi khi có quyết định kiến trúc hoặc nghiệp vụ quan trọng.

---

## 📋 Tổng quan dự án

**Japanese E-Learning Platform** — Nền tảng học tiếng Nhật trực tuyến hỗ trợ luyện thi JLPT (N1–N5).

## 🧩 Domain Entities cốt lõi

| Entity       | Mô tả |
|--------------|-------|
| `User`       | Người dùng (Student / Teacher / Admin) |
| `Course`     | Khóa học |
| `Lesson`     | Bài học trong Course |
| `Vocabulary` | Từ vựng (kanji, reading, meaning, level) |
| `MockTest`   | Đề thi thử JLPT |
| `Question`   | Câu hỏi trong MockTest |
| `Flashcard`  | Thẻ ghi nhớ cá nhân của Student |
| `StudySession` | Phiên học của Student |

## 📌 Trạng thái Features

| Feature | ID | Status |
|---------|----|--------|
| Đăng nhập / Đăng ký | feat-auth | 🟡 Đang phát triển |
| Thi thử JLPT | feat-mock-test | 🔵 Đang lên Spec |
| Quản lý Flashcard | feat-flashcard | 🔵 Đang lên Spec |

## 🔗 Các quyết định kiến trúc gần nhất

- **2026-05-27**: Chọn Prisma thay vì TypeORM → ghi lại tại `.sdd/rfcs/`
- **2026-05-27**: Áp dụng SM-2 algorithm cho Flashcard review
- **2026-05-27**: Dùng cursor-based pagination cho tất cả list API
