package com.websocket.WebsocketProject.dto;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public class MessageResponseDTO {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime sentAt;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private boolean messageDelivered;
    private boolean messageRead;
    private LocalDateTime messageDeliveredAt;
    private LocalDateTime messageReadAt;


    public MessageResponseDTO() {
    }

    public MessageResponseDTO(Long id, Long conversationId, Long senderId, String senderUsername, String content, LocalDateTime sentAt, String fileUrl, String fileName, String fileType, Long fileSize, boolean messageDelivered, boolean messageRead, LocalDateTime messageDeliveredAt, LocalDateTime messageReadAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.sentAt = sentAt;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.messageDelivered = messageDelivered;
        this.messageRead = messageRead;
        this.messageDeliveredAt = messageDeliveredAt;
        this.messageReadAt = messageReadAt;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isMessageDelivered() {
        return messageDelivered;
    }

    public void setMessageDelivered(boolean messageDelivered) {
        this.messageDelivered = messageDelivered;
    }

    public boolean isMessageRead() {
        return messageRead;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    public LocalDateTime getMessageDeliveredAt() {
        return messageDeliveredAt;
    }

    public void setMessageDeliveredAt(LocalDateTime messageDeliveredAt) {
        this.messageDeliveredAt = messageDeliveredAt;
    }

    public LocalDateTime getMessageReadAt() {
        return messageReadAt;
    }

    public void setMessageReadAt(LocalDateTime messageReadAt) {
        this.messageReadAt = messageReadAt;
    }
}