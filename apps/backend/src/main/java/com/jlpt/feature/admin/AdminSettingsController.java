/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.request.UpdateSettingRequest;
import com.jlpt.feature.admin.dto.request.UpdateSettingsBatchRequest;
import com.jlpt.feature.admin.dto.response.SettingResponse;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final AdminSettingsService settingsService;

    /** GET /api/admin/settings/{group} — lấy tất cả setting của một nhóm. */
    @GetMapping("/{group}")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getByGroup(@PathVariable String group) {
        List<SettingResponse> data = settingsService.getByGroup(group);
        return ResponseEntity.ok(ApiResponse.success("Lấy cài đặt thành công", data));
    }

    /** PUT /api/admin/settings/{group}/{key} — cập nhật giá trị một setting. */
    @PutMapping("/{group}/{key}")
    public ResponseEntity<ApiResponse<SettingResponse>> updateSetting(
            @PathVariable String group, @PathVariable String key, @Valid @RequestBody UpdateSettingRequest request) {
        SettingResponse data = settingsService.updateSetting(group, key, request.getSettingValue());
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật cài đặt thành công", data));
    }

    /** PUT /api/admin/settings/{group} — cập nhật nhiều setting cùng nhóm trong 1 request. */
    @PutMapping("/{group}")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> updateSettings(
            @PathVariable String group, @Valid @RequestBody UpdateSettingsBatchRequest request) {
        List<SettingResponse> data = settingsService.updateSettings(group, request.getSettings());
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật cài đặt thành công", data));
    }

    /** POST /api/admin/settings/smtp/test — kiểm tra kết nối SMTP. */
    @PostMapping("/smtp/test")
    public ResponseEntity<ApiResponse<Void>> testSmtp() {
        settingsService.testSmtpConnection();
        return ResponseEntity.ok(ApiResponse.success("Kết nối SMTP thành công", null));
    }
}
