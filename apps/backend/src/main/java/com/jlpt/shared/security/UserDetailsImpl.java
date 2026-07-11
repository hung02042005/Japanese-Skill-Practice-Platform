/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.security;

import com.jlpt.feature.student.StudentUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsImpl implements UserDetails {
    private final StudentUser studentUser;

    public UserDetailsImpl(StudentUser studentUser) {
        this.studentUser = studentUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    @Override
    public String getPassword() {
        return studentUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return studentUser.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (studentUser.getLockedUntil() != null
                && studentUser.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            return false;
        }
        return studentUser.getStatus() != StudentUser.StudentStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // PENDING được coi là enabled một cách CÓ CHỦ ĐÍCH: nếu trả về false ở đây, Spring Security
    // ném DisabledException ngay trong AuthenticationManager.authenticate(), khiến AuthService không
    // thể tự bắt và trả message "vui lòng xác minh email" — nó bị nuốt bởi exception handling chung
    // của Spring. Việc chặn đăng nhập khi PENDING được xử lý tường minh ở
    // AuthService.handleStudentLogin() TRƯỚC KHI gọi authenticationManager.authenticate().
    // Bất kỳ luồng authenticate nào khác dùng UserDetailsService (vd thêm SSO/refresh sau này)
    // PHẢI tự check StudentStatus.PENDING trước khi authenticate, vì isEnabled() sẽ không chặn giúp.
    @Override
    public boolean isEnabled() {
        return studentUser.getStatus() == StudentUser.StudentStatus.ACTIVE
                || studentUser.getStatus() == StudentUser.StudentStatus.PENDING;
    }

    public StudentUser getStudentUser() {
        return studentUser;
    }
}
