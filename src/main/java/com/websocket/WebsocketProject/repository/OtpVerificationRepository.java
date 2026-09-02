package com.websocket.WebsocketProject.repository;

import com.websocket.WebsocketProject.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByUserIdAndVerifiedFalseOrderByCreatedAtDesc(Long userId);
}
