/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.AuthToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByTokenValue(String tokenValue);

    Optional<AuthToken> findByTokenValueAndTokenType(String tokenValue, AuthToken.TokenType tokenType);

    void deleteByTokenValue(String tokenValue);
}
