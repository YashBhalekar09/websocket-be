package com.websocket.WebsocketProject.entity;

import com.websocket.WebsocketProject.enums.ConversationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType conversationType;

    public Conversation() {
    }

    public Conversation(Long id, LocalDateTime createdAt, ConversationType conversationType) {
        this.id = id;
        this.createdAt = createdAt;
        this.conversationType = conversationType;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public ConversationType getConversationType() {return conversationType;}

    public void setConversationType(ConversationType conversationType) {this.conversationType = conversationType;}
}