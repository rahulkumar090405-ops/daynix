package com.daynix.app.task.dto;

import com.daynix.app.task.entity.Priority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TaskCreateRequest(
        UUID customerId,
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String description,
        @NotNull Priority priority,
        @NotNull @Min(1) Integer estimatedMinutes,
        @Size(max = 24) String color
) {
}
