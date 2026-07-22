-- V32: Đổi mô hình nội dung email từ "HTML thô" sang "văn bản thân thiện".
--
-- V31 lưu cả trang HTML vào body_html -> admin nhìn thấy code, khó dùng, dễ vỡ layout.
-- Từ nay khung HTML (header/OTP box/nút/footer) cố định trong EmailService; admin chỉ
-- sửa ĐOẠN VĂN nội dung (body_text, chữ thường + biến {{...}}). EmailService.textToHtml()
-- tự escape và bọc mỗi đoạn vào <p>.
--
-- Xoá body_html cũ (không còn dùng) và seed body_text mặc định = đúng đoạn intro cũ.

-- 1) Gỡ body_html đã seed ở V31 (idempotent — không có thì bỏ qua)
DELETE FROM system_settings
 WHERE setting_key = 'body_html'
   AND setting_group IN ('email_register', 'email_otp', 'email_reset');

-- 2) Seed body_text thân thiện. INSERT IGNORE idempotent theo UQ_setting.
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
  ('email_register', 'body_text',
   'Cảm ơn bạn đã đăng ký tài khoản {{platform_name}}! Nhập mã bên dưới trên trang xác minh để kích hoạt tài khoản.',
   'string'),
  ('email_otp', 'body_text',
   'Sử dụng mã bên dưới để hoàn tất xác thực. Mã có hiệu lực trong thời gian ngắn, vui lòng không chia sẻ mã này cho bất kỳ ai.',
   'string'),
  ('email_reset', 'body_text',
   'Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Nhấn nút bên dưới để tạo mật khẩu mới.',
   'string');
