package com.nabin.spring_ai_chatbot.service;

import com.nabin.spring_ai_chatbot.dto.DocumentInfoResponse;
import com.nabin.spring_ai_chatbot.dto.DocumentListResponse;
import com.nabin.spring_ai_chatbot.exception.ResourceNotFoundException;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;
    private final DocumentRegistry documentRegistry;

    public DocumentService(VectorStore vectorStore, TokenTextSplitter tokenTextSplitter,
                           DocumentRegistry documentRegistry) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
        this.documentRegistry = documentRegistry;
    }

    public DocumentInfoResponse addDocument(String content) {

        Document document = new Document(
                content,
                Map.of("source", "manual", "uploaded_at", Instant
                        .now()
                        .toString())
        );

        List<Document> chunks = tokenTextSplitter.split(document);

        System.out.println("Total chunks created: " + chunks.size());

        vectorStore.add(chunks);

        String documentId = (String) chunks
                .getFirst()
                .getMetadata()
                .get("parent_document_id");

        String source = (String) chunks
                .getFirst()
                .getMetadata()
                .get("source");

        String uploadedAt = (String) chunks
                .getFirst()
                .getMetadata()
                .get("uploaded_at");

        DocumentInfoResponse documentInfo = new DocumentInfoResponse(
                documentId,
                source,
                uploadedAt,
                chunks.size()
        );

        documentRegistry.add(documentInfo);

        return documentInfo;
    }

    public List<Document> searchDocuments(String query, int topK) {

        SearchRequest searchRequest = SearchRequest
                .builder()
                .query(query)
                .topK(topK)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    public List<Document> searchDocumentsById(String query, String documentId, int topK) {
        if (!documentRegistry.exists(documentId)) {
            throw new ResourceNotFoundException(
                    "Document not found: " + documentId
            );
        }

        SearchRequest searchRequest = SearchRequest
                .builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.6)
                .filterExpression(
                        "parent_document_id == '" + documentId + "'"
                                 )
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    public void deleteDocument(String documentId) {

        vectorStore.delete(
                "parent_document_id == '" + documentId + "'"
                          );

        documentRegistry.delete(documentId);
    }

    public Collection<DocumentInfoResponse> getAllDocuments() {
        return documentRegistry.findAll();
    }

    public DocumentListResponse getDocuments(int page, int size) {
        List<DocumentInfoResponse> allDocuments =
                new ArrayList<>(documentRegistry.findAll());

        long totalDocuments = allDocuments.size();

        int totalPages = (int) Math.ceil(
                (double) totalDocuments / size
                                        );

        List<DocumentInfoResponse> documents = allDocuments
                .stream()
                .skip((long) page * size)
                .limit(size)
                .toList();

        return new DocumentListResponse(
                documents,
                page,
                size,
                totalDocuments,
                totalPages
        );
    }

//    public void deleteDocument(String documentId) {
//
//        List<Document> documents = vectorStore.similaritySearch(
//                SearchRequest
//                        .builder()
//                        .query(" ")
//                        .topK(1000)
//                        .similarityThresholdAll()
//                        .build()
//                                                               );
//
//        List<String> ids = documents
//                .stream()
//                .filter(document -> documentId.equals(
//                        document
//                                .getMetadata()
//                                .get("parent_document_id")
//                                                     ))
//                .map(Document::getId)
//                .toList();
//
//        if (!ids.isEmpty()) {
//            vectorStore.delete(ids);
//        }
//    }
}