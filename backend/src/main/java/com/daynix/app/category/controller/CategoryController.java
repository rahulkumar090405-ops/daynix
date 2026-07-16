package com.daynix.app.category.controller;

import com.daynix.app.auth.security.AuthenticatedUser;
import com.daynix.app.category.dto.CategoryCreateRequest;
import com.daynix.app.category.dto.CategoryResponse;
import com.daynix.app.category.dto.CategoryUpdateRequest;
import com.daynix.app.category.service.CategoryService;
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
@RequestMapping("/api/customers/categories")
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List categories")
    public Page<CategoryResponse> list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean archived,
            @ParameterObject Pageable pageable
    ) {
        return categoryService.list(currentUser, customerId, search, archived, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id")
    public CategoryResponse getById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Category id") @PathVariable UUID id
    ) {
        return categoryService.getById(currentUser, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create category")
    public CategoryResponse create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        return categoryService.create(currentUser, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public CategoryResponse update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Category id") @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return categoryService.update(currentUser, id, request);
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive category")
    public CategoryResponse archive(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Category id") @PathVariable UUID id
    ) {
        return categoryService.archive(currentUser, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category")
    public void delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "Category id") @PathVariable UUID id
    ) {
        categoryService.delete(currentUser, id);
    }
}
