/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.dto.response.SettingResponse;
import com.jlpt.shared.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Unit tests cho AdminSettingsService — quản lý cài đặt hệ thống với logic mask password.
 */
@ExtendWith(MockitoExtension.class)
class AdminSettingsServiceTest {

    @Mock
    private SystemSettingRepository settingRepository;

    @Mock
    private JavaMailSenderImpl mailSender;

    @InjectMocks
    private AdminSettingsService adminSettingsService;

    // ── validateGroup ────────────────────────────────────────────────────────

    @Test
    void getByGroup_invalidGroup_throwsBusinessException() {
        BusinessException ex =
                assertThrows(BusinessException.class, () -> adminSettingsService.getByGroup("invalid_group"));

        assertEquals(400, ex.getStatus());
        assertEquals("INVALID_SETTING_GROUP", ex.getErrorCode());
    }

    @Test
    void getByGroup_validGroup_delegatesToRepository() {
        when(settingRepository.findBySettingGroup("general")).thenReturn(List.of());

        List<SettingResponse> result = adminSettingsService.getByGroup("general");

        assertTrue(result.isEmpty());
        verify(settingRepository).findBySettingGroup("general");
    }

    // ── password masking ─────────────────────────────────────────────────────

    @Test
    void getByGroup_passwordKeyWithValue_masksWith8Stars() {
        SystemSetting passwordSetting = SystemSetting.builder()
                .settingKey("smtp_password")
                .settingValue("secret123")
                .valueType(SystemSetting.ValueType.STRING)
                .build();
        when(settingRepository.findBySettingGroup("smtp")).thenReturn(List.of(passwordSetting));

        List<SettingResponse> result = adminSettingsService.getByGroup("smtp");

        assertEquals("********", result.get(0).getSettingValue());
    }

    @Test
    void getByGroup_passwordKeyWithBlankValue_returnsEmptyString() {
        SystemSetting passwordSetting = SystemSetting.builder()
                .settingKey("smtp_password")
                .settingValue("")
                .valueType(SystemSetting.ValueType.STRING)
                .build();
        when(settingRepository.findBySettingGroup("smtp")).thenReturn(List.of(passwordSetting));

        List<SettingResponse> result = adminSettingsService.getByGroup("smtp");

        assertEquals("", result.get(0).getSettingValue());
    }

    @Test
    void getByGroup_nonPasswordKey_returnsPlainValue() {
        SystemSetting setting = SystemSetting.builder()
                .settingKey("smtp_host")
                .settingValue("smtp.example.com")
                .valueType(SystemSetting.ValueType.STRING)
                .build();
        when(settingRepository.findBySettingGroup("smtp")).thenReturn(List.of(setting));

        List<SettingResponse> result = adminSettingsService.getByGroup("smtp");

        assertEquals("smtp.example.com", result.get(0).getSettingValue());
    }

    // ── updateSetting ────────────────────────────────────────────────────────

    @Test
    void updateSetting_invalidGroup_throwsBusinessException() {
        assertThrows(BusinessException.class, () -> adminSettingsService.updateSetting("hacker", "key", "value"));
    }

    @Test
    void updateSetting_lockedSetting_throwsBusinessException() {
        SystemSetting locked = SystemSetting.builder()
                .settingKey("site_name")
                .settingValue("old")
                .isEditable(false)
                .build();
        when(settingRepository.findBySettingGroupAndSettingKey("general", "site_name"))
                .thenReturn(Optional.of(locked));

        BusinessException ex = assertThrows(
                BusinessException.class, () -> adminSettingsService.updateSetting("general", "site_name", "new"));

        assertEquals(403, ex.getStatus());
        assertEquals("SETTING_LOCKED", ex.getErrorCode());
    }

    @Test
    void updateSetting_validNonPassword_savesAndReturnsResponse() {
        SystemSetting existing = SystemSetting.builder()
                .settingKey("site_name")
                .settingValue("old")
                .isEditable(true)
                .valueType(SystemSetting.ValueType.STRING)
                .build();
        when(settingRepository.findBySettingGroupAndSettingKey("general", "site_name"))
                .thenReturn(Optional.of(existing));
        when(settingRepository.save(any(SystemSetting.class))).thenAnswer(i -> i.getArgument(0));

        SettingResponse response = adminSettingsService.updateSetting("general", "site_name", "JLPT System");

        assertEquals("site_name", response.getSettingKey());
        assertEquals("JLPT System", response.getSettingValue());
        verify(settingRepository).save(any(SystemSetting.class));
    }

    // ── updateSettings (batch) ───────────────────────────────────────────────

    @Test
    void updateSettings_passwordKeyWithMaskPlaceholder_skipsUpdate() {
        var item = new com.jlpt.feature.admin.dto.request.UpdateSettingsBatchRequest.Item();
        item.setSettingKey("smtp_password");
        item.setSettingValue("********");

        List<SettingResponse> result = adminSettingsService.updateSettings("smtp", List.of(item));

        assertTrue(result.isEmpty(), "Password with mask placeholder should be skipped");
        verify(settingRepository, never()).save(any());
    }

    @Test
    void updateSettings_passwordKeyBlank_skipsUpdate() {
        var item = new com.jlpt.feature.admin.dto.request.UpdateSettingsBatchRequest.Item();
        item.setSettingKey("smtp_password");
        item.setSettingValue("");

        List<SettingResponse> result = adminSettingsService.updateSettings("smtp", List.of(item));

        assertTrue(result.isEmpty());
    }

    // ── isPassword helper (via behavior) ─────────────────────────────────────

    @Test
    void getByGroup_keyContainingPasswordCaseInsensitive_masksValue() {
        SystemSetting setting = SystemSetting.builder()
                .settingKey("admin_Password_key")
                .settingValue("secret")
                .valueType(SystemSetting.ValueType.STRING)
                .build();
        when(settingRepository.findBySettingGroup("security")).thenReturn(List.of(setting));

        List<SettingResponse> result = adminSettingsService.getByGroup("security");

        assertEquals("********", result.get(0).getSettingValue());
    }
}
