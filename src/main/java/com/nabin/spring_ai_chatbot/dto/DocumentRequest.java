package com.nabin.spring_ai_chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentRequest(

        @NotBlank(message = "Document content cannot be empty")
        @Size(max = 10000,
                message = "Document content cannot exceed 10000 characters")
        String content

) {
}