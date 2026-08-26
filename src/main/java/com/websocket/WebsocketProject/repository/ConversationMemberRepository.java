package com.websocket.WebsocketProject.repository;

import com.websocket.WebsocketProject.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    List<ConversationMember> findByUserId(Long userId);

    List<ConversationMember> findByConversationId(Long conversationId);

    @Query(value = """
        SELECT cm.conversation_id
        FROM conversation_members cm
        JOIN conversations c
            ON c.id = cm.conversation_id
        WHERE c.conversation_type = 'PRIVATE'
          AND cm.user_id IN (:userIds)
        GROUP BY cm.conversation_id
        HAVING COUNT(DISTINCT cm.user_id) = :userCount
        """,
    nativeQuery = true)
    List<Long> findPrivateConversationIdsForUsers(
            @Param("userIds") List<Long> userIds,
            @Param("userCount") long userCount
    );

}