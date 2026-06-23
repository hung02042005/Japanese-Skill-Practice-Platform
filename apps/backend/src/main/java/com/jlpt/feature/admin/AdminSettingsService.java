/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.response.SettingResponse;
import com.jlpt.shared.exception.BusinessException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSettingsService {

    private static final Set<String> ALLOWED_GROUPS = Set.of(
            "general", "smtp", "security", "auto_notification",
            "email_register", "email_otp", "email_reset"
    );

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

        return SettingResponse.builder()
                .settingKey(key)
                .settingValue(isPassword(key) ? "" : value)
                .valueType(setting.getValueType() != null ? setting.getValueType().getValue() : "string")
                .build();
    }

    /** POST /api/admin/settings/smtp/test — kiểm tra kết nối SMTP hiện tại. */
    public void testSmtpConnection() {
        try {
            mailSender.testConnection();
        } catch (Exception e) {
            throw new BusinessException(502, "SMTP_TEST_FAILED",
                    "Kết nối SMTP thất bại: " + e.getMessage());
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validateGroup(String group) {
        if (!ALLOWED_GROUPS.contains(group)) {
            throw new BusinessException(400, "INVALID_SETTING_GROUP",
                    "Nhóm cài đặt không hợp lệ: " + group);
        }
    }

    private boolean isPassword(String key) {
        return key != null && key.toLowerCase().contains(SENSITIVE_KEY);
    }
}