/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.dto.request.LoginRequest;
import com.jlpt.dto.response.AuthResponse;
import com.jlpt.entity.AuthToken;
import com.jlpt.entity.StudentUser;
import com.jlpt.exception.BusinessException;
import com.jlpt.repository.AuthTokenRepository;
import com.jlpt.repository.StudentUserRepository;
import com.jlpt.security.JwtProvider;
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
        when(jwtProvider.generateAccessToken(any(Authentication.class), eq(AuthToken.ActorType.STUDENT))).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(any(Authentication.class), eq(AuthToken.ActorType.STUDENT))).thenReturn("refresh-token");
        when(authTokenRepository.save(any(AuthToken.class))).thenReturn(new AuthToken());

        AuthResponse response = authService.login(loginRequest, "127.0.0.1");

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
        assertEquals(429, ex.getStatus());
        assertEquals("TOO_MANY_REQUESTS", ex.getErrorCode());
        assertEquals(5, mockUser.getLoginAttempts());
        assertNotNull(mockUser.getLockedUntil());
        verify(studentUserRepository).save(mockUser);
    }
}
