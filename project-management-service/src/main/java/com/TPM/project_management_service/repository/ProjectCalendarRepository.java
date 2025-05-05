package com.TPM.project_management_service.repository;

import com.TPM.project_management_service.model.ProjectCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendar, Long> {
    Optional<ProjectCalendar> findByProjectId(Long projectId);
}
