package com.daynix.app.tracker.dto;

import com.daynix.app.task.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TrackerCellResponse(
        UUID taskId,
        UUID timeSlotId,
        LocalDate date,
        TaskStatus status,
        UUID taskLogId,
        Long version,
        Instant loggedAt
) {
}
