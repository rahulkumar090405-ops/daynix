package com.daynix.app.tracker.dto;

import java.time.LocalDate;

public record TrackerDayColumn(
        LocalDate date,
        int dayOfMonth,
        String dayName,
        boolean weekend
) {
}
