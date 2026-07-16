package com.daynix.app.task.controller;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.task.dto.TaskCreateRequest;
import com.daynix.app.task.dto.TaskResponse;
import com.daynix.app.task.dto.TaskUpdateRequest;
import com.daynix.app.task.entity.Priority;
import com.daynix.app.task.service.TaskService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/customers/tasks")
@Tag(name = "Tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "List tasks")
    public Page<TaskResponse> list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean archived,
            @ParameterObject Pageable pageable
    ) {
        return taskService.list(currentUser, customerId, categoryId, priority, active, archived, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    public TaskResponse getById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Task id") @PathVariable UUID id
    ) {
        return taskService.getById(currentUser, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create task")
    public TaskResponse create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        return taskService.create(currentUser, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public TaskResponse update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Task id") @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        return taskService.update(currentUser, id, request);
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive task")
    public TaskResponse archive(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Task id") @PathVariable UUID id
    ) {
        return taskService.archive(currentUser, id);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate task")
    public TaskResponse activate(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Task id") @PathVariable UUID id
    ) {
        return taskService.activate(currentUser, id);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate task")
    public TaskResponse deactivate(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Task id") @PathVariable UUID id
    ) {
        return taskService.deactivate(currentUser, id);
    }

    @PostMapping("/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Duplicate task")
    public TaskResponse duplicate(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Task id") @PathVariable UUID id
    ) {
        return taskService.duplicate(currentUser, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete task")
    public void delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Task id") @PathVariable UUID id
    ) {
        taskService.delete(currentUser, id);
    }
}
