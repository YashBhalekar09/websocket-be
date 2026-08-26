package com.websocket.WebsocketProject.security;

import com.websocket.WebsocketProject.dto.UserResponseDTO;
import com.websocket.WebsocketProject.service.JwtService;
import com.websocket.WebsocketProject.service.UserPresenceService;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JWTChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserPresenceService userPresenceService;
    private final SimpMessagingTemplate messagingTemplate;

    // =========================================
    // SESSION -> AUTHENTICATION
    // =========================================
    private final Map<String, UsernamePasswordAuthenticationToken> authenticatedUsers = new ConcurrentHashMap<>();


    // =========================================
    // USER -> ACTIVE WEBSOCKET SESSIONS
    // =========================================
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();


    public JWTChannelInterceptor(JwtService jwtService, UserPresenceService userPresenceService,@Lazy SimpMessagingTemplate messagingTemplate) {
        this.jwtService = jwtService;
        this.userPresenceService = userPresenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        String sessionId = accessor.getSessionId();

        // =========================================
        // CONNECT
        // =========================================

        if (StompCommand.CONNECT.equals(command)) {
            String auth = accessor.getFirstNativeHeader("Authorization");

            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
            String token = auth.substring(7);

            try {

                String username = jwtService.extractUserName(token);

                Long userId = jwtService.extractUserId(token);

                if (username == null || userId == null || !jwtService.isValidToken(token)) {

                    throw new IllegalArgumentException("Invalid token");
                }

                // =========================================
                // CREATE AUTHENTICATION
                // =========================================

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

                authentication.setDetails(userId);
                // =========================================
                // STORE SESSION -> USER
                // =========================================
                authenticatedUsers.put(sessionId, authentication);

                // =========================================
                // STORE USER -> SESSION
                // =========================================
                userSessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(sessionId);

                // =========================================
                // SESSION ATTRIBUTES
                // =========================================
                accessor.getSessionAttributes().put("username", username);
                accessor.getSessionAttributes().put("userId", userId);
                // =========================================
                // SET PRINCIPAL
                // =========================================
                accessor.setUser(authentication);

                // ========================================
                // USER ONLINE
                // =========================================
                userPresenceService.updateOnlineStatus(userId, true);
                messagingTemplate.convertAndSend(
                        "/topic/user-status", new UserResponseDTO(userId, username,null, true)
                );

                System.out.println("=================================");
                System.out.println("WebSocket CONNECT");
                System.out.println("Username : " + username);
                System.out.println("User ID  : " + userId);
                System.out.println("Session  : " + sessionId);
                System.out.println("Active sessions for user: " + userSessions.get(userId).size());
                System.out.println("=================================");
            } catch (Exception e) {
                System.out.println("WebSocket JWT error: " + e.getMessage());
                throw new IllegalArgumentException("Invalid JWT " + e.getMessage());
            }
        }

        // =========================================
        // OTHER MESSAGES
        // =========================================
        else if (sessionId != null) {
            UsernamePasswordAuthenticationToken authentication =
                    authenticatedUsers.get(sessionId);

            if (authentication != null) {
                accessor.setUser(authentication);
                System.out.println("WebSocket user restored: " + authentication.getName() + " | command: " + command);
            } else {

                System.out.println("No authenticated user for session: " + sessionId + " | command: " + command);
            }
        }


        // =========================================
        // DISCONNECT
        // =========================================
        if (StompCommand.DISCONNECT.equals(command) && sessionId != null) {
            UsernamePasswordAuthenticationToken authentication =
                    authenticatedUsers.remove(sessionId);
            if (authentication != null) {
                Long userId = (Long) authentication.getDetails();

                // =========================================
                // REMOVE THIS SESSION
                // =========================================
                Set<String> sessions = userSessions.get(userId);
                if (sessions != null) {
                    sessions.remove(sessionId);
                    // =========================================
                    // NO MORE SESSIONS
                    // =========================================
                    if (sessions.isEmpty()) {
                        userSessions.remove(userId);
                        userPresenceService.updateOnlineStatus(userId, false);

                        messagingTemplate.convertAndSend(
                                "/topic/user-status",
                                new UserResponseDTO(
                                        userId, authentication.getName(),null, false)
                        );
                        System.out.println("User OFFLINE: " + authentication.getName() + " | userId: " + userId);
                    } else {

                        System.out.println("User still ONLINE: " + authentication.getName() + " | remaining sessions: " + sessions.size());
                    }
                }
            }

            System.out.println("WebSocket session removed: " + sessionId);
        }

        return message;
    }
}