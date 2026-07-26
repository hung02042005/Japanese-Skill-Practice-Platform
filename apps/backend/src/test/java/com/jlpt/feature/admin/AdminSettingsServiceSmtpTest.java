/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.dto.request.SmtpTestRequest;
import com.jlpt.feature.admin.dto.request.UpdateSettingsBatchRequest;
import com.jlpt.feature.admin.dto.response.SettingResponse;
import com.jlpt.shared.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Phần SMTP của AdminSettingsService mà {@link AdminSettingsServiceTest} chưa phủ: đồng bộ cấu hình
 * từ DB xuống JavaMailSender (host/port/username/password/secure) và kiểm tra kết nối SMTP.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminSettingsServiceSmtpTest {

    @Mock
    private SystemSettingRepository settingRepository;

    private JavaMailSenderImpl mailSender;

    @InjectMocks
    private AdminSettingsService service;

    @BeforeEach
    void setUp() {
        mailSender = new JavaMailSenderImpl();
        service = new AdminSettingsService(settingRepository, mailSender);
        when(settingRepository.findBySettingGroupAndSettingKey(anyString(), anyString()))
                .thenReturn(Optional.empty());
    }

    private void stubSmtp(String key, String value) {
        when(settingRepository.findBySettingGroupAndSettingKey("smtp", key))
                .thenReturn(Optional.of(SystemSetting.builder()
                        .settingGroup("smtp")
                        .settingKey(key)
                        .settingValue(value)
                        .build()));
    }

    // ── applySmtpSettingsToMailSender ────────────────────────────────────────

    @Test
    void applySmtp_allValuesPresent_copiesThemToMailSender() {
        stubSmtp("host", "smtp.gmail.com");
        stubSmtp("port", " 587 ");
        stubSmtp("username", "sender@sakuji.com");
        stubSmtp("password", "app pass word");
        stubSmtp("secure", "starttls");

        service.applySmtpSettingsToMailSender();

        assertEquals("smtp.gmail.com", mailSender.getHost());
        assertEquals(587, mailSender.getPort());
        assertEquals("sender@sakuji.com", mailSender.getUsername());
        assertEquals("apppassword", mailSender.getPassword());
        assertEquals("true", mailSender.getJavaMailProperties().get("mail.smtp.starttls.enable"));
        assertEquals("true", mailSender.getJavaMailProperties().get("mail.smtp.starttls.required"));
        assertEquals("false", mailSender.getJavaMailProperties().get("mail.smtp.ssl.enable"));
        assertEquals("true", mailSender.getJavaMailProperties().get("mail.smtp.auth"));
        assertEquals("10000", mailSender.getJavaMailProperties().get("mail.smtp.connectiontimeout"));
        assertNotNull(mailSender.getSession());
    }

    @Test
    void applySmtp_secureSsl_enablesSslAndDisablesStarttls() {
        stubSmtp("secure", "ssl");

        service.applySmtpSettingsToMailSender();

        assertEquals("true", mailSender.getJavaMailProperties().get("mail.smtp.ssl.enable"));
        assertEquals("false", mailSender.getJavaMailProperties().get("mail.smtp.starttls.enable"));
    }

    @Test
    void applySmtp_secureNone_disablesBothStarttlsAndSsl() {
        stubSmtp("secure", "none");

        service.applySmtpSettingsToMailSender();

        assertEquals("false", mailSender.getJavaMailProperties().get("mail.smtp.starttls.enable"));
        assertEquals("false", mailSender.getJavaMailProperties().get("mail.smtp.ssl.enable"));
    }

    @Test
    void applySmtp_secureNullValue_treatedAsNone() {
        stubSmtp("secure", null);

        service.applySmtpSettingsToMailSender();

        assertEquals("false", mailSender.getJavaMailProperties().get("mail.smtp.starttls.enable"));
    }

    @Test
    void applySmtp_invalidPort_isIgnoredWithoutThrowing() {
        stubSmtp("port", "not-a-number");
        stubSmtp("host", "smtp.gmail.com");

        assertDoesNotThrow(() -> service.applySmtpSettingsToMailSender());

        assertEquals("smtp.gmail.com", mailSender.getHost());
    }

    @Test
    void applySmtp_blankPort_isSkipped() {
        stubSmtp("port", "   ");

        service.applySmtpSettingsToMailSender();

        assertEquals(JavaMailSenderImpl.DEFAULT_PORT, mailSender.getPort());
    }

    @Test
    void applySmtp_nullPassword_isSkipped() {
        stubSmtp("password", null);

        service.applySmtpSettingsToMailSender();

        assertNull(mailSender.getPassword());
    }

    @Test
    void applySmtp_noUsername_disablesAuth() {
        stubSmtp("host", "smtp.gmail.com");

        service.applySmtpSettingsToMailSender();

        assertEquals("false", mailSender.getJavaMailProperties().get("mail.smtp.auth"));
    }

    @Test
    void applySmtp_repositoryThrows_isSwallowedSoStartupNotBlocked() {
        when(settingRepository.findBySettingGroupAndSettingKey("smtp", "host"))
                .thenThrow(new IllegalStateException("DB chưa sẵn sàng"));

        assertDoesNotThrow(() -> service.applySmtpSettingsToMailSender());
    }

    // ── updateSettings: nhóm smtp kéo theo apply ─────────────────────────────

    @Test
    void updateSettings_smtpGroup_appliesConfigToMailSender() {
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateSettingsBatchRequest.Item host = new UpdateSettingsBatchRequest.Item();
        host.setSettingKey("host");
        host.setSettingValue("smtp.sakuji.com");

        List<SettingResponse> result = service.updateSettings("smtp", List.of(host));

        assertEquals(1, result.size());
        assertEquals("smtp.sakuji.com", result.get(0).getSettingValue());
        verify(settingRepository).save(any(SystemSetting.class));
    }

    @Test
    void updateSettings_nonSmtpGroup_skipsMailSenderApply() {
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateSettingsBatchRequest.Item item = new UpdateSettingsBatchRequest.Item();
        item.setSettingKey("site_name");
        item.setSettingValue("Sakuji");

        assertEquals(1, service.updateSettings("general", List.of(item)).size());
        assertNull(mailSender.getHost());
    }

    @Test
    void updateSettings_passwordWithRealValue_isSaved() {
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateSettingsBatchRequest.Item item = new UpdateSettingsBatchRequest.Item();
        item.setSettingKey("password");
        item.setSettingValue("real-secret");

        List<SettingResponse> result = service.updateSettings("smtp", List.of(item));

        assertEquals(1, result.size());
        assertEquals("********", result.get(0).getSettingValue());
    }

    @Test
    void updateSettings_invalidGroup_throwsBeforeTouchingRepository() {
        UpdateSettingsBatchRequest.Item item = new UpdateSettingsBatchRequest.Item();
        item.setSettingKey("host");
        List<UpdateSettingsBatchRequest.Item> items = List.of(item);

        assertEquals(
                "INVALID_SETTING_GROUP",
                assertThrows(BusinessException.class, () -> service.updateSettings("hacker", items))
                        .getErrorCode());
        verify(settingRepository, never()).save(any());
    }

    @Test
    void updateSetting_passwordWithMaskPlaceholder_keepsStoredValue() {
        SystemSetting existing = SystemSetting.builder()
                .settingGroup("smtp")
                .settingKey("password")
                .settingValue("original-secret")
                .valueType(SystemSetting.ValueType.STRING)
                .build();
        when(settingRepository.findBySettingGroupAndSettingKey("smtp", "password"))
                .thenReturn(Optional.of(existing));
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SettingResponse response = service.updateSetting("smtp", "password", "********");

        assertEquals("original-secret", existing.getSettingValue());
        assertEquals("********", response.getSettingValue());
    }

    @Test
    void updateSetting_newKey_createsSettingWithStringType() {
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SettingResponse response = service.updateSetting("security", "max_login_attempts", "5");

        assertEquals("5", response.getSettingValue());
        assertEquals("string", response.getValueType());
    }

    // ── testSmtpConnection ───────────────────────────────────────────────────

    private SmtpTestRequest smtpRequest(String host, String port, String username, String password, String secure) {
        SmtpTestRequest request = new SmtpTestRequest();
        request.setHost(host);
        request.setPort(port);
        request.setUsername(username);
        request.setPassword(password);
        request.setSecure(secure);
        return request;
    }

    @Test
    void testSmtpConnection_invalidPort_throwsInvalidPort() {
        SmtpTestRequest request = smtpRequest("smtp.sakuji.com", "abc", "u", "p", "starttls");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.testSmtpConnection(request));
        assertEquals(400, ex.getStatus());
        assertEquals("INVALID_PORT", ex.getErrorCode());
    }

    @Test
    void testSmtpConnection_unreachableHost_throwsSmtpTestFailed() {
        // 127.0.0.1:1 chắc chắn không có SMTP nào lắng nghe → testConnection() ném IOException.
        SmtpTestRequest request = smtpRequest("127.0.0.1", "1", "", "", "none");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.testSmtpConnection(request));
        assertEquals(502, ex.getStatus());
        assertEquals("SMTP_TEST_FAILED", ex.getErrorCode());
    }

    @Test
    void testSmtpConnection_maskedPassword_readsRealPasswordFromDatabase() {
        stubSmtp("password", "stored-secret");
        SmtpTestRequest request = smtpRequest("127.0.0.1", "1", "user", "********", "ssl");

        assertEquals(
                "SMTP_TEST_FAILED",
                assertThrows(BusinessException.class, () -> service.testSmtpConnection(request))
                        .getErrorCode());
        verify(settingRepository).findBySettingGroupAndSettingKey("smtp", "password");
    }

    @Test
    void testSmtpConnection_nullRequest_readsEverythingFromDatabase() {
        stubSmtp("host", "127.0.0.1");
        stubSmtp("port", "1");
        stubSmtp("username", "user");
        stubSmtp("password", "secret");
        stubSmtp("secure", "tls");

        assertEquals(
                "SMTP_TEST_FAILED",
                assertThrows(BusinessException.class, () -> service.testSmtpConnection(null))
                        .getErrorCode());
        verify(settingRepository).findBySettingGroupAndSettingKey("smtp", "host");
    }

    @Test
    void testSmtpConnection_emptyDatabaseAndRequest_stillFailsGracefully() {
        assertEquals(
                "SMTP_TEST_FAILED",
                assertThrows(BusinessException.class, () -> service.testSmtpConnection(null))
                        .getErrorCode());
    }
}
