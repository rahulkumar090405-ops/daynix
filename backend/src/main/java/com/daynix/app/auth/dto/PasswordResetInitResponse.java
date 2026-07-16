package com.daynix.app.auth.dto;

public record PasswordResetInitResponse(
        String message,
        String resetToken
) {
}
