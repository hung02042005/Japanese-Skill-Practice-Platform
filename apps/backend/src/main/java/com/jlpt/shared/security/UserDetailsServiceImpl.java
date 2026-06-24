/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.security;

import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.feature.student.repository.StudentUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final StudentUserRepository studentUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StudentUser user = studentUserRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return new UserDetailsImpl(user);
    }
}
