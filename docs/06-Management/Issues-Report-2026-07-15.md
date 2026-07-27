# Issues Report

> Generate theo [`SPEC-issues-report-template-generation-guide.md`](SPEC-issues-report-template-generation-guide.md), dựa trên `Temp_Document/Template4_Issues Report.xlsx` (FPT University template).
> **Khoảng thời gian**: 2026-06-28 → 2026-07-15 (7 Pull Request đã merge vào `branch_for_hung`).
> **Nguồn dữ liệu**: `git log --all --merges --grep="Merge pull request"` trên repo `hung02042005/Japanese-Skill-Practice-Platform` — **surrogate từ Pull Request**, KHÔNG phải GitHub Issue thật (repo chưa bật GitHub Issues tại thời điểm này). Môi trường tạo file này không có `gh` CLI nên `Title` lấy từ subject dòng commit đầu merge vào PR (không phải text tiêu đề PR nhập tay trên GitHub UI, có thể lệch nếu tác giả từng sửa tiêu đề PR trên web).
> **Assignee**: tác giả commit thật (`git show -s --format=%an`), không suy đoán.
> **Milestone / Due Date**: repo chưa cấu hình GitHub Milestone và chưa có quy ước sprint nội bộ chính thức → để trống theo đúng checklist của spec (không bịa).

---

| Title | Description | Issue ID | URL | State | Assignee | Created At | Due Date | Milestone | Labels | Functions/Screens |
|---|---|---|---|---|---|---|---|---|---|---|
| update code kanji | Cập nhật tra cứu Kanji (`kanjiLookup.js`, `kanjiCharInfo.json`, `kanjiReadingIndex.json`), sửa `KanjiList.jsx`, `ContentFormModal.jsx` (Staff), `studentService.js` | 2 | [github.com/.../pull/2](https://github.com/hung02042005/Japanese-Skill-Practice-Platform/pull/2) | Merged | Duyxh | 2026-06-28 | | | Task, 3_Done | Kanji (Student) |
| update html kanji and gammar / update css | 2 commit: cập nhật giao diện Mock Test Results, Dashboard, Course List, TopNav (CSS + HTML) | 3 | [github.com/.../pull/3](https://github.com/hung02042005/Japanese-Skill-Practice-Platform/pull/3) | Merged | Duyxh | 2026-07-02 | | | Task, 3_Done | Mock Test / Dashboard / Courses |
| update frontend styles | Chỉnh CSS cho các trang Staff/Manager/Student: `GradingPanel`, `TicketDetail`, `ExamTopBar`, `ChangePassword`, `AssignPanel`, `StaffPageHero` | 4 | [github.com/.../pull/4](https://github.com/hung02042005/Japanese-Skill-Practice-Platform/pull/4) | Merged | Duyxh | 2026-07-02 | | | Task, 3_Done | UI Styling (Staff/Manager/Student) |
| Update kanji and kana / Update kanji | 2 commit: cập nhật `KanjiGridPlayer`, `KanjiStrokeLayer`, `KanjiWritingCanvas`, `KanaDetailModal`, `ContentFormModal` (Staff), Postman collection | 5 | [github.com/.../pull/5](https://github.com/hung02042005/Japanese-Skill-Practice-Platform/pull/5) | Merged | Duyxh | 2026-07-14 | | | Task, 3_Done | Kanji Writing / Kana |
| fix kanji | Sửa `KanjiWritingServiceImpl` (backend) + cấu hình nginx frontend | 6 | [github.com/.../pull/6](https://github.com/hung02042005/Japanese-Skill-Practice-Platform/pull/6) | Merged | Duyxh | 2026-07-14 | | | Defect, 3_Done | Kanji Writing |
| feat: add ManagerDeletedContent feature | Thêm soft delete/restore cho vocabulary topics (`ManagerDeletedContentController/Service` + test), sửa lỗi Kanji Writing, thêm trang Manager Deleted Topics | 7 | [github.com/.../pull/7](https://github.com/hung02042005/Japanese-Skill-Practice-Platform/pull/7) | Merged | Duyxh | 2026-07-14 | | | WP, 3_Done | Manager — Deleted Content / Vocabulary Topics |
| fix: run spotless:apply / fix: add package declaration | 2 commit: sửa lỗi CI Spotless (format + thiếu khai báo `package`) trên `ManagerVocabularyTopicController` và các file liên quan — lỗi CI/lint, không phải defect chức năng | 8 | [github.com/.../pull/8](https://github.com/hung02042005/Japanese-Skill-Practice-Platform/pull/8) | Merged | Duyxh | 2026-07-15 | | | Defect, 3_Done | CI/CD (Spotless) |

---

## Ghi chú

- **Issue ID #1** không tồn tại trong lịch sử PR hiện có (`git log` chỉ thấy PR `#2`–`#8`) — không tự tạo entry giả cho ID này.
- Toàn bộ 7 PR đều do cùng 1 tác giả (`Duyxh`, nhánh `branch_Duy`) merge vào `branch_for_hung` — không có surrogate issue nào của các thành viên khác trong khoảng thời gian này (không có nghĩa các thành viên khác không làm việc, chỉ là lịch sử merge PR không phản ánh commit trực tiếp trên `branch_for_hung`).
- Nhãn loại (`WP`/`Task`/`Defect`) áp theo bảng quy tắc ở spec: PR #7 có prefix `feat:` cho 1 feature trọn vẹn (kèm trang UI mới) → `WP`; PR #6 và #8 có từ khóa/prefix `fix` → `Defect` (dù #8 là lỗi CI/lint, không phải lỗi runtime, đã ghi chú rõ trong `Description`); các PR còn lại không theo Conventional Commits (`update ...`) nên xếp `Task` theo mặc định.
- `State = Merged` (giá trị GitHub thật), không ép về `Closed` như demo GitLab gốc trong template.
