/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.SystemSettingRequest;
import com.jlpt.feature.admin.dto.SystemSettingResponse;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin — System Settings bản đầy đủ (UC-39). */
@RestController
@RequestMapping("/api/admin/system-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemSettingController {

    private final AdminSystemSettingService adminSystemSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> list(
            @RequestParam(required = false) String group) {
        List<SystemSettingResponse> data = (group == null || group.isBlank())
                ? adminSystemSettingService.listAll()
                : adminSystemSettingService.listByGroup(group);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping("/{settingId}")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> update(
            Authentication authentication,
            @PathVariable Integer settingId,
            @Valid @RequestBody SystemSettingRequest req) {
        SystemSettingResponse data =
                adminSystemSettingService.updateSetting(settingId, authentication.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật setting", data));
    }
}
