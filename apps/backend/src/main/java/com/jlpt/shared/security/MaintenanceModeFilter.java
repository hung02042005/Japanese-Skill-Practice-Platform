/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.security;

import com.jlpt.feature.admin.service.SystemSettingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Blocks STUDENT users when maintenance_mode = true.
 * Admin/Staff paths (/api/staff/**, /api/admin/**) always bypass.
 * Must run AFTER JwtAuthenticationFilter so SecurityContextHolder is populated.
 */
@Component
@RequiredArgsConstructor
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final SystemSettingService systemSettingService;

    private static final Set<String> BYPASS_PREFIXES = Set.of("/api/staff", "/api/admin");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (BYPASS_PREFIXES.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        if (systemSettingService.isMaintenanceMode()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isStudent = auth != null
                    && auth.isAuthenticated()
                    && auth.getAuthorities().stream()
                            .anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority()));

            if (isStudent) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"status\":503,\"message\":\"Hệ thống đang trong quá trình bảo trì. Vui lòng quay lại sau.\",\"data\":null}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
