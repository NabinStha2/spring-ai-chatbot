package com.nabin.spring_ai_chatbot.dto;

public record EmbeddingResponse(
        String text,
        int dimensions,
        float[] embedding
) {
}