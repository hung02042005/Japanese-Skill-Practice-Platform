/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jlpt.dto.request.GoogleTokenRequest;
import com.jlpt.dto.request.LoginRequest;
import com.jlpt.dto.request.RegisterRequest;
import com.jlpt.dto.response.AuthResponse;
import com.jlpt.dto.response.StudentResponse;
import com.jlpt.entity.AuthToken;
import com.jlpt.entity.StudentUser;
import com.jlpt.exception.BusinessException;
import com.jlpt.repository.AuthTokenRepository;
import com.jlpt.repository.StudentUserRepository;
import com.jlpt.security.JwtProvider;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final StudentUserRepository studentUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Transactional
    public AuthResponse login(LoginRequest request, String ip) {
        StudentUser user = studentUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng"));

        // Check lock
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(429, "TOO_MANY_REQUESTS", "Quá nhiều lần thử. Vui lòng thử lại sau.");
        }
        if (user.getStatus() == StudentUser.StudentStatus.SUSPENDED) {
            throw new BusinessException(403, "ACCOUNT_SUSPENDED", "Tài khoản bị tạm khóa: " + user.getSuspendReason());
        }
        if (user.getStatus() == StudentUser.StudentStatus.PENDING) {
            throw new BusinessException(403, "EMAIL_NOT_VERIFIED", "Vui lòng xác minh email trước khi đăng nhập");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            // Reset attempts on success
            user.setLoginAttempts(0);
            user.setLockedUntil(null);
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(ip);
            studentUserRepository.save(user);

            String accessToken = jwtProvider.generateAccessToken(authentication, AuthToken.ActorType.STUDENT);
            String refreshToken = jwtProvider.generateRefreshToken(authentication, AuthToken.ActorType.STUDENT);

            // Save refresh token
            AuthToken tokenEntity = AuthToken.builder()
                    .actorType(AuthToken.ActorType.STUDENT)
                    .studentId(user.getId())
                    .tokenType(AuthToken.TokenType.REFRESH)
                    .tokenValue(refreshToken)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();
            authTokenRepository.save(tokenEntity);

            log.info("[AuthService] Login success for email: {}", user.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .student(mapToStudentResponse(user))
                    .build();

        } catch (BadCredentialsException ex) {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            if (user.getLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                studentUserRepository.save(user);
                log.warn("[AuthService] Account locked for email: {}", user.getEmail());
                throw new BusinessException(
                        429, "TOO_MANY_REQUESTS", "Tài khoản tạm thời bị khóa do sai mật khẩu 5 lần.");
            }
            studentUserRepository.save(user);
            log.warn("[AuthService] Login failed for email: {}", user.getEmail());
            throw new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
        }
    }

    @Transactional
    public StudentResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
        }

        if (studentUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(409, "EMAIL_EXISTS", "Email đã được sử dụng");
        }

        StudentUser newUser = StudentUser.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(StudentUser.StudentStatus.PENDING)
                .build();

        StudentUser savedUser = studentUserRepository.save(newUser);

        // Mock Email Verification logic
        String verificationToken = UUID.randomUUID().toString();
        AuthToken tokenEntity = AuthToken.builder()
                .actorType(AuthToken.ActorType.STUDENT)
                .studentId(savedUser.getId())
                .tokenType(AuthToken.TokenType.EMAIL_VERIFICATION)
                .tokenValue(verificationToken)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        authTokenRepository.save(tokenEntity);

        log.info("[AuthService] Registration successful for {}, sending verification email.", savedUser.getEmail());
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        return mapToStudentResponse(savedUser);
    }

    private StudentResponse mapToStudentResponse(StudentUser user) {
        return StudentResponse.builder()
                .studentId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .currentJlptLevel(
                        user.getCurrentJlptLevel() != null
                                ? user.getCurrentJlptLevel().name()
                                : null)
                .targetJlptLevel(
                        user.getTargetJlptLevel() != null
                                ? user.getTargetJlptLevel().name()
                                : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public com.jlpt.dto.response.RefreshTokenResponse refresh(com.jlpt.dto.request.RefreshTokenRequest request) {
        AuthToken tokenEntity = authTokenRepository
                .findByTokenValue(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(401, "INVALID_TOKEN", "Refresh token không hợp lệ"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            authTokenRepository.delete(tokenEntity);
            throw new BusinessException(401, "TOKEN_EXPIRED", "Refresh token đã hết hạn");
        }

        StudentUser user = studentUserRepository
                .findById(tokenEntity.getStudentId())
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        String newAccessToken = jwtProvider.generateTokenFromUsername(user.getEmail(), AuthToken.ActorType.STUDENT, 900000); // 15 mins
        String newRefreshToken = jwtProvider.generateTokenFromUsername(user.getEmail(), AuthToken.ActorType.STUDENT, 604800000); // 7 days

        tokenEntity.setTokenValue(newRefreshToken);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7));
        authTokenRepository.save(tokenEntity);

        return com.jlpt.dto.response.RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(com.jlpt.dto.request.LogoutRequest request) {
        authTokenRepository.findByTokenValue(request.getRefreshToken()).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            authTokenRepository.save(token);
        });
    }

    @Transactional
    public void verifyEmail(com.jlpt.dto.request.VerifyEmailRequest request) {
        AuthToken tokenEntity = authTokenRepository
                .findByTokenValue(request.getToken())
                .orElseThrow(() -> new BusinessException(400, "INVALID_TOKEN", "Mã xác minh không hợp lệ"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "TOKEN_EXPIRED", "Mã xác minh đã hết hạn");
        }

        StudentUser user = studentUserRepository
                .findById(tokenEntity.getStudentId())
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        user.setStatus(StudentUser.StudentStatus.ACTIVE);
        user.setEmailVerifiedAt(LocalDateTime.now());
        studentUserRepository.save(user);

        authTokenRepository.delete(tokenEntity);
    }

    @Transactional
    public void resendVerification(com.jlpt.dto.request.ResendVerificationRequest request) {
        StudentUser user = studentUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        if (user.getStatus() != StudentUser.StudentStatus.PENDING) {
            throw new BusinessException(400, "ALREADY_VERIFIED", "Tài khoản đã được xác minh hoặc bị khóa");
        }

        String verificationToken = UUID.randomUUID().toString();
        AuthToken tokenEntity = AuthToken.builder()
                .actorType(AuthToken.ActorType.STUDENT)
                .studentId(user.getId())
                .tokenType(AuthToken.TokenType.EMAIL_VERIFICATION)
                .tokenValue(verificationToken)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        authTokenRepository.save(tokenEntity);

        log.info("[AuthService] Resending verification email to: {}", user.getEmail());
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }

    @Transactional
    public void forgotPassword(com.jlpt.dto.request.ForgotPasswordRequest request) {
        StudentUser user = studentUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        String resetToken = UUID.randomUUID().toString();
        AuthToken tokenEntity = AuthToken.builder()
                .actorType(AuthToken.ActorType.STUDENT)
                .studentId(user.getId())
                .tokenType(AuthToken.TokenType.PASSWORD_RESET)
                .tokenValue(resetToken)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        authTokenRepository.save(tokenEntity);

        log.info("[AuthService] Sending password reset email to: {}", user.getEmail());
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleTokenRequest request) {
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String googleSub = payload.getSubject();
        String avatarUrl = (String) payload.get("picture");

        Optional<StudentUser> existing = studentUserRepository.findByEmail(email);

        StudentUser user;
        if (existing.isPresent()) {
            user = existing.get();
            // Link Google provider if not already linked
            if (user.getOauthProvider() == null) {
                user.setOauthProvider(StudentUser.OauthProvider.GOOGLE);
                user.setOauthProviderId(googleSub);
                user.setOauthProviderEmail(email);
                user.setOauthLinkedAt(LocalDateTime.now());
                studentUserRepository.save(user);
            }
            // Check account is not suspended
            if (user.getStatus() == StudentUser.StudentStatus.SUSPENDED) {
                throw new BusinessException(
                        403, "ACCOUNT_SUSPENDED", "Tài khoản bị tạm khóa: " + user.getSuspendReason());
            }
        } else {
            // Auto-create new account from Google
            user = StudentUser.builder()
                    .email(email)
                    .fullName(name != null ? name : email)
                    .avatarUrl(avatarUrl)
                    .status(StudentUser.StudentStatus.ACTIVE)
                    .emailVerifiedAt(LocalDateTime.now())
                    .oauthProvider(StudentUser.OauthProvider.GOOGLE)
                    .oauthProviderId(googleSub)
                    .oauthProviderEmail(email)
                    .oauthLinkedAt(LocalDateTime.now())
                    .build();
            user = studentUserRepository.save(user);
            log.info("[AuthService] New account created via Google for: {}", email);
        }

        user.setLastLoginAt(LocalDateTime.now());
        studentUserRepository.save(user);

        // Generate JWT tokens directly (no password involved)
        String accessToken = jwtProvider.generateTokenFromUsername(user.getEmail(), AuthToken.ActorType.STUDENT, 900000L);
        String refreshToken = jwtProvider.generateTokenFromUsername(user.getEmail(), AuthToken.ActorType.STUDENT, 604800000L);

        AuthToken tokenEntity = AuthToken.builder()
                .actorType(AuthToken.ActorType.STUDENT)
                .studentId(user.getId())
                .tokenType(AuthToken.TokenType.REFRESH)
                .tokenValue(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        authTokenRepository.save(tokenEntity);

        log.info("[AuthService] Google login success for: {}", email);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .student(mapToStudentResponse(user))
                .build();
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new BusinessException(401, "INVALID_GOOGLE_TOKEN", "Google ID Token không hợp lệ");
            }
            return googleIdToken.getPayload();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AuthService] Google token verification failed: {}", e.getMessage());
            throw new BusinessException(401, "INVALID_GOOGLE_TOKEN", "Xác thực Google thất bại");
        }
    }

    @Transactional
    public void resetPassword(com.jlpt.dto.request.ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
        }

        AuthToken tokenEntity = authTokenRepository
                .findByTokenValue(request.getToken())
                .orElseThrow(() -> new BusinessException(400, "INVALID_TOKEN", "Mã đặt lại mật khẩu không hợp lệ"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "TOKEN_EXPIRED", "Mã đặt lại mật khẩu đã hết hạn");
        }

        StudentUser user = studentUserRepository
                .findById(tokenEntity.getStudentId())
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        studentUserRepository.save(user);

        authTokenRepository.delete(tokenEntity);
    }

    public StudentResponse getProfile(Long studentId) {
        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));
        return mapToStudentResponse(user);
    }

    @Transactional
    public StudentResponse updateProfile(Long studentId, com.jlpt.dto.request.UpdateProfileRequest request) {
        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setBio(request.getBio());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAvatarUrl(request.getAvatarUrl());

        if (request.getTargetJlptLevel() != null) {
            user.setTargetJlptLevel(StudentUser.JlptLevel.valueOf(request.getTargetJlptLevel()));
        }

        return mapToStudentResponse(studentUserRepository.save(user));
    }

    @Transactional
    public void changePassword(Long studentId, com.jlpt.dto.request.ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
        }

        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "INVALID_PASSWORD", "Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        studentUserRepository.save(user);
    }
}
