/* (c) JLPT E-Learning Platform */
package com.jlpt.security;

import com.jlpt.entity.StaffUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class StaffUserDetailsImpl implements UserDetails {
    private final StaffUser staffUser;

    public StaffUserDetailsImpl(StaffUser staffUser) {
        this.staffUser = staffUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + staffUser.getStaffRole().name()));
    }

    @Override
    public String getPassword() {
        return staffUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return staffUser.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (staffUser.getLockedUntil() != null
                && staffUser.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            return false;
        }
        return staffUser.getStatus() != StaffUser.StaffStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return staffUser.getStatus() == StaffUser.StaffStatus.ACTIVE
                || staffUser.getStatus() == StaffUser.StaffStatus.PENDING;
    }

    public StaffUser getStaffUser() {
        return staffUser;
    }
}
