package com.chocowholesale.backend.web;

import com.chocowholesale.backend.dto.*;
import com.chocowholesale.backend.entity.User;
import com.chocowholesale.backend.repository.UserRepository;
import com.chocowholesale.backend.service.OtpAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class CustomerAuthController {

    private final OtpAuthService otpAuthService;
    private final UserRepository userRepo;

    public CustomerAuthController(OtpAuthService otpAuthService, UserRepository userRepo) {
        this.otpAuthService = otpAuthService;
        this.userRepo = userRepo;
    }

    @PostMapping("/otp/request")
    public ResponseEntity<Map<String, String>> requestOtp(@RequestBody OtpRequest req) {
        String msg = otpAuthService.requestOtp(req.phone());
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<LoginResponse> verifyOtp(@RequestBody OtpVerifyRequest req) {
        return ResponseEntity.ok(otpAuthService.verifyOtp(req.phone(), req.code()));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UUID userId) {
        User u = userRepo.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of(
            "id", u.getId(),
            "phone", u.getPhone() != null ? u.getPhone() : "",
            "name", u.getName() != null ? u.getName() : "",
            "businessName", u.getBusinessName() != null ? u.getBusinessName() : "",
            "gstin", u.getGstin() != null ? u.getGstin() : "",
            "role", u.getRole()
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, String>> updateProfile(
            @AuthenticationPrincipal UUID userId, @RequestBody ProfileUpdateRequest req) {
        User u = userRepo.findById(userId).orElseThrow();
        if (req.name() != null) u.setName(req.name());
        if (req.businessName() != null) u.setBusinessName(req.businessName());
        if (req.gstin() != null) u.setGstin(req.gstin());
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }
}
