package com.daynix.app.task.repository;

import com.daynix.app.task.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskLogRepository extends JpaRepository<TaskLog, UUID> {

	Optional<TaskLog> findByTaskIdAndTaskDate(UUID taskId, LocalDate taskDate);

	List<TaskLog> findAllByTaskCustomerIdAndTaskDateBetweenAndDeletedFalse(UUID customerId, LocalDate startDate, LocalDate endDate);
}
