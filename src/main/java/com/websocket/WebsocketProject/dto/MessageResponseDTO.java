package com.websocket.WebsocketProject.dto;

import java.time.LocalDateTime;

public class MessageResponseDTO {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime sentAt;

    public MessageResponseDTO() {
    }

    public MessageResponseDTO(Long id, Long conversationId, Long senderId, String senderUsername, String content, LocalDateTime sentAt) {

        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}