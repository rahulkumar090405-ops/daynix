package com.daynix.app.task.controller;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.task.dto.TaskTimeMappingCreateRequest;
import com.daynix.app.task.dto.TaskTimeMappingResponse;
import com.daynix.app.task.dto.TaskTimeMappingUpdateRequest;
import com.daynix.app.task.service.TaskTimeMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers/task-time-mappings")
@Tag(name = "Task Time Mappings")
public class TaskTimeMappingController {

    private final TaskTimeMappingService taskTimeMappingService;

    public TaskTimeMappingController(TaskTimeMappingService taskTimeMappingService) {
        this.taskTimeMappingService = taskTimeMappingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create task time mapping")
    public TaskTimeMappingResponse create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody TaskTimeMappingCreateRequest request
    ) {
        return taskTimeMappingService.create(currentUser, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task time mapping")
    public TaskTimeMappingResponse update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Mapping id") @PathVariable UUID id,
            @Valid @RequestBody TaskTimeMappingUpdateRequest request
    ) {
        return taskTimeMappingService.update(currentUser, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete task time mapping")
    public void delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Mapping id") @PathVariable UUID id
    ) {
        taskTimeMappingService.delete(currentUser, id);
    }
}
