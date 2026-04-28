package com.chocowholesale.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret = "default-secret-change-me-in-production-must-be-at-least-64-chars-long!!";
    private long accessExpirationMs = 3600000;   // 1 hour
    private long refreshExpirationMs = 604800000; // 7 days

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getAccessExpirationMs() { return accessExpirationMs; }
    public void setAccessExpirationMs(long accessExpirationMs) { this.accessExpirationMs = accessExpirationMs; }
    public long getRefreshExpirationMs() { return refreshExpirationMs; }
    public void setRefreshExpirationMs(long refreshExpirationMs) { this.refreshExpirationMs = refreshExpirationMs; }
}
