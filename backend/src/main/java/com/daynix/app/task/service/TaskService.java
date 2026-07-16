package com.daynix.app.task.service;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.category.dto.CategoryResponse;
import com.daynix.app.common.exception.BadRequestException;
import com.daynix.app.common.exception.ConflictException;
import com.daynix.app.common.exception.ResourceNotFoundException;
import com.daynix.app.reference.entity.Category;
import com.daynix.app.reference.repository.CategoryRepository;
import com.daynix.app.task.dto.TaskCreateRequest;
import com.daynix.app.task.dto.TaskResponse;
import com.daynix.app.task.dto.TaskUpdateRequest;
import com.daynix.app.task.entity.Task;
import com.daynix.app.task.entity.TaskStatus;
import com.daynix.app.task.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(AuthenticatedUser currentUser, UUID customerId, UUID categoryId, com.daynix.app.task.entity.Priority priority, Boolean active, Boolean archived, Pageable pageable) {
        Specification<Task> specification = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());
        UUID resolvedCustomerId = resolveReadCustomerId(currentUser, customerId);
        if (resolvedCustomerId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("customerId"), resolvedCustomerId));
        }
        if (categoryId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category").get("id"), categoryId));
        }
        if (priority != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority));
        }
        if (active != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("activeStatus"), active));
        }
        if (archived != null) {
            specification = specification.and((root, query, criteriaBuilder) -> archived
                    ? criteriaBuilder.isTrue(root.get("deleted"))
                    : criteriaBuilder.isFalse(root.get("deleted")));
        } else {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted")));
        }
        return taskRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(AuthenticatedUser currentUser, UUID id) {
        return toResponse(findAccessibleTask(currentUser, id));
    }

    @Transactional
    public TaskResponse create(AuthenticatedUser currentUser, TaskCreateRequest request) {
        UUID customerId = resolveWriteCustomerId(currentUser, request.customerId());
        Category category = findAccessibleCategory(customerId, request.categoryId());
        Task task = new Task();
        task.setCustomerId(customerId);
        task.setCategory(category);
        task.setTitle(normalize(request.title()));
        task.setDescription(trimToNull(request.description()));
        task.setPriority(request.priority());
        task.setEstimatedMinutes(request.estimatedMinutes());
        task.setColor(trimToNull(request.color()));
        task.setActive(true);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(AuthenticatedUser currentUser, UUID id, TaskUpdateRequest request) {
        Task task = findAccessibleTask(currentUser, id);
        UUID customerId = resolveWriteCustomerId(currentUser, request.customerId());
        if (!task.getCustomerId().equals(customerId)) {
            throw new BadRequestException("Task customer scope cannot be changed");
        }
        Category category = findAccessibleCategory(customerId, request.categoryId());
        task.setCategory(category);
        task.setTitle(normalize(request.title()));
        task.setDescription(trimToNull(request.description()));
        task.setPriority(request.priority());
        task.setEstimatedMinutes(request.estimatedMinutes());
        task.setColor(trimToNull(request.color()));
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse archive(AuthenticatedUser currentUser, UUID id) {
        Task task = findAccessibleTask(currentUser, id);
        task.setDeleted(true);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(AuthenticatedUser currentUser, UUID id) {
        Task task = findAccessibleTask(currentUser, id);
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse activate(AuthenticatedUser currentUser, UUID id) {
        Task task = findAccessibleTask(currentUser, id);
        task.setActive(true);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse deactivate(AuthenticatedUser currentUser, UUID id) {
        Task task = findAccessibleTask(currentUser, id);
        task.setActive(false);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse duplicate(AuthenticatedUser currentUser, UUID id) {
        Task task = findAccessibleTask(currentUser, id);
        Task copy = new Task();
        copy.setCustomerId(task.getCustomerId());
        copy.setCategory(task.getCategory());
        copy.setTitle(task.getTitle() + " (Copy)");
        copy.setDescription(task.getDescription());
        copy.setPriority(task.getPriority());
        copy.setEstimatedMinutes(task.getEstimatedMinutes());
        copy.setColor(task.getColor());
        copy.setActive(task.isActive());
        return toResponse(taskRepository.save(copy));
    }

    private Task findAccessibleTask(AuthenticatedUser currentUser, UUID id) {
        if (currentUser.isSuperAdmin()) {
            return taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        }
        UUID customerId = requireCustomerId(currentUser);
        return taskRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private Category findAccessibleCategory(UUID customerId, UUID categoryId) {
        return categoryRepository.findByIdAndCustomerId(categoryId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
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

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getCustomerId(),
                task.getCategory().getId(),
                task.getCategory().getName(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getEstimatedMinutes(),
                task.getColor(),
                task.isActive(),
                task.isDeleted(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
