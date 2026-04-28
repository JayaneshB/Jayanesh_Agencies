package com.chocowholesale.backend.service;

import com.chocowholesale.backend.dto.LoginResponse;
import com.chocowholesale.backend.entity.OtpCode;
import com.chocowholesale.backend.entity.User;
import com.chocowholesale.backend.repository.OtpCodeRepository;
import com.chocowholesale.backend.repository.UserRepository;
import com.chocowholesale.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Random;

@Service
public class OtpAuthService {

    private static final Logger log = LoggerFactory.getLogger(OtpAuthService.class);
    private final OtpCodeRepository otpRepo;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final Random random = new Random();

    public OtpAuthService(OtpCodeRepository otpRepo, UserRepository userRepo, JwtUtil jwtUtil) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String requestOtp(String phone) {
        if (phone == null || phone.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone number");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));

        OtpCode otp = new OtpCode();
        otp.setPhone(phone);
        otp.setCode(code);
        otp.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        otpRepo.save(otp);

        // TODO: Integrate SMS provider (MSG91 / Twilio)
        log.info("OTP for {}: {} (dev only — replace with SMS)", phone, code);
        return "OTP sent";
    }

    @Transactional
    public LoginResponse verifyOtp(String phone, String code) {
        OtpCode otp = otpRepo.findFirstByPhoneAndVerifiedFalseOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No OTP found for this number"));

        if (otp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }
        if (!otp.getCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        otp.setVerified(true);
        otpRepo.save(otp);

        User user = userRepo.findByPhone(phone).orElseGet(() -> {
            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setRole("CUSTOMER");
            newUser.setName(phone);
            return userRepo.save(newUser);
        });

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        boolean isNewUser = user.getName().equals(phone);

        return new LoginResponse(accessToken, refreshToken, user.getRole(), user.getName(), isNewUser);
    }
}
