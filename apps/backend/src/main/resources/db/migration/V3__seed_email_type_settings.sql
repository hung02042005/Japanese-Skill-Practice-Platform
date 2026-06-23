-- V3: Thêm key smtp còn thiếu + seed 3 nhóm email riêng biệt

-- smtp: bổ sung secure và from_name (host/port/username/from_email đã có ở V1)
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'smtp' AND setting_key = 'secure')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('smtp', 'secure', N'STARTTLS', 'string');
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'smtp' AND setting_key = 'from_name')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('smtp', 'from_name', N'JLPT Platform', 'string');

-- email_register: email gửi xác nhận đăng ký học viên
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_register' AND setting_key = 'from_email')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_register', 'from_email', N'noreply@jlpt.com', 'string');
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_register' AND setting_key = 'from_name')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_register', 'from_name', N'JLPT Platform', 'string');
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_register' AND setting_key = 'subject')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_register', 'subject', N'Xác nhận đăng ký tài khoản', 'string');

-- email_otp: email gửi mã xác thực OTP
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_otp' AND setting_key = 'from_email')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_otp', 'from_email', N'noreply@jlpt.com', 'string');
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_otp' AND setting_key = 'from_name')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_otp', 'from_name', N'JLPT Platform', 'string');
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_otp' AND setting_key = 'subject')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_otp', 'subject', N'Mã xác thực của bạn', 'string');

-- email_reset: email cấp lại mật khẩu cho Staff & Manager
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_reset' AND setting_key = 'from_email')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_reset', 'from_email', N'noreply@jlpt.com', 'string');
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_reset' AND setting_key = 'from_name')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_reset', 'from_name', N'JLPT Platform', 'string');
IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_group = 'email_reset' AND setting_key = 'subject')
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES ('email_reset', 'subject', N'Cấp lại mật khẩu tài khoản Staff', 'string');
GO
