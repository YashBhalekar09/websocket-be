package com.websocket.WebsocketProject.dto;

public class MessageFilterDTO {

    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private String smartSearch;

    public MessageFilterDTO() {}

    public Long getConversationId() {return conversationId;}

    public void setConversationId(Long conversationId) {this.conversationId = conversationId;}

    public Long getSenderId() {return senderId;}

    public void setSenderId(Long senderId) {this.senderId = senderId;}

    public String getSenderUsername() {return senderUsername;}

    public void setSenderUsername(String senderUsername) {this.senderUsername = senderUsername;}

    public String getContent() {return content;}

    public void setContent(String content) {this.content = content;}

    public String getSmartSearch() {return smartSearch;}

    public void setSmartSearch(String smartSearch) {this.smartSearch = smartSearch;}
}