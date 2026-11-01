package com.nabin.spring_ai_chatbot.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record ChatRequest(
        @Schema(description = "Identifier for the conversation", example = "chat-001")
        @NotBlank(message = "Conversation ID must not be blank")
        String conversationId,
        @Schema(description = "The user's message", example = "Hello! Please introduce yourself.")
        @NotBlank(message = "Message must not be blank")
        @Size(max = 2000,
                message = "Message must not exceed 2000 characters")
        String message) {

}
