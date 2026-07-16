package com.daynix.app.tracker.service;

import com.daynix.app.audit.entity.AuditLog;
import com.daynix.app.audit.repository.AuditLogRepository;
import com.daynix.app.auth.entity.UserAccount;
import com.daynix.app.auth.exception.AuthException;
import com.daynix.app.auth.repository.UserAccountRepository;
import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.common.exception.BadRequestException;
import com.daynix.app.common.exception.ResourceNotFoundException;
import com.daynix.app.task.dto.TaskTimeMappingResponse;
import com.daynix.app.task.entity.Task;
import com.daynix.app.task.entity.TaskLog;
import com.daynix.app.task.entity.TaskStatus;
import com.daynix.app.task.entity.TaskTimeMapping;
import com.daynix.app.task.entity.TimeSlot;
import com.daynix.app.task.repository.TaskLogRepository;
import com.daynix.app.task.repository.TaskRepository;
import com.daynix.app.task.repository.TaskTimeMappingRepository;
import com.daynix.app.tracker.dto.TrackerCellResponse;
import com.daynix.app.tracker.dto.TrackerDayColumn;
import com.daynix.app.tracker.dto.TrackerGridResponse;
import com.daynix.app.tracker.dto.TrackerRowResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class TrackerService {

    private static final String AUDIT_ENTITY_NAME = "task_logs";
    private static final String AUDIT_ACTION_STATUS_CYCLED = "TASK_LOG_STATUS_CYCLED";

    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final TaskTimeMappingRepository taskTimeMappingRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserAccountRepository userAccountRepository;
    private final ObjectMapper objectMapper;

    public TrackerService(
            TaskRepository taskRepository,
            TaskLogRepository taskLogRepository,
            TaskTimeMappingRepository taskTimeMappingRepository,
            AuditLogRepository auditLogRepository,
            UserAccountRepository userAccountRepository,
            ObjectMapper objectMapper
    ) {
        this.taskRepository = taskRepository;
        this.taskLogRepository = taskLogRepository;
        this.taskTimeMappingRepository = taskTimeMappingRepository;
        this.auditLogRepository = auditLogRepository;
        this.userAccountRepository = userAccountRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public TrackerGridResponse getGrid(AuthenticatedUser currentUser, UUID customerId, int year, int month) {
        UUID resolvedCustomerId = resolveReadCustomerId(currentUser, customerId);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<TaskTimeMapping> mappings = taskTimeMappingRepository.findAllForCustomer(resolvedCustomerId);
        List<TaskLog> logs = taskLogRepository.findAllByTaskCustomerIdAndTaskDateBetweenAndDeletedFalse(resolvedCustomerId, startDate, endDate);
        Map<String, TaskLog> logIndex = new HashMap<>();
        for (TaskLog log : logs) {
            logIndex.put(key(log.getTask().getId(), log.getTaskDate()), log);
        }

        List<TrackerDayColumn> days = new ArrayList<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            days.add(new TrackerDayColumn(
                    day,
                    day.getDayOfMonth(),
                    day.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH),
                    day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY
            ));
        }

        List<TrackerRowResponse> rows = new ArrayList<>();
        for (TaskTimeMapping mapping : mappings) {
            rows.add(toRowResponse(mapping, days, logIndex));
        }

        return new TrackerGridResponse(resolvedCustomerId, year, month, days, rows);
    }

    @Transactional
    public TrackerCellResponse cycleCellStatus(AuthenticatedUser currentUser, UUID customerId, UUID taskId, LocalDate date, HttpServletRequest request) {
        UUID resolvedCustomerId = resolveWriteCustomerId(currentUser, customerId);
        Task task = resolveTask(currentUser, taskId);
        if (!task.getCustomerId().equals(resolvedCustomerId)) {
            throw new BadRequestException("Task does not belong to the requested customer");
        }

        UserAccount actor = userAccountRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AuthException("Authenticated user was not found"));

        TaskLog taskLog = taskLogRepository.findByTaskIdAndTaskDate(taskId, date).orElseGet(TaskLog::new);
        TaskStatus fromStatus = taskLog.getStatus() == null ? TaskStatus.PENDING : taskLog.getStatus();
        TaskStatus nextStatus = cycleStatus(fromStatus);
        boolean isNew = taskLog.getId() == null;

        taskLog.setTask(task);
        taskLog.setUser(actor);
        taskLog.setTaskDate(date);
        taskLog.setLoggedAt(Instant.now());
        taskLog.setStatus(nextStatus);
        taskLog.setCreatedBy(isNew ? currentUser.getId() : taskLog.getCreatedBy());
        taskLog.setUpdatedBy(currentUser.getId());

        TaskLog saved = taskLogRepository.save(taskLog);
        writeAuditLog(actor, saved, fromStatus, nextStatus, request);
        return toCellResponse(saved);
    }

    private TrackerRowResponse toRowResponse(TaskTimeMapping mapping, List<TrackerDayColumn> days, Map<String, TaskLog> logIndex) {
        Task task = mapping.getTask();
        TimeSlot timeSlot = mapping.getTimeSlot();
        List<TrackerCellResponse> cells = new ArrayList<>(days.size());
        for (TrackerDayColumn day : days) {
            TaskLog log = logIndex.get(key(task.getId(), day.date()));
            cells.add(toCellResponse(task, timeSlot, day.date(), log));
        }
        return new TrackerRowResponse(
                mapping.getId(),
                task.getId(),
                task.getTitle(),
                task.getCategory().getId(),
                task.getCategory().getName(),
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getDisplayOrder(),
                cells
        );
    }

    private TrackerCellResponse toCellResponse(Task task, TimeSlot timeSlot, LocalDate date, TaskLog log) {
        if (log == null) {
            return new TrackerCellResponse(task.getId(), timeSlot.getId(), date, TaskStatus.PENDING, null, null, null);
        }
        return new TrackerCellResponse(task.getId(), timeSlot.getId(), date, log.getStatus(), log.getId(), log.getVersion(), log.getLoggedAt());
    }

    private TrackerCellResponse toCellResponse(TaskLog log) {
        return new TrackerCellResponse(
                log.getTask().getId(),
                null,
                log.getTaskDate(),
                log.getStatus(),
                log.getId(),
                log.getVersion(),
                log.getLoggedAt()
        );
    }

    private void writeAuditLog(UserAccount actor, TaskLog saved, TaskStatus fromStatus, TaskStatus toStatus, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUser(actor);
        auditLog.setEntityName(AUDIT_ENTITY_NAME);
        auditLog.setEntityId(saved.getId());
        auditLog.setAction(AUDIT_ACTION_STATUS_CYCLED);
        auditLog.setOccurredAt(Instant.now());
        auditLog.setCreatedBy(actor.getId());
        auditLog.setUpdatedBy(actor.getId());
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        try {
            auditLog.setChangesJson(objectMapper.writeValueAsString(Map.of(
                    "customerId", saved.getTask().getCustomerId(),
                    "taskId", saved.getTask().getId(),
                    "taskDate", saved.getTaskDate().toString(),
                    "fromStatus", fromStatus.name(),
                    "toStatus", toStatus.name(),
                    "taskLogId", saved.getId(),
                    "version", saved.getVersion()
            )));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize audit log payload", exception);
        }
        auditLogRepository.save(auditLog);
    }

    private TaskLog findTaskLog(Task task, LocalDate date) {
        return taskLogRepository.findByTaskIdAndTaskDate(task.getId(), date)
                .orElseThrow(() -> new ResourceNotFoundException("Task log not found"));
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

    private UUID resolveReadCustomerId(AuthenticatedUser currentUser, UUID requestedCustomerId) {
        if (currentUser.isSuperAdmin()) {
            return requestedCustomerId;
        }
        UUID customerId = requireCustomerId(currentUser);
        if (requestedCustomerId != null && !requestedCustomerId.equals(customerId)) {
            throw new BadRequestException("Customer scope cannot be changed");
        }
        return customerId;
    }

    private UUID resolveWriteCustomerId(AuthenticatedUser currentUser, UUID requestedCustomerId) {
        if (currentUser.isSuperAdmin()) {
            if (requestedCustomerId == null) {
                throw new BadRequestException("customerId is required for super admin requests");
            }
            return requestedCustomerId;
        }
        UUID customerId = requireCustomerId(currentUser);
        if (requestedCustomerId != null && !requestedCustomerId.equals(customerId)) {
            throw new BadRequestException("Customer scope cannot be changed");
        }
        return customerId;
    }

    private UUID requireCustomerId(AuthenticatedUser currentUser) {
        if (currentUser.getCustomerId() == null) {
            throw new BadRequestException("Customer account is missing a customer scope");
        }
        return currentUser.getCustomerId();
    }

    private TaskStatus cycleStatus(TaskStatus currentStatus) {
        return switch (currentStatus) {
            case PENDING -> TaskStatus.COMPLETED;
            case COMPLETED -> TaskStatus.MISSED;
            case MISSED -> TaskStatus.PENDING;
        };
    }

    private String key(UUID taskId, LocalDate date) {
        return taskId + "|" + date;
    }
}
