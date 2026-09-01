package com.websocket.WebsocketProject.repository;

import com.websocket.WebsocketProject.entity.RefreshToken;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken , Long> {

    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);

}
