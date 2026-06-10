/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.admin-notify-email:${spring.mail.username}}")
    private String adminNotifyEmail;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyLink = frontendUrl + "/verify-email?token=" + token;
        String subject = "[JLPT Platform] Xác minh địa chỉ email của bạn";
        String body = buildVerificationEmailBody(verifyLink);
        sendHtmlEmail(toEmail, subject, body);
        log.info("[EmailService] Verification email sent to: {}", toEmail);
    }

    @Async
    public void sendStaffInvitationEmail(String toEmail, String token) {
        String setupLink = frontendUrl + "/staff/setup-password?token=" + token;
        String subject = "[JLPT Platform] Lời mời tham gia hệ thống — Thiết lập mật khẩu";
        String body = buildStaffInvitationEmailBody(setupLink);
        sendHtmlEmail(toEmail, subject, body);
        log.info("[EmailService] Staff invitation email sent to: {}", toEmail);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "[JLPT Platform] Đặt lại mật khẩu";
        String body = buildPasswordResetEmailBody(resetLink);
        sendHtmlEmail(toEmail, subject, body);
        log.info("[EmailService] Password reset email sent to: {}", toEmail);
    }

    @Async
    public void notifyAdminPasswordReset(String staffName, String staffEmail) {
        String subject = "[JLPT Platform] Staff password reset requested";
        String body = """
                <p>Staff <strong>%s</strong> (%s) requested a password reset.</p>
                <p>Please open the Admin Panel to verify the request and issue a temporary password.</p>
                """
                .formatted(staffName, staffEmail);
        sendHtmlEmail(adminNotifyEmail, subject, body);
        log.info("[EmailService] Staff password reset notification sent for staff email={}", staffEmail);
    }

    @Async
    public void notifyAdminsStaffPasswordResetRequested(String staffName, String staffEmail) {
        notifyAdminPasswordReset(staffName, staffEmail);
    }

    @Async
    public void sendStaffTempPassword(String toEmail, String tempPassword) {
        String subject = "[JLPT Platform] Temporary password for Staff account";
        String body = """
                <p>Admin issued a temporary password for your Staff account.</p>
                <p><strong>Temporary password:</strong> %s</p>
                <p>Use it to sign in. The system will require you to set a new password immediately.</p>
                """
                .formatted(tempPassword);
        sendHtmlEmail(toEmail, subject, body);
        log.info("[EmailService] Temporary staff password email sent to: {}", toEmail);
    }

    @Async
    public void sendStaffTempPasswordEmail(String toEmail, String tempPassword) {
        sendStaffTempPassword(toEmail, tempPassword);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("[EmailService] Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Gửi email thất bại tới " + to, e);
        }
    }

    private String buildStaffInvitationEmailBody(String setupLink) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: 'Segoe UI', Arial, sans-serif; background:#f4f6fb; margin:0; padding:0;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6fb; padding:40px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="background:#ffffff; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.08); overflow:hidden;">
                        <tr>
                          <td style="background:linear-gradient(135deg,#0f766e,#0891b2); padding:36px 40px; text-align:center;">
                            <h1 style="color:#ffffff; margin:0; font-size:26px; font-weight:700;">🎌 JLPT Platform</h1>
                            <p style="color:#a5f3fc; margin:8px 0 0; font-size:14px;">Lời mời tham gia hệ thống</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;">
                            <h2 style="color:#1e1b4b; margin:0 0 16px; font-size:20px;">Chào mừng bạn đến với JLPT Platform!</h2>
                            <p style="color:#4b5563; font-size:15px; line-height:1.7; margin:0 0 24px;">
                              Bạn đã được Admin mời tham gia hệ thống với vai trò <strong>Staff</strong>.
                              Vui lòng nhấn nút bên dưới để thiết lập mật khẩu và kích hoạt tài khoản.
                            </p>
                            <div style="text-align:center; margin:32px 0;">
                              <a href="%s"
                                 style="background:linear-gradient(135deg,#0f766e,#0891b2); color:#ffffff;
                                        padding:14px 36px; border-radius:8px; text-decoration:none;
                                        font-size:16px; font-weight:600; display:inline-block;">
                                🔑 Thiết lập mật khẩu
                              </a>
                            </div>
                            <p style="color:#9ca3af; font-size:13px; line-height:1.6;">
                              Link có hiệu lực trong <strong>24 giờ</strong>. Nếu bạn không mong đợi lời mời này, hãy bỏ qua email này.
                            </p>
                            <hr style="border:none; border-top:1px solid #e5e7eb; margin:24px 0;">
                            <p style="color:#9ca3af; font-size:12px; text-align:center; margin:0;">
                              © 2025 JLPT E-Learning Platform. Mọi quyền được bảo lưu.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """
                .formatted(setupLink);
    }

    private String buildVerificationEmailBody(String verifyLink) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: 'Segoe UI', Arial, sans-serif; background:#f4f6fb; margin:0; padding:0;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6fb; padding:40px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="background:#ffffff; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.08); overflow:hidden;">
                        <tr>
                          <td style="background:linear-gradient(135deg,#4f46e5,#7c3aed); padding:36px 40px; text-align:center;">
                            <h1 style="color:#ffffff; margin:0; font-size:26px; font-weight:700;">🎌 JLPT Platform</h1>
                            <p style="color:#c7d2fe; margin:8px 0 0; font-size:14px;">Hệ thống luyện thi tiếng Nhật</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;">
                            <h2 style="color:#1e1b4b; margin:0 0 16px; font-size:20px;">Xác minh địa chỉ email</h2>
                            <p style="color:#4b5563; font-size:15px; line-height:1.7; margin:0 0 24px;">
                              Cảm ơn bạn đã đăng ký tài khoản JLPT Platform! Vui lòng nhấn nút bên dưới
                              để xác minh địa chỉ email và kích hoạt tài khoản.
                            </p>
                            <div style="text-align:center; margin:32px 0;">
                              <a href="%s"
                                 style="background:linear-gradient(135deg,#4f46e5,#7c3aed); color:#ffffff;
                                        padding:14px 36px; border-radius:8px; text-decoration:none;
                                        font-size:16px; font-weight:600; display:inline-block;">
                                ✅ Xác minh Email
                              </a>
                            </div>
                            <p style="color:#9ca3af; font-size:13px; line-height:1.6;">
                              Link có hiệu lực trong <strong>24 giờ</strong>. Nếu bạn không tạo tài khoản này, hãy bỏ qua email này.
                            </p>
                            <hr style="border:none; border-top:1px solid #e5e7eb; margin:24px 0;">
                            <p style="color:#9ca3af; font-size:12px; text-align:center; margin:0;">
                              © 2025 JLPT E-Learning Platform. Mọi quyền được bảo lưu.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """
                .formatted(verifyLink);
    }

    private String buildPasswordResetEmailBody(String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: 'Segoe UI', Arial, sans-serif; background:#f4f6fb; margin:0; padding:0;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6fb; padding:40px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="background:#ffffff; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.08); overflow:hidden;">
                        <tr>
                          <td style="background:linear-gradient(135deg,#dc2626,#9f1239); padding:36px 40px; text-align:center;">
                            <h1 style="color:#ffffff; margin:0; font-size:26px; font-weight:700;">🎌 JLPT Platform</h1>
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
                            <div style="text-align:center; margin:32px 0;">
                              <a href="%s"
                                 style="background:linear-gradient(135deg,#dc2626,#9f1239); color:#ffffff;
                                        padding:14px 36px; border-radius:8px; text-decoration:none;
                                        font-size:16px; font-weight:600; display:inline-block;">
                                🔑 Đặt lại mật khẩu
                              </a>
                            </div>
                            <p style="color:#9ca3af; font-size:13px; line-height:1.6;">
                              Link có hiệu lực trong <strong>1 giờ</strong>. Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
                            </p>
                            <hr style="border:none; border-top:1px solid #e5e7eb; margin:24px 0;">
                            <p style="color:#9ca3af; font-size:12px; text-align:center; margin:0;">
                              © 2025 JLPT E-Learning Platform. Mọi quyền được bảo lưu.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """
                .formatted(resetLink);
    }
}
