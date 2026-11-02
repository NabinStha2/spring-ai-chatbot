package com.nabin.spring_ai_chatbot.dto;

public record DocumentInfoResponse(
        String documentId,
        String source,
        String uploadedAt,
        int totalChunks
) {
}