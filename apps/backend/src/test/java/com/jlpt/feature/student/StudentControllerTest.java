/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.auth.PasswordResetService;
import com.jlpt.feature.auth.StudentProfileService;
import com.jlpt.feature.auth.dto.request.ChangePasswordRequest;
import com.jlpt.feature.auth.dto.request.ConfirmEmailChangeRequest;
import com.jlpt.feature.auth.dto.request.RequestEmailChangeRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * StudentController chỉ điều phối: mọi endpoint phải lấy studentId từ principal (không tin id do
 * client gửi) và chuyển tiếp xuống đúng service.
 */
@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private static final Long STUDENT_ID = 1L;

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private VocabHomeService vocabHomeService;

    @Mock
    private CourseService courseService;

    @Mock
    private StudentDashboardService studentDashboardService;

    @Mock
    private AvatarStorageService avatarStorageService;

    @InjectMocks
    private StudentController controller;

    private UserDetailsImpl principal;

    @BeforeEach
    void setUp() {
        StudentUser student = StudentUser.builder()
                .id(STUDENT_ID)
                .email("student@sakuji.com")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build();
        principal = new UserDetailsImpl(student);
    }

    @Test
    void getDashboard_usesStudentIdFromPrincipal() {
        DashboardResponse data = mock(DashboardResponse.class);
        when(studentDashboardService.getDashboard(STUDENT_ID)).thenReturn(data);

        ResponseEntity<ApiResponse<DashboardResponse>> response = controller.getDashboard(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(data, response.getBody().getData());
    }

    @Test
    void getMyStats_delegates() {
        StudentStatsResponse data = mock(StudentStatsResponse.class);
        when(studentDashboardService.getStats(STUDENT_ID)).thenReturn(data);

        assertSame(data, controller.getMyStats(principal).getBody().getData());
    }

    @Test
    void getNextLesson_delegates() {
        NextLessonResponse data = mock(NextLessonResponse.class);
        when(studentDashboardService.getNextLesson(STUDENT_ID)).thenReturn(data);

        assertSame(data, controller.getNextLesson(principal).getBody().getData());
    }

    @Test
    void submitOnboarding_delegatesAndReturnsMessage() {
        OnboardingRequest request = new OnboardingRequest();
        StudentResponse data = mock(StudentResponse.class);
        when(studentProfileService.submitOnboarding(STUDENT_ID, request)).thenReturn(data);

        ResponseEntity<ApiResponse<StudentResponse>> response = controller.submitOnboarding(principal, request);

        assertEquals("Đã lưu mục tiêu học tập", response.getBody().getMessage());
        assertSame(data, response.getBody().getData());
    }

    @Test
    void uploadAvatar_storesFileThenSavesReturnedUrl() {
        MultipartFile avatar = new MockMultipartFile("avatar", "me.png", "image/png", new byte[] {1, 2, 3});
        StudentResponse data = mock(StudentResponse.class);
        when(avatarStorageService.store(avatar, STUDENT_ID)).thenReturn("/api/files/avatar/1.png");
        when(studentProfileService.updateAvatar(STUDENT_ID, "/api/files/avatar/1.png"))
                .thenReturn(data);

        ResponseEntity<ApiResponse<StudentResponse>> response = controller.uploadAvatar(principal, avatar);

        assertEquals("Cập nhật ảnh đại diện thành công", response.getBody().getMessage());
        assertSame(data, response.getBody().getData());
        verify(avatarStorageService).store(avatar, STUDENT_ID);
    }

    @Test
    void getVocabHome_passesLevelFilterThrough() {
        VocabHomeResponse data = mock(VocabHomeResponse.class);
        when(vocabHomeService.getVocabHome(STUDENT_ID, "N5")).thenReturn(data);

        assertSame(data, controller.getVocabHome(principal, "N5").getBody().getData());
    }

    @Test
    void getVocabHome_withoutLevel_passesNull() {
        VocabHomeResponse data = mock(VocabHomeResponse.class);
        when(vocabHomeService.getVocabHome(STUDENT_ID, null)).thenReturn(data);

        assertSame(data, controller.getVocabHome(principal, null).getBody().getData());
    }

    @Test
    void getCourses_delegates() {
        CourseListResponse data = mock(CourseListResponse.class);
        when(courseService.getCourses(STUDENT_ID)).thenReturn(data);

        assertSame(data, controller.getCourses(principal).getBody().getData());
    }

    @Test
    void getProfile_delegates() {
        StudentResponse data = mock(StudentResponse.class);
        when(studentProfileService.getProfile(STUDENT_ID)).thenReturn(data);

        assertSame(data, controller.getProfile(principal).getBody().getData());
    }

    @Test
    void updateProfile_delegates() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        StudentResponse data = mock(StudentResponse.class);
        when(studentProfileService.updateProfile(STUDENT_ID, request)).thenReturn(data);

        ResponseEntity<ApiResponse<StudentResponse>> response = controller.updateProfile(principal, request);

        assertEquals("Cập nhật hồ sơ thành công", response.getBody().getMessage());
    }

    @Test
    void changePassword_returnsSuccessWithoutData() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        ResponseEntity<ApiResponse<Void>> response = controller.changePassword(principal, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getData());
        verify(passwordResetService).changePassword(STUDENT_ID, request);
    }

    @Test
    void requestEmailChange_returnsSuccessWithoutData() {
        RequestEmailChangeRequest request = new RequestEmailChangeRequest();

        ResponseEntity<ApiResponse<Void>> response = controller.requestEmailChange(principal, request);

        assertEquals("Đã gửi mã OTP đến email mới.", response.getBody().getMessage());
        verify(studentProfileService).requestEmailChange(STUDENT_ID, request);
    }

    @Test
    void confirmEmailChange_delegates() {
        ConfirmEmailChangeRequest request = new ConfirmEmailChangeRequest();
        StudentResponse data = mock(StudentResponse.class);
        when(studentProfileService.confirmEmailChange(STUDENT_ID, request)).thenReturn(data);

        ResponseEntity<ApiResponse<StudentResponse>> response = controller.confirmEmailChange(principal, request);

        assertEquals("Đổi email thành công.", response.getBody().getMessage());
        assertSame(data, response.getBody().getData());
    }
}
