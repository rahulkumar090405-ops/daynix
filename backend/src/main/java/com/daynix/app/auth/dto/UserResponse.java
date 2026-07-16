package com.daynix.app.auth.dto;

import com.daynix.app.auth.entity.Role;
import com.daynix.app.auth.entity.UserAccount;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        UUID customerId
) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCustomerId()
        );
    }
}
