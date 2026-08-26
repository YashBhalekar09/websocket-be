package com.websocket.WebsocketProject.controller;

import com.websocket.WebsocketProject.dto.MessageResponseDTO;
import com.websocket.WebsocketProject.entity.ChatMessage;
import com.websocket.WebsocketProject.service.ConversationService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationService conversationService;

    public ChatMessageController(
            SimpMessagingTemplate messagingTemplate,
            ConversationService conversationService) {

        this.messagingTemplate = messagingTemplate;
        this.conversationService = conversationService;
    }

    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(@DestinationVariable Long conversationId,
            ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        System.out.println(
                "===== CHAT MESSAGE RECEIVED ====="
        );

        System.out.println(
                "Conversation ID : " + conversationId
        );

        System.out.println(
                "Session ID : " +
                        headerAccessor.getSessionId()
        );

        System.out.println(
                "Session attributes : " +
                        headerAccessor.getSessionAttributes()
        );

        // Get authenticated user from STOMP session
        String username =
                (String) headerAccessor
                        .getSessionAttributes()
                        .get("username");

        Long userId =
                (Long) headerAccessor
                        .getSessionAttributes()
                        .get("userId");

        System.out.println(
                "Username : " + username
        );

        System.out.println(
                "User ID : " + userId
        );

        System.out.println(
                "Content : " +
                        chatMessage.getContent()
        );

        if (username == null || userId == null) {

            throw new IllegalArgumentException(
                    "WebSocket user is not authenticated"
            );
        }

        // Save message
        MessageResponseDTO response =
                conversationService.sendMessage(
                        conversationId,
                        chatMessage.getContent(),
                        username
                );

        System.out.println(
                "Message saved with ID : " +
                        response.getId()
        );

        // Broadcast message
        messagingTemplate.convertAndSend(
                "/topic/conversations/" +
                        conversationId,
                response
        );

        System.out.println(
                "Message broadcasted to conversation : " +
                        conversationId
        );
    }
}