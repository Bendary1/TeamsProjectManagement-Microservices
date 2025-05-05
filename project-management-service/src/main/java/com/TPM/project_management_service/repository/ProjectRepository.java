package com.TPM.project_management_service.repository;

import com.TPM.project_management_service.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Integer ownerId);
    List<Project> findByMemberIdsContaining(Integer userId);
    List<Project> findByAdminIdsContaining(Integer userId);
}