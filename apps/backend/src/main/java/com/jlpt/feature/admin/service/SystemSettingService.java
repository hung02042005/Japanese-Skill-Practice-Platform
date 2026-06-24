/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.service;

import com.jlpt.feature.admin.dto.SystemSettingRequest;
import com.jlpt.feature.admin.dto.SystemSettingResponse;
import com.jlpt.feature.admin.repository.SystemSettingRepository;
import com.jlpt.shared.audit.AdminAuditLog;
import com.jlpt.feature.admin.entity.AdminUser;
import com.jlpt.feature.admin.entity.SystemSetting;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.admin.repository.AdminUserRepository;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemSettingService {

    private final SystemSettingRepository settingRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    private static final Set<String> SENSITIVE_KEYWORDS =
            Set.of("password", "secret", "api_key", "token", "private_key");
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    // ── UC-39: Get all groups ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<String> getAllGroups() {
        return settingRepository.findAllDistinctGroups();
    }

    // ── UC-39: Get settings by group ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getSettingsByGroup(String group) {
        return settingRepository.findAllBySettingGroup(group).stream()
                .map(this::toResponse)
                .toList();
    }

    // ── UC-39: Get single setting ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SystemSettingResponse getSetting(String group, String key) {
        return settingRepository.findBySettingGroupAndSettingKey(group, key)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình: " + group + "/" + key));
    }

    // ── UC-39: Update setting ────────────────────────────────────────────────

    @Transactional
    public SystemSettingResponse updateSetting(String group, String key, SystemSettingRequest req, Long adminId) {
        SystemSetting setting = settingRepository.findBySettingGroupAndSettingKey(group, key)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình: " + group + "/" + key));

        if (!Boolean.TRUE.equals(setting.getIsEditable())) {
            throw new BusinessException(422, "SETTING_READ_ONLY", "Cài đặt này không thể chỉnh sửa");
        }

        validateValueType(setting.getValueType(), req.getValue());

        AdminUser admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Admin"));

        String oldValue = setting.getSettingValue();
        setting.setSettingValue(req.getValue());
        setting.setUpdatedBy(admin);
        settingRepository.save(setting);

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .adminActor(admin)
                .action("SETTING_UPDATED")
                .targetTable("system_settings")
                .description(group + "/" + key + " → " + req.getValue()
                        + (req.getChangeReason() != null ? " | Lý do: " + req.getChangeReason() : ""))
                .build());

        log.info("[SystemSetting] Admin {} updated {}/{}: {} → {}", adminId, group, key, oldValue, req.getValue());
        return toResponse(setting);
    }

    // ── UC-39: Maintenance mode ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public boolean isMaintenanceMode() {
        return settingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(false);
    }

    @Transactional
    public void setMaintenanceMode(boolean enabled, Long adminId) {
        SystemSetting setting = settingRepository
                .findBySettingGroupAndSettingKey("system", "maintenance_mode")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cài đặt maintenance_mode"));
        AdminUser admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Admin"));

        setting.setSettingValue(String.valueOf(enabled));
        setting.setUpdatedBy(admin);
        settingRepository.save(setting);

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .adminActor(admin)
                .action("MAINTENANCE_MODE_TOGGLED")
                .targetTable("system_settings")
                .description("maintenance_mode → " + enabled)
                .build());

        log.info("[SystemSetting] Admin {} {} maintenance mode", adminId, enabled ? "ENABLED" : "DISABLED");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validateValueType(SystemSetting.ValueType type, String value) {
        switch (type) {
            case BOOLEAN -> {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new BusinessException(400, "VALIDATION_FAILED",
                            "Giá trị BOOLEAN chỉ chấp nhận 'true' hoặc 'false'");
                }
            }
            case INTEGER -> {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new BusinessException(400, "VALIDATION_FAILED",
                            "Giá trị INTEGER phải là số nguyên hợp lệ");
                }
            }
            case TIME -> {
                if (!TIME_PATTERN.matcher(value).matches()) {
                    throw new BusinessException(400, "VALIDATION_FAILED",
                            "Giá trị TIME phải theo định dạng HH:mm (ví dụ: 08:30)");
                }
            }
            case STRING -> {
                if (value == null || value.isBlank()) {
                    throw new BusinessException(400, "VALIDATION_FAILED",
                            "Giá trị STRING không được để trống");
                }
            }
        }
    }

    /** Masks sensitive values — all masking is centralized here. */
    SystemSettingResponse toResponse(SystemSetting s) {
        String displayValue = s.getSettingValue();
        if (s.getSettingKey() != null) {
            String lower = s.getSettingKey().toLowerCase();
            if (SENSITIVE_KEYWORDS.stream().anyMatch(lower::contains)) {
                displayValue = "*****";
            }
        }
        return SystemSettingResponse.builder()
                .settingId(s.getId())
                .settingGroup(s.getSettingGroup())
                .settingKey(s.getSettingKey())
                .settingValue(displayValue)
                .valueType(s.getValueType().getValue())
                .isEditable(s.getIsEditable())
                .updatedByAdminName(s.getUpdatedBy() != null ? s.getUpdatedBy().getFullName() : null)
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
