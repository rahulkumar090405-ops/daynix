package com.daynix.app.task.repository;

import com.daynix.app.task.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID>, JpaSpecificationExecutor<TimeSlot> {

	Optional<TimeSlot> findByIdAndCustomerId(UUID id, UUID customerId);
}
