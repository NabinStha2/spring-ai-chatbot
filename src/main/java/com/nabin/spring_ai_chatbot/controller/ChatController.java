package com.nabin.spring_ai_chatbot.controller;

import com.nabin.spring_ai_chatbot.dto.ChatRequest;
import com.nabin.spring_ai_chatbot.dto.ChatResponse;
import com.nabin.spring_ai_chatbot.dto.ChatStreamEventResponse;
import com.nabin.spring_ai_chatbot.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(
            @Valid
            @RequestBody
            ChatRequest chatRequest) {
        return chatService.chat(chatRequest.conversationId(), chatRequest.message());
    }

    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<ChatStreamEventResponse>> chatStream(
            @RequestParam
            String conversationId,
            @RequestParam
            String message
                                                                    ) {

        Flux<ServerSentEvent<ChatStreamEventResponse>> startEvent =
                Flux.just(
                        ServerSentEvent
                                .<ChatStreamEventResponse>builder()
                                .event("start")
                                .data(new ChatStreamEventResponse(
                                        "start",
                                        conversationId,
                                        null
                                ))
                                .build()
                         );

        Flux<ServerSentEvent<ChatStreamEventResponse>> messageEvents =
                chatService
                        .chatStream(conversationId, message)
                        .bufferTimeout(100, Duration.ofMillis(200))
                        .filter(chunks -> !chunks.isEmpty())
                        .map(chunks ->
                             {
                                 StringBuilder contentBuilder = new StringBuilder();
                                 for (String chunk : chunks) {
                                     contentBuilder.append(chunk);
                                 }
                                 return ServerSentEvent
                                         .<ChatStreamEventResponse>builder()
                                         .event("message")
                                         .data(new ChatStreamEventResponse(
                                                 "message",
                                                 conversationId,
                                                 contentBuilder.toString()
                                         ))
                                         .build();
                             }
                            );

        Flux<ServerSentEvent<ChatStreamEventResponse>> doneEvent =
                Flux.just(
                        ServerSentEvent
                                .<ChatStreamEventResponse>builder()
                                .event("done")
                                .data(new ChatStreamEventResponse(
                                        "done",
                                        conversationId,
                                        null
                                ))
                                .build()
                         );

        return Flux.concat(startEvent, messageEvents, doneEvent);
    }
}
