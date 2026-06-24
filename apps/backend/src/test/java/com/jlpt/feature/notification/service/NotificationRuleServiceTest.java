/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.admin.entity.AdminUser;
import com.jlpt.feature.admin.entity.SystemSetting;
import com.jlpt.feature.admin.repository.AdminUserRepository;
import com.jlpt.feature.admin.repository.SystemSettingRepository;
import com.jlpt.feature.notification.dto.NotificationRuleRequest;
import com.jlpt.feature.notification.dto.NotificationRuleResponse;
import com.jlpt.feature.notification.repository.NotificationRepository;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.feature.student.repository.StudentUserRepository;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.shared.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRuleServiceTest {

    @Mock
    private SystemSettingRepository settingRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    private NotificationRuleService notificationRuleService;

    private AdminUser admin;

    @BeforeEach
    void setUp() {
        // @InjectMocks can't wire a plain (non-@Mock) ObjectMapper, so build the service manually
        // to get a real Jackson instance for genuine JSON (de)serialization.
        notificationRuleService = new NotificationRuleService(
                settingRepository,
                adminUserRepository,
                adminAuditLogRepository,
                notificationRepository,
                studentUserRepository,
                new ObjectMapper());

        admin = AdminUser.builder()
                .id(1L)
                .email("admin@jlpt.com")
                .fullName("Admin JLPT")
                .status(AdminUser.AdminStatus.ACTIVE)
                .build();
    }

    private NotificationRuleRequest request(String ruleKey) {
        NotificationRuleRequest req = new NotificationRuleRequest();
        req.setRuleKey(ruleKey);
        req.setDescription("Chuc mung dat moc " + ruleKey);
        req.setIsEnabled(true);
        req.setTriggerCondition("streak_10");
        req.setChannel("in_app");
        req.setTemplateTitle("Chuc mung!");
        req.setTemplateContent("Ban da hoc lien tuc 10 ngay.");
        return req;
    }

    @Test
    void createRule_withNewKey_savesAsSystemSetting() {
        when(settingRepository.existsBySettingGroupAndSettingKey("notification", "streak_10_days")).thenReturn(false);
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(settingRepository.save(any(SystemSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationRuleResponse response = notificationRuleService.createRule(request("streak_10_days"), 1L);

        assertEquals("streak_10_days", response.getRuleKey());
        assertTrue(response.getIsEnabled());
        verify(settingRepository).save(argThat((SystemSetting s) ->
                "notification".equals(s.getSettingGroup())
                        && "streak_10_days".equals(s.getSettingKey())
                        && s.getSettingValue().contains("\"enabled\":true")));
        verify(adminAuditLogRepository).save(any());
    }

    @Test
    void createRule_withDuplicateKey_throws400() {
        when(settingRepository.existsBySettingGroupAndSettingKey("notification", "streak_10_days")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> notificationRuleService.createRule(request("streak_10_days"), 1L));
        assertEquals(400, ex.getStatus());
        assertEquals("DUPLICATE_RULE_KEY", ex.getErrorCode());
        verify(settingRepository, never()).save(any());
    }

    @Test
    void deleteRule_setsEnabledFalseInJson_doesNotDeleteRecord() {
        SystemSetting setting = SystemSetting.builder()
                .id(1)
                .settingGroup("notification")
                .settingKey("streak_10_days")
                .settingValue("{\"enabled\":true,\"condition\":\"streak_10\"}")
                .valueType(SystemSetting.ValueType.STRING)
                .isEditable(true)
                .build();
        when(settingRepository.findBySettingGroupAndSettingKey("notification", "streak_10_days"))
                .thenReturn(Optional.of(setting));
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(admin));

        notificationRuleService.deleteRule("streak_10_days", 1L);

        verify(settingRepository).save(argThat((SystemSetting s) -> s.getSettingValue().contains("\"enabled\":false")));
        verify(settingRepository, never()).delete(any());
        verify(settingRepository, never()).deleteById(any());
    }

    @Test
    void listRules_handlesJsonMalformed_withoutException() {
        SystemSetting malformed = SystemSetting.builder()
                .id(1)
                .settingGroup("notification")
                .settingKey("broken_rule")
                .settingValue("{not-valid-json")
                .valueType(SystemSetting.ValueType.STRING)
                .isEditable(true)
                .build();
        when(settingRepository.findAllBySettingGroup("notification")).thenReturn(List.of(malformed));

        List<NotificationRuleResponse> result = assertDoesNotThrow(notificationRuleService::listRules);

        assertTrue(result.isEmpty());
    }

    @Test
    void triggerMilestone_alreadySentIn24h_skips() {
        SystemSetting rule = SystemSetting.builder()
                .id(1)
                .settingGroup("notification")
                .settingKey("milestone_streak_10")
                .settingValue("{\"enabled\":true,\"channel\":\"in_app\"}")
                .valueType(SystemSetting.ValueType.STRING)
                .isEditable(true)
                .build();
        when(settingRepository.findBySettingGroupAndSettingKey("notification", "milestone_streak_10"))
                .thenReturn(Optional.of(rule));
        when(notificationRepository.existsByStudentIdAndRuleKeyAndCreatedAtAfter(
                eq(5L), eq("milestone_streak_10"), any(LocalDateTime.class)))
                .thenReturn(true);

        notificationRuleService.triggerMilestone(5L, "streak_10");

        verify(notificationRepository, never()).save(any());
        verify(studentUserRepository, never()).findById(any());
    }
}
