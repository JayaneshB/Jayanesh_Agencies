package com.chocowholesale.backend.web;

import com.chocowholesale.backend.entity.NotificationToken;
import com.chocowholesale.backend.entity.User;
import com.chocowholesale.backend.repository.NotificationTokenRepository;
import com.chocowholesale.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationTokenRepository tokenRepo;
    private final UserRepository userRepo;

    public NotificationController(NotificationTokenRepository tokenRepo, UserRepository userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> registerToken(
            @AuthenticationPrincipal UUID userId, @RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token required"));
        }
        if (tokenRepo.existsByUserIdAndToken(userId, token)) {
            return ResponseEntity.ok(Map.of("message", "Already registered"));
        }
        User user = userRepo.findById(userId).orElseThrow();
        NotificationToken nt = new NotificationToken();
        nt.setUser(user);
        nt.setToken(token);
        nt.setPlatform(body.getOrDefault("platform", "ANDROID"));
        tokenRepo.save(nt);
        return ResponseEntity.ok(Map.of("message", "Token registered"));
    }
}
