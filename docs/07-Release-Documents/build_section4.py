# -*- coding: utf-8 -*-
import sys, copy
sys.path.insert(0, r".skill/docx-report/scripts")
from docx_tools import *
from docx.oxml.ns import qn

doc = Document("docs/07-Release-Documents/RDS_Document.docx")

# ---- IV.1 Assumptions & Dependencies (bullet list free text) ----
idx = find_paragraph_index(doc, "1. Assumptions & Dependencies")
children = list(doc.element.body.iterchildren())
# tim doan [Record any assumptions...] va <<Sample: ... >> ngay sau heading, thay bang noi dung that
p_instr = Paragraph(children[idx+1], doc)
set_paragraph_text(p_instr, "")
p_sample = Paragraph(children[idx+2], doc)
assumptions_text = (
    "AS-1: Người dùng truy cập qua trình duyệt hiện đại hỗ trợ Web Audio API (ghi âm luyện nói) và Canvas API (luyện viết Kanji).\n"
    "AS-2: Email thật (SMTP) khả dụng để gửi xác minh/reset mật khẩu; hệ thống có cơ chế email_outbox + retry (NotificationDispatcher), không giả định gửi thành công ngay lần đầu.\n"
    "DE-1 (Google OAuth): Đăng nhập Google phụ thuộc GOOGLE_CLIENT_ID cấu hình đúng (AuthenticationService.loginWithGoogle) — dùng luồng ID-token phía client (@react-oauth/google), không phải luồng redirect/callback phía server.\n"
    "DE-2 (Kiến trúc Monolith): Toàn bộ feature chạy trong 1 Spring Boot app; tách AI module ra service riêng là hướng tương lai, chưa thực hiện.\n"
    "DE-3 (File Storage): File ảnh Kanji viết tay/audio luyện nói lưu tại /uploads hoặc S3, không lưu BLOB trong DB."
)
set_paragraph_text(p_sample, assumptions_text)

# ---- IV.2 Limitations & Exclusions (free text) ----
idx2 = find_paragraph_index(doc, "2. Limitations & Exclusions")
children = list(doc.element.body.iterchildren())
p_instr2 = Paragraph(children[idx2+1], doc)
set_paragraph_text(p_instr2, (
    "- Engine chấm điểm phát âm (Speaking, UC-13) hiện là StubSpeechRecognitionEngine — sinh điểm MÔ PHỎNG tất định từ độ dài audio, "
    "KHÔNG PHẢI kết quả nhận diện giọng nói AI/ASR thật. Khi có engine thật, cần thêm 1 implementation khác đánh dấu @Primary.\n"
    "- OCR Kanji (UC-20) chỉ so sánh % giống nhau bằng thuật toán Dynamic Time Warping (DTW) trên toạ độ nét vẽ, KHÔNG phân tích thứ tự nét, "
    "hướng nét, hay thẩm mỹ thư pháp — đúng chủ đích thiết kế, không phải thiếu sót.\n"
    "- 15/40 Use Case trong danh mục hợp nhất (Bao_cao_dac_ta_Use_Case.md) chưa có tài liệu đặc tả chi tiết riêng (UC-NN-*.md); "
    "Mục II của tài liệu này chỉ trình bày đầy đủ 26 UC đã có tài liệu nguồn.\n"
    "- 22/46 màn hình frontend đã có SPEC-*.md nhưng chưa được đưa vào Mục III của tài liệu này (đã đưa 24/46 màn hình có ưu tiên cao nhất theo role)."
))

# ---- IV.3 Business Rules (ID/Category/Rule Definition) ----
biz = [
("BIZ-AUTH-01","Authentication & Authorization","Mọi API (trừ public: login/register/forgot-password) phải đi qua JWT filter."),
("BIZ-AUTH-02","Authentication & Authorization","Password hash bằng bcrypt, cost >= 10 (chuẩn dự án: 12)."),
("BIZ-AUTH-03","Authentication & Authorization","Phân quyền phải check cả Role VÀ Subscription/Level, không chỉ Role."),
("BIZ-AUTH-04","Authentication & Authorization","UI ẩn nút/menu chỉ là UX — backend luôn trả 401/403 khi không đủ quyền."),
("BIZ-AUTH-05","Authentication & Authorization","Không gộp chung UI/logic giữa Staff và Admin."),
("BIZ-AUTH-06","Authentication & Authorization","Subscription hết hạn phải được kiểm tra real-time, cache tối đa 5 phút."),
("BIZ-SUB-01","Subscription & Monetization","Nội dung is_vip_only=true chỉ khả dụng khi user.subscription=VIP còn hiệu lực."),
("BIZ-SUB-02","Subscription & Monetization","Mọi thay đổi subscription phải ghi audit log."),
("BIZ-SUB-03","Subscription & Monetization","Không tự động cấp quyền VIP khi chưa xác nhận thanh toán thành công."),
("BIZ-SUB-04","Subscription & Monetization","Hạ cấp/hết hạn subscription không xóa lịch sử học tập đã có."),
("BIZ-EXAM-01","Điểm số & Bài thi","score luôn >= 0 và <= max_score."),
("BIZ-EXAM-02","Điểm số & Bài thi","Điểm số chỉ được tính ở Service layer (backend) — client không bao giờ gửi score lên."),
("BIZ-EXAM-03","Điểm số & Bài thi","Mỗi lần nộp bài tạo bản ghi attempt MỚI, không update đè lên attempt cũ."),
("BIZ-EXAM-04","Điểm số & Bài thi","Bài đã nộp (SUBMITTED) là bất biến — không cho sửa điểm/đáp án sau khi nộp."),
("BIZ-EXAM-05","Điểm số & Bài thi","Thời gian làm bài phải validate server-side; client không tự khai báo thời gian còn lại."),
("BIZ-EXAM-06","Điểm số & Bài thi","Quiz/Exam đã có ít nhất 1 attempt thì câu hỏi bị lock; sửa nội dung phải tạo version mới."),
("BIZ-EXAM-07","Điểm số & Bài thi","Không trộn lẫn câu hỏi/đề thi giữa các cấp độ JLPT (N1–N5)."),
("BIZ-PATH-01","Lộ trình học","Bài học tiếp theo chỉ mở khóa khi đã hoàn thành bài trước theo lesson_order."),
("BIZ-PATH-02","Lộ trình học","user_progress chỉ được tăng, không giảm thủ công (trừ thao tác Admin có audit log)."),
("BIZ-PATH-03","Lộ trình học","Mọi hoạt động học tập phải ghi vào learning_activity_log."),
("BIZ-CONTENT-01","Quy trình Nội dung","Nội dung do Staff tạo phải qua bước Review của StaffManager trước khi publish."),
("BIZ-CONTENT-02","Quy trình Nội dung","Nội dung PENDING_REVIEW/DRAFT không hiển thị cho Student."),
("BIZ-CONTENT-03","Quy trình Nội dung","CRUD nội dung học tập chỉ Staff/Admin được thực hiện."),
("BIZ-AI-01","AI Features","OCR chỉ trả về similarity % so với ký tự chuẩn, không phân tích stroke order."),
("BIZ-AI-02","AI Features","Kết quả AI là ai_score_suggestion — Staff có quyền override bằng final_score."),
("BIZ-AI-03","AI Features","Gọi AI không bao giờ silent fail: timeout + retry (tối đa 3 lần) + fallback + log đầy đủ."),
("BIZ-AI-04","AI Features","Mọi tác vụ AI chạy bất đồng bộ — trả job_id ngay, học viên poll kết quả sau."),
("BIZ-AI-05","AI Features","File ảnh/audio đầu vào AI lưu tại /uploads hoặc S3 — không lưu BLOB trong DB."),
("BIZ-DATA-01","Dữ liệu & Audit","Toàn hệ thống dùng Soft Delete — cấm DELETE FROM."),
("BIZ-DATA-02","Dữ liệu & Audit","Mọi bảng nghiệp vụ quan trọng phải có created_at, updated_at, created_by."),
("BIZ-DATA-03","Dữ liệu & Audit","Thao tác quan trọng của Admin/Staff phải có audit log."),
("BIZ-DATA-04","Dữ liệu & Audit","Entity (JPA) không được trả trực tiếp ra API — luôn qua DTO."),
("BIZ-SUPPORT-01","Hỗ trợ & Thông báo","Yêu cầu hỗ trợ từ Student phải được Staff phản hồi, có trạng thái theo dõi (OPEN/IN_PROGRESS/CLOSED)."),
("BIZ-SUPPORT-02","Hỗ trợ & Thông báo","Thông báo hệ thống gửi theo rule do Admin cấu hình, không gửi tùy ý từ code."),
]
t = find_table_after(doc, "3. Business Rules")
r0 = biz[0]
set_cell_text(t.rows[1].cells[0], r0[0]); set_cell_text(t.rows[1].cells[1], r0[1]); set_cell_text(t.rows[1].cells[2], r0[2])
# xoa cac dong template con lai (index 2..len-1) truoc khi them du lieu that
for row in list(t.rows[2:]):
    row._tr.getparent().remove(row._tr)
for r in biz[1:]:
    add_table_row_like_last(t, list(r))

# ---- xoa muc "4. .." rong cuoi tai lieu (khong dung den) ----
try:
    idx4 = find_paragraph_index(doc, "4. ..")
    children = list(doc.element.body.iterchildren())
    children[idx4].getparent().remove(children[idx4])
except Exception as e:
    print("skip 4..:", e)

doc.save("docs/07-Release-Documents/RDS_Document.docx")
print("Section IV done. Business rules rows:", len(find_table_after(doc, "3. Business Rules").rows))
