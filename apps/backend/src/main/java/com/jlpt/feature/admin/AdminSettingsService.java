/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

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
                        .settingValue(isPassword(s.getSettingKey()) ? "" : s.getSettingValue())
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
                .settingValue(isPassword(key) ? "" : value)
                .valueType(
                        setting.getValueType() != null ? setting.getValueType().getValue() : "string")
                .build();
    }

    /** POST /api/admin/settings/smtp/test — kiểm tra kết nối SMTP hiện tại. */
    public void testSmtpConnection() {
        try {
            mailSender.testConnection();
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

            settingRepository.findBySettingGroupAndSettingKey("smtp", "port").ifPresent(s -> {
                try {
                    mailSender.setPort(Integer.parseInt(s.getSettingValue()));
                } catch (NumberFormatException ignored) {
                }
            });

            settingRepository
                    .findBySettingGroupAndSettingKey("smtp", "username")
                    .ifPresent(s -> mailSender.setUsername(s.getSettingValue()));

            settingRepository
                    .findBySettingGroupAndSettingKey("smtp", "password")
                    .ifPresent(s -> mailSender.setPassword(s.getSettingValue()));

            settingRepository.findBySettingGroupAndSettingKey("smtp", "secure").ifPresent(s -> {
                String secure = s.getSettingValue().toUpperCase();
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
