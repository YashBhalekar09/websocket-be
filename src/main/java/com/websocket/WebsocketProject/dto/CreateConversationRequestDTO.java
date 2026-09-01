package com.websocket.WebsocketProject.dto;

import com.websocket.WebsocketProject.enums.ConversationType;

import java.util.List;

public class CreateConversationRequestDTO {

    private List<Long> userIds;
    private ConversationType type;
    private String groupName;

    public ConversationType getType() {return type;}

    public void setType(ConversationType type) {this.type = type;}

    public List<Long> getUserIds() {return userIds;}

    public void setUserIds(List<Long> userIds) {this.userIds = userIds;}

    public String getGroupName() {return groupName;}

    public void setGroupName(String groupName) {this.groupName = groupName;}
}