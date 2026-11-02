package com.nabin.spring_ai_chatbot.controller;

import com.nabin.spring_ai_chatbot.dto.EmbeddingRequest;
import com.nabin.spring_ai_chatbot.dto.EmbeddingResponse;
import com.nabin.spring_ai_chatbot.service.EmbeddingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping
    public EmbeddingResponse generateEmbedding(
            @Valid
            @RequestBody
            EmbeddingRequest request
                                              ) {
        float[] embedding = embeddingService.generateEmbedding(request.text());

        return new EmbeddingResponse(
                request.text(),
                embedding.length,
                embedding
        );
    }

    @PostMapping("/calculate-cosine-similarity")
    public double calculateCosineSimilarity(
            @RequestParam
            String textA,
            @RequestParam
            String textB
                                           ) {
        return embeddingService.compareTexts(textA, textB);
    }
}