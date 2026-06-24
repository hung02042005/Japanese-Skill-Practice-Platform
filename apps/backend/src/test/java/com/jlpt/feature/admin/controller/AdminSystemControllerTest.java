/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.admin.entity.AdminUser;
import com.jlpt.feature.admin.entity.SystemSetting;
import com.jlpt.feature.admin.repository.AdminUserRepository;
import com.jlpt.feature.admin.repository.SystemSettingRepository;
import com.jlpt.feature.staff.entity.StaffUser;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.shared.security.JwtProvider;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private AdminAuditLogRepository adminAuditLogRepository;

    private AdminUser admin;
    private String adminToken;
    private String staffToken;

    @BeforeEach
    void setUp() {
        adminAuditLogRepository.deleteAll();
        systemSettingRepository.deleteAll();
        staffUserRepository.deleteAll();
        adminUserRepository.deleteAll();

        admin = adminUserRepository.save(AdminUser.builder()
                .email("admin-system-test@jlpt.com")
                .fullName("Admin System Test")
                .status(AdminUser.AdminStatus.ACTIVE)
                .build());
        StaffUser staff = staffUserRepository.save(StaffUser.builder()
                .email("staff-system-test@jlpt.com")
                .fullName("Staff System Test")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build());

        adminToken = jwtProvider.generateAdminAccessToken(admin.getId(), admin.getEmail());
        staffToken = jwtProvider.generateStaffAccessToken(staff.getId(), staff.getEmail());

        systemSettingRepository.save(SystemSetting.builder()
                .settingGroup("smtp")
                .settingKey("smtp_password")
                .settingValue("real-smtp-secret")
                .valueType(SystemSetting.ValueType.STRING)
                .isEditable(true)
                .build());
        systemSettingRepository.save(SystemSetting.builder()
                .settingGroup("system")
                .settingKey("maintenance_mode")
                .settingValue("false")
                .valueType(SystemSetting.ValueType.BOOLEAN)
                .isEditable(true)
                .build());
        systemSettingRepository.save(SystemSetting.builder()
                .settingGroup("system")
                .settingKey("platform_name")
                .settingValue("JLPT App")
                .valueType(SystemSetting.ValueType.STRING)
                .isEditable(false)
                .build());
    }

    @Test
    void getSmtpSettings_masksPassword() throws Exception {
        mockMvc.perform(get("/api/admin/settings/smtp")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].settingKey").value("smtp_password"))
                .andExpect(jsonPath("$.data[0].settingValue").value("*****"));
    }

    @Test
    void updateMaintenanceMode_withInvalidBoolean_returns400() throws Exception {
        mockMvc.perform(put("/api/admin/settings/system/maintenance_mode")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"yes\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReadOnlySetting_returns422() throws Exception {
        mockMvc.perform(put("/api/admin/settings/system/platform_name")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"New Name\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void deleteNotificationRule_returns200AndRecordStillExists() throws Exception {
        String createBody = """
                {
                  "ruleKey": "streak_10_days",
                  "description": "Chuc mung streak 10 ngay",
                  "isEnabled": true,
                  "triggerCondition": "streak_10",
                  "channel": "in_app",
                  "templateTitle": "Chuc mung!",
                  "templateContent": "Ban da hoc 10 ngay lien tuc."
                }
                """;
        mockMvc.perform(post("/api/admin/notification-rules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/admin/notification-rules/streak_10_days")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Optional<SystemSetting> stillThere =
                systemSettingRepository.findBySettingGroupAndSettingKey("notification", "streak_10_days");
        org.junit.jupiter.api.Assertions.assertTrue(stillThere.isPresent());
        org.junit.jupiter.api.Assertions.assertTrue(
                stillThere.get().getSettingValue().contains("\"enabled\":false"));
    }

    @Test
    void staffToken_callingAdminSettings_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/settings/smtp")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_callingAdminSettings_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/settings/smtp"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSelfModification_suspendOwnAccount_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/users/admin/" + admin.getId() + "/suspend")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Testing self modification guard\"}"))
                .andExpect(status().isForbidden());
    }
}
