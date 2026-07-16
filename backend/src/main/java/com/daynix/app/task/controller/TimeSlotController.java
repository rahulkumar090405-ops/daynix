package com.daynix.app.task.controller;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.task.dto.TimeSlotCreateRequest;
import com.daynix.app.task.dto.TimeSlotReorderRequest;
import com.daynix.app.task.dto.TimeSlotResponse;
import com.daynix.app.task.dto.TimeSlotUpdateRequest;
import com.daynix.app.task.service.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/time-slots")
@Tag(name = "Time Slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    @Operation(summary = "List time slots")
    public Page<TimeSlotResponse> list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer intervalMinutes,
            @RequestParam(required = false) Boolean archived,
            @ParameterObject Pageable pageable
    ) {
        return timeSlotService.list(currentUser, customerId, active, intervalMinutes, archived, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get time slot by id")
    public TimeSlotResponse getById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Time slot id") @PathVariable UUID id
    ) {
        return timeSlotService.getById(currentUser, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create time slot")
    public TimeSlotResponse create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody TimeSlotCreateRequest request
    ) {
        return timeSlotService.create(currentUser, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update time slot")
    public TimeSlotResponse update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Time slot id") @PathVariable UUID id,
            @Valid @RequestBody TimeSlotUpdateRequest request
    ) {
        return timeSlotService.update(currentUser, id, request);
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive time slot")
    public TimeSlotResponse archive(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Time slot id") @PathVariable UUID id
    ) {
        return timeSlotService.archive(currentUser, id);
    }

    @PatchMapping("/reorder")
    @Operation(summary = "Reorder time slots")
    public List<TimeSlotResponse> reorder(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody TimeSlotReorderRequest request
    ) {
        return timeSlotService.reorder(currentUser, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete time slot")
    public void delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Time slot id") @PathVariable UUID id
    ) {
        timeSlotService.delete(currentUser, id);
    }
}
