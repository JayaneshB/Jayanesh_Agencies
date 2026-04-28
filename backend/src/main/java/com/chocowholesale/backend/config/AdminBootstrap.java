package com.chocowholesale.backend.config;

import com.chocowholesale.backend.entity.User;
import com.chocowholesale.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("owner@yourshop.in")) {
            User admin = new User();
            admin.setEmail("owner@yourshop.in");
            admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
            admin.setName("Shop Owner");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            log.info("Admin user created: owner@yourshop.in");
        } else {
            User admin = userRepository.findByEmail("owner@yourshop.in").orElse(null);
            if (admin != null && !passwordEncoder.matches("ChangeMe123!", admin.getPasswordHash())) {
                admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
                userRepository.save(admin);
                log.info("Admin user password reset to default");
            }
        }
    }
}
