package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findTopByOwnerIdAndIsPublicTrueOrderByCompletedAtDescCreatedAtDesc(Long ownerId);
    Optional<Project> findTopByOwnerIdOrderByCompletedAtDescCreatedAtDesc(Long ownerId);
    List<Project> findAllByOwnerIdAndIdIn(Long ownerId, List<Long> ids);
}