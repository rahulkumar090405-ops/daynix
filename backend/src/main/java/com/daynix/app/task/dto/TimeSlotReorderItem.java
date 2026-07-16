package com.daynix.app.task.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TimeSlotReorderItem(
        @NotNull UUID id,
        @NotNull Integer displayOrder
) {
}
