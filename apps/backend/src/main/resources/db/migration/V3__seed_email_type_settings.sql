-- V3: Thêm key smtp còn thiếu + seed 3 nhóm email riêng biệt
--
-- Bản SQL Server dùng "IF NOT EXISTS (...) INSERT" cho từng key. MySQL không
-- chạy được IF ngoài stored procedure, nhưng system_settings đã có sẵn
-- UQ_setting UNIQUE (setting_group, setting_key) — nên INSERT IGNORE cho đúng
-- ngữ nghĩa đó, ngắn hơn và idempotent theo từng dòng.

-- smtp: bổ sung secure và from_name (host/port/username/from_email đã có ở V1)
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
    ('smtp', 'secure',    'STARTTLS',      'string'),
    ('smtp', 'from_name', 'JLPT Platform', 'string');

-- email_register: email gửi xác nhận đăng ký học viên
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
    ('email_register', 'from_email', 'noreply@jlpt.com',            'string'),
    ('email_register', 'from_name',  'JLPT Platform',               'string'),
    ('email_register', 'subject',    'Xác nhận đăng ký tài khoản',  'string');

-- email_otp: email gửi mã xác thực OTP
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
    ('email_otp', 'from_email', 'noreply@jlpt.com',     'string'),
    ('email_otp', 'from_name',  'JLPT Platform',        'string'),
    ('email_otp', 'subject',    'Mã xác thực của bạn',  'string');

-- email_reset: email cấp lại mật khẩu cho Staff & Manager
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
    ('email_reset', 'from_email', 'noreply@jlpt.com',                    'string'),
    ('email_reset', 'from_name',  'JLPT Platform',                       'string'),
    ('email_reset', 'subject',    'Cấp lại mật khẩu tài khoản Staff',    'string');
