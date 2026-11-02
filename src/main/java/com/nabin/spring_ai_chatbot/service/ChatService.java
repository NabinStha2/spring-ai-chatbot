package com.nabin.spring_ai_chatbot.service;

import com.nabin.spring_ai_chatbot.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
                       VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                                       You are a professional programming assistant.
                                       
                                       MARKDOWN FORMATTING RULES:
                                       1. Use ## for main section headings when appropriate.
                                       2. Use ### for subsections when appropriate.
                                       3. Use - for bullet lists.
                                       4. Use numbered lists for sequential steps.
                                       5. Use fenced code blocks only for actual source code.
                                       6. Never wrap ordinary Markdown text, headings, tables or lists inside code blocks.
                                       7. Use the correct language identifier for code blocks, such as ```java.
                                       8. Keep Markdown syntax valid and properly closed.
                                       9. Use **bold** to highlight important concepts.
                                       10. Avoid unnecessary headings for short answers.
                                       
                                       RESPONSE RULES:
                                       1. Explain programming concepts in simple, beginner-friendly language.
                                       2. Focus on Java, Spring Boot, and software development.
                                       3. Provide practical examples whenever useful.
                                       4. Explain technical terms when they are introduced.
                                       5. Keep answers clear, concise, and well-structured.
                                       6. Avoid repeating information.
                                       7. If you are unsure about something, say so honestly.
                                       8. Do not invent APIs, methods, or technical information.
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
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest
                        .builder()
                        .query(message)
                        .topK(3)
                        .similarityThreshold(0.6)
                        .build()
                                                               );

        if (documents.isEmpty()) {
            return new ChatResponse(
                    "I couldn't find relevant information in the uploaded documents to answer the question."
            );
        }

        String context = documents
                .stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        return new ChatResponse(chatClient
                                        .prompt()
                                        .system(system -> system
                                                .text("""
                                                              Answer the user's question using the following document context.
                                                              
                                                              DOCUMENT CONTEXT:
                                                              {context}
                                                              
                                                              RULES:
                                                              1. Answer only using information explicitly stated in the document context.
                                                              2. Do not use your pretrained knowledge to add missing information.
                                                              3. Do not introduce technical details, examples, explanations, or assumptions absent from the context.
                                                              4. If the context only partially answers the question, explain only what is supported by the context.
                                                              5. If the answer is not available in the context, clearly say so.
                                                              6. Never invent information or claim that something comes from the documents when it does not.
                                                              7. Use clear and beginner-friendly Markdown.
                                                              """)
                                                .param("context", context))
                                        .user(message)
                                        .advisors(advisor -> advisor.param(
                                                ChatMemory.CONVERSATION_ID, conversationId))
                                        .call()
                                        .content());
    }

}
