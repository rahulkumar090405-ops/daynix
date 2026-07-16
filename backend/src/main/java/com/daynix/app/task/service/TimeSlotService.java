package com.daynix.app.task.service;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.common.exception.BadRequestException;
import com.daynix.app.common.exception.ResourceNotFoundException;
import com.daynix.app.task.dto.TimeSlotCreateRequest;
import com.daynix.app.task.dto.TimeSlotReorderItem;
import com.daynix.app.task.dto.TimeSlotReorderRequest;
import com.daynix.app.task.dto.TimeSlotResponse;
import com.daynix.app.task.dto.TimeSlotUpdateRequest;
import com.daynix.app.task.entity.TimeSlot;
import com.daynix.app.task.repository.TimeSlotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional(readOnly = true)
    public Page<TimeSlotResponse> list(AuthenticatedUser currentUser, UUID customerId, Boolean active, Integer intervalMinutes, Boolean archived, Pageable pageable) {
        Specification<TimeSlot> specification = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());
        UUID resolvedCustomerId = resolveReadCustomerId(currentUser, customerId);
        if (resolvedCustomerId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("customerId"), resolvedCustomerId));
        }
        if (active != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), active));
        }
        if (intervalMinutes != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("intervalMinutes"), intervalMinutes));
        }
        if (archived != null) {
            specification = specification.and((root, query, criteriaBuilder) -> archived
                    ? criteriaBuilder.isTrue(root.get("deleted"))
                    : criteriaBuilder.isFalse(root.get("deleted")));
        } else {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted")));
        }
        return timeSlotRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TimeSlotResponse getById(AuthenticatedUser currentUser, UUID id) {
        return toResponse(findAccessibleTimeSlot(currentUser, id));
    }

    @Transactional
    public TimeSlotResponse create(AuthenticatedUser currentUser, TimeSlotCreateRequest request) {
        UUID customerId = resolveWriteCustomerId(currentUser, request.customerId());
        validateTimeRange(request.startTime(), request.endTime());
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setCustomerId(customerId);
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());
        timeSlot.setDisplayOrder(request.displayOrder());
        timeSlot.setActive(request.active() == null || request.active());
        timeSlot.setIntervalMinutes(request.intervalMinutes());
        return toResponse(timeSlotRepository.save(timeSlot));
    }

    @Transactional
    public TimeSlotResponse update(AuthenticatedUser currentUser, UUID id, TimeSlotUpdateRequest request) {
        TimeSlot timeSlot = findAccessibleTimeSlot(currentUser, id);
        UUID customerId = resolveWriteCustomerId(currentUser, request.customerId());
        if (!timeSlot.getCustomerId().equals(customerId)) {
            throw new BadRequestException("Time slot customer scope cannot be changed");
        }
        validateTimeRange(request.startTime(), request.endTime());
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());
        timeSlot.setDisplayOrder(request.displayOrder());
        timeSlot.setIntervalMinutes(request.intervalMinutes());
        if (request.active() != null) {
            timeSlot.setActive(request.active());
        }
        return toResponse(timeSlotRepository.save(timeSlot));
    }

    @Transactional
    public TimeSlotResponse archive(AuthenticatedUser currentUser, UUID id) {
        TimeSlot timeSlot = findAccessibleTimeSlot(currentUser, id);
        timeSlot.setDeleted(true);
        return toResponse(timeSlotRepository.save(timeSlot));
    }

    @Transactional
    public void delete(AuthenticatedUser currentUser, UUID id) {
        TimeSlot timeSlot = findAccessibleTimeSlot(currentUser, id);
        timeSlotRepository.delete(timeSlot);
    }

    @Transactional
    public List<TimeSlotResponse> reorder(AuthenticatedUser currentUser, TimeSlotReorderRequest request) {
        UUID customerId = resolveWriteCustomerId(currentUser, request.customerId());
        List<TimeSlot> slots = timeSlotRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("customerId"), customerId));
        if (slots.size() != request.items().size()) {
            throw new BadRequestException("Reorder payload must include all time slots for the customer");
        }

        Set<UUID> knownIds = new HashSet<>();
        for (TimeSlotReorderItem item : request.items()) {
            if (!knownIds.add(item.id())) {
                throw new BadRequestException("Duplicate time slot id in reorder payload");
            }
            TimeSlot slot = slots.stream()
                    .filter(candidate -> candidate.getId().equals(item.id()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
            slot.setDisplayOrder(item.displayOrder());
        }

        return timeSlotRepository.saveAll(slots).stream().map(this::toResponse).toList();
    }

    private TimeSlot findAccessibleTimeSlot(AuthenticatedUser currentUser, UUID id) {
        if (currentUser.isSuperAdmin()) {
            return timeSlotRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
        }
        UUID customerId = requireCustomerId(currentUser);
        return timeSlotRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
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

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("endTime must be after startTime");
        }
    }

    private TimeSlotResponse toResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getCustomerId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getDisplayOrder(),
                timeSlot.isActive(),
                timeSlot.getIntervalMinutes(),
                timeSlot.isDeleted(),
                timeSlot.getCreatedAt(),
                timeSlot.getUpdatedAt()
        );
    }
}
