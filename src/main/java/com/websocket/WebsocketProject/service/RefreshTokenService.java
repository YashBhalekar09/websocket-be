package com.websocket.WebsocketProject.service;

import com.websocket.WebsocketProject.entity.RefreshToken;
import com.websocket.WebsocketProject.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(Long userId, String userAgent, String ipAddress) {

        RefreshToken refreshToken=new RefreshToken();
        refreshToken.setToken(generateSecureToken());
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS));
        refreshToken.setUserAgent(userAgent);
        refreshToken.setCreatedByIpAddress(ipAddress);
        return refreshTokenRepository.save(refreshToken);
    }

    private String generateSecureToken() {

        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(randomBytes);
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new RuntimeException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }
}
