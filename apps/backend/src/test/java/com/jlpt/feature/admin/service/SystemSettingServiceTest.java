/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.dto.SystemSettingRequest;
import com.jlpt.feature.admin.dto.SystemSettingResponse;
import com.jlpt.feature.admin.entity.AdminUser;
import com.jlpt.feature.admin.entity.SystemSetting;
import com.jlpt.feature.admin.repository.AdminUserRepository;
import com.jlpt.feature.admin.repository.SystemSettingRepository;
import com.jlpt.shared.audit.AdminAuditLog;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.shared.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemSettingServiceTest {

    @Mock
    private SystemSettingRepository settingRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @InjectMocks
    private SystemSettingService systemSettingService;

    private AdminUser admin;

    @BeforeEach
    void setUp() {
        admin = AdminUser.builder()
                .id(1L)
                .email("admin@jlpt.com")
                .fullName("Admin JLPT")
                .status(AdminUser.AdminStatus.ACTIVE)
                .build();
    }

    private SystemSetting setting(String group, String key, String value, SystemSetting.ValueType type, boolean editable) {
        return SystemSetting.builder()
                .id(1)
                .settingGroup(group)
                .settingKey(key)
                .settingValue(value)
                .valueType(type)
                .isEditable(editable)
                .build();
    }

    @Test
    void getSettingsByGroup_masksSensitiveValues() {
        SystemSetting smtpPassword = setting("smtp", "smtp_password", "s3cr3t", SystemSetting.ValueType.STRING, true);
        when(settingRepository.findAllBySettingGroup("smtp")).thenReturn(List.of(smtpPassword));

        List<SystemSettingResponse> result = systemSettingService.getSettingsByGroup("smtp");

        assertEquals(1, result.size());
        assertEquals("*****", result.get(0).getSettingValue());
    }

    @Test
    void updateSetting_withBooleanType_acceptsTrueAndFalse() {
        SystemSetting maintenance = setting("system", "maintenance_mode", "false", SystemSetting.ValueType.BOOLEAN, true);
        when(settingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.of(maintenance));
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(admin));

        SystemSettingRequest req = new SystemSettingRequest();
        req.setValue("true");

        SystemSettingResponse response = systemSettingService.updateSetting("system", "maintenance_mode", req, 1L);

        assertEquals("true", response.getSettingValue());
        verify(settingRepository).save(maintenance);
    }

    @Test
    void updateSetting_withBooleanType_rejectsYesNo() {
        SystemSetting maintenance = setting("system", "maintenance_mode", "false", SystemSetting.ValueType.BOOLEAN, true);
        when(settingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.of(maintenance));

        SystemSettingRequest req = new SystemSettingRequest();
        req.setValue("yes");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> systemSettingService.updateSetting("system", "maintenance_mode", req, 1L));
        assertEquals(400, ex.getStatus());
        assertEquals("VALIDATION_FAILED", ex.getErrorCode());
    }

    @Test
    void updateSetting_withIntegerType_rejectsAbc() {
        SystemSetting jwtExpiry = setting("security", "jwt_expiry_minutes", "15", SystemSetting.ValueType.INTEGER, true);
        when(settingRepository.findBySettingGroupAndSettingKey("security", "jwt_expiry_minutes"))
                .thenReturn(Optional.of(jwtExpiry));

        SystemSettingRequest req = new SystemSettingRequest();
        req.setValue("abc");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> systemSettingService.updateSetting("security", "jwt_expiry_minutes", req, 1L));
        assertEquals(400, ex.getStatus());
        assertEquals("VALIDATION_FAILED", ex.getErrorCode());
    }

    @Test
    void updateSetting_withTimeType_accepts2359() {
        SystemSetting quietHour = setting("system", "quiet_hour", "08:00", SystemSetting.ValueType.TIME, true);
        when(settingRepository.findBySettingGroupAndSettingKey("system", "quiet_hour"))
                .thenReturn(Optional.of(quietHour));
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(admin));

        SystemSettingRequest req = new SystemSettingRequest();
        req.setValue("23:59");

        SystemSettingResponse response = systemSettingService.updateSetting("system", "quiet_hour", req, 1L);

        assertEquals("23:59", response.getSettingValue());
    }

    @Test
    void updateSetting_withTimeType_rejects2599() {
        SystemSetting quietHour = setting("system", "quiet_hour", "08:00", SystemSetting.ValueType.TIME, true);
        when(settingRepository.findBySettingGroupAndSettingKey("system", "quiet_hour"))
                .thenReturn(Optional.of(quietHour));

        SystemSettingRequest req = new SystemSettingRequest();
        req.setValue("25:99");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> systemSettingService.updateSetting("system", "quiet_hour", req, 1L));
        assertEquals(400, ex.getStatus());
        assertEquals("VALIDATION_FAILED", ex.getErrorCode());
    }

    @Test
    void updateSetting_withReadOnlySetting_throws422() {
        SystemSetting readOnly = setting("system", "platform_name", "JLPT App", SystemSetting.ValueType.STRING, false);
        when(settingRepository.findBySettingGroupAndSettingKey("system", "platform_name"))
                .thenReturn(Optional.of(readOnly));

        SystemSettingRequest req = new SystemSettingRequest();
        req.setValue("New Name");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> systemSettingService.updateSetting("system", "platform_name", req, 1L));
        assertEquals(422, ex.getStatus());
        assertEquals("SETTING_READ_ONLY", ex.getErrorCode());
        verify(settingRepository, never()).save(any());
    }

    @Test
    void updateSetting_writesAuditLog() {
        SystemSetting maintenance = setting("system", "maintenance_mode", "false", SystemSetting.ValueType.BOOLEAN, true);
        when(settingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.of(maintenance));
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(admin));

        SystemSettingRequest req = new SystemSettingRequest();
        req.setValue("true");

        systemSettingService.updateSetting("system", "maintenance_mode", req, 1L);

        verify(adminAuditLogRepository).save(argThat((AdminAuditLog log) ->
                "SETTING_UPDATED".equals(log.getAction())
                        && "system_settings".equals(log.getTargetTable())
                        && log.getDescription().contains("system/maintenance_mode")));
    }

    @Test
    void isMaintenanceMode_returnsTrueWhenEnabled() {
        SystemSetting maintenance = setting("system", "maintenance_mode", "true", SystemSetting.ValueType.BOOLEAN, true);
        when(settingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.of(maintenance));

        assertTrue(systemSettingService.isMaintenanceMode());
    }
}
