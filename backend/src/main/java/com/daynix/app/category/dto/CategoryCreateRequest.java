package com.daynix.app.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryCreateRequest(
        UUID customerId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description
) {
}
