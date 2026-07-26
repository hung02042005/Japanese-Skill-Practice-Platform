/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.auth.dto.request.ConfirmEmailChangeRequest;
import com.jlpt.feature.auth.dto.request.RequestEmailChangeRequest;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.dto.request.OnboardingRequest;
import com.jlpt.feature.student.dto.request.UpdateProfileRequest;
import com.jlpt.feature.student.dto.response.StudentResponse;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.security.OtpVerificationService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests cho StudentProfileService.
 */
@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpVerificationService otpVerificationService;

    @Mock
    private StudentResponseMapper studentResponseMapper;

    @InjectMocks
    private StudentProfileService studentProfileService;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .email("student@test.com")
                .fullName("Original Name")
                .passwordHash("hashed_pass")
                .build();
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    void getProfile_notFound_throwsBusinessException() {
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> studentProfileService.getProfile(99L));
        assertEquals(404, ex.getStatus());
        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void getProfile_success_returnsMappedResponse() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        StudentResponse expectedResponse = StudentResponse.builder()
                .studentId(1L)
                .fullName("Original Name")
                .build();
        when(studentResponseMapper.toResponse(student)).thenReturn(expectedResponse);

        StudentResponse actual = studentProfileService.getProfile(1L);

        assertEquals(1L, actual.getStudentId());
        assertEquals("Original Name", actual.getFullName());
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    void updateProfile_notFound_throwsBusinessException() {
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateProfileRequest req = new UpdateProfileRequest();

        assertThrows(BusinessException.class, () -> studentProfileService.updateProfile(99L, req));
    }

    @Test
    void updateProfile_withAvatarAndTargetLevel_updatesUserFields() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentUserRepository.save(any())).thenReturn(student);
        when(studentResponseMapper.toResponse(student))
                .thenReturn(StudentResponse.builder().build());

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFullName("Updated Name");
        req.setPhone("0987654321");
        req.setAvatarUrl("http://avatar.url");
        req.setTargetJlptLevel("N3");

        studentProfileService.updateProfile(1L, req);

        assertEquals("Updated Name", student.getFullName());
        assertEquals("0987654321", student.getPhone());
        assertEquals("http://avatar.url", student.getAvatarUrl());
        assertEquals(StudentUser.JlptLevel.N3, student.getTargetJlptLevel());
        verify(studentUserRepository).save(student);
    }

    // ── submitOnboarding ──────────────────────────────────────────────────────

    @Test
    void submitOnboarding_success_setsTargetAndCurrentLevel() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentUserRepository.save(any())).thenReturn(student);
        when(studentResponseMapper.toResponse(student))
                .thenReturn(StudentResponse.builder().build());

        OnboardingRequest req = new OnboardingRequest();
        req.setJlptGoal("N2");

        studentProfileService.submitOnboarding(1L, req);

        assertEquals(StudentUser.JlptLevel.N2, student.getTargetJlptLevel());
        assertEquals(StudentUser.JlptLevel.N2, student.getCurrentJlptLevel());
        verify(studentUserRepository).save(student);
    }

    // ── updateAvatar ──────────────────────────────────────────────────────────

    @Test
    void updateAvatar_success_savesAvatarUrl() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentUserRepository.save(any())).thenReturn(student);
        when(studentResponseMapper.toResponse(student))
                .thenReturn(StudentResponse.builder().build());

        studentProfileService.updateAvatar(1L, "http://new-avatar.jpg");

        assertEquals("http://new-avatar.jpg", student.getAvatarUrl());
        verify(studentUserRepository).save(student);
    }

    // ── requestEmailChange ───────────────────────────────────────────────────

    @Test
    void requestEmailChange_wrongPassword_throwsBusinessException() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("wrongpass", "hashed_pass")).thenReturn(false);

        RequestEmailChangeRequest req = new RequestEmailChangeRequest();
        req.setCurrentPassword("wrongpass");
        req.setNewEmail("new@test.com");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> studentProfileService.requestEmailChange(1L, req));
        assertEquals(400, ex.getStatus());
        assertEquals("INVALID_PASSWORD", ex.getErrorCode());
    }

    @Test
    void requestEmailChange_sameEmail_throwsBusinessException() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("pass", "hashed_pass")).thenReturn(true);

        RequestEmailChangeRequest req = new RequestEmailChangeRequest();
        req.setCurrentPassword("pass");
        req.setNewEmail("student@test.com");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> studentProfileService.requestEmailChange(1L, req));
        assertEquals(400, ex.getStatus());
        assertEquals("SAME_EMAIL", ex.getErrorCode());
    }

    @Test
    void requestEmailChange_emailAlreadyExistsInStudents_throwsConflict() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("pass", "hashed_pass")).thenReturn(true);
        when(studentUserRepository.existsByEmail("exists@test.com")).thenReturn(true);

        RequestEmailChangeRequest req = new RequestEmailChangeRequest();
        req.setCurrentPassword("pass");
        req.setNewEmail("exists@test.com");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> studentProfileService.requestEmailChange(1L, req));
        assertEquals(409, ex.getStatus());
        assertEquals("EMAIL_EXISTS", ex.getErrorCode());
    }

    @Test
    void requestEmailChange_success_generatesAndSendsOtp() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("pass", "hashed_pass")).thenReturn(true);
        when(studentUserRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(staffUserRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(adminUserRepository.existsByEmail("new@test.com")).thenReturn(false);

        RequestEmailChangeRequest req = new RequestEmailChangeRequest();
        req.setCurrentPassword("pass");
        req.setNewEmail("new@test.com");

        studentProfileService.requestEmailChange(1L, req);

        verify(otpVerificationService).generateAndSend("new@test.com");
    }

    // ── confirmEmailChange ───────────────────────────────────────────────────

    @Test
    void confirmEmailChange_invalidOtp_throwsBusinessException() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(otpVerificationService.verify("new@test.com", "000000")).thenReturn(false);

        ConfirmEmailChangeRequest req = new ConfirmEmailChangeRequest();
        req.setNewEmail("new@test.com");
        req.setOtpCode("000000");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> studentProfileService.confirmEmailChange(1L, req));
        assertEquals(400, ex.getStatus());
        assertEquals("INVALID_OTP", ex.getErrorCode());
    }

    @Test
    void confirmEmailChange_emailExistsAtConfirmation_throwsConflict() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(otpVerificationService.verify("new@test.com", "123456")).thenReturn(true);
        when(studentUserRepository.existsByEmail("new@test.com")).thenReturn(true);

        ConfirmEmailChangeRequest req = new ConfirmEmailChangeRequest();
        req.setNewEmail("new@test.com");
        req.setOtpCode("123456");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> studentProfileService.confirmEmailChange(1L, req));
        assertEquals(409, ex.getStatus());
        assertEquals("EMAIL_EXISTS", ex.getErrorCode());
    }

    @Test
    void confirmEmailChange_success_updatesUserEmail() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(otpVerificationService.verify("new@test.com", "123456")).thenReturn(true);
        when(studentUserRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(studentUserRepository.save(any())).thenReturn(student);
        when(studentResponseMapper.toResponse(student))
                .thenReturn(StudentResponse.builder().build());

        ConfirmEmailChangeRequest req = new ConfirmEmailChangeRequest();
        req.setNewEmail("new@test.com");
        req.setOtpCode("123456");

        studentProfileService.confirmEmailChange(1L, req);

        assertEquals("new@test.com", student.getEmail());
        verify(studentUserRepository).save(student);
    }
}
