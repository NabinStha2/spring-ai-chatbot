package com.nabin.spring_ai_chatbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "postman.sync.enabled",
        havingValue = "true")
public class PostmanCollectionSync {
    private static final Logger logger = LoggerFactory.getLogger(PostmanCollectionSync.class);

    private final RestClient restClient;

    @Value("${postman.sync.openapi-url}")
    private String openApiUrl;

    @Value("${postman.sync.spec-file}")
    private String specFile;

    @Value("${postman.sync.collection-name}")
    private String collectionName;

    public PostmanCollectionSync(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void updateCollection() {
        try {
            String spec = restClient
                    .get()
                    .uri(openApiUrl)
                    .retrieve()
                    .body(String.class);
            if (spec == null || spec.isBlank()) {
                logger.warn("OpenAPI endpoint returned an empty document; skipping Postman sync");
                return;
            }

            Path specPath = Path.of(specFile);
            if (specPath.getParent() != null) {
                Files.createDirectories(specPath.getParent());
            }
            Files.writeString(specPath, spec);

            Process process = new ProcessBuilder(
                    "postman", "spec", "generate", "collection",
                    specPath.toString(), "--name", collectionName, "--force")
                    .inheritIO()
                    .start();

            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                logger.warn("Postman CLI timed out; skipping");
                return;
            }
            if (process.exitValue() != 0) {
                logger.warn("Postman CLI exited with code {}", process.exitValue());
                return;
            }

            Process lint = new ProcessBuilder(
                    "postman", "collection", "lint",
                    "postman/collections/" + collectionName)
                    .inheritIO()
                    .start();
            if (!lint.waitFor(60, TimeUnit.SECONDS) || lint.exitValue() != 0) {
                logger.warn("Generated Postman collection did not pass lint");
                return;
            }
            logger.info("Updated Postman collection '{}'", collectionName);
        } catch (InterruptedException ex) {
            Thread
                    .currentThread()
                    .interrupt();
        } catch (Exception ex) {
            logger.warn("Postman sync failed (app keeps running): {}", ex.getMessage());
        }
    }
}
