package com.websocket.WebsocketProject.controller;

import com.websocket.WebsocketProject.dto.*;
import com.websocket.WebsocketProject.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable Long conversationId, Principal principal) {

        List<MessageResponseDTO> response = conversationService.getMessages(conversationId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ConversationResponseDTO> createConversion(@RequestBody CreateConversationRequestDTO requestDTO){
        ConversationResponseDTO conversation = conversationService.createConversation(requestDTO);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/get-conversation")
    public ResponseEntity<List<ConversationResponseDTO>> getMyConversations(Principal principal) {

        List<ConversationResponseDTO> response =
                conversationService.getMyConversations(principal.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{conversationId}/messages/search")
    public ResponseEntity<PaginatedAPIResponse<MessageResponseDTO>> searchMessages(
            @PathVariable Long conversationId, @RequestParam String search, BasePaginationParams paginationParams, Principal principal) {

        PaginatedAPIResponse<MessageResponseDTO> response = conversationService.searchMessages(conversationId, search, paginationParams);

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/{conversationId}/messages/file",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<MessageResponseDTO> sendFile(
            @PathVariable Long conversationId, @RequestParam("file") MultipartFile file,
            Principal principal) {

        MessageResponseDTO response = conversationService.sendFile(
                        conversationId, file, principal.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{conversationId}/members")
    public ResponseEntity<ConversationResponseDTO> updateGroupMembers(
            @PathVariable Long conversationId, @RequestBody GroupMemberUpdateRequestDTO request, Principal principal
    ) {

        ConversationResponseDTO response =
                conversationService.updateGroupMembers(conversationId, request, principal.getName());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{conversationId}/members/me")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long conversationId, Principal principal) {

        conversationService.leaveGroup(conversationId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}