package com.daynix.app.task.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record TimeSlotReorderRequest(
        UUID customerId,
        @NotEmpty @Valid List<TimeSlotReorderItem> items
) {
}
