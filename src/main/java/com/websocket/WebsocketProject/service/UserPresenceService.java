package com.websocket.WebsocketProject.service;

import com.websocket.WebsocketProject.entity.User;
import com.websocket.WebsocketProject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPresenceService {

    private final UserRepository userRepository;

    public UserPresenceService(
            UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateOnlineStatus(Long userId, boolean online) {

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setOnline(online);
        userRepository.save(user);

        System.out.println("Online status changed: " + user.getUsername()+ " -> "+ online);
    }
}