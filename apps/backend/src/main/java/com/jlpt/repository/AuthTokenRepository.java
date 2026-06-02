/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.AuthToken;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenValue(String tokenValue);

    Optional<AuthToken> findByTokenValueAndTokenType(String tokenValue, AuthToken.TokenType tokenType);

    void deleteByTokenValue(String tokenValue);

    void deleteByStudentIdAndTokenType(Long studentId, AuthToken.TokenType tokenType);

    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.tokenValue = :tokenValue AND t.tokenType = :tokenType")
    void bulkDeleteByTokenValueAndType(
            @Param("tokenValue") String tokenValue, @Param("tokenType") AuthToken.TokenType tokenType);

    /** Revoke all active tokens (SESSION + REFRESH) for a student — used on suspend/delete. */
    @Modifying
    @Query("UPDATE AuthToken t SET t.revokedAt = :now WHERE t.studentId = :id AND t.revokedAt IS NULL")
    void revokeAllActiveByStudentId(@Param("id") Long id, @Param("now") LocalDateTime now);

    /** Revoke all active tokens for a staff member — used on suspend/delete. */
    @Modifying
    @Query("UPDATE AuthToken t SET t.revokedAt = :now WHERE t.staffId = :id AND t.revokedAt IS NULL")
    void revokeAllActiveByStaffId(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying
    @Query(
            """
            UPDATE AuthToken t
            SET t.revokedAt = :now
            WHERE t.staffId = :id
              AND t.tokenType IN :tokenTypes
              AND t.revokedAt IS NULL
            """)
    void revokeActiveByStaffIdAndTokenTypes(
            @Param("id") Long id,
            @Param("tokenTypes") Collection<AuthToken.TokenType> tokenTypes,
            @Param("now") LocalDateTime now);

    /** Revoke all active tokens for an admin — used on password reset. */
    @Modifying
    @Query("UPDATE AuthToken t SET t.revokedAt = :now WHERE t.adminId = :id AND t.revokedAt IS NULL")
    void revokeAllActiveByAdminId(@Param("id") Long id, @Param("now") LocalDateTime now);
}
