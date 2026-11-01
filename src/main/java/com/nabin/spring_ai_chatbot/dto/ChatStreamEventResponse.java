package com.nabin.spring_ai_chatbot.dto;

public record ChatStreamEventResponse(
        String type,
        String conversationId,
        String content
) {
}