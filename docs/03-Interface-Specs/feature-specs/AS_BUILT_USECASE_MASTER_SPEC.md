# AS-BUILT USE CASE MASTER SPEC

> **Project:** SakuJi JLPT Learning Platform  
> **Scope:** `apps/frontend/src/features`, `apps/backend/src/main/java/com/jlpt/feature`  
> **Version:** 3.0  
> **Last updated:** 2026-07-27  
> **Status:** Source of truth for current feature-spec use cases

---

## 1. Mục Đích

Tài liệu này cập nhật lại use-case spec tổng trong `docs/03-Interface-Specs/feature-specs` theo dự án thực tế đang có route React và REST API. Các feature spec cũ vẫn có thể dùng làm tài liệu chi tiết, nhưng khi mâu thuẫn với tài liệu này thì tài liệu này là bản ưu tiên cho phạm vi as-built.

Nguyên tắc cập nhật:

- Chỉ ghi nhận use case có UI route và/hoặc backend controller hiện hữu.
- Backend là nguồn quyết định nghiệp vụ: scoring, authorization, trạng thái, audit và validation quan trọng.
- Không tính các module chưa triển khai độc lập vào phạm vi nghiệm thu hiện tại.
- StaffManager là `ROLE_STAFF` có `staffRole=staff_manager`, không phải một role Spring Security tách rời.

---

## 2. Actor Và Vùng Chức Năng

| Actor | UI route chính | API chính | Phạm vi |
| :--- | :--- | :--- | :--- |
| Guest | `/`, `/login`, `/register`, `/forgot-password`, `/reset-password`, `/verify-email` | `/api/auth/**`, `/api/staff/auth/**` | Trang công khai, đăng ký, đăng nhập, xác minh email, khôi phục mật khẩu |
| Student | `/dashboard`, `/courses`, `/lessons/:id`, `/kana`, `/vocabulary`, `/grammar`, `/kanji`, `/quiz`, `/mock-test`, `/speaking`, `/dictionary`, `/notebook`, `/progress`, `/support`, `/notifications` | `/api/students/**`, `/api/kana`, `/api/vocabulary`, `/api/grammar-points`, `/api/kanji`, `/api/assessments`, `/api/test-attempts`, `/api/speaking`, `/api/dictionary`, `/api/notebook`, `/api/flashcards`, `/api/support`, `/api/notifications` | Học tập, luyện tập, hồ sơ, tiến độ, ticket, thông báo |
| Staff | `/staff`, `/staff/content`, `/staff/questions`, `/staff/assessments`, `/staff/tickets`, `/staff/grading`, `/staff/students` | `/api/staff/**` | Soạn nội dung, quản lý câu hỏi/quiz/exam, hỗ trợ, chấm bài |
| StaffManager | `/manager`, `/manager/review-queue`, `/manager/content-pipeline`, `/manager/deleted-topics`, `/manager/notifications`, `/manager/tickets` | `/api/manager/**`, một phần `/api/staff/**` | Duyệt nội dung, quản lý publish status, khôi phục nội dung, phân công ticket, broadcast |
| Admin | `/admin`, `/admin/users`, `/admin/settings`, `/admin/reports` | `/api/admin/**` | Dashboard, audit log, user lifecycle, settings, notification rules |

---

## 3. Danh Mục 40 Use Case As-Built

| UC | Tên use case | Actor | UI route | API/controller chính | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| UC-01 | Đăng ký và xác minh email | Guest | `/register`, `/verify-email` | `POST /api/auth/register`, `/verify-email`, `/resend-verification` | Implemented |
| UC-02 | Đăng nhập theo loại tài khoản | Guest, Student, Staff, Admin | `/login` | `POST /api/auth/check-account-type`, `/api/auth/login`, `/api/staff/auth/login` | Implemented |
| UC-03 | Làm mới phiên và đăng xuất | Authenticated user | route guard toàn hệ thống | `POST /api/auth/refresh`, `/api/auth/logout` | Implemented |
| UC-04 | Quên và đặt lại mật khẩu | Guest | `/forgot-password`, `/reset-password` | `POST /api/auth/forgot-password`, `/api/auth/reset-password` | Implemented |
| UC-05 | Thiết lập/khôi phục mật khẩu Staff | Staff | `/staff/setup-password`, `/staff/forgot-password`, `/staff/change-temp-password` | `POST /api/staff/auth/setup-password`, `/forgot-password`, `/change-temp-password` | Implemented |
| UC-06 | Onboarding mục tiêu học tập | Student | `/onboarding` | `POST /api/students/onboarding` | Implemented |
| UC-07 | Dashboard và thống kê cá nhân | Student | `/dashboard` | `GET /api/students/dashboard`, `/me/stats`, `/next-lesson` | Implemented |
| UC-08 | Hồ sơ, avatar, đổi mật khẩu/email | Student | `/profile`, `/settings/change-password`, `/settings/change-email` | `GET/PUT /api/students/me`, `POST /me/avatar`, `PUT /me/password`, email OTP endpoints | Implemented |
| UC-09 | Chọn khóa học và xem bài học | Student | `/courses`, `/lessons/:id` | `GET /api/students/courses`, `GET /api/lessons/{lessonId}`, `POST /api/learning-progress` | Implemented |
| UC-10 | Học Kana | Student | `/kana` | `GET /api/kana` | Implemented |
| UC-11 | Học từ vựng | Student | `/vocabulary` | `GET /api/students/vocab-home`, `GET /api/vocabulary/topics`, `GET /api/vocabulary` | Implemented |
| UC-12 | Học ngữ pháp | Student | `/grammar` | `GET /api/grammar-points`, `GET /api/grammar-points/{grammarId}` | Implemented |
| UC-13 | Học Kanji và luyện viết | Student | `/kanji`, `/kanji/:id` | `GET /api/kanji`, `GET /api/kanji/{kanjiId}`, `POST /api/kanji/writing/evaluate-stroke`, `/attempt` | Implemented |
| UC-14 | Làm quiz | Student | `/quiz` | `GET /api/assessments`, `POST /api/assessments/{id}/start`, `/submit` | Implemented |
| UC-15 | Làm mock exam và xem kết quả | Student | `/mock-test`, `/mock-test/:id/attempt`, `/mock-test/:id/results` | assessment API, `GET /api/test-attempts`, `GET /api/test-attempts/{attemptId}/review` | Implemented |
| UC-16 | Ôn Flashcard SRS | Student | `/vocabulary/flashcard` | `POST /api/flashcards/session`, `POST /api/flashcards/{id}/review` | Implemented |
| UC-17 | Sổ tay từ vựng | Student | `/notebook` | `GET /api/notebook/decks`, `/cards`, `POST /api/notebook/words`, delete/bulk-delete cards | Implemented |
| UC-18 | Tra từ điển | Student | `/dictionary` | `GET /api/dictionary/search`, `GET /api/dictionary/search/{type}` | Implemented |
| UC-19 | Luyện nói và nhận gợi ý AI | Student | `/speaking` | `GET /api/speaking/exercises`, `POST /api/speaking/submit`, `GET /api/speaking/{jobId}` | Implemented |
| UC-20 | Xem/cập nhật tiến độ | Student | `/progress` | `POST /api/learning-progress`, `DELETE /api/learning-progress/reset` | Implemented |
| UC-21 | Ticket hỗ trợ Student | Student | `/support`, `/support/tickets/:ticketId` | `POST/GET /api/support/tickets`, reply/close endpoints | Implemented |
| UC-22 | Thông báo Student | Student | `/notifications` | `GET /api/notifications`, `POST /api/notifications/{id}/read`, `/read-all` | Implemented |
| UC-23 | Dashboard Staff | Staff | `/staff` | `GET /api/staff/dashboard` | Implemented |
| UC-24 | Theo dõi Student | Staff | `/staff/students` | `GET /api/staff/students`, `/progress`, suspend/activate | Implemented |
| UC-25 | Ngân hàng câu hỏi | Staff | `/staff/questions` | `POST/GET/PUT /api/staff/questions`, submit-review | Implemented |
| UC-26 | Nội dung ngữ pháp Staff | Staff | `/staff/content` | `POST/GET/PUT /api/staff/grammar`, submit-review | Implemented |
| UC-27 | Lesson, vocabulary, Kanji, topic, speaking lesson | Staff | `/staff/content` | `/api/staff/lessons`, `/vocabulary`, `/kanji`, `/vocabulary-topics`, `/speaking-lessons` | Implemented |
| UC-28 | Quản lý quiz | Staff | `/staff/assessments` | `/api/staff/assessments`, assign-questions, submit review | Implemented |
| UC-29 | Quản lý mock exam | Staff | `/staff/assessments` | `/api/staff/exams`, assign-questions | Implemented |
| UC-30 | Gửi duyệt và xem feedback | Staff | `/staff/content`, `/staff/questions`, `/staff/assessments` | `POST /api/staff/contents/submit-review`, specialized submit-review endpoints, `GET /api/staff/content/{contentId}/feedback` | Implemented |
| UC-31 | Ticket và broadcast Staff | Staff | `/staff/tickets` | `/api/staff/tickets`, `POST /api/staff/notifications` | Implemented |
| UC-32 | Chấm bài Speaking | Staff | `/staff/grading` | `GET /api/staff/submissions`, `GET /api/staff/submissions/{id}`, `POST /api/staff/submissions/{id}/grade` | Implemented |
| UC-33 | Review queue | StaffManager | `/manager/review-queue` | `GET /api/manager/review-queue`, `GET /api/manager/contents/{id}`, `POST /api/manager/reviews` | Implemented |
| UC-34 | Published content pipeline | StaffManager | `/manager/content-pipeline` | `GET /api/manager/published-contents`, `PUT /published-contents/{id}/status`, restore | Implemented |
| UC-35 | Khôi phục nội dung xóa mềm | StaffManager | `/manager/deleted-topics` | `GET /api/manager/deleted-contents`, `POST /api/manager/deleted-contents/{type}/{id}/restore` | Implemented |
| UC-36 | Điều phối ticket và broadcast | StaffManager | `/manager/tickets`, `/manager/notifications` | `/api/staff/tickets/{id}/assign`, `/reply`, `/close`, `/api/staff/members`, `/api/staff/notifications` | Implemented |
| UC-37 | Admin dashboard và audit log | Admin | `/admin`, `/admin/reports` | `GET /api/admin/dashboard`, `GET /api/admin/audit-logs` | Implemented |
| UC-38 | Quản lý người dùng | Admin | `/admin/users` | `/api/admin/users`, `/staff`, suspend/activate/reset/delete/restore/role endpoints | Implemented |
| UC-39 | Cài đặt hệ thống | Admin | `/admin/settings` | `GET /api/admin/settings/{group}`, `PUT /api/admin/settings/{group}/{key}`, `PUT /api/admin/settings/{group}`, SMTP test | Implemented |
| UC-40 | Quy tắc thông báo | Admin | `/admin/settings?tab=notification` hoặc API-backed admin settings | `GET/POST/PUT /api/admin/notifications/rules` | Implemented API |

---

## 4. Workflow Nghiệm Thu Tổng

| Workflow | UC bao phủ | Evidence hiện hữu |
| :--- | :--- | :--- |
| WF-AUTH-01 Login | UC-02, UC-03 | Auth controller/service tests, JWT tests, frontend login tests |
| WF-AUTH-02 Register OTP | UC-01 | Registration/OTP service tests, frontend register tests |
| WF-AUTH-03 Reset password | UC-04, UC-05 | Password reset service tests, staff reset/setup APIs |
| WF-STU-00 Profile | UC-08 | Student profile/avatar/password/email APIs và routes |
| WF-STU-01 Learning | UC-06 đến UC-13, UC-20 | Student lesson/kana/kanji/grammar/vocabulary/progress controllers |
| WF-STU-02 Assessment | UC-14, UC-15 | Assessment/mock exam controller/service tests |
| WF-STU-03 Flashcard/Notebook/Dictionary | UC-16 đến UC-18 | Flashcard, notebook, dictionary controllers |
| WF-STU-04 Speaking | UC-19, UC-32 | Speaking submit/poll + Staff grading |
| WF-STF-01 Authoring | UC-23 đến UC-30 | Staff content/question/quiz/exam/speaking controllers |
| WF-SUP-01 Support | UC-21, UC-31, UC-36 | Student/staff ticket controllers |
| WF-NOTIF-01 Notifications | UC-22, UC-31, UC-36, UC-40 | Student inbox, staff broadcast, admin rules |
| WF-MGR-01 Review/Publish | UC-33 đến UC-35 | Manager review, published content, deleted content controllers |
| WF-ADM-01 Admin | UC-37 đến UC-40 | Admin dashboard/users/settings/audit/rule controllers |

---

## 5. Legacy / Out Of Current Scope

Các mục dưới đây có thể còn xuất hiện trong một số spec cũ, nhưng **không được tính là current-scope use case** vì không có route/API đầy đủ tương ứng trong dự án thực tế:

| Legacy item | Tình trạng thực tế | Cách xử lý trong feature spec |
| :--- | :--- | :--- |
| Reading Practice độc lập | Không có route React/module backend độc lập | Đánh dấu legacy, không dùng làm nghiệm thu |
| Listening Practice độc lập | Không có route React/module backend độc lập | Đánh dấu legacy, không dùng làm nghiệm thu |
| Bookmark đa loại nội dung | Spec cũ đã deprecated; code hiện có notebook/review deck từ vựng | Dùng UC-17 thay cho bookmark tổng quát |
| Admin analytics/export PDF/CSV/Excel | `/admin/reports` hiện là audit log; không có endpoint export analytics | Không claim là implemented |
| Subscription/payment | Có spec frontend cũ nhưng không có route trong `App.jsx` và không thấy backend subscription controller | Không tính current scope |
| Certificate | Có spec frontend cũ nhưng không có route trong `App.jsx` | Không tính current scope |

---

## 6. Quy Tắc Đồng Bộ Feature Spec

Khi cập nhật từng file trong `feature-specs`, áp dụng các quy tắc sau:

- UC number trong file chi tiết phải trùng bảng 40 UC ở mục 3.
- Nếu giữ spec cũ để tham khảo, thêm nhãn `DEPRECATED` hoặc `LEGACY` ở đầu file.
- API trong spec phải khớp controller hiện có; không dùng endpoint cũ như `/api/bookmarks` hoặc `/api/submissions/speaking` nếu code hiện dùng `/api/notebook`, `/api/flashcards`, `/api/speaking`.
- Frontend spec phải khớp route trong `apps/frontend/src/App.jsx`.
- Những nghiệp vụ điểm số, phân quyền, trạng thái bài nộp, restore/publish, reset password và audit phải được mô tả là backend-owned.
- Nếu thêm schema/table mới thì spec phải nói rõ cần Flyway migration; không mô tả schema mới như đã tồn tại khi code chưa có.

---

## 7. Các File Nên Cập Nhật Tiếp Theo

| File/spec | Vấn đề hiện tại | Hướng cập nhật |
| :--- | :--- | :--- |
| `backend/feat-testing/TEST-PROCESS-COVERAGE.md` | Matrix tổng đã trỏ về 40 UC as-built; các TC file chi tiết vẫn còn mapping legacy | Remap/đổi tên từng `TC-UC-*` theo bảng UC mới |
| `backend/feat-auth/SPEC.md` | UC numbering cũ: login là UC-01, register là UC-02 | Đổi theo as-built: UC-01 register, UC-02 login, UC-03 refresh/logout, UC-04 reset, UC-05 staff password |
| `backend/feat-ai-skills/SPEC.md` | Endpoint speaking/handwriting cũ không khớp controller hiện tại | Trỏ về `/api/speaking` và `/api/kanji/writing/*` |
| `backend/feat-reading-listening/*` | Mô tả module độc lập chưa có code | Đánh dấu LEGACY |
| `backend/feat-dictionary-bookmark/SPEC.md` | Đã deprecated, còn endpoint `/api/bookmarks` cũ | Giữ deprecated, dẫn sang UC-17/UC-18 |
| `backend/feat-learning-analytics/SPEC.md` | Có export analytics chưa implemented | Đánh dấu future/legacy đối với Admin Reports |
| `frontend/feat-student/SPEC-subscription*.md`, `SPEC-certificates.md` | Không có route hiện tại trong `App.jsx` | Đánh dấu future scope |

---

## 8. Kết Luận

Feature-spec tổng hiện hành gồm **40 use case as-built**. Phạm vi này phản ánh đúng các route frontend và controller backend hiện có vào ngày 27/07/2026. Các tài liệu chi tiết nên được rà soát dần để đồng bộ với bảng này, đặc biệt là nhóm auth, testing coverage, AI skills, reading/listening, analytics/export và subscription/certificate.
