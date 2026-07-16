package com.daynix.app.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authz")
public class CustomerAccessEvaluator {

    public boolean canAccessCustomerId(UUID customerId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return false;
        }
        return user.isSuperAdmin() || customerId.equals(user.getCustomerId());
    }
}
