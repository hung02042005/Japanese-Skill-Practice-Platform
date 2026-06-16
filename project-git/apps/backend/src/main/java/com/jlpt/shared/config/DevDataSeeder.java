/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.config;

import com.jlpt.feature.admin.AdminUser;
import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DevDataSeeder — chỉ chạy khi profile là "default" hoặc "dev" (H2 in-memory).
 *
 * <p>Tạo tài khoản mẫu khi DB trống để có thể test ngay mà không cần chạy SQL thủ công.
 *
 * <pre>
 *   ADMIN   : admin@sakuji.com    / Admin@123456   (two_factor_enabled = false — bỏ qua TOTP)
 *   STUDENT : student1@sakuji.com / Student@123456
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!prod") // Không chạy ở môi trường production
public class DevDataSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final StudentUserRepository studentUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedStudent();
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    private void seedAdmin() {
        String email = "admin@sakuji.com";
        if (adminUserRepository.existsByEmail(email)) {
            log.info("[DevDataSeeder] Admin đã tồn tại — bỏ qua.");
            return;
        }

        AdminUser admin = AdminUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Admin@123456"))
                .fullName("Quản Trị Viên")
                .status(AdminUser.AdminStatus.ACTIVE)
                .loginAttempts(0)
                .build();

        adminUserRepository.save(admin);
        log.info("[DevDataSeeder] ✅ Admin seed thành công: {} / Admin@123456", email);
    }

    // ─── Student mẫu ─────────────────────────────────────────────────────────

    private void seedStudent() {
        String email = "student1@sakuji.com";
        if (studentUserRepository.existsByEmail(email)) {
            log.info("[DevDataSeeder] Student đã tồn tại — bỏ qua.");
            return;
        }

        StudentUser student = StudentUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Student@123456"))
                .fullName("Nguyễn Minh Anh")
                .status(StudentUser.StudentStatus.ACTIVE)
                .emailVerifiedAt(LocalDateTime.now())
                .loginAttempts(0)
                .build();

        studentUserRepository.save(student);
        log.info("[DevDataSeeder] ✅ Student seed thành công: {} / Student@123456", email);
    }
}
