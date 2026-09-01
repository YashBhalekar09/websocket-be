package com.websocket.WebsocketProject.dto;

import com.websocket.WebsocketProject.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationResponseDTO {

    private Long conversationId;
    private LocalDateTime createdAt;
    private List<Long> userIds;
    private ConversationType conversionType;
    private long unreadCount;
    private String groupName;
    private List<ConversationMemberDTO> members;


    public ConversationResponseDTO(Long conversationId, LocalDateTime createdAt, List<Long> userIds, ConversationType conversionType, long unreadCount, String groupName, List<ConversationMemberDTO> members) {
        this.conversationId = conversationId;
        this.createdAt = createdAt;
        this.userIds = userIds;
        this.conversionType = conversionType;
        this.unreadCount = unreadCount;
        this.groupName = groupName;
        this.members = members;
    }

    public Long getConversationId() {return conversationId;}

    public void setConversationId(Long conversationId) {this.conversationId = conversationId;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public List<Long> getUserIds() {return userIds;}

    public void setUserIds(List<Long> userIds) {this.userIds = userIds;}

    public ConversationType getType() {return conversionType;}

    public void setType(ConversationType conversionType) {this.conversionType = conversionType;}

    public ConversationType getConversionType() {
        return conversionType;
    }

    public void setConversionType(ConversationType conversionType) {
        this.conversionType = conversionType;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getGroupName() {return groupName;}

    public void setGroupName(String groupName) {this.groupName = groupName;}

    public List<ConversationMemberDTO> getMembers() {return members;}

    public void setMembers(List<ConversationMemberDTO> members) {this.members = members;}
}