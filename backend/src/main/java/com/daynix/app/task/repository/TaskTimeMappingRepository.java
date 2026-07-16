package com.daynix.app.task.repository;

import com.daynix.app.task.entity.TaskTimeMapping;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskTimeMappingRepository extends JpaRepository<TaskTimeMapping, UUID> {

    Optional<TaskTimeMapping> findByTaskIdAndTimeSlotId(UUID taskId, UUID timeSlotId);

    Optional<TaskTimeMapping> findByIdAndTaskCustomerId(UUID id, UUID customerId);

    boolean existsByTaskIdAndTimeSlotId(UUID taskId, UUID timeSlotId);

    @Query("""
            select mapping
            from TaskTimeMapping mapping
            join fetch mapping.task task
            join fetch task.category category
            join fetch mapping.timeSlot timeSlot
            where task.customerId = :customerId
              and task.deleted = false
              and category.deleted = false
              and timeSlot.deleted = false
            order by timeSlot.displayOrder asc, task.title asc
            """)
    List<TaskTimeMapping> findAllForCustomer(@Param("customerId") UUID customerId);
}
