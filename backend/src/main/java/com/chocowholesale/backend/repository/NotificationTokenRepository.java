package com.chocowholesale.backend.repository;

import com.chocowholesale.backend.entity.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, UUID> {
    List<NotificationToken> findByUserId(UUID userId);
    boolean existsByUserIdAndToken(UUID userId, String token);
}
