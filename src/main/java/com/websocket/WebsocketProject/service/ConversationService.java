package com.websocket.WebsocketProject.service;

import com.websocket.WebsocketProject.dto.ConversationResponseDTO;
import com.websocket.WebsocketProject.dto.CreateConversationRequestDTO;
import com.websocket.WebsocketProject.dto.MessageResponseDTO;
import com.websocket.WebsocketProject.entity.Conversation;
import com.websocket.WebsocketProject.entity.ConversationMember;
import com.websocket.WebsocketProject.entity.Message;
import com.websocket.WebsocketProject.entity.User;
import com.websocket.WebsocketProject.enums.ConversationType;
import com.websocket.WebsocketProject.repository.ConversationMemberRepository;
import com.websocket.WebsocketProject.repository.ConversationRepository;
import com.websocket.WebsocketProject.repository.MessageRepository;
import com.websocket.WebsocketProject.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    private final UserRepository userRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationService(UserRepository userRepository, ConversationMemberRepository conversationMemberRepository, ConversationRepository conversationRepository, MessageRepository messageRepository) {

        this.userRepository = userRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public User getAuthenticatedUser(String username) {

        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean isMember(Long conversationId, Long userId) {

        return conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
    }

    public List<MessageResponseDTO> getMessages(Long conversationId, String username) {

        // 1. Find authenticated user
        User user = getAuthenticatedUser(username);

        // 2. Check conversation exists
        conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 3. Check membership
        boolean member = isMember(conversationId, user.getId());

        if (!member) {
            throw new RuntimeException("User is not a member of this conversation");
        }

        // 4. Get messages
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        // 5. Convert Entity → DTO
        return messages.stream().map(message ->
                new MessageResponseDTO(message.getId(),
                        message.getConversation().getId(),
                        message.getSender().getId(),
                        message.getSender().getUsername(),
                        message.getContent(), message.getSentAt())
        ).toList();
    }

    public MessageResponseDTO sendMessage(Long conversationId, String content, String username) {

        // 1. Find authenticated user
        User user = getAuthenticatedUser(username);

        // 2. Check membership
        boolean member = isMember(conversationId, user.getId());

        if (!member) {
            throw new RuntimeException("User is not a member of this conversation");
        }

        // 3. Find conversation
        Conversation conversation = conversationRepository
                        .findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 4. Create message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(user);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        // 5. Save message
        Message savedMessage = messageRepository.save(message);

        // 6. Create response DTO
        return new MessageResponseDTO(
                savedMessage.getId(),
                conversationId,
                user.getId(),
                user.getUsername(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
        );
    }

    public ConversationResponseDTO createConversation(CreateConversationRequestDTO requestDTO) {

        // Get currently logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String username = authentication.getName();
        User currentUser = getAuthenticatedUser(username);

        // Validate request
        if (requestDTO.getUserIds() == null || requestDTO.getUserIds().isEmpty()) {
            throw new RuntimeException("At least one user is required");
        }

        if (requestDTO.getType() == null) {
            throw new RuntimeException("Conversation type is required");
        }

        // PRIVATE CONVERSATION
        if (requestDTO.getType() == ConversationType.PRIVATE) {
            if (requestDTO.getUserIds().size() != 1) {
                throw new RuntimeException("Private conversation must contain exactly one other user");
            }

            Long otherUserId = requestDTO.getUserIds().get(0);

            // Cannot chat with yourself
            if (currentUser.getId().equals(otherUserId)) {
                throw new RuntimeException("You cannot create a conversation with yourself");
            }

            // Check existing private conversation
            List<Long> userIds = List.of(currentUser.getId(), otherUserId);

            List<Long> existingConversationIds = conversationMemberRepository.findPrivateConversationIdsForUsers(userIds, 2);

            // Existing conversation found
            if (!existingConversationIds.isEmpty()) {

                Long conversationId = existingConversationIds.get(0);

                Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));

                return new ConversationResponseDTO(conversation.getId(), conversation.getCreatedAt(), userIds, conversation.getConversationType());
            }

            // Find other user
            User otherUser = userRepository.findById(otherUserId).orElseThrow(() -> new RuntimeException("User not found"));

            // Create private conversation
            Conversation conversation = new Conversation();
            conversation.setConversationType(ConversationType.PRIVATE);
            Conversation savedConversation = conversationRepository.save(conversation);

            // Add current user
            ConversationMember member1 = new ConversationMember();
            member1.setConversation(savedConversation);
            member1.setUser(currentUser);
            conversationMemberRepository.save(member1);

            // Add other user
            ConversationMember member2 = new ConversationMember();
            member2.setConversation(savedConversation);
            member2.setUser(otherUser);
            conversationMemberRepository.save(member2);

            return new ConversationResponseDTO(savedConversation.getId(), savedConversation.getCreatedAt(), userIds, savedConversation.getConversationType());
        }

        // GROUP CONVERSATION
        if (requestDTO.getType() == ConversationType.GROUP) {
            List<Long> allUserIds = new ArrayList<>(requestDTO.getUserIds());
            // Automatically add logged-in user
            if (!allUserIds.contains(currentUser.getId())) {
                allUserIds.add(currentUser.getId());
            }

            // Remove duplicate IDs
            allUserIds = allUserIds.stream().distinct().toList();

            // Minimum 2 members
            if (allUserIds.size() < 2) {
                throw new RuntimeException("Group conversation must have at least 2 users");
            }

            // Find users
            List<User> users = userRepository.findAllById(allUserIds);

            if (users.size() != allUserIds.size()) {
                throw new RuntimeException("One or more users not found");
            }

            // Create group
            Conversation conversation = new Conversation();
            conversation.setConversationType(ConversationType.GROUP);
            Conversation savedConversation = conversationRepository.save(conversation);

            // Add all members
            for (User user : users) {
                ConversationMember member = new ConversationMember();
                member.setConversation(savedConversation);
                member.setUser(user);
                conversationMemberRepository.save(member);
            }
            return new ConversationResponseDTO(savedConversation.getId(), savedConversation.getCreatedAt(), allUserIds, savedConversation.getConversationType());
        }

        // Invalid type
        throw new RuntimeException("Unsupported conversation type");
    }

    public List<ConversationResponseDTO> getMyConversations(String username) {

        // 1. Find authenticated user
        User user = getAuthenticatedUser(username);

        // 2. Find conversations where user is a member
        List<ConversationMember> memberships = conversationMemberRepository.findByUserId(user.getId());

        // 3. Convert to DTO
        return memberships.stream().map(member -> {

            Conversation conversation = member.getConversation();

            List<Long> userIds = conversationMemberRepository.findByConversationId(conversation.getId())
                    .stream()
                    .map(conversationMember ->
                            conversationMember.getUser().getId()).toList();

            return new ConversationResponseDTO(conversation.getId(), conversation.getCreatedAt(), userIds,conversation.getConversationType());
        }).toList();
    }
}