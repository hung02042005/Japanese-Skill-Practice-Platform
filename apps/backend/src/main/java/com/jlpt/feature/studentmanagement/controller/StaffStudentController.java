/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.studentmanagement.controller;

import com.jlpt.shared.common.ApiResponse;
import com.jlpt.feature.studentmanagement.dto.StudentDetailResponse;
import com.jlpt.feature.admin.dto.SuspendUserRequest;
import com.jlpt.feature.studentmanagement.dto.StudentProgressResponse;
import com.jlpt.feature.studentmanagement.dto.SubmissionSummaryResponse;
import com.jlpt.feature.studentmanagement.service.StudentManagementService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/students")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffStudentController {

    private final StudentManagementService studentManagementService;

    // ── UC-22: List students ─────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listStudents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StudentDetailResponse> result =
                studentManagementService.getStudentList(q, status, jlptLevel, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }

    // ── UC-21: Student detail ────────────────────────────────────────────────

    @GetMapping("/{studentId}")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getStudentDetail(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(
                studentManagementService.getStudentDetail(studentId)));
    }

    // ── UC-21: Student progress summary ─────────────────────────────────────

    @GetMapping("/{studentId}/progress")
    public ResponseEntity<ApiResponse<StudentProgressResponse>> getStudentProgress(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(
                studentManagementService.getStudentProgressSummary(studentId)));
    }

    // ── UC-21: Student submissions ───────────────────────────────────────────

    @GetMapping("/{studentId}/submissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentSubmissions(
            @PathVariable Long studentId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SubmissionSummaryResponse> result =
                studentManagementService.getStudentSubmissions(studentId, type, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }

    // ── UC-23: Suspend account ───────────────────────────────────────────────

    @PostMapping("/{studentId}/suspend")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> suspendStudent(
            Authentication auth,
            @PathVariable Long studentId,
            @Valid @RequestBody SuspendUserRequest request) {
        StudentDetailResponse result =
                studentManagementService.suspendStudent(auth.getName(), studentId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Tài khoản học viên đã bị đình chỉ", result));
    }

    // ── UC-23: Activate account ──────────────────────────────────────────────

    @PostMapping("/{studentId}/activate")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> activateStudent(
            Authentication auth,
            @PathVariable Long studentId) {
        StudentDetailResponse result =
                studentManagementService.activateStudent(auth.getName(), studentId);
        return ResponseEntity.ok(ApiResponse.success("Tài khoản học viên đã được kích hoạt lại", result));
    }
}
