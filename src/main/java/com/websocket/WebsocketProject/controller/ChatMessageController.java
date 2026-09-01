package com.websocket.WebsocketProject.controller;

import com.websocket.WebsocketProject.dto.ConversationResponseDTO;
import com.websocket.WebsocketProject.dto.GroupMemberUpdateRequestDTO;
import com.websocket.WebsocketProject.dto.MessageResponseDTO;
import com.websocket.WebsocketProject.dto.MessageStatusRequestDTO;
import com.websocket.WebsocketProject.entity.ChatMessage;
import com.websocket.WebsocketProject.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

        // Get authenticated user from STOMP session
        String username = (String) headerAccessor
                        .getSessionAttributes().get("username");

        Long userId = (Long) headerAccessor
                        .getSessionAttributes().get("userId");

        if (username == null || userId == null) {
            throw new IllegalArgumentException("WebSocket user is not authenticated");
        }

        // Save message
        MessageResponseDTO response =
                conversationService.sendMessage(
                        conversationId,
                        chatMessage.getContent(),
                        username
                );

        // Broadcast message
        messagingTemplate.convertAndSend(
                "/topic/conversations/" +
                        conversationId, response
        );
    }

    @MessageMapping("/message/delivered")
    public void messageDelivered(MessageStatusRequestDTO request) {

        conversationService.markMessageDelivered(
                request.getMessageId(),
                request.getUsername()
        );
    }

    @MessageMapping("/conversation/read")
    public void markConversationAsRead(
            MessageStatusRequestDTO request) {

        conversationService.markConversationMessagesAsRead(
                request.getConversationId(),
                request.getUsername()
        );
    }

}