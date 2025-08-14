package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findAllByOwnerId(Long ownerId);
}
