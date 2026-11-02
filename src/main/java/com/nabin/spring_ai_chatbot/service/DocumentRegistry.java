package com.nabin.spring_ai_chatbot.service;

import com.nabin.spring_ai_chatbot.dto.DocumentInfoResponse;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DocumentRegistry {

    private final Map<String, DocumentInfoResponse> documents =
            new ConcurrentHashMap<>();

    public void add(DocumentInfoResponse document) {
        documents.put(document.documentId(), document);
    }

    public Collection<DocumentInfoResponse> findAll() {
        return documents.values();
    }

    public void delete(String documentId) {
        documents.remove(documentId);
    }

    public boolean exists(String documentId) {
        return documents.containsKey(documentId);
    }
}