-- V31: Seed nội dung email (body_html) cho 3 nhóm email đã có (Phase 1).
--
-- Trước đây EmailService hard-code toàn bộ HTML body trong Java. Từ nay body_html
-- được lưu trong system_settings để Admin chỉnh sửa qua /admin/settings?tab=email.
-- Giá trị seed = đúng bản HTML đang hard-code, chỉ thay %s -> {{placeholder}} và
-- các "10 phút"/"1 giờ"/"2025" thành biến để cấu hình được.
--
-- INSERT IGNORE: idempotent theo UQ_setting (setting_group, setting_key) — xem V3.
-- Nếu body_html trống/thiếu placeholder bắt buộc, EmailService fallback template
-- mặc định trong code (LESSON-006 — không silent fail).

-- ── email_register: xác minh email khi đăng ký. Placeholder: {{otp_code}}, {{expiry_minutes}}
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
('email_register', 'body_html',
'<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"></head>
<body style="font-family: ''Segoe UI'', Arial, sans-serif; background:#f4f6fb; margin:0; padding:0;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f4f6fb; padding:40px 0;">
    <tr><td align="center">
      <table width="560" cellpadding="0" cellspacing="0"
             style="background:#ffffff; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.08); overflow:hidden;">
        <tr>
          <td style="background:linear-gradient(135deg,#4f46e5,#7c3aed); padding:36px 40px; text-align:center;">
            <h1 style="color:#ffffff; margin:0; font-size:26px; font-weight:700;">🎌 {{platform_name}}</h1>
            <p style="color:#c7d2fe; margin:8px 0 0; font-size:14px;">Hệ thống luyện thi tiếng Nhật</p>
          </td>
        </tr>
        <tr>
          <td style="padding:40px;">
            <h2 style="color:#1e1b4b; margin:0 0 16px; font-size:20px;">Xác minh địa chỉ email</h2>
            <p style="color:#4b5563; font-size:15px; line-height:1.7; margin:0 0 24px;">
              Cảm ơn bạn đã đăng ký tài khoản {{platform_name}}! Nhập mã bên dưới trên trang xác minh
              để kích hoạt tài khoản.
            </p>
            <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin:32px 0;">
              <tr>
                <td align="center">
                  <div style="display:inline-block; background:#f4f6fb; border:1px solid #e5e7eb; border-radius:8px; padding:18px 36px; font-size:32px; font-weight:800; letter-spacing:10px; color:#4f46e5;">
                    {{otp_code}}
                  </div>
                </td>
              </tr>
            </table>
            <p style="color:#9ca3af; font-size:13px; line-height:1.6;">
              Mã có hiệu lực trong <strong>{{expiry_minutes}} phút</strong>. Nếu bạn không tạo tài khoản này, hãy bỏ qua email này.
            </p>
            <hr style="border:none; border-top:1px solid #e5e7eb; margin:24px 0;">
            <p style="color:#9ca3af; font-size:12px; text-align:center; margin:0;">
              © {{current_year}} JLPT E-Learning Platform. Mọi quyền được bảo lưu.
            </p>
          </td>
        </tr>
      </table>
    </td></tr>
  </table>
</body>
</html>',
'string');

-- ── email_otp: mã xác thực OTP dùng chung. Placeholder: {{otp_code}}
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
('email_otp', 'body_html',
'<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"></head>
<body style="font-family: ''Segoe UI'', Arial, sans-serif; background:#f4f6fb; margin:0; padding:0;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f4f6fb; padding:40px 0;">
    <tr><td align="center">
      <table width="560" cellpadding="0" cellspacing="0"
             style="background:#ffffff; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.08); overflow:hidden;">
        <tr>
          <td style="background:linear-gradient(135deg,#4f46e5,#7c3aed); padding:36px 40px; text-align:center;">
            <h1 style="color:#ffffff; margin:0; font-size:26px; font-weight:700;">🎌 {{platform_name}}</h1>
            <p style="color:#c7d2fe; margin:8px 0 0; font-size:14px;">Mã xác thực</p>
          </td>
        </tr>
        <tr>
          <td style="padding:40px;">
            <h2 style="color:#1e1b4b; margin:0 0 16px; font-size:20px;">Mã xác thực của bạn</h2>
            <p style="color:#4b5563; font-size:15px; line-height:1.7; margin:0 0 24px;">
              Sử dụng mã bên dưới để hoàn tất xác thực. Mã có hiệu lực trong thời gian ngắn,
              vui lòng không chia sẻ mã này cho bất kỳ ai.
            </p>
            <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin:32px 0;">
              <tr>
                <td align="center">
                  <div style="display:inline-block; background:#f4f6fb; border:1px solid #e5e7eb; border-radius:8px; padding:18px 36px; font-size:32px; font-weight:800; letter-spacing:10px; color:#4f46e5;">
                    {{otp_code}}
                  </div>
                </td>
              </tr>
            </table>
            <p style="color:#9ca3af; font-size:13px; line-height:1.6;">
              Nếu bạn không yêu cầu mã này, hãy bỏ qua email này.
            </p>
            <hr style="border:none; border-top:1px solid #e5e7eb; margin:24px 0;">
            <p style="color:#9ca3af; font-size:12px; text-align:center; margin:0;">
              © {{current_year}} JLPT E-Learning Platform. Mọi quyền được bảo lưu.
            </p>
          </td>
        </tr>
      </table>
    </td></tr>
  </table>
</body>
</html>',
'string');

-- ── email_reset: đặt lại mật khẩu (link). Placeholder: {{reset_link}}, {{expiry_hours}}
INSERT IGNORE INTO system_settings (setting_group, setting_key, setting_value, value_type) VALUES
('email_reset', 'body_html',
'<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"></head>
<body style="font-family: ''Segoe UI'', Arial, sans-serif; background:#f4f6fb; margin:0; padding:0;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f4f6fb; padding:40px 0;">
    <tr><td align="center">
      <table width="560" cellpadding="0" cellspacing="0"
             style="background:#ffffff; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.08); overflow:hidden;">
        <tr>
          <td style="background:linear-gradient(135deg,#dc2626,#9f1239); padding:36px 40px; text-align:center;">
            <h1 style="color:#ffffff; margin:0; font-size:26px; font-weight:700;">🎌 {{platform_name}}</h1>
            <p style="color:#fecaca; margin:8px 0 0; font-size:14px;">Đặt lại mật khẩu</p>
          </td>
        </tr>
        <tr>
          <td style="padding:40px;">
            <h2 style="color:#1e1b4b; margin:0 0 16px; font-size:20px;">Yêu cầu đặt lại mật khẩu</h2>
            <p style="color:#4b5563; font-size:15px; line-height:1.7; margin:0 0 24px;">
              Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
              Nhấn nút bên dưới để tạo mật khẩu mới.
            </p>
            <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin:32px 0;">
              <tr>
                <td align="center">
                  <table border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="center" style="border-radius: 8px;" bgcolor="#dc2626">
                        <a href="{{reset_link}}" target="_blank" style="font-size: 16px; font-family: ''Segoe UI'', Arial, sans-serif; color: #ffffff; text-decoration: none; border-radius: 8px; padding: 14px 36px; border: 1px solid #dc2626; display: inline-block; font-weight: 600;">
                          🔑 Đặt lại mật khẩu
                        </a>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            <p style="color:#9ca3af; font-size:13px; line-height:1.6;">
              Link có hiệu lực trong <strong>{{expiry_hours}} giờ</strong>. Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
            </p>
            <hr style="border:none; border-top:1px solid #e5e7eb; margin:24px 0;">
            <p style="color:#9ca3af; font-size:12px; text-align:center; margin:0;">
              © {{current_year}} JLPT E-Learning Platform. Mọi quyền được bảo lưu.
            </p>
          </td>
        </tr>
      </table>
    </td></tr>
  </table>
</body>
</html>',
'string');
