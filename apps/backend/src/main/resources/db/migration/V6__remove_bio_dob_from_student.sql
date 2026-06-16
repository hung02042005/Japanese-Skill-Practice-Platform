/* ============================================================
   V6 — Xóa cột bio và date_of_birth khỏi student_users
   ------------------------------------------------------------
   Lý do:
   - bio, date_of_birth : không ảnh hưởng đến logic nghiệp vụ cốt lõi
     (học tập, thi cử, flashcard); đơn giản hóa profile chỉ giữ
     các thông tin định danh cần thiết (full_name, email, phone,
     avatar_url, jlpt_level).
   Lưu ý: IF EXISTS bảo vệ migration chạy trên schema V1 mới
           vốn không có những cột này.
   ============================================================ */

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('student_users') AND name = 'bio')
    ALTER TABLE student_users DROP COLUMN bio;
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('student_users') AND name = 'date_of_birth')
    ALTER TABLE student_users DROP COLUMN date_of_birth;
GO
