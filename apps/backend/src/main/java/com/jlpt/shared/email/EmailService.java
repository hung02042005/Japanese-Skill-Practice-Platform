/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.email;

import com.jlpt.feature.admin.SystemSettingRepository;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final int MAX_OUTBOX_RETRY_ATTEMPTS = 10;
    private static final int OUTBOX_RETRY_BATCH_SIZE = 20;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final JavaMailSender mailSender;
    private final SystemSettingRepository settingRepository;
    private final EmailOutboxRepository emailOutboxRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.admin-notify-email:${spring.mail.username}}")
    private String adminNotifyEmail;

    @Async
    public void sendVerificationEmail(String toEmail, String otpCode) {
        String subject = "[JLPT Platform] Mã xác minh địa chỉ email của bạn";
        String body = buildVerificationOtpEmailBody(otpCode);
        sendHtmlEmail(toEmail, subject, body);
        log.info("[EmailService] Verification OTP email sent to: {}", toEmail);
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
    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "[JLPT Platform] Mã xác thực của bạn";
        String body = buildOtpEmailBody(otpCode);
        sendHtmlEmail(toEmail, subject, body);
        log.info("[EmailService] OTP email sent to: {}", toEmail);
    }

    @Async
    public void notifyAdminPasswordReset(String staffName, String staffEmail) {
        String subject = "[JLPT Platform] Staff password reset requested";
        String body =
                """
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
        String body =
                """
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

    /** Gui 1 thong bao he thong qua email (UC-30, kenh email/both). Goi tu scheduler — khong @Async. */
    public void sendNotificationEmail(String toEmail, String title, String content) {
        String subject = "[JLPT Platform] " + title;
        String body = buildNotificationEmailBody(title, content);
        sendHtmlEmail(toEmail, subject, body);
        log.info("[EmailService] Notification email sent to: {}", toEmail);
    }

    private String buildNotificationEmailBody(String title, String content) {
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
                            <p style="color:#c7d2fe; margin:8px 0 0; font-size:14px;">Thông báo hệ thống</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;">
                            <h2 style="color:#1e1b4b; margin:0 0 16px; font-size:20px;">%s</h2>
                            <p style="color:#4b5563; font-size:15px; line-height:1.7; margin:0 0 24px; white-space:pre-line;">%s</p>
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
                .formatted(title, content);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        String finalFromEmail = resolveFromEmail();
        String finalFromName = resolveFromName();

        int maxRetries = 3;
        int retryDelayMs = 2000;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                sendOnce(to, subject, htmlBody, finalFromEmail, finalFromName);
                return; // Success
            } catch (Exception e) {
                log.warn(
                        "[EmailService] Failed to send email to {} (Attempt {}/{}): {}",
                        to,
                        attempt,
                        maxRetries,
                        e.getMessage());
                if (attempt == maxRetries) {
                    // BUG-01 FIX: Không throw RuntimeException trong @Async context —
                    // exception sẽ bị AsyncUncaughtExceptionHandler bắt nhưng caller không nhận được.
                    // Log ERROR rõ ràng, lưu vào outbox để retry định kỳ, và return gracefully thay vì throw.
                    log.error(
                            "[EmailService] All {} attempts failed. Email queued to outbox for {}. Subject: '{}'",
                            maxRetries,
                            to,
                            subject,
                            e);
                    saveToOutbox(to, subject, htmlBody, maxRetries, e.getMessage());
                    return;
                }
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("[EmailService] Email send thread interrupted for recipient: {}", to, ie);
                    return;
                }
            }
        }
    }

    /** Một lần gửi thật sự qua SMTP — không retry, ném exception nếu thất bại. */
    private void sendOnce(String to, String subject, String htmlBody, String fromEmail, String fromName)
            throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        try {
            helper.setFrom(fromEmail, fromName);
        } catch (Exception ex) {
            helper.setFrom(fromEmail);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    // Gmail (và nhiều SMTP relay khác) chỉ chấp nhận gửi khi header "From" trùng với
    // tài khoản đã xác thực (smtp.username) — dùng nó làm fallback thay vì property tĩnh
    // spring.mail.username, vốn không được cấu hình trong hệ thống này (SMTP set qua DB).
    private String resolveFromEmail() {
        return blankToEmpty(settingRepository
                        .findBySettingGroupAndSettingKey("smtp", "from_email")
                        .map(s -> s.getSettingValue())
                        .orElse(null))
                .or(() -> blankToEmpty(settingRepository
                        .findBySettingGroupAndSettingKey("smtp", "username")
                        .map(s -> s.getSettingValue())
                        .orElse(null)))
                .orElse(fromEmail);
    }

    private String resolveFromName() {
        return settingRepository
                .findBySettingGroupAndSettingKey("smtp", "from_name")
                .map(s -> s.getSettingValue())
                .orElse("JLPT Platform");
    }

    private void saveToOutbox(String to, String subject, String htmlBody, int attempts, String error) {
        emailOutboxRepository.save(EmailOutbox.builder()
                .toEmail(to)
                .subject(subject)
                .bodyHtml(htmlBody)
                .status(EmailOutbox.Status.FAILED)
                .attemptCount(attempts)
                .lastError(truncate(error))
                .lastAttemptAt(LocalDateTime.now())
                .build());
    }

    /**
     * Retry định kỳ các email đã hết 3 lần thử ngay lúc gửi (đã lưu FAILED vào outbox) — phục vụ
     * trường hợp SMTP down tạm thời lâu hơn vài giây, hoặc container restart giữa chừng.
     * Bỏ qua entry đã thử đủ {@link #MAX_OUTBOX_RETRY_ATTEMPTS} lần để tránh retry vô hạn — coi
     * như thất bại vĩnh viễn, vẫn còn trong bảng để tra cứu/alert thủ công.
     */
    @Scheduled(fixedDelay = 900_000)
    @Transactional
    public void retryFailedOutbox() {
        List<EmailOutbox> failed = emailOutboxRepository.findByStatusOrderByCreatedAtAsc(
                EmailOutbox.Status.FAILED, PageRequest.of(0, OUTBOX_RETRY_BATCH_SIZE));
        if (failed.isEmpty()) {
            return;
        }

        String finalFromEmail = resolveFromEmail();
        String finalFromName = resolveFromName();

        for (EmailOutbox entry : failed) {
            if (entry.getAttemptCount() >= MAX_OUTBOX_RETRY_ATTEMPTS) {
                continue;
            }
            try {
                sendOnce(entry.getToEmail(), entry.getSubject(), entry.getBodyHtml(), finalFromEmail, finalFromName);
                entry.setStatus(EmailOutbox.Status.SENT);
                entry.setSentAt(LocalDateTime.now());
                log.info("[EmailService] Outbox retry succeeded for {}", entry.getToEmail());
            } catch (Exception e) {
                entry.setLastError(truncate(e.getMessage()));
                log.warn("[EmailService] Outbox retry failed for {}: {}", entry.getToEmail(), e.getMessage());
            }
            entry.setAttemptCount(entry.getAttemptCount() + 1);
            entry.setLastAttemptAt(LocalDateTime.now());
            emailOutboxRepository.save(entry);
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() <= MAX_ERROR_LENGTH ? s : s.substring(0, MAX_ERROR_LENGTH);
    }

    private Optional<String> blankToEmpty(String value) {
        return (value == null || value.isBlank()) ? Optional.empty() : Optional.of(value);
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
                            <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="margin:32px 0;">
                              <tr>
                                <td align="center">
                                  <table border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                      <td align="center" style="border-radius: 8px;" bgcolor="#0f766e">
                                        <a href="%s" target="_blank" style="font-size: 16px; font-family: 'Segoe UI', Arial, sans-serif; color: #ffffff; text-decoration: none; border-radius: 8px; padding: 14px 36px; border: 1px solid #0f766e; display: inline-block; font-weight: 600;">
                                          🔑 Thiết lập mật khẩu
                                        </a>
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>
                            </table>
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

    private String buildVerificationOtpEmailBody(String otpCode) {
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
                              Cảm ơn bạn đã đăng ký tài khoản JLPT Platform! Nhập mã bên dưới trên trang xác minh
                              để kích hoạt tài khoản.
                            </p>
                            <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="margin:32px 0;">
                              <tr>
                                <td align="center">
                                  <div style="display:inline-block; background:#f4f6fb; border:1px solid #e5e7eb; border-radius:8px; padding:18px 36px; font-size:32px; font-weight:800; letter-spacing:10px; color:#4f46e5;">
                                    %s
                                  </div>
                                </td>
                              </tr>
                            </table>
                            <p style="color:#9ca3af; font-size:13px; line-height:1.6;">
                              Mã có hiệu lực trong <strong>10 phút</strong>. Nếu bạn không tạo tài khoản này, hãy bỏ qua email này.
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
                .formatted(otpCode);
    }

    private String buildOtpEmailBody(String otpCode) {
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
                            <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="margin:32px 0;">
                              <tr>
                                <td align="center">
                                  <div style="display:inline-block; background:#f4f6fb; border:1px solid #e5e7eb; border-radius:8px; padding:18px 36px; font-size:32px; font-weight:800; letter-spacing:10px; color:#4f46e5;">
                                    %s
                                  </div>
                                </td>
                              </tr>
                            </table>
                            <p style="color:#9ca3af; font-size:13px; line-height:1.6;">
                              Nếu bạn không yêu cầu mã này, hãy bỏ qua email này.
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
                .formatted(otpCode);
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
                            <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="margin:32px 0;">
                              <tr>
                                <td align="center">
                                  <table border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                      <td align="center" style="border-radius: 8px;" bgcolor="#dc2626">
                                        <a href="%s" target="_blank" style="font-size: 16px; font-family: 'Segoe UI', Arial, sans-serif; color: #ffffff; text-decoration: none; border-radius: 8px; padding: 14px 36px; border: 1px solid #dc2626; display: inline-block; font-weight: 600;">
                                          🔑 Đặt lại mật khẩu
                                        </a>
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>
                            </table>
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
