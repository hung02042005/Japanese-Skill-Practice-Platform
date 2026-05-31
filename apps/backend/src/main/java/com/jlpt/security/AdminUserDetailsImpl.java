/* (c) JLPT E-Learning Platform */
package com.jlpt.security;

import com.jlpt.entity.AdminUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AdminUserDetailsImpl implements UserDetails {
    private final AdminUser adminUser;

    public AdminUserDetailsImpl(AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public String getPassword() {
        return adminUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return adminUser.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (adminUser.getLockedUntil() != null && adminUser.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            return false;
        }
        return adminUser.getStatus() != AdminUser.AdminStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return adminUser.getStatus() == AdminUser.AdminStatus.ACTIVE
                || adminUser.getStatus() == AdminUser.AdminStatus.PENDING;
    }

    public AdminUser getAdminUser() {
        return adminUser;
    }
}
