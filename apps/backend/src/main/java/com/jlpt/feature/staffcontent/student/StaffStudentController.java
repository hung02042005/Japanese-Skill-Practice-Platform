/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.student;

import com.jlpt.feature.staffcontent.student.dto.StaffStudentListResponse;
import com.jlpt.feature.staffcontent.student.dto.StaffStudentProgressResponse;
import com.jlpt.feature.staffcontent.student.dto.StaffStudentSummaryResponse;
import com.jlpt.feature.staffcontent.student.dto.SuspendStudentRequest;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Staff — quản lý học viên (danh sách, tiến độ, tạm khoá/mở khoá). */
@RestController
@RequestMapping("/api/staff/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Validated
public class StaffStudentController {

    private final StaffStudentService staffStudentService;

    @GetMapping
    public ResponseEntity<ApiResponse<StaffStudentListResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size) {
        return ResponseEntity.ok(
                ApiResponse.success(staffStudentService.listStudents(search, level, status, page, size)));
    }

    @GetMapping("/{studentId}/progress")
    public ResponseEntity<ApiResponse<StaffStudentProgressResponse>> progress(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(staffStudentService.getProgress(studentId)));
    }

    @PostMapping("/{studentId}/suspend")
    public ResponseEntity<ApiResponse<StaffStudentSummaryResponse>> suspend(
            @PathVariable Long studentId,
            @Valid @RequestBody(required = false) SuspendStudentRequest request,
            Authentication authentication) {
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(ApiResponse.success(
                "Đã tạm khoá học viên", staffStudentService.suspend(authentication.getName(), studentId, reason)));
    }

    @PostMapping("/{studentId}/activate")
    public ResponseEntity<ApiResponse<StaffStudentSummaryResponse>> activate(
            @PathVariable Long studentId, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã mở khoá học viên", staffStudentService.activate(authentication.getName(), studentId)));
    }
}
