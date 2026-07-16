package com.daynix.app.auth.security;

import com.daynix.app.auth.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {

    private final SecretKey signingKey;
    private final long accessTokenMinutes;

    public JwtProvider(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(getAccessTokenExpiresInSeconds());

        return Jwts.builder()
                .subject(user.getEmail())
                .claims(Map.of(
                        "userId", user.getId().toString(),
                        "role", user.getRole().name(),
                        "customerId", user.getCustomerId() == null ? "" : user.getCustomerId().toString()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public boolean isValid(String token) {
        parseClaims(token);
        return true;
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenMinutes * 60;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
