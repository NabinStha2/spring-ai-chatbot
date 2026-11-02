package com.nabin.spring_ai_chatbot.controller;

import com.nabin.spring_ai_chatbot.dto.DocumentInfoResponse;
import com.nabin.spring_ai_chatbot.dto.DocumentListResponse;
import com.nabin.spring_ai_chatbot.dto.DocumentRequest;
import com.nabin.spring_ai_chatbot.dto.DocumentSearchResponse;
import com.nabin.spring_ai_chatbot.service.DocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@Validated
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentInfoResponse> addDocument(
            @Valid
            @RequestBody
            DocumentRequest request
                                                           ) {
        return ResponseEntity.ok(documentService.addDocument(request.content()));
    }

    @GetMapping("/search")
    public List<DocumentSearchResponse> searchDocuments(
            @RequestParam
            String query,
            @RequestParam(defaultValue = "3")
            @Min(value = 1,
                    message = "topK must be at least 1")
            @Max(value = 10,
                    message = "topK cannot exceed 10")
            int topK
                                                       ) {
        return documentService
                .searchDocuments(query, topK)
                .stream()
                .map(document -> new DocumentSearchResponse(
                        document.getText(),
                        document.getMetadata()
                ))
                .toList();
    }

    @GetMapping("/{documentId}/search")
    public List<DocumentSearchResponse> searchDocumentsById(
            @PathVariable
            String documentId,
            @RequestParam
            String query,
            @RequestParam(defaultValue = "3")
            @Min(1)
            @Max(10)
            int topK
                                                           ) {
        return documentService
                .searchDocumentsById(
                        query, documentId, topK
                                    )
                .stream()
                .map(document -> new DocumentSearchResponse(
                        document.getText(),
                        document.getMetadata()
                ))
                .toList();
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @PathVariable
            String documentId
                                                             ) {
        documentService.deleteDocument(documentId);

        return ResponseEntity.ok(
                Map.of("message", "Document deleted successfully")
                                );
    }

    @GetMapping("/all")
    public Collection<DocumentInfoResponse> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping
    public DocumentListResponse getDocuments(
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,
            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(50)
            int size
                                            ) {
        return documentService.getDocuments(page, size);
    }
}
