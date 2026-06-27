/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.SystemSettingRequest;
import com.jlpt.feature.admin.dto.SystemSettingResponse;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin — quản lý System Settings bản đầy đủ (UC-39): che giá trị nhạy cảm,
 * tôn trọng cờ is_editable, ghi audit kèm lý do thay đổi.
 * KHÔNG thay thế AdminSettingsController cũ (/api/admin/settings) — chạy song song ở
 * /api/admin/system-settings; cần hợp nhất sau.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSystemSettingService {

    private static final String MASK = "*****";
    private static final List<String> SENSITIVE_KEYWORDS =
            List.of("password", "secret", "api_key", "token", "private_key");

    private final SystemSettingRepository systemSettingRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> listAll() {
        return systemSettingRepository.findAll().stream()
                .sorted(Comparator.comparing(SystemSetting::getSettingGroup)
                        .thenComparing(SystemSetting::getSettingKey))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> listByGroup(String group) {
        return systemSettingRepository.findBySettingGroup(group).stream()
                .sorted(Comparator.comparing(SystemSetting::getSettingKey))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SystemSettingResponse updateSetting(Integer settingId, String adminEmail, SystemSettingRequest req) {
        SystemSetting setting = systemSettingRepository.findById(settingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy setting id=" + settingId));
        if (Boolean.FALSE.equals(setting.getIsEditable())) {
            throw new BusinessException(403, "SETTING_NOT_EDITABLE",
                    "Setting này không được phép chỉnh sửa");
        }
        AdminUser admin = adminUserRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Admin"));

        String oldMasked = isSensitive(setting.getSettingKey()) ? MASK : setting.getSettingValue();
        setting.setSettingValue(req.getValue());
        setting.setUpdatedBy(admin);
        setting.setUpdatedAt(LocalDateTime.now());
        systemSettingRepository.save(setting);

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .adminActor(admin)
                .action("SETTING_UPDATED")
                .targetTable("system_settings")
                .targetId(settingId.longValue())
                .description("Setting '" + setting.getSettingGroup() + "." + setting.getSettingKey()
                        + "' cập nhật"
                        + (isSensitive(setting.getSettingKey()) ? "" : " (cũ=" + oldMasked + ")")
                        + (req.getChangeReason() != null ? " — lý do: " + req.getChangeReason() : ""))
                .build());

        log.info("[AdminSettings] {} updated setting {}.{}",
                adminEmail, setting.getSettingGroup(), setting.getSettingKey());
        return toResponse(setting);
    }

    private SystemSettingResponse toResponse(SystemSetting s) {
        return SystemSettingResponse.builder()
                .settingId(s.getId())
                .settingGroup(s.getSettingGroup())
                .settingKey(s.getSettingKey())
                .settingValue(isSensitive(s.getSettingKey()) ? MASK : s.getSettingValue())
                .valueType(s.getValueType() != null ? s.getValueType().getValue() : null)
                .isEditable(s.getIsEditable())
                .updatedByAdminName(s.getUpdatedBy() != null ? s.getUpdatedBy().getFullName() : null)
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private boolean isSensitive(String key) {
        if (key == null) return false;
        String lower = key.toLowerCase();
        return SENSITIVE_KEYWORDS.stream().anyMatch(lower::contains);
    }
}
