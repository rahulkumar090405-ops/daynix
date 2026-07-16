package com.daynix.app.task.dto;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record TimeSlotResponse(
        UUID id,
        UUID customerId,
        LocalTime startTime,
        LocalTime endTime,
        Integer displayOrder,
        boolean active,
        Integer intervalMinutes,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {
}
