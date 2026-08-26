package com.websocket.WebsocketProject.repository;

import com.websocket.WebsocketProject.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {


}