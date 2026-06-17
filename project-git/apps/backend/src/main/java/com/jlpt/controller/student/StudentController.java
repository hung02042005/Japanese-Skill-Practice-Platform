/* (c) JLPT E-Learning Platform */
package com.jlpt.controller.student;

import com.jlpt.common.ApiResponse;
import com.jlpt.dto.request.ChangePasswordRequest;
import com.jlpt.dto.request.UpdateProfileRequest;
import com.jlpt.dto.response.StudentResponse;
import com.jlpt.security.UserDetailsImpl;
import com.jlpt.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentResponse>> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        StudentResponse response =
                authService.getProfile(userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentResponse>> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody UpdateProfileRequest request) {
        StudentResponse response =
                authService.updateProfile(userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", response));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công.", null));
    }
}
