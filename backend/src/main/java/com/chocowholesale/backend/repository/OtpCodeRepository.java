package com.chocowholesale.backend.repository;

import com.chocowholesale.backend.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    Optional<OtpCode> findFirstByPhoneAndVerifiedFalseOrderByCreatedAtDesc(String phone);
}
