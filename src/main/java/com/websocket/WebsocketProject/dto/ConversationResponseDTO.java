package com.websocket.WebsocketProject.dto;

import com.websocket.WebsocketProject.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationResponseDTO {

    private Long conversationId;
    private LocalDateTime createdAt;
    private List<Long> userIds;
    private ConversationType conversionType;

    public ConversationResponseDTO(Long conversationId, LocalDateTime createdAt, List<Long> userIds, ConversationType conversionType) {
        this.conversationId = conversationId;
        this.createdAt = createdAt;
        this.userIds = userIds;
        this.conversionType = conversionType;
    }

    public Long getConversationId() {return conversationId;}

    public void setConversationId(Long conversationId) {this.conversationId = conversationId;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public List<Long> getUserIds() {return userIds;}

    public void setUserIds(List<Long> userIds) {this.userIds = userIds;}

    public ConversationType getType() {return conversionType;}

    public void setType(ConversationType conversionType) {this.conversionType = conversionType;}
}