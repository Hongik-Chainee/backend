package com.chaineeproject.chainee.kyc;

import org.springframework.data.jpa.repository.JpaRepository;
public interface KycSessionRepository extends JpaRepository<KycSession, String> {}
