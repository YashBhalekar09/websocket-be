package com.websocket.WebsocketProject.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id",nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id",nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private String fileUrl;

    @Column
    private String fileName;

    @Column
    private String fileType;

    @Column
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean messageDelivered = false;

    @Column(nullable = false)
    private boolean messageRead = false;

    private LocalDateTime messageDeliveredAt;

    private LocalDateTime messageReadAt;

    public Message() {
    }

    public Long getId() {
        return id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void setId(Long id) {this.id = id;}

    public String getFileUrl() {return fileUrl;}

    public void setFileUrl(String fileUrl) {this.fileUrl = fileUrl;}

    public String getFileName() {return fileName;}

    public void setFileName(String fileName) {this.fileName = fileName;}

    public String getFileType() {return fileType;}

    public void setFileType(String fileType) {this.fileType = fileType;}

    public Long getFileSize() {return fileSize;}

    public void setFileSize(Long fileSize) {this.fileSize = fileSize;}

    public boolean isMessageDelivered() {return messageDelivered;}

    public void setMessageDelivered(boolean messageDelivered) {this.messageDelivered = messageDelivered;}

    public boolean isMessageRead() {return messageRead;}

    public void setMessageRead(boolean messageRead) {this.messageRead = messageRead;}

    public LocalDateTime getMessageDeliveredAt() {return messageDeliveredAt;}

    public void setMessageDeliveredAt(LocalDateTime messageDeliveredAt) {this.messageDeliveredAt = messageDeliveredAt;}

    public LocalDateTime getMessageReadAt() {return messageReadAt;}

    public void setMessageReadAt(LocalDateTime messageReadAt) {this.messageReadAt = messageReadAt;}
}