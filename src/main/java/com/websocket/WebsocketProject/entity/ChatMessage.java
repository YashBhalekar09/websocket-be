package com.websocket.WebsocketProject.entity;

public class ChatMessage {

    //new private/particular user
    private Long conversationId;

    private String sender;
    private String content;

    public ChatMessage() {
    }

    public ChatMessage(Long conversationId, String sender, String content) {
        this.conversationId = conversationId;
        this.sender = sender;
        this.content = content;
    }

    public String getSender() {return sender;}

    public void setSender(String sender) {this.sender = sender;}

    public String getContent() {return content;}

    public void setContent(String content) {this.content = content;}

    public Long getConversationId() {return conversationId;}

    public void setConversationId(Long conversationId) {this.conversationId = conversationId;}
}
