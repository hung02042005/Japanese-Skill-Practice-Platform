/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.admin.AdminUser;
import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.admin.SystemSetting;
import com.jlpt.feature.admin.SystemSettingRepository;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.shared.notification.dto.NotificationRuleRequest;
import com.jlpt.shared.notification.dto.NotificationRuleResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRuleServiceTest {

    @Mock
    private SystemSettingRepository settingRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificationRuleService service;

    private AdminUser admin;

    @BeforeEach
    void setUp() {
        admin = AdminUser.builder().id(1L).email("admin@example.com").build();
    }

    @Test
    void createRule_duplicateKey() {
        NotificationRuleRequest req = new NotificationRuleRequest();
        req.setRuleKey("rule_1");
        when(settingRepository.existsBySettingGroupAndSettingKey("notification", "rule_1"))
                .thenReturn(true);

        assertThrows(BusinessException.class, () -> service.createRule(req, 1L));
    }

    @Test
    void createRule_adminNotFound() {
        NotificationRuleRequest req = new NotificationRuleRequest();
        req.setRuleKey("rule_1");
        when(settingRepository.existsBySettingGroupAndSettingKey("notification", "rule_1"))
                .thenReturn(false);
        when(adminUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createRule(req, 1L));
    }

    @Test
    void createRule_success() {
        NotificationRuleRequest req = new NotificationRuleRequest();
        req.setRuleKey("rule_1");
        req.setDescription("Rule 1");
        req.setTriggerCondition("EVT");
        req.setChannel("email");
        req.setIsEnabled(true);

        when(settingRepository.existsBySettingGroupAndSettingKey("notification", "rule_1"))
                .thenReturn(false);
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(settingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        NotificationRuleResponse res = service.createRule(req, 1L);
        assertNotNull(res);
        assertEquals("rule_1", res.getRuleKey());
    }

    @Test
    void listRules_success() {
        SystemSetting setting = SystemSetting.builder()
                .settingGroup("notification")
                .settingKey("rule_1")
                .settingValue(
                        "{\"description\":\"Rule 1\",\"triggerCondition\":\"EVT\",\"channel\":\"email\",\"isEnabled\":true}")
                .build();
        when(settingRepository.findBySettingGroup("notification")).thenReturn(List.of(setting));

        List<NotificationRuleResponse> res = service.listRules();
        assertEquals(1, res.size());
        assertEquals("rule_1", res.get(0).getRuleKey());
    }
}
