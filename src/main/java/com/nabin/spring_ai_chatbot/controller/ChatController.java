package com.nabin.spring_ai_chatbot.controller;

import com.nabin.spring_ai_chatbot.dto.ChatRequest;
import com.nabin.spring_ai_chatbot.dto.ChatResponse;
import com.nabin.spring_ai_chatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(
            @Valid
            @RequestBody
            ChatRequest chatRequest) {
        return chatService.chat(chatRequest.message());
    }
}
