package com.daynix.app.task.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskTimeMappingCreateRequest(
        @NotNull UUID taskId,
        @NotNull UUID timeSlotId
) {
}
