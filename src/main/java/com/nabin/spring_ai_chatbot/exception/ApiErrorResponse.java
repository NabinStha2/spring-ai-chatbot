package com.nabin.spring_ai_chatbot.exception;

import java.util.List;

public record ApiErrorResponse(String errorCode, String message, int status,
                               List<FieldError> validationErrors) {

    public record FieldError(String field, String message) {
    }

}
