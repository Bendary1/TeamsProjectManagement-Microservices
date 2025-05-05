package com.TPM.project_management_service.repository;

import com.TPM.project_management_service.model.Task;
import com.TPM.project_management_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssigneeId(Integer assigneeId);
    List<Task> findByCreatorId(Integer creatorId);
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    List<Task> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);
    List<Task> findByParentTaskId(Long parentTaskId);
}
