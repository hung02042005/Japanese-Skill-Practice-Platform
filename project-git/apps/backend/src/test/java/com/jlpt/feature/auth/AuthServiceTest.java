/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminAuthService;
import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.auth.dto.request.LoginRequest;
import com.jlpt.feature.auth.dto.response.LoginApiResponse;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.email.EmailService;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.security.JwtProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AdminAuthService adminAuthService;

    @InjectMocks
    private AuthService authService;

    private StudentUser mockUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockUser = new StudentUser();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("Test User");
        mockUser.setStatus(StudentUser.StudentStatus.ACTIVE);
        mockUser.setLoginAttempts(0);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testLogin_Success() {
        when(studentUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtProvider.generateAccessToken(mockAuth)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(mockAuth)).thenReturn("refresh-token");
        when(authTokenRepository.save(any(AuthToken.class))).thenReturn(new AuthToken());

        LoginApiResponse response = authService.login(loginRequest, "127.0.0.1");

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(0, mockUser.getLoginAttempts());
        assertNull(mockUser.getLockedUntil());
        verify(studentUserRepository).save(mockUser);
    }

    @Test
    void testLogin_UserNotFound() {
        when(studentUserRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authService.login(loginRequest, "127.0.0.1"));
        assertEquals(401, ex.getStatus());
        assertEquals("INVALID_CREDENTIALS", ex.getErrorCode());
    }

    @Test
    void testLogin_AccountLocked() {
        mockUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(studentUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authService.login(loginRequest, "127.0.0.1"));
        assertEquals(429, ex.getStatus());
        assertEquals("TOO_MANY_REQUESTS", ex.getErrorCode());
    }

    @Test
    void testLogin_Suspended() {
        mockUser.setStatus(StudentUser.StudentStatus.SUSPENDED);
        when(studentUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authService.login(loginRequest, "127.0.0.1"));
        assertEquals(403, ex.getStatus());
        assertEquals("ACCOUNT_SUSPENDED", ex.getErrorCode());
    }

    @Test
    void testLogin_BadCredentials_IncrementsLoginAttempts() {
        when(studentUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authService.login(loginRequest, "127.0.0.1"));
        assertEquals(401, ex.getStatus());
        assertEquals("INVALID_CREDENTIALS", ex.getErrorCode());
        assertEquals(1, mockUser.getLoginAttempts());
        verify(studentUserRepository).save(mockUser);
    }

    @Test
    void testLogin_BadCredentials_LocksAccountAfter5Attempts() {
        mockUser.setLoginAttempts(4);
        when(studentUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BusinessException ex =
                assertThrows(BusinessException.class, () -> authService.login(loginRequest, "127.0.0.1"));
        assertEquals(401, ex.getStatus());
        assertEquals("INVALID_CREDENTIALS", ex.getErrorCode());
        assertEquals(5, mockUser.getLoginAttempts());
        assertNotNull(mockUser.getLockedUntil());
        verify(studentUserRepository).save(mockUser);
    }

    @Test
    void testStaffLogin_MustChangePassword_IssuesLimitedSession() {
        StaffUser staff = StaffUser.builder()
                .id(2L)
                .email("staff@example.com")
                .passwordHash("hash")
                .fullName("Staff User")
                .status(StaffUser.StaffStatus.ACTIVE)
                .mustChangePassword(true)
                .loginAttempts(0)
                .build();
        loginRequest.setEmail("staff@example.com");

        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtProvider.generateLimitedSessionToken(2L, "staff@example.com")).thenReturn("limited-token");
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginApiResponse response = authService.login(loginRequest, "127.0.0.1");

        assertEquals("limited-token", response.getAccessToken());
        assertTrue(response.getRequirePasswordChange());
        assertEquals("STAFF", response.getRole());
        verify(authTokenRepository)
                .save(argThat(token -> token.getTokenType() == AuthToken.TokenType.LIMITED_SESSION
                        && token.getStaffId().equals(2L)
                        && token.getExpiresAt() != null));
    }
}
