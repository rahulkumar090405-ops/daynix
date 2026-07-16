package com.daynix.app.tracker.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record TrackerRowResponse(
        UUID mappingId,
        UUID taskId,
        String taskTitle,
        UUID categoryId,
        String categoryName,
        UUID timeSlotId,
        LocalTime startTime,
        LocalTime endTime,
        Integer displayOrder,
        List<TrackerCellResponse> cells
) {
}
