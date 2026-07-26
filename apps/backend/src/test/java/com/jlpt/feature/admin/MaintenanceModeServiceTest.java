/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho MaintenanceModeService — đọc cờ bảo trì từ system settings.
 */
@ExtendWith(MockitoExtension.class)
class MaintenanceModeServiceTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @InjectMocks
    private MaintenanceModeService maintenanceModeService;

    @Test
    void isEnabled_settingValueTrue_returnsTrue() {
        SystemSetting setting = SystemSetting.builder()
                .settingGroup("system")
                .settingKey("maintenance_mode")
                .settingValue("true")
                .build();
        when(systemSettingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.of(setting));

        assertTrue(maintenanceModeService.isEnabled());
    }

    @Test
    void isEnabled_settingValueTrueUpperCase_returnsTrue() {
        SystemSetting setting = SystemSetting.builder()
                .settingGroup("system")
                .settingKey("maintenance_mode")
                .settingValue("TRUE")
                .build();
        when(systemSettingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.of(setting));

        assertTrue(maintenanceModeService.isEnabled());
    }

    @Test
    void isEnabled_settingValueFalse_returnsFalse() {
        SystemSetting setting = SystemSetting.builder()
                .settingGroup("system")
                .settingKey("maintenance_mode")
                .settingValue("false")
                .build();
        when(systemSettingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.of(setting));

        assertFalse(maintenanceModeService.isEnabled());
    }

    @Test
    void isEnabled_settingNotFound_returnsFalse() {
        when(systemSettingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.empty());

        assertFalse(maintenanceModeService.isEnabled());
    }

    @Test
    void isEnabled_settingValueArbitraryString_returnsFalse() {
        SystemSetting setting = SystemSetting.builder()
                .settingGroup("system")
                .settingKey("maintenance_mode")
                .settingValue("yes")
                .build();
        when(systemSettingRepository.findBySettingGroupAndSettingKey("system", "maintenance_mode"))
                .thenReturn(Optional.of(setting));

        assertFalse(maintenanceModeService.isEnabled());
    }
}
