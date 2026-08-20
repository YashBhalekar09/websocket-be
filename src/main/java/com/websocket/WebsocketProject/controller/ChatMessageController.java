package com.websocket.WebsocketProject.controller;

import com.websocket.WebsocketProject.entity.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatMessageController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        System.out.println("Sender  : " + chatMessage.getSender());
        System.out.println("Content : " + chatMessage.getContent());
        return chatMessage;
    }
}
