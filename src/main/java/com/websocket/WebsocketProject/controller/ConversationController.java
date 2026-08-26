package com.websocket.WebsocketProject.controller;

import com.websocket.WebsocketProject.dto.ConversationResponseDTO;
import com.websocket.WebsocketProject.dto.CreateConversationRequestDTO;
import com.websocket.WebsocketProject.dto.MessageResponseDTO;
import com.websocket.WebsocketProject.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}