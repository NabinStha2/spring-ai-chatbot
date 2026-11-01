package com.nabin.spring_ai_chatbot.service;

import com.nabin.spring_ai_chatbot.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                                       You are a professional programming assistant.
                                       
                                       RESPONSE FORMATTING RULES:
                                       1. Use clear Markdown formatting.
                                       2. Use headings (##) to organise explanations.
                                       3. Use bullet points for lists.
                                       4. Use numbered lists for step-by-step instructions.
                                       5. Put all code inside properly labelled code blocks.
                                       6. Use **bold** for important concepts.
                                       7. Avoid unnecessarily long paragraphs.
                                       8. Include practical examples when useful.
                                       9. Avoid repeating the same information.
                                       10. Keep explanations clear and beginner-friendly.
                                       
                                       Follow these rules:
                                       1. Explain programming concepts in simple, beginner-friendly language.
                                       2. Focus on Java, Spring Boot, and software development.
                                       3. Provide practical examples whenever useful.
                                       4. Explain technical terms when they are introduced.
                                       5. Keep answers clear and well-structured.
                                       6. If you are unsure about something, say so honestly.
                                       """)
                .defaultAdvisors(MessageChatMemoryAdvisor
                                         .builder(chatMemory)
                                         .build())
                .build();
    }

    public Flux<String> chatStream(String conversationId, String message) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    public ChatResponse chat(String conversationId, String message) {
        return new ChatResponse(chatClient
                                        .prompt()
                                        .user(message)
                                        .advisors(advisor -> advisor.param(
                                                ChatMemory.CONVERSATION_ID, conversationId))
                                        .call()
                                        .content());
    }

}
