package com.nabin.spring_ai_chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The assistant's reply")
public record ChatResponse(
        @Schema(description = "Generated assistant response",
                example = "Hello! I'm an AI assistant. How can I help?")
        String response) {
}
