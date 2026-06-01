/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.AuthToken;
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
    void bulkDeleteByTokenValueAndType(@Param("tokenValue") String tokenValue,
                                       @Param("tokenType") AuthToken.TokenType tokenType);
}
