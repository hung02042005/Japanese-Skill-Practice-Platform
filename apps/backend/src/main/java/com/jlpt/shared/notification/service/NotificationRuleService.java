/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.shared.notification.dto.NotificationRuleRequest;
import com.jlpt.shared.notification.dto.NotificationRuleResponse;
import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminUser;
import com.jlpt.feature.admin.SystemSetting;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.admin.SystemSettingRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationRuleService {

    private static final String NOTIFICATION_GROUP = "notification";

    private final SystemSettingRepository settingRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final ObjectMapper objectMapper;

    // ── UC-40: List rules ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificationRuleResponse> listRules() {
        return settingRepository.findBySettingGroup(NOTIFICATION_GROUP).stream()
                .map(this::parseRule)
                .filter(r -> r != null)
                .toList();
    }

    // ── UC-40: Create rule ───────────────────────────────────────────────────

    @Transactional
    public NotificationRuleResponse createRule(NotificationRuleRequest req, Long adminId) {
        if (settingRepository.existsBySettingGroupAndSettingKey(NOTIFICATION_GROUP, req.getRuleKey())) {
            throw new BusinessException(400, "DUPLICATE_RULE_KEY", "Rule key đã tồn tại: " + req.getRuleKey());
        }
        AdminUser admin = findAdminOrThrow(adminId);
        String jsonValue = buildJson(req);

        SystemSetting setting = SystemSetting.builder()
                .settingGroup(NOTIFICATION_GROUP)
                .settingKey(req.getRuleKey())
                .settingValue(jsonValue)
                .valueType(SystemSetting.ValueType.STRING)
                .isEditable(true)
                .updatedBy(admin)
                .build();
        settingRepository.save(setting);

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .adminActor(admin)
                .action("NOTIFICATION_RULE_CREATED")
                .targetTable("system_settings")
                .description("Created rule: " + req.getRuleKey())
                .build());

        log.info("[NotifRule] Admin {} created rule {}", adminId, req.getRuleKey());
        return parseRule(setting);
    }

    // ── UC-40: Update rule ───────────────────────────────────────────────────

    @Transactional
    public NotificationRuleResponse updateRule(String ruleKey, NotificationRuleRequest req, Long adminId) {
        SystemSetting setting = findRuleOrThrow(ruleKey);
        AdminUser admin = findAdminOrThrow(adminId);

        // Use ruleKey from path, not body
        NotificationRuleRequest merged = new NotificationRuleRequest();
        merged.setRuleKey(ruleKey);
        merged.setDescription(req.getDescription());
        merged.setIsEnabled(req.getIsEnabled());
        merged.setTriggerCondition(req.getTriggerCondition());
        merged.setChannel(req.getChannel());
        merged.setTemplateTitle(req.getTemplateTitle());
        merged.setTemplateContent(req.getTemplateContent());

        setting.setSettingValue(buildJson(merged));
        setting.setUpdatedBy(admin);
        settingRepository.save(setting);

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .adminActor(admin)
                .action("NOTIFICATION_RULE_UPDATED")
                .targetTable("system_settings")
                .description("Updated rule: " + ruleKey)
                .build());

        log.info("[NotifRule] Admin {} updated rule {}", adminId, ruleKey);
        return parseRule(setting);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private SystemSetting findRuleOrThrow(String ruleKey) {
        return settingRepository.findBySettingGroupAndSettingKey(NOTIFICATION_GROUP, ruleKey)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc thông báo: " + ruleKey));
    }

    private AdminUser findAdminOrThrow(Long adminId) {
        return adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Admin"));
    }

    private String buildJson(NotificationRuleRequest req) {
        Map<String, Object> map = new HashMap<>();
        map.put("enabled", req.getIsEnabled());
        map.put("condition", req.getTriggerCondition());
        map.put("channel", req.getChannel());
        map.put("templateTitle", req.getTemplateTitle());
        map.put("templateContent", req.getTemplateContent());
        map.put("description", req.getDescription());
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "INTERNAL_ERROR", "Lỗi serialize JSON rule");
        }
    }

    @SuppressWarnings("unchecked")
    NotificationRuleResponse parseRule(SystemSetting setting) {
        try {
            Map<String, Object> json = objectMapper.readValue(setting.getSettingValue(), Map.class);
            return NotificationRuleResponse.builder()
                    .ruleKey(setting.getSettingKey())
                    .description((String) json.get("description"))
                    .isEnabled((Boolean) json.getOrDefault("enabled", false))
                    .triggerCondition((String) json.get("condition"))
                    .channel((String) json.get("channel"))
                    .templateTitle((String) json.get("templateTitle"))
                    .templateContent((String) json.get("templateContent"))
                    .updatedAt(setting.getUpdatedAt())
                    .updatedByAdminName(setting.getUpdatedBy() != null
                            ? setting.getUpdatedBy().getFullName() : null)
                    .build();
        } catch (Exception e) {
            log.warn("[NotifRule] Malformed JSON for rule key '{}': {}", setting.getSettingKey(), e.getMessage());
            return null;
        }
    }
}
