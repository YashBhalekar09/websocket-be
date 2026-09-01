package com.websocket.WebsocketProject.dto;

public class MessageStatusRequestDTO {

    private Long messageId;
    private Long conversationId;
    private String username;

    public MessageStatusRequestDTO() {
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}