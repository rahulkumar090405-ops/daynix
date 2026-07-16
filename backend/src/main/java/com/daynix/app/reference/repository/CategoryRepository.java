package com.daynix.app.reference.repository;

import com.daynix.app.reference.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findByIdAndCustomerId(UUID id, UUID customerId);

    boolean existsByNameIgnoreCaseAndCustomerIdAndDeletedFalse(String name, UUID customerId);
}
