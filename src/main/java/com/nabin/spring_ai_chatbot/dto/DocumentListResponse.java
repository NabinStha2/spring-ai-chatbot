package com.nabin.spring_ai_chatbot.dto;

import java.util.List;

public record DocumentListResponse(
        List<DocumentInfoResponse> documents,
        int currentPage,
        int pageSize,
        long totalDocuments,
        int totalPages
) {
}