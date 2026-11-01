# Spring AI Chatbot

A locally running AI chatbot built with **Java, Spring Boot, Spring AI, and Ollama**. This project integrates a locally hosted Large Language Model (LLM) with a Spring Boot REST API, without relying on paid AI APIs.

## Tech Stack

- Java 21
- Spring Boot 4.1.2
- Spring AI 2.0.2
- Ollama
- Llama 3.2 (3B)
- Maven
- Postman

## Architecture

```text
Postman
   |
   v
ChatController
   |
   v
ChatService
   |
   v
Spring AI ChatClient
   |
   v
Ollama
   |
   v
Llama 3.2 (3B)
```

## Prerequisites

Make sure you have installed:

- Java 21 or later
- Maven (or use the included Maven Wrapper)
- Ollama
- Postman (for API testing)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/NabinStha2/spring-ai-chatbot.git
cd spring-ai-chatbot
```

### 2. Download the Ollama model

```bash
ollama pull llama3.2:3b
```

### 3. Start Ollama

```bash
ollama serve
```

If Ollama is already running in the background, you can skip this command.

### 4. Run the Spring Boot application

```bash
./mvnw spring-boot:run
```

On startup, the application reads its generated OpenAPI document and updates
the local Postman collection. Install the Postman CLI before starting the app.
In Postman, select the `Local` environment from `postman/environments/Local.environment.yaml`.

The application starts at:

`http://localhost:8080`

## API Endpoint

### Chat with AI

| Property | Value |
|---|---|
| Method | POST |
| Endpoint | `/api/v1/chat` |
| Content-Type | `application/json` |

**Request:**

```http
POST http://localhost:8080/api/v1/chat
Content-Type: application/json
Accept: application/json
```

Request body:

```json
{
  "conversationId": "chat-001",
  "message": "Hello! Please introduce yourself."
}
```

**Response:**

```json
{
  "response": "Hello! I'm an AI assistant. How can I help?"
}
```

## Project Structure

```text
src/
├── main/
│   ├── java/com/nabin/spring_ai_chatbot/
│   │   ├── controller/
│   │   │   └── ChatController.java
│   │   ├── service/
│   │   │   └── ChatService.java
│   │   └── SpringAiChatbotApplication.java
│   │
│   └── resources/
│       └── application.yaml
│
└── test/
```

## Configuration

The Ollama connection and model are configured in `application.yaml`:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2:3b
```

## Project Goals

This project is being developed incrementally to explore:

- Local LLM integration using Ollama
- Spring AI and ChatClient
- REST API development
- Conversation memory
- Streaming AI responses
- Embeddings and vector databases
- Retrieval-Augmented Generation (RAG)
- AI tool calling

## License

This project is open-source and available for learning and educational purposes.
