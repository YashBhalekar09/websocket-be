package com.websocket.WebsocketProject.service;

import com.websocket.WebsocketProject.dto.LoginResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1 hour

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId,String username) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(username)
                .claim("userId",userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserName(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token){
        Object userId = extractAllClaims(token).get("userId");

        if(userId == null){
            return null;
        }
        return ((Number) userId).longValue();
    }

    public boolean isValidToken(String token) {
        try{
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }





}
