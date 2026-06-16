/* ============================================================
   V5 — Xóa các cột không cần thiết khỏi bảng người dùng
   ------------------------------------------------------------
   Lý do:
   - last_login_ip   : IP đã được ghi tại auth_tokens.ip_address (khi tạo token),
                       lưu thêm ở bảng user là dư thừa.
   - password_changed_at : không có use case nào cần kiểm tra hay hiển thị trường này;
                           không có tính năng "password expires" trong roadmap hiện tại.
   - email_verified_at (admin_users, staff_users) : Admin/Staff được tạo bởi Admin,
                           không qua luồng self-registration + email verification.
                           Trường này vẫn giữ ở student_users (dùng cho UC-02).
   Lưu ý: Các kiểm tra IF EXISTS bảo vệ migration chạy trên schema đã được
           dọn dẹp sẵn (V1 mới không có những cột này).
   ============================================================ */

-- ── admin_users ─────────────────────────────────────────────
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('admin_users') AND name = 'last_login_ip')
    ALTER TABLE admin_users DROP COLUMN last_login_ip;
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('admin_users') AND name = 'password_changed_at')
    ALTER TABLE admin_users DROP COLUMN password_changed_at;
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('admin_users') AND name = 'email_verified_at')
    ALTER TABLE admin_users DROP COLUMN email_verified_at;
GO

-- ── staff_users ─────────────────────────────────────────────
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('staff_users') AND name = 'last_login_ip')
    ALTER TABLE staff_users DROP COLUMN last_login_ip;
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('staff_users') AND name = 'password_changed_at')
    ALTER TABLE staff_users DROP COLUMN password_changed_at;
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('staff_users') AND name = 'email_verified_at')
    ALTER TABLE staff_users DROP COLUMN email_verified_at;
GO

-- ── student_users ────────────────────────────────────────────
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('student_users') AND name = 'last_login_ip')
    ALTER TABLE student_users DROP COLUMN last_login_ip;
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('student_users') AND name = 'password_changed_at')
    ALTER TABLE student_users DROP COLUMN password_changed_at;
GO
