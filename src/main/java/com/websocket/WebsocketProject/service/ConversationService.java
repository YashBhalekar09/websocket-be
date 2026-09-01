package com.websocket.WebsocketProject.service;

import com.websocket.WebsocketProject.dto.*;
import com.websocket.WebsocketProject.entity.Conversation;
import com.websocket.WebsocketProject.entity.ConversationMember;
import com.websocket.WebsocketProject.entity.Message;
import com.websocket.WebsocketProject.entity.User;
import com.websocket.WebsocketProject.enums.ConversationType;
import com.websocket.WebsocketProject.helper.PaginationHelper;
import com.websocket.WebsocketProject.repository.ConversationMemberRepository;
import com.websocket.WebsocketProject.repository.ConversationRepository;
import com.websocket.WebsocketProject.repository.MessageRepository;
import com.websocket.WebsocketProject.repository.UserRepository;
import com.websocket.WebsocketProject.specification.MessageSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final UserRepository userRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversationService(UserRepository userRepository, ConversationMemberRepository conversationMemberRepository, ConversationRepository conversationRepository, MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate) {

        this.userRepository = userRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
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
        return messages.stream()
                .map(message ->
                        new MessageResponseDTO(
                                message.getId(),
                                message.getConversation().getId(),
                                message.getSender().getId(),
                                message.getSender().getUsername(),
                                message.getContent(),
                                message.getSentAt(),
                                message.getFileUrl(),
                                message.getFileName(),
                                message.getFileType(),
                                message.getFileSize(),
                                message.isMessageDelivered(),
                                message.isMessageRead(),
                                message.getMessageDeliveredAt(),
                                message.getMessageReadAt()
                        )
                )
                .toList();
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
                savedMessage.getSentAt(),
                savedMessage.getFileUrl(),
                savedMessage.getFileName(),
                savedMessage.getFileType(),
                savedMessage.getFileSize(),
                savedMessage.isMessageDelivered(),
                savedMessage.isMessageRead(),
                savedMessage.getMessageDeliveredAt(),
                savedMessage.getMessageReadAt()

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

                return new ConversationResponseDTO(conversation.getId(), conversation.getCreatedAt(), userIds, conversation.getConversationType(), 0,conversation.getName(),null);
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

            return new ConversationResponseDTO(savedConversation.getId(), savedConversation.getCreatedAt(), userIds, savedConversation.getConversationType(),0, savedConversation.getName(),null);
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
            conversation.setName(requestDTO.getGroupName());
            Conversation savedConversation = conversationRepository.save(conversation);

            // Add all members
            for (User user : users) {
                ConversationMember member = new ConversationMember();
                member.setConversation(savedConversation);
                member.setUser(user);
                conversationMemberRepository.save(member);
            }
            return new ConversationResponseDTO(savedConversation.getId(), savedConversation.getCreatedAt(), allUserIds, savedConversation.getConversationType(),0,savedConversation.getName(),null);
        }

        // Invalid type
        throw new RuntimeException("Unsupported conversation type");
    }

    public List<ConversationResponseDTO> getMyConversations(String username) {

        // 1. Find authenticated user
        User user = getAuthenticatedUser(username);

        // 2. Find conversations where user is a member
        List<ConversationMember> memberships =
                conversationMemberRepository.findByUserId(user.getId());

        // 3. Convert to DTO
        return memberships.stream().map(member -> {

            Conversation conversation = member.getConversation();

            List<ConversationMember> conversationMembers =
                    conversationMemberRepository
                            .findByConversationId(conversation.getId());

            List<Long> userIds =
                    conversationMembers.stream()
                            .map(conversationMember ->
                                    conversationMember.getUser().getId())
                            .toList();

            List<ConversationMemberDTO> members =
                    conversationMembers.stream()
                            .map(conversationMember -> {

                                User memberUser =
                                        conversationMember.getUser();

                                return new ConversationMemberDTO(
                                        memberUser.getId(),
                                        memberUser.getUsername(),
                                        memberUser.getProfilePicture()
                                );
                            })
                            .toList();

            // 4. Count unread messages
            long unreadCount =
                    messageRepository.countUnreadMessages(
                            conversation.getId(),
                            user.getId()
                    );

            // 5. Return DTO
            return new ConversationResponseDTO(
                    conversation.getId(),
                    conversation.getCreatedAt(),
                    userIds,
                    conversation.getConversationType(),
                    unreadCount,
                    conversation.getName(),
                    members
            );

        }).toList();
    }


    public PaginatedAPIResponse<MessageResponseDTO> searchMessages(Long conversationId, String search, BasePaginationParams paginationParams) {

        MessageFilterDTO filterDTO = new MessageFilterDTO();

        filterDTO.setConversationId(conversationId);
        filterDTO.setSmartSearch(search);

        Specification<Message> specification = MessageSpecification.getSpecification(filterDTO);

        Sort sort = "desc".equalsIgnoreCase(paginationParams.getSortDirection()) ? Sort.by(Sort.Direction.DESC, paginationParams.getSortBy()) : Sort.by(Sort.Direction.ASC, paginationParams.getSortBy());

        Pageable pageable = PageRequest.of(paginationParams.getPage(), paginationParams.getPageSize(), sort);

        Page<Message> messagePage = messageRepository.findAll(specification, pageable);

        List<MessageResponseDTO> dtoList = messagePage.getContent().stream().map(message -> new MessageResponseDTO(

                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getSentAt(),
                message.getFileUrl(),
                message.getFileName(),
                message.getFileType(),
                message.getFileSize(),
                message.isMessageDelivered(),
                message.isMessageRead(),
                message.getMessageDeliveredAt(),
                message.getMessageReadAt()

        )).toList();

        return PaginationHelper.createPaginationResponse(
                dtoList,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalPages(),
                messagePage.getTotalElements(),
                messagePage.isLast(),
                dtoList.isEmpty() ? "No messages found!" : "Messages fetched successfully!"

        );
    }

    public MessageResponseDTO sendFile(Long conversationId, MultipartFile file, String username) {


        // VALIDATE FILE
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select a file.");
        }

        // FIND CONVERSATION
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found."));

        // FIND SENDER
        User sender = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found."));

        // CREATE UPLOAD DIRECTORY
        Path uploadPath = Paths.get("uploads/chat-files");

        try {
            Files.createDirectories(uploadPath);

            // ORIGINAL FILE NAME
            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                originalFileName = "file";
            }


            // CREATE UNIQUE FILE NAME
            String fileName = UUID.randomUUID() + "_" + originalFileName;

            Path filePath = uploadPath.resolve(fileName);

            // SAVE FILE
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);


            // CREATE MESSAGE
            Message message = new Message();

            message.setConversation(conversation);
            message.setSender(sender);
            // File message doesn't need text content
            message.setContent(null);
            message.setFileUrl("/uploads/chat-files/" + fileName);
            message.setFileName(originalFileName);
            message.setFileType(file.getContentType());
            message.setFileSize(file.getSize());
            message.setSentAt(LocalDateTime.now());
            message.setMessageDelivered(false);
            message.setMessageRead(false);
            message.setMessageDeliveredAt(null);
            message.setMessageReadAt(null);

            // SAVE MESSAGE
            Message savedMessage = messageRepository.save(message);

            // CREATE RESPONSE
            MessageResponseDTO response = new MessageResponseDTO(
                    savedMessage.getId(),
                    conversation.getId(),
                    sender.getId(),
                    sender.getUsername(),
                    savedMessage.getContent(),
                    savedMessage.getSentAt(),
                    savedMessage.getFileUrl(),
                    savedMessage.getFileName(),
                    savedMessage.getFileType(),
                    savedMessage.getFileSize(),
                    savedMessage.isMessageDelivered(),
                    savedMessage.isMessageRead(),
                    savedMessage.getMessageDeliveredAt(),
                    savedMessage.getMessageReadAt()
            );

            // BROADCAST TO WEBSOCKET
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + conversationId,
                    response
            );

            // RETURN TO REST CLIENT
            return response;

        } catch (IOException e) {

            throw new RuntimeException("Failed to save file.", e);
        }
    }

    public void markMessageDelivered(
            Long messageId,
            String username) {

        Message message =
                messageRepository.findById(messageId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Message not found."
                                ));

        User receiver = userRepository.findByUsername(username).orElseThrow(() ->
                                new RuntimeException("User not found."));

        // Don't mark your own message as delivered
        if (message.getSender().getId()
                .equals(receiver.getId())) {
            return;
        }

        // Already delivered
        if (message.isMessageDelivered()) {
            return;
        }

        message.setMessageDelivered(true);
        message.setMessageDeliveredAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        MessageResponseDTO response =
                new MessageResponseDTO(
                        savedMessage.getId(),
                        savedMessage.getConversation().getId(),
                        savedMessage.getSender().getId(),
                        savedMessage.getSender().getUsername(),
                        savedMessage.getContent(),
                        savedMessage.getSentAt(),
                        savedMessage.getFileUrl(),
                        savedMessage.getFileName(),
                        savedMessage.getFileType(),
                        savedMessage.getFileSize(),
                        savedMessage.isMessageDelivered(),
                        savedMessage.isMessageRead(),
                        savedMessage.getMessageDeliveredAt(),
                        savedMessage.getMessageReadAt()
                );

        messagingTemplate.convertAndSend(
                "/topic/conversations/"
                        + savedMessage.getConversation().getId(),
                response
        );
    }

    @Transactional
    public void markConversationMessagesAsRead(Long conversationId, String username) {

        User receiver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));

        List<Message> messages = messageRepository
                        .findByConversationIdAndSenderIdNotAndMessageReadFalse(conversationId, receiver.getId());

        if (messages.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (Message message : messages) {
            message.setMessageRead(true);
            message.setMessageReadAt(now);
        }

        messageRepository.saveAll(messages);

        for (Message savedMessage : messages) {

            MessageResponseDTO response = new MessageResponseDTO(
                    savedMessage.getId(),
                    savedMessage.getConversation().getId(),
                    savedMessage.getSender().getId(),
                    savedMessage.getSender().getUsername(),
                    savedMessage.getContent(),
                    savedMessage.getSentAt(),
                    savedMessage.getFileUrl(),
                    savedMessage.getFileName(),
                    savedMessage.getFileType(),
                    savedMessage.getFileSize(),
                    savedMessage.isMessageDelivered(),
                    savedMessage.isMessageRead(),
                    savedMessage.getMessageDeliveredAt(),
                    savedMessage.getMessageReadAt()
            );

            messagingTemplate.convertAndSend(
                    "/topic/conversations/"
                            + conversationId,
                    response
            );
        }
    }

    @Transactional
    public ConversationResponseDTO updateGroupMembers(
            Long conversationId,
            GroupMemberUpdateRequestDTO request,
            String username
    ) {

        User currentUser = getAuthenticatedUser(username);

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new RuntimeException("Conversation not found"));

        // Must be a group
        if (conversation.getConversationType() != ConversationType.GROUP) {
            throw new RuntimeException("Members can only be changed for groups");
        }

        // Current user must already belong to group
        ConversationMember currentMember =
                conversationMemberRepository
                        .findByConversationId(conversationId)
                        .stream()
                        .filter(member ->
                                member.getUser().getId()
                                        .equals(currentUser.getId()))
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this group"
                                ));

        // =========================================
        // REMOVE MEMBERS
        // =========================================

        if (request.getRemoveUserIds() != null) {

            List<ConversationMember> members =
                    conversationMemberRepository
                            .findByConversationId(conversationId);

            members.stream()
                    .filter(member ->
                            request.getRemoveUserIds()
                                    .contains(member.getUser().getId()))
                    .forEach(conversationMemberRepository::delete);
        }

        // =========================================
        // ADD MEMBERS
        // =========================================

        if (request.getAddUserIds() != null) {

            List<ConversationMember> existingMembers =
                    conversationMemberRepository
                            .findByConversationId(conversationId);

            List<Long> existingUserIds =
                    existingMembers.stream()
                            .map(member ->
                                    member.getUser().getId())
                            .toList();

            for (Long userId : request.getAddUserIds()) {

                if (existingUserIds.contains(userId)) {
                    continue;
                }

                User user =
                        userRepository.findById(userId)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "User not found: " + userId
                                        ));

                ConversationMember newMember =
                        new ConversationMember();

                newMember.setConversation(conversation);
                newMember.setUser(user);

                conversationMemberRepository.save(newMember);
            }
        }

        // =========================================
        // RETURN UPDATED GROUP
        // =========================================

        List<ConversationMember> updatedMembers =
                conversationMemberRepository
                        .findByConversationId(conversationId);

        List<Long> userIds =
                updatedMembers.stream()
                        .map(member ->
                                member.getUser().getId())
                        .toList();

        List<ConversationMemberDTO> members =
                updatedMembers.stream()
                        .map(member -> {

                            User memberUser = member.getUser();

                            return new ConversationMemberDTO(
                                    memberUser.getId(),
                                    memberUser.getUsername(),
                                    memberUser.getProfilePicture()
                            );
                        })
                        .toList();

        long unreadCount =
                messageRepository.countUnreadMessages(
                        conversationId,
                        currentUser.getId()
                );

        return new ConversationResponseDTO(
                conversation.getId(),
                conversation.getCreatedAt(),
                userIds,
                conversation.getConversationType(),
                unreadCount,
                conversation.getName(),
                members
        );
    }
    @Transactional
    public void leaveGroup(Long conversationId, String username) {

        User currentUser = getAuthenticatedUser(username);

        Conversation conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (conversation.getConversationType() != ConversationType.GROUP) {
            throw new RuntimeException("This is not a group");
        }

        ConversationMember member = conversationMemberRepository.findByConversationId(conversationId)
                        .stream()
                        .filter(m ->
                                m.getUser().getId().equals(currentUser.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        conversationMemberRepository.delete(member);
    }
}