package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
}
