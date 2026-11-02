package com.nabin.spring_ai_chatbot.dto;

import jakarta.validation.constraints.NotBlank;

public record EmbeddingRequest(
        @NotBlank(message = "Text cannot be empty")
        String text
) {
}