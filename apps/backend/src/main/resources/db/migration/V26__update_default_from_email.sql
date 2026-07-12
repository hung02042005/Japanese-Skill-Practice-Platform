-- V26: noreply@jlpt.com là domain giả, không tồn tại — chuyển sang địa chỉ Gmail
-- thật đang dùng để xác thực SMTP (jlptelearningplatform@gmail.com). Gmail bắt buộc
-- header "From" phải trùng tài khoản đã xác thực, nên from_email cũ khiến gửi mail
-- thất bại âm thầm dù test kết nối vẫn pass.

UPDATE system_settings
SET setting_value = N'jlptelearningplatform@gmail.com'
WHERE setting_group IN ('smtp', 'email_register', 'email_otp', 'email_reset')
  AND setting_key = 'from_email'
  AND setting_value = N'noreply@jlpt.com';
GO
