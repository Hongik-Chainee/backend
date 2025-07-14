package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
