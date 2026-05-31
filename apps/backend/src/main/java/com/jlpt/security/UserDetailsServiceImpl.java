/* (c) JLPT E-Learning Platform */
package com.jlpt.security;

import com.jlpt.entity.StudentUser;
import com.jlpt.repository.StudentUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final StudentUserRepository studentUserRepository;
    private final com.jlpt.repository.AdminUserRepository adminUserRepository;
    private final com.jlpt.repository.StaffUserRepository staffUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UsernameNotFoundException("Use loadUserByUsernameAndActorType instead");
    }

    public UserDetails loadUserByUsernameAndActorType(String username, com.jlpt.entity.AuthToken.ActorType actorType) throws UsernameNotFoundException {
        if (actorType == com.jlpt.entity.AuthToken.ActorType.STUDENT) {
            StudentUser user = studentUserRepository
                    .findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Student not found: " + username));
            return new UserDetailsImpl(user);
        } else if (actorType == com.jlpt.entity.AuthToken.ActorType.ADMIN) {
            com.jlpt.entity.AdminUser user = adminUserRepository
                    .findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));
            return new AdminUserDetailsImpl(user);
        } else if (actorType == com.jlpt.entity.AuthToken.ActorType.STAFF) {
            com.jlpt.entity.StaffUser user = staffUserRepository
                    .findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Staff not found: " + username));
            return new StaffUserDetailsImpl(user);
        }
        throw new UsernameNotFoundException("Invalid actor type");
    }
}
