# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r".skill/docx-report/scripts")
from docx_tools import *

doc = Document("docs/07-Release-Documents/RDS_Document.docx")

# ---------------------------------------------------------------- Actors ---
actors = [
    ("1", "Student", "Học viên — đối tượng người dùng cuối, học tập và luyện thi JLPT (20 Use Case)."),
    ("2", "Staff", "Nhân viên — soạn thảo nội dung học tập, gửi duyệt, chấm bài thủ công, hỗ trợ học viên (12 Use Case)."),
    ("3", "StaffManager", "Quản lý nội dung — là Staff với staff_role='staff_manager'; duyệt/từ chối/xuất bản nội dung Staff gửi (2 Use Case)."),
    ("4", "Admin", "Quản trị viên — quản lý hệ thống ở cấp cao nhất: phân quyền người dùng, cấu hình kỹ thuật, báo cáo (6 Use Case)."),
]
t = find_table_after(doc, "1.1 Actors")
set_cell_text(t.rows[1].cells[0], actors[0][0]); set_cell_text(t.rows[1].cells[1], actors[0][1]); set_cell_text(t.rows[1].cells[2], actors[0][2])
for a in actors[1:]:
    add_table_row_like_last(t, list(a))

# --------------------------------------------------------- UC Descriptions ---
# (Mã, Nhóm/Feature, Ten UC, Mo ta)
uc_list = [
("UC-01","Học viên","User Login (Đăng nhập)","Khách xác thực bằng Email/Mật khẩu hoặc Google OAuth để truy cập hệ thống với tư cách Học viên. [Chi tiết đầy đủ ở Mục II]"),
("UC-02","Học viên","User Register (Đăng ký tài khoản)","Người dùng tạo tài khoản mới, xác minh bằng mã OTP 6 số gửi qua email. [Chi tiết đầy đủ ở Mục II]"),
("UC-03","Học viên","Reset Password (Khôi phục mật khẩu)","Người dùng quên mật khẩu, đặt lại qua liên kết email có hạn 15 phút. [Chi tiết đầy đủ ở Mục II]"),
("UC-04","Học viên","User Profile (Hồ sơ cá nhân)","Học viên xem/cập nhật hồ sơ cá nhân, ảnh đại diện. [Chi tiết đầy đủ ở Mục II]"),
("UC-05","Học viên","Change Password (Đổi mật khẩu)","Học viên đang đăng nhập chủ động đổi mật khẩu, xác minh bằng mật khẩu hiện tại. [Chi tiết đầy đủ ở Mục II]"),
("UC-06","Học viên","Learn Grammar (Học ngữ pháp)","Học viên chọn cấp độ JLPT, xem cấu trúc/nghĩa/ví dụ ngữ pháp và đánh dấu đã học. [Chi tiết đầy đủ ở Mục II]"),
("UC-07","Học viên","Learn Kanji (Học chữ Hán)","Học viên xem danh sách/chi tiết Kanji theo cấp độ, đánh dấu đã học, thêm Flashcard. [Chi tiết đầy đủ ở Mục II]"),
("UC-08","Học viên","Learn Kana (Học bảng chữ cái Kana)","Học viên xem bảng Hiragana/Katakana, nghe phát âm, đánh dấu đã học. [Chi tiết đầy đủ ở Mục II]"),
("UC-09","Học viên","Vocabulary (Học từ vựng)","Học viên xem từ vựng theo cấp độ/chủ đề, đánh dấu đã học, thêm Flashcard. [Chi tiết đầy đủ ở Mục II]"),
("UC-10","Học viên","Take JLPT Mock Test (Làm bài thi thử JLPT)","Học viên làm đề thi thử đầy đủ 3 phần với đồng hồ đếm ngược, nhận điểm đạt/không đạt. [Chi tiết đầy đủ ở Mục II]"),
("UC-11","Học viên","Practice & Quiz (Luyện tập trắc nghiệm)","Học viên làm quiz theo chủ đề, nhận điểm và giải thích ngay sau khi nộp. [Chi tiết đầy đủ ở Mục II]"),
("UC-12","Học viên","Flashcard Learning (Học bằng thẻ ghi nhớ)","Học viên ôn tập từ vựng theo thuật toán lặp lại ngắt quãng (SRS). Chưa có file UC-NN chi tiết riêng."),
("UC-13","Học viên","Speaking Practice & AI Grading (Luyện nói & AI chấm điểm)","Học viên ghi âm luyện nói, hệ thống chấm điểm phát âm/lưu loát bất đồng bộ. [Chi tiết đầy đủ ở Mục II]"),
("UC-14","Học viên","Reading Practice (Luyện đọc hiểu)","Học viên đọc đoạn văn tiếng Nhật, trả lời câu hỏi trắc nghiệm. [Chi tiết đầy đủ ở Mục II]"),
("UC-15","Học viên","Listening Practice (Luyện nghe hiểu)","Học viên nghe audio, trả lời câu hỏi, xem transcript sau khi nộp. [Chi tiết đầy đủ ở Mục II]"),
("UC-16","Học viên","Dictionary & Search (Tìm kiếm & Tra từ điển)","Học viên tra cứu Kanji/từ vựng theo từ khóa. Chưa có file UC-NN chi tiết riêng."),
("UC-17","Học viên","Bookmark Learning (Đánh dấu nội dung)","Học viên đánh dấu nội dung học để xem lại sau. Chưa có file UC-NN chi tiết riêng."),
("UC-18","Học viên","Logout (Đăng xuất)","Học viên đăng xuất, thu hồi token session hiện tại. [Chi tiết đầy đủ ở Mục II]"),
("UC-19","Học viên","Learning Progress & Stats (Tiến độ học tập)","Học viên xem thống kê tiến độ học tập cá nhân. Chưa có file UC-NN chi tiết riêng."),
("UC-20","Học viên","AI Handwriting Practice (Luyện viết chữ bằng AI)","Học viên vẽ Kanji/Kana trên canvas, AI OCR so sánh % giống ký tự chuẩn. [Chi tiết đầy đủ ở Mục II]"),
("UC-21","Staff","View Student Progress (Theo dõi học viên)","Staff xem tiến độ học tập của học viên. Chưa có file UC-NN chi tiết riêng."),
("UC-22","Staff","Manage Student Accounts (Quản lý học viên)","Staff quản lý thông tin tài khoản học viên. Chưa có file UC-NN chi tiết riêng."),
("UC-23","Staff","Suspend or Activate Account (Khóa / Mở tài khoản)","Staff khóa hoặc mở khóa tài khoản học viên. Chưa có file UC-NN chi tiết riêng."),
("UC-24","Staff","Manage Question Bank (Quản lý ngân hàng câu hỏi)","Staff tạo/sửa/tìm kiếm câu hỏi, gửi duyệt; câu hỏi đã dùng trong bài làm bị khóa. [Chi tiết đầy đủ ở Mục II]"),
("UC-25","Staff","Manage Grammar Content (Quản lý nội dung ngữ pháp)","Staff tạo/sửa điểm ngữ pháp, liên kết bài học, gửi duyệt. [Chi tiết đầy đủ ở Mục II]"),
("UC-26","Staff","Manage Quiz (Quản lý bài Quiz)","Staff tạo quiz, gán câu hỏi (Σ điểm phải khớp total_score), gửi duyệt. [Chi tiết đầy đủ ở Mục II]"),
("UC-27","Staff","Manage Learning Content (Quản lý nội dung bài học)","Staff tạo/sửa bài học, từ vựng, Kanji, gửi duyệt. [Chi tiết đầy đủ ở Mục II]"),
("UC-28","Staff","Manage JLPT Mock Exams (Quản lý đề thi thử JLPT)","Staff tạo đề thi thử, chia section, gán câu hỏi, gửi duyệt. [Chi tiết đầy đủ ở Mục II]"),
("UC-29","Staff","Respond to Student Support (Hỗ trợ người dùng)","Staff phản hồi ticket hỗ trợ của học viên. Chưa có file UC-NN chi tiết riêng."),
("UC-30","Staff","Send Notifications (Gửi thông báo)","Staff gửi thông báo tới học viên. Chưa có file UC-NN chi tiết riêng."),
("UC-31","Staff","Grade Speaking Submission (Chấm bài nói)","Staff xem bài nói đã AI chấm, ghi đè điểm/nhận xét thủ công. Chưa có file UC-NN chi tiết riêng (xem SDS §5)."),
("UC-32","Staff","View Quiz Results (Xem kết quả kiểm tra)","Staff xem kết quả làm bài của học viên. Chưa có file UC-NN chi tiết riêng."),
("UC-33","StaffManager","Review Submitted Content (Duyệt nội dung Staff gửi)","StaffManager duyệt/từ chối/yêu cầu sửa nội dung pending_review, theo nguyên tắc “bốn mắt”. [Chi tiết đầy đủ ở Mục II]"),
("UC-34","StaffManager","Manage Published Content Status (Quản lý trạng thái xuất bản)","StaffManager unpublish/archive/delete/restore nội dung đã published. [Chi tiết đầy đủ ở Mục II]"),
("UC-35","Admin","Login System (Đăng nhập Admin Panel)","Admin đăng nhập qua màn hình chung, hệ thống tự nhận diện role theo email. [Chi tiết đầy đủ ở Mục II]"),
("UC-36","Admin","View Dashboard (Bảng điều khiển)","Admin xem thống kê tổng quan hệ thống. Chưa có file UC-NN chi tiết riêng."),
("UC-37","Admin","User Management (Quản lý người dùng)","Admin quản lý toàn bộ tài khoản Student/Staff/Admin: tạo, sửa, khóa, đặt lại mật khẩu, xóa mềm. [Chi tiết đầy đủ ở Mục II]"),
("UC-38","Admin","Report Screen (Báo cáo & Thống kê)","Admin xem báo cáo thống kê hệ thống. Chưa có file UC-NN chi tiết riêng."),
("UC-39","Admin","Settings (Cài đặt hệ thống)","Admin cấu hình SMTP, bảo mật, chế độ bảo trì. Chưa có file UC-NN chi tiết riêng."),
("UC-40","Admin","Notification Rules (Quản lý quy tắc thông báo)","Admin cấu hình quy tắc gửi thông báo tự động. Chưa có file UC-NN chi tiết riêng."),
]
t = find_table_after(doc, "b. Descriptions")
r0 = uc_list[0]
set_cell_text(t.rows[1].cells[0], r0[0]); set_cell_text(t.rows[1].cells[1], r0[1]); set_cell_text(t.rows[1].cells[2], r0[2]); set_cell_text(t.rows[1].cells[3], r0[3])
# xoa dong mau thu 2 ('03 | ... ') truoc khi them cac dong that
row_to_del = t.rows[2]._tr
row_to_del.getparent().remove(row_to_del)
for r in uc_list[1:]:
    add_table_row_like_last(t, list(r))

# ------------------------------------------------------ Screen Descriptions ---
screens = [
("1","Auth","Login, Register, Forgot/Reset Password, Verify Email","Đăng nhập/đăng ký/khôi phục mật khẩu Student; Staff có bộ riêng (/staff/forgot-password, /staff/setup-password, /staff/change-temp-password)"),
("2","Student — Dashboard","Dashboard, Onboarding, Profile, Settings","Trang chủ sau đăng nhập, thiết lập ban đầu, hồ sơ, đổi mật khẩu/email"),
("3","Student — Học nội dung","Lesson Detail, Kanji List, Kanji Practice, Grammar, Kana, Vocabulary, Dictionary","Xem bài học, luyện viết Kanji (canvas), học ngữ pháp/kana/từ vựng, tra từ điển"),
("4","Student — Ôn tập & Thi","Notebook, Flashcard Session, Quiz, Mock Test List/Attempt/Results","Sổ tay flashcard (SRS), quiz theo chủ đề, danh sách/làm/xem kết quả mock test"),
("5","Student — Kỹ năng khác","Reading, Speaking, Progress","Luyện đọc, luyện nói (Shadowing + AI chấm), xem tiến độ học tập"),
("6","Student — Hỗ trợ","Support Tickets, Ticket Detail, Notifications","Gửi/theo dõi ticket hỗ trợ, xem thông báo"),
("7","Staff — Dashboard","Staff Dashboard","Tổng quan công việc Staff"),
("8","Staff — Soạn nội dung","Staff Content, Staff Questions, Staff Assessments","CRUD bài học/ngữ pháp/từ vựng, ngân hàng câu hỏi, quiz/đề thi (trạng thái nháp → gửi duyệt)"),
("9","Staff — Vận hành","Staff Grading, Staff Tickets, Staff Students","Chấm bài nói (UC-31), trả lời ticket, theo dõi/quản lý học viên"),
("10","Manager — Dashboard","Manager Dashboard","Tổng quan hàng đợi duyệt"),
("11","Manager — Duyệt nội dung","Review Queue, Content Pipeline, Deleted Topics","Duyệt/từ chối/yêu cầu sửa nội dung Staff gửi, xem pipeline, khôi phục nội dung đã xoá mềm"),
("12","Manager — Khác","Manager Notifications, Manager Tickets","Thông báo, ticket (chia sẻ 1 phần route với Staff)"),
("13","Admin — Dashboard","Admin Dashboard","Thống kê hệ thống"),
("14","Admin — Quản trị","Admin Users, Admin Settings, Admin Reports","Quản lý người dùng, cấu hình hệ thống, báo cáo"),
]
t = find_table_after(doc, "2.2 Screen Descriptions")
r0 = screens[0]
set_cell_text(t.rows[1].cells[0], r0[0]); set_cell_text(t.rows[1].cells[1], r0[1]); set_cell_text(t.rows[1].cells[2], r0[2]); set_cell_text(t.rows[1].cells[3], r0[3])
row_to_del = t.rows[2]._tr
row_to_del.getparent().remove(row_to_del)
for r in screens[1:]:
    add_table_row_like_last(t, list(r))

# ------------------------------------------------------- Screen Authorization ---
t = find_table_after(doc, "2.3 Screen Authorization")
header = t.rows[0]
set_cell_text(header.cells[0], "Khu vực màn hình")
set_cell_text(header.cells[1], "Student")
set_cell_text(header.cells[2], "Staff")
set_cell_text(header.cells[3], "StaffManager")
set_cell_text(header.cells[4], "Admin")
auth_rows = [
("Public (/, /login, /register...)", "X", "X", "X", "X"),
("Student area (PrivateRoute, hasRole('STUDENT'))", "X", "", "", ""),
("Staff area (StaffRoute, hasRole('STAFF'))", "", "X", "X", ""),
("Manager area (ManagerRoute: role STAFF + staffRole=staff_manager)", "", "", "X", "X"),
("Admin area (AdminRoute, hasRole('ADMIN'))", "", "", "", "X"),
]
# xoa het cac dong mau con lai (index 1..12), chi giu header
for row in list(t.rows[1:]):
    row._tr.getparent().remove(row._tr)
for r in auth_rows:
    add_table_row_like_last(t, list(r)) if len(t.rows) > 1 else None
# truong hop bang chi con header (khong co last-row template) -> tu tao dong bang cach copy header roi xoa dinh dang X
if len(t.rows) == 1:
    for r in auth_rows:
        new_tr = __import__("copy").deepcopy(t.rows[0]._tr)
        t.rows[0]._tr.addnext(new_tr)
    # dien lai toan bo (bo qua header) theo thu tu
    for i, r in enumerate(auth_rows, start=1):
        for ci, val in enumerate(r):
            set_cell_text(t.rows[i].cells[ci], val)

# ------------------------------------------------------------ Non-UI Functions ---
nonui = [
("1","Speaking (UC-13)","SpeakingAsyncProcessor.process()","@Async — xử lý chấm điểm phát âm sau khi Student nộp bản ghi âm, không block request nộp bài"),
("2","Auth (UC-02, UC-03)","AuthEventListener.onSendVerificationEmailEvent()","Gửi email xác minh tài khoản (bất đồng bộ qua Spring Event) sau khi đăng ký"),
("3","Auth (UC-03)","AuthEventListener.onSendPasswordResetEmailEvent()","Gửi email reset mật khẩu (bất đồng bộ)"),
("4","Notification (UC-30, UC-40)","NotificationDispatcher (@Async fan-out)","Gửi thông báo tới nhiều Student cùng lúc mà không block request tạo notification"),
("5","Notification / Email Outbox","NotificationDispatcher.deliverPendingEmails()","@Scheduled(fixedDelay=60000) — job chạy mỗi 60s, retry gửi lại email thất bại trong hàng đợi email_outbox"),
]
t = find_table_after(doc, "2.4 Non-UI Functions")
r0 = nonui[0]
set_cell_text(t.rows[1].cells[0], r0[0]); set_cell_text(t.rows[1].cells[1], r0[1]); set_cell_text(t.rows[1].cells[2], r0[2]); set_cell_text(t.rows[1].cells[3], r0[3])
row_to_del = t.rows[2]._tr
row_to_del.getparent().remove(row_to_del)
for r in nonui[1:]:
    add_table_row_like_last(t, list(r))

# ------------------------------------------------------- DB Table Descriptions ---
tables = [
("01","admin_users","Tài khoản Admin. — Primary keys: id"),
("02","staff_users","Tài khoản Staff và StaffManager (staff_role). — Primary keys: id"),
("03","student_users","Tài khoản học viên + OAuth (Google), current/target_jlpt_level, streak. — Primary keys: id"),
("04","auth_tokens","Token dùng chung (session/refresh/reset/verify) cho Admin/Staff/Student qua actor_type. — Primary keys: id — Foreign keys: actor_id (polymorphic theo actor_type)"),
("05","courses","Khóa học JLPT (container tổ chức nhiều lesson). — Primary keys: id"),
("06","lessons","Bài học thuộc khóa (lesson/reading/listening/speaking). — Primary keys: id — Foreign keys: course_id"),
("07","kana_characters","Hiragana/Katakana. — Primary keys: id"),
("08","kanji","Kanji theo cấp độ JLPT. — Primary keys: id"),
("09","vocabulary","Từ vựng, gắn với lesson/topic. — Primary keys: id — Foreign keys: lesson_id"),
("10","grammar_points","Điểm ngữ pháp, gắn với lesson. — Primary keys: id — Foreign keys: lesson_id"),
("11","questions","Ngân hàng câu hỏi (đáp án A/B/C/D inline). — Primary keys: id"),
("12","assessments","Quiz và Exam JLPT (gộp chung qua assessment_type). — Primary keys: id"),
("13","question_assignments","Gán câu hỏi vào assessment hoặc lesson. — Primary keys: id — Foreign keys: question_id"),
("14","test_attempts","1 lần làm quiz/exam/practice/reading/listening của Student. — Primary keys: id — Foreign keys: student_id"),
("15","attempt_answers","Câu trả lời của Student cho từng câu hỏi trong 1 attempt. — Primary keys: id — Foreign keys: attempt_id, question_id"),
("16","student_submissions","Bài nói/viết tay cần AI chấm trước rồi Staff duyệt lại (SPEAKING/HANDWRITING). — Primary keys: id — Foreign keys: student_id"),
("17","student_content_progress","Tiến độ học và bookmark. — Primary keys: id — Foreign keys: student_id"),
("18","flashcards","Thẻ flashcard + trạng thái SRS + deck. — Primary keys: id — Foreign keys: student_id, deck_id"),
("19","kanji_writing_attempts","Lượt Student viết tay 1 chữ Kanji trên canvas, điểm DTW trung bình (bổ sung ở migration V8). — Primary keys: attempt_id — Foreign keys: student_id, kanji_id"),
("20","tickets","Ticket hỗ trợ Student gửi lên. — Primary keys: id — Foreign keys: student_id"),
("21","ticket_replies","Phản hồi ticket. — Primary keys: id — Foreign keys: ticket_id"),
("22","notifications","Thông báo tới từng Student. — Primary keys: id — Foreign keys: student_id"),
("23","system_settings","Cấu hình hệ thống (key-value). — Primary keys: id"),
("24","admin_audit_logs","Audit log thao tác Admin/Staff/StaffManager. — Primary keys: id"),
]
t = find_table_after(doc, "b. Table Descriptions")
r0 = tables[0]
set_cell_text(t.rows[1].cells[0], r0[0]); set_cell_text(t.rows[1].cells[1], r0[1]); set_cell_text(t.rows[1].cells[2], r0[2])
row_to_del = t.rows[2]._tr
row_to_del.getparent().remove(row_to_del)
for r in tables[1:]:
    add_table_row_like_last(t, list(r))

# ------------------------------------------------------------- Code Packages ---
packages = [
("01","auth","Đăng nhập/đăng ký Student, JWT access+refresh, xác minh email, quên mật khẩu, Google OAuth."),
("02","staff","Tài khoản Staff/StaffManager: đăng nhập riêng, quản lý thành viên, reset mật khẩu staff."),
("03","admin","Tài khoản Admin, audit log, dashboard thống kê, cấu hình hệ thống, chế độ bảo trì."),
("04","student","Hồ sơ Student, dashboard, tiến độ học, avatar, khóa học đã đăng ký."),
("05","learning","Nội dung học: Kana, Kanji, Vocabulary, Grammar, Lesson theo cấp JLPT (N5–N1)."),
("06","assessment","Ngân hàng câu hỏi, Quiz/Exam, lượt làm bài, chấm điểm server-side, StudentSubmission."),
("07","staffcontent","Nơi Staff soạn nội dung nháp trước khi publish."),
("08","contentreview","Luồng duyệt nội dung do Staff soạn (approve/reject) trước khi công khai."),
("09","publishedcontent","Snapshot nội dung đã publish, phục vụ Student đọc."),
("10","flashcard","Flashcard + Spaced Repetition (SRS), deck hệ thống hoặc do user tạo."),
("11","dictionary","Tra cứu từ điển Kanji/Vocabulary theo từ khóa hoặc loại."),
("12","notification","Thông báo tới từng Student."),
("13","support","Ticket hỗ trợ Student gửi lên và phản hồi."),
("14","speaking","Luyện nói Shadowing + AI chấm điểm bất đồng bộ (engine hiện là StubSpeechRecognitionEngine, mô phỏng tất định, chưa phải ASR thật)."),
]
t = find_table_after(doc, "3.2 Code Packages")
r0 = packages[0]
set_cell_text(t.rows[1].cells[0], r0[0]); set_cell_text(t.rows[1].cells[1], r0[1]); set_cell_text(t.rows[1].cells[2], r0[2])
row_to_del = t.rows[2]._tr
row_to_del.getparent().remove(row_to_del)
for r in packages[1:]:
    add_table_row_like_last(t, list(r))

doc.save("docs/07-Release-Documents/RDS_Document.docx")
print("Section I done. Tables:", len(doc.tables))
