package com.TPM.project_management_service.repository;

import com.TPM.project_management_service.model.TimeTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeTrackingRepository extends JpaRepository<TimeTracking, Long> {
    List<TimeTracking> findByTaskId(Long taskId);
    List<TimeTracking> findByUserId(Integer userId);
    List<TimeTracking> findByTaskIdAndUserId(Long taskId, Integer userId);
    List<TimeTracking> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
