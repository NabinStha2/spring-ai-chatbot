package com.nabin.spring_ai_chatbot.dto;

import java.util.Map;

public record DocumentSearchResponse(
        String content,
        Map<String, Object> metadata
) {
}