/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.jlpt.feature.admin.AdminAuthService;
import com.jlpt.feature.admin.AdminUser;
import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.admin.MaintenanceModeService;
import com.jlpt.feature.auth.dto.request.GoogleTokenRequest;
import com.jlpt.feature.auth.dto.request.LoginRequest;
import com.jlpt.feature.auth.dto.request.LogoutRequest;
import com.jlpt.feature.auth.dto.request.RefreshTokenRequest;
import com.jlpt.feature.auth.dto.response.AccountTypeResponse;
import com.jlpt.feature.auth.dto.response.AuthResponse;
import com.jlpt.feature.auth.dto.response.LoginApiResponse;
import com.jlpt.feature.auth.dto.response.RefreshTokenResponse;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.security.JwtProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Bổ sung độ phủ cho AuthenticationService: checkAccountType + rate limit, định tuyến login theo
 * admin/staff/student, khoá tài khoản staff, refresh token theo từng actor, logout và Google login.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceCoverageTest {

    private static final String STAFF_EMAIL = "staff@sakuji.com";
    private static final String STUDENT_EMAIL = "student@sakuji.com";
    private static final String ADMIN_EMAIL = "admin@sakuji.com";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminAuthService adminAuthService;

    @Mock
    private MaintenanceModeService maintenanceModeService;

    @Mock
    private StudentResponseMapper studentResponseMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "googleClientId", "test-client-id");
        ReflectionTestUtils.setField(authenticationService, "accessExpirationMs", 900_000L);
        ReflectionTestUtils.setField(authenticationService, "refreshExpirationMs", 604_800_000L);

        staff = StaffUser.builder()
                .id(2L)
                .email(STAFF_EMAIL)
                .passwordHash("hashed")
                .fullName("Staff One")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .loginAttempts(0)
                .mustChangePassword(false)
                .build();
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    // ── checkAccountType ─────────────────────────────────────────────────────

    @Test
    void checkAccountType_staffEmail_returnsStaff() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        AccountTypeResponse response = authenticationService.checkAccountType("  Staff@Sakuji.com  ");

        assertEquals("staff", response.getAccountType());
        verify(studentUserRepository, never()).findByEmail(any());
    }

    @Test
    void checkAccountType_studentEmail_returnsStudent() {
        when(staffUserRepository.findByEmail(STUDENT_EMAIL)).thenReturn(Optional.empty());
        when(studentUserRepository.findByEmail(STUDENT_EMAIL)).thenReturn(Optional.of(new StudentUser()));

        assertEquals(
                "student", authenticationService.checkAccountType(STUDENT_EMAIL).getAccountType());
    }

    @Test
    void checkAccountType_unknownEmail_returnsUnknown() {
        when(staffUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());
        when(studentUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());

        assertEquals(
                "unknown",
                authenticationService.checkAccountType("ghost@sakuji.com").getAccountType());
    }

    @Test
    void checkAccountType_withIp_allowsTenCallsThenRateLimits() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        for (int i = 0; i < 10; i++) {
            assertEquals(
                    "staff",
                    authenticationService
                            .checkAccountType(STAFF_EMAIL, "10.0.0.1")
                            .getAccountType());
        }

        BusinessException ex = assertThrows(
                BusinessException.class, () -> authenticationService.checkAccountType(STAFF_EMAIL, "10.0.0.1"));
        assertEquals(429, ex.getStatus());
        assertEquals("TOO_MANY_REQUESTS", ex.getErrorCode());
    }

    @Test
    void checkAccountType_blankIp_countedUnderSharedKey() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        assertEquals(
                "staff",
                authenticationService.checkAccountType(STAFF_EMAIL, "  ").getAccountType());
        assertEquals(
                "staff",
                authenticationService.checkAccountType(STAFF_EMAIL, null).getAccountType());
    }

    // ── login routing ────────────────────────────────────────────────────────

    @Test
    void login_adminEmail_delegatesToAdminAuthService() {
        AdminUser admin = AdminUser.builder().id(9L).email(ADMIN_EMAIL).build();
        LoginApiResponse expected = LoginApiResponse.builder().role("ADMIN").build();
        when(adminUserRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(adminAuthService.processAdminLogin(admin, "pass", "127.0.0.1")).thenReturn(expected);

        assertSame(expected, authenticationService.login(loginRequest(ADMIN_EMAIL, "pass"), "127.0.0.1"));
        verifyNoInteractions(staffUserRepository, studentUserRepository);
    }

    @Test
    void login_noAccountAnywhere_throwsInvalidCredentials() {
        when(adminUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());
        when(staffUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());
        when(studentUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());
        LoginRequest request = loginRequest("ghost@sakuji.com", "pass");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authenticationService.login(request, "127.0.0.1"));
        assertEquals(401, ex.getStatus());
        assertEquals("INVALID_CREDENTIALS", ex.getErrorCode());
    }

    @Test
    void loginStaff_emailNotFound_throwsInvalidCredentials() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.empty());
        LoginRequest request = loginRequest(STAFF_EMAIL, "pass");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authenticationService.loginStaff(request, "127.0.0.1"));
        assertEquals("INVALID_CREDENTIALS", ex.getErrorCode());
    }

    // ── staff login rules ────────────────────────────────────────────────────

    @Test
    void loginStaff_success_issuesAccessAndRefreshToken() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtProvider.generateStaffAccessToken(2L, STAFF_EMAIL)).thenReturn("staff-access");

        LoginApiResponse response = authenticationService.loginStaff(loginRequest(STAFF_EMAIL, "pass"), "127.0.0.1");

        assertEquals("staff-access", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("STAFF", response.getRole());
        assertEquals("staff", response.getStaffRole());
        assertFalse(response.getRequirePasswordChange());
        assertEquals(0, staff.getLoginAttempts());
        assertNotNull(staff.getLastLoginAt());

        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(tokenCaptor.capture());
        assertEquals(AuthToken.TokenType.REFRESH, tokenCaptor.getValue().getTokenType());
        assertEquals(AuthToken.ActorType.STAFF, tokenCaptor.getValue().getActorType());
    }

    @Test
    void loginStaff_lockedAccount_throwsTooManyRequests() {
        staff.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        LoginRequest request = loginRequest(STAFF_EMAIL, "pass");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authenticationService.loginStaff(request, "127.0.0.1"));
        assertEquals(429, ex.getStatus());
    }

    @Test
    void loginStaff_expiredLock_doesNotBlockLogin() {
        staff.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtProvider.generateStaffAccessToken(2L, STAFF_EMAIL)).thenReturn("staff-access");

        assertEquals(
                "staff-access",
                authenticationService
                        .loginStaff(loginRequest(STAFF_EMAIL, "pass"), "127.0.0.1")
                        .getAccessToken());
    }

    @Test
    void loginStaff_suspendedAccount_throwsAccountSuspended() {
        staff.setStatus(StaffUser.StaffStatus.SUSPENDED);
        staff.setSuspendReason("vi phạm");
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        LoginRequest request = loginRequest(STAFF_EMAIL, "pass");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authenticationService.loginStaff(request, "127.0.0.1"));
        assertEquals(403, ex.getStatus());
        assertEquals("ACCOUNT_SUSPENDED", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("vi phạm"));
    }

    @Test
    void loginStaff_wrongPassword_incrementsAttemptsWithoutLocking() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        LoginRequest request = loginRequest(STAFF_EMAIL, "wrong");

        assertThrows(BusinessException.class, () -> authenticationService.loginStaff(request, "127.0.0.1"));

        assertEquals(1, staff.getLoginAttempts());
        assertNull(staff.getLockedUntil());
        verify(staffUserRepository).save(staff);
    }

    @Test
    void loginStaff_fifthWrongPassword_locksAccountFor15Minutes() {
        staff.setLoginAttempts(4);
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        LoginRequest request = loginRequest(STAFF_EMAIL, "wrong");

        assertThrows(BusinessException.class, () -> authenticationService.loginStaff(request, "127.0.0.1"));

        assertEquals(5, staff.getLoginAttempts());
        assertNotNull(staff.getLockedUntil());
        assertTrue(staff.getLockedUntil().isAfter(LocalDateTime.now().plusMinutes(14)));
    }

    @Test
    void login_staffEmail_routesToStaffHandler() {
        when(adminUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.empty());
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtProvider.generateStaffAccessToken(2L, STAFF_EMAIL)).thenReturn("staff-access");

        assertEquals(
                "STAFF",
                authenticationService
                        .login(loginRequest(STAFF_EMAIL, "pass"), "127.0.0.1")
                        .getRole());
    }

    // ── student login: nhánh bảo trì & chưa xác minh email ───────────────────

    @Test
    void login_studentWhileMaintenance_throwsMaintenanceMode() {
        StudentUser student = new StudentUser();
        student.setEmail(STUDENT_EMAIL);
        student.setStatus(StudentUser.StudentStatus.ACTIVE);
        when(studentUserRepository.findByEmail(STUDENT_EMAIL)).thenReturn(Optional.of(student));
        when(maintenanceModeService.isEnabled()).thenReturn(true);
        LoginRequest request = loginRequest(STUDENT_EMAIL, "pass");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authenticationService.login(request, "127.0.0.1"));
        assertEquals(503, ex.getStatus());
        assertEquals("MAINTENANCE_MODE", ex.getErrorCode());
    }

    @Test
    void login_studentPendingVerification_throwsEmailNotVerified() {
        StudentUser student = new StudentUser();
        student.setEmail(STUDENT_EMAIL);
        student.setStatus(StudentUser.StudentStatus.PENDING);
        when(studentUserRepository.findByEmail(STUDENT_EMAIL)).thenReturn(Optional.of(student));
        LoginRequest request = loginRequest(STUDENT_EMAIL, "pass");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authenticationService.login(request, "127.0.0.1"));
        assertEquals("EMAIL_NOT_VERIFIED", ex.getErrorCode());
    }

    // ── refresh ──────────────────────────────────────────────────────────────

    private RefreshTokenRequest refreshRequest(String value) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(value);
        return request;
    }

    private AuthToken refreshToken(AuthToken.ActorType actorType) {
        return AuthToken.builder()
                .actorType(actorType)
                .tokenType(AuthToken.TokenType.REFRESH)
                .tokenValue("rt")
                .studentId(1L)
                .staffId(2L)
                .adminId(9L)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void refresh_unknownToken_throwsInvalidToken() {
        when(authTokenRepository.findByTokenValue("rt")).thenReturn(Optional.empty());
        RefreshTokenRequest request = refreshRequest("rt");

        BusinessException ex = assertThrows(BusinessException.class, () -> authenticationService.refresh(request));
        assertEquals("INVALID_TOKEN", ex.getErrorCode());
    }

    @Test
    void refresh_revokedToken_throwsTokenRevoked() {
        AuthToken token = refreshToken(AuthToken.ActorType.STUDENT);
        token.setRevokedAt(LocalDateTime.now().minusMinutes(1));
        when(authTokenRepository.findByTokenValue("rt")).thenReturn(Optional.of(token));
        RefreshTokenRequest request = refreshRequest("rt");

        BusinessException ex = assertThrows(BusinessException.class, () -> authenticationService.refresh(request));
        assertEquals("TOKEN_REVOKED", ex.getErrorCode());
    }

    @Test
    void refresh_expiredToken_deletesTokenAndThrows() {
        AuthToken token = refreshToken(AuthToken.ActorType.STUDENT);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(authTokenRepository.findByTokenValue("rt")).thenReturn(Optional.of(token));
        RefreshTokenRequest request = refreshRequest("rt");

        BusinessException ex = assertThrows(BusinessException.class, () -> authenticationService.refresh(request));
        assertEquals("TOKEN_EXPIRED", ex.getErrorCode());
        verify(authTokenRepository).delete(token);
    }

    @Test
    void refresh_studentToken_rotatesAndIssuesPlainAccessToken() {
        AuthToken token = refreshToken(AuthToken.ActorType.STUDENT);
        StudentUser student = new StudentUser();
        student.setEmail(STUDENT_EMAIL);
        when(authTokenRepository.findByTokenValue("rt")).thenReturn(Optional.of(token));
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(jwtProvider.generateTokenFromUsername(eq(STUDENT_EMAIL), anyLong()))
                .thenReturn("new-access", "new-refresh");

        RefreshTokenResponse response = authenticationService.refresh(refreshRequest("rt"));

        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
        assertEquals("new-refresh", token.getTokenValue());
        verify(authTokenRepository).save(token);
    }

    @Test
    void refresh_staffToken_usesStaffAccessToken() {
        AuthToken token = refreshToken(AuthToken.ActorType.STAFF);
        when(authTokenRepository.findByTokenValue("rt")).thenReturn(Optional.of(token));
        when(staffUserRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(jwtProvider.generateStaffAccessToken(2L, STAFF_EMAIL)).thenReturn("staff-access");
        when(jwtProvider.generateTokenFromUsername(eq(STAFF_EMAIL), anyLong())).thenReturn("new-refresh");

        RefreshTokenResponse response = authenticationService.refresh(refreshRequest("rt"));

        assertEquals("staff-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
    }

    @Test
    void refresh_adminToken_usesAdminAccessToken() {
        AuthToken token = refreshToken(AuthToken.ActorType.ADMIN);
        AdminUser admin = AdminUser.builder().id(9L).email(ADMIN_EMAIL).build();
        when(authTokenRepository.findByTokenValue("rt")).thenReturn(Optional.of(token));
        when(adminUserRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(jwtProvider.generateAdminAccessToken(9L, ADMIN_EMAIL)).thenReturn("admin-access");
        when(jwtProvider.generateTokenFromUsername(eq(ADMIN_EMAIL), anyLong())).thenReturn("new-refresh");

        assertEquals(
                "admin-access",
                authenticationService.refresh(refreshRequest("rt")).getAccessToken());
    }

    @Test
    void refresh_studentDeleted_throwsUserNotFound() {
        when(authTokenRepository.findByTokenValue("rt"))
                .thenReturn(Optional.of(refreshToken(AuthToken.ActorType.STUDENT)));
        when(studentUserRepository.findById(1L)).thenReturn(Optional.empty());
        RefreshTokenRequest request = refreshRequest("rt");

        BusinessException ex = assertThrows(BusinessException.class, () -> authenticationService.refresh(request));
        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void refresh_staffDeleted_throwsUserNotFound() {
        when(authTokenRepository.findByTokenValue("rt"))
                .thenReturn(Optional.of(refreshToken(AuthToken.ActorType.STAFF)));
        when(staffUserRepository.findById(2L)).thenReturn(Optional.empty());
        RefreshTokenRequest request = refreshRequest("rt");

        assertEquals(
                "USER_NOT_FOUND",
                assertThrows(BusinessException.class, () -> authenticationService.refresh(request))
                        .getErrorCode());
    }

    @Test
    void refresh_adminDeleted_throwsUserNotFound() {
        when(authTokenRepository.findByTokenValue("rt"))
                .thenReturn(Optional.of(refreshToken(AuthToken.ActorType.ADMIN)));
        when(adminUserRepository.findById(9L)).thenReturn(Optional.empty());
        RefreshTokenRequest request = refreshRequest("rt");

        assertEquals(
                "USER_NOT_FOUND",
                assertThrows(BusinessException.class, () -> authenticationService.refresh(request))
                        .getErrorCode());
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    void logout_existingToken_deletesIt() {
        AuthToken token = refreshToken(AuthToken.ActorType.STUDENT);
        when(authTokenRepository.findByTokenValue("rt")).thenReturn(Optional.of(token));
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("rt");

        authenticationService.logout(request);

        verify(authTokenRepository).delete(token);
    }

    @Test
    void logout_unknownToken_isNoOp() {
        when(authTokenRepository.findByTokenValue("rt")).thenReturn(Optional.empty());
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("rt");

        authenticationService.logout(request);

        verify(authTokenRepository, never()).delete(any(AuthToken.class));
    }

    // ── Google login ─────────────────────────────────────────────────────────

    private GoogleTokenRequest googleRequest() {
        GoogleTokenRequest request = new GoogleTokenRequest();
        request.setIdToken("google-id-token");
        return request;
    }

    @Test
    void loginWithGoogle_maintenanceMode_throwsBeforeVerifyingToken() {
        when(maintenanceModeService.isEnabled()).thenReturn(true);
        GoogleTokenRequest request = googleRequest();

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authenticationService.loginWithGoogle(request));
        assertEquals(503, ex.getStatus());
        assertEquals("MAINTENANCE_MODE", ex.getErrorCode());
    }

    @Test
    void loginWithGoogle_malformedToken_throwsInvalidGoogleToken() {
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        GoogleTokenRequest request = googleRequest();

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authenticationService.loginWithGoogle(request));
        assertEquals(401, ex.getStatus());
        assertEquals("INVALID_GOOGLE_TOKEN", ex.getErrorCode());
    }

    /** Chặn ở tầng verifier: token đúng định dạng nhưng Google từ chối → verify() trả null. */
    @Test
    void loginWithGoogle_verifierRejectsToken_throwsInvalidGoogleToken() throws Exception {
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
        when(verifier.verify("google-id-token")).thenReturn(null);

        try (MockedConstruction<GoogleIdTokenVerifier.Builder> ignored =
                mockConstruction(GoogleIdTokenVerifier.Builder.class, (mock, context) -> {
                    when(mock.setAudience(any())).thenReturn(mock);
                    when(mock.build()).thenReturn(verifier);
                })) {
            GoogleTokenRequest request = googleRequest();

            BusinessException ex =
                    assertThrows(BusinessException.class, () -> authenticationService.loginWithGoogle(request));
            assertEquals("INVALID_GOOGLE_TOKEN", ex.getErrorCode());
        }
    }

    @Test
    void loginWithGoogle_newEmail_autoCreatesActiveStudent() throws Exception {
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        when(studentUserRepository.findByEmail("newbie@gmail.com")).thenReturn(Optional.empty());
        when(studentUserRepository.save(any(StudentUser.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtProvider.generateTokenFromUsername(eq("newbie@gmail.com"), anyLong()))
                .thenReturn("access", "refresh");

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("newbie@gmail.com");
        payload.setSubject("google-sub-1");
        payload.set("name", "Newbie");
        payload.set("picture", "https://lh3.google.com/avatar.png");

        try (MockedConstruction<GoogleIdTokenVerifier.Builder> ignored = mockVerifierReturning(payload)) {
            AuthResponse response = authenticationService.loginWithGoogle(googleRequest());

            assertEquals("access", response.getAccessToken());
            assertEquals("refresh", response.getRefreshToken());
        }

        ArgumentCaptor<StudentUser> userCaptor = ArgumentCaptor.forClass(StudentUser.class);
        verify(studentUserRepository, atLeastOnce()).save(userCaptor.capture());
        StudentUser created = userCaptor.getAllValues().get(0);
        assertEquals("Newbie", created.getFullName());
        assertEquals(StudentUser.StudentStatus.ACTIVE, created.getStatus());
        assertEquals(StudentUser.OauthProvider.GOOGLE, created.getOauthProvider());
        assertEquals("google-sub-1", created.getOauthProviderId());
        assertNotNull(created.getEmailVerifiedAt());
    }

    @Test
    void loginWithGoogle_newEmailWithoutName_fallsBackToEmailAsFullName() throws Exception {
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        when(studentUserRepository.findByEmail("noname@gmail.com")).thenReturn(Optional.empty());
        when(studentUserRepository.save(any(StudentUser.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtProvider.generateTokenFromUsername(eq("noname@gmail.com"), anyLong()))
                .thenReturn("access", "refresh");

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("noname@gmail.com");
        payload.setSubject("google-sub-2");

        try (MockedConstruction<GoogleIdTokenVerifier.Builder> ignored = mockVerifierReturning(payload)) {
            authenticationService.loginWithGoogle(googleRequest());
        }

        ArgumentCaptor<StudentUser> userCaptor = ArgumentCaptor.forClass(StudentUser.class);
        verify(studentUserRepository, atLeastOnce()).save(userCaptor.capture());
        assertEquals("noname@gmail.com", userCaptor.getAllValues().get(0).getFullName());
    }

    @Test
    void loginWithGoogle_existingUnlinkedAccount_linksGoogleProvider() throws Exception {
        StudentUser existing = new StudentUser();
        existing.setId(1L);
        existing.setEmail("linked@gmail.com");
        existing.setStatus(StudentUser.StudentStatus.ACTIVE);
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        when(studentUserRepository.findByEmail("linked@gmail.com")).thenReturn(Optional.of(existing));
        when(jwtProvider.generateTokenFromUsername(eq("linked@gmail.com"), anyLong()))
                .thenReturn("access", "refresh");

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("linked@gmail.com");
        payload.setSubject("google-sub-3");

        try (MockedConstruction<GoogleIdTokenVerifier.Builder> ignored = mockVerifierReturning(payload)) {
            authenticationService.loginWithGoogle(googleRequest());
        }

        assertEquals(StudentUser.OauthProvider.GOOGLE, existing.getOauthProvider());
        assertEquals("google-sub-3", existing.getOauthProviderId());
        assertNotNull(existing.getOauthLinkedAt());
        assertNotNull(existing.getLastLoginAt());
    }

    @Test
    void loginWithGoogle_alreadyLinkedAccount_skipsRelinking() throws Exception {
        StudentUser existing = new StudentUser();
        existing.setId(1L);
        existing.setEmail("linked@gmail.com");
        existing.setStatus(StudentUser.StudentStatus.ACTIVE);
        existing.setOauthProvider(StudentUser.OauthProvider.GOOGLE);
        existing.setOauthProviderId("old-sub");
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        when(studentUserRepository.findByEmail("linked@gmail.com")).thenReturn(Optional.of(existing));
        when(jwtProvider.generateTokenFromUsername(eq("linked@gmail.com"), anyLong()))
                .thenReturn("access", "refresh");

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("linked@gmail.com");
        payload.setSubject("new-sub");

        try (MockedConstruction<GoogleIdTokenVerifier.Builder> ignored = mockVerifierReturning(payload)) {
            authenticationService.loginWithGoogle(googleRequest());
        }

        assertEquals("old-sub", existing.getOauthProviderId());
    }

    @Test
    void loginWithGoogle_suspendedAccount_throwsAccountSuspended() throws Exception {
        StudentUser existing = new StudentUser();
        existing.setId(1L);
        existing.setEmail("banned@gmail.com");
        existing.setStatus(StudentUser.StudentStatus.SUSPENDED);
        existing.setSuspendReason("spam");
        existing.setOauthProvider(StudentUser.OauthProvider.GOOGLE);
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        when(studentUserRepository.findByEmail("banned@gmail.com")).thenReturn(Optional.of(existing));

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("banned@gmail.com");
        payload.setSubject("google-sub-4");

        try (MockedConstruction<GoogleIdTokenVerifier.Builder> ignored = mockVerifierReturning(payload)) {
            GoogleTokenRequest request = googleRequest();

            BusinessException ex =
                    assertThrows(BusinessException.class, () -> authenticationService.loginWithGoogle(request));
            assertEquals(403, ex.getStatus());
            assertEquals("ACCOUNT_SUSPENDED", ex.getErrorCode());
        }
    }

    private MockedConstruction<GoogleIdTokenVerifier.Builder> mockVerifierReturning(GoogleIdToken.Payload payload)
            throws Exception {
        GoogleIdToken idToken = mock(GoogleIdToken.class);
        when(idToken.getPayload()).thenReturn(payload);
        GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
        when(verifier.verify("google-id-token")).thenReturn(idToken);

        return mockConstruction(GoogleIdTokenVerifier.Builder.class, (mock, context) -> {
            when(mock.setAudience(any())).thenReturn(mock);
            when(mock.build()).thenReturn(verifier);
        });
    }
}
