package com.daynix.app.category.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID customerId,
        String name,
        String description,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {
}
