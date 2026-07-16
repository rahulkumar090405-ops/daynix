package com.daynix.app.tracker.controller;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.tracker.dto.TrackerCellResponse;
import com.daynix.app.tracker.dto.TrackerGridResponse;
import com.daynix.app.tracker.service.TrackerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/tracker")
@Validated
@Tag(name = "Tracker")
public class TrackerDashboardController {

    private final TrackerService trackerService;

    public TrackerDashboardController(TrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @GetMapping("/grid")
    @Operation(summary = "Get tracker grid for a customer month")
    public TrackerGridResponse grid(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Customer id") @PathVariable UUID customerId,
            @RequestParam @Min(2000) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        return trackerService.getGrid(currentUser, customerId, year, month);
    }

    @PatchMapping("/tasks/{taskId}/dates/{date}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cycle a single tracker cell status")
    public TrackerCellResponse cycleStatus(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Customer id") @PathVariable UUID customerId,
            @Parameter(description = "Task id") @PathVariable UUID taskId,
            @Parameter(description = "Cell date") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request
    ) {
        return trackerService.cycleCellStatus(currentUser, customerId, taskId, date, request);
    }
}
