package com.daynix.app.tracker.dto;

import java.util.List;
import java.util.UUID;

public record TrackerGridResponse(
        UUID customerId,
        int year,
        int month,
        List<TrackerDayColumn> days,
        List<TrackerRowResponse> rows
) {
}
