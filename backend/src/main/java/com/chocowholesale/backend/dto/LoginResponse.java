package com.chocowholesale.backend.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String role,
    String name,
    Boolean isNewUser
) {
    public LoginResponse(String accessToken, String refreshToken, String role, String name) {
        this(accessToken, refreshToken, role, name, false);
    }
}
