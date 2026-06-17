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

    @Override
    public boolean isEnabled() {
        return studentUser.getStatus() == StudentUser.StudentStatus.ACTIVE
                || studentUser.getStatus() == StudentUser.StudentStatus.PENDING;
    }

    public StudentUser getStudentUser() {
        return studentUser;
    }
}
