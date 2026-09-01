package com.websocket.WebsocketProject.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long refreshTokenId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_by_ip_address")
    private String createdByIpAddress;

    @Column(name = "seller_id")
    private Long sellerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean shouldShuffleToken() {
        return Duration.between(
                createdAt,
                LocalDateTime.now()
        ).getSeconds() > 3.5 * 24 * 60 * 60;
    }

    // getters and setters
    public Long getRefreshTokenId() {return refreshTokenId;}

    public void setRefreshTokenId(Long refreshTokenId) {this.refreshTokenId = refreshTokenId;}

    public String getToken() {return token;}

    public void setToken(String token) {this.token = token;}

    public Long getUserId() {return userId;}

    public void setUserId(Long userId) {this.userId = userId;}

    public LocalDateTime getExpiresAt() {return expiresAt;}

    public void setExpiresAt(LocalDateTime expiresAt) {this.expiresAt = expiresAt;}

    public LocalDateTime getRevokedAt() {return revokedAt;}

    public void setRevokedAt(LocalDateTime revokedAt) {this.revokedAt = revokedAt;}

    public String getUserAgent() {return userAgent;}

    public void setUserAgent(String userAgent) {this.userAgent = userAgent;}

    public String getCreatedByIpAddress() {return createdByIpAddress;}

    public void setCreatedByIpAddress(String createdByIpAddress) {this.createdByIpAddress = createdByIpAddress;}

    public Long getSellerId() {return sellerId;}

    public void setSellerId(Long sellerId) {this.sellerId = sellerId;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}