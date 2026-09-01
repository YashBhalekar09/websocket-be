package com.websocket.WebsocketProject.specification;

import com.websocket.WebsocketProject.dto.MessageFilterDTO;
import com.websocket.WebsocketProject.entity.Message;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class MessageSpecification {

    public static Specification<Message> getSpecification(MessageFilterDTO dto) {

        return hasConversationId(dto.getConversationId()).and(hasSenderId(dto.getSenderId())).and(hasSenderUsername(dto.getSenderUsername())).and(hasContent(dto.getContent())).and(hasSmartSearch(dto.getSmartSearch()));
    }

    public static Specification<Message> hasConversationId(Long conversationId) {

        return (root, query, cb) -> {
            if (conversationId == null) {
                return null;
            }
            return cb.equal(root.get("conversation").get("id"), conversationId);
        };
    }

    public static Specification<Message> hasSenderId(Long senderId) {

        return (root, query, cb) -> {

            if (senderId == null) {
                return null;
            }
            return cb.equal(root.get("sender").get("id"), senderId);
        };
    }

    public static Specification<Message> hasSenderUsername(String senderUsername) {

        return (root, query, cb) -> {

            if (senderUsername == null || senderUsername.trim().isEmpty()) {
                return null;
            }
            return cb.like(cb.lower(root.get("sender").get("username")), "%" + senderUsername.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Message> hasContent(String content) {

        return (root, query, cb) -> {

            if (content == null || content.trim().isEmpty()) {

                return null;
            }
            return cb.like(cb.lower(root.get("content")), "%" + content.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Message> hasSmartSearch(String smartSearch) {

        return (root, query, cb) -> {

            if (smartSearch == null || smartSearch.trim().isEmpty()) {

                return null;
            }

            String search = "%" + smartSearch.trim().toLowerCase() + "%";

            Predicate contentPredicate = cb.like(cb.lower(root.get("content")), search);
            Predicate usernamePredicate = cb.like(cb.lower(root.get("sender").get("username")), search);
            return cb.or(contentPredicate, usernamePredicate);
        };
    }
}