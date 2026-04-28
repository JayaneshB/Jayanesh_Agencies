package com.chocowholesale.backend.repository;

import com.chocowholesale.backend.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findByOrderId(UUID orderId, Pageable pageable);
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);
}
