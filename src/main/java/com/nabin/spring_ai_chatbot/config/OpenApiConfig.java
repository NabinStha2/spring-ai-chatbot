package com.nabin.spring_ai_chatbot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Spring AI Chatbot API", version = "v1",
                description = "Local chat API powered by Spring AI and Ollama."),
        servers = @Server(url = "http://localhost:8080")
)
public class OpenApiConfig {
}
