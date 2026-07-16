package com.daynix.app.task.service;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.common.exception.BadRequestException;
import com.daynix.app.common.exception.ConflictException;
import com.daynix.app.common.exception.ResourceNotFoundException;
import com.daynix.app.reference.entity.Category;
import com.daynix.app.task.dto.TaskTimeMappingCreateRequest;
import com.daynix.app.task.dto.TaskTimeMappingResponse;
import com.daynix.app.task.dto.TaskTimeMappingUpdateRequest;
import com.daynix.app.task.entity.Task;
import com.daynix.app.task.entity.TaskTimeMapping;
import com.daynix.app.task.entity.TimeSlot;
import com.daynix.app.task.repository.TaskRepository;
import com.daynix.app.task.repository.TaskTimeMappingRepository;
import com.daynix.app.task.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TaskTimeMappingService {

    private final TaskTimeMappingRepository taskTimeMappingRepository;
    private final TaskRepository taskRepository;
    private final TimeSlotRepository timeSlotRepository;

    public TaskTimeMappingService(
            TaskTimeMappingRepository taskTimeMappingRepository,
            TaskRepository taskRepository,
            TimeSlotRepository timeSlotRepository
    ) {
        this.taskTimeMappingRepository = taskTimeMappingRepository;
        this.taskRepository = taskRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional
    public TaskTimeMappingResponse create(AuthenticatedUser currentUser, TaskTimeMappingCreateRequest request) {
        Task task = resolveTask(currentUser, request.taskId());
        TimeSlot timeSlot = resolveTimeSlot(currentUser, request.timeSlotId());
        ensureSameCustomer(task, timeSlot);
        if (taskTimeMappingRepository.existsByTaskIdAndTimeSlotId(task.getId(), timeSlot.getId())) {
            throw new ConflictException("Task time mapping already exists");
        }

        TaskTimeMapping mapping = new TaskTimeMapping();
        mapping.setTask(task);
        mapping.setTimeSlot(timeSlot);
        return toResponse(taskTimeMappingRepository.save(mapping));
    }

    @Transactional
    public TaskTimeMappingResponse update(AuthenticatedUser currentUser, UUID id, TaskTimeMappingUpdateRequest request) {
        TaskTimeMapping mapping = findAccessibleMapping(currentUser, id);
        Task task = resolveTask(currentUser, request.taskId());
        TimeSlot timeSlot = resolveTimeSlot(currentUser, request.timeSlotId());
        ensureSameCustomer(task, timeSlot);
        if (!mapping.getTask().getId().equals(task.getId())
                || !mapping.getTimeSlot().getId().equals(timeSlot.getId())) {
            taskTimeMappingRepository.findByTaskIdAndTimeSlotId(task.getId(), timeSlot.getId())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(mapping.getId())) {
                            throw new ConflictException("Task time mapping already exists");
                        }
                    });
        }
        mapping.setTask(task);
        mapping.setTimeSlot(timeSlot);
        return toResponse(taskTimeMappingRepository.save(mapping));
    }

    @Transactional
    public void delete(AuthenticatedUser currentUser, UUID id) {
        TaskTimeMapping mapping = findAccessibleMapping(currentUser, id);
        taskTimeMappingRepository.delete(mapping);
    }

    private TaskTimeMapping findAccessibleMapping(AuthenticatedUser currentUser, UUID id) {
        if (currentUser.isSuperAdmin()) {
            return taskTimeMappingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task time mapping not found"));
        }
        UUID customerId = requireCustomerId(currentUser);
        return taskTimeMappingRepository.findByIdAndTaskCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Task time mapping not found"));
    }

    private Task resolveTask(AuthenticatedUser currentUser, UUID taskId) {
        if (currentUser.isSuperAdmin()) {
            return taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        }
        UUID customerId = requireCustomerId(currentUser);
        return taskRepository.findByIdAndCustomerId(taskId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private TimeSlot resolveTimeSlot(AuthenticatedUser currentUser, UUID timeSlotId) {
        if (currentUser.isSuperAdmin()) {
            return timeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
        }
        UUID customerId = requireCustomerId(currentUser);
        return timeSlotRepository.findByIdAndCustomerId(timeSlotId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
    }

    private void ensureSameCustomer(Task task, TimeSlot timeSlot) {
        if (!task.getCustomerId().equals(timeSlot.getCustomerId())) {
            throw new BadRequestException("Task and time slot must belong to the same customer");
        }
    }

    private UUID requireCustomerId(AuthenticatedUser currentUser) {
        if (currentUser.getCustomerId() == null) {
            throw new BadRequestException("Customer account is missing a customer scope");
        }
        return currentUser.getCustomerId();
    }

    private TaskTimeMappingResponse toResponse(TaskTimeMapping mapping) {
        Task task = mapping.getTask();
        Category category = task.getCategory();
        TimeSlot timeSlot = mapping.getTimeSlot();
        return new TaskTimeMappingResponse(
                mapping.getId(),
                task.getCustomerId(),
                task.getId(),
                task.getTitle(),
                category.getId(),
                category.getName(),
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getDisplayOrder()
        );
    }
}
