package com.nabin.spring_ai_chatbot.service;

import com.nabin.spring_ai_chatbot.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ChatResponse chat(String message) {
        return new ChatResponse(chatClient
                                        .prompt()
                                        .user(message)
                                        .call()
                                        .content());
    }

}
