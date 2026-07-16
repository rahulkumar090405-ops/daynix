package com.daynix.app.auth;

import com.daynix.app.auth.dto.AuthResponse;
import com.daynix.app.auth.dto.ForgotPasswordRequest;
import com.daynix.app.auth.dto.LoginRequest;
import com.daynix.app.auth.dto.LogoutRequest;
import com.daynix.app.auth.dto.MessageResponse;
import com.daynix.app.auth.dto.PasswordResetInitResponse;
import com.daynix.app.auth.dto.RegisterRequest;
import com.daynix.app.auth.dto.ResetPasswordRequest;
import com.daynix.app.auth.dto.TokenRefreshRequest;
import com.daynix.app.auth.dto.UserResponse;
import com.daynix.app.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody LogoutRequest request) {
        return authService.logout(request.refreshToken());
    }

    @PostMapping("/forgot-password")
    public PasswordResetInitResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.currentUser(authentication);
    }
}
