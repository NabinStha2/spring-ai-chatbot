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
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;
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

    @Value("${postman.sync.collection-path}")
    private String collectionPath;

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

            Path targetCollection = Path.of(collectionPath).normalize();
            Path collectionsRoot = Path.of("postman/collections").toAbsolutePath().normalize();
            Path absoluteTarget = targetCollection.toAbsolutePath().normalize();
            if (!absoluteTarget.startsWith(collectionsRoot) || absoluteTarget.equals(collectionsRoot)) {
                throw new IllegalArgumentException("Postman collection path must be inside postman/collections");
            }

            String stagingName = collectionName + " Sync " + UUID.randomUUID();
            Path stagingCollection = collectionsRoot.resolve(stagingName);
            Path backupCollection = collectionsRoot.resolve(collectionName + " Backup " + UUID.randomUUID());

            Process process = new ProcessBuilder(
                    "postman", "spec", "generate", "collection",
                    specPath.toString(), "--name", stagingName, "--force",
                    "--folder-strategy", "Tags")
                    .inheritIO().start();
            if (!waitForSuccess(process, "Postman collection generation")) {
                deleteTree(stagingCollection);
                return;
            }

            removeGeneratedExamples(stagingCollection);

            Process lint = new ProcessBuilder(
                    "postman", "collection", "lint", stagingCollection.toString())
                    .inheritIO().start();
            if (!waitForSuccess(lint, "Generated collection lint")) {
                deleteTree(stagingCollection);
                return;
            }

            replaceCollection(stagingCollection, absoluteTarget, backupCollection);
            logger.info("Updated Postman collection '{}'", collectionName);
        } catch (InterruptedException ex) {
            Thread
                    .currentThread()
                    .interrupt();
        } catch (Exception ex) {
            logger.warn("Postman sync failed (app keeps running): {}", ex.getMessage());
        }
    }

    private boolean waitForSuccess(Process process, String operation) throws InterruptedException {
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            logger.warn("{} timed out; existing collection was kept", operation);
            return false;
        }
        if (process.exitValue() != 0) {
            logger.warn("{} exited with code {}; existing collection was kept",
                    operation, process.exitValue());
            return false;
        }
        return true;
    }

    private void replaceCollection(Path staging, Path target, Path backup) throws Exception {
        boolean hadExistingCollection = Files.exists(target);
        if (hadExistingCollection) {
            Files.move(target, backup, StandardCopyOption.ATOMIC_MOVE);
        }
        try {
            Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception ex) {
            if (hadExistingCollection && Files.exists(backup)) {
                Files.move(backup, target, StandardCopyOption.ATOMIC_MOVE);
            }
            throw ex;
        }
        if (hadExistingCollection) {
            deleteTree(backup);
        }
    }

    private void deleteTree(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private void removeGeneratedExamples(Path collection) throws Exception {
        try (var paths = Files.walk(collection)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                if (Files.isRegularFile(path)
                        && path.getFileName().toString().endsWith(".example.yaml")) {
                    Files.deleteIfExists(path);
                } else if (Files.isRegularFile(path)
                        && path.getFileName().toString().endsWith(".request.yaml")) {
                    String request = Files.readString(path);
                    String withoutExamples = request.replaceAll("(?m)^examples:.*(?:\\R|$)", "");
                    if (!request.equals(withoutExamples)) {
                        Files.writeString(path, withoutExamples);
                    }
                } else if (Files.isDirectory(path) && !path.equals(collection)) {
                    try (var children = Files.list(path)) {
                        if (children.findAny().isEmpty()) {
                            Files.deleteIfExists(path);
                        }
                    }
                }
            }
        }
    }
}
