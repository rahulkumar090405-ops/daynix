package com.daynix.app.task.dto;

import java.time.LocalTime;
import java.util.UUID;

public record TaskTimeMappingResponse(
        UUID id,
        UUID customerId,
        UUID taskId,
        String taskTitle,
        UUID categoryId,
        String categoryName,
        UUID timeSlotId,
        LocalTime startTime,
        LocalTime endTime,
        Integer displayOrder
) {
}
