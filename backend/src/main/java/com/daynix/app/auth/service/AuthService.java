package com.daynix.app.auth.service;

import com.daynix.app.auth.dto.AuthResponse;
import com.daynix.app.auth.dto.ForgotPasswordRequest;
import com.daynix.app.auth.dto.LoginRequest;
import com.daynix.app.auth.dto.MessageResponse;
import com.daynix.app.auth.dto.PasswordResetInitResponse;
import com.daynix.app.auth.dto.RegisterRequest;
import com.daynix.app.auth.dto.ResetPasswordRequest;
import com.daynix.app.auth.dto.TokenRefreshRequest;
import com.daynix.app.auth.dto.UserResponse;
import com.daynix.app.auth.entity.PasswordResetToken;
import com.daynix.app.auth.entity.RefreshToken;
import com.daynix.app.auth.entity.Role;
import com.daynix.app.auth.entity.UserAccount;
import com.daynix.app.auth.exception.AuthException;
import com.daynix.app.auth.exception.DuplicateEmailException;
import com.daynix.app.auth.repository.PasswordResetTokenRepository;
import com.daynix.app.auth.repository.RefreshTokenRepository;
import com.daynix.app.auth.repository.UserAccountRepository;
import com.daynix.app.auth.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final long refreshTokenDays;
    private final long passwordResetTokenMinutes;

    public AuthService(
            UserAccountRepository userAccountRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays,
            @Value("${app.password-reset.token-minutes}") long passwordResetTokenMinutes
    ) {
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.refreshTokenDays = refreshTokenDays;
        this.passwordResetTokenMinutes = passwordResetTokenMinutes;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException("Email is already registered");
        }

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(Role.ROLE_CUSTOMER);
        user.setCustomerId(UUID.randomUUID());
        user.setEnabled(true);
        user.setLocked(false);

        UserAccount savedUser = userAccountRepository.save(user);
        return issueTokens(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    normalizeEmail(request.email()),
                    request.password()
            ));
        } catch (BadCredentialsException ex) {
            throw new AuthException("Invalid email or password");
        } catch (DisabledException ex) {
            throw new AuthException("User account is disabled");
        } catch (LockedException ex) {
            throw new AuthException("User account is locked");
        }

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new AuthException("Invalid email or password"));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(sha256(request.refreshToken()))
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (!refreshToken.isActive()) {
            throw new AuthException("Refresh token is expired or revoked");
        }

        UserAccount user = refreshToken.getUser();
        ensureUserCanAuthenticate(user);
        refreshToken.revoke();
        return issueTokens(user);
    }

    @Transactional
    public MessageResponse logout(String refreshTokenValue) {
        refreshTokenRepository.findByTokenHash(sha256(refreshTokenValue))
                .ifPresent(refreshToken -> {
                    if (refreshToken.getRevokedAt() == null) {
                        refreshToken.revoke();
                    }
                });
        return new MessageResponse("Logged out");
    }

    @Transactional
    public PasswordResetInitResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<UserAccount> user = userAccountRepository.findByEmailIgnoreCase(request.email());
        if (user.isEmpty()) {
            return new PasswordResetInitResponse("If the account exists, a password reset link will be issued", null);
        }

        String resetTokenValue = generateOpaqueToken();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user.get());
        resetToken.setTokenHash(sha256(resetTokenValue));
        resetToken.setExpiresAt(Instant.now().plusSeconds(passwordResetTokenMinutes * 60));
        passwordResetTokenRepository.save(resetToken);

        return new PasswordResetInitResponse(
                "Password reset token generated. Wire this token into email delivery before production.",
                resetTokenValue
        );
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(sha256(request.token()))
                .orElseThrow(() -> new AuthException("Invalid password reset token"));

        if (!resetToken.isActive()) {
            throw new AuthException("Password reset token is expired or already used");
        }

        UserAccount user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetRequired(false);
        resetToken.markUsed();
        refreshTokenRepository.revokeActiveTokensForUser(user, Instant.now());

        return new MessageResponse("Password has been reset");
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AuthException("Authentication is required");
        }

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AuthException("Authenticated user was not found"));
        return UserResponse.from(user);
    }

    private AuthResponse issueTokens(UserAccount user) {
        ensureUserCanAuthenticate(user);
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtProvider.getAccessTokenExpiresInSeconds(),
                UserResponse.from(user)
        );
    }

    private String createRefreshToken(UserAccount user) {
        String refreshTokenValue = generateOpaqueToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(sha256(refreshTokenValue));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshTokenDays * 24 * 60 * 60));
        refreshTokenRepository.save(refreshToken);
        return refreshTokenValue;
    }

    private void ensureUserCanAuthenticate(UserAccount user) {
        if (!user.isEnabled()) {
            throw new AuthException("User account is disabled");
        }
        if (user.isLocked()) {
            throw new AuthException("User account is locked");
        }
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
