/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.request.SmtpTestRequest;
import com.jlpt.feature.admin.dto.request.UpdateSettingsBatchRequest;
import com.jlpt.feature.admin.dto.response.SettingResponse;
import com.jlpt.shared.exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSettingsService {

    private static final Set<String> ALLOWED_GROUPS = Set.of(
            "general", "system", "smtp", "security", "auto_notification", "email_register", "email_otp", "email_reset");

    private static final String SENSITIVE_KEY = "password";

    private final SystemSettingRepository settingRepository;
    private final JavaMailSenderImpl mailSender;

    /** GET /api/admin/settings/{group} — trả list setting, ẩn giá trị password. */
    @Transactional(readOnly = true)
    public List<SettingResponse> getByGroup(String group) {
        validateGroup(group);
        return settingRepository.findBySettingGroup(group).stream()
                .map(s -> SettingResponse.builder()
                        .settingKey(s.getSettingKey())
                        .settingValue(isPassword(s.getSettingKey()) ? (s.getSettingValue() == null || s.getSettingValue().isBlank() ? "" : "********") : s.getSettingValue())
                        .valueType(s.getValueType() != null ? s.getValueType().getValue() : "string")
                        .build())
                .toList();
    }

    /** PUT /api/admin/settings/{group}/{key} — upsert một setting. */
    @Transactional
    public SettingResponse updateSetting(String group, String key, String value) {
        validateGroup(group);
        return upsert(group, key, value);
    }

    /** PUT /api/admin/settings/{group} — upsert nhiều setting cùng nhóm trong 1 transaction. */
    @Transactional
    public List<SettingResponse> updateSettings(String group, List<UpdateSettingsBatchRequest.Item> items) {
        validateGroup(group);
        List<SettingResponse> result = new ArrayList<>(items.size());
        for (UpdateSettingsBatchRequest.Item item : items) {
            // Để trống ô mật khẩu = giữ nguyên giá trị hiện tại (không ghi đè bằng rỗng).
            if (isPassword(item.getSettingKey())
                    && (item.getSettingValue() == null || item.getSettingValue().isBlank())) {
                continue;
            }
            result.add(upsert(group, item.getSettingKey(), item.getSettingValue()));
        }
        if ("smtp".equals(group)) {
            applySmtpSettingsToMailSender();
        }
        return result;
    }

    private SettingResponse upsert(String group, String key, String value) {
        SystemSetting setting = settingRepository
                .findBySettingGroupAndSettingKey(group, key)
                .orElseGet(() -> SystemSetting.builder()
                        .settingGroup(group)
                        .settingKey(key)
                        .valueType(SystemSetting.ValueType.STRING)
                        .build());

        if (Boolean.FALSE.equals(setting.getIsEditable())) {
            throw new BusinessException(403, "SETTING_LOCKED", "Cài đặt này không được phép chỉnh sửa");
        }

        setting.setSettingValue(value);
        settingRepository.save(setting);

        if ("smtp".equals(group)) {
            applySmtpSettingsToMailSender();
        }

        return SettingResponse.builder()
                .settingKey(key)
                .settingValue(isPassword(key) ? (value == null || value.isBlank() ? "" : "********") : value)
                .valueType(
                        setting.getValueType() != null ? setting.getValueType().getValue() : "string")
                .build();
    }

    /** POST /api/admin/settings/smtp/test — kiểm tra kết nối SMTP hiện tại. */
    public void testSmtpConnection(SmtpTestRequest request) {
        try {
            JavaMailSenderImpl testSender = new JavaMailSenderImpl();

            // Nếu request không có cấu hình (ví dụ gọi từ code cũ), lấy từ DB
            String host = request != null && request.getHost() != null ? request.getHost() : settingRepository.findBySettingGroupAndSettingKey("smtp", "host").map(SystemSetting::getSettingValue).orElse("");
            String portStr = request != null && request.getPort() != null ? request.getPort() : settingRepository.findBySettingGroupAndSettingKey("smtp", "port").map(SystemSetting::getSettingValue).orElse("");
            String username = request != null && request.getUsername() != null ? request.getUsername() : settingRepository.findBySettingGroupAndSettingKey("smtp", "username").map(SystemSetting::getSettingValue).orElse("");
            String password = request != null && request.getPassword() != null ? request.getPassword() : settingRepository.findBySettingGroupAndSettingKey("smtp", "password").map(SystemSetting::getSettingValue).orElse("");
            String secure = request != null && request.getSecure() != null ? request.getSecure() : settingRepository.findBySettingGroupAndSettingKey("smtp", "secure").map(SystemSetting::getSettingValue).orElse("");

            testSender.setHost(host);
            
            if (portStr != null && !portStr.trim().isEmpty()) {
                try {
                    testSender.setPort(Integer.parseInt(portStr.trim()));
                } catch (NumberFormatException ex) {
                    throw new BusinessException(400, "INVALID_PORT", "Cổng SMTP không hợp lệ: " + portStr);
                }
            }
            
            testSender.setUsername(username);
            if (password != null) {
                testSender.setPassword(password.replace(" ", ""));
            }

            secure = secure == null ? "" : secure.toUpperCase();
            if (secure.contains("STARTTLS") || secure.contains("TLS")) {
                testSender.getJavaMailProperties().put("mail.smtp.starttls.enable", "true");
                testSender.getJavaMailProperties().put("mail.smtp.starttls.required", "true");
            } else {
                testSender.getJavaMailProperties().put("mail.smtp.starttls.enable", "false");
                testSender.getJavaMailProperties().put("mail.smtp.starttls.required", "false");
            }
            if (secure.equals("SSL")) {
                testSender.getJavaMailProperties().put("mail.smtp.ssl.enable", "true");
            } else {
                testSender.getJavaMailProperties().put("mail.smtp.ssl.enable", "false");
            }

            if (testSender.getUsername() != null && !testSender.getUsername().isEmpty()) {
                testSender.getJavaMailProperties().put("mail.smtp.auth", "true");
            } else {
                testSender.getJavaMailProperties().put("mail.smtp.auth", "false");
            }

            testSender.getJavaMailProperties().put("mail.smtp.connectiontimeout", "5000");
            testSender.getJavaMailProperties().put("mail.smtp.timeout", "5000");
            testSender.getJavaMailProperties().put("mail.smtp.writetimeout", "5000");

            jakarta.mail.Session newSession = jakarta.mail.Session.getInstance(testSender.getJavaMailProperties());
            testSender.setSession(newSession);

            testSender.testConnection();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[AdminSettingsService] SMTP Test failed: ", e);
            throw new BusinessException(
                    502,
                    "SMTP_TEST_FAILED",
                    "Kết nối SMTP thất bại. Vui lòng kiểm tra lại thông tin cấu hình. Chi tiết: " + e.getMessage());
        }
    }

    @jakarta.annotation.PostConstruct
    public void applySmtpSettingsToMailSender() {
        try {
            settingRepository
                    .findBySettingGroupAndSettingKey("smtp", "host")
                    .ifPresent(s -> mailSender.setHost(s.getSettingValue()));

            settingRepository
                    .findBySettingGroupAndSettingKey("smtp", "port")
                    .ifPresent(s -> {
                        String portStr = s.getSettingValue();
                        if (portStr != null && !portStr.trim().isEmpty()) {
                            try {
                                mailSender.setPort(Integer.parseInt(portStr.trim()));
                            } catch (NumberFormatException ex) {
                                log.warn("[AdminSettingsService] Invalid port value in DB: {}", portStr);
                            }
                        }
                    });

            settingRepository
                    .findBySettingGroupAndSettingKey("smtp", "username")
                    .ifPresent(s -> mailSender.setUsername(s.getSettingValue()));

            settingRepository
                    .findBySettingGroupAndSettingKey("smtp", "password")
                    .ifPresent(s -> {
                        String pass = s.getSettingValue();
                        if (pass != null) {
                            mailSender.setPassword(pass.replace(" ", ""));
                        }
                    });

            settingRepository.findBySettingGroupAndSettingKey("smtp", "secure").ifPresent(s -> {
                String val = s.getSettingValue();
                String secure = val == null ? "" : val.toUpperCase();
                if (secure.contains("STARTTLS") || secure.contains("TLS")) {
                    mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", "true");
                    mailSender.getJavaMailProperties().put("mail.smtp.starttls.required", "true");
                } else {
                    mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", "false");
                    mailSender.getJavaMailProperties().put("mail.smtp.starttls.required", "false");
                }
                if (secure.equals("SSL")) {
                    mailSender.getJavaMailProperties().put("mail.smtp.ssl.enable", "true");
                } else {
                    mailSender.getJavaMailProperties().put("mail.smtp.ssl.enable", "false");
                }
            });

            if (mailSender.getUsername() != null && !mailSender.getUsername().isEmpty()) {
                mailSender.getJavaMailProperties().put("mail.smtp.auth", "true");
            } else {
                mailSender.getJavaMailProperties().put("mail.smtp.auth", "false");
            }

            // Set timeouts to prevent hanging (e.g. 10 seconds)
            mailSender.getJavaMailProperties().put("mail.smtp.connectiontimeout", "10000");
            mailSender.getJavaMailProperties().put("mail.smtp.timeout", "10000");
            mailSender.getJavaMailProperties().put("mail.smtp.writetimeout", "10000");

            // Force recreation of the Session so that changes take effect immediately
            jakarta.mail.Session newSession = jakarta.mail.Session.getInstance(mailSender.getJavaMailProperties());
            mailSender.setSession(newSession);

            log.info("[AdminSettingsService] Applied SMTP settings to JavaMailSender (Host: {})", mailSender.getHost());
        } catch (Exception e) {
            log.error("[AdminSettingsService] Failed to apply SMTP settings to JavaMailSender", e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validateGroup(String group) {
        if (!ALLOWED_GROUPS.contains(group)) {
            throw new BusinessException(400, "INVALID_SETTING_GROUP", "Nhóm cài đặt không hợp lệ: " + group);
        }
    }

    private boolean isPassword(String key) {
        return key != null && key.toLowerCase().contains(SENSITIVE_KEY);
    }
}
