package com.daynix.app.auth.repository;

import com.daynix.app.auth.entity.RefreshToken;
import com.daynix.app.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken token set token.revokedAt = :revokedAt where token.user = :user and token.revokedAt is null")
    int revokeActiveTokensForUser(UserAccount user, Instant revokedAt);
}
