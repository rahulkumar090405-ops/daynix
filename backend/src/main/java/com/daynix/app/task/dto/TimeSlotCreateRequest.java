package com.daynix.app.task.dto;

import com.daynix.app.common.validation.AllowedIntervalMinutes;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record TimeSlotCreateRequest(
        UUID customerId,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull Integer displayOrder,
        @NotNull @AllowedIntervalMinutes Integer intervalMinutes,
        Boolean active
) {
}
