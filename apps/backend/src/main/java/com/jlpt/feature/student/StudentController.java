/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.auth.AuthService;
import com.jlpt.feature.auth.dto.request.ChangePasswordRequest;
import com.jlpt.feature.student.dto.request.OnboardingRequest;
import com.jlpt.feature.student.dto.request.UpdateProfileRequest;
import com.jlpt.feature.student.dto.response.CourseListResponse;
import com.jlpt.feature.student.dto.response.DashboardResponse;
import com.jlpt.feature.student.dto.response.NextLessonResponse;
import com.jlpt.feature.student.dto.response.StudentResponse;
import com.jlpt.feature.student.dto.response.StudentStatsResponse;
import com.jlpt.feature.student.dto.response.VocabHomeResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final AuthService authService;
    private final VocabHomeService vocabHomeService;
    private final CourseService courseService;
    private final StudentDashboardService studentDashboardService;
    private final AvatarStorageService avatarStorageService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DashboardResponse response =
                studentDashboardService.getDashboard(userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<StudentStatsResponse>> getMyStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        StudentStatsResponse response =
                studentDashboardService.getStats(userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/next-lesson")
    public ResponseEntity<ApiResponse<NextLessonResponse>> getNextLesson(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NextLessonResponse response =
                studentDashboardService.getNextLesson(userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<StudentResponse>> submitOnboarding(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody OnboardingRequest request) {
        StudentResponse response =
                authService.submitOnboarding(userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã lưu mục tiêu học tập", response));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<StudentResponse>> uploadAvatar(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("avatar") MultipartFile avatar) {
        Long studentId = userDetails.getStudentUser().getId();
        String avatarUrl = avatarStorageService.store(avatar, studentId);
        StudentResponse response = authService.updateAvatar(studentId, avatarUrl);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh đại diện thành công", response));
    }

    @GetMapping("/vocab-home")
    public ResponseEntity<ApiResponse<VocabHomeResponse>> getVocabHome(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam(required = false) String level) {
        VocabHomeResponse response =
                vocabHomeService.getVocabHome(userDetails.getStudentUser().getId(), level);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<CourseListResponse>> getCourses(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CourseListResponse response =
                courseService.getCourses(userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

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
