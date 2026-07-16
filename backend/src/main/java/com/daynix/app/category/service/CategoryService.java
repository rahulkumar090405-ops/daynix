package com.daynix.app.category.service;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.category.dto.CategoryCreateRequest;
import com.daynix.app.category.dto.CategoryResponse;
import com.daynix.app.category.dto.CategoryUpdateRequest;
import com.daynix.app.common.exception.BadRequestException;
import com.daynix.app.common.exception.ConflictException;
import com.daynix.app.common.exception.ResourceNotFoundException;
import com.daynix.app.reference.entity.Category;
import com.daynix.app.reference.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(AuthenticatedUser currentUser, UUID customerId, String search, Boolean archived, Pageable pageable) {
        Specification<Category> specification = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());
        UUID resolvedCustomerId = resolveReadCustomerId(currentUser, customerId);
        if (resolvedCustomerId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("customerId"), resolvedCustomerId));
        }
        if (StringUtils.hasText(search)) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern));
        }
        if (archived != null) {
            specification = specification.and((root, query, criteriaBuilder) -> archived
                    ? criteriaBuilder.isTrue(root.get("deleted"))
                    : criteriaBuilder.isFalse(root.get("deleted")));
        } else {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted")));
        }
        return categoryRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(AuthenticatedUser currentUser, UUID id) {
        return toResponse(findAccessibleCategory(currentUser, id));
    }

    @Transactional
    public CategoryResponse create(AuthenticatedUser currentUser, CategoryCreateRequest request) {
        UUID customerId = resolveWriteCustomerId(currentUser, request.customerId());
        String name = normalizeName(request.name());
        if (categoryRepository.existsByNameIgnoreCaseAndCustomerIdAndDeletedFalse(name, customerId)) {
            throw new ConflictException("Category name already exists");
        }

        Category category = new Category();
        category.setCustomerId(customerId);
        category.setName(name);
        category.setDescription(trimToNull(request.description()));
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(AuthenticatedUser currentUser, UUID id, CategoryUpdateRequest request) {
        Category category = findAccessibleCategory(currentUser, id);
        UUID customerId = resolveWriteCustomerId(currentUser, request.customerId());
        if (!category.getCustomerId().equals(customerId)) {
            throw new BadRequestException("Category customer scope cannot be changed");
        }

        String name = normalizeName(request.name());
        if (!category.getName().equalsIgnoreCase(name)
                && categoryRepository.existsByNameIgnoreCaseAndCustomerIdAndDeletedFalse(name, customerId)) {
            throw new ConflictException("Category name already exists");
        }

        category.setName(name);
        category.setDescription(trimToNull(request.description()));
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse archive(AuthenticatedUser currentUser, UUID id) {
        Category category = findAccessibleCategory(currentUser, id);
        category.setDeleted(true);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(AuthenticatedUser currentUser, UUID id) {
        Category category = findAccessibleCategory(currentUser, id);
        categoryRepository.delete(category);
    }

    private Category findAccessibleCategory(AuthenticatedUser currentUser, UUID id) {
        if (currentUser.isSuperAdmin()) {
            return categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        UUID customerId = requireCustomerId(currentUser);
        return categoryRepository.findByIdAndCustomerId(id, customerId)
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

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCustomerId(),
                category.getName(),
                category.getDescription(),
                category.isDeleted(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private String normalizeName(String value) {
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
