package com.daynix.app.task.dto;

import com.daynix.app.task.entity.Priority;
import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID customerId,
        UUID categoryId,
        String categoryName,
        String title,
        String description,
        Priority priority,
        Integer estimatedMinutes,
        String color,
        boolean activeStatus,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {
}
