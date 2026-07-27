/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.SystemSetting;
import com.jlpt.feature.admin.SystemSettingRepository;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Phần còn lại của EmailService mà {@link EmailServiceTest} (chỉ phủ outbox) chưa chạm: render
 * subject/nội dung lấy từ DB, thay biến {{...}}, escape HTML do admin nhập, và fallback địa chỉ
 * người gửi theo thứ tự from_email → username → property.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTemplateTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SystemSettingRepository settingRepository;

    @Mock
    private EmailOutboxRepository emailOutboxRepository;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@sakuji.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://sakuji.com");
        ReflectionTestUtils.setField(emailService, "adminNotifyEmail", "admin@sakuji.com");

        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(settingRepository.findBySettingGroupAndSettingKey(anyString(), anyString()))
                .thenReturn(Optional.empty());
    }

    private SystemSetting setting(String value) {
        return SystemSetting.builder().settingValue(value).build();
    }

    private String sentHtml() throws Exception {
        verify(mailSender).send(any(MimeMessage.class));
        return extractText(mimeMessage);
    }

    /** MimeMessageHelper dựng message dạng multipart nên phải duyệt đệ quy mới lấy được phần HTML. */
    private String extractText(Part part) throws Exception {
        Object content = part.getContent();
        if (content instanceof String text) {
            return text;
        }
        if (content instanceof Multipart multipart) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                sb.append(extractText(multipart.getBodyPart(i)));
            }
            return sb.toString();
        }
        return "";
    }

    private String firstRecipient() throws Exception {
        return mimeMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString();
    }

    // ── nội dung mặc định khi DB chưa cấu hình ───────────────────────────────

    @Test
    void sendVerificationEmail_noDbTemplate_usesDefaultSubjectAndContent() throws Exception {
        emailService.sendVerificationEmail("student@example.com", "123456");

        assertEquals("[JLPT Platform] Mã xác minh địa chỉ email của bạn", mimeMessage.getSubject());
        String html = sentHtml();
        assertTrue(html.contains("123456"));
        assertTrue(html.contains("Cảm ơn bạn đã đăng ký tài khoản JLPT Platform!"));
        assertEquals("student@example.com", firstRecipient());
    }

    @Test
    void sendOtpEmail_noDbTemplate_usesDefaultOtpContent() throws Exception {
        emailService.sendOtpEmail("student@example.com", "999888");

        assertEquals("[JLPT Platform] Mã xác thực của bạn", mimeMessage.getSubject());
        String html = sentHtml();
        assertTrue(html.contains("999888"));
        assertTrue(html.contains("Sử dụng mã bên dưới để hoàn tất xác thực."));
    }

    @Test
    void sendPasswordResetEmail_noDbTemplate_buildsResetLinkFromFrontendUrl() throws Exception {
        emailService.sendPasswordResetEmail("student@example.com", "tok-123");

        assertEquals("[JLPT Platform] Đặt lại mật khẩu", mimeMessage.getSubject());
        String html = sentHtml();
        assertTrue(html.contains("https://sakuji.com/reset-password?token=tok-123"));
        assertTrue(html.contains("Chúng tôi nhận được yêu cầu đặt lại mật khẩu"));
    }

    // ── template lấy từ DB ───────────────────────────────────────────────────

    @Test
    void sendVerificationEmail_dbTemplate_overridesSubjectAndSubstitutesVariables() throws Exception {
        when(settingRepository.findBySettingGroupAndSettingKey("email_register", "subject"))
                .thenReturn(Optional.of(setting("[{{platform_name}}] Xác minh tài khoản")));
        when(settingRepository.findBySettingGroupAndSettingKey("email_register", "body_text"))
                .thenReturn(Optional.of(setting("Chào bạn, liên hệ {{support_email}} nếu cần hỗ trợ.")));
        when(settingRepository.findBySettingGroupAndSettingKey("smtp", "from_name"))
                .thenReturn(Optional.of(setting("Sakuji")));
        when(settingRepository.findBySettingGroupAndSettingKey("smtp", "from_email"))
                .thenReturn(Optional.of(setting("hello@sakuji.com")));

        emailService.sendVerificationEmail("student@example.com", "123456");

        assertEquals("[Sakuji] Xác minh tài khoản", mimeMessage.getSubject());
        assertTrue(sentHtml().contains("liên hệ hello@sakuji.com nếu cần hỗ trợ."));
    }

    @Test
    void sendOtpEmail_dbSubjectWithYearVariable_isSubstituted() throws Exception {
        when(settingRepository.findBySettingGroupAndSettingKey("email_otp", "subject"))
                .thenReturn(Optional.of(setting("Mã xác thực {{current_year}}")));

        emailService.sendOtpEmail("student@example.com", "999888");

        assertEquals("Mã xác thực " + java.time.Year.now().getValue(), mimeMessage.getSubject());
    }

    @Test
    void sendVerificationEmail_blankDbTemplate_fallsBackToDefault() throws Exception {
        when(settingRepository.findBySettingGroupAndSettingKey("email_register", "subject"))
                .thenReturn(Optional.of(setting("   ")));
        when(settingRepository.findBySettingGroupAndSettingKey("email_register", "body_text"))
                .thenReturn(Optional.of(setting("")));

        emailService.sendVerificationEmail("student@example.com", "123456");

        assertEquals("[JLPT Platform] Mã xác minh địa chỉ email của bạn", mimeMessage.getSubject());
        assertTrue(sentHtml().contains("Cảm ơn bạn đã đăng ký tài khoản"));
    }

    @Test
    void sendPasswordResetEmail_nullDbTemplateValue_fallsBackToDefault() throws Exception {
        when(settingRepository.findBySettingGroupAndSettingKey("email_reset", "subject"))
                .thenReturn(Optional.of(setting(null)));
        when(settingRepository.findBySettingGroupAndSettingKey("email_reset", "body_text"))
                .thenReturn(Optional.of(setting(null)));

        emailService.sendPasswordResetEmail("student@example.com", "tok-123");

        assertEquals("[JLPT Platform] Đặt lại mật khẩu", mimeMessage.getSubject());
        assertTrue(sentHtml().contains("Chúng tôi nhận được yêu cầu đặt lại mật khẩu"));
    }

    // ── escape + xuống dòng ──────────────────────────────────────────────────

    @Test
    void renderContent_htmlInAdminTemplate_isEscapedNotInjected() throws Exception {
        when(settingRepository.findBySettingGroupAndSettingKey("email_register", "body_text"))
                .thenReturn(Optional.of(setting("Xin chào <script>alert(\"x\")</script> & bạn")));

        emailService.sendVerificationEmail("student@example.com", "123456");

        String html = sentHtml();
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("&amp;"));
        assertTrue(html.contains("&quot;"));
    }

    @Test
    void renderContent_multiParagraphTemplate_wrapsEachParagraphAndKeepsLineBreaks() throws Exception {
        when(settingRepository.findBySettingGroupAndSettingKey("email_register", "body_text"))
                .thenReturn(Optional.of(setting("Đoạn một\ncó xuống dòng\n\n\nĐoạn hai")));

        emailService.sendVerificationEmail("student@example.com", "123456");

        String html = sentHtml();
        assertTrue(html.contains("Đoạn một<br>có xuống dòng"));
        assertTrue(html.contains("Đoạn hai"));
    }

    // ── địa chỉ người gửi ────────────────────────────────────────────────────

    @Test
    void resolveFromEmail_blankFromEmail_fallsBackToSmtpUsername() throws Exception {
        when(settingRepository.findBySettingGroupAndSettingKey("smtp", "from_email"))
                .thenReturn(Optional.of(setting("  ")));
        when(settingRepository.findBySettingGroupAndSettingKey("smtp", "username"))
                .thenReturn(Optional.of(setting("smtp-user@sakuji.com")));

        emailService.sendNotificationEmail("student@example.com", "Tiêu đề", "Nội dung");

        assertTrue(mimeMessage.getFrom()[0].toString().contains("smtp-user@sakuji.com"));
    }

    @Test
    void resolveFromEmail_noDbValue_fallsBackToConfiguredProperty() throws Exception {
        emailService.sendNotificationEmail("student@example.com", "Tiêu đề", "Nội dung");

        assertTrue(mimeMessage.getFrom()[0].toString().contains("noreply@sakuji.com"));
    }

    // ── các email không dùng template DB ─────────────────────────────────────

    @Test
    void sendStaffInvitationEmail_buildsSetupLinkWithToken() throws Exception {
        emailService.sendStaffInvitationEmail("staff@example.com", "tok-456");

        assertTrue(mimeMessage.getSubject().contains("Lời mời tham gia hệ thống"));
        assertTrue(sentHtml().contains("https://sakuji.com/staff/setup-password?token=tok-456"));
    }

    @Test
    void notifyAdminsStaffPasswordResetRequested_sendsToAdminNotifyAddress() throws Exception {
        emailService.notifyAdminsStaffPasswordResetRequested("Staff One", "staff@example.com");

        assertEquals("admin@sakuji.com", firstRecipient());
        String html = sentHtml();
        assertTrue(html.contains("Staff One"));
        assertTrue(html.contains("staff@example.com"));
    }

    @Test
    void sendStaffTempPasswordEmail_includesTemporaryPassword() throws Exception {
        emailService.sendStaffTempPasswordEmail("staff@example.com", "Tmp@12345");

        assertTrue(mimeMessage.getSubject().contains("Temporary password"));
        assertTrue(sentHtml().contains("Tmp@12345"));
    }

    @Test
    void sendNotificationEmail_prefixesSubjectAndRendersContent() throws Exception {
        emailService.sendNotificationEmail("student@example.com", "Bài mới", "Đã có bài học mới");

        assertEquals("[JLPT Platform] Bài mới", mimeMessage.getSubject());
        String html = sentHtml();
        assertTrue(html.contains("Bài mới"));
        assertTrue(html.contains("Đã có bài học mới"));
    }
}
