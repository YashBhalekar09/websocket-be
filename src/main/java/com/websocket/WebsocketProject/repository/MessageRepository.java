package com.websocket.WebsocketProject.repository;

import com.websocket.WebsocketProject.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> , JpaSpecificationExecutor<Message> {

    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    List<Message> findByConversationIdAndSenderIdNotAndMessageReadFalse(Long conversationId, Long id);

    @Query("""
    SELECT COUNT(m)
    FROM Message m
    WHERE m.conversation.id = :conversationId
      AND m.sender.id <> :userId
      AND m.messageRead = false
""")
    long countUnreadMessages(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );
}