package com.chocowholesale.backend.repository;

import com.chocowholesale.backend.entity.Order;
import com.chocowholesale.backend.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :from AND o.createdAt <= :to")
    Page<Order> findByDateRange(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status NOT IN ('CANCELLED') AND o.createdAt >= :from AND o.createdAt <= :to")
    BigDecimal sumRevenueInRange(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('CANCELLED') AND o.createdAt >= :from AND o.createdAt <= :to")
    long countOrdersInRange(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    List<Order> findTop20ByOrderByCreatedAtDesc();

    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
