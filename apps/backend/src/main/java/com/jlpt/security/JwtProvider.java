/* (c) JLPT E-Learning Platform */
package com.jlpt.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration-ms:900000}") // 15 minutes
    private long jwtAccessExpirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}") // 7 days
    private long jwtRefreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername(), jwtAccessExpirationMs);
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername(), jwtRefreshExpirationMs);
    }

    public String generateTokenFromUsername(String username, long expirationMs) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Issues a 15-min JWT with role=ADMIN claim for Admin Panel access (BR-35-07). */
    public String generateAdminAccessToken(Long adminId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("role", "ADMIN")
                .claim("adminId", adminId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtAccessExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Issues a 15-min JWT with role=STAFF claim (UC-36). */
    public String generateStaffAccessToken(Long staffId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("role", "STAFF")
                .claim("staffId", staffId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtAccessExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateLimitedSessionToken(Long staffId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("role", "STAFF")
                .claim("staffId", staffId)
                .claim("tokenType", "limited_session")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1_800_000))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateStaffLimitedSessionToken(Long staffId, String email) {
        return generateLimitedSessionToken(staffId, email);
    }

    /** Returns the role claim value ("ADMIN", "STAFF") or null for legacy student tokens. */
    public String getRoleFromToken(String token) {
        Object role = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role");
        return role != null ? role.toString() : null;
    }

    public String getTokenTypeFromToken(String token) {
        Object tokenType = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("tokenType");
        return tokenType != null ? tokenType.toString() : null;
    }

    public Long getStaffIdFromToken(String token) {
        Object staffId = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("staffId");
        if (staffId == null) {
            return null;
        }
        if (staffId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(staffId.toString());
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
